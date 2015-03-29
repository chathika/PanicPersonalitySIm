/* THIS IS THE ACTIVE WORK ON LKF STRATEGY. 
 *DO NOT MOCK THE AUTHOR'S LACK OF PROGRAMMING PROWESS, THIS FILE IS STILL A WORK IN PROGRESS!
 * $RCSfile: LKFStrategy2.java,v $ $Date: 2009/06/11 20:36:55 $
 */ 

package crowdsimulation.actioncontroller.strategy;

import java.util.*;
import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import crowdsimulation.util.*;
import math.*;								// used for Vector2D
import ec.util.*;
import sim.field.continuous.*;
import sim.util.*;

/**
 * This is a model used to simulate LKF movement.
 *
 * @see crowdsimulation.actioncontroller.strategy.ActionStrategy
 * @author $Author: ganil $
 * @version $Revision: 1.3 $
 * $State: Exp $
 * $Date: 2009/06/11 20:36:55 $
 **/
public class LKFStrategy2 extends HMFVStrategy 
{
	final double PI = Math.PI;
	
	/**The current level of excitment.**/
	private double eCurrent = 0.0;						
	
	
	 /**The social repulsive force fall-off distance modified from Helbing. Was originally .08m.**/
	private double b = .5;								
	/**The fall-off distance for the exit attraction force**/
	private double bExit = 50.0;
	/**Back/front pedestrian ratio of perception**/
	private double B = .3;								
	
	private double cYoung = 24000.0;
	/**Panic parameter**/
	private double panicParameter = 0.0;
	
	private double p = 0.0;								
	/**High density correction factor for face-to-back orientation.**/
	private double k0 = 0.3;
	private double k1 = 1.8;  							
	private double k2  = 4.5;  							
	private double kappa = 1.0;
	
	/**The maximum allowed density, reported to be 5.4 ped/m^2.**/
	private double rhoMax = 5.4;  						
	/**The preferred speed of an individual. 1.5 m/s, 3.0 m/s, or 4.5 m/s**/
	private double v0 = 1.5;							
	/**The speed of an isolated individual.**/
	private double w0 = 1.34;                       	
	/**"Radius of concern", set to 1.0m.**/
	private double R = 1.0;    							
    /**Current memory.**/
    private double mCurrent = 1.0;						
	/**The learning time in seconds.**/
	private double tauPlus = 2.0;							
	/**The forgeting time in seconds.**/
 	private double tauMinus = 10.0;						
	/**Excitement lag time in seconds**/
	private double delM;
    private double T = 2.0;								
	/**Coefficient of the max face-to-face social repulsion magnitude. Responsible for the extremely high momentary
	 *repulsive force between two face-to-face colliding individuals. Units in Newtons.**/
	private double F = 600.0;																 
	/**The maximum excitment parameter, taken to be approximately 1 
	 *in non panicked conditions. Set to 1/(1 + H), where H = v0/w0 - 1 
	 *for panicked situations.**/	
	//private double eMax = w0/v0;						
	private double eMax = 1.0/(1.0 + panicParameter);
	
	private Vector2D currentPreferredVelocityVector = new Vector2D( 0, 0);		
	
	//OE related fields// 
	
	private double maxSeverityOfOverlap = 0.0;
	/**An individual is considered stoned if 
	 *he has already been moved during Overlap Elimination.**/
	private boolean isStoned = false;		    		
	/**The projected position of the associated individual from OE**/
	private Vector2D projectedPosition = null;
	/**The projected velocity of the associated individual due to OE**/
	private Vector2D projectedVelocity = null;
	/**The time waited by the associated individual during the current round of OE**/
	private double timeWaited = 0.0;
	private Entity mostOverlappedWith = null;
	/**Determines if the implicit individual is already a member of the "master" overlap stack
	 *that is created during OE.**/
	 boolean belongsToOEStack = false;
	/**think is is the mass. General enough concept that this should
	 *rightly be a property of Individual. Will remove later.**/
	private double m = 80.0;											 
	/**the extra amount added to ensure two
	 *overlapping individuals that have been moved
	 *apart slightly exceeds their severity of
	 *overlap. Including this on strategy for now.**/
	private double epsilon =.01;
	
	
	/**remove this**/
	public int numOfTimesOverlapped = 0;
	
	/**keeps the id of the last cascade experienced by the implicit individual during OE**/
	private int lastCascadeNumber = 0;
	
	
	///Constructors/////////////////////////////////////////////////////////////////////////////////////////////////////
	public LKFStrategy2()
	{  
		super();
	}
	
