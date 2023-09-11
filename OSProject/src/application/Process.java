package application;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;


public class Process implements Comparable<Object>{
	public int id;
	int arrivalTime;
	LinkedList<Integer> cpuBurstTime;
	LinkedList<Integer> copyOfCpuBurstTime;
	LinkedList<Integer> ioBurstTime;
	int status; // 0: running, 1: waiting, 2:ready, 3: finished
	Timer timer;
	int noOfPreemptionQ1;
	int noOfPreemptionQ2;
	int noOfPreemptionQ3;
	int previousCpuBurst;
	double previousPredict;
	double predictOfNextBurst;
	int currentQ;
	int turnAroundTime;
	int waitingTime;
	int sumOfCPUBursts;
	int sumOfIOBursts;
	

	public Process(int id, int arrivalTime, LinkedList<Integer> cpuBurstTime, LinkedList<Integer> ioBurstTime) {
		this.id = id;
		this.arrivalTime = arrivalTime;
		this.cpuBurstTime = cpuBurstTime;
		this.ioBurstTime = ioBurstTime;
		this.copyOfCpuBurstTime = new LinkedList<>(cpuBurstTime);//make a copy
		this.status = 0;
		this.noOfPreemptionQ1=0;
		this.noOfPreemptionQ2=0;
		this.noOfPreemptionQ3=0;
		//this.previousCpuBurst=0;
		this.previousPredict=0;
		this.predictOfNextBurst=0;
		this.sumOfCPUBursts=0;
		this.sumOfIOBursts=0;
		for (Integer integer : cpuBurstTime) {
			this.sumOfCPUBursts += integer;
		}
		for (Integer integer : ioBurstTime) {
			this.sumOfIOBursts += integer;
		}
		
		
		timer = new Timer();
	}
	
	public int getId() {
		return id;
	}



	public void setId(int id) {
		this.id = id;
	}



	public int getArrivalTime() {
		return arrivalTime;
	}



	public void setArrivalTime(int arrivalTime) {
		this.arrivalTime = arrivalTime;
	}



	public LinkedList<Integer> getCpuBurstTime() {
		return cpuBurstTime;
	}



	public void setCpuBurstTime(LinkedList<Integer> cpuBurstTime) {
		this.cpuBurstTime = cpuBurstTime;
	}



	public LinkedList<Integer> getIoBurstTime() {
		return ioBurstTime;
	}



	public void setIoBurstTime(LinkedList<Integer> ioBurstTime) {
		this.ioBurstTime = ioBurstTime;
	}



	public int getStatus() {
		return status;
	}



	public void setStatus(int status) {
		this.status = status;
	}



	public Timer getTimer() {
		return timer;
	}



	public void setTimer(Timer timer) {
		this.timer = timer;
	}
	
	@Override
	public int compareTo(Object o) {
		return this.arrivalTime - ((Process)o).arrivalTime;
	}
	
	

	public int getNoOfPreemptionQ1() {
		return noOfPreemptionQ1;
	}

	public void setNoOfPreemptionQ1(int noOfPreemptionQ1) {
		this.noOfPreemptionQ1 = noOfPreemptionQ1;
	}

	public int getNoOfPreemptionQ2() {
		return noOfPreemptionQ2;
	}

	public void setNoOfPreemptionQ2(int noOfPreemptionQ2) {
		this.noOfPreemptionQ2 = noOfPreemptionQ2;
	}

	public int getNoOfPreemptionQ3() {
		return noOfPreemptionQ3;
	}

	public void setNoOfPreemptionQ3(int noOfPreemptionQ3) {
		this.noOfPreemptionQ3 = noOfPreemptionQ3;
	}

	
	public int getPreviousCpuBurst() {
		int n = copyOfCpuBurstTime.size() - cpuBurstTime.size();
		int prev;
		if(n==0) {
			prev=copyOfCpuBurstTime.get(0);
		}
		else {
			prev = copyOfCpuBurstTime.get(n-1);
		}
		return prev;
	}

	public void setPreviousCpuBurst(int previousCpuBurst) {
		this.previousCpuBurst = previousCpuBurst;
	}
	
	

	public double getPredictOfNextBurst() {
		return predictOfNextBurst;
	}

	public void setPredictOfNextBurst(double predictOfNextBurst) {
		this.predictOfNextBurst = predictOfNextBurst;
	}
	
	

	public double getPreviousPredict() {
		return previousPredict;
	}

	public void setPreviousPredict(double previousPredict) {
		this.previousPredict = previousPredict;
	}
	
	

	public int getCurrentQ() {
		return currentQ;
	}

	public void setCurrentQ(int currentQ) {
		this.currentQ = currentQ;
	}

	
	public int getTurnAroundTime() {
		return turnAroundTime;
	}

	public void setTurnAroundTime(int turnAroundTime) {
		this.turnAroundTime = turnAroundTime;
	}

	public int getWaitingTime() {
		return waitingTime;
	}

	public void setWaitingTime(int waitingTime) {
		this.waitingTime = waitingTime;
	}

	@Override
	public String toString() {
		return "Process [id=" + id + ", arrivalTime=" + arrivalTime + ", cpuBurstTime=" + cpuBurstTime
				+ ", ioBurstTime=" + ioBurstTime + ", status=" + status + "]";
	}

	
	
	

}


class SortByPredictOfNextBurst implements Comparator<Process> { //compare two media by their titles  
	
	public int compare(Process o1, Process o2) {
		return Double.compare(o1.getPredictOfNextBurst(), o2.getPredictOfNextBurst());
	}

}




