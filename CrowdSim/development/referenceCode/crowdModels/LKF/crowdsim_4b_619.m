% This program models behavior of pedestrians in a crowd.
% Started  around May 1, 2003
% Version 4b

% This version models equations of a pedestrian motion, which take into
% account only "non-physical" and physical forces, as described by
% D. Helbing et al. 

% Limitations of Version 1:
%  1. Does NOT model various stages of "excitement" of pedestrians.
%  2. Supports crowd motion in a rectangular domain only, with possible
%     doors in the walls and circular "columns" (impenetrable, of course)
%     located at arbitrary points inside the domain.
%  3. Attarctions (see below for description) are 
%     (a) modelled as points, i.e. they do not have extended dimensions;
%     (b) stationary, i.e. they do not move;
%     (c) each attraction attracts all pedestrians with the same "strength";
%     (d) and their "strength" does not change in time.
%  4. Such parameters of the pedestrians as:
%     preferred speed
%     are assumed not to change in time.

% The equations modelled are:
%
%   d(v_i)/dt = F_i + sum_{nearest j}F_ij + F_ib + F_ia + ...
%               sum_{nearest j}(N_ij + T_ij) + (N_ib+T_ib) + noise.
% 
% Here i     =  index of the pedestrian;
%      v_i   =  2-dimensional velocity of the i-th pedestrian;
%      j     =  indices of the nearest neighbors to the i-th pedestrian, 
%               with the number of nearest neighbors being a parameter 
%               of this simulator;
%      F_i   =  (v0_i-v_i)/tau_i  describes the tendency of the
%               pedestrian to maintain his "preferred" velocity  v0_i
%               along a and  tau_i  is his "reaction" time;
%      F_ij  -  describes the tendency of the pedestrian to keep a certain
%               distance from his nearest neighbors;
%      F_ib  -  describes similar tendency of keeping away from boundaries
%               such as walls etc.;
%      F_ia  -  describes attraction of the pedestrian to a certain place
%               or event;
%      noise -  describes the tendency of a pedestrian to deviate slightly
%               from his "ideal" path;
%      The "forces" listed above are of "non-physical" nature.
%      The forces listed below are real physical forces:
%      N_ij,
%      T_ij  -  normal and tangential forces arising on contact of two bodies;
%      N_ib,
%      T_ib  -  similar forces arising on contact between the body and the
%               boundary.
% The above forces and parameters are described in more detail inside the
% code.

%%% global aux_size1 aux_size2 aux_size3 aux_size4   % this is for debugging purposes only

global EPS EPSCOR

EPS=1000*eps;                                   % redefine a small number, which is sufficiently small but is
                                                %  still large enough so that dividing by it won't cause trouble;
                                                %  NOTE, however, that the original "eps" also occurs in this code,
                                                %  since in some cases it's incorrect to replace it with EPS.
EPSCOR=1/(1+EPS);

time_the_code=0;                                % decide whether one wants to time various parts of the code either
                                                %  to gather statistics or for speed improvement purposes;
                                                %  1 =>  time the code,
                                                %  0 =>  do not time it.

date_name=input('enter todays date (format  y/m/d) for file identification;  date_name=');

Jmin=2;
Jmax=4;
for J=Jmin:Jmax                                 % allow for the possibility to run the code several times
                                                %  while changing its parameters
randn('state',0+floor((J-1)/3));                               % prepare the state of random generators
rand('state',0+floor((J-1)/3)); 

count=0;


%
% -------------------  Set up the environment of the crowd  --------------
% 

N_peds=2;                                       % number of pedestrians inside the room at t=0
ped_indices=[1:N_peds];                         % auxiliary array used during the reshuffling procedure


% 
% Boundaries of the room:
B_wall_xmin=0;
%B_wall_xmax=12*sqrt(1);                         % position of left and right outer
rightWallParams = B_wall_Parameters;
B_wall_xmaxMin=rightWallParams(1);
B_wall_xmaxMax=rightWallParams(2);
Door_right_lowLow = rightWallParams(3);
Door_right_low = rightWallParams(4);
Door_right_up = rightWallParams(5);
Door_right_lowHigh = rightWallParams(6);


                                                %  boundaries (walls) [m]
                                                
B_wall_ymin=rightWallParams(7);
B_wall_ymax=rightWallParams(8);                         % position of "lower" and "upper" outer
                                                %  boundaries (walls) [m]
                                                
%being read in from a file now                                                
%for i=1:B_wall_ymax
%    B_wall_xmax(i) = 12*sqrt(1);
%end;
%
                                                
% Outer boundaries:
% NOTE that pedestrians outside the room move freely (i.e. w/o forces);
%      and as soon as they reach the outer boundaries, they "stick" to
%      them and move no further. This is done to faciliate visual perception
%      of the results of the program.
B_outerwall_left=B_wall_xmin-2;
B_outerwall_right=B_wall_xmaxMax+2;
B_outerwall_lower=B_wall_ymin-2;
B_outerwall_upper=B_wall_ymax+2;

%            
% Doors:
N_doors_left=0;
N_doors_right=1;
N_doors_lower=0;
N_doors_upper=0;                                % # of doors in each of the walls
% N_doors=[N_doors_left,N_doors_right,N_doors_lower,N_doors_upper];
%                                               % combine info about # of doors into
%                                               %  one array
Door_left_low=B_wall_ymin+[];
Door_left_up=B_wall_ymin+[];                    % the k-th door on the left wall is between
                                                %  Door_left_low(k)  and
                                                %  Door_left_up(k)  [m]
%Door_right_low=B_wall_ymin+[7.0];
%Door_right_up=B_wall_ymin+[8.0];                 % the k-th door on the right wall is between
                                                %  Door_right_low(k)  and
                                                %  Door_right_up(k)  [m]
Door_lower_left=B_wall_xmin+[];
Door_lower_right=B_wall_xmin+[];                % the k-th door on the lower wall is between
                                                %  Door_lower_left(k)  and
                                                %  Door_lower_right(k)  [m]
Door_upper_left=B_wall_xmin+[];
Door_upper_right=B_wall_xmin+[];                % the k-th door on the upper wall is between
                                                %  Door_upper_left(k)  and
                                                %  Door_upper_right(k)  [m]
% Make sure the announced ## of doors on each wall coincide with the corresponding
%  ## which are actually set up above:
if size(Door_left_low,2) ~= N_doors_left | size(Door_left_up,2) ~= N_doors_left
    disp('   Doors on the LEFT wall are set up incorrectly ! ')
    pause
end
if size(Door_right_low,2) ~= N_doors_right | size(Door_right_up,2) ~= N_doors_right
    disp('   Doors on the RIGHT wall are set up incorrectly ! ')
    pause
end
if size(Door_lower_left,2) ~= N_doors_lower | size(Door_lower_right,2) ~= N_doors_lower
    disp('   Doors on the LOWER wall are set up incorrectly ! ')
    pause
end
if size(Door_upper_left,2) ~= N_doors_upper | size(Door_upper_right,2) ~= N_doors_upper
    disp('   Doors on the UPPER wall are set up incorrectly ! ')
    pause
end

wall_thickness=0.25;                            % thickness of walls  [m]; because of this thickness being nonzero,
                                                %  pedestrians remain between door posts for a short period after
                                                %  they have already exited the room, and thus they still block
                                                %  the door through which they are exiting
                                                
%
% Circular columns inside the room:
Column_c_x=[0.2];                             % x-coordinates of the columns' centers  [m]
Column_c_y=[0.2];                              % y-coordinates of the columns' centers  [m]
Column_radius=[0.1];                        % radii of the columns  [m]
N_columns=length(Column_c_x);                   % # of columns

% 
% Events that attract pedestrians:
N_attractions=N_doors_left+N_doors_right+N_doors_lower+N_doors_upper;
                                                % assume that pedestrians are only attracted to doors
                                                %  as exits 
Attraction_x(1:N_doors_left)=B_wall_xmin*ones(1,N_doors_left);
Attraction_y(1:N_doors_left)=(Door_left_low+Door_left_up)/2;
Attraction_x(N_doors_left+1:N_doors_left+N_doors_right)=B_wall_xmaxMax*ones(1,N_doors_right);
Attraction_y(N_doors_left+1:N_doors_left+N_doors_right)=(Door_right_low+Door_right_up)/2;
Attraction_x(N_doors_left+N_doors_right+1:N_doors_left+N_doors_right+N_doors_lower)=...
             (Door_lower_left+Door_lower_right)/2;
Attraction_y(N_doors_left+N_doors_right+1:N_doors_left+N_doors_right+N_doors_lower)=...
             B_wall_ymin*ones(1,N_doors_lower);
Attraction_x(N_doors_left+N_doors_right+N_doors_lower+1:N_attractions)=...
             (Door_upper_left+Door_upper_right)/2;
Attraction_y(N_doors_left+N_doors_right+N_doors_lower+1:N_attractions)=...
             B_wall_ymax*ones(1,N_doors_upper);
                                                % x- and y-coordinates of the attractions  [m]
%
% I assume that the attractive force acts on the pedestrian with intensity which very slowly
%  decays with the distance from the attraction to the pedestrian:
%  F_ia=w(e_i,r_ia)*Attraction_strength*exp(-|r_ia|/ped_attraction_dist)
%  where
%  e_i  is the direction of motion of pedestrian i,
%  r_ia=r_a-r_i is the radius-vector between the pedestrian and the attraction, 
%  w  is the weight describing change of perception by the pedestrian with his angle of view
%   (its form is set up in a separate subroutine),  and
%  ped_attraction_dist   is the typical distance at which the attraction force decays 
%   by  e  times;  the value for this distance is set up below, in the block where all
%   parameters of pedestrians are set up.


%
% -----------------------------   Define parameters of pedestrians   -------------------------------
% ---  Among those parameters, there are those which do NOT change as the crowd evolves, such as   ---
% ---  their radii, mass, and elasticity, and others, which DO evolve, such as the preferred   ---
% ---  speed, reaction time, independence factor, and intensity of fluctuations.               ---

%
% First, set up the average parameters of all pedestrians and deviations from those average values:

% Non-evolving parameters:   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

Ped_radius_av=0.35; %r_i                             % average "radius" of pedestrians  [m]
Ped_radius_dev=0.05;                            % deviation of the pedestrians' radii from the average  [m]
ped_squeezability=0.2;                          % this is a nondimensional factor showing by how much (relative to 1)
                                                %  any pedestrian can be squeezed in a collision
Ped_mass_av=80;  %m                                 % average mass of a pedestrian  [kg]
Ped_mass_dev=5;                                 % deviation of the pedestrians' masses from the average  [kg]
% NOTATIONAL NOTE: I use names starting with capital "Ped" to denote parameters which will be later assigned
%                  to individual pedestrians, at which point their names will begin with the lower-case "ped".
%                  If a quantity is not re-assigned later on an individual basis, then its name begins
%                  with the lower-case "ped" right from the start.
ped_elasticity=24*10^3;                        % elastic coefficient (k) of the pedestrians (pedestrians
                                                %  are modelled as springs when they come in contact 
                                                %  with one another)  [kg/s^2]
ped_withindoor_elasticity=2*ped_elasticity;   % make the elasticity within door posts higher than normal, because the
                                                %  reshuffling procedure doesn't apply when a ped is within door posts,
                                                %  and we still want him not to penetrate those posts;
                                                %  ALSO this can be used a fitting parameter   [kg/s^2]
ped_friction=1.0;                               % coefficient of lateral (tangential) friction between two 
                                                %  pedestrians in contact with each other or boundaries
                                                %  (this is a _nondimensional_ proportionality coefficient 
                                                %   between the friction force and the normal reaction force)
Ncontact_max=4*Ped_mass_av*9.8;                 % this is the maximum reaction force of a body when it has been squashed
                                                %  beyond what is allowed by the parameter  ped_squeezability;
                                                %  it is used when I calculate   dt_of_this_reshuffling_step;
                                                %  the coefficient in front of the r.h.s. is rather arbitrary 
                                                %  (it says how many times this force is greater than the ped's weight),
                                                %  and this coefficient is a free parameter of the program
                                                %  (but hopefully it should not have much effect on its outcome)
                                             
                                                
ped_b_impact_inelasticity=0;                  
   % CAN'T  HAVE  IT  ~= 0   W/O  FURTHER EFFORT (see VERY IMPORTANT NOTE below) !!!!
                                                % this nondimensional parameter characterizes what percentage of
                                                %  tangential velocity of a pedestrian is conserved upon his impact
                                                %  with a wall or column; 
                                                %  if it = 1  =>  pedestrian loses all of his tangential velocity,
                                                %  if it = 0  =>  pedestrian preserves all of his tangential velocity;
                          %   !!!!!!! ---->>    % VERY IMPORTANT NOTE (!!!!!!!!!!!!!):
                                                %  For the moment, I have to set this parameter to 0.
                                                %   The reason is that otherwise, the following happens:
                                                %   A ped gets squashed against a wall by the crowd, and looses
                                                %    ped_b_impact_inelasticity*100%  of his tangential velocity to the wall.
                                                %    At the very next step, he, most likely, gets squashed again, and
                                                %    again looses the same percentage of his velocity. Thus, after a
                                                %    very short time, he looses almost all of his tangential velocity
                                                %    and gets turned towards the wall by the pressure of the crowd.
                                                %   To prevent this from happening when  ped_b_impact_inelasticity ~=0,
                                                %    one needs to introduce the memory that he has just been squashed against
                                                %    the wall and in the next X seconds, if he is squashed again, he won't
                                                %    loose any part of his tangential velocity. 
                                                %    As of 5/9/03, this has not been implemented.
                                                %  OTHER NOTES:
                                                %  1. The normal velocity at the boundary is always 0.
                                                %  2. In a collision of two pedestrians, their impact is always
                                                %     treated as absolutely inelastic, meaning that their velocities
                                                %     are set to be the same immediately after the collision.
                                                
a_centrifugal_comfortable=0.6;                  % this is the centrifugal acceleration,  [m/s^2],  that a ped can 
                                                %  comfortably have (as opposed to "perceive" as above) when steering 
                                                %  towards a sufficiently far-away attraction or away from an obstacle
                                                %  (column, corner, or flat wall). 
                                                %  
% Next block of coefficients characterizes the "social" repulsion force F_ij between two pedestrians:
%  F_ij=w_far(e_i,r_ij)*ped_rep_close_max*exp(-(|r_mod_ij|-d_ij)/ped_rep_close_dist)*n_mod_ij,
%  where:
%  e_i  is the direction of motion of pedestrian i,
%  r_ij=r_j-r_i  is the radius-vector between the pedestrians,
%  w  is the weight describing change of perception by the pedestrian with his angle of view
%   (its form is set up in a separate subroutine),
%  r_mod_ij=r_ij+(v_j-v_i)*tau_i  is the distance between the pedestrians which pedestrian i
%   anticipates to occur after their next step,
%  d_ij=ped_radius_i+ped_radius_j,
%  n_mod_ij=r_mod_ij/|r_mod_ij|,
%  and the other parameters are defined below.
% NOTE 1: Of the coefficients in this block, we assume that all of them except the repulsion
%         strength and distance do NOT evolve as the crowd evolves. Thus we define all but those
%         parameters here, and the aforementioned 2 parameters, later, when we define other evolving parameters.
% NOTE 2: The force of repulsion from boundaries is assumed to have the same form, but possibly
%         different parameter values than the repulsion force among pedestrians. The corresponding
%         values are denoted by "ped_b_", as opposed to "ped_", which pertains to pedestrians only.

ped_rep_close_percep_angle_min=pi/2;            % one-sided angle in which a given pedestrian has 
                                                %  100% perception of other pedestrians  [rad];
                                                % this is also a min angle within a ped has a 100%
                                                %  perception of non-moving obstacles and attractions;
                                                %  a ped has a memory of what was a the location where he was
                                                %  heading, and mathematically this is modeled as an increase
                                                %  of his perception angle, with the max perception angle being  pi
cos_ped_rep_close_percep_angle_min=cos(ped_rep_close_percep_angle_min);
                                                % this quantity is used in the code at every step multiple times
ped_rep_close_percep_back=0.3;                 % ratio of perceptions of other pedestrians "directly behind" 
                                                %  and "directly in front" by a given pedestrian
ped_b_rep_close_percep_back_min=ped_rep_close_percep_back;
                                                % ratio of perceptions of boundaries (walls, columns) "directly behind"
                                                %  and "directly in front" by a given pedestrian;
                                                %  again, as for  ped_rep_close_percep_angle_min  above,
                                                %  this represents the minimum backward perception of a
                                                %  non-moving obstacle, and it evolves in strict connection with
                                                %  the evolutions of  ped_{attraction,column,walls}_percep_angle
% The next two variables characterize the angle in which a ped still sees an attraction or an obstacle, and
%  according to this he may rotate. This angle is greater than  ped_rep_close_percep_angle_min  because even
%  if a ped has 90% perception as opposed to 100% one, he can still see an object.
ped_seeing_angle=1.2*ped_rep_close_percep_angle_min;
                                                %  [rad]
cos_ped_seeing_angle=cos(ped_seeing_angle);     % this quantity is used many time in each time step, so it is best
                                                %  to calculate the cosine of this angle at the outset

% These two time variables characterize the ped's memory of the location of the attraction he has been going to
%  or of the obstacle he has recently avoided. In terms of this code, this memory is modeled as the increase
%  the perception angle in which a ped sees the attraction or obstacle in question. 
% % % Ped_tau_loc_memory_av=5;                       % the average (over all peds) time  [s],  for which a ped remembers
% % %                                                 %  (or gains memory of) the location of an attraction or obstacle
% % % Ped_tau_loc_memory_dev=1;                       % max deviation of the above time among pedestrians  [s]
Ped_tau_loc_memory_learn_av=2;                  % the average (over all peds) time  [s],  for which a ped gains memory 
                                                %  (i.e. learns about) the location of an attraction or obstacle
Ped_tau_loc_memory_learn_dev=0.2;               % max deviation of the above time among pedestrians  [s]
Ped_tau_loc_memory_forget_av=10;                 % the average (over all peds) time  [s],  for which a ped forgets about the
                                                %  location of an attraction or obstacle, if he hasn't seen it recently
Ped_tau_loc_memory_forget_dev=0.6;                % max deviation of the above time among pedestrians  [s]
%
% Perception by a given pedestrian of other pedestrians, when the former decides with which velocity he wants to move,
%  has a form analoguous to the perception function that he has of the boundaries. However, only the pereception angle
%  in this case does not evolve, while the perception at his back does evolve. Indeed, if the crowd is very dense,
%  the pedestrian hears what people behind him do, and may react correspondingly.
ped_collective_percep_angle=pi/4;               % one-sided angle in which a given pedestrian has 
                                                %  100%-perception of other pedestrians  [rad]
Ped_collective_percep_back=0.2;                 % ratio of perception "directly behind" and "directly
                                                %  in front of the pedestrian
% ped_collective_percep_dist  is defined below among  evolving  parameters.


%
% Evolving parameters:   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
%  (they will be updated as the density of the crowd around a given pedestrian changes) 
%
panic_parameter=1*rem(J-1,3);
%
%
%
Ped_speed0_max_av=1.5*(1+panic_parameter);                          % average preferred speed of pedestrians in the
                                                %  absence of other pedestrians or obstacles  [m/s]
Ped_speed0_dev=0.1*Ped_speed0_max_av;           % deviation of the preferred speed among pedestrians  [m/s]
Ped_speed0_min_av=Ped_speed0_max_av;            % average preferred speed of an isolated pedestrian who is approaching
                                                %  an exit (in this case he tends to slow down)  [m/s]
ped_speedcorrection_dist=2;                     % characteristic distance  [m]  over which the ped decides to slow down
                                                %  when he approaches an exit or a boundary
%
Ped_tau_reaction_av_max=0.5;
Ped_tau_reaction_av_min=0.5;                    % average reaction time, in which the pedestrian tends
                                                %  to restore his velocity to the preferred value  [s];
                                                %  max  and  min  refer to the values attained at light and high densities
                                                %  of the crowd, respectively (or, alternatively, far from and close to
                                                %  the exit)
Ped_tau_reaction_dev=0.05;                       % deviation of the reaction time among pedestrians  [s]
Ped_acceleration_fluct=(0.05+0*floor((J-1)/2))*Ped_speed0_max_av/Ped_tau_reaction_av_max^2;
                                                % fluctuation source (described by the noise term in the equation 
                                                %  of motion) of the pedestrian's acceleration  [m/s^3]
                                                %  (yes, I mean  m/s^3, not m/s^2 - see how the noise is computed)
% The next 3 parameters characterize the weight with which the pedestrian prefers to keep his own direction of
%  motion as opposed to following others. The minimum value is possesed by a pedestrian when he is far away
%  from his target. His independence increases to a max value as he approaches the target.
Ped_independence_min_av=0.9999;                    % average value of the independence parameter (must be in [0,1])
Ped_independence_min_dev=0;                   % deviation of the above parameter from the average value
Ped_independence_max=0.999999;                      % the max value of the independence parameter (assumed to be the
                                                %  same for all pedestrians)
% The next block defines the parameters which define the peds' ability to give up on his persistence to get
%  towards his strongest attraction, and start looking for other attractions.
Ped_accumulated_excitement_threshold_av=15;     % when the integral over time of the ped's excitement factor reaches
                                                %  this threshold value (averaged among peds), the ped will give up
                                                %  his preference to go to a specific attraction and start looking
                                                %  for a new one;
                                                %  the dimension of the above quantity is [s] (see the definition)
Ped_accumulated_excitement_threshold_dev=0;     % the deviation of the above quantity among peds
Ped_P_to_give_up_av=0;                          % this is the average (among all peds) probability that the
                                                %  ped who has reached his  Ped_accumulated_excitement_threshold
                                                %  will give up on his old attraction and start looking for a new one
Ped_P_to_give_up_dev=0;                         % the deviation of the above quantity among peds
%
Ped_excitement_max_av=1/(1+panic_parameter);    % average value of the maximum (or about that) value of the 
                                                %  excitement that can possibly be reached by a pedestrian
Ped_excitement_max_dev=0;                     % deviation of the above quantity from one pedestrian to another
%
% This block defines evolving parameters of the "social" repulsion force among pedestrians and between them and walls:
Ped_rep_close_max_f2b_min=0.3*0.9*(1+Ped_excitement_max_av)/(Ped_tau_reaction_av_max*(1-ped_rep_close_percep_back))*Ped_mass_av;
Ped_rep_close_max_f2b_max=4*Ped_rep_close_max_f2b_min;
                                                % the force on a ped facing the other ped's back  [N]
                                                %  max  and  min  refer to the values attained at light and high densities
                                                %  of the crowd, respectively (or, alternatively, far from and close to
                                                %  the exit)
                                                % the coefficient 0.9=1.34*0.67  and comes from matching the force
                                                %  with the velocity-vs-density curve reported in
                                                %  [U. Weidmann, "Transporttechnik der Fussganger"]
Ped_rep_close_max_f2f_min=1200*(1+Ped_excitement_max_av)/2;
Ped_rep_close_max_f2f_max=1.5*Ped_rep_close_max_f2f_min;
                                                % similar coefficients face-to-face
Ped_b_rep_close_max_b2w_min=1.2*Ped_rep_close_max_f2b_min;
Ped_b_rep_close_max_b2w_max=1.2*Ped_rep_close_max_f2b_max;
Ped_b_rep_close_max_f2w_min=1.2*Ped_rep_close_max_f2f_min;
Ped_b_rep_close_max_f2w_max=1.2*Ped_rep_close_max_f2f_max;  
                                                % max and min values of "social" repulsive forces among pedestrians
                                                %  and between pedestrians and walls  [N]
                                                %  (f2w = face to wall;  b2w = back to wall)
%
Ped_rep_close_dist=0.6;
Ped_b_rep_close_dist=1.1*Ped_rep_close_dist;      % distances over which corresponding "social" forces reduce by  e  [m];
                                                %  the characteristic distances of repulsion among peds can very between
                                                %  the above max and min values depending on the crowd density the ped
                                                %  sees around himself; if the crowd it dense, he may agree to reduce
                                                %  his characteristic repulsion distance from a max to a min
rep_close_exponent=1;                           % the repulsive forces have the form
                                                %   exp(-("actual distance"/"characteristic distance")^rep_close_exponent)
% 
Ped_react_far_dist_max=6;                       % max distance  [m],  at which a ped begins to change his direction 
                                                %  (w/o changing the magnitude of velocity) in order to steer towards an
                                                %  attraction or away from an obstacle (column, corner, or flat wall)
Ped_react_far_dist_min=0.4;                     % the corresponding min distance  [m]; if he is closer to a particular
                                                %  attraction or an obstacle than this min distance, his behavior is governed
                                                %  only by forces, and not steering preferences
% This block defines evolving parameters of the attraction force:
Ped_attraction_strength=Ped_mass_av*Ped_speed0_max_av/Ped_tau_reaction_av_max*ones(1,N_attractions);
                       %%%%  (1+panic_parameter)*240*ones(1,N_attractions); 
                                                % "strengths" of attractions  [N]
Ped_attraction_dist_av=50;                       % characteristic average distance at which pedestrians
                                                %  are attracted to attractions  [m]
Ped_attraction_dist_dev=1;                      % deviation of the above quantity from one pedestrian
                                                %  to another  [m]
% This block defines evolving parameters of the collective perception, described earlier:
Ped_collective_percep_dist_max=1;
Ped_collective_percep_dist_min=0.6;             % typical distance(s) ([m]) from i-th pedestrian to other
                                                %  pedestrians whose velocities he takes into account;
                                                %  this distance changes from its max to min value when a ped
                                                %  moves from a light crowd to a denser one

Ped_percep_maxdist=3*max(Ped_rep_close_dist/rep_close_exponent,Ped_collective_percep_dist_max);
                                                % a pedestrian doesn't take into account those
                                                %  pedestrians who are > than this distance away from him  [m]
Ped_b_percep_maxdist=Ped_react_far_dist_max;    % the max distance beyond which a pedestrain doesn't
                                                %  care about boundaries
                                                

%
% Now assign to each pedestrian his individual characteristic, based on the above collective parameters:
for i=1:N_peds
    
    % Non-evolving parameters:   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    ped_radius(i)=Ped_radius_av+(2*rand-1)*Ped_radius_dev;
                                                % radius of i-th pedestrian  [m]
    ped_area(i)=pi*(ped_radius(i))^2;           % area occupied by i-th pedestrian  [m^2]
    ped_radius_min(i)=(1-ped_squeezability)*ped_radius(i);
                                                % minimum radius to which i-th pedestrian can be
                                                %  squeezed by the surrounding crowd  [m]
    ped_mass(i)=Ped_mass_av+(2*rand-1)*Ped_mass_dev;
                                                % mass of i-th pedestrian  [kg]
                                                
    % Evolving parameters:   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    % Set up preferred speeds of pedestrians as having a Gaussian distribution with 
    %  max deviation = 3*ped_speed0_dev:
    dev_ped_speed0=10*Ped_speed0_max_av;        % initiation of the quantity named on the l.h.s.
                                                %  (see the following "while"-loop)
    while dev_ped_speed0 > 3*Ped_speed0_dev
        dev_ped_speed0=Ped_speed0_dev*randn;
    end
    ped_speed0_max(i)=Ped_speed0_max_av+dev_ped_speed0;
                                                % preferred speed of an isolated i-th pedestrian [m/s]
    ped_speed0_min(i)=Ped_speed0_min_av/Ped_speed0_max_av*ped_speed0_max(i);
                                                % the lower bound of the preferred speed of a i-th pedestrian,
                                                %  when he is approaching an exit or a crowd  [m/s]
    ped_tau_reaction(i)=Ped_tau_reaction_av_max+(2*rand-1)*Ped_tau_reaction_dev;
                                                % reaction time of i-th pedestrian  [s]
%     ped_tau_loc_memory(i)=Ped_tau_loc_memory_av+(2*rand-1)*Ped_tau_loc_memory_dev;
%                                                 % time for which i-th pedestrian has memory of the location of
%                                                 %  his strongest attraction or the obstacle he has been avoiding [s]
    ped_tau_loc_memory_learn(i)=Ped_tau_loc_memory_learn_av+(2*rand-1)*Ped_tau_loc_memory_learn_dev;
                                                % time for which i-th pedestrian gains memory of the location of
                                                %  his strongest attraction or the obstacle he has been avoiding [s]
    ped_tau_loc_memory_forget(i)=Ped_tau_loc_memory_forget_av+(2*rand-1)*Ped_tau_loc_memory_forget_dev;
                                                % time for which i-th pedestrian forgets about the location of
                                                %  his strongest attraction or the obstacle he has been avoiding [s]
    %
    ped_independence_min(i)=Ped_independence_min_av+(2*rand-1)*Ped_independence_min_dev;
                                                % minimum possible independence parameter of i-th pedestrian
    ped_independence_max(i)=Ped_independence_max; 
                                                % maximum possible independence parameter of i-th pedestrian
    ped_acceleration_fluct(i)=Ped_acceleration_fluct;
                                                % magnitude of acceleration fluctuations source for each pedestrian  [m/s^3]
    ped_rep_close_max(i)=Ped_rep_close_max_f2b_min;                           
    ped_b_rep_close_max(i)=Ped_b_rep_close_max_b2w_min;
                                                % magnitude of the max "social" repulsion forces among pedestrians
                                                %  and between pedestrians and boundaries  [N]
    ped_rep_close_max_ratio(i)=Ped_rep_close_max_f2b_max/Ped_rep_close_max_f2b_min;
    ped_b_rep_close_max_ratio(i)=Ped_b_rep_close_max_b2w_max/Ped_b_rep_close_max_b2w_min;
                                                % corresponding ratios of repulsion forces face-to-face and otherwise
    ped_rep_close_dist(i)=Ped_rep_close_dist;
    ped_b_rep_close_dist(i)=Ped_b_rep_close_dist;
                                                % corresponding distances over which the "social" repulsion forces
                                                %  are reduced by  e  [m]
    ped_react_far_dist_max(i)=Ped_react_far_dist_max;
                                                % max distance  [m]  at which  i-th ped  begins to react to an attraction
                                                %  or obstacle (column, corner, or flat wall) in order to steer either 
                                                %  towards it or away from it 
    ped_react_far_dist_min(i)=Ped_react_far_dist_min;
                                                % the corresponding min distance  [m], below which he does not rotate
                                                %  towards an attraction or away from an obstacle, but behaves based only
                                                %  on the short-range forces acting on him
    % Define initial values of the angles in which each pedestrian perceives an attraction or obstacle,
    %  and the corresponding values of backward perception:
    for k=1:N_attractions
        ped_attraction_percep_angle(i,k)=ped_rep_close_percep_angle_min;
                                                % perception angles for each attraction  [rad]
        ped_attraction_percep_back(i,k)=ped_b_rep_close_percep_back_min;
                                                % backward perception for each attraction 
    end
    for k=1:N_columns
        ped_column_percep_angle(i,k)=ped_rep_close_percep_angle_min;
                                                % perception angles for the columns  [rad]
        ped_column_percep_back(i,k)=ped_b_rep_close_percep_back_min;
                                                % backward perceptions for the columns
    end
    
    ped_left_wall_percep_angle(i)=ped_rep_close_percep_angle_min;
    ped_right_wall_percep_angle(i)=ped_rep_close_percep_angle_min;
    ped_lower_wall_percep_angle(i)=ped_rep_close_percep_angle_min;
    ped_upper_wall_percep_angle(i)=ped_rep_close_percep_angle_min;
                                                % perception angles for the walls  [rad]
    ped_left_wall_percep_back(i)=ped_b_rep_close_percep_back_min;
    ped_right_wall_percep_back(i)=ped_b_rep_close_percep_back_min;
    ped_lower_wall_percep_back(i)=ped_b_rep_close_percep_back_min;
    ped_upper_wall_percep_back(i)=ped_b_rep_close_percep_back_min;
                                                % backward perceptions for the walls
    %                                            
    ped_collective_percep_dist(i)=Ped_collective_percep_dist_max;
                                                % typical distance of perception by a pedestrian of others for deciding
                                                %  on whether he will adjust his speed to the surrounding speed  [m]
    ped_collective_percep_back(i)=Ped_collective_percep_back;
                                                % pereception in the backwards direction relative to that in the forward
                                                %  direction in the above perception case
    ped_percep_maxdist(i)=Ped_percep_maxdist;   % maximum distance at which the pedestrian still cares of what other
                                                %  pedestrians around him do  [m]
    ped_b_percep_maxdist(i)=Ped_b_percep_maxdist;
                                                % similar max distance, but with respect to the boundaries  [m]
    ped_attraction_dist(i)=Ped_attraction_dist_av+(2*rand-1)*Ped_attraction_dist_dev;
                                                % typical distance at which the i-th pedestrian
                                                %  is attracted to attractions  [m]
    ped_attraction_strength(i,:)=Ped_attraction_strength;
                                                % strength with which a pedestrian is attracted to an attraction
                                                %  when he is very close to it  [N]
    ped_attr_loc_memory(i,:)=zeros(1,N_attractions);
                                                % the array of initial values of the memory of each attraction
    ped_excitement_max(i)=Ped_excitement_max_av+(2*rand-1)*Ped_excitement_max_dev;
                                                % the maximum value of the excitement that can be possibly achieved
                                                %  by a pedestrian (if he moves AWAY from his attraction, the excitement
                                                %  can even exceed that value and turn him back)
    ped_accumulated_excitement_threshold(i)=Ped_accumulated_excitement_threshold_av+...
                                            (2*rand-1)*Ped_accumulated_excitement_threshold_dev;
                                                % threshold value of the accumulated excitement  [s]  above which a ped may
                                                %  give up on his strongest attraction and start looking for a new one
                                                %  (the initial accumulated excitement itself is set up ~100 lines below)
    ped_P_to_give_up(i)=Ped_P_to_give_up_av+(2*rand-1)*Ped_P_to_give_up_dev;
                                                % the probability of the above act of giving up
end

for i=1:N_peds
    for j=1:N_peds
        d_ij(i,j)=ped_radius(i)+ped_radius(j);  % distances between i-th and j-th pedestrians
                                                %  when they have just come into contact,
                                                %  w/o squeezing each other  [m]
        d_min_ij(i,j)=ped_radius_min(i)+ped_radius_min(j);
                                                % minimum possible distance between i-th and
                                                %  j-th pedestrians  [m]
    end
    for k=1:N_columns
        pedCol_radius(i,k)=ped_radius(i)+Column_radius(k);
        pedCol_radius_min(i,k)=ped_radius_min(i)+Column_radius(k);
    end
end
for i=1:N_peds
    d_ij(i,i)=0;
    d_min_ij(i,i)=0;
end                                             % these settings are for convenience later on

ped_squeezable_length=ped_radius-ped_radius_min;
                                                % max length by which a pedestrian can be squeezed
                                                %  towards a wall  [m]
pedCol_squeezable_length=pedCol_radius-pedCol_radius_min;
                                                % same thing as immediately above, but it has the
                                                %  same array dimension as  r_ib_column,
                                                %  i.e. N_peds x N_columns  [m]
squeezable_length_ij=d_ij-d_min_ij;             % max length by which two pedestrians can be 
                                                %  squeezed towards each other  [m]
                                                
% % % squeezable_length_ij_inv=1./squeezable_length_ij;
% % % squeezable_length_ij_sq=squeezable_length_ij.^2;
% % % ped_squeezable_length_inv=1./ped_squeezable_length;
% % % ped_squeezable_length_sq=ped_squeezable_length.^2;
% % % pedCol_squeezable_length_inv=1./pedCol_squeezable_length;
% % % pedCol_squeezable_length_sq=pedCol_squeezable_length.^2;
% % %                                                 % quantities used at each step of the computation,
% % %                                                 %  so I define them once and for the rest of the code

%
% -----------------  Set up initial positions and velocities of the pedestrians ---------------
%

for i=1:N_peds
    y_ped(i)=B_wall_ymin+ped_radius(i)+(B_wall_ymax-2*ped_radius(i)-B_wall_ymin)*rand;
    x_ped(i)=B_wall_xmin+ped_radius(i)+(B_wall_xmaxMin-2*ped_radius(i)-B_wall_xmin)*rand;
                                                 % initial x- and y-coordinates of i-th pedestrian  [m]
    % Verify that no two pedestrians are "on top" of each other or inside the columns:
    counter_notontop=0;
    if i == 1
        while min(sqrt((Column_c_x-x_ped).^2+(Column_c_y-y_ped).^2) - (Column_radius+ped_radius(1))) < 0
            y_ped(i)=B_wall_ymin+ped_radius(i)+(B_wall_ymax-2*ped_radius(i)-B_wall_ymin)*rand;            
            x_ped(i)=B_wall_xmin+ped_radius(i)+(B_wall_xmaxMin-2*ped_radius(i)-B_wall_xmin)*rand;
            counter_notontop=counter_notontop+1;
            if counter_notontop > 1000
                warning('  Seems to be no space to put the 1st pedestrian outside of columns !')
                pause
            end
        end
    else                                         % i.e. for i >= 2
        while min(sqrt((Column_c_x-x_ped(i)).^2+(Column_c_y-y_ped(i)).^2) - (Column_radius+ped_radius(i))) < 0 ...
              | min(sqrt((x_ped(1:i-1)-x_ped(i)).^2+(y_ped(1:i-1)-y_ped(i)).^2) - d_ij(i,1:i-1)) < 0
            y_ped(i)=B_wall_ymin+ped_radius(i)+(B_wall_ymax-2*ped_radius(i)-B_wall_ymin)*rand;
            x_ped(i)=B_wall_xmin+ped_radius(i)+(B_wall_xmaxMin-2*ped_radius(i)-B_wall_xmin)*rand;
            counter_notontop=counter_notontop+1;
            if counter_notontop > 1000
                sprintf('  Seems to be no space to put the %i-th pedestrian outside of columns ',i)
                disp('     and not on top of other pedestrians !')
                pause
            end
        end
    end
    
%     
%     pause
%     [x_ped' y_ped']
%     i
%     plot(x_ped,y_ped,'o')
%     
    
    vx_ped(i)=0;
    vy_ped(i)=0;
    while (vx_ped(i))^2+(vy_ped(i))^2 < EPS
        vx_ped(i)=0.1*(rand-0.5);
        vy_ped(i)=0.1*(rand-0.5);               % initial x- and y-velocities of i-th pedestrian  [m/s]
                                                %  (they have to be non-zero in order to have a well-
                                                %   defined direction of motion for each pedestrian)
    end
end

% Set up the initial values of the vectors of directions of motions of all peds.
speed_ped=sqrt(vx_ped.^2+vy_ped.^2)+0.1*eps;
ex_ped=vx_ped./speed_ped;
ey_ped=vy_ped./speed_ped;
if max(abs(ex_ped.^2+ey_ped.^2-1)) > 10^(-13)   % make sure all direction vectors are initially of unit length;
                                                %  stop the program if this is not so
    [smth_wrong_with_this_ex,i_in_trouble]=max(abs(ex_ped.^2+ey_ped.^2-1))
    Stop_____Initial_direction_vector_has_nonunit_length
end

Vx_pref_prevstep=zeros(1,N_peds);
Vy_pref_prevstep=zeros(1,N_peds);               % initiation of the quantities on the l.h.s.  [m/s];
                                                %  these quantities are used in the code to compute an equivalent
                                                %  of the time derivative of V{x,y}_pref
                                                
                                                
% Set up initial values of the independence parameter, ped_speed0, excitement factor, accumulated excitement, 
%  the auxiliary vector related to the crowd density, the vector containing info about acts of giving up, 
%  the indices of the strongest attraction for each pedestrian, the indices characterizing whether the ped is evolving or not
%  (he doesn't evolve if his dt_after_reshuffling=0, as explained near the initialization of the arry  dt_backlog), 
%  and the noise sources, 
%  for all pedestrians:
ped_independence=ped_independence_min;
ped_speed0=ped_speed0_max;                      % [m/s]
excitement_ped=zeros(1,N_peds);
ped_accumulated_excitement=zeros(1,N_peds);     % [s]  (see above the definition of Ped_accumulated_excitement_threshold_av)
aux_crowddensity=zeros(1,N_peds);               % this vector is updated at the end of the code, after the peds' coordinates
                                                %  and velocities have been updated
ped_gave_up=zeros(1,N_peds);
strongest_attraction_index=logical(zeros(N_peds,1));
                                                % array of indices of strongest attractions for each pedestrian
aux_index_ped_advancing=logical(ones(1,N_peds)); % all peds will be advancing (i.e. evolving by means other than
                                                %   reshuffling) at the first step of the calculation
noise_vx=zeros(1,N_peds);
noise_vy=zeros(1,N_peds);                       % [m/s^2]
delta_ped_rep_close_max=zeros(1,N_peds);





%
% -----------------  Begin to integrate equations of motion  -----------------------------------
% ----------------------------------------------------------------------------------------------
%

T_sim=100;                                       % max physical time until the model is simulated,
                                                %  in units of the model  [s]
T_computed=0;                                   % initial value for the time during which the computation
                                                %  has been going;
                                                %  since we don't have a fixed step size, we cannot determine
                                                %  upfront how many steps in time we need
dT_save=0.3333;                                 % time interval over which I save (record) the computed data  [s]
T_saved=0;                                      % initial value of the time at which the data is saved into an array  [s]
                                                %  (this quantity is updated at every instance of data saving)
N_parts_of_calcs=10;                            % number of parts into which the entire calculations are
                                                %  divided in order not to overflow the Matlab memory 
T_onepart=T_sim/N_parts_of_calcs;               % duration of one part of calculations in model's time  [s]
dT_display_progress=5;                          % the state of the simulations is reported on the screen every
                                                %  dT_display_progress  seconds of T_computed  [s]
n_displayed_part=0;                             % initial value for the counter of instances when the progress of the
                                                %  simulations has been displayed
n_computed_part=1;                              % initial value for the counter, which counts which part of the
                                                %  calculations is now being computed
                                                %  (it is used below when determining the step size in time in such
                                                %   a way that each part of the calculations contains an integer
                                                %   number of such steps)
n_saved=0;                                      % initial value for the counter which determines when the pedestrian
                                                %  coordinates need to be saved (i.e., recorded) into a file

dt_backlog=zeros(1,N_peds);                     % this is the array of "backlog" of time steps for each pedestrian
                                                % The time step of each ped is computed as follows:
                                                %  1) dt  is computed from considerations that a ped should travel less
                                                %     than part of his squeezable length, given his current velocity;
                                                %  2) delta_t   is computed for the reshuffling process
                                                %     (details of this are explained at the time of that computation);
                                                %  3) peds who weren't reshuffled, advance by  dt;
                                                %     peds who were reshuffled, have to spend their respective  delta_t's
                                                %     on reshuffling, and during that time, they cannot advance at all.
                                                %     These delta_t's are likely to be greater than  dt, in which case
                                                %     a reshuffling ped has to spend  (delta_t-dt)  still reshuffling,
                                                %     during the time frame of the next time step   dt. And so on.
                                                %     Thus, I keep track of those "leftover delta_t's" in the array
                                                %      dt_backlog.

