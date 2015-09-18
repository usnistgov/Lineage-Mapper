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




function Explore_Tracked_Images(raw_images_path, raw_images_common_name, tracked_images_path, tracked_images_common_name, XBorder, YBorder, MaxWindowX, MaxWindowY)

%  These are required inputs, sanity check
if isempty(tracked_images_path), errordlg('Missing Tracked images path required to Explore Tracking Images'); return; end
% if isempty(tracked_images_common_name), errordlg('Missing tracked_images_common_name required to Explore Tracking Images'); return; end

% ensure that the passed in filepath is valid
if ~isempty(raw_images_path) 
    raw_images_path = validate_filepath(raw_images_path);
end
tracked_images_path = validate_filepath(tracked_images_path);

% Load Data and Get confidence index
if ~exist([tracked_images_path 'tracking_workspace.mat'],'file')
    errordlg(sprintf(['- Output of tracking not found:\n\n',...
                    '- Check paths for correctness\n\n',...
                    '- Run segmentation and tracking before plotting output']))
    return;
end


tracking_vars = load([tracked_images_path 'tracking_workspace.mat'], 'nb_frames', 'cell_colors', 'highest_cell_number', 'frames_to_track');
if ~isfield(tracking_vars, 'nb_frames'), errordlg('Missing nb_frames required to Explore Tracking Images'); return; end
if ~isfield(tracking_vars, 'cell_colors'), errordlg('Missing cell_colors required to Explore Tracking Images'); return; end
if ~isfield(tracking_vars, 'highest_cell_number'), errordlg('Missing highest_cell_number required to Explore Tracking Images'); return; end
if ~isfield(tracking_vars, 'frames_to_track'), errordlg('Missing frames_to_track required to Explore Tracking Images'); return; end
   
nb_frames = tracking_vars.nb_frames;
cell_colors = tracking_vars.cell_colors;
highest_cell_number = tracking_vars.highest_cell_number;
frames_to_track = tracking_vars.frames_to_track;

% Create figure, if found return, this prevents opening multiples of the same figure
open_fig_handle = findobj('type','figure','name','Explore Tracked Images');
if ~isempty(open_fig_handle)
    figure(open_fig_handle); %brings the figure to front
    return;
end

% Read images path
image_tracked_files = dir([tracked_images_path '*' tracked_images_common_name '*.tif']);

%  Raw images are optional and are used to display the contour images
%  If the raw image path does exist, then the number of images in the directory should
%     match the number of tracked images
if ~isempty(raw_images_path)
    image_raw_files = dir([raw_images_path '*' raw_images_common_name '*.tif']);
    if(length(image_tracked_files) ~= length(image_raw_files))
       errordlg('Missing files, the total number of TIFF images in the raw image directory and tracked image directory do not match.'); 
       return;
    end
end
% elseif isempty(raw_images_path)
%     warndlg('Missing a raw image input, contour images will not be displayed.');
% end
% if isempty(raw_images_common_name), errordlg('Missing raw_images_common_name required to Explore Tracking Images'); return; end


% Read first image and study the size of each image
tracked_image = imread([tracked_images_path image_tracked_files(1).name]);
[nb_rows, nb_cols] = size(tracked_image);

% if image is very large, send the user a warning
if numel(tracked_image) > 10^7
    warning_button = questdlg('Images are large! Visualization might be slow, Continue?','Large Images Warning?','No');
    if ~strcmp(warning_button,'Yes')
        % if the user did not select yes for continue, abort visualization
        return;
    end
end

% Create figure
track_fig = figure(...
    'units', 'pixels',...
    'Name','Explore Tracked Images',...
    'NumberTitle','off',...
    'Position',[ XBorder, YBorder, MaxWindowX, MaxWindowY ],...
    'Resize', 'on');

% Creat axes to plot figures
Haxes_tracks = axes('Parent', track_fig, 'Units', 'normalized', 'outerposition', [0.1 0.2 0.8 0.7]);

% Create Slider for image display
slider_edit = uicontrol('style','slider',...
    'Parent',track_fig,...
    'unit','normalized',...
    'Min',1,'Max',nb_frames,'Value',1, ...
    'position',[0.15 0.185 0.7 0.05],...
    'SliderStep', [1, 1]/(nb_frames - 1), ...  % Map SliderStep to whole number, Actual step = SliderStep * (Max slider value - Min slider value)
    'callback',{@sliderCallback});

