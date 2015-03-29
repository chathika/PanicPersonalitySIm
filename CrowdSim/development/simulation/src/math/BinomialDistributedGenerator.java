/*
 * $RCSfile: BinomialDistributedGenerator.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package math;

/**
 * Generator to generate random numbers with a Binomial Distribution.
 * The probabilty and number of trials need to be set on the generator
 * before a random number is generated.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class BinomialDistributedGenerator extends RandomGenerators
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	static int nold = -1;	
	static double pold = -1;
	static double pc = 0;
	static double plog = 0;
	static double pclog = 0;
	static double en = 0;
	static double oldg = 0;
	
	double probability;
	int trials;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Constructs a Generator for Binomial Distributed random numbers with a given probability and number of trials.
	 *
	 * @param ppVal The probability for the distribution of the random numbers.
	 * @param trialsVal The number of trials to be used for the generation of the random number.
	 **/
    public BinomialDistributedGenerator( double ppVal, int trialsVal )
    {
    	super();
    	setProbability( ppVal );
    	setTrials( trialsVal );
    }
    
	/**
	 * Constructs a Generator for Binomial Distributed random numbers with a given probability and number of trials.
	 *
	 * @param seedVal The seed for the generators. This is only ussed if no seed has previously been set for any random generator.
	 * @param ppVal The probability for the distribution of the random numbers.
	 * @param trialsVal The number of trials to be used for the generation of the random number.
	 **/
    public BinomialDistributedGenerator( int seedVal, double ppVal, int trialsVal )
    {
    	super( seedVal );
    	setProbability( ppVal );
    	setTrials( trialsVal );
    }
    
	/**
     * Generates a floating-point number which is a random deviate drawn from a 
     * Binomial distribution of n trials each of probability pp.
     *
     * @return The random number drawn from a Binomial Distribution.
     **/ 
    public double bnldev( double pp, int n )    
    {
    	int j;
    	double am;
    	double em;
    	double g;
    	double angle;
    	double p;
    	double bnl;
    	double sq;
    	double t;
    	double y;
    	
    	if( pp < 05. )
    	{
    		p = pp;
    	}
    	else
    	{
    		p = 1.0-pp;
    	}
    	am = n*p;		//mean of the deviate to be produced
    	if( n < 25 )
    	{
    		bnl = 0.0;
    		for( j = 0; j <= n; j++ )
    		{
    			if( rand1() < p )
    			++bnl;
    		}
    	}
    	else if( am < 1.0 )
    	{
    		g = Math.exp( -1.0*am );
    		t = 1.0;
    		for( j = 0; j <= n; j++ )
    		{
    			t *= rand1();
    			if( t < g )
    			{
    				break;
    			}
    		}
    		if( j <=n )
    		{
    			bnl = j;
    		}
    		else
    		{
    			bnl = n;
    		}
    	}
    	else
    	{
    		if( n != nold )
    		{
    			en = n;
    			oldg = gammln( en+1.0 );
    			nold = n;
    		}
    		if( p != pold )
    		{
    			pc = 1.0-p;
    			plog = Math.log( p );
    			pclog = Math.log( pc );
    			pold = p;
    		}
    		sq = Math.sqrt( 2.0*am*pc );
    		do
    		{
    			do
    			{
    				angle = Math.PI*rand1();
    				y = Math.tan( angle );
    				em = sq*y+am;
    			}while( em < 0.0 || em >= (en+1.0));
    			em = Math.floor( em );
    			t = 1.2*sq*(1.0+y*y)*Math.exp(oldg-gammln(em+1.0)
    				-gammln(en-em+1.0)+em*plog+(en-em)*pclog);
    		}while( rand1() > t );
    		bnl = em;
    	}
    	if( p != pp )
    	{
    		bnl = n - bnl;
    	}
    	return bnl;
    }
   
	/**
	 * Returns the next random number with a Binomial Distribution. This uses the 
	 * probability and trial values set in the class instance.
	 *
	 * @return A randomly generated number from a binomial distribution.
	 **/
	public double nextValue()
	{
		return bnldev( probability, trials );
	}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Sets the Probability of the generator.
	 *
	 * @param probabilityVal The probability for the distribution.
	 **/
	public void setProbability( double probabilityVal )
	{
		probability = probabilityVal;
	}
	
	/**
	 * Gets the Probability of the generator.
	 *
	 * @return The probability for the distribution.
	 **/
	public double getProbability()
	{
		return probability;
	}
	
	/**
	 * Sets the number of trials for the generator.
	 *
	 * @param trialsVal The number of trials for the distribution.
	 **/
	public void setTrials( int trialsVal )
	{
		trials = trialsVal;
	}
	
	/**
	 * Gets the number of trials of the generator.
	 *
	 * @return The number of trials for the distribution.
	 **/
	public int getTrials()
	{
		return trials;
	}

}
