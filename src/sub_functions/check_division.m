% Disclaimer: IMPORTANT: This software was developed at the National
% Institute of Standards and Technology by employees of the Federal
% Government in the course of their official duties. Pursuant to
% title 17 Section 105 of the United States Code this software is not
% subject to copyright protection and is in the public domain. This
% is an experimental system. NIST assumes no responsibility
% whatsoever for its use by other parties, and makes no guarantees,
% expressed or implied, about its quality, reliability, or any other
% characteristic. We would appreciate acknowledgement if the software
% is used. This software can be redistributed and/or modified freely
% provided that any derivative works bear some notice that they are
% derived from it, and any modified versions bear some notice that
% they have been modified.


% 
%  This is a sub-function of the main function "start_tracking.m".
% 
% Function to check the division vectors for two consecutive frames.
% This fucntion will update and return the track_vector for two consecutive frames after accounting for division.
% It will also return the frame_division vector. This vector has the same number of lines as the number of target
% cells and each element will contain the number of the source cell that divided into the target cells,
% 0 otherwise. For example if frame_division(5) = 3, this means that source cell number 3 has divided into cell
% number 5 and some other cell. This also implies that in the frame_division vector there is only one more line
% that has number 3. This is coming from the assumption that a cell mother can only divide into two daughters
% cells. if frame_division(2) = 0, this means that target cell number 2 is not a daughter cell.
% 

function [frame_2, frame_division, frame_track_vector_updated, frame_cost, frame_overlap, number_cells_2, centroids_2, cells_size_2] = check_division(frame_2, ...
    frame_1, frame_track_vector, cells_size_1, cells_size_2, frame_overlap, frame_cost, number_cells_1, number_cells_2, centroids_1, centroids_2, division_overlap_threshold, ...
     daughter_size_similarity, daughter_aspect_ratio_similarity, enable_cell_mitosis_flag, weight_overlap, weight_centroids, weight_size, max_centroids_distance, ...
    cell_circularity, circularity_threshold, i_frame,renumbering_vectors_1, number_of_frames_check_circularity)
    

restrict_mitosis_to_2_daughters_flag = true;
                        
% Initialize the frame_division vector that contains the number of the target cells that result from a
% division of one source cell. For example if cell 2 divides into cells 4 and 6, then the resulting frame_division holds
% a value of 2 at the index locations 4 and 6
frame_division = zeros(number_cells_2, 1);
% this is a copy of the frame_track_vector that will be updated and modified within this function
frame_track_vector_updated = frame_track_vector;

% get the min value of the frame_cost by column
% this is used to see if more than one cell in frame_2 maps to a single cell in frame_1
% If that is the case, then that cell in frame_1 is a potential mother cell for division
[costs_found, frame_2_mapping] = min(frame_cost,[],1);
% remove any mappings that had a cost of NaN, as there was no mapping to a cell in frame_1
frame_2_mapping(isnan(costs_found)) = NaN;

% examine the mapping of the cells in frame_2 to the cells in frame_1
% A cell in frame_1 is considered a potential mother cell if more than one cell in frame_2 maps to it.
% the cell mapping is defined as the minium cost between 2 cells. For example the min cost in a row is a mapping from
% the frame_1 cell (i) to the index of the cell with the minimum cost in that row (j) 
% if a cell is a potential mother cell, append it to the list
potential_mother_cells = zeros(number_cells_1,1);
for i = 1:number_cells_1
    potential_mother_cells(i) = nnz(frame_2_mapping == i);
end
potential_mother_cells = find(potential_mother_cells > 1);

% if there are no potential mother cells to consider, then there are no divisions, so this function is done
if isempty(potential_mother_cells), return; end

% Compute Aspect ratio of frame_2 cells this is used to compare potential daughters
F =  regionprops(frame_2, 'MajorAxisLength', 'MinorAxisLength');
major_axes = [F.MajorAxisLength]';
minor_axes = [F.MinorAxisLength]';
aspect_ratio = major_axes./minor_axes;