save(['crowdsim_data\' int2str(date_name), '_J' int2str(J) '_SimulationParameters'], 'B*wall*', 'N_*', 'Door_*',...
     'wall_thickness', 'Column*', 'Attraction*', 'Ped_*', 'ped_*', 'a_centrifugal*', 'd*ij', 'T_sim')
                                                
aux_name=['crowdsim_data\' int2str(date_name), '_J' int2str(J) '_Part'];
                                                % this string will be used as part of the name of the file where data
                                                %  will be saved
                                                
name_errorlog=['crowdsim_data\' int2str(date_name), '_J' int2str(J) '_ErrorLog'];
                                                % this is the name of the file where info of possible errors
                                                %  (such as, e.g., too high speed of a given ped) will be recorded
                                                %  for further analysis
saved_to_errorlog_already=0;                    % this is an initial value of the indicator of whether any data have been
                                                %  saved to the ErrorLog file already;
                                                %  if it = 0, no data have been saved yet, and to save a new piece of
                                                %  data, use save('name_errolog,'array_...'), and
                                                %  it it =1, the some data has already been saved, and then use
                                                %  save('name_errolog,'array_...','-append')
count_toofastpeds=0;                            % counter that is used to record info about peds whose speed exceeds
                                                %  a specified maximum (see the end of the code for details)
count_toolarge_reshufflingtime=0;               % similar counter to record info about cases where the time spent
                                                %  on reshuffling is too large compared to the calculated time step

% Save parameters required to plot the room, doors, etc.:
save([aux_name '0'], 'B*wall*','N_doors*','Door*','Column*','N_columns','Attraction*','x_ped','y_ped','ex_ped','ey_ped',...
     'Ped_excitement_max_av','N_peds','dT_save','N_parts_of_calcs','ped_radius','wall_thickness')

t_2update_peds=0;                               % this is a counter having the dimension of time  (i.e., [s]);
                                                %  when it reaches a certain value (determined inside the
                                                %  simulation), parameters of the pedestrians get updated based
                                                %  on the density of the crowd that surrounds them
while T_computed < T_sim
    
    if time_the_code == 1
        tic 
        disp('Set up arrays containing info on relative positions and velocities of peds and boundaries:')
    end
    % ++++++++++++++++++++++++++++   Enclosed in "+++" is one step in time   +++++++++++++++++++++++++++
    
    %
	% -----------     Determine relative positions between all pairs of pedestrians.    -----------
    % Pre-allocate necessary arrays for this step:
	x_ij=zeros(N_peds);
	y_ij=zeros(N_peds);
	r_ij=zeros(N_peds);
	vx_ij=zeros(N_peds);
	vy_ij=zeros(N_peds);
%  COMMENTED  06/11/03    
% 	x_mod_ij=zeros(N_peds);
% 	y_mod_ij=zeros(N_peds);
% 	r_mod_ij=zeros(N_peds);
	nx_ij=zeros(N_peds);
	ny_ij=zeros(N_peds);
%  COMMENTED  06/11/03    
% 	nx_mod_ij=zeros(N_peds);
% 	ny_mod_ij=zeros(N_peds);
	x_ib_column=zeros(N_peds,N_columns);
	y_ib_column=zeros(N_peds,N_columns);
	r_ib_column=zeros(N_peds,N_columns);
	nx_ib_column=zeros(N_peds,N_columns);
	ny_ib_column=zeros(N_peds,N_columns);

  	for i=1:N_peds

        % Determine distances of pedestrians from walls:
    	ped_dist_from_left(i)=x_ped(i)-B_wall_xmin;
    	ped_dist_from_right(i)=BwallRight(y_ped(i))-x_ped(i);
    	ped_dist_from_lower(i)=y_ped(i)-BwallLower(x_ped(i));
    	ped_dist_from_upper(i)=BwallUpper(x_ped(i))-y_ped(i);      % distances of pedestrians from walls  [m]
    
    	check_xped_inside(i)=-(ped_dist_from_left(i)+wall_thickness).*(ped_dist_from_right(i)+wall_thickness);
    	check_yped_inside(i)=-(ped_dist_from_lower(i)+wall_thickness).*(ped_dist_from_upper(i)+wall_thickness);
                                                % this arrays are used below to make sure that
                                                %  calculations are performed only for pedestrians
                                                %  inside the room (this includes those who are
                                                %  in right the process of going out of a door)
    	aux_index_ped_inside(i)=check_xped_inside(i) < 0 & check_yped_inside(i) < 0;
                                                % vector of indices which singles out only pedestrians
                                                %  who are inside the room
                                                %  (this indexing vector is used later, when we compute
                                                %   forces acting on pedestrians)
    end;                                                    
                                                    
	% Set up relative positions and relative velocities of pedestrians, and pedestrians and columns:
	for i=1:N_peds
        if aux_index_ped_inside(i) == 1
            for j=(i+1):N_peds
                if aux_index_ped_inside(j) == 1
                    x_ij(i,j)=x_ped(j)-x_ped(i);
                    y_ij(i,j)=y_ped(j)-y_ped(i);
                end
            end
            r_ij(i,i+1:N_peds)=sqrt((x_ij(i,i+1:N_peds)).^2+(y_ij(i,i+1:N_peds)).^2);
            for k=1:N_columns
                x_ib_column(i,k)=Column_c_x(k)-x_ped(i);
                y_ib_column(i,k)=Column_c_y(k)-y_ped(i);
                                                % x- and y-components of relative positions of columns
                                                %  and pedestrians (the corresponding radius-vector is
                                                %  directed from a pedestrian to a column)
            end
        end
	end
	x_ij=x_ij-x_ij';
	y_ij=y_ij-y_ij';
	r_ij=r_ij+r_ij';                            % x,y-matrices are anti-symmetric, r-matrix is symmetric
	r_ib_column=sqrt(x_ib_column.^2+y_ib_column.^2);
                                                % matrix of distances of pedestrians from columns  [m]
	
	% toc
	% disp('% Account only for those pedestrians who are inside the room and not too far from each other:')
	% tic
    for i=1:N_peds
	    aux_index_2far(i,:)=r_ij(i,:) > ped_percep_maxdist(i);
                                                % indices of those entries of the array which correspond
                                                %  to pedestrians being "too far" from one another
	    x_ij(i,aux_index_2far(i,:))=0;
	    y_ij(i,aux_index_2far(i,:))=0;
	    r_ij(i,aux_index_2far(i,:))=0;            % ignore those pairs of pedestrians which are too far
                                                %  from each other
    end
	%
	aux_index_ij=r_ij ~= 0;                     % indices of those pairs of pedestrians where both pedestrians
                                                %  are inside the room and not too far from each other, and
                                                %  indices of diagonal entries are also excluded
	% Calculate the remaining pair-wise quantities only for the pairs selected above:
	for i=1:N_peds
        aux_index_ij_i=aux_index_ij(i,:);
        if max(aux_index_ij_i) > 0
            vx_ij(i,aux_index_ij_i)=vx_ped(aux_index_ij_i)-vx_ped(i);
            vy_ij(i,aux_index_ij_i)=vy_ped(aux_index_ij_i)-vy_ped(i);
        end                                     % x- and y-components of relative velocities  [m/s]
	end
    
    %
    % Of all peds who are close to each other, select only those who will be advancing at this time step:
    aux_index_ij_advancing=logical(zeros(N_peds));
    for i=1:N_peds
        if aux_index_ped_advancing(i) == 1
            aux_index_ij_advancing(i,:)=aux_index_ij(i,:);
        end
    end
    
    % 
    % Calculate the matrix of cosines of mutual location angles, used in determining whether the peds are
    %  facing each other or not:
    cos_ij=zeros(N_peds);
    for i=1:N_peds
        if aux_index_ped_advancing(i) == 1
            cos_ij(i,aux_index_ij_advancing(i,:))=ex_ped(i)*nx_ij(i,aux_index_ij_advancing(i,:))+...
                                                  ey_ped(i)*ny_ij(i,aux_index_ij_advancing(i,:));
        end
    end
    
    
%  COMMENTED  06/11/03    
%     if time_the_code == 1
%         toc
%     
%         tic
%         disp('This is the part where x_mod etc. are calculated. How long is it compared with the rest of this part?')
%     end
% 
%     
%     % Each pedestrian can estimate the location of anyone around him in the next instance of time
% 	%  (determined by the average pedestrian reaction time, ped_tau_reaction_av). A pedestrian calculates the
% 	%  "social" force of repulsion from others based on those _anticipated_ locations of other pedestrians.
% 	x_mod_ij(aux_index_ij_advancing)=x_ij(aux_index_ij_advancing)+vx_ij(aux_index_ij_advancing)*Ped_tau_reaction_av;
% 	y_mod_ij(aux_index_ij_advancing)=y_ij(aux_index_ij_advancing)+vy_ij(aux_index_ij_advancing)*Ped_tau_reaction_av;
% 	r_mod_ij(aux_index_ij_advancing)=sqrt(x_mod_ij(aux_index_ij_advancing).^2+y_mod_ij(aux_index_ij_advancing).^2);
%                                                 % x-,y-components of anticipated relative locations of
%                                                 %  pedestrians, and corresponding distances  [m]
%     
%     if time_the_code == 1
%         toc
% 	
%         tic
%         disp('Continuing to set up the relative positions of peds relative to the boundaries:')
%     end
    
    
	% disp('% Now account only for those pedestrians who are inside the room and not too far from columns:')
    for k=1:N_columns
    	aux_index_col2far_k=r_ib_column(:,k) > ped_b_percep_maxdist';
                                                % indices of those entries which correspond to pedestrians
                                                %  being "too far" from columns
	    x_ib_column(aux_index_col2far_k,k)=0;
	    y_ib_column(aux_index_col2far_k,k)=0;
	    r_ib_column(aux_index_col2far_k,k)=0;
    end
	% 
	aux_index_ib_column=r_ib_column ~= 0;       % indices of those pairs pedestrian-column where pedestrians
                                                %  are inside the room and not too far from the columns;
                                                %  this array of indices is used later, when we calculate
                                                %  perception of columns by a given pedestrian
    % Of all peds who are close to columns, select only those who will be advancing at this time step:
    aux_index_ib_column_advancing=logical(zeros(N_peds,N_columns));
    for i=1:N_peds
        if aux_index_ped_advancing(i) == 1
            aux_index_ib_column_advancing(i,:)=aux_index_ib_column(i,:);
        end
    end

                                                
    % NOTE: I commented out the sparse declarations below, because for matrices smaller than 200x200,
    %       this actually slows down the calculation.
    %       This may change, however, if N_peds increases much beyond 200.
	% % Declare the calculated matrices to be sparse:
	% x_ij=sparse(x_ij);
	% y_ij=sparse(y_ij);
	% r_ij=sparse(r_ij);
	% x_mod_ij=sparse(x_mod_ij);
	% y_mod_ij=sparse(y_mod_ij);
	% r_mod_ij=sparse(r_mod_ij);
	% vx_ij=sparse(vx_ij);
	% vy_ij=sparse(vy_ij);
	% x_ib_column=sparse(x_ib_column);
	% y_ib_column=sparse(y_ib_column);
	% r_ib_column=sparse(r_ib_column);
	
	%toc
	% disp('Define nx,ny:')
	% tic
	% nx_ij=x_ij./(r_ij+eps);
	% ny_ij=y_ij./(r_ij+eps);
	% nx_mod_ij=x_mod_ij./(r_mod_ij+eps);
	% ny_mod_ij=y_mod_ij./(r_mod_ij+eps);
	% nx_ib_column=x_ib_column./(r_ib_column+eps);
	% ny_ib_column=y_ib_column./(r_ib_column+eps);
	nx_ij(aux_index_ij)=x_ij(aux_index_ij)./r_ij(aux_index_ij);
	ny_ij(aux_index_ij)=y_ij(aux_index_ij)./r_ij(aux_index_ij);
%  COMMENTED  06/11/03     
% 	nx_mod_ij(aux_index_ij_advancing)=x_mod_ij(aux_index_ij_advancing)./r_mod_ij(aux_index_ij_advancing);
% 	ny_mod_ij(aux_index_ij_advancing)=y_mod_ij(aux_index_ij_advancing)./r_mod_ij(aux_index_ij_advancing);
	nx_ib_column(aux_index_ib_column)=x_ib_column(aux_index_ib_column)./r_ib_column(aux_index_ib_column);
	ny_ib_column(aux_index_ib_column)=y_ib_column(aux_index_ib_column)./r_ib_column(aux_index_ib_column);
	% nx_ij=sparse(nx_ij);
	% ny_ij=sparse(ny_ij);
	% nx_mod_ij=sparse(nx_mod_ij);
	% ny_mod_ij=sparse(ny_mod_ij);
	% nx_ib_column=sparse(nx_ib_column);
	% ny_ib_column=sparse(ny_ib_column);
                                                % components of corresponding unit vectors 
    
                                                
    if time_the_code == 1
        toc
    

    %
    	tic
	    disp('% ------  Determine force F_i (the tendency of the pedestrian to keep its preferred velocity):  -------')
    end
    
    % Pre-allocate arrays necessary at this step:
	weight_collective_i=zeros(1,N_peds);
	Vx_collective=zeros(1,N_peds);
	Vy_collective=zeros(1,N_peds);
	%
	% % This IS a time-consuming part
	% tic
	for i=1:N_peds
        if aux_index_ped_inside(i) == 1 && aux_index_ped_advancing(i) == 1
            aux_index_ij_advancing_i=aux_index_ij_advancing(i,:);   % see immediately below
            weight_collective_i(aux_index_ij_advancing_i)=perception(ex_ped(i),ey_ped(i),...
                                                          nx_ij(i,aux_index_ij_advancing_i),ny_ij(i,aux_index_ij_advancing_i),...
                                                          ped_collective_percep_angle,ped_collective_percep_back(i)).*...
                                                          exp(-r_ij(i,aux_index_ij_advancing_i)/ped_collective_percep_dist(i));
                                                % weight with which i-th pedestrian takes into account
                                                %  velocity of j-th  pedestrian to adjust his own
                                                %  velocity to that value  (NOTE that he always takes
                                                %  into account his own velocity with weight of zero);
                                                %  function  perception.m is defined
                                                %   in a separate file
            total_weight_collective_i=sum(weight_collective_i(aux_index_ij_advancing_i))+EPS;
            Vx_collective(i)=sum(weight_collective_i(aux_index_ij_advancing_i).*vx_ped(aux_index_ij_advancing_i))/...
                             total_weight_collective_i;
            Vy_collective(i)=sum(weight_collective_i(aux_index_ij_advancing_i).*vy_ped(aux_index_ij_advancing_i))/...
                             total_weight_collective_i;
                                                % x- and y-components of the preferred velocity which
                                                %  i-th pedestrian determines based on the velocities
                                                %  of pedestrians around him  [m/s]
        end
	end
    % For later use in calculation of e{x,y}_ped, find direction vectors of the collective velocities:
    speed_collective=sqrt(Vx_collective.^2+Vy_collective.^2)+eps;
    ex_collective=Vx_collective./speed_collective;
    ey_collective=Vy_collective./speed_collective;
    % Determine the direction of the individually-preferred speed of each peds:
    for i=1:N_peds
        if ped_gave_up(i) == 0 
            if ped_attr_loc_memory(i) ~= 0      % this loop is necessary to handle the initial time step, when the
                                                %  vectors from the ped to the attractions haven't yet been determined
                aux_ex_individual=ex_ped(i)*(1-ped_attr_loc_memory(i))+...
                                  nx_ia(i,strongest_attraction_index(i))*ped_attr_loc_memory(i);
                aux_ey_individual=ey_ped(i)*(1-ped_attr_loc_memory(i))+...
                                  ny_ia(i,strongest_attraction_index(i))*ped_attr_loc_memory(i);
                aux_e_abs=sqrt(aux_ex_individual^2+aux_ey_individual^2);
                if aux_e_abs > EPS
                    ex_individual(i)=aux_ex_individual/aux_e_abs;
                    ey_individual(i)=aux_ey_individual/aux_e_abs;
                else
                    ex_individual(i)=0;
                    ey_individual(i)=0;
                end
            else                                % i.e. if the ped's memory of the location of the attraction is 0, which,
                                                %  in particular, is so at the initial moment
                ex_individual(i)=ex_ped(i);
                ey_individual(i)=ey_ped(i);
            end
        elseif ped_gave_up(i) == 1              % if the ped has given up his strongest attraction, he gets chooses his
                                                %  direction of preferred motion randomly:
            ex_individual(i)=(2*rand-1);
            ey_individual(i)=sign((2*rand-1))*sqrt(1-(ex_individual(i))^2);
        end
    end
    % The resulting preferred velocity is sum of the ped's individual and collective velocities:    
	Vx_pref=(1+ped_attr_loc_memory'.*excitement_ped).*ped_speed0.*ex_individual.*ped_independence+...
            Vx_collective.*(1-ped_independence);
	Vy_pref=(1+ped_attr_loc_memory'.*excitement_ped).*ped_speed0.*ey_individual.*ped_independence+...
            Vy_collective.*(1-ped_independence);
                                                % x- and y-components of the preferred velocity of pedestrians  [m/s]
% % % 	Fx_pref_velocity=-ped_mass.*(vx_ped-Vx_pref)./ped_tau_reaction;
% % % 	Fy_pref_velocity=-ped_mass.*(vy_ped-Vy_pref)./ped_tau_reaction;
% % %                                                 % x- and y-components of the force with which 
% % %                                                 %  pedestrians try to maintain preferred velocity  [N]


    if time_the_code == 1
        toc
    
    
	
	    %
        tic
	    disp('% ----- Determine repulsive forces F_ij and N_ij and the friction force T_ij between pedestrians. ------')
        disp('% ----- First determine  F_ij :')
    end
    
    % Pre-allocate, and at the same time {\em clean}, arrays necessary at this step
	%  (cleaning may be necessary since Fcontact_ped_ij is supposed to be updated at each step,
	%   but the loop below, by construction, updates only its nonzero entries) :
	Frep_close_ij_i=zeros(1,N_peds);
	weight_close_peds_i=zeros(1,N_peds);
    face2face_modifying_factor=ones(1,N_peds);
	Fabs_ij=zeros(N_peds);
	Fx_ij=zeros(N_peds);
	Fy_ij=zeros(N_peds);
	Nabs_ij=zeros(N_peds);
	Tabs_ij=zeros(N_peds);
	Nx_ij=zeros(N_peds);
	Ny_ij=zeros(N_peds);
	Tx_ij=zeros(N_peds);
	Ty_ij=zeros(N_peds);
    %
	for i=1:N_peds
        if aux_index_ped_inside(i) == 1 &&  aux_index_ped_advancing(i) == 1
            aux_index_ij_advancing_i=aux_index_ij_advancing(i,:);
                                                % see immediately below
            %%% Frep_close_ij_i(aux_index_ij_advancing_i)=ped_rep_close_max(i)*exp(-((r_mod_ij(i,aux_index_ij_advancing_i)-...
          	Frep_close_ij_i(aux_index_ij_advancing_i)=ped_rep_close_max(i)*exp(-((r_ij(i,aux_index_ij_advancing_i)-...
                                                      d_ij(i,aux_index_ij_advancing_i))/...
                                                      ped_rep_close_dist(i)).^rep_close_exponent);
                                                % absolute value of the repulsive "social" force
                                                %  between i-th pedestrian and others  [N];
                                                %  it is computed only for those pairs of pedestrians
                                                %  which yield  aux_index_ij ~= 0 (see explanation for
                                                %  aux_index_ij  above)
            weight_close_peds_i(aux_index_ij_advancing_i)=perception(ex_ped(i),ey_ped(i),...
                                                          nx_ij(i,aux_index_ij_advancing_i),ny_ij(i,aux_index_ij_advancing_i),...
                                                          ped_rep_close_percep_angle_min,ped_rep_close_percep_back);
                                                % weight with which i-th pedestrian takes into account
                                                %  presence of j-th  pedestrian;
                                                %  function  perception.m  is defined
                                                %  in a separate file
            face2face_modifying_factor(aux_index_ij_advancing_i)=(ped_rep_close_max_ratio(aux_index_ij_advancing_i)-1).*...
                                                                 ((cos_ij(i,aux_index_ij_advancing_i) > ...
                                                                   cos_ped_rep_close_percep_angle_min) & ...
                                                                  (cos_ij(aux_index_ij_advancing_i,i) > ...
                                                                   cos_ped_rep_close_percep_angle_min)')+1;
            Fabs_ij(i,aux_index_ij_advancing_i)=weight_close_peds_i(aux_index_ij_advancing_i).*...
                                                face2face_modifying_factor(aux_index_ij_advancing_i).*...
                                                Frep_close_ij_i(aux_index_ij_advancing_i);      
        end
	end
    
    % NOTE: I commented out the sparsity declaration for the same reason I did it earlier in this code.
	% % % Declare the calculated matrices to be sparse:
	% % Fcontact_ped_ij=sparse(Fcontact_ped_ij);
	% % Frep_close_ij=sparse(Frep_close_ij);
	% % weight_close_peds=sparse(weight_close_peds);
%  COMMENTED ON 06/11/03
% 	Fx_ij(aux_index_ij_advancing)=-Fabs_ij(aux_index_ij_advancing).*nx_mod_ij(aux_index_ij_advancing);
% 	Fy_ij(aux_index_ij_advancing)=-Fabs_ij(aux_index_ij_advancing).*ny_mod_ij(aux_index_ij_advancing);
    Fx_ij(aux_index_ij_advancing)=-Fabs_ij(aux_index_ij_advancing).*nx_ij(aux_index_ij_advancing);
 	Fy_ij(aux_index_ij_advancing)=-Fabs_ij(aux_index_ij_advancing).*ny_ij(aux_index_ij_advancing);
                                                % x- and y-components of the repulsive "social"
                                                %  force with which i-th and j-th pedestrians  
                                                %  avoid each other  [N]
                                                
    if time_the_code == 1
        toc
        
        tic
        disp('% ------   Now determine  N_ij  and T_ij :')
    end
                                                    
    aux_index_contact_ij=r_ij<d_ij & aux_index_ij_advancing; 
                                                % the contact force below is nonzero only for those
                                                %  pedestrians who come in contact with each other and 
                                                %  will be advancing at this time step
	Nabs_ij(aux_index_contact_ij)=ped_elasticity*(d_ij(aux_index_contact_ij)-r_ij(aux_index_contact_ij));
                                                % TRY THE HOOKE'S LAW    
	Tabs_ij(aux_index_contact_ij)=ped_friction*Nabs_ij(aux_index_contact_ij).*...
                                  (-vx_ij(aux_index_contact_ij).*ny_ij(aux_index_contact_ij)+...
                                  vy_ij(aux_index_contact_ij).*nx_ij(aux_index_contact_ij))./...
                                  sqrt(vx_ij(aux_index_contact_ij).^2+vy_ij(aux_index_contact_ij).^2+EPS);
                                                % magnitudes of normal and tangential forces acting
                                                %  when i-th and j-th pedestrians come into contact  [N]  
	Nx_ij(aux_index_contact_ij)=-Nabs_ij(aux_index_contact_ij).*nx_ij(aux_index_contact_ij);
	Ny_ij(aux_index_contact_ij)=-Nabs_ij(aux_index_contact_ij).*ny_ij(aux_index_contact_ij);
	Tx_ij(aux_index_contact_ij)=-Tabs_ij(aux_index_contact_ij).*ny_ij(aux_index_contact_ij);
	Ty_ij(aux_index_contact_ij)=Tabs_ij(aux_index_contact_ij).*nx_ij(aux_index_contact_ij); 
                                                % x- and y-components of the normal and 
                                                %  tangential forces  [N]
	
    if time_the_code == 1
        toc
	
                                                    
	    %
        tic
	    disp('% --------------   Now in a way similar to the above, determine forces between a pedestrian  ------------')
	    disp('% -----------   and the boundaries (which include both the walls and surfaces of the columns).   --------')
        disp(' ')
        disp('% -----------  First determine the social repulsion forces :')
    end
    
    %
    % Pre-allocate, and at the same time {\em clean}, arrays necessary at this step
	%  (cleaning may be necessary since Fcontact_{wall,column} arrays are supposed to be updated
	%   at each step, but the loop below, by construction, updates only their nonzero entries) :
	ped_infront_left_wall=zeros(1,N_peds);
	ped_infront_right_wall=zeros(1,N_peds);
	ped_infront_lower_wall=zeros(1,N_peds);
	ped_infront_upper_wall=zeros(1,N_peds);
    ped_infront_left_door=zeros(1,N_peds);
	ped_infront_right_door=zeros(1,N_peds);
	ped_infront_lower_door=zeros(1,N_peds);
	ped_infront_upper_door=zeros(1,N_peds);
    ped_within_left_door=zeros(1,N_peds);
    ped_within_right_door=zeros(1,N_peds);
    ped_within_lower_door=zeros(1,N_peds);
    ped_within_upper_door=zeros(1,N_peds);
	Fabs_ib_wall_left=zeros(1, N_peds);
	Fabs_ib_wall_right=zeros(1, N_peds);
	Fabs_ib_wall_lower=zeros(1, N_peds);
	Fabs_ib_wall_upper=zeros(1, N_peds);
	Fabs_ib_column=zeros(N_peds,N_columns);
	weight_close_wall_left=zeros(1, N_peds);
	weight_close_wall_right=zeros(1, N_peds);
	weight_close_wall_lower=zeros(1, N_peds);
	weight_close_wall_upper=zeros(1, N_peds);
	weight_close_column=zeros(N_peds,N_columns);
    face2left_modifying_factor=ones(1,N_peds);
    face2right_modifying_factor=ones(1,N_peds);
    face2lower_modifying_factor=ones(1,N_peds);
    face2upper_modifying_factor=ones(1,N_peds);
    face2column_modifying_factor=ones(N_peds,N_columns);
	Fcontact_wall_left=zeros(1, N_peds);
	Fcontact_wall_right=zeros(1, N_peds);
	Fcontact_wall_lower=zeros(1, N_peds);
	Fcontact_wall_upper=zeros(1, N_peds);
	Fcontact_column=zeros(N_peds,N_columns);
	cosangle_ped_tangcolumn=zeros(N_peds,N_columns);
    %
	% First, select only those pedestrians who are inside the room and not in front of any of the doors
	%  (if a pedestrian is in front of a door, then there is no "social" repulsion acting on him from the
	%   wall containing that door). In the same loop, determine if the pedestrian is in the process of
    %   going through any door (i.e. then he is between that door's posts), and also determine that door's
    %   sequential number on the wall it belongs to.
	for i=1:N_peds
        if aux_index_ped_inside(i) == 1        % do calculations only for pedestrians inside the room
            
            % Left wall:
            if length(Door_left_low) == 0
                ped_infront_left_wall(i)=1;
            else
                [isnot_infront_door,n_of_door]=min((y_ped(i)-ped_radius(i)/3000-Door_left_low).*...
                                                   (y_ped(i)+ped_radius(i)/3000-Door_left_up));
                                                % an auxiliary quantity used immediately below
                if isnot_infront_door >= 0
                    ped_infront_left_wall(i)=1;
                else                            % i.e. if pedestrian is in front of a door on left wall
                    ped_infront_left_wall(i)=0;
                    ped_infront_left_door(i)=1;
                    if x_ped(i) <= B_wall_xmin & x_ped(i) >= B_wall_xmin-wall_thickness
                                                % i.e. if pedestrian is going through a door at this moment
                        ped_within_left_door(i)=n_of_door;
                    end
                end
            end
            % Right wall:
            if length(Door_right_low) == 0
                ped_infront_right_wall(i)=1;
            else
                [isnot_infront_door,n_of_door]=min((y_ped(i)-ped_radius(i)/3000-Door_right_low).*...
                                                   (y_ped(i)+ped_radius(i)/3000-Door_right_up));
                                                % an auxiliary quantity used immediately below
                if isnot_infront_door >= 0
                    ped_infront_right_wall(i)=1;
                else                            % i.e. if pedestrian is in front of a door on right wall
                    ped_infront_right_wall(i)=0;
                    ped_infront_right_door(i)=1;
                    if x_ped(i) >= BwallRight(y_ped(i)) & x_ped(i) <= BwallRight(y_ped(i))+wall_thickness
                                                % i.e. if pedestrian is going through a door at this moment
                        ped_within_right_door(i)=n_of_door;
                    end
                end
            end
            % Lower wall:
            if length(Door_lower_left) == 0
                ped_infront_lower_wall(i)=1;
            else
                [isnot_infront_door,n_of_door]=min((x_ped(i)-ped_radius(i)/3000-Door_lower_left).*...
                                                   (x_ped(i)+ped_radius(i)/3000-Door_lower_right));
                                                % an auxiliary quantity used immediately below
                if isnot_infront_door >= 0
                    ped_infront_lower_wall(i)=1;
                else                            % i.e. if pedestrian is in front of a door on lower wall
                    ped_infront_lower_wall(i)=0;
                    ped_infront_lower_door(i)=1;
                    if y_ped(i) <= B_wall_ymin & y_ped(i) >= B_wall_ymin-wall_thickness
                                                % i.e. if pedestrian is going through a door at this moment
                        ped_within_lower_door(i)=n_of_door;
                    end
                end
            end
            % Upper wall:
            if length(Door_upper_left) == 0
                ped_infront_upper_wall(i)=1;
            else
                [isnot_infront_door,n_of_door]=min((x_ped(i)-ped_radius(i)/3000-Door_upper_left).*...
                                                   (x_ped(i)+ped_radius(i)/3000-Door_upper_right));
                                                % an auxiliary quantity used immediately below
                if isnot_infront_door >= 0
                    ped_infront_upper_wall(i)=1;
                else                            % i.e. if pedestrian is in front of a door on upper wall
                    ped_infront_upper_wall(i)=0;
                    ped_infront_upper_door(i)=1;
                    if y_ped(i) >= B_wall_ymax & y_ped(i) <= B_wall_ymax+wall_thickness
                                                % i.e. if pedestrian is going through a door at this moment
                        ped_within_upper_door(i)=n_of_door;
                    end
                end
            end
            
        end
	end
    
	aux_index_ib_left=ped_infront_left_wall == 1 & ped_dist_from_left < ped_b_percep_maxdist;
	aux_index_ib_right=ped_infront_right_wall == 1 & ped_dist_from_right < ped_b_percep_maxdist;
	aux_index_ib_lower=ped_infront_lower_wall == 1 & ped_dist_from_lower < ped_b_percep_maxdist;
	aux_index_ib_upper=ped_infront_upper_wall == 1 & ped_dist_from_upper < ped_b_percep_maxdist;
                                                % these indices select only those pedestrians who are
                                                %  inside the room, not in front of a door, and not
                                                %  too far from a wall to ignore it
	
    aux_index_ib_left_advancing=aux_index_ib_left & aux_index_ped_advancing;
    aux_index_ib_right_advancing=aux_index_ib_right & aux_index_ped_advancing;
    aux_index_ib_lower_advancing=aux_index_ib_lower & aux_index_ped_advancing;
    aux_index_ib_upper_advancing=aux_index_ib_upper & aux_index_ped_advancing;
                                               % corresponding indices which also take into account whether the ped
                                               %  will advance at this time step
    
	% Calculate the weights with which the selected pedestrians take into account walls and columns:
	weight_close_wall_left(aux_index_ib_left_advancing)=perception(-1,0,...
                                              ex_ped(aux_index_ib_left_advancing),ey_ped(aux_index_ib_left_advancing),...
                                              ped_left_wall_percep_angle(aux_index_ib_left_advancing),...
                                              ped_left_wall_percep_back(aux_index_ib_left_advancing));
	weight_close_wall_right(aux_index_ib_right_advancing)=perception(1,0,...
                                                ex_ped(aux_index_ib_right_advancing),ey_ped(aux_index_ib_right_advancing),...
                                                ped_right_wall_percep_angle(aux_index_ib_right_advancing),...
                                                ped_right_wall_percep_back(aux_index_ib_right_advancing));
	weight_close_wall_lower(aux_index_ib_lower_advancing)=perception(0,-1,...
                                                ex_ped(aux_index_ib_lower_advancing),ey_ped(aux_index_ib_lower_advancing),...
                                                ped_lower_wall_percep_angle(aux_index_ib_lower_advancing),...
                                                ped_lower_wall_percep_back(aux_index_ib_lower_advancing));
	weight_close_wall_upper(aux_index_ib_upper_advancing)=perception(0,1,...
                                                ex_ped(aux_index_ib_upper_advancing),ey_ped(aux_index_ib_upper_advancing),...
                                                ped_upper_wall_percep_angle(aux_index_ib_upper_advancing),...
                                                ped_upper_wall_percep_back(aux_index_ib_upper_advancing));
	for k=1:N_columns
        isindex_nonempty=max(aux_index_ib_column_advancing(:,k));
        if isindex_nonempty == 1
            weight_close_column(aux_index_ib_column_advancing(:,k),k)=...
                                                            transpose(perception(ex_ped(aux_index_ib_column_advancing(:,k)),...
                                                            ey_ped(aux_index_ib_column_advancing(:,k)),...
                                                            (nx_ib_column(aux_index_ib_column_advancing(:,k),k))',...
                                                            (ny_ib_column(aux_index_ib_column_advancing(:,k),k))',...
                                                            (ped_column_percep_angle(aux_index_ib_column_advancing(:,k),k))',...
                                                            (ped_column_percep_back(aux_index_ib_column_advancing(:,k),k))'));
        else
            weight_close_column(:,k)=0;
        end
	end
                                                % weights with which pedestrians take into account
                                                %  presence of walls and columns
                                                
    face2left_modifying_factor(aux_index_ib_left_advancing)=(ped_b_rep_close_max_ratio(aux_index_ib_left_advancing)-1).*...
                                                            (ex_ped(aux_index_ib_left_advancing) < ...
                                                             -cos_ped_rep_close_percep_angle_min)+1;
    face2right_modifying_factor(aux_index_ib_right_advancing)=(ped_b_rep_close_max_ratio(aux_index_ib_right_advancing)-1).*...
                                                              (ex_ped(aux_index_ib_right_advancing) > ...
                                                               cos_ped_rep_close_percep_angle_min)+1;
    face2lower_modifying_factor(aux_index_ib_lower_advancing)=(ped_b_rep_close_max_ratio(aux_index_ib_lower_advancing)-1).*...
                                                              (ey_ped(aux_index_ib_lower_advancing) < ...
                                                               -cos_ped_rep_close_percep_angle_min)+1;
    face2upper_modifying_factor(aux_index_ib_upper_advancing)=(ped_b_rep_close_max_ratio(aux_index_ib_upper_advancing)-1).*...
                                                              (ey_ped(aux_index_ib_upper_advancing) > ...
                                                               cos_ped_rep_close_percep_angle_min)+1;
    for k=1:N_columns
        isindex_nonempty=max(aux_index_ib_column_advancing(:,k));
        if isindex_nonempty == 1
            face2column_modifying_factor(aux_index_ib_column_advancing(:,k),k)=...
                                        (ped_b_rep_close_max_ratio(aux_index_ib_column_advancing(:,k))-1)'.*...
                                        ((ex_ped(aux_index_ib_column_advancing(:,k)))'.*...
                                         nx_ib_column(aux_index_ib_column_advancing(:,k),k)+...
                                         (ey_ped(aux_index_ib_column_advancing(:,k)))'.*...
                                         ny_ib_column(aux_index_ib_column_advancing(:,k),k) > ...
                                         cos_ped_rep_close_percep_angle_min)+1;
        end
    end
                                                % factors accounting for whether a ped is facing a boundary or not;
                                                %  note that the angle determining whether he does so is
                                                %  ped_rep_close_percep_angle_min   rather than the current value,
                                                %  ped_rep_close_percep_angle,   because the effect of higher repulsion
                                                %  is based on a ped actually seeing the obstacle rather than knowing
                                                %  where it is located
        
        
	
	% Calculate quantities needed for subsequent calculation of "social" forces of repulsion from boundaries:
	Fabs_ib_wall_left(aux_index_ib_left_advancing)=ped_b_rep_close_max(aux_index_ib_left_advancing).*...
                                                   weight_close_wall_left(aux_index_ib_left_advancing).*...
                                                   face2left_modifying_factor(aux_index_ib_left_advancing).*...
                                                   exp(-((ped_dist_from_left(aux_index_ib_left_advancing)-...
                                                   ped_radius(aux_index_ib_left_advancing))./...
                                                   ped_b_rep_close_dist(aux_index_ib_left_advancing)).^rep_close_exponent);
	Fabs_ib_wall_right(aux_index_ib_right_advancing)=ped_b_rep_close_max(aux_index_ib_right_advancing).*...
                                                     weight_close_wall_right(aux_index_ib_right_advancing).*...
                                                     face2right_modifying_factor(aux_index_ib_right_advancing).*...
                                                     exp(-((ped_dist_from_right(aux_index_ib_right_advancing)-...
                                                     ped_radius(aux_index_ib_right_advancing))./...
                                                     ped_b_rep_close_dist(aux_index_ib_right_advancing)).^rep_close_exponent);
	Fabs_ib_wall_lower(aux_index_ib_lower_advancing)=ped_b_rep_close_max(aux_index_ib_lower_advancing).*...
                                                     weight_close_wall_lower(aux_index_ib_lower_advancing).*...
                                                     face2lower_modifying_factor(aux_index_ib_lower_advancing).*...
                                                     exp(-((ped_dist_from_lower(aux_index_ib_lower_advancing)-...
                                                     ped_radius(aux_index_ib_lower_advancing))./...
                                                     ped_b_rep_close_dist(aux_index_ib_lower_advancing)).^rep_close_exponent);
	Fabs_ib_wall_upper(aux_index_ib_upper_advancing)=ped_b_rep_close_max(aux_index_ib_upper_advancing).*...
                                                     weight_close_wall_upper(aux_index_ib_upper_advancing).*...
                                                     face2upper_modifying_factor(aux_index_ib_upper_advancing).*...
                                                     exp(-((ped_dist_from_upper(aux_index_ib_upper_advancing)-...
                                                     ped_radius(aux_index_ib_upper_advancing))./...
                                                     ped_b_rep_close_dist(aux_index_ib_upper_advancing)).^rep_close_exponent);
    for k=1:N_columns
	    Fabs_ib_column(aux_index_ib_column_advancing(:,k),k)=(ped_b_rep_close_max(aux_index_ib_column_advancing(:,k)))'.*...
                                                             weight_close_column(aux_index_ib_column_advancing(:,k),k).*...
                                                          face2column_modifying_factor(aux_index_ib_column_advancing(:,k),k).*...
                                                             exp(-((r_ib_column(aux_index_ib_column_advancing(:,k),k)-...
                                                             pedCol_radius(aux_index_ib_column_advancing(:,k),k))./....
                                                             (ped_b_rep_close_dist(aux_index_ib_column_advancing(:,k)))').^...
                                                             rep_close_exponent);
    end                                         % these quantities are used to calculate "social"
                                                %  repulsive forces from walls and columns
    
    if time_the_code == 1	
        toc
        
	    tic
        disp('Now calculate the physical forces of repulsion of peds from boundaries :')
    end
    
	% Now select only those pedestrians who are in physical contact with walls or columns and who will be advancing
    %  at this time step :
	aux_index_2close2left=aux_index_ib_left_advancing & ped_dist_from_left < ped_radius;
	aux_index_2close2right=aux_index_ib_right_advancing & ped_dist_from_right < ped_radius;
	aux_index_2close2lower=aux_index_ib_lower_advancing & ped_dist_from_lower < ped_radius;
	aux_index_2close2upper=aux_index_ib_upper_advancing & ped_dist_from_upper < ped_radius;
	aux_index_2close2column=aux_index_ib_column_advancing & r_ib_column < pedCol_radius;
                                                % these indices select only those pedestrians who are
                                                %  in physical contact with walls and/or columns and 
                                                %  who will be advancing at this time step
	Fcontact_wall_left(aux_index_2close2left)=ped_radius(aux_index_2close2left)-ped_dist_from_left(aux_index_2close2left);
	Fcontact_wall_right(aux_index_2close2right)=ped_radius(aux_index_2close2right)-ped_dist_from_right(aux_index_2close2right);
	Fcontact_wall_lower(aux_index_2close2lower)=ped_radius(aux_index_2close2lower)-ped_dist_from_lower(aux_index_2close2lower);
	Fcontact_wall_upper(aux_index_2close2upper)=ped_radius(aux_index_2close2upper)-ped_dist_from_upper(aux_index_2close2upper);
	Fcontact_column(aux_index_2close2column)=pedCol_radius(aux_index_2close2column)-r_ib_column(aux_index_2close2column);
                                                % TRY THE HOOKE'S LAW
                                                % auxiliary quantities which are used to calculate
	                                            %  both normal and tangential "physical" forces 
	                                            %  occurring when a pedestrian comes into
	                                            %  contact with the walls and/or columns
	for k=1:N_columns
        isindex_nonempty=max(aux_index_2close2column(:,k));
        if isindex_nonempty == 1
            cosangle_ped_tangcolumn(aux_index_2close2column(:,k),k)=-(ex_ped(aux_index_2close2column(:,k)).*...
                                                                (ny_ib_column(aux_index_2close2column(:,k),k))'-...
                                                                ey_ped(aux_index_2close2column(:,k)).*...
                                                                (nx_ib_column(aux_index_2close2column(:,k),k))')';
        else
            cosangle_ped_tangcolumn(:,k)=0;
        end
	end                                             %  cosine of the angle between the pedestrian's velocity and
                                                    %  the tangent vector to a given column
	% toc
	% part='C'
	% tic
	% Calculate the x,y-components of the "social" and physical forces between pedestrians and boundaries:
	Fx_ib_wall_left=Fabs_ib_wall_left;
	Fy_ib_wall_left=zeros(size(Fabs_ib_wall_left));
	Fx_ib_wall_right=-Fabs_ib_wall_right;
	Fy_ib_wall_right=zeros(size(Fabs_ib_wall_right));
	Fx_ib_wall_lower=zeros(size(Fabs_ib_wall_lower));
	Fy_ib_wall_lower=Fabs_ib_wall_lower;
	Fx_ib_wall_upper=zeros(size(Fabs_ib_wall_upper));
	Fy_ib_wall_upper=-Fabs_ib_wall_upper;
	Fx_ib_column=-Fabs_ib_column.*nx_ib_column;
	Fy_ib_column=-Fabs_ib_column.*ny_ib_column;     % x- and y-components of "social" repulsive forces from
                                                    %  walls and columns  [N]
	
	Nabs_ib_wall_left=ped_elasticity*Fcontact_wall_left;
	Nabs_ib_wall_right=ped_elasticity*Fcontact_wall_right;
	Nabs_ib_wall_lower=ped_elasticity*Fcontact_wall_lower;
	Nabs_ib_wall_upper=ped_elasticity*Fcontact_wall_upper;
	Nabs_ib_column=ped_elasticity*Fcontact_column;
	Tabs_ib_wall_left=ped_friction*Nabs_ib_wall_left;
	Tabs_ib_wall_right=ped_friction*Nabs_ib_wall_right;
	Tabs_ib_wall_lower=ped_friction*Nabs_ib_wall_lower;
	Tabs_ib_wall_upper=ped_friction*Nabs_ib_wall_upper;
	Tabs_ib_column=-ped_friction*Nabs_ib_column.*cosangle_ped_tangcolumn;
                                                    % absolute values of "physical" repulsive and frictional 
                                                    %  forces from walls and columns  [N] 
	Nx_ib_wall_left=Nabs_ib_wall_left;
	Ny_ib_wall_left=zeros(size(Nabs_ib_wall_left));
	Nx_ib_wall_right=-Nabs_ib_wall_right;
	Ny_ib_wall_right=zeros(size(Nabs_ib_wall_right));
	Nx_ib_wall_lower=zeros(size(Nabs_ib_wall_lower));
	Ny_ib_wall_lower=Nabs_ib_wall_lower;
	Nx_ib_wall_upper=zeros(size(Nabs_ib_wall_upper));
	Ny_ib_wall_upper=-Nabs_ib_wall_upper;
	Nx_ib_column=-Nabs_ib_column.*nx_ib_column;
	Ny_ib_column=-Nabs_ib_column.*ny_ib_column; 
	%
	Tx_ib_wall_left=zeros(size(Tabs_ib_wall_left));
	Ty_ib_wall_left=-ey_ped.*Tabs_ib_wall_left;
	Tx_ib_wall_right=zeros(size(Tabs_ib_wall_right));
	Ty_ib_wall_right=-ey_ped.*Tabs_ib_wall_right;
	Tx_ib_wall_lower=-ex_ped.*Tabs_ib_wall_lower;
	Ty_ib_wall_lower=zeros(size(Tabs_ib_wall_lower));
	Tx_ib_wall_upper=-ex_ped.*Tabs_ib_wall_upper;
	Ty_ib_wall_upper=zeros(size(Tabs_ib_wall_upper));
	Tx_ib_column=-Tabs_ib_column.*ny_ib_column;
	Ty_ib_column=Tabs_ib_column.*nx_ib_column; 
                                                    % x- and y-components of "physical" repulsive and
                                                    %  frictional forces from walls and columns  [N]
	
    if time_the_code == 1
        toc
	
     	%
        tic
	    disp('% -------------------   Set up attraction forces acting on pedestrians:  -------------------------')
    end
    
    % Pre-allocate arrays necessary at the following step:
	x_ia=zeros(N_peds,N_attractions);
	y_ia=zeros(N_peds,N_attractions);
	weight_attraction=zeros(N_peds,N_attractions);
	F_ia_auxaux=zeros(N_peds,N_attractions);
    %
	% tic
    % If a ped is not in front of a door, locate the attraction his radius AHEAD of the door's center;
    %  if he is in front of the door, leave the attraction at the door's center.
    %  The former provision is necessary to correctly account for the movement of peds who are along the walls
    %  and moving towards the door on that wall. If the attraction is not moved out of the door opening,
    %  the ped will be constantly oriented towards the door and hence will see excessive friction from the wall.
    for i=1:N_peds
        if aux_index_ped_inside(i) == 1 && aux_index_ped_advancing(i) == 1
            for k=1:N_doors_left
                if k == strongest_attraction_index(i)
                    x_ia(i,k)=Attraction_x(k)+(1-ped_infront_left_door(i))*ped_radius(i)-x_ped(i);
                    y_ia(i,k)=Attraction_y(k)-y_ped(i);
                else
                    x_ia(i,k)=Attraction_x(k)-x_ped(i);
                    y_ia(i,k)=Attraction_y(k)-y_ped(i);
                end
            end
            for k=N_doors_left+1:N_doors_left+N_doors_right
                if k == strongest_attraction_index(i)
                    x_ia(i,k)=Attraction_x(k)-(1-ped_infront_right_door(i))*ped_radius(i)-x_ped(i);
                    y_ia(i,k)=Attraction_y(k)-y_ped(i);
                else
                    x_ia(i,k)=Attraction_x(k)-x_ped(i);
                    y_ia(i,k)=Attraction_y(k)-y_ped(i);
                end
            end
            for k=N_doors_left+N_doors_right+1:N_doors_left+N_doors_right+N_doors_lower
                if k == strongest_attraction_index(i)
                    x_ia(i,k)=Attraction_x(k)-x_ped(i);
                    y_ia(i,k)=Attraction_y(k)+(1-ped_infront_lower_door(i))*ped_radius(i)-y_ped(i);
                else
                    x_ia(i,k)=Attraction_x(k)-x_ped(i);
                    y_ia(i,k)=Attraction_y(k)-y_ped(i);
                end
            end
            for k=N_doors_left+N_doors_right+N_doors_lower+1:N_attractions
                if k == strongest_attraction_index(i)
                    x_ia(i,k)=Attraction_x(k)-x_ped(i);
                    y_ia(i,k)=Attraction_y(k)-(1-ped_infront_upper_door(i))*ped_radius(i)-y_ped(i);
                else
                    x_ia(i,k)=Attraction_x(k)-x_ped(i);
                    y_ia(i,k)=Attraction_y(k)-y_ped(i);
                end
            end
        end
    end
% 	for k=1:N_attractions
%         x_ia(aux_index_ped_inside,k)=Attraction_x(k)-x_ped(aux_index_ped_inside)';
%         y_ia(aux_index_ped_inside,k)=Attraction_y(k)-y_ped(aux_index_ped_inside)';
% 	end

	r_ia=sqrt(x_ia.^2+y_ia.^2)+eps;
	aux_nx_ia=x_ia./r_ia;
	aux_ny_ia=y_ia./r_ia;                       % preliminary values of x- and y- coordinates of the vectors and
                                                %  corresponding unit vectors pointing from pedestrians to attractions;
                                                %  because of the "eps" in  r_ai, the length of the resulting vector
                                                %  may be slightly different from 1 (I actually observed this in one
                                                %  particular case! - apparently the ped was too close to the center
                                                %  of the attraction, so that his distance from it became comparable to
                                                %  1000*eps or so); 
                                                %  therefore, next we make sure that the length of those vectors is
                                                %  exactly 1:
    aux_length_ia=sqrt(aux_nx_ia.^2+aux_ny_ia.^2);
    for k=1:N_attractions
        nx_ia(aux_index_ped_inside & aux_index_ped_advancing,k)=aux_nx_ia(aux_index_ped_inside & aux_index_ped_advancing,k)./...
                                                                aux_length_ia(aux_index_ped_inside & aux_index_ped_advancing,k);
        ny_ia(aux_index_ped_inside & aux_index_ped_advancing,k)=aux_ny_ia(aux_index_ped_inside & aux_index_ped_advancing,k)./...
                                                                aux_length_ia(aux_index_ped_inside & aux_index_ped_advancing,k);
    end
                          
                                                
	for k=1:N_attractions
        weight_attraction(aux_index_ped_inside & aux_index_ped_advancing,k)=...
                                                  transpose(perception(ex_ped(aux_index_ped_inside & aux_index_ped_advancing),...
                                                  ey_ped(aux_index_ped_inside & aux_index_ped_advancing),...
                                                  (nx_ia(aux_index_ped_inside & aux_index_ped_advancing,k))',...
                                                  (ny_ia(aux_index_ped_inside & aux_index_ped_advancing,k))',...
                                                  (ped_attraction_percep_angle(aux_index_ped_inside & aux_index_ped_advancing,k))',...
                                                  (ped_attraction_percep_back(aux_index_ped_inside & aux_index_ped_advancing,k))'));
                                                % weight with which pedestrians take into account
                                                %  presence of the k-th attraction
        F_ia_auxaux(aux_index_ped_inside & aux_index_ped_advancing,k)=...
                                            ped_attraction_strength(aux_index_ped_inside & aux_index_ped_advancing,k).*...
                                            exp(-r_ia(aux_index_ped_inside & aux_index_ped_advancing,k)./...
                                            (ped_attraction_dist(aux_index_ped_inside & aux_index_ped_advancing))');
                                                % auxiliary quantity used immediately below
	end                                            
	Fabs_ia_aux=F_ia_auxaux.*weight_attraction; % array of forces from all attractions acting on pedestrians  [N]
	[Fabs_ia,strongest_attraction_index]=max(Fabs_ia_aux,[],2); 
                                                % each pedestrian is attracted to only one attraction;
                                                %  specifically, he is attracted to the one which
                                                %  attracts him the most
	for i=1:N_peds
        Fx_ia(i)=Fabs_ia(i)*nx_ia(i,strongest_attraction_index(i));
        Fy_ia(i)=Fabs_ia(i)*ny_ia(i,strongest_attraction_index(i));
	end                                         % x- and y-components of the force acting on a pedestrian
                                                %  from its strongest attraction  [N]
	   
     
    if time_the_code == 1
	    toc
        
	    % 
        tic
        disp('Now compute the size of the time step :')
    end

    
    % -----------------   Now, compute the time step. It is determined as follows.  -----------------------
    %    1) For each pedestrian, we compute the minimum distance between, on one hand, him and,
    %  on the other hand, other pedestrians and the boundaries. We then replace the so-found
    %  min distance by the maximum between it and ped_squeezable_length, so as to avoid using
    %  too small a distance for calculations of the time step.
    %  We compute two initial guesses for the time step,  dt_{repulsive,relmotion}_ij (and similar
    %  quantities for the pedestrian interacting with the boundaries), determined by how fast the
    %  pedestrian gets repelled from others and how fast he goes through the "min distance" found above.
    %  We then take the least of all these initial guesses for the time step.
    %  This is the end of the first step of time-step selection, producing aux_dt for the i-th pedestrian.
    %    2) We take the minimum between  aux_dt  and the reaction time ped_tau_reaction(i), and divide it by 4
    %  (this is a rather arbitrary factor; it could have as well been 2, 3, or 10, etc.).
    %  This is the end of step 2 of the time-step selection, producing  dt_i(i).
    %    3) We take the minimum of all dt_i's for all pedestrians.
    %  This is the end of the third step of the time-step selection, producing AUX_dt.
    %    4) Finally, note that the time step so computed may be modified below if pedestrians need to be reshuffled.
    %  The explanation of how this new  dt  is computed (for each individual ped) is found after the reshuffling procedure.
    %
    %  NOTE: One reason (there probably were more, but I didn't record them at the time of writing this part of the code
    %        and hence don't remember) for computing the time step  dt  before reshuffling the pedestrians is that the
    %        former calculation uses the values of  aux_index_ij etc, determined above, while the reshuffling
    %        process modifies them. Of course, this problem could be eliminated by certain renaming of the variables
    %        in question, but this is a tedious thing to do.
    % ---->  A CONSEQUENCE of the fact that the  dt  calculation is done before the reshuffling is that
    %        the peds may be squashed against boundaries and each others beyond their minimal boundaries, i.e.
    %        e.g.,  ped_dist_from_wall-ped_radius_min  can be negative. However, this doesn't seem to affect the
    %        calculations.
  
    dt_i=zeros(1,N_peds);                       % prepare a clean array used below in step 3 
           
    if max(aux_index_ped_inside) == 0           % if there are no peds in the room
        dt_before_reshuffling=dT_save;
    else                                        % i.e. if there is at least one ped in the room
        for i=1:N_peds
            % NOTE that this calculation is performed only for the peds who are inside the room, because arrays
            %      aux_index_ij etc. have 0 entries for peds who are outside the room.
            % Implement first step described above:
            % (a) Consider distances of a given pedestrian from each other:
            if sum(aux_index_ij(i,:)) > 0
                [dist_ij_min,j_closest_to_i]=min(r_ij(i,aux_index_ij(i,:))-d_min_ij(i,aux_index_ij(i,:)));
                                                % min distance between a given pedestrian and his nearest neighbor  [m],
                                                %  and the latter's index
                aux_vx_ij=vx_ij(i,aux_index_ij(i,:));
                aux_vy_ij=vy_ij(i,aux_index_ij(i,:));
                aux_nx_ij=nx_ij(i,aux_index_ij(i,:));
                aux_ny_ij=ny_ij(i,aux_index_ij(i,:));
                                                % these are relative velocities and direction vector components, restricted
                                                %  only to peds who are sufficiently close to the i-th pedestrian
                v_ij_projection=abs(aux_vx_ij(j_closest_to_i)*aux_nx_ij(j_closest_to_i)+...
                                    aux_vy_ij(j_closest_to_i)*aux_ny_ij(j_closest_to_i))+eps;
                                                % the projection of the relative velocity of this pair of pedestrians on
                                                %  the vector connecting them  [m/s]
                if dist_ij_min < 2*min(d_ij(i,aux_index_ij(i,:))-d_min_ij(i,aux_index_ij(i,:)))
                    dt_repulsive_ij=sqrt(ped_squeezable_length(i)*ped_mass(i)/...
                                    (ped_elasticity*ped_squeezable_length(i)+max(Fabs_ij(i,aux_index_ij(i,:)))));
                                                % this is the idea of the time step determined by the (combined) 
                                                %  repulsive force acting between the pedestrians  [s]
                    dt_relmotion_ij=ped_squeezable_length(i)/v_ij_projection;
                                                % this is the idea of the time step determined by the relative 
                                                %  motion of the closest pair of pedestrians  [s]
                else 
                    dt_repulsive_ij=sqrt(dist_ij_min*ped_mass(i)/(max(Fabs_ij(i,aux_index_ij(i,:)))+EPS));
                    dt_relmotion_ij=dist_ij_min/v_ij_projection;
                end
            else
                dt_repulsive_ij=sqrt(min((BwallRight(y_ped(i))-B_wall_xmin),(B_wall_ymax-B_wall_ymin))/2*ped_mass(i)/...
                                ped_rep_close_max(i));
                dt_relmotion_ij=min((BwallRight(y_ped(i))-B_wall_xmin)/(abs(vx_ped(i))+EPS),...
                                    (B_wall_ymax-B_wall_ymin)/(abs(vy_ped(i))+EPS))/2;
            end
                                                % NOTE: Quantities analogous to the  dt_{repulsive,relmotion}_ij
                                                %       are also calculated below for pedestrians and the boundaries.
                                                %       Since detailed explanations of the steps involved are given
                                                %       just above, I will NOT repeat these explanations for
                                                %       the similar calculations done below.
        
            % (b) Consider distances of a given pedestrian from columns:
            if sum(aux_index_ib_column(i,:)) > 0
                [dist_ib_column_min,col_closest_to_i]=min(r_ib_column(i,aux_index_ib_column(i,:))-...
                                                          pedCol_radius_min(i,aux_index_ib_column(i,:)));
                aux_nx_ib_column=nx_ib_column(i,aux_index_ib_column(i,:));
                aux_ny_ib_column=ny_ib_column(i,aux_index_ib_column(i,:));
                                                % components of the direction vectors between peds and columns,
                                                %  restricted only to the columns which are sufficiently close to
                                                %  the i-th pedestrian
                v_ib_column_projection=abs(vx_ped(i)*aux_nx_ib_column(col_closest_to_i)+...
                                           vy_ped(i)*aux_ny_ib_column(col_closest_to_i))+eps;
                if dist_ib_column_min < 2*min(pedCol_squeezable_length(i,aux_index_ib_column(i,:)))
                    dt_repulsive_ib_column=sqrt(ped_squeezable_length(i)*ped_mass(i)/...
                                           (ped_elasticity*ped_squeezable_length(i)+...
                                           max(Fabs_ib_column(i,aux_index_ib_column(i,:)))));
                    dt_relmotion_ib_column=ped_squeezable_length(i)/v_ib_column_projection;
                else 
                    dt_repulsive_ib_column=sqrt(dist_ib_column_min*ped_mass(i)/...
                                               (max(Fabs_ib_column(i,aux_index_ib_column(i,:)))+EPS));
                    dt_relmotion_ib_column=dist_ib_column_min/v_ib_column_projection;
                end
            else                                % (i.e. the i-th pedestrian has no sufficiently close-by neighbors)
                dt_repulsive_ib_column=sqrt(min((BwallRight(y_ped(i))-B_wall_xmin),(B_wall_ymax-B_wall_ymin))/2*ped_mass(i)/...
                                            ped_rep_close_max(i));
                dt_relmotion_ib_column=min((BwallRight(y_ped(i))-B_wall_xmin)/(abs(vx_ped(i))+EPS),...
                                           (B_wall_ymax-B_wall_ymin)/(abs(vy_ped(i))+EPS))/2;
            end
            
            % (c) Consider distances of a given pedestrian from walls:
            if aux_index_ib_left(i) > 0
                dist_ib_left_min=ped_dist_from_left(i)-ped_radius_min(i);
                if dist_ib_left_min < 2*ped_squeezable_length(i)
                    dt_repulsive_ib_left=sqrt(ped_squeezable_length(i)*ped_mass(i)/...
                                         (ped_elasticity*ped_squeezable_length(i)+Fabs_ib_wall_left(i)));
                    dt_relmotion_ib_left=ped_squeezable_length(i)/(abs(vx_ped(i))+EPS);
                else
                    dt_repulsive_ib_left=sqrt(dist_ib_left_min*ped_mass(i)/(Fabs_ib_wall_left(i)+EPS));
                    dt_relmotion_ib_left=dist_ib_left_min/(abs(vx_ped(i))+EPS);
                end
            else
                dt_repulsive_ib_left=sqrt((BwallRight(y_ped(i))-B_wall_xmin)/2*ped_mass(i)/ped_rep_close_max(i));
                dt_relmotion_ib_left=(BwallRight(y_ped(i))-B_wall_xmin)/2/(abs(vx_ped(i))+EPS);
            end
            
            if aux_index_ib_right(i) > 0
                dist_ib_right_min=ped_dist_from_right(i)-ped_radius_min(i);
                if dist_ib_right_min < 2*ped_squeezable_length(i)
                    dt_repulsive_ib_right=sqrt(ped_squeezable_length(i)*ped_mass(i)/...
                                          (ped_elasticity*ped_squeezable_length(i)+Fabs_ib_wall_right(i)));
                    dt_relmotion_ib_right=ped_squeezable_length(i)/(abs(vx_ped(i))+EPS);
                else
                    dt_repulsive_ib_right=sqrt(dist_ib_right_min*ped_mass(i)/(Fabs_ib_wall_right(i)+EPS));
                    dt_relmotion_ib_right=dist_ib_right_min/(abs(vx_ped(i))+EPS);
                end
            else
                dt_repulsive_ib_right=sqrt((BwallRight(y_ped(i))-B_wall_xmin)/2*ped_mass(i)/ped_rep_close_max(i));
                dt_relmotion_ib_right=(BwallRight(y_ped(i))-B_wall_xmin)/2/(abs(vx_ped(i))+EPS);
            end
            
            if aux_index_ib_lower(i) > 0
                dist_ib_lower_min=ped_dist_from_lower(i)-ped_radius_min(i);
                if dist_ib_lower_min < 2*ped_squeezable_length(i)
                    dt_repulsive_ib_lower=sqrt(ped_squeezable_length(i)*ped_mass(i)/...
                                          (ped_elasticity*ped_squeezable_length(i)+Fabs_ib_wall_lower(i)));
                    dt_relmotion_ib_lower=ped_squeezable_length(i)/(abs(vy_ped(i))+EPS);
                else
                    dt_repulsive_ib_lower=sqrt(dist_ib_lower_min*ped_mass(i)/(Fabs_ib_wall_lower(i)+EPS));
                    dt_relmotion_ib_lower=dist_ib_lower_min/(abs(vy_ped(i))+EPS);
                end
            else
                dt_repulsive_ib_lower=sqrt((B_wall_ymax-B_wall_ymin)/2*ped_mass(i)/ped_rep_close_max(i));
                dt_relmotion_ib_lower=(B_wall_ymax-B_wall_ymin)/2/(abs(vy_ped(i))+EPS);
            end
            
            if aux_index_ib_upper(i) > 0
                dist_ib_upper_min=ped_dist_from_upper(i)-ped_radius_min(i);
                if dist_ib_upper_min < 2*ped_squeezable_length(i)
                    dt_repulsive_ib_upper=sqrt(ped_squeezable_length(i)*ped_mass(i)/...
                                          (ped_elasticity*ped_squeezable_length(i)+Fabs_ib_wall_upper(i)));
                    dt_relmotion_ib_upper=ped_squeezable_length(i)/(abs(vy_ped(i))+EPS);
                else
                    dt_repulsive_ib_upper=sqrt(dist_ib_upper_min*ped_mass(i)/(Fabs_ib_wall_upper(i)+EPS));
                    dt_relmotion_ib_upper=dist_ib_upper_min/(abs(vy_ped(i))+EPS);
                end
            else
                dt_repulsive_ib_upper=sqrt((B_wall_ymax-B_wall_ymin)/2*ped_mass(i)/ped_rep_close_max(i));
                dt_relmotion_ib_upper=(B_wall_ymax-B_wall_ymin)/2/(abs(vy_ped(i))+EPS);
            end
            aux_dt=min([dt_repulsive_ij dt_relmotion_ij dt_repulsive_ib_column dt_relmotion_ib_column ...
                        dt_repulsive_ib_left dt_relmotion_ib_left dt_repulsive_ib_right dt_relmotion_ib_right ...
                        dt_repulsive_ib_lower dt_relmotion_ib_lower dt_repulsive_ib_upper dt_relmotion_ib_upper]);
                        
            if imag(aux_dt) ~= 0 | aux_dt < 0
                i
                disp('Warning: dt_before_reshuffling is complex or negative !')
                this_is_an_undefined_variable
                pause
            end
                    
            % Implement the 2nd step described above:
            dt_i(i)=min(aux_dt,ped_tau_reaction(i))/2^3;
        end
       
        % Implement the 3rd step described above:
        dt=min(dt_i);                           % try this value for the step size in time  [s]
        
    end                                         % end of loop deciding if there are peds in the room
            
    
    if time_the_code == 1
        toc
	    % %
        
	    disp('%   --------   Reshuffle the pedestrians in such a way that no pedestrians overlap  --------------') 
        disp('%   -----------            with each other  or  the boundaries:                     --------------')
	    tic
    end
    
	%
    % EXPLANATION OF THE RESHUFFLING PROCEDURE:
    %    1)  We find the most squashed pedestrian and determine whether he is squashed the most against
    %  another pedestrian or against a boundary. By "squashed", we mean that he has been squeezed as much as he
    %  was allowed to (as determined by the parameter  ped_squeezability  at the beginning of this code) and,
    %  on top of that, has begun to penetrate the boundary or the minimal radius of another pedestrian.
    %    2)  If he is squashed the most against another pedestrian, we verify whether he is also squashed
    %  (a little less) against a boundary. 
    %    2a) If he is, we move him away from that boundary first.
    %  VERY IMPORTANT NOTE: He has to be moved away so that he is no longer squashed against that boundary and
    %                       is actually at a small positive distance away from it. For example, if his minimal
    %                       radius is  ped_radius_min  and his distance from a given wall is  
    %                       ped_dist_from_wall < ped_radius_min, then he has to be moved away from his current
    %                       location by  [(ped_radius_min-ped_dist_from_wall)+"small POSITIVE number"].
    %                       If this  "small positive number"  is not added, then at the next step the pedestrian
    %                       will still be treated as squashed (because in the reshuffling process his velocity
    %                       normal to the boundary is set to zero and his distance from the boundary won't change),
    %                       with the end result being that he gets "GLUED" to the wall! 
    %  As mentioned above, upon collision with a wall or column, the pedestrian's normal velocity towards that
    %  boundary is set to zero, and its remaining tangential velocity is determined by the factor
    %  ped_b_impact_inelasticity,  which is defined at the beginning of the code.
    %  After we move the pedestrian away from the boundary, we "stone" (as defined below) him and then move his 
    %  squashed neighbors away from him just enough (+ slightly more) so as to eliminate the squashing. 
    %    2b) If the pedestrian is only squashed against another pedestrian, we determine who of the two is squashed
    %  the most with others, then "stone" the most squashed one and move the other one so as to eliminate
    %  squashing, similarly to the end of step 2a above. The velocities of these two pedestrians are set to coincide.
    %  Note, however, that this will not lead to their "glueing" to each other, since their new distance is such
    %  that they are no longer squashed and hence will have a chance to move independently at the next time step.
    %    3)  We then exclude this "stoned" pedestrian from consideration and repeat the entire process with the
    %  pedestrians who have not been squashed so far, until we either eliminate all squashings or go through all
    %  pedestrians, whichever happens sooner.
    %
    
    % Pre-allocate arrays necessary at the following step
    %  (NOTE: notation "rbmb" stands for "distance (i.e., r) between minimal boundaries") :
    rbmb_i_others=zeros(N_peds);                % "rbmb" between pairs of pedestrians  [m]
    rbmb_i_column=zeros(N_peds,N_columns);      % "rbmb" between pedestrians and columns  [m]
    % Next 4 are "rbmb" between pedestrians and walls  [m]:
    rbmb_i_left=zeros(1,N_peds);
    rbmb_i_right=zeros(1,N_peds);
    rbmb_i_lower=zeros(1,N_peds);
    rbmb_i_upper=zeros(1,N_peds);
    rbmb_i_stoned=zeros(N_peds);                % "rbmb" between pairs of movable and "stoned" pedestrians  [m]
                                                % NOTE: Pedestrians are called "stoned" once their new positions have been
                                                %       already determined during the reshuffling process and who, therefore,
                                                %       are considered immobile and thus are treated as effective boundaries.
    rbmb_i_close=zeros(N_peds,4+N_columns+2*N_peds);
                                                % negative entries of this matrix determine those pedestrians who are
                                                %  squashed against each other or boundaries, where the latter include both
                                                %  the real boundaries (walls and columns) and the "stoned" pedestrians
                                                %  (hence the factor "2" in front of the N_peds in the # of columns)
    rbmb_i_min=zeros(1,N_peds);                 % min distance between each given pedestrians and peds and boundaries he
                                                %  is squashed against  [m]
    x_stoned=zeros(1,N_peds);
    y_stoned=zeros(1,N_peds);                   % x- and y-coordinates of the "stoned" pedestrians  [m]
    x_ib_stoned=zeros(N_peds);
    y_ib_stoned=zeros(N_peds);
    r_ib_stoned=zeros(N_peds);
    nx_ib_stoned=zeros(N_peds);
    ny_ib_stoned=zeros(N_peds);                 % corrsponding quantities characterizing distances and directions among
                                                %  pedestrians who can still be moved and the "stoned" ones
    
    aux_index_squashed=logical(zeros(N_peds,4+N_columns+2*N_peds));
                                                % matrix of indices of peds and objects that a given ped is squashed against
    N_peds_stoned=0;                            % number of "stoned" pedestrians 
    indices_of_stoned_peds=logical(zeros(1,N_peds));
                                                % array of indices of pedestrians who have been "stoned"
    clear   delta_t_of_this_reshuffling_step  cluster_micro
                                                % these are auxiliary arrays used to determine the amount of time needed
                                                %  to perform one the entire reshffling procedure at this time step
                                                %  (terminologically, note that the reshuffling procedure at one time step
                                                %   may (and normally does) consist of several reshuffling steps
                                                
    aux_index_ij_b4_reshuffle=aux_index_ij;     % save the value of this index array before ("b4") this reshuffling
                                                %  step, because it wil later be used compute relative positions
                                                %  of pedestrians who have been "stoned" and those who can still be moved
                                                

    % Calculate distances between "minimal boundaries" of pedestrians who are sufficiently close to each other:
    rbmb_i_others(aux_index_ij)=r_ij(aux_index_ij)-d_min_ij(aux_index_ij);
                                                % distance between "minimal boundaries" of any pair of pedestrians  [m]
                                                %  (if it is negative, then they have overlapped)
    rbmb_i_column(aux_index_ib_column)=r_ib_column(aux_index_ib_column)-pedCol_radius_min(aux_index_ib_column);
    rbmb_i_left(aux_index_ib_left)=ped_dist_from_left(aux_index_ib_left)-ped_radius_min(aux_index_ib_left);
    rbmb_i_right(aux_index_ib_right)=ped_dist_from_right(aux_index_ib_right)-ped_radius_min(aux_index_ib_right);
    rbmb_i_lower(aux_index_ib_lower)=ped_dist_from_lower(aux_index_ib_lower)-ped_radius_min(aux_index_ib_lower);
    rbmb_i_upper(aux_index_ib_upper)=ped_dist_from_upper(aux_index_ib_upper)-ped_radius_min(aux_index_ib_upper);
                                                % distances between min boundaries of pedestrians and walls  [m]
    for i=1:N_peds
        rbmb_i_close(i,:)=[rbmb_i_others(i,:) rbmb_i_left(i) rbmb_i_right(i) rbmb_i_lower(i) rbmb_i_upper(i) ...
                             rbmb_i_column(i,:) rbmb_i_stoned(i,:)];
                                                % see the explaination of this matrix given above, when it is initialized;
                                                % Note that at this first step, all rbmb_i_stoned are zeros.
        aux_index_squashed(i,:)=rbmb_i_close(i,:) < 0;
        % This loop determines the minimum distances existing between "minimal boundaries" of peds and boundaries 
        %  of other objects in the room:
        if sum(aux_index_squashed(i,:)) > 0     % if i-th pedestrian is squashed against other pedestrians or boundaries
            rbmb_i_min(i)=min(rbmb_i_close(i,aux_index_squashed(i,:)));
        end                                 
    end
    [rbmb_i1_min,i1_min]=min(rbmb_i_min);       % determine the pedestrian who is squashed the most, and his sequential index
    
    % Run a loop which will determine all squashed pedestrians and reshuffle them so as to eleiminate their overlap
    %  with each other and with the boundaries (i.e. walls, columns, and "stoned" pedestrians):
    counter_of_reshuffling=0;                   % this counter is used if we want to know how many times reshuffling
                                                %  has been performed
    
    while rbmb_i1_min < 0 & sum(indices_of_stoned_peds) < N_peds-1
                                                % this verifies if there are any pedestrians who need to be reshuffled 
       counter_of_reshuffling=counter_of_reshuffling+1;
                                                % update the counter of number of reshufflings performed
       % sum(indices_of_stoned_peds);
       
        rbmb_i_min_mod=[rbmb_i_min(1:i1_min-1) 100 rbmb_i_min(i1_min+1:end)];
                                                % modified vector of minimal distances, created in order to find the
                                                %  "second most squashed" pedestrian
        [rbmb_i2_min,i2_min]=min(rbmb_i_min_mod);
                                                % determine the pedestrian who is "second most squashed", and his index
                                                
        if rbmb_i1_min > rbmb_i2_min-2*eps      % i.e. if some 2 pedestrians are squashed against each other the most,
                                                %  as opposed to just one pedestrian being most squashed against a boundary
                                                %  (Indeed, in the former case, there are 2 indices, the above  i1  and  i2,
                                                %   for which the corresponding pedestrians have the same rbmb_i_min;
                                                %   but in the latter case, there is only one rbmb_i_min, namely, that
                                                %   corresponding to  i1, which is less than the next-least rbmb_i_min.)
            % Calculate distance from i1-th pedestrian to its neighbor who is "second closest" after i2-th pedestrian,
            %  and similar for the i2-th pedestrian:
            rbmb_i1_nextnearest=min([rbmb_i_close(i1_min,1:i2_min-1) rbmb_i_close(i1_min,i2_min+1:end)]);
            rbmb_i2_nextnearest=min([rbmb_i_close(i2_min,1:i1_min-1) rbmb_i_close(i2_min,i1_min+1:end)]);
            % Now move that pedestrian whose next-nearest neighbor is farther away:
            if rbmb_i1_nextnearest <= rbmb_i2_nextnearest
                                                % is the next-nearest neighbor to i1-th pedestrian is closer to him than
                                                %  the next-nearest neighbor of the i2-th pedestrian is closer to the latter
                i_mostsquashed=i1_min;
            else
                i_mostsquashed=i2_min;
            end
            
            % Check if the most-squashed pedestrian is also squashed against any boundary
            %  (which may be a wall, a column, or a pedestrian moved at a previous iteration of the reshuffling process).
            %  In this case, first, move the most-squashed pedestrian away from the boundary, 
            %  and, second, move all pedestrains overlapping with him away from his new location.
            %  If he is not squashed against any boundary, then just move others, overlapping with him, away from him.
            delta_x_ped=0;
            delta_y_ped=0;                      % initiation of the distances in x- and y-directions by which the pedestrian
                                                %  in question is to be moved away from the boundaries; if he has to be moved,
                                                %  then these values are reset in the next loop, and if he doesn't, then these
                                                %  distances remain zero
            delta_t_move_ped_away_from_b=0;     % initiation of the amount of time needed to perform the part of reshuffling 
                                                %  described in the loop immediately below;  if the code doesn't go inside
                                                %  that loop, the amount remains zero, and if the code does go inside the loop,
                                                %  that amount is reset at the end of that loop
            if min(rbmb_i_close(i_mostsquashed,N_peds+1:end)) < 0
                aux_index_mostsquashed_b=rbmb_i_close(i_mostsquashed,N_peds+1:end)<0;
                                                % indices of the boundaries against which the most-squashed pedestrian is 
                                                %  also squashed
                aux_rand_1=0.1*(rand(1,N_columns)-0.5);
                aux_rand_2=0.1*(rand(1,N_peds)-0.5);
                                                %  The small random terms (proportional to  aux_rand_1,2) are included in this
                                                %  reshuffling step so as to move the pedestrian NOT DIRECTLY away from columns
                                                %  and already-stoned pedestrians, but slightly to the side in a random 
                                                %  direction.
                                                %  NOTE that the factor "0.1" in the definition of  aux_rand  is arbitrary
                                                %       and can be replaced by any small number.
                aux1_incr_dist=(1+ped_squeezability/3);
                aux2_incr_dist=(1+ped_squeezability/3);
                                                % these are parameters by which guarantee that, after the reshuffling,
                                                %  the distances between "minimal boundaries" of pedestrians and
                                                %  obstacles are slightly positive as opposed to being zero;
                                                %  the 1st parameter is used for pedestrians squashed against walls,
                                                %  and the 2nd, for pedestrian squashed against columns and "stoned"
                                                %  pedestrians (it is smaller because in the latter case we deal with
                                                %  a sum of two radii, and plus, this parameter can actually be even set
                                                %  to 1, because we move the pedestrians relative to each other and the
                                                %  columns a little to the side of the direction connecting their centers --
                                                %  see explanations for the parameters  aux_rand_{1,2}  above);
                                                %  note also that the divisors 3 and 3 above are rather arbitrary
                delta_x_ped=aux_index_mostsquashed_b(1)*...
                            (aux1_incr_dist*ped_radius_min(i_mostsquashed)-ped_dist_from_left(i_mostsquashed))+...
                            aux_index_mostsquashed_b(2)*...
                            (-aux1_incr_dist*ped_radius_min(i_mostsquashed)+ped_dist_from_right(i_mostsquashed))+...
                            sum(aux_index_mostsquashed_b(5:4+N_columns).*...
                               (-(pedCol_radius_min(i_mostsquashed,:)+...
                                  (aux2_incr_dist-1)*pedCol_squeezable_length(i_mostsquashed,:))+...
                                r_ib_column(i_mostsquashed,:)).*...
                               (nx_ib_column(i_mostsquashed,:)+aux_rand_1.*ny_ib_column(i_mostsquashed,:)))+...
                            sum(aux2_incr_dist*rbmb_i_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end)).*...
                               (nx_ib_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end))+...
                               aux_rand_2(aux_index_mostsquashed_b(5+N_columns:end)).*...
                               ny_ib_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end))));
                delta_y_ped=aux_index_mostsquashed_b(3)*...
                            (aux1_incr_dist*ped_radius_min(i_mostsquashed)-ped_dist_from_lower(i_mostsquashed))+...
                            aux_index_mostsquashed_b(4)*...
                            (-aux1_incr_dist*ped_radius_min(i_mostsquashed)+ped_dist_from_upper(i_mostsquashed))+...
                            sum(aux_index_mostsquashed_b(5:4+N_columns).*...
                               (-(pedCol_radius_min(i_mostsquashed,:)+...
                                  (aux2_incr_dist-1)*pedCol_squeezable_length(i_mostsquashed,:))+...
                                r_ib_column(i_mostsquashed,:)).*...
                               (ny_ib_column(i_mostsquashed,:)-aux_rand_1.*nx_ib_column(i_mostsquashed,:)))+...
                            sum(aux2_incr_dist*rbmb_i_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end)).*...
                               (ny_ib_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end))-...
                               aux_rand_2(aux_index_mostsquashed_b(5+N_columns:end)).*...
                               nx_ib_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end))));
                x_ped(i_mostsquashed)=x_ped(i_mostsquashed)+delta_x_ped;
                y_ped(i_mostsquashed)=y_ped(i_mostsquashed)+delta_y_ped;
                                                %  these are the new x- and y-coordinates the most-squashed pedestrian who is
                                                %   squashed against boundaries; unless the space is packed toooo tightly,
                                                %   this step should be able to move the pedestrian away from those boundaries
                vx_ped_mostsquashed_old=vx_ped(i_mostsquashed);
                vy_ped_mostsquashed_old=vy_ped(i_mostsquashed);
                                                % record the velocity of the mostsquashed ped before he is resheffled;
                                                %  this velocity is used below for the computation of the amount of time
                                                %  needed to perform the reshuffling
                % After having stopped the pedestrian, rotate him so that his new direction of motion (determined by
                %  the vector [ex_ped,ey_ped]) becomes tangential to the boundary that he is squashed against. 
                %  His tangential velocity w.r.t. that boundary has the same direction as before the impact,
                %  and its magnitude is reduced by the factor  ped_b_impact_inelasticity, 
                %  which was defined at the beginning of this code.
                %  In the case when the pedestrian is in a corner between two walls, orient him along the corner's
                %  bisector pointing out of the corner, and set his velocity to zero.
                aux_direction=0;                % a marker used (only) in the loop below
                if aux_index_mostsquashed_b(1) == 1   
                                                % i.e. if the pedestrian is squashed against the left wall
                    if aux_index_mostsquashed_b(3)+aux_index_mostsquashed_b(4) == 0
                                                % and if he is squashed only against the left wall
                        vx_ped(i_mostsquashed)=0;
                        vy_ped(i_mostsquashed)=vy_ped(i_mostsquashed)*(1-ped_b_impact_inelasticity);
                        ey_ped(i_mostsquashed)=sign(vy_ped(i_mostsquashed));
                        ex_ped(i_mostsquashed)=sqrt(1-(ey_ped(i_mostsquashed))^2);
                    elseif aux_index_mostsquashed_b(3) == 1
                                                % i.e. if he is in the lower left corner
                        vx_ped(i_mostsquashed)=0;
                        vy_ped(i_mostsquashed)=0;
                        ex_ped(i_mostsquashed)=1/sqrt(2);
                        ey_ped(i_mostsquashed)=1/sqrt(2);
                    elseif aux_index_mostsquashed_b(4) == 1
                                                % i.e. if he is in the upper left corner
                        vx_ped(i_mostsquashed)=0;
                        vy_ped(i_mostsquashed)=0;
                        ex_ped(i_mostsquashed)=1/sqrt(2);
                        ey_ped(i_mostsquashed)=-1/sqrt(2);
                    end
                    aux_direction=1;
                elseif aux_index_mostsquashed_b(2) == 1
                                                % i.e. if the pedestrian is squashed against the right wall
                    if aux_index_mostsquashed_b(3)+aux_index_mostsquashed_b(4) == 0
                                                % and if he is squashed only against the right wall
                        vx_ped(i_mostsquashed)=0;
                        vy_ped(i_mostsquashed)=vy_ped(i_mostsquashed)*(1-ped_b_impact_inelasticity);
                        ey_ped(i_mostsquashed)=sign(vy_ped(i_mostsquashed));
                        ex_ped(i_mostsquashed)=-sqrt(1-(ey_ped(i_mostsquashed))^2);
                    elseif aux_index_mostsquashed_b(3) == 1
                                                % i.e. if he is in the lower right corner
                        vx_ped(i_mostsquashed)=0;
                        vy_ped(i_mostsquashed)=0;
                        ex_ped(i_mostsquashed)=-1/sqrt(2);
                        ey_ped(i_mostsquashed)=1/sqrt(2);
                    elseif aux_index_mostsquashed_b(4) == 1
                                                % i.e. if he is in the upper right corner
                        vx_ped(i_mostsquashed)=0;
                        vy_ped(i_mostsquashed)=0;
                        ex_ped(i_mostsquashed)=-1/sqrt(2);
                        ey_ped(i_mostsquashed)=-1/sqrt(2);
                    end
                    aux_direction=1;
                elseif aux_index_mostsquashed_b(3) == 1
                                                % i.e. if the pedestrian is squashed against the lower wall
                    if aux_direction == 0       % i.e. re-orient the pedetrian only if this has not been done
                                                %  immediately above in the case when he is squashed in a corner
                        vx_ped(i_mostsquashed)=vx_ped(i_mostsquashed)*(1-ped_b_impact_inelasticity);
                        vy_ped(i_mostsquashed)=0;
                        ex_ped(i_mostsquashed)=sign(vx_ped(i_mostsquashed));
                        ey_ped(i_mostsquashed)=sqrt(1-(ex_ped(i_mostsquashed))^2);
                    end
                elseif aux_index_mostsquashed_b(4) == 1
                                                % i.e. if the pedestrian is squashed against the upper wall
                    if aux_direction == 0       % i.e. re-orient the pedetrian only if this has not been done
                                                %  immediately above in the case when he is squashed in a corner
                        vx_ped(i_mostsquashed)=vx_ped(i_mostsquashed)*(1-ped_b_impact_inelasticity);
                        vy_ped(i_mostsquashed)=0;
                        ex_ped(i_mostsquashed)=sign(vx_ped(i_mostsquashed));
                        ey_ped(i_mostsquashed)=-sqrt(1-(ex_ped(i_mostsquashed))^2);
                    end
                elseif max(aux_index_mostsquashed_b(5:4+N_columns)) == 1
                                                % i.e. if the pedestrian is squashed against a column
                                                %  (note that he can be squashed only against one column, since no two
                                                %  columns are so close that the same person can be simultaneously
                                                %  squashed between them; this means that in the sum below, only
                                                %  one term, corresponding to the closest column, is nonzero)
                    aux_inner_product=sum(aux_index_mostsquashed_b(5:4+N_columns).*...
                                          (vx_ped(i_mostsquashed)*nx_ib_column(i_mostsquashed,:)+...
                                          vy_ped(i_mostsquashed)*ny_ib_column(i_mostsquashed,:)));
                    % The ped retains his tangential velocity relative to the column,
                    %  but his normal velocity vanishes:
                    vx_ped(i_mostsquashed)=vx_ped(i_mostsquashed)-aux_inner_product*...
                                           sum(aux_index_mostsquashed_b(5:4+N_columns).*nx_ib_column(i_mostsquashed,:));
                    vy_ped(i_mostsquashed)=vy_ped(i_mostsquashed)-aux_inner_product*...
                                           sum(aux_index_mostsquashed_b(5:4+N_columns).*ny_ib_column(i_mostsquashed,:));
                    % The next step is done so as not to determine ex{y}_ped by dividing v{x,y}_ped by the total speed:
                    where2rotate=sum(aux_index_mostsquashed_b(5:4+N_columns).*...
                                     (vx_ped(i_mostsquashed)*ny_ib_column(i_mostsquashed,:)-...
                                     vy_ped(i_mostsquashed)*nx_ib_column(i_mostsquashed,:)));
                    if where2rotate > 0
                        ex_ped(i_mostsquashed)=sum(aux_index_mostsquashed_b(5:4+N_columns).*ny_ib_column(i_mostsquashed,:));
                        ey_ped(i_mostsquashed)=-sum(aux_index_mostsquashed_b(5:4+N_columns).*nx_ib_column(i_mostsquashed,:));
                    else
                        ex_ped(i_mostsquashed)=-sum(aux_index_mostsquashed_b(5:4+N_columns).*ny_ib_column(i_mostsquashed,:));
                        ey_ped(i_mostsquashed)=sum(aux_index_mostsquashed_b(5:4+N_columns).*nx_ib_column(i_mostsquashed,:));
                    end
                end                             % this is the end of the loop which sets the new velocity and re-orients
                                                %  the mostsquashed pedestrian
                %
                % Now compute the time that is needed to move that ped away from the boundary(ies). 
                %  This time is computed as follows:   
                %  delta_t_move_ped_away_from_b=(change of normal component of momentum)/(normal force).
                %  (Note that the normal component of the momentum does indeed change since the ped gets stopped.)
                delta_t_move_ped_away_from_b=max([ ...
                                              aux_index_mostsquashed_b(1)*max(-vx_ped_mostsquashed_old,0)/Ncontact_max,...
                                              aux_index_mostsquashed_b(2)*max(vx_ped_mostsquashed_old,0)/Ncontact_max,...
                                              aux_index_mostsquashed_b(3)*max(-vy_ped_mostsquashed_old,0)/Ncontact_max,...
                                              aux_index_mostsquashed_b(4)*max(vy_ped_mostsquashed_old,0)/Ncontact_max,...
                                              aux_index_mostsquashed_b(5:N_columns+4).*...
                                               max(vx_ped_mostsquashed_old*nx_ib_column(i_mostsquashed,:)+...
                                                   vy_ped_mostsquashed_old*ny_ib_column(i_mostsquashed,:),0)./Ncontact_max,...
                                              max(vx_ij(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end)).*...
                                                   nx_ib_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end))+...
                                                  vy_ij(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end)).*...
                                                   ny_ib_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end)),0)./...
                                               Ncontact_max...
                                                  ])*ped_mass(i_mostsquashed);
                
            end                                 % this is the end of the loop that checked whether the pedestrian is squashed
                                                %  against a boundary in addition to being squashed against another pedestrian
            
            % Now, as noted above, move pedestrians overlapping with the most-squashed one away from him:
            aux_index_mostsquashed_j=aux_index_squashed(i_mostsquashed,1:N_peds);
                                                % get indices of pedestriand squashed against the most-squashed one
            aux_rand=0.1*(rand(1,sum(aux_index_mostsquashed_j))-0.5);
                                                % the purpose of this random vector is the same of that of aux_rand_1,2,
                                                %  defined when the most-squashed pedestrian is moved away from the boundaries
            x_ped(aux_index_mostsquashed_j)=x_ped(i_mostsquashed)+d_min_ij(i_mostsquashed,aux_index_mostsquashed_j).*...
                                           (nx_ij(i_mostsquashed,aux_index_mostsquashed_j)+...
                                            aux_rand.*ny_ij(i_mostsquashed,aux_index_mostsquashed_j));
            y_ped(aux_index_mostsquashed_j)=y_ped(i_mostsquashed)+d_min_ij(i_mostsquashed,aux_index_mostsquashed_j).*...
                                           (ny_ij(i_mostsquashed,aux_index_mostsquashed_j)-...
                                            aux_rand.*nx_ij(i_mostsquashed,aux_index_mostsquashed_j));
                                                % these are updated positions of those pedestrians who are squashed against
                                                %  the most-squashed pedestrian; they are moved so as to eliminate the overlap 
                                                %  which has existed between them and him up to now.
            vx_ped(aux_index_mostsquashed_j)=vx_ped(i_mostsquashed);
            vy_ped(aux_index_mostsquashed_j)=vy_ped(i_mostsquashed);
                                                % set velocities of pedestrians overlapping with the most-squashed pedestrian equal
                                                %  to his velocity, so that they would not squash against him at the next step
            %
            % Now computed the amount of time needed to perform this reshuffling. The general form of the formula by which
            %  this amount of time is computed is as given above for   delta_t_move_ped_away_from_b.
            delta_t_move_peds_away_from_mostsquashed=max([ ...
                                                      max(-(vx_ij(i_mostsquashed,aux_index_mostsquashed_j).*...
                                                             nx_ij(i_mostsquashed,aux_index_mostsquashed_j)+...
                                                            vy_ij(i_mostsquashed,aux_index_mostsquashed_j).*...
                                                             ny_ib_stoned(i_mostsquashed,aux_index_mostsquashed_j)),0)./...
                                                       Ncontact_max.*ped_mass(aux_index_mostsquashed_j)...
                                                        ]);
            % Because of the  epsilon involved in the decision about whether a ped is squashed the most against
            %  another ped or a boundary, there is a slight chance that we can get into a "dead zone", where
            %  a ped is actually not squashed against any other people. In that case,  aux_index_mostsquashed_j  is
            %  all zeros and  delta_t_move_peds_away_from_mostsquashed=[]. I actually ran into such a case.
            %  To prevent the code from stalling in that situation, I implement the loop below:
            if sum(delta_t_move_peds_away_from_mostsquashed) > 0
                delta_t_of_this_reshuffling_step(counter_of_reshuffling)=delta_t_move_ped_away_from_b+...
                                                                         delta_t_move_peds_away_from_mostsquashed;
                                                % amount of time needed to perform this reshuffling step  [s]
            else
                delta_t_of_this_reshuffling_step(counter_of_reshuffling)=delta_t_move_ped_away_from_b;
                                                % amount of time needed to perform this reshuffling step  [s]
            end
                                            
            %
            % Now form a micro-CLUSTER of indices of peds who have had to be reshuffled at this step of the 
            %  reshuffling procedure:
            aux_index_mostsquashed_j(i_mostsquashed)=1;
            cluster_micro(counter_of_reshuffling,:)=aux_index_mostsquashed_j;
            
        elseif rbmb_i1_min <= rbmb_i2_min-2*eps
                                                % i.e. if the most-squashed pedestrian is squashed against a boundary 
                                                %  rather than against another pedestrian, just move him away from the
                                                %  boundaries, similarly to how I already did it above
            i_mostsquashed=i1_min;
            
            if min(rbmb_i_close(i_mostsquashed,N_peds+1:end)) < 0
                                                % this step is really just checking the consistency of the code:
                                                %  this condition MUST be satisfied if the code has come up to this point;
                                                %  if it is not (as I check below), then I have to stop and understand
                                                %  what has gone wrong
                aux_index_mostsquashed_b=rbmb_i_close(i_mostsquashed,N_peds+1:end)<0;
                                                % indices of the boundaries against which the most-squashed pedestrian is 
                                                %  also squashed
                aux_rand_1=0.1*(rand(1,N_columns)-0.5);
                aux_rand_2=0.1*(rand(1,N_peds)-0.5);
                                                %  The small random terms (proportional to  aux_rand_1,2) are included in this
                                                %  reshuffling step so as to move the pedestrian NOT DIRECTLY away from columns
                                                %  and already-stoned pedestrians, but slightly to the side in a random 
                                                %  direction.
                                                %  NOTE that the factor "0.1" in the definition of  aux_rand  is arbitrary
                                                %       and can be replaced by any small number.
                aux1_incr_dist=(1+ped_squeezability/3);
                aux2_incr_dist=(1+ped_squeezability/3);
                                                % these are parameters by which guarantee that, after the reshuffling,
                                                %  the distances between "minimal boundaries" of pedestrians and
                                                %  obstacles are slightly positive as opposed to being zero;
                                                %  the 1st parameter is used for pedestrians squashed against walls,
                                                %  and the 2nd, for pedestrian squashed against columns and "stoned"
                                                %  pedestrians (it is smaller because in the latter case we deal with
                                                %  a sum of two radii, and plus, this parameter can actually be even set
                                                %  to 1, because we move the pedestrians relative to each other and the
                                                %  columns a little to the side of the direction connecting their centers --
                                                %  see explanations for the parameters  aux_rand_{1,2}  above)
                                                %  note also that the divisors 3 and 3 above are rather arbitrary
                delta_x_ped=aux_index_mostsquashed_b(1)*...
                            (aux1_incr_dist*ped_radius_min(i_mostsquashed)-ped_dist_from_left(i_mostsquashed))+...
                            aux_index_mostsquashed_b(2)*...
                            (-aux1_incr_dist*ped_radius_min(i_mostsquashed)+ped_dist_from_right(i_mostsquashed))+...
                            sum(aux_index_mostsquashed_b(5:4+N_columns).*...
                               (-(pedCol_radius_min(i_mostsquashed,:)+...
                                  (aux2_incr_dist-1)*pedCol_squeezable_length(i_mostsquashed,:))+...
                                r_ib_column(i_mostsquashed,:)).*...
                               (nx_ib_column(i_mostsquashed,:)+aux_rand_1.*ny_ib_column(i_mostsquashed,:)))+...
                            sum(aux2_incr_dist*rbmb_i_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end)).*...
                               (nx_ib_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end))+...
                               aux_rand_2(aux_index_mostsquashed_b(5+N_columns:end)).*...
                               ny_ib_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end))));
                delta_y_ped=aux_index_mostsquashed_b(3)*...
                            (aux1_incr_dist*ped_radius_min(i_mostsquashed)-ped_dist_from_lower(i_mostsquashed))+...
                            aux_index_mostsquashed_b(4)*...
                            (-aux1_incr_dist*ped_radius_min(i_mostsquashed)+ped_dist_from_upper(i_mostsquashed))+...
                            sum(aux_index_mostsquashed_b(5:4+N_columns).*...
                               (-(pedCol_radius_min(i_mostsquashed,:)+...
                                  (aux2_incr_dist-1)*pedCol_squeezable_length(i_mostsquashed,:))+...    
                                r_ib_column(i_mostsquashed,:)).*...
                               (ny_ib_column(i_mostsquashed,:)-aux_rand_1.*nx_ib_column(i_mostsquashed,:)))+...
                            sum(aux2_incr_dist*rbmb_i_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end)).*...
                               (ny_ib_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end))-...
                               aux_rand_2(aux_index_mostsquashed_b(5+N_columns:end)).*...
                               nx_ib_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end))));
                x_ped(i_mostsquashed)=x_ped(i_mostsquashed)+delta_x_ped;
                y_ped(i_mostsquashed)=y_ped(i_mostsquashed)+delta_y_ped;
                                                %  these are the new x- and y-coordinates of the most-squashed pedestrian who
                                                %   is squashed against boundaries; unless the space is packed toooo tightly,
                                                %   this step should be able to move the pedestrian away from those boundaries
                vx_ped_mostsquashed_old=vx_ped(i_mostsquashed);
                vy_ped_mostsquashed_old=vy_ped(i_mostsquashed);
                                                % record the velocity of the mostsquashed ped before he is resheffled;
                                                %  this velocity is used below for the computation of the amount of time
                                                %  needed to perform the reshuffling
                % After having stopped the pedestrian, rotate him so that his new direction of motion (determined by
                %  the vector [ex_ped,ey_ped]) becomes tangential to the boundary that he is squashed against. 
                %  His tangential velocity w.r.t. that boundary has the same direction as before the impact,
                %  and its magnitude is reduced by the factor  ped_b_impact_inelasticity, 
                %  which was defined at the beginning of this code.
                %  In the case when the pedestrian is in a corner between two walls, orient him along the corner's
                %  bisector pointing out of the corner, and set his velocity to zero.
                aux_direction=0;                % a marker used (only) in the loop below
                if aux_index_mostsquashed_b(1) == 1   
                                                % i.e. if the pedestrian is squashed against the left wall
                    if aux_index_mostsquashed_b(3)+aux_index_mostsquashed_b(4) == 0
                                                % and if he is squashed only against the left wall
                        vx_ped(i_mostsquashed)=0;
                        vy_ped(i_mostsquashed)=vy_ped(i_mostsquashed)*(1-ped_b_impact_inelasticity);
                        ey_ped(i_mostsquashed)=sign(vy_ped(i_mostsquashed));
                        ex_ped(i_mostsquashed)=sqrt(1-(ey_ped(i_mostsquashed))^2);
                    elseif aux_index_mostsquashed_b(3) == 1
                                                % i.e. if he is in the lower left corner
                        vx_ped(i_mostsquashed)=0;
                        vy_ped(i_mostsquashed)=0;
                        ex_ped(i_mostsquashed)=1/sqrt(2);
                        ey_ped(i_mostsquashed)=1/sqrt(2);
                    elseif aux_index_mostsquashed_b(4) == 1
                                                % i.e. if he is in the upper left corner
                        vx_ped(i_mostsquashed)=0;
                        vy_ped(i_mostsquashed)=0;
                        ex_ped(i_mostsquashed)=1/sqrt(2);
                        ey_ped(i_mostsquashed)=-1/sqrt(2);
                    end
                    aux_direction=1;
                elseif aux_index_mostsquashed_b(2) == 1
                                                % i.e. if the pedestrian is squashed against the right wall
                    if aux_index_mostsquashed_b(3)+aux_index_mostsquashed_b(4) == 0
                                                % and if he is squashed only against the right wall
                        vx_ped(i_mostsquashed)=0;
                        vy_ped(i_mostsquashed)=vy_ped(i_mostsquashed)*(1-ped_b_impact_inelasticity);
                        
