% This file processes results of crowdsim_*.m.
clear;clc;
cd crowdsim_data

frame=1;

%%% N_of_excitement_levels=4;             % this is the number of excitement levels which are distinguished
                                      %  by this plotting program;
                                      %  the most excited peds are plotted as darkest-faced circles,
                                      %  and the least excited ones are plotted as the white-faced circles
%%% % This map sets up the color map for the peds in accordance with the above statement:
%%% for mm=1:N_of_excitement_levels
%%%     aux_face_colors=1-(mm-1)/(N_of_excitement_levels-1);
%%%     face_colors(mm,:)=[aux_face_colors*[1 1] 1];
%%% end
% 
% Fix 4 excitement levels of peds: 
%  Level 1:  Excitement factor <= 0.5,
%  Level 2:  0.5 < Excitement factor <= 1,
%  Level 3:  1 < Excitement factor <= Ped_excitement_max_av,
%  Level 4:  Excitement factor > Ped_excitement_max_av;
%   and introduce the corresponding colors for painting the peds in the movie:
face_color_exc_below_1=[1 1 1];
face_color_exc_below_max=[0.5 0.5 1];
face_color_exc_above_max=[0 0 1];

aux_loadedhead='2_J2_';
aux_loadedname= strcat(aux_loadedhead, 'Part');
%movie_speed='fast';
movie_speed='normal';

% First, load the parameters which allow one to draw the room, doors, etc.
loadedname0=[aux_loadedname '0'];
load(loadedname0)                     % load the file contains parameters of the room etc.
% load 21226_J1_Part0
theta = atan((Door_right_low(1)-Door_right_lowLow)/(B_wall_xmaxMax-B_wall_xmaxMin))*180/pi;
 
%
%  ------------------   Display the boundaries and attractions on the screen  -------------------
%  -------  ( Symbols representing pedestrians will be redrawn during the computation, ----------
%  ---         but the boundaries and attractions will not be redrawn.)                       ---
fig_with_peds_n=131;                    % handle # assigned to the figure which display the moving peds
fig_with_excitement_n=fig_with_peds_n+1000;
                                      % handle # assigned to the figure which display the excitement factor
                                      
figure(fig_with_peds_n);
clf
axis([B_outerwall_left+1 B_outerwall_right+0.5 B_outerwall_lower+1 B_outerwall_upper-1]);
hold on
% Draw the left wall with doors whose posts have finite thickness:
if N_doors_left > 0
    plot(B_wall_xmin*[1 1],[B_wall_ymin Door_left_low(1)],'LineWidth',3);
    for n=1:N_doors_left-1
        plot(B_wall_xmin*[1 1],[Door_left_up(n) Door_left_low(n+1)],'LineWidth',3);
    end
    plot(B_wall_xmin*[1 1],[Door_left_up(N_doors_left) B_wall_ymax],'LineWidth',3);
    for n=1:N_doors_left
        plot([B_wall_xmin-wall_thickness B_wall_xmin], Door_left_low(n)*[1 1],'LineWidth',3);
        plot([B_wall_xmin-wall_thickness B_wall_xmin], Door_left_up(n)*[1 1],'LineWidth',3);
    end
else                                             % i.e. if there are no doors on the left wall
    plot(B_wall_xmin*[1 1],[B_wall_ymin B_wall_ymax],'LineWidth',3);
