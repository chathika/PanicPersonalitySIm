/*
 * $RCSfile: Individual.java,v $ $Date: 2011/01/31 17:00:20 $
 */
package crowdsimulation.entities.individual;

import ec.util.*;
import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.actioncontroller.strategy.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import math.*;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import sim.portrayal.simple.*;
import sim.portrayal.*;

/**
 * This class represents an individual in the simulation.
 * An instance of this class is used to represent a person which is 
 * to be simulated over the execution of the given simulation. The 
 * action model determines the way the individual moves over time.
 * The action strategy contains the information specific to an individual
 * to allow movement to occur. 
 *
 * @see crowdsimulation.actioncontroller.ActionController
 * @see crowdsimulation.actioncontroller.strategy.ActionStrategy
 *
 * @author $Author: dkaup $
 * @version $Revision: 1.14 $
 * $State: Exp $
 * $Date: 2011/01/31 17:00:20 $
 **/
public class Individual extends Entity
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////
	
	/** Maintains unique ids for all individuals. **/
	static int master_id=0;
	/** Shortcut to the Java definition of PI. **/
	final double PI = Math.PI;
	
	/** A unique identifier for the individual. **/
	private int id;
	
	/** The scale of the radius in the x direction. This is usefull in elongating the circle (elipse).**/
	private double xScale = 1;
	/** The scale of the radius in the y direction. This is usefull in elongating the circle (elipse).**/
	private double yScale = 1;

	/** Represents the movement of the individual. **/
	private Vector2D momentum = new Vector2D(0,0);
	/** Represents the acceleration for the movement of the individual. **/
	private Vector2D acceleration = new Vector2D( 0,0 );
	/** The velocity that the individual started the simulation with. **/
	private double initialVelocity = 4.0;

	/** The obstacles the individual interacted with in the last timestep. **/
	private Bag interactingObstacles;
	/** The other individuals the individual interacted with in the last timestep. **/
	private Bag interactingIndividuals;
	/** The paths that the individual is attracted to. **/
	private Bag interactingPaths;
	/** The paths that the individual is currently on. **/
	private Path selectedPath = null;
	
	/** A string to allow defining different types of individuals. This is intended as an output 
	 * only parameter. 
	 **/
	private String type = "Person";
	/** The size of and individual. **/
	private double diameter = 1;
	/** The radius of the individual. **/
	private double radius= 0.5;
	/** The individuals color. **/
	private	Color color = Color.pink;
	/** The amount of energy an individual has. This could be used for fatigue. **/
	private double energy = 100;
	/** The way to physically represent the individual when drawing and interacting with other the simulation. **/
	private CircularObstacle physicalRepresentation;
	/**The maximum compression an individual should feel in meters.
      *The maximum tolerable "squeeze", in other words.**/
	private double sMax =0.07;


	/** Is the angle from the direction the individual is headed in which they can see. **/
	private double viewAngle = 0.5*Math.PI;
	/** The direction of the individual's center of gaze, not
     *the same as orientation which is in the direction of movement.**/
	private Vector2D gaze = new Vector2D(0,1);
	
	/** The id of the group this individual is associated with. **/
	private double groupID = 0;
	
