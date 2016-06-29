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
% This is a sub-function of "centroids_trajectory_vizualization.m".
% 
% plot_centroid_trajectory_2D   plots in 2D the centroid trajectory of the chosen cell
% 
% plot_centroid_trajectory_2D(user_defined_cells, XY_centroids, birth, death)
% 
% It takes as inputs:
% - XY_centroids: a 3 dimensional matrix "XY_centroids" with the following dimensions:
%                 (frames x  number_of_cells x 2D_coordinates(X,Y))
% - user_defined_cells: which is a vector that contains the number of the cells that the user would like to plot
%                       the 2D trajectories of ([1:25 50 63]).
% Each trajectory will be ploted on a new figure from the birth of the cell til its death. For plotting
% convenience it is advised to plot no more than 5 cells at the same time. This will open 5 new windows.
% 

function plot_centroids_trajectory_2D(user_defined_cells, XY_centroids, birth, death, size_I)

% Check user inputs
% cells is the number of cells that will be taken into consideration for plotting. 
if any(user_defined_cells(:) <= 0) || ~isvector(user_defined_cells)
    error('user_defined_cells must be a vector or a positive number > 0')
end

% Make the cells vectors as a column vectors
cells = user_defined_cells(:);

cur_title = 'Cell Migration';
% find if the proposed lineage plot already exists
fig_handles = findobj('Type','figure');
for i = 1:numel(fig_handles)
    if strcmpi(get(fig_handles(i), 'Name'), cur_title)
        % bring the figure to the front and return
        figure(fig_handles(i));
        return;
    end
end

% Create the figure
figure('Name',cur_title,...
  'NumberTitle','off',...
  'Resize', 'on');
hold on
title('Cell Migration', 'fontsize', 12)
xlabel('X Centroids'), ylabel('Y Centroids')
axis([0 size_I(2) 0 size_I(1)]);

% Plot the Data
for i = 1:length(cells)
    
    % Create the X and Y coordinates vector of all cells with the starting points [X1, Y1] and the finishing
    % points [X2, Y2] of the trajectorty
    X = XY_centroids(:,cells(i),1); Y = XY_centroids(:,cells(i),2);
    X1 = X(1:end-1); Y1 = Y(1:end-1);
    X2 = X(2:end); Y2 = Y(2:end);
    
    % Plot the tracking of the cells centroids direction with an arrow pointing to the direction of the track
    tmpX = X2-X1;
    tmpY = Y2-Y1;
    % if there is a previous location to plot a tail from
    if any(~isnan(tmpX)) && any(~isnan(tmpY))
      quiver(X1, Y1, tmpX, tmpY, 0, 'MaxHeadSize', 0.1);
    end
    
    % Mark the starting point of each arrow
    plot(X1, Y1, 'o', 'color', 'm', 'MarkerSize', 2, 'MarkerFaceColor', 'm')
    
    % Mark the birth of the cell i by a big cross
    plot(XY_centroids(birth(cells(i)), cells(i), 1), XY_centroids(birth(cells(i)), cells(i), 2), ...
        'p', 'color', 'g', 'LineWidth', 3, 'MarkerFaceColor', 'g', 'MarkerSize', 10)
        
    % Mark the death of the cell i by a cross
    plot(XY_centroids(death(cells(i)), cells(i), 1), XY_centroids(death(cells(i)), cells(i), 2), ...
        'x', 'color', 'r', 'LineWidth', 3, 'MarkerSize', 10)
    
    text(XY_centroids(birth(cells(i)), cells(i), 1),XY_centroids(birth(cells(i)), cells(i), 2), num2str(cells(i)), 'fontsize', 8, 'FontWeight', 'bold', 'Clipping', 'On');
      
end

hold off



