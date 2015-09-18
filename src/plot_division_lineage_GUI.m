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



function plot_division_lineage_GUI(tracked_images_path, plot_text, cell_nb)

if tracked_images_path(end) ~= filesep
    tracked_images_path = [tracked_images_path filesep];
end

% Load Data 
if ~exist([tracked_images_path 'tracking_workspace.mat'],'file')
    errordlg(sprintf('Output of tracking not found:\nCheck paths for correctness\nRun segmentation and tracking before plotting output'))
    return
end
load([tracked_images_path 'tracking_workspace.mat']);

if ~exist('highest_cell_number', 'var'), errordlg('Missing highest_cell_number required.'); return; end
if ~exist('birth', 'var'), errordlg('Missing birth required.'); return; end
if ~exist('death', 'var'), errordlg('Missing death required.'); return; end
if ~exist('division_matrix', 'var'), errordlg('Missing division_matrix required.'); return; end
if ~exist('nb_frames', 'var'), errordlg('Missing nb_frames required.'); return; end

% Convert user input to numeric
cell_nb = str2num(cell_nb); %#ok<ST2NM>

% Check Validity of user input
if any(cell_nb < 0) || any(cell_nb > highest_cell_number)
    errordlg(['Invalid Cell Numbers. Please choose between 0 and ' num2str(highest_cell_number)]);
    return;
end

% Plot Cell Lineage
for i = 1:length(cell_nb)
    plot_cell_division_lineage(cell_nb(i), plot_text, highest_cell_number, birth, death, division_matrix, nb_frames)
end
