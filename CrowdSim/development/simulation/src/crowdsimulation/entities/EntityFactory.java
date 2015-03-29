/*
 * $RCSfile: EntityFactory.java,v $ $Date: 2010/11/08 16:05:02 $
 */
package crowdsimulation.entities;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;
import crowdsimulation.actioncontroller.strategy.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import java.awt.*;
import java.util.*;
import math.*;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;


/**
 * This is a factory for contructing most entities.
 * This is the only way to construct an individual or an obstacle.
 * This is based on the FACTORY pattern, where all construction of
 * certain types of objects are controlled by a single object.
 * This also implements the SINGLETON pattern such that there is only 
 * ever one factory for the simulation.
 *
 * @author $Author: dkaup $
 * @version $Revision: 1.4 $
 * $State: Exp $
 * $Date: 2010/11/08 16:05:02 $
 **/
public class EntityFactory implements Steppable
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** Unique identifier for all entities created. **/
	static int entityID = 0;
	/** The instance of this class which will be used in the singleton implementation. **/
	static EntityFactory instance = null;
	/** The minimum distance between individuals **/
	static double epsilon = 0.00001;
	/** The current id of any groups of entities created. **/
	static int groupID = 0;
	
	/** A collection of data for creating individuals at given time intervals during the simualtion. **/
	Bag recurringIndividuals = new Bag();
	/** A collection of data for creating obstacles at given time intervals during the simualtion. **/
	Bag recurringObstacles = new Bag();
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a set of individuals from the data being passed in. 
	 * 
	 * @param numberOfIndividuals The number of individuals to create.
	 * @param regionOfCreation The region where these individuals should be created.
	 * @param movementController The Action Controller which is in charge of moving these individuals.
	 * @param attributes The Map of parameters which are used to initialize the individuals.
	 * @return A bag of individuals created from the information which was passed in. 
	 **/
	public Bag createIndividuals
		( 
			int numberOfIndividuals, 
			Obstacle regionOfCreation,  
			ActionController movementController,
			Map attributes
		)
	{
		Bag people = new Bag();
		
		if( numberOfIndividuals>0 )
		{
			Image image = this.createImageForIndividuals( attributes );
			
			if( attributes.containsKey( "groups" ) )
			{
				int numGroups = 0;
				Object numGroupsObject = attributes.get( "groups" );
				if( numGroupsObject instanceof Integer )
				{
					numGroups = ((Integer)numGroupsObject).intValue();
				}
				else if( numGroupsObject instanceof String )
				{
					numGroups = Integer.parseInt((String)numGroupsObject);
				}
				
				int groupSize = 0;
				Object groupSizeObject = attributes.get( "groupSize" );
				if( groupSizeObject instanceof Integer )
				{
					groupSize = ((Integer)groupSizeObject).intValue();
				}
				else if( groupSizeObject instanceof String )
				{
					groupSize = Integer.parseInt((String)groupSizeObject);
				}

				double groupDist = 2;
				Obstacle groupRegion = null;
				if( attributes.containsKey( "groupDist" ) )
				{
					Object groupDistObject = attributes.get( "groupDist" );
					if( groupDistObject instanceof Double )
					{
						groupDist = ((Double)groupSizeObject).doubleValue();
					}
					else if( groupDistObject instanceof String )
					{
						groupDist = Double.parseDouble((String)groupSizeObject);
					}
				}

				for( int i = 0; i < numGroups && numberOfIndividuals > 0; i++ )
				{
					groupID++;
					for( int j =0; j < groupSize && numberOfIndividuals > 0; j++ )
					{
		        		Individual person = new Individual( entityID++, attributes );
			            person.setActionController( movementController );
			            if( j==0 )
			            {
			            	person.setLocation( this.getValidStartLocation( person, regionOfCreation ) );
			            	groupRegion = new CircularObstacle
			            		( new Continuous2D( 10, CrowdSimulation.getInstance().getTerrain().getWidth(), CrowdSimulation.getInstance().getTerrain().getHeight() ), 
			            		  person.getCenter().x, 
			            		  person.getCenter().y, 
			            		  groupDist );
			            }
			            else
			            {
			            	person.setLocation( this.getValidStartLocation( person, groupRegion ) );
			            }
			            person.setActionStrategy( this.createActionStrategy( movementController, attributes ) );
			            person.setGroupId( groupID );
			            person.setParameters( attributes );
			            
			            if(image != null)
			            {
			            	person.setImage(image);
			            }
			            CrowdSimulation.getInstance().getWorld().setObjectLocation( person, person.getLocation().getDouble2D());
			            people.add( person );
			            
			            numberOfIndividuals--;
					}	
				}
			}
			
			for(int i=0; i<numberOfIndividuals; i++)
			{
        		Individual person = new Individual( entityID++, attributes );
	            person.setActionController( movementController );
	            person.setLocation( this.getValidStartLocation( person, regionOfCreation ) );

	            person.setActionStrategy( this.createActionStrategy( movementController, attributes ) );
	            
	            if(image != null)
	            {
	            	person.setImage(image);
	            }
	            CrowdSimulation.getInstance().getWorld().setObjectLocation( person, person.getLocation().getDouble2D());
	            people.add( person );
			}
		}
		
		return people;
	}


	/**
	 * Creates a set of individuals from the data being passed in, and create these individuals at given intervals throught the simulation. 
	 * These individuals are set in the action controller at the given interval determined by the timeBetweenCreations parameter.
	 * 
	 * @param timeBetweenCreations The time to wait before creating another set of individuals.
	 * @param numberOfIndividuals The number of individuals to create.
	 * @param regionOfCreation The region where these individuals should be created.
	 * @param movementModel The Action Controller which is in charge of moving these individuals.
	 * @param attributes The Map of parameters which are used to initialize the individuals.
	 **/
	public void createIndividuals
		( 
			Object timeBetweenCreations,
			int numberOfIndividuals, 
			Obstacle regionOfCreation,
			ActionController movementModel,
			Map attributes
		)
	{
		Object elements[] = new Object[7];
		elements[0] = new Double( 0 );
		elements[1] = timeBetweenCreations;
		elements[2] = new Integer( numberOfIndividuals );
		elements[3] = regionOfCreation;
		elements[4] = movementModel;
		elements[5] = attributes;
                elements[6] = null;
		recurringIndividuals.add( elements );
	}
	
	/**
	 * Creates an obstacle from the data being passed in. 
	 * 
	 * @param movementController The Action Controller which is in charge this obstacle.
	 * @param attributes The Map of parameters which are used to initialize the obstacle.
	 * @return The obstacle created from the information which was passed in. 
	 **/
	public Obstacle createObstacle
		( 
			ActionController movementController,
			Map attributes
		)
	{
		Obstacle obstacle = Obstacle.build( entityID++, attributes );
		
		// This code allows starting regions to not be visible since
		// we don't set a movementModel for a starting region.
		if( movementController != null )
		{
			obstacle.setActionController( movementController );
			CrowdSimulation.getInstance().getTerrain().
				setObjectLocation( obstacle, obstacle.getLocation().getDouble2D() );
		}
		
		return obstacle;
	}
	
	/**
	 * Creates an obstacle from the data being passed in, and creates it again at a given time interval. 
	 * 
	 * @param timeBetweenCreations The time to wait before creating a new obstacle.
	 * @param movementController The Action Controller which is in charge this obstacle.
	 * @param attributes The Map of parameters which are used to initialize the obstacle.
	 **/
	public void createObstacles
		( 
			double timeBetweenCreations,
			ActionController movementController,
			Map attributes
		)
	{
		Object elements[] = new Object[4];
		elements[0] = new Double( 0 );
		elements[1] = new Double( timeBetweenCreations );
		elements[2] = movementController;
		elements[3] = attributes;
		recurringIndividuals.add( elements );
	}
	
	/**
	 * This function returns a valid starting location for an individual.  It
	 * checks that the returned point does not lie inside any of the obstacles, or individuals.
	 *
	 * @param ind The individual which is trying to be placed in the starting region.
	 * @param startRegion The region in which the individual is supposed to be placed.
	 * @return The starting location of the individual or object.
	 */
	private Vector2D getValidStartLocation( Individual ind, Obstacle startRegion )
	{
		Vector2D startLocation = null;
		Random rand = new Random();
		RandomGenerators gen = new UniformDistributedGenerator( 0, 1 );
		Continuous2D world = CrowdSimulation.getInstance().getWorld();

		// Keep looping through random start locations until you find "valid" ones
		boolean foundValidLocation = false;
		while(foundValidLocation == false)
		{
			// Start location for an individual or object
			if( startRegion instanceof CircularObstacle )
			{
				CircularObstacle circRegion = (CircularObstacle)startRegion;
				startLocation = new Vector2D
					( 
						circRegion.getLocation().x-circRegion.getRadius()+
						2*rand.nextDouble()*circRegion.getRadius(), 
						circRegion.getLocation().y-circRegion.getRadius()+
						2*rand.nextDouble()*circRegion.getRadius() 
					);
			}
			else
			{
				RectangularObstacle recRegion = (RectangularObstacle)startRegion;
				startLocation = new Vector2D
					( 
						recRegion.getLocation().x+
						rand.nextDouble()*recRegion.getWidth(), 
						recRegion.getLocation().y+
						rand.nextDouble()*recRegion.getHeight() 
					);
			}
			
			// Check that the start location is not inside any of the Obstacles
			foundValidLocation = true;
			
			// If the startRegion is defined then check if we are in it.
			if( startRegion != null )
			{
				// Check if the starting location is inside the region.
				foundValidLocation = startRegion.isInside(startLocation);
			}
			
			// These conditions handle the start region being possibly null.
			if(startRegion == null || (startRegion != null && foundValidLocation) )
			{
				// Make sure it is not anyside any of the obstacles.
				for( int i=0; i< CrowdSimulation.getInstance().getTerrain().allObjects.size(); i++ )
				{
					if(((Obstacle)(CrowdSimulation.getInstance().getTerrain().allObjects.objs[i])).isInside( startLocation ))
					{
						foundValidLocation = false;
						break;	
					}
				}
			}
			
			// If a start location is valid so far we need to do one more check
			// and make sure that the individual isn't too close to another individual
			if( foundValidLocation )
			{
				for( int i=0; i< world.allObjects.size(); i++ )
				{
					Individual currentInd = (Individual)CrowdSimulation.getInstance().getWorld().allObjects.objs[i];
					// Check if the new individual intersects with individual i
					if( startLocation.distance( currentInd.getLocation() ) <
						( (currentInd.getDiameter() + ind.getDiameter())/2.0 + this.epsilon) )
					{
						foundValidLocation = false;	
						break;
					}		
				}
			}
		}
		
		return startLocation;
	}
	
	/**
	 * Constructs an image for a set of individuals.
	 * This reads in an image if there is one in the attrbiutes set.
	 * This is helpfull in trying to create image portyals for entities.
	 *
	 * @param attributes The set of attributes which chould contain an image key value pair.
	 * @return The image class loaded from the information in the attributes.
	 **/
	private Image createImageForIndividuals( Map attributes )
	{
		Image image = null;
		if( attributes.containsKey( "image" ) )
		{
			Object value = attributes.get( "image" );
			
			String fileName = "../images/" + (String)value;
			
			// This quirky method is used because it immediately loads the image.	
			image = new javax.swing.ImageIcon(fileName).getImage();		
		}
		
		return image;
	}

	/**
	 * Create an action strategy for an individual based on the specified input parameters.
	 * 
	 * @param movementController The movement controller for the strategy to be created.
	 * @param attributes The Map of attrbitues to initialize the ActionStrategy.
	 * @return The action strategy to be used by an individual to determine it's movement.
	 **/
	private ActionStrategy createActionStrategy( ActionController movementController, Map attributes )
	{
		ActionStrategy newStrategy = movementController.getNewStrategy();
		
		if( newStrategy != null )
		{
			newStrategy.setParameters( attributes );
		}
		
		return newStrategy;
	}

	/**
	 * This is the method called at each step of the simulation.
	 * This allows for individuals an obstacle to be created at defined time intervals thrughout the simulation.
	 * each step of the simulation this checks to see if any individuals, or obstacles need to be created.
	 *
	 * @param state This is the object containing the information of the current state of the simulation.
	 **/
    public void step( SimState state )
	{
		CrowdSimulation simState = (CrowdSimulation)state;
		
		//check to see if any individuals need to be created this timestep
                //System.out.println("Reccuring Individual size : " + recurringIndividuals.size());
                for( int i = 0; i < recurringIndividuals.size(); i++ )
		{
			Object entitiesData[] = (Object[])recurringIndividuals.get(i);
                        double timebetweenCreations = 0;
                        if(entitiesData[6] == null)
                        {
                            if(entitiesData[1] instanceof Double)
                            {
                                timebetweenCreations = (Double)entitiesData[1];
                            }
                            else if(entitiesData[1] instanceof RandomGenerators)
                            {
                                 
                                 timebetweenCreations = ((RandomGenerators)entitiesData[1]).nextValue();
                                 entitiesData[6] = (Double)timebetweenCreations; 
                            }        
                        }
                        else
                        {
                            timebetweenCreations = (Double)entitiesData[6];
                        }

			if( (simState.getSimTime()-((Double)entitiesData[0]).doubleValue()) > timebetweenCreations )
			{
				if( entitiesData.length == 7 )
				{
                                    //System.out.println("Number of Individuals " + ((Integer)entitiesData[2]).intValue());
                                    //System.out.println("Time between entity Creations : " + timebetweenCreations);
                                    //System.out.println("Sim Time : " + simState.getSimTime());
                                    //System.out.println("entitiesData[0] : " + ((Double)entitiesData[0]).doubleValue());
					
                                    createIndividuals
						( 
							((Integer)entitiesData[2]).intValue(), 
							((Obstacle)entitiesData[3]),
							((ActionController)entitiesData[4]),
							((Map)entitiesData[5])
						);
					entitiesData[0] = new Double( simState.getSimTime() );
                                        entitiesData[6] = null;
				}
				if( entitiesData.length == 4 )
				{
					createObstacle
						( 
							((ActionController)entitiesData[2]),
							((Map)entitiesData[3])
						);
					entitiesData[0] = new Double( simState.getSimTime() );
				}

			}

		}
	}

	/**
	 * This is how any object in the simulation gets a reference to the EntityFactory.
	 * This is the method implementing the SINGLETON pattern.
	 *
	 * @return The instance on the EntityFactor that is to be used trhough the simulation.
	 **/
	public static EntityFactory getInstance()
	{
		if( instance == null )
		{
			instance = new EntityFactory();
		}
		
		return instance;
	}

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

}
