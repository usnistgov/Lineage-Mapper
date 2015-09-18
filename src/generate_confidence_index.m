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




function [CI_vals, CI_indx] = generate_confidence_index(birth, death, border_cells,nb_touching_matrix, cell_life_threshold, border_cell_flag, cell_density_flag)

highest_cell_nb = size(border_cells,1);

% if using the border cells in the confidence index
if border_cell_flag
    % if the cell is not a border cell its border cell component is 1, otherwise 0
    border_cell_component = double(~border_cells);
else
    % if not using border cell component all border cell components are zero
    border_cell_component = zeros(highest_cell_nb,1);
end

% init the cell density component to zero
cell_density_component = zeros(highest_cell_nb,1);
% if cell density component is used in the confidence index then overwrite the zero init value
if cell_density_flag
    for i = 1:highest_cell_nb
        v = nonzeros(nb_touching_matrix(i,:));
        v = v - 1;
        % the cell density component is the mean number of cells that the given cell is touching when it is alive
        if ~isempty(v)
            cell_density_component(i) = mean(v);
        end
    end
end
cell_density_component = ones(highest_cell_nb,1)./(cell_density_component + 1);

cell_life_component = (death - birth + 1) > cell_life_threshold;

CI_vals = cell_life_component + border_cell_component + cell_density_component + 1;
CI_vals = CI_vals ./ 4; % normalize to 0-1

[CI_vals, CI_indx] = sort(CI_vals, 'descend');


end


