package fr.david.mdm.dataset;

import java.util.Vector;

import fr.david.mdm.models.TrajectoryAsSet;
import fr.david.mdm.traclus.clustering.DirectionSpaceTimeDistanceTraclus;
import fr.david.mdm.traclus.clustering.TraClusterDoc;

public class TraclusClustersFinder {

	/*Dbscan parameters*/
	private double eps = 0.004;
	private int minPoints = 20;	
	
	private DirectionSpaceTimeDistanceTraclus dtraclus;
	private TraClusterDoc tcd;
	
	public TraclusClustersFinder(double eps, int minPoints) {
		this.eps=eps;
		this.minPoints=minPoints;
		this.dtraclus = new DirectionSpaceTimeDistanceTraclus();
	}
	
	
	
	public void computeTimeWindowTraclusClusters(int time_window, Vector<TrajectoryAsSet> objectList){
		
		tcd = new TraClusterDoc(dtraclus);						
		tcd.onOpenDocument(objectList);
		tcd.onClusterGenerate("timewindow"+time_window+"-traclus.txt", eps,minPoints, time_window); 
		
	}
	
	
}
