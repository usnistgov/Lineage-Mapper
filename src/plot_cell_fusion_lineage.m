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



% function plot_cell_fusion_lineage(fusion_matrix ,birth, death)
% 
% plots the fusion matrix lineages



function plot_cell_fusion_lineage(fusion_matrix ,birth, death, text_flag, cell_nb_to_plot)

% extract the highest cell number
highest_cell_number = numel(birth);
% create an empty struct
st = struct([]);
% define the struct fields and the maximum number of elements in the structure
st(highest_cell_number,1).generation = [];
st(highest_cell_number,1).parents = [];
st(highest_cell_number,1).birth = [];
st(highest_cell_number,1).death = [];
st(highest_cell_number,1).y_val = [];


% populate the struct 
for i = 1:highest_cell_number
    
    % fill in the basic information for every cell in fusion matrix
    st(i).generation = 1;
    st(i).birth = birth(i);
    st(i).death = death(i);
end
for i = 1:highest_cell_number
    % if the current cell number (i) was not the result of a fusion event, continue
    if sum(fusion_matrix(i,:)) == 0, continue, end
    
    % copy the parents numbers to the struct
    st(i).parents = nonzeros(fusion_matrix(i,:))';
    % this generation is the max generation between the 2 parents + 1
    
    for k = 1:numel(st(i).parents)
        st(i).generation = max(st(i).generation, st(st(i).parents(k)).generation);
    end
    % generation is the parents generation +1
    st(i).generation = st(i).generation + 1;
end


% determine how many generations of fusion happened
generation = [st.generation]';
max_nb_generations = max(generation);

% since the struct was filled starting with fusion events, the binary tree is inverted
% what now has to happen is take that inverted tree where a node as 2 parents, and flip it
% this is done by looking thru the tree recursively starting with the node with the highest generation number
% every node that gets touch in that traversal is recorded as covered.
% the process is repeated with the node that has the second highest generation.
% the process ends when all the node have been covered

% starting index will hold the list of nodes that are parents for a fusion tree
% for example if you recursively traverse starting with every node in starting_index then you will cover the entire tree
% aka each starting_indx_val holds a complete binary tree
starting_indx_vals = [];
% this hold the list of nodes to ensure that full coverage is obtained
tf_vec = false(highest_cell_number,1);
% loop until all the nodes are covered and the loop is broken
while true
	% nullify the generation number for those node covered
    generation(tf_vec) = 0;
    % find the node with the highest generation
    [val, indx] = max(generation);
    if val == 1, break; end % break the loop if all nodes are covered
    % add the current starting node to the list
    starting_indx_vals = [starting_indx_vals indx]; %#ok<AGROW>
    generation(indx) = 0; % nullify the generation number for the startin node
    
    % traverse the tree searching the nodes
    tf_vec = tree_traverse_check(st, tf_vec, indx);
end

if ~isempty(cell_nb_to_plot) && cell_nb_to_plot(1) ~= 0
    to_delete = false(numel(starting_indx_vals),1);
    for i = 1:numel(starting_indx_vals)
        % search the trees and remove any tree that does not contain a node with a number from cell_nb_to_plot
        tf_vec = zeros(highest_cell_number,1);
        tf_vec = tree_traverse_search(st, tf_vec, starting_indx_vals(i), cell_nb_to_plot);
        if ~any(tf_vec == 2)
            to_delete(i) = true;
        end
    end
    starting_indx_vals(to_delete) = [];
end



% generate the coloring vector for coloring each generation
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

% generate the lineage plot
cur_title = 'Cell Fusion Lineage';
% find if the proposed lineage plot already exists
fig_handles = findobj('Type','figure');
for i = 1:numel(fig_handles)
    if strcmpi(get(fig_handles(i), 'Name'), cur_title)
        % bring the figure to the front and return
        figure(fig_handles(i));
        return;
    end
end

figure('Name',cur_title,...
  'NumberTitle','off',...
  'Resize', 'on');
title(cur_title, 'fontsize', 12)
xlabel('Frame index'), ylabel('Cell ID'), hold on
y_max = highest_cell_number + 3;
axis([0 max(death) 0 y_max]) % Set the axis of the figure
y_step = 1; % init the y_step to 1

% in order traveral of the tree
% plot each binary tree held in starting_indx_vals
for i = 1:numel(starting_indx_vals)
    % plot the current tree nodes (aka the cell lifespans)
    [y_step, ~, st] = plot_tree(st, starting_indx_vals(i), y_step, text_flag, cell_colors);
    % plot the connections parents to children for each portion of the tree
    % this connects the death of a set of cell parents to the child 
    plot_children_parent_connection(st, starting_indx_vals(i), cell_colors);
end

% adjust the plot height to match the highest value used
axis([0 max(death) 0 y_step+1]);


end



function [y_step, bool, st] = plot_tree(st, indx, y_step, plot_text, cell_colors)

% if the bottom has been found, return
if isempty(st(indx).parents)
    bool = true;
    return;
end

