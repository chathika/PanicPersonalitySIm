/*
 * $RCSfile: GroupingStrategy.java,v $ $Date: 2009/08/18 19:55:25 $
 */
package crowdsimulation.actioncontroller.strategy;

import java.util.*;
import crowdsimulation.*;
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
 * This is the strategy for the model based around the Allegience grouping created by Kaup and Oleson.
 *
 *  Individuals move according to the vector sum of five contributing vectors:
 *     cohesion:    		Tendency to move to the center of mass of the local group
 *     avoidance:   		Tendency to not collide with others, very large compared to others
 *     consistency: 		Tendency to move in the same direction as the local group
 *     groupingStrength:    Tendancy of individuals to group together passed on a personality parameter
 *     randomness:  		Random variations applied to the above.
 *
 * @see crowdsimulation.actioncontroller.strategy.ActionStrategy
 * @author $Author: ajolly $
 * @version $Revision: 1.3 $
 * $State: Exp $
 * $Date: 2009/08/18 19:55:25 $
 **/
public class GroupingStrategy extends SocialPotentialStrategy
{		

	/** 
	 * Mathematical representation of the Hyberbolic cosine function.
	 *
	 * @param x The input the to cosh function.
	 * @return The result of calculating the cosh( x ).
	 **/
	static private double cosh( double x )
	{
		return (Math.E*x + Math.pow(Math.E,-x))/2;
	}
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The strength of attraction when the difference in personalities is 0. **/
    private double pfA = 10;
    /** The difference of personality when the attracion/repulsion is 0. **/
    private double pfB = 5;
    /** The strength of repulsion when the difference in personalities is 10. **/				
    private double pfC = 10;

	/** The weight used for summing the cohesion of the individuals. 
	 *  The cohesion controlls how close together the individuals will become. **/
	private double cohesion = 0.02;
	/** The weight used for summing the avoidance of the individuals. 
	 *  The acoidance controlls how close far apart the individuals want to be. **/			
    private double avoidance = 5.0;
    /** The weight used for summing the consistency of the individuals. 
     *  The consistency controlls how strongly the individual will stay on its previous path. **/    
	private double consistency = 0.06;
	/** The weight used for summing a randomness in for the individuals. **/
	private double randomness = 0.06;
	
	/** The weight used for summing the momentum of the individuals. 
	 *  The momentum controlls how difficult it is for the individual to change movement. **/
    private double momentum = 0.2;
    /** The weight used for summing the avoiding obstacle by the individuals. 
     *  The features controlls how the individuals will avoid obstacles. **/		
    private double features = 55.0;
    /** The weight used for summing the waypoint attractions for the individuals. 
     *  The features controlls how the individuals will be attracted to waypoints. **/
    private double waypoints = 80.0;
    /** The distance to be moved at each time step. **/
   	private double jump = 0.7;
   	/** The weight used for summing the grouping nature of the individuals. 
   	 *  The groupingStrength controlls how the individual want to gather together into groups. **/
	private double groupingStrength = 10.0; 
	
	/** The violence rating of the individual. This is what the grouping is based on. **/
	private double violenceRating = 5;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Default constructor for a Grouping Strategy.
	 **/
	public GroupingStrategy() 
	{
	}

	
	/**
	 * Constructor for an Grouping Strategy where the data is set by attribute set passed in.
	 *
	 * @param attributes Map of attributes to be set in the strategy.
	 **/
	public GroupingStrategy( Map attributes ) 
	{
		this();
		this.setParameters( attributes );
	}


	/**
	 * Calculates the attraction between two individuals based on the difference of there violence ratings.
	 * This force is modeled as 2 parabolas. 
	 *    The first one is concave down with a maximum value at (0,pfA) and 
	 *       intersect the x-axis at (pfB,0). 
	 *    The second parabola is concave up andconnects to the first at (pfB, 0) 
	 *       and has a minimum value at ( 10, -pfC )
	 *
	 * @param self The individual which is to be moved based on this force.
	 * @param bob The individual causing hte influence.
	 * @return The strength of attraction or repulsion between these individuals.
	 **/
   	private double calculatePersonalityFactor( Individual self, Individual bob )
   	{
    	double personalityFactor = 0.0;
      	double dP = 0.0;
		ActionStrategy strategy = bob.getActionStrategy();
      	
      	if( strategy instanceof GroupingStrategy)
      	{
      		dP = Math.abs(getViolenceRating() - ((GroupingStrategy)(strategy)).getViolenceRating());
      	}
      	else
      	{
      		dP = 10;
      	}

      	if( dP <= pfB )
      	{
         	personalityFactor = -1*(pfA/Math.pow(pfB,2))*Math.pow(dP,2)+pfA;
      	}
      	else if( dP > pfB )
      	{
			double a = pfC/SQR(pfB-10);
         	personalityFactor = a*SQR(dP-10)-pfC;
      	}
      	
      	return personalityFactor;
   	}	