% loop over the potential mother cells to record them into frame_division if they qualify
for i = 1:numel(potential_mother_cells)
    mother_cell = potential_mother_cells(i);
    
    % get the potential daughters from the frame_2_mapping list, the daughters are the cells from frame_2 that map to
    % the mother cell number
    potential_daughter_cells = find(frame_2_mapping == mother_cell);
    daughter_overlap_percentages = NaN(1,numel(potential_daughter_cells));
    
    % find the overlap percentage with the mother for each daughter
    for k = 1:numel(potential_daughter_cells)
        % Compute the overlap_percentage for the daughter cell(s) overlapping the mother cell (aka the overlap row-wise)
        % the overlap will be 1 if every pixel in the daughter cell overlaps a pixel in the mother cell
        daughter_overlap_percentages(k) = frame_overlap(mother_cell,potential_daughter_cells(k)) / cells_size_2(potential_daughter_cells(k));
    end
    % Find the overlaps that surpasses the threshold. The number of these cells are stock in the vector
    % "potential_daughter_cells". These cells are potential daughter cells of the current mother.
    valid_daughter_indx = daughter_overlap_percentages > division_overlap_threshold;
    
    % Delete the daughter cells that are tracked to other cells than the potential mother, if they overlap with the cells they are being tracked to.
    for k = 1:numel(potential_daughter_cells)
        % if daughter cell is not valid, continue
        if ~valid_daughter_indx(k), continue, end
        
        % find if the daughter cell is being tracked to other cell than the mother
        other_cells = find(frame_track_vector_updated == potential_daughter_cells(k));
        for j = 1:length(other_cells)
            if other_cells(j) ~= mother_cell && frame_overlap(other_cells(j),potential_daughter_cells(k))
                valid_daughter_indx(k) = 0;
            end
        end
    end
    
    % remove the potential daughters that do not overlap the mother cell enough
    potential_daughter_cells = potential_daughter_cells(valid_daughter_indx);
    
    % If the current mother cell has no potential daughters, do not affect the track
    if isempty(potential_daughter_cells)
        continue; % do not modify the track
    end
    % if the current mother has a single potential daughter, it is not a mitosis, do not affect the track
    if numel(potential_daughter_cells) == 1 
        continue;
    end
	
    % if there are more than 2 potential daughter cells and the user wants to restrict mitosis to 2 daughter cells
    % take the best 2 daughters by cost, aka 2 potential daughters with the lowest cost
    % do not limit the number of daughters if mitosis is disallowed
    if enable_cell_mitosis_flag && restrict_mitosis_to_2_daughters_flag && numel(potential_daughter_cells) > 2
        % find the best 2 potential daughters using frame_cost
        costs = frame_cost(mother_cell, potential_daughter_cells);
        [~, indx] = sort(costs, 'ascend');
        % keep only daughters 1 and 2
        potential_daughter_cells = potential_daughter_cells(indx);
        potential_daughter_cells = [potential_daughter_cells(1), potential_daughter_cells(2)];
    end

    % filter the daughters to ensure that they overlap the mother cell enough to be considered daughters
    % (overlap(mother, d1) + overlap(mother, d2)) / size(mother) 
    overlap_sum = 0;
    for k = 1:numel(potential_daughter_cells)
        overlap_sum = overlap_sum + frame_overlap(mother_cell,potential_daughter_cells(k));
    end
    if (overlap_sum/cells_size_1(mother_cell)) <= division_overlap_threshold
        % the combined set of daughters did not overlap the mother enough to be considered a mitosis
        % do not modify the tracking vector
        continue;
    end

	% the potential mitosis between the mother_cell from frame_1 and the potential_daughter_cells from frame_2
	% to be considered a valid mitosis a pair of daughters must have similar sizes and aspect ratios
	% valid mitosis events into a pair of daughters have size similarities and aspect ratio similarities greater than the threshold
	% the user controls the tolerance threshold using the variable daughter_size_similarity and aspect_ratio_similarity
	% if there are more than 2 potential daughter cells, each pair of daughter cells is tested for size and aspect ratio similarity
	% only the pairs that are above the thresholds get added to the frame_division matrix
	% The similarity between size and aspect ratio is computed with the following metric: 1-(abs(a1-a2) / (a1+a2) ). This metric is equal to 1 when
    % the two values a1 and a2 are identical and is equal to 0 when there is an extreme case of dissimlarity between a1 and a2. 
    % In this case a1 and a2 represent size or aspect ratio

    % if cell_circ_vec(i) is true, then within the last n frames the cell was above the circularity threshold
    % this means it has a potential to have been mitotic
    circ_vec = cell_circularity(renumbering_vectors_1(mother_cell), max(1, i_frame-number_of_frames_check_circularity):(i_frame-1));
    cell_circulatiry_check = any(circ_vec > circularity_threshold);
