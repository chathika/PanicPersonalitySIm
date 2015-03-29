/*
 * $RCSfile: Path.java,v $ $Date: 2010/12/22 20:17:11 $
 */
package crowdsimulation.entities;

import crowdsimulation.entities.individual.*;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import math.*;			// Used for Vector2D.

import sim.portrayal.*;	// Used for SimplePortrayal2D.

import java.awt.*;
import java.awt.geom.*; // Used for Ellipse2D.

import java.util.*;		// Used for ArrayList.

import sim.util.*; 		// Used for Bag object.


/**
 * Represents a path which contains waypoints.
 * This object can be assigned to an individual in order to give them 
 * directed travel.  As each waypoint is reached within a threshold
 * distance by an individual, that waypoint will be removed from the path
 * object assigned to that individual.
 *
 * @author $Author: dkaup $
 * @version $Revision: 1.8 $
 * $State: Exp $
 * $Date: 2010/12/22 20:17:11 $
 **/
public class Path extends Entity implements Cloneable {

////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
    /** The collection of waypoints which make up this path. **/
    public Bag waypoints = null;
    /** The unique identifier of the path. **/
    protected int id;
    /** The distance to which individuals need to be before moving to the next waypoint. **/
    protected double thresholdDistance = 0.08;
    /** This is how important a path is in comparison to other paths the individual might have. **/
    protected double weight = 1.0;
    /** Attribute needed to draw a dashed line between waypoints in the path. **/
    private static Stroke drawingStroke =
	    new BasicStroke(
	    1,
	    BasicStroke.CAP_BUTT,
	    BasicStroke.JOIN_ROUND,
	    0,
	    new float[]{9, 3},
	    0);

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////	
    /**
     * Constructs the path object.  Typically you add waypoints later to
     * the path by calling the addWaypoint() method.
     **/
    public Path() {
	waypoints = new Bag();
    }

    /**
     * This creates a clone of the current waypoint.
     * The new waypoint will have all of the same information as the original waypoint, including ID.
     *
     * @return The OBJECT whihc is a clone of this object. The object needs to be cast to a waypoint.
     **/
    public Object clone() {
	Path copyPath = null;
	try {
	    copyPath = this.getClass().getConstructor().newInstance();
	    copyPath.setID(getID());
	    copyPath.waypoints = (Bag) waypoints.clone();
        copyPath.weight = this.weight;
        copyPath.thresholdDistance = this.thresholdDistance;
	} catch (InstantiationException ex) {
	    Logger.getLogger(Path.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IllegalAccessException ex) {
	    Logger.getLogger(Path.class.getName()).log(Level.SEVERE, null, ex);
	} catch (IllegalArgumentException ex) {
	    Logger.getLogger(Path.class.getName()).log(Level.SEVERE, null, ex);
	} catch (InvocationTargetException ex) {
	    Logger.getLogger(Path.class.getName()).log(Level.SEVERE, null, ex);
	} catch (NoSuchMethodException ex) {
	    Logger.getLogger(Path.class.getName()).log(Level.SEVERE, null, ex);
	} catch (SecurityException ex) {
	    Logger.getLogger(Path.class.getName()).log(Level.SEVERE, null, ex);
	} catch (CloneNotSupportedException e) {
	    e.printStackTrace();
	    // This should never happen.
	    return null;
	}
	return copyPath;
    }

    /**
     * Method draws the path on the GUI. 
     * This method needs to be overridden in any class subclassing from this class.
     * The api of this class is defined by the MASON framework.
     *
     * @param object May be null.
     * @param graphics The graphic object which the obstacle should be drawn onto.
     * @param info The information about the graphics object to be drawn on.
     **/
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
	int size = waypoints.size();
	int[] xPoints = new int[size];
	int[] yPoints = new int[size];
	//double[] radii = new double[size];

	for (int i = 0; i < size; i++) {
	    Waypoint wp = ((Waypoint) waypoints.objs[i]);
        Vector2D center = wp.getCenter();
        xPoints[i] = (int) (info.draw.x + (center.x) * info.draw.width);
        yPoints[i] = (int) (info.draw.y + (center.y) * info.draw.height);
        
	}

	// Store the current color and stroke.
	Color oldColor = graphics.getColor();
	Stroke oldStroke = graphics.getStroke();

	// Set the graphics objects' settings and draw the path.
	graphics.setColor(Color.blue);
	graphics.setStroke(drawingStroke);
	graphics.drawPolyline(xPoints, yPoints, size);
	Stroke bs = new BasicStroke((float)5.0);

	// Draw the waypoints.
	for (int i = 0; i < size; i++) {
	    graphics.setColor(Color.darkGray);
        Waypoint wp = ((Waypoint) waypoints.objs[i]);
        if(wp instanceof CircularWaypoint) {
    	    graphics.setColor(Color.darkGray);
            Vector2D center = wp.getCenter();
            int xPoint = (int) (info.draw.x + (center.x) * info.draw.width);
            int yPoint = (int) (info.draw.y + (center.y) * info.draw.height);
            double radii = ((CircularWaypoint)wp).getRadius();
            Shape shape = new Ellipse2D.Double(
    		    xPoint - radii * info.draw.width,
    		    yPoint - radii * info.draw.height,
    		    2 * radii * info.draw.width,
    		    2 * radii * info.draw.height);
       	    graphics.fill(shape);
        } else if (wp instanceof LineWaypoint || wp instanceof KaupLineWaypoint){
            LineWaypoint lwp = (LineWaypoint)wp;
            int x =  (int) (info.draw.x + (lwp.getX()) * info.draw.width);
            int y =  (int) (info.draw.y + (lwp.getY()) * info.draw.height);
            int x2 = (int) (info.draw.x + (lwp.getX() + Math.cos(lwp.getAngle()) * lwp.getWidth()) * info.draw.height);
            int y2 = (int) (info.draw.y + (lwp.getY() + Math.sin(lwp.getAngle()) * lwp.getWidth()) * info.draw.height);
            Shape shape = new Line2D.Double(x,y,x2,y2);

        	graphics.setColor(Color.LIGHT_GRAY);
            graphics.setStroke(bs);
            graphics.draw(shape);
        }
	}

	// Reset the color and stroke used by the graphics object.
	graphics.setColor(oldColor);
	graphics.setStroke(oldStroke);
    }

