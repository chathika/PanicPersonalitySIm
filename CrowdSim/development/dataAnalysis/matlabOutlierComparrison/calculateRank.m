%Calculates the ranks of a comparative dataset to a historical dataset.
%
%@param historicalData The data to compare against.
%@param comparativeData The data to compare.
%
%@return count The number of times that the value of the comparativeData
%is less than a value in the historicalData.
%@return rank The count divided by the number of items in the
%historicalData.
function [count, rank] = calculateRank( historicalData, comparativeData )

[x1, y1] = size( historicalData );
[x2, y2] = size( comparativeData );

%pre-allocating the Matrix for count.
count = zeros( x2, y1 );

for i = 1:y1, 
    for j = 1:x2, 
       for k = 1:x1, 
           if( historicalData( k, i ) <= comparativeData( j, i ) )
               count( j, i ) = count( j, i )+1;
           end
       end
    end
end

rank = count./x1;