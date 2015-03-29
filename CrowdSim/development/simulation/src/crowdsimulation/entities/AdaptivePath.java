/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package crowdsimulation.entities;


import crowdsimulation.entities.individual.Individual;
import math.*;			// Used for Vector2D.

import sim.portrayal.*;	// Used for SimplePortrayal2D.
import java.awt.*;
import java.awt.geom.*; // Used for Ellipse2D.
import java.util.*;		// Used for ArrayList.
import sim.util.*; 		// Used for Bag object.

/**
 *
 * @author ganil
 */
public class AdaptivePath extends Path{

    
    public AdaptivePath() {
	super();
	//System.out.println("Creating adaptivepath");
    }
    
    @Override
    public Waypoint getActiveWaypoint(Individual currentIndividual) {
	//System.out.println("GAW");
		// Remove a waypoint if you get within a certain distance to the waypoint or if the 
        	//System.out.println("In get Active waypoint.");
		if( waypoints.size() > 0 )
		{
			Waypoint activeWaypoint = (Waypoint)waypoints.objs[0];
                        

            //System.out.println(activeWaypoint);
            //System.out.println(activeWaypoint.getCenter());
            //System.out.flush();
			double distance = activeWaypoint.getCenter().distance( currentIndividual.getLocation() );
			
			if( currentIndividual.distanceTo( activeWaypoint.getTargetPoint(currentIndividual) ).magnitude() < thresholdDistance+activeWaypoint.getRadius() )
			{
				waypoints.removeNondestructively(0);	
			}
                        else if (waypoints.size() > 1) {
                            Waypoint nextWaypoint = (Waypoint)waypoints.objs[1];
                            double waypointToNext = activeWaypoint.getCenter().distance(nextWaypoint.getCenter());
                            double distanceToNext = nextWaypoint.getCenter().distance(currentIndividual.getLocation());
			   // System.out.println("Distance = " + distance + " rad = " + activeWaypoint.getRadius());
                            if(distance < activeWaypoint.getRadius() * 2 && Math.pow(distanceToNext,2) < Math.pow(waypointToNext,2) + Math.pow(distance,2)) {
				System.out.println("Changing to closer waypoint.");
				waypoints.removeNondestructively(0);	
                            }
                        }
		}
		
		// Return a waypoint if there is at least one remaining in the path	
		if( waypoints.size() > 0 ) 
		{
			return (Waypoint)waypoints.objs[0];
		}
		else 
		{
			return null;	
		}	
    }

    @Override
    public Waypoint setActiveWaypoint(Vector2D location) {
 		// Remove a waypoint if you get within a certain distance to the waypoint
	System.out.println("SAW");
		if( waypoints.size() > 0 )
		{
			Waypoint activeWaypoint = (Waypoint)waypoints.objs[0];
			double distance = location.distance( activeWaypoint.getCenter() );
			if( distance < activeWaypoint.getRadius()+thresholdDistance )
			{
				waypoints.removeNondestructively(0);	
				return setActiveWaypoint( location );

			}
			else if (waypoints.objs.length > 1) {
                            Waypoint nextWaypoint = (Waypoint)waypoints.objs[1];
                            double waypointToNext = activeWaypoint.getCenter().distance(nextWaypoint.getCenter());
                            double distanceToNext = nextWaypoint.getCenter().distance(location);
                            if(waypointToNext + distance > distanceToNext + thresholdDistance) {
				waypoints.removeNondestructively(0);	
                            }
			    return setActiveWaypoint( location );
                        }
			
				
		}
		if( waypoints.size() > 0 ) 
		{
			return (Waypoint)waypoints.objs[0];
		}
		else 
		{
			return null;	
		}
    }
    
    
}
