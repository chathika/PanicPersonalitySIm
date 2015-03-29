/*
 * $RCSfile: IndividualTest.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.tests;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import java.util.*;
import sim.field.continuous.*;
import sim.util.*;

/**
 * Class For Testing features of the simulation Framework.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class IndividualTest extends Test
{
	/**
	 * Exectues the tests for the crowdSimulation System.
	 *
	 * @param args
	 */
	public static void main(String[] args) 
	{
		IndividualTest tests = new IndividualTest();
        Log systemLog = new Log( "../logs/SystemTest.dat" );
        systemLog.setFileOutput( true );
        systemLog.setLogLevel(0);
        
        tests.runTest();
	}
	
	public IndividualTest()
	{
	}
	
	public boolean executeScenarios()
	{
		boolean pass = true;
		
		pass = pass && generateByParameterSet();
		
		return pass;		
	}

	public boolean generateByParameterSet()
	{
		scenarioName = "generateByParameterSet";
		boolean pass = true;
		
		int entity_id = 1;
		HashMap parameters = new HashMap();
		parameters.put("diameter", new Double(0.6));
		parameters.put("violenceRating", new Double(5.0));
		
		HMFVModel movementModel = new HMFVModel();
		Continuous2D terrain = new Continuous2D( 10, 200, 200 );
		CrowdSimulation simState = new CrowdSimulation( (long)1.0 );
		RectangularObstacle regionOfCreation = new RectangularObstacle( terrain, 0, 0, 200, 200 );
		
		

//scenario parameters
		scenarioHeader();
		log( "" );
		
		pass = pass && newIndividual( entity_id, parameters );
		
		parameters.put("diameter", new Double(6.4));
		parameters.put("violenceRating", new Double(9.2));
		
		pass = pass && newIndividual( ++entity_id, parameters );
		
		parameters.put("diameter", new Double(3.4));
		parameters.put("violenceRating", new Double(7.2));
		
		pass = pass && newIndividual( ++entity_id, parameters );
		
		pass = pass && createIndividuals( 100, regionOfCreation, movementModel, parameters );
		
//end Method Testing section
		scenarioFooter( pass );
		return pass;
	}

	public boolean newIndividual( int entity_id, Map parameters ) 
	{
		methodHeader();
		log( "      newIndividual( " + entity_id + ", " + parameters + " ) " );
		log( "" );
		
		Individual ind = new Individual( entity_id, parameters );
		
		log( "         Created Individual "  );		
		log( "            entityID = " + ind.getEntityID() );
		log( "            individualID = " + ind.getID() );
		log( "            paths = " + ind.getInteractingPaths() );
		boolean pass = true;
		
		pass = pass && (entity_id == ind.getEntityID());
		pass = pass && (((Double)parameters.get("diameter")).doubleValue() == ind.getDiameter());
		
		log( "" );
		methodFooter();
		
		return true;
	}

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
		log( "            violenceRating = " + ((Double)parameters.get("violenceRating")).doubleValue() );
		
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
