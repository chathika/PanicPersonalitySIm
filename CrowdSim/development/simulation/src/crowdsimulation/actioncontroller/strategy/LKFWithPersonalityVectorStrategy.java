package crowdsimulation.actioncontroller.strategy;

import java.util.*;
import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import crowdsimulation.util.*;
import math.*;								// used for Vector2D
import ec.util.*;
import sim.field.continuous.*;
import sim.util.*;

/**
 * This is a model used to simulate human movement
 *
 * @see crowdsimulation.actioncontroller.strategy.ActionStrategy
 * @author $Author: roleson $
 * @version $Revision: 1.1.1.1 $
 * $State: Exp $
 * $Date: 2008/03/17 19:10:19 $
 **/
public class LKFWithPersonalityVectorStrategy extends LKFStrategy2{
 	
 
 /**
  *The "personality" vector that determines how to modify the social forces felt by an individual 
  *from the base LKF strategy based on his psychological makeup.Parameters are all from 0-1, 
  *where 0 corresponds to base or unmodified behavior.
  *The personality vector is of the form: 
  *<uninjuredSame, injuredSame, deadSame, uninjuredDifferent, injuredDifferent, deadDifferent,  , >
  **/
 	
 /*affects how much the implicit individual is attracted to living individuals
  *with same model.*/	
 private double uninjuredSame = 0;
 /*affects how much the implicit individual is attracted to injured individuals
  *with same model.*/
 private double injuredSame = 0;
 /*affects how much the implicit individual is attracted to dead individuals
  *with same model.*/
 private double deadSame = 0;
 /*affects how much the implicit individual is attracted to living individuals
  *with a different model.*/
 private double uninjuredDifferent = 0;
 /*affects how much the implicit individual is attracted to injured individuals
  *with a different model.*/
 private double injuredDifferent = 0;
 /*affects how much the implicit individual is attracted to dead individuals
  *with same model.*/	
 private double deadDifferent = 0;
 
 
 private double goalSame = 0;
 
 private double goalDifferent = 0;	

////////////////////////////////*Methods*//////////////////////////////////////////////////////


	/**
  	*Constructor
 	**/
 	public LKFWithPersonalityVectorStrategy()
 	{
 		super();
 	}
 
 	
 	/**
  	*An attractive, "clustering" force that makes the implicit individual attracted to 
  	*another individual with the same model instance
 	*
  	*@param
  	*@return
  	**/
  	public Vector2D groupingForce(Individual ind2)
  	{
  		
  		int ind2Health = healthStatus(ind2); // 0 := Dead, 1 := Injured, 2 := Uninjured
		boolean groupMember = isAGroupMember(ind2);
		double modificationFactor;// setting to 1 the default (unmodified)
		
		double lowerBound;
		double upperBound;
  		
  		switch (ind2Health){
			case 0:
			{
				if(groupMember)
				{	
					lowerBound = 1;
					upperBound = 2;
					modificationFactor = performTransformation(lowerBound, upperBound, this.getDeadSame());	
				}
				else	
				{	
					lowerBound = 1; 
					upperBound = 2;
					modificationFactor = performTransformation(lowerBound, upperBound, this.getDeadDifferent());
				}
			}
			break;	
			case 1:
			{
				if(groupMember)
				{
					lowerBound = 1;
					upperBound = 2;
					modificationFactor = performTransformation(lowerBound,upperBound, this.getInjuredSame()); 	
				}
				else
				{
					lowerBound = 1;
					upperBound = 2;
					modificationFactor = performTransformation(lowerBound,upperBound, this.getInjuredDifferent()); 
				}
			}
			break;
			default:
				if(groupMember)
				{
					lowerBound = 1;
					upperBound = 2;
					modificationFactor = performTransformation(lowerBound,upperBound, this.getUninjuredSame()); 
				}
				else
				{
					lowerBound = 1;
					upperBound = 2;
					modificationFactor = performTransformation(lowerBound,upperBound, this.getUninjuredDifferent()); 
				}
			break;
		
		}
		
		Vector2D tempF = super.groupingForce(ind2);
		return new Vector2D(modificationFactor*tempF.x,modificationFactor*tempF.y );	
  	}	
  		
