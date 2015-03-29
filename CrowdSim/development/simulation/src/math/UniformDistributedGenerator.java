/*
 * $RCSfile: UniformDistributedGenerator.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package math;

/**
 * Generator to generate random numbers with a Uniform Distribution.
 * The min and max values need to be set on the generator
 * before a random number is generated.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class UniformDistributedGenerator extends RandomGenerators
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The minimum value of for the uniform distribution. **/
	double min = 0;
	/** The maximum value of for the uniform distribution. **/
	double max = 1;	
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Constructs a Generator for Uniform Distributed random numbers with given min and max values.
	 *
	 * @param minVal The minimum value for the distribution of the random numbers.
	 * @param maxVal The maximum value of trials to be used for the generation of the random number.
	 **/    
    public UniformDistributedGenerator( double minVal, double maxVal )
    {
		super();
		setMin( minVal );
		setMax( maxVal );
    }
    
	/**
	 * Constructs a Generator for Uniform Distributed random numbers with given min and max values.
	 *
	 * @param seedVal The seed for the generators. This is only ussed if no seed has previously been set for any random generator.
	 * @param minVal The minimum value for the distribution of the random numbers.
	 * @param maxVal The maximum value of trials to be used for the generation of the random number.
	 **/
    public UniformDistributedGenerator( int seedVal, double minVal, double maxVal )
    {
		super( seedVal );
		setMin( minVal );
		setMax( maxVal );
    }
    
    /**
	 * Returns the next random number with a Uniform Distribution. This uses the 
	 * min and max values set in the class instance.
	 *
	 * @return A randomly generated number from a uniform distribution.
	 **/
	public double nextValue()
	{
		double val = rand1();
		double dist = max - min;
		return (val*dist)+min;
	} 
	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Sets the Minimum value for the generator.
	 *
	 * @param minVal The minimum value for the distribution.
	 **/
	public void setMin( double minVal )
	{
		min = minVal;
	}
	
	/**
	 * Gets the Minimum value for the generator.
	 *
	 * @return The minimum value for the distribution.
	 **/
	public double getMin()
	{
		return min;
	}
	
	/**
	 * Sets the Maximum value for the generator.
	 *
	 * @param maxVal The maximum value for the distribution.
	 **/
	public void setMax( double maxVal )
	{
		max = maxVal;
	}
	
	/**
	 * Gets the Maximum value for the generator.
	 *
	 * @return The maximum value for the distribution.
	 **/
	public double getMax()
	{
		return max;
	}
}