// Image related member variables.//

	/** The individuals image. **/
	private Image image = null;
	/** If the image is set this flag determines if the image's bounding box will be drawn. **/
	private boolean imageBoundingBox = false;
	/** If the image is set this flag determines if we draw the interaction circle. **/ 
	private boolean interactionCircle = false;
	
	/** The individuals graphics object. **/
	private OrientedPortrayal2D orientedPortrayal2D;
	
	/** String containing the comma seperated list of all forces acting on the individual. **/
	private String forceValues ="";
	
	/** The action strategy will contain model specific data for the individual. **/
	private ActionStrategy actionStrategy;
	
	/** How long it takes for the individual to react to its environement. **/	
	private double reactionTime = 1;
	/** Shows if the individual can still move at normal speeds. **/
	boolean injured = false;
	/** The vector (speed and direction) at which the individual will move the next timestep. **/
	private Vector2D nextVelocity = new Vector2D(0,0);
	/** Is the individual still capable of moving. **/
	private boolean dead = false;
	/** What angle (direction) the individual is moving in. Also is parallel to his velocity.**/
	private double orientation;
	
	/**If the individual sees a particular object. **/
	private boolean notSeen = false;
	/** The max distance that the individual can see.**/ 
    private double radiusOfConcern = 1;
    /**The max distance of attraction felt toward an attraction point.**/
	private double radiusOfAttraction = 15;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Default Construction of the individual.
	 * This is the base actions that all constructors must call.
	 **/
	private Individual() 
	{
		setID( master_id++ );
		interactingObstacles = new Bag();
		interactingIndividuals = new Bag();
		interactingPaths = new Bag();
		setTerrain( CrowdSimulation.getInstance().getWorld() );
	}


	/**
	 * Constructs an individual with an entity, and a set of parameters.
	 * 
	 * @param entity_id The unique identifier for all entities.
	 * @param parameters The parameters for the individual.
	 **/
	public Individual( int entity_id, Map parameters ) 
	{
		this();
		setEntityID( entity_id );
		setParameters( parameters );
	}


	/**
 	 * Method to draw the obstacle on the GUI. 
 	 * This method needs to be overridden in any class subclassing from this class.
 	 *
 	 * @param object This object may be null.
 	 * @param graphics The graphic object which the obstacle should be drawn onto.
 	 * @param info The information about the graphics object to be drawn on.
 	 **/
	public void draw( Object object, Graphics2D graphics, DrawInfo2D info )
	{
		// Store the current color.
		Color temp = graphics.getColor();
			
		// Draw the image if it is set.
		if( image != null )
		{
			double scale = 2.0*radius*info.draw.width/image.getWidth( null );
			double angle = getOrientation();

			int width = (int)(scale*xScale*image.getWidth( null ));
			int height = (int)(scale*yScale*image.getHeight( null ));
			
			AffineTransform tx = new AffineTransform();
			AffineTransform oldTransform = (AffineTransform)(graphics.getTransform().clone());
        	tx.rotate( angle );
        	
        	try
        	{
        		Point2D locationPoint = new Point( (int)info.draw.x, (int)info.draw.y );
        		Point2D transformedPoint = new Point();
	        	graphics.transform( tx );

	        	AffineTransform invTrans = tx.createInverse();
	        	transformedPoint = invTrans.transform( locationPoint, transformedPoint );
	        	graphics.drawImage
	        		( 
		        		image, 
		        		(int)transformedPoint.getX()-width/2, 
		        		(int)transformedPoint.getY()-height/2, 
		        		(int)(width), 
		        		(int)(height),
		        		null 
		        	);
	        	graphics.transform( tx.createInverse() );
	        }
	        catch( Exception e )
	        {
	        	Log.log( 0, e );
	        	System.exit( 1 );
	        }
							
			if( isImageBoundingBox() )
			{
				Shape shape = new Rectangle2D.Double
					( 
						(int)(info.draw.x-width/2),
						(int)(info.draw.y-height/2),
						width,
						height 
					);
				graphics.setColor( Color.BLACK );
	        	graphics.draw( shape );
			}
							
			if( isInteractionCircle() )
			{
				Shape shape = new Ellipse2D.Double
					( 
						info.draw.x-radius*info.draw.width, 
						info.draw.y-radius*info.draw.height, 
						2*radius*info.draw.width,
						2*radius*info.draw.height 
					);
				
	        	graphics.setColor( Color.BLACK );
	        	graphics.draw( shape );
			}
		}   
		else
		{
			// In this case the image isn't set so we default to
			// a circle with a nose indicating the direction the
			// individual is facing.
			Shape shape = new Ellipse2D.Double
				( 
					info.draw.x-radius*info.draw.width, 
					info.draw.y-radius*info.draw.height, 
					2*radius*info.draw.width,
					2*radius*info.draw.height 
				);
				
	        graphics.setColor( this.getColor() );
	        graphics.fill( shape );
	        graphics.setColor( Color.BLACK );
	        graphics.draw( shape );
	              
	        // Draw a short line segment indicating facing direction.
	        Vector2D direction = this.getMomentum();
	        if( direction.magnitude() != 0.0 )
	        {
	        	direction.normalize();
	        	Line2D.Double line = new Line2D.Double
	        		( 
	        			info.draw.x,
	        			info.draw.y,
	        			info.draw.x+radius*info.draw.width*direction.x*1.4,
	        			info.draw.y+radius*info.draw.height*direction.y*1.4 
	        		);
	        	graphics.draw( line );
	       	}    
		}
		
		// Reset the color.
		graphics.setColor( temp );
		
	}


	/**
	 * Kills the individuals and removes them from the simulations.
	 * This method stops the individual from being simulated anymore.
	 * The individual is set to dead and the references to the indivudal
	 * are removed from the action controller and from the world.
	 **/
	public void kill()
	{
		setDead(true);
		this.getActionController().removeIndividual(this);
		CrowdSimulation.getInstance().getWorld().remove(this);
	}


	/**
	 * Makes it so that whenever the object is written out it just writes the individuals id.
	 *
	 * @return The id of the individual as a string.
	 **/
	public String toString()
	{
		return ""+id;
	}


	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the indiviudal.
	 **/
	public void setParameters( Map parameters )
	{
			if( parameters.containsKey( "diameter" ) )
			{
				Object value = parameters.get( "diameter" );
				double val = 0;
				
				if( value instanceof String )
				{
					val = Double.parseDouble( (String)value );
				}
				else if( value instanceof Double )
				{
					val = ((Double)value).doubleValue();
				}
				else if( value instanceof RandomGenerators )
				{
					val = ((RandomGenerators)value).nextValue();
				}
				else
				{
					Log.log( 1, " Diameter for Individuals construction must be a Double or a string representing a Double.");
					Log.log( 1, "    Random generators are also allowed!");
				}
				setDiameter( val );
			}
			if( parameters.containsKey( "initialVelocity" ) )
			{
				Object value = parameters.get( "initialVelocity" );
				double val = 0;
				
				if( value instanceof String )
				{
					val = Double.parseDouble( (String)value );
				}
				else if( value instanceof Double )
				{
					val = ((Double)value).doubleValue();
				}
				else if( value instanceof RandomGenerators )
				{
					val = ((RandomGenerators)value).nextValue();
				}
				else
				{
					Log.log( 1, " initialVelocity for Individuals construction must be a Double or a string representing a Double.");
					Log.log( 1, "    Random generators are also allowed!");
				}
				setInitialVelocity( val );
			}
            if (parameters.containsKey("radiusOfConcern")) {
                Double val = (Double) parameters.get("radiusOfConcern");
                this.radiusOfConcern = val;

            }
			if( parameters.containsKey( "location" ) )
			{
				Vector2D loc = (Vector2D)parameters.get( "location" );
				setLocation( loc );
			}
			if( parameters.containsKey( "velocity" ) )
			{
				Vector2D vel = (Vector2D)parameters.get( "velocity" );
				setVelocity( vel );
				this.setNextVelocity( vel );
			}
			if( parameters.containsKey( "type" ) )
			{
				setType( (String)parameters.get( "type" ) );
			}
			if( parameters.containsKey( "portrayl" ) )
			{
				Object value = parameters.get( "portrayl" );
				setOrientedPortrayal2D( (OrientedPortrayal2D)value );	
			}
			if( parameters.containsKey( "color" ) )
			{
				Object value = parameters.get( "color" );
				Color val = null;
				
				if( value instanceof String )
				{
					String[] sections = ((String)value).split( " " );
					
					int red = Integer.parseInt( sections[0] );
					int green = Integer.parseInt( sections[1] );
					int blue = Integer.parseInt( sections[2] );
					
					val = new Color( red, green, blue );
				}
				else if( value instanceof Color )
				{
					val = (Color)value;
				}
				else
				{
					Log.log( 1, " color for Individual construction must be a color object or a string with rgb colors seperated by a space.");
				}
				setColor( val );
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
					Log.log( 1, " yScale for Individual construction must be a Double or a string representing a Double.");
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
					Log.log( 1, " xScale for Individual construction must be a Double or a string representing a Double.");
				}
				setXScale( val );
			}
			if( parameters.containsKey( "paths" ) )
			{
				Object value = parameters.get( "paths" );
				Bag val = new Bag();
				
				if( value instanceof String )
				{
					String[] ids = ((String)value).split(" ");
					for( int i =0; i < ids.length; i++ )
					{
						val.add( ((Path)CrowdSimulation.getInstance().getPaths().get( new Integer( ids[i] ) )).clone() ) ;
					}
				}
				else if( value instanceof Bag )
				{
					Bag elements = (Bag)value;
					for( int i =0; i < elements.size(); i++ )
					{
						val.add( ((Path)elements.get( i )).clone() );
					}
					val = (Bag)value;
				}
				else
				{
					Log.log( 1, " color for Individual construction must be a color object or a string with rgb colors seperated by a space.");
				}
				
				setInteractingPaths( val );
			}
			if( parameters.containsKey( "pathID" ) )
			{
				Object value = parameters.get( "pathID" );
				String val = "";
				
				if( value instanceof String )
				{
					val = (String)value;
				}
				else
				{
					Log.log( 1, " pathID for Individual construction must be a String representing the id of the path.");
				}
				for( int i=0; i < this.interactingPaths.size(); i++ )
				{
					Path pth = (Path)interactingPaths.get( i );
					if( pth.getID() == Integer.parseInt( val ) )
					{
						selectedPath = pth;
					}
				}
			}
			if( parameters.containsKey( "wayPointLocation" ) )
			{
				Vector2D loc = (Vector2D)parameters.get( "wayPointLocation" );
				selectedPath.setActiveWaypoint( loc );
			}
			if( parameters.containsKey( "groupID" ) )
			{
				Object value = parameters.get( "groupID" );
				double val = 0;
				
				if( value instanceof String )
				{
					if( !value.equals( "" ) && !value.equals( " " ) )
					{
						val = Double.parseDouble( (String)value );
					}
				}
				else
				{
					Log.log( 1, " groupID for Individual construction must be a String representing the id of the group.");
				}
				this.setGroupId( val );
			}
	}

	
	/**
	 * This method is needed in order to "inspect" the individual in Mason.
	 * This allows loading in the attributes of the individual into a view window
	 * by clicking on the individual of interest in the GUI.
	 *
	 * @param object This object may be null.
	 * @param range The individual which describe the scaling, if any, used in displaying the obstacle.
	 * @return A boolean representing if the individual was hit.
	 **/
	public boolean hitObject( Object object, DrawInfo2D range )
    {
	    final double SLOP = 1.0;  // need a little extra diameter to hit circles
	    final double width = range.draw.width * this.getDiameter();
	    final double height = range.draw.height * this.getDiameter();
	    
	    Ellipse2D.Double ellipse = new Ellipse2D.Double
	    	( 
	        	range.draw.x-width/2-SLOP, 
	        	range.draw.y-height/2-SLOP, 
	        	width+SLOP*2,
	        	height+SLOP*2 
	        );
	    return ( ellipse.intersects( range.clip.x, range.clip.y, range.clip.width, range.clip.height ) );
    }


	/**
	 * Gets all individuals within the region of concern around the individual.
	 *
	 * @return Bag A bag of all individuals within this individuals interaction radius.
	 **/
    public Bag getNeighboringIndividuals()
 	{
 		setInteractingIndividuals( getIndividualsWithinDistance( radiusOfConcern ) );
    	return getInteractingIndividuals();
    }

    
    /**
	 * Gets all obstacles within the region of concern around this individual.  
	 *
	 * @return Bag A bag of all obstacle with this individauls raidusOfCOncern.
	 **/
    public Bag getNeighboringObstacles()
 	{
 		setInteractingObstacles( getObstaclesWithinDistance( radiusOfConcern ) );
    	return getInteractingObstacles();
    }

    
    /** 
	 * Returns the normal from a given point on the boundary of this individual.
     *
     * This creates a normalized vector directed away from the center of the individual,
     * and away from the given point, which is on the boundary of the individual.
	 *
	 * @param point The point at which to place the normal.
	 * @return Vector2D The normal to the individual. If the point is not on the
     * boundary, null is returned.
	 **/
    public Vector2D getNormalAt( Vector2D point )
    {
//        if( !isOnBoundary( point ) )  {return null; }
    	Vector2D vv = new Vector2D( point.x-this.getCenter().x, point.y-this.getCenter().y );
        return vv.normalize();
    }


    /**
	 * Returns the Normal to the entity at the closest point to the individual.
	 * 
	 * @param ent The entity to find the normal on.
	 * @return The normal at the entity.
	 **/
    public Vector2D getNormalOf( Entity ent )
	{
		if( ent instanceof Individual ) return getNormalOf( (Individual)ent );
		if( ent instanceof Obstacle ) return getNormalOf( (Obstacle)ent );

		return new Vector2D( 0, 0 );
	}

    
    /** 
     * Edited by DJK on 7/5/10. Revised by DJK on 11/04/10.
     *
	 * Returns the normal to the obstacle, directed outward from the obstacle at the
     * closest point, on the boundary of the obstacle, to the individual. 
     *
     * @param obs The obstacle that will have the normal.
	 * @return The normal from the obstacle (and is generally toward the individual).
	 **/
    public Vector2D getNormalOf( Obstacle obs )
    {
    	Vector2D point = surfacePointOn( obs );
//        if( point == null )
//        {
//            System.out.println(" Individual " + this.getID() + " is inside obs " + obs.getId() );
//            return point;
//        }
    	return  obs.getNormalAt( point );
    }

    /**
	 * Returns the normal to the individual ind2 at the point on the boundary of ind2
     * which is closest to this individual. If the center of ind2 is inside this ind, then null
     * is returned.
     *
	 * @param ind2 The individual on which the normal is.
	 * @return The normal to ind2 and on the boundary of ind2, directed to this individual.
	 **/
    public Vector2D getNormalOf( Individual ind2 )
    {
    	Vector2D normal = new Vector2D( getCenter().x-ind2.getCenter().x, getCenter().y-ind2.getCenter().y );
    	return  normal.normalize();
    }

    /** 
     * Revised by DJK on 7/5/10 and on Dec 7 2010.
     *
     * Returns the tangent to the individual at a given point. The sign of the tangent
     * is such that its dot product with the vector orientation is positive.
	 *
	 * @param point The point at which the tangent line is to be located.
	 * @param orientation The direction in which (+-90 degrees) the tangent is to point.
	 * @return The tangent on the individual at the specified point.
     * If the point is not on the boundary, null is returned.
	 **/
    public Vector2D getTangentAt( Vector2D point, Vector2D orientation )
    {
        Vector2D normal = getNormalAt( point );
        Vector2D vec = new Vector2D( normal.y, - normal.x );
    	double dot = vec.x*orientation.x + vec.y*orientation.y;
    	if( dot < 0.0 )
    	{
    		vec = new Vector2D( - normal.y, normal.x );
    	}
    	return vec;
    }

    
    /**
	 * Returns the tangent to an entity.
	 * 
	 * @param ent The Entity at which to find the tangent line.
	 * @return The tangent to the entity.
	 **/
    public Vector2D getTangentOf( Entity ent )
	{
		if( ent instanceof Individual ) return getTangentOf( (Individual)ent );
		if( ent instanceof Obstacle ) return getTangentOf( (Obstacle)ent );

		return new Vector2D( 0, 0 );
	}

    /** 
	 * Returns the tangent at the closest point to the boundary of an obstacle.
     
	 *
	 * @param obs The obstacle to find the tangent line on.
	 * @return The tangent to the obstacle.
	 **/
    public Vector2D getTangentOf( Obstacle obs )
    {
    	Vector2D point = surfacePointOn( obs );
        return  obs.getTangentAt( point, new Vector2D( Math.cos(this.getOrientation()), Math.sin(this.getOrientation()) ) );
    }

	/** 
     * Returns the tangent at the closest point to the boundary of Ind2.
	 *
	 * @param ind2 The individual to find the tangent line on.
	 * @return The tangent on ind2 and aligned per this.individual.
	 **/
    public Vector2D getTangentOf( Individual ind2 )
    {
        Vector2D point = surfacePointOn( ind2 );
    	return  ind2.getTangentAt( point, new Vector2D( Math.cos(this.getOrientation()), Math.sin(this.getOrientation()) ) );
    }

    
	/**     
	 * Returns the vector distance from the center of this.ind to the edge of another entity.
	 * 
	 * @param ent The entity to find the distance to.
	 * @return Vector2D The distance to the entity.
	 **/
   	public Vector2D distanceTo( Entity ent )
	{
		if( ent instanceof Individual ) return distanceTo( (Individual)ent );
		if( ent instanceof Obstacle ) return distanceTo( (Obstacle)ent );

		return new Vector2D( 0, 0 );
	}


	/**  
	 * Returns the vector distance from the center of this.ind to the boundary of another individual, ind2.
	 *
	 * @param ind2 The Individual to find the distance to.
	 * @return The vector distance to the other Individual.
	 **/
	public Vector2D distanceTo( Individual ind2 )
	{
		Vector2D point = surfacePointOn( ind2 );
		return distanceTo( point );
	}


	/**
	 * Returns the vector distance to a point from the center of this.ind.
	 * 
	 * @param point The point to find the distance to.
	 * @return The vector distance to the point.
	 **/
	public Vector2D distanceTo( Vector2D point )
	{
//		Vector2D direction = new Vector2D( point.x-this.getLocation().x, point.y-this.getLocation().y );
//		direction.normalize();

		return new Vector2D( point.x-this.getLocation().x, point.y-this.getLocation().y );
	}

	
	/**  
	 * Returns the vector distance from the center of the individual to the nearest edge of an obstacle.
     * 
	 *
	 * @param obs The obstacle to find the distance to.
	 * @return The shortest vector distance from the center of the individual to the edge of the
     * obstacle. If the center of the individual is inside the obstacle, a zero vector is returned.
	 **/
	public Vector2D distanceTo( Obstacle obs )
	{
        Vector2D point = surfacePointOn( obs );
		return distanceTo( point );
	}

	/**
	 * Returns a point of intersection between a given ray and the individual.
	 * The point should be on the circle.
	 *
	 * @param origin The origin of the ray to test the intersection with.
	 * @param direction The direction of the ray to test the intersection with. This does not have to be normalized.
	 * @return The point of intersection. If no intersection is found then it returns (-1,-1).
	 **/
	public Vector2D getClosestPoint( Vector2D origin, Vector2D direction )
	{
    	Vector2D closestPoint = new Vector2D( -1, -1 );           // Closest point - value returned.

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

		if( ((tempDir.x > 0 && direction.x > 0) || (tempDir.x < 0 && direction.x < 0) || (tempDir.x == 0 && direction.x == 0)) &&
		    ((tempDir.y > 0 && direction.y > 0) || (tempDir.y < 0 && direction.y < 0) || (tempDir.y == 0 && direction.y == 0)) )
		{
			closestPoint = new Vector2D( tempPoint.x+getCenter().x, tempPoint.y+getCenter().y );
		}

		return closestPoint;
    }

