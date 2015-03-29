/*
 * $RCSfile: Trig.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package math; 
 
import java.lang.*;
import java.util.*;

/**
 * Trig class essentially wraps the existing Math class trig functions so
 * that the user is alerted if inverse methods return arguments outside the
 * desired range. 
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class Trig
{
	/** redefines a local varaible PI to be equal to java.lang.Math.PI. **/
	private static double PI = Math.PI;
	
	/** Defines the range for arcCos to be 0 to PI. **/
	private static double[] acosRange =  {0 , PI};
	/** Defines the range for arcSin to be -PI/2 to PI/2. **/
	private static double[] asinRange =  {-PI/2, PI/2};
	/** Defines the range for arcTan to be 0 to PI/2. **/
	private static double[] atanRange =  {0, PI/2};
	/** Defines the range for arcTan2 to be -PI to PI. **/
	private static double[] atan2Range = {-PI, PI};
	
	/**
	 * Wrapper for cos and gives a warning if the the argument is not between 0 and PI.
	 *
	 * @param arg The angle to take the cosine of.
	 * @return The Cosine of the argument.
	 **/
	public static double cos( double arg )
	{ 
		if( rangeExceeded( acosRange, arg ) )
		{
			warn();
		}
		return Math.cos( arg );
	}
	
	/**
	 * Wrapper for sin and gives a warning if the the argument is not between -PI/2 and PI/2.
	 *
	 * @param arg The angle to take the sine of.
	 * @return The sine of the argument.
	 **/
	public static double sin( double arg )
	{ 
		if( rangeExceeded( asinRange, arg) )
		{
			warn();
		}
		return Math.sin( arg );
	}
	
	/**
	 * Wrapper for tan and gives a warning if the the argument is not between 0 and PI/2.
	 *
	 * @param arg The angle to take the tangent of.
	 * @return The tangent of the argument.
	 **/
	public static double tan( double arg )
	{ 
		if( rangeExceeded( atanRange, arg ) )
		{
		 	warn();
		}
		return Math.tan( arg );
	}
	
	/**
	 * Wrapper for arccos and gives a warning if the the result is not between 0 and PI.
	 *
	 * @param value The value to fine the angle of using the arccosine function.
	 * @return The Angle found from the arccosine function.
	 **/
	public static double acos( double value )
	{ 
		double argument = Math.acos( value );
	   	if( rangeExceeded( acosRange, argument ) )
	   	{
			warn();
		}
		return argument;
	}
	
	/**
	 * Wrapper for arcsin and gives a warning if the the result is not between -PI/2 and PI/2.
	 *
	 * @param value The value to fine the angle of using the arcsine function.
	 * @return The Angle found from the arcsine function.
	 **/
	public static double asin( double value )
	{ 
		double argument = Math.asin( value );
	  	if( rangeExceeded( asinRange, argument ) )
	  	{
	    	warn();
	    }
	    return argument;
	}
	
	/**
	 * Wrapper for arctan and gives a warning if the the result is not between 0 and PI/2.
	 *
	 * @param value The value to fine the angle of using the arctangent function.
	 * @return The Angle found from the arctangent function.
	 **/
	public static double atan( double value )
	{  
		double argument = Math.atan( value );
	 	if( rangeExceeded( atanRange, argument ) )
	 	{
	   		warn();
	   	}
	   	return argument;
	}
	
	/**
	 * Wrapper for atan2 and gives a warning if the the result is not between -PI and PI.
	 *
	 * @param x The witdh of the triangle to find the angle of using the arctangent function.
	 * @param y The height of the triangle to find the angle of using the arctangent function.
	 * @return The Angle found from the arctangent function.
	 **/
	public static double atan2( double x , double y )
	{ 
		double argument = Math.atan2( x, y );
		
	  	if( rangeExceeded( atan2Range, argument ) )
	  	{
	   		warn();
	   	}
	   	return argument;
	}
	
	/**
	 * Method to print out the warning stating that the argument range was exceeded.
	 **/
	private static void warn()
	{ 
		System.out.println( "Exceeded Argument Range" );
	}

	/**
	 * Test to see if the argument falls outside of the defined range.
	 *
	 * @param range The range to be tested against.
	 * @param argument The argument to see if it falls outside of the passed in range.
	 * @return The boolean value representing if it has fallen outside of the range.
	 **/
	private static boolean rangeExceeded( double[] range , double argument)
	{
		if((argument< range[0]) || (argument> range[1]))
		{
			return true;
		}
		return false;
	}


}	
	