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




function [segmented_image, nb_cell] = relabel_image(segmented_image)

% Create a renumber_cells vector that contains the renumbering of the cells in the labeled mask
nb_cell = max(segmented_image(:));
renumber_cells = zeros(nb_cell, 1);
[m, n] = size(segmented_image);

% Get unique cell ID
u = unique(segmented_image(:));
if u(1) == 0, u(1) = []; end % delete background pixel

for i = 1:length(u), renumber_cells(u(i)) = i; end
renumber_cells = [0;renumber_cells]; % Account for background

% Renumber image
segmented_image = renumber_cells(segmented_image+1);
segmented_image = reshape(segmented_image, m, n);

% Nb of cells
nb_cell = length(u);


