package crowdsimulation.logging;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.*;	// Contains models of behavior for the entities. 
import crowdsimulation.entities.*;		// Contains definition of the crowd agents and features.
import crowdsimulation.entities.individual.*;
import java.io.*;
import java.util.*;
import sim.engine.*;
import sim.util.*;
import crowdsimulation.actioncontroller.cellularautomata.*;
import crowdsimulation.actioncontroller.cellularautomata.strategy.*;
import crowdsimulation.entities.obstacle.*;
import crowdsimulation.logging.*;
import crowdsimulation.util.*;
import crowdsimulation.actioncontroller.strategy.*;
import crowdsimulation.maps.*;
import ec.util.*;


public class SimLog
{

	double[] total_serviceTime_job = {0,0,0};
	int[] numSampleServiceTimeJob = {0,0,0};
	double[] total_queueDelay_job = {0,0,0};
	int[] numSampleDelayJob = {0,0,0};
	double[] total_queueDelay_ws = {0,0,0,0,0};
	int[] numSampleDelayWS = {0,0,0,0,0};
	double[][] total_serviceTime_job_ws = { { 0,0,0,0,0}, { 0,0,0,0,0 },
        { 0,0,0,0,0 }, };
	int[][] numSampleServiceTime_job_ws = { { 0,0,0,0,0}, { 0,0,0,0,0 },
        { 0,0,0,0,0 }, };
	double[] totalNumInQueue = {0,0,0,0,0};
	
	
	public SimLog()
	{

	}
	
	
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	public void setTotalServiceTimeJob(int jobNumber, double val)
	{
		total_serviceTime_job[jobNumber] += val;
		this.numSampleServiceTimeJob[jobNumber] += 1;
	}
	public double getTotalServiceTimeJob(int jobNumber)
	{
		return total_serviceTime_job[jobNumber];
	}
	
	public void setTotalQueueDelayJob(int jobNumber, double val)
	{
		total_queueDelay_job[jobNumber] += val;
		this.numSampleDelayJob[jobNumber] += 1;
	}
	public double getTotalQueueDelayJob(int jobNumber)
	{
		return total_queueDelay_job[jobNumber];
	}
	
	public void setTotalQueueDelayWS(int wsNumber, double val)
	{
		total_queueDelay_ws[wsNumber] += val;
		this.numSampleDelayWS[wsNumber] += 1;
	}
	public double getTotalQueueDelayWS(int wsNumber)
	{
		return total_queueDelay_ws[wsNumber];
	}
	
	public void setTotalNumInWsQueue(int wsNumber, double val)
	{
		//Log.log(1,"WS Queue " + wsNumber + " val : " + val);
		totalNumInQueue[wsNumber] += val;
		//Log.log(1,"Total Number " + getTotalNumInWsQueue(wsNumber));
	}
	public double getTotalNumInWsQueue(int wsNumber)
	{
		return totalNumInQueue[wsNumber];
	}
	
	public void setTotalServiceTime_Job_Ws(int jobNumber, int wsNumber, double val)
	{
		total_serviceTime_job_ws[jobNumber][wsNumber] += val;
		this.numSampleServiceTime_job_ws[jobNumber][wsNumber] += 1;
	}
	public double getTotalServiceTime_Job_Ws(int jobNumber, int wsNumber)
	{
		return total_serviceTime_job_ws[jobNumber][wsNumber];
	}
	public void TestArrays()
	{
		
	Log.log(1,"total_serviceTime_job[2] " + total_serviceTime_job[1]);
	Log.log(1,"numSampleServiceTimeJob[2] " + numSampleServiceTimeJob[1]);
	Log.log(1,"total_queueDelay_job[2] "  + total_queueDelay_job[2]);
	Log.log(1,"numSampleDelayJob[2] " + numSampleDelayJob[2]);
	Log.log(1,"total_queueDelay_ws[2] " + total_queueDelay_ws[2]);
	Log.log(1,"numSampleDelayWS[2] " + numSampleDelayWS[2]);
	Log.log(1,"total_serviceTime_job_ws[2][2] " + total_serviceTime_job_ws[1][2]);
	Log.log(1,"numSampleServiceTime_job_ws[2][2] " + numSampleServiceTime_job_ws[1][2]);
	Log.log(1,"totalNumInQueue[2] " + totalNumInQueue[2]);
	}

}
