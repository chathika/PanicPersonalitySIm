/*
 * $RCSfile: RectangularObstacle.java,v $ $Date: 2010/12/31 20:21:07 $
 */
package crowdsimulation.entities.obstacle;

import ec.util.*;
import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.entities.*;
import crowdsimulation.logging.*;
import java.awt.geom.*;
import java.awt.*;
import java.util.*;
import math.*;
import sim.engine.*;
import sim.field.continuous.*;
import sim.portrayal.*;
import sim.util.*;

/**
 * Represents a rectangular obstacle.
 * This object can be used to represent an obstacle, area of interest or a waypoint.
 * These obstacles can be constructed as attractive, repulsive, or indifferent.
 *
 * @see crowdsimulation.entities.obstacle.Obstacle
 * @author $Author: dkaup $
 * @version $Revision: 1.10 $
 * $State: Exp $
 * $Date: 2010/12/31 20:21:07 $
 **/
public class RectangularObstacle extends Obstacle
{
////////////////////////////////////////////////////////////////////////////////
// Attributes  
////////////////////////////////////////////////////////////////////////////////

	/** Represents the width of the obstacle. **/
   	private double width  = 0;
   	/** Represents the height of the obstacle. **/ 	
    private double height = 0;	
   	/** Represents the angle from the x-axis which the obstacle sets at, in a CW direction. **/
    private double angle = 0;	
    /** The magnitude of p12   **/
    public double mag12 = 0;
    /** The magnitude of p23   **/
    public double mag23 = 0;
    /** The magnitude of p34   **/
    public double mag34 = 0;
    /** The magnitude of p41   **/
    public double mag41 = 0;
    
    /** The first corner of the rectangle, upper left, before any rotation. **/
    public Vector2D p1;
    /** The second corner of the rectangle, going CW. **/
    public Vector2D p2;
    /** The third corner of the rectangle, going CW. **/
    public Vector2D p3;
    /** The fourth corner of the rectangle, going CW. **/
    public Vector2D p4;
    /** The center of the rectangle. **/
    public Vector2D pc;
    /** The following vectors are pij = pi - pj.   **/
    public Vector2D p12;
    public Vector2D p23;
    public Vector2D p34;
    public Vector2D p41;
    
    /** The location of the upper left corner, p1, of the rectangle, before rotation. **/
    private Vector2D location;
    
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
     * Constructs the rectangular obstacle from a upper left corner of the obstacle with the width and height.
	 * This Obstacle is aligned with the axes.
	 *
	 * @param terrain The terrain that this obstacle should be a part of.
	 * @param x The x component of the upper left corner of the obstacle.
	 * @param y The y component of the upper left corner of the obstacle.
	 * @param width The width of the obstacle.
	 * @param height The height of the obstacle.
	 **/
	public RectangularObstacle( Continuous2D terrain, double x, double y, double width, double height )
	{
		this( terrain, x, y, width, height, 0 );
	}
	
	/**
	 * Construction of a Rectangular Obstacle.
	 * 
	 * @param entity_id The unique identifier for all entities.
	 * @param parameters The parameters for the individual.
	 **/
	public RectangularObstacle( int entity_id, Map parameters ) 
	{
		super();
		setEntityID( entity_id );
		setParameters( parameters );
		initializePoints();
	}
	
	/**
     * Constructs the rectangular obstacle, given the upper left corner of the obstacle
     * and the width and height.
	 *
	 * @param terrain The terrain that this obstacle should be a part of.
	 * @param x The x component of the upper left corner of the obstacle.
	 * @param y The y component of the upper left corner of the obstacle.
	 * @param width The width of the obstacle.
	 * @param height The height of the obstacle.
	 * @param angle The angle which the obstacle should be off the x-axis, in a CW direction.
	 **/
	public RectangularObstacle( Continuous2D terrain, double x, double y, double width, double height, double angle )
	{
//	      double xc = x + Math.cos( angle )*(width/2) - Math.sin( angle )*(height/2);
//        double yc = y + Math.cos( angle )*(height/2) + Math.sin( angle )*(width/2);
		super( terrain, new Vector2D( x + Math.cos( angle )*(width/2) - Math.sin( angle )*(height/2),
                y + Math.cos( angle )*(height/2) + Math.sin( angle )*(width/2) ),
                new Vector2D( x, y));
        //Above "super" refers to:  Obstacle( Continuous2D terrain_val, Vector2D center, Vector2D location )
		this.width = width;
        this.height = height;
        this.angle = angle;
        
        initializePoints();
	}
	
