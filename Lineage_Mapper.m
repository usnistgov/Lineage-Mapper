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


% Lineage_Mapper(varargin)
% 
% Run without any inputs to open the GUI.
% Run with 2 or more inputs to run in batch mode.
% 
% Batch Mode
% Input ('String', value) pairs 
%   'String' is the variable value is assigned to.
% 	Ex: 'Raw Images Path', 'C:\Images\'
% 
% "Default"                                 <none> 
% "Raw Images Path"                         <String>
% "Segmented Images Path"                   <String> 
% "Tracked Images Path"                     <String> 
% "Segmented Images Common Name"            <String>
% "Raw Images Common Name"                  <String>
% "Tracked Images Common Name"              <String>
% "Weight Overlap"                          <numeric>
% "Weight Centroids"                        <numeric>
% "Weight Size"                             <numeric>
% "Max Centroids Distance"                  <numeric>
% "Frames To Track"                         <String>
% "Min Mitotic Overlap"                     <numeric>
% "Daughter Size Similarity"                <numeric>
% "Daughter Aspect Ratio Similarity"        <numeric>
% "Circularity Index"                       <numeric>
% "Number Frames to Check Circularity"      <numeric>
% "Enable Cell Mitosis"                     <boolean>
% "Min Cell Life CI Threshold"              <numeric>
% "Cell Death Delta Centroid Threshold"     <numeric>
% "Cell Density Affects CI"                 <boolean>
% "Border Cell Affects CI"                  <boolean>
% "Cell Fusion Min Area"                    <numeric>
% "Min Cell Fusion Overlap"                 <numeric>
% "Enable Cell Fusion"                      <boolean>



function Lineage_Mapper(varargin)

% add the folder 'Sub_Functions' to the Matlab search path so it finds any
% functions placed in that subfolder
if ~isdeployed
    addpath([pwd filesep 'src']);
    addpath([pwd filesep 'doc']);
end

% ----------------------------------------------------------------------
% Global Variables
% ----------------------------------------------------------------------

% Folder path params
raw_images_path = '';
segmented_images_path = '';
tracked_images_path = '';
segmented_images_common_name = '';
raw_images_common_name = '';
tracked_images_common_name = '';

% Cost function params
weight_overlap = 100;
weight_centroids = 50;
weight_size = 20;
max_centroids_distance = 150;
frames_to_track = 'All';

% Mitotic params
division_overlap_threshold = 20;
daughter_size_similarity = 50;
daughter_aspect_ratio_similarity = 70;
circularity_threshold = 30;
number_of_frames_check_circularity = 5;
enable_cell_mitosis_flag = 1;

% Confidence Index params
cell_life_threshold = 32;
cell_apoptosis_delta_centroid_thres = 10;
cell_density_ci_flag = 1;
border_cell_ci_flag = 1;


% Fusion params
cell_size_threshold = 200;
fusion_overlap_threshold = 20;
enable_cell_fusion_flag = 0;

% Non-user params
frames_to_track_nb = 0;
nb_frames = 0;
max_cell_num = 12000;

params = {'raw_images_path',...
    'segmented_images_path',...
    'tracked_images_path',...
    'segmented_images_common_name',...
    'raw_images_common_name',...
    'tracked_images_common_name',...
    'weight_overlap',...
    'weight_centroids',...
    'weight_size',...
    'max_centroids_distance',...
    'frames_to_track',...
    'division_overlap_threshold',...
    'daughter_size_similarity',...
    'daughter_aspect_ratio_similarity',...
    'circularity_threshold',...
    'number_of_frames_check_circularity',...
    'enable_cell_mitosis_flag',...
    'cell_life_threshold',...
    'cell_apoptosis_delta_centroid_thres',...
    'cell_density_ci_flag',...
    'border_cell_ci_flag',...
    'cell_size_threshold',...
    'fusion_overlap_threshold',...
    'enable_cell_fusion_flag'};


batch_mode = false;
if nargin > 0
    % This runs batch mode
    batch_mode = true;
	%                     Input String, Type, Variable Name
    batchmode_inputs = {{'Default', 'none', ''},...
                        {'Raw Images Path', 'char', 'raw_images_path'},...
                        {'Segmented Images Path', 'char', 'segmented_images_path'},...
                        {'Tracked Images Path', 'char', 'tracked_images_path'},...
                        {'Segmented Images Common Name', 'char', 'segmented_images_common_name'},...
                        {'Raw Images Common Name', 'char', 'raw_images_common_name'},...
                        {'Tracked Images Common Name', 'char', 'tracked_images_common_name'},...
                        {'Weight Overlap', 'numeric', 'weight_overlap'},...
                        {'Weight Centroids', 'numeric', 'weight_centroids'},...
                        {'Weight Size', 'numeric', 'weight_size'},...
                        {'Max Centroids Distance', 'numeric', 'max_centroids_distance'},...
                        {'Frames To Track', 'char', 'frames_to_track'},...
                        {'Min Mitotic Overlap', 'numeric', 'division_overlap_threshold'},...
                        {'Daughter Size Similarity', 'numeric', 'daughter_size_similarity'},...
                        {'Daughter Aspect Ratio Similarity', 'numeric', 'daughter_aspect_ratio_similarity'},...
                        {'Circularity Index', 'numeric', 'circularity_threshold'},...
                        {'Number Frames to Check Circularity', 'numeric', 'number_of_frames_check_circularity'},...
                        {'Enable Cell Mitosis', 'logical', 'enable_cell_mitosis_flag'},...
                        {'Min Cell Life CI Threshold', 'numeric', 'cell_life_threshold'},...
                        {'Cell Death Delta Centroid Threshold', 'numeric', 'cell_apoptosis_delta_centroid_thres'},...
                        {'Cell Density Affects CI', 'logical', 'cell_density_ci_flag'},...
                        {'Border Cell Affects CI', 'logical', 'border_cell_ci_flag'},...
                        {'Cell Fusion Min Area', 'numeric', 'cell_size_threshold'},...
                        {'Min Cell Fusion Overlap', 'numeric', 'fusion_overlap_threshold'},...
                        {'Enable Cell Fusion', 'logical', 'enable_cell_fusion_flag'} };
    
    % parse inputs
    % If the user is passing values into this function, that means they are attempting to use this in batch mode without the GUI being displayed
    indx = 1;
    while indx <= nargin
        % loop over the arguments passed in using varargin
        % if the argument passed in is a string, check to see if it is one of the variables that can be altered
        if isa(varargin{indx}, 'char')
            cur_str = varargin{indx}; % extract the current variable name string
            % remove any non alphanumberic values
            cur_str = regexprep(cur_str,'[^a-zA-Z0-9]+',''); 
            % loop over possible values for this input string
            found_flag = false;
            for k = 1:numel(batchmode_inputs)
                cmp_str = strrep(batchmode_inputs{k}{1},' ' ,'');
                if strcmpi(cur_str, cmp_str)
                    % match found
                    if strcmpi(batchmode_inputs{k}{2}, 'none')
                        % set the default params
                        batchmode_set_default_parameters();
                        indx = indx - 1;
                        found_flag = true; break;
                    end
                    % test that the value portion of the string value pair is the correct type
                    if indx < nargin
                        val = varargin{indx+1};
                        switch batchmode_inputs{k}{2}
                            case 'char'
                                if isa(val, 'char')
                                    eval([batchmode_inputs{k}{3} '=val;']);
                                    found_flag = true; break;
                                end
                                if isa(val, 'numeric')
                                    eval([batchmode_inputs{k}{3} '=num2str(val);']);
                                    found_flag = true; break;
                                end
                            case 'numeric'
                                if isa(val, 'char')
                                    eval([batchmode_inputs{k}{3} '=str2num(val);']);
                                    found_flag = true; break;
                                end
                                if isa(val, 'numeric')
                                    eval([batchmode_inputs{k}{3} '=' num2str(val) ';']);
                                    found_flag = true; break;
                                end
                            case 'logical'
                                if isa(val, 'char')
                                    eval([batchmode_inputs{k}{3} '=logical(str2num(val));']);
                                    found_flag = true; break;
                                end
                                if isa(val, 'logical') || isa(val, 'numeric')
                                    eval([batchmode_inputs{k}{3} '=logical(' num2str(val) ');']);
                                    found_flag = true; break;
                                end
                        end
                    end
                end
            end
            if found_flag
                indx = indx + 1; % skip the next index as it was already used
            else
                cur_str = varargin{indx}; % extract the current variable name string
                error('Cell_tracker_GUI_v4:argCk',['Invalid Input String <' cur_str '>']);
            end
        end
        indx = indx + 1;
    end
    
    if ~check_input_batchmode(), return; end
    
    start_tracking_Callback();
    return;
end


% check if the GUI is already open, if so do nothing and return
open_fig_handle = findobj('type','figure','name','Lineage_Mapper');
if ~isempty(open_fig_handle)
    figure(open_fig_handle); %brings the figure to front
    return;
end


% Define General colors
lt_gray = [0.86,0.86,0.86];
dark_gray = [0.7,0.7,0.7];
green_blue = [0.0,0.3,0.4];

TabLabels = {'Main'; 'Folders'; 'Tracking Parameters'; 'Results'; 'Help'};

% Number of tabs to be generated
NumberOfTabs = length(TabLabels);

% indicator of tabs callback
tab_index = zeros(NumberOfTabs,1);
tab_index(3) = 1;

% Get user screen size
SC = get(0, 'ScreenSize');
MaxMonitorX = SC(3);
MaxMonitorY = SC(4);

main_tabFigScale = 0.5;
gui_ratio = 0.6; % height/width
MaxWindowX = round(MaxMonitorX*main_tabFigScale);
MaxWindowY = MaxWindowX*gui_ratio;

% Set the figure window size values and calculate center of the screen position for the lower left corner
gui_width = round(MaxMonitorX/2); % Gui is half the screen wide
gui_height = round(gui_width*gui_ratio); % fix the ratio of the GUI height to width
% if the users screen resolution is not the same as their screen size the GUI will be placed off center
% of possibly out of view
offset = 0;
if (SC(2) ~= 1) 
    offset = abs(SC(2));
