/*
 * $RCSfile: NormalDistributedGenerator.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package math;

/**
 * Generator to generate random numbers with a Normal Distribution.
 * The mean and standard deviation needs to be set on the generator
 * before a random number is generated.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class NormalDistributedGenerator extends RandomGenerators
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	static int iset = 0;
	static double gset;
	
	double mean = 0;
	double stdDev = 1;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Constructs a Generator for Normal Distributed random number.
	 **/
    public NormalDistributedGenerator()
    {
		super();
    }

	/**
	 * Constructs a Generator for Normal Distributed random number.
	 *
	 * @param seedVal The seed for the generators. This is only ussed if no seed has previously been set for any random generator.
	 **/
    public NormalDistributedGenerator( int seedVal )
    {
    	super( seedVal );
    }
    
    /**
	 * Constructs a Generator for Normal Distributed random number.
	 *
	 * @param meanVal The mean of the distribution.
	 * @param stdDevVal The standard deviation of the distribution.
	 **/
    public NormalDistributedGenerator( double meanVal, double stdDevVal )
    {
		super();
		setMean( meanVal );
		setStdDev( stdDevVal );
    }
    
    /**
	 * Constructs a Generator for Normal Distributed random number.
	 *
	 * @param meanVal The mean of the distribution.
	 * @param stdDevVal The standard deviation of the distribution.
	 * @param seedVal The seed for the generators. This is only ussed if no seed has previously been set for any random generator.
	 **/
    public NormalDistributedGenerator( int seedVal, double meanVal, double stdDevVal )
    {
    	super( seedVal );
    	setMean( meanVal );
		setStdDev( stdDevVal );
    }
   
    /**
     * Generates a random number from a Normal Distribution with zero mean and unit variance distribution.
     *
     * @return The randomly generated number.
     **/ 
    public double gasdev()
    {
    	double fac;
    	double rsq;
    	double v1;
    	double v2;
    	
    	if( seed < 0 )	
    	{
    		iset = 0;
    	}
    	if( iset == 0 )
    	{
    		do
    		{
    			v1 = 2.0*rand1()-1.0;
    			v2 = 2.0*rand1()-1.0;
    			rsq = v1*v1+v2*v2;
    		}while( rsq >=1.0 || rsq ==0.0 );
    		fac = Math.sqrt(-2*Math.log(rsq)/rsq);
    		gset = v1*fac;
    		iset = 1;
    		return v2*fac;
    	}
    	else
    	{
    		iset = 0;
    		return gset;
    	}
    }
    
    /**
     * Normal Distribution with mean of mean and variance of stdv.
     *
     * @param mean The mean of the distribution.
     * @param stdv The standard deviation of the distribution.
     * @return A randomly generated number based on a Normal (Gaussian) Distribution.
     **/ 
    public double gasdev( double mean, double stdv )
    {
		return (gasdev() * stdv) + mean;	
	}
	
    /**
     * Normal Distribution with mean of mean and variance of sqrt(sigma_sq). It also only returns
     * values that fall between [mean-cutMult*sqrt(sigma_sq),mean+cutMult*sqrt(sigma_sq)].
     *
     * @param mean The mean of the distribution.
     * @param sigma_sq The square of the standard deviation of the distribution.
     * @param cutMult The number of standard deviation before the cutoff.
     * @return A randomly generated number based on a Normal (Gaussian) Distribution.
     **/ 
	public double gaussRand( double mean, double sigma_sq, double cutMult )
	{
		double val = 0;
		
		do
		{
			val = gasdev( mean, Math.sqrt( sigma_sq ) );
		} while( Math.abs( mean-val ) > cutMult*Math.sqrt( sigma_sq ) );
		
		return val;
	}

	/**
	 * Returns the next random number with a Normal Distribution. This uses the 
	 * mean and standard deviation values set in the class instance.
	 *
	 * @return A randomly generated number from a normal distribution.
	 **/
	public double nextValue()
	{
		return gasdev( mean, stdDev );
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
	public void setStdDev( double stdDevVal )
	{
		stdDev = stdDevVal;
	}
	
	/**
	 * Gets the standard deviation of the generator.
	 *
	 * @return The standard deviation for the distribution.
	 **/
	public double getStdDev()
	{
		return stdDev;
	}
}
