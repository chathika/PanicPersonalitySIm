/*
 * $RCSfile: ExponentialDistributedGenerator.java,v $ $Date: 2008/10/02 19:19:07 $
 */
package math;

/**
 * Generator to generate random numbers with an Exponential Distribution.
 *
 * @author $Author: ajolly $
 * @version $Revision: 1.2 $
 * $State: Exp $
 * $Date: 2008/10/02 19:19:07 $
 **/
public class ExponentialDistributedGenerator extends RandomGenerators
{
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructs a Generator for Exponentialy Distributed random numbers with a given probability and number of trials.
	**/
    public ExponentialDistributedGenerator()
    {
    	super();
    }
    
    
    /**
	 * Constructs a Generator for Exponentialy Distributed random numbers with a given probability and number of trials.
	 *
	 * @param seedVal The seed for the generators. This is only ussed if no seed has previously been set for any random generator.
	 **/
    public ExponentialDistributedGenerator( int seedVal )
    {
    	super( seedVal );
    }
  
	/**
     * Generates a floating-point number which is a random deviate drawn from an 
     * exponential distribution.
     *
     * @return The random number drawn from a Exponential Distribution.
     **/ 
    public double expdev()
    {
    	double dum;
    	do
    	{
    		dum = rand1();
    	}while( dum == 0 );
    	return -1*Math.log(dum);
    }
    
    /**
	 * Returns the next random number with an Exponential Distribution.
	 *
	 * @return A randomly generated number from an exponential distribution.
	 **/
	public double nextValue()
	{
		return expdev();
	}
}
