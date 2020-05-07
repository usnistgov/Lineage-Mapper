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


function Explore_Time_Sequence_Images

bcgrd_c = [0.8,0.8,0.8];
file_extension = 'tif';

% Define path to read images
raw_images_path = 'D:\Programming\Car_T_cell\Data\2019-08-02-CART-microgrid-sorted-Ed\Stitched_images\';
raw_images_common_name = 'Phase';
% raw_images_path = 'D:\temp\phase_stitched\\';
% raw_images_common_name = 'stitched_c01t';
image_raw_files = dir([raw_images_path '*' raw_images_common_name '*.' file_extension]);

tracked_images_path = 'D:\Programming\Car_T_cell\Data\2019-08-02-CART-microgrid-sorted-Ed\tracked_cells_AI_Densenet_clean\';
tracked_images_common_name = 'track';
% tracked_images_path = 'D:\temp\tracked\';
% tracked_images_common_name = 'tracked_stitched_t';

save_images_path = 'D:\Programming\Car_T_cell\Data\2019-08-02-CART-microgrid-sorted-Ed\tracked_cells_AI_Densenet_clean\';
save_images_common_name = 'superimpose_';
Res = 300; % Resolution of the saved image. The higher the number the lager the output file size on disk

image_tracked_files = dir([tracked_images_path '*' tracked_images_common_name '*.' file_extension]);
nb_frames = length(image_tracked_files);
frames_to_track = 1:nb_frames;

highest_cell_number = 50000;
cell_colors = jet(highest_cell_number);
cell_colors = cell_colors(randperm(highest_cell_number),:);
    
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

% Get user screen size
MP = get(0, 'MonitorPositions');
MP = MP(1,:); % removes any secondary monitors if they exist
MaxMonitorX = MP(3);

SC = get(0, 'ScreenSize');

% Set the figure window size values and calculate center of the screen position for the lower left corner
gui_width = round(MaxMonitorX/2); % Gui is half the screen wide
gui_height = round(gui_width*0.6); % fix the ratio of the GUI height to width
XBorder = (gui_width/2);
if (SC(2) == 1) % TODO why??? if breaks things
    YBorder = (gui_height/2);
else
    YBorder = ((gui_height/2) + abs(SC(2)));
end

% Create figure
track_fig = figure(...
    'units', 'pixels',...
    'Name','Explore Time-Sequence Images',...
    'NumberTitle','off',...
    'Position',[ XBorder, YBorder, gui_width, gui_height],...
    'Resize', 'on');

% Creat axes to plot figures
Haxes_tracks = axes('Parent', track_fig, 'Units', 'normalized', 'position', [0.05 0.2 0.9 0.7]);

% Create Slider for image display
slider_edit = uicontrol('style','slider',...
    'Parent',track_fig,...
    'unit','normalized',...
    'Min',1,'Max',nb_frames,'Value',1, ...
    'position',[0.1 0.15 0.8 0.03],...
    'SliderStep', [1/nb_frames, 1/nb_frames], ...
    'callback',{@plot_image});

% CheckBox: Display Cell Numbers Checkbox
disp_cell_nb_check_box = uicontrol('style','checkbox',...
    'Parent',track_fig,...
    'unit','normalized',...
    'position',[0.1 0.075 0.18 0.07],...
    'HorizontalAlignment','left',...
    'String','Disp Cell Numbers',...
    'Value',0,...
    'FontUnits', 'normalized',...
    'fontsize',.4,...
    'fontweight','bold',...
    'backgroundcolor', bcgrd_c, ...
    'callback',{@plot_image});

% Overlay Contour or Cell Area popup menu
display_contour_checkbox = uicontrol('Style','checkbox',...
    'Parent',track_fig,...
    'String','Display Contour',...
    'unit','normalized',...
    'position',[0.33 0.075 0.18 0.07], ...
    'fontweight','bold',...
    'HorizontalAlignment','center',...
    'backgroundcolor',bcgrd_c,...
    'FontUnits', 'normalized',...
    'fontsize',.4, ...
    'callback',{@plot_image});

set(display_contour_checkbox, 'value', 1);

% Pushbutton: Choose Cell Numbers to Show on Movie
uicontrol('style','push',...
    'Parent',track_fig,...
    'unit','normalized',...
    'position',[0.56 0.085 0.18 0.05],...
    'HorizontalAlignment','right',...
    'String','Choose Cell Numbers',...
    'FontUnits', 'normalized',...
    'fontsize',.5,...
    'fontweight','bold',...
    'backgroundcolor', bcgrd_c, ...
    'callback',{@plot_image});

