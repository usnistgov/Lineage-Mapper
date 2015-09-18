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
% This function check and correct for any fusion between two consecutive frames. It will "deny" fusion between
% cells and will correct the fusion target cell region by deviding it into the number of source cells that are
% fusing. The division will be made by affecting each pixel of the fusion target cell region to the source cell
% that has the minimum distance between the centroid of the source cell and that pixel.
%
% This fucntion will compute and return the track_vector for two consecutive frames. The track_vector is a
% vector that will indicate which source cells are tracked to which tartget cells. It has the same number of
% lines as the number of source cells and each element will contain the number of the target cell that the source
% cell should be tracked to. for example if "track_vector(2) = 4" this means that the target cell number 4 is
% tracked to the source cell numbe 2. This is the same cell from one frame to another.
%
% This fucntion will also compute and return the frame_fusion matrix between the two consecutive frames. This
% matrix is a 2D matrix with the following dimensions: (number_of_source_cells x number_of_target_cells). One
% should look at this matrix columnwise. It will indicate which of the target cells is actually a fusion target
% region. For example if column 3 has 2 nonzero elements say frame_fusion(2,3) = 3 and frame_fusion(5,3) = 12,
% this means that the source cells number 2 and 5 has fused into the target region cell number 3. And that part
% of the target region number 3 that is close (in centroid distance) to source cell number 2 will keep the
% numbering 3. And the numbering of the other part of the target region number 3 that is close to source cell
% number 5 will be renumbered to 12.
%
% This fucntion will return a variable called "update_segmentation" that indicates if the segmented frame needs
% to be updated (1) or not (0).
%

function [frame_track_vector, frame_fusion, frame_2, frame_cost, centroids_2, cells_size_2, number_cells_2, frame_division, frame_overlap] = ...
  check_correct_fusion(frame_2, frame_1, number_cells_2, number_cells_1, centroids_2, centroids_1, cells_size_2, cells_size_1,...
  frame_overlap, frame_cost, frame_track_vector, fusion_overlap_threshold, cell_size_threshold, enable_fusion, weight_overlap, weight_centroids,...
  weight_size, max_centroids_distance, frame_division)

% Get the frame dimensions
[number_rows, number_columns] = size(frame_2);

% Set the initial value of the frame_fusion matrix
frame_fusion = zeros(number_cells_1, number_cells_2);

% Check to see if any target cell has been tracked to more than one source cell. if yes then check
% to see if this is a fusion case. If it is, modify the input frame and save it into the input_modified folder
% and set the "update_segmentation" value to 1.
%
% Start looping through all the line or source cells of the frame_track_vector to check for any duplicate
% mapping.
for i = 1:number_cells_1
  % if the frame_track_vector cost is NaN => cell is dead: continue
  if ~isnan(frame_track_vector(i)) && frame_track_vector(i) > 0
    % Otherwise change the corresponding element between source cell i and its target cell to 1 in the
    % frame_fusion matrix
    frame_fusion(i, frame_track_vector(i)) = 1;
  end
end

% Compute the sum vector S which is the sum of all the columns of frame_fusion matrix. The fusion target region
% will have at least 2 cells tracked to it => S>1
S = sum(frame_fusion, 1);

% Reloop through all the source cells and look for the fusion candidate cells. Delete the ones that do not meet
% the fusion_threshold
for i = 1:number_cells_1
  
  % if cell i is dead: continue
  if isnan(frame_track_vector(i)) || frame_track_vector(i) == 0, continue, end
  
  % if cell i does not belong to any fusion region, reset to 0 the element between i and its target cell
  if S(frame_track_vector(i)) < 2, frame_fusion(i, frame_track_vector(i)) = 0; continue, end
  
  % if cell i belongs to a fused region and doesn't meet the fusion_threshold => delete the track
  if frame_overlap(i, frame_track_vector(i))/cells_size_1(i) < fusion_overlap_threshold
    frame_fusion(i, frame_track_vector(i)) = 0;
    frame_track_vector(i) = nan;
  end
