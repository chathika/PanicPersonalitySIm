/*
 * $RCSfile: LKFModel2.java,v $ $Date: 2008/03/20 14:56:23 $
 */
package crowdsimulation.actioncontroller;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.strategy.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import ec.util.*;
import java.util.*;
import math.*;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import java.awt.*;


/**
 * ActionController based on the Lakoba Kaup Finkelstein model of crowd movement.
 *
 * @see crowdsimulation.actioncontroller.ActionController
 * @author $Author: rhauser $
 * @version $Revision: 1.2 $
 * $State: Exp $
 * $Date: 2008/03/20 14:56:23 $
 **/
public class LKFModel2 extends ModifiedHMFVModel
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
//	Bag overlappers = (Bag)individuals.clone();
	
	/** default time step (decreased dynamically) **/
	double defaultDeltaT = 0.01;			
	/** adaptive time step **/
	double deltaT = defaultDeltaT;			
	/** The extra amount by which an individual is shifted during overlap elimination.**/
	double epsilon = .01;					
	/** precision at potential forces ( 0.0 < C_NS < 1.0 ) **/
	double cNS= 0.95;					
   	/** limit for the change of the relative velocity during one iteration **/
   	double velocityChangeLimit = 0.01;		
	/** size of the room along the x direction **/
	double roomSizeX = 18;				
	
	/** defines the number of individuals in the room **/
	int numberInRoom;		

	/**The social repulsive force fall-off distance modified from Helbing. Was originally .08m.**/
	private double b = .5;								
	/**Back/front pedestrian ratio of perception**/
	private double B = .3;	
	
	/**A numerical factor to ...  Paper suggests beta = 1/8 is adequate for N=100 X=5.**/
	private double beta = 1.0/8.0; 
	
	/**Reaction time of an individual**/
	private double tau = 0.5;
	
	/**Spring constant.**/
	private double k = 2.4*(10^4);
	private double A = 2000;
	
	
	/**The maximum time allowed to perform overlap elimination during the current time-step.**/
	private double maxDeltaT = 0;
	/**The maximum time required to perform the movement of an individual due to OE.**/ 
	private double maxDeltaTOE = 0;
	/**The maximum timeWaited experienced by an individual due to OE.**/
	private double maxtimeWaited = 0;
	
	public static String forceNames = 	"wallPsychRelationForce.x,wallPsychRelationForce.y,"+
										"wallYoungForce.x,wallYoungForce.y,"+
										"wallTangForce.x,wallTangForce.y,"+
										"pp_PsychForce.x,pp_PsychForce.y,"+
										"pp_YoungForce.x,pp_YoungForce.y,"+
										"pp_TangForce.x,pp_TangForce.y,"+
										"selfPropellingForce.x,selfPropellingForce.y,"+
										"attractionToExitForce.x,attractionToExitForce.y,"+
										"randomForce.x,randomForce.y,"; 
										; /** The names of all the forces. **/
										
	public static String forceValues = 	"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0,"+	
										"0,0"; /** Default values for all forces 0's. **/



/*temporary variable to ensure OE doesn't get into n inf loop*/
int numOfTimes = 0;



