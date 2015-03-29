%This normalizes two datasets. The mean and standard deviation ar
%calculated from dataSet1 then used to normalize both datasets.
%
% @param dataSet1 The dataset to be normalized and used to norlamize
%           dataset2.
% @param dataSet2 The second dataset to be normalized. It is normalized
%           against the first dataset.
%
% @return dataSet1Noramlized The normalization of the first dataset.
% @return dataSet2Normalized The normalization of the second dataset.
% @return meanVal The mean of the dataset1.
% @return stdDev The standard deviation of the dataset1.
function [dataSet1Noramlized,dataSet2Normalized,meanVal,stdDev]=normalizeDataSets(dataSet1,dataSet2)

[x1,y1] = size(dataSet1);
[x2,y2] = size(dataSet2);
dataSet1Noramlized = zeros(x1,y1);
dataSet2Normalized = zeros(x2,y2);

meanVal = mean(dataSet1);
stdDev = std(dataSet1);

for i = 1: 1: x1, 
    dataSet1Noramlized(i,:)=(dataSet1(i,:)-meanVal)./stdDev;
end

for i = 1: 1: x2, 
    dataSet2Normalized(i,:)=(dataSet2(i,:)-meanVal)./stdDev;
end


