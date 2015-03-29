/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package crowdsimulation.entities;

import crowdsimulation.entities.individual.Individual;
import math.Vector2D;

/**
 *
 * @author ganil
 */
public interface Waypoint {

	/**
	 * Get the waypoint's id.
	 *
	 * @return The waypoint's id.
	 **/
	public int getID();

	/**
	 * Set the waypoint's id.
	 *
	 * @param val The waypoint's id.
	 **/
	public void setID( int val );

    Vector2D getTargetPoint(Individual ind);

    Vector2D getCenter();

    double getRadius();
}
