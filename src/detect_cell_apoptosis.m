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



function [cell_apoptosis] = detect_cell_apoptosis(XY_centroids, cells_size_matrix, cell_life_threshold, cell_apoptosis_delta_centroid_thres)


% cell apoptosis (death) is defined as when a cell does not move (drifts only with the background) and the size becomes
% very stable.
% detect cell apoptosis by looking at the cell sizes. If the cell size becomes stable, look at the centroids stability
% the centroids should only drift with the background, this requires a distance threshold for per frame translation

% XY_centroids is (frame_nb, cell_nb, [x y]);

cell_apoptosis = zeros(size(cells_size_matrix,1),1);

% loop over each cell
for cell_nb = 1:size(cells_size_matrix,1)
    % extract the current row of cell sizes removing any zero values. The zeros are placeholders for frames the cell was
    % not alive
    cur_row = nonzeros(cells_size_matrix(cell_nb,:));
    % if the cell meets the lifespan threshold defined to be valid
    if numel(cur_row) < cell_life_threshold, continue; end
    
    % determine the size threshold that detemines if the cell size is stable
    % this is defined as 10% of the mean cell size
    diff_thres = 0.1*mean(cur_row); 
    % find the difference in size between each frame over the life of the cell
    diff_vec = abs(diff(cur_row));
    % find the locations where the cell size changed less than 10% of the average size
    indx = diff_vec < diff_thres;
    % record the lifespan of the current cell
    lifespan = numel(cur_row);
    
    % find the highest frame number where there has been a continuously stable cell size until the cell died
    % do this be searching the cell size difference vector back to front until a cell size difference is found that is
    % above the threshold
    % this results in finding the first frame where the cell size is stable
    i = lifespan-1;
    while i > 1
        if ~indx(i)
            i = i + 1; % reset i to point to the index past the current index
            break;
        end
        i = i - 1;
    end
    % at this point i:end is a stable cell
    if (lifespan-i+1) > cell_life_threshold
        % check the centroids to see if the cell is stable in location
        
        % create the distance vector holding the dist traveled between each frame
        dist_vec = NaN(lifespan-1,1);
        for k = i:lifespan-1
            dist_vec(k) = sqrt((XY_centroids(k,cell_nb,1)-XY_centroids(k+1,cell_nb,1)).^2 + (XY_centroids(k,cell_nb,2)-XY_centroids(k+1,cell_nb,2)).^2);
        end
        dist_vec(isnan(dist_vec)) = [];
        if mean(dist_vec) < cell_apoptosis_delta_centroid_thres
            % this is a dead cell
            cell_apoptosis(cell_nb) = 1;
        end
        
    end
    
    
end


end


