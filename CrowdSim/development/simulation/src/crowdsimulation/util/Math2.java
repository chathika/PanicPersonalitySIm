/*
 * $RCSfile: Math2.java,v $ $Date: 2010/12/09 20:44:47 $
 */
package crowdsimulation.util;

import java.lang.reflect.*;
import math.*;

/** 
 * Helper class to deal with mathematical problems of finding intercepts. 
 *
 * @author $Author: dkaup $
 * @version $Revision: 1.1 $
 * $State: Exp $
 * $Date: 2010/12/09 20:44:47 $
 **/

public class Math2
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
	
	/** The amount of error allowed when comparing two numbers. **/
    private double ERROR = 0.0000001;


	/** 
	 * A comparison between two vector to see if they are similar.  Each
     * component must be within ERROR of each other to pass the test.
	 *
	 * @param a The first vector for the comparison.
	 * @param b The second vector for the comparison.
	 * @return The boolean representing if the two vectors are close enough to be considered the same.
	 **/
	public boolean approximatelyEqual( Vector2D a, Vector2D b )
	{
		return ( Math.abs(a.x - b.x) < ERROR && Math.abs(a.y - b.y) < ERROR );
	}
	
	/** 
	 * A comparison between two doubles to see if they are similar.
	 *
	 * @param a The first double for the comparison.
	 * @param b The second double for the comparison.
	 * @return The boolean representing if the two doubles are close enough to be considered the same.
	 **/
	public boolean approximatelyEqual( double a, double b )
	{
		return Math.abs(a - b) < ERROR;
	}
	
	/** 
	 * A comparison between two floats to see if they are similar.
     *
	 * @param a The first float for the comparison.
	 * @param b The second float for the comparison.
	 * @return The boolean representing if the two floats are close enough to be considered the same.
	 **/
	public boolean approximatelyEqual( float a, float b )
	{
		return Math.abs(a - b) < ERROR;
	}
	
	/** 
	 * A comparison between two ints to see if they are similar.
	 * This is not necessary, but was implemented to match the other primitive number types.
	 *
	 * @param a The first int for the comparison.
	 * @param b The second int for the comparison.
	 * @return The boolean representing if the two ints are close enough to be considered the same.
	 **/
	public boolean approximatelyEqual( int a, int b )
	{
		return Math.abs(a - b) < ERROR;
	}

    /** Written by DJK on 10/17/10.
     *
     *  Calculates the intersection of a circle with a given line.
     *
     * @param double origin The source point of the line to be tested if it intersects the circle.
     * @param Vector2D dir The direction of the line to be tested.
     * @param Vector2D cen The center of the circle.
     * @param double capR The radius of the circle.
     * @return Vector2D If true, the closest point of intersection to origin. If no intersection, (-1,-1) is returned.
     **/
public Vector2D lineIntersectCircle( Vector2D origin, Vector2D dir, Vector2D cen, double capR  )
	{
    	// First we set up our vectors and their magnitudes.
    	double x1 = cen.x - origin.x;
    	double y1 = cen.y - origin.y;   // components of the vector r1.
        double r1sq = x1*x1 + y1*y1;
        double r1 = Math.sqrt(r1sq);

        Vector2D norm = dir.normalize();
        // Let us make sure that norm is directed from the origin toward "this".
        double dot = x1*norm.x + y1*norm.y;
        if( dot < 0.0 )
        {
            norm.x = - norm.x;
            norm.y = - norm.y;
        }
        // now we have norm pointing toward "this".
    	double x2 = norm.x;
    	double y2 = norm.y;
//
//  If the vector line intersects with the circle, then there is a vector beta*norm, of length beta, which goes from the
//  origin and through the two intersection points on the circle. Then the three vectors: r1, beta*norm and a radius vector,
//  capR, compose a triangle where the radius vector is capR = beta*norm - r1.   We use the following angles between these three
//  vectors ( CCW is positive):
//             theta = angle from r1 to n
//             alpha = angle from n to capR
//
//  With this convention and due to the left handed coordinate system, we have the following cross and dot products:
//
//             r1 x n = - r1*sin(theta),                 r1 dot n = r1*cos(theta),
//             n x capR = - capR*sin(alpha),             n dot capR = capR cos(alpha).
//
//  Letting capR = beta*norm - r1 in the last two equations gives
//
//             n x capR = r1 x n,                        n dot capR = beta - n dot r1.
//
//  We have the vectors r1 and n as known and given. The magnitude of capR is given. Then with these and the above, we have:
//
//             sin(theta) = - (r1 x n ) / r1,             cos(theta) = (r1 dot n) / r1,
//             sin(alpha) = r1*sin(theta) / capR,         cos(alpha) = +-sqrt( 1 - sin(alpha)*sin(alpha) ).
//
//  The solution for the intesections are:
//
//             beta = r1*cos(theta) + capR*cos(alpha),
//
//  where there are two possible signs for the value of cos(alpha), giving the two possible intersections.
//

        boolean aE = approximatelyEqual( r1/capR, 1.0 );
        if( r1 < capR && !aE  )
        {
            return new Vector2D( -1, -1 );  // since the origin is inside the circle.
        }
//
        double cr12 = x1*y2 - x2*y1;   // The cross product = - r1*sin(theta).
        double dot12 = x1*x2 + y1*y2;  // The dot product = r1*cos(theta).
        double salf = - cr12 / capR;
//
//  If salf^2 > 1, then there is no intersection.  But we still take it, if it is close.
//
        aE = approximatelyEqual(salf*salf, 1.0);
        double calf=0;

        if( salf*salf > 1.0 && !aE ) { return new Vector2D(-1, -1 ); }

        if( salf*salf < 1.0 && !aE )
        {
            calf = Math.sqrt(1.0 - salf*salf);

        }
        // we return the position of the closest intersection.
        double beta = dot12 - capR*calf;

        return new Vector2D( origin.x + beta*x2, origin.y + beta*y2 );
// This method is the same as getClosestPoint in CirObs, except for input variables.
    }