end
left_bottom_corner_x_coordinate = (MaxMonitorX-MaxWindowX)/2 - offset;
left_bottom_corner_y_coordinate = (MaxMonitorY-MaxWindowY)/2 + offset;

hctfig = figure(...
    'units', 'pixels',...
    'Position',[left_bottom_corner_x_coordinate, left_bottom_corner_y_coordinate, gui_width, gui_height],...
    'Name','Lineage_Mapper',...
    'NumberTitle','off',...
    'Menubar','none',...
    'Toolbar','none',...
    'Resize', 'on');
%--------------------------------------------------------------------------
% Panel tabs
%--------------------------------------------------------------------------

h_tabpanel = zeros(NumberOfTabs,1);
h_tabpb = zeros(NumberOfTabs,1);


tab_label_text_size = 0.5;

% Create main menu tab, 'Main'
h_tabpanel(1) = uipanel('Units', 'normalized', 'Parent', hctfig,'Visible', 'off', 'Backgroundcolor', lt_gray,'BorderWidth',1, 'Position', [0,0,1,1] );
h_tabpanel(2) = uipanel('Units', 'normalized', 'Parent', hctfig, 'Visible', 'off', 'Backgroundcolor', lt_gray, 'BorderWidth',1, 'Position', [0,0,1,1]);
h_tabpanel(3) = uipanel('Units', 'normalized', 'Parent', hctfig,'Visible', 'off', 'Backgroundcolor', lt_gray,'BorderWidth',1,'Position', [0,0,1,1]);
h_tabpanel(4) = uipanel('Units', 'normalized','Parent', hctfig, 'Visible', 'off', 'Backgroundcolor', lt_gray, 'BorderWidth',1, 'Position', [0,0,1,1]);

h_tabpb(1) = push_button(hctfig, [0 0.95 0.23 0.05], TabLabels(1), 'center', 'k', dark_gray, tab_label_text_size, 'serif', 'bold', 'on', {@first_tab_callback} );
h_tabpb(2) = push_button(hctfig, [0.23 0.95 0.23 0.05], TabLabels(2), 'center', 'k', dark_gray, tab_label_text_size, 'serif', 'bold', 'on', {@second_tab_callback} );
h_tabpb(3) = push_button(hctfig, [0.46 0.95 0.23 0.05], TabLabels(3), 'center', 'k', dark_gray, tab_label_text_size, 'serif', 'bold', 'off', {@third_tab_callback} );
h_tabpb(4) = push_button(hctfig, [0.69 0.95 0.23 0.05], TabLabels(4), 'center', 'k', dark_gray, tab_label_text_size, 'serif', 'bold', 'off', {@fourth_tab_callback} );

% Create main menu tab, 'Help'
push_button(hctfig, [0.92 0.95 0.08 0.05], TabLabels(5), 'center', 'k', dark_gray, tab_label_text_size, 'serif', 'bold', 'on', {@Open_Help_PDF_callback} );

    function Open_Help_PDF_callback(varargin)
        winopen('GUI_HELP.pdf');
    end
%-----------------------------------------------------------------------------------


%-----------------------------------------------------------------------------------
%  Main
%-----------------------------------------------------------------------------------

label(h_tabpanel(1), [0.12 0.865 0.75 0.07], 'Lineage Mapper', 'center', green_blue, lt_gray, .7, 'serif', 'bold');

% Time t
label(h_tabpanel(1), [0.2 0.15 0.1 0.04], 'Time t', 'center', 'k', lt_gray, .7, 'sans serif', 'bold');

% Time T + 1
label(h_tabpanel(1), [0.66 0.15 0.15 0.04], 'Time t + 1', 'center', 'k', lt_gray, .7, 'sans serif', 'bold');

% Push button: Start Tracking
start_tracking_pb = push_button(h_tabpanel(1), [0.04 0.005 0.22 0.06], 'Start Tracking', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'off', {@start_tracking_Callback});
push_button(h_tabpanel(1), [0.3 0.005 0.22 0.06], 'Load Tracking Parameters', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'on', {@load_tracking_workspace});

% handle to the intro figure
axes('Parent', h_tabpanel(1), 'Units', 'normalized', 'position', [0.05 0.088 0.87 0.87]);
imshow(imread('celltracker_image_v1.png'), []);
axes('Parent', h_tabpanel(1), 'Units', 'normalized', 'Position', [0.83 -.03 0.16 0.2]);
imshow('NIST_Logo.tif');


%-----------------------------------------------------------------------------------
%  / End Main
%-----------------------------------------------------------------------------------


%-----------------------------------------------------------------------------------------
% Input pathnames
%-----------------------------------------------------------------------------------------

input_panel = sub_panel(h_tabpanel(2), [0.025,0.45,.95,.45], 'Input', 'lefttop', green_blue, lt_gray, 14, 'serif');
output_panel = sub_panel(h_tabpanel(2), [0.025,0.15,.95,.25], 'Output', 'lefttop', green_blue, lt_gray, 14, 'serif');

% Input Folder Parameters:

% -- Input Help
push_button(input_panel, [0.95 0.925 0.04 0.1], '?', 'center', 'k', 'default', .6, 'sans serif', 'bold', 'on', {@Input_help_callback});

    function Input_help_callback(varargin)
        winopen('Input_help.pdf');
    end

% -- Segmented 
label(input_panel, [0.001 0.72 0.22 0.126], 'Segmented Images', 'right', 'k', lt_gray, .5, 'sans serif', 'normal');
segmented_images_path_edit = editbox(input_panel, [0.23 0.74 0.61 0.126], segmented_images_path, 'left', 'k', 'w', .5, 'normal');
push_button(input_panel, [0.851 0.74 0.12 0.126], 'Open', 'center', 'k', 'default', .5, 'sans serif', 'normal', 'on', {@select_folder_segmented_images_Callback});

label(input_panel, [0.001 0.54 0.22 0.126], 'Common segmented name', 'right', 'k', lt_gray, .5, 'sans serif', 'normal');
segmented_images_common_name_edit = editbox(input_panel, [0.23 0.56 0.25 0.126], segmented_images_common_name, 'left', 'k', 'w', .5, 'normal');


% -- Raw Images
label(input_panel,[0.001 0.25 0.22 0.126], 'Raw Images', 'right', 'k', lt_gray, .5, 'sans serif', 'normal');
raw_images_path_edit = editbox_check(input_panel, [0.23 0.28 0.61 0.126], raw_images_path, 'left', 'k', 'w', .5, 'normal', {@save_parameters});
push_button(input_panel, [0.851 0.28 0.12 0.126], 'Open', 'center', 'k', 'default', .5, 'sans serif', 'normal', 'on', {@select_folder_raw_images_Callback}); 

label(input_panel, [0.001 0.08 0.22 0.126], 'Common raw name', 'right', 'k', lt_gray, .5, 'sans serif', 'normal');
raw_images_common_name_edit = editbox(input_panel, [0.23 0.1 0.25 0.126], raw_images_common_name, 'left', 'k', 'w', .5, 'normal');


% Output Folder Parameters:

% -- Output Help
push_button(output_panel, [0.95 0.84 0.04 0.22], '?', 'center', 'k', 'default', .55, 'sans serif', 'bold', 'on', {@Output_help_callback});

    function Output_help_callback(varargin)
        winopen('Output_help.pdf');
    end

%  -- Output Folder
label(output_panel, [0.001 0.47 0.22 0.25], 'Tracked Images', 'right', 'k', lt_gray, .54, 'sans serif', 'normal');
tracked_images_path_edit = editbox(output_panel, [0.23 0.5 0.61 0.25], tracked_images_path, 'left', 'k', 'w', .54, 'normal');
push_button(output_panel, [0.851 0.5 0.12 0.25], 'Open', 'center', 'k', 'default', .54, 'sans serif', 'normal', 'on', {@select_folder_tracked_images_Callback});

label(output_panel, [0.001 0.12 0.22 0.25], 'Common tracked name', 'right', 'k', lt_gray, .54, 'sans serif', 'normal');
tracked_images_common_name_edit = editbox(output_panel, [0.23 0.15 0.25 0.25], tracked_images_common_name, 'left', 'k', 'w', .54, 'normal');


% Push Button: Save User Input
push_button(h_tabpanel(2), [0.04 0.005 0.22 0.06], 'Start Tracking', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'on', {@start_tracking_Callback});

% Push Button: Cancel
push_button(h_tabpanel(2), [0.3 0.005 0.22 0.06], 'Cancel', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'on', {@pb_cancel_pathnames_Callback});


%-----------------------------------------------------------------------------------------
% /End Input pathnames
%-----------------------------------------------------------------------------------------



%------------------------------------------------------------------------------------------
% Tracking Parameters
%------------------------------------------------------------------------------------------

% UI panels for tracking parameters
cost_function_parameters_panel = sub_panel(h_tabpanel(3), [0.032 0.42 0.45 0.5], 'Cost Function Parameters', 'lefttop', green_blue, lt_gray, 13, 'serif');
mitotic_parameters_panel = sub_panel(h_tabpanel(3), [0.518 0.42 0.45 0.5], 'Mitotic Parameters', 'lefttop', green_blue, lt_gray, 13, 'serif');
confidence_index_parameters_panel = sub_panel(h_tabpanel(3), [0.032 0.1 0.45 0.3], 'Confidence Index Parameters', 'lefttop', green_blue, lt_gray, 13, 'serif');
fusion_parameters_panel = sub_panel(h_tabpanel(3), [0.518 0.1 0.45 0.3], 'Fusion Parameters', 'lefttop', green_blue, lt_gray, 13, 'serif');

%-------------------------------------------------------------------------------------------------------------------------------------------
% Cost Function Parameters
%-------------------------------------------------------------------------------------------------------------------------------------------

x_pos = 0.03; y_pos = 0.2; hgt = 0.1;
% edit boxes
eb_pos = x_pos + 0.6;
% unit labels
u_pos = eb_pos + 0.15;
% uicontrol margin
bm = hgt + .05; % bottom margin