	/**
	 * Initializes the points needed for display and obstacle interactions.
     *
     * The initial orientation of the rectangle is with the width horizonal and the height vertical.
     * The top left point is p1, and then one goes CCW from p2 to p3 to p4.
     * Then the final orientation of the rectangle is a CCW rotation of the angle theta about p1.
     *
     * The value of location() is the point p1, which, upon rotation, does not change.
     *
	 **/
	public void initializePoints()
	{
		double x = getLocation().x;
		double y = getLocation().y;

        p1 = new Vector2D( x, y );
        p2 = new Vector2D( (x+(Math.cos( angle )*width)), (y+(Math.sin( angle )*width)) );
        p3 = new Vector2D( p2.x-Math.sin( angle )*height, p2.y+Math.cos( angle )*height );
        p4 = new Vector2D( p3.x-Math.cos( angle )*width, p3.y-Math.sin( angle )*width );
        pc = new Vector2D( (p1.x + p3.x)/2., (p1.y + p3.y)/2.0 );  // The center of the rectangle.

        p12 = new Vector2D( p1.x - p2.x, p1.y - p2.y );
        p23 = new Vector2D( p2.x - p3.x, p2.y - p3.y );
        p34 = new Vector2D( p3.x - p4.x, p3.y - p4.y );
        p41 = new Vector2D( p4.x - p1.x, p4.y - p1.y );

        mag12 = p12.magnitude();
        mag23 = p23.magnitude();
        mag34 = p34.magnitude();
        mag41 = p41.magnitude();
	}

 	/**
 	 * Method to draw the obstacle on the GUI. 
 	 * This method needs to be overridden in any class subclassing from this class.
 	 *
 	 * @param object This object may be null;
 	 * @param graphics The graphic object which the obstacle should be drawn onto.
 	 * @param info The information about the graphics object to be drawn on.
 	 **/
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	{
		Color oldColor = graphics.getColor();
		
		graphics.setColor(Color.black);
        
        double scale = info.draw.width < info.draw.height ? info.draw.width : info.draw.height;
        
        Polygon poly = new Polygon();
        
        Point p_1  = new Point( (int)info.draw.x, (int)info.draw.y );
        Point p_2  = new Point( (int)(info.draw.x+( p2.x-p1.x )*info.draw.width), (int)(info.draw.y+( p2.y-p1.y )*info.draw.height) );
        Point p_3  = new Point( (int)(info.draw.x+( p3.x-p1.x )*info.draw.width), (int)(info.draw.y+( p3.y-p1.y )*info.draw.height) );
        Point p_4  = new Point( (int)(info.draw.x+( p4.x-p1.x )*info.draw.width), (int)(info.draw.y+( p4.y-p1.y )*info.draw.height) );
        
        poly.addPoint( p_1.x, p_1.y );
        poly.addPoint( p_2.x, p_2.y );
        poly.addPoint( p_3.x, p_3.y );
        poly.addPoint( p_4.x, p_4.y );
        
		graphics.setColor(Color.black);
		graphics.fill( poly );

		if( isDisplayCenter() )        
		{
			// Determine the radius.
			double radius = width/8;
			if( height/8 < radius ) radius = height/8;
			
			// Determine the shape using the calculated radius.
			Shape center = new Ellipse2D.Double
				( 
					info.draw.x+(( getCenter().x-p1.x-radius )*info.draw.width), 
					info.draw.y+(( getCenter().y-p1.y-radius )*info.draw.height), 	
					radius*2*info.draw.width,
					radius*2*info.draw.height
			 	); 
			
			// Draw a red circle shape at the center of the rectangle.
			graphics.setColor( Color.red );
			graphics.fill( center );
		}

		graphics.setColor( oldColor );
	}


