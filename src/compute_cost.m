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



% - Compute the distance "delta_centroid" between the centroids of the cells_pair.
% if delta_centroid is bigger than an allowed user defined distance "max_centroids_distance" then cost of this
% cells_pair is set to nothing.
% 
% Compute the overlap_term = 1 - #overlaps / (2 * source_cell_size) + #overlaps / (2 * target_cell_size)
% the overlap_term is made in a way that if the source cell overlaps completely with the target cell and has the
% same size then the #overlaps = source_cell_size = target_cell_size and the overlap_term = 0. This means that
% this is a perfect cells_pair. The lower the overlap_term the higher the probability that this cells_pair is a
% good match. The overlap_term is limited between 0 & 1, 0 < overlap_term < 1
% 
% Compute the centroid_term = delta_centroid/diagonal_distance
% the centroid_term is made in a way that if the cells_pair were completely overlaping and the same size and
% shape then their two centroids will coincide and delta_centroid = 0 => centroid_term = 0. This means that
% this is a perfect cells_pair. The lower the centroid_term the higher the probability that this cells_pair is a
% good match. The diagonal_distance is the maximum centroid distance that is possible in the
% selected image. It is used to render the 0 < centroid_term < 1
% 
% Compute the size_term = abs(source_cell_size - target_cell_size) / max(source_cell_size, target_cell_size)
% the size_term is made in a way that if the cells_pair were a good match they will have the same size and
% size_term = 0. The lower the size_term the higher the probability that this cells_pair is a good match. 
% The division of the difference in sizes over the maximum size is used to render the 0 < size_term < 1
% 
% cost_term = (weight_overlap * overlap_term) + (weight_centroids * centroid_term) + (weight_size * size_term)
% 

function frame_cost = compute_cost(frame_overlap, centroids_1, centroids_2, cells_size_1, cells_size_2, ...
    number_cells_1, number_cells_2, weight_overlap, weight_centroids, weight_size, max_centroids_distance)

% Expand input vectors to matrices
M_centroids_1_X = repmat(centroids_1(:,1),1,number_cells_2);
M_centroids_1_Y = repmat(centroids_1(:,2),1,number_cells_2);
M_centroids_2_X = repmat((centroids_2(:,1))',number_cells_1,1);
M_centroids_2_Y = repmat((centroids_2(:,2))',number_cells_1,1);
M_cells_size_1 = repmat(cells_size_1,1,number_cells_2);
M_cells_size_2 = repmat(cells_size_2',number_cells_1,1);

% Compute the centroid_term. delta_centroid is the distance between a source cell centroid and a target cell centroid.
centroid_term = sqrt( (M_centroids_1_X - M_centroids_2_X).^2 + (M_centroids_1_Y - M_centroids_2_Y).^2 ) / max_centroids_distance;

% Compute the overlap_term
overlap_term = 1 - (frame_overlap./(2*M_cells_size_1) + frame_overlap./(2*M_cells_size_2));

% Compute the size_term
size_term = abs(M_cells_size_1 - M_cells_size_2) ./ max(M_cells_size_1, M_cells_size_2);

% Compute the cost
frame_cost = weight_overlap*overlap_term + weight_centroids*centroid_term + weight_size*size_term;

% Set to nan the invalid track costs
frame_cost(overlap_term == 1 & centroid_term > 1) = nan;
