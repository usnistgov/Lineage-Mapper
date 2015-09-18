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
% This is a sub-function of the main function "start_tracking.m".
%
% create_outputs writes the outputs of the cell tracker.
% 
% This function gives as outputs the output_matrix the birth and death vectors and the division_matrix.
% - The output_matrix has five columns: [frame_vector original_numbering cells_renumbering X_centroid Y_centroid]
% - XY_centroids matrix: a 3 dimensional matrix which will be used to plot the centroids trajectories in 2D or 3D
%   with the following dimensions: (frames x  number_of_cells x 2D_coordinates(X,Y))
% - division_matrix is a 2D matrix that has the daughters numbers of each cell if any. Assuming that a cell can
%   only divid in two cells, division_matrix has thus (highest_cell_number x 2) dimensions.
% - touching_cells is a vector that contains the cells that touched another cell or cells during its lifetime
% - dtd_cells is a vector containing the number of the cells that goes from division to division. These are 
%   daughter cells that did not leave the frame at any given moment and divide in two cells at death. a dtd_cell
%   must also live at least cell_life_threshold frames. cell_life_threshold is the minimum number of frames that
%   a cell must survive in order to be biologically eligible.
% - border_cells vector that contains the number of the cells that touch the borders
% - surviving_cells vector that contains the number of cells that survived til the end of the experiment
% 

function [XY_centroids, division_matrix, dtd_cells, daughter_mother_vector, cells_size_matrix, perimeter_matrix, fusion_matrix, nb_touching_matrix] = create_outputs(centroids, touching_vectors,...
    cells_size, division, birth, death, renumbering_vectors, nb_frames, highest_cell_number, cell_life_threshold, border_cells, perimeter, fusion, enable_fusion)

% Construct the XY_centroids matrix which will be used to plot the centroids trajectories in 2D or 3D
% Initialize the matrix XY_centroids to a nan matrix
XY_centroids = nan(nb_frames, highest_cell_number, 2);

for i = 1:nb_frames
    % Stocks the X centroid coordinate in the matrix XY_centroids
    XY_centroids(i, renumbering_vectors{i}, 1) = centroids{i}(:,1);
    % Stocks the Y centroid coordinate in the matrix XY_centroids
    XY_centroids(i, renumbering_vectors{i}, 2) = centroids{i}(:,2);
end



% Construct the cells_size_matrix and the perimeter_matrix that have respectively the size of each cell and its
% perimeter in any frame
cells_size_matrix = zeros(highest_cell_number, nb_frames);
perimeter_matrix = zeros(highest_cell_number, nb_frames);
nb_touching_matrix = zeros(highest_cell_number, nb_frames);
for i = 1:nb_frames   
    cells_size_matrix(renumbering_vectors{i}, i) = cells_size{i};
    perimeter_matrix(renumbering_vectors{i}, i) = perimeter{i};
    tch_vec = touching_vectors{i};
    nb_fnd = numel(tch_vec);
    for k = 1:nb_fnd
        nb_touching_matrix(renumbering_vectors{i}(k), i) = numel(find(tch_vec{k,:}));
    end
end



% construct the division_matrix that has highest_cell_number as number of rows and 2 columns that will give us the
% number of daughters of the mother cell if any.
division_matrix = zeros(highest_cell_number, 2);

for i = 1:highest_cell_number
    
    % frame_death is the frame number where the cell_to_track last appeared
    frame_death = death(i);
    
    % Find its original numbering
    original_number = find(renumbering_vectors{frame_death} == i);
    
    % if the frame_death of the cell is the last frame, no need to check for any division
    if frame_death == nb_frames, continue, end
    
    % if the frame_death of the cell is not the last frame, then check if that cell has divided or not
    % find the children that the cell divided into
    cell_division = find(division{frame_death+1} == original_number);
        
    % if the cell has no daughters: continue
    if isempty(cell_division), continue, end
    
    % if the cell has daughters then check their numbers and put it in the division_matrix
    division_matrix(i,:) = renumbering_vectors{frame_death+1}(cell_division);
end

fusion_matrix = [];
if enable_fusion
    % construct the fusion_matrix that has highest_cell_number as number of rows and 2 columns that will give us the
    % number of daughters of the mother cell if any.
    fusion_matrix_c = cell(highest_cell_number, 1);

    for i_frame = 2:numel(fusion)
        if isempty(fusion{i_frame}), continue, end

        % create full copy of the fusion matrix
        cur_fusion_matrix = full(fusion{i_frame});
        % remove any cells that died
        cur_fusion_matrix(cur_fusion_matrix < 0) = 0;
        % loop over its columns
        for j = 1:size(cur_fusion_matrix,2)
            % if anything in the column is nonzero
            if any(cur_fusion_matrix(:,j))
                % find the new cell number
                cur_new_cell_nb = renumbering_vectors{i_frame}(j);
                indx = cur_fusion_matrix(:,j)>0;
                % of there is more than 1 element in the column it is a fusion
                if sum(indx) > 1
                    % record the fused cell numbers
                    fusion_matrix_c{cur_new_cell_nb} = (horzcat(fusion_matrix_c{cur_new_cell_nb}, renumbering_vectors{i_frame-1}(indx)'));
                end
            end
        end
    end
    
    nb_elems_fnd = 0;
    for i = 1:highest_cell_number
        nb_elems_fnd = max(nb_elems_fnd, numel(fusion_matrix_c{i}));
    end
    fusion_matrix = zeros(highest_cell_number, nb_elems_fnd);
    for i = 1:highest_cell_number
        nb_fnd = numel(fusion_matrix_c{i});
        fusion_matrix(i,1:nb_fnd) = fusion_matrix_c{i}(1:nb_fnd);
    end
end


% Find the mother cells and constitute the daughter_mother_vector that has the number of the
% mother cell of each daughter, example if daughter_mother_vector(23) = 5, this means that cell 5 is the mother
% of daughter cell 23
daughter_mother_vector = zeros(highest_cell_number, 1);
for i = 1:highest_cell_number
    % if cell i has no daughters: continue
    if division_matrix(i,1) == 0, continue, end
    
    % Update the daughter vector
    daughter_mother_vector(division_matrix(i,:)) = i;
end




% Initialize the dtd_cells
dtd_cells = 1:highest_cell_number;

% Filter the dtd_cells
% Check if the life of the dtd_cells meets the minimum threshold
% Delete the cells that touch the borders
% Delete the cells that survived til the end
% Keep only the daughters cells
% Make sure that the daughter cells divides as well
for i = 1:highest_cell_number
    if death(i) - birth(i) < cell_life_threshold || border_cells(i) > 0 || death(i) == nb_frames || ...
            daughter_mother_vector(i) == 0 || division_matrix(i,1) == 0, dtd_cells(i) = 0; end
end

dtd_cells = nonzeros(dtd_cells(:));




