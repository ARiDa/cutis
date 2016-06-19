package fr.david.mdm.dbscan;


public class ClusterObjectDBScan<T> implements Comparable<ClusterObjectDBScan<T>> {
	protected boolean processed;
	protected double reachabilityDistance;
	protected boolean isCore;
	protected T originalObject;
	protected int idCluster;

	public ClusterObjectDBScan(T originalObject) {
		this.originalObject = originalObject;
		processed = false;
		reachabilityDistance = Double.POSITIVE_INFINITY;
		isCore = false;
		idCluster = -1;
	}


	public T getOriginalObject() {
		return originalObject;
	}

	public void setOriginalObject(T originalObject) {
		this.originalObject = originalObject;
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}
	
	public boolean isCore() {
		return isCore;
	}

	public void setIsCore(boolean isCore) {
		this.isCore = isCore;
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
		return reachabilityDistance + " - " + originalObject.toString()+" - "+idCluster;
	}

	public void setIdCluster(int id) {
		idCluster = id;
	}
	
	public int getIdCluster(){
		return idCluster;
	}

	@Override
	public int compareTo(ClusterObjectDBScan<T> o) {
		return new Double(reachabilityDistance).compareTo(new Double(o
				.getReachabilityDistance()));
	}
}

