function y = BwallLower( x );

params = B_wall_Parameters;
width = params(2)-params(1);

height = params(4)-params(3);

topOfDoor = params(5);
bottomOfDoor = params(4);
xMax = params(2);
yMin = params(7);
yMax = params(8);

y = yMin;
if x < (xMax-width) 
    y = yMin;
elseif x <= xMax
    y = max((height/width)*(x-xMax)+7,yMin);
end
