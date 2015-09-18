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




function frame_track_vector_updated = hungarian_optimization(number_cells_2, number_cells_1, frame_track_vector, frame_division, frame_fusion, frame_cost)
% Refine the tracking by checking if any possible track can be made between the remaining untracked target cells
% and the remaining untracked source cells without taking the division cell into consideration.
% We do this by creating a temporary frame_cost matrix that is the same as the actual frame_cost but with all the
% rows and columns, of the cells with track, filled by nan.

% Find the untracked target cells without the daughters cells if any.
% Find the untracked target cells.
frame_track_vector_updated = frame_track_vector;
untracked_target_cells = zeros(number_cells_2,1);
for j = 1:number_cells_1
    % if the target cell number frame_track_vector(j) has been assigne, memorize it
    if frame_track_vector(j)>0, untracked_target_cells(frame_track_vector(j)) = j; end
end
% Get the untracked cells and not the tracked ones
untracked_target_cells = ~untracked_target_cells;

% Delete the cells that came from mitosis
untracked_target_cells(frame_division>0) = 0;

% delete cells that came from fusion and are dead due to size or when fusion is disabled
cell_idx = unique(frame_fusion(:)); 
cell_idx(cell_idx<1) = [];
% S = sum(frame_fusion>0,1);
% cell_idx = find(S>1);
untracked_target_cells(cell_idx) = 0;

% Check to see if there is a need to look for a possible division. if not then return
if ~any(~untracked_target_cells), return, end 

% Find the untracked source cells from the original frame_track_vector
untracked_source_cells = isnan(frame_track_vector);

% delete cells that came from fusion and are dead due to size or when fusion is disabled
for i = 1:length(untracked_source_cells)
    if any(frame_fusion(i,:)), untracked_source_cells(i) = 0; end
end

% if no untracked source cells found: return
if ~any(~untracked_source_cells), return, end

% Check if there is still untracked source cells that might be tracked to unmapped target cells.
% 
% Initialize the frame_cost_temp
frame_cost_temp = frame_cost;

% Delete the costs between the tracked cells
frame_cost_temp(~untracked_source_cells, :) = nan;
frame_cost_temp(:, ~untracked_target_cells) = nan;

% Search recursively for a possible track between the unmapped source cells and the unmapped target if any.
% frame_track_vector_updated  = assign_new_track(frame_cost_temp, frame_track_vector_updated);
frame_track_vector_updated  = assign_new_track(frame_cost_temp, frame_track_vector);


end



% Recursive function that searches for a possible track between the unmapped source cells and the unmapped target
function [track_vector_out, cost_matrix_out] = assign_new_track(cost_matrix_in, track_vector_in)

% Initialize the outputs
cost_matrix_out = cost_matrix_in;
track_vector_out = track_vector_in;

% Check is there is still any tracks that can be assigned. The stopping condition
if isnan(cost_matrix_out), return, end

[row_min, row_min_index] = min(cost_matrix_in,[],2); % minimum rowwise
[col_min, col_min_index] = min(cost_matrix_in); %#ok<ASGLU> % minimum columnwise

nb_cells_1 = length(row_min_index);

for i = 1:nb_cells_1
    
    % If the source cell is dead: continue
    if isnan(row_min(i)), continue, end
    
    % If we found a perfect match between the source cell i and the target cell j then assign a track.
    % j is row_min_index(i).
    if col_min_index(row_min_index(i)) == i
        track_vector_out(i) = row_min_index(i);
        cost_matrix_out(:, row_min_index(i)) = nan;
        cost_matrix_out(i,:) = nan;
    end
end

% Check for the remaining cells and reloop until no more tracks can be assigned. This means that the cost_matrix
% becomes all invalid (nan)
[track_vector_out, cost_matrix_out] = assign_new_track(cost_matrix_out, track_vector_out);

end