end

% remove any columns with a single lingering value
S = sum(frame_fusion>0,1);
frame_fusion(:, S==1) = 0;
% Update the sum vector
S = sum(frame_fusion, 1);

% If we didn't find any fusion cases, return
if all(S < 2), return; end

% Find the fused cell regions number
fused_cells = find(S >= 2);

% Initialise the index highest_cell_number_2 that will give us the number of target cells in the modified target
% frame after deviding all the fusion target regions.
highest_cell_number_2 = number_cells_2;

% Affect new numbers to use when decomposing the fusion target cell regions. This is done by using the
% frame_fusion matrix as explained in the begining of the fucntion.
for j = 1:length(fused_cells)
  % indicator that tells us the first time we encouter a source cell that form the fused target cell j this
  % cell will have the same number as j
  first_occurance = 1;
  
  % Scout all the lines searching for the cells that form target cell j and affect them with new numbers
  for i = 1:number_cells_1
    
    % if object i does not belong to object j: continue
    if frame_fusion(i,fused_cells(j)) == 0, continue, end
    
    % if fusion is allowed, delete all tracked source cells so they die and the fused cell gets a different number
    if enable_fusion, frame_track_vector(i) = nan; end
    
    % if source cell i is the first cell that we encounter that belongs to cell j: affect it with the
    % same number as j
    if first_occurance == 1, frame_fusion(i,fused_cells(j)) = fused_cells(j); first_occurance = 0; continue,end
    
    % if we reach this stage of the loop, this means that cell i is not the first cell that we encountered
    % and that it also belongs to the fused cell_region: affect it with the Highest_cell_number
    highest_cell_number_2 = highest_cell_number_2 + 1;
    frame_fusion(i,fused_cells(j)) = highest_cell_number_2;
    if enable_fusion, frame_track_vector(i) = nan; continue, end
    frame_track_vector(i) = highest_cell_number_2;
  end
end

% If enable fusion is selected return at this point so the cells are not fused together below
if enable_fusion
  % loop over the column and set all non zero element to the column number
  for j = 1:size(frame_fusion,2)
    indx = frame_fusion(:,j) > 0;
    frame_fusion(indx,j) = j;
  end
  return;
end

% Get the boxing bound of the fused cell
% fused_cell_region(:,k) = [i_min; i_max; j_min; j_max].
fused_cell_region = [number_rows; 0; number_columns; 0] * ones(1, number_cells_2);

[nonzero_frame_2_ij_pixels(:,1), nonzero_frame_2_ij_pixels(:,2)] = find(frame_2);
for counter = 1:size(nonzero_frame_2_ij_pixels,1)
  i = nonzero_frame_2_ij_pixels(counter,1);
  j = nonzero_frame_2_ij_pixels(counter,2);
  
  if S(frame_2(i,j)) < 2, continue, end
  
  % Update the box region of the fusion_target_cell if necessary.
  % if i < i_min
  if i < fused_cell_region(1, frame_2(i,j)), fused_cell_region(1, frame_2(i,j)) = i; end
  
  % if i > i_max
  if i > fused_cell_region(2, frame_2(i,j)), fused_cell_region(2, frame_2(i,j)) = i; end
  
  % if j < j_min
  if j < fused_cell_region(3, frame_2(i,j)), fused_cell_region(3, frame_2(i,j)) = j; end
  
  % if j > j_max
  if j > fused_cell_region(4, frame_2(i,j)), fused_cell_region(4, frame_2(i,j)) = j; end
end

m1 = size(frame_1,1);
n1 = size(frame_1,2);