end
% Draw the right wall with doors whose posts have finite thickness:
if N_doors_right > 0
    plot(B_wall_xmaxMax*[1 1],[B_wall_ymin Door_right_low(1)],'LineWidth',3);
    plot(B_wall_xmaxMin*[1 1],[B_wall_ymin Door_right_lowLow],'LineWidth',3);
    plot([B_wall_xmaxMin B_wall_xmaxMax],[Door_right_lowLow Door_right_low(1)],'LineWidth',3);
    plot(B_wall_xmaxMin*[1 1],[Door_right_lowHigh B_wall_ymax],'LineWidth',3);

    plot([B_wall_xmaxMin B_wall_xmaxMax],[Door_right_lowHigh Door_right_up(1)],'LineWidth',3);
  
    plot(B_wall_xmaxMin*[1 1],[Door_right_lowHigh B_wall_ymax],'LineWidth',3);
    for n=1:N_doors_right-1
        plot(B_wall_xmaxMax*[1 1],[Door_right_up(n) Door_right_low(n+1)],'LineWidth',3);
    end
    plot(B_wall_xmaxMax*[1 1],[Door_right_up(N_doors_right) B_wall_ymax],'LineWidth',3);
    for n=1:N_doors_right
        plot([B_wall_xmaxMax B_wall_xmaxMax+wall_thickness], Door_right_low(n)*[1 1],'LineWidth',3);
        plot([B_wall_xmaxMax B_wall_xmaxMax+wall_thickness], Door_right_up(n)*[1 1],'LineWidth',3);
    end
else                                             % i.e. if there are no doors on the right wall
    plot(B_wall_xmaxMax*[1 1],[B_wall_ymin B_wall_ymax],'LineWidth',3);
end
% Draw the lower wall with doors whose posts have finite thickness:
if N_doors_lower > 0
    plot([B_wall_xmin Door_lower_left(1)],B_wall_ymin*[1 1],'LineWidth',3);
    for n=1:N_doors_lower-1
        plot([Door_lower_right(n) Door_lower_left(n+1)],B_wall_ymin*[1 1],'LineWidth',3);
    end
    plot([Door_lower_right(N_doors_lower) B_wall_xmaxMax],B_wall_ymin*[1 1],'LineWidth',3);
    for n=1:N_doors_lower
        plot(Door_lower_left(n)*[1 1], [B_wall_ymin-wall_thickness B_wall_ymin], 'LineWidth',3);
        plot(Door_lower_right(n)*[1 1], [B_wall_ymin-wall_thickness B_wall_ymin], 'LineWidth',3);
    end
else                                             % i.e. if there are no doors on the lower wall
    plot([B_wall_xmin B_wall_xmaxMax],B_wall_ymin*[1 1],'LineWidth',3);
end
% Draw the upper wall with doors whose posts have finite thickness:
if N_doors_upper > 0
    plot([B_wall_xmin Door_upper_left(1)],B_wall_ymax*[1 1],'LineWidth',3);
    for n=1:N_doors_upper-1
        plot([Door_upper_right(n) Door_upper_left(n+1)],B_wall_ymax*[1 1],'LineWidth',3);
    end
    plot([Door_upper_right(N_doors_upper) B_wall_xmaxMax],B_wall_ymax*[1 1],'LineWidth',3);
    for n=1:N_doors_upper
        plot(Door_upper_left(n)*[1 1], [B_wall_ymax B_wall_ymax+wall_thickness], 'LineWidth',3);
        plot(Door_upper_right(n)*[1 1], [B_wall_ymax B_wall_ymax+wall_thickness], 'LineWidth',3);
    end
else                                             % i.e. if there are no doors on the upper wall
    plot([B_wall_xmin B_wall_xmaxMax],B_wall_ymax*[1 1],'LineWidth',3);
end
% Draw columns:
aux_2draw_columns=[0:0.01:2*pi];
for k=1:N_columns
    plot(Column_c_x(k)+Column_radius(k)*sin(aux_2draw_columns),...
         Column_c_y(k)+Column_radius(k)*cos(aux_2draw_columns),'LineWidth',3);
end
% Draw attractions:
plot(Attraction_x,Attraction_y,'o',...
    'MarkerSize',14,'MarkerEdgeColor',[1 .7 .7],'MarkerFaceColor','g');

pause(0.1)