% Weight of Overlap
label(cost_function_parameters_panel, [x_pos y_pos+bm*4-.01 0.53 hgt], 'Weight of Cell Overlap', 'right', 'k', lt_gray, .55, 'sans serif', 'normal');
weight_overlap_edit = editbox_check(cost_function_parameters_panel, [eb_pos y_pos+bm*4 0.15 hgt], weight_overlap, 'center', 'k', 'w', .55, 'normal', {@save_parameters});
label(cost_function_parameters_panel, [u_pos y_pos+bm*4-.015 0.1 hgt], '%', 'center', 'k', lt_gray, .55, 'sans serif', 'normal');

% Weight of Centroids Distance
label(cost_function_parameters_panel, [x_pos y_pos+bm*3-.01 0.53 hgt], 'Weight of Centroids Distance', 'right', 'k', lt_gray, .55, 'sans serif', 'normal');
weight_centroids_edit = editbox_check(cost_function_parameters_panel, [eb_pos y_pos+bm*3 0.15 hgt], weight_centroids, 'center', 'k', 'w', .55, 'normal', {@save_parameters});
label(cost_function_parameters_panel, [u_pos y_pos+bm*3-.015 0.1 hgt], '%', 'center', 'k', lt_gray, .55, 'sans serif', 'normal');

% Weight of cell Size
label(cost_function_parameters_panel, [x_pos y_pos+bm*2-.01 0.53 hgt], 'Weight of Cell Size', 'right', 'k', lt_gray, .55, 'sans serif', 'normal');
weight_size_edit = editbox_check(cost_function_parameters_panel, [eb_pos y_pos+bm*2 0.15 hgt], weight_size, 'center', 'k', 'w', .55, 'normal', {@save_parameters});
label(cost_function_parameters_panel, [u_pos y_pos+bm*2-.015 0.1 hgt], '%', 'center', 'k', lt_gray, .55, 'sans serif', 'normal');

% Max_centroids_distance
label(cost_function_parameters_panel, [x_pos y_pos+bm-0.1 0.53 hgt], 'Max Centroids Distance', 'right', 'k', lt_gray, .55, 'sans serif', 'normal');
max_centroids_distance_edit = editbox_check(cost_function_parameters_panel, [eb_pos y_pos+bm-0.09 0.15 hgt], max_centroids_distance, 'center', 'k', 'w', .55, 'normal', {@save_parameters});
label(cost_function_parameters_panel, [u_pos y_pos+bm-0.1 0.1 hgt], 'px', 'center', 'k', lt_gray, .55, 'sans serif', 'normal');

% Frames to track
label(cost_function_parameters_panel, [x_pos y_pos-0.1 0.53 hgt], 'Frames To Track', 'right', 'k', lt_gray, .55, 'sans serif', 'normal');
frames_to_track_edit = editbox_check(cost_function_parameters_panel, [eb_pos y_pos-0.09 0.15 hgt], frames_to_track, 'center', 'k', 'w', .55, 'normal', {@save_parameters});

% Cost Function Help
push_button(cost_function_parameters_panel, [0.92 0.925 0.07 hgt], '?', 'center', 'k', 'default', .6, 'sans serif', 'bold', 'on', {@cost_function_help});

    function cost_function_help(varargin)
        winopen('Cost_Function_Parameters.pdf');
    end

%-------------------------------------------------------------------------------------------------------------------------------------------

%-------------------------------------------------------------------------------------------------------------------------------------------
% Mitotic Parameters
%-------------------------------------------------------------------------------------------------------------------------------------------

x_pos = 0.03; y_pos = 0.2; hgt = 0.1;
% edit boxes
eb_pos = x_pos + 0.6;
% unit labels
u_pos = eb_pos + 0.15;
% uicontrol margin
bm = hgt + .05; % bottom margin

% Min Mitotic Overlap (%size)
label(mitotic_parameters_panel, [x_pos y_pos+bm*4-.01 0.53 hgt], 'Min Mitotic Overlap', 'right', 'k', lt_gray, .55, 'sans serif', 'normal');
division_overlap_threshold_edit = editbox_check(mitotic_parameters_panel, [eb_pos y_pos+bm*4 0.15 hgt], division_overlap_threshold, 'center', 'k', 'w', .55, 'normal', {@save_parameters});
label(mitotic_parameters_panel, [u_pos y_pos+bm*4-.015 0.1 hgt], '%', 'center', 'k', lt_gray, .55, 'sans serif', 'normal');

% Daughter Size Similarity
label(mitotic_parameters_panel, [x_pos y_pos+bm*3-.01 0.53 hgt], 'Daughter Size Similarity', 'right', 'k', lt_gray, .55, 'sans serif', 'normal');
daughter_size_similarity_edit = editbox_check(mitotic_parameters_panel, [eb_pos y_pos+bm*3 0.15 hgt], daughter_size_similarity, 'center', 'k', 'w', .55, 'normal', {@save_parameters});
label(mitotic_parameters_panel, [u_pos y_pos+bm*3-.015 0.1 hgt], '%', 'center', 'k', lt_gray, .55, 'sans serif', 'normal');

% Daughter Aspect Ratio Similarity
label(mitotic_parameters_panel, [x_pos y_pos+bm*2-.01 0.53 hgt], 'Daughter Aspect Ratio Similarity', 'right', 'k', lt_gray, .55, 'sans serif', 'normal');
daughter_aspect_ratio_similarity_edit = editbox_check(mitotic_parameters_panel, [eb_pos y_pos+bm*2 0.15 hgt], daughter_aspect_ratio_similarity, 'center', 'k', 'w', .55, 'normal', {@save_parameters});
label(mitotic_parameters_panel, [u_pos y_pos+bm*2-.015 0.1 hgt], '%', 'center', 'k', lt_gray, .55, 'sans serif', 'normal');

% Circularity Index
label(mitotic_parameters_panel, [x_pos y_pos+bm-.01 0.53 hgt], 'Mother Circularity Index', 'right', 'k', lt_gray, .55, 'sans serif', 'normal');
circularity_threshold_edit = editbox_check(mitotic_parameters_panel, [eb_pos y_pos+bm 0.15 hgt], circularity_threshold, 'center', 'k', 'w', .55, 'normal', {@save_parameters});
label(mitotic_parameters_panel, [u_pos y_pos+bm-.015 0.1 hgt], '%', 'center', 'k', lt_gray, .55, 'sans serif', 'normal');

% Number of frames
label(mitotic_parameters_panel, [x_pos y_pos-.01 0.53 hgt], '# of Frames to Check Circularity', 'right', 'k', lt_gray, .55, 'sans serif', 'normal');
number_of_frames_check_circularity_edit = editbox_check(mitotic_parameters_panel, [eb_pos y_pos 0.15 hgt], number_of_frames_check_circularity, 'center', 'k', 'w', .55, 'normal', {@save_parameters});

% Enable Cell Mitosis
enable_cell_mitosis_checkbox = checkbox(mitotic_parameters_panel, [x_pos-0.01 y_pos-0.18 0.43 hgt], 'Enable Cell Mitosis', 'right', 'k', lt_gray, .55, 'sans serif', 'normal', {@cell_mitosis_callback});

    function cell_mitosis_callback(varargin)
        enable_cell_mitosis_flag = get(enable_cell_mitosis_checkbox, 'value');
        if(enable_cell_mitosis_flag)
            set(daughter_size_similarity_edit, 'enable', 'on');
            set(daughter_aspect_ratio_similarity_edit, 'enable', 'on');
            set(circularity_threshold_edit, 'enable', 'on');
            set(number_of_frames_check_circularity_edit, 'enable', 'on');
        else
            set(daughter_size_similarity_edit, 'enable', 'off');
            set(daughter_aspect_ratio_similarity_edit, 'enable', 'off');
            set(circularity_threshold_edit, 'enable', 'off');
            set(number_of_frames_check_circularity_edit, 'enable', 'off');
        end
    end

% Mitotic Help
push_button(mitotic_parameters_panel, [0.92 0.925 0.07 hgt], '?', 'center', 'k', 'default', .6, 'sans serif', 'bold', 'on', {@mitotic_help});

    function mitotic_help(varargin)
         winopen('Mitotic_Parameters.pdf');
    end

%-------------------------------------------------------------------------------------------------------------------------------------------



%-------------------------------------------------------------------------------------------------------------------------------------------
% Confidence Index Parameters
%-------------------------------------------------------------------------------------------------------------------------------------------
x_pos = 0.03; y_pos = 0.2; hgt = 0.18;
% edit boxes
eb_pos = x_pos + 0.6;

% uicontrol margin
bm = hgt + .05; % bottom margin

% Min Cell Life (nb frames)
label(confidence_index_parameters_panel, [x_pos y_pos+bm*2-.015 0.53 hgt], 'Min Cell Life (nb frames)', 'right', 'k', lt_gray, .55, 'sans serif', 'normal');
cell_life_threshold_edit = editbox_check(confidence_index_parameters_panel, [eb_pos y_pos+bm*2 0.15 hgt], cell_life_threshold, 'center', 'k', 'w', .55, 'normal', {@save_parameters});

% Max delta centroid distance to be considered dead cell
label(confidence_index_parameters_panel, [x_pos y_pos+bm-.015 0.53 hgt], 'Cell Death Delta Centroid Thres', 'right', 'k', lt_gray, .55, 'sans serif', 'normal');
cell_apoptosis_cent_thres_edit = editbox_check(confidence_index_parameters_panel, [eb_pos y_pos+bm 0.15 hgt], cell_apoptosis_delta_centroid_thres, 'center', 'k', 'w', .55, 'normal', {@save_parameters});

% Cell Density
cell_density_checkbox = checkbox(confidence_index_parameters_panel, [x_pos-0.01 y_pos-0.18 0.43 hgt], 'Surrounding Density ', 'right', 'k', lt_gray, .55, 'sans serif', 'normal', {@cell_density_callback});
   
    function cell_density_callback(varargin)
        cell_density_ci_flag = get(cell_density_checkbox, 'value');
    end

