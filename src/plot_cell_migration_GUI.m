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




function plot_cell_migration_GUI(tracked_images_path, user_defined_cells)

if tracked_images_path(end) ~= filesep
    tracked_images_path = [tracked_images_path filesep];
end

% Load Data 
if ~exist([tracked_images_path 'tracking_workspace.mat'],'file')
    errordlg(sprintf('Output of tracking not found:\nCheck paths for correctness\nRun segmentation and tracking before plotting output'))
    return
end
load([tracked_images_path 'tracking_workspace.mat']);

if ~exist('segmented_images_path', 'var'), errordlg('Missing segmented_images_path required.'); return; end
if ~exist('input_files', 'var'), errordlg('Missing input_files required.'); return; end
if ~exist('highest_cell_number', 'var'), errordlg('Missing highest_cell_number required.'); return; end
if ~exist('XY_centroids', 'var'), errordlg('Missing XY_centroids required.'); return; end
if ~exist('birth', 'var'), errordlg('Missing birth required.'); return; end
if ~exist('death', 'var'), errordlg('Missing death required.'); return; end


% get the size of one of the images
info = imfinfo([segmented_images_path filesep input_files(1).name]);
size_I = [info.Height, info.Width];

% Convert user input to numeric
user_defined_cells = str2num(user_defined_cells); %#ok<ST2NM>

% Check Validity of user input
if any(user_defined_cells < 1) || any(user_defined_cells > highest_cell_number)
    errordlg(['Invalid Cell Numbers. Please choose between 1 and ' num2str(highest_cell_number)]);
    return;
end

% Plot Cell Lineage
plot_centroids_trajectory_2D(user_defined_cells, XY_centroids, birth, death, size_I)


