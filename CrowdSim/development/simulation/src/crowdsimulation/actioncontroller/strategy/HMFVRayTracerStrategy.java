/*
 * $RCSfile: HMFVRayTracerStrategy.java,v $ $Date: 2009/06/11 20:36:55 $
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
 * Strategy for the action controller based on the HMFV model and uses ray tracing to sample the environement.
 * The forces are the same as the standard HMFV model, but instead of using a single point
 * on each entity to calculate the forces the environment is sampled and the forces are calculated 
 * from all the intersection points which where found.
 *
 * @see crowdsimulation.actioncontroller.strategy.ActionStrategy
 * @author $Author: ganil $
 * @version $Revision: 1.2 $
 * $State: Exp $
 * $Date: 2009/06/11 20:36:55 $
 **/
public class HMFVRayTracerStrategy extends SocialPotentialWithRayTraceVisionStrategy 
{		

////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

    /** Parameter to adjust amplitude the social repulsion forces. **/
	double a		= 25.0;
	/** Parameter to adjust exponential the social repulsion forces. **/
	double b		= 0.08;
	/** Youngian based compressive friction constant **/
	double cYoung	= 750.0;
	/** The attraction between two individuals in the same grouping**/
	double c = 0.8;
	/** friction constant **/
	double gamma	= 200.0;
	/** friction constant **/
	double kappa	= 3000.0;
	/**	0: friction force: 	Gamma * (v_j-v_i)_tang
      * 1: friction force: 	Kappa*(r_0-r) * (v_j-v_i)_tang, if r<r_0
	  *    	0,  if r>r_0 **/
	int frictionSwitch = 1;
	/** time constant of the "self-driving force" 
	 * (~ reaction time) measured in seconds**/	
	double tau = 0.5;
	/** Gauss theta, used for the sample in the random movement**/
	double gaTh = 1.0;
	/** Gauss mean, used for the sample in the random movement**/
	double gaMe = 0;
	/** Gauss cutoff multiplier, used for the sample in the random movement**/
	double gaCM = 3.0;			
	double RAND_MAX = 0;
	/**	Maximal velocity (allowed in numerical method) 
	 * measured in meters per second.**/
	double Vmax	= 5.0;	/*m/s*/
	/** Which kind of injury should be used.
	  * 
	  * 0 none
	  * 1 a pedestrian is crushed (injured) if the sum of the magnitude of
	  *  Young forces exerted on it (by the boundaries and other
	  *  pedestrians) exceeds FCrush_over_1m * the circumference of
	  *  the circle representing the pedestrian 
	  * 2 smoke/fire front started at t=SmokeStartTime, moving with VSmoke
	  *  towards exit
	  *  - smoke/fire front exerts repulsive force on
	  *  pedestrian i if it's within R
	  *  magnitude of force: A' * exp(-(r-r_i)/B), 
	  * 	where r=dist. to smoke/fire
	  * 	direction: towards exit
	  * 	and A' = A*AMult_forSmoke
	  *  - pedestrians passed by it will switch to v_0^i = 0
	  * 	(passing is measured at the particle center)
	  * 3 same as 2 except for the following:
	  *  - pedestrians passed by it will stop moving at all: v_i = 0
	  *  (passing is measured at the particle center) **/	
	int injurySwitch = 1;		
	