% Distance To Border
border_cell_checkbox = checkbox(confidence_index_parameters_panel, [x_pos+0.45 y_pos-0.18 0.5 hgt], 'Touching FOV Border ', 'right', 'k', lt_gray, .55, 'sans serif', 'normal', {@border_cell_callback});

    function border_cell_callback(varargin)
        border_cell_ci_flag = get(border_cell_checkbox, 'value');
    end

% Confidence Help
push_button(confidence_index_parameters_panel, [0.915 0.87 0.07 hgt], '?', 'center', 'k', 'default', .6, 'sans serif', 'bold', 'on', {@confidence_help});

    function confidence_help(varargin)
         winopen('Confidence_Index_Parameters.pdf');
    end

%-------------------------------------------------------------------------------------------------------------------------------------------


%-------------------------------------------------------------------------------------------------------------------------------------------
% Fusion Parameters
%-------------------------------------------------------------------------------------------------------------------------------------------
x_pos = 0.03; y_pos = 0.2; hgt = 0.18;
% edit boxes
eb_pos = x_pos + 0.6;
% unit labels
u_pos = eb_pos + 0.15;
% uicontrol margin
bm = hgt + .05; % bottom margin

% Min Fusion Overlap (%size)
label(fusion_parameters_panel, [x_pos y_pos+bm*2-.015 0.53 hgt], 'Min Fusion Overlap', 'right', 'k', lt_gray, .55, 'sans serif', 'normal');
fusion_overlap_threshold_edit = editbox_check(fusion_parameters_panel, [eb_pos y_pos+bm*2 0.15 hgt], fusion_overlap_threshold, 'center', 'k', 'w', .55, 'normal', {@save_parameters});
label(fusion_parameters_panel, [u_pos y_pos+bm*2-.02 0.1 hgt], '%', 'center', 'k', lt_gray, .55, 'sans serif', 'normal');

% Min Cell Area (pixels)
label(fusion_parameters_panel, [x_pos y_pos+bm-.015 0.53 hgt], 'Min Cell Area', 'right', 'k', lt_gray, .55, 'sans serif', 'normal');
cell_size_threshold_edit = editbox_check(fusion_parameters_panel, [eb_pos y_pos+bm 0.15 hgt], cell_size_threshold, 'center', 'k', 'w', .55, 'normal', {@save_parameters});
label(fusion_parameters_panel, [u_pos y_pos+bm-.015 0.1 hgt], 'px', 'center', 'k', lt_gray, .55, 'sans serif', 'normal');

% Disable fusion
enable_fusion_checkbox = checkbox(fusion_parameters_panel, [x_pos-0.01 y_pos-0.18 0.43 hgt], 'Enable Cell Fusion', 'right', 'k', lt_gray, .55, 'sans serif', 'normal', {@fusion_flag_callback});

    function fusion_flag_callback(varargin)
        enable_cell_fusion_flag = get(enable_fusion_checkbox, 'value');
        if enable_cell_fusion_flag
            set(cell_size_threshold_edit, 'enable', 'off');
        else
            set(cell_size_threshold_edit, 'enable', 'on');
        end
    end

% Fusion Help
push_button(fusion_parameters_panel, [0.915 0.87 0.07 hgt], '?', 'center', 'k', 'default', .6, 'sans serif', 'bold', 'on', {@fusion_help});

    function fusion_help(varargin)
         winopen('Fusion_Parameters.pdf');
    end


%-------------------------------------------------------------------------------------------------------------------------------------------

% Push Button: Save User Input
push_button(h_tabpanel(3), [0.04 0.005 0.22 0.06], 'Start Tracking', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'on', {@start_tracking_Callback});
% Push Button: Cancel
push_button(h_tabpanel(3), [0.3 0.005 0.22 0.06], 'Cancel', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'on', {@pb_tracking_cancel_Callback});
% Push Button: Default Parameters
push_button(h_tabpanel(3), [0.56 0.005 0.22 0.06], 'Default Parameters', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'on', {@default_parameters});


%------------------------------------------------------------------------------------------
% / End Tracking Parameters
%------------------------------------------------------------------------------------------



%------------------------------------------------------------------------------------------
%  Result Tab
%------------------------------------------------------------------------------------------
% Examining Tracking Results
label(h_tabpanel(4), [0.12 0.865 0.75 0.07], 'Examining Tracking Results', 'center', green_blue, lt_gray, .7, 'serif', 'bold');

review_data_subpanel = sub_panel(h_tabpanel(4), [.032 .7 .928 .17], 'Tracking Data', 'lefttop', green_blue, lt_gray, 13, 'serif');


% Tracking Data Help
push_button(h_tabpanel(4), [.925 .795 0.03 .045], '?', 'center', 'k', 'default', .6, 'sans serif', 'bold', 'on', {@Tracking_Data_help});

    function Tracking_Data_help(varargin)
         winopen('Tracking_Data.pdf');
    end


x_pos = .008;
y_pos = 0.15;
w = 0.19;
h = 0.47;
% left margin
lm = w + .008;

% Show Confidence Index
push_button(review_data_subpanel, [x_pos y_pos w h], 'Confidence Index', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'on', {@Show_Confidence_Index_callback});

% Show Birth and Death
push_button(review_data_subpanel, [x_pos+lm y_pos w h], 'Birth and Death', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'on', {@show_birth_and_death_callback});

% Show Division Matrix
show_division_matrix_button = push_button(review_data_subpanel, [x_pos+lm*2 y_pos w h], 'Division Matrix', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'on', {@show_division_matrix_callback});

% Show Fusion Matrix
show_fusion_matrix_button = push_button(review_data_subpanel, [x_pos+lm*3 y_pos w h], 'Fusion Matrix', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'off', {@show_fusion_matrix_callback});

% Show Cell Apoptosis
push_button(review_data_subpanel, [x_pos+lm*4 y_pos w h], 'Cell Apoptosis', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'on', {@Show_Cell_Apoptosis_callback});


% Text: Plot Cell Migration (2D)
plot_migration_handle = sub_panel(h_tabpanel(4), [0.032 0.11 0.42 0.56], 'Plot Cell Migration (2D)', 'centertop', green_blue, lt_gray, 13, 'serif');

% Plot Cell Migration (2D) Help
push_button(plot_migration_handle, [0.92 0.925 0.07 0.09], '?', 'center', 'k', 'default', .6, 'sans serif', 'bold', 'on', {@Plot_Cell_Migration_help});

    function Plot_Cell_Migration_help(varargin)
         winopen('Plot_Cell_Migration.pdf');
    end

% Plot cell Migration image
axes('Parent',plot_migration_handle, 'Units', 'normalized', 'position', [0.03 0.33 0.92 0.55]);
imshow(imread('Cell_Migration.png'),[])

% Cell Numbers to Plot
label(plot_migration_handle, [0.25 0.2 0.42 0.1], 'Cell Numbers to Plot:', 'center', 'k', lt_gray, .5, 'sans serif', 'bold');
plot_cell_nb_migration_edit = editbox(plot_migration_handle, [0.66 0.22 0.09 0.1], 'All', 'center', 'k', 'w', .5, 'normal');
push_button(plot_migration_handle, [0.25 0.05 0.5 0.12], 'Plot Cell Migration', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'on', {@Plot_Cell_Migration_callback});


% Plot Cell Lineage
plot_cell_lineage_handle = sub_panel(h_tabpanel(4), [0.54 0.11 0.42 0.56], 'Plot Cell Lineage', 'centertop', green_blue, lt_gray, 13, 'serif');

% Plot Cell Migration (2D) Help
push_button(plot_cell_lineage_handle, [0.92 0.925 0.07 0.09], '?', 'center', 'k', 'default', .6, 'sans serif', 'bold', 'on', {@Plot_Cell_Lineage_help});

    function Plot_Cell_Lineage_help(varargin)
         winopen('Plot_Cell_Lineage.pdf');
    end

% Plot cell lineage image
axes('Parent', plot_cell_lineage_handle, 'Units', 'normalized', 'position', [0.03 0.33 0.92 0.55]);
imshow(imread('Cell_Lineage.png'),[])

% Text: Cell Numbers to Plot
label(plot_cell_lineage_handle, [0.25 0.2 0.42 0.1], 'Cell Numbers to Plot:', 'center', 'k', lt_gray, .5, 'sans serif', 'bold');


plot_lineage_nb_edit = editbox(plot_cell_lineage_handle, [0.66 0.22 0.09 0.1], 'All', 'center', 'k', 'w', .5, 'normal');
push_button(plot_cell_lineage_handle, [0.01 0.05 0.48 0.12], 'Plot Division Lineage', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'on', {@Plot_Division_Lineage_callback});
plot_fusion_linage_pb = push_button(plot_cell_lineage_handle, [0.51 0.05 0.48 0.12], 'Plot Fusion Lineage', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'off', {@Plot_Fusion_Lineage_callback});


% Return To Main
% push_button(h_tabpanel(4), [0.183 0.01 0.27 0.06], '<--- Return To Main', 'center', 'k', 'default', .4, 'sans serif', 'bold', 'on', {@first_tab_callback});

% Explore Tracked Images
push_button(h_tabpanel(4), [0.36 0.01 0.28 0.07], 'Explore Tracked Images', 'center', 'k', 'default', .34, 'sans serif', 'bold', 'on', {@Explore_Tracked_Images_callback});


%--------------------------------------------------------------------------------------------------
%  / End Result tab
%--------------------------------------------------------------------------------------------------