	/**
	 * Returns a point of intersection between a given ray and this obstacle.
	 * The point should be on the rectangle.
	 *
	 * @param origin The origin of the ray to test the intersection with.
	 * @param direction The direction of the ray to test the intersection with. This does not have to be normalized.
	 * @return The point of intersection. If no intersection is found then it returns (-1,-1).
	 **/
	public Vector2D getClosestPoint( Vector2D origin, Vector2D direction )
	{
		Vector2D closestPoint;        // Closest point - value returned.
        Vector2D point;               // temporary point being investigated.
        Vector2D lineDirection;       // Line Segment (u,v)-vector
        Vector2D lineLength;

        double u = Math.cos( angle );
        double v = Math.sin( angle );
        double closestDistance = -1.0;
        double distance = -1.0;

        // *** Initialize the closest point to a null value (no intersection so far).
        closestPoint = new Vector2D( -1.0, -1.0 );

       	// *** Get location and vector for the first line segment, then
       	// get intersection with the ray (-1, -1) indicates no intersection, then
       	// if this point is closer than the current closest point, swap them out.
		//***broken in the next block!
       	lineDirection = new Vector2D( u, v );
       	lineLength = new Vector2D( p2.x-p1.x, p2.y-p1.y );
       	point = getIntersection( p1, lineDirection, origin, direction, lineLength.magnitude() );
       	if( point.x >= 0 )
       	{
			closestPoint = new Vector2D( point.x, point.y );
         	closestDistance = (closestPoint.x - origin.x )*(closestPoint.x - origin.x ) + (closestPoint.y - origin.y )*(closestPoint.y - origin.y );
       	}

       	// *** Get location and vector for the second line segment.
       	// Then get intersection with the ray.  (-1, -1) indicates no intersection.
       	// Then if this point is closer than the current closest point, swap them out.
        //
       	lineDirection = new Vector2D( -v, u );
       	lineLength = new Vector2D( p3.x-p2.x, p3.y-p2.y );
       	point = getIntersection( p2, lineDirection, origin, direction, lineLength.magnitude() );

       	if (point.x >= 0 )
       	{
         	distance = (point.x - origin.x )*(point.x - origin.x ) + (point.y - origin.y )*(point.y - origin.y );
         	if (closestPoint.x < 0 || distance < closestDistance )
         	{
           		closestPoint = new Vector2D( point.x, point.y );
           		closestDistance = distance;
         	}
       	}

       	// *** Get location and vector for the third line segment, then
       	// get intersection with the ray (-1, -1) indicates no intersection, then
       	// if this point is closer than the current closest point, swap them out.
       	lineDirection = new Vector2D( -u, -v );
       	lineLength = new Vector2D( p4.x-p3.x, p4.y-p3.y );
       	point = getIntersection( p3, lineDirection, origin, direction, lineLength.magnitude() );

       	if (point.x >= 0 )
       	{
         	distance = (point.x - origin.x )*(point.x - origin.x ) + (point.y - origin.y )*(point.y - origin.y );
         	if (closestPoint.x < 0 || distance < closestDistance )
         	{
           		closestPoint = new Vector2D( point.x, point.y );
           		closestDistance = distance;
         	}
       	}

       	// *** Get location and vector for the fourth line segment, then
       	// get intersection with the ray (-1, -1) indicates no intersection, then
       	// if this point is closer than the current closest point, swap them out.

       	lineDirection = new Vector2D( v, -u );
       	lineLength = new Vector2D( p1.x-p4.x, p1.y-p4.y );
       	point = getIntersection( p4, lineDirection, origin, direction, lineLength.magnitude() );

       	if (point.x >= 0 )
       	{
         	distance = (point.x - origin.x )*(point.x - origin.x ) + (point.y - origin.y )*(point.y - origin.y );
         	if (closestPoint.x < 0 || distance < closestDistance )
         	{
           		closestPoint = new Vector2D( point.x, point.y );
         	}
       	}
		return closestPoint;
	}

    /**
     * Checks for intersection between a line segment and a ray.
     * Given a line segment and a ray, determines the intersection of them.
	 * lo and lv gives the line origin and (u,v) vector, respectively.
     * ro and rv give the rays origin and (u,v) vector, respectiveley.
     *
     * @param lo The origin of the line segment.
     * @param lv The direction of the line segment.
     * @param ro The origin of the ray.
     * @param rv The direction of the ray.
     * @param len The length of the line segment.
     * @return point of Intersection.
     **/
    public Vector2D getIntersection(Vector2D lo, Vector2D lv, Vector2D ro, Vector2D rv, double len )
    {
        Vector2D normal = new Vector2D( -lv.y, lv.x );
        Vector2D intersection = new Vector2D( -1.0, -1.0 );

        // First check out the parallel line cases...
        if (lv.x == 0 && rv.x == 0 )  { return intersection; }
        if (lv.y == 0 && rv.y == 0 )  { return intersection; }

        if (lv.y != 0 && rv.y != 0 )
        {
        	if ( lv.x/lv.y == rv.x/rv.y ) { return intersection; }
        }

        if ( (lv.y*rv.x - lv.x*rv.y) == 0 ) { return intersection; }

		double t = (normal.dot( lo )-normal.dot( ro ))/normal.dot( rv );
		if( t < 0 ) { return intersection; };

		double px = ro.x+rv.x*t;
		double py = ro.y+rv.y*t;

		Vector2D dist = new Vector2D( px-lo.x, py-lo.y );
		Vector2D dir = new Vector2D( dist.x*lv.x, dist.y*lv.y );

		if( dist.magnitude() > len || dir.x < 0 || dir.y < 0 ) { return intersection; };
		Vector2D disp = new Vector2D( px, py );

		return new Vector2D( px, py );
	}