% Edit: Cell Numbers to show
disp_user_cells_edit = uicontrol('style','Edit',...
    'Parent',track_fig,...
    'unit','normalized',...
    'position',[0.76 0.085 0.1 0.05],...
    'HorizontalAlignment','center',...
    'String','0',...
    'FontUnits', 'normalized',...
    'fontsize',.5,...
    'fontweight','bold',...
    'backgroundcolor', 'w');

% Push Button: ?
    uicontrol('style','push',...
    'Parent',track_fig,...
    'unit','normalized',...
    'position',[0.87 0.089 0.03 0.04],...
    'HorizontalAlignment','center',...
    'string','?',...
    'fontunits','normalized',...
    'FontSize', .6,...
    'fontweight','bold',...
    'callback',{@Choose_Cell_Numbers_Help_Callback});

% Text: Superimposed Name
uicontrol('style','text',...
    'Parent',track_fig,...
    'unit','normalized',...
    'position',[0.1 -0.01 0.2 0.07],...
    'HorizontalAlignment','left',...
    'String','Superimposed Name',...
    'FontUnits', 'normalized',...
    'fontsize',.4,...
    'fontweight','bold',...
    'backgroundcolor', bcgrd_c);

% Edit: Superimposed Common Name
superimposed_common_name_edit = uicontrol('Style','Edit',...
    'Parent',track_fig,...
    'unit','normalized',...
    'position',[0.33 0.015 0.18 0.05], ...
    'HorizontalAlignment','center',...
    'String','superimposed_',...
    'FontUnits', 'normalized',...
    'fontsize',.5,...
    'fontweight','bold',...
    'backgroundcolor', 'w');

% Pushbutton: Save Superimposed Images
save_superimposed_image_push = uicontrol('style','push',...
    'Parent',track_fig,...
    'unit','normalized',...
    'position',[0.56 0.015 0.3 0.05],...
    'HorizontalAlignment','right',...
    'String','Save Superimposed Images',...
    'FontUnits', 'normalized',...
    'fontsize',.5,...
    'fontweight','bold',...
    'backgroundcolor', bcgrd_c, ...
    'callback',{@save_superimposed_images});

% Push Button: ?
    uicontrol('style','push',...
    'Parent',track_fig,...
    'unit','normalized',...
    'position',[0.87 0.018 0.03 0.04],...
    'HorizontalAlignment','center',...
    'string','?',...
    'fontunits','normalized',...
    'FontSize', .6,...
    'fontweight','bold',...
    'callback',{@save_superimposed_images_Help_Callback});

set(track_fig, 'Toolbar','figure')

