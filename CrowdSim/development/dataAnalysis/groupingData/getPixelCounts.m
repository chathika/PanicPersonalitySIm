function [blackPixelCount,whitePixelCount,wholesPixelCount] = getPixelCounts(image)

[L,num]=bwlabeln(image,8);

groupSizes=zeros(1,num);
for i = 2:num+1
    [rows,columns,values] = find(L==i-1);
    [x,y]=size(values);
    groupSizes(i-1) = x;
end;

[rows,columns,values] = find(image==0);
[blackPixelCount,y] = size(values);
[rows,columns,values] = find(image==1);
[whitePixelCount,y] = size(values);

index=1;
wholesPixelCount=zeros(1,num-1);
for i = 1:num
    if( groupSizes(i) < max(groupSizes) )
        wholesPixelCount(index)=groupSizes(i);
        index=index+1;
    end;
end;
