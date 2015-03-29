
 %Clear Screen
 clc;
 %Read in the image from explicit path
 Citrus = imread('snapshot_citrus.jpg');
 
 %Setup Coordinate Matrixes and Rotation Matrix
 %oriCoordMatrix = | x1 x2 x3 .... x501 |
 %                 | y1 y2 y3 .... y501 |
 %                 | z1 z2 z3 .... z501 |
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
 rotationMatrix = zeros(3,3);
 xcomp = zeros(3,1);
 ycomp = zeros(3,1);
 zcomp = zeros(3,1);
 clf reset; %clear the figure
 
 %Need to flip the Y axis for calculations
 y = [0:500];
 y1 = abs(y - 500);
 
 %Line Equations solve for x = (y-b)/m from point-slope equation
 x1 = (y1 - 287)/(213/101); 
 x2 = (y1 + 200.826087)/(90/23);
 x3 = (y1 - 2735.68)/(-162/25);
 x4 = (y1 - 1816.164)/(-262/85);
 x5 = (y1 - 1380.755102)/(-75/49);
 
 %Line Equations solve for x = (y-b)/m from point-slope equation
% x1a = zeros(3,501);
% x1a(1,:) = 422.17
 x2a = zeros(1,501);
 x2a(1,:) = 51.3222;
 x3a = zeros(1,501);
 x3a(1,:) = 422.17;
 x4a = zeros(1,501);
 x4a(1,:) = 589.2135;
 x5a = zeros(1,501);
 x5a(1,:) = 902.0932;

 %Assign x,y values to Coordinate Matrix 
 oriCoordMatrix1(1,:) = x1;
 oriCoordMatrix1(2,:) = y;
 
 oriCoordMatrix2(1,:) = x2;
 oriCoordMatrix2(2,:) = y;
 
 oriCoordMatrix3(1,:) = x3;
 oriCoordMatrix3(2,:) = y;
 
 oriCoordMatrix4(1,:) = x4;
 oriCoordMatrix4(2,:) = y;
 
 oriCoordMatrix5(1,:) = x5;
 oriCoordMatrix5(2,:) = y;
 
 %get user info

 %rotationMatrix|A1,B1,C1| =
 %              |A2,B2,C2|
 %              |A3,B3,C3|
 fprintf('Define Rotation Matrix using Eular Angles Matrix = [alpha,beta,gamma]\n')
 replyAlpha = input('alpha the angle between the x-axis and the line of nodes. Range 0 - 360. [0]?');
 if isempty(replyAlpha)
    replyAlpha = 0;
 end
 replyBeta = input('beta the angle between the z-axis and the Z-axis.  Range 0 - 180. [0]?');
 if isempty(replyBeta)
    replyBeta = 0;
 end
 replyGamma = input('gamma the angle between the line of nodes and the X-axis (moving X-axis). Range 0-360 [0]?');
 if isempty(replyGamma)
    replyGamma = 0;
 end
 %Eular's Angles in Degrees
 degreesAlpha = replyAlpha; %is the angle between the x-axis and the line of nodes. Range 0 - 360
 degreesBeta = replyBeta; % is the angle between the z-axis and the Z-axis.  Range 0 - 180
 degreesGamma = replyGamma; % is the angle between the line of nodes and the X-axis (moving X-axis) Range 0-360
 
 % Convert to radians
 
 Alpha = degreesAlpha * pi/180
 Beta = degreesBeta * pi/180 
 Gamma = degreesGamma * pi/180 
 
 % Formulas from Wikipedia http://en.wikipedia.org/wiki/Rotation_matrix
 A1 = cos(Beta)*cos(Gamma);
 B1 = sin(Alpha)*sin(Beta)*cos(Gamma) + cos(Alpha)*sin(Gamma);
 C1 = -cos(Alpha)*sin(Beta)*cos(Gamma) + sin(Alpha)*sin(Gamma);

 A2 = -cos(Beta)*sin(Gamma);
 B2 = -sin(Alpha)*sin(Beta)*sin(Gamma) + cos(Alpha)*cos(Gamma);
 C2 = cos(Alpha)*sin(Beta)*sin(Gamma) + sin(Alpha)*cos(Gamma);
 
 A3 = sin(Beta);
 B3 = -sin(Alpha)*cos(Beta);
 C3 = cos(Alpha)*cos(Beta);
 
 rotationMatrix = [A1, B1, C1; A2, B2, C2; A3, B3, C3]
 
 %Multiply Matrixes
 rotatedCoords1 = rotationMatrix * oriCoordMatrix1;
 rotatedCoords2 = rotationMatrix * oriCoordMatrix2;
 rotatedCoords3 = rotationMatrix * oriCoordMatrix3;
 rotatedCoords4 = rotationMatrix * oriCoordMatrix4;
 rotatedCoords5 = rotationMatrix * oriCoordMatrix5;
 
 oriMatrix = oriCoordMatrix2(:,200)
 
 i = [oriCoordMatrix2(1,200);0;0]
 j = [0;oriCoordMatrix2(2,200);0]
 k = [0;0;oriCoordMatrix2(3,200)]
 
 xcomp = rotationMatrix * i
 ycomp = rotationMatrix * j
 zcomp = rotationMatrix * k
 
 rotatedMatrix = rotatedCoords2(:,200)
 
 %Set up the figure set units to pixel
 set(figure(1),'Name','CitrusBowl','Units','Pixel');
 %Load Image and hold Image
 image(Citrus);
 hold on;
 
 %Plot Lines on top of Image
 plot(x1,y,'LineWidth',1,'Color', 'r');
 plot(x2,y,'LineWidth',1,'Color', 'c');
 plot(x3,y,'LineWidth',1,'Color', 'm');
 plot(x4,y,'LineWidth',1,'Color', 'g');
 plot(x5,y,'LineWidth',1,'Color', 'b');
 
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