	/** A random generator with a normal distribution. **/
	NormalDistributedGenerator randomGen;
	/** A random generator with a uniform distribution. **/
	UniformDistributedGenerator unifRandomGen;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Default constructor for a HMFVRayTracerStrategy.
	 **/
	public HMFVRayTracerStrategy() 
	{
		randomGen = new NormalDistributedGenerator();
		unifRandomGen = new UniformDistributedGenerator( 0, 1 );
	}

	
	/**
	 * Constructor for an HMFV Strategy where the data is set by attribute set passed in.
	 *
	 * @param attributes Map of attributes to be set in the strategy.
	 **/
	public HMFVRayTracerStrategy( Map attributes ) 
	{
		this();
		this.setParameters( attributes );
	}

	
	/**
	 * Calculates the tangential forces on the individual from an entity.
	 * The force is : Kappa*(radius_i - dist) *velocity
	 *
	 * @param poi The Point of Interest which is causing the tangential force.
	 * @return Vector2D The resultant force vector.
	 **/
	public Vector2D tangentialForces( PointOfInterest poi )
	{
		Vector2D pointOfInterest = poi.getLocation();

		Vector2D r = poi.getDistance();
		Vector2D velocity = new Vector2D( ind.getVelocity().x-poi.getVelocity().x, ind.getVelocity().y-poi.getVelocity().y );
		Vector2D tangent = poi.getTangent();
		tangent = new Vector2D( -tangent.x, -tangent.y);
		
     	if( poi.getRadialDistance() > ind.getRadius() )
     	{
     		return new Vector2D( 0, 0 );
     	}
		
		double rx = ind.getCenter().x-pointOfInterest.x;
		double ry = ind.getCenter().y-pointOfInterest.y;
		double scal_prod_over_rsqr = (ry*ind.getVelocity().x - rx*ind.getVelocity().y) / SQR(r.magnitude()); 
		
		double tempF = -1 * kappa * (ind.getRadius() - r.magnitude()) * scal_prod_over_rsqr;
		
		return new Vector2D( ry * tempF, -1 * rx * tempF );
	}

	
	/**
	 * Calculates the normal forces on the individual from an entity.
	 * The force is : 2.0*C_young*(radius_i-dist)
	 *
	 * @param poi The Point of Interest which is causing the normal force.
	 * @return The resultant force vector.
	 **/
	public Vector2D normalForces( PointOfInterest poi  )
	{
		Vector2D pointOfInterest = poi.getLocation();
		double dist = poi.getRadialDistance();
		Vector2D normal = poi.getNormal();
		
     	if( dist > ind.getRadius() )
     	{
     		return new Vector2D( 0, 0 );
     	}
		
		double tempF = cYoung*( ind.getRadius() - dist );
		
		return new Vector2D( normal.x * tempF, normal.y * tempF );
	}

	
	/**
	 * Calculates the social forces on the individual from an entity.
	 * The force is : A*exp[(radius_i-dist)/B]
	 *
	 * @param poi The Point of Interest  which is causing the social force.
	 * @return The resultant force vector.
	 **/
	public Vector2D socialForces( PointOfInterest poi )
	{
		Vector2D pointOfInterest = poi.getLocation();
		double dist = poi.getRadialDistance();
		Vector2D normal = poi.getNormal();
		
		double tempF = a*Math.exp( (ind.getRadius() - dist)/b );
		return new Vector2D( normal.x * tempF, normal.y * tempF );
	}

	
	/**
	 * The force which causes individuals of the same group to gather together.
	 *
	 * @param ind2 The entity which is causing the grouping force.
	 * @return The resultant force vector.
	 **/
	public Vector2D groupingForce( Individual ind2 )
	{
		Vector2D pointOfInterest = ind.surfacePointOn( ind2 );
		Vector2D dist = ind.distanceTo( pointOfInterest );
		Vector2D normal = ind.getNormalOf( ind2 );
		
		double tempF = c*(dist.magnitude()-ind.getRadius());
		
		return new Vector2D( -1*normal.x * tempF, -1*normal.y * tempF );
	}

	
	/**
	 * This get the collection of all individals which are in the same group as this individual.
	 *
	 * @return The bag of individuals which are in the same group as this individual.
	 **/
	public Bag getGroupedIndividuals()
	{
		Bag allIndvs = CrowdSimulation.getInstance().getWorld().getAllObjects();
		Bag grpInd = new Bag();
		if( ind.getGroupId() == 0 ) { return grpInd; };
		
		for( int i = 0; i < allIndvs.size(); i++ )
		{
			Individual indv = (Individual)allIndvs.get( i );
			if( !ind.equals( indv ) && ind.getGroupId() == indv.getGroupId() )
			{
				grpInd.add( indv );
			}
		}
		
		return grpInd;
	}

	
	/**
	 * Force to keep away from the obstacle by a certain distance.
	 * 
	 * @param pointsOfInterest A The Point of Interest with which the interaction is occuring.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
	public Vector2D socialForces( Bag pointsOfInterest )
	{
		Vector2D socialForce = new Vector2D(0,0);

		for( int i =0; i < pointsOfInterest.size(); i++ )
		{
			Vector2D tempForce = socialForces( (PointOfInterest)pointsOfInterest.get( i ) );
			
			socialForce.x = socialForce.x+tempForce.x;
			socialForce.y = socialForce.y+tempForce.y;
		}

		return socialForce;
	}

	
	/**
	 * Force pushing the individual away from an obstacle.
	 * 
	 * @param pointsOfInterest A The Point of Interest with which the interaction is occuring.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
	public Vector2D normalForces( Bag pointsOfInterest )
	{
		Vector2D normalForce = new Vector2D( 0, 0 );
		
		for( int i =0; i < pointsOfInterest.size(); i++ )
		{
			Vector2D tempForce = normalForces( (PointOfInterest)pointsOfInterest.get( i ) );
			normalForce.x = normalForce.x+tempForce.x;
			normalForce.y = normalForce.y+tempForce.y;
		}

		return normalForce;
	}
	
	
	/**
	 * Gives both a frictional and a sliding force for physical contact with an object.
	 * 
	 * @param pointsOfInterest A The Point of Interest with which the interaction is occuring.
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/
	public Vector2D tangentialForces( Bag pointsOfInterest )
	{
		Vector2D tangForce = new Vector2D();
		
		for( int i =0; i < pointsOfInterest.size(); i++ )
		{
			Vector2D tempForce = tangentialForces( (PointOfInterest)pointsOfInterest.get( i ) );
			tangForce.x = tangForce.x+tempForce.x;
			tangForce.y = tangForce.y+tempForce.y;
		}

		return tangForce;
	}

	
	/**
	 * Gives the force due to the current movement of the individual.
	 * 
	 * @return 2 Dimensional vector representing the force in the x and y directions.
	 **/	
	public Vector2D selfPropellingForce()
	{
		Vector2D force = new Vector2D( 0, 0 );
		if( ind.getSelectedPath() == null || ind.getVelocity().magnitude() < ind.getInitialVelocity() )
		{
			selectPath();
			if( ind.isDead() ) return( new Vector2D( 0, 0 ) );
		}
		
    	// self-propelling 
    	Vector2D initialVelocity = ind.getVelocity();
    	Vector2D vel = (Vector2D)initialVelocity.clone();
    	
    	vel.normalize();

  		Path path = ind.getSelectedPath();
  		if( path == null )
  		{
  			return force;
  		}
  		Waypoint wPoint = path.getActiveWaypoint( ind );
	  			  		
  		if( wPoint == null )
  		{
  			((HMFVRayTracerModel)ind.getActionController()).removeIndividual( ind );
  			return( new Vector2D( 0, 0 ) );
  		}
	  		
  		Vector2D contact = ind.distanceTo( wPoint.getTargetPoint(ind) );
  		double r = contact.magnitude();
  		double phi = Math.atan2( contact.y, contact.x );
	  		
    	force.x += path.getWeight() * 1/tau * (ind.getInitialVelocity()*Math.cos(phi) - ind.getVelocity().x);
	  	force.y += path.getWeight() * 1/tau * (ind.getInitialVelocity()*Math.sin(phi) - ind.getVelocity().y);
	  	
	  	return force;
	}