	/**
	 * This will adjust the individuals violence rating based on the individuals witin a given region.
	 *
	 * @param b The colloection of all individuals or obstacle needed to modify the personality.
	 * @param world The map on which this individual is interacting with the object in. 
	 **/
   	private void updatePersonality( Bag b, Continuous2D world )
   	{
      	int xi;
      	int yi;
      	double newPersonality = 0; 
      	double bobPersonality = 0;
      	double personality = getViolenceRating();
      	double bobInfluence;
      	double bobDistance;
      	double xb = 0;
      	double yb = 0;
      
        for( int i=0; i<b.numObjs; i++ )
        {
            Individual influencingAgent = (Individual)(b.objs[i]);
            bobPersonality = ((GroupingStrategy)influencingAgent.getActionStrategy()).getViolenceRating();
            
            Vector2D dis = new Vector2D( world.tv( ind.getLocation().getDouble2D(), influencingAgent.getLocation().getDouble2D() ) );
               
            bobDistance = Math.sqrt(Math.pow((dis.x),2)+Math.pow((dis.y),2));
            bobInfluence = (1/cosh(Math.pow((bobPersonality-personality),2)))*(bobPersonality-personality);
        	newPersonality += (bobInfluence/Math.max( Math.pow( bobDistance, 2 ), 4));
        }
        
		setViolenceRating( newPersonality );
	}

