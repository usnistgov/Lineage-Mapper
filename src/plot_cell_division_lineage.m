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
% PLOT_CELL_LINEAGE plots the cell lineage of 1 or more ancestor cells

% sub_plot_cell_nb: if 0, plots all foremothers; otherwise plot just the
% lineage(s) from the specified cell numbers. Any negatives will be ignored
% plot_text is (0 or 1), turns plotting of cell numbers on or off

% cell_lineage is a cell array: where index{i}{} is cell number,
% cell_lineage{i}{1} is it an ancestor cell? Possible values(0 or 1)
% cell_lineage{i}{2} is vector of daughter cells (next generation)
% cell_lineage{i}{3} is max number of generations
% cell_lineage{i}{4} is all future children
% cell_lineage{i}{5} is the number of its ancestor  cell
% cell_lineage{i}{6} is the generation number of the current cell
% 

function plot_cell_division_lineage(sub_plot_cell_nb, plot_text, highest_cell_number, birth, death, division_matrix, nb_frames)

%%%%%%%%
% Variable and Plot Initialization
%%%%%%%%

% Extract the cell lineage data from division_matrix into a cell array
% division_matrix: each index represents the cell's number
%   and every element in the row represents its children.
%   If element is 0, there are no children.
cell_lineage = generate_cell_lineage_data(division_matrix, birth, highest_cell_number);

% Extract the foremothers vector from the cell_lineage
foremothers = zeros(highest_cell_number,1);
for i = 1:highest_cell_number, if cell_lineage{i}{1}, foremothers(i) = 1; end, end
foremothers = find(foremothers);

% Create the cell_colors vector to allow the distinct coloration of each generation
max_nb_generations = 1;
for i = 1:highest_cell_number
    max_nb_generations = max(cell_lineage{i}{3}, max_nb_generations);
end

vector_c = Vector_of_Colors(max_nb_generations);
vector_c(end,:) = []; % Delete the last color which should be yellow or white (for visualization clarity)
vector_c(end,:) = [1 0 0]; % Replace the last color by red (for visualization clarity)

% Create an alternated distributed color vector. one color is dark and the next color is clear.
cell_colors = zeros(max_nb_generations,3);
m = 1;
for i = 1:max_nb_generations
    cell_colors(m,:) = vector_c(i,:);
    cell_colors(m+1,:) = vector_c(end-i+1,:);
    m = m + 2;
end

% Create the figure to plot the cell lineage
if sub_plot_cell_nb == 0
    cur_title = 'Cell Division Lineage';
else
    cur_title = ['Cell Division Lineage of Cell: ' num2str(sub_plot_cell_nb)];
end
% find if the proposed lineage plot already exists
fig_handles = findobj('Type','figure');
for i = 1:numel(fig_handles)
    if strcmpi(get(fig_handles(i), 'Name'), cur_title)
        % bring the figure to the front and return
        figure(fig_handles(i));
        return;
    end
end

% otherwise generate the linaege plot
figure('Name',cur_title,...
  'NumberTitle','off',...
  'Resize', 'on');
title(cur_title, 'fontsize', 12)
xlabel('Frame index'), ylabel('Cell ID'), hold on
y_max = highest_cell_number + 3;
axis([0 nb_frames 0 y_max]) % Set the axis of the figure

if sub_plot_cell_nb
    % If the user specified the cell lineages to be plotted, substitute the
    % given cell numbers in place of the original foremothers, then let the
    % plotting proceed
    foremothers = sub_plot_cell_nb;
end

% Sort foremothers by total number of children they will spawn

% Initialize matrix to sort foremothers
foremother_matrix = zeros(length(foremothers),2);

for i = 1:length(foremothers)
    % Foremothers_matrix column 1 holds the cell number of the foremother
    foremother_matrix(i,1) = foremothers(i);
    
    % Foremothers_matrix column 2 holds the number of children for each
    % foremother
    foremother_matrix(i,2) = length(cell_lineage{foremothers(i)}{4});
end

% Sort the foremothers_matrix by column 2, with the smallest value on top
% so that the 'heaviest' weighted branch is on the bottom
foremother_matrix = sortrows(foremother_matrix, -2);