% Edit: Cell Numbers to show
goto_user_frame_edit = uicontrol('style','Edit',...
    'Parent',track_fig,...
    'unit','normalized',...
    'position',[0.86 0.185 0.05 0.05],...
    'HorizontalAlignment','center',...
    'String','1',...
    'FontUnits', 'normalized',...
    'fontsize',.5,...
    'fontweight','normal',...
    'backgroundcolor', 'w',...
    'callback',{@gotoFrameCallback});

% Pushbutton: Goto Frame
uicontrol('style','push',...
    'Parent',track_fig,...
    'unit','normalized',...
    'position',[0.92 0.185 0.05 0.05],...
    'HorizontalAlignment','right',...
    'String','Go',...
    'FontUnits', 'normalized',...
    'fontsize',.5,...
    'fontweight','normal',...
    'callback',{@gotoFrameCallback});

% CheckBox: Display Cell Numbers Checkbox
disp_cell_nb_check_box = uicontrol('style','checkbox',...
    'Parent',track_fig,...
    'unit','normalized',...
    'FontUnits', 'normalized',...
    'position',[0.16 0.1 0.18 0.06],...
    'HorizontalAlignment','center',...
    'String','Disp Cell Numbers',...
    'Value',1,...
    'fontsize',.41,...
    'fontweight','normal',...
    'backgroundcolor', [0.75,0.75,0.75], ...
    'callback',{@plot_image});

% Overlay Contour or Cell Area popup menu
display_contour_checkbox = uicontrol('Style','checkbox',...
    'Parent',track_fig,...
    'unit','normalized',...
    'FontUnits', 'normalized',...
    'String','Disp Contour',...
    'position',[0.16 0.03 0.18 0.06], ...
    'fontweight','normal',...
    'HorizontalAlignment','center',...
    'fontsize',.4, ...
    'backgroundcolor', [0.75,0.75,0.75],...
    'value', 1, ...
    'callback',{@plot_image});

% Pushbutton: Choose Cell Numbers to Show on Movie
uicontrol('style','push',...
    'Parent',track_fig,...
    'unit','normalized',...
    'position',[0.4 0.11 0.23 0.05],...
    'HorizontalAlignment','right',...
    'String','Show Cell Number(s)',...
    'FontUnits', 'normalized',...
    'fontsize',.5,...
    'fontweight','normal',...
    'callback',{@plot_image});

% Edit: Cell Numbers to show
disp_user_cells_edit = uicontrol('style','Edit',...
    'Parent',track_fig,...
    'unit','normalized',...
    'position',[0.65 0.11 0.05 0.05],...
    'HorizontalAlignment','center',...
    'String','0',...
    'FontUnits', 'normalized',...
    'fontsize',.5,...
    'fontweight','normal',...
    'backgroundcolor', 'w');


% Edit: Save Images
save_images_edit = uicontrol('style','Edit',...
    'Parent',track_fig,...
    'unit','normalized',...
    'position',[0.4 0.031 0.23 0.05],...
    'HorizontalAlignment','center',...
    'String','common_image_name_',...
    'FontUnits', 'normalized',...
    'fontsize',.5,...
    'fontweight','normal',...
    'backgroundcolor', 'w');

% Pushbutton: Save Superimposed Images
    uicontrol('style','push',...
    'Parent',track_fig,...
    'unit','normalized',...
    'position',[0.65 0.031 0.13 0.05],...
    'HorizontalAlignment','right',...
    'String','Save Images',...
    'FontUnits', 'normalized',...
    'fontsize',.5,...
    'fontweight','normal',...
    'callback',{@select_superimposed_folder_Callback});

% Push Button: ?
    uicontrol('style','push',...
    'Parent',track_fig,...
    'unit','normalized',...
    'position',[0.9 0.9 0.09 0.06],...
    'HorizontalAlignment','center',...
    'string','Help',...
    'fontunits','normalized',...
    'FontSize', .4,...
    'fontweight','bold',...
    'BackgroundColor', [0.7,0.7,0.7], ...
    'callback',{@Explore_Tracked_Help_Callback});


    function Explore_Tracked_Help_Callback(varargin)
        winopen('Explore_Tracked_Images.pdf');
    end



