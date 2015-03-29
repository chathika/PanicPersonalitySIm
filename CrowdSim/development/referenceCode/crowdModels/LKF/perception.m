% This is a subroutine determining perception by a pedestrian of his surroundings. 
%  Based on this perception, the pedestrian adjusts his velocity.

function y=perception(v1x,v1y,v2x,v2y,percep_angle,percep_back)
                                 % EXPLANATION OF NOTATIONS:
                                 % *  v1x,y and v2x,y are the x,y-components of 2 {\em unit}
                                 %    vectors v1 and v2; 
                                 %    perception is a function of the angle between these vectors.
                                 %
                                 %    !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                                 %    NOTE that components sizes of components v1{x,y} and v2{x,y}
% DIMENSIONS OF v{1,2}{x,y} ->>  %         must satisfy one of the 2 possibilities:
                                 %         (i) v1{x,y} are scalars, then v2{x,y} are row vectors of
                                 %             equal length,  or
                                 %         (ii) v1{x,y} are row vectors of equal length, then v2{x,y}
                                 %              are row vectors of the same length.
                                 %         In both cases, the output of this program is a row vector
                                 %         of the same length as  v2{x,y}.
                                 %    !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                                 %
                                 % The next 2 quantities are {\em parameters} of this function:
                                 % *  percep_angle = one-sided angle in which a given pedestrian has 
                                 %    100%-perception of other pedestrians or boundaries  [rad];
                                 %    this parameter can be either a scalar or a vector of the same
                                 %    dimension as  v2{x,y}.
                                 % *  percep_back = ratio of perception "directly behind" and 
                                 %    "directly in front" of the pedestrian; 
                                 %    it has the same dimension as percep_angle.
                                 
%%% global aux_size1 aux_size2 aux_size3 aux_size4   % this is for debugging purposes only

global EPS EPSCOR

                                 

aux_size1=length(v1x);
aux_size2=length(v2x);           
aux_size3=size(percep_angle);
aux_size4=size(percep_back);   % verify if the vector components v{1,2}{x,y} etc. have a single
                                 %  dimension, or they are vectors, as explained above
if aux_size3(2) ~= aux_size4(2)
    disp(' ')
    disp('Warning:  Dimensions of percep_angle  and percep_back  are different !!!!!')
end
                                 
y=zeros(1,aux_size2);             % pre-allocate space for the output function
% % % if abs(v2x(1)) < 100*eps & abs(v2y(1)) < 100*eps
% % %     y=y;                         % this case corresponds to a pedestrian taking into account
% % %                                  %  his own velocity; by construction, this value is always
% % %                                  %  set to be zero;
% % %                                  %  (the 1st entries of v2x,v2y are specified in case that
% % %                                  %   these quantities are row vectors (i.e. for aux_size > 1))
% % % elseif max(abs(v1x.^2+v1y.^2-1)) > 10^(-9) | max(abs(v2x.^2+v2y.^2-1)) > 10^(-9)
% % %     disp('   Warning:  The vectors in the function  "perception"  are not of unit length !')
% % %     pause
% % % else
% % %     angle_v1v2=acos(v1x.*v2x+v1y.*v2y);
% % %                              % angles between corresponding entries of v1 and v2   [rad]
% % %     y(angle_v1v2<percep_angle)=1;
% % %     y(angle_v1v2>=percep_angle)=(percep_back-1)/(pi-percep_angle)*...
% % %                                 (angle_v1v2(angle_v1v2>=percep_angle)-percep_angle)+1;
% % % end

% Assign zero values to perception by a pedestrian of himself:
aux_index_zero=abs(v2x)<EPS & abs(v2y)<EPS;
y(aux_index_zero)=0;             % this case corresponds to a pedestrian NOT taking into account
                                 %  his own velocity or velocities of other pedestrians who are
                                 %  too far away from him; by construction, this value is always
                                 %  set to be zero
                                 
% Make sure that all vectors, except the zero vectors taken care of in the previous step,
%  have unit lengths:
aux_index_Nzero=~aux_index_zero; 
if aux_size1 == 1                % i.e. if v1{x,y} are scalars
    if abs(v1x^2+v1y^2-1) > 10^(-12) | ...
       max(abs((v2x(aux_index_Nzero)).^2+(v2y(aux_index_Nzero)).^2-1)) > 10^(-12) 
        disp('   Warning:  The vectors in the function  "perception"  are not of unit length !')
        aux_size1
        abs(v1x^2+v1y^2-1)
        max(abs((v2x(aux_index_Nzero)).^2+(v2y(aux_index_Nzero)).^2-1))
        pause
	end
else                             % i.e. if v1{x,y} have array length > 1
	if max(abs((v1x(aux_index_Nzero)).^2+(v1y(aux_index_Nzero)).^2-1)) > 10^(-12) | ...
       max(abs((v2x(aux_index_Nzero)).^2+(v2y(aux_index_Nzero)).^2-1)) > 10^(-12) 
        disp('   Warning:  The vectors in the function  "perception"  are not of unit length !')
        max(abs((v1x(aux_index_Nzero)).^2+(v1y(aux_index_Nzero)).^2-1))
        max(abs((v2x(aux_index_Nzero)).^2+(v2y(aux_index_Nzero)).^2-1))
        pause
	end
end

% Assign appropriate values to the remaining quadruplets of vector components:
% size_v1x=size(v1x)
% size_v1y=size(v1y)
% size_v2x=size(v2x)
% size_v2y=size(v2y)
if aux_size3 == 1                % i.e. if percep_{angle,back} are scalars
    if percep_angle > 0.99*pi
        y(aux_index_Nzero)=1;
    else
        angle_v1v2=acos((v1x.*v2x+v1y.*v2y)*EPSCOR);
                                 % angles between corresponding entries of v1 and v2   [rad]
        y(angle_v1v2<percep_angle & aux_index_Nzero)=1;
        y(angle_v1v2>=percep_angle)=(percep_back-1)/(pi-percep_angle)*...
                                    (angle_v1v2(angle_v1v2>=percep_angle)-percep_angle)+1;
    end
else                             % i.e. if percep_{angle,back} are vectors of the same dimension as  v2{x,y}
    if percep_angle > 0.99*pi
        y(aux_index_Nzero)=1;
    else
        angle_v1v2=acos((v1x.*v2x+v1y.*v2y)*EPSCOR);
                                 % angles between corresponding entries of v1 and v2   [rad]
        y(angle_v1v2<percep_angle & aux_index_Nzero)=1;
        y(angle_v1v2>=percep_angle)=(percep_back(angle_v1v2>=percep_angle)-1)./(pi-percep_angle(angle_v1v2>=percep_angle)).*...
                                    (angle_v1v2(angle_v1v2>=percep_angle)-percep_angle(angle_v1v2>=percep_angle))+1;
    end
end