	/**
	 * Gives the force due to a random movement for the individual.
	 * 
	 * @return A 2 Dimensional vector representing the force in the x and y directions.
	 **/	
	public Vector2D random()
	{
		double ksi = 0;
		double eta = 0;
		
		Vector2D force = new Vector2D( 0, 0 );
		
		double sqrt_fact = Math.sqrt( getTimeStep()/((HMFVRayTracerModel)ind.getActionController()).getDefaultDeltaT() );
		
		if(gaTh!=0.0)
	  	{ 
	    	ksi = randomGen.gaussRand( gaMe, gaTh, gaCM );
		  	eta = 2.0*Math.PI * unifRandomGen.nextValue() / (RAND_MAX+1.0);
	  	}
	  	else
	  	{ 
	  		ksi=0.0; 
	  		eta=0.0; 
	  	}
	  	
	  	force.x = sqrt_fact * ksi * Math.cos(eta);
	  	force.y = sqrt_fact * ksi * Math.sin(eta);
	  	return force;
	 }


	/**
	 * Returns The direction of a waypoint which should represent an exit.
	 *
	 * @return The direction of an exit.
	 **/
	Vector2D DirectionOfExit()
	{
        Waypoint wPoint = ind.getSelectedPath().getActiveWaypoint( ind );
        Vector2D exit = wPoint.getTargetPoint(ind);
        return new Vector2D( exit.x-ind.getCenter().x, exit.y-ind.getCenter().y );
	}

	
	/**
	 * This function calculates all of the forces acting on the Individual.
	 *
	 * @param simState The object containing the state of the simulation.
	 **/
	public void calculateMovementParameters( CrowdSimulation simState )
	{
		super.calculateMovementParameters( simState );
        
        Vector2D socialRelationForce = new Vector2D( 0,0 );
        Vector2D normalForce = new Vector2D( 0,0 );
        Vector2D tangentialForce = new Vector2D( 0,0 );
        
        socialRelationForce = socialForces( pointsOfInterest );
        normalForce = normalForces( pointsOfInterest );
        tangentialForce = tangentialForces( pointsOfInterest );
		
		Bag grpIndivs = getGroupedIndividuals();
		Vector2D groupingForce = new Vector2D( 0.0, 0.0 );
		
		for( int i = 0; i < grpIndivs.size(); i++ )
		{
			Vector2D tempGroupingForce = groupingForce( (Individual) grpIndivs.get(i) );
			groupingForce = new Vector2D( groupingForce.x+tempGroupingForce.x, groupingForce.y+tempGroupingForce.y );
		}

		Vector2D randomForce = random();
		Vector2D selfPropellForce = selfPropellingForce();

		
		// sum the forces to estimate the acceleration and assuming dt = 1 this is also the velocites
		double summForcesX = socialRelationForce.x +
	        normalForce.x +
	        tangentialForce.x +
	        groupingForce.x +
	        randomForce.x;
	    
		double summForcesY = socialRelationForce.y +
	        normalForce.y +
	        tangentialForce.y +
	        groupingForce.y + 
	        randomForce.y;
	    
		ind.setForces( summForcesX, summForcesY );
		
    	double dx = summForcesX;
        double dy = summForcesY;
        this.sumOfForces = new Vector2D( this.sumOfForces.x+selfPropellForce.x, this.sumOfForces.y+selfPropellForce.y );
        Vector2D wallFollowForce = followWall( new Vector2D( sumOfForces.x, sumOfForces.y ) );
        //this.sumOfForces = new Vector2D( this.sumOfForces.x+wallFollowForce.x, this.sumOfForces.y+wallFollowForce.y );

		ind.setForceValues( "" +socialRelationForce.x+","+socialRelationForce.y
						   +","+normalForce.x+","+normalForce.y
						   +","+tangentialForce.x+","+tangentialForce.y
						   +","+groupingForce.x+","+groupingForce.y
						   +","+selfPropellForce.x+","+selfPropellForce.y
						   +","+randomForce.x+","+randomForce.y );
	}