	/**
	 * Calculates the force on a given individual for the grouping component.
	 * 
	 * @param b The colloection of all individuals or obstacle needed to calculate this force.
	 * @param world The map on which this individual is interacting with the object in the bag b.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
   	private Vector2D towardsGroup( Bag b, Continuous2D world )
   	{
		// Shortcut done here defined the local variabels and gave them default values in one step.
      	double dX = 0.0;
      	double dY = 0.0;
      	double bobDistance = 1.0;
      	double pF = 1.0;
      
        if( b==null || b.numObjs == 0 ) 
        {
        	return new Vector2D(0,0);
        }
        
        for( int i=0; i<b.numObjs; i++ )
		{
        	Individual bob = (Individual)(b.objs[i]);
        	if( bob != ind)
        	{
	            Vector2D dis = new Vector2D( world.tv( bob.getLocation().getDouble2D(), ind.getLocation().getDouble2D() ) );
	            
	            bobDistance = world.tds( bob.getLocation().getDouble2D(), ind.getLocation().getDouble2D() );
	            pF = calculatePersonalityFactor( ind, bob );
	
	            dX = dX + pF*(dis.x)/Math.max(Math.pow(bobDistance, 3),1);
	            dY = dY + pF*(dis.y)/Math.max(Math.pow(bobDistance, 3),1);
	    	}
        }

		return new Vector2D( dX, dY );
   	}
	
	/**
	 * Calculates the force on a given individual for the consistency component;
	 * 
	 * @param b The colloection of all individuals or obstacle needed to calculate this force.
	 * @param world The map on which this individual is interacting with the object in the bag b.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
    public Vector2D consistency( Bag b, Continuous2D world )
    {
        if( b==null || b.numObjs == 0 ) 
        {
        	return new Vector2D(0,0);
        }
        
		double x = 0; 
        double y = 0;
        int i =0;
        for( i=0; i<b.numObjs; i++ )
        {
        	Individual other = (Individual)(b.objs[i]);
            

            if( !other.isDead() )
            {
            	double dx = world.tdx( ind.getLocation().x, other.getLocation().x );
                double dy = world.tdy( ind.getLocation().y, other.getLocation().y);
                double lensquared = dx*dx+dy*dy;
                if (lensquared <= CrowdSimulation.getInstance().getDiscretization() * CrowdSimulation.getInstance().getDiscretization() )
                {
                	Vector2D m = ((Individual)b.objs[i]).getMomentum();
                    x += m.x;
                    y += m.y;
                }
            }
        }
        
        x /= b.numObjs;
        y /= b.numObjs;
        
        return new Vector2D( consistency*x, consistency*y );
	}
    
	/**
	 * Calculates the force on a given individual for the cohesion component;
	 * 
	 * @param b The colloection of all individuals or obstacle needed to calculate this force.
	 * @param world The map on which this individual is interacting with the object in the bag b.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
    public Vector2D cohesion( Bag b, Continuous2D world )
    {
    	
        if( b==null || b.numObjs == 0 ) 
        {
        	return new Vector2D( 0,0 );
        }
        
        double x = 0; 
        double y= 0;        

        int i =0;
        for( i=0; i<b.numObjs; i++ )
        {
            Individual other = (Individual)(b.objs[i]);
            if( !other.isDead() )
            {
                double dx = ind.getLocation().x - other.getLocation().x;
                double dy = ind.getLocation().y - other.getLocation().y;
                double lensquared = dx*dx+dy*dy;
                if( lensquared <= CrowdSimulation.getInstance().getDiscretization() * CrowdSimulation.getInstance().getDiscretization() )
                {
                    x -= dx;
                    y -= dy;
                }
            }
        }
        
        x /= b.numObjs;
        y /= b.numObjs;
        
        return new Vector2D( cohesion*x, cohesion*y );
    }
    
	/**
	 * Calculates the force on a given individual for the avoidance component;
	 * 
	 * @param b The colloection of all individuals or obstacle needed to calculate this force.
	 * @param world The map on which this individual is interacting with the object in the bag b.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
    public Vector2D avoidance( Bag b, Continuous2D world )
    {
        if( b==null || b.numObjs == 0 )
        {
        	return new Vector2D(0,0);
        }
        
        double x = 0;
        double y = 0;
        
        int i=0;
        int count = 0;

        for( i=0; i<b.numObjs; i++ )
        {
            Individual other = (Individual)(b.objs[i]);
            if( !other.isDead() )
            {
                count++;
                double dx = ind.getLocation().x - other.getLocation().x;
                double dy = ind.getLocation().y - other.getLocation().y;
                double lensquared = dx*dx+dy*dy;
                if( lensquared <= CrowdSimulation.getInstance().getDiscretization() * CrowdSimulation.getInstance().getDiscretization() )
                {
                    x += dx/(lensquared*lensquared + 1);
                    y += dy/(lensquared*lensquared + 1);
                }
            }
        }
        if( count>0 )
        {
            x /= count;
            y /= count;
        }
        
        return new Vector2D( avoidance*x, avoidance*y );
    }
    
    /**
	 * Calculates the force on a given individual towards a given waypoint.
	 * 
	 * @param world The map on which this individual is interacting with the object in the bag b.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
    public Vector2D waypoint( Continuous2D world )
    {
        double x = 0;
	    double y = 0;
    	
    	Bag paths = ind.getInteractingPaths();
    	if( paths.numObjs < 1 ) return new Vector2D(0,0);
    	
    	for( int i=0; i < paths.numObjs; i++)
    	{
    		Path path = (Path)paths.objs[i];
    		
			Waypoint wPoint = path.getActiveWaypoint( ind );
			if( wPoint != null)
			{

              	double dx = ind.getLocation().x - wPoint.getTargetPoint(ind).x;
                double dy = ind.getLocation().y - wPoint.getTargetPoint(ind).y;
                double lensquared = dx*dx+dy*dy;
       
                if( lensquared <= CrowdSimulation.getInstance().getDiscretization() * CrowdSimulation.getInstance().getDiscretization() )
                {
 	            	x -= dx/(lensquared*lensquared + 1);
   	            	y -= dy/(lensquared*lensquared + 1);
  	        	}
	        }
	    }
        
        return new Vector2D( waypoints*x, waypoints*y );
    }
        
	/**
	 * Creates a random force.
	 * 
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
    public Vector2D randomness( CrowdSimulation simState )
    {
    	
        double x = simState.rand() * 2.0 - 1.0;
        double y = simState.rand() * 2.0 - 1.0;
        double l = Math.sqrt(x * x + y * y);
        x /= l;
        y /= l;
        
        return new Vector2D( randomness* x,randomness * y);
    }

	/**
	 * Calculates the force on a given individual for the feature component;
	 * 
	 * @param b The collection of all individuals or obstacle needed to calculate this force.
	 * @param terrain The map on which this individual is interacting with the object in the bag b.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
    public Vector2D features( Bag b, Continuous2D terrain )
    {
        if( b==null || b.numObjs == 0 )
        {
        	return new Vector2D(0,0);
        }
        
        double x = 0;
        double y = 0;
        
        int i=0;
        int count = 0;

        for( i=0; i<b.numObjs; i++ )
        {
			Obstacle feature = (Obstacle)(b.objs[i]);
            double force = feature.getForce();
			count++;
			
          	Vector2D origin = ind.getLocation();
          	
          	Vector2D rVec = ind.distanceTo( feature );
          	
            double dx = rVec.x;
            double dy = rVec.y;
            double lensquared = dx*dx+dy*dy;
            if( lensquared <= CrowdSimulation.getInstance().getDiscretization() * CrowdSimulation.getInstance().getDiscretization() )
            {
            	x -= dx/(lensquared*lensquared + 1);
                y -= dy/(lensquared*lensquared + 1);
            }
        }
        if( count>0 )
        {
            x /= count;
            y /= count;
        }
        
        return new Vector2D( features*x, features*y );
    }

	/**
	 * Moves the individuals using the passed in time step.
	 *
	 * @param deltaT the timestep that has been set for use for this step.
	 **/
	public void move( double deltaT )
	{
		// Set the force acting on the individual.
		ind.setAcceleration( getSumOfForces() );
			
		double Xprev = ind.getLocation().x;
		double Yprev = ind.getLocation().y;
		Vector2D direction = ind.getForces();
		direction.normalize();

		double X = Xprev + direction.x* getSpeed() * deltaT;
		double Y = Yprev + direction.y* getSpeed() * deltaT;
		
		// Actually moves the individual.	
		ind.setMomentum( new Vector2D( (X-Xprev)/(Math.pow(deltaT, 3 )), (Y-Yprev)/(Math.pow(deltaT, 3 )) ) );
		ind.setLocation( CrowdSimulation.getInstance().confineToWorld( ind, X, Y ) );
		
		// Set the new velocity.
		ind.setVelocity( ind.getForces() );		
	}
	