% THIS BLOCK IS FOR DEBUGGING ONLY                        
%                         i_mostsquashed
%                         disp('values before reshuffling')
%                         ex_ped(i_mostsquashed)
%                         ey_ped(i_mostsquashed)
                        ey_ped(i_mostsquashed)=sign(vy_ped(i_mostsquashed));
                        ex_ped(i_mostsquashed)=-sqrt(1-(ey_ped(i_mostsquashed))^2);
%                         disp('values after reshuffling')
%                         ex_ped(i_mostsquashed)
%                         ey_ped(i_mostsquashed)
%                         x_ped(i_mostsquashed)
%                         y_ped(i_mostsquashed)
                        
                    elseif aux_index_mostsquashed_b(3) == 1
                                                % i.e. if he is in the lower right corner
                        vx_ped(i_mostsquashed)=0;
                        vy_ped(i_mostsquashed)=0;
                        ex_ped(i_mostsquashed)=-1/sqrt(2);
                        ey_ped(i_mostsquashed)=1/sqrt(2);
                    elseif aux_index_mostsquashed_b(4) == 1
                                                % i.e. if he is in the upper right corner
                        vx_ped(i_mostsquashed)=0;
                        vy_ped(i_mostsquashed)=0;
                        ex_ped(i_mostsquashed)=-1/sqrt(2);
                        ey_ped(i_mostsquashed)=-1/sqrt(2);
                    end
                    aux_direction=1;
                elseif aux_index_mostsquashed_b(3) == 1
                                                % i.e. if the pedestrian is squashed against the lower wall
                    if aux_direction == 0       % i.e. re-orient the pedetrian only if this has not been done
                                                %  immediately above in the case when he is squashed in a corner
                        vx_ped(i_mostsquashed)=vx_ped(i_mostsquashed)*(1-ped_b_impact_inelasticity);
                        vy_ped(i_mostsquashed)=0;
                        ex_ped(i_mostsquashed)=sign(vx_ped(i_mostsquashed));
                        ey_ped(i_mostsquashed)=sqrt(1-(ex_ped(i_mostsquashed))^2);
                    end
                elseif aux_index_mostsquashed_b(4) == 1
                                                % i.e. if the pedestrian is squashed against the upper wall
                    if aux_direction == 0       % i.e. re-orient the pedetrian only if this has not been done
                                                %  immediately above in the case when he is squashed in a corner
                        vx_ped(i_mostsquashed)=vx_ped(i_mostsquashed)*(1-ped_b_impact_inelasticity);
                        vy_ped(i_mostsquashed)=0;
                        ex_ped(i_mostsquashed)=sign(vx_ped(i_mostsquashed));
                        ey_ped(i_mostsquashed)=-sqrt(1-(ex_ped(i_mostsquashed))^2);
                    end
                elseif max(aux_index_mostsquashed_b(5:4+N_columns)) == 1
                                                % i.e. if the pedestrian is squashed against a column
                                                %  (note that he can be squashed only against one column, since no two
                                                %  columns are so close that the same person can be simultaneously
                                                %  squashed between them; this means that in the sum below, only
                                                %  one term, corresponding to the closest column, is nonzero)
                    aux_inner_product=sum(aux_index_mostsquashed_b(5:4+N_columns).*...
                                          (vx_ped(i_mostsquashed)*nx_ib_column(i_mostsquashed,:)+...
                                          vy_ped(i_mostsquashed)*ny_ib_column(i_mostsquashed,:)));
                    % See comments about the next 3 equations and the loop in Part 2a) of this reshiffling process.
                    vx_ped(i_mostsquashed)=vx_ped(i_mostsquashed)-aux_inner_product*...
                                           sum(aux_index_mostsquashed_b(5:4+N_columns).*nx_ib_column(i_mostsquashed,:));
                    vy_ped(i_mostsquashed)=vy_ped(i_mostsquashed)-aux_inner_product*...
                                           sum(aux_index_mostsquashed_b(5:4+N_columns).*ny_ib_column(i_mostsquashed,:));
                    where2rotate=sum(aux_index_mostsquashed_b(5:4+N_columns).*...
                                     (vx_ped(i_mostsquashed)*ny_ib_column(i_mostsquashed,:)-...
                                     vy_ped(i_mostsquashed)*nx_ib_column(i_mostsquashed,:)));
                    if where2rotate > 0
                        ex_ped(i_mostsquashed)=sum(aux_index_mostsquashed_b(5:4+N_columns).*ny_ib_column(i_mostsquashed,:));
                        ey_ped(i_mostsquashed)=-sum(aux_index_mostsquashed_b(5:4+N_columns).*nx_ib_column(i_mostsquashed,:));
                    else
                        ex_ped(i_mostsquashed)=-sum(aux_index_mostsquashed_b(5:4+N_columns).*ny_ib_column(i_mostsquashed,:));
                        ey_ped(i_mostsquashed)=sum(aux_index_mostsquashed_b(5:4+N_columns).*nx_ib_column(i_mostsquashed,:));
                    end
                end                             % this is the end of the loop which sets the new velocity and re-orientats
                                                %  the mostsquashed pedestrian
                %
                % Now compute the time that is needed to move that ped away from the boundary(ies). 
                %  This time is computed as follows:   
                %  delta_t_move_ped_away_from_b=(change of normal component of momentum)/(normal force).
                %  (Note that the normal component of the momentum does indeed change since the ped gets stopped.)
                delta_t_move_ped_away_from_b=max([ ...
                                              aux_index_mostsquashed_b(1)*max(-vx_ped_mostsquashed_old,0)/Ncontact_max,...
                                              aux_index_mostsquashed_b(2)*max(vx_ped_mostsquashed_old,0)/Ncontact_max,...
                                              aux_index_mostsquashed_b(3)*max(-vy_ped_mostsquashed_old,0)/Ncontact_max,...
                                              aux_index_mostsquashed_b(4)*max(vy_ped_mostsquashed_old,0)/Ncontact_max,...
                                              aux_index_mostsquashed_b(5:N_columns+4).*...
                                               max(vx_ped_mostsquashed_old*nx_ib_column(i_mostsquashed,:)+...
                                                   vy_ped_mostsquashed_old*ny_ib_column(i_mostsquashed,:),0)./Ncontact_max,...
                                              max(vx_ij(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end)).*...
                                                   nx_ib_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end))+...
                                                  vy_ij(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end)).*...
                                                   ny_ib_stoned(i_mostsquashed,aux_index_mostsquashed_b(5+N_columns:end)),0)./...
                                               Ncontact_max...
                                                  ])*ped_mass(i_mostsquashed);
                delta_t_of_this_reshuffling_step(counter_of_reshuffling)=delta_t_move_ped_away_from_b;
                                                % amount of time needed to perform this reshuffling step  [s]
                                                %  (the reason this calculation is done in two steps, the second of which
                                                %   is just a renaming of the variable, is to have it in the same form
                                                %   as in the case where a mostsquashed ped is squashed against a boundary
                                                %   as well as against other peds)
                %
                % Now form a micro-CLUSTER of peds who had to be reshuffled at this step of the reshuffling procedure
                %  (in this case, the micro-cluster consists of a single ped, the most squashed one against a boundary):
                cluster_micro(counter_of_reshuffling,:)=logical([zeros(1,i_mostsquashed-1), 1, ...
                                                                 zeros(1,N_peds-i_mostsquashed)]);
                
            else
                disp('WARNING: Smth wrong with detecting whether the pedestrian is most squashed against another one')
                disp('         or against a boundary !!!')
                pause
            end            
        
        end                                     % this is the end of the loop which verifies whether the most-squashed 
                                                %  pedestrian has been squashed against a boundary or against another
                                                %  pedestrian
                                          
                                                
        indices_of_stoned_peds(i_mostsquashed)=1;
                                                % this pedestrian will be considered "stoned" at the next step of
                                                %  the reshufling process
        % Once he has been "stoned", we remove him from those pedestrians who could be moved at the next step:
        aux_index_ij(i_mostsquashed,:)=logical(zeros(1,N_peds));
        aux_index_ij(:,i_mostsquashed)=logical(zeros(N_peds,1));
        aux_index_ib_column(i_mostsquashed,:)=logical(zeros(1,N_columns));
        aux_index_ib_left(i_mostsquashed)=0;
        aux_index_ib_right(i_mostsquashed)=0;
        aux_index_ib_lower(i_mostsquashed)=0;
        aux_index_ib_upper(i_mostsquashed)=0;
        % Assign values to the coordinates of this newly-stoned pedestrian:
        x_stoned(i_mostsquashed)=x_ped(i_mostsquashed);
        y_stoned(i_mostsquashed)=y_ped(i_mostsquashed);

        % Clean arrays that contain the by-now-obsolete information about relative location of pedestrians and walls:
        x_ij=zeros(N_peds);
        y_ij=zeros(N_peds);
        r_ij=zeros(N_peds);
        nx_ij=zeros(N_peds);
        ny_ij=zeros(N_peds);
        x_ib_stoned=zeros(N_peds);
        y_ib_stoned=zeros(N_peds);
        r_ib_stoned=zeros(N_peds);
        nx_ib_stoned=zeros(N_peds);
        ny_ib_stoned=zeros(N_peds);
        x_ib_column=zeros(N_peds,N_columns);
        y_ib_column=zeros(N_peds,N_columns);
        r_ib_column=zeros(N_peds,N_columns);
        nx_ib_column=zeros(N_peds,N_columns);
        ny_ib_column=zeros(N_peds,N_columns);
        ped_dist_from_left=zeros(1,N_peds);
        ped_dist_from_right=zeros(1,N_peds);
        ped_dist_from_lower=zeros(1,N_peds);
        ped_dist_from_upper=zeros(1,N_peds);
        rbmb_i_others=zeros(N_peds);
        rbmb_i_column=zeros(N_peds,N_columns);
        rbmb_i_left=zeros(1,N_peds);
        rbmb_i_right=zeros(1,N_peds);
        rbmb_i_lower=zeros(1,N_peds);
        rbmb_i_upper=zeros(1,N_peds);
        rbmb_i_stoned=zeros(N_peds);
        rbmb_i_min=zeros(1,N_peds);

        % Prepare an array that will be used at the next step:
        aux_index_ib_stoned=logical(zeros(N_peds));
        % Now I need to compute modified (by the reshuffling process) distances among pedestrians who still can be moved 
        %  and who, at the same time, are sufficiently close to one another and the boundaries:
      	for i=1:N_peds
            if sum(aux_index_ij(i,:)) > 0
                x_ij(i,aux_index_ij(i,:))=x_ped(aux_index_ij(i,:))-x_ped(i);
                y_ij(i,aux_index_ij(i,:))=y_ped(aux_index_ij(i,:))-y_ped(i);
                                                % x- and y-components of relative positions of pedestrians
                                                %  who can still be moved  [m]
            end
            aux_index_ib_stoned(i,:)=sum(aux_index_ij_b4_reshuffle(i,:)) > 0  & ...
                                     indices_of_stoned_peds(i) ~= 1  &  indices_of_stoned_peds;
                                                % this is a modified list of indices of stoned peds:
                                                %  it coincides with the latter if  
                                                %  (i) the i-th ped has any neighbors whom he takes into account and
                                                %  (ii) he himself has not been stoned
            if sum(aux_index_ib_stoned(i,:)) > 0
                x_ib_stoned(i,indices_of_stoned_peds)=x_stoned(indices_of_stoned_peds)-x_ped(i);
                y_ib_stoned(i,indices_of_stoned_peds)=y_stoned(indices_of_stoned_peds)-y_ped(i);
                                                % x- and y-components of relative positions of still-movable
                                                %  and stoned pedestrians  [m]
            end
            if sum(aux_index_ib_column(i,:)) > 0
                x_ib_column(i,aux_index_ib_column(i,:))=Column_c_x(aux_index_ib_column(i,:))-x_ped(i);
                y_ib_column(i,aux_index_ib_column(i,:))=Column_c_y(aux_index_ib_column(i,:))-y_ped(i);
                                                % x- and y-components of relative positions of columns
                                                %  and pedestrians (the corresponding radius-vector is
                                                %  directed from a pedestrian to a column)  [m]
            end
            
	    end
        
        r_ij(aux_index_ij)=sqrt((x_ij(aux_index_ij)).^2+(y_ij(aux_index_ij)).^2);
                                                % new distances among movable pedestrians  [m]
	    r_ib_column(aux_index_ib_column)=sqrt((x_ib_column(aux_index_ib_column)).^2+(y_ib_column(aux_index_ib_column)).^2);
                                                % matrix of new distances of movable pedestrians from columns  [m]
        r_ib_stoned(aux_index_ib_stoned)=sqrt((x_ib_stoned(aux_index_ib_stoned)).^2+...
                                              (y_ib_stoned(aux_index_ib_stoned)).^2);
        nx_ij(aux_index_ij)=x_ij(aux_index_ij)./r_ij(aux_index_ij);
        ny_ij(aux_index_ij)=y_ij(aux_index_ij)./r_ij(aux_index_ij);
        nx_ib_column(aux_index_ib_column)=x_ib_column(aux_index_ib_column)./r_ib_column(aux_index_ib_column);
        ny_ib_column(aux_index_ib_column)=y_ib_column(aux_index_ib_column)./r_ib_column(aux_index_ib_column);
        nx_ib_stoned(aux_index_ib_stoned)=x_ib_stoned(aux_index_ib_stoned)./...
                                          r_ib_stoned(aux_index_ib_stoned);
        ny_ib_stoned(aux_index_ib_stoned)=y_ib_stoned(aux_index_ib_stoned)./...
                                          r_ib_stoned(aux_index_ib_stoned);
                                                % corresponding new components of unit vectors in the x- and y-directions
                                                
        % Determine new distances of selected pedestrians from walls:
    	ped_dist_from_left(aux_index_ib_left)=x_ped(aux_index_ib_left)-B_wall_xmin;
	    ped_dist_from_right(aux_index_ib_right)=BwallRight(y_ped(i))-x_ped(aux_index_ib_right);
	    ped_dist_from_lower(aux_index_ib_lower)=y_ped(aux_index_ib_lower)-B_wall_ymin;
	    ped_dist_from_upper(aux_index_ib_upper)=B_wall_ymax-y_ped(aux_index_ib_upper);
                                                % distances of pedestrians from walls  [m]    
        % Now compute the new rbmb's :
        rbmb_i_others(aux_index_ij)=r_ij(aux_index_ij)-d_min_ij(aux_index_ij);
                                                % distance between "minimal boundaries" of any pair of pedestrians  [m]
                                                %  (if it is negative, then they have overlapped)
        rbmb_i_column(aux_index_ib_column)=r_ib_column(aux_index_ib_column)-pedCol_radius_min(aux_index_ib_column);
                                                % distance between min boundaries of pedestrians and columns  [m]
        rbmb_i_left(aux_index_ib_left)=ped_dist_from_left(aux_index_ib_left)-ped_radius_min(aux_index_ib_left);
        rbmb_i_right(aux_index_ib_right)=ped_dist_from_right(aux_index_ib_right)-ped_radius_min(aux_index_ib_right);
        rbmb_i_lower(aux_index_ib_lower)=ped_dist_from_lower(aux_index_ib_lower)-ped_radius_min(aux_index_ib_lower);
        rbmb_i_upper(aux_index_ib_upper)=ped_dist_from_upper(aux_index_ib_upper)-ped_radius_min(aux_index_ib_upper);
                                                % distances between min boundaries of pedestrians and walls  [m]
        rbmb_i_stoned(aux_index_ib_stoned)=r_ib_stoned(aux_index_ib_stoned)-d_min_ij(aux_index_ib_stoned);
                                                % distance between "minimal boundaries" of pedestrians who are "stoned"
                                                %  and who can still be moved at the next step  [m]
        for i=1:N_peds
            rbmb_i_close(i,:)=[rbmb_i_others(i,:) rbmb_i_left(i) rbmb_i_right(i) rbmb_i_lower(i) rbmb_i_upper(i) ...
                                 rbmb_i_column(i,:) rbmb_i_stoned(i,:)];
            aux_index_squashed(i,:)=rbmb_i_close(i,:) < 0;
            % This loop determines the minimum distances existing between "minimal boundaries" of peds and boundaries 
            %  of other objects in the room:
            if sum(aux_index_squashed(i,:)) > 0 % if i-th pedestrian is squashed against other pedestrians or boundaries
                rbmb_i_min(i)=min(rbmb_i_close(i,aux_index_squashed(i,:)));
            end                                 
        end
        
        [rbmb_i1_min,i1_min]=min(rbmb_i_min);   % determine the pedestrian who is squashed the most, and his sequential index 
    
    end                                         % this is the end of the loop determining whether the reshuffling process
                                                %  needed to be implemented
  %%%  min_dist_to_col=min(rbmb_i_column,[],1);   this was used during debugging only 
  

  
    if time_the_code == 1
        toc
        
        disp('% ----------------    Calculate the time needed to perform reshuffling ---------------------')
        tic
    end

    %  
    % Now   compute   the   total   time   needed   to   perform   the   entire   reshuffling   procedure   above.
    %  First, determine which micro-clusters form a macro-cluster (e.g., 2 micro-clusters are parts of the same
    %  macro-cluster if at least one of their elements is the same). Now, the time needed to reshuffle a macro-cluster
    %  is the sum of the times needed to reshuffle all micro-clusters that is consists of. 
    %  Determine all separate macro-clusters and the reshuffling times for each of them. 
    %
    N_ungrouped_microclusters=counter_of_reshuffling;
                                                % initial # of micro-clusters which haven't yet been grouped into macro-ones
    indices_of_grouped_microclusters=[];
    index_of_micro_to_form_a_macro=1;           % start combining micro-clusters into macto-ones with micro-cluster # 1
    N_macroclusters=0;                          % initially, there are no macro-clusters
    clear    delta_t_of_this_macrocluster  cluster_macro*
    while N_ungrouped_microclusters > 0
        N_macroclusters=N_macroclusters+1;
        cluster_macro_old(N_macroclusters,:)=logical(zeros(1,N_peds));
        cluster_macro_new(N_macroclusters,:)=cluster_micro(index_of_micro_to_form_a_macro,:);
        indices_of_grouped_microclusters=[indices_of_grouped_microclusters,   index_of_micro_to_form_a_macro];
                                                % record the index of a microcluster each time as it has been counted during
                                                %  the process of forming macro-clusters from micro-ones
        delta_t_of_this_macrocluster(N_macroclusters)=delta_t_of_this_reshuffling_step(index_of_micro_to_form_a_macro);
                                                % % initiate the time needed to reshuffle the macroclusters
        while sum(cluster_macro_new(N_macroclusters,:) ~= cluster_macro_old(N_macroclusters,:)) > 0
                                                % repeat comparing micro-clusters with each other until, after one round of
                                                %  comparison, no new micro-clusters get added to the macro-cluster
            cluster_macro_old(N_macroclusters,:)=cluster_macro_new(N_macroclusters,:);
            for n_micro=1:counter_of_reshuffling
                if n_micro ~= indices_of_grouped_microclusters
                    if sum(cluster_macro_new(N_macroclusters,:) & cluster_micro(n_micro,:)) > 0
                                                % if micro-cluster # n_micro has at least one element in common with
                                                %  micro-cluster # index_of_micro_to_form_a_macro, 
                                                %  combine them into a macrocluster 
                        cluster_macro_new(N_macroclusters,:)=cluster_macro_new(N_macroclusters,:) | cluster_micro(n_micro,:);
                                                % first, increase the micro-cluster # index_of_micro_to_form_a_macro
                                                %  to a new size
                        indices_of_grouped_microclusters=[indices_of_grouped_microclusters n_micro];
                                                % update the indices of grouped micro-clusters to include the lates one
                        delta_t_of_this_macrocluster(N_macroclusters)=delta_t_of_this_macrocluster(N_macroclusters)+...
                                                                      delta_t_of_this_reshuffling_step(n_micro);
                                                % update the delta_t_... 
                                                %
                    else                        % i.e. if micro-cluster # n_micro has at least one element in common with
                                                %  micro-cluster # index_of_micro_to_form_a_macro,
                                                %  make the former the a candidate for the new micro-cluster
                                                %  index_of_micro_to_form_a_macro
                        index_of_micro_to_form_a_macro=n_micro;
                    end
                end
            end
            
        end
        N_ungrouped_microclusters=counter_of_reshuffling-length(indices_of_grouped_microclusters);
    end
    %
    % For uniform handling of possible cases, define the macro-clusters in case where no reshuffling has been performed:
    if counter_of_reshuffling > 0
        cluster_macro=cluster_macro_new;
    else
        cluster_macro=logical(zeros(1,N_peds));
        delta_t_of_this_macrocluster=0;
    end
    % Verify that no two macro-clusters have common peds:
    aux_for_macroclusters=sum(cluster_macro,1);
    if aux_for_macroclusters > 1
        disp('Partition into macro-clusters is wrong !    Stop and fix the problem !')
        unknown_variable
    end
    cluster_macro(N_macroclusters+1,:)=~aux_for_macroclusters;
                                                % create the last macro-cluster - the one which contains only peds
                                                %  who have not been reshuffled at the previous step
    delta_t_of_this_macrocluster(N_macroclusters+1)=0;
                                                % peds in this last macro-cluster spend no time on reshuffling
    max_delta_t_of_reshuffling=max(delta_t_of_this_macrocluster);
                                                % max time spend on reshuffling, among all the macro-clusters
    % Make a record to the errorlog file if the time spent on reshuffling is too large compared to 
    %  dt_before_reshuffling:
    if max_delta_t_of_reshuffling > 10*dt
        count_toolarge_reshufflingtime=count_toolarge_reshufflingtime+1;
        array_toolarge_reshufflingtime(count_toolarge_reshufflingtime,:)=...
                                      [max_delta_t_of_reshuffling,dt,T_computed];
        max_delta_t_of_reshuffling=max_delta_t_of_reshuffling;
        if saved_to_errorlog_already == 1
            save(name_errorlog,'array_toolarge_reshufflingtime', '-append')
        else
            save(name_errorlog,'array_toolarge_reshufflingtime')
        end
        saved_to_errorlog_already=1;
    end
            
    %
    % Now modify the time step as was explained just before the beginning of the calculation,
    %  at the place in the code where I initialized the array  dt_backlog:
    %
    dt_after_reshuffling = max(dt - delta_t_of_this_macrocluster*cluster_macro + dt_backlog,0);
    dt_backlog=min(dt - delta_t_of_this_macrocluster*cluster_macro + dt_backlog,0);
    %
    aux_index_ped_advancing_prev=aux_index_ped_advancing;
                                                % record previous indices of the advancing peds
    % If the backlogged time for a given ped is more than 5 dt's, assume that he will not be advancing 
    %  at the next time step. The # "6" is rather arbitrary; it seems to me now that it is safe to assume
    %  that two consecutively found dt's won't differ by more than 6 times.
    for i=1:N_peds
        if dt_backlog(i) < -6*dt
            aux_index_ped_advancing(i)=0;
        else
            aux_index_ped_advancing(i)=1;
        end
    end
        
    %        
    % Calculate the computational time spent so far:
    T_computed_prev=T_computed;                 % record the time accumulated until taking this step  [s]
    T_computed=T_computed_prev+dt;              % record the time accumulated after taking this step  [s]
    if (T_computed_prev-n_computed_part*T_onepart)*(T_computed-n_computed_part*T_onepart) <= 0
        increase_computed_part_number=1;
    else                                        
        increase_computed_part_number=0;
    end
    n_computed_part=n_computed_part+increase_computed_part_number;
                                                % i.e. the number of computed part increases only if T_computed
                                                %  becomes equal to an integer multiple of T_onepart
        

  
   	%
	%  ------------   Set up noise terms, added to the changes in velocity of pedestrians:  --------------
    %
    aux1_rand_for_noise=(2*rand(size(x_ped))-1);
    aux2_rand_for_noise=(2*rand(size(x_ped))-1)./(1+speed_ped./ped_speed0).^4;
                                                % ped's velocity fluctuates mostly along his direction vector;
                                                %  fluctuations in the orthogonal direction are less than those
                                                %  in the direction of motion, with the difference decreasing when
                                                %  the ped's velocity decreases
    aux_factor_for_noise=dt_after_reshuffling.*ped_acceleration_fluct.*(1+7*max([excitement_ped; zeros(1,N_peds)],[],1));
	noise_vx=(noise_vx+aux_factor_for_noise.*(aux1_rand_for_noise.*ex_ped+aux2_rand_for_noise.*ey_ped)).*...
             exp(-dt_after_reshuffling./ped_tau_reaction);
	noise_vy=(noise_vy+aux_factor_for_noise.*(aux1_rand_for_noise.*ey_ped-aux2_rand_for_noise.*ex_ped)).*...
             exp(-dt_after_reshuffling./ped_tau_reaction);
    %     

  
    
    if time_the_code == 1
        toc
        
        disp('% ----------------    Advance peds` parameters at this time step.  -----------------------')
        disp('% -----------   Unfortunately, this has to be done for all peds at once, -----------------')
        disp('% ---- and so it is impossible to time how long individual parts of this updating take :-( ')
        tic
    end
        
  	% %
	%  -------  Advance the pedestrians` coordinates, velocities, and other necessary parameters by one step in time: -----
    %  --------------------------------------------------------------------------------------------------------------------
	% tic
        
    
%     %
%     %                                           THIS IS USED FOR DEBUGGING ONLY:
%     num_of_peds_inside=sum(aux_index_ped_inside)
%     plot(dt_after_reshuffling)
%     pause(0.01)
%     %
    
    
    for i=1:N_peds
        oldX = x_ped(i);
        oldY = y_ped(i);
        
        if (x_ped(i)-B_outerwall_left)*(x_ped(i)-B_outerwall_right) >= 0 | ...
           (y_ped(i)-B_outerwall_lower)*(y_ped(i)-B_outerwall_upper) >= 0
                                                % if the pedestrian wanders outside the outer boundaries,
                                                %  stop him where he is and don't move him anymore
            x_ped(i)=x_ped(i);
            y_ped(i)=y_ped(i);
            
            
            
            vx_ped(i)=0;
            vy_ped(i)=0;
	% % %             sprintf('%i-th guy is ouside OUTER walls', i)
    
        elseif aux_index_ped_inside(i) == 0     % if the pedestrian gets outside the room but is still
                                                %  within the outer boundaries, let him move freely
            x_ped(i)=x_ped(i)+dt_after_reshuffling(i)*vx_ped(i);
            y_ped(i)=y_ped(i)+dt_after_reshuffling(i)*vy_ped(i);
            vx_ped(i)=vx_ped(i);
            vy_ped(i)=vy_ped(i);
        elseif aux_index_ped_inside(i) == 1 && aux_index_ped_advancing_prev(i) == 1
                                                % if the pedesrian is inside the room, apply the 
                                                %  full set of forces to him (but in this case we distinguish
                                                %  between two different scenarios, as explained shortly below)
                                                
            % First, compute the excitement factor, which is determined as follows.
            %  At each moment, a pedestrian compares the projection of his ideally preferred (see below) velocity
            %  on the direction connecting him and the strongest attraction. His excitement increases if he underachieves
            %  his goal, and decreases, if he overachieves it. On top of this, his excitement is subject to an exponential
            %  decay over time, if no external factors are present. 
            %  By "ideally preferred", we mean aux_V{x,y}_pref  rather than  V{x,y}_pref.  The reason is that in a dense
            %  crowd, V_pref may become too small (to limit the centrifugal acceleration), which will cause rapid and
            %  unrealistic changes in excitement of a ped who is exiting the room. I actually observed this.
            %
            %  Note that the excitement factor is not allowed (by our choosing) to drop below zero.
            excitement_ped(i)=(excitement_ped(i)+ped_excitement_max(i)*dt_after_reshuffling(i)/...
                              (ped_tau_loc_memory_learn(i))*...
                              (1-(nx_ia(i,strongest_attraction_index(i))*vx_ped(i)+...
                              ny_ia(i,strongest_attraction_index(i))*vy_ped(i))/...
                              ped_speed0(i)))*exp(-dt_after_reshuffling(i)/(ped_tau_loc_memory_learn(i)));
            % Compute the accumulated excitement, which grows and decays with the same exponential factor as
            %  the excitement factor itself:
            ped_accumulated_excitement(i)=(ped_accumulated_excitement(i)+dt_after_reshuffling(i)*excitement_ped(i))*...
                                          exp(-dt_after_reshuffling(i)/(ped_tau_loc_memory_forget(i)));
            % Decide if the ped's threshold has been exceeded and if it has been, then let him give up his
            %  strongest attraction with the probability  ped_P_to_give_up.
            %  First, clear the entry containing the info about the ped's giving-up at the previous step:
            ped_gave_up(i)=0;
            if ped_accumulated_excitement(i) > ped_accumulated_excitement_threshold(i)
                if rand < ped_P_to_give_up(i)
                    ped_gave_up(i)=1;           % if this entry is nonzero, the ped has given up
                end
            end
                            
            %
            %
            x_ped_old=x_ped(i);
            y_ped_old=y_ped(i);                 % record the old coordinates of the given ped for the purpose of
                                                %  determining whether he sees corners on not (see below)
            x_ped(i)=x_ped(i)+dt_after_reshuffling(i)*vx_ped(i);
            y_ped(i)=y_ped(i)+dt_after_reshuffling(i)*vy_ped(i);
            % When a pedestrian is inside the room, we still distinguish between two situations:
            %  (i)   he is not yet going through a door,  and
            %  (ii)  he is already going through a door to exit the room.
            %  In case (i), we apply the full set of forces to him. 
            %     Moreover, in this case he also rotates so as to steer either towards his strongest
            %     attraction or away from an obstacle (a column, corner, or flat wall). He rotates
            %     BEFORE the components of his velocity are updated on an individual basis.
            %  In case (ii), the only forces acting on a pedestrian are the physical forces of
            %     his interaction with walls (door posts) and other pedestrians and the social force
            %     of desiring to exit the room (which has the same magnitude as the attraction force
            %     that has brought him to the door). He does NOT rotate in this case.
            if ped_within_left_door(i) == 0 & ped_within_right_door(i) == 0 & ...
               ped_within_lower_door(i) == 0 & ped_within_upper_door(i) == 0
                % As explained above, in addition to changing their velocities, the pedestrians also change their
                %  orientations (i.e. the vectors ex_ped and ey_ped, according to the rules stated below. This is done
                %  in order to allow a ped to change his direction _far away_ from an object, so as to avoid a collision
                %  or, in the case of attraction, more readily reach it.
                %  The rules determining the direction of rotation are as follows:
                %  -  If a ped sees (see below for an explanation of "sees") his strongest attraction, he turns towards it.
                %  -  If he doesn't see his strongest attraction:
                %    ~  But sees a column on his way, he turns away from it.
                %    ~  Sees no column on his way, but 
                %      * sees a flat wall (no corner), he turns so as to move more tangentially relative to the wall.
                %      * sees a corner, he turns away from it while staying on the same side of the vector connecting
                %            him to the corner (i.e., his reaction in this case is similar to his reaction to a column).
                %  Definition of the term "sees", used above, is as follows. 
                %  - To see an attraction or a corner, the ped has to be a distance X away from it, 
                %    where   ped_react_far_dist_min < X < ped_react_far_dist_max, PLUS the object has to be within the angle
                %    ped_seeing_angle  on either side of the ped's directional vector e{x,y}_ped.
                %  - To see a column on his way, the pedestrian has to be a distance  X  away from it, where X is
                %    within the same bounds as above, and his direction vector e{x,y}_ped has to be pointing inside
                %    the circle of radius   ped_radius(i)+Column_radius(k), with the center of the circle coinciding
                %    with the center of the given column (i.e. he accounts for the fact that he has a nonzero radius).
                %  - To see a wall, the pedestrian has to be a distance  X  away from it, where X is
                %    within the same bounds as above, and the normal extending from the ped to the wall has to be
                %    within the angle  ped_seeing_angle  from the ped's directional vector e{x,y}_ped.
                %  Now, the magnitude of the angle of rotation is determined from the consideration that the resulting
                %   centrifugal acceleration must not exceed  a_centrifugal_confortable. Since it is the same in all cases,
                %   we determine it first.
                %  Finally, a note about the _ORDER_ in which I perform this calculation:
                %   Even though I do this after I computed the new values of v{x,y}_ped, I use the old values of
                %   pedestrian coordinates, distances, and directions of motion.
                %   After I determine the new rotated directions of motions, I align the new velocities along those
                %   new direction vectors.
                %
                
                what_ped_has_seen(i,:)=zeros(1,N_attractions+N_columns+4);
                                                % this is an auxiliary quanitity that determines which rotating subloop
                                                %  the last ped has been through; 
                                                %  it is used  BOTH  for debugging purposes as well as
                                                %  to assess the memory that the pedestrian has of the location
                                                %  of an attraction he was heading to or an abstacle he was avoiding
                                                %  (specifically, it is used when we calculate the  ped_percep_angle's;
                                                %   the entries in it correspond to, in sequential order:
                                                %   attractions, columns, left wall, right wall, lower wall, and upper wall)
                
                dphi=0;                         % if the ped doesn't change his direction in the loop below, his
                                                %  angle of rotation is set to zero; if he does change his direction,
                                                %  the value of the rotation angle is determined in the loop
                dphi_magnitude_max=a_centrifugal_comfortable*dt_after_reshuffling(i)/(speed_ped(i)+EPS);
                                                % this is the maximum magnitude of the angle by which a pedestrian 
                                                %  would turns (it can be less if he doesn't need to turn that much)  [rad]
                                                % (we use the magnitude of the ped's speed before it has been updated
                                                %  immediately above - this is done to reduce the amount of computation)
                cos_angle_ia=ex_ped(i)*nx_ia(i,strongest_attraction_index(i))+ey_ped(i)*ny_ia(i,strongest_attraction_index(i));
                                                % cosine of the angle between the ped's direction of motion and the
                                                %  vector connecting him to the strongest attraction
                if r_ia(i,strongest_attraction_index(i)) < ped_react_far_dist_max(i) && ...
                   cos_angle_ia > cos_ped_seeing_angle   &&  ...
                   r_ia(i,strongest_attraction_index(i)) > ped_react_far_dist_min(i)
                                                % i.e. if the pedestrian sees his closest attraction
                    dphi_sign=sign(ex_ped(i)*ny_ia(i,strongest_attraction_index(i))-...
                                   ey_ped(i)*nx_ia(i,strongest_attraction_index(i)));
                                                % this is the sign of the angle of rotation; it determines to which
                                                %  side the ped's direction vector is rotated at the next step
                    dphi=min(dphi_magnitude_max,acos(cos_angle_ia/(1+EPS)))*dphi_sign;
                                                % this is the rotation angle (divide by 1+EPS to make sure the cos < 1)
                    what_ped_has_seen(i,strongest_attraction_index(i))=1;
                                                % the quantity initiated before the beginning of the loop
                                                %
                else                            % i.e. if the ped doesn't see his strongest attraction
                    ped_sees_column_n=0;        % initiate the counter, used below, which will be given the
                                                %  number of the column that the given ped sees (if any)
                    for k=1:N_columns           % determine whether he sees a column on his way, and, in case
                                                %  that he sees more than one column, decide on the closest one
                        if aux_index_ib_column(i,k) == 1
                                                % this calculation is performed only if the ped is not too far 
                                                %  from the k-th column
                            aux_angle_to_deflect_from_column=asin(pedCol_radius_min(i,k)/r_ib_column(i,k)/(1+EPS))-...
                                                             acos(ex_ped(i)*nx_ib_column(i,k)+ey_ped(i)*ny_ib_column(i,k)/...
                                                             (1+EPS));
                                                % this is the angle by which a ped has to change his direction so that
                                                %  the column is no longer on his way (if it is now)  [rad]
                            if r_ib_column(i,k) < ped_react_far_dist_max(i)  && ...
                               aux_angle_to_deflect_from_column > 0   && ...
                               r_ib_column(i,k) > ped_react_far_dist_min(i)
                                                % determine is the k-th column is on the i-th ped's way
                                if ped_sees_column_n == 0
                                                % i.e. if this is, in sequential order, the first column he sees on his way
                                    ped_sees_column_n=k;
                                    aux_dist_to_column_onway=r_ib_column(i,k);
                                    angle_to_deflect_from_column=aux_angle_to_deflect_from_column;
                                else            % i.e. if he has detected other columns on his way already
                                    if aux_dist_to_column_onway > r_ib_column(i,k)
                                        ped_sees_column_n=k;
                                        aux_dist_to_column_onway=r_ib_column(i,k);
                                        angle_to_deflect_from_column=aux_angle_to_deflect_from_column;
                                    end
                                end
                            end
                        end
                    end                         % this is the loop in which it is determined whether the ped sees a
                                                %  column on his way and, if he does, the columns number is recorded
                                                %  into the variable  ped_sees_column_n
                    if ped_sees_column_n ~= 0   % i.e. if a ped does see a column on his way, he turns away from it
                        dphi_sign=-sign(ex_ped(i)*ny_ib_column(i,ped_sees_column_n)-...
                                        ey_ped(i)*ny_ib_column(i,ped_sees_column_n));
                                                % this is the sign of the angle of rotation; it determines to which
                                                %  side the ped's direction vector is rotated at the next step
                        dphi=min(dphi_magnitude_max,angle_to_deflect_from_column)*dphi_sign;
                                                % this is the rotation angle
                        what_ped_has_seen(i,N_attractions+ped_sees_column_n)=1;
                                                % the quantity initiated before the beginning of the loop
                                                %
                    else                        % i.e. if the ped doesn't see his strongest attraction or a column on his way,
                                                %  he may see either a corner or a flat wall;
                                                %  he first determines whether he sees any corners, starting from the
                                                %  left lower one and going in the clockwise direction, and then,
                                                %  if he does NOT see any corners, he determines whether he sees any
                                                %  flat walls on his way
                        x_ic_left_lower=(B_wall_xmin-x_ped_old);
                        y_ic_left_lower=(B_wall_ymin-y_ped_old);
                        r_ic_left_lower=sqrt(x_ic_left_lower^2+y_ic_left_lower^2);
                                                % {x,y}-coordinates and the distance from i-th pedestrian 
                                                %  to the left lower corner  [m]
                        cos_angle_ic_left_lower=(ex_ped(i)*x_ic_left_lower+ey_ped(i)*y_ic_left_lower)/r_ic_left_lower;
                                                % cosine of the angle between the ped's direction of motion and the
                                                %  vector connecting him to the left lower corner
                        if r_ic_left_lower < ped_react_far_dist_max(i) && ...
                           cos_angle_ic_left_lower > cos_ped_seeing_angle  && ...
                           r_ic_left_lower > ped_react_far_dist_min(i)
                                                % if all those conditions apply, the ped sees the left lower corner
                                                %  and turns away from it in the same way he turns away from a column;
                                                %  NOTE that it is not possible for a ped to see more than 1 corner !
                            dphi_sign=-sign(ex_ped(i)*y_ic_left_lower-ey_ped(i)*x_ic_left_lower);
                                                % this is the sign of the angle of rotation; it determines to which
                                                %  side the ped's direction vector is rotated at the next step
                            dphi=min(dphi_magnitude_max,ped_seeing_angle-acos(cos_angle_ic_left_lower/(1+EPS)))*dphi_sign;
                                                % this is the rotation angle
                            what_ped_has_seen(i,N_attractions+N_columns+1)=1;
                            what_ped_has_seen(i,N_attractions+N_columns+3)=1;
                                                % the quantity initiated before the beginning of the loop
                                                %
                        else                    % if he doesn't see the left lower corner, he checks if he sees
                                                %  the left upper corner
                            x_ic_left_upper=(B_wall_xmin-x_ped_old);
                            y_ic_left_upper=(B_wall_ymax-y_ped_old);
                            r_ic_left_upper=sqrt(x_ic_left_upper^2+y_ic_left_upper^2);
                                                % {x,y}-coordinates and the distance from i-th pedestrian 
                                                %  to the left upper corner  [m]
                            cos_angle_ic_left_upper=(ex_ped(i)*x_ic_left_upper+ey_ped(i)*y_ic_left_upper)/r_ic_left_upper;
                                                % cosine of the angle between the ped's direction of motion and the
                                                %  vector connecting him to the left upper corner
                            if r_ic_left_upper < ped_react_far_dist_max(i) && ...
                               cos_angle_ic_left_upper > cos_ped_seeing_angle  && ...
                               r_ic_left_upper > ped_react_far_dist_min(i)
                                                % if all those conditions apply, the ped sees the left upper corner
                                                %  and turns away from it
                                dphi_sign=-sign(ex_ped(i)*y_ic_left_upper-ey_ped(i)*x_ic_left_upper);
                                                % this is the sign of the angle of rotation; it determines to which
                                                %  side the ped's direction vector is rotated at the next step
                                dphi=min(dphi_magnitude_max,ped_seeing_angle-acos(cos_angle_ic_left_upper/(1+EPS)))*...
                                     dphi_sign;
                                                % this is the rotation angle
                                what_ped_has_seen(i,N_attractions+N_columns+1)=1;
                                what_ped_has_seen(i,N_attractions+N_columns+4)=1;
                                                % the quantity initiated before the beginning of the loop
                                                %
                            else                % if he doesn't see the left two corners, he checks if he sees
                                                %  the right upper corner
                                x_ic_right_upper=(BwallRight(y_ped(i))-x_ped_old);
                                y_ic_right_upper=(B_wall_ymax-y_ped_old);
                                r_ic_right_upper=sqrt(x_ic_right_upper^2+y_ic_right_upper^2);
                                                % {x,y}-coordinates and the distance from i-th pedestrian 
                                                %  to the right upper corner  [m]
                                cos_angle_ic_right_upper=(ex_ped(i)*x_ic_right_upper+...
                                                          ey_ped(i)*y_ic_right_upper)/r_ic_right_upper;
                                                % cosine of the angle between the ped's direction of motion and the
                                                %  vector connecting him to the right upper corner
                                if r_ic_right_upper < ped_react_far_dist_max(i) && ...
                                   cos_angle_ic_right_upper > cos_ped_seeing_angle  && ...
                                   r_ic_right_upper > ped_react_far_dist_min(i)
                                                % if all those conditions apply, the ped sees the right upper corner
                                                %  and turns away from it
                                    dphi_sign=-sign(ex_ped(i)*y_ic_right_upper-ey_ped(i)*x_ic_right_upper);
                                                % this is the sign of the angle of rotation; it determines to which
                                                %  side the ped's direction vector is rotated at the next step
                                    dphi=min(dphi_magnitude_max,ped_seeing_angle-acos(cos_angle_ic_right_upper/(1+EPS)))*...
                                         dphi_sign;
                                                % this is the rotation angle
                                    what_ped_has_seen(i,N_attractions+N_columns+2)=1;
                                    what_ped_has_seen(i,N_attractions+N_columns+4)=1;
                                                % the quantity initiated before the beginning of the loop
                                                %
                                else            % if he doesn't see the left two corners and the right upper corner,
                                                %  he checks if he sees the right lower corner
                                    x_ic_right_lower=(BwallRight(y_ped(i))-x_ped_old);
                                    y_ic_right_lower=(B_wall_ymin-y_ped_old);
                                    r_ic_right_lower=sqrt(x_ic_right_lower^2+y_ic_right_lower^2);
                                                % {x,y}-coordinates and the distance from i-th pedestrian 
                                                %  to the right lower corner  [m]
                                    cos_angle_ic_right_lower=(ex_ped(i)*x_ic_right_lower+...
                                                              ey_ped(i)*y_ic_right_lower)/r_ic_right_lower;
                                                % cosine of the angle between the ped's direction of motion and the
                                                %  vector connecting him to the right lower corner
                                    if r_ic_right_lower < ped_react_far_dist_max(i) && ...
                                       cos_angle_ic_right_lower > cos_ped_seeing_angle  && ...
                                       r_ic_right_lower > ped_react_far_dist_min(i)
                                                % if all those conditions apply, the ped sees the right lower corner
                                                %  and turns away from it
                                        dphi_sign=-sign(ex_ped(i)*y_ic_right_lower-ey_ped(i)*x_ic_right_lower);
                                                % this is the sign of the angle of rotation; it determines to which
                                                %  side the ped's direction vector is rotated at the next step
                                        dphi=min(dphi_magnitude_max,ped_seeing_angle-...
                                                 acos(cos_angle_ic_right_lower/(1+EPS)))*dphi_sign;
                                                % this is the rotation angle
                                        what_ped_has_seen(i,N_attractions+N_columns+2)=1;
                                        what_ped_has_seen(i,N_attractions+N_columns+3)=1;
                                                % the quantity initiated before the beginning of the loop
                                                %
                                    else        % in this case, the ped sees none of the corners, and so he now
                                                %  determines whether he sees any of the walls, as was defined above
                                        if aux_index_ib_left(i) == 1 && ...
                                           -ex_ped(i) > cos_ped_seeing_angle && ...
                                           ped_dist_from_left(i) < ped_react_far_dist_max(i)  && ...
                                           ped_dist_from_left(i) > ped_react_far_dist_min(i)
                                                % the ped sees the left wall
                                            dphi_sign=-sign(ey_ped(i));
                                                % this is the sign of the angle of rotation; it determines to which
                                                %  side the ped's direction vector is rotated at the next step
                                            dphi=min(dphi_magnitude_max,ped_seeing_angle-acos(-ex_ped(i)/(1+EPS)))...
                                                 *dphi_sign;
                                                % this is the rotation angle
                                            what_ped_has_seen(i,N_attractions+N_columns+1)=1;
                                                % the quantity initiated before the beginning of the loop
                                        elseif aux_index_ib_right(i) == 1 && ...
                                               ex_ped(i) > cos_ped_seeing_angle && ...
                                               ped_dist_from_right(i) < ped_react_far_dist_max(i)  && ...
                                               ped_dist_from_right(i) > ped_react_far_dist_min(i)
                                                % the ped sees the right wall
                                            dphi_sign=sign(ey_ped(i));
                                                % this is the sign of the angle of rotation; it determines to which
                                                %  side the ped's direction vector is rotated at the next step
                                            dphi=min(dphi_magnitude_max,ped_seeing_angle-acos(ex_ped(i)/(1+EPS)))...
                                                 *dphi_sign;
                                                % this is the rotation angle
                                            what_ped_has_seen(i,N_attractions+N_columns+2)=1;
                                                % the quantity initiated before the beginning of the loop
                                        elseif aux_index_ib_lower(i) == 1 && ...
                                               -ey_ped(i) > cos_ped_seeing_angle && ...
                                               ped_dist_from_lower(i) < ped_react_far_dist_max(i)  && ...
                                               ped_dist_from_lower(i) > ped_react_far_dist_min(i)
                                                % the ped sees the lower wall
                                            dphi_sign=sign(ex_ped(i));
                                                % this is the sign of the angle of rotation; it determines to which
                                                %  side the ped's direction vector is rotated at the next step
                                            dphi=min(dphi_magnitude_max,ped_seeing_angle-acos(-ey_ped(i)/(1+EPS)))...
                                                 *dphi_sign;
                                                % this is the rotation angle
                                            what_ped_has_seen(i,N_attractions+N_columns+3)=1;
                                                % the quantity initiated before the beginning of the loop
                                        elseif aux_index_ib_upper(i) == 1 && ...
                                               ey_ped(i) > cos_ped_seeing_angle && ...
                                               ped_dist_from_upper(i) < ped_react_far_dist_max(i)  && ...
                                               ped_dist_from_upper(i) > ped_react_far_dist_min(i)
                                                % the ped sees the upper wall
                                            dphi_sign=-sign(ex_ped(i));
                                                % this is the sign of the angle of rotation; it determines to which
                                                %  side the ped's direction vector is rotated at the next step
                                            dphi=min(dphi_magnitude_max,ped_seeing_angle-acos(-ey_ped(i)/(1+EPS)))...
                                                 *dphi_sign;
                                                % this is the rotation angle
                                            what_ped_has_seen(i,N_attractions+N_columns+4)=1;
                                                % the quantity initiated before the beginning of the loop
                                        end     % end of loop which determines if the ped sees any of the walls
                                    end
                                end
                            end
                        end
                    end
                end                             % end of loop which decides whether the ped changes his direction at all
                cos_dphi=cos(dphi);
                sin_dphi=sin(dphi);
                ex_ped_old=ex_ped(i);
                ey_ped_old=ey_ped(i);
                ex_ped(i)=ex_ped_old*cos_dphi-ey_ped_old*sin_dphi;
                ey_ped(i)=ex_ped_old*sin_dphi+ey_ped_old*cos_dphi;
                                                % these are the new coordinates of the direction vector
                % Now change the direction of the ped's velocity in accordance with his new direction vector:
                vx_ped(i)=ex_ped(i)*speed_ped(i);
                vy_ped(i)=ey_ped(i)*speed_ped(i);
                % 
                % Now individually update the components of the velocity, based on the forces acting on the ped:
                vx_ped(i)=(vx_ped(i) + dt_after_reshuffling(i)*(sum(Fx_ij(i,:)+Nx_ij(i,:)+Tx_ij(i,:))...
                          + (Fx_ib_wall_left(i)+Fx_ib_wall_right(i)+Fx_ib_wall_lower(i)+Fx_ib_wall_upper(i))...
                          + (Nx_ib_wall_left(i)+Nx_ib_wall_right(i)+Nx_ib_wall_lower(i)+Nx_ib_wall_upper(i))...
                          + (Tx_ib_wall_left(i)+Tx_ib_wall_right(i)+Tx_ib_wall_lower(i)+Tx_ib_wall_upper(i))...
                          + sum(Fx_ib_column(i,:)+Nx_ib_column(i,:)+Tx_ib_column(i,:))...
                          + Fx_ia(i)*(1+excitement_ped(i))*(1-ped_attr_loc_memory(i)))/ped_mass(i)...
                          + 1*dt_after_reshuffling(i)*noise_vx(i))*exp(-dt_after_reshuffling(i)/ped_tau_reaction(i))...
                          + (2*Vx_pref(i)-Vx_pref_prevstep(i))*(1-exp(-dt_after_reshuffling(i)/ped_tau_reaction(i)));
                vy_ped(i)=(vy_ped(i) + dt_after_reshuffling(i)*(sum(Fy_ij(i,:)+Ny_ij(i,:)+Ty_ij(i,:))...
                          + (Fy_ib_wall_left(i)+Fy_ib_wall_right(i)+Fy_ib_wall_lower(i)+Fy_ib_wall_upper(i))...
                          + (Ny_ib_wall_left(i)+Ny_ib_wall_right(i)+Ny_ib_wall_lower(i)+Ny_ib_wall_upper(i))...
                          + (Ty_ib_wall_left(i)+Ty_ib_wall_right(i)+Ty_ib_wall_lower(i)+Ty_ib_wall_upper(i))...
                          + sum(Fy_ib_column(i,:)+Ny_ib_column(i,:)+Ty_ib_column(i,:))...
                          + Fy_ia(i)*(1+excitement_ped(i))*(1-ped_attr_loc_memory(i)))/ped_mass(i)...
                          + 1*dt_after_reshuffling(i)*noise_vy(i))*exp(-dt_after_reshuffling(i)/ped_tau_reaction(i))...
                          + (2*Vy_pref(i)-Vy_pref_prevstep(i))*(1-exp(-dt_after_reshuffling(i)/ped_tau_reaction(i)));
                % Update the ped's independence factor based on his proximity to the exit:
                ped_independence(i)=(ped_independence_max(i)-ped_independence_min(i))*...
                                    exp(-r_ia(i,strongest_attraction_index(i))/ped_attraction_dist(i))+...
                                    ... % NEW IDEA:  ped_attr_loc_memory(i,strongest_attraction_index(i)) + ...
                                    ped_independence_min(i);
                % Update the ped's ideally-desired velocity based on his proximity to an exit or a boundary:
%                 aux_dist_ia_or_ib=min([(r_ia(i,strongest_attraction_index(i))-ped_radius(i))/...
%                                         (weight_attraction(i,strongest_attraction_index(i))+ped_b_rep_close_percep_back_min),...
%                                        min((r_ib_column(i,:)-pedCol_radius(i,:))./...
%                                            (weight_close_column(i,:)+ped_b_rep_close_percep_back_min)),...
%                                        (ped_dist_from_left(i)-ped_radius(i))/...
%                                         (weight_close_wall_left(i)+ped_b_rep_close_percep_back_min),...
%                                        (ped_dist_from_right(i)-ped_radius(i))/...
%                                         (weight_close_wall_right(i)+ped_b_rep_close_percep_back_min),...
%                                        (ped_dist_from_lower(i)-ped_radius(i))/...
%                                         (weight_close_wall_lower(i)+ped_b_rep_close_percep_back_min),...
%                                        (ped_dist_from_upper(i)-ped_radius(i))/...
%                                         (weight_close_wall_upper(i)+ped_b_rep_close_percep_back_min)]);
%
%
                aux_dist_ia_or_ib=min([min((r_ib_column(i,:).*aux_index_ib_column(i,:)+...
                                           (BwallRight(y_ped(i))-B_wall_xmin)/2*(1-aux_index_ib_column(i,:))-pedCol_radius_min(i,:))./...
                                           (weight_close_column(i,:)+ped_b_rep_close_percep_back_min)),...
                                       (ped_dist_from_left(i)*aux_index_ib_left(i)+...
                                        (BwallRight(y_ped(i))-B_wall_xmin)/2*(1-aux_index_ib_left(i))-ped_radius_min(i))/...
                                        (weight_close_wall_left(i)+ped_b_rep_close_percep_back_min),...
                                       (ped_dist_from_right(i)*aux_index_ib_right(i)+...
                                        (BwallRight(y_ped(i))-B_wall_xmin)/2*(1-aux_index_ib_right(i))-ped_radius_min(i))/...
                                        (weight_close_wall_right(i)+ped_b_rep_close_percep_back_min),...
                                       (ped_dist_from_lower(i)*aux_index_ib_lower(i)+...
                                        (B_wall_ymax-B_wall_ymin)/2*(1-aux_index_ib_lower(i))-ped_radius_min(i))/...
                                        (weight_close_wall_lower(i)+ped_b_rep_close_percep_back_min),...
                                       (ped_dist_from_upper(i)*aux_index_ib_upper(i)+...
                                        (B_wall_ymax-B_wall_ymin)/2*(1-aux_index_ib_upper(i))-ped_radius_min(i))/...
                                        (weight_close_wall_upper(i)+ped_b_rep_close_percep_back_min)]);
                                                % this complex construction with aux_index_ib_{...} is used so as to
                                                %  neglect the contribution of the corresponding term(s) when the ped
                                                %  is too far from the boundary(ies)
%                 ped_speed0(i)=-(ped_speed0_max(i)-ped_speed0_min(i))*...
%                               max(exp(-aux_dist_ia_or_ib/ped_speedcorrection_dist),aux_crowddensity(i))+...
%                               ped_speed0_max(i);
                delta_ped_rep_close_max(i)=(delta_ped_rep_close_max(i)+0.3*excitement_ped(i)*(2*rand-1)*dt_after_reshuffling(i)/ped_tau_reaction(i))*exp(-dt_after_reshuffling(i)/ped_tau_reaction(i));
                ped_rep_close_max(i)=(Ped_rep_close_max_f2b_min+aux_crowddensity(i)*(Ped_rep_close_max_f2b_max-Ped_rep_close_max_f2b_min))*(1+delta_ped_rep_close_max(i));
                ped_rep_close_max_ratio(i)=(Ped_rep_close_max_f2f_min+...
                                            aux_crowddensity(i)*(Ped_rep_close_max_f2f_max-Ped_rep_close_max_f2f_min))/...
                                           (Ped_rep_close_max_f2b_min+...
                                            aux_crowddensity(i)*(Ped_rep_close_max_f2b_max-Ped_rep_close_max_f2b_min));
                ped_b_rep_close_max(i)=(Ped_b_rep_close_max_b2w_min + exp(-aux_dist_ia_or_ib/ped_speedcorrection_dist)*...
                                        (Ped_b_rep_close_max_b2w_max-Ped_b_rep_close_max_b2w_min))*...
                                        (1+delta_ped_rep_close_max(i));
                ped_b_rep_close_max_ratio(i)=(Ped_b_rep_close_max_f2w_min + exp(-aux_dist_ia_or_ib/ped_speedcorrection_dist)*...
                                              (Ped_b_rep_close_max_f2w_max-Ped_b_rep_close_max_f2w_min))/...
                                             (Ped_b_rep_close_max_b2w_min + exp(-aux_dist_ia_or_ib/ped_speedcorrection_dist)*...
                                              (Ped_b_rep_close_max_b2w_max-Ped_b_rep_close_max_b2w_min));
                
                ped_tau_reaction(i)=Ped_tau_reaction_av_max-...
                                    exp(-(r_ia(i,strongest_attraction_index(i))-ped_radius(i))/ped_speedcorrection_dist)*...
                                    (Ped_tau_reaction_av_max-Ped_tau_reaction_av_min);
                                                
                                                
                % Now update the perception angles (in  [rad])  and backward perceptions by each pedestrian
                %  of attractions and obstacles. This is done as follows. 
                %  The perception angles are assumed to satisfy the evolutions of the form:
                %      d(percep_angle)/dt=[-(percep_angle-percep_angle_min)+(pi-percep_angle_min)*WPHS]/tau_loc_memory,
                %  where  WPHS  is the value (0 or 1 at each given moment of calculations) of   what_ped_has_seen
                %  for this particular ped and attraction/obstacle.
                %  They are also forced to be between  ped_rep_close_percep_angle_min  and  pi.
                %  Then, the backward perceptions are defined by the formulae:
                %     percep_back=max(percep_back_min,-cos(percep_angle)),
                %  which becomes plausible if one graphs it.
                % MOREOVER, in addition to updating the perception angle of the strongest attraction, the pedestrian
                %  also gains memory of the direction towards it, which he uses when calculating the individual part
                %  of his preferred velocity. Such a memory of _obstacles_ is not gained in the framework of this code.
% % %                 aux_exp=exp(-dt/ped_tau_loc_memory(i));
% % %                 aux_dangle=dt/ped_tau_loc_memory(i)*(pi-ped_rep_close_percep_angle_min);
% % %                                                 % it is convenient to define these quantities,
% % %                                                 %  which are used in many places immediately below
% % %                 for k=1:N_attractions
% % %                     if ped_gave_up(i) == 0      % i.e. if the ped hasn't given up his strongest attraction
% % %                         % Update the perception angle and perception in the backward direction:
% % %                         ped_attraction_percep_angle(i,k)=(ped_attraction_percep_angle(i,k)+...
% % %                                                           aux_dangle*what_ped_has_seen(i,k))*aux_exp+...
% % %                                                           ped_rep_close_percep_angle_min*(1-aux_exp);
% % %                         ped_attraction_percep_angle(i,k)=min(pi,ped_attraction_percep_angle(i,k));
% % %                                                 % make sure the angle doesn't get greater than  pi
% % %                         ped_attraction_percep_angle(i,k)=max(ped_rep_close_percep_angle_min,ped_attraction_percep_angle(i,k));
% % %                                                 % make sure the angle doesn't get less than  ped_rep_close_percep_angle_min
% % %                         ped_attraction_percep_back(i,k)=max(ped_b_rep_close_percep_back_min,...
% % %                                                             -cos(ped_attraction_percep_angle(i,k)));
% % %                                                 % backward perception of the attraction
% % %                         % Update the memory of the location of an attraction:
% % %                         ped_attr_loc_memory(i,k)=(ped_attr_loc_memory(i,k)+dt/ped_tau_loc_memory(i)*...
% % %                                                           what_ped_has_seen(i,k))*aux_exp;
% % %                         ped_attr_loc_memory(i,k)=min(1,ped_attr_loc_memory(i,k));
% % %                         ped_attr_loc_memory(i,k)=max(0,ped_attr_loc_memory(i,k));
% % %                                                 % make sure that the memory variable is between 0 and 1
% % %                     else                        % i.e. if the ped has given up his strongest attraction,
% % %                                                 %  reset his perception abilities to their min values and
% % %                                                 %  reset his memory of the previously-strongest attraction to 0
% % %                         ped_attraction_percep_angle(i,k)=ped_rep_close_percep_angle_min;
% % %                         ped_attraction_percep_back(i,k)=ped_b_rep_close_percep_back_min;
% % %                         ped_attr_loc_memory(i,k)=0;
% % %                     end
% % %                 end
% % %                 for k=1:N_columns
% % %                     ped_column_percep_angle(i,k)=(ped_column_percep_angle(i,k)+...
% % %                                                   aux_dangle*what_ped_has_seen(i,N_attractions+k))*aux_exp+...
% % %                                                   ped_rep_close_percep_angle_min*(1-aux_exp);
% % %                     ped_column_percep_angle(i,k)=min(pi,ped_column_percep_angle(i,k));
% % %                                                 % make sure the angle doesn't get greater than  pi
% % %                     ped_column_percep_angle(i,k)=max(ped_rep_close_percep_angle_min,ped_column_percep_angle(i,k));
% % %                                                 % make sure the angle doesn't get less than  ped_rep_close_percep_angle_min
% % %                     ped_column_percep_back(i,k)=max(ped_b_rep_close_percep_back_min,...
% % %                                                         -cos(ped_column_percep_angle(i,k)));
% % %                                                 % backward perception of the column
% % %                 end
% % %                 ped_left_wall_percep_angle(i)=(ped_left_wall_percep_angle(i)+...
% % %                                                   aux_dangle*what_ped_has_seen(i,N_attractions+N_columns+1))*aux_exp+...
% % %                                                   ped_rep_close_percep_angle_min*(1-aux_exp);
% % %                 ped_left_wall_percep_angle(i)=min(pi,ped_left_wall_percep_angle(i));
% % %                                                 % make sure the angle doesn't get greater than  pi
% % %                 ped_left_wall_percep_angle(i)=max(ped_rep_close_percep_angle_min,ped_left_wall_percep_angle(i));
% % %                                                 % make sure the angle doesn't get less than  ped_rep_close_percep_angle_min
% % %                 ped_left_wall_percep_back(i)=max(ped_b_rep_close_percep_back_min, -cos(ped_left_wall_percep_angle(i)));
% % %                                                 % backward perception of the left wall
% % %                 %
% % %                 ped_right_wall_percep_angle(i)=(ped_right_wall_percep_angle(i)+...
% % %                                                   aux_dangle*what_ped_has_seen(i,N_attractions+N_columns+2))*aux_exp+...
% % %                                                   ped_rep_close_percep_angle_min*(1-aux_exp);
% % %                 ped_right_wall_percep_angle(i)=min(pi,ped_right_wall_percep_angle(i));
% % %                                                 % make sure the angle doesn't get greater than  pi
% % %                 ped_right_wall_percep_angle(i)=max(ped_rep_close_percep_angle_min,ped_right_wall_percep_angle(i));
% % %                                                 % make sure the angle doesn't get less than  ped_rep_close_percep_angle_min
% % %                 ped_right_wall_percep_back(i)=max(ped_b_rep_close_percep_back_min, -cos(ped_right_wall_percep_angle(i)));
% % %                                                 % backward perception of the right wall
% % %                 %
% % %                 ped_lower_wall_percep_angle(i)=(ped_lower_wall_percep_angle(i)+...
% % %                                                   aux_dangle*what_ped_has_seen(i,N_attractions+N_columns+3))*aux_exp+...
% % %                                                   ped_rep_close_percep_angle_min*(1-aux_exp);
% % %                 ped_lower_wall_percep_angle(i)=min(pi,ped_lower_wall_percep_angle(i));
% % %                                                 % make sure the angle doesn't get greater than  pi
% % %                 ped_lower_wall_percep_angle(i)=max(ped_rep_close_percep_angle_min,ped_lower_wall_percep_angle(i));
% % %                                                 % make sure the angle doesn't get less than  ped_rep_close_percep_angle_min
% % %                 ped_lower_wall_percep_back(i)=max(ped_b_rep_close_percep_back_min, -cos(ped_lower_wall_percep_angle(i)));
% % %                                                 % backward perception of the lower wall
% % %                 %
% % %                 ped_upper_wall_percep_angle(i)=(ped_upper_wall_percep_angle(i)+...
% % %                                                   aux_dangle*what_ped_has_seen(i,N_attractions+N_columns+4))*aux_exp+...
% % %                                                   ped_rep_close_percep_angle_min*(1-aux_exp);
% % %                 ped_upper_wall_percep_angle(i)=min(pi,ped_upper_wall_percep_angle(i));
% % %                                                 % make sure the angle doesn't get greater than  pi
% % %                 ped_upper_wall_percep_angle(i)=max(ped_rep_close_percep_angle_min,ped_upper_wall_percep_angle(i));
% % %                                                 % make sure the angle doesn't get less than  ped_rep_close_percep_angle_min
% % %                 ped_upper_wall_percep_back(i)=max(ped_b_rep_close_percep_back_min, -cos(ped_upper_wall_percep_angle(i)));
% % %                                                 % backward perception of the upper wall
% % %                 %
                
                aux_dangle=dt_after_reshuffling(i)*(pi-ped_rep_close_percep_angle_min);
                                                % it is convenient to define this quantity,
                                                %  which are used in many places immediately below  
                for k=1:N_attractions
                    ped_tau_loc_attr_memory=ped_tau_loc_memory_forget(i)-...
                                            (ped_tau_loc_memory_forget(i)-ped_tau_loc_memory_learn(i))*...
                                            sign(what_ped_has_seen(i,k));
                    if ped_gave_up(i) == 0      % i.e. if the ped hasn't given up his strongest attraction
                        % Update the perception angle and perception in the backward direction:
                        ped_attraction_percep_angle(i,k)=((ped_attraction_percep_angle(i,k)-ped_rep_close_percep_angle_min)+...
                                                          aux_dangle/ped_tau_loc_attr_memory*what_ped_has_seen(i,k))*...
                                                          exp(-dt_after_reshuffling(i)/ped_tau_loc_attr_memory) + ...
                                                          ped_rep_close_percep_angle_min;
                        ped_attraction_percep_angle(i,k)=min(pi,ped_attraction_percep_angle(i,k));
                                                % make sure the angle doesn't get greater than  pi
                        ped_attraction_percep_angle(i,k)=max(ped_rep_close_percep_angle_min,ped_attraction_percep_angle(i,k));
                                                % make sure the angle doesn't get less than  ped_rep_close_percep_angle_min
                        ped_attraction_percep_back(i,k)=max(ped_b_rep_close_percep_back_min,...
                                                            -cos(ped_attraction_percep_angle(i,k)));
                                                % backward perception of the attraction
                        % Update the memory of the location of an attraction:
                        ped_attr_loc_memory(i,k)=(ped_attr_loc_memory(i,k)+dt_after_reshuffling(i)/ped_tau_loc_attr_memory*...
                                                          what_ped_has_seen(i,k))*...
                                                          exp(-dt_after_reshuffling(i)/ped_tau_loc_attr_memory);
                        ped_attr_loc_memory(i,k)=min(1,ped_attr_loc_memory(i,k));
                        ped_attr_loc_memory(i,k)=max(0,ped_attr_loc_memory(i,k));
                                                % make sure that the memory variable is between 0 and 1
                        %
                    else                        % i.e. if the ped has given up his strongest attraction,
                                                %  reset his perception abilities to their min values and
                                                %  reset his memory of the previously-strongest attraction to 0
                        ped_attraction_percep_angle(i,k)=ped_rep_close_percep_angle_min;
                        ped_attraction_percep_back(i,k)=ped_b_rep_close_percep_back_min;
                        ped_attr_loc_memory(i,k)=0;
                    end
                end
                for k=1:N_columns
                    ped_tau_loc_column_memory=ped_tau_loc_memory_forget(i)-...
                                              (ped_tau_loc_memory_forget(i)-ped_tau_loc_memory_learn(i))*...
                                              sign(what_ped_has_seen(i,N_attractions+k));
                    %
                    ped_column_percep_angle(i,k)=((ped_column_percep_angle(i,k)-ped_rep_close_percep_angle_min)+...
                                                  aux_dangle/ped_tau_loc_column_memory*what_ped_has_seen(i,N_attractions+k))*...
                                                  exp(-dt_after_reshuffling(i)/ped_tau_loc_column_memory)+... 
                                                  ped_rep_close_percep_angle_min;
                    ped_column_percep_angle(i,k)=min(pi,ped_column_percep_angle(i,k));
                                                % make sure the angle doesn't get greater than  pi
                    ped_column_percep_angle(i,k)=max(ped_rep_close_percep_angle_min,ped_column_percep_angle(i,k));
                                                % make sure the angle doesn't get less than  ped_rep_close_percep_angle_min
                    ped_column_percep_back(i,k)=max(ped_b_rep_close_percep_back_min,...
                                                        -cos(ped_column_percep_angle(i,k)));
                                                % backward perception of the column
                end
                ped_tau_loc_left_wall_memory=ped_tau_loc_memory_forget(i)-...
                                              (ped_tau_loc_memory_forget(i)-ped_tau_loc_memory_learn(i))*...
                                              sign(what_ped_has_seen(i,N_attractions+N_columns+1));
                ped_left_wall_percep_angle(i)=( (ped_left_wall_percep_angle(i)-ped_rep_close_percep_angle_min)+...
                                               aux_dangle/ped_tau_loc_left_wall_memory*...
                                               what_ped_has_seen(i,N_attractions+N_columns+1))*...
                                               exp(-dt_after_reshuffling(i)/ped_tau_loc_left_wall_memory)+...
                                               ped_rep_close_percep_angle_min;
                ped_left_wall_percep_angle(i)=min(pi,ped_left_wall_percep_angle(i));
                                                % make sure the angle doesn't get greater than  pi
                ped_left_wall_percep_angle(i)=max(ped_rep_close_percep_angle_min,ped_left_wall_percep_angle(i));
                                                % make sure the angle doesn't get less than  ped_rep_close_percep_angle_min
                ped_left_wall_percep_back(i)=max(ped_b_rep_close_percep_back_min, -cos(ped_left_wall_percep_angle(i)));
                                                % backward perception of the left wall
                %
                ped_tau_loc_right_wall_memory=ped_tau_loc_memory_forget(i)-...
                                              (ped_tau_loc_memory_forget(i)-ped_tau_loc_memory_learn(i))*...
                                              sign(what_ped_has_seen(i,N_attractions+N_columns+2));
                ped_right_wall_percep_angle(i)=( (ped_right_wall_percep_angle(i)-ped_rep_close_percep_angle_min) +...
                                                  aux_dangle/ped_tau_loc_right_wall_memory*...
                                                  what_ped_has_seen(i,N_attractions+N_columns+2))*...
                                                  exp(-dt_after_reshuffling(i)/ped_tau_loc_right_wall_memory)+...
                                                  ped_rep_close_percep_angle_min;
                ped_right_wall_percep_angle(i)=min(pi,ped_right_wall_percep_angle(i));
                                                % make sure the angle doesn't get greater than  pi
                ped_right_wall_percep_angle(i)=max(ped_rep_close_percep_angle_min,ped_right_wall_percep_angle(i));
                                                % make sure the angle doesn't get less than  ped_rep_close_percep_angle_min
                ped_right_wall_percep_back(i)=max(ped_b_rep_close_percep_back_min, -cos(ped_right_wall_percep_angle(i)));
                                                % backward perception of the right wall
                %
                ped_tau_loc_lower_wall_memory=ped_tau_loc_memory_forget(i)-...
                                              (ped_tau_loc_memory_forget(i)-ped_tau_loc_memory_learn(i))*...
                                              sign(what_ped_has_seen(i,N_attractions+N_columns+3));
                ped_lower_wall_percep_angle(i)=( (ped_lower_wall_percep_angle(i)-ped_rep_close_percep_angle_min) +...
                                                  aux_dangle/ped_tau_loc_lower_wall_memory*...
                                                  what_ped_has_seen(i,N_attractions+N_columns+3))*...
                                                  exp(-dt_after_reshuffling(i)/ped_tau_loc_lower_wall_memory)+...
                                                  ped_rep_close_percep_angle_min;
                ped_lower_wall_percep_angle(i)=min(pi,ped_lower_wall_percep_angle(i));
                                                % make sure the angle doesn't get greater than  pi
                ped_lower_wall_percep_angle(i)=max(ped_rep_close_percep_angle_min,ped_lower_wall_percep_angle(i));
                                                % make sure the angle doesn't get less than  ped_rep_close_percep_angle_min
                ped_lower_wall_percep_back(i)=max(ped_b_rep_close_percep_back_min, -cos(ped_lower_wall_percep_angle(i)));
                                                % backward perception of the lower wall
                %
                ped_tau_loc_upper_wall_memory=ped_tau_loc_memory_forget(i)-...
                                              (ped_tau_loc_memory_forget(i)-ped_tau_loc_memory_learn(i))*...
                                              sign(what_ped_has_seen(i,N_attractions+N_columns+4));
                ped_upper_wall_percep_angle(i)=( (ped_upper_wall_percep_angle(i)-ped_rep_close_percep_angle_min) +...
                                                  aux_dangle/ped_tau_loc_upper_wall_memory*...
                                                  what_ped_has_seen(i,N_attractions+N_columns+4))*...
                                                  exp(-dt_after_reshuffling(i)/ped_tau_loc_upper_wall_memory)+...
                                                  ped_rep_close_percep_angle_min;
                ped_upper_wall_percep_angle(i)=min(pi,ped_upper_wall_percep_angle(i));
                                                % make sure the angle doesn't get greater than  pi
                ped_upper_wall_percep_angle(i)=max(ped_rep_close_percep_angle_min,ped_upper_wall_percep_angle(i));
                                                % make sure the angle doesn't get less than  ped_rep_close_percep_angle_min
                ped_upper_wall_percep_back(i)=max(ped_b_rep_close_percep_back_min, -cos(ped_upper_wall_percep_angle(i)));
                                                % backward perception of the upper wall
                %

                
            % 
            %
            % THIS BLOCK IS FOR DEBUGGING PURPOSES ONLY:
            if imag(vx_ped(i)) ~= 0 | imag(vy_ped(i)) ~= 0 | imag(ex_ped(i)) ~= 0 | imag(ey_ped(i)) ~= 0 | ...
               imag(x_ped(i)) ~= 0 | imag(y_ped(i)) ~= 0
                i
                disp('Warning:  One of the peds has a complex coordinate or velocity ! ')
                unknown_variable
            end

            
            
            % Now we go back to describing the behavior of peds who are in the process of exiting through the doors:
            %    
            elseif ped_within_left_door(i) ~= 0 % recall now that the quantity   ped_within_left_door(i)  is the
                                                %  sequential # of the attraction which the given door is, and it
                                                %  also equals the sequential # of the door on the left wall through
                                                %  which the pedestrian is exiting the room
                Ny_ib(i)=ped_withindoor_elasticity*...
                         (max(ped_radius(i)-(y_ped(i)-Door_left_low(ped_within_left_door(i))),0)-...
                          max(ped_radius(i)-(Door_left_up(ped_within_left_door(i))-y_ped(i)),0));
                                                % physical force of repulsion of the pedestrian from door posts  [N]
                Tx_ib(i)=-sign(vx_ped(i))*ped_friction*abs(Ny_ib(i));
                                                % friction force between the pedestrian and door posts [N]
                vx_ped(i)=vx_ped(i) + dt_after_reshuffling(i)*(sum(Nx_ij(i,:)+Tx_ij(i,:)) + Tx_ib(i)...
                          - ped_attraction_strength(i,ped_within_left_door(i))*(1+excitement_ped(i)))/ped_mass(i)...
                          + 0*dt_after_reshuffling(i)*noise_vx(i);
                vy_ped(i)=vy_ped(i) + dt_after_reshuffling(i)*(sum(Ny_ij(i,:)+Ty_ij(i,:)) + Ny_ib(i))/ped_mass(i)...
                          + 0*dt_after_reshuffling(i)*noise_vy(i);
                ped_independence(i)=1;
                ped_speed0(i)=ped_speed0_min(i);
            elseif ped_within_right_door(i) ~= 0 % recall now that the quantity   ped_within_right_door(i)+N_doors_left
                                                %  is the sequential # of the attraction which the given door is, and
                                                %  ped_within_right_door(i)  also equals the sequential # of the door on 
                                                %  the right wall through which the pedestrian is exiting the room
                Ny_ib(i)=ped_withindoor_elasticity*...
                         (max(ped_radius(i)-(y_ped(i)-Door_right_low(ped_within_right_door(i))),0)-...
                          max(ped_radius(i)-(Door_right_up(ped_within_right_door(i))-y_ped(i)),0));
                                                % physical force of repulsion of the pedestrian from door posts  [N]
                Tx_ib(i)=-sign(vx_ped(i))*ped_friction*abs(Ny_ib(i));
                                                % friction force between the pedestrian and door posts [N]
                vx_ped(i)=vx_ped(i) + dt_after_reshuffling(i)*(sum(Nx_ij(i,:)+Tx_ij(i,:)) + Tx_ib(i)...
                          + ped_attraction_strength(i,ped_within_right_door(i)+N_doors_left)*(1+excitement_ped(i)))/ped_mass(i)...
                          + 0*dt_after_reshuffling(i)*noise_vx(i);
                vy_ped(i)=vy_ped(i) + dt_after_reshuffling(i)*(sum(Ny_ij(i,:)+Ty_ij(i,:)) + Ny_ib(i))/ped_mass(i)...
                          + 0*dt_after_reshuffling(i)*noise_vy(i);
                ped_independence(i)=1;
                ped_speed0(i)=ped_speed0_min(i);
            elseif ped_within_lower_door(i) ~= 0 % recall now that the quantity   ped_within_lower_door(i)+N_doors_{left+right}
                                                %  is the sequential # of the attraction which the given door is, and
                                                %  ped_within_lower_door(i)  also equals the sequential # of the door on 
                                                %  the lower wall through which the pedestrian is exiting the room
                Nx_ib(i)=ped_withindoor_elasticity*...
                         (max(ped_radius(i)-(x_ped(i)-Door_lower_left(ped_within_lower_door(i))),0)-...
                          max(ped_radius(i)-(Door_lower_right(ped_within_lower_door(i))-x_ped(i)),0));
                                                % physical force of repulsion of the pedestrian from door posts  [N]
                Ty_ib(i)=-sign(vy_ped(i))*ped_friction*abs(Nx_ib(i));
                                                % friction force between the pedestrian and door posts [N]
                vx_ped(i)=vx_ped(i) + dt_after_reshuffling(i)*(sum(Nx_ij(i,:)+Tx_ij(i,:)) + Nx_ib(i))/ped_mass(i)...
                          + 0*dt_after_reshuffling(i)*noise_vx(i);
                vy_ped(i)=vy_ped(i) + dt_after_reshuffling(i)*(sum(Ny_ij(i,:)+Ty_ij(i,:)) + Ty_ib(i)...
                          -ped_attraction_strength(i,ped_within_lower_door(i)+N_doors_left+N_doors_right)...
                          *(1+excitement_ped(i)))/ped_mass(i)...
                          + 0*dt_after_reshuffling(i)*noise_vy(i);
                ped_independence(i)=1;
                ped_speed0(i)=ped_speed0_min(i);
            elseif ped_within_upper_door(i) ~= 0 % recall now that the quantity   ped_within_upper_door(i)+N_doors_{left+right+
                                                %  lower}  is the sequential # of the attraction which the given door is, and
                                                %  ped_within_upper_door(i)  also equals the sequential # of the door on 
                                                %  the upper wall through which the pedestrian is exiting the room
                Nx_ib(i)=ped_withindoor_elasticity*...
                         (max(ped_radius(i)-(x_ped(i)-Door_upper_left(ped_within_upper_door(i))),0)-...
                          max(ped_radius(i)-(Door_upper_right(ped_within_upper_door(i))-x_ped(i)),0));
                                                % physical force of repulsion of the pedestrian from door posts  [N]
                Ty_ib(i)=-sign(vy_ped(i))*ped_friction*abs(Nx_ib(i));
                                                % friction force between the pedestrian and door posts [N]
                vx_ped(i)=vx_ped(i) + dt_after_reshuffling(i)*(sum(Nx_ij(i,:)+Tx_ij(i,:)) + Nx_ib(i))/ped_mass(i)...
                          + 0*dt_after_reshuffling(i)*noise_vx(i);
                vy_ped(i)=vy_ped(i) + dt_after_reshuffling(i)*(sum(Ny_ij(i,:)+Ty_ij(i,:)) + Ty_ib(i)...
                          + ped_attraction_strength(i,ped_within_upper_door(i)+N_doors_left+N_doors_right+N_doors_lower)...
                          *(1+excitement_ped(i)))/ped_mass(i)...
                          + 0*dt_after_reshuffling(i)*noise_vy(i);
                ped_independence(i)=1;
                ped_speed0(i)=ped_speed0_min(i);
            end
                
        end                                      % end of loop that decides if pedestrian is inside or outside the room
	end                                          % end of loop over all pedestrians
    
    
    
    
    
    
    
    
    
% PLOT MEMORY ETC FOR DEBUGING:
%
% %     if sum(aux_index_ped_inside) > 0
% %         ped_count=1:sum(aux_index_ped_inside);
% %         figure(1);
% %         plot(ped_count,ped_attr_loc_memory(aux_index_ped_inside),ped_count,what_ped_has_seen(aux_index_ped_inside,1),'r')
% %         axis([0 N_peds -0.1 1.1])
% %         pause(0.02)
% % %         figure(2); plot(ped_count,aux_speed_pref(aux_index_ped_inside),...
% % %                         ped_count,sqrt(Vx_pref(aux_index_ped_inside).^2+Vy_pref(aux_index_ped_inside).^2),'r')
% % %         axis([0 N_peds 0 2*Ped_speed0_max_av])
% %     end
    
    
    
    
    
    
    
    
    
    
    
    if time_the_code == 1
        toc
        
        disp('Compute the peds` speed and the direction vectors and update slowly-changing parameters:')
        tic
    end
    
    % ----------   Compute new values of components of the pedestrians' direction vectors, e{x,y}_ped, ----------
    % ---------------                  and record old ones for possible use.                     ----------------
    speed_ped_prev=speed_ped;                   % record velocities at previous step
    
    speed_ped=sqrt(vx_ped.^2+vy_ped.^2)+eps;    % magnitude of pedestrian's speed, used below  [m/s]
    % Make sure they don't move too fast, since if they do, then smth must wrong with the code:
    if max(speed_ped+speed_ped_prev)/2 > 3.5*Ped_speed0_max_av
        [this_is_the_max_actual_speed,i_fastest]=max(speed_ped);
        count_toofastpeds=count_toofastpeds+1;
        array_toofastpeds(count_toofastpeds,:)=[i_fastest,this_is_the_max_actual_speed,T_computed];
        %% disp('Warning:  Peds move too fast!  Stop and fix the problem!')
        %% unknown_variable
        if saved_to_errorlog_already == 1
            save(name_errorlog,'array_toofastpeds','-append')
        else
            save(name_errorlog,'array_toofastpeds')
        end
        saved_to_errorlog_already=1;
    end
    % Continue with calculation of e{x,y}:
   	for i=1:N_peds
        if aux_index_ped_inside(i) == 1
            if ped_within_left_door(i) ==0 && ped_within_right_door(i) ==0 && ...
               ped_within_lower_door(i) ==0 && ped_within_upper_door(i) ==0
                aux_ex_ped=((1+EPS-aux_crowddensity(i))*vx_ped(i)/speed_ped(i)+aux_crowddensity(i)*ex_collective(i))*...
                           (1-ped_attr_loc_memory(i))+...
                           nx_ia(i,strongest_attraction_index(i))*(EPS+ped_attr_loc_memory(i));
                aux_ey_ped=((1+EPS-aux_crowddensity(i))*vy_ped(i)/speed_ped(i)+aux_crowddensity(i)*ey_collective(i))*...
                           (1-ped_attr_loc_memory(i))+...
                           ny_ia(i,strongest_attraction_index(i))*(EPS+ped_attr_loc_memory(i));
                                                % EPS is added to avoid the following VERY RARE, but possible situation,
                                                %  which I actually ran into once:
                                                %  aux_crowddensity=1, ped_attr_loc_memory=0, e_collective=0 !
                                                %  (the 1st and 3rd facts became compatible because aux_density was updated
                                                %   on too slow a scale - 5*tau_reaction, and a ped has run away from the
                                                %   crowd still retaining the reduced perception distance that peds have in
                                                %   a crowd. To avoid this in the future, I now update   aux_density   every
                                                %   tau_reaction  seconds.)
                                                %   BUT note another case where it is possible to obtain the same situation:
                                                %   aux_crowddensity=1, ped_attr_loc_memory=0,  and  e_collective=0 because
                                                %   the ped is not advancing at this time step (he is being reshuffled) and
                                                %   hence his e_collective is automatically zeroed out!
                                                %  To avoid this altogether, I run an if-loop below:
                aux_eabs_ped=sqrt(aux_ex_ped^2+aux_ey_ped^2);
                if aux_eabs_ped > 0.0001        % change the direction vector of a ped only if the length of the auxiliary
                                                %  vector  aux_e_ped  is sufficiently large; otherwise, leave e_ped  the
                                                %  same as at the previous step
                    ex_ped(i)=aux_ex_ped/aux_eabs_ped;
                    ey_ped(i)=aux_ey_ped/aux_eabs_ped;
                end
            elseif ped_within_left_door(i) ==1
                ex_ped(i)=-1;
                ey_ped(i)=0;
            elseif ped_within_right_door(i) ==1
                ex_ped(i)=1;
                ey_ped(i)=0;
            elseif ped_within_lower_door(i) ==1
                ex_ped(i)=0;
                ey_ped(i)=-1;
            elseif ped_within_upper_door(i) ==1
                ex_ped(i)=0;
                ey_ped(i)=1;
            end
        end
	end 

    
    %   ----------   Update pedestrian parameters,  such as independence, perception, etc.,  ---------------------
    %   -------   but only after an interval ~ Ped_tau_reaction_av, rather than on every time step.   ----------
    %   -------   This is update is done based on the density of the crowd surrounding a given pedestrian. -------
    t_2update_peds=t_2update_peds+max(dt_after_reshuffling);
    if floor(t_2update_peds/Ped_tau_reaction_av_min) == 1
        for i=1:N_peds
            if aux_index_ped_inside(i) == 1 && ped_within_left_door(i) == 0 && ped_within_right_door(i) == 0 && ...
                                               ped_within_lower_door(i) == 0 && ped_within_upper_door(i) == 0
                aux_index_ij_i=aux_index_ij(i,:);
                % ped_crowddensity(i)=sum(ped_area(aux_index_ij_i))/(pi/4*(ped_percep_maxdist(i))^2);
                ped_crowddensity(i)= sum(aux_index_ij_i)/(pi/4*(min(ped_percep_maxdist(i),x_ped(i)-B_wall_xmin+EPS)+min(ped_percep_maxdist(i),BwallRight(y_ped(i))-x_ped(i)+EPS))*(min(ped_percep_maxdist(i),y_ped(i)-B_wall_ymin+EPS)+min(ped_percep_maxdist(i),B_wall_ymax-y_ped(i)+EPS)));
                                                % denssity of the surrounding crowd for each pedestrian  [ped/m^2];
                                                %  it's necessary to make sure that the ped is not within any door, 
                                                %  since then his distance from the wall becaomes negative
                aux_crowddensity(i)=min((ped_crowddensity(i))*4*ped_radius(i)^2, 1);
                                                % the quantity which we use to upgrade perception distances by a given ped
                                                %  of other peds;   
                                                %  by construction, it is not allowed to exceed 1;
                                                %  also, the exponent "4" is rather arbitrary and reflects the fact that
                                                %  as long as the density is low, it will not significantly affect
                                                %  the perception distances