% Renumber the segmented frame by renumber the fused cell regions
for cell_j = 1:length(fused_cells)
  
  % Scout the pixels of the box region to study
  for j = fused_cell_region(3,fused_cells(cell_j)):fused_cell_region(4,fused_cells(cell_j))
    for i = fused_cell_region(1,fused_cells(cell_j)):fused_cell_region(2,fused_cells(cell_j))
      % if pixel (i,j) is the background or if it doesn't belong to the target cell j: continue
      if frame_2(i,j) == 0, continue, end
      if frame_2(i,j) ~= fused_cells(cell_j), continue, end
      
      % check that (i,j) exists in frame_1
      if i > m1 || j > n1
        % if not seach the edge of frame_1 looking for the  dominant neighbor using the same search employed for
        % non overlapping pixels
        
        % initialize the distance in number of pixels between pixel(i,j) and the closest nonzero pixel
        pixel_distance = 1;
        % Initialize the indicators that we found neighbors to the pixel at a certain distance or we checked
        % all pixels in the image. it is the stop criterion for the while loop
        not_found_neighbors = 1;
        % Initialize the neighbors vector that contains the number of the neighbors at the same distace
        % from pixel (i,j)
        neighbors = zeros(number_cells_1, 1);
        
        % Start searching for the closest neighbors
        while not_found_neighbors
          % make sure that the pixels at pixel_distance from i belong to the image
          x_min = max(1, n1-pixel_distance);
          x_max = min(j+pixel_distance, n1);
          y_min = max(1, m1-pixel_distance);
          y_max = min(i+pixel_distance, m1);
          % Start looking at the pixels around pixel (i,j) at distance x_min and x_max (scout vertically, by
          % lines)
          for k = y_min : y_max
            % if the pixel at frame_1(k,x_min) is not a background, and belongs to the fused cell region
            % memorize it as a neighbor
            if frame_1(k, x_min) > 0 && frame_fusion(frame_1(k, x_min), frame_2(i,j)) > 0
              not_found_neighbors = 0;
              neighbors(frame_1(k, x_min)) = neighbors(frame_1(k, x_min)) + 1;
            end
            
            % if the pixel at frame_1(k,x_max) is not a background, and belongs to the fused cell region
            % memorize it as a neighbor
            if frame_1(k, x_max) > 0 && frame_fusion(frame_1(k, x_max), frame_2(i,j)) > 0
              not_found_neighbors = 0;
              neighbors(frame_1(k, x_max)) = neighbors(frame_1(k, x_max)) + 1;
            end
          end
          
          % Start looking at the pixels around pixel(i,j) at distance y_min and y_max (scout vertically, by
          % lines)
          for k = x_min+1 : x_max-1
            % if the pixel at frame_1(y_min,k) is not a background, and belongs to the fused cell region
            % memorize it as a neighbor
            if frame_1(y_min,k) > 0 && frame_fusion(frame_1(y_min,k), frame_2(i,j)) > 0
              not_found_neighbors = 0;
              neighbors(frame_1(y_min,k)) = neighbors(frame_1(y_min,k)) + 1;
            end
            
            % if the pixel at frame_1(y_max,k) is not a background, and belongs to the fused cell region
            % memorize it as a neighbor
            if frame_1(y_max,k) > 0 && frame_fusion(frame_1(y_max,k), frame_2(i,j)) > 0
              not_found_neighbors = 0;
              neighbors(frame_1(y_max,k)) = neighbors(frame_1(y_max,k)) + 1;
            end
          end
          
          % if no neighbors are found then increase the pixel_distance and reloop
          pixel_distance = pixel_distance + 1;
        end
        
        % Find the dominant neighbor
        [max_value, neighbor_number] = max(neighbors); %#ok<ASGLU>
        
        % Update the value of pixel frame_2(i,j)
        frame_2(i,j) = frame_fusion(neighbor_number, frame_2(i,j));
        continue; % skip the remaining as the pixel value has been found
      end
      
      
      % if pixel(i,j) belongs to a source cell in frame_1 => affect that pixel in frame_2 to the same
      % cell as frame_1 if pixel(i,j) in frame_1 belongs to the fused_region. In other words give every
      % overlapping pixel to the same cell
      if frame_1(i,j) > 0 && frame_fusion(frame_1(i,j), frame_2(i,j)) > 0, ...
          frame_2(i,j) = frame_fusion(frame_1(i,j), frame_2(i,j)); continue, end
      
      % The pixels that belongs to the fusion target region without belonging to any source cell will be
      % given the number of the most dominant source cell neighbor in frame_1 if that source cell belongs
      % to the fused region of course
      
      % initialize the distance in number of pixels between pixel(i,j) and the closest nonzero pixel
      pixel_distance = 1;
      
      % Initialize the indicators that we found neighbors to the pixel at a certain distance or we checked
      % all pixels in the image. it is the stop criterion for the while loop
      not_found_neighbors = 1;
      
      % Initialize the neighbors vector that contains the number of the neighbors at the same distace
      % from pixel (i,j)
      neighbors = zeros(number_cells_1, 1);
      
      % Start searching for the closest neighbors
      while not_found_neighbors
        
        % make sure that the pixels at pixel_distance from i belong to the image
        x_min = max(1, j-pixel_distance);
        x_max = min(number_columns, j+pixel_distance);
        y_min = max(1, i-pixel_distance);
        y_max = min(number_rows, i+pixel_distance);
        
        % Start looking at the pixels around pixel (i,j) at distance x_min and x_max (scout vertically, by
        % lines)
        for k = y_min : y_max
          % if the pixel at frame_1(k,x_min) is not a background, and belongs to the fused cell region
          % memorize it as a neighbor
          if frame_1(k, x_min) > 0 && frame_fusion(frame_1(k, x_min), frame_2(i,j)) > 0
            not_found_neighbors = 0;
            neighbors(frame_1(k, x_min)) = neighbors(frame_1(k, x_min)) + 1;
          end
          
          % if the pixel at frame_1(k,x_max) is not a background, and belongs to the fused cell region
          % memorize it as a neighbor
          if frame_1(k, x_max) > 0 && frame_fusion(frame_1(k, x_max), frame_2(i,j)) > 0
            not_found_neighbors = 0;
            neighbors(frame_1(k, x_max)) = neighbors(frame_1(k, x_max)) + 1;
          end
        end
        
        % Start looking at the pixels around pixel(i,j) at distance y_min and y_max (scout vertically, by
        % lines)
        for k = (x_min+1) : (x_max-1)
          % if the pixel at frame_1(y_min,k) is not a background, and belongs to the fused cell region
          % memorize it as a neighbor
          if frame_1(y_min,k) > 0 && frame_fusion(frame_1(y_min,k), frame_2(i,j)) > 0
            not_found_neighbors = 0;
            neighbors(frame_1(y_min,k)) = neighbors(frame_1(y_min,k)) + 1;
          end
          
          % if the pixel at frame_1(y_max,k) is not a background, and belongs to the fused cell region
          % memorize it as a neighbor
          if frame_1(y_max,k) > 0 && frame_fusion(frame_1(y_max,k), frame_2(i,j)) > 0
            not_found_neighbors = 0;
            neighbors(frame_1(y_max,k)) = neighbors(frame_1(y_max,k)) + 1;
          end
        end
        
        % if no neighbors are found then increase the pixel_distance and reloop
        pixel_distance = pixel_distance + 1;
      end
      
      % Find the dominant neighbor
      [max_value, neighbor_number] = max(neighbors); %#ok<ASGLU>
      
      % Update the value of pixel frame_2(i,j)
      frame_2(i,j) = frame_fusion(neighbor_number, frame_2(i,j));
    end
  end
