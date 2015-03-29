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

public class OETest extends Test {

/**
 * Exectues the tests for the crowdSimulation System.
 *
 * @param args
 */
public static void main(String[] args) 
{
	OETest tests = new OETest();
    Log systemLog = new Log( "../logs/SystemTest.dat" );
    systemLog.setFileOutput( true );
    systemLog.setLogLevel(0);
        
   tests.runTest();
}

/**
 *Performs tests and checks to see if all pass.
 *
 **/
public boolean executeScenarios()
{
	boolean pass = true;
	pass = pass &&  severityOfOverlapTests();
//	pass = pass &&  overlapDetectionTests();
//	pass = pass && 	calculatedOEMoveTests();
//	pass = pass &&  masterOverlapStackTests();
//	pass = pass &&	initiativeAndDependenceTests();
	
	return pass;
}	

/**
 *Tests whether the severity of overlap is being correctly calculated
 *for two situations: overlap between individuals and overlap of an individual by
 *an obstacle.
 *
 *@return boolean value true if overlap detections accurate for all scenarios. 
 **/
public boolean overlapDetectionTests()
{
	scenarioName = "overlap detection tests";
	boolean pass = false;
	
	/*create instance of crowd simulation*/
	String configName = "../configs/emptyWorld.xml";
	CrowdSimulation simState = new CrowdSimulation( (long)1.0,configName );

    /*create an instance of LKFModel to control individuals*/
    LKFModel2 movementModel = new LKFModel2();
	
	int entity_id1 = 1;
	HashMap parameters1 = new HashMap();
	parameters1.put("diameter", new Double(0.6));
	parameters1.put("location", new Vector2D(10, 10));
	Individual ind = new Individual( entity_id1, parameters1);
		
	int entity_id2 = 2;
	HashMap parameters2 = new HashMap();
	parameters2.put("diameter", new Double(0.6));	 
	parameters2.put("location" , new Vector2D( 10, 10.43));
	Individual overlapper1 = new Individual( entity_id2, parameters2);
	
	int entity_id3 = 3;
	HashMap parameters3 = new HashMap();
	parameters3.put("diameter", new Double(0.6));
	parameters3.put("location", new Vector2D(10,9.52));
	Individual overlapper2 = new Individual( entity_id3, parameters3);
		
	int entity_id4 = 4;
	HashMap parameters4 = new HashMap();
	parameters4.put("diameter", new Double(0.6));	 
	parameters4.put("location" , new Vector2D(10.55,10));
	Individual nonOverlapper1 = new Individual( entity_id4, parameters4);
	
	int entity_id5 = 5;
	HashMap parameters5 = new HashMap();
	parameters5.put("diameter", new Double(0.6));	 
	parameters5.put("location" , new Vector2D(11,11));
	Individual nonOverlapper2 = new Individual( entity_id5, parameters5);
	
	int entity_id6 = 6;
	HashMap attributes1 = new HashMap();
	attributes1.put("center", new Vector2D(10, 9.52));
	attributes1.put("radius", new Double(.3) );
	attributes1.put("type", "Circular");
	CircularObstacle overlappingCircularObstacle1 =(CircularObstacle) Obstacle.build(entity_id6, attributes1);
			
	int entity_id7 = 7;
	HashMap attributes2 = new HashMap();	
	attributes2.put("center", new Vector2D(9,9));
	attributes2.put("radius", 1  );
	attributes2.put("type", "Circular");
	CircularObstacle overlappingCircularObstacle2 =(CircularObstacle) Obstacle.build(entity_id7, attributes2);
	
	ind.setActionController(movementModel);
	ind.setActionStrategy((LKFStrategy2)(((LKFModel2)(ind.getActionController())).getNewStrategy()));
	LKFStrategy2 str = (LKFStrategy2) ind.getActionStrategy();
	
	scenarioHeader();
	log( "         Detection of overlapping individuals" );
	log("		   ind ID = " + ind.getID());
	log( "         ind location = " + ind.getLocation() );
	log( "         ind radius = " + ind.getRadius() );
	log( "         ind compression radius = " + (ind.getRadius() - ind.getMaxCompression()));
	log( "         " );
	log("		   overlapper1 ID = " + overlapper1.getID());
	log( "		   Overlapper #1, most overlapped with ind");
	log( "		   overlapper1 location = " + overlapper1.getLocation() );
	log( "		   overlapper1 radius = " + overlapper1.getRadius() );
	log( "		   overlapper1 compression radius = " + (overlapper1.getRadius() - overlapper1.getMaxCompression()));
	log( "		   Overlapper1 severity of overlap with ind = " + str.severityOfOverlapWithInd(overlapper1));
	log( "		   " );
	log("		   overlapper2 ID = " + overlapper2.getID());
	log( "         Overlapper #2, least overlapped with ind");
	log( "		   overlapper2 location = " + overlapper2.getLocation() );
	log( " 		   overlapper2 radius = " + overlapper2.getRadius());
	log( "		   overlapper2 compression radius = " + (overlapper2.getRadius() - overlapper2.getMaxCompression()));
	log( "		   Overlapper2 severity of overlap with ind = " + str.severityOfOverlapWithInd(overlapper2));
	log( "         " );
	log("		   Non-overlapper1 ID = " + nonOverlapper1.getID());
	log( "         Non-Overlapper #1, Touching but not overlapping, i.e. radius penetrates ind but within compression limit");
	log( "		   nonOverlapper1 location = " + nonOverlapper1.getLocation() );
	log( " 		   nonOverlapper1 radius = "+ nonOverlapper1.getRadius());
	log( "		   nonOverlapper1 severity of overlap with ind = " + str.severityOfOverlapWithInd(nonOverlapper1));
	log( "		   " );
	log("		   nonOverlapper2 ID = " + nonOverlapper2.getID());
	log( "         nonOverlapper #2, Not touching.");
	log( "		   nonOverlapper2 location = " + nonOverlapper2.getLocation() );
	log( " 		   nonOverlapper2 radius = " + nonOverlapper2.getRadius());
	log( "		   nonOverlapper2 compression radius = " + (nonOverlapper2.getRadius() - nonOverlapper2.getMaxCompression()));
	log( "		   nonOverlapper2 severity of overlap with ind = " + str.severityOfOverlapWithInd(nonOverlapper2));
	log( "			" );
	log("		   circular Obstacle1 ID = " + overlappingCircularObstacle1.getId());
	log( "		   obstacle 1");
	log( "		   obstacle1 location = " + overlappingCircularObstacle1.getLocation());
	log( "		   obstacle1 radius = " + overlappingCircularObstacle1.getRadius() );
	log( "		   obstacle1 severity of overlap with ind = " + str.severityOfOverlapWithInd(overlappingCircularObstacle1));
	log( "		   " );


	Bag overlappingIndividuals = str.individualsOverlappingInd();
	Bag overlappingObstacles = str.obstaclesOverlappingInd();
	
	/*check if the right number of overlappers have been grabbed*/ 
	int numberOfOverlappingIndividuals = overlappingIndividuals.size();
	int numberOfOverlappingObstacles = overlappingObstacles.size();
	if(numberOfOverlappingIndividuals == 2)
	{	
		if((numberOfOverlappingObstacles)== 1)
		{pass = true;}
	}
	
	
	log( "			number of expected overlapping Individuals = " + 2);					
	log( "          numberOfOverlappers = " + numberOfOverlappingIndividuals);
	log( " 			number of expected overlapping Individuals = " + 1);
	log( "          number of overlapping obstacles = " + numberOfOverlappingObstacles);
	log( "			");
	
	/*check to see if overlapper1 and overlapper2 has been grabbed*/
    log( "			 overlapping individuals: ");
    for(int i = 0; i < numberOfOverlappingIndividuals; i++)
    {
    	Individual current = (Individual) overlappingIndividuals.get(i);
    	log( "         "+current.getID());
    }
    log( "			 overlapping obstacles: ");
    for(int i = 0; i < numberOfOverlappingObstacles; i++)
    {
    	Obstacle obs = (Obstacle) overlappingObstacles.get(i);
    	log( "		   "+obs.getId());
    }
	
	/*check to see if overlappers in the proper order, by severity of overlap*/
	
	
	
	
	
	
	return pass;
}

/**
 *Checks if overlaps are being detected properly.
 *
 **/
public boolean severityOfOverlapTests()
{
	scenarioName = "severity of overlap tests";
	boolean pass = false;
	
	ObstacleModel obsController = new ObstacleModel();
	LKFModel2 movementModel = new LKFModel2();
	CrowdSimulation simState = new CrowdSimulation( (long)1.0, "../configs/emptyWorld.xml" );

    EntityFactory factory = new EntityFactory();
  	 
    
    int entity_id1 = 1;
	HashMap parameters1 = new HashMap();
	parameters1.put("diameter", new Double(0.69257));
	//parameters1.put("location", new Vector2D(9.65, 10));
	//parameters1.put("location", new Vector2D(10,10));
	parameters1.put("location", new Vector2D(4, 9.117330407));
	Individual ind1 = new Individual( entity_id1, parameters1);

/*		
	int entity_id2 = 2;
	HashMap parameters2 = new HashMap();
	parameters2.put("diameter", new Double(0.6));	 
	//parameters2.put("location" , new Vector2D(10,10.48));
	double angle = Math.PI/3;
	//parameters2.put("location", new Vector2D(10 + Math.cos(angle)*.43 , 10 + Math.sin(angle)*.43 ));
	parameters2.put("location" , new Vector2D(20,20));
	Individual ind2 = new Individual( entity_id2, parameters2);
*/

	
	int entity_id3 = 3;
	HashMap attributes1 = new HashMap();
	//attributes1.put("radius", new Double(.3));
	attributes1.put("radius", new Double(.5));
	//attributes1.put("center", new Vector2D( 10, 10));
	//attributes1.put("center", new Vector2D((10 + Math.cos(angle)*.43), (10 + Math.sin(angle)*.43)));
	attributes1.put("center", new Vector2D(5,5));
	attributes1.put("center", new Vector2D(10,10.7));
	attributes1.put("type", "Circular");
   // CircularObstacle circularObs = factory.createObstacle( obsController ,attributes1);
	
	
	int entity_id4 = 4;
	HashMap attributes2 = new HashMap();
	attributes2.put("width", new Double(15));
	attributes2.put("height", new Double(1));
	attributes2.put("center", new Vector2D(9.5,8.5));
	attributes2.put("type", "Rectangular");
	RectangularObstacle rectangularObs = (RectangularObstacle)factory.createObstacle( obsController,attributes2); 



    Bag obstacles = CrowdSimulation.getInstance().getTerrain().getAllObjects();
	int numberOfObstacles = obstacles.size();



/*	
	scenarioHeader();
	log( "         Entities Involved: " );
	log( "         ind1 location = " + ind1.getLocation() );
	log( "         ind1 radius = " + ind1.getRadius() );
	log( "         ind1 compression radius = " + (ind1.getRadius() - ind1.getMaxCompression()));
	log( "         " );
	log( "		   ind2 location = " + ind2.getLocation() );
	log( "		   ind2 radius = " + ind2.getRadius() );
	log( "		   ind2 compression radius = " + (ind2.getRadius() - ind2.getMaxCompression()));
	log( "		   " );
//	log( "         circObs location = " + circularObs.getCenter());
//	log( "		   circObs radius = " + circularObs.getRadius());
	log( "         " );
*/
	ind1.setActionController(movementModel);
	ind1.setActionStrategy((LKFStrategy2)(((LKFModel2)(ind1.getActionController())).getNewStrategy()));
	LKFStrategy2 str1 = (LKFStrategy2) ind1.getActionStrategy();
	
/*	double maxOverlapSeverity = str1.calculateIndMaxSeverityOfOverlapWithIndividuals();
	double severityOfOverlapWithOverlapper = str1.severityOfOverlapWithInd(ind2);
	double severityOfOverlapWithCircObs = str1.severityOfOverlapWithInd(circularObs);
	if(approximatelyEqual(maxOverlapSeverity,severityOfOverlapWithOverlapper))
	{
		if(approximatelyEqual(maxOverlapSeverity, .05))
		pass = true;
	}
	
	Vector2D centroidal = new Vector2D();
	centroidal.x =-1*((ind1.getCenter()).x - (circularObs.getCenter()).x);
	centroidal.y = -1*((ind1.getCenter()).y - (circularObs.getCenter()).y);
*/	
	
	Vector2D d = str1.calculateIndOEMove_Sub(ind1, rectangularObs);
	double distanceMoved  = d.magnitude();
/*	
	log("		   expected overlap severity = " + 0);
	log("          maxOverlapSeverity = " + maxOverlapSeverity);
	log("		   severityOfOverlap with overlapper = " + severityOfOverlapWithOverlapper);
	log("		   severityOfOverlap with obstacle = " + severityOfOverlapWithCircObs);
	log("		   expected distance " + .13	);
	log("		   distance to overlapper = " + (ind1.distanceTo(ind2)).magnitude());
	log("		   distance to obstacle = " + (ind1.distanceTo(circularObs)).magnitude());
	log("          closestPoint on obstacle" + circularObs.getClosestPoint(ind1.getCenter(),centroidal));
*/	
	/*Case 2: individual to circular obstacle*/
	
	/*Case 3: individual to rectangluar obstacle*/
	
	
	double rectangularOverlap = str1.severityOfOverlapWithInd(rectangularObs);
	str1.calculateIndOEMove();
	Vector2D projectedPosition = str1.getIndProjectedPosition();
	
	/*let's see how many obstacles have been created*/
	//Bag obstacles = CrowdSimulation.getInstance().getTerrain().getAllObjects();
	//int numberOfObstacles = obstacles.size();
	
	log("RECTANGULAR OVERLAP: " + rectangularOverlap );
	log("distance to rectangle: " + ind1.distanceTo(rectangularObs).magnitude());
	log("OE: projected position " + projectedPosition);
	log("numberOfObstacles " + numberOfObstacles);
	
	return pass;
}



/**
 *Checks if projected movements (and velocities) are correct.
 *Considers three situations: individual overlapped by an obstacle, individual overlapped
 *the most by a stoned individual, and individual overlapped the most by another "unstoned"
 *individual.  
 *
 **/

public boolean calculatedOEMoveTests()
{	
	scenarioName = " OE movement calculation tests";
	boolean pass = false;
	
	LKFModel2 movementModel = new LKFModel2();
	String configName = "../configs/emptyWorld.xml";
	CrowdSimulation simState = new CrowdSimulation( (long)1.0,configName );

	/*Case 1: individual overlapped by an obstacle*/
	int entity_id1 = 1;
	HashMap parameters1 = new HashMap();
	parameters1.put("diameter", new Double(0.6));
	parameters1.put("location", new Vector2D(9.65, 10));
	Individual ind = new Individual( entity_id1, parameters1);
	
	int entity_id2 = 2;
	HashMap attributes1 = new HashMap();
	attributes1.put("center", new Vector2D(10, 10));
	attributes1.put("radius", new Double(.3) );
	attributes1.put("type", "Circular");
	CircularObstacle overlappingCircularObstacle1 =(CircularObstacle) Obstacle.build(entity_id2, attributes1);
	
	ind.setActionController(movementModel);
	ind.setActionStrategy((LKFStrategy2)(((LKFModel2)(ind.getActionController())).getNewStrategy()));
	LKFStrategy2 str = (LKFStrategy2) ind.getActionStrategy();
	log("		 severity of Overlap " + str.severityOfOverlapWithInd(overlappingCircularObstacle1));
	
	str.calculateIndOEMove();
	
	Vector2D finalLocation = str.getIndProjectedPosition();
	Vector2D expectedFinalLocation = new Vector2D(9.47,10);
	
	Vector2D finalVelocity = str.getIndProjectedVelocity();
	Vector2D expectedFinalVelocity = new Vector2D(0,0);
	
	log("         expected position due to OE " + expectedFinalLocation );
	log("		  actual position due to OE " + finalLocation);
	
	log("		  expected velocity due to OE " + expectedFinalVelocity );
	log("		  actual velocity due to OE " + finalVelocity);

	
	if(approximatelyEqual(expectedFinalLocation, finalLocation))
	{
		pass = true;
	}

/* Case 2: individual overlapped solely with other individuals*/
 log("individual overlapped solely by individuals");

	configName = "../configs/emptyWorld.xml";
	simState = new CrowdSimulation( (long)1.0,configName );

	entity_id1 = 1;
	parameters1 = new HashMap();
	parameters1.put("diameter", new Double(0.6));
	parameters1.put("location", new Vector2D(9.65, 10));
	Individual ind1 = new Individual( entity_id1, parameters1);
	
	entity_id2 = 2;
	HashMap parameters2 = new HashMap();
	parameters2.put("diameter", new Double(0.6));
	parameters2.put("location", new Vector2D(10, 10));
	Individual ind2 = new Individual( entity_id2, parameters2);

	int entity_id3 = 3;
	HashMap parameters3 = new HashMap();
	parameters3.put("diameter", new Double(0.6));
	parameters3.put("location", new Vector2D(10.34, 10));
	Individual ind3 = new Individual( entity_id3, parameters3);
	
	ind1.setActionController(movementModel);
	ind1.setActionStrategy((LKFStrategy2)(((LKFModel2)(ind1.getActionController())).getNewStrategy()));
	LKFStrategy2 str1 = (LKFStrategy2) ind1.getActionStrategy();

	ind2.setActionController(movementModel);
	ind2.setActionStrategy((LKFStrategy2)(((LKFModel2)(ind2.getActionController())).getNewStrategy()));
	LKFStrategy2 str2 = (LKFStrategy2) ind2.getActionStrategy();

	ind3.setActionController(movementModel);
	ind3.setActionStrategy((LKFStrategy2)(((LKFModel2)(ind3.getActionController())).getNewStrategy()));
	LKFStrategy2 str3 = (LKFStrategy2) ind3.getActionStrategy();

	/*Case 2, Scenario 1:Individual overlaps only nonstoned individuals*/
	str2.setIndStonedStatus(false);
	str2.calculateIndOEMove();

	Vector2D expectedPosition1 = new Vector2D(9.46,10);
	Vector2D expectedPosition2 = new Vector2D(10,10);
	Vector2D expectedPosition3 = new Vector2D(10,10);

	log("          ");
	log("          ind1 calculated projected position" + str1.getIndProjectedPosition());
	log("          ind2 calculated projected position" + str2.getIndProjectedPosition());
	log("		   ind3 calculated projected position" + str3.getIndProjectedPosition());
	log("		   ind1 expected projected position" + expectedPosition1);
	log("		   ind2 expected projected position" + expectedPosition2);
	log("		   ind3 expected projected position" + expectedPosition3);

	/*Case 2, Scenario 2: Individual most overlapped with a stoned individual*/
	






return pass;
}



/**
 *Checks if 
 *
 *
 **/
public boolean initiativeAndDependenceTests()
{	scenarioName = "";

	return true;
}

/**
 *Checks if status of the master overlap stack  
 *
 *@return boolean value true if all tests are satisfied.
 **/
public boolean masterOverlapStackTests()
{	
	scenarioName = "master overlap stack tests";
	boolean pass = false;
	
	/*create instance of crowd simulation*/
	String configName = "../configs/emptyWorld.xml";
	CrowdSimulation simState = new CrowdSimulation( (long)1.0,configName );

    /*create an instance of LKFModel to control individuals*/
    LKFModel2 movementModel = new LKFModel2();
	
	/*Case 1: cascadial impact of a stoned individual*/ 
	int entity_id1 = 1;
	HashMap parameters1 = new HashMap();
	parameters1.put("diameter", new Double(0.6));
	parameters1.put("location", new Vector2D(10, 10));
	Individual ind = new Individual( entity_id1, parameters1);
	ind.setActionController(movementModel);
		
	int entity_id2 = 2;
	HashMap parameters2 = new HashMap();
	parameters2.put("diameter", new Double(0.6));	 
	parameters2.put("location" , new Vector2D( 10, 10.43));
	Individual ind2 = new Individual( entity_id2, parameters2);
	ind2.setActionController(movementModel);
	
	int entity_id3 = 3;
	HashMap parameters3 = new HashMap();
	parameters3.put("diameter", new Double(0.6));
	parameters3.put("location", new Vector2D(10,9.52));
	Individual ind3 = new Individual( entity_id3, parameters3);
	ind3.setActionController(movementModel);
		
	int entity_id4 = 4;
	HashMap parameters4 = new HashMap();
	parameters4.put("diameter", new Double(0.6));	 
	parameters4.put("location" , new Vector2D(10.55,10));
	Individual ind4 = new Individual( entity_id4, parameters4);
	ind4.setActionController(movementModel);
	
	int entity_id5 = 5;
	HashMap parameters5 = new HashMap();
	parameters5.put("diameter", new Double(0.6));	 
	parameters5.put("location" , new Vector2D(11,11));
	Individual ind5 = new Individual( entity_id5, parameters5);
	ind5.setActionController(movementModel);
	LKFStrategy2 str5 = (LKFStrategy2) ind5.getActionStrategy();
	str5.setIndStonedStatus(true);
	
	Bag masterOverlapStack = movementModel.overlappedIndividuals();
	movementModel.overlapElim(ind, masterOverlapStack);	
	
	/*Case 2: cascadial impact of an obstacle*/
	return true;
}
public Bag returnMasterOverlapStack()
{
    return null;	
}

}