%     cell_circulatiry_check = sum(cell_circularity(renumbering_vectors_1(mother_cell), max(1, i_frame-number_of_frames_check_circularity):(i_frame-1)), 2) > 0;
    cell_circulatiry_check = cell_circulatiry_check || (i_frame < number_of_frames_check_circularity);
    % the (~enable_cell_mitosis_flag ||) will ignore whatever lies in cell_circ_vec if mitosis is disabled
    if ~enable_cell_mitosis_flag ||cell_circulatiry_check
        for k1 = 1:numel(potential_daughter_cells)
            for k2 = (k1+1):numel(potential_daughter_cells)
                d1 = potential_daughter_cells(k1);
                d2 = potential_daughter_cells(k2);
                % compute the size similarity
                size_similarity = 1 - (abs(cells_size_2(d1) - cells_size_2(d2)) / (cells_size_2(d1) + cells_size_2(d2)));
                % compute the aspect ratio similarity
                aspect_ratio_similarity = 1 - (abs(aspect_ratio(d1) - aspect_ratio(d2)) / (aspect_ratio(d1) + aspect_ratio(d2)));
                % if mitosis is disabled, or the size and aspect ratios are above the threshold add them to the division matrix
                % they are added to division matrix if mitosis is disabled to allow those cells to be merged into one label later
                if ~enable_cell_mitosis_flag || (size_similarity > daughter_size_similarity && aspect_ratio_similarity > daughter_aspect_ratio_similarity)
                    frame_division(d1) = mother_cell;
                    frame_division(d2) = mother_cell;
                    frame_track_vector_updated(mother_cell) = 0;
                    frame_track_vector_updated(frame_track_vector_updated == d1) = NaN; 
                    frame_track_vector_updated(frame_track_vector_updated == d2) = NaN;
                end
            end
        end
    end
end


% if cell mitosis is not enabled check to see if there are any nonzero entries in frame_division
% if there are those mitosis events need to be merged so they share a label
if ~enable_cell_mitosis_flag && any(frame_division)
	% find the mother cell number(s) from frame_division
    mother_cells = unique(nonzeros(frame_division));
    % initialize the renumbering vector which will not only merge the mitosis events into one label
    % but also relabel the cells so that the resulting labels are consecutive starting at 1
    renum_vec = 1:size(frame_division,1);
    renum_vec = renum_vec'; % make a column vector

	% loop over the mother cells relabeling the daughters
    for i = 1:numel(mother_cells)
    	% get the mother cell number
        mother_cell = mother_cells(i);
        % get the daughters of that mother cell
        daughters = find(frame_division == mother_cell);
        % remove any tracks to the first daughter so that this overwrite any previously assigned tracks
        % kill any tracks that point to the first daughter, if it is the mother it will be restored, otherwise that
        % track is now dead
        for k = 1:numel(daughters)
            frame_track_vector_updated(frame_track_vector_updated == daughters(k)) = NaN;
        end
        % track the mother cell to the first daughter
        frame_track_vector_updated(mother_cell) = daughters(1);
        % relabel all other daughters to the first daughter
        for k = 2:numel(daughters)
            renum_vec(daughters(k)) = daughters(1);
        end
    end
    % adjust the renum_vec so that resulting numbers are sequential
    vals = unique(renum_vec);
    temp = zeros(size(renum_vec));
    k = 1;
    for i = 1:numel(vals)
        temp(renum_vec == vals(i)) = k;
        k = k + 1;
    end
    renum_vec = temp;
    
    % find the non nan values in frame_track_vector_updated
    indx = frame_track_vector_updated>0;
    % relabel them with the values in renum_vec
    frame_track_vector_updated(indx) = renum_vec(frame_track_vector_updated(indx));
    % use renum_vec to relabel the image frame_2 so the labels are sequential starting at 1
    renum_vec = [0;renum_vec]; % add prefix zero
    frame_2 = renum_vec(frame_2+1);
    
    % update the number of cells in frame_2
    number_cells_2 = max(renum_vec(:));
    % update the centroids and cell sizes to reflect the new labeling after merging the daughters to they share the mother cell label
    [centroids_2, cells_size_2] = get_info_frame(frame_2, number_cells_2);
    
    % reCompute the overlap between the previous and current frame
    frame_overlap = compute_overlap(frame_1, frame_2, number_cells_1, number_cells_2);
    
    % reCompute the cost matrix for two consecutive frames
    frame_cost = compute_cost(frame_overlap, centroids_1, centroids_2, cells_size_1, cells_size_2, ...
        number_cells_1, number_cells_2, weight_overlap, weight_centroids, weight_size, max_centroids_distance);

    % make division matrix empty as no division occurred in this frame, since mitosis is diabled
    frame_division(frame_division ~= 0) = 0;
end

