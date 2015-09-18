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




function Show_Birth_And_Death(tracked_images_path, XBorder, YBorder, MaxWindowX, MaxWindowY )

if tracked_images_path(end) ~= filesep
    tracked_images_path = [tracked_images_path filesep];
end

% Load Data and Birth And Death
if ~exist([tracked_images_path 'tracking_workspace.mat'],'file')
    errordlg(sprintf('Output of tracking not found:\nCheck paths for correctness\nRun segmentation and tracking before plotting output'))
    return
end

load([tracked_images_path 'tracking_workspace.mat']);
if ~exist('birth', 'var'), errordlg('Missing birth required.'); return; end
if ~exist('death', 'var'), errordlg('Missing death required.'); return; end

% Create figure in case not found
if ~isempty(findobj('type','figure','name','Birth And Death Window'))
    BD_fig = findobj('type','figure','name','Birth And Death Window');
    figure(BD_fig)
else    
    BD_fig = figure(...
    'units', 'pixels',... 
    'Position', [XBorder+MaxWindowX, YBorder, MaxWindowX*0.3, MaxWindowY], ... 
    'Name','Birth And Death Window',...
    'NumberTitle','off',...
    'Menubar','none',...
    'Toolbar','none',...
    'Resize', 'on');
end

%Scrollable EditBox
uitable('Parent',BD_fig,...
    'units','normalized',...
    'position',[0 0 1 1],...
    'ColumnName', {' Cell ID ', ' Birth ', ' Death '}, ...
    'fontweight','bold',...
    'RowName',[],...
    'Data',[(1:highest_cell_number)', birth, death]);
end