end



u = unique(frame_fusion(:));
if u(1) == 0, u(1) = []; end
number_cells_2 = max(frame_2(:));
frame_2 = check_body_connectivity(frame_2, number_cells_2, u);

% Delete cells with size less than threshold
[frame_fusion, frame_track_vector, frame_2, number_cells_2, frame_division] = check_cell_size(frame_fusion, frame_track_vector, frame_2, highest_cell_number_2, cell_size_threshold, frame_division);
% remove the elements in frame_fusion that are single elements in the given column
S = sum(frame_fusion>0,1);
frame_fusion(:, S==1) = 0;

% read the info of the updated frame_2
[centroids_2, cells_size_2] = get_info_frame(frame_2, number_cells_2);

% Compute the overlap between the previous and current frame
frame_overlap = compute_overlap(frame_1, frame_2, number_cells_1, number_cells_2);

% Compute the cost matrix for two consecutive frames
frame_cost = compute_cost(frame_overlap, centroids_1, centroids_2, cells_size_1, cells_size_2, ...
  number_cells_1, number_cells_2, weight_overlap, weight_centroids, weight_size, max_centroids_distance);

end % end check_correct_fusion


function [frame_fusion, frame_track_vector, image_2, highest_cell_number, frame_division] = check_cell_size(frame_fusion, frame_track_vector, image_2, ...
  Highest_cell_number, cell_size_threshold, frame_division)

