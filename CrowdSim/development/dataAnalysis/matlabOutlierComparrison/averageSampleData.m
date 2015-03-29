function [data]=averageSampleData(sampledData,times,duration)

[x,numberOfSamples] = size(times);
[x,y] = size(sampledData);

currentSampleID=1;
currentRow=1;
data=zeros(numberOfSamples,y);

while currentSampleID <= numberOfSamples,
    currentTime=0;
    while (currentTime < (times(currentSampleID)-duration/2)) && currentRow<x,
        currentRow=currentRow+1;
        currentTime=sampledData(currentRow,2);
    end
    tempDataID=1;
    tempData=zeros(1,y);
    count=0;
    while (currentTime < (times(currentSampleID)+duration/2)) && currentRow<x,
        tempData(tempDataID,:)=tempData(tempDataID,:)+sampledData(currentRow,:);
        currentTime=sampledData(currentRow,2);
        count=count+1;
        currentRow=currentRow+1;
    end
    if count == 0,
    else
        data(currentSampleID,:)=tempData(tempDataID,:)./count;
    end
    currentSampleID = currentSampleID+1;
end