	/**
	 * This function calculates all of the forces acting on the Individual.
	 *
	 * @param simState The object containing the state of the simulation.
	 **/
	public void calculateMovementParameters( CrowdSimulation simState )
	{
		Vector2D forceRandom = randomness( simState );
		Vector2D forceCohesion = cohesion( ind.getInteractingIndividuals(), simState.getWorld() );
		Vector2D forceAvoidance = avoidance( ind.getInteractingIndividuals(), simState.getWorld() );
		Vector2D forceConsistency = waypoint( simState.getWorld() );
		Vector2D forceMomentum = consistency( ind.getInteractingIndividuals(), simState.getWorld() );
		Vector2D forceFeatures = features( ind.getInteractingObstacles(), simState.getTerrain() );
		Vector2D forceWaypoints = waypoint( simState.getWorld() );
		Vector2D forceGrouping = towardsGroup( ind.getInteractingIndividuals(), simState.getWorld() );
		
		double dx = forceRandom.x +
					forceCohesion.x +
					forceAvoidance.x +
					forceConsistency.x +
					forceMomentum.x +
					forceFeatures.x + 
					forceWaypoints.x +
					forceGrouping.x;
		
		double dy = forceRandom.y +
					forceCohesion.y +
					forceAvoidance.y +
					forceConsistency.y +
					forceMomentum.y +
					forceFeatures.y + 
					forceWaypoints.y +
					forceGrouping.y;
		
		ind.setForceValues( "" +forceRandom.x+","+forceRandom.y
							   +","+forceCohesion.x+","+forceCohesion.y
							   +","+forceAvoidance.x+","+forceAvoidance.y
							   +","+forceConsistency.x+","+forceConsistency.y
							   +","+forceMomentum.x+","+forceMomentum.y
							   +","+forceFeatures.x+","+forceFeatures.y
							   +","+forceWaypoints.x+","+forceWaypoints.y
							   +","+forceGrouping.x+","+forceGrouping.y );

		this.sumOfForces = new Vector2D( dx, dy );
	}

	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
		super.setParameters( parameters );
		