//	/**   This is the DJK form.
//	 * Revised by DJK on 7/3/10. Revised again by DJK on 12/01/10.
//	 *
//	 * @param origin The origin from which the ray extends.
//	 * @param direction The direction of the ray. This does not have to be normalized.
//	 * @return The closest point of intersection. If no intersection, (-1,-1) is returned.
//     * If the origin is inside the individual, null is returned.
//	 **/
//	public Vector2D getClosestPoint( Vector2D origin, Vector2D dir )
//	{
//// First we set up our vectors and their magnitudes.
//    	double x1 = this.getCenter().x - origin.x;
//    	double y1 = this.getCenter().y - origin.y;   // components of the vector r1.
//        double r1sq = x1*x1 + y1*y1;
//        double r1 = Math.sqrt(r1sq);
//        double capR = this.getRadius();  // The radius of the circle.
//        if( r1 < capR )
//        {
//            return null;  // since the origin is inside the circle.
//        }
//
//        Vector2D norm = dir.normalize();
//        // Let us make sure that the norm is directed from the origin toward "this".
//        double dot = x1*norm.x + y1*norm.y;
//        if( dot < 0.0 )
//        {
//            norm.x = - norm.x;
//            norm.y = - norm.y;
//            dot = - dot;
//        }
//        // now we have norm pointing toward "this".
//    	double x2 = norm.x;
//    	double y2 = norm.y;
////
////  The vector from the origin to the center of the circle is (x1, y1).
////  The radius of the circle is CapR.
////  The vector from the origin to the intersection point on the circle has the direction (x2, y2).
////  The magnitude of the vector from the origin point to the intersection point is beta.
////  The intersection between the line and the circle gives rise to a quadratic equation.
////  Define DisSq =  CapR^2 - (x1*y2 - x2*y1)^2.
////  Define b = - dot.
////  Then if DisSq < 0, there is no intersection.
////  If DisSq > 0, then the two intersection points are given by:
////
////  beta = - b +- sqrt( DisSq ) or  + dot +- sqrt( DisSq )
////
//        double cr12 = x1*y2 - x2*y1;
//        double DisSq = capR*capR - cr12*cr12;
////
////        double Dis=0;
//
//        if( DisSq < 0 ) { return new Vector2D(-1, -1 ); }   // If DisSq < 0, there is no intersection.
//// we return the position of the closest intersection.
//        double beta = dot - Math.sqrt( DisSq );
////
//        return new Vector2D( origin.x + beta*x2, origin.y + beta*y2 );
//    }


    /**
	 * Calculates an intersection point on the given obstacle from an individual.
	 * 5 different tests are run. Each coordinate direction is tested along with from
	 * center of the individual to the center of the obstacle. The intersection point
	 * with the minimum distance is returned.
	 * 
	 * @param ent The individual to check distances to.
	 * @return The intersection point, as a 2 Dimensional vector, which had the minimum distance from the individual.
	 **/
	public Vector2D surfacePointOn( Entity ent )
	{
		if( ent instanceof Individual ) return surfacePointOn( (Individual)ent );
		if( ent instanceof Obstacle ) return surfacePointOn( (Obstacle)ent );

		return new Vector2D( 0, 0 );
	}

   	/** This is the DJK form.
     * Revised by DJK on 7/4/10. Edited on 10/7/10 and on 12/15/10.
     *
	 * Calculates the closest point on the boundary of another individual from the center of
     * this individual.
     *
	 * @param ind The individual to get distances to.
	 * @return A 2-Dimensional vector from the center of this individual to the closest point on the circle of ind.
     * If this individual is inside the other, then null is returned.
	 **/
	public Vector2D surfacePointOn( Individual ind )
	{
		Vector2D center = this.getLocation();
		Vector2D indCenter = ind.getLocation();

    	double x = indCenter.x - center.x;
    	double y = indCenter.y - center.y;
    	double r = Math.sqrt( x*x + y*y );
        double R2 = ind.getRadius();
     //  double R1 = this.getRadius();
        //
            // If r < R1 + R2, then this individual overlaps with the other individual.
            // If r < R2, then the center of this individual is inside the other individual.
            //
//            if( r < R2  )
//            {
//                //If we get to here, then the center of the individual is inside ind.
//                System.out.println("The center of this ind " + this.getID() + " is inside individual " + ind.getID() );
//
//            return null;
//            }
//
        double xx = x*(1-R2/r) + center.x;
        double yy = y*(1-R2/r) + center.y;
        return new Vector2D( xx, yy );
	}

	

     /**
     * Written by DJK on Dec 7 2010.
     * Revised by DJK on Jan 10 2011 to put points p1 to p4 in CW order.
     *
     * Returns the closest point to the origin which is on the boundary of rect.
	 *
	 * @param rect The rectangular obstacle on which the point on the border is to be found.
	 * @return The vector position of the closest point.
	 **/
	public Vector2D surfacePointOnRect( RectangularObstacle rect )
	{
        Vector2D vecP;
        Vector2D vecX;
        Vector2D vecY;
        //First we find if the individual is inside rect
//        if( rect.isInside( this.getLocation() ) )
//            {
//                return null;
//            }
        // Since ind is not inside, we find which corner is the closest to ind.
        // We solve relative to the ind.
        double x = getLocation().x;
        double y = getLocation().y;
        Vector2D vec1 = new Vector2D(rect.p1.x - x, rect.p1.y - y);
        Vector2D vec2 = new Vector2D(rect.p2.x - x, rect.p2.y - y);
        Vector2D vec3 = new Vector2D(rect.p3.x - x, rect.p3.y - y);
        Vector2D vec4 = new Vector2D(rect.p4.x - x, rect.p4.y - y);
        double r1 = vec1.magnitude();
        double r2 = vec2.magnitude();
        double r3 = vec3.magnitude();
        double r4 = vec4.magnitude();
        // We start with the first corner and work around to find the closest corner.
        double capR = r1;
        vecP = vec1;
        // We set up a left-handed set of X and Y coordinates at the corner, directed outward from the obstacle.
        vecX= rect.p12;
        vecY = new Vector2D( - rect.p41.x, - rect.p41.y );
        if( r2 < capR )
        {
            capR = r2;
            vecP = vec2;
            vecX = rect.p23;
            vecY = new Vector2D( - rect.p12.x, - rect.p12.y );
        }
        if( r3 < capR )
        {
            capR = r3;
            vecP = vec3;
            vecX = rect.p34;
            vecY = new Vector2D( - rect.p23.x, - rect.p23.y );
        }
        if( r4 < capR )
        {
            capR = r4;
            vecP = vec4;
            vecX = rect.p41;
            vecY = new Vector2D( -rect.p34.x, -rect.p34.y );
        }
        // Now we have the closest corner and a set of left-handed axies set up on that point.
        // We want to know which quadrant vecP points into.
        // Remember that vecP points from the origin to the corner point.
        //
        vecX = vecX.normalize();
        vecY = vecY.normalize();
        double xdotP = vecX.x*vecP.x + vecX.y*vecP.y;
        double ydotP = vecY.x*vecP.x + vecY.y*vecP.y;
        // If both dot products are negative, then the corner is the closest point.
        // If both dot products are postive, then origin has to be approximately on the boundary the rectangle.
        // Otherwise, whichever one is positive, our point then lies along that side of the rectangle.
        {
        if( xdotP > 0 )
            {
            if( ydotP < 0 )
                {
                    return new Vector2D( x + ydotP*vecY.x, y + ydotP*vecY.y );
                }
            else
                {
                    return new Vector2D( x + vecP.x, y + vecP.y );
                    // This case can occur due to round-off error or if origin = corner.
                    // So we return the corner.
                }
            }
        else
            {
            if( ydotP > 0 )
                {
                    return new Vector2D( x + xdotP*vecX.x, y + xdotP*vecX.y );
                }
            else
                {
                    return new Vector2D( x + vecP.x, y + vecP.y );
                    // The nearest point can only be the corner, so we return it.
                }
            }
        }
    }

   	/** This is the DJK form. 
     * Revised by DJK on 12/15/10.
	 * Calculates the closest point on the given obstacle from the center of this individual.
	 *
	 * @param obs The obstacle to check distances to.
	 * @return Vector2D The closest point on the boundary of obs, as a 2 Dimensional
     * vector, which has the shortest distance from the center of the individual.
	 **/
	public Vector2D surfacePointOn( Obstacle obs )
	{
    	if( obs instanceof RectangularObstacle )
    	{
    		RectangularObstacle rect = (RectangularObstacle)obs;
            Vector2D endPoint = surfacePointOnRect( rect );
//            if( endPoint == null )
//            {
//                System.out.println("This ind " + this.getID() + " is inside RectObs " + obs.getId() );
//            }
            return endPoint;
    	}
    	else
    	{
    		CircularObstacle circ = (CircularObstacle)obs;
	    	Vector2D center = this.getLocation();
		    Vector2D cirCenter = circ.getLocation();

    	    double x = cirCenter.x - center.x;
    	    double y = cirCenter.y - center.y;
    	    double r = Math.sqrt( x*x + y*y );
            double R2 = circ.getRadius();
            //double R1 = this.getRadius();
            //
            // If r < R1 + R2, then the individual overlaps with the obstacle.
            // If r < R2, then the center of this individual is inside the obstacle.
            //
//            if( r < R2  )
//            {
//                //If we get to here, then the center of the individual is inside the cirObs.
//                System.out.println("The center of this ind " + this.getID() + " is inside CirObs " + circ.getId() );
//                 //
//                return null;
//            }
              //
            double xx = x*(1-R2/r) + center.x;
            double yy = y*(1-R2/r) + center.y;
            return new Vector2D( xx, yy );
        }
    }

   	
	/**
     * Corrected on 31Jan2011 by DJK to exclude obstacles from this bag.
     *
	 * Get all individuals within a certain distance of this individual.
	 *
	 * @param distance The scalar distance of which the returned individuals are within.
	 * @return Bag The collection of indiviudals within the requested distance.
	 **/	
	public Bag getIndividualsWithinDistance( double distance )
	{
		Bag selectedInds = new Bag();
		Bag indsPlusObsts = CrowdSimulation.getInstance().getWorld().getAllObjects();

		for( int i = 0; i < indsPlusObsts.size(); i++ )
		{
            if (indsPlusObsts.get(i) instanceof Individual)
            {
			Individual ind = (Individual)indsPlusObsts.get(i);
	    		if( !ind.equals(this) )
		    	{
		    		double r = distanceTo( ind ).magnitude();

		    		if( r - this.getRadius() < distance )
		    		{
		    			selectedInds.add(ind);
			    	}
	    		}
		    }
        }
		return selectedInds;
	}


	/**
     * Corrected on 31Jan2011 by DJK to exclude individuals from this bag.
     *
	 * Get all obstacles whose edge is within a certain distance from the edge of this individual.
	 *
	 * @param distance The scalar distance of which the returned obstacles are within.
	 * @return Bag The collection of obstacles within the requested distance.
	 **/
	public Bag getObstaclesWithinDistance( double distance  )
	{
		Bag selectedObs = new Bag();
		Bag indsPlusObsts = CrowdSimulation.getInstance().getTerrain().getAllObjects();
		
		for( int i = 0; i < indsPlusObsts.size(); i++ )
		{
            if (indsPlusObsts.get(i) instanceof Obstacle) 
            {
    			Obstacle obs = (Obstacle)indsPlusObsts.get(i);
	    		double r = distanceTo( obs ).magnitude() - this.getRadius();
    			if( r  < distance )
	    		{
		    		selectedObs.add(obs);
			    }
    		}
        }
		return selectedObs;
	}
    /**
     * Written by DJK on Dec 7 2010.
     *
	 * Returns true if the point is effectively on the boundary of the individual and
     * false if the point is inside or outside the individual.
	 *
	 *@param point The point being tested to see if it is on the boundary of the individual.
	 *@return  A boolean is returned indicating if the given point is on the boundary of the individual.
	 **/
	public boolean isOnBoundary( Vector2D point )
	{
//        if( point == null )
//        {
//            System.out.println("Point is null and is not on boundary for ind " + this.getID() );
//            return false;
//        }
//        if( point.x==-1.0 && point.y==-1.0 )
//        {
//            System.out.println("Point is out of this World and not on boundary for ind " + this.getID() );
//            return false;
//        }
		double distance = point.distance( getCenter() );

		if( approximatelyEqual( distance/getRadius(), 0.0 ) )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**  This is the DJK form.
 	 * Returns true if the point is inside the individual and false if the point
     * is on the edge or outside the obstacle.
	 *
	 *@param point The point being tested to see if it is inside the individual.
	 *@return  A boolean is returned indicating if the given point is inside the individual.
	 **/
	public boolean isInside( Vector2D point )
	{
//        if( point == null )
//        {
//            System.out.println("Point is null in isInside() for ind " + this.getID() );
//            return false;
//        }
//        if( point.x==-1.0 && point.y==-1.0 )
//        {
//            System.out.println("Point is out of World in isInside() for ind " + this.getID() );
//            return false;
//        }
//
        double distance = point.distance( getLocation() );

		if( distance < getRadius() )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

    /**
     * Written by DJK on 21 Jan, 2011.
     * Calculates the point on obs which is the farthest from the origin.
	 *
     * @param origin the point from which to measure the distance.
     * @param obs The obstacle to check distances to.
	 * @return Vector2D The point on the boundary of obs that is the farthest away from obs.
	 **/
	public Vector2D farthestPointFrom( Vector2D origin, Obstacle obs )
	{
    	if( obs instanceof RectangularObstacle )
    	{
    		RectangularObstacle rect = (RectangularObstacle)obs;
            double x = origin.x;
            double y = origin.y;
            Vector2D vec1 = new Vector2D(rect.p1.x - x, rect.p1.y - y);
            Vector2D vec2 = new Vector2D(rect.p2.x - x, rect.p2.y - y);
            Vector2D vec3 = new Vector2D(rect.p3.x - x, rect.p3.y - y);
            Vector2D vec4 = new Vector2D(rect.p4.x - x, rect.p4.y - y);
            // The solution is the corner which is the farthest.
            // We start with the first corner and work around to find the farthest corner.
            double capRsq = vec1.x*vec1.x + vec1.y*vec1.y;
            Vector2D vecP = rect.p1;

            double capR2sq = vec2.x*vec2.x + vec2.y*vec2.y;
            if( capR2sq > capRsq )
            {
                capRsq = capR2sq;
                vecP = rect.p2;
            }
            capR2sq = vec3.x*vec3.x + vec3.y*vec3.y;
            if( capR2sq > capRsq )
            {
                capRsq = capR2sq;
                vecP = rect.p3;
            }
            capR2sq = vec4.x*vec4.x + vec4.y*vec4.y;
            if( capR2sq > capRsq )
            {
                capRsq = capR2sq;
                vecP = rect.p4;
            }
            return vecP;
        }
    	else
    	{
    		CircularObstacle circ = (CircularObstacle) obs;
		    Vector2D center = circ.getCenter();
            double x = center.x - origin.x;
    	    double y = center.y - origin.y;
        	double r = Math.sqrt( x*x + y*y );
            double R2 = circ.getRadius();
            double xx = x*R2/r + center.x;
            double yy = y*R2/r + center.y;
            return new Vector2D( xx, yy );
        }
    }

	/**
	 * Determines if an indivdual is within this indivdual's filed of view.
	 *
     * BUG - DJK changed "ind.radiusOfConcern" to "this.radiusOfConcern".
	 *
	 * @param ind The indiviual to test if you can see.
	 * @return boolean Can the individual be seen?
	 **/
	public boolean canSee( Individual ind )
	{
		return canSeeCenter( ind, this.radiusOfConcern );
	}


	/**
	 * Determines if a waypoint is withing this indivdual's filed of view.
	 *
	 * @param w The Waypoint to test if you can see.
	 * @return boolean Can the waypoint be seen?
	 **/
	public boolean canSee( Waypoint w )
	{
		return canSeeCenter( (Obstacle)w, this.radiusOfAttraction );
	}


	/**
	 * Determines if an obstacle is withing this indivdual's filed of view.
	 *
	 * @param obs The obstacle to test if you can see.
	 * @return boolean Can the obstacle be seen?
	 **/
	public boolean canSee( Obstacle obs )
	{
		Vector2D posVector = getCenter();

		/*Check if the object center can be seen*/
		if( canSeeCenter(obs, this.radiusOfConcern))
			return true;

	    double theta = getGazeAngle();

	    /*Check if the object is within the field of view.*/
	    Vector2D right = new Vector2D(Trig.cos( theta + PI/2), Trig.sin( theta + PI/2));
	    Vector2D left = new Vector2D(Trig.cos( theta - PI/2), Trig.sin( theta - PI/2));

		Vector2D rightIntersection = obs.getClosestPoint(posVector, right);
		Vector2D leftIntersection = obs.getClosestPoint( posVector, left);

		if((leftIntersection!= null) || (rightIntersection != null))
			return true;

		return notSeen;
	}


	/**
	 * Determins if the individual sees the an entity.
	 * The determination is determined by passing the request to specific canSee methods
	 * for Individual, Waypoint, or Obstacle.
	 *
	 * @param ent The entity to test if it can be seen.
	 * @return boolean Can the entity be seen?
	 **/
	public boolean canSee( Entity ent )
	{
		if ( ent instanceof Individual )
		{
			return canSee((Individual) ent);
		}
		if( ent instanceof Waypoint)
		{
			return canSee((Waypoint)ent);
		}
		if(ent instanceof Obstacle)
		{
			return canSee((Obstacle)ent);
		}

		return notSeen;
	}


	/**
	 * Determins if the individual sees the an center of the entity.
	 *
	 * @param ent The entity to test if it can be seen.
	 * @param distance The distance in which the center of the Entity must be.
	 * @return boolean Can the center of the entity be seen?
	 **/
	public boolean canSeeCenter( Entity ent, double distance )
	{
	  	Vector2D posVector = getCenter();
		Vector2D entPosVector = ent.getCenter();
		Vector2D centroidal_ij = new Vector2D();

		centroidal_ij.x = posVector.x - entPosVector.x;
		centroidal_ij.y = posVector.y - entPosVector.y;

		/*Check if the center of the object is outside the individual's radius of concern*/
		if (centroidal_ij.magnitude() > distance )
		{
			return false;
		}

		double cosine_phi = (gaze.dot(centroidal_ij))/(centroidal_ij.magnitude()*gaze.magnitude());
		double phi = Trig.acos(cosine_phi);

		if( Math.abs(phi) < PI/2)
		{
			return notSeen;
		}
		return true;

	}


	/**
	 * Calculate the angle between 2 individuals and the direction these individuals are looking.
	 * The calculation is done as the angle between the vectore between the center of this
	 * individual and the individual of interest as compared to the direction that this
	 * individual is looking.
	 *
	 * @param ind The indiviual the angle is being calculated for.
	 * @return double The angle calculate.
	 **/
	public double angleBetweenGazes( Individual ind )
	{
        ///get the angle of ind1's view, get the location vector of ind1 and ind2
        ///subtract these to get the vector from ind1 to ind2
        ///calculate the orientation of this vector and compare to ind1's view angle.

        Vector2D ind1Position = this.getLocation();
        Vector2D ind2Position = ind.getLocation();

        Vector2D v_ij = new Vector2D();

        v_ij.x = ind1Position.x - ind2Position.x;
		v_ij.y = ind1Position.y - ind2Position.y;


        double cos_theta = v_ij.dot(gaze)/( v_ij.magnitude()*gaze.magnitude());
     	double theta = Trig.acos(cos_theta);

        return theta;

	}


	/**
	 * Calculate the angle between an individual's gaze and an obstacle.
	 * The calculation is done as the angle between the vector between the centor of this
	 * individual and the obstacle of interest as compared to the direction that this
	 * individual is looking.
	 *
	 * @param obs The indiviual the angle is being calculated for.
	 * @return double The angle calculate.
	 **/
	public double angleBetweenGazeAnd( Obstacle obs )
	{
        ///get the angle of ind1's view, get the location vector of ind1 and ind2
        ///subtract these to get the vector from ind1 to ind2
        ///calculate the orientation of this vector and compare to ind1's view angle.
     	Vector2D v_io = new Vector2D();
        v_io = getNormalOf(obs);

        double cos_theta = v_io.dot(gaze)/( v_io.magnitude()*gaze.magnitude());
     	double theta = Trig.acos(cos_theta);

        return theta;
	}


	/**
	 * Injure the individual by a certain amount.
	 * This reduces the energy available to an individual. If the
	 * amount of energy an individual has drops to or below 0 the
	 * individual is killed.
	 *
	 * @param val The amount of energy reduction caused by the injury.
	 **/
	public void injure( double val )
	{
		this.energy = this.energy-val;
		if( energy <= 0 )
		{
			this.kill();
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Sets the ActionController which will move the individual.
	 *
	 * @param val The ActionController object which represents the way which the individual moves.
	 * @see crowdsimulation.actioncontroller.ActionController
	 **/
	public void setActionController( ActionController val )
	{
		super.setActionController( val );
		getActionController().addIndividual( this );
	}

	/**
	 * Gets the ID of the individual
	 *
	 * @return The ID of the individual.
	 **/
	public int getID()
	{
		return id;
	}
	
	/**
	 * Sets the ID of the individual
	 *
	 * @param idVal The new ID for the individual.
	 **/
	public void setID( int idVal )
	{
		this.id = idVal;
	}
	
	/**
	 * Gets the group id for the individual.
	 *
	 * @return The group id for the individual
	 **/
	public double getGroupId()
	{
		return groupID;
	}

	/**
	 * Sets the group id for the individual.
	 *
	 * @param val The group id for the individual
	 **/
	public void setGroupId( double val )
	{
		groupID = val;
	}	

	/**
	 * Gets the viewable angle from the orientation.
	 *
	 * @return The angle in Radians.
	 **/
    public double getViewAngle()
    {
    	return viewAngle;
    }
    
	/**
	 * Sets the viewable angle from the orientation.
	 *
	 * @param viewAngleVal The view angle in Radians.
	 **/
    public void setViewAngle( double viewAngleVal )
    {
    	viewAngle = viewAngleVal;
    }

	/**
	 * Gets the forces on the individual.
	 *
	 * @return The 2Dimensional vector repersenting the forces.
	 **/
	public Vector2D getForces()
	{
		return acceleration;
	}
	
	/**
	 * Sets the forces on the individual.
	 *
	 * @param xVal The x component of the forces.
	 * @param yVal The y component of the forces.
	 **/
	public void setForces( double xVal, double yVal )
	{
		acceleration.x = xVal;
		acceleration.y = yVal;
	}
	
	/**
	 * Sets the Acceleration of the individual.
	 *
	 * @param accelerationVal The 2 Dimensional vector representing the acceleration.
	 **/
	public void setAcceleration( Vector2D accelerationVal )
	{
		acceleration = accelerationVal;
	}

	/**
	 * Gets the reaction time of the individual.
	 *
	 * @return double representing the actionTime.
	 **/    
    public double getReactionTime()
    {
    	return this.reactionTime;
    }

	/**
	 * Sets the reaction time of the individual.
	 *
	 * @param val The time it takes the individual to react to changes in the world..
	 **/    
    public void setReactionTime( double val )
    {
    	this.reactionTime = val;
    }
  
	/**
	 * Gets the velocity at which the individual started the simulation with.
	 *
	 * @return double The velocity the individual started with.
	 **/      
    public double getInitialVelocity()
    {
    	return this.initialVelocity;
    }
    
	/**
	 * Sets the velocity at which the individual started the simulation with.
	 *
	 * @param val The velocity of the individual started with.
	 **/      
    public void setInitialVelocity( double val )
    {
    	this.initialVelocity = val;
    }
    
	/**
	 * Gets the diameter of the individual.
	 *
	 * @return The diameter of the individual.
	 **/      
    public double getDiameter()
    {
    	return this.diameter;
    }

	/**
	 * Gets the radius of the individual.
	 *
	 * @return The radius of the individual.
	 **/      
    public double getRadius()
    {
    	return this.diameter/2;
    }
    
	/**
	 * Sets the diameter of the individual.
	 *
	 * @param val The diameter of the individual.
	 **/      
    public void setDiameter( double val )
    {
    	this.diameter = val;
    	this.radius = val/2;
    }
    
	/**
     * Gives acces to all the Obstacles that an individual will interact with in a timestep.
     *
     * @return The collection of Obstacles.
     **/
	public Bag getInteractingObstacles()
	{
		return interactingObstacles;
	}

	/**
     * Sets the obstacles that an individual will interact with in a timestep.
     *
     * @param val The collection of Obstacles.
     **/
	public void setInteractingObstacles( Bag val )
	{
		interactingObstacles = val;
	}

	/**
     * Gives acces to all the individuals that an individual will interact with in a timestep.
     *
     * @return The collection of Individuals.
     **/
	public Bag getInteractingIndividuals()
	{
		return interactingIndividuals;
	}
    
    /**
     * Sets the individuals that an individual will interact with in a timestep.
     *
     * @param val The collection of Individuals.
     **/
	public void setInteractingIndividuals( Bag val )
	{
		interactingIndividuals = val;
	}

	/**
	 * Get all entities that the individual is interating with.
	 *
	 * @return The entities that the individual is interating with.
	 */
	public Bag getInteractingEntities()
	{
		Bag allEntities = new Bag();
		allEntities.addAll( getInteractingObstacles() );
		allEntities.addAll( getInteractingIndividuals() );
		return allEntities;
	}

	/**
	 * Get the individuals paths that he is actively following.
	 *
	 * @return The individuals paths that the following is attracted to.
	 */
	public Bag getInteractingPaths()
	{
		return this.interactingPaths;
	}
	
	/**
	 * Sets the individual's paths to a specific collection.
	 *
	 * @param paths The paths that the individual interacts with.
	 **/
	public void setInteractingPaths( Bag paths )
	{
		this.interactingPaths = new Bag();
		for( int i =0; i < paths.size(); i++ )
		{
			this.interactingPaths.add( ((Path)paths.get( i )).clone() );
		}
	}
	
	/**
	 * Get the individual's graphic portrayal.
	 *
	 * @return OrientedPortrayal2D The individuals graphics portrayal.
	 **/
	public OrientedPortrayal2D getOrientedPortrayal2D()
	{
		return this.orientedPortrayal2D;	
	}
	
	/**
	 * Set the individuals graphics portrayal.  The portrayal controls what 
	 * the individual will look like when drawn to the screen.
	 **/
	public void setOrientedPortrayal2D( OrientedPortrayal2D portrayal )
	{
		this.orientedPortrayal2D = portrayal;	
	}
	
	/**
	 * Tells if the individual is injured.
	 *
	 * @return boolean to tell if the individual is injured.
	 **/
	public boolean isInjured() 
	{ 
		return injured; 
	}

	/**
	 * Allows an individual to be injured or healed.
	 *
	 * @param val Represents either injuring or reactivating an individual.
	 **/	
    public void setInjured( boolean val )
    { 
    	injured = val; 
    }

	/**
	 * Gets the Velocity of the individual at the next timestep.
	 *
	 * @return The Velocity of the individual as a 2 Dimensional Vector.
	 **/
	public Vector2D getNextVelocity()
	{
		return this.nextVelocity;
	}
	
	/**
	 * Sets the Velocity of the individual for the next timestep.
	 *
	 * @param xVal the X component of the velocity.
	 * @param yVal the Y component of the velocity.
	 **/
	public void setNextVelocity( double xVal, double yVal )
	{
		Vector2D newVel = new Vector2D( xVal, yVal );
		this.setNextVelocity( newVel );
	}
	
	/**
     * Sets the Velocity of the individual for the next timestep.
	 *
	 * Revised by DJK on 10/16/10 to use the atan correctly.
     *  Depending on how Orientation is defined, it looks like arguements in
     *  original version were incorrect (atan(x,y)).
     *
	 * @param val The Velocity of the individual.
	 **/
	public void setNextVelocity( Vector2D val )
	{
		this.nextVelocity = val;
		setOrientation( Math.atan2( val.y, val.x ) );
	}
	
	/**
     * Returns the Color of the individual.  The color only affects how an individual
     * is drawn to the screen.
     *
     * @return The color of the individual.
     **/
	public Color getColor()
	{
		return color;	
	}
	
	/**
	 * Sets the individual's color to a specific color.
	 *
	 * @param val The color of the individual.
	 **/
	public void setColor(Color val)
	{
		color = val;
	}
	
	/**
     * Returns the individual's image.
     *
     * @return The image of the individual.
     **/
	public Image getImage()
	{
		return image;	
	}
	
	/**
	 * Sets the individual's image.
	 *
	 * @param val The image of the individual.
	 **/
	public void setImage(Image val)
	{
		image = val;
	}
	
	/**
     * Returns status of the flag which indicates if the bounding box of
     * the individual's image will be drawn.
     *
     * @return The status of the drawImageBoundingBox flag.
     **/
	public boolean isImageBoundingBox()
	{
		return imageBoundingBox;	
	}
	
	/**
	 * Sets the flag indiciating if the image of the individual should draw it's bounding box.
	 *
	 * @param val The flag indicating if the bounding box should be drawn.
	 **/
	public void setImageBoundingBox(boolean val)
	{
		imageBoundingBox = val;
	}
	
	/**
     * Returns status of the flag indicating if the interaction circle should be drawn on top
	 * of an individual's image.
     *
     * @return The status of the drawInteractionCircle flag.
     **/
	public boolean isInteractionCircle()
	{
		return interactionCircle;	
	}
	
	/**
	 * Sets the flag indicating if the interaction circle should be drawn on top
	 * of an individual's image.
	 *
	 * @param val The flag indicating if the interaction circle should be drawn on top
	 * of an individual's image.
	 **/
	public void setInteractionCircle(boolean val)
	{
		interactionCircle = val;
	}

	/**
	 * Gets the forceValues of the individual
	 *
	 * @return The forceValues of the individual.
	 **/
	public String getForceValues()
	{
		return forceValues;
	}
	
	/**
	 * Sets the forceValues of the individual
	 *
	 * @param val The new forveValues for the individual.
	 **/
	public void setForceValues( String val )
	{
		this.forceValues = val;
	}
	
	/**
	 * Gets the action strategy of the individual.
	 *
	 * @return The ActionStrategy of the individual.
	 **/
	public ActionStrategy getActionStrategy()
	{
		return this.actionStrategy;	
	}

	/**
	 * Sets the ActionStrategy of the individual.
	 *
	 * @param val The ActionStrategy to be assigned to the individual.
	 **/
	public void setActionStrategy( ActionStrategy val)
	{
		this.actionStrategy = val;
		if( val != null )
		{
			this.actionStrategy.setIndividual(this);
		}
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
	
	/**
	 * Gets the type of the Individual.
	 *
	 * @return The type of the Individual.
	 **/	
	public String getType()
	{
		return type;
	}
	
	/**
	 * Sets the type of the Individual.
	 *
	 * @param val The type of the Individual.
	 **/
	public void setType( String val )
	{
		this.type = val;
	}
	
	/**
	 * Gets the energy of the Individual.
	 *
	 * @return The energy of the Individual.
	 **/	
	public double getEnergy()
	{
		return energy;
	}
	
	/**
	 * Sets the Energy of the Individual.
	 *
	 * @param val The energy of the Individual.
	 **/
	public void setEnergy( double val )
	{
		this.energy = val;
	}

	/**
	 * Gets the radiusOfConcern of the Individual.
	 *
	 * @return The radiusOfConcern of the Individual.
	 **/	
	public double getRadiusOfConcern()
	{
		return radiusOfConcern;
	}
	
	/**
	 * Sets the radiusOfConcern of the Individual.
	 *
	 * @param val The radiusOfConcern of the Individual.
	 **/
	public void setRadiusOfConcern( double val )
	{
		this.radiusOfConcern = val;
	}
	
	/**
	 * Gets the selected path of the Individual.
	 *
	 * @return The selected path of the Individual.
	 **/	
	public Path getSelectedPath()
	{
		return selectedPath;
	}
	
	/**
	 * Sets the selected path of the Individual.
	 *
	 * @param val The selected path of the Individual.
	 **/
	public void setSelectedPath( Path val )
	{
		this.selectedPath = val;
	}

	/**
	 * Sets if the individual is dead.
	 *
	 * @param val Is the indiviudal dead?
	 **/	
	public void setDead( boolean val )
	{
		dead = val;
	}
	
	/**
	 * Sets if the individual is dead.
	 *
	 * @return boolean Is the indiviudal dead?
	 **/
	public boolean isDead()
	{
		return dead;
	}
	
	/**
	 * Gets the maximum compressions for the individual.
     *
	 * @return double The maximum compressions for the individual
	 **/
	public double getsMax()
	{
		return sMax;
	}

	/**
	 * Sets the maximum compressions for the individual.
	 *
	 	 * @param val The maximum compressions for the individual
	 **/
	public void setsMax( double val )
	{
		sMax = val;
	}
	
	/**
	 * Gets the maximum compressions for the individual.
	 *
	 * @return double The maximum compressions for the individual
	 **/
	public double getMaxCompression()
	{
		return sMax;
	}
	
	/**
	 * Gets the vector representing to center line of the view.
	 *
	 * @return Vector2D The vectore of the centerline of the view.
	 **/
	public Vector2D getGaze()
	{ 
		return gaze;
	}
	
	/**
	 * Sets the vector representing to center line of the view.
	 *
	 * @param val The vectore of the centerline of the view.
	 **/
	public void setGaze( Vector2D val )
	{
		this.gaze = val;
	}
	
	/**
	 * Gets the center of gaze as an angle.
	 *
	 * @return double The radian representation of the angle of the view of the indiviudal.
	 **/
	public double getGazeAngle()
	{
		return Trig.atan2(gaze.y, gaze.x);
	}
	
    /**
     * Calculates the direction which the individual is looking at/heading towards.
     *
     * @return The angle of the direction in Radians.
     **/
    public double getOrientation()
    {
    	return orientation;
    }

    /**
     * Sets the direction and momentum which the individual is looking at/heading towards.
     *
     * @param val The angle of the direction in Radians.
     **/
    public void setOrientation( double val )
    {
    	orientation = val;
    }
	
}
