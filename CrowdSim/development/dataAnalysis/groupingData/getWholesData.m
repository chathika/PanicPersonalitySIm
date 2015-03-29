% pixelcount,numHoles,black/blackPlusWholes,wholes/blackPlusWholes,
function data = getWholesData(image)

[blackPixelCount,whitePixelCount,wholesPixelCount] = getPixelCounts(image);

[x,numberOfHoles]=size(wholesPixelCount);
totalCount = blackPixelCount;
totalWholesCount = 0;
for i=1:1:numberOfHoles
    totalCount = totalCount + wholesPixelCount(i);
    totalWholesCount = totalWholesCount + wholesPixelCount(i);
end;

data(1) = blackPixelCount;
data(2) = numberOfHoles;
data(3) = blackPixelCount/totalCount;
data(4) = totalWholesCount/totalCount;