/** Written by DJK on 10/17/10
 *
 * Calculates the intersection point of an infinite line with a finite line segement.
 *
 * @param Vector2D origin The source point of the line to be tested if it intersects the line segment.
 * @param Vector2D dir The direction of the line to be tested.
 * @param Vector2D p1 One endpoint of the line segment.
 * @param Vector2D p2 The other endpoint of the line segment.
 * @return Vector2D If true, the interception point. If the two lines are parallel and overlay, origin is returned.  If false, (-1,-1) is returned.
 **/
public Vector2D lineIntersectSegment(Vector2D origin, Vector2D dir, Vector2D p1, Vector2D p2 )
{   // The vector equation of a line is: line = t*dir + origin where t is a scalar parameter.
    // For the line segment, the vector equation is: segment = s*(p2 - p1) + p1 where s is another parameter.
    // Setting these two equal gives that the intesection point is where:
    //    s = (origin - p1)x(dir) / (p2-p1)x(dir)
    // If the two directions are parallel, then (p2-p1)x(dir) = 0 and if the lines do
    // intersect, then they would be identical lines, in which case, (dir)x(origin - p1)=0 also.

        double crorgdir = (origin.y - p1.y)*dir.x - (origin.x - p1.x)*dir.y;
        double cr12dir = (p2.y - p1.y)*(dir.x) - (p2.x - p1.x)*(dir.y);
        double mag12 = Math.sqrt( (p2.x - p1.x)*(p2.x - p1.x) + (p2.y - p1.y)*(p2.y - p1.y) );
        double magdir = Math.sqrt( (dir.x)*(dir.x) + (dir.y)*(dir.y) );
        double magorp1 = Math.sqrt( (origin.x - p1.x)*(origin.x - p1.x) + (origin.y - p1.y)*(origin.y - p1.y) );
        double testorgdir = crorgdir / ( magorp1*magdir )/100.0;
        // The factor of 100.00 is to ensure that if the test is positive, tnen one has at least 2 significant digits.
        double testdir = cr12dir / ( mag12*magdir )/100.0;
        {   if( approximatelyEqual(testdir, 0.0 ))
            {   if( approximatelyEqual( testorgdir, 0.0 ))
                {
                   return origin;
                   // The lines are essentially parallel and essentially do overlay.
                }
                else
                {
                   return new Vector2D(-1,-1);
                   // The lines are parallel and never intersect.
                }
            }
        else
        // The lines are not parallel. Now to construct the intersection.
            {
                double ss = - crorgdir / cr12dir;
                {   if( ss >=0.0 && ss <= 1.0 )  //Necessary in order to be between p1 and p2.
                    {
                        double interx = ss*(p2.x-p1.x) + p1.x;
                        double intery = ss*(p2.y-p1.y) + p1.y;
                        return new Vector2D( interx, intery );
                    }
                    else
                    {
                        return new Vector2D(-1,-1);  // The intersection is outside of the line segment
                    }
                }
            }
        }
}

    /** Written by DJK on 10/17/10.
     *
     *  Calculates the intersection of an arc of a circle with a given line.
     *
     * @param double origin The source point of the line to be tested to see if it intersects the arc of the circle.
     * @param Vector2D dir The direction of the line to be tested.
     * @param Vector2D cen The center of the circle of the arc.
     * @param double capR The radius of the circle of the arc.
    * @return Vector2D If true, the closest point of intersection of the arc to origin. If false, (-1,-1).
     **/
