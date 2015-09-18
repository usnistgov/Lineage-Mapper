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




% FUNCTION THAT EXECUTES THE TRACKER CODE
% DN
function bool = start_tracking(segmented_images_path, segmented_images_common_name, tracked_images_path,...
  tracked_images_common_name, max_centroids_distance, weight_size, weight_centroids, weight_overlap, fusion_overlap_threshold, division_overlap_threshold,...
  cell_life_threshold, max_cell_num, nb_frames, daughter_size_similarity, daughter_aspect_ratio_similarity, cell_size_threshold, enable_cell_fusion_flag, frames_to_track,...
  cell_density_flag, border_cell_flag, number_of_frames_check_circularity, circularity_threshold, cell_apoptosis_delta_centroid_thres, enable_cell_mitosis_flag)

print_to_command('Tracking Cells', tracked_images_path);

% DN
bool = false;

segmented_images_path = validate_filepath(segmented_images_path);
tracked_images_path = validate_filepath(tracked_images_path);


input_images_in_single_tif_flag = false;
% Read images from input folder
input_files = dir([segmented_images_path '*' segmented_images_common_name '*.tif']);
if numel(input_files) == 1
  stats = imfinfo([segmented_images_path input_files(1).name]);
  % check if there is an image sequence in the single tif
  if numel(stats) > 1
    input_files = repmat(input_files, numel(stats),1);
    input_images_in_single_tif_flag = true;
  end
end


if ~isempty(frames_to_track) && any(frames_to_track > 0)
  nb_frames = numel(frames_to_track);
else
  nb_frames = numel(input_files);
  frames_to_track = 1:nb_frames;
  nb_frames = numel(frames_to_track);
end

% ----------------------------------------------- Initialize Variables ----------------------------------------------------------------

% Initiate the vector "number_cells" that contain the number of cells in each frame
number_cells = zeros(nb_frames,1);

% Creat a cell structure to store the "cell_size" vector for each frame. The cell_size vector has the number of pixels (the size) of each cell in the
% frame. This vector has different length which is relative to the number of cells in the frame.
cells_size = cell(1, nb_frames);

% Creat a cell structure to store the "centroids" matrix for each frame. The centroids matrix has the centroids coordinates (X,Y) of each cell in the
% frame. This matrix has different length which is relative to the number of cells in the frame. The dimension of this matrix is (number_cells x 2)
centroids = cell(1, nb_frames);

% Creat a cell structure to store the "track_vectors" for each pair of frames. The track_vectors contains the tracking info between two consecutive
% frames. track_vector has a length equal to the number of source cells. Each line represents the number of the source cell and it contains the number
% of the target cell that the source cell should be tracked to. if this number is nan (not a number) this means that this cell is dead. For example if
% track_vectors{2}(4) = 6, this means that the source cell number 4 in frame number 1 should be tracked to target cell number 6 in frame number 2.
track_vectors = cell(1, nb_frames);

% Creat a cell structure to store the "fusion" for each pair of frame. Between the two consecutive frames, we have a 2D matrix with the following
% dimensions: (number_of_source_cells x number_of_target_cells). One should look at this matrix columnwise. It will indicate which of the target cells
% is actually a fusion target region. For example if column 3 has 2 nonzero elements say frame_fusion(2,3) = 3 and frame_fusion(5,3) = 12, this means
% that the source cells number 2 and 5 has fused into the target region cell number 3. And that part of the target region number 3 that is close (in
% centroid distance) to source cell number 2 will keep the numbering 3. And the numbering of the other part of the target region number 3 that is
% close to source cell  number 5 will be renumbered to 12.
fusion = cell(1, nb_frames);

% Creat a cell structure to store the "division" for each pair of frames. This vector has the same number of lines as the number of target cells and
% each element will contain the number of the source cell that divided into the target cells, 0 otherwise. For example if division{2}(5) = 3, this
% means that source cell number 3 from frame number 1 has divided into cell number 5 from frame number 2 and some other cell of that same frame. This
% means that in the division vector there is at least one more line that has number 3. If division{4}(2) = 0, this means that target cell number 2
% from frame number 4 is not a daughter cell.
division = cell(1, nb_frames);

% Creat a cell structure to store the "renumbering_vectors" for each pair of frame. The renumbering_vectors contains the new tracking numbers of the
% target cells. renumbering_vectors has a length equal to the number of target cells. Each line represents the number of the target cell and it
% contains the new number of that cell given by the track.
renumbering_vectors = cell(1, nb_frames);

% Create the birth vector containing the frame number of each cell when it first appeared
birth = zeros(max_cell_num,1);

% Create the death vector containing the frame number of each cell when it last appeared
death = zeros(max_cell_num,1);

% hold the circularity index of each global cell number
% cell array to hold the circularity of each cell, 0 (noncircular) to 1 (perfect circle)
cell_circularity = zeros(max_cell_num,nb_frames);