	public LKFStrategy2(Map attributes)
	{
		super(attributes);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	///Methods//////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	

	/**
	 *Calculate the current excitement level of the individual. This value is necessary to calculate 
	 *the preferred velocity of an individual. Initially eCurrent = 0. It is updated each timestep by 
	 *performing explicit Euler integration. 
	 *
	 *@param val
	 *@return
	 **/
	public void updateExcitement(double timeStep)
	{
		
		//eCurrent = -(timeStep/T)*(eCurrent + eMax*((ind.getVelocity().magnitude())/v0) - 1 ) + eCurrent;
		eCurrent = eCurrent -timeStep*((eCurrent/T)+ (eMax/T)*((1.0 - (ind.getVelocity().magnitude()))/v0));
		
		/*keep excitment from turning negativ*/
		if (eCurrent < 0)
		{eCurrent = 0;}
		
		//if(eCurrent > eMax)
		//{eCurrent = eMax;}
	
	}
	
	/**Calculate current M. This is used to calculate ei, the ith individual's direction of gaze.
	 *This implements eq. 15 in the LKF paper.
	 *
	 *@param val
	 *@return
	 **/
	public void updateMemory(double timeStep)
	{
		//if the door is in sight, delM = 1 and tau+, if not, use delM = 0 and tau-
		Path currentPath = (Path)ind.getInteractingPaths().get(0);
		
		if(ind.canSee(currentPath.getActiveWaypoint(ind)))
		{
			delM = 1.0;
			mCurrent = mCurrent + (delM - mCurrent)*(timeStep/tauPlus);
		}else
		{
		  delM = 0.0;
		  mCurrent = mCurrent + (delM - mCurrent)*(timeStep/tauMinus);
		}	
	
	}
	
	/** 
	 *Calculates and writes to the gaze field of an individual, the unit vector in 
	 *the direction of the center of ind's field of view. This depends on the individual's 
	 *current knowledge of the exit(s), the individual's preferred 
	 *velocity, and the influence of the surrounding crowd members.
	 *
	 *
	 *@param val
	 *@return
	 **/
    public void updateGaze(Bag individuals)
    {
		Vector2D velocity = new Vector2D(ind.getVelocity().x, ind.getVelocity().y);
		double currentMemory = mCurrent;
	    double rhoTilde = nondimDensity();
	    
	    Vector2D normalizedVelocity = new Vector2D(0,0);
	    normalizedVelocity.x = velocity.x;
	    normalizedVelocity.y = velocity.y;

		//determine the normal vector from the individual to the door
		Vector2D indPosition = ind.getLocation();													
		
		Path path = (Path)ind.getSelectedPath();
		
		Waypoint wPoint = path.getActiveWaypoint( ind );
		
		if(wPoint == null)
		{return	;}
		
		Vector2D doorPosition = wPoint.getCenter();  //get the location of the door.
		Vector2D n = new Vector2D(doorPosition.x - indPosition.x  , doorPosition.y - indPosition.y);
		
		n.normalize();
		
		/*ecoll is <vj>i/|<vj>i|, the normalized average of the velocities of all the neighbors in the
		radius of concern.*/
		Vector2D ecoll = averageNeighborVelocity(individuals);
		ecoll.normalize();
	
		double ei_x =(normalizedVelocity.x*(1.0 - rhoTilde) + ecoll.x*rhoTilde)*(1.0 - mCurrent) + n.x*mCurrent;
		double ei_y =(normalizedVelocity.y*(1.0 - rhoTilde) + ecoll.y*rhoTilde)*(1.0 - mCurrent) + n.y*mCurrent;
		
		Vector2D ei = new Vector2D(ei_x, ei_y);
		
		
		//update individuals gazeAngle
		ind.setGaze(ei);
	}
   
   /**
    *Calculates <vj>i the sum of velocities of neighbors in a specfied radius of an individual
    *
    *@param val
    *@return
    **/
   public Vector2D averageNeighborVelocity(Bag neighbors)
   {
   	   double averageNeighborVelocity_x = 0;
   	   double averageNeighborVelocity_y = 0;
   	   double n = neighbors.size();
   	   
   	   if( n == 0)
   	   return new Vector2D(0,0);
   	   
   	    for( int i = 0; i <neighbors.size(); i++)
   	    {
   	    	Individual ind2 = (Individual)neighbors.get(i);
   	    	Vector2D temp = new Vector2D(ind2.getVelocity().x, ind2.getVelocity().y);
   	    	
   	    	averageNeighborVelocity_x  = temp.x + averageNeighborVelocity_x;
   	    	averageNeighborVelocity_y  = temp.y + averageNeighborVelocity_y;
   	    }
   		
   		
   		return new Vector2D(averageNeighborVelocity_x/n,averageNeighborVelocity_y/n);
   }
   
   /** 
    *This method calculates the prefered velocity of the individual at each time step.
    *This depends on the direction of sight(ei), the excitment level, the crowd density, and the velocity of the 
    *surrounding crowd.
    *
    *
    *@param individuals the Bag of Individuals inside the implicit individual's radius of interest
    *@return 
    **/
    public void updatePreferredVelocityVector(Bag individuals)
    {		
    	 Vector2D indLocation = ind.getLocation();    	
    	 Vector2D ei = new Vector2D(ind.getGaze().x, ind.getGaze().y);
    		
    	 double x = ei.x*(1.0 + eCurrent*mCurrent)* v0*(1.0-p) + averageNeighborVelocity(individuals).x*p; 
		 double y = ei.y*(1.0 + eCurrent*mCurrent)* v0*(1.0-p) + averageNeighborVelocity(individuals).y*p;
	
		 currentPreferredVelocityVector = new Vector2D(x,y);
    }		
    
    
   
    /**
     *Calculate the crowd density around each individual. This is the number of individuals in
     *ind's radius of concern divided by the area of concern. 
     *
     *@param val
     *@return
     **/
    public double density()
    {
   		double radiusOfConcern = ind.getRadiusOfConcern();
    	return (ind.getInteractingIndividuals().size() / (PI*radiusOfConcern*radiusOfConcern));
    }
    
    
    /**
     *This is simply a nondimensionalization of the density calculated above. In the LKF paper,
     *this quantity is refered to as rho tilde
     *
     *@return non-dimensional density double
     **/
    public double nondimDensity()
    {
    	double D0 = ind.getDiameter();
    	return (density()*D0*D0*PI)/4.0;
    }
    
    /**Calculate the force magnitude of the max face-to-face social force. In the LKF Paper this
	 *is given by eq. 13
	 *
	 *@return maxFaceToBack a double
	 **/
	public double maxFaceToFace()
	{
		double rhoTilde = nondimDensity();
		return F*( 1.0 + eMax)*( 1.0*(1.0 - rhoTilde) + k2*rhoTilde);
		
	}	
	
	/**
	 *Calculate the force magnitude of the max face-to-back social force. This is given by 
	 *eq. 12 in the LKF paper.
	 *
	 *@param val
	 *@return
	 **/
	public double maxFaceToBack()
	{
		 double rhoTilde = nondimDensity();
		 return (w0*80.0*(1.0 + eMax)*Math.exp( -1.91*(ind.getDiameter() - 1.0/rhoMax)))*(k0*(1.0 - rhoTilde) + k1*rhoTilde)* ( 1.0/(tau*(1.0 - B)));
		
	}
    
    /**
     *Returns a weighting factor based on whether ind2 is in the visual field of ind(assumed to be 180)
	 *This should return 1 when ind2 is inside the visual field of ind and return 
	 * 1 - (thetai - Pi/2)*(2/Pi)*(1 - b), when ind1 does not see ind2.
	 *
	 *@param val
	 *@return
	 **/
	private double weightFactor1( Individual ind2 )
	{
	       double theta = ind.angleBetweenGazes( ind2 );
		   if(ind.canSee(ind2))
		   {
		   	 return 1;
		   }
		   else
		   {
		   	 double ans = 1.0 - (theta - PI/2.0)*(2.0/PI)*(1.0 - B);
		     return ans;
		   }
	}
	
	/**
	 *The weight factor needed to compute the social repulsive force
	 *on an individual due to obstacles.
	 *
	 *
	 *@param val
	 *@return
	 **/
	private double weightFactor1( Obstacle obs)
	 { 
	 	double theta;
	 	theta = ind.angleBetweenGazeAnd( obs );
	 	
	 	if(ind.canSee(obs))
	 	{
	 		return 1;
	 	}
		else 
		{
			return (1.0 - (theta - PI/2.0)*(2.0/PI)*(1.0 - B));
		} 	
	}
	
	
	/**
	 *Returns a second weighting factor that depends on whether individuals are facing each other. Has a
	 *value of (f_max_facetoface/ f_max_facetoback)if ind and ind2 see each other. 
	 *Otherwise the weight is assigned a value of 1.
	 *
	 *@param val
	 *@return
	 **/ 
	private double weightFactor2( Individual ind2)
	{
		if(ind.canSee(ind2) && ind2.canSee(ind))
		{
			return (maxFaceToFace()/ maxFaceToBack());
		}
		else
		{
			return 1;
		}
	}	
	
////////////////////////////////////////////////////////////////////////////////////////////
///Forces
////////////////////////////////////////////////////////////////////////////////////	
	
	
	/**Depends on eCurrent, the current excitment level of an individual, 
	 *and the preferredVelocity.This is the first term of eq. 10b) in the LKF paper
	 *
	 *
	 *@param val
	 *@return
	 **/
	public Vector2D selfPropellingForce()
	{
		
		Vector2D LKFSPForce = new Vector2D(0,0);
		if( ind.getSelectedPath() == null || ind.getVelocity().magnitude() < 2.0 )
		{
			selectPath();
			if( ind.isDead() ) return( new Vector2D( 0, 0 ) );
		}
		Path path = (Path)ind.getSelectedPath();
		Waypoint wPoint = path.getActiveWaypoint( ind );

	  	if( wPoint == null )
  		{
  			((ModifiedHMFVModel)ind.getActionController()).removeIndividual( ind );
  			return( new Vector2D( 0, 0 ) );
  		}
		
		LKFSPForce.x = (-1.0/tau)* path.getWeight()*((ind.getVelocity().x)-(currentPreferredVelocityVector.x));
		LKFSPForce.y = (-1.0/tau)* path.getWeight()*((ind.getVelocity().y)-(currentPreferredVelocityVector.y));
				
		return LKFSPForce;
	} 
	
	
	/**
	 *Returns the social repulsive force on the implicit individual due to individual ind2 
	 *
	 *@param ind2 the affecting Individual
	 *@return
	 **/
	public Vector2D pp_PsychForce(Individual ind2)
	{
		 Vector2D pp_PsychForce = new Vector2D(0,0);
	     double pp_PsychForceMagnitude = 0;
	     double edgeToEdgeDistance = 0;
	     Vector2D indLocation = ind.getLocation();    	
   		 Vector2D n = new Vector2D(0,0);
				n = ind.distanceTo(ind2);
				n.x = (-1.0)*n.x;
				n.y = (-1.0)*n.y;
				n.normalize();
				edgeToEdgeDistance = ind.distanceTo(ind2).magnitude() - ind.getRadius();
				
				/*the .0125 is the reciprocal of the average individual weight 80.0kg*/
				pp_PsychForceMagnitude = (.0125)*weightFactor1(ind2)*weightFactor2(ind2)*maxFaceToBack()*Math.exp(-1.91*edgeToEdgeDistance); 
					
		
				pp_PsychForce.x = pp_PsychForceMagnitude*n.x;
				pp_PsychForce.y = pp_PsychForceMagnitude*n.y;
		
		
		return pp_PsychForce;
	}	
	
	
	/**
	 *Returns the social repulsive force on the individual due to obstacle obs  
	 *
	 *@param val
	 *@return
	 **/	
	public Vector2D wall_PsychForce(Obstacle obs)
	{
		Vector2D wall_PsychForce = new Vector2D(0,0);
		double wall_PsychForceMagnitude = 0;
		Vector2D n = new Vector2D(0,0);
		
		n.x = ind.getGaze().x;
		n.y = ind.getGaze().y;
		n.normalize();
		
		double wall_PyschForceMagnitude = 2*(.0125)*weightFactor1(obs)*1.0*maxFaceToBack();
			
		wall_PsychForce.x = wall_PsychForceMagnitude*n.x;
		wall_PsychForce.y = wall_PsychForceMagnitude*n.y;
		
		return wall_PsychForce;
	}

	
	/**
	 *Force attracting the individual to the door.
	 *
	 *@return a Vector2D
	 **/
	public Vector2D attractionToExitForce()
	{
		
		if( ind.getSelectedPath() == null || ind.getVelocity().magnitude() < 2.0 )
		{
			selectPath();
			if( ind.isDead() ) return( new Vector2D( 0, 0 ) );
		}
		Path path = (Path)ind.getSelectedPath();
		Waypoint wPoint = path.getActiveWaypoint( ind );

	  	if( wPoint == null )
  		{
  			((LKFModel2)ind.getActionController()).removeIndividual( ind );
  			return( new Vector2D( 0, 0 ) );
  		}
		
		Vector2D centerToCenter = ind.distanceTo(wPoint.getTargetPoint(ind));
		
		/*create normalized vector along line of action*/
		Vector2D n = new Vector2D(0,0);
		n.x = centerToCenter.x/centerToCenter.magnitude();
		n.y = centerToCenter.y/centerToCenter.magnitude();
		
		double aExit = (m*v0)/tau;
		
		double tempF = aExit*Math.exp( (ind.getRadius() - centerToCenter.magnitude())/bExit );
		
		return new Vector2D( n.x * tempF, n.y * tempF );
	}	





//////////////////////////////////////////////////////////////////////////////////////
///End of Force Calculations
///////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////////////
///Overlap Elimination Methods
////////////////////////////////////////////////////////////////////////////////////



	/* A Comparator for comparing two entities based on how much they each
	 *overlap individual ind.*/
	private class OverlapComparator implements Comparator
	{
		public int compare(Object obj1, Object obj2) 
		{
			Entity ent1 = (Entity) obj1;
			Entity ent2 = (Entity) obj2;
			double difference = (severityOfOverlapWithInd(ent1)- severityOfOverlapWithInd(ent2));
			if( difference < 0)
			{ 
				return 1; 
			}
			if(difference == 0)
			{
				return 0;
			}
			return -1;
		}
	}

	/**
	 *Returns the bag of overlapping individuals of implicit individual , sorted by degree 
	 *of overlap with said individual
	 *
	 *@param val
	 *@return
	 **/  
    public Bag individualsOverlappingInd()
    {
		Bag overlappingIndividuals =  ind.getIndividualsWithinDistance(-ind.getMaxCompression());
		overlappingIndividuals.sort(new OverlapComparator());
    	return overlappingIndividuals;
	}
    
  	/**
  	 *Returns subset of all overlapping individuals that have already been moved
  	 *during current overlap cascade.
  	 *
  	 *
  	 *@returns a Bag containing the implicit individual's stoned overlappers
  	 **/
  	public Bag stonedIndividualsOverlappingInd()
  	{
  		Bag overlappers = this.individualsOverlappingInd();
  		Bag stonedOverlappers = new Bag();
  		for(int i = 0; i < overlappers.size(); i++)
  		{
  			Individual currentInd = (Individual) overlappers.get(i);
  			LKFStrategy2 currentStr = (LKFStrategy2) currentInd.getActionStrategy();
  			if(currentStr.getIndStonedStatus())
  			{ stonedOverlappers.add(currentInd);}
  		}
  		return stonedOverlappers;
  	}
  	
  	
  	 /**
	  *Returns bag of obstacles that overlap the individual
	  *
	  *@param val
	  *@return
	  **/
	 public Bag obstaclesOverlappingInd()
	 {	
	 	Bag obstacles = ind.getObstaclesWithinDistance(-ind.getMaxCompression());
	 	obstacles.sort(new OverlapComparator()); 	
	 	return obstacles;
	 }
  
    /**
 	 *Decide if ind is overlapped by an obstacle or only by individuals.
	 *Calculate the resulting new position of ind and its new velocity.
	 *Returns the newPosition so the individual can be moved.
	 *
	 *@param val Bag of individuals and Bag of obstacles that currently overlap him.
	 *@return
	 **/
	public void calculateIndOEMove()
	{
		Vector2D newPosition = new Vector2D(0,0);
		Vector2D newVelocity = new Vector2D(0,0);
		Vector2D indPosition = ind.getLocation();
		
		Bag overlappingIndividuals = this.individualsOverlappingInd();
		Bag overlappingObstacles = this.obstaclesOverlappingInd();
		
		/*Three cases:
		 *case 1: Implicit individual overlapped by an obstacle.
		 *The remaining 2 cases involve overlap strictly with other individuals. 
		 *case 2: Implicit individual is ovelapped most severly by another individual
		 *who is stoned.
		 *case 3: The greatest overlapper is not yet stoned himself. A decision will then
		 *be made to determine which of the two is fixed and which is moved. 
		 */ 
			
		/*If ind is overlapped by an obstacle, it must be moved away from the obstacle regardless
		 *if other individuals overlap it with a greater severity*/
		
		if(!(overlappingObstacles.isEmpty()))
		{
		   Obstacle obs = (Obstacle)overlappingObstacles.get(0);
			
		    Vector2D obstacleTangential = ind.getTangentOf(obs);
		    obstacleTangential.normalize();
		    
		    /*write to the strategy's projected velocity and position fields*/
	    	newVelocity.x = ((ind.getVelocity()).dot(obstacleTangential))*obstacleTangential.x;
	    	newVelocity.y = ((ind.getVelocity()).dot(obstacleTangential))*obstacleTangential.y;
		    this.setIndProjectedVelocity( newVelocity);
			newPosition = calculateIndOEMove_Sub(ind,obs);
		    this.setIndProjectedPosition(newPosition);
			return;
		}
		else
		{
			/*If the individual does not overlap any obstacles, check to see if it overlaps other
			 *individuals.*/
			if(!overlappingIndividuals.isEmpty())
			{
				Individual greatestOverlapper = (Individual)overlappingIndividuals.get(0);
				LKFStrategy2 greatestOverlapperStr = (LKFStrategy2) greatestOverlapper.getActionStrategy();
				
				/*Since the current individual has overlapping individuals we have 2 cases. Check if the greatest overlapper 
				 *has already been moved i.e. stoned. If it has, treat it as an obstacle. Otherwise, it must be decided 
				 *whether it is the current ind or his greatest overlapper which must be moved. This is decided by
				 *stoning the one who is more overlapped more by their next greatest overlapper.
				 */
				if(greatestOverlapperStr.getIndStonedStatus())
				{
					this.setIndProjectedPosition(calculateIndOEMove_Sub(ind,greatestOverlapper));
					this.setIndProjectedVelocity(tempVelocityCapper(greatestOverlapper.getNextVelocity()));
					//this.setIndProjectedVelocity(greatestOverlapper.getNextVelocity());
					//this.setIndProjectedVelocity(greatestOverlapper.getVelocity());
					//this.setIndProjectedVelocity( ind.getVelocity());
					
					return;
				}
				else
				{
					if(indMoreOverlappedThan(greatestOverlapper))
					{
						this.setIndProjectedPosition(ind.getCenter());
						this.setIndProjectedVelocity(ind.getVelocity());
					
						greatestOverlapperStr.setIndProjectedPosition(calculateIndOEMove_Sub(greatestOverlapper, ind));
						//greatestOverlapperStr.setIndProjectedVelocity(ind.getVelocity());
						//greatestOverlapperStr.setIndProjectedVelocity(greatestOverlapper.getVelocity());
						greatestOverlapperStr.setIndProjectedVelocity(tempVelocityCapper(ind.getNextVelocity()));
						//greatestOverlapperStr.setIndProjectedVelocity(ind.getNextVelocity());
					}	
					else
					{
						this.setIndProjectedPosition(calculateIndOEMove_Sub(ind, greatestOverlapper));
						this.setIndProjectedVelocity(tempVelocityCapper(greatestOverlapper.getNextVelocity()));
					//	this.setIndProjectedVelocity(greatestOverlapper.getNextVelocity());
					//	this.setIndProjectedVelocity(greatestOverlapper.getVelocity());
					//	this.setIndProjectedVelocity(ind.getVelocity());
						
						greatestOverlapperStr.setIndProjectedPosition(greatestOverlapper.getCenter());
						greatestOverlapperStr.setIndProjectedVelocity(greatestOverlapper.getVelocity());
					}
					return;
				}
			
  			}
		 
	    }
	/*this state can only occur is we call OE move on someone who is not overlapped.
	 *this is essentially a do nothing response.*/
	this.setIndProjectedPosition(ind.getCenter());
	this.setIndProjectedVelocity(ind.getVelocity());
	return;
	} 	
 
 
	 /**
	 *Returns the projected position Vector2D that carries the new position due to OE. 
	 *
	 *@param val Individual intimeAlottedoBeMoved and the Entity entToBeMovedAwayFrom
	 *@return Vector2D projectedPosition
	 **/
	public Vector2D calculateIndOEMove_Sub(Individual indToBeMoved, Entity entToBeMovedAwayFrom)
	{
		
		/*Since we want to move indToBeMoved away from entToBeMovedAwayFrom, it should be
		 *in the direction of the vector normal to the closest surface point of the entity.*/
		Vector2D normal = indToBeMoved.getNormalOf(entToBeMovedAwayFrom);
		normal.normalize();
		Vector2D noise = random();
		
		Vector2D newPosition = new Vector2D(0,0);	
		
		if(indToBeMoved.getID() != ind.getID())
		{
		
		LKFStrategy2 str = (LKFStrategy2) indToBeMoved.getActionStrategy();
		normal.x = normal.x*((str.severityOfOverlapWithInd(entToBeMovedAwayFrom) + epsilon));
		normal.y = normal.y*((str.severityOfOverlapWithInd(entToBeMovedAwayFrom) + epsilon));
		
		
		newPosition.x = indToBeMoved.getCenter().x + normal.x + noise.x;
		newPosition.y = indToBeMoved.getCenter().y + normal.y + noise.y;
		return newPosition;
		}
		else
		{
		
		normal.x = normal.x*((this.severityOfOverlapWithInd(entToBeMovedAwayFrom) + epsilon));
		normal.y = normal.y*((this.severityOfOverlapWithInd(entToBeMovedAwayFrom) + epsilon));
		
		newPosition.x = ind.getCenter().x + normal.x;
		newPosition.y = ind.getCenter().y + normal.y;
		return newPosition;
		}
		
	}
	
	public Vector2D tempVelocityCapper(Vector2D vel)
	{
		Vector2D cappedVec = new Vector2D(vel.getX(), vel.getY());	
		if( vel.magnitude() > 5.0)
		{
			cappedVec.x =(5.0*(cappedVec.getX()/cappedVec.magnitude()));
			cappedVec.y =(5.0*(cappedVec.getY()/cappedVec.magnitude()));
			
		
		}
		return cappedVec;
	}	

    /**
	 *Determines whether the implicit individual is to be moved or his greatest overlapper. 
	 *This is done by comparing the severities of overlap between each person and their next greatest 
	 *overlapper. He who is overlapped more severely will be fixed in current location, 
	 *the other will be moved to eliminate the overlap.
	 *
	 *@param val Individual who are comparing the implicit Individual to.
	 *@return boolean value
	 **/
	public boolean indMoreOverlappedThan(Individual ind2)
    {
		LKFStrategy2 str2 = (LKFStrategy2)ind2.getActionStrategy();
		Bag indOverlappers = this.individualsOverlappingInd();
		Bag ind2Overlappers = str2.individualsOverlappingInd(); 
 		Iterator indIterator = indOverlappers.iterator();
 		Iterator ind2Iterator = ind2Overlappers.iterator();
 		
 		/*advance the pointer to the first position, which must exist. 
 		If it doesn't something is wrong.*/ 
        indIterator.next();
 		double indNextGreatestSeverityOfOverlap = 0;
 		double ind2NextGreatestSeverityOfOverlap = 0;
 		
 		if( indIterator.hasNext())
 		{
 			Individual indsNextGreatestOverlapper = (Individual)indIterator.next();
 			indNextGreatestSeverityOfOverlap = this.severityOfOverlapWithInd(indsNextGreatestOverlapper);
 		//	System.out.println("second greatest overlapper" + indsNextGreatestOverlapper.getID());
 		}
 		
 		while(ind2Iterator.hasNext())
 		{  
 			/*search through ind2's overlappers until the greatest overlapper that is 
 			 *not implicit individual is found*/
 			Individual ind2sNextGreatestOverlapper = (Individual)ind2Iterator.next();
 			if( ind2sNextGreatestOverlapper.getID() != ind.getID()) 
 			{
 		//	  System.out.println("overlapper's greatest overlapper other than ind");
 			  ind2NextGreatestSeverityOfOverlap = str2.severityOfOverlapWithInd(ind2sNextGreatestOverlapper);
 			  break;
 			}
 		}
 		
 		
 		if( indNextGreatestSeverityOfOverlap >= ind2NextGreatestSeverityOfOverlap)
 		{	
 			return true;
 		}
 		
 		return false;
 		
 }
    
 	 /**
	 *Updates the current position of strategy's associated individual to Vector2D newPosition.
	 *
	 *@param val Vector2D projected position of strategy's ind. 
	 *@return
	 **/
	public void performIndOEMove(Vector2D newPosition,Vector2D newVelocity)
	{	
		ind.setVelocity(newVelocity);
	    ind.setLocation(newPosition);
	    
	    /*since we've now moved the individual, stone him*/
	    this.setIndStonedStatus(true); 
	    return;
	}
 	 
 	 /**
	  *Returns DeltaTOE or "Required OE Move Time". This is the amount of 
	  *time required to change individual ind from his current position and
	  *velocity to his projected one. 
	  *
	  *@param val Individual ind's Vector2D projected velocity due to OE.
	  *@return double the time required to perform the calculated OE move for ind  
	  **/
	 public double calculateIndOERoundRequiredMoveTime(Vector2D velocityChange)
	 {
	 	/*This seems to be the change in momentum of ind due to its projected OE move divided 
	 	 *by Lakoba's free parameter fOE which is apparently 4 times the ind's mass.
	 	 *Based on an individual weighing 80 kilos. Guess it doesn't matter though because
	 	 *of cancellation.*/
	 	double fOE = 4.0*m;
	 	double momentumChangeDueToOE = m*velocityChange.magnitude();
	 //	System.out.println("Required Move Time" + Math.abs(momentumChangeDueToOE/fOE));
	 	return	Math.abs(momentumChangeDueToOE/fOE);
	 	
	 }   
    
    /**
	 *Return the severity of overlap of the implicit individual ind
	 *with entity ent
	 *
	 *@param val
	 *@return
	 **/
	public double severityOfOverlapWithInd( Entity ent)
	 {
	 	Vector2D centerToSurface = ind.distanceTo(ent);
		double squeezeRadius = ind.getRadius() - ind.getMaxCompression();
		double overlap = centerToSurface.magnitude()-squeezeRadius;
		
		if( overlap < 0)
		{ overlap = Math.abs(overlap);}
		else
		{ overlap = 0;}
		return overlap;
	}
     
    /**
   	 *Updates the maximum severity of overlap experienced by the implicit individual.
   	 **/
    public void updateIndMaxSeverityOfOverlap()
    {
		double maxOverlapSeverityFromOtherIndividuals = calculateIndMaxSeverityOfOverlapWithIndividuals();
		double maxOverlapSeverityFromObstacles = calculateIndMaxSeverityOfOverlapWithObstacles();    	
			
		double maxOverlapSeverity = Math.max(maxOverlapSeverityFromOtherIndividuals, maxOverlapSeverityFromObstacles);
		setIndMaxSeverityOfOverlap(maxOverlapSeverity);
	}
    
    /**
     *Calculates the maximum severity of overlap on the implicit individual
     *due to other individuals.
     *
     *@returns double
     **/
    public double calculateIndMaxSeverityOfOverlapWithIndividuals()
    {
    	Bag overlappingIndividuals = individualsOverlappingInd();
		double maxOverlapSeverityFromOtherIndividuals =0.0;
			 
		if(overlappingIndividuals.size() > 0)
		{
			Individual individualMostOverlappedWith = (Individual)overlappingIndividuals.get(0);
			maxOverlapSeverityFromOtherIndividuals = severityOfOverlapWithInd(individualMostOverlappedWith);
		}
    
    	return maxOverlapSeverityFromOtherIndividuals;
    } 
    
    /**
     *Calculates the maximum severity of overlap on the implicit individual
     *due to "stoned" individuals.
     *
     *@returns double
     **/
    public double calculateIndMaxSeverityOfOverlapWithStonedIndividuals()
    {
    	Bag overlappingIndividuals = stonedIndividualsOverlappingInd();
		double maxOverlapSeverityFromOtherIndividuals =0.0;
			 
		if(overlappingIndividuals.size() > 0)
		{
			Individual individualMostOverlappedWith = (Individual)overlappingIndividuals.get(0);
			maxOverlapSeverityFromOtherIndividuals = severityOfOverlapWithInd(individualMostOverlappedWith);
		}
    
    	return maxOverlapSeverityFromOtherIndividuals;
    } 
    
    
    
    
    /**
     *Calculates the maximum severity of overlap on the implicit individual
     *due to overlapping obstacles.
     *
     *@returns 
     **/
    public double calculateIndMaxSeverityOfOverlapWithObstacles()
    {
    	Bag overlappingObstacles = obstaclesOverlappingInd();
		double maxOverlapSeverityFromObstacles = 0.0;
			
		if(!overlappingObstacles.isEmpty())
		{
			 Obstacle obstacleMostOverlappedWith = (Obstacle)overlappingObstacles.get(0);
			 maxOverlapSeverityFromObstacles = severityOfOverlapWithInd(obstacleMostOverlappedWith);
		}
    	
    	return maxOverlapSeverityFromObstacles;	
    }
    
    /**
     *Returns the boolean value depending on whether the implicit individual
     *is overlapped by entity ent. 
     *
     *@param val entity in question
     **/ 
    public boolean indIsOverlappedBy(Entity ent)
	{
		if(severityOfOverlapWithInd(ent) > 0)
		{
		   return true;
		}
		return false;
	}
     
    /**
     *Returns true if implicit individual falls inside of Entity ent.
     *This should rightly be in Individual as it is a fairly general 
     *concept.
     *
     *@param val
     *@return 
     **/
	 public boolean isInsideOf(Entity ent)
	 {
	 	Vector2D r = new Vector2D();
	 	Vector2D r2 = new Vector2D();
	 	
	 	r.x = ent.getCenter().x - ind.getCenter().x;
	 	r.y = ent.getCenter().y - ind.getCenter().y;
	 	
	 	r2.x = ent.getCenter().x - ind.surfacePointOn(ent).x;
	 	r2.y = ent.getCenter().y - ind.surfacePointOn(ent).y;	
	 
	 	if( r.magnitude() < r2.magnitude())
	 	{
	 		return true;	
	 	}
	 	return false;
	 }
	 
////////////////////////////////////////////////////////////////////////////////
//End of OverlapElimination Methods
/////////////////////////////////////////////////////////////////////////////////


/**
	 * This function calculates all of the forces acting on the Individual.
	 *
	 * @param simState The object containing the state of the simulation.
	 **/
	public void calculateMovementParameters	( CrowdSimulation simState )
	{
        Vector2D loc = new Vector2D( simState.getWorld().getObjectLocation( ind ) );
        
        if( ind.isDead() )
        { 
        	return;
       	}
        
        Bag individuals = ind.getNeighboringIndividuals();
        Bag obstacles = ind.getNeighboringObstacles();
        
        int numberOfObstacles = obstacles.numObjs;
        int numberOfIndividuals = individuals.numObjs;
        
        Vector2D wallPsychRelationForce = new Vector2D( 0, 0 );
        Vector2D wallYoungForce = new Vector2D( 0, 0 );
        Vector2D wallTangForce = new Vector2D( 0, 0 );
        Vector2D pp_PsychForce = new Vector2D( 0, 0 );
        Vector2D pp_YoungForce = new Vector2D( 0, 0 );
        Vector2D pp_TangForce_FS = new Vector2D( 0, 0 );     


/////////////////////////////////////////////////////////////////////////////
//	1.1	Obstacle Forces
/////////////////////////////////////////////////////////////////////////////
        for( int j=0; j < numberOfObstacles; j++ )
        {
        	Obstacle obs = (Obstacle)(obstacles.objs[j]);

        	double temp_radius = 0;
        	boolean canSee = wallParticleRelation( obs, temp_radius );

        	Vector2D tempWallPsychRelationForce = wallPsychForce( obs );
        	wallPsychRelationForce = new Vector2D( wallPsychRelationForce.x+tempWallPsychRelationForce.x, wallPsychRelationForce.y+tempWallPsychRelationForce.y );
        	
			if( temp_radius <= ind.getRadius() )
			{
				Vector2D tempWallYoungForce = wallYoungForce( obs );
				wallYoungForce = new Vector2D( wallYoungForce.x+tempWallYoungForce.x, wallYoungForce.y+tempWallYoungForce.y );
				
				Vector2D tempTangForce = new Vector2D( 0, 0 );
			  	if( frictionSwitch == 0 )
			  	{
			    	tempTangForce = wallTangForce_FS0( obs );
			  	}
				else
				{
			    	tempTangForce = wallTangForce_FS1( obs );
				}
				wallTangForce = new Vector2D( wallTangForce.x+tempTangForce.x, wallTangForce.y+tempTangForce.y );
			}
		}


/////////////////////////////////////////////////////////////////////////////
//	1.2	Particle Forces
/////////////////////////////////////////////////////////////////////////////
        for( int j=0; j < numberOfIndividuals; j++ )
        {
        	Individual inInd = (Individual)(individuals.objs[j]);
        	Vector2D inIndLoc = inInd.getLocation();
        	
			double tmprsqr = SQR(loc.x-inIndLoc.x) + SQR(loc.y-inIndLoc.y);

			if( tmprsqr <= SQR( getR() ) )
			{
				double tempRadius = Math.sqrt( tmprsqr );
				
		 		Vector2D tempPP_PsychForce = pp_PsychForce( inInd );
		 		pp_PsychForce = new Vector2D( pp_PsychForce.x+tempPP_PsychForce.x, pp_PsychForce.y+tempPP_PsychForce.y );
		 		
				if( tempRadius <= 0.5*(ind.getDiameter()+inInd.getDiameter()) )
				{
		  			Vector2D tempPP_YoungForce = pp_YoungForce( inInd );
		  			pp_YoungForce = new Vector2D( pp_YoungForce.x+tempPP_YoungForce.x, pp_YoungForce.y+tempPP_YoungForce.y );
		  			
		  			Vector2D tempPP_TangForce_FS = new Vector2D( 0, 0 );
		  			
		  			switch(frictionSwitch)
		  			{
		  				case 0:
		  				{
		      				tempPP_TangForce_FS = pp_TangForce_FS0( inInd );
		      				break;
		  				}
		  				case 1:
		  				{
		      				tempPP_TangForce_FS = pp_TangForce_FS1( inInd );
		      				break;
		  				}
		  			}
		  			
		  			pp_TangForce_FS = new Vector2D( pp_TangForce_FS.x+tempPP_TangForce_FS.x, pp_TangForce_FS.y+tempPP_TangForce_FS.y );
	      		}			 		
			}
		}
		
		Bag grpIndivs = getGroupedIndividuals();
		Vector2D groupingForce = new Vector2D( 0.0, 0.0 );
		
		for( int i = 0; i < grpIndivs.size(); i++ )
		{
			Vector2D tempGroupingForce = groupingForce( (Individual) grpIndivs.get(i) );
			groupingForce = new Vector2D( groupingForce.x+tempGroupingForce.x, groupingForce.y+tempGroupingForce.y );
		}

		Vector2D randomForce = random();
		Vector2D selfPropellForce = selfPropellingForce();
		Vector2D attractionToExitForce = attractionToExitForce();

		// sum the forces to estimate the acceleration and assuming dt = 1 this is also the velocites
		double summForcesX = wallPsychRelationForce.x +
	        wallYoungForce.x +
	        wallTangForce.x +
	        pp_PsychForce.x +
	        pp_YoungForce.x +
	        pp_TangForce_FS.x +
	        groupingForce.x +
	        selfPropellForce.x +
	        attractionToExitForce.x +
	        randomForce.x;
	    
		double summForcesY = wallPsychRelationForce.y +
	        wallYoungForce.y +
	        wallTangForce.y +
	        pp_PsychForce.y +
	        pp_YoungForce.y +
	        pp_TangForce_FS.y +
	        groupingForce.y + 
	        selfPropellForce.y +
	        attractionToExitForce.y +
	        randomForce.y;
	    
		ind.setForces( summForcesX, summForcesY );
		double dx = summForcesX;
        double dy = summForcesY;
        
		ind.setForceValues( "" +wallPsychRelationForce.x+","+wallPsychRelationForce.y
						   +","+wallYoungForce.x+","+wallYoungForce.y
						   +","+wallTangForce.x+","+wallTangForce.y
						   +","+pp_PsychForce.x+","+pp_PsychForce.y
						   +","+pp_YoungForce.x+","+pp_YoungForce.y
						   +","+pp_TangForce_FS.x+","+pp_TangForce_FS.y
						   +","+groupingForce.x+","+groupingForce.y
						   +","+selfPropellForce.x+","+selfPropellForce.y
						   +","+attractionToExitForce.x+","+attractionToExitForce.y
						   +","+randomForce.x+","+randomForce.y );

		this.sumOfForces = new Vector2D( dx, dy );
	}


	 
	 
///End of Methods////////////////////////////////////////////////////////////////////////////////////////////////
	
		
///Accessors and Mutators////////////////////////////////////////////////////////////////////////////////////////	
	
	
	/* */ 
	 public boolean getIndStonedStatus()
	 {
	 	return this.isStoned;
	 }
	
	/**
	 *Sets the "has been processed" status of the associated individual
	 *during Overlap Elimination. 
	 *
	 *@param val boolean
	 **/
	 public void setIndStonedStatus(boolean status)
	 {	
	 	this.isStoned = status;
	 }
	

	/**
	 *Gets the max severity of overlap experienced by the individual associated
	 *with strategy. 
	 *
	 *@return
	 **/
	public double getIndMaxSeverityOfOverlap()
	{
		return maxSeverityOfOverlap;
	}

	/**
	 *Sets the max severity of overlap experienced by the individual associated
	 *with the strategy.
	 *
	 *@param val maximum degree of overlap in meters 
	 **/
	public void setIndMaxSeverityOfOverlap(double maxSeverity)
	{
		maxSeverityOfOverlap = maxSeverity;
	}

	/**
	 *Gets the entity with which the associated individual is most overlapped with.
	 *
	 *@return
	 **/
	public Entity getIndMostOverlappedWith()
	{
		return mostOverlappedWith;
	}

	/**
	 *Sets the entity that most overlaps the individual associated with this strategy
	 *
	 *@param val 
	 **/
	 public void setIndMostOverlappedWith(Entity ent)
	{
		mostOverlappedWith = ent;
	}

	
	/**
 	* Returns the individuals projected position during OE. Default is null.
 	*
 	*@returns the Vector2D representing the individual's projected position due to OE.
 	**/
	public Vector2D getIndProjectedPosition()
	{
		return this.projectedPosition;
	}
	
	
	/**
	 *Sets projected position calculated during OE.
	 *
	 *@param val position 
	 **/
	public void setIndProjectedPosition( Vector2D position)
	{
		this.projectedPosition = position;
	}

	/**
	 *Returns the Vector2D representing the implicit individual's velocity after OE.
	 *
	 *@returns 
	 **/
	public Vector2D getIndProjectedVelocity()
	{
		return this.projectedVelocity;
	}

	/**
	 *Sets the projected velocity due to OE of the implicit individual. 
	 *
	 *
	 *
	 *@param vals 
	 **/
	 public void setIndProjectedVelocity(Vector2D velocity)
	 {
	 	this.projectedVelocity = velocity;
	 }
	 
	 /**
	 * Returns true if implicit individual is on the "master" overlap
	 *stack created during OE.
	 *
	 *@returns boolean  
	 **/
	public boolean getIndOverlapStackMembershipStatus()
	{
		return this.belongsToOEStack;
	}
	
	/**
	 *Sets the "master" overlap stack
	 *membership status of the implicit individual.
	 *
	 *@param val boolean status of stack membership 
	 **/
	public void setIndOverlapStackMembershipStatus(boolean status)
	{
		this.belongsToOEStack = status;
	}

	/**
	 *Returns the amount of time the individual has waited to makethe current OE round
	 *
	 *@returns double value of the amount of time waited
	 **/
	public double getIndTimeWaited()
	{
		return this.timeWaited;
	}

	/**
	 *Sets the time the implicit individual has waited during current OE round.
	 *
	 *@param val double
	 **/
	public void setIndTimeWaited( double time)
	{
		this.timeWaited = time;
	}
	
	/**
	 *Get the last cascade number 
	 *
	 **/
	public int getLastCascadeNumber()
	{
		return lastCascadeNumber;
		
	}
	/**
	 *Sets the time the implicit individual has waited during current OE round.
	 *
	 *@param val int
	 **/
	public void setLastCascadeNumber(int casNum)
	{
		this.lastCascadeNumber = casNum;
	}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


}





		