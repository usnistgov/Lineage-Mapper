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


function Write_Metadata_to_CSV_File(tracked_images_path, enable_cell_fusion_flag)

% write Confidence Index to Disk
if tracked_images_path(end) ~= filesep
    tracked_images_path = [tracked_images_path filesep];
end

% Load Data and Get confidence index
if ~exist([tracked_images_path 'tracking_workspace.mat'],'file')
    return
end
load([tracked_images_path 'tracking_workspace.mat']);

% if exist('Confidence_Index','var')
%     write_matrix_to_disk([tracked_images_path 'confidence_index.csv'], Confidence_Index);
% end
% if exist('birth','var')
%     write_matrix_to_disk([tracked_images_path 'birth.csv'], birth);
% end
% if exist('death','var')
%     write_matrix_to_disk([tracked_images_path 'death.csv'], death);
% end
% if exist('division_matrix','var')
%     write_matrix_to_disk([tracked_images_path 'division.csv'], division_matrix);
% end
% if enable_cell_fusion_flag && exist('fusion_matrix','var')
%     write_matrix_to_disk([tracked_images_path 'fusion.csv'], fusion_matrix);
% end



% write the birth matrix to disk
fh = fopen([tracked_images_path 'birth.csv'],'w');
fprintf(fh,'Cell ID,Birth Frame\n');
for i = 1:numel(birth)
    fprintf(fh,'%d,%d\n',i, birth(i));
end
fclose(fh);

% write the death matrix to disk
fh = fopen([tracked_images_path 'death.csv'],'w');
fprintf(fh,'Cell ID,Death Frame\n');
for i = 1:numel(death)
    fprintf(fh,'%d,%d\n',i, death(i));
end
fclose(fh);

% write confidence Index to csv
fh = fopen([tracked_images_path 'confidence_index.csv'],'w');
fprintf(fh, 'Cell ID,Confidence Index\n');
for i = 1:size(Confidence_Index,1)
    fprintf(fh, '%d,%d\n', Confidence_Index(i,2),Confidence_Index(i,1));
end
fclose(fh);

% write object positions to csv
fh = fopen([tracked_images_path 'positions.csv'],'w');
fprintf(fh, 'Cell ID,Frame Number,X Coordinate, Y Coordinate\n');
for i = 1:numel(centroids)
  cents = centroids{i};
  for j = 1:size(cents,1)
    global_id = renumbering_vectors{i}(j);
    frame_number = i;
    x = cents(j,1);
    y = cents(j,2);
    fprintf(fh, '%d,%d,%.14f,%.14f\n', global_id, frame_number, x, y);
  end
end
fclose(fh);



if exist('division_matrix','var')
    % write division to csv
    fh = fopen([tracked_images_path 'division.csv'],'w');
    fprintf(fh, 'Mother ID');
    for i = 1:size(division_matrix,2)
        fprintf(fh,',Daughter%d ID', i);
    end
    fprintf(fh,'\n');
    for i = 1:size(division_matrix,1)
        printed_row = false;
        for j = 1:size(division_matrix,2)
            if division_matrix(i,j) > 0
                printed_row = true;
                if j == 1, fprintf(fh,'%d',i); end
                fprintf(fh,',%d',division_matrix(i,j));
            end
            if printed_row && j == size(division_matrix,2)
                fprintf(fh,'\n');
            end
        end
    end
    fclose(fh);
end

if enable_cell_fusion_flag && exist('fusion_matrix','var')
    % write fusion to csv
    fh = fopen([tracked_images_path 'fusion.csv'],'w');
    for i = 1:size(fusion_matrix,2)
        fprintf(fh,'Fusion ID @t,');
    end
    fprintf(fh, 'Fused ID @t+1\n');
    for i = 1:size(fusion_matrix,1)
        printed_row = false;
        for j = 1:size(fusion_matrix,2)
            if fusion_matrix(i,j) > 0
                printed_row = true;
                fprintf(fh,'%d,',fusion_matrix(i,j));
            else
                if printed_row
                    fprintf(fh,',');
                end
            end
            if printed_row && j == size(fusion_matrix,2)
                 fprintf(fh,'%d\n',i);
            end
        end
    end
    fclose(fh);
end

% timeframe_of_fusion,ID_after_fusion,ID1_before_fusion,ID2_before_fusion,ID3_before_fusion,...
if enable_cell_fusion_flag && exist('fusion_matrix','var')
    % write lineage to csv
    fh = fopen([tracked_images_path 'lineage.csv'],'w');
    fprintf(fh, 't (time),');
    fprintf(fh, 'Fused ID @t+1,');
    for i = 1:size(fusion_matrix,2)
        fprintf(fh,'Fusion ID @t,');
    end
    fprintf(fh,'\n');
    
    for i = 1:size(fusion_matrix,1)
        % if there are fusion events in this row
        if any(fusion_matrix(i,:))
            fprintf(fh,'%d,',birth(i)-1);
            fprintf(fh,'%d,',i);
            
            % print the fused cell numbers
            for j = 1:size(fusion_matrix,2)
                if fusion_matrix(i,j) > 0
                    fprintf(fh,'%d,',fusion_matrix(i,j));
                else
                    fprintf(fh,',');
                end
            end
            fprintf(fh,'\n');
        end
    end
    fclose(fh);
end



if exist('cell_apoptosis','var')
    fh = fopen([tracked_images_path 'apoptosis.csv'],'w');
    fprintf(fh,'Cell ID\n');
    for i = 1:numel(cell_apoptosis)
        if cell_apoptosis(i) > 0
            fprintf(fh,'%d\n',i);
        end
    end
    fclose(fh);
end



end


