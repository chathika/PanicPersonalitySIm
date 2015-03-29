/*
 * $RCSfile: SalamanderStrategy.java,v $ $Date: 2010/12/09 20:42:19 $
 */
package crowdsimulation.actioncontroller.strategy;

import java.util.*;
import crowdsimulation.*;
//import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
//import crowdsimulation.logging.*;
import crowdsimulation.util.*;
import math.*;								// used for Vector2D
//import ec.util.*;
import sim.field.continuous.*;
import sim.util.*;

//TODO: Rework this class. Some things just don't really make sense.
/**
 * This represents the ActionStrategy to represent salamander movement in the simulation.
 * This is specifically constructed to reproduce the experimental setup done by
 * Dr. Fauth.
 *
 * @see crowdsimulation.actioncontroller.strategy.ActionStrategy
 * @author $Author: dkaup $
 * @version $Revision: 1.31 $
 * $State: Exp $
 * $Date: 2010/12/09 20:42:19 $
 **/
public class SalamanderStrategy extends SocialPotentialStrategy
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

//	private double violenceRating;
	private double pfA;
	private double pfB;
	private double pfC;

	private double coverMemoryAttraction = 0;
	private double coverAttraction = 0;
    private double ejectionForce = 0;
    private double foodAttraction = 0;
	private double foodSourceAttraction = 0;
//	private double influenceOfOthers = 0;
//    private double interactionRadius = 0;
//	private double radiusOfConcern = 0;
	private double regionalAffinity = 0;
	private double regionMarker = 0;
	private double waterAttraction = 0;
//	private double smallerSalamanderAttraction = 0;
	//private double maxVelocity=3;//

	private double size=1;

	private double randomStrength = 0;
    private int memorysize;
	private int randomMoveRecycle;
	private int randomRecycleTime;
	private int randomDirection;
	private Vector2D lastMoveDirection;

	private RandomGenerators rand;
	private Bag hidingLocationMemory = new Bag();	// A bag of hiding locations, i.e. obstacles
    private Bag occupiedObstacleMemory = new Bag(); // A bag of obstacles salamander has tried but are occupied
	private int memorySize = 1;
	private String coverType = "";
	private String substrate = "";

	// Salamander states.
	private boolean isHiding = false;
	private boolean tryingToHide = false;


	// The 7 forces that affect Salamander movement.
	private Vector2D forceRandom = new Vector2D(0.0, 0.0);
	private Vector2D forceTowardsCoverMemory = new Vector2D(0.0, 0.0);
	private Vector2D forceTowardsFoodSource = new Vector2D(0.0, 0.0);
	private Vector2D forceTowardsFood = new Vector2D(0.0, 0.0);
	private Vector2D forceTowardsHiding = new Vector2D(0.0, 0.0);
	private Vector2D forceTowardsOthers = new Vector2D(0.0, 0.0);
	private Vector2D forceTowardsRegion = new Vector2D(0.0, 0.0);
	private Vector2D forceTowardsWater = new Vector2D(0.0, 0.0);
//	private Vector2D forceTowardsSmallerSalamander = new Vector2D(0.0, 0.0);

	private double regionSize = 30;


