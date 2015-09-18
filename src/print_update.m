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


function print_update(status, i, nb)

% prints a number/number to screen
% status = 1, it simply prints <i>/<nb> to screen
% status = 2, it removes a previous <i>/<nb> written to the screen and writes a new one in its place
% status = 3, it removes the last written <i>/<nb> leaving the cursor at the begining of the line

zero_pad = length(num2str(nb));
switch status
    case 1
        eval_str = ['%' num2str(zero_pad) 'i/%' num2str(zero_pad) 'i\n'];
        fprintf(eval_str,i,nb);
    case 2
        eval_str = ['%' num2str(zero_pad) 'i/%' num2str(zero_pad) 'i\n'];
        del_str = [];
        for ii = 1:(2*zero_pad + 2)
            del_str = horzcat(del_str, '\b');
        end
        fprintf([del_str eval_str],i,nb);
    case 3
        del_str = [];
        for ii = 1:(2*zero_pad + 2)
            del_str = horzcat(del_str, '\b');
        end
        fprintf(del_str);
end
end