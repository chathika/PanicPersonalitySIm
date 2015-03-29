/*
 * $RCSfile: Entity.java,v $ $Date: 2010/12/31 20:23:23 $
 */
package crowdsimulation.entities;

import ec.util.*;
import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;
import math.*;
import sim.engine.*;
import sim.field.continuous.*;
import sim.portrayal.*;
import sim.util.*;

/**
 * This class represents an individual in the simulation.
 * An instance of this class is used to represent each person which is 
 * to be simulated over the execution of the given simulation. The 
 * action model determines the way the individual moves over time.
 *
 * @see crowdsimulation.actioncontroller.ActionController
 * @author $Author: dkaup $
 * @version $Revision: 1.8 $
 * $State: Exp $
 * $Date: 2010/12/31 20:23:23 $
 **/
public abstract class Entity extends SimplePortrayal2D
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
	
	/** A vector pointing in the positive x direction. **/
	public static Vector2D xPlusVector = new Vector2D(1.0,0.0);
	/** A vector pointing in the negative x direction. **/
	public static Vector2D xMinusVector = new Vector2D(-1,0);
	/** A vector pointing in the positive y direction. **/
	public static Vector2D yPlusVector = new Vector2D(0,1);
	/** A vector pointing in the negative y direction. **/
	public static Vector2D yMinusVector = new Vector2D(0,-1);
	/** The speed at which the individual moved. **/
	protected Vector2D velocity = new Vector2D(0,0);
	
	/** The amount of error allowed when comparing two numbers. **/
    double ERROR = 0.00000001;

	/** The EntityID of the obstacle **/
	int entityID;

  // Parameters Affecting Location and Movement
  	/** Center of the entity. **/
    Vector2D center;
    /** Momentum of the obstacle, when they finally move. Defaults to (0,0). **/					
    Vector2D momentum = new Vector2D(0,0);

  // Parameters Tieing Obstacle to Other Simulation Components
    /** The map that the obstacle belongs too. This map should mimic the world map just with a different discretization. **/
    Continuous2D terrain;
    /** The method which actually moves the obstacle. Currently all obstacles are static (non moving). **/
    ActionController actionController;
    
    /** Can the individual still move. **/
	boolean dead = false;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns a point of intersection between a given ray and this entity.
	 * 
	 * @param origin The origin of the ray to test the intersection with.
	 * @param vector The direction of the ray to test the intersection with.
	 * @return The position of the point of intersection. If no intersection or
     * if the origin is inside the entity, it returns (-1,-1).
	 **/
	public abstract Vector2D getClosestPoint(Vector2D origin, Vector2D vector);

	/**
	 * Returns the normal vector at a given point on the obstacle.
     *
	 * Returns the normal vector at a given point on the boundary of an entity.
     * If the point is on a corner, we average the two possible norms.
     *
	 * @param point The point on the boundary of the entity from which the normal is to be extended.
	 * @return The normal to the surface of the entity at the point. 
	 **/
	public abstract Vector2D getNormalAt( Vector2D point );
	
	/**
	 * Returns the tangent to the entity at a given point.
	 * 
	 * @param point The point which the tangent line must pass through.
	 * @param orientation The direction (+ - 90 degrees) in which the tangent vector is to align.
	 * @return Vector2D The tangent to the entity.
	 **/
    public abstract Vector2D getTangentAt( Vector2D point, Vector2D orientation );

	/**
	 * A comparison between two vector to see if they are similar.  Each
     * component must be within ERROR of each other to pass the test.
	 *
	 * @param a The first vector for the comparison.
	 * @param b The second vector for the comparison.
	 * @return The boolean representing if the two vectors are close enough to be considered the same.
	 **/
	public boolean approximatelyEqual( Vector2D a, Vector2D b )
	{
		return ( Math.abs(a.x - b.x) < ERROR && Math.abs(a.y - b.y) < ERROR );
	}

	/** 
	 * A comparison between two doubles to see if they are similar.
	 *
	 * @param a The first double for the comparison.
	 * @param b The second double for the comparison.
	 * @return The boolean representing if the two doubles are close enough to be considered the same.
	 **/
	public boolean approximatelyEqual( double a, double b )
	{
		return Math.abs(a - b) < ERROR;
	}
	