////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Default constructor for SalamanderStrategy.
	 */
	public SalamanderStrategy()
	{
	}

	/**
	 * Constructor for a Salamander Strategy where the data is set by attribute set passed in.
	 *
	 * @param attributes Map of attributes to be set in the strategy.
	 **/
	public SalamanderStrategy( Map attributes )
	{
		this();
		setParameters( attributes );
	}

	/**
	 * Defines the mod function, makes x represent a number between 0 and y.
	 *
	 * @param x The value to be conifed.
	 * @param y The maximum value which can be returned.
	 * @return The value representing the input confined to the the given maximal value.
	 **/
    public double mod( double x, double y )
    {
         int multiple = ((int)x)/((int)y);
         return (x-multiple*y);
    }

	/**
	 * This function calculates all of the forces acting on the Salamander.
	 *
	 * @param simState The object containing the state of the simulation.
	 **/
    @Override
	public void calculateMovementParameters( CrowdSimulation simState )
	{
		Continuous2D world = simState.getWorld();
        Vector2D zeroVec = new Vector2D(0, 0);

            // Get the forces acting on the Salamander and store them in member variables.
            forceRandom = forceRandom(simState);
            forceTowardsCoverMemory = forceTowardsCoverMemory();
            forceTowardsFoodSource = forceTowardsFoodSource();
            forceTowardsFood = forceTowardsFood(world);
            forceTowardsHiding = forceTowardsHiding();
            forceTowardsOthers = forceTowardsOthers(world);
            forceTowardsRegion = forceTowardsRegion();
            forceTowardsWater = forceTowardsWater();
            //forceTowardsSmallerSalamander = forceTowardsSmallerSalamanders();


		double dx = getForceRandom().x +
					getForceTowardsCoverMemory().x +
					getForceTowardsFoodSource().x +
					getForceTowardsFood().x +
					getForceTowardsHiding().x +
					getForceTowardsOthers().x +
					getForceTowardsRegion().x +
					getForceTowardsWater().x;
					//forceTowardsSmallerSalamander.x;

		double dy = getForceRandom().y +
					getForceTowardsCoverMemory().y +
					getForceTowardsFoodSource().y +
					getForceTowardsFood().y +
					getForceTowardsHiding().y +
					getForceTowardsOthers().y +
					getForceTowardsRegion().y +
					getForceTowardsWater().y;
					//forceTowardsSmallerSalamander.y;

		ind.setForceValues( "" +getForceRandom().x+","+getForceRandom().y
							   +","+getForceTowardsCoverMemory().x+","+getForceTowardsCoverMemory().y
							   +","+getForceTowardsFoodSource().x+","+getForceTowardsFoodSource().y
							   +","+getForceTowardsFood().x+","+getForceTowardsFood().y
							   +","+getForceTowardsHiding().x+","+getForceTowardsHiding().y
							   +","+getForceTowardsOthers().x+","+getForceTowardsOthers().y
							   +","+getForceTowardsRegion().x+","+getForceTowardsRegion().y
							   +","+getForceTowardsWater().x+","+getForceTowardsWater().y );

		sumOfForces = new Vector2D( dx, dy );

	}

	/**

	 * This function adds a hiding place, aka an obstacle, to
	 * the Salamander's memory.  The Salamander's memory size
	 * and the current fullness of the memory determines how the
	 * addition takes place.  If the memory is full we will replace
	 * the furthest away hiding place with the new hiding place.
	 *
	 * @param obstacle The obstacle to be added to the individuals memory of possible hiding locations.
	 **/
	private void addHidingPlaceToMemory( Obstacle obstacle )
	{
		if( hidingLocationMemory.contains(obstacle) )
		{
			return;
		}
		if( hidingLocationMemory.numObjs < memorySize )
		{
			// If the memory isn't full then add the hiding place.
			hidingLocationMemory.add(obstacle);
		}
		else
		{
			// Find the furthest hiding location and replace it with this one.
			int indexOfFurthestObstacle=0;
			double distance=-1;

			for( int i=0; i<hidingLocationMemory.numObjs; i++ )
			{
				Obstacle currentObstacle = (Obstacle)hidingLocationMemory.objs[i];

				if(obstacle.equals(currentObstacle))
				{
					// The obstacle is already in the Salamander's memory.
					return;
				}
				else
				{
					double newDistance = ind.distanceTo( (Obstacle)hidingLocationMemory.objs[i] ).magnitude();
					if( distance==-1 || newDistance>distance )
					{
						indexOfFurthestObstacle=i;
						distance = newDistance;
					}
				}

			}// end for loop

			// Replace the furthest hiding place with the new one.
			hidingLocationMemory.objs[indexOfFurthestObstacle] = obstacle;
		}
	}

    /**
	 * Causes the individaul to attack all attackable entities within its interactionRadius.
     *
     * If "ind" ever overlaps the edge of "other", then "other" is first injured,
     * and on second pass, is eaten.
	 **/
    public void attack()
    {
    	Bag individuals = ind.getIndividualsWithinDistance(interactionRadius);

    	for( int i = 0; i < individuals.numObjs; i++ )
    	{
    		Individual other = (Individual)individuals.get(i);
    		if( ind.distanceTo( other ).magnitude() < ind.getRadius() )
                // distanceTo gets distance from ind.center() to EDGE of ind2
    		{
    			if( other.isInjured() )
    			{
    				other.setDead( true );
    			}
    			else
    			{
    				other.setInjured( true );
    			}
    		}
    	}
    }

	/**
	 * Calculates the attraction between two individuals based on their sizes.
	 * This force is given as a look-up table.
	 *
	 * @param bob The individual causing the influence.
	 * @return The strength of attraction or repulsion between these individuals cating on ind.
	 **/
    private double calculatePersonalityFactor(Individual bob) {
        /**    	double personalityFactor = 0.0;
        double dP = 0.0;

        if( bob.getActionStrategy() instanceof SalamanderStrategy )
        {
        SalamanderStrategy bobStrategy = (SalamanderStrategy)bob.getActionStrategy();

        dP = Math.abs(getViolenceRating() - bobStrategy.getViolenceRating());

        // Same violence rating which reflects their size is our code.
        if( dP == 0 )
        {
        return -pfC;
        }
        else if( dP <= pfB )
        {
        personalityFactor = -1*(pfA/Math.pow(pfB,2))*Math.pow(dP,2)+pfA;
        }
        else if( dP > pfB )
        {
        double a = pfC/SQR(pfB-10);
        personalityFactor = a*SQR(dP-10)-pfC;
        }
        }
        return personalityFactor;
        }
         **/
        double personalityFactor = 0.0;

        if (bob.getActionStrategy() instanceof SalamanderStrategy) {
            SalamanderStrategy bobStrategy = (SalamanderStrategy) bob.getActionStrategy();

            if (bobStrategy.getSize() == 1) {
                personalityFactor = this.pfA;
            } else if (bobStrategy.getSize() == 5) {
                personalityFactor = this.pfB;
            } else if (bobStrategy.getSize() == 10) {
                personalityFactor = this.pfC;
            }
        }
        return personalityFactor;
    }

    /**
	 * Determines if the individual is close enough to attack another.
	 *
	 * @return Boolean value telling if the individual can attacking.
	 **/
    public boolean canAttack()
    {
    	Bag individuals = ind.getIndividualsWithinDistance( interactionRadius );
        double iSize = getSize();
    	for( int i = 0; i < individuals.numObjs; i++ )
    	{
    		Individual other = (Individual)individuals.get(i);
    		if( other.getActionStrategy() instanceof SalamanderStrategy
    		        && ind.distanceTo( other ).magnitude() < ind.getRadius()
    				&& iSize > ((SalamanderStrategy)other.getActionStrategy()).getSize()  )
    		{
    			return true;
    		}
    	}
    	return false;
    }

    /**
	 * Determines if the individual is currently capable of eating an insect.
	 *
	 * @return Boolean value telling if the individual can eat.
	 **/
    public boolean canEat()
    {
    	Bag individuals = ind.getIndividualsWithinDistance(interactionRadius );

    	for( int i = 0; i < individuals.numObjs; i++ )
    	{
    		Individual other = (Individual)individuals.get(i);
    		if( other.getType().equalsIgnoreCase("Insect")
    		        && ind.distanceTo( other ).magnitude() < ind.getRadius() )
    		{
    			return true;
    		}
    	}
    	return false;
    }

    /**
	 * Causes the individaul to eat all edible entities.
	 **/
    public void eat()
    {
    	Bag individuals = ind.getIndividualsWithinDistance(interactionRadius );

    	for( int i = 0; i < individuals.numObjs; i++ )
    	{
    		Individual other = (Individual)individuals.get(i);
    		if( other.getType().equalsIgnoreCase("Insect")
    		        && ind.distanceTo( other ).magnitude() < ind.getRadius() )
    		{
    			other.kill();
    		}
            if( (other.getActionStrategy() instanceof SalamanderStrategy)
    		        && ind.distanceTo( other ).magnitude() < ind.getRadius()
                    && ind.getRadius() > other.getRadius() )
    		{
    			other.kill();
    		}
    	}
    }

	/**
	 * Gives the force which will add a random movement to the motion of the individual.
	 *
	 * @param simState The object containing the state of the simulation.
	 * @return Vector2D A 2 Dimensional vector representing the force in the x and y directions.
     * rand() gives out a number between 0 and 1.
     * This force is zero if the activity level is zero.
	 **/
    public Vector2D forceRandom( CrowdSimulation simState )
	{
        double x = simState.rand() * 2.0 - 1.0;
        double y = simState.rand() * 2.0 - 1.0;
        double l = Math.sqrt( x*x + y*y );
        //returns: getActivityLevel()*RandomStrength*x/l; The forceRandom for the y-component
        // is 3 times as large as for the x-component
        if( getIsHiding() ) {
            return new Vector2D( 0, 0 );
        }
		return new Vector2D( getActivityLevel()*randomStrength*x/l,
							 getActivityLevel()*randomStrength*3*y/l);
	}

	/**
	 * Gives the force due to wanting to go towards a cover object which he remembers.
     *
     * This force vanishes when activity level is 1.  So it only activates for
     * activity levels 1/2 and 0 and when the salamander is not hiding. The purpose
     * of this force is to put the salamander "to bed".
     *
	 * @return Vector2D A 2 Dimensional vector representing the force in the x and y directions.
	 **/
	public Vector2D forceTowardsCoverMemory()
	 // First we will assume that he will not be ejected.
    {
        double eject = 1;

        // Next we set the force to zero if activityLevel() is 1 or 0.7 or if hiding.
		if( getActivityLevel() > 0.6 || getIsHiding() )
		{
			return new Vector2D(0,0);
		}
        //If we are here, then he is not hiding and activity level is not 1 or 0.7.
        //Next we must find a possible bedding place.
		Obstacle hidingObstacle = getClosestObstacle( hidingLocationMemory );
		if( hidingObstacle == null )
        {
    //  If we get to here, then he has no memory of any hiding locations and activity level probably is dropping.
    //  So we have to ensure he is scurring around to find something to hid under.  Otherwise he is just sitting there
    //  as a sitting duck.  So we spread a wider net.
            Obstacle hidingObstacle2 = getClosestHidingObstacle();
            {   if( hidingObstacle2 == null )
                {

        // this is a case that can only occur if there are no obstacles.
                    System.out.println("ERROR: NO OBSTACLES HAVE BEEN FOUND AND SALAMANDER NEEDS TO HIDE." );
                    return new Vector2D(0,0);
                }
                else
		        {
        // Now that we have selected a hiding place, we add that to his memory, if it is not there.
                    if (!hidingLocationMemory.contains(hidingObstacle))
                    {
                    addHidingPlaceToMemory( hidingObstacle2 );
                    }
                }
            // At this point we have an obstacle, so we set it to be hidingObstacle.
            // If he is already hiding, it would be the one that he is under.
            hidingObstacle = hidingObstacle2;
 	        }
        }
        // Now we have a possible hiding place.  Next we need to know if the
        //last move put him inside this hidingObstacle, or was he already under it.
        //
		if( isInside( hidingObstacle ) )
        {
        // If this is so, then we need to see if we have more than one here.
        // If so, then there will be a fight and only one stays.
	        setIsHiding( true );
			setCoverType( hidingObstacle );
            sortSalamanders( hidingObstacle );
            // If our boy is to be ejected, then his isHiding will come back as false.
            {    if( getIsHiding() )
                {
                // If he was already hiding or if he is allowed to hid there, we don't move him.
                    return new Vector2D(0,0);
                }
                else
                {
                // Now we have to eject him and fast. Here we calculate the ejection magnitude.
                // sortSalamanders also sets this obstacle into his occupiedObstacleMemory.
                    eject = -1 * getEjectionForce();
                }
            }
        }
        //  So now we can calculate the main body of the force for ejection or running to get in bed.
   	    Vector2D hidingPoint = hidingObstacle.getCenter();
		// The difference in the x and y coordinates of the salamander and the hiding point.
		double dX = hidingPoint.x-ind.getLocation().x;
		double dY = hidingPoint.y-ind.getLocation().y;
        Vector2D Vec=new Vector2D( dX,dY );
        double mag = Vec.magnitude();
        double forceX = eject * (1-getActivityLevel())* getCoverMemoryAttraction() * dX/mag;
		double forceY = eject * (1-getActivityLevel())* getCoverMemoryAttraction() * dY/mag;
		return new Vector2D( forceX, forceY );
    }

    /**
	 * Gives the force due to wanting to go towards food.  I.e. he sees an insect and runs to grab it.
     *
	 * @return Vector2D A 2 Dimensional vector representing the force in the x and y directions.
	 **/
	public Vector2D forceTowardsFood( Continuous2D world )
	{
		if( this.isHiding || this.getActivityLevel() < 0.6 )
        {
            return new Vector2D(0,0);
        }
		double interactR = getInteractionRadius();
        Bag b = ind.getIndividualsWithinDistance( interactR );

        // Check if we are given an empty collection of individuals.
		if( b==null || b.numObjs == 0 )
        {
        	return new Vector2D(0,0);
        }
		// Shortcut done here defined the local variables and gave them default values in one step.
      	double dX = 0.0;
      	double dY = 0.0;
      	double bobDist = 1.0;
     // We need a scale factor for the distances.  We use the radius of the individual.
        double rscale = ind.getRadius();

        for( int i=0; i<b.numObjs; i++ )
		{
        	Individual bob = (Individual)(b.objs[i]);
        	if( bob != ind && bob.getType().equalsIgnoreCase("Insect"))
        	{
                double dX2 = bob.getLocation().x-ind.getLocation().x;
                double dY2 = bob.getLocation().y-ind.getLocation().y;
	            Vector2D dis = new Vector2D( dX2, dY2 );
                // bobDist is the distance to the insect compared to ind's radius.
                // We limit this to 1/2 so as not to get a singular result.
	            bobDist = dis.magnitude() / rscale;
                if(bobDist < 0.5)
                {
                    bobDist = 0.5;
                }
                // n-vec * ( rscale/dist - rscale/R_i )^2 This checks out.
	            dX += (dis.x/(rscale*bobDist))* Math.pow(1.0/bobDist - rscale/interactR, 2);
	            dY += (dis.y/(rscale*bobDist))* Math.pow(1.0/bobDist - rscale/interactR, 2);
	    	}
        }
        // force = n-vector * (R/|delta x|)**2 * ( 1 - |delta x|/R_i )**2
        // Scale the force by the InfluenceOfOthers factor.
        dX *= foodAttraction;
        dY *= foodAttraction;

		return new Vector2D( dX, dY );
	}

	/**
	 * DJK: Gives the force in the y-direction due to wanting to go towards the region where food can be found.
     *
	 * @return Vector2D: A 2 Dimensional vector representing the force in the y direction.
	 **/
	public Vector2D forceTowardsFoodSource()
	{
        // If he is hiding or needing to be bedded or is bedded, we leave him alone.
		if(this.isHiding || this.getActivityLevel() < 0.6)
        {
            return new Vector2D(0,0);
        }
   		double force = 0.0;
   		double y = ind.getLocation().y;
   		double height = (CrowdSimulation.getInstance()).getWorld().getHeight();
   		//DJK:  old force was = 1.0/(height-y+eps)-(1.0/(y+eps));  We replace it with this.
        //NOTE THAT WE ARE IN A LH SYSTEM WHERE Y INCREASES GOING DOWNWARD.
        if( y > 7.0*height/8.0)
        {
            force = 7.0 - 8.0*y/height;
        }
        else
        { if( y > 5.0*height/8.0 )
          {
            force = 0.0;
          }
          else
          {  if( y > height/2.0 )
             {
               force = 5.0 - 8.0*y/height;
             }
             else
             {  if( y > 3.0*height/8.0 )
                {
                    force = 3.0 - 8.0*y/height;
                }
                {  if( y > height/8.0 )
                   {
                       force = 0.0;
                   }
                   else
                   {
                       force = 1.0 - 8.0*y/height;
                   }
                }
            }
          }
        }
        // The 8 is to make the max force = foodSourceAttraction.
        return new Vector2D(0,getActivityLevel()*foodSourceAttraction*8*force);
	}

	/** Revised by DJK on 11/11/2010.
	 * Gives the force due to wanting to go to some hiding object when there is a larger salamander nearby.
     * We get the closest obstacle which he knows to be unoccupied.
     * Then if he is not in it, we add it to his hiding obstacle memory and calculate the force to it.
	 *
	 * @return Vector2D A 2 Dimensional vector representing the force in the x and y directions.
	 **/
    public Vector2D forceTowardsHiding()
    {
        // We need a criteria for turning this force on.
        if( largerNearBy() ) 
        {
            if( !this.isHiding )
            {
                return new Vector2D(0, 0);
            }
            else
            {
                //  setIsHiding(false);
                return new Vector2D(0, 0);
            }
        }
        else
        {
            if( this.isHiding )
            {
                return new Vector2D(0, 0);
            }
            else
            {
                // First we set the eject parameter in case he won't be ejected.  (Ejection means eject = -1)
                double eject = +1;
                // If we are here, he is being chased by a bigger salamander during any acivityLevel() or being chased out of his bed.
                Obstacle hidingObstacle = getClosestHidingObstacle();
                if (hidingObstacle != null)
                {
                    System.out.println("Ind " + ind.getID() + " wants to hid in Obs " + hidingObstacle.getId() );
                    Vector2D hidingPoint = hidingObstacle.getCenter();
                    double dX = hidingPoint.x - ind.getLocation().x;
                    double dY = hidingPoint.y - ind.getLocation().y;
                    Vector2D direction = new Vector2D(dX,dY).normalize();
                    if (isInside(hidingObstacle))
                    {
                        System.out.println("Ind " + ind.getID() + " is inside Obs " + hidingObstacle.getId() );
                        sortSalamanders(hidingObstacle);
                        // If our boy is to be ejected, then his isHiding will come back as false.
                        if( getIsHiding() )
                        {
                            // If he was already hiding or if he is allowed to hid there, we don't move him.
                           return new Vector2D(0,0);
                        }
                        else
                        {
                            // So now we have to eject him and fast. Here we calculate the ejection magnitude.
                            // sortSalamanders has already set this obstacle into his occupiedObstacleMemory.
                            eject = -1 * getEjectionForce();
                        }
                    }
                    else
                    {
                        if (!hidingLocationMemory.contains(hidingObstacle))
                        {
                            addHidingPlaceToMemory( hidingObstacle );
                        }
                    }
                    // Scale the force value by the cover memory attraction of the salamander
                    double forceX = eject * getCoverAttraction() * direction.x;
                    double forceY = eject * getCoverAttraction() * direction.y;

                    return new Vector2D(forceX, forceY);
                }
                else
                {
                    System.out.println("ERROR: NO OBSTACLES HAVE BEEN FOUND AND the salamander is being chased." );
                    return new Vector2D(0, 0);
                }
            }
        }
    }

	/** Revised on Nov12,2010 by DJK.
     *
	 * Calculates the force on a given individual due to all other individuals.
	 * This is to send him running away from bigger and the same size ones, and towards smaller ones.
	 * @param world The map on which this individual is interacting with objects.
	 * @return A 2-dimensional vector representing the force in the x and y directions.
	 **/
	public Vector2D forceTowardsOthers( Continuous2D world )
	{
        if(this.isHiding)
        {
            return new Vector2D(0,0);
        }
        Bag b = ind.getIndividualsWithinDistance( interactionRadius );
		// Check if we are given an empty collection of individuals.
        if (b == null || b.numObjs == 0)
        {
            return new Vector2D(0, 0);
        }
        else
        {
            // Now to remove all bobs who are hiding and who are inside an
            // obs which this.ind knows to be occupied.
            for (int i = 0; i < b.numObjs; i++)
            {
                Individual bob = (Individual) (b.objs[i]);
                if (bob != ind && bob.getType().equals(ind.getType()))
                {
                    if (((SalamanderStrategy) bob.getActionStrategy()).getIsHiding())
                    {
                        for (int j = 0; j < this.occupiedObstacleMemory.numObjs; j++)
                        {
                            Obstacle obs = (Obstacle) occupiedObstacleMemory.get(j);
                            // If bob is inside any known occupied obstacle and is hidding, we remove bob from the bag b.
                            if (obs.isInside(bob.getLocation()))
                            {
                                b.remove(bob);
                            }
                        }
                    }
                }
            }
        }
		// Check if we now have an empty collection of individuals.
        if (b == null || b.numObjs == 0)
        {
            return new Vector2D(0, 0);
        }
		// Shortcut done here defined the local variables and gave them default values in one step.
      	double dX = 0.0;
      	double dY = 0.0;
      	double bobDistance = 1.0;
      	double pF = 1.0;
        double xx = 1.0;
        double rr = 1.0;
        //
        for( int i=0; i<b.numObjs; i++ )
		{
          	Individual bob = (Individual)(b.objs[i]);
        	if( bob != ind && bob.getType().equals(ind.getType()))
        	{
                // If bob is not hiding, we calculate the force.
                if( !((SalamanderStrategy)bob.getActionStrategy()).getIsHiding() )
        		{
                    double dX2 = bob.getLocation().x-ind.getLocation().x;
                    double dY2 = bob.getLocation().y-ind.getLocation().y;
                    Vector2D dis = new Vector2D( dX2, dY2 );
                    rr = dis.magnitude();
  //                  bobDistance = Math.pow(rr, 2);
	            	pF = calculatePersonalityFactor( bob );
                    xx = rr / getInteractionRadius();

	            	dX += pF*((dis.x)/rr)*( 1 - xx )*( 1 - xx );
	            	dY += pF*((dis.y)/rr)*( 1 - xx )*( 1 - xx );
	            }
	    	}
        }
        // Scale the force by the InfluenceOfOthers factor.
//        dX *= getInfluenceOfOthers();
//        dY *= getInfluenceOfOthers();

		return new Vector2D( dX, dY );
	}

	/**
     * Verified by DJK on Nov12,2010
	 * Calculates the salamanders' attraction towards his given region.
	 * Currently we only calculate the y-component of the force.
	 *
	 * @return Vector2D A 2 Dimensional vector representing the force in the y direction.
	 **/
	public Vector2D forceTowardsRegion()
	{
		if( this.isHiding || this.getActivityLevel() < 0.6 )
        {
            return new Vector2D(0,0);
        }

        double forceY = 0;

		//~r
		double distanceToMarker = getRegionMarker() - ind.getLocation().y;

		if( Math.abs( distanceToMarker ) > regionSize )
		{
			// Scale the force value by the activity level and the regional affinity of the salamander
            //    and also by the regionSize so that the units are carried by regionalAffinity.
			if( distanceToMarker > 0 )
            {
                forceY = getActivityLevel() * getRegionalAffinity() * (distanceToMarker/regionSize-1);
            }
            else
            {
                forceY = getActivityLevel() * getRegionalAffinity() * (distanceToMarker/regionSize+1);
            }
		}

		return new Vector2D(0.0,forceY);
	}

	/**
     * Signs corrected by DJK on Nov12, 2010.
     *
	 * The force of attraction that the Salamander has toward the water source at (0,0).
	 *
	 * @return Vector2D A 2 Dimensional vector representing the force in the y direction.
	 */

    public Vector2D forceTowardsWater()
	{
		if( this.isHiding || this.getActivityLevel() < 0.6 )
        {
            return new Vector2D(0,0);
        }
		double forceY = 0;
        // If there is a larger one nearby, we take the water to be no longer attractive.
		if( largerNearBy() )
        {
            return new  Vector2D(0.0, 0.0);
        }
		// Note that this distance is negative.
		double distanceToWater = 0.0 - ind.getLocation().y;
		double height = (CrowdSimulation.getInstance()).getWorld().getHeight();
        { 
            if( Math.abs(distanceToWater) > getInteractionRadius() )
            {
		        forceY = getActivityLevel() * getWaterAttraction() * getInteractionRadius()/distanceToWater;
            }
            else
            {
		        forceY = - getActivityLevel() * getWaterAttraction();
            }
        }
		return new Vector2D(0.0,forceY);
	}

    /**
     * Represents how active an individual is at differeing times of day.
     *
     * @return The activity level of the individual represented as a value between 0 and 1.
     **/
   	public double getActivityLevel()
   		// The current simTime in hours.
   	{
        // We are currently using an 8 hour day,
        //     0:00 - 1:00, They are in bed
        //     1:00 - 2:00, waking up and activity level = 0.7
        //     2:00 - 5:00, They are roaming around and activity level = 1.0
        //     5:00 - 6:00, They are hunting for bedding and activity level = 0.5
        //     6:00 - 8:00, They are in bed.
   		//double time = mod( (CrowdSimulation.getInstance()).getSimTime(), 24);
   		double time = mod( (CrowdSimulation.getInstance()).getSimTime() + 5.1, 8);
//
   		if( time >= 2 && time < 5 )
   		{
   			return 1.0;
   		}
        if ( time > 1 && time < 2 )
        {
            return 0.7;  // This is the wake-up regime
        }
   		if( time <= 1 || time >= 6 )
   		{
           if ( time < 2 * ind.getActionController().getTimeStep())  // We clear memory at midnight.
            {
                this.occupiedObstacleMemory.clear();
                return 0.0;
            }
            if( time >= 1 - 2 * ind.getActionController().getTimeStep() || time <= 1 )
            {
                this.setIsHiding( false );  // This is to wake him up.
                this.setCoverType( "" );   // This is to remove any cover type.
            }
            return 0.0;
        }
        return 0.5;
    }


 		/* if( time >= 22 && time < 23 )
//   		{
//            if (time > 22 && time < 22 + 2 * ind.getActionController().getTimeStep())
//            {
//                this.occupiedObstacleMemory.clear();
//            }
//            return 0.5;
//   		}
//   		else if( time >= 23 || time <= 1)
//   		{
//   		//	this.occupiedObstacleMemory.clear();
//            return 1.0;
//   		}
//   		else if( time > 1 && time < 2 )
//   		{
//   		//	this.occupiedObstacleMemory.clear();
//            return 0.5;
//   		}
//
//   		return 0;
//   	}
*/
    /**
	 * The force of attraction that the Salamander has toward where smaller salamanders might be.
	 *
	 * @return Vector2D A 2 Dimensional vector representing the force in the x and y directions.
	 */
