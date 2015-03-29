/*
 * $RCSfile: CircularWaypoint.java,v $ $Date: 2009/06/11 20:36:56 $
 */
package crowdsimulation.entities;

import crowdsimulation.entities.individual.Individual;
import math.Vector2D;

import java.util.*;
import sim.field.continuous.*;

/**
 * Represents a waypoint to be used to construct paths.
 * A waypoint consists of a Vector2D and an ID.
 * @see crowdsimulation.entities.Path
 *
 *
 * @author $Author: ganil $
 * @version $Revision: 1.1 $
 * $State: Exp $
 * $Date: 2009/06/11 20:36:56 $
 **/
public class CircularWaypoint extends crowdsimulation.entities.obstacle.CircularObstacle implements crowdsimulation.entities.Waypoint
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
   
   	/** The id of the waypoint. **/
	private int ID;
	/** The id to be used at the creation of the next waypoint. **/
	
    private static int currentID=0;
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////	

	/**
	 * Constructs a Waypoint on a given terrain, with a givin location and a radius of interest.
	 *
	 * @param terrain The terrain which this waypoint is asscoiated with.
	 *   This allows the user to turn on and off seeing the waypoints on the graphical display.
	 * @param centerX The x position of the center of the waypoint.
	 * @param centerY The y position of the center of the waypoint.
	 * @param radius The radius of influence of the waypoint. This is used to tell when to 
	 *   turn off or disable a waypoint.
	 **/
	public CircularWaypoint( Continuous2D terrain, double centerX, double centerY, double radius )
	{
		//super(terrain, centerX, centerY, radius);
		super(-1, new HashMap());
		setTerrain( terrain );
		this.setCenter(new Vector2D(centerX, centerY));
		this.setRadius( radius );
		this.ID = currentID;
		currentID++;
	}

	/**
	 * This creates a clone of the current waypoint.
	 * The new waypoint will have all of the same information as the original waypoint, including ID.
	 *
	 * @return The OBJECT whihc is a clone of this object. The object needs to be cast to a waypoint.
	 **/
	public Object clone()
	{
		CircularWaypoint clonePoint = new CircularWaypoint( getTerrain(), getCenter().x, getCenter().y, getRadius() );
		currentID--;
		
		clonePoint.setID( ID );
		
		return clonePoint;
	}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Get the waypoint's id.
	 *
	 * @return The waypoint's id.
	 **/
	public int getID()
	{
		return this.ID;	
	}

	/**
	 * Set the waypoint's id.
	 *
	 * @param val The waypoint's id.
	 **/
	public void setID( int val )
	{
		ID = val;
	}

    public Vector2D getTargetPoint(Individual ind) {
        return this.getCenter();
    }

    @Override
    public Vector2D getCenter() {
        return super.getCenter();
    }

}
