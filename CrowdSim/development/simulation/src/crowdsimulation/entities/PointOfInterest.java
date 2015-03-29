/*
 * $RCSfile: PointOfInterest.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.entities;

import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import math.*;
import sim.util.*;

/**
 * This is a representation of a point which an individual is interested in.
 * This is usually used for interaction calculations and contains all the needed 
 * data to exectue the calculations. This reduces the number of redundant calls
 * to find pertinent data about interactions with an obstacle. The data is tied to 
 * the indiviudal which is interested and the entity which contains the point.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class PointOfInterest
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The location of the point (X,Y) location in the world. **/
	private Vector2D location;
	/** The distance to the point as an (X,Y) vector. **/
	private Vector2D distance;
	/** The scalr distance to the point. 
	 *  This is the magnitude of the distance vector **/
	private double radialDistance;
	/** The normal to the entity at the point of interest. **/
	private Vector2D normal;
	/** The tangent to the entity at the point of interest. **/
	private Vector2D tangent;
	/** The entity which the poin occurs on. **/
	private Entity associatedEntity;
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
   
   /**
    * This construct a point of interest. It sets up all necesary information, and
    * should do so in the most efficient way.
    *
    * @param ind The individual that is interested in the point.
    * @param point The point that the individual is interested in.
    * @param entity The entity which the point lies on.
    * @param dist The distance as a vector between the indiviudal and the point.
    * @param r The scalar distance to the point from the individual.
    **/
	public PointOfInterest( Individual ind, Vector2D point, Entity entity, Vector2D dist, double r )
	{
		location = point;
		associatedEntity = entity;

		distance = dist;
		radialDistance = r;
		
		normal = entity.getNormalAt( point );
		tangent = entity.getTangentAt( point, ind.getVelocity() );
	}
	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/** 
	 * This gets the velocity of the point.
	 * This delegates getting the velocity to the 
	 * entity which contains the point.
	 *
	 * @see Entity#getVelocity()
	 **/
	public Vector2D getVelocity()
	{
		return associatedEntity.getVelocity();
	}

	/**
	 * Gets the location of the point of interest.
	 *
	 * @return The location as an (X,Y) location in the world.
	 **/
	public Vector2D getLocation()
	{
		return location;
	}
	
	/**
	 * Sets the location of the point of interest.
	 *
	 * @param val The location as an (X,Y) location in the world.
	 **/
	public void setLocation( Vector2D val )
	{
		location = val;
	}
	
	/**
	 * Gets the distance of the point of interest.
	 *
	 * @return The distance as an (X,Y) location in the world.
	 **/
	public Vector2D getDistance()
	{
		return distance;
	}
	
	/**
	 * Sets the distance of the point of interest.
	 *
	 * @param val The distance as an (X,Y) location in the world.
	 **/
	public void setDistance( Vector2D val )
	{
		distance = val;
	}
	
	/**
	 * Gets the normal of the entity at the point of interest.
	 *
	 * @return The normal of the entity at the point of interest.
	 **/
	public Vector2D getNormal()
	{
		return normal;
	}

	/**
	 * Sets the normal of the entity at the point of interest.
	 *
	 * @param val The normal of the entity at the point of interest.
	 **/
	public void setNormal( Vector2D val )
	{
		normal = val;
	}
	
	/**
	 * Gets the tangent of the entity at the point of interest.
	 *
	 * @return The tangent of the entity at the point of interest.
	 **/
	public Vector2D getTangent()
	{
		return tangent;
	}
	
	/**
	 * Sets the tangent of the entity at the point of interest.
	 *
	 * @param val The tangent of the entity at the point of interest.
	 **/
	public void setTangent( Vector2D val )
	{
		tangent = val;
	}
	
	/**
	 * Gets the distance as a scalar to the point of interest.
	 *
	 * @return The distance as a scalar to the point of interest.
	 **/
	public double getRadialDistance()
	{
		return radialDistance;
	}
	
	/**
	 * Sets the distance as a scalar to the point of interest.
	 *
	 * @param val The distance as a scalar to the point of interest.
	 **/
	public void setRadialDistance( double val )
	{
		radialDistance = val;
	}
	
	/**
	 * Gets the entity which contians the point of interest.
	 *
	 * @return The entity which contians the point of interest.
	 **/
	public Entity getAssociatedEntity()
	{
		return associatedEntity;
	}
	
	/**
	 * Sets the entity which contians the point of interest.
	 *
	 * @param val The entity which contians the point of interest.
	 **/
	public void setAssociatedEntity( Entity val )
	{
		associatedEntity = val;
	}
}