%                 ped_tau_reaction(i)=ped_tau_reaction(i);
%                 ped_speed0(i)=ped_speed0(i);
%                 ped_independence(i)=ped_independence(i);
%                 ped_acceleration_fluct(i)=ped_acceleration_fluct(i);
%                  ped_rep_close_max(i)=Ped_rep_close_max*(1-aux_crowddensity(i))+...
%                                       Ped_rep_close_min*aux_crowddensity(i);
%                  ped_b_rep_close_max(i)=Ped_b_rep_close_max*(1-aux_crowddensity(i))+...
%                                         Ped_b_rep_close_min*aux_crowddensity(i);
%                 %%% ped_rep_close_dist(i)=ped_rep_close_dist(i);
%                 ped_rep_close_dist(i)=Ped_rep_close_dist_max*(1-aux_crowddensity(i))+...
%                                       Ped_rep_close_dist_min*aux_crowddensity(i);
%                 ped_b_rep_close_dist(i)=ped_b_rep_close_dist(i);
%                 ped_collective_percep_back(i)=ped_collective_percep_back(i);
%                 %%% ped_collective_percep_dist(i)=ped_collective_percep_dist(i);
                ped_collective_percep_dist(i)=Ped_collective_percep_dist_max*(1-aux_crowddensity(i))+...
                                              Ped_collective_percep_dist_min*aux_crowddensity(i);
                ped_percep_maxdist(i)=3*max(ped_rep_close_dist(i)/rep_close_exponent,ped_collective_percep_dist(i));
