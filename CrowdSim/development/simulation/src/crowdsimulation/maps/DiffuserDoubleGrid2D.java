package crowdsimulation.maps;

import sim.util.*;
import sim.field.grid.*;

public class DiffuserDoubleGrid2D extends DoubleGrid2D
{
	public DiffuserDoubleGrid2D(int width, int height)
	{
		super(width, height);
	}
	public DiffuserDoubleGrid2D (int width, int height, double initialValue)
    {
		super(width, height, initialValue);
    }
	public final DiffuserDoubleGrid2D diffuse1()
	{
		double timestep = 0.3;
		double alpha = 0.2;
		double average;
		DoubleGrid2D valgrid = new DoubleGrid2D(this.getWidth(),this.getHeight(),0);
		valgrid.setTo(this) ;       
                 for(int x=0;x< this.getWidth();x++)
                     for(int y=0;y< this.getHeight();y++)
                         {
                         average = 0.0;
                         // for each neighbor of that position
                        // System.out.println("x pos " + x + " y pos "+ y);
                         for(int dx=-1; dx< 2; dx++)
                             for(int dy=-1; dy<2; dy++)
                                 {
                                 	int xx = -999;
                                 	int yy = -999;
                                 	if(x+dx >= 0 && x+dx < this.getWidth()){
                                 		xx =  this.stx(x+dx);
                        
                                 	}
                                	 if(y+dy >= 0 && y+dy < this.getHeight()){
                                 		yy = this.sty(y+dy);
                                 	}                                
                                 // compute average
                                 	if (dx == 0 && dy == 0)
                                 	{

                                 	}
                                 	else if (xx > -999 && yy > -999)
                                 	{
                                 		//	System.out.println(" dx " + dx + " dy " + dy + " the val " + this.field[xx][yy]);
                                 			average += Math.pow(this.field[xx][yy],timestep);
                                 	}
                                 }
                         average /= 8;
                         //System.out.println("----------------------------");
                         // load the new value into HeatBugs.this.valgrid2
                         valgrid.field[x][y] = Math.pow(this.field[x][y],timestep) - alpha*Math.pow(this.field[x][y],timestep) + alpha*average;
                         }
                 this.setTo(valgrid) ;  
                 return this;                    		
	}
	public final DiffuserDoubleGrid2D decay(double tstep, double decayRate)
	{
		double timestep = tstep;
		double gamma = decayRate;
		DoubleGrid2D valgrid = new DoubleGrid2D(this.getWidth(),this.getHeight(),0);
		valgrid.setTo(this) ;       
                 for(int x=0;x< this.getWidth();x++)
                     for(int y=0;y< this.getHeight();y++)
                         {
                         // load the new value into HeatBugs.this.valgrid2
                         valgrid.field[x][y] = Math.pow(this.field[x][y],timestep) - gamma*Math.pow(this.field[x][y],timestep);
                         }
                 this.setTo(valgrid) ;  
                 return this;                    		
	}
	