	/**
	 * Returns the normal vector at a given point on the obstacle.
	 *
	 * @param point The Point on the obstacle for which the normal is being found.
	 * @return The normal to the surface of the obstacle.
	 **/
	public Vector2D getNormalAt( Vector2D point )
	{
		Vector2D u = new Vector2D( Math.cos( angle ), Math.sin( angle ) );
		Vector2D v = new Vector2D( Math.cos( angle+Math.PI/2 ), Math.sin( angle+Math.PI/2 ) );
		Vector2D dir;
        // First to check if Point on a corner.
		if( approximatelyEqual( point, p1 ) )
		{
			return new Vector2D( Math.cos( angle-3*Math.PI/4 ), Math.sin( angle-3*Math.PI/4 ), true );
		}
		else if( approximatelyEqual( point, p2 ) )
		{
			return new Vector2D( Math.cos( angle-Math.PI/4 ), Math.sin( angle-Math.PI/4 ), true );
		}
		else if( approximatelyEqual( point, p3 ) )
		{
			return new Vector2D( Math.cos( angle+Math.PI/4 ), Math.sin( angle+Math.PI/4 ), true );
		}
		else if( approximatelyEqual( point, p4 ) )
		{
			return new Vector2D( Math.cos( angle+3*Math.PI/4 ), Math.sin( angle+3*Math.PI/4 ), true );
		}
        //  Now to check if it lies on one of the edges.
		dir = new Vector2D( p2.x-p1.x, p2.y-p1.y );
		double t = (point.x-p1.x)/dir.x;                        // if theta = +- Math.PI/2, then dir.x = 0.   !!
		if( approximatelyEqual( point.y , p1.y + dir.y*t ) )
		{
            // This condition can be satisfied if point is anywhere along the two lines extended from p12.
            // One also has to require that (point - p1) be in the direction of p12 and its magnitude be less than mag12.
            // The return value is correct if above is so.
            // If this method is preceded by getClosestPoint( Vector2D, Vector2D ), then these conditions will be satisfied.
            //
			return new Vector2D( Math.cos( angle-Math.PI/2 ), Math.sin( angle-Math.PI/2 ) );
		}

		dir = new Vector2D( p3.x-p2.x, p3.y-p2.y );
		t = (point.y-p2.y)/dir.y;
		if( approximatelyEqual( point.x, p2.x + dir.x*t ) )
		{
			return new Vector2D( Math.cos( angle ), Math.sin( angle ) );
		}

		dir = new Vector2D( p4.x-p3.x, p4.y-p3.y );
		t = (point.x-p3.x)/dir.x;

		if( approximatelyEqual( point.y, p3.y + dir.y*t ) )
		{
			return new Vector2D( Math.cos( angle+Math.PI/2 ), Math.sin( angle+Math.PI/2 ) );
		}

		dir = new Vector2D( p1.x-p4.x, p1.y-p4.y );
		t = (point.y-p4.y)/dir.y;
		if( approximatelyEqual( point.x, p4.x + dir.x*t ) )
		{
			return new Vector2D( Math.cos( angle+Math.PI ), Math.sin( angle+Math.PI ) );
		}

		return new Vector2D( 0, 0 );
	}

