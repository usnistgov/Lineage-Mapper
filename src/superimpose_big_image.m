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




function image_RGB = superimpose_big_image(I1, segmented_image, txt, colors_vector, shuffle)

% Compute the number of objects in the image
objects_numbers = unique(segmented_image(:)); % including the background number which is 1
max_objects = double(objects_numbers(end));
if objects_numbers(1) == 0, objects_numbers(1) = []; end    % without background
nb_objects = length(objects_numbers); 

% If user specified a color use it, otherwise create new one with random shuffling
if nargin < 4 || isempty(colors_vector), colors_vector = jet(max_objects); end
if nargin < 5 || shuffle == 1, colors_vector = colors_vector(randperm(max_objects),:); end
if nargin < 3, txt = 0; end

% Adjust contrast
I1 = imadjust(uint16(I1));
[number_rows, number_columns] = size(I1);

% Convert the matrix to a scaled image with intensities between 0 and 1
I1 = mat2gray(I1); I1 = I1(:);

% Find the edges of segmentation and tracking of the image
[edge_image, text_location] = find_edges(segmented_image, nb_objects);

% Dilate the edge_image to thicken the contour plot
edge_image = imdilate(edge_image, strel('square', 1)); edge_image = edge_image(:);

% Fuse the two images
image_RGB = [I1, I1, I1];
image_RGB(edge_image>0,:) = colors_vector(nonzeros(edge_image),:);
image_RGB = reshape(image_RGB, [number_rows number_columns 3]);

if nargout == 0
    % Plot the edge contoured resulting image
    figure, image(image_RGB)
    hold on
    title(['Total Cells = ' num2str(nb_objects)])

    % Place the number of the cell in the image
    if txt
        for i = 1:nb_objects
            cell_number = segmented_image(text_location(i,2), text_location(i,1));

            text(text_location(i,1), text_location(i,2), num2str(cell_number), 'fontsize', 8, ...
                'FontWeight', 'bold', 'Margin', .1, 'color', colors_vector(cell_number,:), 'BackgroundColor', 'w')
        end
    end
end



function [edge_image, text_location, perimeter] = find_edges(segmented_image, nb_cells)

% Compute the image dimensions
[nb_rows, nb_columns] = size(segmented_image);

% highest_number in the image
highest_number = max(segmented_image(:));

% Initialize the output image that contains only the edge pixels of the cells
edge_image = zeros(nb_rows, nb_columns, 'uint16');

% Initialize the perimeter vector that contains the number of pixel on the perimeter of each cell in the image
perimeter = zeros(highest_number, 1);

% Initialize the matrix text_location that contains the coordinates X and Y of the a pixel that belongs to the
% edge of a cell
text_location = zeros(nb_cells, 2);

% Initialize the binary vector "first_occurance" that contains 0 if the cell was already encountered 1 if it's
% the first time we encounter this cell
first_occurance = ones(highest_number, 1);

% Scout all the pixels in segmented_image looking for the edge ones. An edge pixel will have at least one of it's
% eight neighbors that haas a different value than the pixel itself
m = 1; % index for the lines of the text_location matrix
for j = 1:nb_columns
    for i = 1:nb_rows
        
        % if pixel(i,j) is a background pixel: continue
        if segmented_image(i,j) == 0, continue, end
        
        % if pixel(i,j) is on the left border of the image, or if pixel(i,j) is on the right border of the image,
        % or if pixel(i,j) is on the top border of the image or if pixel(i,j) is on the top border of the image,
        % or if one of the pixels around pixel (i,j) ==> pixel (i,j) is an edge pixel
        if j == 1 || j == nb_columns || i == 1 || i == nb_rows || ...
                segmented_image(i-1,j-1) ~= segmented_image(i,j) || ...
                segmented_image(i-1,j) ~= segmented_image(i,j) || ...
                segmented_image(i-1,j+1) ~= segmented_image(i,j) || ...
                segmented_image(i,j-1) ~= segmented_image(i,j) || ...
                segmented_image(i,j+1) ~= segmented_image(i,j) || ...
                segmented_image(i+1,j-1) ~= segmented_image(i,j) || ...
                segmented_image(i+1,j) ~= segmented_image(i,j) || ...
                segmented_image(i+1,j+1) ~= segmented_image(i,j)
            
            % Assign pixel (i,j) as an edge pixel
            edge_image(i,j) = segmented_image(i,j);
            
            % Increase the size of the perimeter for cell(i,j)
            perimeter(edge_image(i,j)) = perimeter(edge_image(i,j)) + 1;
            
            % if it is the first time we encounter that cell, memorise the pixel location
            if first_occurance(edge_image(i,j)) > 0
                % Memorise the coordinates for that cell
                text_location(m, 1) = j;
                text_location(m, 2) = i;
                m = m + 1;
                % Set the first_occurance to false
                first_occurance(edge_image(i,j)) = 0;
            end
        end
    end
end