////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the EntityID of the entity.
	 *
	 * @return int The EntityID of the entity.
	 **/
	public int getEntityID()
	{
		return entityID;
	}
	
	/**
	 * Sets the ID of the entity.
	 *
	 * @param val The new ID for the entity.
	 **/
	public void setEntityID( int val )
	{
		entityID = val;
	}

	/**
	 * Gets the center of the entity.
	 *
	 * @return The 2Dimensional vector representing the center of the entity.
	 **/
	public Vector2D getCenter()
	{
		return center;
	}
	
	/**
	 * Sets the center of the entity.
	 *
	 * @param val The 2Dimensional vector representing the center of the entity.
	 **/
	public void setCenter( Vector2D val )
	{
		center = val;
	}

	/**
	 * Gets the terrain which the entity is simulated on.
	 *
	 * @return A Continuous2D object which represents the terrain the entity is on.
	 **/	
	public Continuous2D getTerrain()
	{
		return this.terrain;
	}
	
	/**
	 * Sets the terrain which the entity is simulated on.
	 *
	 * @param terrainVal The Continuous2D object which represents the world the entity is on.
	 **/
	public void setTerrain( Continuous2D terrainVal )
	{
		this.terrain = terrainVal;
	}

	/**
	 * Gets the ActionController which will move the entity.
	 *
	 * @return ActionController object which represents the way which the entity moves.
	 **/
	public ActionController getActionController()
	{
		return this.actionController;
	}
	
	/**
	 * Sets the ActionController which will move the entity.
	 *
	 * @param val The ActionController object which represents the way which the entity moves.
	 * @see crowdsimulation.actioncontroller.ActionController
	 **/
	public void setActionController( ActionController val )
	{
		this.actionController = val;
	}
		
	/**
	 * Tells if the entity is dead. Currently obstacles do not move and therefore cannot be killed.
	 *
	 * @return boolean to tell if the entity is dead.
	 **/
	public boolean isDead() 
	{ 
		return dead; 
	}

	/**
	 * Allows an entity to be killed or healed.
	 *
	 * @param val Represents either killing or reactivating an entity.
	 **/		
    public void setDead(boolean val) 
    { 
      	dead = val; 
    }
    
	/**
	 * Gets the X component of the current Velocity of the entity.
	 *
	 * @return The X component of the Velocity.
	 **/
	public double getVelocityX()
	{
		return this.velocity.x;
	}
	
	/**
	 * Sets the X component of the current Velocity of the entity.
	 *
	 * @param xVal The X component of the Velocity.
	 **/
	public void setVelocityX( double xVal )
	{
		setVelocity( xVal, this.velocity.y );
	}
	
	/**
	 * Sets the X and Y components of the current Velocity of the entity.
	 *
	 * @param xVal The X component of the Velocity.
	 * @param yVal The Y component of the Velocity.
	 **/
	public void setVelocity( double xVal, double yVal )
	{
		setVelocity( xVal, yVal );
	}
	
	/**
	 * Sets the Velocity of the entity.
	 *
	 * @param velocityVal The Velocity.
	 **/
	public void setVelocity( Vector2D velocityVal )
	{
		this.velocity = velocityVal;
	}

	
	/**
	 * Gets the Y component of the current Velocity of the entity.
	 *
	 * @return The Y component of the Velocity.
	 **/
	public double getVelocityY()
	{
		return this.velocity.y;
	}
	
	/**
	 * Sets the Y component of the current Velocity of the entity.
	 *
	 * @param yVal The Y component of the Velocity.
	 **/
	public void setVelocityY( double yVal )
	{
		setVelocity( this.velocity.x, yVal );
	}
	
	/**
	 * Gets the current Velocity of the entity.
	 *
	 * @return The Velocity of the entity as a 2 Dimensional Vector.
	 **/
	public Vector2D getVelocity()
	{
		return this.velocity;
	}
	
	/**
	 * Gets the momentum of the entity.
	 *
	 * @return The momentum of the entity as a 2 Dimensional Vector.
	 **/	
    public Vector2D getMomentum()
    {
        return momentum;
    }
    
    /**
	 * Sets the momentum of the entity.
	 *
	 * @param val The momentum of the entity as a 2 Dimensional Vector.
	 **/	
    public void setMomentum( Vector2D val )
    {
    	this.momentum = val;
    }

	/**
	 * Gets the X component of the current location of the entity.
	 *
	 * @return The X component of the location.
	 **/
	public double getX()
	{
		return getLocation().x;
	}
	
	/**
	 * Sets the X component of the current location of the entity.
	 *
	 * @param xVal The X component of the location.
	 **/
	public void setX( double xVal )
	{
		setLocation( xVal, getY() );
	}
	
	/**
	 * Gets the Y component of the current location of the entity.
	 *
	 * @return The Y component of the location.
	 **/
	public double getY()
	{
		return getLocation().y;
	}
	
	/**
	 * Sets the Y component of the current location of the entity.
	 *
	 * @param yVal The y component of the location.
	 **/
	public void setY( double yVal )
	{
		setLocation( getX(), yVal );
	}
	
	/**
	 * Gets the location of the entity.
	 *
	 * @return The 2Dimensional vector repersenting the location.
	 **/
	public Vector2D getLocation()
    {
		return getCenter();
	}
	
	/**
	 * Sets the location of the entity.
	 *
	 * @param xVal The x component of the entity.
	 * @param yVal The y component of the entity.
	 **/
	public void setLocation( double xVal, double yVal )
	{
		Vector2D newLoc = new Vector2D( xVal, yVal );
		this.setLocation( newLoc );
	}
	
	/**
	 * Sets the location of the entity.
	 *
	 * @param locationVal The 2 Dimensional vector representing the location.
	 **/
	public void setLocation( Vector2D locationVal )
	{
		getTerrain().setObjectLocation( this, locationVal.getDouble2D() );
		setCenter( locationVal );
	}

}
