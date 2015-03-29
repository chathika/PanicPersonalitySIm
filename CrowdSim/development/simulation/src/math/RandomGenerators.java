/*
 * $RCSfile: RandomGenerators.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package math;

/**
 * Base class for random number generators.
 * Contains the attributes to keep all generators executing from 
 * the same random generator, along with restricting all generators
 * to a single seed.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public abstract class RandomGenerators
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	static int IA = 16807;
    static int IM = 2147483647;
    static int IQ = 127773;
    static int IR = 2836;
    static int NTAB = 32;
    static int NDIV = (1+(IM-1)/NTAB);
    static double EPS = 3*Math.exp(-16.0);
    static double AM = 1.0/IM;
    static double RNMX = (1.0-EPS);
	static int iy = 0;
	static int iv[] = new int[NTAB];
	
	static int seed=0;

	static double cof[] = {76.18009172947146,-86.50532032941677,
							24.01409824083091,-1.231739572450155,
							0.001208650973866179,-0.000005395239384953};

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	static public double gammln( double xx )
	{
		int j;
		double x;
		double y;
		double tmp;
		double ser;
		
		y = x = xx;
		tmp = x+5.5;
		tmp -= (x+0.5)*Math.log(tmp);
		ser = 1.000000000190015;
		for( j = 0; j < 6; j++)
		{
			ser += cof[j]/++y;
		}
		
		return -1*tmp+Math.log( 2.5066282746310005*ser/x );
	}

	/**
	 * Constructs a RandomGenerator and randomly sets the seed if one has not been previously set.
	 **/
    public RandomGenerators()
    {
    	if( seed == 0 )
    	{
    		java.util.Random rand = new java.util.Random();
    		seed = -1*rand.nextInt();
    		
    		for( int i = 0; i<NTAB; i++)
    		{
    			iv[i] = 0;
    		}
    	}
    }
    
    /**
	 * Constructs a RandomGenerator and sets the seed if one has not been previously set.
	 *
	 * @param seedVal The seed to use for the generator. This is only used if a seed has not been previously set.
	 **/
    public RandomGenerators( int seedVal )
    {
    	if( seed == 0 )
    	{
    		seed = seedVal;
    		
	    	for( int i = 0; i<NTAB; i++)
	    	{
	    		iv[i] = 0;
	    	}
    	}
    }
    
    /**
     * Generates a uniform random number between 0 and 1.
     *
     * @return double The random number.
     **/
    double rand1()
    {
    	int j;
    	int k;
    	
    	double temp;
    	
    	if( seed <= 0 || iy == 0 )
    	{
    		if( -1*seed < 1 )
    		{
    			seed = 1;
    		}
    		else
    		{
    			seed = -1*seed;
    		}
    		for( j = NTAB+7; j>=0; j-- )
    		{
    			k = seed/IQ;
    			seed = IA*(seed-k*IQ)-IR*k;
    			if( seed < 0 )
    			{
    				seed += IM;
    			}
    			if( j < NTAB )
    			{
    				iv[j] = seed;
    			}
    		}
    		iy = iv[0];
    	}
    	
    	k = (int)(seed/IQ);
    	seed = IA*(seed-k*IQ)-IR*k;

    	if( seed < 0 )
    	{
    		seed += IM;
    	}
    	j = iy/NDIV;
    	iy = iv[j];
    	iv[j] = seed;
    	temp = AM*iy;

    	if( temp > RNMX )
    	{
    		return RNMX;
    	}
    	
    	return temp;
    }
    
    /**
     * Abstract method to get the next random number from one of the Generators.
     *
     * @return double The next random number.
     **/
    public abstract double nextValue();

}
