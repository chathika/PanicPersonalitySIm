/*
 * $RCSfile: IndividualWithRayTest.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.tests;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.actioncontroller.strategy.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import math.*;
import java.util.*;
import sim.field.continuous.*;
import sim.util.*;

/**
 * Class For Testing Ray Tracing features of the simulation Framework.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class IndividualWithRayTest extends Test
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The simulation state to hold the obstacles to sample the environment. **/
	CrowdSimulation simState;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Exectues the Ray Tracing tests for the crowdSimulation System.
	 *
	 * @param args
	 */
	public static void main(String[] args) 
	{
		IndividualWithRayTest tests = new IndividualWithRayTest();
        Log systemLog = new Log( "../logs/SystemTest.dat" );
        systemLog.setFileOutput( true );
        systemLog.setLogLevel(0);
        
        tests.runTest();
	}
	
	
	/**
	 * Constructs the Ray to test ray tracing aspects of the framework.
	 **/
	public IndividualWithRayTest()
	{
	}
	
	
	/**
	 * This executes the different scenarios for the test.
	 * The tests being run are:
	 *   1. axialRayTrace()
	 **/
	public boolean executeScenarios()
	{
		boolean pass = true;
		
//		pass = pass && generateByParameterSet();
		pass = pass && axialRayTrace();
		
		return pass;
	}

	/**
	 * Tests the axial ray tracing methods to find obstacles.
	 **/
	public boolean axialRayTrace()
	{
		scenarioName = "axialRayTrace";
		boolean pass = true;
		int entity_id = 1;
		HashMap parameters = new HashMap();
		parameters.put("diameter", new Double(0.6));
		
		HMFVRayTracerModel movementModel = new HMFVRayTracerModel();
		simState = new CrowdSimulation( (long)1.0 );
		
		Obstacle obs1 = new CircularObstacle( simState.getTerrain(), 2, 1, 0.6 );
		

//scenario parameters
		scenarioHeader();
		log( "" );
		
		parameters.put("location", new Vector2D( 1, 1 ));
		Individual ind1 = newIndividual( entity_id, parameters );
		ind1.setActionController( movementModel );
		ind1.getActionController().setParameters( parameters );
		ind1.setActionStrategy( ind1.getActionController().getNewStrategy() );
		ind1.getActionStrategy().setParameters( parameters );

//		parameters.put("location", new Vector2D( 2, 1 ));
//		Individual ind2 = newIndividual( entity_id, parameters );
//		ind2.setActionController( movementModel );
//		ind2.getActionController().setParameters( parameters );
//		ind2.setActionStrategy( ind2.getActionController().getNewStrategy() );
//		ind2.getActionStrategy().setParameters( parameters );
		
		ind1.getActionStrategy().calculateMovementParameters( simState );
		Bag pois = ((HMFVRayTracerStrategy)ind1.getActionStrategy()).getPointsOfInterest();

		for( int i = 0; i < pois.size(); i++ )
		{
			System.out.println( "" + ((PointOfInterest)pois.get(i)).getLocation() );
		}

//end Method Testing section
		scenarioFooter( pass );
		return pass;
	}

	/**
	 * Constructs a new individual, and prints the important data for it out.
	 *
	 * @param entity_id The id on the entity to be created.
	 * @param parameters The parameters to be used to construct the individuals.
	 * @return The individual which has been created for the test.
	 **/
	public Individual newIndividual( int entity_id, Map parameters ) 
	{
		methodHeader();
		log( "      newIndividual( " + entity_id + ", " + parameters + " ) " );
		log( "" );
		
		Individual ind = new Individual( entity_id, parameters );
		
		log( "         Created Individual "  );		
		log( "            entityID = " + ind.getEntityID() );
		log( "            individualID = " + ind.getID() );
		log( "            paths = " + ind.getInteractingPaths() );
		log( "            location = " + ind.getLocation() );
		boolean pass = true;
		
		pass = pass && (entity_id == ind.getEntityID());
		pass = pass && (((Double)parameters.get("diameter")).doubleValue() == ind.getDiameter());
		
		log( "" );
		methodFooter();
		
		return ind;
	}

	/**
	 * Creates a set of individuals for the simulation.
	 *
	 * @param numberOfIndividuals The number of indiviudals to be created.
	 * @param regionOfCreation The region in which to construct the individuals.
	 * @param movementController The controller which should maintain the indiviudals.
	 * @param parameters The parameters used to construct the controller and the individuals.
	 * @return If the indiviudals were able to be created or not.
	 **/
	public boolean createIndividuals
		( 
			int numberOfIndividuals, 
			Obstacle regionOfCreation,  
			ActionController movementController,
			Map parameters
		)
	{
		boolean pass = true;
		EntityFactory factory = EntityFactory.getInstance();
		
		methodHeader();
		log( "      createIndividuals( " + numberOfIndividuals + ", " + regionOfCreation + ", " + movementController + ", " + parameters + " ) " );
		log( "" );
		
		
		log( "         Created Individuals "  );
		log( "            number Of Individuals To Create = " + numberOfIndividuals  );
		log( "            diameter = " + ((Double)parameters.get("diameter")).doubleValue() );
		log( "            location = " + (parameters.get("location") ) );
		

		Bag individuals = factory.createIndividuals( numberOfIndividuals, regionOfCreation, movementController, parameters );
		
		pass = pass && numberOfIndividuals == individuals.size();
		
		for ( int i = 0; i < numberOfIndividuals; i++ )
		{
			Individual ind = (Individual)individuals.get(i);
			pass = pass && (ind.getActionController().equals( movementController ));
			pass = pass && (((Double)parameters.get("diameter")).doubleValue() == ind.getDiameter());
		}
		
		return pass;
	}
	
}