%--------------------------------------------------------------------------------------------------
% Pushbutton Callbacks
%--------------------------------------------------------------------------------------------------

    function start_tracking_Callback(varargin)
        
        d = dir([segmented_images_path '*' segmented_images_common_name '*.tif']);
        nb_frames = length(d);
        if nb_frames <= 0
            if ~batch_mode
                tab_index(2) = 0;
                second_tab_callback();
            end
            errordlg('Segmented Images Folder is empty');
            return;
        end
        
       % truncate frames_to_track_nb
       frames_to_track_nb( frames_to_track_nb > nb_frames) = [];
        
        
        % Save user input and check for errors
        if ~batch_mode
            set(h_tabpb(4), 'enable', 'off');
            if ~save_parameters(), return; end
        end
        
        
        % check if there exists a tracked images folder
        % if so query the user for overwrite permission
        if ~batch_mode && exist([tracked_images_path tracked_images_common_name sprintf('%d',nb_frames) '.tif'],'file')
            overwrite_button = questdlg('Tracked Images Exist! Overwrite?', 'Warning','Yes','Cancel','Yes');
            if ~strcmp(overwrite_button,'Yes')
                % if the user did not select yes for overwrite, abort tracking
                return;
            end
            print_to_command('Tracked Images Exist, overwriting...', tracked_images_path);
        end
        
        write_metadata_to_log_file();
        
        % Track the segmented images
        finished_tracking = start_tracking(segmented_images_path, segmented_images_common_name, tracked_images_path,...
            tracked_images_common_name, max_centroids_distance, weight_size/100, weight_centroids/100, weight_overlap/100, fusion_overlap_threshold/100, division_overlap_threshold/100,...
            cell_life_threshold, max_cell_num, nb_frames, daughter_size_similarity/100, daughter_aspect_ratio_similarity/100, cell_size_threshold, enable_cell_fusion_flag, ...
            frames_to_track_nb, cell_density_ci_flag, border_cell_ci_flag, number_of_frames_check_circularity, circularity_threshold/100, cell_apoptosis_delta_centroid_thres, enable_cell_mitosis_flag);
        
        if finished_tracking
            if ~batch_mode
                set(h_tabpb(4), 'enable', 'on');
                fourth_tab_callback();
            end
            % write the CSV file for Confidence Index, Birth, Death, Division, and potentially Fusion to disk
            Write_Metadata_to_CSV_File(tracked_images_path, enable_cell_fusion_flag);
            write_parameters_to_mat_file();
        end
        
    end

%--------------------------------------------------------------------------------------------------
%--------------------------------------------------------------------------------------------------
    function write_metadata_to_log_file()
        print_to_command('', tracked_images_path);
        print_to_command('  -------- Lineage Mapper GUI --------',tracked_images_path);
        print_to_command('Folder Tab Parameters',tracked_images_path);
        print_to_command(['  Raw Images Directory: ' raw_images_path],tracked_images_path);
        print_to_command(['  Segmented Images Directory: ' segmented_images_path],tracked_images_path);
        print_to_command(['  Tracked Images Directory: ' tracked_images_path],tracked_images_path);
        print_to_command(['  Metadata Directory: ' tracked_images_path],tracked_images_path);
        print_to_command(['  Raw Image Common Name: <' raw_images_common_name '>'],tracked_images_path);
        print_to_command(['  Segmented Image Common Name: <' segmented_images_common_name '>'],tracked_images_path);
        print_to_command(['  Tracked Image Common Name: <' tracked_images_common_name '>'],tracked_images_path);
        
        print_to_command('Tracking Parameters: ',tracked_images_path);
        print_to_command('  Cost Function Parameters: ',tracked_images_path);
        print_to_command(['    Weight Cell Overlap: <' num2str(weight_overlap) '>'],tracked_images_path);
        print_to_command(['    Weight Centroids Distance: <' num2str(weight_centroids) '>'],tracked_images_path);
        print_to_command(['    Weight Cell Size: <' num2str(weight_size) '>'],tracked_images_path);
        print_to_command(['    Max Centroids Distance: <' num2str(max_centroids_distance) '>'],tracked_images_path);
        print_to_command(['    Frames to Track: <' frames_to_track '>'],tracked_images_path);
        print_to_command('  Mitotic Parameters: ',tracked_images_path);
        print_to_command(['    Min Mitotic Overlap: <' num2str(division_overlap_threshold) '>'],tracked_images_path);
        print_to_command(['    Circularity Index: <' num2str(circularity_threshold) '>'],tracked_images_path);
        print_to_command(['    Daughter Size Similarity: <' num2str(daughter_size_similarity) '>'],tracked_images_path);
        print_to_command(['    Daughter Aspect Ratio Similarity: <' num2str(daughter_aspect_ratio_similarity) '>'],tracked_images_path);
        print_to_command(['    Enable Cell Mitotis: <' num2str(enable_cell_mitosis_flag) '>'],tracked_images_path);
        print_to_command(['    Number of Frames: <' num2str(number_of_frames_check_circularity) '>'],tracked_images_path);
        print_to_command('  Confidence Index Parameters: ',tracked_images_path);
        print_to_command(['    Min Cell Life: <' num2str(cell_life_threshold) '>'],tracked_images_path);
        print_to_command(['    Cell Death Delta Centroid: <' num2str(cell_apoptosis_delta_centroid_thres) '>'],tracked_images_path);
        print_to_command(['    Cell Density CI Flag: <' num2str(cell_density_ci_flag) '>'],tracked_images_path);
        print_to_command(['    Border Cell CI Flag: <' num2str(border_cell_ci_flag) '>'],tracked_images_path);
        
        print_to_command('  Fusion Parameters: ',tracked_images_path);
        print_to_command(['    Min Cell Area: <' num2str(cell_size_threshold) '>'],tracked_images_path);
        print_to_command(['    Min Fusion Overlap: <' num2str(fusion_overlap_threshold) '>'],tracked_images_path);
        print_to_command(['    Enable Fusion: <' num2str(enable_cell_fusion_flag) '>'],tracked_images_path);
    end

    function write_parameters_to_mat_file()
        if numel(params) > 0
            save([tracked_images_path 'parameters.mat'], params{1});
            for i = 1:numel(params)
                save([tracked_images_path 'parameters.mat'], params{i}, '-append');
            end
        end
    end

    
%--------------------------------------------------------------------------------------------------
%--------------------------------------------------------------------------------------------------

    function select_folder_raw_images_Callback(varargin)
        % get directory
        sdir = uigetdir(pwd,'Select Folder:');
        if sdir ~= 0
            try
                raw_images_path_loc = validate_filepath(sdir);
            catch err
                if (strcmp(err.identifier,'validate_filepath:notFoundInPath')) || ...
                        (strcmp(err.identifier,'validate_filepath:argChk'))
                    errordlg('Invalid directory selected');
                    return;
                else
                    rethrow(err);
                end
            end
            set(raw_images_path_edit, 'String', raw_images_path_loc);
        end
    end

%--------------------------------------------------------------------------------------------------

    function select_folder_segmented_images_Callback(varargin)
        % get directory
        sdir = uigetdir(pwd,'Select Folder:');
        if sdir ~= 0
            try
                segmented_images_path_loc = validate_filepath(sdir);
            catch err
                if (strcmp(err.identifier,'validate_filepath:notFoundInPath')) || ...
                        (strcmp(err.identifier,'validate_filepath:argChk'))
                    errordlg('Invalid directory selected');
                    return;
                else
                    rethrow(err);
                end
            end
            set(segmented_images_path_edit, 'String', segmented_images_path_loc);
        end
    end

%----------------------------------------------------------------------------

    function select_folder_tracked_images_Callback(varargin)
        % get directory
        sdir = uigetdir(pwd,'Select Folder:');
        if sdir ~= 0
            try
                tracked_images_path_loc = validate_filepath(sdir);
            catch err
                if (strcmp(err.identifier,'validate_filepath:notFoundInPath')) || ...
                        (strcmp(err.identifier,'validate_filepath:argChk'))
                    errordlg('Invalid directory selected');
                    return;
                else
                    rethrow(err);
                end
            end
            set(tracked_images_path_edit, 'String', tracked_images_path_loc);
        end
    end

%------------------------------------------------------------------------------------


    function pb_cancel_pathnames_Callback(varargin)
        
        set(raw_images_path_edit,'string',raw_images_path);
        set(raw_images_common_name_edit,'string',raw_images_common_name);
        set(segmented_images_path_edit,'string',segmented_images_path);
        set(segmented_images_common_name_edit,'string',segmented_images_common_name);
        set(tracked_images_path_edit,'string',tracked_images_path);
        set(tracked_images_common_name_edit,'string',tracked_images_common_name);
        
    end


%-----------------------------------------------------------------------------------------------

    function pb_tracking_cancel_Callback(varargin)
        
        set(weight_overlap_edit,'String',weight_overlap);
        set(weight_centroids_edit,'String',weight_centroids);
        set(weight_size_edit,'String',weight_size);
        set(max_centroids_distance_edit,'String',max_centroids_distance);
        if (strcmp(frames_to_track, '0'))
            set(frames_to_track_edit,'String','All');
        else 
            set(frames_to_track_edit,'String',frames_to_track);
        end
        set(division_overlap_threshold_edit,'String',division_overlap_threshold);
        set(daughter_size_similarity_edit,'String',daughter_size_similarity);
        set(daughter_aspect_ratio_similarity_edit,'String',daughter_aspect_ratio_similarity);
        set(circularity_threshold_edit,'String',circularity_threshold);
        set(number_of_frames_check_circularity_edit, 'String', number_of_frames_check_circularity);
        set(enable_cell_mitosis_checkbox, 'value', enable_cell_mitosis_flag);
        
        set(cell_life_threshold_edit,'String',cell_life_threshold);
        set(cell_apoptosis_cent_thres_edit, 'String', cell_apoptosis_delta_centroid_thres);
        set(cell_density_checkbox, 'value', cell_density_ci_flag);
        set(border_cell_checkbox, 'value', border_cell_ci_flag);
        
        set(cell_size_threshold_edit,'String',cell_size_threshold);
        set(fusion_overlap_threshold_edit,'String',fusion_overlap_threshold);
        set(enable_fusion_checkbox,'Value',enable_cell_fusion_flag);  

    end