% 	            ped_b_percep_maxdist(i)=ped_b_percep_maxdist(i);
%                 ped_attraction_strength(i)=ped_attraction_strength(i);
%                 ped_attraction_dist(i)=ped_attraction_dist(i);
            end
        end
        t_2update_peds=0;                       % reset this time counter  [s]
    end
    
  	%
	% ------   Record old values of the preferred velocities, which are required at the next calculation step :  -------
	%%% ex_ped_prevstep=ex_ped;
	%%% ey_ped_prevstep=ey_ped;                      % directions of pedestrian's motion at the just-completed time step
    %
    Vx_pref_prevstep=Vx_pref;
    Vy_pref_prevstep=Vy_pref;                    % x- and y-components of the velocity with which each of the
                                                 %  pedestrians ideally wants to move  [m/s]
    
                                                 
    %  ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    

    if time_the_code == 1
        toc
        
        disp('Save the computed data into a file :')
        tic
    end
       
    % -------------------   Now save the computed positions of pedestrians to a file  -------------------
        
    % First save the data into arrays after every  ~dT_save  seconds:
    if floor((T_computed-T_saved)/dT_save) >= 1
        T_saved=T_computed;
        n_saved=n_saved+1;                      % increase the value of the counter which counts every instance
                                                %  of data saving
        x_ped_saved(n_saved,:)=x_ped;
        y_ped_saved(n_saved,:)=y_ped;           % save the pedestrian coordinates into arrays
        ex_ped_saved(n_saved,:)=ex_ped;
        ey_ped_saved(n_saved,:)=ey_ped;         % save the pedestrian directions of motion into arrays
        excitement_ped_saved(n_saved,:)=excitement_ped;
                                                % save excitement factors of all pedestrians
    end
    
    % Now save these arrays into a file upron completion of each part of the calculations:
    if increase_computed_part_number == 1
        thename=[aux_name int2str(n_computed_part-1)];
                                                % prepare the name for the file where the output will
                                                %  be saved
        save(thename,'x_ped_saved','y_ped_saved','ex_ped_saved','ey_ped_saved','excitement_ped_saved')
        clear x_ped_saved y_ped_saved ex_ped_saved ey_ped_saved excitement_ped_saved
        n_saved=0;

        % sprintf('  %d-th part of total %d parts of the simulation has been completed',n_computed_part-1,N_parts_of_calcs)
    end
    
    if floor(T_computed/dT_display_progress) >= n_displayed_part+1
        sprintf('  %d seconds of total %d seconds of the %d-th simulation has been completed',...
                 (n_displayed_part+1)*dT_display_progress,T_sim,J)
        n_displayed_part=n_displayed_part+1;
    end
    
    
    if time_the_code == 1
        toc
        
        disp('This completes timing of one time step !!!!!!!!!!!!!!!!!!!!!  Horray !!! ')
    end
        
    
    % Cleaning up garbage from the workspace:
    
    cwd = pwd;
    cd(tempdir);
    cd(cwd)

end                                             % end of the computational loop (while T_computed < T_sim)

clear x_ped y_ped vx_ped vy_ped array_toofastpeds array_toolarge_reshufflingtime
end                                             % end of loop in J (see the beginning of this file)