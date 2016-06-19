package fr.david.mdm.dataset;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import fr.david.mdm.dbscan.ClusterObjectDBScan;
import fr.david.mdm.dbscan.DBScan;
import fr.david.mdm.distances.trajectory.DirectionDistanceTrajectoryAsSet;
import fr.david.mdm.distances.trajectory.DirectionSpaceTimeDistance;
import fr.david.mdm.distances.trajectory.TimeSimilarityTrajectoryAsSet;
import fr.david.mdm.models.TrajectoryAsSet;

public class DBSCANClustersFinder {

	/*Dbscan parameters*/
	private double eps = 0.004;
	private int minPoints = 20;	
	
	/*Distance Function weights*/
	private double w_theta = 0.5;
	private double w_lambda = 0.5;
	
	private DBScan<TrajectoryAsSet> clusterAlgorithm;
	private DirectionSpaceTimeDistance d; 
	
	public DBSCANClustersFinder(double eps, int minPoints, double w_theta, double w_lambda) {
		this.eps=eps;
		this.minPoints=minPoints;
		d = new DirectionSpaceTimeDistance(new DirectionDistanceTrajectoryAsSet(),
				new TimeSimilarityTrajectoryAsSet(),w_theta ,w_lambda);
	}
	
	public HashMap<TrajectoryAsSet, ClusterObjectDBScan<TrajectoryAsSet>> computeTimeWindowDBSCANClusters (int time_window, Vector<TrajectoryAsSet> objectList) 
			throws SQLException, IOException {
		
		/**
		 * Execucao do DBSCAN */
    	//long starttime,finaltime;
    	long starttime=System.currentTimeMillis();
    	this.clusterAlgorithm = new DBScan<TrajectoryAsSet>(d, objectList);
    	clusterAlgorithm.setTimeWindow(time_window);
		clusterAlgorithm.dbscan(eps,minPoints,"timewindow"+time_window+"-dbscanclusters.csv");
		long finaltime=System.currentTimeMillis();
		System.out.println("time to create clusters DBSCAN:"+(finaltime-starttime));
		System.out.println("Number of clusters "+clusterAlgorithm.numberClusters);
		
		return clusterAlgorithm.getCos();
	}
	
	
}
