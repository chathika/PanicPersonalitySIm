/*
 * $RCSfile: Vector2D.java,v $ $Date: 2010/11/08 16:06:43 $
 */
package math;

import sim.util.*;

/**
 * Class representing a 2 Dimensional Mathematical Vector.
 * Based on the code from the mason simulation framework.
 *
 * @author $Author: dkaup $
 * @version $Revision: 1.3 $
 * $State: Exp $
 * $Date: 2010/11/08 16:06:43 $
 **/
public class Vector2D
{
	double ERROR = 0.0000001;
    public double x;
    public double y;
    
    public Vector2D() { x = 0.0; y = 0.0; }
    public Vector2D(final java.awt.Point p) { x = p.x; y = p.y; }
    public Vector2D(Double2D p) { x = p.x; y = p.y; }
    public Vector2D(final java.awt.geom.Point2D.Double p) { x = p.x; y = p.y; }
    public Vector2D(final java.awt.geom.Point2D.Float p) { x = p.x; y = p.y; }
    /** Only included for completeness' sakes, in case a new Point2D subclass is created in the future. */
    public Vector2D(final java.awt.geom.Point2D p) { x = p.getX(); y = p.getY(); }
    public Vector2D(final double x, final double y) { this.x = x; this.y = y; }
    public Vector2D(final double x, final double y, boolean normalize ) 
    { 
    	this.x = x; 
    	this.y = y;
    	if( normalize )
    	{
    		this.normalize();
    	}
    }
    public final double getX() { return x; }
    public final double getY() { return y; }
    public String toString() { return "Vector2D["+x+","+y+"]"; }
    public String toCoordinates() { return "(" + x + ", " + y + ")"; }
    
    public Double2D getDouble2D(){ return new Double2D( x, y ); }
    
    public double getAngle(){return Math.atan2(y,x);}
    
    public Vector2D add(Vector2D rhs)  { return new Vector2D(x + rhs.x, y + rhs.y); }

    public static boolean isAngleInClockwiseArc(double first, double second, double test) {
        if (first < 0 && second < 0 || first > 0 && second > 0) {
            return (test > first && test < second) || (test < first && test > second);
        }
        if(first >= 0 && second <= 0) {
            return (test < second || test > first);
        } else {
            return (test < second && test > first);
        }
    }

    public static boolean isAngleInSmallestArc(double first, double second, double test) {
        if(first < 0 && second >= 0) {
            double temp = first;
            first = second;
            second = temp;
        }
        if (first < 0 && second < 0 || first > 0 && second > 0) {
            return (test > first && test < second) || (test < first && test > second);
        }
        if(first - second > Math.PI) {
            return (test < second || test > first);
        } else if (first - second < Math.PI) {
            return (test > second && test < first);
        } else return true;
    }

    public Vector2D normalize()
    {
    	double dist = magnitude();
		
		if( dist == 0 )
		{
			return this;
		}
		
    	x = x/dist;
    	y = y/dist;
    	
    	return this;
    }
    
    public double dot( Vector2D a )
    {
    	return a.x*this.x+a.y*this.y;
    }
    
    public double getAngleBetween( Vector2D b)
    {
    	return Math.atan2(b.y,b.x) - Math.atan2(this.y,this.x);
    }
    