%------------------------------------------------------------------------------------

    function batchmode_set_default_parameters()
         % Cost function params
        weight_overlap = 100;
        weight_centroids = 50;
        weight_size = 20;
        max_centroids_distance = 150;
        frames_to_track = 'All';

        % Mitotic params
        division_overlap_threshold = 20;
        daughter_size_similarity = 50;
        daughter_aspect_ratio_similarity = 70;
        circularity_threshold = 30;
        number_of_frames_check_circularity = 5;
        enable_cell_mitosis_flag = 1;
        
        % Confidence Index params
        cell_life_threshold = 32;
        cell_apoptosis_delta_centroid_thres = 10;
        cell_density_ci_flag = 1;
        border_cell_ci_flag = 1;

        % Fusion params
        cell_size_threshold = 200;
        fusion_overlap_threshold = 20;
        enable_cell_fusion_flag = 0;
    end

    function default_parameters(varargin)
        
        % Cost function params
        weight_overlap_loc = 100;
        weight_centroids_loc = 50;
        weight_size_loc = 20;
        max_centroids_distance_loc = 150;
        frames_to_track_loc = 'All';

        % Mitotic params
        division_overlap_threshold_loc = 20;
        daughter_size_similarity_loc = 50;
        daughter_aspect_ratio_similarity_loc = 70;
        circularity_threshold_loc = 30;
        number_of_frames_check_circularity_loc = 5;
        enable_cell_mitosis_flag = 1;
        % make sure edit boxes are enabled
        if enable_cell_mitosis_flag
            set(daughter_size_similarity_edit, 'enable', 'on');
            set(daughter_aspect_ratio_similarity_edit, 'enable', 'on');
            set(circularity_threshold_edit, 'enable', 'on');
            set(number_of_frames_check_circularity_edit, 'enable', 'on');
        else
            set(daughter_size_similarity_edit, 'enable', 'off');
            set(daughter_aspect_ratio_similarity_edit, 'enable', 'off');
            set(circularity_threshold_edit, 'enable', 'off');
            set(number_of_frames_check_circularity_edit, 'enable', 'off');
        end


        % Confidence Index params
        cell_life_threshold_loc = 32;
        cell_apoptosis_delta_centroid_thres_loc = 10;
        cell_density_ci_flag = 1;
        border_cell_ci_flag = 1;

        % Fusion params
        cell_size_threshold_loc = 200;
        fusion_overlap_threshold_loc = 20;
        enable_cell_fusion_flag = 0;
        if enable_cell_fusion_flag
            set(cell_size_threshold_edit, 'enable', 'off');
        else
            set(cell_size_threshold_edit, 'enable', 'on');
        end
        
       
        % Display parameters
        set(weight_overlap_edit,'String',weight_overlap_loc);
        set(weight_centroids_edit,'String',weight_centroids_loc);
        set(weight_size_edit,'String',weight_size_loc);
        set(max_centroids_distance_edit,'String',max_centroids_distance_loc);
        if(strcmp(frames_to_track_loc, '0'))
            frames_to_track_loc = 'All';
        end
        set(frames_to_track_edit,'String',frames_to_track_loc);
        
        set(division_overlap_threshold_edit,'String',division_overlap_threshold_loc);
        set(daughter_size_similarity_edit,'String',daughter_size_similarity_loc);
        set(daughter_aspect_ratio_similarity_edit,'String',daughter_aspect_ratio_similarity_loc);
        set(circularity_threshold_edit,'String',circularity_threshold_loc);
        set(number_of_frames_check_circularity_edit, 'String', number_of_frames_check_circularity_loc);
        set(enable_cell_mitosis_checkbox, 'value', enable_cell_mitosis_flag);
         
        set(cell_life_threshold_edit,'String',cell_life_threshold_loc);
        set(cell_apoptosis_cent_thres_edit, 'String', cell_apoptosis_delta_centroid_thres_loc);
        set(cell_density_checkbox, 'value', cell_density_ci_flag);
        set(border_cell_checkbox, 'value', border_cell_ci_flag);
        
        set(cell_size_threshold_edit,'String',cell_size_threshold_loc);
        set(fusion_overlap_threshold_edit,'String',fusion_overlap_threshold_loc);
        set(enable_fusion_checkbox,'Value',enable_cell_fusion_flag);        
    end

%---------------------------------------------------------------------------------------------------
    function update_GUI_with_parameters()
        
        if enable_cell_fusion_flag
            set(cell_size_threshold_edit, 'enable', 'off');
        else
            set(cell_size_threshold_edit, 'enable', 'on');
        end
        if enable_cell_mitosis_flag
            set(daughter_size_similarity_edit, 'enable', 'on');
            set(daughter_aspect_ratio_similarity_edit, 'enable', 'on');
            set(circularity_threshold_edit, 'enable', 'on');
            set(number_of_frames_check_circularity_edit, 'enable', 'on');
        else
            set(daughter_size_similarity_edit, 'enable', 'off');
            set(daughter_aspect_ratio_similarity_edit, 'enable', 'off');
            set(circularity_threshold_edit, 'enable', 'off');
            set(number_of_frames_check_circularity_edit, 'enable', 'off');
        end
        % Display parameters
        set(raw_images_path_edit, 'String', raw_images_path);
        set(segmented_images_path_edit, 'String', segmented_images_path);
        set(tracked_images_path_edit, 'String', tracked_images_path);
        set(raw_images_common_name_edit, 'String', raw_images_common_name);
        set(segmented_images_common_name_edit, 'String', segmented_images_common_name);
        set(tracked_images_common_name_edit, 'String', tracked_images_common_name);
        
        set(weight_overlap_edit,'String',weight_overlap);
        set(weight_centroids_edit,'String',weight_centroids);
        set(weight_size_edit,'String',weight_size);
        set(max_centroids_distance_edit,'String',max_centroids_distance);
        
        set(frames_to_track_edit,'String',frames_to_track);
        
        set(division_overlap_threshold_edit,'String',division_overlap_threshold);
        set(daughter_size_similarity_edit,'String',daughter_size_similarity);
        set(daughter_aspect_ratio_similarity_edit,'String',daughter_aspect_ratio_similarity);
        set(circularity_threshold_edit,'String',circularity_threshold);
        set(number_of_frames_check_circularity_edit, 'String', number_of_frames_check_circularity);
        set(enable_cell_mitosis_checkbox, 'value', enable_cell_mitosis_flag);
        
        set(cell_life_threshold_edit,'String',cell_life_threshold);
        set(cell_apoptosis_cent_thres_edit, 'String', cell_apoptosis_delta_centroid_thres);
        set(cell_density_checkbox, 'value', cell_density_ci_flag);
        set(border_cell_checkbox, 'value', border_cell_ci_flag);
        
        set(cell_size_threshold_edit,'String',cell_size_threshold);
        set(fusion_overlap_threshold_edit,'String',fusion_overlap_threshold);
        set(enable_fusion_checkbox,'Value',enable_cell_fusion_flag);
        if enable_cell_fusion_flag
            set(show_fusion_matrix_button, 'enable','on');
            set(plot_fusion_linage_pb, 'enable','on');
        else
            set(show_fusion_matrix_button, 'enable','off');
            set(plot_fusion_linage_pb, 'enable','off');
        end
        
        if enable_cell_mitosis_flag
            set(show_division_matrix_button, 'enable','on');
            % set(plot_division_linage_pb, 'enable','on');
        else
            set(show_division_matrix_button, 'enable','off');
            %set(plot_division_linage_pb, 'enable','off');
        end
        
    end



%---------------------------------------------------------------------------------------------------
    function Show_Confidence_Index_callback(varargin)
        Show_Confidence_Index(tracked_images_path, left_bottom_corner_x_coordinate, left_bottom_corner_y_coordinate, gui_width, gui_height);
    end

%---------------------------------------------------------------------------------------------------
    function Show_Cell_Apoptosis_callback(varargin)
        Show_Cell_Apoptosis(tracked_images_path, left_bottom_corner_x_coordinate, left_bottom_corner_y_coordinate, gui_width, gui_height);
    end
%---------------------------------------------------------------------------------------------------
    function show_birth_and_death_callback(varargin)
        Show_Birth_And_Death(tracked_images_path, left_bottom_corner_x_coordinate, left_bottom_corner_y_coordinate, gui_width, gui_height);
    end
%---------------------------------------------------------------------------------------------------
    function show_division_matrix_callback(varargin)
        Show_Division_Matrix(tracked_images_path, left_bottom_corner_x_coordinate, left_bottom_corner_y_coordinate, gui_width, gui_height);
    end

    function show_fusion_matrix_callback(varargin)
        Show_Fusion_Matrix(tracked_images_path, left_bottom_corner_x_coordinate, left_bottom_corner_y_coordinate, gui_width, gui_height); 
    end
%---------------------------------------------------------------------------------------------------

    function Plot_Cell_Migration_callback(varargin)
        % Plot Cell Migration        
        cell_nb = get(plot_cell_nb_migration_edit, 'String');
        
        if(strcmp(cell_nb, 'All'))
            % Load Data 
            if ~exist([tracked_images_path 'tracking_workspace.mat'],'file')
                errordlg(sprintf('Output of tracking not found:\nCheck paths for correctness\nRun segmentation and tracking before plotting output'))
                return
            end
            load([tracked_images_path 'tracking_workspace.mat'], 'highest_cell_number');
            cell_nb = ['1:' num2str(highest_cell_number)];
        end
        
        plot_cell_migration_GUI(tracked_images_path, cell_nb);
    end
%---------------------------------------------------------------------------------------------------
    function Plot_Division_Lineage_callback(varargin)
        % Plot Cell Lineage
        if(strcmp(get(plot_lineage_nb_edit, 'String'), 'All'))
            cell_nb = '0';
        else 
            cell_nb = get(plot_lineage_nb_edit, 'String');
        end
        plot_division_lineage_GUI(tracked_images_path, 1, cell_nb);
    end

%---------------------------------------------------------------------------------------------------
function Plot_Fusion_Lineage_callback(varargin)
        % Plot Cell Lineage
        if(strcmp(get(plot_lineage_nb_edit, 'String'), 'All'))
            cell_nb = '0';
        else 
            cell_nb = get(plot_lineage_nb_edit, 'String');
        end
        plot_fusion_lineage_GUI(tracked_images_path, 1, cell_nb);
    end

%---------------------------------------------------------------------------------------------------

    function Explore_Tracked_Images_callback(varargin)
        % Save user input and check for errors
        Explore_Tracked_Images(raw_images_path, raw_images_common_name, tracked_images_path, tracked_images_common_name, ...
            left_bottom_corner_x_coordinate, left_bottom_corner_y_coordinate, gui_width, gui_height);
    end

