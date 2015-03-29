package crowdsimulation.entities.obstacle;

import java.util.*;
import crowdsimulation.*;
import crowdsimulation.util.*;
import crowdsimulation.logging.*;
import crowdsimulation.entities.*;
import crowdsimulation.entities.individual.*;
import crowdsimulation.actioncontroller.cellularautomata.*;
import crowdsimulation.actioncontroller.cellularautomata.strategy.*;
import sim.engine.*;
import sim.field.grid.*;
import sim.util.*;
import math.*;


public class StationObstacle extends RectangularObstacle implements Steppable {
	
	LinkedList StationQueue = new LinkedList();
    
	Individual[] theMachinesInUse; 
	
	HashMap theJobs;
	private int StationID;
	private int StationMachines;
	private double queueNumberStartTime = 0;
	private boolean runFinal = true;
	private double aveWsQueue = 0;
	private int Steps = 0;
	
public StationObstacle( int entity_id, Map parameters )  {
		super( entity_id, parameters );
		setParameters( parameters );
		CrowdSimulation.getInstance().schedule(this);
		theJobs = setUpMeanServiceTimes();
		theMachinesInUse = initMachinesinUse(getStationMachines());
	}
	/**
	 * This is the method called at each step of the simulation.
	 * 
	 * 
	 *
	 * @param state The object representing the current state of the simulation.
	 **/
	public void step( SimState state )
	{
		CrowdSimulation crowdState = CrowdSimulation.getInstance();
		double current_time = crowdState.getSimTime();
		
	    SimLog updateStats = crowdState.getUpdateStats();
	    
	    this.Steps = Steps + 1;
	    this.aveWsQueue = this.aveWsQueue + this.StationQueue.size();
	    
	    
		for(int i = 0;i < this.theMachinesInUse.length; i++)
		{
			Individual ind;
			SimLibCAStrategy theIndStrategy;
			if(this.theMachinesInUse[i] != null)
			{
				//Log.log(1,"Inside If");
				ind = (Individual)this.theMachinesInUse[i];
				theIndStrategy = (SimLibCAStrategy)ind.getActionStrategy();
				if(isTaskFinished(current_time, theIndStrategy))
				{
					
					endTask(ind, theIndStrategy, current_time, updateStats);
					this.theMachinesInUse[i] = null;
					if(StationQueue.size() > 0)
					{
						this.updateAverageNumberInQueue(current_time, updateStats);
						ind = (Individual)StationQueue.removeFirst();
						theIndStrategy = (SimLibCAStrategy)ind.getActionStrategy();
						//theIndStrategy.setInQueue(false);
						updateQueueStats(ind,theIndStrategy,current_time, updateStats);
						//Log.log(1,"Remove from queue , " + current_time + "," + ind.getID() + "," + theIndStrategy.jobNumber + "," + StationID);
						theIndStrategy.setEnterQueueTime(-99999);
						AttachToTask(current_time, ind, theIndStrategy);
						SetTaskLocation(ind);
					}
				}
			}
		}
		if((crowdState.getDuration() - current_time) < 0.31)
		{
			if(this.runFinal)
			{
			     Log.log(1, "ave in queue Station " + StationID + " " + aveWsQueue/Steps);
			   // Log.log(1,"This is the final log " + StationID);
			    this.updateAverageNumberInQueue(current_time, updateStats);
			    Individual aInd;
//			    for(int i = 0; i < StationQueue.size();i++)
//			    {
//				    aInd = (Individual)StationQueue.get(i);
//				    SimLibCAStrategy theIndStrategy = (SimLibCAStrategy)aInd.getActionStrategy();
//				    updateQueueStats(aInd,theIndStrategy,current_time, updateStats);
//			    }
			    this.runFinal = false;
			}
		}
	}
	
	
	public void updateQueueStats(Individual ind, SimLibCAStrategy theIndStrategy, double current_time, SimLog updateStats)
	{
//		if(theIndStrategy.getEnterQueueTime() < 0)
//		{
//			Log.log(1,"theIndStrategy.getEnterQueueTime() : " + theIndStrategy.getEnterQueueTime());
//			System.exit(0);
//		}
//		
		double timeInQueue = current_time - theIndStrategy.getEnterQueueTime();		
		Path thePath = ind.getSelectedPath();
		int jobNumber = thePath.getID();
		updateStats.setTotalQueueDelayJob(jobNumber,timeInQueue);
		
		theIndStrategy.timeInQueue += timeInQueue;
		
		updateStats.setTotalQueueDelayWS(this.StationID, timeInQueue);


	}
	