% only consider cells that are involved in the fusion event
cells_involved = unique(frame_fusion);
if cells_involved(1) == 0, cells_involved(1) = []; end

% Get the size of all cells in the image
cell_size = zeros(Highest_cell_number, 1);
for i = 1:numel(image_2), if image_2(i) > 0, cell_size(image_2(i)) = cell_size(image_2(i)) + 1; end, end
cell_size = cell_size > cell_size_threshold;

cell_sz = true(size(cell_size));
for i = 1:numel(cells_involved)
  cell_sz(cells_involved(i)) = cell_size(cells_involved(i));
end
cell_size = cell_sz;

% Check if there is a need to delete any cell, if not retuen
if all(cell_size), highest_cell_number = Highest_cell_number; return, end

% Create a renumber_cells vector that contains the renumbering of the cells with size > min_size
renumber_cells = zeros(Highest_cell_number+1, 1);
highest_cell_number = 0;
for i = 1:Highest_cell_number
  % if cell i is a cell with size > min_size, give it a new number
  if cell_size(i)
    highest_cell_number = highest_cell_number + 1;
    renumber_cells(i+1) = highest_cell_number;
  end
end

% Update frame_fusion matrix and the frame_track_vector
for i = 1:length(frame_track_vector)
  
  % if source cell is dead, continue, or a mother cell
  if isnan(frame_track_vector(i)) || frame_track_vector(i) == 0 , continue, end
  
  % if source cell i is tracked to a target cell frame_track_vector(i) with size < threshold. delete the track
  if ~renumber_cells(frame_track_vector(i)+1)
    frame_track_vector(i) = nan;
    frame_fusion(i,:) = 0;
    continue;
  end
  
  % Update the tracking number to the correct one after deleting small cells
  frame_track_vector(i) = renumber_cells(frame_track_vector(i)+1);
end

frame_fusion = renumber_cells(frame_fusion+1);

% update frame_division to reflect the numbering changes from deleting cells that were to small
temp = zeros(size(frame_division));
for i = 1:length(frame_division)
  if frame_division(i)
    mother_cell_nb = frame_division(i);
    temp(renumber_cells(i+1)) = mother_cell_nb;
  end
end
frame_division = temp;

% Delete small cells
BW = image_2 > 0;
image_2 = renumber_cells(image_2+1);

% [image_2, ~] = labeled_geodesic_dist(image_2, BW);
image_2 = assign_nearest_connected_label(image_2, BW);

% Get the closest neighbor to the deleted pixels
% [~,L] = bwdist(image_2);

% Replace with the closest neighbor
% image_2 = image_2(L);

% Delete the background pixels from the labeling matrix
% image_2(~BW) = 0;

end % end check cell size