% Create the touching_vector cell structure that contains for each frame another cell structure with length = nb_cells(i_frame) and each structure
% contains a matrix M that contains the number of the neighboring cell and the size of touching area in pixels. For example if touching_vector{5}{2} =
% [1 23; 4 12]; this means that cell number 2 in frame 5 was touching 2 cells in that frame (cell number 1 and cell number 4 and the number of
% touching pixels is 23 with 1 and 12 with 4.
touching_vectors = cell(1, nb_frames);

% Create the perimeter cell structure that contains for each frame i_frame a vector with length = nb_cells(i_frame) and that contains the perimeter of
% the cells in the frame
perimeter = cell(1, nb_frames);

% Initialize the border_cells that indicate if a cell is touching the border (1) or not (0)
border_cells = false(max_cell_num, 1);

% The number of zeros to add to the left of the files numbering
zero_pad = num2str(length(num2str(nb_frames)));

% --------------------------------------------------------------------------------------------------------------------------------------------

% Set the files name of the output
output_files = cell(nb_frames,1);
for i = 1:nb_frames
  if input_images_in_single_tif_flag
    output_files{i} = [tracked_images_common_name '.tif'];
  else
    output_files{i} = [tracked_images_common_name sprintf(['%0' zero_pad 'd'],i) '.tif'];
  end
end

% DN
try
  tracking_cancelled = false;
  total_frames = length(frames_to_track);
  print_update(1, 1, nb_frames);
  
  % Start with the first frame
  if input_images_in_single_tif_flag
    frame_1 = imread([segmented_images_path input_files(frames_to_track(1)).name], 'Index', frames_to_track(1));
  else
    frame_1 = imread([segmented_images_path input_files(frames_to_track(1)).name]);
  end
  
  % force sequential labeling of the labeled segmented image
  [frame_1, number_cells(1)] = relabel_image(frame_1);
  
  % Get the centroid and the size of the cells in the first frame
  [centroids{1}, cells_size{1}] = get_info_frame(frame_1, number_cells(1));
  
  % The tracking vector of the first frame is itself
  track_vectors{1} = (1:number_cells(1))';
  
  % The renumbereing vector of the first frame is the same as the track_vectors
  renumbering_vectors{1} = track_vectors{1};
  
  % Update the birth vector to 1 for all the cells in the first frame
  birth(1:number_cells(1),1) = 1;
  
  % Renumber the Cells according to the tracking in the input_frames and saves them as an output_frame
  [touching_vectors{1}, border_cells, perimeter{1}, cell_circularity] = renumber_frames(frame_1, 1, number_cells(1), tracked_images_path, ...
    output_files{1}, renumbering_vectors{1}, border_cells, cell_circularity, input_images_in_single_tif_flag);
  
  % The initial value of the maximum cell number reached is the total number of cells in the first frame + 1.
  highest_cell_number = number_cells(1) + 1;
  
  % Start tracking the cells from one frame to another
  for i_frame = 2:numel(frames_to_track)
    print_update(2,i_frame,nb_frames);
    
    % Read the info of the next frame
    if input_images_in_single_tif_flag
      frame_2 = imread([segmented_images_path input_files(frames_to_track(i_frame)).name], 'Index', frames_to_track(i_frame));
    else
      frame_2 = imread([segmented_images_path input_files(frames_to_track(i_frame)).name]);
    end
    

    % force sequential labeling of the labeled segmented image
    [frame_2, number_cells(i_frame)] = relabel_image(frame_2);
    
    [centroids{i_frame}, cells_size{i_frame}] = get_info_frame(frame_2, number_cells(i_frame));
    
    % Compute the overlap between the previous and current frame
    overlap = compute_overlap(frame_1, frame_2, number_cells(i_frame-1), number_cells(i_frame));
    
    % Compute the cost matrix for two consecutive frames
    cost = compute_cost(overlap, centroids{i_frame-1}, centroids{i_frame}, cells_size{i_frame-1}, cells_size{i_frame}, ...
      number_cells(i_frame-1), number_cells(i_frame), weight_overlap, weight_centroids, weight_size, max_centroids_distance);
    
    % Generate track vector between time t and t+1
    track_vectors{i_frame} = generate_track_vector(cost);
    
    % Check for division bewteen two consecutive frames
    [frame_2, division{i_frame}, track_vectors{i_frame}, cost, overlap, number_cells(i_frame), centroids{i_frame}, cells_size{i_frame}] = ...
      check_division(frame_2, frame_1, track_vectors{i_frame}, cells_size{i_frame-1}, cells_size{i_frame}, overlap, cost,...
      number_cells(i_frame-1), number_cells(i_frame), centroids{i_frame-1}, centroids{i_frame}, division_overlap_threshold,...
      daughter_size_similarity, daughter_aspect_ratio_similarity, enable_cell_mitosis_flag, weight_overlap, weight_centroids, ...
      weight_size, max_centroids_distance, cell_circularity, circularity_threshold, i_frame, renumbering_vectors{i_frame-1}, number_of_frames_check_circularity);
    
    % Check and correct for any possible fusion case between two consecutive frames and compute the track vector
    [track_vectors{i_frame}, fusion{i_frame}, frame_2, cost, centroids{i_frame}, cells_size{i_frame}, number_cells(i_frame), division{i_frame}, overlap] = ...
      check_correct_fusion(frame_2, frame_1, number_cells(i_frame), number_cells(i_frame-1), centroids{i_frame}, centroids{i_frame-1},...
      cells_size{i_frame}, cells_size{i_frame-1}, overlap, cost, track_vectors{i_frame}, fusion_overlap_threshold, cell_size_threshold, enable_cell_fusion_flag,...
      weight_overlap, weight_centroids, weight_size, max_centroids_distance, division{i_frame});
    
    % compute the hungarian optimization to find tracks for all untracked cells
    track_vectors{i_frame} = hungarian_optimization(number_cells(i_frame), number_cells(i_frame-1), track_vectors{i_frame}, division{i_frame}, fusion{i_frame}, cost);
    
    % Renumber cells according to the tracking
    [renumbering_vectors{i_frame}, highest_cell_number, birth, death] = renumber_tracking(i_frame, track_vectors{i_frame}, ...
      renumbering_vectors{i_frame-1}, number_cells(i_frame-1), number_cells(i_frame), highest_cell_number, birth, death);
    
    % Before re-looping assign the current frame_2 to frame_1
    frame_1 = frame_2;
    
    % Renumber the Cells according to the tracking in the input_frames and saves them as an output_frame
    [touching_vectors{i_frame}, border_cells, perimeter{i_frame}, cell_circularity] = renumber_frames(frame_2, i_frame, number_cells(i_frame), tracked_images_path, ...
      output_files{i_frame}, renumbering_vectors{i_frame}, border_cells, cell_circularity, input_images_in_single_tif_flag);
    
    % Transform most matrices into sparse for better storage
    [i,j,s] = find(fusion{i_frame});
    fusion{i_frame} = sparse(i,j,s);
    [i,j,s] = find(division{i_frame});
    division{i_frame} = sparse(i,j,s);
    
%     % Save Tracking Data
%     save([tracked_images_path 'tracking_workspace.mat'], 'i_frame', 'number_cells', 'centroids', 'cells_size', 'overlap', 'cost', 'track_vectors', 'fusion', 'division', ...
%       'renumbering_vectors', 'highest_cell_number', 'birth', 'death', 'touching_vectors', 'border_cells', 'perimeter');
    
    if highest_cell_number > round(0.9*max_cell_num)
      % add stoarage space to matricies using max_cell_num to define their size
      % double the allocated size
      birth = [birth; zeros(max_cell_num,1)]; %#ok<AGROW>
      death = [death; zeros(max_cell_num,1)]; %#ok<AGROW>
      border_cells = [border_cells; false(max_cell_num, 1)]; %#ok<AGROW>
      cell_circularity = [cell_circularity; zeros(max_cell_num,nb_frames)]; %#ok<AGROW>
      max_cell_num = max_cell_num*2;
    end
  end
  
catch err % if user canceled or error was thrown
  rethrow(err);
end
if tracking_cancelled
  print_to_command('Canceled...');
  return;
end
print_update(3,nb_frames,nb_frames);

% Update the highest_cell_number
highest_cell_number = highest_cell_number - 1;

% Update the border_cells, cell_circularity, birth and death vector
border_cells = border_cells(1:highest_cell_number);
cell_circularity = cell_circularity(1:highest_cell_number); %#ok<NASGU>
birth = birth(1:highest_cell_number);
death = death(1:highest_cell_number);
death(death == 0) = nb_frames; % the cells with a death frame still = 0 are not dead.

% Create the outputs
print_to_command('Generating Ouput Metadata',tracked_images_path);
[XY_centroids, division_matrix, dtd_cells, daughter_mother_vector, cells_size_matrix, perimeter_matrix, fusion_matrix, nb_touching_matrix] = create_outputs(centroids, touching_vectors,...
  cells_size, division, birth, death, renumbering_vectors, nb_frames, highest_cell_number, cell_life_threshold, border_cells, perimeter, fusion, enable_cell_fusion_flag); %#ok<ASGLU>

% detect cell apoptosis
cell_apoptosis = detect_cell_apoptosis(XY_centroids, cells_size_matrix, cell_life_threshold, cell_apoptosis_delta_centroid_thres); %#ok<NASGU>

% generate the confidence index
print_to_command('Generating Confidence Index',tracked_images_path);
[CI_vals, CI_indx] = generate_confidence_index(birth, death, border_cells,nb_touching_matrix, cell_life_threshold, border_cell_flag, cell_density_flag);
Confidence_Index = [CI_vals CI_indx]; %#ok<NASGU>

% Generate color_vector
cell_colors = jet(highest_cell_number+5);
cell_colors = cell_colors(randperm(highest_cell_number+5),:); %#ok<NASGU>

% Save Tracking Data
clear frame_1 frame_2;
save([tracked_images_path 'tracking_workspace.mat']);
print_to_command('Done Tracking',tracked_images_path);

% DN
bool = true;

