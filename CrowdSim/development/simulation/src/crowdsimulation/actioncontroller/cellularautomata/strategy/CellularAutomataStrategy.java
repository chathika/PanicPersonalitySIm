/*
 * $RCSfile: CellularAutomataStrategy.java,v $ $Date: 2009/06/11 20:36:56 $
 */
package crowdsimulation.actioncontroller.cellularautomata.strategy;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.actioncontroller.cellularautomata.*;
import crowdsimulation.actioncontroller.strategy.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import ec.util.*;
import java.util.*;
import math.*;
import sim.engine.*;
import sim.field.grid.*;
import sim.util.*;

/**
 * Base class to controll entities with using Cellular Automata.
 * This class contains the attributes and methods which are common to Action Models
 * which will use ray traced vision techniques to view the environment.
 * This is object which is called at each step in the simulation.
 *
 * @author $Author: ganil $
 * @version $Revision: 1.6 $
 * $State: Exp $
 * $Date: 2009/06/11 20:36:56 $
 **/
public abstract class CellularAutomataStrategy extends ActionStrategy
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
    
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	public CellularAutomataStrategy()
	{
		super();
	}

	/**
	 * Sets the attributes for the Action Controller defined in this class.
	 **/
	public CellularAutomataStrategy( Map parameters  ) 
	{
		this();
		setParameters( parameters );
	}

	/**
	 * This calculates the points that are needed to calculate the movement parameters.
	 *
	 * @param state The current State of the simulation.
	 **/
	public void calculateMovementParameters( CrowdSimulation state )
	{
        //System.out.println("Calculate Movement Parameters CellularAutomata Strategy");
        if( ind.isDead() )
        { 
        	return;
       	}
       	
        Bag individuals = ind.getNeighboringIndividuals();
        Bag obstacles = ind.getNeighboringObstacles();
	}
	
	/**
	 * This is where the individual is moved forward by a small timestep.
	 * This is basically integrating the forces over the timestep to calculate where the individuals
	 * moves to and moves them there.
	 **/
	public abstract void move( );

	/**
	 * Moves the individuals.
	 **/
	public void setLocation( int x, int y )
	{
		double cellWidth = ((CellularAutomata)ind.getActionController()).getCellWidth();
		double cellHeight = ((CellularAutomata)ind.getActionController()).getCellHeight();
		
		ind.setLocation( (x*cellWidth)+cellWidth/2, (y*cellHeight)+cellHeight/2 );
		((CellularAutomata)ind.getActionController()).getWorldGrid().setObjectLocation(ind,x,y);
	}
	
	public Int2D getLocation()
	{
		double cellWidth = ((CellularAutomata)ind.getActionController()).getCellWidth();
		double cellHeight = ((CellularAutomata)ind.getActionController()).getCellHeight();
		
		Vector2D loc = ind.getLocation();
		return new Int2D( (int)(loc.x/cellWidth), 
		                  (int)(loc.y/cellHeight) );
	}
	
	/**
	 * Returns the location of the current wayPoint;
	 *
	 * @return The location of the individuals current waypoint.
	 **/
	public Int2D getWayPointLocation()
	{
		double cellWidth = ((CellularAutomata)ind.getActionController()).getCellWidth();
		double cellHeight = ((CellularAutomata)ind.getActionController()).getCellHeight();

		selectPath();
		Path path = ind.getSelectedPath();
		CircularWaypoint wp = (CircularWaypoint)path.getActiveWaypoint( ind );

		if( wp != null )
		{		
			return new Int2D( (int)(wp.getCenter().x/cellWidth), 
		                  (int)(wp.getCenter().y/cellHeight) );
		}
		else
		{		
            StringBuffer data = new StringBuffer();
            if (ind.getActionStrategy() instanceof FloorFieldCAStrategy) {
                data.append(ind.getID());
                data.append(",");
                data.append(((FloorFieldCAStrategy) ind.getActionStrategy()).createTime);
                data.append(",");
                data.append(((FloorFieldCAStrategy) ind.getActionStrategy()).timeWalking);
                data.append(",");
                data.append(((FloorFieldCAStrategy) ind.getActionStrategy()).numCollisions);
                data.append(",");
                if (ind.getSelectedPath() != null) {
                    data.append(ind.getSelectedPath().getID());
                }
            }
            if (ind.getActionStrategy() instanceof SimLibCAStrategy) {
                data.append(ind.getID());
                data.append(",");                Log.log(1, data.toString());
                data.append(((SimLibCAStrategy) ind.getActionStrategy()).createTime);
                data.append(",");
                data.append(((SimLibCAStrategy) ind.getActionStrategy()).timeWalking);
                data.append(",");
                data.append(((SimLibCAStrategy) ind.getActionStrategy()).timeInQueue);
                data.append(",");
                data.append(((SimLibCAStrategy) ind.getActionStrategy()).timeWorking);
                data.append(",");
                if (ind.getSelectedPath() != null) {
                    data.append(ind.getSelectedPath().getID());
                }
            }
             Log.log(1, data.toString());
            ind.kill();

		}
		return new Int2D( 0, 0 );

	}
	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
		super.setParameters( parameters );	
	}
	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

}