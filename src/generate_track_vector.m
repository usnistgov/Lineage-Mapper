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




function track_vector = generate_track_vector(frame_cost)

% Find the minimum of all the rows of the cost matrix and stock the indexes (the number of the corresponding 
% target cells) into the frame_track_vector.
% The min_cost_vector is a vector that has the minimum cost value between each source cell and its corresponding
% target cell that might be tracked to.
[min_vals, track_vector] = min(frame_cost, [], 2);
% Start looping through all the line or source cells of the frame_track_vector to check for any duplicate
% mapping.
for i = 1:numel(track_vector)
    if isnan(min_vals(i))
        track_vector(i) = NaN;
    end
end