	public void endTask(Individual ind, SimLibCAStrategy theIndStrategy, double current_time, SimLog updateStats)
	{
		double taskTime = current_time - theIndStrategy.getTaskStartTime();
	//	if(theIndStrategy.getTaskStartTime() < 0)
	//	{
	//		Log.log(1,"theIndStrategy.getTaskStartTime() : " + theIndStrategy.getTaskStartTime());
	//	}
		
	//	if(theIndStrategy.getTaskDurationTime() < 0)
	//	{
	//		Log.log(1,"theIndStrategy.getTaskDurationTime() : " + theIndStrategy.getTaskDurationTime());
	//	}
		
	    updateStats.setTotalServiceTime_Job_Ws(theIndStrategy.getJobNumber(),
		this.StationID, taskTime);
		
		theIndStrategy.timeWorking +=  taskTime;
		
		theIndStrategy.setAttachToStation(false);
		
		//set to negative to insure correct output
		theIndStrategy.setTaskStartTime(-99999);
		theIndStrategy.setTaskDurationTime(-99999);
		//theIndStrategy.setIsWorking(false);
		NextActiveWaypoint(ind);
		
	}

	public boolean isTaskFinished(double current_time, SimLibCAStrategy theIndStrategy)
	{
		boolean taskFinished = false;
		double checkTime = current_time - theIndStrategy.getTaskStartTime();
	    if(checkTime >= theIndStrategy.getTaskDurationTime())
		{
			taskFinished = true;
		}		
		return taskFinished;
	
	}
	
	public void AttachToTask(double current_time, Individual ind, SimLibCAStrategy theStrategy)
	{
		theStrategy.setTaskStartTime(current_time);
		theStrategy.setTaskDurationTime(GetServiceTime(ind));
		//theStrategy.setIsWorking(true);
		setMachineToBusy(ind);		
	}
	public void AttachIndividualToStation(Individual ind)
	{
		
		CrowdSimulation crowdState = CrowdSimulation.getInstance();
		double current_time = crowdState.getSimTime();
		SimLog updateStats = crowdState.getUpdateStats();
		
		SimLibCAStrategy theStrategy = (SimLibCAStrategy)ind.getActionStrategy();
		theStrategy.setTheStation(this.StationID);
		theStrategy.setAttachToStation(true);
		
	 	if(isBusy())
	 	{
			theStrategy.setEnterQueueTime(current_time);
			this.updateAverageNumberInQueue(current_time, updateStats);
			//theStrategy.setInQueue(true);
			//Log.log(1,"Add to queue , " + current_time + "," + ind.getID() + "," + theStrategy.jobNumber + "," +this.StationID);
			this.StationQueue.add(ind);	
			this.SetQueueLocation(ind);
	 	}
		else
		{
			AttachToTask(current_time, ind, theStrategy);
					
		}
	}
	
	public boolean isBusy()
	{
		
		boolean stationBusy = true;
		//Log.log(1,"isBusy : " + this.theMachinesInUse.length);
		for(int i = 0;i < this.theMachinesInUse.length; i++)
		{
			//check to see if all the machines are busy
			if(this.theMachinesInUse[i] == null)
			{
				stationBusy = false;
				break;
			}
		}
			
		return stationBusy;
		
	}
	
