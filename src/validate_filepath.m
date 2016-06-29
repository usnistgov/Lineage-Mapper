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




function [cur_path] = validate_filepath(cur_path)
if nargin ~= 1, return, end
if ~isa(cur_path, 'char')
    error('validate_filepath:argChk','invalid input type');
end
if isempty(cur_path)
    return;
end

% first attempt is to see if this is a file
[status,message] = fileattrib(cur_path);
if status == 0
    error('validate_filepath:notFoundInPath',...
            'No such file or directory: \"%s\"',cur_path);
else
    cur_path = message.Name;
    if message.directory == 0
        % the path represents a file
        % do nothing
    else
        % the path represents a directory
        if cur_path(end) ~= filesep
            cur_path = [cur_path filesep];
        end
    end
end

end




