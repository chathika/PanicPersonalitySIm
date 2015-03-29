/*
 * $RCSfile: GammaDistributedGenerator.java,v $ $Date: 2008/03/17 19:10:20 $
 */
package math;

/**
 * Generator to generate random numbers with a Gamma Distribution.
 * The  event number needs to be set on the generator
 * before a random number is generated. A waiting time to the iath event 
 * in a Poisson process of unit mean.
 *
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:20 $
 **/
public class GammaDistributedGenerator extends RandomGenerators
{
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	private int eventNumber;

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Constructs a Generator for Gamma Distributed random numbers for a given event.
	 *
	 * @param iathEvent The event number to be used for the generation of the random number.
	 **/
    public GammaDistributedGenerator( int iathEvent )
    {
		super();
		setEventNumber( iathEvent );
    }
    
    /**
	 * Constructs a Generator for Gamma Distributed random numbers for a given event.
	 *
	 * @param seedVal The seed for the generators. This is only ussed if no seed has previously been set for any random generator.
	 * @param iathEvent The event number to be used for the generation of the random number.
	 **/
    public GammaDistributedGenerator( int seedVal, int iathEvent )
    {
		super( seedVal );
		setEventNumber( iathEvent );
    }

	/**
     * Generates a floating-point number which is a random deviate drawn from a 
     * Gamma distribution for the iath event.
     *
     * @return The random number drawn from a Gamma Distribution.
     **/ 
    public double gamdev( int ia )
    {
    	int j;
    	double am;
    	double e;
    	double s;
    	double v1;
    	double v2;
    	double x;
    	double y;
    	
    	if( ia < 1)
    	{
    		System.out.println( "error in routine gamdev:: ia must be >= 1" );
    		System.exit( -1 );
    	}
    	if( ia < 6 )
    	{
    		x = 1.0;
    		for( j = 1; j <= ia; j++ )
    		{
    			x *= rand1();
    		}
    		x = -1*Math.log( x );
    	}
    	else
    	{
    		do
    		{
    			do
    			{
    				do
    				{
    					v1 = rand1();
    					v2 = 2.0*rand1()-1.0;
    				}while( v1*v1+v2*v2 > 1.0 );
    				
    				y = v2/v1;
    				am = ia -1;
    				s = Math.sqrt( 2.0*am+1.0 );
    				x = s*y+am;
    			}while( x <= 0.0 );
    			e = (1.0+y*y)*Math.exp(am*Math.log(x/am)-s*y);
    		}while( rand1() > e );
    	}
    	
    	return x;
    }

	/**
	 * Returns the next random number with a Gamma Distribution. This uses the 
	 * event number value set in the class instance.
	 *
	 * @return A randomly generated number from a gamma distribution.
	 **/
	public double nextValue()
	{
		return gamdev( eventNumber );
	}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Sets the eventNumber of the generator.
	 *
	 * @param iaVal The event number for the distribution.
	 **/	
	public void setEventNumber( int iaVal )
	{
		eventNumber = iaVal;
	}
	
	/**
	 * Gets the event number of the generator.
	 *
	 * @return The event number for the distribution.
	 **/
	public int getEventNumber()
	{
		return eventNumber;
	}
}