%---------------------------------------------------------------------------------------------------

    function first_tab_callback(varargin)
        set(h_tabpb(1), 'Backgroundcolor', lt_gray);
        set(h_tabpb(2), 'Backgroundcolor', dark_gray);
        set(h_tabpb(3), 'Backgroundcolor', dark_gray);
        set(h_tabpb(4), 'Backgroundcolor', dark_gray);
        
        set(h_tabpanel(1), 'Visible', 'on');
        set(h_tabpanel(2), 'Visible', 'off');
        set(h_tabpanel(3), 'Visible', 'off');
        set(h_tabpanel(4), 'Visible', 'off');
        
        if tab_index(1) && tab_index(2), save_parameters(); return, end
        tab_index(1) = 1;
    end
%--------------------------------------------------------------------------------------------------

    function second_tab_callback(varargin)
        set(h_tabpb(1), 'Backgroundcolor', dark_gray);
        set(h_tabpb(2), 'Backgroundcolor', lt_gray);
        set(h_tabpb(3), 'Backgroundcolor', dark_gray);
        set(h_tabpb(4), 'Backgroundcolor', dark_gray);
        
        set(h_tabpanel(1), 'Visible', 'off');
        set(h_tabpanel(2), 'Visible', 'on');
        set(h_tabpanel(3), 'Visible', 'off');
        set(h_tabpanel(4), 'Visible', 'off');
        
        if tab_index(2), save_parameters(); end
        tab_index(2) = 1;
        % Enable computation push button
        set(start_tracking_pb, 'Enable', 'on')
        set(h_tabpb(3),'Enable', 'on')
        
    end

%--------------------------------------------------------------------------------------------------

    function third_tab_callback(varargin)
        set(h_tabpb(1), 'Backgroundcolor', dark_gray);
        set(h_tabpb(2), 'Backgroundcolor', dark_gray);
        set(h_tabpb(3), 'Backgroundcolor', lt_gray);
        set(h_tabpb(4), 'Backgroundcolor', dark_gray);
        
        set(h_tabpanel(1), 'Visible', 'off');
        set(h_tabpanel(2), 'Visible', 'off');
        set(h_tabpanel(3), 'Visible', 'on');
        set(h_tabpanel(4), 'Visible', 'off');
        
        if tab_index(3), save_parameters(); end
        tab_index(3) = 1;
    end

%--------------------------------------------------------------------------------------------------

    function fourth_tab_callback(varargin)
        set(h_tabpb(1), 'Backgroundcolor', dark_gray);
        set(h_tabpb(2), 'Backgroundcolor', dark_gray);
        set(h_tabpb(3), 'Backgroundcolor', dark_gray);
        set(h_tabpb(4), 'Backgroundcolor', lt_gray);
        
        set(h_tabpanel(1), 'Visible', 'off');
        set(h_tabpanel(2), 'Visible', 'off');
        set(h_tabpanel(3), 'Visible', 'off');
        set(h_tabpanel(4), 'Visible', 'on');
        
        save_parameters();
        tab_index(4) = 1;
    end

% --------------------------------------------------------------------------------------------------
    function bool = check_input_batchmode()
        bool = false;

        if ~validate_all_filepaths();
            return;
        end
        
        if isnan(cell_size_threshold) || cell_size_threshold < 0
            return;
        end
        if isnan(weight_overlap) || weight_overlap < 0 || weight_overlap > 100
            return;
        end
        if isnan(weight_centroids) || weight_centroids < 0 || weight_centroids > 100
            return;
        end
        if isnan(weight_size) || weight_size < 0 || weight_size > 100
            return;
        end
        if isnan(division_overlap_threshold) || division_overlap_threshold < 0 || division_overlap_threshold > 100
            return;
        end
        if isnan(fusion_overlap_threshold) || fusion_overlap_threshold < 0 || fusion_overlap_threshold > 100
            return;
        end
        if isnan(cell_life_threshold) || cell_life_threshold < 0
            return;
        end
        if isnan(circularity_threshold) || circularity_threshold < 0 || circularity_threshold > 100
            return;
        end
        if isnan(daughter_size_similarity) || daughter_size_similarity < 0 || daughter_size_similarity > 100
            return;
        end
        if isnan(daughter_aspect_ratio_similarity) || daughter_aspect_ratio_similarity < 0 || daughter_aspect_ratio_similarity > 100
            return;
        end
        if isnan(frames_to_track_nb) || any(frames_to_track_nb < 0)
            return;
        end
        if isnan(max_centroids_distance) || max_centroids_distance < 0
            return;
        end
        if isnan(number_of_frames_check_circularity) || number_of_frames_check_circularity < 0
            return;
        end
        if isnan(cell_apoptosis_delta_centroid_thres) || cell_apoptosis_delta_centroid_thres < 0
            return;
        end
        
         if strcmpi( frames_to_track, 'All') || isempty(frames_to_track)
             frames_to_track = '0';
         end
        
        frames_to_track_nb = str2num(frames_to_track); %#ok<ST2NM>
        % Check Validity of user input
        if any(frames_to_track_nb < 0)
            return;
        end
        
        bool = true;        
    end


%------------------------------------------------------------------------------------

    function bool = save_parameters(varargin)
        bool = false;
        tab_index = ones(NumberOfTabs,1);
        raw_images_path = get(raw_images_path_edit,'String');
        if ~isempty(raw_images_path) &&  ~strcmpi(raw_images_path(end), filesep)
          raw_images_path = [raw_images_path, filesep];
          set(raw_images_path_edit,'String', raw_images_path);
        end
        raw_images_common_name = get(raw_images_common_name_edit,'String');
        
        segmented_images_path = get(segmented_images_path_edit,'String');
        if ~isempty(segmented_images_path) && ~strcmpi(segmented_images_path(end), filesep)
          segmented_images_path = [segmented_images_path, filesep];
          set(segmented_images_path_edit,'String', segmented_images_path);
        end
        segmented_images_common_name = get(segmented_images_common_name_edit,'String');
        
        tracked_images_path = get(tracked_images_path_edit,'String');
        if ~isempty(tracked_images_path) && ~strcmpi(tracked_images_path(end), filesep)
          tracked_images_path = [tracked_images_path, filesep];
          set(tracked_images_path_edit,'String', tracked_images_path);
        end
        tracked_images_common_name = get(tracked_images_common_name_edit,'String');
        
        if ~validate_all_filepaths()
            return;
        end
        
        % Cost function params
        weight_overlap = str2num(get(weight_overlap_edit,'String')); %#ok<ST2NM>
        weight_centroids = str2num(get(weight_centroids_edit,'String')); %#ok<ST2NM>
        weight_size = str2num(get(weight_size_edit,'String')); %#ok<ST2NM>
        max_centroids_distance = str2num(get(max_centroids_distance_edit,'String')); %#ok<ST2NM>
        if(strcmp(get(frames_to_track_edit, 'String'), 'All'))
            frames_to_track = '0';
        else 
            frames_to_track = get(frames_to_track_edit, 'String');
        end
        
        frames_to_track_nb = str2num(frames_to_track); %#ok<ST2NM>

        % Mitotic params
        division_overlap_threshold = str2num(get(division_overlap_threshold_edit,'String')); %#ok<ST2NM> 
        daughter_size_similarity = str2num(get(daughter_size_similarity_edit,'String')); %#ok<ST2NM>
        daughter_aspect_ratio_similarity = str2num(get(daughter_aspect_ratio_similarity_edit,'String')); %#ok<ST2NM>
        circularity_threshold = str2num(get(circularity_threshold_edit,'String')); %#ok<ST2NM>
        number_of_frames_check_circularity = str2num(get(number_of_frames_check_circularity_edit, 'String')); %#ok<ST2NM>
        enable_cell_mitosis_flag = get(enable_cell_mitosis_checkbox, 'value');

        % Confidence Index params
        cell_life_threshold = str2num(get(cell_life_threshold_edit,'String')); %#ok<ST2NM>
        cell_apoptosis_delta_centroid_thres = str2num(get(cell_apoptosis_cent_thres_edit, 'String')); %#ok<ST2NM>
        cell_density_ci_flag = get(cell_density_checkbox, 'value');
        border_cell_ci_flag = get(border_cell_checkbox, 'value');
        
        
        % Fusion params
        cell_size_threshold = str2num(get(cell_size_threshold_edit,'String')); %#ok<ST2NM> 
        fusion_overlap_threshold = str2num(get(fusion_overlap_threshold_edit,'String')); %#ok<ST2NM>
        enable_cell_fusion_flag = get(enable_fusion_checkbox,'value');
        
        if enable_cell_fusion_flag
            set(show_fusion_matrix_button, 'enable','on');
            set(plot_fusion_linage_pb, 'enable','on');
        else
            set(show_fusion_matrix_button, 'enable','off');
            set(plot_fusion_linage_pb, 'enable','off');
        end
        
        if enable_cell_mitosis_flag
            set(show_division_matrix_button, 'enable','on');
            %set(plot_division_linage_pb, 'enable','on');
        else
            set(show_division_matrix_button, 'enable','off');
            %set(plot_division_linage_pb, 'enable','off');
        end
        
        if ~check_user_input()
            return;
        end
        
        bool = true;
    end


%------------------------------------------------------------------------------------
    function bool = validate_all_filepaths()
        bool = false;
        
        try
            raw_images_path = validate_filepath(raw_images_path);
        catch err
            if strcmpi(err.identifier, 'validate_filepath:notFoundInPath')
                tab_index(2) = 0;
                second_tab_callback();
                errordlg('Raw Images Folder Seems to be incorrect');
                return;
            else
                rethrow(err);
            end
        end
        
        try
            segmented_images_path = validate_filepath(segmented_images_path);
        catch err
            if strcmpi(err.identifier, 'validate_filepath:notFoundInPath')
                tab_index(2) = 0;
                second_tab_callback();
                errordlg('Segmented Images Folder Seems to be incorrect');
                return;
            else
                rethrow(err);
            end
        end
        
        try
            tracked_images_path = validate_filepath(tracked_images_path);
        catch err
            if strcmpi(err.identifier, 'validate_filepath:notFoundInPath')
                tab_index(2) = 0;
                second_tab_callback();
                errordlg('Tracked Images Folder Seems to be incorrect');
            else
                rethrow(err);
            end
        end
        
        if isempty(segmented_images_path)
            tab_index(2) = 0;
            second_tab_callback();
            errordlg('Segmented Images Folder required');
            return;
        end
        
        if isempty(tracked_images_path)
            tracked_images_path = [segmented_images_path 'tracked' filesep];
        end
        
        % Create folders if they don't exist
        if ~exist(tracked_images_path, 'dir'), mkdir(tracked_images_path); end
        
        bool = true;
    end