set(track_fig, 'Toolbar','figure')

% Define common set of variables
contour_indicator = 1;
raw_image = 0;
image_RGB = 0;
text_location = 0;
nb_cells = 0;
user_cell_nb = 0;
plot_text = 0;
user_tracked_image = 0;
current_frame_nb = 1;
plot_image


    % Plot image and refresh axes
    function plot_image(varargin)
        % Get user input
        plot_text = get(disp_cell_nb_check_box, 'value');
        user_cell_nb = str2num(get(disp_user_cells_edit, 'String')); %#ok<ST2NM>
        
        if isempty(raw_images_path)
           contour_indicator = 0;
           set(display_contour_checkbox, 'value', 0, 'enable', 'off');
        else
           raw_image = imread([raw_images_path image_raw_files(frames_to_track(current_frame_nb)).name]);
           contour_indicator = get(display_contour_checkbox, 'value');
        end        
        
        
        % Check Validity of user input
        if any(user_cell_nb < 0) || any(user_cell_nb > highest_cell_number)
            errordlg(['Invalid Cell Numbers. Please choose between 0 and ' num2str(highest_cell_number)]);
            return;
        end
        
        % Read corresponding images
        tracked_image = imread([tracked_images_path image_tracked_files(current_frame_nb).name]);
        [nb_rows, nb_cols] = size(tracked_image);

        % Get the corresponding colored
        superimpose_image_GUI();
        
        % Plot with the corresponding outputs
        delete(get(Haxes_tracks, 'Children'));
        if contour_indicator == 1, 
            imshow(raw_image,[], 'Parent', Haxes_tracks);
            hold on                    
        end
        imshow(image_RGB, 'Parent', Haxes_tracks);

        title(Haxes_tracks, ['Frame: ', num2str(current_frame_nb), ', Total Cells: ', num2str(nb_cells)], ...
            'FontUnits', 'normalized', 'fontsize', 0.07, 'fontweight','bold');

        % Place the number of the cell in the image
        if plot_text
            for i = 1:nb_cells
                cell_number = tracked_image(text_location(i,2), text_location(i,1));
                
                text(text_location(i,1), text_location(i,2), num2str(cell_number), 'Parent', Haxes_tracks, 'fontunits','normalized', 'fontsize', .03, ...
                    'FontWeight', 'bold', 'Margin', .005, 'color', cell_colors(cell_number+1,:), 'BackgroundColor', 'w', 'color', 'k', 'EraseMode', 'non', 'Clipping', 'on')
            end
        end
    end


    % Get image RGB in full or just the contour per user input
    function superimpose_image_GUI
        % Display only user input cells
        if user_cell_nb
            user_input_cells_vec = zeros(highest_cell_number,1);
            user_input_cells_vec(user_cell_nb) = 1;
            user_tracked_image = zeros(nb_rows, nb_cols);
            for i = 1:numel(tracked_image)
                if tracked_image(i) && user_input_cells_vec(tracked_image(i)), user_tracked_image(i) = tracked_image(i); end
            end
        else
            user_tracked_image = tracked_image;
        end
        
        % Find the edges of segmentation and tracking of the image
        unique_numbers = unique(user_tracked_image);
        if unique_numbers(1) == 0, unique_numbers(1) = []; end
        nb_cells = length(unique_numbers);
        [edge_image, text_location] = find_edges(user_tracked_image, nb_cells);
        
        if contour_indicator
            % Dilate the edge_image to thicken the contour plot
            edge_image = imdilate(edge_image, strel('square', 2)); edge_image = edge_image(:);
           
            % reshape the matrix edge_image_RGB to be a 3D matrix with dimensions = nb_rows x nb_cols x 3
            image_RGB = mat2gray(raw_image); image_RGB = image_RGB(:);
            image_RGB = [image_RGB image_RGB image_RGB];
            image_RGB(edge_image>0,:) = cell_colors(nonzeros(edge_image), :);

            % reshape the matrix edge_image_RGB to be a 3D matrix with dimensions = number_rows x number_columns x 3
            image_RGB = reshape(image_RGB, [ nb_rows, nb_cols, 3]);
        else
            segmented_image1 = round(double(user_tracked_image+ 1));
            c_colors = [0 0 0; cell_colors];
            image_RGB = c_colors(segmented_image1(:), :);
            % reshape the matrix image_RGB to be a 3D matrix with dimensions = number_rows x number_columns x 3
            image_RGB = reshape(image_RGB, [nb_rows nb_cols 3]);
        end
    end
    

    function save_superimposed_images(output_path, varargin)
        print_to_command(['Saving Superimposed Images to: ' output_path]);
        contour_indicator = get(display_contour_checkbox, 'value');
        user_cell_nb = str2num(get(disp_user_cells_edit, 'String')); %#ok<ST2NM>
        plot_text = get(disp_cell_nb_check_box, 'value');
        superimposed_common_name = get(save_images_edit, 'String');
        zero_pad = num2str(length(num2str(nb_frames)));
        
        wb = waitbar(0,'Please wait...','Name','Please Wait...', 'CreateCancelBtn', 'setappdata(gcbf,''canceling'',1)');
        setappdata(wb,'canceling',0);
        
        print_update(1,1,nb_frames);
        for i = 1:nb_frames
            
            if getappdata(wb,'canceling')
                break;
            end
            waitbar(i/nb_frames,wb,sprintf('Saving %d of %d frames...',i, nb_frames));
            
            print_update(2,i,nb_frames);
            % Read corresponding images
            if contour_indicator, raw_image = imread([raw_images_path image_raw_files(frames_to_track(i)).name]); end
            tracked_image = imread([tracked_images_path image_tracked_files(i).name]);
            [nb_rows, nb_cols] = size(tracked_image);

            % Get the corresponding colored image and save it
            superimpose_image_GUI
            
            % Plot image
            h = figure; set(h, 'visible', 'off');
            imshow(image_RGB)
            hold on
            
            % Place the number of the cell in the image
            if plot_text
                for j = 1:nb_cells
                    cell_number = tracked_image(text_location(j,2), text_location(j,1));
                    
                    text(text_location(j,1), text_location(j,2), num2str(cell_number), 'fontunits','normalized', 'fontsize', .03, ...
                    'FontWeight', 'bold', 'Margin', .005, 'color', cell_colors(cell_number+1,:), 'BackgroundColor', 'w', 'color', 'k', 'EraseMode', 'non', 'Clipping', 'on')
                end
            end
            print(h, [output_path filesep superimposed_common_name sprintf(['%0' zero_pad 'd'],i) '.tif'], '-dtiff')
            close(h)
        end
        delete(wb); % delete the waitbar, closing it closes everything.
        print_update(3,1,nb_frames);
        print_to_command('Done Saving Superimposed Images');
    end
    

    function sliderCallback(varargin)
        current_frame_nb = ceil(get(slider_edit, 'value'));
        set(goto_user_frame_edit, 'String', num2str(current_frame_nb));
        plot_image
    end

    function gotoFrameCallback(varargin)
        if( str2num(get(goto_user_frame_edit, 'String')) <= 0 || str2num(get(goto_user_frame_edit, 'String')) > nb_frames )%#ok<ST2NM>
            errordlg(['Invalid frame, please input a number between 1 and ' num2str(nb_frames) '.']);
            set(goto_user_frame_edit, 'String', num2str(current_frame_nb));
            return;
        end
        current_frame_nb = ceil(str2num(get(goto_user_frame_edit, 'String'))); %#ok<ST2NM>
        set(slider_edit, 'value', current_frame_nb);
        plot_image
    end


    function select_superimposed_folder_Callback(varargin)
        % get directory
        sdir = uigetdir(pwd,'Select Folder:');
        if sdir ~= 0
            try
                validate_filepath(sdir);
                save_superimposed_images(sdir);
            catch err
                if (strcmp(err.identifier,'validate_filepath:notFoundInPath')) || ...
                        (strcmp(err.identifier,'validate_filepath:argChk'))
                    errordlg('Invalid directory selected');
                    return;
                else
                    rethrow(err);
                end
            end
%             
        end
    end

end