  	/**
  	*The pyschological repulsive force between the implicit individual and another specified individual
  	* 
  	*@param 
  	*@return 
  	**/
  	public Vector2D pp_PsychForce( Individual ind2 )
  	{
		int ind2Health = healthStatus(ind2);// 0 := Dead, 1 := Injured, 2 := Uninjured
		boolean groupMember = isAGroupMember(ind2);
		double modificationFactor = 1; // setting to 1 the default (unmodified)
		
		double lowerBound;
		double upperBound;
		
		/*determine the modification factor based on the state of ind2*/
		switch(ind2Health){
			case 0:
			
				if(groupMember)
				{
					lowerBound = 1;
					upperBound = 2;	
					modificationFactor = performTransformation(lowerBound, upperBound, this.getDeadSame());	
				}
				else	
				{
					lowerBound = 1;
					upperBound = 2;	
					modificationFactor = performTransformation(lowerBound, upperBound, this.getDeadDifferent());
				}
			break;
			case 1:
			
				if(groupMember)
				{
					lowerBound = 1;
					upperBound = 2;
					modificationFactor = performTransformation(lowerBound,upperBound, this.getInjuredSame()); 	
				}
				else
				{
					lowerBound = 1;
					upperBound = 2;
					modificationFactor = performTransformation(lowerBound,upperBound, this.getInjuredDifferent()); 
				}
			break;
			default:
				if(groupMember)
				{
					lowerBound = 1;
					upperBound = 2;
					modificationFactor = performTransformation(lowerBound,upperBound, this.getUninjuredSame()); 
				}
				else
				{
					lowerBound = 1;
					upperBound = 2;
					modificationFactor =performTransformation(lowerBound,upperBound, this.getUninjuredDifferent()); 
				}
			break;
		}
		
		Vector2D tempF = super.pp_PsychForce( ind2 );
		return new Vector2D(modificationFactor*tempF.x,modificationFactor*tempF.y );
  	}
 	
 	/**
 	 *Tests to see if supplied individual is controlled by the same model instance
 	 *as the implicit individual.
 	 *
 	 *@param
 	 *@returns boolean true if ind2 is controlled by the same model controller
 	 **/
 	public boolean isAGroupMember(Individual ind2)
 	{
 		if((ind.getActionController()).equals(ind2.getActionController()))
 		{return true;}
 		return false;
 	}
 
 	/**
 	 *Returns the health state of ind2. 2 = Uninjured (not injured, not dead), 1 = Injured,
 	 *0 = Dead.
 	 *
 	 *@params
 	 *@returns int health status of Individual ind2
 	 **/
 	public int healthStatus(Individual ind2)
 	{	
 		/* check if injured*/
 		if( ind2.isInjured())
 		{return 1;}
 		/*check if dead*/
 		if( ind2.isDead())
 		{return 0;}
 		/*otherwise, ind2 is uninjured so return 2*/
 		return 2;
 		
 	}
 	
 	/**
 	 *
 	 *
 	 **/			
 	public double determineModFactor(double lowerBound1, double upperBound1, double lowerBound2, double upperBound2, boolean groupMember)			
 	{			
 		
 		/*Assume first that ind2 is not in the same group as the implicit individual*/
 		double modFactor = performTransformation(lowerBound2,upperBound2, this.getUninjuredDifferent()); 
 		
 		/*Check if the above assumption is incorrect, and correct the value for modFactor if ind2 a group member*/
 		if(groupMember)
		{ modFactor = performTransformation(lowerBound1,upperBound1, this.getUninjuredSame());}
		
		return modFactor;
	}
 	/**
 	 *Keeps boundaries for parameter values enforced
 	 *
 	 *@param
 	 *@returns double the bounded parameter value
 	 **/
 	public double enforceIntervalBounds(double lowerBound, double upperBound, double parameter )
 	{
 		if( parameter < lowerBound)
 		{return lowerBound;}
 		if(parameter > upperBound)	
 		{return upperBound;}
 		return parameter;	
 	}
 	