nb_parents = numel(st(indx).parents);
half = floor(nb_parents/2);
for i = 1:half
    % recursively search the tree
    [y_step, bool, st] = plot_tree(st, st(indx).parents(i), y_step, plot_text, cell_colors);
    if bool
        % plot the parent
        [y_step, st] = plot_node(st, st(indx).parents(i), y_step, plot_text, cell_colors);
    end
end
% plot the node for the child
[y_step, st] = plot_node(st, indx, y_step, plot_text, cell_colors);

for i = (half+1):nb_parents
    % recursively search the tree
    [y_step, bool, st] = plot_tree(st, st(indx).parents(i), y_step, plot_text, cell_colors);
    if bool
        % plot the parent 2
        [y_step, st] = plot_node(st, st(indx).parents(i), y_step, plot_text, cell_colors);
    end
end



% % recursively search the tree
% [y_step, bool, st] = plot_tree(st, st(indx).parents(1), y_step, plot_text, cell_colors);
% if bool
%     % plot the parent 1
%     [y_step, st] = plot_node(st, st(indx).parents(1), y_step, plot_text, cell_colors);
% end
% 
% % plot the node for the child
% [y_step, st] = plot_node(st, indx, y_step, plot_text, cell_colors);
% 
% % recursively search the tree
% [y_step, bool, st] = plot_tree(st, st(indx).parents(2), y_step, plot_text, cell_colors);
% if bool
%     % plot the parent 2
%     [y_step, st] = plot_node(st, st(indx).parents(2), y_step, plot_text, cell_colors);
% end

bool = false;

end



function [y_step, st] = plot_node(st, indx, y_step, plot_text_flag, cell_colors) 

st(indx).y_val = y_step;
% If plot_text is true, it plots the cell numbers
if plot_text_flag
    text(st(indx).birth, y_step+0.2, sprintf('%u',indx), 'color', cell_colors(st(indx).generation,:),'fontsize', 12, 'Clipping', 'on')
end
% Plots birth point on graph
plot(st(indx).birth, y_step, '.', 'color', cell_colors(st(indx).generation,:), 'LineWidth', 20)
% Plots death point on graph
plot(st(indx).death, y_step, '.', 'color', cell_colors(st(indx).generation,:), 'LineWidth', 20)
% Plots life line connecting birth and death
plot([st(indx).birth, st(indx).death], [y_step y_step], 'color', cell_colors(st(indx).generation,:))

% Increments y_step so each cell has its own y value
y_step = y_step + 1;
end

function plot_children_parent_connection(st, indx, cell_colors)
if ~isempty(st(indx).parents)
    for i = 1:numel(st(indx).parents)
        plot_children_parent_connection(st, st(indx).parents(i), cell_colors);
    end
    %     plot_children_parent_connection(st, st(indx).parents(1), cell_colors);
    %     plot_children_parent_connection(st, st(indx).parents(2), cell_colors);
    nb_parents = numel(st(indx).parents);
    for i = 1:nb_parents
        for j = (i+1):nb_parents
            parent1_nb = st(indx).parents(i);
            parent2_nb = st(indx).parents(j);
            % Plots line up to daughter with most number of children
            plot([st(parent1_nb).death, st(parent1_nb).death], [st(parent1_nb).y_val, st(parent2_nb).y_val], 'color', cell_colors(st(parent1_nb).generation,:));
            % plot the horizontal connection
            plot([st(parent2_nb).death, st(indx).birth], [st(indx).y_val, st(indx).y_val], 'color', cell_colors(st(indx).generation,:));
        end
    end
    %     parent1_nb = st(indx).parents(1);
    %     parent2_nb = st(indx).parents(2);
    %     % Plots line up to daughter with most number of children
    %     plot([st(parent1_nb).death, st(parent1_nb).death], [st(parent1_nb).y_val, st(parent2_nb).y_val], 'color', cell_colors(st(parent1_nb).generation,:))
    %     % plot the horizontal connection
    %     plot([st(parent2_nb).death, st(indx).birth], [st(indx).y_val, st(indx).y_val], 'color', cell_colors(st(indx).generation,:))
end
end



function tf_vec = tree_traverse_check(st, tf_vec, indx)
% this tree is inverted from expected, what are called parents are the nodes children, as this is the fusion tree

if tf_vec(indx) > 0
    errordlg('Fusion tree has a loop');
end

% check validity of the current node
if isempty(st(indx).parents)
    tf_vec(indx) = 1; % this has no children
    return;
end

for i = 1:numel(st(indx).parents)
    % check parent i
    tf_vec = tree_traverse_check(st, tf_vec, st(indx).parents(i));
    tf_vec( st(indx).parents(i)) = 1;
end

end

function tf_vec = tree_traverse_search(st, tf_vec, indx, cell_nbs)
% this tree is inverted from expected, what are called parents are the nodes children, as this is the fusion tree

if tf_vec(indx) == 1
    errordlg('Fusion tree has a loop');
end

if any(cell_nbs == indx)
    tf_vec(indx) = 2;
end
% check validity of the current node
if isempty(st(indx).parents)
    tf_vec(indx) = 1;
    return;
end

for i = 1:numel(st(indx).parents)
    % check parent i
    tf_vec = tree_traverse_search(st, tf_vec, st(indx).parents(i), cell_nbs);
end

end


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






