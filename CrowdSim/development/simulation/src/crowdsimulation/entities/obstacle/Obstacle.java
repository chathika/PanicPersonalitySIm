/*
 * $RCSfile: Obstacle.java,v $ $Date: 2010/12/31 20:18:22 $
 */
package crowdsimulation.entities.obstacle;

import ec.util.*;
import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.entities.*;
import crowdsimulation.util.*;
import java.awt.geom.*;
import java.awt.*;
import java.lang.reflect.*;
import java.util.*;
import math.*;
import sim.engine.*;
import sim.field.continuous.*;
import sim.portrayal.*;
import sim.util.*;

/**
 * Abstract base class of an obstacle. This gives the common attributes and functions
 * between all obstacles.
 *
 * @see crowdsimulation.entities.obstacle.CircularObstacle
 * @see crowdsimulation.entities.obstacle.RectangularObstacle
 *
 * @author $Author: dkaup $
 * @version $Revision: 1.7 $
 * $State: Exp $
 * $Date: 2010/12/31 20:18:22 $
 **/
public abstract class Obstacle extends Entity
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
	
	/** Maintains unique ids for all obstacles. **/
	static int master_id=0;
	/** The id of the obstacle **/
	private int id;						


  // Parameters Affecting Other Entities
    /** Force of attraction/repulsion for the obstacle. Positive force values repel, negative values attract. **/
    private double force;
    /** Is the obstacle able to be passed over/through. **/
    private boolean penetrable = false;
    /** Obstacles of interest act like waypoints and should occur over much larger distances. **/
    private boolean ofInterest = false;	
    
  // display properties
   /** Tells if hte obstacle should display its center **/
  	private boolean displayCenter = false; 
  	
  	/** Map of the default params to be used to construct an obstacle. **/
  	Map params = new HashMap();

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * The method used to build a particular an obstacle. 
	 * This reads in the type of obstacle to be created from the attributes then
	 * using reflection constructs the correct obstacle type.
	 * This is an implementation of the FACTORY METHOD pattern.
	 * 
	 * @param entityIDVal The entity id of the obstacle being created.
	 * @param attributes The attributes used for constructing the obstacle. This\
	 *  should contain the type of obstacle to create also.
	 * @return The obstacle with the given entity ID and attributes.
	 **/
	public static Obstacle build( int entityIDVal, Map attributes )
	{
		Obstacle obstacle = null;
		String className = (String)attributes.get( "type" );
		
		Class[] paramSet = { int.class, Map.class };
		Object[] objectEmptySet = { new Integer( entityIDVal ), attributes };
		
		try
		{
			Class obstacleClass = Class.forName( "crowdsimulation.entities.obstacle." + className + "Obstacle" );
			Constructor obstacleCreator = obstacleClass.getConstructor( paramSet );
			obstacle = (Obstacle)obstacleCreator.newInstance( objectEmptySet );
		}
		catch( Exception e )
		{
			System.out.println( "Make sure that the obstacle crowdsimulation.entities.obstacle." + className + 
				"Obstacle exists, and has a constructor with the signature " + className + 
				"Obstacle( int id, Map attributes )" );
			System.exit(-1);
		}

		return obstacle;
	}

	
	/**
	 * Constructs an obstacle given the terrain which it exists in and the x, y coordinates of its center.
	 *
	 * @param terrain_val The terrain which the obstacle is to be added to.
	 * @param x The x component of the center of the object.
	 * @param y The y component of the center of the object.
	 **/
	Obstacle( Continuous2D terrain_val, int x, int y )
    {
    	this(terrain_val, new Vector2D( x, y ) );
	}

	
	/**
	 * Constructs an obstacle given the terrain which it exists in and the center of the obstacle.
	 *
	 * @param terrain_val The terrain which the obstacle is to be added to.
	 * @param center The center of the obstacle as a 2Dimensional vector.
	 **/
	Obstacle( Continuous2D terrain_val, Vector2D center )
    {
    	this(terrain_val, center, center);
	}


	/**
	 * Constructs an obstacle given the terrain which it exists in and the center of the obstacle.
	 *
	 * @param terrain_val The terrain which the obstacle is to be added to.
	 * @param center The center of the obstacle as a 2Dimensional vector.
	 * @param params The parameters for this obstacle.
	 **/
	Obstacle( Continuous2D terrain_val, Vector2D center, Map params )
    {
    	this(terrain_val, center, center);
    	setParameters( params );
	}

	
	/**
	 * Constructs an obstacle given the terrain which it exists in, the center of the obstacle, and an anchor point for the location.
	 *
	 * @param terrain_val The terrain which the obstacle is to be added to.
	 * @param center The center of the obstacle as a 2Dimensional vector.
	 * @param location An anchor point for the obstacle as a 2Dimensional vector .
	 **/
	Obstacle( Continuous2D terrain_val, Vector2D center, Vector2D location )
	{
		this();
		setTerrain( terrain_val );
		setCenter( center );
		setLocation( location );
	}

	
	/**
	 * Common construction for all constructors. 
	 * This sets the basic information needed by all other constructors. 
	 * This should never be called by itself.
	 **/
	Obstacle( )
	{
		setId( master_id++ );
		setTerrain( CrowdSimulation.getInstance().getTerrain() );
	}

 
 	/**
 	 * Method to draw the obstacle on the GUI. This method needs to be overridden in any class subclassing from this class.
 	 *
 	 * @param object This may be null;
 	 * @param graphics The graphic object which the obstacle should be drawn onto.
 	 * @param info The information about the graphics object to be drawn on.
 	 **/
	public abstract void draw(Object object, Graphics2D graphics, DrawInfo2D info );


	/**
	 * Returns the tangent to the individual at a given point.
	 *
	 * @param point The point at which to find the tangent line.
	 * @param orientation The direction of the incident ray. This is used to pick the direction of the tangent vector.
	 * @return Vector2D The tangent to the obstacle.
	 **/
    public Vector2D getTangentAt( Vector2D point, Vector2D orientation )
    {
    	Vector2D normal = getNormalAt( point );
    	normal.normalize();
    	Vector2D tangent = new Vector2D( -1.0*normal.y, normal.x);


		if( normal.getAngleBetween( tangent ) == 0 || normal.getAngleBetween( tangent ) == Math.PI
			|| normal.getAngleBetween( tangent ) == -1.0*Math.PI )
		{
			tangent = new Vector2D( -1.0*tangent.y, tangent.x );
		}

		Vector2D tangent2 = new Vector2D( -1.0*tangent.x, -1.0*tangent.y );

		if( Math.abs( orientation.getAngleBetween( tangent ) ) < Math.abs( orientation.getAngleBetween( tangent2 ) ) )
		{
			tangent = tangent2;
		}

		return tangent;
    }


    /**
     * Gives the capability to copy an obstacle.
     *
     * @return Clones the obstacle.
     **/
    public abstract Object clone();

	
	/**
	 * Returns true if the point is inside the obstacle and false if the point
     * is on the boundary or outside the obstacle.
	 *
	 *@param point The point being tested to see if it is inside the obstacle.
	 *@return  A boolean is returned indicating if the given point is inside the obstacle.
	 **/
	public abstract boolean isInside( Vector2D point );


	/**
	 * Sets all the parameters from a key value pairs collection.
	 * This stores the params as a local cache in case they are needed later in the simulation.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
		params = parameters;
        //AJ Added to allow setting of variable from configuration file
		if( parameters.containsKey( "ofInterest" ) )
		{
			setOfInterest( Boolean.valueOf(parameters.get( "ofInterest" ).toString()) );
        }

	}
	
	
	/**
	 * Makes it so that whenever the object is written out, it just write the individuals id.
	 *
	 * @return The id of the individual as a string.
	 **/	
	public String toString()
	{
		return ""+id;
	}
	
	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the ActionController which will move the obstacle.
	 *
	 * @param val The ActionController object which represents the way which the obstacle moves.
	 * @see crowdsimulation.actioncontroller.ActionController
	 **/	
	public void setActionController( ActionController val )
	{
		super.setActionController( val );
		val.addObstacle( this );
	}
	
	/**
	 * Gets the ID of the individual
	 *
	 * @return The ID of the individual.
	 **/
	public int getId()
	{
		return id;
	}
	
	/**
	 * Sets the ID of the individual
	 *
	 * @param val The new ID for the individual.
	 **/
	public void setId( int val )
	{
		id = val;
	}
	
	/**
	 * Gets the attraction/repulsion force of the obstacle.
	 *
	 * @return The magnitude of the attraction or repulsion of the obstacle.
	 **/	
    public double getForce()
    {
        return force;
    }

	/**
	 * Sets the attraction/repulsion force of the obstacle.
	 *
	 * @param forceVal The magnitude of the attraction or repulsion of the obstacle.
	 **/
    public void setForce( double forceVal)
    {
        force = forceVal;
    }

	/**
	 * Tells if the obstacle is of interest.
	 *
	 * @return boolean represnting if the obstacle is of interest or not.
	 **/
    public boolean isOfInterest()
    {
        return ofInterest;
    }

	/**
	 * Sets whether or not the obstacle is a point of interest.
	 *
	 * @param interestVal sets this obstacle to be in an area of interest or not.
	 **/
    public void setOfInterest( boolean interestVal)
    {
        ofInterest = interestVal;
    }
 
	/**
	 * Tells if the obstacle is penetrable.
	 *
	 * @return boolean to tell if the obstacle is penetrabel.
	 **/
	public boolean isPenetrable() 
	{ 
		return penetrable; 
	}

	/**
	 * Allows an obstacle to be penetrable.
	 *
	 * @param val Represents if the obstacle is penetrable.
	 **/		
    public void setPenetrable(boolean val) 
    { 
      	penetrable = val; 
    }
    
	/**
	 * Tells if the obstacle should display its center.
	 *
	 * @return boolean to tell if the obstacle can display its center.
	 **/
	public boolean isDisplayCenter() 
	{ 
		return displayCenter; 
	}

	/**
	 * Allows an obstacle to display its center.
	 *
	 * @param val Represents if the obstacle should display its center.
	 **/		
    public void setDisplayCenter(boolean val) 
    { 
      	displayCenter = val; 
    }
    
    
	public Map getParameters()
	{
		return params;
	}
}