		if( parameters.containsKey( "cohesion" ) )
		{
			setCohesion( Parameters.convertToDouble( 
				parameters.get( "cohesion" ), 
				getCohesion(),
			   "cohesion must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "avoidance" ) )
		{
			setAvoidance( Parameters.convertToDouble( 
				parameters.get( "avoidance" ), 
				getAvoidance(),
			   "avoidance must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "consistency" ) )
		{
			setConsistency( Parameters.convertToDouble( 
				parameters.get( "consistency" ), 
				getConsistency(),
			   "consitency must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "randomness" ) )
		{
			setRandomness( Parameters.convertToDouble( 
				parameters.get( "randomness" ), 
				getRandomness(),
			   "randomness must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "momentum" ) )
		{
			setMomentum( Parameters.convertToDouble( 
				parameters.get( "momentum" ), 
				getMomentum(),
			   "momentum must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "features" ) )
		{
			setFeatures( Parameters.convertToDouble( 
				parameters.get( "features" ), 
				getFeatures(),
			   "features must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "waypoints" ) )
		{
			setWaypoints( Parameters.convertToDouble( 
				parameters.get( "waypoints" ), 
				getWaypoints(),
			   "waypoints must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "jump" ) )
		{
			jump = Parameters.convertToDouble( 
				parameters.get( "jump" ), 
				jump,
			   "jump must be a Double or a string representing a Double." );
		}
		if( parameters.containsKey( "groupingStrength" ) )
		{
			setGroupingStrength( Parameters.convertToDouble( 
				parameters.get( "groupingStrength" ), 
				getGroupingStrength(),
			   "groupingStrength must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "pfA" ) )
		{
			setPfA( Parameters.convertToDouble( 
				parameters.get( "pfA" ), 
				getPfA(),
			   "pfA must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "pfB" ) )
		{
			setPfB( Parameters.convertToDouble( 
				parameters.get( "pfB" ), 
				getPfB(),
			   "pfB must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "pfC" ) )
		{
			setPfC( Parameters.convertToDouble( 
				parameters.get( "pfC" ), 
				getPfC(),
			   "pfC must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "violenceRating" ) )
		{
			setViolenceRating( Parameters.convertToDouble( 
				parameters.get( "violenceRating" ), 
				getViolenceRating(),
			   "violenceRating must be a Double or a string representing a Double." ) );
		}
	}
	

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the strength for the cohesion for the Model.
	 *
	 * @return The strength for the cohesion for the Model.
	 **/
    public double getCohesion()
	{
		return cohesion;
	}
	
	/**
	 * Sets the strength for the cohesion for the Model.
	 *
	 * @param val The strength for the cohesion for the Model.
	 **/
	public void setCohesion( double val )
	{
		cohesion = val;
	}
	
	/**
	 * Gets the valid interval for the strength for the cohesion for the Model.
	 *
	 * @return The interval for the cohesion for the Model.
	 **/
	public Object domCohesion() { return new Interval(0.0,1.0); }
	
	/**
	 * Gets the strength for the avoidance for the Model.
	 *
	 * @return The strength for the avoidance for the Model.
	 **/
	public double getAvoidance()
	{
		return avoidance;
	}
	
	/**
	 * Sets the strength for the cohesion for the Model.
	 *
	 * @param val The strength for the cohesion for the Model.
	 **/
	public void setAvoidance( double val )
	{
		avoidance = val;
	}
	
	/**
	 * Gets the valid interval for the strength for the avoidance for the Model.
	 *
	 * @return The interval for the avoidance for the Model.
	 **/
	public Object domAvoidance() { return new Interval(0.0,1.0); }
	
	/**
	 * Gets the strength for the consistency for the Model.
	 *
	 * @return double The strength for the consistency for the Model.
	 **/
	public double getConsistency()
	{
		return consistency;
	}
    
    /**
	 * Sets the strength for the consistency for the Model.
	 *
	 * @param val The strength for the consistency for the Model.
	 **/
    public void setConsistency( double val )
    {
    	consistency = val;
    }
    
    /**
	 * Gets the valid interval for the strength for the Consistency for the Model.
	 *
	 * @return The interval for the consistency for the Model.
	 **/
    public Object domConsistency() { return new Interval(0.0,1.0); }
    
    /**
	 * Gets the strength for the randomness for the Model.
	 *
	 * @return The strength for the cohesion for the Model.
	 **/
	public double getRandomness()
	{
		return randomness;
	}
	
	/**
	 * Sets the strength for the randomness for the Model.
	 *
	 * @param val The strength for the randomness for the Model.
	 **/
	public void setRandomness( double val )
	{
		randomness = val;
	}
	
	/**
	 * Gets the valid interval for the strength for the randomness for the Model.
	 *
	 * @return The Interval for the randomness for the Model.
	 **/
	public Object domRandomness() { return new Interval(0.0,1.0); }
	
	/**
	 * Gets the strength for the momentum for the Model.
	 *
	 * @return The strength for the momentum for the Model.
	 **/
	public double getMomentum()
	{
		return momentum;
	}
	
	/**
	 * Sets the strength for the momentum for the Model.
	 *
	 * @param val The strength for the momentum for the Model.
	 **/
	public void setMomentum( double val )
	{
		momentum = val;
	}
	
	/**
	 * Gets the valid interval for the strength for the momentum for the Model.
	 *
	 * @return The interval for the momentum for the Model.
	 **/
	public Object domMomentum() { return new Interval(0.0,1.0); }
	
	/**
	 * Gets the strength for the feature for the Model.
	 *
	 * @return The strength for the feature for the Model.
	 **/
    public double getFeatures()
    {
    	return features;
    }
    
    /**
	 * Sets the strength for the feature for the Model.
	 *
	 * @param val The strength for the feature for the Model.
	 **/
    public void setFeatures( double val )
    {
    	features = val;
    }
    
    /**
	 * Gets the valid interval for the strength for the features for the Model.
	 *
	 * @return The Interval strength for the features for the Model.
	 **/
    public Object domFeatures() { return new Interval(0.0,10.0); }
    
    /**
	 * Gets the valid interval for the size of the tStep for the Model.
	 *
	 * @return The Interval of the tStep for the Model.
	 **/
    public Object domtStep() { return new Interval(0.0,1.0); }

	/**
	 * Gets the strength for the waypoints for the Model.
	 *
	 * @return The strength for the waypoints for the Model.
	 **/
    public double getWaypoints()
    {
    	return waypoints;
    }
    
    /**
	 * Sets the strength for the waypoints for the Model.
	 *
	 * @param val The strength for the waypoints for the Model.
	 **/
    public void setWaypoints( double val )
    {
    	waypoints = val;
    }
    
    /**
	 * Gets the valid interval for the strength for the waypoints for the Model.
	 *
	 * @return The Interval for the strength for the waypoints for the Model.
	 **/
    public Object domWaypoints() { return new Interval(0.0,30.0); }

    /**
	 * Gets the strength of the grouping for the Model.
	 *
	 * @return The strength of the grouping for the Model.
	 **/
    public double getGroupingStrength()
    {
    	return groupingStrength;
    }
    
    /**
	 * Sets the strength of the grouping for the Model.
	 *
	 * @param val The strength of the grouping for the Model.
	 **/
    public void setGroupingStrength( double val )
    {
    	groupingStrength = val;
    }


    /**
     * Gets the value for the parameter pfA;
     *
     * @return The value of the pfA parameter.
	 **/
    public double getPfA()
    {
    	return pfA;
    }
    
    /**
     * Sets the value for the parameter pfA;
     *
     * @param val The value of the pfA parameter.
	 **/
    public void setPfA( double val )
    {
    	pfA = val;
    }

	/**
     * Gets the value for the parameter pfB;
     *
     * @return The value of the pfa parameter.
	 **/
    public double getPfB()
    {
    	return pfB;
    }
    
    /**
     * Sets the value for the parameter pfB;
     *
     * @param val The value of the pfB parameter.
	 **/
    public void setPfB( double val )
    {
    	pfB = val;
    }
    
    /**
     * Gets the value for the parameter pfC;
     *
     * @return The value of the pfC parameter.
	 **/
    public double getPfC()
    {
    	return pfC;
    }
    
    /**
     * Sets the value for the parameter pfC;
     *
     * @param val The value of the pfC parameter.
	 **/
    public void setPfC( double val )
    {
    	pfC = val;
    }

    /**
     * Gets the violence rating for the strategy.
     *
     * @return The violence rating for the strategy.
	 **/
    public double getViolenceRating()
    {
    	return violenceRating;
    }
    
    /**
     * Sets the violence rating for the strategy.
     *
     * @param val The violence rating for the strategy.
	 **/
    public void setViolenceRating( double val )
    {
    	violenceRating = val;
    }


}