% Here, it sorts any cell that has the same birth frame by its death frame
% starting with the earliest.
ind = find(foremother_matrix(:,2));
if ~isempty(ind)
    single = foremother_matrix(ind(end)+1:end, :);
    single = [single birth(single(:,1)) death(single(:,1))];
    single = sortrows(single,[3 -4]);
    
    % Replace into foremother_matrix
    foremother_matrix(ind(end)+1:end, :) = single(:,1:2);
end

% Assign the now sorted foremothers back into their original row vector
foremothers = foremother_matrix(:,1)';

%%%%%%%
% Loop Through Foremothers
%%%%%%%

% Loops through foremothers plotting each one separately

% Reinitialize the top bound for plotting each foremother
y_max = 0;

for i = foremothers
    % Cell_y_vals matrix holds the y values for plotting each cell, each cell number corresponds to an index in the matrix
    cell_y_vals = zeros(length(cell_lineage{i}{4}),1);
    
    % Sets the y_min for each new foremother as the top of the previous foremother + 1
    y_min = y_max + 1;
   
    % Recursively walks the children of each foremother plotting them
    y_max = traverse_children(i,y_min);
    
end

% Resets the axis of the figure to adjust for the new highest y value
axis([0 nb_frames 0 y_max+1])
return


%%%%%%%%
% Traverse Children
%%%%%%%%

    function y_step = traverse_children(cell_nb, y_step)
        
        % If statement evaluates true when the current cell has daughters;
        % otherwise it skips to below to plot all cells without children
        if ~isempty(cell_lineage{cell_nb}{2})
            
            % nb_div is the number of daughter cells the current parent
            % splits into creating the immediate next generation
            nb_div = length(cell_lineage{cell_nb}{2});
            
            daughters_matrix = zeros(nb_div,2);
            % Initializes daughters_matrix to hold cell number in column 1
            % and the total number of children for each cell in column 2
            daughters = cell_lineage{cell_nb}{2};
            
            for z = 1:nb_div
                % Fills daughters_matrix
                % Column 1 is daughter cell numbers
                daughters_matrix(z,1) = daughters(z);
                % Column 2 is the total number of children that cell will have
                daughters_matrix(z,2) = length(cell_lineage{daughters(z)}{4});
            end
            
            % Sorts daughters_matrix by total number of children from 
            % fewest to most.
            sorted_daughters_matrix = sortrows(daughters_matrix,2);
            
            % Starts from the first index
            cur_ind = 1;
            
            % Get number of rows
            nb_rows = size(sorted_daughters_matrix,1);
            num_daughters = sorted_daughters_matrix(1,2);
            
            % Traverse through the list of daughter cells. 
            % For every cluster of elements that
            % have the same number of daughters, sort those cells by the
            % time of their deaths, starting with the earliest.
            while(cur_ind < nb_rows)
                % Extract the daughter cells with the same total number of
                % childrento be sorted by length of life.
                same_ind = sum(sorted_daughters_matrix(:,2) == num_daughters) + cur_ind - 1;
                
                % Sort those elements with the same number of daughters
                % based on the death values
                temp_death_matrix = [sorted_daughters_matrix(cur_ind:same_ind,:) death(sorted_daughters_matrix(cur_ind:same_ind,1))];
                temp_death_matrix = sortrows(temp_death_matrix, 3);
                
                % Replace sorted list with updated sorted list
                sorted_daughters_matrix(cur_ind:same_ind,:) = temp_death_matrix(:,1:2);
                
                % Increment index
                cur_ind = same_ind + 1;
                
            end

            % half_thru_daughters determines where to plot the parent cell's
            % life line in reference to the daughters life lines.
            % once half of the daughters have been plotted it plots the parent
            half_thru_daughters = nb_div/2;
            
            % Sets parent plotted flag and the parent cell number to zero
            % This flag is to indicate if the parent cell has been plotted
            parent_plotted = 0;
            parent_cell = 0;
            
            % Loops through the daughters of each cell plotting them on 
            % the graph from bottom to top
            for z = size(sorted_daughters_matrix,1):-1:1
                
                % If more than half the daughters have been plotted, plot parent
                if (z <= half_thru_daughters && ~parent_plotted) || nb_div == 1
                    
                    % Stores the y values of each cell as it is plotted to
                    % connect the cell life lines together later.
                    cell_y_vals(cell_nb) = y_step;
                    
                    % Stores the generation number of the current cell for
                    % use in coloring the lines
                    generation = cell_lineage{cell_nb}{6} + 1;
                    
                    % If plot_text is true, it plots the cell numbers
                    if plot_text
                        text(birth(cell_nb), y_step+0.2, sprintf('%u',cell_nb), 'color', cell_colors(generation,:),'fontsize', 12, 'Clipping', 'on')
                    end
                    
                    % - - Plots parent life line here - - %
                    % Plots birth point on graph
                    plot(birth(cell_nb), y_step, '.', 'color', cell_colors(generation,:), 'LineWidth', 20)
                    % Plots death point on graph
                    plot(death(cell_nb), y_step, '.', 'color', cell_colors(generation,:), 'LineWidth', 20)
                    % Plots life line connecting birth and death
                    plot([birth(cell_nb) death(cell_nb)], [y_step y_step], 'color', cell_colors(generation,:))
                    % Increments y_step so each cell has its own y value
                    y_step = y_step + 1;
                    
                    % Saves off the cell number since this is a parent cell
                    % being plotted
                    parent_cell = cell_nb;
                    % Sets the parent plotted flag to true
                    parent_plotted = 1;
                end
                
                % Recursively calls the traverse_children function on any
                % daughters of the current cell
                y_step = traverse_children(sorted_daughters_matrix(z,1),y_step);
                
                % If the last daughter has been plotted, then plot the
                % vertical lines connecting the daughters life lines to the
                % parent
                if z == 1, parent_plotted = 0; end
                
                % If there is currently a parent cell, and parent_plotted
                % is false...
                if parent_cell && ~parent_plotted
                    % Stores the generation number of the current cell for
                    % use in coloring the lines
                    generation = cell_lineage{cell_nb}{6} + 1;
                    % Plots lines connecting parent to daughters
                    % Plots line up to daughter with most number of children
                    plot([death(parent_cell), death(parent_cell)], [cell_y_vals(parent_cell), cell_y_vals(sorted_daughters_matrix(1,1))], 'color', cell_colors(generation,:))
                    % Plots line down to daughter with lease number of children
                    plot([death(parent_cell), death(parent_cell)], [cell_y_vals(parent_cell), cell_y_vals(sorted_daughters_matrix(end,1))], 'color', cell_colors(generation,:))
                    % Plots horizontal lines for all daughter cells
                    for j = 1:length(cell_lineage{parent_cell}{2})
                        plot([death(parent_cell), death(parent_cell)+1], [cell_y_vals(cell_lineage{parent_cell}{2}(j)), cell_y_vals(cell_lineage{parent_cell}{2}(j))], 'color', cell_colors(generation,:))
                    end
                    % Resets the parent cell values since the current parent
                    % cell is finished being plotted
                    parent_cell = 0;
                end
            end
        else
            
            % - - Plots all the cells which have no children - - %
            
            % Stores the generation number of the current cell for
            % use in coloring the lines
            generation = cell_lineage{cell_nb}{6} + 1;
            
            % Stores off the y value of each cell plotted
            cell_y_vals(cell_nb) = y_step;
            
            % If plot_text is true, it plots the cell numbers
            if plot_text
                text(birth(cell_nb), y_step+0.2, sprintf('%u',cell_nb), 'color', cell_colors(generation,:),'fontsize', 12, 'Clipping', 'on')
            end
            
            % Plots birth point on graph
            plot(birth(cell_nb), y_step, '.', 'color', cell_colors(generation,:), 'LineWidth', 20)
            % Plots death point on graph
            plot(death(cell_nb), y_step, '.', 'color', cell_colors(generation,:), 'LineWidth', 20)
            % Plots life line connecting birth and death
            plot([birth(cell_nb) death(cell_nb)], [y_step y_step], 'color', cell_colors(generation,:))
            % Increments y_step so each cell has its own y value
            y_step = y_step + 1;  
            
        end
    end