    /**
     * Add a waypoint to the path.
     * 
     * @param waypoint The waypoint ot be added to the end of the current list of waypoints.
     **/
    public void addWaypoint(Waypoint waypoint) {
	waypoints.add(waypoint);
    }

    /**
     * Get the active waypoint in the path. The active waypoint is the waypoint
     *  the individual should be moving towards. If the individual is close
     *  enough to the current active waypoint, then that waypoint is deactivated
     *  and the next waypoint in the path is activated and returned as the 
     *  current active waypoint.
     *
     * @param currentIndividual The individual to find the activewaypoint for.
     * @return A Waypoint object that is an attractor for an individual.
     **/
    public Waypoint getActiveWaypoint(Individual currentIndividual) {
	// Remove a waypoint if you get within a certain distance to the waypoint
	if (waypoints.size() > 0) {
	    Waypoint activeWaypoint = (Waypoint) waypoints.objs[0];

	    if (currentIndividual.distanceTo(activeWaypoint.getTargetPoint(currentIndividual)).magnitude() < activeWaypoint.getRadius() + thresholdDistance) {
		waypoints.removeNondestructively(0);
	    }
	}

	// Return a waypoint if there is at least one remaining in the path	
	if (waypoints.size() > 0) {
	    return (Waypoint) waypoints.objs[0];
	} else {
	    return null;
	}
    }

    /**
     * Sets the active waypoint in the path. This can be used to programaticaly 
     * move forward in the path, just pass the location of the current active waypoint.
     *
     * @param location The location of the  active wayPoint.
     * @return The current active waypoint after any removals which were necessary.
     **/
    public Waypoint setActiveWaypoint(Vector2D location) {
	// Remove a waypoint if you get within a certain distance to the waypoint
	if (waypoints.size() > 0) {
	    Waypoint activeWaypoint = (Waypoint) waypoints.objs[0];

	    if (location.distance(activeWaypoint.getCenter()) < activeWaypoint.getRadius() + thresholdDistance) {
		waypoints.removeNondestructively(0);
	    } else {
		if (waypoints.size() > 0) {
		    return (Waypoint) waypoints.objs[0];
		} else {
		    return null;
		}
	    }
	}

	return setActiveWaypoint(location);
    }

    /**
     * Returns a point of intersection between a given ray and this obstacle.
     * No intersections should ever occur with a path since they are theoretical,
     * and not physical entities.
     * 
     * @param origin The origin of the ray to test the intersection with.
     * @param vector The direction of the ray to test the intersection with.
     * @return The point of intersection. If no intersection is found then it returns (-1,-1).
     **/
    public Vector2D getClosestPoint(Vector2D origin, Vector2D vector) {
	return new Vector2D(-1, -1);
    }

   /**
     * Returns the normal vector at a given point on the entity. 
     * No intersections should ever occur with a path since they are theoretical,
     * and not physical entities.
     * 
     * @param point The point on the obstacle for which the normal is being found.
     * @return The normal to the surface of the obstacle.
     **/
    public Vector2D getNormalAt(Vector2D point) {
	return new Vector2D(0, 0);
    }

    /**
     * Returns the tangent to the entity at a given point.
     * No intersections should ever occur with a path since they are theoretical,
     * and not physical entities.
     * 
     * @param point The Point at which to fine the tangent line.
     * @param orientation The direction of the incident ray. This is used to pick the direction of the tangent vector.
     * @return Vector2D The tangent to the obstacle.
     **/
    public Vector2D getTangentAt(Vector2D point, Vector2D orientation) {
	return new Vector2D(0, 0);
    }

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
    /**
     * Return the size of the path, i.e. the number of remaining waypoints
     * in the path.
     *
     * @return An int indicating the number of waypoints in the path.
     **/
    public int size() {
	return waypoints.size();
    }

    /**
     * Get the path's id.
     *
     * @return An int representing the path's id.
     **/
    public int getID() {
	return this.id;
    }

    /**
     * Set the path's id.
     * 
     * @param val The ID for the path.
     **/
    public void setID(int val) {
	this.id = val;
    }

    /**
     * Get the weight of the path.
     *
     * @return A double representing the path's weight.
     **/
    public double getWeight() {
	return this.weight;
    }

    /**
     * Set the path's weight.
     *
     * @param val The weight associated with this path.
     **/
    public void setWeight(double val) {
	this.weight = val;
    }

    /**
     * Get the waypoints for the path.
     *
     * @return A bag containing the waypoints for the path.
     **/
    public Bag getWaypoints() {
	return this.waypoints;
    }

    /**
     * Sets the waypoints for the path.
     *
     * @param val A bag containing the waypoints for the path.
     **/
    public void setWaypoints(Bag val) {
	this.waypoints = val;
    }
}
