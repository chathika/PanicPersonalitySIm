%RGB = imread('.\data\groupImage_0.0_0.bmp');
%RGB = imread('.\data\groupImage_0.0_1.bmp');
%RGB = imread('.\data\primaryGroupImage.bmp');
%RGB = imread('.\data\test4.bmp');
RGB = imread('.\data\primaryGroupImage_328.bmp');

invMat=ones(size(RGB));
RGB=xor(RGB,invMat);

data = getWholesData(RGB);