% ----------------- Auxiliary, purely Matlab-related, step of initiating -----------------------
% -----------------               plotting of pedestrians:                 ---------------------
%
% Draw initial positions of pedestrians.
%  For each excitement level, draw all pedestrians, since the max # of peds in a given level is determined
%  by this step, and this # can possibly be as much as N_peds for any of the excitement levels during the simulation.
%  Also, adding small random values to the peds' coordinates is purely Matlab-related: 
%  if all (or, actually, any two) graphs overlap, then Matlab displays a white line, which is not visible.
for mm=1:3  %%% N_of_excitement_levels
    p4plot1(mm)=plot(x_ped+rand*0.1,y_ped+rand*0.1,'o','EraseMode','xor','MarkerSize',12,...
                     'MarkerEdgeColor','b','MarkerFaceColor','none');
end
% Now draw "noses" of the peds at the initial moment.
p4plot2=plot(x_ped+ex_ped.*ped_radius,y_ped+ey_ped.*ped_radius,'o','EraseMode','xor','MarkerSize',5,...
            'MarkerEdgeColor','r','MarkerFaceColor','r');
% axis([B_outerwall_left-1 B_outerwall_right+1 B_outerwall_lower-1 B_outerwall_upper+1]);
% hold on

% Create a figure window where excitement factor will be displayed:
%%% figure(fig_with_excitement_n);

pause

num_parts=N_parts_of_calcs;                       % number of files with the data which will be played in this movie
time_elapsed=0;                                   % counter for the time that has elapsed since the start of the simulation
if strcmp(movie_speed,'fast') == 1
    n_start=10;
elseif strcmp(movie_speed,'normal') == 1
    n_start=1;
end
for n_file=n_start:num_parts  
	thename=[aux_loadedname int2str(n_file)];
	load(thename);
    n_file
	for m=1:size(x_ped_saved,1)
		x_ped_current=x_ped_saved(m,:);
		y_ped_current=y_ped_saved(m,:);
        ex_ped_current=ex_ped_saved(m,:);
		ey_ped_current=ey_ped_saved(m,:);
        excitement_ped_current=excitement_ped_saved(m,:);
        %%% delta_excitement=(max(excitement_ped_current)-min(excitement_ped_current)+2000*eps)/N_of_excitement_levels;
        %%% excitement_levels=[min(excitement_ped_current)-1000*eps:delta_excitement:max(excitement_ped_current)+1000*eps];
        for mm=1:3  %%% N_of_excitement_levels
            counter_peds_in_exc_level(mm)=0;      % this is the initial value of a counter used to count all peds
                                                  %  who have the mm-th  level of excitement at a given time step
        end    
        clear x_ped_exc_level* y_ped_exc_level*   % IMPORTANT: This is a necessary step; w/o clearing these arrays at
                                                  %            every plotting step, there will be garbage on the screen!
        for i=1:N_peds
% % %             for mm=1:N_of_excitement_levels
% % %                 if excitement_ped_current(i) >= excitement_levels(mm) && ...
% % %                    excitement_ped_current(i) < excitement_levels(mm+1)
% % %                     counter_peds_in_exc_level(mm)=counter_peds_in_exc_level(mm)+1;
% % %                     x_ped_exc_level(mm,counter_peds_in_exc_level(mm))=x_ped_current(i);
% % %                     y_ped_exc_level(mm,counter_peds_in_exc_level(mm))=y_ped_current(i);
% % %                 end
% % %             end
            if excitement_ped_current(i) <= 0
                counter_peds_in_exc_level(1)=counter_peds_in_exc_level(1)+1;
                x_ped_exc_level_1(counter_peds_in_exc_level(1))=x_ped_current(i);
                y_ped_exc_level_1(counter_peds_in_exc_level(1))=y_ped_current(i);
            elseif excitement_ped_current(i) <= Ped_excitement_max_av  &&  excitement_ped_current(i) > 0
                counter_peds_in_exc_level(2)=counter_peds_in_exc_level(2)+1;
                x_ped_exc_level_2(counter_peds_in_exc_level(2))=x_ped_current(i);
                y_ped_exc_level_2(counter_peds_in_exc_level(2))=y_ped_current(i);
            elseif excitement_ped_current(i) > Ped_excitement_max_av 
                counter_peds_in_exc_level(3)=counter_peds_in_exc_level(3)+1;
                x_ped_exc_level_3(counter_peds_in_exc_level(3))=x_ped_current(i);
                y_ped_exc_level_3(counter_peds_in_exc_level(3))=y_ped_current(i);
            end
        end
        % Plot peds in each of the excitement levels:
% % %         for mm=1:N_of_excitement_levels
% % %             if counter_peds_in_exc_level(mm) > 0
% % %         		set(p4plot1(mm),'XData',x_ped_exc_level(mm,:),'YData',y_ped_exc_level(mm,:),...
% % %                     'MarkerFaceColor',face_colors(mm,:))
% % %             else
% % %                 set(p4plot1(mm),'XData',[],'YData',[],...
% % %                     'MarkerFaceColor',face_colors(mm,:))
% % %             end
% % %         end
        if counter_peds_in_exc_level(1) > 0
    		set(p4plot1(1),'XData',x_ped_exc_level_1,'YData',y_ped_exc_level_1,...
                'MarkerFaceColor',face_color_exc_below_1)
        else
            set(p4plot1(1),'XData',[],'YData',[],...
                'MarkerFaceColor',face_color_exc_below_1)
        end
        if counter_peds_in_exc_level(2) > 0
    		set(p4plot1(2),'XData',x_ped_exc_level_2,'YData',y_ped_exc_level_2,...
                'MarkerFaceColor',face_color_exc_below_max)
        else
            set(p4plot1(2),'XData',[],'YData',[],...
                'MarkerFaceColor',face_color_exc_below_max)
        end
        if counter_peds_in_exc_level(3) > 0
    		set(p4plot1(3),'XData',x_ped_exc_level_3,'YData',y_ped_exc_level_3,...
                'MarkerFaceColor',face_color_exc_above_max)
        else
            set(p4plot1(3),'XData',[],'YData',[],...
                'MarkerFaceColor',face_color_exc_above_max)
        end
        drawnow
        % Plots the peds' "noses":
        set(p4plot2,'XData',x_ped_current+ex_ped_current.*ped_radius,'YData',y_ped_current+ey_ped_current.*ped_radius)
		drawnow
        M(:,frame) = getframe;
        frame=frame+1;

% % %         % Now plot the excitement factor in the form of the number of peds in each of the excitement levels
% % %         %  versus the numerical value of the middle of the given excitement level:
% % %         figure(fig_with_excitement_n);
% % %         hold off
% % %         plot((excitement_levels(1)+delta_excitement/2)*ones(1,counter_peds_in_exc_level(1)),...
% % %              [1:counter_peds_in_exc_level(1)],'square',...
% % %              'MarkerSize',20,'MarkerEdgeColor',[0.9 0.9 1],'MarkerFaceColor',[0.95 0.95 1]);
% % %         axis([0 2.5*max(excitement_ped) 0 N_peds])
% % %         xlabel('Excitement factor')
% % %         ylabel('Number of peds with given excitement factor')
% % %         set(gca,'Fontsize',16)
% % %         hold on
% % %         for mm=2:N_of_excitement_levels
% % %             plot((excitement_levels(mm)+delta_excitement/2)*ones(1,counter_peds_in_exc_level(mm)),...
% % %              [1:counter_peds_in_exc_level(mm)],'square',...
% % %              'MarkerSize',20,'MarkerEdgeColor',face_colors(mm,:),'MarkerFaceColor',face_colors(mm,:));
% % %         end
        
        if strcmp(movie_speed,'fast') == 1
            pause(0.09*dT_save)
        elseif strcmp(movie_speed,'normal') == 1
            pause(0.9*dT_save)
        end
        time_elapsed=time_elapsed+dT_save;
        if min(x_ped_current) > B_wall_xmaxMax+wall_thickness
            exit_time=time_elapsed
            disp('All peds have exited !')
            
        end
    end
    % pause
end

movie2avi(M,strcat(aux_loadedhead, 'movie'),'FPS',29);

number_of_exiting_peds=sum(x_ped_current>B_wall_xmaxMax+wall_thickness)
theta
 
cd ..