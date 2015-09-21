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


% [centroids, cells_size] = get_info_frame(frame, nb_cells)
%  This is a sub-function of the main function "start_tracking.m".
% 
% This function extract the information from a given frame. The information contain the size of each cell and the
% centroid coordinates of each cell.
% 

function [centroids, cells_size] = get_info_frame(frame, nb_cells)

if nargin == 1
    nb_cells = max(frame(:));
end

% Initiate the vector "cells_size" containing the size of each cell. The size of each cell is the number of 
% pixels of this cell in the current frame.
cells_size = zeros(nb_cells, 1);

%  Initiate the matrix "centroids" containing the X and Y coordinates of the centroids of each cell
%  in the current frame.
centroids = zeros(nb_cells, 2);

% find the nonzero pixels in the image
[nonzero_pixels(:,1), nonzero_pixels(:,2)] = find(frame);

for counter = 1:size(nonzero_pixels,1)
    i = nonzero_pixels(counter,1);
    j = nonzero_pixels(counter,2);
    % Increment the size of the pixel
    cells_size(frame(i,j)) = cells_size(frame(i,j)) + 1;
    
    % Add the coordiante of the pixel to the sum of the centroids coordinates
    centroids(frame(i,j), 1) = centroids(frame(i,j), 1) + j;
    centroids(frame(i,j), 2) = centroids(frame(i,j), 2) + i;
end
% Compute the centroids
% centroids = round(centroids ./ (cells_size * [1 1]));
centroids = centroids ./ (cells_size * [1 1]);

