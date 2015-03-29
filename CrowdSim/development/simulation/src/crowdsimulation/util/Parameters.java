/*
 * $RCSfile: Parameters.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package crowdsimulation.util;

import crowdsimulation.logging.*;
import java.lang.reflect.*;
import math.*;

/** 
 * Helper class to deal with parameters for constructing entities in the simulation.
 * This is a helper clas used by AppConfig to convert the data read in from the 
 * config files to something that can be placed in the paramters Map to create
 * the different entities.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class Parameters
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/**
	 * This will convert a object from a config file to a double.
	 *
	 * @param value The object representing the value.
	 * @param errorMessage The message to display if the value cannot be pulled out of the object.
	 * @return The double represented by the value passed in.
	 **/
	public static double convertToDouble( Object value, String errorMessage )
	{
		return convertToDouble( value, 0, true, errorMessage );
	}

	/**
	 * This will convert a object from a config file to a double.
	 *
	 * @param value The object representing the value.
	 * @param randomGensAllowed This states if random gereators should be allowed for this value.
	 * @param errorMessage The message to display if the value cannot be pulled out of the object.
	 * @return The double represented by the value passed in.
	 **/
	public static double convertToDouble( Object value, boolean randomGensAllowed, String errorMessage )
	{
		return convertToDouble( value, 0, randomGensAllowed, errorMessage );
	}

	/**
	 * This will convert a object from a config file to a double.
	 *
	 * @param value The object representing the value.
	 * @param defaultVal A default value if the object doesn't contain a valid value.
	 * @param errorMessage The message to display if the value cannot be pulled out of the object.
	 * @return The double represented by the value passed in.
	 **/
	public static double convertToDouble( Object value, double defaultVal, String errorMessage )
	{
		return convertToDouble( value, defaultVal, true, errorMessage );
	}
	
	/**
	 * This will convert a object from a config file to a double.
	 *
	 * @param value The object representing the value.
	 * @param defaultVal A default value if the object doesn't contain a valid value.
	 * @param randomGensAllowed This states if random gereators should be allowed for this value.
	 * @param errorMessage The message to display if the value cannot be pulled out of the object.
	 * @return The double represented by the value passed in.
	 **/
	public static double convertToDouble( Object value, double defaultVal, boolean randomGensAllowed, String errorMessage )
	{
		double retValue = 0.0;
		
		if( value instanceof String )
		{
			retValue = Double.parseDouble( (String)value );
		}
		else if( value instanceof Double )
		{
			retValue = ((Double)value).doubleValue();
		}
		else if( randomGensAllowed && value instanceof RandomGenerators )
		{
			retValue = ((RandomGenerators)value).nextValue();
		}
		else
		{
			Log.log( 1, errorMessage );
			if( randomGensAllowed )
			{
				Log.log( 1, "Random Generators are also valid.");
			}
			else if( value instanceof RandomGenerators )
			{
				Log.log( 1, "Random Generators are not valid for this parameter.");	
			}
			
			retValue = defaultVal;
		}
		
		return retValue;	
	}

	/**
	 * This will convert a object from a config file to a float.
	 *
	 * @param value The object representing the value.
	 * @param errorMessage The message to display if the value cannot be pulled out of the object.
	 * @return The float represented by the value passed in.
	 **/
	public static float convertToFloat( Object value, String errorMessage )
	{
		return convertToFloat( value, 0, true, errorMessage );
	}

	/**
	 * This will convert a object from a config file to a float.
	 *
	 * @param value The object representing the value.
	 * @param randomGensAllowed This states if random gereators should be allowed for this value.
	 * @param errorMessage The message to display if the value cannot be pulled out of the object.
	 * @return The float represented by the value passed in.
	 **/
	public static float convertToFloat( Object value, boolean randomGensAllowed, String errorMessage )
	{
		return convertToFloat( value, 0, randomGensAllowed, errorMessage );
	}
	
	/**
	 * This will convert a object from a config file to a float.
	 *
	 * @param value The object representing the value.
	 * @param defaultVal A default value if the object doesn't contain a valid value.
	 * @param errorMessage The message to display if the value cannot be pulled out of the object.
	 * @return The float represented by the value passed in.
	 **/
	public static float convertToFloat( Object value, float defaultVal, String errorMessage )
	{
		return convertToFloat( value, defaultVal, true, errorMessage );
	}


	/**
	 * This will convert a object from a config file to a float.
	 *
	 * @param value The object representing the value.
	 * @param defaultVal A default value if the object doesn't contain a valid value.
	 * @param randomGensAllowed This states if random gereators should be allowed for this value.
	 * @param errorMessage The message to display if the value cannot be pulled out of the object.
	 * @return The float represented by the value passed in.
	 **/
	public static float convertToFloat( Object value, float defaultVal, boolean randomGensAllowed, String errorMessage )
	{
		float retValue = 0;
		
		if( value instanceof String )
		{
			retValue = Float.parseFloat( (String)value );
		}
		else if( value instanceof Float )
		{
			retValue = ((Float)value).floatValue();
		}
		else if( randomGensAllowed && value instanceof RandomGenerators )
		{
			retValue = (float)((RandomGenerators)value).nextValue();
		}
		else
		{
			Log.log( 1, errorMessage );
			if( randomGensAllowed )
			{
				Log.log( 1, "Random Generators are also valid.");
			}
			else if( value instanceof RandomGenerators )
			{
				Log.log( 1, "Random Generators are not valid for this parameter.");	
			}
			
			retValue = defaultVal;
		}
		
		return retValue;	
	}

	/**
	 * This will convert a object from a config file to an int.
	 *
	 * @param value The object representing the value.
	 * @param errorMessage The message to display if the value cannot be pulled out of the object.
	 * @return The int represented by the value passed in.
	 **/
	public static int convertToInt( Object value, String errorMessage )
	{
		return convertToInt( value, 0, true, errorMessage );
	}

	/**
	 * This will convert a object from a config file to an int.
	 *
	 * @param value The object representing the value.
	 * @param randomGensAllowed This states if random gereators should be allowed for this value.
	 * @param errorMessage The message to display if the value cannot be pulled out of the object.
	 * @return The int represented by the value passed in.
	 **/
	public static int convertToInt( Object value, boolean randomGensAllowed, String errorMessage )
	{
		return convertToInt( value, 0, randomGensAllowed, errorMessage );
	}
	
	/**
	 * This will convert a object from a config file to an int.
	 *
	 * @param value The object representing the value.
	 * @param defaultVal A default value if the object doesn't contain a valid value.
	 * @param errorMessage The message to display if the value cannot be pulled out of the object.
	 * @return The int represented by the value passed in.
	 **/
	public static int convertToInt( Object value, int defaultVal, String errorMessage )
	{
		return convertToInt( value, defaultVal, true, errorMessage );
	}

	/**
	 * This will convert a object from a config file to an int.
	 *
	 * @param value The object representing the value.
	 * @param defaultVal A default value if the object doesn't contain a valid value.
	 * @param randomGensAllowed This states if random gereators should be allowed for this value.
	 * @param errorMessage The message to display if the value cannot be pulled out of the object.
	 * @return The int represented by the value passed in.
	 **/
	public static int convertToInt( Object value, int defaultVal, boolean randomGensAllowed, String errorMessage )
	{
		int retValue = 0;
		
		if( value instanceof String )
		{
			retValue = Integer.parseInt( (String)value );
		}
		else if( value instanceof Integer )
		{
			retValue = ((Integer)value).intValue();
		}
		else if( randomGensAllowed && value instanceof RandomGenerators )
		{
			retValue = (int)((RandomGenerators)value).nextValue();
		}
		else
		{
			Log.log( 1, errorMessage );
			if( randomGensAllowed )
			{
				Log.log( 1, "Random Generators are also valid.");
			}
			else if( value instanceof RandomGenerators )
			{
				Log.log( 1, "Random Generators are not valid for this parameter.");	
			}
			
			retValue = defaultVal;
		}
		
		return retValue;	
	}


	/**
	 * This will convert a object from a config file to an Object.
	 *
	 * @param value The object representing the value.
	 * @param defaultVal A default value if the object doesn't contain a valid value.
	 * @param errorMessage The message to display if the value cannot be pulled out of the object.
	 * @return The Object represented by the value passed in.
	 **/
	public static Object convertToObject( Object value, Object defaultVal, String errorMessage )
	{
		Object retValue = 0;
		Class parentType = defaultVal.getClass().getSuperclass();
		
		if( value instanceof String )
		{
			Object[] params = {};
			Class[] classEmptySet = {};
			Object[] objectEmptySet = {};
			
			try
			{
				String packageDesc = parentType.getPackage().getName();
				Class modelClass = Class.forName( packageDesc + "." +value );
				Constructor modelCreator = modelClass.getConstructor( classEmptySet );
				retValue = modelCreator.newInstance( objectEmptySet );
			}
			catch( Exception e )
			{
				System.out.println( "Class Not Found : crowdsimulation.actioncontroller. " + value   );
				retValue = defaultVal;
				e.printStackTrace();
			}
		}
		else if( value.getClass().getSuperclass() == parentType )
		{
			retValue = value;
		}
		else
		{
			Log.log( 1, errorMessage );
				
			retValue = defaultVal;
		}
		
		return retValue;	
	}	
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	 
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

}