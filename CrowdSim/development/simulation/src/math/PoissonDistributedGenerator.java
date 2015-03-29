/*
 * $RCSfile: PoissonDistributedGenerator.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package math;

/**
 * Generator to generate random numbers with a Poisson Distribution.
 * The mean needs to be set on the generator before a random number is generated.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class PoissonDistributedGenerator extends RandomGenerators
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

 	static double alxm;
 	static double sq;
 	static double g;
 	static double oldm=-1.0;

	double mean;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Constructs a Generator for Poisson Distributed random numbers with a given mean.
	 *
	 * @param meanVal The mean for the distribution of the random numbers.
	 **/	
    public PoissonDistributedGenerator( double meanVal )
    {
		super();
		setMean( meanVal );
    }
    
	/**
	 * Constructs a Generator for Poisson Distributed random numbers with a given mean.
	 *
	 * @param seedVal The seed for the generators. This is only ussed if no seed has previously been set for any random generator.
	 * @param meanVal The mean for the distribution of the random numbers.
	 **/
    public PoissonDistributedGenerator( int seedVal, double meanVal )
    {
		super( seedVal );
		setMean( meanVal );
    }
    
    /**
     * Generates a floating-point number which is a random deviate drawn from a 
     * Poisson distribution with a given mean.
     *
     * @param xm The mean for the poisson distribution.
     * @return The random number drawn from a Poisson Distribution.
     **/
    public double poidev( double xm )
    {
    	double em;
    	double t;
    	double y;
    	
    	if( xm < 12 )
    	{
	    	if( xm != oldm )
	    	{
	    		oldm = xm;
	    		g = Math.exp( -1*xm );
	    	}
	    	em = -1.0;
	    	t = 1.0;
	    	do
	    	{
	    		++em;
	    		t *= rand1();
	    	}while( t > g );
    	}
		else
		{
			if( xm != oldm )
			{
				oldm = xm;
				sq = Math.sqrt( 2.0*xm );
				alxm = Math.log( xm );
				g = xm*alxm-gammln(xm+1);
			}
			do
			{
				do
				{
					y = Math.tan(Math.PI*rand1());
					em = sq*y+xm;
				}while( em < 0.0 );
				em = Math.floor( em );
				t = 0.9*(1.0+y*y)*Math.exp(em*alxm-gammln(em+1.0)-g);
			}while( rand1() > t );
		}
		
		return em;   	
    }

	/**
	 * Returns the next random number with a Poisson Distribution. This uses the 
	 * mean set in the class instance.
	 *
	 * @return A randomly generated number from a poisson distribution.
	 **/
	public double nextValue()
	{
		return poidev( mean );
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
