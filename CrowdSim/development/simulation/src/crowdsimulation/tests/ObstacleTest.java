/*
 * $RCSfile: ObstacleTest.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.tests;

import crowdsimulation.entities.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import math.*;
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
public class ObstacleTest extends Test
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
	
	//RectangularObstacle rectangularObs;
	/** The continuous field whihc the obstacles should be placed in. **/
	Continuous2D field;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Exectues the Obstacle tests for the crowdSimulation System.
	 *
	 * @param args
	 */
	public static void main(String[] args) 
	{
		ObstacleTest tests = new ObstacleTest();
        Log systemLog = new Log( "../logs/SystemTest.dat" );
        systemLog.setFileOutput( false );
        systemLog.setLogLevel(0);
        
        tests.runTest();
	}
	
	
	/**
	 * Constructs the Obstacle test to test ray tracing aspects of the framework.
	 **/
	public ObstacleTest()
	{
		field = new Continuous2D( 10, 100, 100 );
		//rectangularObs = new RectangularObstacle( field, 10, 10, 10, 10 );
	}
	
	
	/**
	 * This executes the different scenarios for the test.
	 * The tests being run are:
	 *   1. circularTests()
	 *   2. rectangularTests()
	 **/
	public boolean executeScenarios()
	{
		boolean pass = true;
		pass = pass && circularTests();
		pass = pass && rectangularTests();
		return pass;		
	}
	
	
	/**
	 * Tests the circular obstacle features.
	 **/
	public boolean circularTests()
	{
		scenarioName = "circularTestFromOrigin";
		boolean pass = true;

//scenario parameters
		CircularObstacle circularObs = new CircularObstacle( field, 20, 20, 5 );

		scenarioHeader();
		log( "      CircularObstacle" );
		log( "         center = " + circularObs.getCenter() );
		log( "         radius = " + circularObs.getRadius() );
		log( "" );
		
////// Method testing segment		
		pass = pass && getClosestPoint( 
							circularObs, 
							new Vector2D( 0, 0 ), 
							new Vector2D( 1, 1, true ), 
							new Vector2D( 16.464466094067262, 16.464466094067262 ) );
		pass = pass && getNormalAt( 
							circularObs, 
							new Vector2D( 16.464466094067262, 16.464466094067262 ), 
							new Vector2D( -1, -1, true ) );
		
//end Method Testing section
		scenarioFooter( pass );
		return pass;
	}


	/**
	 * Tests the rectangular obstacle features.
	 **/
	public boolean rectangularTests()
	{
		scenarioName = "rectangularTestFromOrigin";
		boolean pass = true;
		boolean tempPass = true;

//scenario parameters
		RectangularObstacle rectangularObs = new RectangularObstacle( field, 10, 10, 10,10 );

		scenarioHeader();
		log( "      RectangularObstacle" );
		log( "         location = " + rectangularObs.getLocation() );
		log( "         width = " + rectangularObs.getWidth() );
		log( "         height = " + rectangularObs.getHeight() );
		log( "         angle = " + rectangularObs.getAngle() );
		log( "" );
		
////// Method testing segment		
		pass = pass && getClosestPoint( rectangularObs, new Vector2D( 0,0 ), new Vector2D( 1, 1, true), new Vector2D( 10.0, 10.0 ) );
		pass = pass && getNormalAt( rectangularObs, new Vector2D( 10, 10 ), new Vector2D( -1, -1, true ) );
		pass = pass && getNormalAt( rectangularObs, new Vector2D( 15, 10 ), new Vector2D( 0, -1, true ) );
		pass = pass && getNormalAt( rectangularObs, new Vector2D( 20, 10 ), new Vector2D( 1, -1, true ) );
		pass = pass && getNormalAt( rectangularObs, new Vector2D( 20, 15 ), new Vector2D( 1, 0, true ) );
		pass = pass && getNormalAt( rectangularObs, new Vector2D( 20, 20 ), new Vector2D( 1, 1, true ) );
		pass = pass && getNormalAt( rectangularObs, new Vector2D( 15, 20 ), new Vector2D( 0, 1, true ) );
		pass = pass && getNormalAt( rectangularObs, new Vector2D( 10, 20 ), new Vector2D( -1, 1, true ) );
		pass = pass && getNormalAt( rectangularObs, new Vector2D( 10, 15 ), new Vector2D( -1, 0, true ) );
		
		pass = pass && getIntersection( 
						rectangularObs,
						new Vector2D( 10, 10 ),  
						new Vector2D( 0, 1 ),
						new Vector2D( 0, 0 ),
						new Vector2D( 1, 1, true ),
						300,
						new Vector2D( 10, 10 ) );

		pass = pass && getClosestPoint( rectangularObs, new Vector2D( 30, 5 ), new Vector2D( -1, 1, true ), new Vector2D( 20.0, 15.0 ) );
		
		rectangularObs = new RectangularObstacle( field, 15, 75, 10,10 );

		scenarioHeader();
		log( "      RectangularObstacle" );
		log( "         location = " + rectangularObs.getLocation() );
		log( "         width = " + rectangularObs.getWidth() );
		log( "         height = " + rectangularObs.getHeight() );
		log( "         angle = " + rectangularObs.getAngle() );
		log( "" );

		pass = pass && isInside( rectangularObs, new Vector2D( 10, 15 ), false );
		
//scenario parameters
		rectangularObs = new RectangularObstacle( field, 10, 10, 5, 5, Math.PI/4 );

		scenarioHeader();
		log( "      RectangularObstacle" );
		log( "         location = " + rectangularObs.getLocation() );
		log( "         width = " + rectangularObs.getWidth() );
		log( "         height = " + rectangularObs.getHeight() );
		log( "         angle = " + rectangularObs.getAngle() );
		log( "" );
		
		pass = pass && getClosestPoint( rectangularObs, new Vector2D( 0,0 ), new Vector2D( 1, 1, true), new Vector2D( 10.0, 10.0 ) );		
		pass = pass && getClosestPoint( rectangularObs, new Vector2D( 11,0 ), new Vector2D( 0, 1, true), new Vector2D( 11.0, 10.999999999 ) );
		pass = pass && getClosestPoint( rectangularObs, new Vector2D( 15,12 ), new Vector2D( -1, 0, true), new Vector2D( 12.0, 12.0 ) );
		pass = pass && getClosestPoint( rectangularObs, new Vector2D( 15,15 ), new Vector2D( -1, 0, true), new Vector2D( 12.07106, 15.0 ) );
		pass = pass && getClosestPoint( rectangularObs, new Vector2D( 12,18 ), new Vector2D( 0, -1, true), new Vector2D( 12, 15.07106 ) );
		pass = pass && getClosestPoint( rectangularObs, new Vector2D( 17,11 ), new Vector2D( -1, 0, true), new Vector2D( 11, 11 ) );
		pass = pass && getClosestPoint( rectangularObs, new Vector2D( 9,18 ), new Vector2D( 0, -1, true), new Vector2D( 9, 16.07106 ) );
		pass = pass && getClosestPoint( rectangularObs, new Vector2D( 5,12 ), new Vector2D( 1, 0, true), new Vector2D( 7.9999999, 12 ) );
		pass = pass && getClosestPoint( rectangularObs, new Vector2D( 6,11 ), new Vector2D( 1, 0, true), new Vector2D( 9.0, 11.0 ) );		
		pass = pass && getClosestPoint( rectangularObs, new Vector2D( 0,11 ), new Vector2D( 1, 0, true), new Vector2D( 9.0, 11.0 ) );


		pass = pass && getNormalAt( rectangularObs, new Vector2D( 10.0, 10.0 ), new Vector2D( 0.0, -1 ) );		
		pass = pass && getNormalAt( rectangularObs, new Vector2D( 11.0, 11.0 ), new Vector2D( 1, -1, true ) );
		pass = pass && getNormalAt( rectangularObs, new Vector2D( 12.0, 12.0 ), new Vector2D( 1, -1, true ) );
		pass = pass && getNormalAt( rectangularObs, rectangularObs.getClosestPoint(new Vector2D( 12,18 ), new Vector2D( 0, -1, true)), new Vector2D( 1, 1, true ) );
		pass = pass && getNormalAt( rectangularObs, new Vector2D( 11, 11 ), new Vector2D( 1, -1, true ) );
		pass = pass && getNormalAt( rectangularObs, rectangularObs.getClosestPoint(new Vector2D( 9,18 ), new Vector2D( 0, -1, true)), new Vector2D( -1,1,true ) );
		pass = pass && getNormalAt( rectangularObs, rectangularObs.getClosestPoint(new Vector2D( 5,12 ), new Vector2D( 1, 0, true)), new Vector2D( -1, -1, true ) );
		pass = pass && getNormalAt( rectangularObs, new Vector2D( 9.0, 11.0 ), new Vector2D( -1, -1, true ) );
		pass = pass && getNormalAt( rectangularObs, rectangularObs.p1, new Vector2D( 0.0, -1, true ) );
		pass = pass && getNormalAt( rectangularObs, rectangularObs.p2, new Vector2D( 1, 0, true ) );
		pass = pass && getNormalAt( rectangularObs, rectangularObs.p3, new Vector2D( 0, 1, true ) );
		pass = pass && getNormalAt( rectangularObs, rectangularObs.p4, new Vector2D( -1, 0, true ) );
		
		Vector2D p1 = new Vector2D( 11,11 );
		Vector2D loc1_a = new Vector2D( 12, 8 );
		Vector2D dir1_a = new Vector2D( p1.x-loc1_a.x, p1.y-loc1_a.y );
		Vector2D loc1_b = new Vector2D( 14, 12 );
		Vector2D dir1_b = new Vector2D( p1.x-loc1_b.x, p1.y-loc1_b.y );
		Vector2D p2 = new Vector2D( 12.07106, 15.0 );
		Vector2D loc2_a = new Vector2D( 14, 8 );
		Vector2D dir2_a = new Vector2D( p2.x-loc2_a.x, p2.y-loc2_a.y );
		Vector2D loc2_b = new Vector2D( 14, 17 );
		Vector2D dir2_b = new Vector2D( p2.x-loc2_b.x, p2.y-loc2_b.y );		
		
		pass = pass && getTangentAt( rectangularObs, p1, dir1_a,  new Vector2D( 1, 1, true ) );
		pass = pass && getTangentAt( rectangularObs, p1, dir1_b,  new Vector2D( -1, -1, true ) );
//		pass = pass && getTangentAt( rectangularObs, p2, dir2_a,  new Vector2D( -1, 1, true ) );
//		pass = pass && getTangentAt( rectangularObs, p2, dir2_b,  new Vector2D( 1, -1, true ) );
//end Method Testing section
		scenarioFooter( pass );
		return pass;
	}

	/**
	 * Tests the methods for getting the closest point on an obstacle.
	 *
	 * @param obs The obstacle to get the closest point on.
	 * @param origin The origin of where the test ray came from.
	 * @param dir The direction of the test ray to check for intersection with the obstacle.
	 * @param expectedResult The result which is expected from this call. 
	 * @return The boolean representing if the method got the expected result.
	 **/
	private boolean getClosestPoint( Obstacle obs, Vector2D origin, Vector2D dir, Vector2D expectedResult )
	{
		//execute method
		Vector2D closestPoint = obs.getClosestPoint( origin, dir );
		boolean pass = approximatelyEqual( closestPoint, expectedResult );
		
		//display results
		methodHeader();
		log( "      getClosestPoint(  " + origin + " , " + dir + " ) " );
		log( "         return = " + closestPoint );
		log( "         correct return = " + expectedResult );
		log( "      " + result( pass ) + "::getClosestPoint" );
		methodFooter();

		return pass;
	}
	
	
	/**
	 * Tests the methods for getting the intersection on an obstacle.
	 *
	 * @param obs The obstacle to get the intersection of.
     * @param lo The origin of the line segment.
     * @param lv The direction of the line segment.
     * @param ro The origin of the ray.
     * @param rv The direction of the ray.
     * @param len The length of the line segment.
     * @param expectedResult The result which is expected from this call. 
	 * @return The boolean representing if the method got the expected result.
	 **/
	private boolean getIntersection
		( 
			RectangularObstacle obs, 
			Vector2D lo,  
			Vector2D lv,
			Vector2D ro,
			Vector2D rv,
			double len,
			Vector2D expectedResult
		)
	{
		//execute method
		Vector2D intersectionPt = obs.getIntersection( lo, lv, ro, rv, len );
		boolean pass = approximatelyEqual( intersectionPt, expectedResult);
		
		//display results
		methodHeader();
		log( "      getIntersection(  " + lo + ", " + lv + ", " + ro + ", " + rv + ", " + len + " ) " );
		log( "         return = " + intersectionPt );
		log( "         correct return = " + expectedResult );
		log( "      " + result( pass ) + "::getIntersection" );
		methodFooter();
		
		return pass;
	}
	
	
	/**
	 * Tests the methods for getting the normal vector on an obstacle.
	 *
	 * @param obs The obstacle to get the normal point on.
	 * @param point The point at which to find the normal.
	 * @param expectedResult The result which is expected from this call. 
	 * @return The boolean representing if the method got the expected result.
	 **/
	private boolean getNormalAt( Obstacle obs, Vector2D point, Vector2D expectedResult )
	{
		//execute method
		Vector2D normal = obs.getNormalAt( point );
		boolean pass = approximatelyEqual( normal, expectedResult);
		
		//display results
		methodHeader();
		log( "      getNormal(  " + point + " ) " );
		log( "         return = " + normal );
		log( "         correct return = " + expectedResult );
		log( "      " + result( pass ) + "::getNormal" );
		methodFooter();

		return pass;
	}


	/**
	 * Tests the methods for getting the tangent vector on an obstacle.
	 *
	 * @param obs The obstacle to get the tangent point on.
	 * @param point The point at which to find the tangent.
	 * @param orientation The orientation with which to align the tangent.
	 * @param expectedResult The result which is expected from this call. 
	 * @return The boolean representing if the method got the expected result.
	 **/
	private boolean getTangentAt( Obstacle obs, Vector2D point, Vector2D orientation, Vector2D expectedResult )
	{
		//execute method
		Vector2D tangent = obs.getTangentAt( point, orientation );
		boolean pass = approximatelyEqual( tangent, expectedResult);
		
		//display results
		methodHeader();
		log( "      getTangent(  " + point + " ) " );
		log( "         return = " + tangent );
		log( "         correct return = " + expectedResult );
		log( "      " + result( pass ) + "::getNormal" );
		methodFooter();

		return pass;
	}

	
	/**
	 * Tests the methods for checking to see if a point is inside the obstacle.
	 *
	 * @param obs The obstacle to check the point against.
	 * @param point The point to check.
	 * @param expectedResult The result which is expected from this call. 
	 * @return The boolean representing if the method got the expected result.
	 **/
	private boolean isInside( Obstacle obs, Vector2D point, boolean expectedResult )
	{
		//execute method
		boolean isInside = obs.isInside( point );
		boolean pass = isInside == expectedResult;
		
		//display results
		methodHeader();
		log( "      isInside(  " + point + " ) " );
		log( "         return = " + isInside );
		log( "         correct return = " + expectedResult );
		log( "      " + result( pass ) + "::getNormal" );
		methodFooter();

		return pass;
	}
	
}
