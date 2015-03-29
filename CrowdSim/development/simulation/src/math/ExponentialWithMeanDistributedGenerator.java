/*
 * $RCSfile: ExponentialWithMeanDistributedGenerator.java,v $ $Date: 2008/10/02 19:21:07 $
 */
package math;

/**
 * Generator to generate random numbers with an Exponential Distribution.
 *
 * @author $Author: ajolly $
 * @version $Revision: 1.1 $
 * $State: Exp $
 * $Date: 2008/10/02 19:21:07 $
 **/
public class ExponentialWithMeanDistributedGenerator extends ExponentialDistributedGenerator
{
	double mean = 1;
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructs a Generator for Exponentialy Distributed random numbers with a given probability and number of trials.
	**/
    public ExponentialWithMeanDistributedGenerator(double aMean)
    {
    	super();
    	this.mean = aMean;
    }
    
    
    /**
	 * Constructs a Generator for Exponentialy Distributed random numbers with a given probability and number of trials.
	 *
	 * @param seedVal The seed for the generators. This is only ussed if no seed has previously been set for any random generator.
	 **/
    public ExponentialWithMeanDistributedGenerator( int seedVal )
    {
    	super( seedVal );
    }
  

    /**
	 * Returns the next random number with an Exponential Distribution.
	 *
	 * @return A randomly generated number from an exponential distribution.
	 **/
	public double nextValue()
	{
		return mean*super.nextValue();
	}
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Sets the mean of the generator.
	 *
	 * @param meanVal The mean for the distribution.
	 **/
	public void setMean( double meanVal )
	{
		mean = meanVal;
	}
	
	/**
	 * Gets the mean of the generator.
	 *
	 * @return The mean for the distribution.
	 **/
	public double getMean()
	{
		return mean;
	}
	
}