% ------------------------------------------------------------------------------------------
    function bool = check_user_input(varargin)
        try
            
            if any(frames_to_track_nb < 0)
                errordlg(['Invalid Frames to Track. Please input ''All'' or numbers greater than 1']);
                set(frames_to_track_edit,'String','All');
                return;
            end

            if isnan(cell_size_threshold) || cell_size_threshold < 0
                tab_index(3) = 0;
                third_tab_callback();
                errordlg('Min Cell Area must be a positive number.');
                set(cell_size_threshold_edit,'String','200');
                return;
            end

            if isnan(weight_overlap) || weight_overlap < 0 || weight_overlap > 100
                tab_index(3) = 0;
                third_tab_callback();
                errordlg('Weight of Cell Overlap must be between 0 and 100%');
                set(weight_overlap_edit,'String','100');
                return;
            end

            if isnan(weight_centroids) || weight_centroids < 0 || weight_centroids > 100
                tab_index(3) = 0;
                third_tab_callback();
                errordlg('Weight of Centroids Distance must be between 0 and 100%');
                set(weight_centroids_edit,'String','50');
                return;
            end

            if isnan(weight_size) || weight_size < 0 || weight_size > 100
                tab_index(3) = 0;
                third_tab_callback();
                errordlg('Weight of Cell Size must be between 0 and 100%');
                set(weight_size_edit,'String','20');
                return;
            end

            if isnan(division_overlap_threshold) || division_overlap_threshold < 0 || division_overlap_threshold > 100
                tab_index(3) = 0;
                third_tab_callback();
                errordlg('Min Mitotic Overlap must be between 0 and 100%');
                set(division_overlap_threshold_edit,'String','20');
                return;
            end

            if isnan(fusion_overlap_threshold) || fusion_overlap_threshold < 0 || fusion_overlap_threshold > 100
                tab_index(3) = 0;
                third_tab_callback();
                errordlg('Min Fusion Overlap must be between 0 and 100%');
                set(fusion_overlap_threshold_edit,'String','20');
                return;
            end

            if isnan(cell_life_threshold) || cell_life_threshold < 0
                tab_index(3) = 0;
                third_tab_callback();
                errordlg('Min Cell Life must be a positive number.');
                set(cell_life_threshold_edit,'String','32');
                return;
            end

            if isnan(circularity_threshold) || circularity_threshold < 0 || circularity_threshold > 100
                tab_index(3) = 0;
                third_tab_callback();
                errordlg('Circularity Threshold must be between 0 and 100%');
                set(circularity_threshold_edit,'String','30');
                return;
            end

            if isnan(daughter_size_similarity) || daughter_size_similarity < 0 || daughter_size_similarity > 100
                tab_index(3) = 0;
                third_tab_callback();
                errordlg('Daughter Size Similarity Threshold must be between 0 and 100%');
                set(daughter_size_similarity_edit,'String','50');
                return;
            end

            if isnan(daughter_aspect_ratio_similarity) || daughter_aspect_ratio_similarity < 0 || daughter_aspect_ratio_similarity > 100
                tab_index(3) = 0;
                third_tab_callback();
                errordlg('Daughter Aspect Ratio Similarity Threshold must be between 0 and 100%');
                set(daughter_aspect_ratio_similarity_edit,'String','70');
                return;
            end

            if isnan(cell_apoptosis_delta_centroid_thres) || cell_apoptosis_delta_centroid_thres < 0
                tab_index(3) = 0;
                third_tab_callback();
                errordlg('Cell Death Delta Centroid Threshold must be a positive number.');
                set(cell_apoptosis_cent_thres_edit,'String','10');
                return;
            end

            if isnan(max_centroids_distance) || max_centroids_distance < 0
                tab_index(3) = 0;
                third_tab_callback();
                errordlg('Max Centroids Distance must be a positive number.');
                set(max_centroids_distance_edit,'String','150');
                return;
            end

            if isnan(number_of_frames_check_circularity) || number_of_frames_check_circularity < 0
                tab_index(3) = 0;
                third_tab_callback();
                errordlg('# of Frames to Check Circularity must be a positive number.');
                set(number_of_frames_check_circularity_edit,'String','5');
                return;
            end

            bool = true;
            tab_index = ones(NumberOfTabs,1);

        catch err
            errordlg(err.message);
            bool = false;
        end
    end

% --------------------------------------------------------------------------------------------------
    function load_tracking_workspace(varargin)
        
        [FileName,PathName,~] = uigetfile('.mat');
        
        if (strcmp(FileName, 'parameters.mat'))
            if isa(FileName, 'char')
                S = importdata([PathName FileName]);
                % load the varaibles from parameters directly
                fn = fieldnames(S);
                for i = 1:numel(params)
                    if any(strcmpi(params{i}, fn))
                        eval([params{i} '=S.' params{i} ';']);
                    end
                end
                update_GUI_with_parameters();
                if check_input_batchmode()
                    set(start_tracking_pb, 'enable', 'on');
                    set(h_tabpb(3), 'enable','on');
                    set(h_tabpb(4), 'enable','on');
                end
                % update the GUI
                update_GUI_with_parameters();
                fourth_tab_callback() 
            end
        else 
            errordlg('Incorrect file input, "parameters.mat" file expected.');
            return;
        end
    end

% --------------------------------------------------------------------------------------------------

% initialize the GUI to the main tab and Initialize parameters to the 20x ones
default_parameters();

% set the default path
raw_images_path = '';
segmented_images_path = [pwd filesep 'test' filesep 'segmented_images' filesep];
tracked_images_path = [pwd filesep 'test' filesep 'tracked_images' filesep];

segmented_images_common_name = 'seg_';
raw_images_common_name = '';
tracked_images_common_name = 'tracked_';


set(raw_images_path_edit, 'String', raw_images_path);
set(raw_images_common_name_edit,'String', raw_images_common_name);
set(segmented_images_path_edit, 'String', segmented_images_path);
set(segmented_images_common_name_edit, 'String', segmented_images_common_name);
set(tracked_images_path_edit, 'String', tracked_images_path);
set(tracked_images_common_name_edit, 'String', tracked_images_common_name);

first_tab_callback();

end


% UI Control Wrappers
function edit_return = editbox(parent_handle, position, string, horz_align, color, bgcolor, fontsize, fontweight, varargin)
    edit_return = uicontrol('style','edit',...
        'parent',parent_handle,...
        'unit','normalized',...
        'fontunits', 'normalized',...
        'position',position,...
        'horizontalalignment',horz_align,...
        'string',string,...
        'foregroundcolor',color,...
        'backgroundcolor',bgcolor,...     
        'fontsize',fontsize,...
        'fontweight',fontweight);
end

function edit_return = editbox_check(parent_handle, position, string, horz_align, color, bgcolor, fontsize, fontweight, callback, varargin)
    edit_return = uicontrol('style','edit',...
        'parent',parent_handle,...
        'unit','normalized',...
        'fontunits', 'normalized',...
        'position',position,...
        'horizontalalignment',horz_align,...
        'string',string,...
        'foregroundcolor',color,...
        'backgroundcolor',bgcolor,...     
        'fontsize',fontsize,...
        'fontweight',fontweight,...
        'callback', callback);
end

function label_return = label(parent_handle, position, string, horz_align, color, bgcolor, fontsize, fontname, fontweight, varargin)
     label_return = uicontrol('style','text',...
        'parent',parent_handle,...
        'unit','normalized',...
        'fontunits','normalized',...
        'position',position,...
        'horizontalalignment',horz_align,...
        'string',string,...
        'foregroundcolor',color,...
        'backgroundcolor',bgcolor,...     
        'fontsize',fontsize,...
        'fontname', fontname,...
        'fontweight',fontweight);
 end


 function button_return = push_button(parent_handle, position, string, horz_align, color, bgcolor, fontsize, fontname, fontweight, on_off, callback, varargin)
    button_return = uicontrol('style','pushbutton',...
        'parent',parent_handle,...
        'unit','normalized',...
        'fontunits','normalized',...
        'position',position,...
        'horizontalalignment',horz_align,...
        'foregroundcolor',color,...
        'backgroundcolor',bgcolor,...
        'string',string,...        
        'fontsize',fontsize,...
        'fontname', fontname,...
        'fontweight',fontweight,...
        'enable', on_off,...
        'callback',callback);
 end
 
 
 function check_return = checkbox(parent_handle, position, string, horz_align, color, bgcolor, fontsize, fontname, fontweight, callback, varargin)
    check_return = uicontrol('style','checkbox',...
        'Parent',parent_handle,...
        'unit','normalized',...
        'fontunits', 'normalized',...
        'position',position,...
        'horizontalalignment',horz_align,...
        'string',string,...
        'foregroundcolor',color,...
        'backgroundcolor',bgcolor,...
        'fontsize', fontsize,...
        'fontname', fontname,...
        'fontweight',fontweight,...
        'callback', callback);
 end
 

% UI Panels
function panel_return = sub_panel(parent_handle, position, title, title_align, color, bgcolor, fontsize, fontname, varargin)
    panel_return = uipanel('parent', parent_handle,...
        'units', 'normalized',...
        'position',position,...
        'title',title,...
        'titleposition',title_align,...
        'foregroundcolor',color,...
        'backgroundcolor',bgcolor,...
        'fontname', fontname,...
        'fontsize',fontsize,...
        'fontweight', 'bold',...
        'visible', 'on',...
        'borderwidth',1);
end
 