////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs the LKF style model to be used for a set of individuals.
	 **/		
	public LKFModel2() 
	{
		super();

	}
	
	public ActionStrategy getNewStrategy()
	{
		return new LKFStrategy2(params);
	}
	
	
	
	public double speed()
	{
		return 1;
	}
	
	/**
	 * Function still needing to be updated from the original helbing model.
	 **/
	public double eulTStep( double tstep, double f )
	{
	 	/* adjusts the time step in a way that the force (fx,fy) doesn't
	     change the velocity of particle i by more than V_ChangeLimit */
	  
	  	while( f*(tstep) >= velocityChangeLimit ){ tstep *= cNS; }
	  	
	  	return tstep;
	}

    
    public void preStep( CrowdSimulation state )
	{
	  	
	 
		
		
        
        
        final CrowdSimulation simState = (CrowdSimulation)state;
// ********** 1 ********** //        
        for( int i =0; i < individuals.numObjs; i++ )
        {
/////////////////////////////////////////////////////////////////////////////
//	1.0	variable initialization and default values
/////////////////////////////////////////////////////////////////////////////
        	
        	Individual ind = (Individual)individuals.get(i);
        	
        	ind.getActionStrategy().calculateMovementParameters( simState );
			
		}

/////////////////////////////////////////////////////////////////////////////
//	1.4	Column Forces (Taken care of by the Obstacle Forces)
/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
//	1.5	Injury Forces (Not being implemented in this simulation)
/////////////////////////////////////////////////////////////////////////////
// ********** 2 ********** //
/////////////////////////////////////////////////////////////////////////////
//	2.1	Preparing update of equations of motion
/////////////////////////////////////////////////////////////////////////////
		double sqrt_fact = Math.sqrt( getTimeStep()/defaultDeltaT );
		
  		for( int i = 0 ; i < individuals.numObjs; i++ ) 
  		{ 
  			
		    Individual ind = (Individual)individuals.get(i);
		   
		    LKFStrategy2 strategy = (LKFStrategy2)ind.getActionStrategy();
			
			
			strategy.updateMemory(getTimeStep());
			strategy.updateExcitement(getTimeStep());
		    strategy.calculateMovementParametersII( simState );
		  
		}

	}

	/**
	 * This is the method called at each step of the simulation.
	 * Movement is calculated for each individual associated with this ActionController
	 *   and the 5 forces are calculated, summed then integrated to find the velocities
	 *   of each individual. Then each individual is moved acording to the calculated movement 
	 *   and there jump value.
	 **/
    public void step( SimState state )
	{
		final CrowdSimulation simState = (CrowdSimulation)state;
		
		for( int i = 0 ; i < individuals.numObjs; i++ ) 
  		{ 
  			Individual ind = (Individual)individuals.get(i);
  			ind.getActionStrategy().move( getTimeStep() );
		}
	    /*perform overlap elimination*/ 
	  overlapElim();
	   	
	    /*update all the gazes of the individuals after they've been moved*/
	    for( int i= 0; i < individuals.numObjs; i++)
	    {
	    	
	    	Individual ind = (Individual)individuals.get(i);
	        Bag neighbors = ind.getInteractingIndividuals();
	     	LKFStrategy2 strategy = ((LKFStrategy2)ind.getActionStrategy());
	        strategy.updateGaze(neighbors);
	    	strategy.updatePreferredVelocityVector(neighbors);
	    
	    }
	
		
		/*reset the maximum recorded waiting time back to zero for the next time-step*/
		maxtimeWaited = 0;
	
	}


	
	/**
	 *Performs a post-movement elimination of overlap between individuals 
	 *
	 *@param val
	 *@return
	 **/
	 public void overlapElim()
	 {
	 	//System.out.println("START OF OE");
	 	/*Determine all the individuals that are overlapped*/
	 	Bag overlappedIndividuals = overlappedIndividuals();
	 	
	 	int currentCascadeNumber = 0; 
	 	
	 	/*diagnostic*/
	 	//System.out.println("***************");
	 	for(int i = 0; i < overlappedIndividuals.size(); i++)
	 	{
	 		Individual currentInd = (Individual) overlappedIndividuals.get(i);
	 		LKFStrategy2 currentStr = (LKFStrategy2) currentInd.getActionStrategy();
	 		//System.out.println(currentInd.getID() + " " +  currentStr.getIndMaxSeverityOfOverlap());
	 	} 	
	 	//System.out.println("***************");
	 	
	 	while( !overlappedIndividuals.isEmpty())
	 	{
	 		
	 		currentCascadeNumber = currentCascadeNumber + 1;
	 	//	System.out.println("BEGINNING OF CASCADE");
	 	//	System.out.println("First individual in the list "+ ((Individual) overlappedIndividuals.get(0)).getID());
	 		Individual currentInd =(Individual) overlappedIndividuals.pop();
	 //		System.out.println("Popped Individual" +   currentInd.getID());
	 		LKFStrategy2 currentStr =(LKFStrategy2) currentInd.getActionStrategy();
	 		
	 		/*OE terminates when all individuals are free from overlap i.e.
	 		 *with zero max severity of overlap.The stack must therefore always be 
	 		 *kept sorted.If I pull up an individual with zero overlap on the master stack I know
	 		 *i'm done.*/
	 		if(currentStr.getIndMaxSeverityOfOverlap()==0)
	 		{return;}
	 		
	 		currentStr.setLastCascadeNumber(currentCascadeNumber);
	 		overlapElim(currentInd, overlappedIndividuals);
	 		
	 		
	 		/*It may happen that the current overlapped individual was moved into an obstacle
	 		 *in which case he must be placed back on the stack.*/
	 		if( !currentStr.obstaclesOverlappingInd().isEmpty()|| !currentStr.individualsOverlappingInd().isEmpty())
	 		{overlappedIndividuals.push(currentInd);}
	 		
	 		/*We may have modified the max severity of overlap for individuals in 
	 		 *the main OE stack, so we must re-sort*/
	 		overlappedIndividuals.sort(new OverlapComparator());
	 		
	 		/*reset the projected position and stonedStatus field of all individuals
	 		 *for the next cascade of OE*/ 
	 		for(int i = 0; i < individuals.size(); i++)
	 		{
	 			Individual ind = (Individual) individuals.get(i);
	 			LKFStrategy2 str = (LKFStrategy2) ind.getActionStrategy();
	 			str.setIndProjectedPosition(null);
	 			str.setIndStonedStatus(false);
	 			str.setIndTimeWaited(0);
	 		}
	 	//	System.out.println("END OF CASCADE");
	 	}
	 	
	 	/*When we've eliminated all the overlaps, reset  master overlap stack membership status
	 	 *for all individuals*/
	 	for(int i = 0; i < individuals.size(); i++)
	 	{	
	 		Individual ind = (Individual) individuals.get(i);
	 		LKFStrategy2 str = (LKFStrategy2) ind.getActionStrategy();
	 		str.setIndOverlapStackMembershipStatus(false);
	 		str.setLastCascadeNumber(0);
	 	}
	 	
	 //	System.out.println("END OF OE");
	 	return;	
	 }
	
	/**
	 *Recursive method for handling the order in which individuals are
	 *moved in order to eliminate overlaps.
	 *
	 *@param val the individual that initiates the current round of overlap and bag main
	 *overlapped stack.
	 **/
	public void overlapElim(Individual ind, Bag masterOverlapStack)
	{
		
		
		LKFStrategy2 str = (LKFStrategy2) ind.getActionStrategy();
		Vector2D projectedPosition = str.getIndProjectedPosition();
		Vector2D newPosition;
		Vector2D newVelocity;
		
		/*check to see if ind has a projected position already calculated
		 *for this cascade of OE*/
		if( projectedPosition == null)
		{	
		  str.calculateIndOEMove();
		  newPosition = str.getIndProjectedPosition();
		  newVelocity = str.getIndProjectedVelocity();
		}
		else
		{	
		  newPosition = projectedPosition;
		  newVelocity = str.getIndProjectedVelocity();
		}
		
		/*move the current individual to his projected position*/
		str.performIndOEMove(newPosition, newVelocity);
		
		/*determine if the individual has moved into any obstacles. If
		 *not, set its overlap severity to 0.*/
		if(str.obstaclesOverlappingInd().isEmpty()&&str.stonedIndividualsOverlappingInd().isEmpty())
		{
			str.setIndMaxSeverityOfOverlap(0);	
		}
		else
		{
			/*assume that we'll be moving
			 *all the current individual
			 *overlappers away from him.
			 *Therefore, his max overlap severity should
			 *be set to that with the most overlapping obstacle.*/
			double stonedIndOverlapSeverity = str.calculateIndMaxSeverityOfOverlapWithStonedIndividuals();
			double obsOverlapSeverity = str.calculateIndMaxSeverityOfOverlapWithObstacles();
			double maxOverlapSeverity = Math.max(obsOverlapSeverity, stonedIndOverlapSeverity);
			str.setIndMaxSeverityOfOverlap(maxOverlapSeverity);
			
			/*If he isn't already on the main stack, put him there.*/
			/*On strategy make a field for stack member status. check if he belongs to
			 *the stack already. If not, push him on as we still have to deal with him.*/
			if(!str.getIndOverlapStackMembershipStatus())
			{
				str.setIndOverlapStackMembershipStatus(true);
				masterOverlapStack.push(ind);
				masterOverlapStack.sort(new OverlapComparator());
			}
			/*A fix, added 11/13/07. If individual impacted stoned individuals then they to must be palced back on
			 *the master overlap stack. */
			 Bag stonedImpactedIndividuals = str.stonedIndividualsOverlappingInd();
			 for( int i = 0; i < stonedImpactedIndividuals.size(); i++)
			 {
			 	Individual currentInd = (Individual) stonedImpactedIndividuals.get(i);
			 	LKFStrategy2 currentStr = (LKFStrategy2) currentInd.getActionStrategy();
			 	currentStr.setIndMaxSeverityOfOverlap(currentStr.severityOfOverlapWithInd(ind));
			 	if(!currentStr.getIndOverlapStackMembershipStatus())
				{
					currentStr.setIndOverlapStackMembershipStatus(true);
					masterOverlapStack.push(currentInd);
				}
			 
			 }
			 	masterOverlapStack.sort(new OverlapComparator());
	}
		
		Bag projectedOverlappers = str.individualsOverlappingInd();
		/*the terminating condition*/
		if(allMembersStoned(projectedOverlappers)|| projectedOverlappers.isEmpty())
		{return;}
		
		
		/*if( allMembersStoned(projectedOverlappers) || projectedOverlappers.isEmpty())
		{return;}
		*/
	//	System.out.println("THE CURRENT NUMBER OF PROJECTED OVERLAPPERS is "  + projectedOverlappers.size());
	//	System.out.println("The current ind is " + ind.getID());
		/*calculate the projected positions of all the projected overlappers if they aren't 
		 *yet stoned.*/
		for(int i = 0; i < projectedOverlappers.size(); i++)
		{
			Individual currentInd = (Individual) projectedOverlappers.get(i);
		//	System.out.println(" The projected Overlapper(s): " + currentInd.getID());
			LKFStrategy2 currentStr = (LKFStrategy2) currentInd.getActionStrategy();	
		//	System.out.println(" The Stoned Status of this overlapper " + currentStr.getIndStonedStatus());
			if(!currentStr.getIndStonedStatus())
			{currentStr.calculateIndOEMove();}
		}	 
	
		/*calculate the time alloted for the current round of OE*/
		double timeAlotted = calculateOERoundAlottedTime(projectedOverlappers);

		while(!allMembersStoned(projectedOverlappers))
		{
		
			for( int i = 0; i < projectedOverlappers.size(); i++)
			{
				Individual currentInd = (Individual) projectedOverlappers.get(i);
			 	LKFStrategy2 currentStr = (LKFStrategy2)currentInd.getActionStrategy();
				if( currentStr.getIndStonedStatus())
				{ 
					currentStr.updateIndMaxSeverityOfOverlap();
				}
				else
				{
					
					Vector2D velocityChange = new Vector2D(0,0);
					velocityChange.x = currentStr.getIndProjectedVelocity().x - ind.getVelocity().x;
					velocityChange.y = currentStr.getIndProjectedVelocity().y - ind.getVelocity().y;
					
					double timeRequired = currentStr.calculateIndOERoundRequiredMoveTime(velocityChange);
					
				//	System.out.println("Time Req" + timeRequired);
					
					
				    if(timeAlotted + currentStr.getIndTimeWaited() >= timeRequired)
					{	overlapElim(currentInd, masterOverlapStack);	
					}
					else
					{ 	
						double timeWaited = timeAlotted + currentStr.getIndTimeWaited();
						currentStr.setIndTimeWaited(timeWaited); 
						
					
					}
				}
			}
		}	
		return;
	}
	
	
	/**
	 *Returns all the individuals who are overlapped in some way after movements have been
	 *made during the current time step, sorted by decreasing severity of overlap.
	 *For each individual, we first check to see if any other individuals overlap him.
	 *If so, put these in a bag. Sort the bag. The head of the bag will be the person who overlaps him
	 *the most. Save this severity of overlap.  Next, do the same for all the obstacles. Put all the obstacles overlapping
	 *the said individual in a bag and sort it. Save the overlap severity of head of the bag. Take the max
	 *of both overlap severities and save it as the maximum overlap severity for the individual. Save this
	 *in a field in LKFStrategy called maxSeverityOfOverlap. If this is greater than 0, add it in a bag called
	 *sortedOverlappers. Then sort this bag. This will yield the bag of all individuals that are in someway 
	 *overlapped sorted by decreasing severity of overlap.
	 *
	 *@param val
	 *@return the initial (or "master") Bag of overlapped Individuals that is sorted by severity of overlap
	 **/  
	public Bag overlappedIndividuals()
	{
		Bag overlappedIndividuals = new Bag();
		
		/*Find the max severity of Overlap for each individual*/
		
		for( int i = 0; i < individuals.size(); i++)
		{
			Individual currentInd = (Individual) individuals.get(i);
			LKFStrategy2 currentStr = (LKFStrategy2) currentInd.getActionStrategy();
			
			currentStr.updateIndMaxSeverityOfOverlap();
			
			if(currentStr.getIndMaxSeverityOfOverlap() > 0)
			{	
				overlappedIndividuals.add(currentInd);
				((LKFStrategy2)currentInd.getActionStrategy()).setIndOverlapStackMembershipStatus(true);
				overlappedIndividuals.sort(new OverlapComparator());
			}
			
		}
		return overlappedIndividuals;
	}
	

	 /**
	 *Calculates time alloted for each round of overlap elimination. 
	 *By a single round of OE, we mean the handling of all individuals "impacted"
	 *by the OE movement of the current individual, i.e. the current individual's
	 *projected overlappers.
	 *The time alotted or DeltaT, as it is labeled in the LKF paper, 
	 *is determined by the most overlapped individual in this set of projected overlappers. 
	 *Essentially, this is the time alotted to the current round of overlap
	 *elimination.
	 *
	 *@param val
	 *@return 
	 **/
	 public double calculateOERoundAlottedTime( Bag projectedStack)
	 {
	 	    double timeAlottedRepulsionMin = tau; 
	 	 	double timeAlottedRelMotionMin = tau;
	 	 	double timeAlottedRepulsion_i = 0;
	 	 	double timeAlottedRelMotion_i = 0;
	 	 	double timeAlotted_i_Min = tau;
	 		
	 		Individual currentInd;
	 	
	 	
	 	/*Calculate boundedMinDistance for each individual.
	 	 *This is the distance to the closest object up to compression.
	 	 *That is to say, compression tolerance acts as a lowerbound to
	 	 *this quantity.
	 	 */
	 	 for(int i = 0; i < projectedStack.size(); i++)
	 	{
	 		currentInd = (Individual) projectedStack.get(i);
	 		double timeAlotted_i;
	 		double minDistance_i = -1;
	 		double currentMinDistance_i = 0;
	 		double boundedMinDistance_i;
	 		
	 		for(int j = 0; j < individuals.size(); j++)
	 		{
	 			Individual ind_j =(Individual)individuals.get(j);
	 			if(ind_j.getID() != currentInd.getID())
	 			{
	 				currentMinDistance_i = currentInd.getRadius()-currentInd.distanceTo(ind_j).magnitude();
	 				if(currentMinDistance_i < 0)
	 				{currentMinDistance_i = 0;}
	 			
	 				if(minDistance_i == -1)
	 				{minDistance_i = currentMinDistance_i;}	
	 				if(minDistance_i > currentMinDistance_i)
	 				{minDistance_i = currentMinDistance_i;}
	 			}
	 		}
	 		
	 		Bag obstacles = CrowdSimulation.getInstance().getTerrain().getAllObjects();	
	 		if( !obstacles.isEmpty())
	 		{
	 			for(int j = 0 ; j < obstacles.size(); j++)
	 			{
	 				Obstacle obs_j = (Obstacle) obstacles.get(j);
	 				currentMinDistance_i = (currentInd.distanceTo(obs_j)).magnitude();
	 				if(minDistance_i == -1)
	 				{minDistance_i = currentMinDistance_i;}
	 				if(minDistance_i > currentMinDistance_i)
	 				{ minDistance_i = currentMinDistance_i;}	
	 			}
	 		

	 		}
	 		boundedMinDistance_i = Math.max(minDistance_i , currentInd.getMaxCompression());
	 		/*End of boundedMinDistance_i calculation*///////////////////////////////////////////
	 		
	 	 	/*Now calculate timeAlottedRepulsion_i and timeAlottedRelMotion_i for ind
	 	 	 *using calculated boundedMinDistance_i.*/
			for( int j = 1; j <projectedStack.size(); j++)
	 	 	{	
	 	 		if( ((Individual)projectedStack.get(j)).getID() != currentInd.getID())
				{
					Individual ind_j = (Individual)projectedStack.get(j);
				
					timeAlottedRepulsion_i = calculateOERoundRepulsionAlottedTime(currentInd, ind_j, boundedMinDistance_i);
					if (timeAlottedRepulsion_i < timeAlottedRepulsionMin)
					{timeAlottedRepulsionMin = timeAlottedRepulsion_i;}
					
					timeAlottedRelMotion_i = calculateOERoundRelMotionAlottedTime(currentInd, ind_j, boundedMinDistance_i);
	 	 			if( timeAlottedRelMotion_i < timeAlottedRelMotionMin)
	 	 			{timeAlottedRelMotionMin = timeAlottedRelMotion_i;}
	 	 		}
	 	 	}
	 		timeAlotted_i = Math.max( timeAlottedRelMotionMin, tau);
			timeAlotted_i = Math.max(timeAlotted_i, timeAlottedRepulsionMin );
		//	System.out.println("timeAlottedRepulsion"+ timeAlottedRepulsionMin);
		//	System.out.println("timeAlottedRelMotion" + timeAlottedRelMotionMin);
		//	System.out.println("timeAlotted_i" + timeAlotted_i);
			
			if(timeAlotted_i < timeAlotted_i_Min)
	 		{
	 		   timeAlotted_i_Min = timeAlotted_i;
	 		}
		}	 
	 	double timeAlotted = beta*timeAlotted_i_Min;
	 	
	 	
	 	return timeAlotted;
	 
	 }
	 
	 /**
	  *Returns the time alotted for OE due to repulsion for individual ind1 from individual ind2
	  *It seems that this is a control to keep large velocity changes from happening too quickly.
	  *
	  * 
	  *@param val
	  *@return
	  **/
	 public double calculateOERoundRepulsionAlottedTime(Individual ind1,Individual ind2, double timeAlottedTilde)
	 {
	 	
	 	double Rij = ind1.getRadius() + ind2.getRadius();
		Vector2D centroidal = new Vector2D();
		centroidal.x = ind1.getLocation().x - ind2.getLocation().x;
		centroidal.y = ind1.getLocation().y - ind2.getLocation().y; 
		 
	 	double centroidalDistance = centroidal.magnitude();
	 	double overlap = Rij - centroidalDistance;
	 	if( overlap < 0)
	 	{
	 		overlap = 0; 
	 	}
	 	
	 	double denominator = (overlap*k*0.07 + Math.abs(A*Math.exp(overlap/b)))/80.0;

	 	double numerator = timeAlottedTilde;
	 	return Math.sqrt(numerator/denominator);
	 	
	 }
	 
	 /**
	  *Calculates the timeAlottedRelMotion between Individuals ind1 and ind2
	  *
	  *@param val
	  *@return
	  **/
	 public  double calculateOERoundRelMotionAlottedTime(Individual ind1, Individual ind2, double dTilde)
	 {
	 		double numerator = dTilde;
	 		
	 		Vector2D centroidal = new Vector2D();
	 		centroidal.x = ind1.getLocation().x - ind2.getLocation().x;
	 		centroidal.y = ind1.getLocation().y - ind2.getLocation().y;
	 		
	 		Vector2D relativeVelocity = new Vector2D();
	 		relativeVelocity.x = ind1.getVelocity().x - ind2.getVelocity().x; 
	 		relativeVelocity.y = ind1.getVelocity().y - ind2.getVelocity().y;
	 		
	 		double denominator = ((relativeVelocity.dot(centroidal))/(centroidal.magnitude()));
	 		
	 		double relMotionTimeAlloted= Math.abs(numerator/denominator);
			return relMotionTimeAlloted;
			
	}
	 
	 
	 /*Comparator for sorting individuals by their maximum severity of overlap (in descending order).*/
	 private static class OverlapComparator implements Comparator
	{
		public int compare(Object obj1, Object obj2) 
		{
			Individual ind1 = (Individual) obj1;
			Individual ind2 = (Individual) obj2;
			
			LKFStrategy2 str1 = (LKFStrategy2) ind1.getActionStrategy();
			LKFStrategy2 str2 = (LKFStrategy2) ind2.getActionStrategy();
			
			double difference = str1.getIndMaxSeverityOfOverlap()- str2.getIndMaxSeverityOfOverlap();
			
			if(difference < 0)
			{return -1;}
			if(difference == 0)
			{
			  if( str1.getLastCascadeNumber() < str2.getLastCascadeNumber())
			  {return 1;}
			  if( str2.getLastCascadeNumber() < str1.getLastCascadeNumber())
			  {return -1;}
			  
		    }
			return 1;
		}
	}
	
	/**
	 *Returns true if all the individuals in the supplied bag are not overlapped.
	 *This is used as one of the terminating conditions for an OE round.
	 *
	 *@param val Bag of individuals
	 *@returns boolean
	 **/
	public boolean allMembersOverlapped(Bag selectedInds)
	{
		for(int i = 0; i < selectedInds.size(); i++)
		{
			Individual currentInd = (Individual)selectedInds.get(i);
			LKFStrategy2 currentStr = (LKFStrategy2) currentInd.getActionStrategy();
			if(currentStr.getIndMaxSeverityOfOverlap()!=0)
			{return false;}
		}
		return true;
		
	}

	/**
	 *Returns true if all supplied individuals are stoned. 
	 *
	 *@param val Bag of individuals
	 *@returns boolean value
	 **/
	public boolean allMembersStoned(Bag selectedInds)
	{
		for(int i =0; i < selectedInds.size(); i++ )
		{
			Individual currentInd = (Individual) selectedInds.get(i);
			LKFStrategy2 currentStr = (LKFStrategy2) currentInd.getActionStrategy();
			if(currentStr.getIndStonedStatus() == false)
			{return false;}
		}
		return true;
	}
	
	
	
	public double eMean( String sw, int unfreq, float stfreq ) 
	{
	  /* calculates the mean value of the efficiency of the system for the last
	     few update steps -- NOTE: use this function only when UpdNum > 0 
	
	     if unfreq != 0, the average will be calculated for the last unfreq
	     updates (the present one included)
	     if unfreq == 0, the average will be calculated for the shortest
	     possible time interval exceeding stfreq */
/*	
		int i, start;
	  	float e_mean, f;
	
	
	  	if(strcmp(sw,"un")==0) 
	  	{ start = UpdNum - unfreq; }
	  	else // i.e. if(strcmp(sw,"st")==0)  
	  	{ 
	  		start = Mb; // start from beginning of present time window 
		  	f = floor( SimTime[UpdNum] / stfreq );
		  	while( f - floor( SimTime[start] / stfreq ) > 1.0 ) { start++; }
		  	if( start==UpdNum ) { start--; }
	  	}
	  	e_mean = 0.0;
	  	for(i=start+1; i<=UpdNum; i++) 
	  	{
	    	e_mean += E[i] * ( SimTime[i] - SimTime[i-1] );
	  	}	  
	  	e_mean /= SimTime[UpdNum] - SimTime[start];
	
	
	  	e_mean /= V0; 
	  	return e_mean;
*/
		return 0;
	}
	
	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
		super.setParameters( parameters );
		
		Object[] keys = parameters.keySet().toArray();
		
		for( int i = 0; i < keys.length; i++ )
		{
			String key = (String)keys[i];
			
			if( key.equals( "velocityChangeLimit" ) )
			{
				Object value = parameters.get( key );
				double val = 0;
				
				if( value instanceof String )
				{
					val = Double.parseDouble( (String)value );
				}
				else if( value instanceof Double )
				{
					val = ((Double)value).doubleValue();
				}
				else if( value instanceof RandomGenerators )
				{
					val = ((RandomGenerators)value).nextValue();
				}
				else
				{
					Log.log( 1, " VelocityChangeLimit for HMFVModel construction must be a Double or a string representing a Double.");
					Log.log( 1, " Random Generators are also valid.");
				}
				setVelocityChangeLimit( val );
			}
			else if( key.equals( "cNS" ) )
			{
				Object value = parameters.get( key );
				double val = 0;
				
				if( value instanceof String )
				{
					val = Double.parseDouble( (String)value );
				}
				else if( value instanceof Double )
				{
					val = ((Double)value).doubleValue();
				}
				else if( value instanceof RandomGenerators )
				{
					val = ((RandomGenerators)value).nextValue();
				}
				else
				{
					Log.log( 1, " CNS for HMFVModel construction must be a Double or a string representing a Double.");
					Log.log( 1, " Random Generators are also valid.");
				}
				setCNS( val );
			}
			else if( key.equals( "defaultDeltaT" ) )
			{
				Object value = parameters.get( key );
				double val = 0;
				
				if( value instanceof String )
				{
					val = Double.parseDouble( (String)value );
				}
				else if( value instanceof Double )
				{
					val = ((Double)value).doubleValue();
				}
				else if( value instanceof RandomGenerators )
				{
					val = ((RandomGenerators)value).nextValue();
				}
				else
				{
					Log.log( 1, " DefaultDeltaT for HMFVModel construction must be a Double or a string representing a Double.");
					Log.log( 1, " Random Generators are also valid.");
				}
				setDefaultDeltaT( val );
			}
			else if( key.equals( "roomSizeX" ) )
			{
				Object value = parameters.get( key );
				double val = 0;
				
				if( value instanceof String )
				{
					val = Double.parseDouble( (String)value );
				}
				else if( value instanceof Double )
				{
					val = ((Double)value).doubleValue();
				}
				else if( value instanceof RandomGenerators )
				{
					val = ((RandomGenerators)value).nextValue();
				}
				else
				{
					Log.log( 1, " RoomSizeX for HMFVModel construction must be a Double or a string representing a Double.");
					Log.log( 1, " Random Generators are also valid.");
				}
				setRoomSizeX( val );
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	

	    
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds an individual to the action model.
	 * This places the individual in the collection which denotes which individuals are controlled by this action model.
	 *
	 * @param ind The individual to be controlled by this ActionController.
	 **/
	public void addIndividual( Individual ind )
	{
		individuals.add( ind );
		numberInRoom++;
	}


	/**
	 * Removes an individual to the action model.
	 * This removes the individual from the collection which denotes which individuals are controlled by this action model.
	 *
	 * @param ind The individual to be controlled by this ActionController.
	 **/
	public void removeIndividual( Individual ind )
	{
		ind.setDead(true);
		individuals.remove( ind );
		CrowdSimulation.getInstance().getWorld().remove( ind );
	}

	/**
	 * Gets the names of all forces used for this model as a comma seperated list.
	 *
	 * @return String The string on the force names. 
	 **/
	public String getForceNames()
	{
		return forceNames;
	}
	
	/**
	 * This gets a string of zero's for the forces used in this model seperated by commas.
	 *
	 * @return String The string of zeros for each force.
	 **/
	public String getForceValues()
	{
		return forceValues;
	}    

	

	/**
	 **/
    public double getVelocityChangeLimit()
	{
		return velocityChangeLimit;
	}
	
	/**
	 **/
	public void setVelocityChangeLimit( double val )
	{
		velocityChangeLimit = val;
	}

	/**
	 **/
    public double getDefaultDeltaT()
	{
		return defaultDeltaT;
	}
	
	/**
	 **/
	public void setDefaultDeltaT( double val )
	{
		defaultDeltaT = val;
	}


	/**
	 **/
    public double getRoomSizeX()
	{
		return roomSizeX;
	}
	
	/**
	 **/
	public void setRoomSizeX( double val )
	{
		roomSizeX = val;
	}

	/**
	 **/
    public double getCNS()
	{
		return cNS;
	}
	
	/**
	 **/
	public void setCNS( double val )
	{
		cNS = val;
	}

	/**
	 **/
    public void incrementNumberInRoom()
	{
		numberInRoom++;
	}

	/**
	 **/
    public void decrimentNumberInRoom()
	{
		numberInRoom--;
	}


	/**
	 *
	 **/
	 public double getMaxtimeWaited()
	 {
	 	return maxtimeWaited;
	 }

	/**
	 *
	 *
	 **/
	 public void setMaxtimeWaited(double time)
	 {
	 	this.maxtimeWaited = time;
	 }

}
