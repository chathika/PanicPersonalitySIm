
 %Clear Screen
 clc;
 %Read in the image from explicit path
 Citrus = imread('snapshot_citrus.jpg');
 
 %Setup Coordinate Matrixes and Rotation Matrix
 %oriCoordMatrix = | y1 y2 y3 .... y501 |
 %                 | z1 z2 z3 .... z501 |
 %                 | x1 x2 x3 .... x501 |
 oriCoordMatrix1 = zeros(3,501);
 oriCoordMatrix2 = zeros(3,501);
 oriCoordMatrix3 = zeros(3,501);
 oriCoordMatrix4 = zeros(3,501);
 oriCoordMatrix5 = zeros(3,501);
 rotatedCoords1 = zeros(3,501);
 rotatedCoords2 = zeros(3,501);
 rotatedCoords3 = zeros(3,501);
 rotatedCoords4 = zeros(3,501);
 rotatedCoords5 = zeros(3,501);
 
 
 
 %Set up to print out a single point to check math
 rotationMatrix = zeros(3,3);
 xcomp = zeros(3,1);
 ycomp = zeros(3,1);
 zcomp = zeros(3,1);
 
 clf reset; %clear the figure
 
 %Need to flip the Y axis for calculations
 z = [0:500];
 z1 = abs(y - 500);
 
 %Line Equations solve for x = (y-b)/m from point-slope equation
 y1 = (z1 - 287)/(213/101); 
 y2 = (z1 + 200.826087)/(90/23);
 y3 = (z1 - 2735.68)/(-162/25);
 y4 = (z1 - 1816.164)/(-262/85);
 y5 = (z1 - 1380.755102)/(-75/49);
 
%Set up straight lines not implemeted in this 
% x1a = zeros(3,501);
% x1a(1,:) = 422.17
% x2a = zeros(1,501);
% x2a(1,:) = 51.3222;
% x3a = zeros(1,501);
% x3a(1,:) = 422.17;
% x4a = zeros(1,501);
% x4a(1,:) = 589.2135;
% x5a = zeros(1,501);
% x5a(1,:) = 902.0932;

 %Assign x,y values to Coordinate Matrix 
 oriCoordMatrix1(1,:) = y1;
 oriCoordMatrix1(2,:) = z;
 
 oriCoordMatrix2(1,:) = y2;
 oriCoordMatrix2(2,:) = z;
 
 oriCoordMatrix3(1,:) = y3;
 oriCoordMatrix3(2,:) = z;
 
 oriCoordMatrix4(1,:) = y4;
 oriCoordMatrix4(2,:) = z;
 
 oriCoordMatrix5(1,:) = y5;
 oriCoordMatrix5(2,:) = z;
 
 %get user info

 %rotationMatrix|A1,B1,C1| =
 %              |A2,B2,C2|
 %              |A3,B3,C3|
 fprintf('Define Rotation Matrix using Eular Angles Matrix = [phi,theta,psi]\n')
 replyphi = input('phi the angle between the x-axis and the line of nodes. Range 0 - 360. [0]?');
 if isempty(replyphi)
    replyphi = 0;
 end
 replytheta = input('theta the angle between the z-axis and the Z-axis.  Range 0 - 180. [0]?');
 if isempty(replytheta)
    replytheta = 0;
 end
 replypsi = input('psi the angle between the line of nodes and the X-axis (moving X-axis). Range 0-360 [0]?');
 if isempty(replypsi)
    replypsi = 0;
 end
 %Euler's Angles in Degrees
 degreesphi = replyphi; %is the angle between the x-axis and the line of nodes. Range 0 - 360
 degreestheta = replytheta; % is the angle between the z-axis and the Z-axis.  Range 0 - 180
 degreespsi = replypsi; % is the angle between the line of nodes and the X-axis (moving X-axis) Range 0-360
 
 % Convert to radians
 
 phi = degreesphi * pi/180
 theta = degreestheta * pi/180 
 psi = degreespsi * pi/180 
 
 % Formulas from wolfram mathworld http://mathworld.wolfram.com/EulerAngles.html
 A1 = cos(psi)*cos(phi)-cos(theta)*sin(phi)*sin(psi);
 B1 = cos(psi)*sin(phi)-cos(theta)*cos(phi)*sin(psi);
 C1 = sin(psi)*sin(theta);

 A2 =  -sin(psi)*cos(phi)- cos(theta)*sin(phi)*cos(psi);
 B2 = -sin(psi)*sin(phi)+ cos(theta)*cos(phi)*cos(psi);
 C2 = cos(psi)*sin(theta);
 
 A3 = sin(theta)*sin(phi);
 B3 = -sin(theta)*cos(phi);
 C3 = cos(theta);
 
 rotationMatrix = [A1, B1, C1; A2, B2, C2; A3, B3, C3]
 
 %Multiply Matrixes
 rotatedCoords1 = rotationMatrix * oriCoordMatrix1;
 rotatedCoords2 = rotationMatrix * oriCoordMatrix2;
 rotatedCoords3 = rotationMatrix * oriCoordMatrix3;
 rotatedCoords4 = rotationMatrix * oriCoordMatrix4;
 rotatedCoords5 = rotationMatrix * oriCoordMatrix5;
 

 %Print out a single result to check math
 oriMatrix = oriCoordMatrix2(:,200)
 
 i = [oriCoordMatrix2(1,200);0;0]
 j = [0;oriCoordMatrix2(2,200);0]
 k = [0;0;oriCoordMatrix2(3,200)]
 
 xcomp = rotationMatrix * k
 ycomp = rotationMatrix * i
 zcomp = rotationMatrix * j
 
 rotatedMatrix = rotatedCoords2(:,200)
 
 
 %Set up the figure set units to pixel
 set(figure(1),'Name','CitrusBowl','Units','Pixel');
 %Load Image and hold Image
 image(Citrus);
 hold on;
 
 %Plot Lines on top of Image
 plot(y1,z,'LineWidth',1,'Color', 'r');
 plot(y2,z,'LineWidth',1,'Color', 'c');
 plot(y3,z,'LineWidth',1,'Color', 'm');
 plot(y4,z,'LineWidth',1,'Color', 'g');
 plot(y5,z,'LineWidth',1,'Color', 'b');
 
  %Plot Lines on top of Image
 %plot(x1a,y,'LineWidth',1,'Color', 'r');
 %plot(x2a,y,'LineWidth',3,'Color', 'c');
 %plot(x3a,y,'LineWidth',3,'Color', 'm');
 %plot(x4a,y,'LineWidth',3,'Color', 'g');
 %plot(x5a,y,'LineWidth',3,'Color', 'b');
 
 %Plot the Rotated Matrix
 plot(rotatedCoords1(1,:),rotatedCoords1(2,:),'-.r')
 plot(rotatedCoords2(1,:),rotatedCoords2(2,:),'-.c')
 plot(rotatedCoords3(1,:),rotatedCoords1(2,:),'-.m')
 plot(rotatedCoords4(1,:),rotatedCoords2(2,:),'-.g')
 plot(rotatedCoords5(1,:),rotatedCoords2(2,:),'-.b')
  
 %Scale the axes
 axis([0  1000  0  480]);
 hold off;

