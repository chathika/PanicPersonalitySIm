% Sample a series of directories and gather the data together in a single
% matrix.
%
% @param direc The directory which contains the directories of each of the
%    data runs. These individual directories are where the data will be
%    recovered from.
% @param fileName The name of the files in the data directories.
% @param sampleTimes The times where the data should be sampled. This is an
%    array of the times to sample.
% @param sampleDuration The duration of time to sample each time a sample
%    is taken.
function [data]=sampleRun(direc,folderName,fileName,sampleTimes,sampleDuration)

[r,numberOfSamples] = size(sampleTimes);

[rows,columns]=size(dir(direc));

numberOfFiles=rows-3;
clear rows;
clear columns;

%data = zeros(numberOfFiles);

for i = 1: 1: numberOfFiles, 
    strcat(direc,folderName,num2str(i-1),'_0/',fileName);
    rawData=csvread(strcat(direc,folderName,num2str(i-1),'_0/',fileName),1,0);
    [x,y]=size(rawData);

    tempData=averageSampleData(rawData,sampleTimes,sampleDuration);
    avgData=zeros(numberOfSamples,y);

    for j = 1: 1: numberOfSamples, 
        avgData(1,:)=avgData(1,:)+tempData(j,:);
    end
    data(i,:)=avgData(1,:)./numberOfSamples;
end