//	public Vector2D forceTowardsSmallerSalamanders()
//	{
//		double forceY = 0;
//		double height = (CrowdSimulation.getInstance()).getWorld().getHeight();
//
//		//~r
//		double distanceToEdge = height - ind.getLocation().y;
//
//		forceY = getActivityLevel() * getSmallerSalamanderAttraction() * distanceToEdge;
//
//		return new Vector2D(0.0,forceY);
//	}

	/**
	 * Returns an obstacle whose center is closest to him and which he thinks is not occupied.
	 * The closest hiding point will be the center of the closest
	 * "attractive" obstacle.  The closest attractive obstacles's center
	 * may not be the closest center of all the attractive obstacles.
	 *
	 * @return The obstacle whose edge is closest to the individual.
	 **/
	private Obstacle getCenterOfClosestObstacle(Bag obstacles)
	{
        if( obstacles == null)
		{
            System.out.println("Error: Found bag empty on input to getCenterOfClosestObstacle()");
			return null;
		}
        // So now we know the bag is not empty
		Obstacle closestObstacle = null;

		double distance=-1;

        Bag obstaclesOfInterest = obstacles;

		for( int i=0; i<obstaclesOfInterest.numObjs; i++ )
		{
                // We calculate distance from ind.center to the closest edge of the obstacle.
                double newDistance = ind.distanceTo((Obstacle) obstaclesOfInterest.objs[i]).magnitude();

               // System.out.println(" Obstacle of Interest : " + ((Obstacle) obstacles.objs[i]).isOfInterest());
                // Check 3 things:
                // 1.) Is the obstacle within interaction distance
                // 2.) Is it an "attractive" obstacle
                // 3.) Is it closer than the previous "attractive" obstacles etc...
                // AJ added fourth thing to check - if the obstacle is already occupied took out next line to check if obstacle is within interaction radius.
                // if (newDistance - ind.getRadius() < getInteractionRadius() &&
                //       ((Obstacle) obstaclesOfInterest.objs[i]).getForce() < 0 &&
                if(distance == -1 || newDistance < distance)
                {
                    closestObstacle = (Obstacle) obstaclesOfInterest.objs[i];
                    //System.out.println("Individual ID : " + ind.getID() + ", Obstacle ID : " + closestObstacle.getId() + " Obstacle ofInterest " + closestObstacle.isOfInterest() );
                    distance = newDistance;
                }
		}
		// If we don't find any attractive obstacles then return null.
		if( distance == -1)
		{
            System.out.println("Error: Found bag empty at end of getCenterOfClosestObstacle()");
			return null;
		}
		// Return the center of the closest obstacle.
		return closestObstacle;
	}
	/** Revised by DJK on Nov09,2010
     *
	 * This collects all obstacles which he knows are not occupied and finds the closest one.
     * If he thinks all are occupied, then his memory is scrubed.
     * If he is inside one, then that one is returned.
	 *
	 * @return The unoccupied obstacle which is the closest to the Individual and can be used for hiding.
	 **/

    private Obstacle getClosestHidingObstacle()
	{
		Bag obstacles = CrowdSimulation.getInstance().getTerrain().allObjects;

		if( obstacles == null)
		{
            System.out.println("Error: Found no obstacles anywhere in the world at start of getClosestHidingObstacle()");
			return null;
		}

        Bag obstaclesOfInterest = new Bag();
        System.out.println("Ind " + ind.getID() + " thinks there are " + this.occupiedObstacleMemory.numObjs + " occupied obs" );
        { if (this.occupiedObstacleMemory.numObjs > 0)
            {
                for (int i = 0; i < obstacles.numObjs; i++)
                {
                    if (!this.occupiedObstacleMemory.contains(obstacles.get(i)))
                    {
                        obstaclesOfInterest.add(obstacles.objs[i]);
                    }
                }
            }
            else
            {
                // If we are here, then there is no obstactle occupied, as far as his memory is concerned.
                System.out.println("Ind " + ind.getID() + " thinks that no obs is occupied" );
                // So we include all obstacles in his bag.
                obstaclesOfInterest = obstacles;
            }
        }
        // Now we have the collection of relevant obstacles.
        if (obstaclesOfInterest.numObjs == 0)
        {
            // If we are here, he thinks they all are occupied.  So we will null his memory and
            // send him on a hunting trip, to find one unoccupied.
            obstaclesOfInterest = obstacles;
            occupiedObstacleMemory.clear();
        }
	    // Check if the individual is inside any obstacle. If he is, then that is the obstacle.
        for( int i=0; i<obstaclesOfInterest.numObjs; i++ )
        {
			Obstacle obs = (Obstacle)obstaclesOfInterest.objs[i];
			if( obs.getForce() < 0 && obs.isInside( ind.getLocation() ) )  {
				return obs;
			}
		}
        if( obstaclesOfInterest == null)
		{
            System.out.println("Error: Found no obstacles at end of getClosestHidingObstacle()");
			return null;
		}
        // So he is not under an obstacle and the bag is not empty.
        // So we now have to sort through and find the closest one.
        return getClosestObstacle(obstaclesOfInterest);
	}

	/**
	 * Returns the closest obstacle to him among the contents of a bag.
     * Checked by DJK on 11/9/10.
	 *
	 * @return The obstacle whose edge is closest to the individual.
	 **/
	private Obstacle getClosestObstacle(Bag obstacles)
	{
        if( obstacles == null)
		{
            System.out.println("Error: Found bag empty on input to getClosestObstacle()");
			return null;
		}
        // So now we know the bag is not empty
		Obstacle closestObstacle = null;

		double distance=-1;

        Bag obstaclesOfInterest = obstacles;

		for( int i=0; i<obstaclesOfInterest.numObjs; i++ )
		{
                // We calculate distance from ind.center to the closest edge of the obstacle.
                double newDistance = ind.distanceTo((Obstacle) obstaclesOfInterest.objs[i]).magnitude();

               // System.out.println(" Obstacle of Interest : " + ((Obstacle) obstacles.objs[i]).isOfInterest());
                // Check 3 things:
                // 1.) Is the obstacle within interaction distance
                // 2.) Is it an "attractive" obstacle
                // 3.) Is it closer than the previous "attractive" obstacles etc...
                // AJ added fourth thing to check - if the obstacle is already occupied took out next line to check if obstacle is within interaction radius.
                // if (newDistance - ind.getRadius() < getInteractionRadius() &&
                //       ((Obstacle) obstaclesOfInterest.objs[i]).getForce() < 0 &&
                if(distance == -1 || newDistance < distance)
                {
                    closestObstacle = (Obstacle) obstaclesOfInterest.objs[i];
                    System.out.println("Ind " + ind.getID() + " finds that Obs " + closestObstacle.getId() + " is closest" );
                    distance = newDistance;
                }
		}
		// If we don't find any attractive obstacles then return null.
		if( distance == -1)
		{
            System.out.println("Error: Found bag empty at end of getClosestObstacle()");
			return null;
		}
		// Return the center of the closest obstacle.
		return closestObstacle;
	}


     /** Revised by DJK on Nov10, 2010.
	 * Determines if any, and collects all salamanders under the obstacle.
	 *
	 * @return Bag of all salamanders who are under the obstacle.
	 **/
    public Bag individualsUnderObstacle(Obstacle obs)
    {
        Bag individualsUnderObstacle = new Bag();
        Bag individuals = ind.getActionController().getIndividuals();
        for (int i = 0; i < individuals.numObjs; i++)
        {
            Individual other = (Individual) individuals.get(i);
        //
       //     Shorten this method by using the "isInside" which is below.
       //
            if (other.getActionStrategy() instanceof SalamanderStrategy) 
            {
                if (obs instanceof RectangularObstacle)
                {
                    System.out.println("Obs "+ obs.getId() + " is rectangular");
                    if( obs.isInside( other.getLocation() ) )
                    {
                        System.out.println("Ind "+ other.getID() + " is under Obs " + obs.getId());
                        individualsUnderObstacle.add(other);
                    }
                }
                else
                {
                    if( obs instanceof CircularObstacle )
                    {
                        System.out.println("Obs "+ obs.getId() + " is circular");
                        if( obs.isInside( other.getLocation() ) )
                        {
                            System.out.println("Ind "+ other.getID() + " is under Obs " + obs.getId());
                            individualsUnderObstacle.add(other);
                        }
                    }
                }
            }
        }
        return individualsUnderObstacle;
    }

    /**  Revised by DJK on 10/26/10.
     *
	 * Test to see if the center of the individual is inside an obstacle.
	 *
	 * @param obs The obstacle to test to see if the center of the individual is inside the obs.
	 * @return A Boolean representing if the individual is under the obstacle.
	 **/
    private boolean isInside( Obstacle obs )
    {
    	if( obs instanceof RectangularObstacle )
    	{
            Vector2D endPoint = ind.surfacePointOn( obs );
            // This returns the closest point on the edge of the obs, unless ind's center is inside the obs,
            // in which case (-1,-1) is returned.
            if( endPoint.x == -1.0 && endPoint.y == -1.0 )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    	else
    	{
    		CircularObstacle circ = (CircularObstacle)obs;
    		Vector2D dist = new Vector2D(circ.getCenter().x-ind.getLocation().x, circ.getCenter().y-ind.getLocation().y);
            if(dist.magnitude() < circ.getRadius() )
    		{
    			return true;
    		}
            else
            {
    	    	return false;
            }
        }
    }

    /** Checked by DJK on 11/11/2010.
	 * Determines if a larger Salamander is within its interaction radius.
	 *
	 * @return Boolean value telling if the individual can be attacked.
	 **/
    public boolean largerNearBy()
    {
    	Bag individuals = ind.getIndividualsWithinDistance( interactionRadius );

    	for( int i = 0; i < individuals.numObjs; i++ )
    	{
    		Individual other = (Individual)individuals.get(i);
    		if( other.getActionStrategy() instanceof SalamanderStrategy
    				&& getSize() < ((SalamanderStrategy)other.getActionStrategy()).getSize()  )
    		{
    			return true;
    		}
    	}
    	return false;
    }


	/**
	 * Moves the individuals using the passed in time step.
	 *
	 * @param deltaT the timestep that has been set for use for this step.
	 **/
    @Override
	public void move( double deltaT )
	{
		// Set the force acting on the individual.
		ind.setAcceleration( getSumOfForces() );

		//  Set the tryingToHide flag.
		//if( getActivityLevel() == 0 )
		//{
        // setTryingToHide(true);
		//}

		double Xprev = ind.getLocation().x;
		double Yprev = ind.getLocation().y;
		Vector2D direction = ind.getForces();
		direction.normalize();

		double X = Xprev + direction.x* getSpeed() * deltaT;
		double Y = Yprev + direction.y* getSpeed() * deltaT;

		// Actually moves the individual.
		ind.setLocation( CrowdSimulation.getInstance().confineToWorld( ind, X, Y ) );

		// Set the new velocity of the individual.
		ind.setVelocity( new Vector2D( direction.x * getSpeed(), direction.y * getSpeed() ) );
	}

 	/** Revised by DJK on Nov11,2010.
     *
	 * This is to sort the salamanders under an obstacle into those which have to leave and the one
     * that gets to stay. If our ind is one of the ones which have to leave, his isHiding is turned
     * to false and the obs is put into his occupiedObstacleMemory.
     *
     * This assumes that the individual has been found to be under the obstacle.
	 *
	 * @return Void.  It sets isHiding of ind according to size and number of other salamanders present.
     **/
    private void sortSalamanders( Obstacle hidingObstacle )
    {
       Bag indUnderObs = individualsUnderObstacle(hidingObstacle);
                System.out.println("Number of inds inside Obs " + hidingObstacle.getId() + " is " + indUnderObs.numObjs);
                System.out.println("This Ind "  + ind.getID() + " is inside Obs " + hidingObstacle.getId());
                if (indUnderObs.numObjs > 1)
                {
                // Now we have more than one under this obstacle.  So we have to sort them out as to who stays.
                // First we see if any smaller ones are here.
                    for (int i = 0; i < indUnderObs.numObjs; i++)
                    {
                        Individual bob = (Individual) indUnderObs.get(i);
                        if (ind.getID() != bob.getID())
                        {
                            if (getSize() > ((SalamanderStrategy) bob.getActionStrategy()).getSize())
                            {
                                // If ind > bob, then we get to here.  That means that he could chase this bob out.
                                System.out.println("Ind " + ind.getID() + " is larger than Bob (Ind " + bob.getID() + ")" );
                                setIsHiding(true);
                                setCoverType(hidingObstacle);
                                ((SalamanderStrategy) bob.getActionStrategy()).setIsHiding(false);
                                if (!((SalamanderStrategy) bob.getActionStrategy()).occupiedObstacleMemory.contains(hidingObstacle))
                                {
                                    ((SalamanderStrategy) bob.getActionStrategy()).occupiedObstacleMemory.add(hidingObstacle);
                                }
                                // Note that this isHiding could become set to false down below.
                            }
                        }
                    }
                    // Second we see if any bigger ones are there.
                    for (int i = 0; i < indUnderObs.numObjs; i++)
                    {
                        Individual bob = (Individual) indUnderObs.get(i);
                        if (ind.getID() != bob.getID())
                        {
                            if (getSize() < ((SalamanderStrategy) bob.getActionStrategy()).getSize())
                            {
                                // If Ind < bob(i), then we get to here.
                                System.out.println("Ind " + ind.getID() + " is smaller than Bob (Ind " + bob.getID()+ ")" );
                                // So there is a bob bigger than our boy.  So he cannot stay here and hid.
                                // So once the next two statements are done, we can return.
                                setIsHiding(false);
                                setCoverType("");
                                if (!this.occupiedObstacleMemory.contains(hidingObstacle))
                                {
                                occupiedObstacleMemory.add(hidingObstacle);
                                }
                                // If we ever get to here, we need go no further - he will have to leave.
                                return;
                            }
                        }
                    }
                    // Third, if we get to here, we check for the presence of any his own size.
                    for (int i = 0; i < indUnderObs.numObjs; i++)
                    {
                        Individual bob = (Individual) indUnderObs.get(i);
                        if (ind.getID() != bob.getID())
                        {
                            if (getSize() == ((SalamanderStrategy) bob.getActionStrategy()).getSize())
                            {
                                // Now, if bob(i) is already hiding there, our boy is going to be chased out.
                                if (((SalamanderStrategy) bob.getActionStrategy()).getIsHiding())
                                {
                                    System.out.println("Bob (Ind " + bob.getID() + ") was here before Ind " + ind.getID() );
                                    // If we ever get to here, he cannot hid here and we need go no further.
                                    setIsHiding(false);
                                    setCoverType("");
                                    if (!this.occupiedObstacleMemory.contains(hidingObstacle))
                                    {
                                        occupiedObstacleMemory.add(hidingObstacle);
                                    }
                                    return;
                                }
                            }
                        }
                    }
                    // If we get to here, then no one his own size has isHiding set to TRUE.
                    // So we let him stay here.
                    System.out.println("Ind " + ind.getID() + " is to stay here and hide." );
                    setIsHiding(true);
                    setCoverType(hidingObstacle);
                } 
                else
                {
                // Here we only have our guy under the obstacle so it is okay for him to hide here.
                System.out.println("Only ind under Obs " + hidingObstacle.getId() + " is Ind " + ind.getID());
                    setIsHiding(true);
                    setCoverType( hidingObstacle );
                    return;
                }
            }

	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
    @Override
	public void setParameters( Map parameters )
	{
		super.setParameters( parameters );

		if( parameters.containsKey( "coverAttraction" ) )
		{
			setCoverAttraction( Parameters.convertToDouble(
				parameters.get( "coverAttraction" ),
				getCoverAttraction(),
				"coverAttraction for SalamanderStrategy construction must be a Double or a string representing a Double." ));
		}
		if( parameters.containsKey( "coverMemoryAttraction" ) )
		{
			setCoverMemoryAttraction( Parameters.convertToDouble(
				parameters.get( "coverMemoryAttraction" ),
				getCoverMemoryAttraction(),
				"coverMemoryAttraction for SalamanderStrategy construction must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "ejectionForce" ) )
		{
			setEjectionForce( Parameters.convertToDouble(
				parameters.get( "ejectionForce" ),
				getEjectionForce(),
				"ejectionForce for SalamanderStrategy construction must be a Double or a string representing a Double." ));
		}
		if( parameters.containsKey( "foodSourceAttraction" ) )
		{
			setFoodSourceAttraction( Parameters.convertToDouble(
				parameters.get( "foodSourceAttraction" ),
				getFoodSourceAttraction(),
				"foodSourceAttraction for SalamanderStrategy construction must be a Double or a string representing a Double." ));
		}
		if( parameters.containsKey( "foodAttraction" ) )
		{
			setFoodAttraction( Parameters.convertToDouble(
				parameters.get( "foodAttraction" ),
				getFoodAttraction(),
				"foodAttraction for SalamanderStrategy construction must be a Double or a string representing a Double." ));
		}
//		if( parameters.containsKey( "influenceOfOthers" ) )
//		{
//			setInfluenceOfOthers( Parameters.convertToDouble(
//				parameters.get( "influenceOfOthers" ),
//				getInfluenceOfOthers(),
//				"influenceOfOthers for SalamanderStrategy construction must be a Double or a string representing a Double." ));
//		}
		if( parameters.containsKey( "interactionRadius" ) )
		{
			setInteractionRadius( Parameters.convertToDouble(
				parameters.get( "interactionRadius" ),
				getInteractionRadius(),
				"interactionRadius for SalamanderStrategy construction must be a Double or a string representing a Double." ));

		}
		if( parameters.containsKey( "memorySize" ) )
		{
			setMemorySize( Parameters.convertToInt(
                    parameters.get( "memorySize" ),
				getMemorySize(),
				"memorySize for SalamanderStrategy construction must be an Integer or a string representing an Integer." ));
		}
//          if( parameters.containsKey( "radiusOfConcern" ) )
//		{
//			setRadiusOfConcern( Parameters.convertToDouble(
//				parameters.get( "radiusOfConcern" ),
//				getRadiusOfConcern(),
//				"radiusOfConcern for SalamanderStrategy construction must be a Double or a string representing a Double." ));
//		}
        if( parameters.containsKey( "regionalAffinity" ) )
		{
			setRegionalAffinity( Parameters.convertToDouble(
				parameters.get( "regionalAffinity" ),
				getRegionalAffinity(),
				"regionalAffinity for SalamanderStrategy construction must be a Double or a string representing a Double." ));
		}
		if( parameters.containsKey( "regionMarker" ) )
		{
			setRegionMarker( Parameters.convertToDouble(
				parameters.get( "regionMarker" ),
				getRegionMarker(),
				"regionMarker for SalamanderStrategy construction must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "waterAttraction" ) )
		{
			setWaterAttraction( Parameters.convertToDouble(
				parameters.get( "waterAttraction" ),
				getWaterAttraction(),
				"WaterAttraction for SalamanderStrategy construction must be a Double or a string representing a Double." ));
		}
//		if( parameters.containsKey( "violenceRating" ) )
//		{
//			setViolenceRating( Parameters.convertToDouble(
//				parameters.get( "violenceRating" ),
//				getViolenceRating(),
//				"WaterAttraction for SalamanderStrategy construction must be a Double or a string representing a Double." ));
//		}
		if( parameters.containsKey( "pfA" ) )
		{
			setPfA( Parameters.convertToDouble(
				parameters.get( "pfA" ),
				getPfA(),
				"pfA for SalamanderStrategy construction must be a Double or a string representing a Double." ));
		}
		if( parameters.containsKey( "pfB" ) )
		{
			setPfB( Parameters.convertToDouble(
				parameters.get( "pfB" ),
				getPfB(),
				"pfB for SalamanderStrategy construction must be a Double or a string representing a Double." ));
		}
		if( parameters.containsKey( "pfC" ) )
		{
			setPfC( Parameters.convertToDouble(
				parameters.get( "pfC" ),
				getPfC(),
				"pfC for SalamanderStrategy construction must be a Double or a string representing a Double." ));
		}
//		if( parameters.containsKey( "speed" ) )
//		{
//			setSpeed( Parameters.convertToDouble(
//				parameters.get( "speed" ),
//				getSpeed(),
//				"speed for SalamanderStrategy construction must be a Double or a string representing a Double." ));
//		}
		if( parameters.containsKey( "randomStrength" ) )
		{
			setRandomStrength( Parameters.convertToDouble(
				parameters.get( "randomStrength" ),
				getRandomStrength(),
				"random Strength for SalamanderStrategy construction must be a Double or a string representing a Double."));
		}
//		if( parameters.containsKey( "smallerSalamanderAttraction" ) )
//		{
//			setSmallerSalamanderAttraction( Parameters.convertToDouble(
//				parameters.get( "smallerSalamanderAttraction" ),
//				getRandomStrength(),
//				"smallerSalamanderAttraction for SalamanderStrategy construction must be a Double or a string representing a Double."));
//		}
        if( parameters.containsKey( "size" ) )
		{
			setSize( Parameters.convertToDouble(
				parameters.get( "size" ),
				getSize(),
				"size for SalamanderStrategy construction must be a Double or a string representing a Double."));
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
	/**
	 **/
    public double getCoverAttraction()
	{
		return coverAttraction;
	}

	/**
	 **/
	public void setCoverAttraction( double val )
	{
		coverAttraction = val;
	}

	/**
	 **/
    public double getCoverMemoryAttraction()
	{
		return coverMemoryAttraction;
	}

	/**
	 **/
	public void setCoverMemoryAttraction( double val )
	{
		coverMemoryAttraction = val;
	}

	/**
	 **/
    public double getFoodSourceAttraction()
	{
		return foodSourceAttraction;
	}

	/**
	 **/
	public void setFoodSourceAttraction( double val )
	{
		foodSourceAttraction = val;
	}

	/**
	 **/
    public double getFoodAttraction()
	{
		return foodAttraction;
	}

	/**
	 **/
	public void setFoodAttraction( double val )
	{
		foodAttraction = val;
	}

//	/**
//	 **/
//    public double getInfluenceOfOthers()
//	{
//		return influenceOfOthers;
//	}
//
//	/**
//	 **/
//	public void setInfluenceOfOthers( double val )
//	{
//		influenceOfOthers = val;
//	}
//
//	/**
//     **/
//    @Override
//    public double getInteractionRadius()
//    {
//		return interactionRadius;
//	}
//	/**
//	 **/
//    @Override
//
//	/**
//     **/
//    public double getRadiusOfConcern()
//    {
//		return radiusOfConcern;
//	}
//
//	/**
//	 **/
//	public void setRadiusOfConcern( double val )
//	{
//		radiusOfConcern = val;
//	}
//
//	/**
//     **/
    public int getMemorySize()
    {
		return memorySize;
	}

	/**
	 **/
	public void setMemorySize( int val )
	{
		memorySize = val;
	}

		/**
	 **/
    public double getPfA()
    {
    	return pfA;
    }

    /**
	 **/
    public void setPfA( double val )
    {
    	pfA = val;
    }

	/**
	 **/
    public double getPfB()
    {
    	return pfB;
    }

    /**
	 **/
    public void setPfB( double val )
    {
    	pfB = val;
    }

    /**
	 **/
    public double getPfC()
    {
    	return pfC;
    }

    /**
	 **/
    public void setPfC( double val )
    {
    	pfC = val;
    }

     /**
	 **/
    public double getRegionalAffinity()
	{
		return regionalAffinity;
	}

	/**
	 **/
	public void setRegionalAffinity( double val )
	{
		regionalAffinity = val;
	}

	/**
	 **/
	public void setRegionMarker( double val )
	{
		regionMarker = val;
	}

	/**
	 **/
    public double getRegionMarker()
	{
		return regionMarker;
	}

	/**
	 **/
//    public double getViolenceRating()
//	{
//		return violenceRating;
//	}

	/**
	 **/
//	public void setViolenceRating( double val )
//	{
//		violenceRating = val;
//	}
//
	/**
	 **/
    public double getWaterAttraction()
	{
		return waterAttraction;
	}

	/**
	 **/
	public void setWaterAttraction( double val )
	{
		waterAttraction = val;
	}


////////////////////////////////////////////////////////////////////////////////
// SALAMANDER STATE Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
    /**
	 **/
    public boolean getIsHiding()
    {
    	return isHiding;
    }

    /**
	 **/
    public void setIsHiding( boolean val)
    {
    	isHiding = val;
    }

    /**
	 **/
    public boolean getTryingToHide()
    {
    	return tryingToHide;
    }

    /**
	 **/
    public void setTryingToHide( boolean val)
    {
    	tryingToHide = val;
    }

    public String getCoverType()
    {
    	return coverType;
    }

    public void setCoverType( String val )
    {
    	coverType = val;
    }

    public void setCoverType( Obstacle obs )
    {
    	if( obs instanceof CircularObstacle )
    	{
    		setCoverType( "rock" );
    	}
    	else if( obs instanceof RectangularObstacle )
    	{
    		if( obs.getLocation().getY() < 20  )
    		{
    			setCoverType( "slate" );
    		}
    		else
    		{
    			setCoverType( "log" );
    		}
    	}
    	else
    	{
    		setCoverType( "" );
    	}
    }

////////////////////////////////////////////////////////////////////////////////
// SALAMANDER FORCES Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

    /**
	 **/
    public double getEjectionForce()
    {
    	return ejectionForce;
    }

    /**
	 **/
    public void setEjectionForce( double val )
    {
    	ejectionForce = val;
    }

    /**
	 **/
    public Vector2D getForceRandom()
    {
    	return forceRandom;
    }

    /**
	 **/
    public void setForceRandom( Vector2D val )
    {
    	forceRandom = val;
    }

    /**
	 **/
    public Vector2D getForceTowardsCoverMemory()
    {
    	return forceTowardsCoverMemory;
    }

    /**
	 **/
    public void setForceTowardsCoverMemory( Vector2D val)
    {
    	forceTowardsCoverMemory = val;
    }

    /**
	 **/
    public Vector2D getForceTowardsFood()
    {
    	return forceTowardsFood;
    }

    /**
	 **/
    public void setForceTowardsFood( Vector2D val)
    {
    	forceTowardsFood = val;
    }

    /**
	 **/
    public Vector2D getForceTowardsFoodSource()
    {
    	return forceTowardsFoodSource;
    }

    /**
	 **/
    public void setForceTowardsFoodSource( Vector2D val)
    {
    	forceTowardsFoodSource = val;
    }

    /**
	 **/
    public Vector2D getForceTowardsHiding()
    {
    	return forceTowardsHiding;
    }

    /**
	 **/
    public void setForceTowardsHiding( Vector2D val)
    {
    	forceTowardsHiding = val;
    }

    /**
	 **/
    public Vector2D getForceTowardsOthers()
    {
    	return forceTowardsOthers;
    }

    /**
	 **/
    public void setForceTowardsOthers( Vector2D val)
    {
    	forceTowardsOthers = val;
    }

    /**
	 **/
    public Vector2D getForceTowardsRegion()
    {
    	return forceTowardsRegion;
    }

    /**
	 **/
    public void setForceTowardsRegion( Vector2D val)
    {
    	forceTowardsRegion = val;
    }

    /**
	 **/
    public Vector2D getForceTowardsWater()
    {
    	return forceTowardsWater;
    }

    /**
	 **/
    public void setForceTowardsWater( Vector2D val)
    {
    	forceTowardsWater = val;
    }

/**
     * Gets the interaction radius of the strategy.
     *
     * @return The interaction radius of the strategy.
	 **/
    public double getInteractionRadius()
	{
		return super.getInteractionRadius();
	}

	/**
     * Sets the interaction radius of the strategy.
     * This also sets the radius of concern to match the interacting radius.
     *
     * @param val The interaction radius of the strategy.
	 **/
    public void setInteractionRadius( double val )
	{
		super.setInteractionRadius(val);
        //ind.setRadiusOfConcern(val);
	}

   public void setIndividual( Individual ent )
   {
       this.ind = ent;
       ind.setRadiusOfConcern(this.getInteractionRadius());
   }

/**
	 **/
    public double getRandomStrength()
    {
    	return randomStrength;
    }

    /**
	 **/
    public void setRandomStrength( double val )
    {
    	randomStrength = val;
    }

    /**
	 **/
    public double getSize()
    {
    	return size;
    }

    /**
	 **/
    public void setSize( double val )
    {
    	size = val;
    }

//    /**
//     **/
//    public double getSmallerSalamanderAttraction()
//    {
//    	return smallerSalamanderAttraction;
//    }
//
//    /**
//	 **/
//    public void setSmallerSalamanderAttraction( double val )
//    {
//    	smallerSalamanderAttraction = val;
//    }

    public Bag getOccupiedObstacleMemory(){
       return  occupiedObstacleMemory;
    }

}
