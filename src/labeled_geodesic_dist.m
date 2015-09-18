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



function [marker_matrix, dist_mat] = labeled_geodesic_dist(marker_matrix, mask_matrix)

assert(islogical(mask_matrix));

marker_matrix = padarray(marker_matrix, [1, 1]);
mask_matrix = padarray(mask_matrix, [1, 1]);
[r, c] = size(mask_matrix);

dist_mat = inf(r,c);
dist_mat(marker_matrix>0) = 0;
dist_mat(~mask_matrix) = NaN;

init = 100; 
indx = 0;
loc_vec = zeros(init,1);
lab_vec = zeros(init,1);
neighbors = zeros(8,1);
% find edge pixels
[ii, jj] = find(marker_matrix);
for k = 1:numel(ii)
    i = ii(k);
    j = jj(k);
    neighbors(1) = marker_matrix(i-1,j-1);
    neighbors(2) = marker_matrix(i,j-1);
    neighbors(3) = marker_matrix(i+1,j-1);
    neighbors(4) = marker_matrix(i-1,j);
    neighbors(5) = marker_matrix(i+1,j);
    neighbors(6) = marker_matrix(i-1,j+1);
    neighbors(7) = marker_matrix(i,j+1);
    neighbors(8) = marker_matrix(i+1,j+1);
    if any(neighbors == 0)
        push(i,j);
    end

end
edge_pixels = [loc_vec(1:indx) lab_vec(1:indx)];
indx = 0;

iteration_count = 0;

while size(edge_pixels,1) > 0
    iteration_count = iteration_count + 1;
    
    for k = 1:size(edge_pixels,1)
        % for each pixel that has a label, find all its neighbors that dont have a label and are valid for traversal
        i = edge_pixels(k,1);
        j = edge_pixels(k,2);
        if i > 0 && j > 0
            label = marker_matrix(i,j);
            % check 4 connected nhood
            ii = i; jj = j-1; % left
            if marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj)
                marker_matrix(ii,jj) = label;
                push(ii,jj);
                edge_pixels(k,1) = -1;
                edge_pixels(k,2) = -1;
            end
            ii = i - 1; jj = j; % top
            if marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj) 
                marker_matrix(ii,jj) = label;
                push(ii,jj);
                edge_pixels(k,1) = -1;
                edge_pixels(k,2) = -1;
            end
            ii = i + 1; jj = j; % bottom
            if marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj) 
                marker_matrix(ii,jj) = label;
                push(ii,jj);
                edge_pixels(k,1) = -1;
                edge_pixels(k,2) = -1;
            end
            ii = i; jj = j+1; % right
            if marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj) 
                marker_matrix(ii,jj) = label;
                push(ii,jj);
                edge_pixels(k,1) = -1;
                edge_pixels(k,2) = -1;
            end
        end
    end
    for k = 1:size(edge_pixels,1)
        % for each pixel that has a label, find all its neighbors that dont have a label and are valid for traversal
        i = edge_pixels(k,1);
        j = edge_pixels(k,2);
        if i > 0 && j > 0
            label = marker_matrix(i,j);
            % check unfound 8 connected nhood
            ii = i - 1; jj = j-1; % top left
            if marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj)
                marker_matrix(ii,jj) = label;
                push(ii,jj);
                edge_pixels(k,1) = -1;
                edge_pixels(k,2) = -1;
            end
            ii = i + 1; jj = j-1; % bottom left
            if marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj) 
                marker_matrix(ii,jj) = label;
                push(ii,jj);
                edge_pixels(k,1) = -1;
                edge_pixels(k,2) = -1;
            end
            ii = i-1; jj = j+1; % top right
            if marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj) 
                marker_matrix(ii,jj) = label;
                push(ii,jj);
                edge_pixels(k,1) = -1;
                edge_pixels(k,2) = -1;
            end
            ii = i+1; jj = j+1; % bottom right
            if marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj)
                marker_matrix(ii,jj) = label;
                push(ii,jj);
                edge_pixels(k,1) = -1;
                edge_pixels(k,2) = -1;
            end
        end
    end
    
    % assign the label as the most dominant neighbor
    
    
    % update the new edge pixels
    edge_pixels = [loc_vec(1:indx) lab_vec(1:indx)];
    indx = 0;
    % update the distance matrix
    for k = 1:size(edge_pixels,1)
        dist_mat(edge_pixels(k,1),edge_pixels(k,2)) = iteration_count;
    end
   
    
    temp = (edge_pixels(:,2)-1).*r + edge_pixels(:,1);
    [~, ind_vec] = sort(temp, 'ascend');
    edge_pixels = edge_pixels(ind_vec,:);


end

marker_matrix = marker_matrix(2:end-1,2:end-1);
dist_mat = dist_mat(2:end-1,2:end-1);

    function push(val1, val2)
        if indx >= numel(loc_vec)
            loc_vec = [loc_vec; zeros(init,1)];
            lab_vec = [lab_vec; zeros(init,1)];
            init = numel(loc_vec);
        end
        indx = indx + 1;
        loc_vec(indx) = val1;
        lab_vec(indx) = val2;
    end



end


