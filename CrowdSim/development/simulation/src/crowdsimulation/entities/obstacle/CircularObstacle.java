/*
 * $RCSfile: CircularObstacle.java,v $ $Date: 2010/12/31 20:16:49 $
 */
package crowdsimulation.entities.obstacle;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.entities.*;
import crowdsimulation.logging.*;
import ec.util.*;
import java.awt.geom.*;
import java.awt.*;
import java.util.*;
import math.*;
import sim.engine.*;
import sim.field.continuous.*;
import sim.portrayal.*;
import sim.util.*;

/**
 * Represents a circular obstacle.
 * This object can be used to create an obstacle, area of interest or a waypoint.
 * These obstacles can be constructed as attractive, repulsive, or indifferent.
 *
 * @see crowdsimulation.entities.obstacle.Obstacle
 * @author $Author: dkaup $
 * @version $Revision: 1.7 $
 * $State: Exp $
 * $Date: 2010/12/31 20:16:49 $
 **/
public class CircularObstacle extends Obstacle
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
	
	/** The radius of the circle. **/
	private double radius;
	/** The scale of the radius in the y direction. This is usefull in elongating the circle (elipse).**/
	private double yScale = 1;
	/** The scale of the radius in the x direction. This is usefull in elongating the circle (elipse).**/
	private double xScale = 1;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Construct a circular obstacle given a center point and the radius.
	 *
	 * @param terrain The terrain that this obstacle should be a part of.
	 * @param centerX The x component of the center of the obstacle.
	 * @param centerY The y component of the center of the obstacle.
	 * @param radius The radius of the obstacle.
	 **/	
	public CircularObstacle( Continuous2D terrain, double centerX, double centerY, double radius )
	{
		super(  terrain, new Vector2D( centerX, centerY ) );
		this.radius = radius;
	}
	
	
	/**
	 * Construct a circular obstacle given a center point and the radius.
	 *
	 * @param terrain The terrain that this obstacle should be a part of.
	 * @param centerX The x component of the center of the obstacle.
	 * @param centerY The y component of the center of the obstacle.
	 * @param radius The radius of the obstacle.
	 * @param params The Map containing all parameters to be set in the obstacle.
	 **/	
	public CircularObstacle( Continuous2D terrain, double centerX, double centerY, double radius, Map params )
	{
		super(  terrain, new Vector2D( centerX, centerY ), params );
		this.radius = radius;
	}
	
	
	/**
	 * Construction of a Circular Obstacle.
	 * 
	 * @param entity_id The unique identifier for all entities.
	 * @param parameters The parameters for the individual.
	 **/
	public CircularObstacle( int entity_id, Map parameters ) 
	{
		super();
		setEntityID( entity_id );
		setParameters( parameters );
	}


	/**
 	 * Method to draw the obstacle on the GUI. This method needs to be overridden in any class subclassing from this class.
 	 *
 	 * @param object This may be null.
 	 * @param graphics The graphic object which the obstacle should be drawn onto.
 	 * @param info The information about the graphics object to be drawn on.
 	 **/
	public void draw( Object object, Graphics2D graphics, DrawInfo2D info )
	{
		Color oldColor = graphics.getColor();		
        graphics.setColor( Color.black );
        
        Shape shape = new Ellipse2D.Double
        	( 
				info.draw.x-radius*info.draw.width, 
				info.draw.y-radius*info.draw.height, 
				2*radius*info.draw.width,
				2*radius*info.draw.height 
			);
			
        graphics.fill( shape );
		if( isDisplayCenter() )        
		{
	        double centerRadius = radius*info.draw.width/8;
			if( radius*info.draw.width/8 < centerRadius ) centerRadius = radius*info.draw.width/8;
			
			Shape center = new Ellipse2D.Double
				( 
					info.draw.x-centerRadius, 
					info.draw.y-centerRadius,
					centerRadius*2,
					centerRadius*2
			 	);
			
			graphics.setColor( Color.red );
			graphics.fill( center );
		}
		graphics.setColor( oldColor );
	}

	/**
	 * This method is needed in order to "inspect" the object in Mason.
	 * This allows loading in the attributes of the obstacle into a view window
	 * by clicking on the obstacle of interest in the GUI.
	 *
	 * @param object 
	 * @param info The object which describe the scaling, if any, used in displaying the obstacle.
	 * @return A boolean representing if the object was hit.
	 **/
	public boolean hitObject(Object object, DrawInfo2D info)
    {
	    final double SLOP = 1.0;  // need a little extra diameter to hit circles
	    Rectangle2D.Double rect = new Rectangle2D.Double();
	    
        Shape shape = new Ellipse2D.Double
        	( 
				info.draw.x-radius*info.draw.width, 
				info.draw.y-radius*info.draw.height, 
				2*radius*info.draw.width,
				2*radius*info.draw.height 
			);
	    
	    return ( shape.intersects( info.clip.x, info.clip.y, info.clip.width, info.clip.height ) );
    }

	/**
	 * Returns a point of intersection between a given ray and the circular obstacle.
	 * The point should be on the circle.
	 *
	 * @param origin The origin of the ray to test the intersection with.
	 * @param direction The direction of the ray to test the intersection with. This does not have to be normalized.
	 * @return The point of intersection. If no intersection is found then it returns (-1,-1).
	 **/
	public Vector2D getClosestPoint( Vector2D origin, Vector2D direction )
	{
    	Vector2D closestPoint = new Vector2D(-1,-1);           // Closest point - value returned.

    	double x1 = origin.x-getCenter().x;
    	double y1 = origin.y-getCenter().y;
    	double x2 = origin.x+2*direction.x-getCenter().x;
    	double y2 = origin.y+2*direction.y-getCenter().y;
    	double dx = x2-x1;
    	double dy = y2-y1;
    	double dr = Math.sqrt( dx*dx + dy*dy );
    	double D = x1*y2-x2*y1;

    	double discrim = (getRadius()*getRadius()*dr*dr)-(D*D);

    	if( discrim < 0 )
    	{
    		return closestPoint;
    	}

    	double sgn = 1;
    	if(dy<0) sgn = -1;

    	double int_x1 = (D*dy+sgn*dx*Math.sqrt(discrim))/(dr*dr);
    	double int_x2 = (D*dy-sgn*dx*Math.sqrt(discrim))/(dr*dr);

    	double int_y1 = (-1*D*dx+Math.abs(dy)*Math.sqrt(discrim))/(dr*dr);
    	double int_y2 = (-1*D*dx-Math.abs(dy)*Math.sqrt(discrim))/(dr*dr);

    	Vector2D tempPoint;
    	Vector2D tempDir;
		Vector2D dist1 = new Vector2D( int_x1-x1, int_y1-y1 );
		Vector2D dist2 = new Vector2D( int_x2-x1, int_y2-y1 );

		if( dist1.magnitude() < dist2.magnitude() )
		{
			tempDir = dist1;
			tempPoint = new Vector2D( int_x1, int_y1 );
		}
    	else
    	{
			tempDir = dist2;
			tempPoint = new Vector2D( int_x2, int_y2 );
		}

		if( ((tempDir.x > 0 && direction.x > 0) || (tempDir.x < 0 && direction.x < 0) || (tempDir.x == 0 && direction.x == 0) ) &&
		    ((tempDir.y > 0 && direction.y > 0) || (tempDir.y < 0 && direction.y < 0) || (tempDir.y == 0 && direction.y == 0)) )
		{
			closestPoint = new Vector2D( tempPoint.x+getCenter().x, tempPoint.y+getCenter().y );
		}

		return closestPoint;
    }

	/**
	 * Returns the normal vector at a given point on the obstacle.
	 *
	 * @param point The point on the obstacle for which the normal is being found.
	 * @return The normal to the surface of the obstacle.
	 **/
	public Vector2D getNormalAt( Vector2D point )
	{
		return new Vector2D( point.x-getCenter().x, point.y-getCenter().y, true );
	}

	/**
	 * Returns true if the point is inside the obstacle and false if the point
     * is on the edge or outside the obstacle.
	 *
	 *@param point The point being tested to see if it is inside the obstacle.
	 *@return  A boolean is returned indicating if the given point is inside the obstacle.
	 **/
	public boolean isInside( Vector2D point )
	{
		double distance = point.distance( this.getCenter() );

		if( distance < this.getRadius() )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

   	/**
     * Gives the capability to copy an obstacle.
     * This creates a new circular obstacle with all the same information as this object.
     *
     * @return A new instance 
     **/
    public Object clone()
    {
    	return new CircularObstacle( null, getCenter().x, getCenter().y, radius );
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
				Log.log( 1, " Force for Circular Obstacle construction must be a Double or a string representing a Double.");
			}
			setForce( val );
		}
		if( parameters.containsKey( "terrain" ) )
		{
			setTerrain( (Continuous2D)parameters.get( "terrain" ) );
		}
		if( parameters.containsKey( "center" ) )
		{
			Vector2D center = (Vector2D)parameters.get( "center" );
			setCenter( center );
		}
		if( parameters.containsKey( "radius" ) )
		{
			Object value = parameters.get( "radius" );
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
				Log.log( 1, " Radius for Circular Obstacle construction must be a Double or a string representing a Double.");
			}
			setRadius( val );
		}
		if( parameters.containsKey( "yScale" ) )
		{
			Object value = parameters.get( "yScale" );
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
				Log.log( 1, " yScale for Circular Obstacle construction must be a Double or a string representing a Double.");
			}
			setYScale( val );
		}
		if( parameters.containsKey( "xScale" ) )
		{
			Object value = parameters.get( "xScale" );
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
				Log.log( 1, " xScale for Circular Obstacle construction must be a Double or a string representing a Double.");
			}
			setXScale( val );
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
				Log.log( 1, " displayCenter for Circular Obstacle construction must be a Boolean or a string representing a Boolean.");
			}
			setDisplayCenter( val );			
		}
		
		if(this.getCenter() != null)
		{
			this.setLocation(this.getCenter());
		}
	}
	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Gets the radius of the circular obstacle.
	 *
	 * @return The raidus of the obstacle.
	 **/	
	public double getRadius()
	{
		return radius;
	}
	
	/**
	 * Sets the radius of the circular obstacle.
	 *
	 * @param val The raidus of the obstacle.
	 **/
	public void setRadius( double val )
	{
		this.radius = val;
	}
	
	/**
	 * Gets the yScale of the circular obstacle.
	 *
	 * @return The yScale of the obstacle.
	 **/	
	public double getYScale()
	{
		return yScale;
	}
	
	/**
	 * Sets the yScale of the circular obstacle.
	 *
	 * @param val The yScale of the obstacle.
	 **/
	public void setYScale( double val )
	{
		this.yScale = val;
	}
	
	/**
	 * Gets the xScale of the circular obstacle.
	 *
	 * @return The xScale of the obstacle.
	 **/	
	public double getXScale()
	{
		return xScale;
	}
	
	/**
	 * Sets the xScale of the circular obstacle.
	 *
	 * @param val The xScale of the obstacle.
	 **/
	public void setXScale( double val )
	{
		this.xScale = val;
	}
	
}
