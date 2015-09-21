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


function write_matrix_to_disk(save_path, matrix_to_save)
% open/overwrite a new file for writing, fopen with the 'w' option creates a new file or overwrites an existing file
output_fileID = fopen(save_path,'w');

% determine how many images are in the current stitching
[r, c] = size(matrix_to_save);
zero_pad = num2str(length(num2str(max(matrix_to_save(:)))));

if r > 0 && c > 0
    % loop over the images in the mosaic column wise
    for i = 1:r
        for j = 1:(c-1)
            % print to file <image name>
            fprintf(output_fileID, ['%' zero_pad 'd, '], matrix_to_save(i,j));
        end
        % print the last image name in the file appending a UNIX newline
        fprintf(output_fileID, ['%' zero_pad 'd\n'], matrix_to_save(i,c));
    end
end
% close the file
fclose(output_fileID);

end