	/**
	 * This function calculates all of the forces acting on the Individual.
	 * In general this function looks at the strength of the forces previously 
	 * calculated and calculates the timestep used for the simulation and the
	 * velocities for each individual.
	 *
	 * @param simState The object containing the state of the simulation.
	 **/
	public void calculateMovementParametersII( CrowdSimulation simState )
	{
		// time step adjustment for velocity change			
		((HMFVRayTracerModel)ind.getActionController()).setTimeStep( ((HMFVRayTracerModel)ind.getActionController()).eulTStep( getTimeStep(), ind.getForces().magnitude() ) );

	  	// new velocity
	  	if(  (ind.isInjured()) &&((injurySwitch==1)||(injurySwitch==3)))
	    {
	    	ind.setVelocity( new Vector2D( 0, 0) );
	  	}
	  	else
	  	{
	  		Vector2D oldVelocity = ind.getVelocity();
	  		oldVelocity.x = oldVelocity.x + sumOfForces.x*getTimeStep();
	  		oldVelocity.y = oldVelocity.y + sumOfForces.y*getTimeStep();
	  		ind.setNextVelocity( oldVelocity );
	  	}
	  	
		// checking new velocity
		double nextVelocityMag = ind.getNextVelocity().magnitude();
		
	 	if( nextVelocityMag > Vmax ) 
		{
			ind.setNextVelocity( 
				ind.getNextVelocity().x/nextVelocityMag*Vmax, 
				ind.getNextVelocity().y/nextVelocityMag*Vmax);
		}
	}

	
	/**
	 * Moves the individuals using the passed in time step.
	 *
	 * @param currentTimeStep The timestep that has been set for use for this step.
	 **/
	public void move( double currentTimeStep )
	{
	  	double Xprev = ind.getLocation().x;
	  	double Yprev = ind.getLocation().y;
	
		double X = Xprev + getSpeed()*ind.getVelocity().x * currentTimeStep;
		double Y = Yprev + getSpeed()*ind.getVelocity().y * currentTimeStep;
		
		ind.setMomentum( new Vector2D( (X-Xprev)/(Math.pow(currentTimeStep, 3 )), (Y-Yprev)/(Math.pow(currentTimeStep, 3 )) ) );
		ind.setLocation( X, Y );

		ind.setVelocity( ind.getNextVelocity() );
	}

	
	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
		super.setParameters( parameters );
		
