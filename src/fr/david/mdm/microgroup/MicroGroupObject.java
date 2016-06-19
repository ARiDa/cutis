package fr.david.mdm.microgroup;

import fr.david.mdm.Enum.State;
import fr.david.mdm.dbscan.ClusterObjectDBScan;

public class MicroGroupObject<T> implements Comparable<MicroGroupObject<T>>{
	protected State processed;
	protected double reachabilityDistance; //distance between the object and the representative subtrajectory
	protected T originalObject;
	protected int idMicroGroup;

	public MicroGroupObject(T originalObject) {
		this.originalObject = originalObject;
		processed = State.UNVISITED;
		reachabilityDistance = Double.POSITIVE_INFINITY;
		idMicroGroup = -1;
	}


	public T getOriginalObject() {
		return originalObject;
	}

	public void setOriginalObject(T originalObject) {
		this.originalObject = originalObject;
	}

	public State getProcessed() {
		return processed;
	}

	public void setProcessed(State state) {
		this.processed = state;
	}
	
	public double getReachabilityDistance() {
		return reachabilityDistance;
	}

	public void setReachabilityDistance(double reachabilityDistance) {
		this.reachabilityDistance = reachabilityDistance;
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ClusterObjectDBScan))
			return false;
		ClusterObjectDBScan co = (ClusterObjectDBScan) o;
		if (originalObject != null)
			return originalObject.equals(co.getOriginalObject());
		return super.equals(o);
	}

	public String toString() {
		return reachabilityDistance + " - " + originalObject.toString()+" - "+idMicroGroup;
	}

	public void setIdMicroGroup(int id) {
		idMicroGroup = id;
	}
	
	public int getIdMicroGroup(){
		return idMicroGroup;
	}

	public int compareTo(MicroGroupObject<T> o) {
		return new Double(reachabilityDistance).compareTo(new Double(o
				.getReachabilityDistance()));
	}

}
