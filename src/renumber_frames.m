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
% This function will renumber the input frame according the the tracking and will save it in the specified folder
% defined by the user in the function "set_parameters"
% 

function [touching_vector, border_cells, perimeter, cell_circularity] = renumber_frames(frame, i_frame, number_cells, path_to_frames, frame_name, renumbering_vector, border_cells_in, cell_circularity, input_images_in_single_tif_flag)

% Get the image dimensions
[nb_rows, nb_cols] = size(frame);

% Initialize the output
frame1 = zeros(nb_rows,nb_cols);
touching_matrix = zeros(number_cells,number_cells);
border_cells = border_cells_in;
% perimeter = zeros(number_cells,1);
cell_size = zeros(number_cells,1);

stats = regionprops(frame, 'PerimeterOld');
perimeter = cell2mat({stats.PerimeterOld}');

% check if the number of cells is including any newly removed cells that no longer exist in the image
if numel(perimeter) ~= number_cells
    perimeter = vertcat(perimeter, zeros(number_cells-numel(perimeter),1));
end

% renumber the new frame1
frame1(frame>0) = renumbering_vector(frame(frame>0));

[ii, jj] = find(frame);
r = size(ii,1);
for k = 1:r
    i = ii(k);
    j = jj(k);
    pix = frame(i,j);
    cell_size(pix) = cell_size(pix) + 1;
    
    % Compute the touching borders based on 4-connectivity
    
    % check the pixel to the left, if it is different than pixel i,j and the background, update the
    % touching_vector.
    if j > 1 && frame(i,j-1) > 0 && pix ~= frame(i,j-1)
        % check if it is the first time we register touching pixels for cell with number = frame(i,j). Register the global cell number and
        % count the number of cells it is touching
        touching_matrix(pix,frame(i,j-1)) = touching_matrix(pix,frame(i,j-1)) + 1;
    end
    
    % check the pixel to the right, if it is different than pixel i,j and the background, update the
    % touching_vector.
    if j < nb_cols && frame(i,j+1) > 0 && pix ~= frame(i,j+1)
        % check if it is the first time we register touching pixels for cell with number = frame(i,j). Register the global cell number and
        % count the number of cells it is touching
        touching_matrix(pix,frame(i,j+1)) = touching_matrix(pix,frame(i,j+1)) + 1;
    end
    
    % check the pixel below, if it is different than pixel i,j and the background, update the
    % touching_vector
    if i < nb_rows && frame(i+1,j) > 0 && pix ~= frame(i+1,j)
        % check if it is the first time we register touching pixels for cell with number = frame(i,j). Register the global cell number and
        % count the number of cells it is touching
        touching_matrix(pix,frame(i+1,j)) = touching_matrix(pix,frame(i+1,j)) + 1;
    end
    
    % check the pixel above, if it is different than pixel i,j and the background, update the
    % touching_vector
    if i > 1 && frame(i-1,j) > 0 && pix ~= frame(i-1,j)
        % check if it is the first time we register touching pixels for cell with number = frame(i,j). Register the global cell number and
        % count the number of cells it is touching
        touching_matrix(pix,frame(i-1,j)) = touching_matrix(pix,frame(i-1,j)) + 1;
    end
end


circ = ((4*pi).*cell_size)./(perimeter.^2);
cell_circularity(renumbering_vector, i_frame) = circ;

% Transform the touching matrix data into cell structure to save space 
touching_vector = cell(number_cells,1);
for i = 1:number_cells
    % find the cell numbers that are neighbors to cell i
    indexes = find(touching_matrix(:,i));
    
    % Create the ouptut matrix where the first column is the neighboring cell number and the second one is the number of pixels on the
    % touching boundaries
    touching_vector{i} = [indexes touching_matrix(indexes,i)];
end

% Update the touching border cells
% Scout the upper and lower boundaries for the pixels that touch them
for k = 2:nb_cols-1

    % Assign the corresponding upper pixel number in the border_cells to 1 if that pixel is ~= 0.
    if frame1(2,k) > 0, border_cells(frame1(2,k)) = 1; end

    % Assign the corresponding lower pixel number in the border_cells to 1 if that pixel is ~= 0.
    if frame1(nb_rows-1,k) > 0, border_cells(frame1(nb_rows-1,k)) = 1; end
end

% Scout the upper and lower boundaries for the pixels that touch them
for r = 2:nb_rows-1

    % Assign the corresponding upper pixel number in the border_cells to 1 if that pixel is ~= 0.
    if frame1(r,2) > 0, border_cells(frame1(r,2)) = 1; end

    % Assign the corresponding lower pixel number in the border_cells to 1 if that pixel is ~= 0.
    if frame1(r,nb_cols-1) > 0, border_cells(frame1(r,nb_cols-1)) = 1; end
end

% write the image in the specified folder
if i_frame == 1 || ~input_images_in_single_tif_flag
    imwrite(uint16(frame1), [path_to_frames frame_name]);
else
    imwrite(uint16(frame1), [path_to_frames frame_name], 'WriteMode', 'append');
end