		if( parameters.containsKey( "a" ) )
		{
			setA( Parameters.convertToDouble( 
				parameters.get( "a" ), 
				getA(),
			   "a must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "b" ) )
		{
			setB( Parameters.convertToDouble( 
				parameters.get( "b" ), 
				getB(),
			   "b must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "cYoung" ) )
		{
			setCYoung( Parameters.convertToDouble( 
				parameters.get( "cYoung" ), 
				getCYoung(),
			   "cYoung must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "gamma" ) )
		{
			setGamma( Parameters.convertToDouble( 
				parameters.get( "gamma" ), 
				getGamma(),
			   "gamma must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "kappa" ) )
		{
			setKappa( Parameters.convertToDouble( 
				parameters.get( "kappa" ), 
				getKappa(),
			   "kappa must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "frictionSwitch" ) )
		{
			setFrictionSwitch( Parameters.convertToInt( 
				parameters.get( "frictionSwitch" ), 
				getFrictionSwitch(),
			   "frictionSwitch must be an Integer or a string representing an Integer." ) );
		}
		if( parameters.containsKey( "R" ) )
		{
			setR( Parameters.convertToDouble( 
				parameters.get( "R" ), 
				getR(),
			   "r must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "tau" ) )
		{
			setTau( Parameters.convertToDouble( 
				parameters.get( "tau" ), 
				getTau(),
			   "tau must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "gaTh" ) )
		{
			setGaTh( Parameters.convertToDouble( 
				parameters.get( "gaTh" ), 
				getGaTh(),
			   "gaTh must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "gaMe" ) )
		{
			setGaMe( Parameters.convertToDouble( 
				parameters.get( "gaMe" ), 
				getGaMe(),
			   "gaMe must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "gaCM" ) )
		{
			setGaCM( Parameters.convertToDouble( 
				parameters.get( "gaCM" ), 
				getGaCM(),
			   "gaCM must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "RAND_MAX" ) )
		{
			setRAND_MAX( Parameters.convertToDouble( 
				parameters.get( "RAND_MAX" ), 
				getRAND_MAX(),
			   "RAND_MAX must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "c" ) )
		{
			setC( Parameters.convertToDouble( 
				parameters.get( "c" ), 
				getC(),
			   "c must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "vMax" ) )
		{
			setVMax( Parameters.convertToDouble( 
				parameters.get( "vMax" ), 
				getVMax(),
			   "Vmax must be a Double or a string representing a Double." ) );
		}
		if( parameters.containsKey( "injurySwitch" ) )
		{
			setInjurySwitch( Parameters.convertToInt( 
				parameters.get( "injurySwitch" ), 
				getInjurySwitch(),
			   "injurySwitch must be an Integer or a string representing an Integer." ) );
		}

	}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the value for attribute a.
	 *
	 * @return The value of attribute a.
	 **/
    public double getA()
	{
		return a;
	}

	/**
	 * Sets the value for attribute a.
	 *
	 * @param val The value of attribute a.
	 **/
	public void setA( double val )
	{
		a = val;
	}

	/**
	 * Gets the value for attribute b.
	 *
	 * @return The value of attribute b.
	 **/
    public double getB()
	{
		return b;
	}
	
	/**
	 * Sets the value for attribute b.
	 *
	 * @param val The value of attribute b.
	 **/
	public void setB( double val )
	{
		b = val;
	}

	/**
	 * Gets the value for attribute cYoung.
	 *
	 * @return The value of attribute cYoung.
	 **/
    public double getCYoung()
	{
		return cYoung;
	}
	
	/**
	 * Sets the value for attribute cYoung.
	 *
	 * @param val The value of attribute cYoung.
	 **/
	public void setCYoung( double val )
	{
		cYoung = val;
	}

	/**
	 * Gets the value for attribute c.
	 *
	 * @return The value of attribute c.
	 **/
    public double getC()
	{
		return c;
	}
	
	/**
	 * Sets the value for attribute c.
	 *
	 * @param val The value of attribute c.
	 **/
	public void setC( double val )
	{
		c = val;
	}

	/**
	 * Gets the value for attribute gamma.
	 *
	 * @return The value of attribute gamma.
	 **/
    public double getGamma()
	{
		return gamma;
	}
	
	/**
	 * Sets the value for attribute gamma.
	 *
	 * @param val The value of attribute gamma.
	 **/
	public void setGamma( double val )
	{
		gamma = val;
	}

	/**
	 * Gets the value for attribute kappa.
	 *
	 * @return The value of attribute kappa.
	 **/
    public double getKappa()
	{
		return kappa;
	}
	
	/**
	 * Sets the value for attribute kappa.
	 *
	 * @param val The value of attribute kappa.
	 **/
	public void setKappa( double val )
	{
		kappa = val;
	}

	/**
	 * Sets the value for attribute frictionSwitch.
	 * This attribute decides which type of friction to calculate.
	 *
	 * @param val The value of attribute frictionSwitch.
	 **/
	public void setFrictionSwitch( int val )
	{
		frictionSwitch = val;
	}
	
	/**
	 * Gets the value for attribute frictionSwitch.
	 * This attribute decides which type of friction to calculate.
	 *
	 * @return int The value of attribute frictionSwitch.
	 **/
	public int getFrictionSwitch( )
	{
		return frictionSwitch;
	}

	/**
	 * Gets the value for attribute R.
	 * This is the interaction radius.
	 *
	 * @return int The value of attribute R.
	 **/
    public double getR()
	{
		return ind.getRadiusOfConcern();
	}
	
	/**
	 * Sets the value for attribute R.
	 * This is the interaction radius.
	 *
	 * @param val The value of attribute R.
	 **/
	public void setR( double val )
	{
		ind.setRadiusOfConcern( val );
	}

	/**
	 * Gets the value for attribute tau.
	 *
	 * @return double The value of attribute tau.
	 **/
    public double getTau()
	{
		return tau;
	}
	
	/**
	 * Sets the value for attribute tau.
	 *
	 * @param val The value of attribute tau.
	 **/
	public void setTau( double val )
	{
		tau = val;
	}

	/**
	 * Gets the value for attribute gaTh.
	 *
	 * @return double The value of attribute gaTh.
	 **/
    public double getGaTh()
	{
		return gaTh;
	}
	
	/**
	 * Sets the value for attribute gaTh.
	 *
	 * @param val The value of attribute gaTh.
	 **/
	public void setGaTh( double val )
	{
		gaTh = val;
	}

	/**
	 * Gets the value for attribute gaMe.
	 *
	 * @return double The value of attribute gaMe.
	 **/
    public double getGaMe()
	{
		return gaMe;
	}
	
	/**
	 * Sets the value for attribute gaMe.
	 *
	 * @param val The value of attribute gaMe.
	 **/
	public void setGaMe( double val )
	{
		gaMe = val;
	}

	/**
	 * Gets the value for attribute gaCM.
	 *
	 * @return double The value of attribute gaCM.
	 **/
    public double getGaCM()
	{
		return gaCM;
	}
	
	/**
	 * Gets the value for attribute gaCM.
	 *
	 * @param val The value of attribute gaCM.
	 **/
	public void setGaCM( double val )
	{
		gaCM = val;
	}

	/**
	 * Gets the value for attribute RAND_MAX.
	 *
	 * @return double The value of attribute RAND_MAX.
	 **/
    public double getRAND_MAX()
	{
		return RAND_MAX;
	}
	
	/**
	 * Sets the value for attribute RAND_MAX.
	 *
	 * @param val The value of attribute RAND_MAX.
	 **/
	public void setRAND_MAX( double val )
	{
		RAND_MAX = val;
	}

	/**
	 * Gets the value for attribute vMax.
	 *
	 * @return double The value of attribute vMax.
	 **/
    public double getVMax()
	{
		return Vmax;
	}
	
	/**
	 * Sets the value for attribute vMax.
	 *
	 * @param val The value of attribute vMax.
	 **/
	public void setVMax( double val )
	{
		Vmax = val;
	}

	/**
	 * Gets the value for attribute injurySwitch.
	 * Decides if the individuals cincure injuries.
	 *
	 * @return int The value of attribute injurySwitch.
	 **/
    public int getInjurySwitch()
	{
		return injurySwitch;
	}
	
	/**
	 * Sets the value for attribute injurySwitch.
	 * Decides if the individuals cincure injuries.
	 *
	 * @param val The value of attribute injurySwitch.
	 **/
	public void setInjurySwitch( int val )
	{
		injurySwitch = val;
	}
	
}