public Vector2D lineIntersectArc( Vector2D origin, Vector2D dir, Vector2D cen, double capR, double angorg, double delang  )
{
    // This is the same as the method used in lineIntersectCircle, except we also check on the angular locations.
    // Our convention is that the arc starts at angorg and rotates in the postive direction (CCW) through the angle delang.
    // The units used on these angles are degrees. We assume that 180 > angorg >= -180.
        // Now to set up the vectors.
        double x1 = cen.x - origin.x;
    	double y1 = cen.y - origin.y;   // components of the vector r1.
        double r1sq = x1*x1 + y1*y1;
        double r1 = Math.sqrt(r1sq);

        Vector2D norm = dir.normalize();
        // Let us make sure that norm is directed from the origin toward "this".
        double dot = x1*norm.x + y1*norm.y;
        if( dot < 0.0 )
        {
            norm.x = - norm.x;
            norm.y = - norm.y;
        }
        // now we have norm pointing toward "this".
    	double x2 = norm.x;
    	double y2 = norm.y;
//
//  If the vector line intersects with the circle, then there is a vector beta*norm, of length beta, which goes from the
//  origin and through the two intersection points on the circle. Then the three vectors: r1, beta*norm and a radius vector,
//  capR, compose a triangle where the radius vector is capR = beta*norm - r1.   We use the following angles between these three
//  vectors ( CCW is positive):
//             theta = angle from r1 to n
//             alpha = angle from n to capR
//
//  With this convention and due to the left handed coordinate system, we have the following cross and dot products:
//
//             r1 x n = - r1*sin(theta),                 r1 dot n = r1*cos(theta),
//             n x capR = - capR*sin(alpha),             n dot capR = capR cos(alpha).
//
//  Letting capR = beta*norm - r1 in the last two equations gives
//
//             n x capR = r1 x n,                        n dot capR = beta - n dot r1.
//
//  We have the vectors r1 and n as known and given. The magnitude of capR is given. Then with these and the above, we have:
//
//             sin(theta) = - (r1 x n ) / r1,             cos(theta) = (r1 dot n) / r1,
//             sin(alpha) = r1*sin(theta) / capR,         cos(alpha) = +-sqrt( 1 - sin(alpha)*sin(alpha) ).
//
//  The solution for the intesections are:
//
//             beta = r1*cos(theta) + capR*cos(alpha),
//
//  where there are two possible signs for the value of cos(alpha), giving the two possible intersections.
//
        double cr12 = x1*y2 - x2*y1;   // The cross product = - r1*sin(theta).
        double dot12 = x1*x2 + y1*y2;  // The dot product = r1*cos(theta).
        double salf = - cr12 / capR;
//
//  If salf^2 > 1, then there is no intersection.  But we still take it, if it is close.
//
        boolean aE = approximatelyEqual(salf*salf, 1.0);
        double calf=0;

        if( salf*salf > 1.0 && !aE ) { return new Vector2D(-1, -1 ); }

        if( salf*salf < 1.0 && !aE )
        {
            calf = Math.sqrt(1.0 - salf*salf);
        }
// we calculate the position of both intersections.
        double beta1 = dot12 - capR*calf;
        double beta2 = dot12 + capR*calf;
        Vector2D capR1 = new Vector2D( beta1*x2 - x1, beta1*y2 - y1);
        Vector2D capR2 = new Vector2D( beta2*x2 - x1, beta2*y2 - y1);
        double theta1 = Math.atan2(capR1.y,capR1.x)*180/Math.PI;
        double theta2 = Math.atan2(capR2.y,capR2.x)*180/Math.PI;

// Now, first we need to have angorg and the theta's between -180 and +180.
        double newangorg = angorg;
        if( newangorg < -180. || newangorg > 180. )
        {
            double frac = newangorg/360.;
            double rint = Math.round(frac);
            newangorg = angorg - rint*360.;
        }
        double angf = newangorg + delang;
// If a theta is between -180 and newangorg, then the theta could still intersect
// with the arc if the theta + 360 was less than angf.  If the theta > newangorg,
// then in order to intersect the arc, theta must be < angf. Those are the only
// possibilities.
        boolean intsct1 = false;
        boolean intsct2 = false;
        {   if( theta1 < newangorg )
            {
                if( theta1 + 360. < angf ) { intsct1 = true; }
            }
            else
            {
                if( theta1 < angf ) { intsct1 = true; }
            }
        }

        {   if( theta2 < newangorg )
            {
                if( theta2 + 360. < angf ) { intsct2 = true; }
            }
            else
            {
                if( theta2 < angf ) { intsct2 = true; }
            }
        }
        double beta = 1.0;  // Just defining beta here.
        if( !intsct1 && !intsct2 )  { return new Vector2D( -1., -1.); }

        {   if( intsct1 && intsct2)
            {
                beta = Math.max(beta1, beta2);
                return new Vector2D( beta*x2 + origin.x, beta*y2 + origin.y );
            }
            else
            {
                if( intsct1 )  { beta = beta1; }
                else
                { beta = beta2; }
            }
        }

        return new Vector2D( origin.x + beta*x2, origin.y + beta*y2 );
    }
}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