end



function cell_lineage = generate_cell_lineage_data(division_matrix ,birth, highest_cell_number)

% Preallocate cell_lineage array
cell_lineage = cell(highest_cell_number, 1);
all_daughters = zeros(highest_cell_number, 1);

for i = 1:highest_cell_number
    
    % Preallocate
    cell_lineage{i} = cell(6,1);
    
    % #2 - Determining daughter cells
    daughters = nonzeros(division_matrix(i,:));
    cell_lineage{i}{2} = daughters;
    
    % Assign 1 in the corresponding daughter element number
    all_daughters(daughters) = 1;
end

% Find all daughter's numbers
all_daughters = find(all_daughters);

for i = 1:highest_cell_number
    % #3 - Determining highest number of generations produced by each cell
    cell_lineage{i}{3} = getHeight(i,cell_lineage);
    
    % #4 - Determining children
    all_children = zeros(highest_cell_number, 1);
    all_children = getChildren(i, cell_lineage, all_children);
    cell_lineage{i}{4} = find(all_children);
    
    % #1 - Determining ancestor cells
    if birth(i) == 1 || ~ismember(i, all_daughters)
        % set ancestor cell to true, its parent cell to 0, and its
        % generation to 0
        cell_lineage{i}{1} = 1;
        cell_lineage{i}{5} = 0;
        cell_lineage{i}{6} = 0;
        
        % #6 - Determining current generation number
        cell_lineage = setGenerations(i, cell_lineage);
        
    else
        cell_lineage{i}{1} = 0;
    end
    
    % #5 - Determining the cells ancestor
    for j = 1:length(cell_lineage{i}{2})
        % set daughters' parent to current cell_index
        daughters = cell_lineage{i}{2};
        cell_lineage{daughters(j)}{5} = i;
    end
    