    public final int hashCode()
        {
        // so we hash to the same value as Int2D does, if we're ints
        if ((((int)x) == x) && ((int)y) == y)
            //return Int2D.hashCodeFor((int)x,(int)y);
            
            {
            int y = (int)this.y;
            int x = (int)this.x;

            // copied from Int2D and inserted here because hashCodeFor can't be
            // inlined and this saves us a fair chunk on some hash-heavy applications

            y += ~(y << 15);
            y ^=  (y >>> 10);
            y +=  (y << 3);
            y ^=  (y >>> 6);
            y += ~(y << 11);
            y ^=  (y >>> 16);

            // nifty!  Now mix in x
            
            return x ^ y;
            }
            
            
            
        // I don't like Sun's simplistic approach to random shuffling.  So...
        // basically we need to randomly disperse <double,double> --> int
        // We do this by doing <double,double> -> <long,long> -> long -> int
        // The first step is done with doubleToLongBits (not RawLongBits;
        // we want all NaN to hash to the same thing).  Then conversion to
        // a single long is done by hashing (shuffling) y, then xoring it with x.
        // So I need something that will hash y to a nicely random value.
        // this taken from http://www.cris.com/~Ttwang/tech/inthash.htm
        // Last we fold the long onto itself to form the int.

        // Some further discussion.  Sun's moved to a new hash table scheme
        // which has (of all things!) tables with lengths that are powers of two!
        // Normally hash table lengths should be prime numbers, in order to
        // compensate for bad hashcodes.  To fix matters, Sun now is
        // pre-shuffling the hashcodes with the following algorithm (which
        // is short but not too bad -- should we adopt it?  Dunno).  See
        // http://developer.java.sun.com/developer/bugParade/bugs/4669519.html
        //    key += ~(key << 9);
        //    key ^=  (key >>> 14);
        //    key +=  (key << 4);
        //    key ^=  (key >>> 10);
        // This is good for us because Int2D, Int3D, Double2D, and Double3D
        // have hashcodes well distributed with regard to y and z, but when
        // you mix in x, they're just linear in x.  We could do a final
        // shuffle I guess.  In Java 1.3, they DON'T do a pre-shuffle, so
        // it may be suboptimal.  Since we're all moving to 1.4.x, it's not
        // a big deal since 1.4.x is shuffling the final result using the
        // Sun shuffler above.  But I'd appreciate some tests on our method
        // below, and suggestions as to whether or not we should adopt the
        // shorter, likely suboptimal but faster Sun shuffler instead
        // for y and z values.  -- Sean
        
        long key = Double.doubleToLongBits(y);
            
        key += ~(key << 32);
        key ^= (key >>> 22);
        key += ~(key << 13);
        key ^= (key >>> 8);
        key += (key << 3);
        key ^= (key >>> 15);
        key += ~(key << 27);
        key ^= (key >>> 31);
        
        // nifty!  Now mix in x
        
        key ^= Double.doubleToLongBits(x);
        
        // Last we fold on top of each other
        return (int)(key ^ (key >> 32));
        }
        
    
    public double magnitude()
    {
    	return distance( 0, 0 );	
    }
    
    public Object clone()
    {
    	return new Vector2D( this.x, this.y );
    }
    
	public boolean approximatelyEqual( Vector2D a, Vector2D b )
	{
		return ( Math.abs(a.x - b.x) < ERROR && Math.abs(a.y - b.y) < ERROR );
	}
    
    // can't have separate equals(...) methods as the
    // argument isn't virtual
    public final boolean equals(final Object obj)
        {
        if (obj==null) return false;
        else if (obj instanceof Vector2D)  // do Double2D first
        {
        	if( approximatelyEqual( (Vector2D)obj, this ) ) return true;
            Vector2D other = (Vector2D) obj;
            // can't just do other.x == x && other.y == y because we need to check for NaN
            return (Double.doubleToLongBits(other.x) == Double.doubleToLongBits(x) &&
                    Double.doubleToLongBits(other.y) == Double.doubleToLongBits(y));
        }
        else return false;
        }
        
    /** Returns the distance FROM this Double2D TO the specified point */
    public double distance(final double x, final double y)
        {
        final double dx = (double)this.x - x;
        final double dy = (double)this.y - y;
        return Math.sqrt(dx*dx+dy*dy);
        }

    /** Returns the distance FROM this Double2D TO the specified point.   */
    public double distance(final Vector2D p)
        {
        final double dx = (double)this.x - p.x;
        final double dy = (double)this.y - p.y;
        return Math.sqrt(dx*dx+dy*dy);
        }

    /** Returns the distance FROM this Double2D TO the specified point.    */
    public double distance(final java.awt.geom.Point2D p)
        {
        final double dx = (double)this.x - p.getX();
        final double dy = (double)this.y - p.getY();
        return Math.sqrt(dx*dx+dy*dy);
        }

    /** Returns the distance FROM this Double2D TO the specified point */
    public double distanceSq(final double x, final double y)
        {
        final double dx = (double)this.x - x;
        final double dy = (double)this.y - y;
        return (dx*dx+dy*dy);
        }

    /** Returns the distance FROM this Double2D TO the specified point.    */
    public double distanceSq(final Vector2D p)
        {
        final double dx = (double)this.x - p.x;
        final double dy = (double)this.y - p.y;
        return (dx*dx+dy*dy);
        }

    /** Returns the distance FROM this Double2D TO the specified point */
    public double distanceSq(final java.awt.geom.Point2D p)
        {
        final double dx = (double)this.x - p.getX();
        final double dy = (double)this.y - p.getY();
        return (dx*dx+dy*dy);
        }

    }