	/**
	 * Returns true if the point is inside the obstacle and false if the point is on the boundary or outside the obstacle.
	 *
	 * @param point The point being tested if it is inside the obstacle.
	 * @return  A boolean is returned indicating if the given point is inside the obstacle.
	 **/
	public boolean isInside( Vector2D point )
	{
        // The "areas" of triangles p1 p2 point, p2 p3 point, p3 p4 point,
		// and p4 p1 point must all be positive for the point to be inside
		// the rectangle.
		if	(
				positiveArea( p1, p2, point ) &&
				positiveArea( p2, p3, point ) &&
				positiveArea( p3, p4, point ) &&
				positiveArea( p4, p1, point )
			)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
     * This creates a clone of the current obstacle.
     * The clone will contain all the same information as the original object.
     *
     * @return A new instance containing the information of the original object.
     **/
    public Object clone()
    {
    	return new RectangularObstacle( null, this.getLocation().x, this.getLocation().y, width, height, angle );
    }
	
	/**
	 *	Three points, in CW order, in a left-handed (LH) coordinate system, form a triangle with positive area, as calculated using
	 *    	1 | x0 y0 1 |
	 *	A = - | x1 y1 1 |.  
	 *	    2 | x2 y2 1 |
	 *
     * In terms of cross products in a LH system with CW rotation positive, this is the same as:
     * 2*A = (p2 - p3) x (p3 - p1) = p2 x p3 - p1 x (p3 - p2)
     *
     * In a left-handed (LH) coordinate system, the sign of the area will be the opposite of that for the RH system. So to maintain
     * a positive area, we have to take a CW sense of rotation as positive in a LH coordinate system.
     *
	 * @param point1 The object which contains the values for x0 and y0.
	 * @param point2 The object which contains the values for x1 and y1.
	 * @param point3 The object which contains the values for x2 and y2.
	 * @return  A boolean which indicates if the three points form a triangle with positive, nonzero area.
	 **/
    private boolean positiveArea(Vector2D point1, Vector2D point2, Vector2D point3)
	{
		// We can neglect the leading term, 1/2, because we only care about the sign
		double determinant = (point2.x*point3.y - point2.y*point3.x -point1.x*point3.y + point1.y*point3.x + point1.x*point2.y - point1.y*point2.x);
		return (determinant > 0);
	}
	
	/**
	 * Sets all the parameters from a key value pairs collection.
     *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{		

        super.setParameters( parameters );

        if( parameters.containsKey( "force" ) )
		{
			Object value = parameters.get( "force" );
			double val = 0;
			
			if( value instanceof String )
			{
				val = Double.parseDouble( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Double)value).doubleValue();
			}
			else
			{
				Log.log( 1, " Force for Rectangular Obstacle construction must be a Double or a string representing a Double.");
			}
			setForce( val );
		}
		if( parameters.containsKey( "terrain" ) )
		{
			setTerrain( (Continuous2D)parameters.get( "terrain" ) );
		}
	
		if( parameters.containsKey( "width" ) )
		{
			Object value = parameters.get( "width" );
			double val = 0;
			
			if( value instanceof String )
			{
				val = Double.parseDouble( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Double)value).doubleValue();
			}
			else
			{
				Log.log( 1, " Width for Rectangular Obstacle construction must be a Double or a string representing a Double.");
			}
			setWidth( val );
		}
		if( parameters.containsKey( "height" ) )
		{
			Object value = parameters.get( "height" );
			double val = 0;
			
			if( value instanceof String )
			{
				val = Double.parseDouble( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Double)value).doubleValue();
			}
			else
			{
				Log.log( 1, " Height for Rectangular Obstacle construction must be a Double or a string representing a Double.");
			}
			setHeight( val );
		}
		if( parameters.containsKey( "angle" ) )
		{
			Object value = parameters.get( "angle" );
			double val = 0;
			
			if( value instanceof String )
			{
				val = Double.parseDouble( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Double)value).doubleValue();
			}
			else
			{
				Log.log( 1, " Angle for Rectangular Obstacle construction must be a Double or a string representing a Double.");
			}
			setAngle( val );			
		}
		if( parameters.containsKey( "location" ) )
		{
			Vector2D loc = (Vector2D)parameters.get( "location" );
			setLocation( loc );  // The location is the same as p1, which is the upper left corner before rotation.
            // A positive angle is a CW rotation.
            //We also set the Center at this time, to ensure consistency.
			double x = loc.x + Math.cos( angle )*(width/2) - Math.sin( angle )*(height/2);
            double y = loc.y + Math.cos( angle )*(height/2) + Math.sin( angle )*(width/2);
            setCenter( new Vector2D( x, y ) );
		}
		if( parameters.containsKey( "center" ) ) 
		{
			Vector2D center = (Vector2D)parameters.get( "center" );
			setCenter( center );
			double x = center.x;
			double y = center.y;
   // To get the location from the center, we just reverse the previous.
     		double x1 = x - Math.cos( angle )*(width/2) + Math.sin( angle )*(height/2);
            double y1 = y - Math.cos( angle )*(height/2) - Math.sin( angle )*(width/2);
            setLocation( new Vector2D( x1, y1 ) );
		}
		if( parameters.containsKey( "displayCenter" ) )
		{
			Object value = parameters.get( "displayCenter" );
			boolean val = false;
			
			if( value instanceof String )
			{
				val = Boolean.getBoolean( (String)value );
			}
			else if( value instanceof Boolean )
			{
				val = ((Boolean)value).booleanValue();
			}
			else
			{
				Log.log( 1, " displayCenter for Rectangular Obstacle construction must be a Boolean or a string representing a Boolean.");
			}
			setDisplayCenter( val );			
		}
		
		initializePoints();
	}
	
	/**
	 * This method is needed in order to "inspect" the object in Mason.
	 * This allows for clicking on the object in the GUI and getting the current information about the 
	 * attributes and state of the object.
	 *
	 * @param object This May be null.
	 * @param info The information to tell what scaling, if any, was done on the display.
	 * @return A boolean representing if the object was hit.
	 **/
	public boolean hitObject(Object object, DrawInfo2D info)
    {
	    final double SLOP = 1.0;  // need a little extra diameter to hit circles
	    Rectangle2D.Double rect = new Rectangle2D.Double();
	    
	    Polygon poly = new Polygon();
        // Equations for the points have been revised so as to be in CW order.
        // See the method initializePoints() for the equations used.
        Point p_1  = new Point( (int)info.draw.x, (int)info.draw.y );
        Point p_2  = new Point( (int)(info.draw.x+( p2.x-p1.x )*info.draw.width), (int)(info.draw.y+( p2.y-p1.y )*info.draw.height) );
        Point p_3  = new Point( (int)(info.draw.x+( p3.x-p1.x )*info.draw.width), (int)(info.draw.y+( p3.y-p1.y )*info.draw.height) );
        Point p_4  = new Point( (int)(info.draw.x+( p4.x-p1.x )*info.draw.width), (int)(info.draw.y+( p4.y-p1.y )*info.draw.height) );

// The equations from the previous version are below.
//        Point p_2  = new Point( (int)(info.draw.x+Math.cos( angle )*width*info.draw.width), (int)(info.draw.y+Math.sin( angle )*height*info.draw.height) );
//        Point p_3  = new Point( (int)(p_2.x+Math.cos( angle+Math.PI/2 )*width*info.draw.width), (int)(p_2.y+Math.sin( angle+Math.PI/2 )*height*info.draw.height) );
//        Point p_4  = new Point( (int)(info.draw.x+Math.cos( angle+Math.PI/2 )*width*info.draw.width), (int)(info.draw.y+Math.sin( angle+Math.PI/2 )*height*info.draw.height) );

        poly.addPoint( p_1.x, p_1.y );
        poly.addPoint( p_2.x, p_2.y );
        poly.addPoint( p_3.x, p_3.y );
        poly.addPoint( p_4.x, p_4.y );
	    
	    return ( poly.intersects( info.clip.x, info.clip.y, info.clip.width, info.clip.height ) );
    }	
    	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the width of the obstacle.
	 *
	 * @return The width of the obstacle.
	 **/
	public double getWidth()
    {
		return width;
	}

	/**
	 * Sets the width of the obstacle.
	 *
	 * @param val The width of the obstacle.
	 **/
	public void setWidth( double val )
	{
		this.width = val;
    }

	/**
	 * Gets the height of the obstacle.
	 *
	 * @return The height of the obstacle.
	 **/
	public double getHeight()
	{
		return height;
	}

	/**
	 * Sets the height of the obstacle.
	 *
	 * @param val The height of the obstacle.
	 **/
	public void setHeight( double val )
	{
		this.height = val;
	}

	/**
	 * Gets the angle of the obstacle to the x axis.
	 *
	 * @return The angle of inclination of the obstacle.
	 **/
	public double getAngle()	
	{
		return angle;
	}
	
	/**
	 * Sets the angle of the obstacle to the x axis.
	 *
	 * @param val The angle of inclination of the obstacle.
	 **/
	public void setAngle( double val )
	{
		angle = val*Math.PI/180;
	}
	
	/**
	 * Gets the location of the obstacle.
	 *
	 * @return The location of the obstacle.
	 **/
	public Vector2D getLocation()
	{
		return location;
	}
	
	/**
	 * Sets the location of the obstacle.
	 *
	 * @param val The location of the obstacle.
	 **/
	public void setLocation( Vector2D val )
	{
		location = val;
	}
}