	public void setMachineToBusy(Individual ind)
	{
		for(int i = 0;i < this.theMachinesInUse.length; i++)
		{
			//set first available machine to busy
			if(this.theMachinesInUse[i] == null)
			{
				this.theMachinesInUse[i] = ind;
				return;
			}
		}
	}
	
	private double GetServiceTime(Individual ind)
	{
		Path thePath = ind.getSelectedPath();
		int jobNumber = thePath.getID();
		return ((ErlangDistributedGenerator)((HashMap)theJobs.get(jobNumber)).get(this.StationID)).nextValue();
		
	}
	
	private void SetQueueLocation(Individual ind)
	{
		Vector2D center = this.getCenter();
		double stationHeight = this.getHeight();
		double stationWidth = this.getWidth();
		ind.setLocation(center.x,center.y + stationHeight);
	}
	
	private void SetTaskLocation(Individual ind)
	{
		Vector2D center = this.getCenter();
		double stationHeight = this.getHeight();
		double stationWidth = this.getWidth();
        
        SimLibCAModel caModel = (SimLibCAModel)ind.getActionController();
        SparseGrid2D theWorldGrid = (SparseGrid2D)caModel.getWorldGrid();
        Bag taskLocations = (Bag)theWorldGrid.getObjectsAtLocation((int)(center.x/0.4),(int)((center.y + stationHeight/2)/0.4));
		if(taskLocations == null)
		{
			ind.setLocation(center.x,center.y + stationHeight/2);
			return;
		}
		else
		{
			//Log.log(1,"Found Objects");
		}
		taskLocations = (Bag)theWorldGrid.getObjectsAtLocation((int)((center.x + stationWidth/2)/0.4),(int)(center.y/0.4));
		if(taskLocations == null)
		{
		   ind.setLocation(center.x + stationWidth/2,center.y);
		   return;
		}
		taskLocations = (Bag)theWorldGrid.getObjectsAtLocation((int)((center.x - stationWidth/2)/0/4),(int)(center.y/0.4));
		if(taskLocations == null)
		{
		   ind.setLocation(center.x - stationWidth/2,center.y);
		   return;
		}
		Log.log(1,"Cannot find a place for this guy");
		ind.setLocation(center.x - stationWidth/2,center.y + stationHeight/2);
	}
	