% Define common set of variables
contour_indicator = 0;
raw_image = 0;
image_RGB = 0;
text_location = 0;
nb_cells = 0;
user_cell_nb = 0;
plot_text = 0;
user_tracked_image = 0;
plot_image


    % Plot image and refresh axes
    function plot_image(varargin)
        % Get user input
        plot_text = get(disp_cell_nb_check_box, 'value');
        user_cell_nb = str2num(get(disp_user_cells_edit, 'String')); %#ok<ST2NM>
        
        slider_value = round(get(slider_edit, 'value'));
        
        if isempty(raw_images_path)
           contour_indicator = 0;
           set(display_contour_checkbox, 'value', 0, 'enable', 'off');
           set(save_superimposed_image_push, 'enable', 'off');
        else
           raw_image = imread([raw_images_path image_raw_files(frames_to_track(slider_value)).name]);
           contour_indicator = get(display_contour_checkbox, 'value');
        end        
        
        
        % Check Validity of user input
        if any(user_cell_nb < 0) || any(user_cell_nb > highest_cell_number)
            errordlg(['Invalid Cell Numbers. Please choose between 0 and ' num2str(highest_cell_number)]);
            return;
        end
        
        % Read corresponding images
        tracked_image = imread([tracked_images_path image_tracked_files(slider_value).name]);
        [nb_rows, nb_cols] = size(tracked_image);

        % Get the corresponding colored
        superimpose_image_GUI();
        
        % Plot with the corresponding outputs
        delete(get(Haxes_tracks, 'Children'));
        if contour_indicator == 1
            imshow(raw_image,[], 'Parent', Haxes_tracks);
            hold on                    
        end
        imshow(image_RGB, 'Parent', Haxes_tracks);
        set(Haxes_tracks,'nextplot','replacechildren');
        title(Haxes_tracks, ['Frame: ', num2str(slider_value), ', Total Cells: ', num2str(nb_cells)], ...
            'FontUnits', 'normalized', 'fontsize', 0.07, 'fontweight','bold');

        % Place the number of the cell in the image
        if plot_text
            for i = 1:nb_cells
                cell_number = tracked_image(text_location(i,2), text_location(i,1));
                
                text(text_location(i,1), text_location(i,2), num2str(cell_number), 'Parent', Haxes_tracks, 'fontunits','normalized', 'fontsize', .03, ...
                    'FontWeight', 'bold', 'Margin', .005, 'color', cell_colors(cell_number+1,:), 'BackgroundColor', 'w', 'color', 'k', 'Clipping', 'on')
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
        [edge_image, text_location] = find_edges(user_tracked_image);
        
        if contour_indicator == 1
            % Dilate the edge_image to thicken the contour plot
            edge_image = imdilate(edge_image, strel('square', 2)); edge_image = edge_image(:);
            
            % Give every pixel its color
            image_RGB = zeros(numel(edge_image),3);
            image_RGB(edge_image>0,:) = cell_colors(nonzeros(edge_image)+1, :);
            
            % reshape the matrix edge_image_RGB to be a 3D matrix with dimensions = nb_rows x nb_cols x 3
            image_RGB = reshape(image_RGB, [nb_rows nb_cols 3]);
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
    

    function save_superimposed_images(varargin)
        print_to_command('Saving Images');
        contour_indicator = get(display_contour_checkbox, 'value');
        user_cell_nb = str2num(get(disp_user_cells_edit, 'String')); %#ok<ST2NM>
        plot_text = get(disp_cell_nb_check_box, 'value');
        superimposed_common_name = get(superimposed_common_name_edit, 'String');
        zero_pad = num2str(length(num2str(nb_frames)));
        
        print_update(1,1,nb_frames);
        for i = 1:nb_frames
            print_update(2,i,nb_frames);
            % Read corresponding images
            raw_image = imread([raw_images_path image_raw_files(frames_to_track(i)).name]);
            tracked_image = imread([tracked_images_path image_tracked_files(i).name]);
            [nb_rows, nb_cols] = size(tracked_image);

            % Get the corresponding colored image and save it
            superimpose_image_GUI
            
            % Adjust contrast
            I1 = imadjust(uint16(raw_image));

            % Convert the matrix to a scaled image with intensities between 0 and 1
            I1 = mat2gray(I1);
            
            % Fuse the two images
            superimposed_image = repmat(I1,[1,1,3]);
            superimposed_image(image_RGB>0) = image_RGB(image_RGB>0);
            
            % Plot image
            h = figure; h.Visible = 'off'; h.WindowState = 'fullscreen'; 
            ax = gca; 
            ax.Position = ax.OuterPosition;
            imshow(superimposed_image)
            hold on
            
            % Place the number of the cell in the image
            if plot_text
                for j = 1:nb_cells
                    cell_number = tracked_image(text_location(j,2), text_location(j,1));
                    
                    text(text_location(j,1), text_location(j,2), num2str(cell_number), 'fontunits','normalized', 'fontsize', .03, ...
                        'FontWeight', 'bold', 'Margin', .1, 'color', cell_colors(cell_number+1,:), 'BackgroundColor', 'w')
                end
            end
            %print(h, [tracked_images_path superimposed_common_name sprintf(['%0' zero_pad 'd'],i) '.tif'], '-dtiff')
            %cdata = print(h,'-RGBImage','-r500');
            %imwrite(cdata,[save_images_path save_images_common_name sprintf(['%0' zero_pad 'd'],i) '.tif'])
            exportgraphics(ax,[save_images_path save_images_common_name sprintf(['%0' zero_pad 'd'],i) '.tif'],'Resolution',Res)
            close(h)
        end
        print_update(3,1,nb_frames);
        print_to_command('Done Saving Images');
    end
    

    function Choose_Cell_Numbers_Help_Callback(varargin)
        msgbox(sprintf(['- Input the cell numbers to plot their contours. \n\n',...
                        '- Use commas or space to seperate the cell numbers of interest: 1,7 23 \n\n',...
                        '- To plot contour of cells 14, 20 to 25 and 45, input: 14, 20:25, 45']));
    end


    function save_superimposed_images_Help_Callback(varargin)
        msgbox(sprintf(['- Give the images a common name in the edit box\n\n',...
                        '- The Labeled Masks will be superimposed on top of raw images \n\n',...
                        '- The output images can be viewed in the tracking folder']));
    end

end

