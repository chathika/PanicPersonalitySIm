/*
 * $RCSfile: SalamanderModel.java,v $ $Date: 2010/04/27 19:23:25 $
 */
package crowdsimulation.actioncontroller;

import crowdsimulation.actioncontroller.strategy.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.logging.*;
import crowdsimulation.*;
import math.*;
import java.util.*;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;

/**
 * This represents the ActionController to represent salamander movement in the simulation.
 *
 * @see crowdsimulation.actioncontroller.ActionController
 * @author $Author: dkaup $
 * @version $Revision: 1.6 $
 * $State: Exp $
 * $Date: 2010/04/27 19:23:25 $
 **/
public class SalamanderModel extends SocialPotentialModel
{

////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
	
	/** The names of all the forces. **/
	public static String forceNames = 	"forceRandom.x,forceRandom.y," +
										"forceTowardsCoverMemory.x,forceTowardsCoverMemory.y,"+
										"forceTowardsFoodSource.x,forceTowardsFoodSource.y,"+
										"forceTowardsFood.x,forceTowardsFood.y,"+
										"forceTowardsHiding.x,forceTowardsHiding.y,"+
										"forceTowardsOthers.x,forceTowardsOthers.y,"+
										"forceTowardsRegion.x,forceTowardsRegion.y,"+
										"forceTowardsWater.x,forceTowardsWater.y,"; 
	
	/** Default values for all forces 0's. **/
	public static String forceValues = 	"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0,"+
										"0,0";

    //saves the hiding obstacle that individuals are heading
    // DJK: I do not find that these two are used anywhere      *************
    public Bag forceTowardHidingObstacles = new Bag();
    public Bag forceTowardCoverMemoryObstacles = new Bag();

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Default constructor of the SalamanderModel.
	 **/
	public SalamanderModel() 
	{
		super();
	}
	
	/**
	 * Constructor for SalamanderModel which sets default attributes.
	 *
	 * @param parameters The parameters to use to initial the model and all associated strategies.
	 **/
	public SalamanderModel( Map parameters ) 
	{
		super( parameters );
		this.setParameters( parameters );
	}
	
	/**
	 * Creates an instance of a default  salamander strategy.
	 * The strategy is populated with the same parameters that this model was constructed with.
	 *
	 * @return ActionStrategy A new instance of a SalamanderStrategy
	 * @see crowdsimulation.actioncontroller.strategy.SalamanderStrategy
	 **/
	public ActionStrategy getNewStrategy()
	{
		return new SalamanderStrategy( params );
	}

    @Override
    public void preStep( CrowdSimulation state )
    {
        System.out.println( state.getStepNum() );
        calculateMovementParameters( state );

    }
    /**
	 * This is where any cleanup or after move operations should take place.
	 * Currently this allows salamanders to "eat" any insects they have moved over.
	 * @Override overrides ActionController
	 * @param state This is the current state of the simulation.
	 **/
    @Override
	public void postStep( CrowdSimulation state )
	{	// loop overal all individuals
        for( int i =0; i < individuals.numObjs; i++ )
        {
        	Individual ind = (Individual)individuals.get( i );
        	if( !ind.isDead() )
	        {
        		if( ind.getActionStrategy() instanceof SalamanderStrategy){
                    ((SalamanderStrategy)ind.getActionStrategy()).eat();
                }
        	}
		}
        //Check to see if any indiviudals are under hiding obstacle
       	Bag obstacles = CrowdSimulation.getInstance().getTerrain().allObjects;
        for( int i=0; i<obstacles.numObjs; i++ )
		{
			Obstacle obs = (Obstacle)obstacles.objs[i];
            obs.setOfInterest(true);
        }
        //System.out.println("StepNum " + CrowdSimulation.getInstance().getStepNum() + " --------------------------------------");
    }

	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the salamander model.
	 **/
	public void setParameters( Map parameters )
	{
		super.setParameters( parameters );
		
	}
	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets the names of all forces used for this model as a comma seperated list.
	 *
	 * @return String The string on the force names. 
	 **/
	public String getForceNames()
	{
		return forceNames;
	}
	
	/**
	 * This gets a string of zero's for the forces used in this model seperated by commas.
	 *
	 * @return String The string of zeros for each force.
	 **/
	public String getForceValues()
	{
		return forceValues;
	}
	
}
