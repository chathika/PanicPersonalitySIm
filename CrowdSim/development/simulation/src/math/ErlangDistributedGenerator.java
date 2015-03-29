/*
 * $RCSfile: ErlangDistributedGenerator.java,v $ $Date: 2008/10/02 19:21:35 $
 */
package math;

/**
 * Generator to generate random numbers with an Exponential Distribution.
 *
 * @author $Author: ajolly $
 * @version $Revision: 1.1 $
 * $State: Exp $
 * $Date: 2008/10/02 19:21:35 $
 **/
public class ErlangDistributedGenerator extends RandomGenerators
{
	double mean;
	int m;
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs a Generator for m-Erlang Distributed random numbers with a given probability and number of trials.
	 **/
    public ErlangDistributedGenerator(int m, double mean)
    {
    	super();
    	this.setM(m);
    	this.setMean(mean);
    }
    
    /**
	 * Constructs a Generator for m-Erlang Distributed random numbers with a given probability and number of trials.
	 *
	 * @param seedVal The seed for the generators. This is only used if no seed has previously been set for any random generator.
	 **/
    public ErlangDistributedGenerator( int seedVal ,int m, double mean)
    {
    	super( seedVal );
    	this.setM(m);
    	this.setMean(mean);
    }
    
	/**
     * Generates a floating-point number which is a random deviate drawn from an 
     * exponential distribution.
     *
     * @return The random number drawn from a Exponential Distribution.
     **/ 
    public double erlangdev()
    {
    	double mean_exponential;
    	double sum;
        	
    	mean_exponential = mean/m;
    	sum = 0.0;
    	
    	ExponentialWithMeanDistributedGenerator theExponential = new ExponentialWithMeanDistributedGenerator(mean_exponential);
    	
    	for(int i = 1; i <= m; i++)
    	{
    		sum += theExponential.nextValue();
    	}
    	return sum;
    }
    
    /**
	 * Returns the next random number with an Exponential Distribution.
	 *
	 * @return A randomly generated number from an exponential distribution.
	 **/
	public double nextValue()
	{
		return erlangdev();
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
	
	/**
	 * Sets the standard deviation of the generator.
	 *
	 * @param stdDevVal The standard deviation for the distribution.
	 **/
	public void setM( int mVal )
	{
		m = mVal;
	}
	
	/**
	 * Gets the standard deviation of the generator.
	 *
	 * @return The standard deviation for the distribution.
	 **/
	public int getM()
	{
		return m;
	}
}
