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



% This function will plot the segmented image with each object in the object assigned with different color
function ploti(segmented_image, txt, colors_vector, shuffle)

% Extract the nb_rows and nb_columns of the segmented_image
[nb_rows, nb_columns] = size(segmented_image);

% Compute the number of objects in the image
objects_numbers = unique(segmented_image(:)); % including the background number which is 1
max_objects = objects_numbers(end); 
nb_objects = length(nonzeros(objects_numbers)); % number of objects without background

% If user specified a color use it, otherwise create new one with random shuffling
if nargin < 3 || isempty(colors_vector), colors_vector = jet(double(max_objects)); end
if nargin < 4 || shuffle == 1, colors_vector = colors_vector(randperm(max_objects),:); end
if nargin < 2, txt = 1; end

% Make colored image
image_RGB = label2rgb(segmented_image, colors_vector, 'k');

% Plot the segmented_image
image(image_RGB)
title(['Number of objects in image = ' num2str(nb_objects)]);
hold on

% Initialize the matrix text_location that contains the coordinates X and Y of a pixel that belongs to an object
text_location = zeros(max_objects, 2);

% Initialize the binary vector "first_occurance" that contains 0 if the object was already encountered 1 if it's
% the first time we encounter this object. This way the label of the object will always be on the top left
first_occurance = zeros(max_objects, 1);

% Scout all the image and assign a text location for each object
m = 1; % index for the lines of the text_location matrix
for j = 1:nb_columns
    for i = 1:nb_rows
        
        % if pixel(i,j) is a background pixel or the object was already set: continue
        if segmented_image(i,j) == 0 || first_occurance(segmented_image(i,j)) > 0, continue, end
        
        % Memorise the coordinates for that cell
        text_location(m, 1) = j;
        text_location(m, 2) = i;
        m = m + 1;
        % Set the first_occurance to false
        first_occurance(segmented_image(i,j)) = 1;
    end
end

% Place the number of the object in the segmented_image
if txt == 1
    for i = 1:nb_objects
        object_number = segmented_image(text_location(i,2), text_location(i,1));
        
        text(text_location(i,1), text_location(i,2), num2str(object_number), 'fontsize', 18,...
            'FontWeight', 'bold', 'Margin', .1, 'color', 'k', 'BackgroundColor', 'w')
    end
end

