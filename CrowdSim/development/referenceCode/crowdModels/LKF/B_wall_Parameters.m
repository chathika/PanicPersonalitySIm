function values = B_wall_Parameters();

xMin = 0;
xMax = 12;
yMin = 0;
yMax = 15;
theta = 60;
width = 4;

height = abs(width*tan((theta/180)*(pi)));
xMin = xMax - width;
topOfDoor = 8;
bottomOfDoor = 7;
wall_yMin = 7 - height;
wall_yMax = 8 + height;

values(1) = xMin;
values(2) = xMax;
values(3) = wall_yMin; 
values(4) = bottomOfDoor;
values(5) = topOfDoor;
values(6) = wall_yMax;
values(7) = yMin;
values(8) = yMax;