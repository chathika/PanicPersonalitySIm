function x = BwallRight( y );

params = B_wall_Parameters;
width = params(2)-params(1);

height = params(4)-params(3);

topOfDoor = params(5);
bottomOfDoor = params(4);
xMax = params(2);
x = xMax;

if y < (7-height) 
    x = xMax - width;
elseif y <= 7 
    x = (width/height)*(y-7+height)+(xMax-width);
elseif y <= 8 
    x = xMax;
elseif y <= 8+height
    x = -1*(width/height)*(y-8-height)+(xMax-width);
else
    x = xMax - width;
end