	public final DoubleGrid2D diffuse(double diffusionRate, double tstep)
	{
     	DoubleGrid2D valgrid1 = new DoubleGrid2D(this.getWidth(),this.getHeight(),0);
     	DoubleGrid2D valgrid2 = new DoubleGrid2D(this.getWidth(),this.getHeight(),0);
     	
     	valgrid1.setTo(this);
     	
     	final DoubleGrid2D _valgrid = valgrid1;
        final double[][] _valgrid_field = valgrid1.field;
        final double[][] _valgrid2_field = valgrid2.field;
        final int _gridWidth = _valgrid.getWidth();
        final int _gridHeight = _valgrid.getHeight();
        
        final double _diffusionRate = diffusionRate;
		final double timestep = tstep;
        double average = 0;
        
        double[] _past = new double[_gridWidth];
        //double[] _past = _valgrid_field[_valgrid.stx(-1)];
        //for (int i = 0; i < _past.length; i++)
        //{
        //	System.out.println("_past[" + i + "] = " + _past[i]);
        // }
        double[] _current = _valgrid_field[0];
        double[] _next;
        double[] _put;
        
        int yminus1;
        int yplus1;
        
        // for each x and y position
        for(int x=0;x< _gridWidth;x++)
            {
            _next = _valgrid_field[_valgrid.stx(x+1)];
            if (x + 1 >= _gridWidth)
            {
              //System.out.println("x + 1 >= _gridWidth : " + (x + 1) + "," + _gridWidth);
              _next = new double[_next.length];
            }            
            _put = _valgrid2_field[_valgrid.stx(x)];
            
            yminus1 = _valgrid.sty(-1);     // initialized
            for(int y=0;y < _gridHeight;y++)
                {
                // for each neighbor of that position
                // go across top
                yplus1 = _valgrid.sty(y+1);
                
            	if(y >= 0 && yplus1 > 0)
            	{
                	//System.out.println("y and yplus1 > 0 : " + y + "," + yplus1);
                	average = (Math.pow(_past[yminus1], timestep) + Math.pow(_past[y], timestep) + Math.pow(_past[yplus1], timestep) +
                           Math.pow(_current[yminus1], timestep)  + Math.pow(_current[yplus1],timestep) +
                           Math.pow(_next[yminus1],timestep) + Math.pow(_next[y], timestep) + Math.pow(_next[yplus1],timestep)) / 8.0;
				}
				if (y == 0)
				{
					//System.out.println("y == 0 : " + y);
					average = (Math.pow(0, timestep) + Math.pow(_past[y], timestep) + Math.pow(_past[yplus1], timestep) +
                           Math.pow(0, timestep)  + Math.pow(_current[yplus1],timestep) +
                           Math.pow(0,timestep) + Math.pow(_next[y], timestep) + Math.pow(_next[yplus1],timestep)) / 8.0;
				}
				if (yplus1 == 0)
				{
					//System.out.println("yplus1 == 0 : " + yplus1);
					average = (Math.pow(_past[yminus1], timestep) + Math.pow(_past[y], timestep) + Math.pow(0, timestep) +
                           Math.pow(_current[yminus1], timestep)  + Math.pow(0,timestep) +
                           Math.pow(_next[yminus1],timestep) + Math.pow(_next[y], timestep) + Math.pow(0,timestep)) / 8.0;
				}
                // load the new value into this.valgrid2
                _put[y] = Math.pow(_current[y],timestep) - _diffusionRate * Math.pow(_current[y],timestep) + _diffusionRate * average;

                // set y-1 to what y was "last time around"
                yminus1 = y;
                }
                
            // swap elements
            _past = _current;
            _current = _next;
            }




        // ----------------------------------------------------------------------
        // If you have a multiprocessor machine, you can speed this up further by
        // dividing the work among two processors.  We do that over in ThreadedDiffuser.java
        //
        // You can also avoid some of the array bounds checks by using linearized
        // double arrays -- that is, using a single array but computing the double
        // array location yourself.  That way you only have one bounds check instead
        // of two.  This is how, for example, Repast does it.  This is certainly a
        // little faster than two checks.  We use a two-dimensional array because a
        // linearized array class is just too cumbersome to use in Java right now, 
        // what with all the get(x,y) and set(x,y,v) instead of just saying foo[x][y].  
        // Plus it turns out that for SMALL (say 100x100) arrays, the double array is 
        // actually *faster* because of cache advantages.
        //
        // At some point in the future Java's going to have to fix the lack of true
        // multidimensional arrays.  It's a significant speed loss.  IBM has some proposals
        // in the works but it's taking time.  However their proposals are for array classes.
        // So allow me to suggest how we can do a little syntactic sugar to make that prettier.
        // The array syntax for multidimensional arrays should be foo[x,y,z] and for
        // standard Java arrays it should be foo[x][y][z].  This allows us to mix the two:
        // a multidimensional array of Java arrays for example:  foo[x,y][z].  Further we
        // should be allowed to linearize a multidimensional array, accessing all the elements
        // in row-major order.  The syntax for a linearized array simply has empty commas:
        // foo[x,,]
        
        
        // oh yeah, we have one last step.
        
        // now finally copy HeatBugs.this.valgrid2 to HeatBugs.this.valgrid, and we're done
        this.setTo(valgrid2);
        return this;
        }    	
}