 	/**
 	 *Used to transform the parameter value from the standardized [0,1] range to the specified
 	 *[a,b] range.
 	 *
 	 *@param
 	 *@returns double the non standardized value of the parameter
 	 **/
 	public double performTransformation(double lowerBound, double upperBound, double preImage)
 	{
 			return lowerBound*(1.0 - preImage) + upperBound*preImage;
 	}
 	
 	
 	/**
	 *Sets all the parameters from a key value pairs collection.
	 *
	 *@param parameters The parameters for the indiviudal.
	 **/
 	public void setParameters(Map parameters)
 	{
 		super.setParameters(parameters);
 		
 		if( parameters.containsKey("uninjuredSame"))
 		{
 			setUninjuredSame( Parameters.convertToDouble( 
		    parameters.get( "uninjuredSame" ), 
		    getUninjuredSame(),
			"uninjuredSame must be a Double or a string representing a Double." ) );
 		
 		}
 		
 		if( parameters.containsKey("injuredSame"))
 		{
 			setInjuredSame( Parameters.convertToDouble( 
			parameters.get( "injuredSame" ), 
			getInjuredSame(),
			"injuredSame must be a Double or a string representing a Double." ) );
 			
 		}
 		
 		if( parameters.containsKey("deadSame"))
 		{
 			setDeadSame( Parameters.convertToDouble( 
			parameters.get( "deadSame" ), 
			getDeadSame(),
			"deadSame must be a Double or a string representing a Double." ) );
 		}
 		
 		if( parameters.containsKey("uninjuredDifferent"))
 		{
 		    setUninjuredSame( Parameters.convertToDouble( 
			parameters.get( "uninjuredDifferent" ), 
			getUninjuredDifferent(),
			"uninjuredDifferent must be a Double or a string representing a Double." ) );		
 		
 		}
 		
 		if( parameters.containsKey("injuredDifferent"))
 		{
 		    setUninjuredSame( Parameters.convertToDouble( 
			parameters.get( "uninjuredDifferent" ), 
			getInjuredDifferent(),
			"injuredDifferent must be a Double or a string representing a Double." ) );		
 		
 		}
 		if( parameters.containsKey("deadDifferent"))
 		{
 		    setUninjuredSame( Parameters.convertToDouble( 
			parameters.get( "deadDifferent" ), 
			getUninjuredSame(),
			"deadDifferent must be a Double or a string representing a Double." ) );		
 		
 		}
 }
 	
///////////////////////////*Accessor and Mutator Methods*////////////////////////
 	
 	public double getUninjuredSame()
 	{
 		return this.uninjuredSame;
 	}
 	
 	public void setUninjuredSame(double val )
 	{
 		
 		double value = enforceIntervalBounds(0,1,val);
 		this.uninjuredSame = value;
 	}
 	
 	public double getInjuredSame()
 	{
 		return this.injuredSame;
 	}
 	
 	public void setInjuredSame(double val )
 	{
 		double value = enforceIntervalBounds(0,1,val);
 		this.injuredSame = value;
 	}
 	
 	public double getDeadSame()
 	{
 		return this.deadSame;
 	}
 	
 	public void setDeadSame(double val )
 	{
 		double value = enforceIntervalBounds(0,1,val);
 		this.deadSame = value;
 	}
 	
 	public double getUninjuredDifferent()
 	{
 		return this.uninjuredDifferent;
 	}
 	
 	public void setUninjuredDifferent(double val )
 	{
 		
 		double value = enforceIntervalBounds(0,1,val);
 		this.uninjuredSame = value;
 	}
 	
 	public double getInjuredDifferent()
 	{
 		return this.injuredDifferent;
 	}
 	
 	public void setInjuredDifferent(double val )
 	{
 		double value = enforceIntervalBounds(0,1,val);
 		this.injuredDifferent = value;
 	}
 	
 	public double getDeadDifferent()
 	{
 		return this.deadDifferent;
 	}
 	
 	public void setDeadDifferent(double val )
 	{
 		double value = enforceIntervalBounds(0,1,val);
 		this.deadDifferent = value;
 	}
 	
 }