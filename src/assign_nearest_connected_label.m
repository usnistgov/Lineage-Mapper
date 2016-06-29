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


% function marker_matrix = assign_nearest_connected_label(marker_matrix, mask_matrix)
% assignes all of the true pixels in mask_matrix the value of the nearest connected pixel in marker_matrix
function marker_matrix = assign_nearest_connected_label(marker_matrix, mask_matrix)


% labeled_geodesic_dist_mex
% check for the mex file binaries that are required to run the mex file
mex_file_name = ['labeled_geodesic_dist_mex.' mexext];
if ~exist(mex_file_name, 'file') && exist('labeled_geodesic_dist_mex.c','file')
    % check the compiler configuration, do not compile unless there is a selected C Compiler
    try
        if ~isempty(mex.getCompilerConfigurations('C','Selected'))
            eval(['mex ' which('labeled_geodesic_dist_mex.c')]);
        end
    catch err %#ok<NASGU>
    end
%     mex labeled_geodesic_dist_mex.c; % compile the c file
end
if exist(mex_file_name, 'file')
    marker_matrix = labeled_geodesic_dist_mex(marker_matrix, mask_matrix);
    return;
end

[m,n] = size(marker_matrix);

assert(islogical(mask_matrix));
assert(size(mask_matrix,1) == m);
assert(size(mask_matrix,2) == n);
init = m; 
indx = 0;
loc_vec = zeros(init,1);
lab_vec = zeros(init,1);

% find edge pixels
[ii, jj] = find(marker_matrix);
for k = 1:numel(ii)
    i = ii(k);
    j = jj(k);
    if i == 1 || j == 1 || i == m || j == n || ...
        ~marker_matrix(i-1,j-1) || ...
        ~marker_matrix(i,j-1) || ...
        ~marker_matrix(i+1,j-1) || ...
        ~marker_matrix(i-1,j) || ...
        ~marker_matrix(i+1,j) || ...
        ~marker_matrix(i-1,j+1) || ...
        ~marker_matrix(i,j+1) || ...
        ~marker_matrix(i+1,j+1)
    
        push(i,j);
    end
end

edge_pixels = [loc_vec(1:indx) lab_vec(1:indx)];
indx = 0;

while size(edge_pixels,1) > 0
    
    for k = 1:size(edge_pixels,1)
        % for each pixel that has a label, find all its neighbors that dont have a label and are valid for traversal
        i = edge_pixels(k,1);
        j = edge_pixels(k,2);
        label = marker_matrix(i,j);
        % check 4 connected nhood
        ii = i; jj = j-1; % left
        if jj > 0 && marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj)
            marker_matrix(ii,jj) = label;
            push(ii,jj);
            edge_pixels(k,1) = -1;
        end
        ii = i - 1; jj = j; % top
        if ii > 0 && marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj) 
            marker_matrix(ii,jj) = label;
            push(ii,jj);
            edge_pixels(k,1) = -1;
        end
        ii = i + 1; jj = j; % bottom
        if ii < m && marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj) 
            marker_matrix(ii,jj) = label;
            push(ii,jj);
            edge_pixels(k,1) = -1;
        end
        ii = i; jj = j+1; % right
        if jj < n && marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj) 
            marker_matrix(ii,jj) = label;
            push(ii,jj);
            edge_pixels(k,1) = -1;
        end
    end
    for k = 1:size(edge_pixels,1)
        % for each pixel that has a label, find all its neighbors that dont have a label and are valid for traversal
        i = edge_pixels(k,1);
        j = edge_pixels(k,2);
        if i > 0
            label = marker_matrix(i,j);
            % check unfound 8 connected nhood
            ii = i - 1; jj = j-1; % top left
            if ii > 0 && jj > 0 && marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj)
                marker_matrix(ii,jj) = label;
                push(ii,jj);
            end
            ii = i + 1; jj = j-1; % bottom left
            if ii < m && jj > 0 && marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj) 
                marker_matrix(ii,jj) = label;
                push(ii,jj);
            end
            ii = i-1; jj = j+1; % top right
            if ii > 0 && jj < n && marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj) 
                marker_matrix(ii,jj) = label;
                push(ii,jj);
            end
            ii = i+1; jj = j+1; % bottom right
            if ii < m && jj < n && marker_matrix(ii,jj) == 0 &&  mask_matrix(ii,jj)
                marker_matrix(ii,jj) = label;
                push(ii,jj);
            end
        end
    end
    
    % update the new edge pixels
    edge_pixels = [loc_vec(1:indx) lab_vec(1:indx)];
    indx = 0;
    
    temp = (edge_pixels(:,2)-1).*m + edge_pixels(:,1);
    [~, ind_vec] = sort(temp, 'ascend');
    edge_pixels = edge_pixels(ind_vec,:);
end

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