end

end

function height = getHeight(cell_number, cell_lineage)
    % Extract immediate daughter cells
    daughters = cell_lineage{cell_number}{2};
    num_daughters = length(daughters);
    % Base Case - no generation if no daughter cells present
    if num_daughters == 0
        height = 0;
        return
    end
    daughters_height = zeros(num_daughters,1);
    for i = 1:num_daughters
        daughters_height(i) = getHeight(daughters(i), cell_lineage);
    end
    
    height = 1 + max(daughters_height);
end

function all_children = getChildren(cell_number, cell_lineage, all_children)

    % Extract immediate daughter cells
    daughters = cell_lineage{cell_number}{2};
    
    % Termination condition
    if isempty(daughters), return, end

    all_children(daughters) = 1;
    for i = 1:length(daughters)
        % append to children the children of its daughters
        all_children = getChildren(daughters(i), cell_lineage, all_children);
    end

end

function cell_lineage = setGenerations(cell_number, cell_lineage)

    daughters = cell_lineage{cell_number}{2};
    
    if isempty(daughters) % no daughters
        return
    end
    
    for i = 1:length(daughters)
        % daughter's generation number = parent's generation number + 1
        cell_lineage{daughters(i)}{6} = cell_lineage{cell_number}{6} + 1;
        cell_lineage = setGenerations(daughters(i), cell_lineage);
    end


end






% 
% Vector_of_Colors  is used to generate n_uniform colors
% 
% color = Vector_of_Colors(n_uniform)
% 
% This function takes as input the n_uniform number of uniformely distributed colors to be generated. It outputs
% a matrix "color" of RGB values that is used to change the color in a figure.
% 
% The minimum number of lines of the matrix color will be = n_uniform.
% Each line is an RGB color and is unique. The white color is never chosen. If
% needed the user may added as a line after calling the function. For example color(:, end+1) = [1 1 1];
% 

function color = Vector_of_Colors(n_uniform)

% The +4 is used to take into account the fact that the white color, and the second closest color to it, will be
% deleted later.
n_uniform = n_uniform + 4;

% nc contains the number of configuration per RGB column
nc = ceil(n_uniform^(1/3));

% Compute the step
if nc == 1, step = 23; else step = 1/(nc-1); end

% Total number of the uniformily distributed colors
nt = nc^3;

% Initiate the color vector that represents the RGB
color = zeros(nt,3);

m = 1; % row counter of matrix color
h = zeros(1,3); % initial color
for i = 1:nc
    h(2) = 0; % initialize the second column of h to 0
    for j = 1:nc
        h(3) = 0; % initialize the third column of h to 0
        for k = 1:nc
            color(m,:) = h;
            m = m+1;
            h(3) = h(3) + step;
        end
        h(2) = h(2) + step;
    end
    h(1) = h(1) + step;
end

% delete the last row of the matrix color  = [1 1 1], that represents the white color
if length(color) > 3, color([end-1 end],:) = []; end

end



