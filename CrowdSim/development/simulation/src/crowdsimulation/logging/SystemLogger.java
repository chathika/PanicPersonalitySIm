/*
 * $RCSfile: SystemLogger.java,v $ $Date: 2008/06/10 17:47:08 $
 */
package crowdsimulation.logging;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;	// Contains models of behavior for the entities. 
import crowdsimulation.entities.*;		// Contains definition of the crowd agents and features.
import crowdsimulation.entities.individual.*;
import java.io.*;
import java.util.*;
import sim.engine.*;
import sim.util.*;

/**
 * The class prints out the data for a given timestep, for all individuals.
 * This logger should be used sparing, since it generates incredibly large
 * amounts of data. This can be usefull in debuging and creating system 
 * enhancements.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.2 $
 * $State: Exp $
 * $Date: 2008/06/10 17:47:08 $
 **/
public class SystemLogger extends Logger
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** Do we include the time of execution for the simulation. **/
	private boolean timing = true;
	/** Do we list all of the parameters for an individual. **/
	private boolean parameters = true;
	/** Is the location of the individual included? **/
	private boolean location = true;
	/** Is the orientation of the individual included? **/
	private boolean orientation = true;
	/** Is the momentum of the individual included? **/
	private boolean momentum = true;
	/** Do we include the velocity of the individual? **/
	private boolean velocity = true;
	/** Should the model type being used be included? This is necessary to see the forces acting on the bodies. **/
	private boolean model = true;
	/** When displaying the forces are all shown or just the one being used? **/
	private boolean allForces = false;
	/** Do we want to see what other Obstacles were interaction with the individuals during this step? **/ 
	private boolean objInteraction = true;
	/** Do we want to see what other individuals were interaction with the individuals during this step?  **/
	private boolean indInteractions = true;
	/** The name of the model the data is being recorded for. This is defaulted to HMFVModel. **/
	private String nameOfModel = HMFVModel.class.getName();
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs the system logger given a file and the individuals to collect the data for.
	 *
	 * @param fileName The name of the file to store the collected data in. This should also have any directory info along with it.
	 **/
	public SystemLogger( String fileName )
	{
		super( fileName );
	}
	
	/**
	 * This initializes the timing information and constructs the header (Field names).
	 **/
	public void init()
	{		
		Bag headerSet = new Bag();
		headerSet.add( "time" );
		headerSet.add( "stepNum" );
		headerSet.add( "dt" );
		headerSet.add( "agentNum" );
		
		if( timing )
		{
			headerSet.add( "runningTime" );
		}
		if( parameters )
		{
			headerSet.add( "diameter" );
		}
		if( orientation )
		{
			headerSet.add( "orientation" );
		}
		if( location )
		{
			headerSet.add( "location.x" );
			headerSet.add( "location.y" );
		}
		if( velocity )
		{
			headerSet.add( "velocity.x" );
			headerSet.add( "velocity.y" );
		}
		if( momentum )
		{
			headerSet.add( "momentum.x" );
			headerSet.add( "momentum.y" );
		}
		if( model )
		{
			headerSet.add( "Model" );
			if( allForces )
			{
				headerSet.add( FlockingModel.forceNames );
				headerSet.add( HMFVSocialModel.forceNames );
				headerSet.add( HMFVModel.forceNames );
			}
			else
			{
				if( nameOfModel.equals( FlockingModel.class.getName() ) )
				{
					headerSet.add( FlockingModel.forceNames );
				}
				if( nameOfModel.equals( SocialPotentialModel.class.getName() ) )
				{
					headerSet.add( HMFVSocialModel.forceNames );
				}
				if( nameOfModel.equals( HMFVModel.class.getName() ) )
				{
					headerSet.add( HMFVModel.forceNames );
				}
			}
			
			if( objInteraction )
			{
				headerSet.add( "Interacting Obstacles" );
			}
			if( indInteractions )
			{
				headerSet.add( "Interacting Individuals" );
			}			
		}
		setHeader( (String[])headerSet.toArray() );
		setHeaderWritten( false );
	}
	
	//TODO: Should be moved to a utility function
	/**
	 * Converts a collection of objects to a string of the name of the objects delimeted by the delimeter.
	 *
	 * @param collection The collection to be converted to a string.
	 * @param delimeter The delimeter to be used to seperate elemenets of the collection.
	 * @return The String representing the memebers of the collection and seperated by delimeter.
	 **/
	public String bagToString( Bag collection, String delimeter )
	{
		String data = "";
		
		for( int i =0; i < collection.numObjs-1; i++ )
		{
			data += collection.get(i).toString() + delimeter;
		}
		if( collection.numObjs > 0 )
		{
			data += collection.get( collection.numObjs-1 ).toString();
		}
		
		return data;
	}
	
	/**
	 * Write the current data to the file.
	 * Writes the data as a delimeted series of text to the file which this log is assocaited with.
	 **/
	public void log()
	{
		super.log();

		CrowdSimulation state = CrowdSimulation.getInstance();
		
		Bag individuals = state.getWorld().getAllObjects();
		
		Date currentTime = new Date();

		for( int i =0; i < individuals.numObjs; i++ )
		{
			Individual ind = (Individual)individuals.get( i );
			double dt = state.getDeltaT();
			
			// The StringBuilder class can be replaced by the StringBuffer class if needed.
			StringBuffer data = new StringBuffer();
			data.append( state.getSimTime() );
			data.append( "," );
			data.append( state.schedule.getSteps() );
			data.append( "," );
			data.append( dt );
			data.append( "," );
			data.append( ind.getID() );
			
			if( timing )
			{
				data.append( "," );
				data.append( (currentTime.getTime() - CrowdSimulation.getInstance().getStartTime().getTime()) );
			}
			if( parameters )
			{
				data.append( "," );
				data.append( ind.getDiameter() );
			}
			if( orientation )
			{
				data.append( "," );
				data.append( ind.getOrientation() );
			}
			if( location )
			{
				data.append( "," );
				data.append( (ind.getLocation()).x );
				data.append( "," );
				data.append( (ind.getLocation()).y );
			}
			if( velocity )
			{
				data.append( "," );
				data.append( (ind.getVelocity()).x );
				data.append( "," );
				data.append( (ind.getVelocity()).y );
			}
			if( momentum )
			{
				data.append( "," );
				data.append( (ind.getMomentum()).x );
				data.append( "," );
				data.append( (ind.getMomentum()).y );
			}
			if( model )
			{

				String modelName = ind.getActionController().getClass().getName();
				data.append( "," );
				data.append( modelName );
				
				if( modelName.equals( FlockingModel.class.getName() ) )
				{
					data.append( "," );
					data.append( ind.getForceValues() );
				}
				else if( allForces )
				{
					data.append( "," );
					data.append( FlockingModel.forceValues );
				}
				if( modelName.equals( SocialPotentialModel.class.getName() ) )
				{
					data.append( "," );
					data.append( ind.getForceValues() );
				}
				else if( allForces )
				{
					data.append( "," );
					data.append( HMFVSocialModel.forceValues );
				}
				if( modelName.equals( HMFVModel.class.getName() ) )
				{
					data.append( "," );
					data.append( ind.getForceValues() );
				}
				else if( allForces )
				{
					data.append( "," );
					data.append( HMFVModel.forceValues ); 
				}
				if( objInteraction )
				{
					data.append( "," );
					data.append( bagToString( ind.getInteractingObstacles(), " " ) );
				}
				if( indInteractions )
				{
					data.append( "," );
					data.append( bagToString( ind.getInteractingIndividuals(), " " ) );
				}
			}
			
			write( data.toString() );
		}		
		
	}
	
	/**
	 * Sets all the parameters from a key value pairs collection.
	 *
	 * @param parameters The parameters for the logger.
	 **/
	public void setParameters( Map parameters )
	{
		super.setParameters( parameters );

		if( parameters.containsKey( "timing" ) )
		{
			Object value = parameters.get( "timing" );
			boolean val = false;
				
			if( value instanceof String )
			{
				val =  Boolean.getBoolean( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Boolean)value).booleanValue();
			}
			else
			{
				Log.log( 1, " Timing for the loger must be a Boolena or a string representing a Boolean.");
			}
			setTiming( val );
		}
		if( parameters.containsKey( "parameters" ) )
		{
			Object value = parameters.get( "parameters" );
			boolean val = false;
				
			if( value instanceof String )
			{
				val =  Boolean.getBoolean( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Boolean)value).booleanValue();
			}
			else
			{
				Log.log( 1, " Parameters for the loger must be a Boolena or a string representing a Boolean.");
			}
			setParameters( val );
		}
		if( parameters.containsKey( "parameters" ) )
		{
			Object value = parameters.get( "parameters" );
			boolean val = false;
				
			if( value instanceof String )
			{
				val =  Boolean.getBoolean( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Boolean)value).booleanValue();
			}
			else
			{
				Log.log( 1, " Parameters for the loger must be a Boolena or a string representing a Boolean.");
			}
			setParameters( val );
		}
		if( parameters.containsKey( "location" ) )
		{
			Object value = parameters.get( "location" );
			boolean val = false;
				
			if( value instanceof String )
			{
				val =  Boolean.getBoolean( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Boolean)value).booleanValue();
			}
			else
			{
				Log.log( 1, " Location for the loger must be a Boolena or a string representing a Boolean.");
			}
			setLocation( val );
		}
		if( parameters.containsKey( "orientation" ) )
		{
			Object value = parameters.get( "orientation" );
			boolean val = false;
				
			if( value instanceof String )
			{
				val =  Boolean.getBoolean( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Boolean)value).booleanValue();
			}
			else
			{
				Log.log( 1, " Orientation for the loger must be a Boolena or a string representing a Boolean.");
			}
			setOrientation( val );
		}
		if( parameters.containsKey( "allForces" ) )
		{
			Object value = parameters.get( "allForces" );
			boolean val = false;
				
			if( value instanceof String )
			{
				val =  Boolean.getBoolean( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Boolean)value).booleanValue();
			}
			else
			{
				Log.log( 1, " AllForces for the loger must be a Boolena or a string representing a Boolean.");
			}
			setAllForces( val );
		}
		if( parameters.containsKey( "momentum" ) )
		{
			Object value = parameters.get( "momentum" );
			boolean val = false;
				
			if( value instanceof String )
			{
				val =  Boolean.getBoolean( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Boolean)value).booleanValue();
			}
			else
			{
				Log.log( 1, " Momentum for the loger must be a Boolena or a string representing a Boolean.");
			}
			setMomentum( val );
		}
		if( parameters.containsKey( "velocity" ) )
		{
			Object value = parameters.get( "velocity" );
			boolean val = false;
				
			if( value instanceof String )
			{
				val =  Boolean.getBoolean( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Boolean)value).booleanValue();
			}
			else
			{
				Log.log( 1, " Velocity for the loger must be a Boolena or a string representing a Boolean.");
			}
			setVelocity( val );
		}
		if( parameters.containsKey( "model" ) )
		{
			Object value = parameters.get( "model" );
			boolean val = false;

			if( value instanceof String )
			{
				val =  Boolean.getBoolean( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Boolean)value).booleanValue();
			}
			else
			{
				Log.log( 1, " Model for the loger must be a Boolena or a string representing a Boolean.");
			}
			setModel( val );
		}
		if( parameters.containsKey( "allForces" ) )
		{
			Object value = parameters.get( "allForces" );
			boolean val = false;
				
			if( value instanceof String )
			{
				val =  Boolean.getBoolean( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Boolean)value).booleanValue();
			}
			else
			{
				Log.log( 1, " AllForces for the loger must be a Boolena or a string representing a Boolean.");
			}
			setAllForces( val );
		}
		if( parameters.containsKey( "objInteraction" ) )
		{
			Object value = parameters.get( "objInteraction" );
			boolean val = false;
				
			if( value instanceof String )
			{
				val =  Boolean.getBoolean( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Boolean)value).booleanValue();
			}
			else
			{
				Log.log( 1, " ObjInteraction for the loger must be a Boolena or a string representing a Boolean.");
			}
			setObjInteraction( val );
		}
		if( parameters.containsKey( "indInteractions" ) )
		{
			Object value = parameters.get( "indInteractions" );
			boolean val = false;
				
			if( value instanceof String )
			{
				val =  Boolean.getBoolean( (String)value );
			}
			else if( value instanceof Double )
			{
				val = ((Boolean)value).booleanValue();
			}
			else
			{
				Log.log( 1, " IndInteractions for the loger must be a Boolena or a string representing a Boolean.");
			}
			setIndInteractions( val );
		}		

	}	
	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Sets if the logger is supposed to record the time.
	 *
	 * @param val The value of if the timing is to be recorded.
	 **/
	public void setTiming( boolean val )
	{
		timing = val;
	}
	
	/**
	 * Gets if the logger is supposed to record the time.
	 *
	 * @return The value of if the timing is to be recorded.
	 **/
	public boolean getTiming()
	{
		return timing;
	}
	
	/**
	 * Sets if the logger is supposed to record the parameters.
	 *
	 * @param val The value of if the parameters are to be recorded.
	 **/
	public void setParameters( boolean val )
	{
		parameters = val;
	}
	
	/**
	 * Gets if the logger is supposed to record the parameters.
	 *
	 * @return The value of if the parameters are to be recorded.
	 **/
	public boolean getParameters()
	{
		return parameters;
	}
	
	/**
	 * Sets if the logger is supposed to record the location of the entities.
	 *
	 * @param val The value of if the locations are to be recorded.
	 **/
	public void setLocation( boolean val )
	{
		location = val;
	}
	
	/**
	 * Gets if the logger is supposed to record the location of the entities.
	 *
	 * @return The value of if the locations are to be recorded.
	 **/
	public boolean getLocation()
	{
		return location;
	}
	
	/**
	 * Sets if the logger is supposed to record the orientation of the entities.
	 *
	 * @param val The value of if the orientations are to be recorded.
	 **/
	public void setOrientation( boolean val )
	{
		orientation = val;
	}
	
	/**
	 * Gets if the logger is supposed to record the orientation of the entities.
	 *
	 * @return The value of if the orientations are to be recorded.
	 **/
	public boolean getOrintation()
	{
		return orientation;
	}
	
	/**
	 * Sets if the logger is supposed to record the momentum of the entities.
	 *
	 * @param val The value of if the momentums are to be recorded.
	 **/
	public void setMomentum( boolean val )
	{
		momentum = val;
	}
	
	/**
	 * Gets if the logger is supposed to record the momentum of the entities.
	 *
	 * @return The value of if the momentums are to be recorded.
	 **/
	public boolean getMomentum()
	{
		return momentum;
	}
	
	/**
	 * Sets if the logger is supposed to record the velocities of the entities.
	 *
	 * @param val The value of if the velocities are to be recorded.
	 **/
	public void setVelocity( boolean val )
	{
		velocity = val;
	}
	
	/**
	 * Gets if the logger is supposed to record the velocities of the entities.
	 *
	 * @return The value of if the velocities are to be recorded.
	 **/
	public boolean getVelocity()
	{
		return velocity;
	}
	
	/**
	 * Sets if the logger is supposed to record the model of the entities.
	 *
	 * @param val The value of if the models are to be recorded.
	 **/
	public void setModel( boolean val )
	{
		model = val;
	}
	
	/**
	 * Gets if the logger is supposed to record the model of the entities.
	 *
	 * @return The value of if the models are to be recorded.
	 **/
	public boolean getModel()
	{
		return model;
	}
	
	/**
	 * Sets if the logger is supposed to record all the forces from the model of the entities.
	 *
	 * @param val The value of if all the forces from the models are to be recorded.
	 **/
	public void setAllForces( boolean val )
	{
		allForces = val;
	}
	
	/**
	 * Gets if the logger is supposed to record all the forces from the model of the entities.
	 *
	 * @return The value of if all the forces from the models are to be recorded.
	 **/
	public boolean getAllForces()
	{
		return allForces;
	}
	
	/**
	 * Sets if the logger is supposed to record all object interactions from the model of the entities.
	 *
	 * @param val The value of if all object interactions from the models are to be recorded.
	 **/
	public void setObjInteraction( boolean val )
	{
		objInteraction = val;
	}
	
	/**
	 * Gets if the logger is supposed to record all object interactions from the model of the entities.
	 *
	 * @return The value of if all object interactions from the models are to be recorded.
	 **/
	public boolean getObjInteraction()
	{
		return objInteraction;
	}
	
	/**
	 * Sets if the logger is supposed to record all individual interactions from the model of the entities.
	 *
	 * @param val The value of if all individual interactions from the models are to be recorded.
	 **/
	public void setIndInteractions( boolean val )
	{
		indInteractions = val;
	}
	
	/**
	 * Gets if the logger is supposed to record all individual interactions from the model of the entities.
	 *
	 * @return The value of if all individual interactions from the models are to be recorded.
	 **/
	public boolean getIndInteractions()
	{
		return indInteractions;
	}
	
}
