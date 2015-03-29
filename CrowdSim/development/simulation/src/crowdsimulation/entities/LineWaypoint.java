/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package crowdsimulation.entities;

import crowdsimulation.entities.individual.Individual;
import crowdsimulation.entities.obstacle.RectangularObstacle;

import java.util.*;
import math.Vector2D;
import sim.field.continuous.*;

/**
 *
 * @author ganil
 */
public class LineWaypoint extends RectangularObstacle implements Waypoint {
   	/** The id of the waypoint. **/
	private int ID;

    /** The distance to which and individual comes before the waypoint is considered reached. */
    protected final double radius = 0.5;

    protected double x;
    protected double y;
    protected double length;
    protected double angle;
    protected double leftTarget;
    protected double rightTarget;

    /**
	 * Constructs a Waypoint on a given terrain, with a givin location and a radius of interest.
	 *
	 * @param terrain The terrain that this obstacle should be a part of.
	 * @param x The x component of the upper left corner of the obstacle.
	 * @param y The y component of the upper left corner of the obstacle.
	 * @param width The width of the obstacle.
	 * @param height The height of the obstacle.
	 * @param angle The angle which the obstacle should be off the x axis.
	 **/

    public LineWaypoint( Continuous2D terrain, double x, double y, double length, double angle ) {
        super(terrain,x,y,length,1,angle);
        this.x = x;
        this.y = y;
        this.length = length;
        this.angle = angle;
        this.leftTarget = .1;
        this.rightTarget = .9;

    }

    /**
	 * Constructs a Waypoint on a given terrain, with a givin location and a radius of interest.
	 *
	 * @param terrain The terrain that this obstacle should be a part of.
	 * @param x The x component of the upper left corner of the obstacle.
	 * @param y The y component of the upper left corner of the obstacle.
	 * @param width The width of the obstacle.
	 * @param height The height of the obstacle.
	 * @param angle The angle which the obstacle should be off the x axis.
	 **/

    public LineWaypoint( Continuous2D terrain, double x, double y, double length, double angle, double leftTarget, double rightTarget ) {
        super(terrain,x,y,length,1,angle);
        this.x = x;
        this.y = y;
        this.length = length;
        this.angle = angle;
        this.leftTarget = leftTarget;
        this.rightTarget = rightTarget;
    }

    public int getID() {
        return ID;
    }

    public void setID(int val) {
        ID = val;
    }

    public Vector2D getTargetPoint(Individual ind) {
        double x2 = x + Math.cos(angle)*length;
        double y2 = y + Math.sin(angle)*length;
        double x3 = ind.getLocation().x;
        double y3 = ind.getLocation().y;

        double u = ((x3-x)*(x2-x) + (y3-y)*(y2-y)) * 1. / (Math.pow((x2-x),2) + Math.pow((y2-y),2));

        if(u < leftTarget) {
            u = leftTarget;
        } else if(u > rightTarget) {
            u = rightTarget;
        }

        return new Vector2D(x + u * (x2-x),y + u * (y2-y));
    }


    /**
     * Returns the center of the line. Used mainly for drawing the line between waypoints.
     *
     * @return
     */
    @Override
    public Vector2D getCenter() {
        //System.out.println(new Vector2D(x + length/2. * Math.cos(angle), y + length/2. * Math.sin(angle)));
        return new Vector2D(x + length/2. * Math.cos(angle), y + length/2. * Math.sin(angle));
    }
    public double getRadius() {
        return radius;
    }




}
