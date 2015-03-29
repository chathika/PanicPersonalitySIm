% flips the columns of data so that the last column becomes the first
% column.
%
% @param data The data which needs to be flipped.
%
% @return fillpedMatrix data having the columns of data flipped.
function [flippedMatrix]=flip( data )

%find the of the inputed data
[x,y] = size( data );

%preallocates the output matrix
flippedMatrix = zeros(x,y);

%flip the comlumns of data
for i=1:1:y
    flippedMatrix(:,i) = data(:,y-i+1);
end