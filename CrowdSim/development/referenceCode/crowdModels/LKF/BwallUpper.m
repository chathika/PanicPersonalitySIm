function y = BwallUpper( x );

params = B_wall_Parameters;
width = params(2)-params(1);

height = params(4)-params(3);

topOfDoor = params(5);
bottomOfDoor = params(4);
xMax = params(2);
yMin = params(7);
yMax = params(8);

y = yMax;
if x < (xMax-width) 
    y = yMax;
elseif x <= xMax
    y = min(-1*(height/width)*(x-xMax)+8, yMax);
end