	private HashMap setUpMeanServiceTimes()
	{
		//Generators Job1
		ErlangDistributedGenerator erlang11 = new ErlangDistributedGenerator(2,18.00);
		ErlangDistributedGenerator erlang12 = new ErlangDistributedGenerator(2,21.60);
		ErlangDistributedGenerator erlang13 = new ErlangDistributedGenerator(2,30.60);
		ErlangDistributedGenerator erlang14 = new ErlangDistributedGenerator(2,18.00);

		//Generators Job2
		ErlangDistributedGenerator erlang21 = new ErlangDistributedGenerator(2,39.60);
		ErlangDistributedGenerator erlang22 = new ErlangDistributedGenerator(2,28.80);
		ErlangDistributedGenerator erlang23 = new ErlangDistributedGenerator(2,27.00);
		
		//Generators Job3
		ErlangDistributedGenerator erlang31 = new ErlangDistributedGenerator(2,43.20);
		ErlangDistributedGenerator erlang32 = new ErlangDistributedGenerator(2,9.00);
		ErlangDistributedGenerator erlang33 = new ErlangDistributedGenerator(2,25.20);
		ErlangDistributedGenerator erlang34 = new ErlangDistributedGenerator(2,32.40);		
		ErlangDistributedGenerator erlang35 = new ErlangDistributedGenerator(2,36.00);
				
//		//Generators Job1
//		ErlangDistributedGenerator erlang11 = new ErlangDistributedGenerator(2,10);
//		ErlangDistributedGenerator erlang12 = new ErlangDistributedGenerator(2,15);
//		ErlangDistributedGenerator erlang13 = new ErlangDistributedGenerator(2,20);
//		ErlangDistributedGenerator erlang14 = new ErlangDistributedGenerator(2,25);
//
//		//Generators Job2
//		ErlangDistributedGenerator erlang21 = new ErlangDistributedGenerator(2,7);
//		ErlangDistributedGenerator erlang22 = new ErlangDistributedGenerator(2,13);
//		ErlangDistributedGenerator erlang23 = new ErlangDistributedGenerator(2,18);
//		
//		//Generators Job3
//		ErlangDistributedGenerator erlang31 = new ErlangDistributedGenerator(2,10);
//		ErlangDistributedGenerator erlang32 = new ErlangDistributedGenerator(2,15);
//		ErlangDistributedGenerator erlang33 = new ErlangDistributedGenerator(2,20);
//		ErlangDistributedGenerator erlang34 = new ErlangDistributedGenerator(2,25);		
//		ErlangDistributedGenerator erlang35 = new ErlangDistributedGenerator(2,30);		
//		
		
		HashMap StationToServiceTime = new HashMap();
		HashMap JobToServiceTimes = new HashMap();
		
		//Setup Job1
		StationToServiceTime.put(2,erlang11);
		StationToServiceTime.put(0,erlang12);
		StationToServiceTime.put(1,erlang13);
		StationToServiceTime.put(4,erlang14);
		
		JobToServiceTimes.put(0,StationToServiceTime);
		
		StationToServiceTime.clear();
		
		//Setup Job2
		StationToServiceTime.put(3,erlang21);
		StationToServiceTime.put(0,erlang22);
		StationToServiceTime.put(2,erlang23);

		JobToServiceTimes.put(1,StationToServiceTime);
		
		StationToServiceTime.clear();
		
		//Setup Job3
		StationToServiceTime.put(2,erlang31);
		StationToServiceTime.put(0,erlang32);
		StationToServiceTime.put(4,erlang33);
		StationToServiceTime.put(1,erlang34);
		StationToServiceTime.put(3,erlang35);
		
		JobToServiceTimes.put(2,StationToServiceTime);
		
		return JobToServiceTimes;
	}
	@Override
	public void setParameters( Map parameters )
	{
		super.setParameters(parameters);
		if( parameters.containsKey( "StationID" ) )
		{
				setStationID( Parameters.convertToInt ( 
				parameters.get( "StationID" ), 
				getStationID(),
			   "StationID for StationObstacle construction must be a Integer or a string representing a Integer." ) );
		}
		if( parameters.containsKey( "StationMachines" ) )
		{
				setStationMachines( Parameters.convertToInt ( 
				parameters.get( "StationMachines" ), 
				getStationID(),
			   "StationMachines for StationObstacle construction must be a Integer or a string representing a Integer." ) );
		}
	}
	
    private void NextActiveWaypoint(Individual ind)
	{
		//Log.log(1,ind.getID() + " Next Active Waypoint");
		// Set the next active waypoint
		Path thePath = ind.getSelectedPath();
	    thePath.setActiveWaypoint(this.getCenter());
	}
	
	private Individual[] initMachinesinUse(int arrayLength)
	{
		//Log.log(1,"Init Machines in Use - arrayLength " + arrayLength);
		Individual[] aArray = new Individual[arrayLength];
		return aArray;
	}
	
	private void updateAverageNumberInQueue(double currentTime, SimLog updateStats)
	{
		double theValue = (currentTime - this.queueNumberStartTime)*this.StationQueue.size();
	    updateStats.setTotalNumInWsQueue(StationID,theValue);
	    this.queueNumberStartTime = currentTime;
	}
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets Station ID
	 *
	 * @param val - StationID
	 **/

	private void setStationID( int val )
	{
		StationID = val;
	}
	private int getStationID( )
	{
		return this.StationID;
	}
	private void setStationMachines( int val )
	{
		StationMachines = val;
	}
	private int getStationMachines( )
	{
		return this.StationMachines;
	}
				
}
