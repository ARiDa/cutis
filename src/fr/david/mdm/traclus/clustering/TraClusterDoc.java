package fr.david.mdm.traclus.clustering;

import fr.david.mdm.models.TrajectoryAsSet;
import fr.david.mdm.traclus.clustering.ClusterGen.LineSegmentCluster;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.Vector;

public class TraClusterDoc {
	
	public int m_nDimensions;
	public int m_nTrajectories;
	public int m_nClusters;
	public double m_clusterRatio;
	public int m_maxNPoints;
	public ArrayList<Trajectory> m_trajectoryList;
	public ArrayList<Cluster> m_clusterList;
	private DirectionSpaceTimeDistanceTraclus dm;
	
	public TraClusterDoc(DirectionSpaceTimeDistanceTraclus dm) {
			
		m_nTrajectories = 0;
		m_nClusters = 0;
		m_clusterRatio = 0.0;	
		m_trajectoryList = new ArrayList<Trajectory>();
		m_clusterList = new ArrayList<Cluster>();
		this.dm=dm;
	}
	
	public class Parameter {
		double epsParam;
		int minLnsParam;
	}
	
	boolean onOpenDocument(String inputFileName) {
		
		int nDimensions = 2;		// default dimension = 2
		int nTrajectories = 0;
		int nTotalPoints = 0;		//no use
		int trajectoryId;
		int nPoints;
		double value;
		long time;
		

		DataInputStream in;
		BufferedReader inBuffer = null;
		try {
			in = new DataInputStream(new BufferedInputStream(   
			        new FileInputStream(inputFileName)));
			
			
			
			inBuffer = new BufferedReader(   
	                new InputStreamReader(in));
			
			
			nDimensions = Integer.parseInt(inBuffer.readLine());			// the number of dimensions
			m_nDimensions = nDimensions;
			nTrajectories = Integer.parseInt(inBuffer.readLine());		// the number of trajectories
			m_nTrajectories = nTrajectories;
			
			m_maxNPoints = -1;		// initialize for comparison
			
			// the trajectory Id, the number of points, the coordinate of a point ...
			for(int i=0; i<nTrajectories; i++) {				
	
				String str = inBuffer.readLine();
				
				
				Scanner sc = new Scanner(str); 
				sc.useLocale(Locale.ENGLISH);
				
				trajectoryId = sc.nextInt();		//trajectoryID
				nPoints = sc.nextInt();				//nubmer of points in the trajectory
				
				if(nPoints > m_maxNPoints) m_maxNPoints = nPoints;
				nTotalPoints += nPoints;
				
				Trajectory pTrajectoryItem = new Trajectory(trajectoryId, nDimensions);
				
				
				for(int j=0; j<nPoints; j++) {
					
					CMDPoint point = new CMDPoint(nDimensions);   // initialize the CMDPoint class for each point
					
					for(int k =0; k< nDimensions; k++) {
						value = sc.nextDouble();						
						point.setM_coordinate(k, value);
						
					}
					time = new Long(sc.nextLong());
					point.setTime(time);
					
					pTrajectoryItem.addPointToArray(point);				
				}
				
				m_trajectoryList.add(pTrajectoryItem);
				
//				for(int m=0; m<pTrajectoryItem.getM_pointArray().size();m++) {
//					System.out.print(pTrajectoryItem.getM_pointArray().get(m).getM_coordinate(0)+" ");
//				}
//				System.out.println();
				
			}			
			
//			System.out.println(m_nDimensions+"haha"+m_nTrajectories);
//			System.out.println(inBuffer.readLine());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Unable to open input file");
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}  finally {
			try {
				inBuffer.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        		
		return true;
	}
	
	
	public boolean onOpenDocument(Vector<TrajectoryAsSet> objects) {
		
		int nDimensions = 2;		// default dimension = 2
		int nTrajectories = 0;
		int trajectoryId;
		int nPoints;
		double value;
		long time;

		m_nDimensions = nDimensions;
		nTrajectories = objects.size();		// the number of trajectories
		m_nTrajectories = nTrajectories;
		
		m_maxNPoints = -1;		// initialize for comparison
		
		// the trajectory Id, the number of points, the coordinate of a point ...
		for(int i=0; i<nTrajectories; i++) {
			
			TrajectoryAsSet t = objects.get(i);
			
			trajectoryId = t.getTid();		//trajectoryID
			nPoints = t.getCoordArray().size();				//nubmer of points in the trajectory
			
			if(nPoints > m_maxNPoints) m_maxNPoints = nPoints;
			
			Trajectory pTrajectoryItem = new Trajectory(trajectoryId, nDimensions);				
			
			for(int j=0; j<nPoints; j++) {					
				CMDPoint point = new CMDPoint(nDimensions);   // initialize the CMDPoint class for each point
				
				value = t.getCoordArray().get(j).x;						
				point.setM_coordinate(0, value);
				value = t.getCoordArray().get(j).y;						
				point.setM_coordinate(1, value);
				
				time = t.getTsArray().get(j);
				point.setTime(time);
				
				pTrajectoryItem.addPointToArray(point);				
			}
			
			m_trajectoryList.add(pTrajectoryItem);
			
//				for(int m=0; m<pTrajectoryItem.getM_pointArray().size();m++) {
//					System.out.print(pTrajectoryItem.getM_pointArray().get(m).getM_coordinate(0)+" ");
//				}
//				System.out.println();
			
		}			
		
//			System.out.println(m_nDimensions+"haha"+m_nTrajectories);
//			System.out.println(inBuffer.readLine());
		
        		
		return true;
	}
	
	public boolean onClusterGenerate(String clusterFileName, double epsParam, int minLnsParam, int resultid) {
//////////////////////////////////////////////////still to be written
		Long startTime = System.currentTimeMillis();
		ClusterGen generator = new ClusterGen(this,dm);
		
		if(m_nTrajectories ==0) {
			System.out.println("Load a trajectory data set first");
		}
		else System.out.println("Load a trajectory data set succesfull");
		// FIRST STEP: Trajectory Partitioning
		if (!generator.partitionTrajectory())
		{
			System.out.println("Unable to partition a trajectory\n");
			return false;
		}
		else System.out.println("Partitioned trajectory data set succesfull");
		// SECOND STEP: Density-based Clustering
		if (!generator.performDBSCAN(epsParam, minLnsParam))
		{
			System.out.println("Unable to perform the DBSCAN algorithm\n");
			return false;
		}
		else System.out.println("Performed DBSCAN succesfull");
		// THIRD STEP: Cluster Construction
		if (!generator.constructCluster())
		{
			System.out.println( "Unable to construct a cluster\n");
			return false;
		}
		Long endTime = System.currentTimeMillis();
		System.out.println("Total time Traclus: "+(endTime-startTime));

		
	/*	for(int i=0; i<m_clusterList.size(); i++) {
			//m_clusterList.
			System.out.println(m_clusterList.get(i).getM_clusterId());
			for(int j=0; j<m_clusterList.get(i).getM_PointArray().size(); j++) {
				double x = m_clusterList.get(i).getM_PointArray().get(j).getM_coordinate(0);
				double y = m_clusterList.get(i).getM_PointArray().get(j).getM_coordinate(1);
				System.out.print(x +" "+ y +", ");
			}
			System.out.println();
		}*/
		FileOutputStream fos = null;
		BufferedWriter bw = null;
		OutputStreamWriter osw = null;
		try {
			fos = new FileOutputStream(clusterFileName);
			osw = new OutputStreamWriter(fos);
			bw = new BufferedWriter(osw);
			
			bw.write("epsParam:"+epsParam +"   minLnsParam:"+minLnsParam);
			
		/*	for(int i=0; i<m_clusterList.size(); i++) {
				//m_clusterList.
				bw.write("\nclusterID: "+ m_clusterList.get(i).getM_clusterId()+"  Points Number:  "+m_clusterList.get(i).getM_PointArray().size()+"\n");
				for(int j=0; j<m_clusterList.get(i).getM_PointArray().size(); j++) {
					
					double x = m_clusterList.get(i).getM_PointArray().get(j).getM_coordinate(0);
					double y = m_clusterList.get(i).getM_PointArray().get(j).getM_coordinate(1);
					bw.write(x+" "+y+"   ");
					
				}
				System.out.println();
			}*/
			
			//para os clusters
			System.out.println("------------------------");
			for (Cluster cluster : m_clusterList) {
				//bw.write(cluster.getM_clusterId());
				System.out.println(cluster.getM_clusterId()+" tem "+generator.m_lineSegmentClusters[cluster.getM_clusterId()].nTrajectories);
				for (int i=0;i<generator.m_lineSegmentPointArray.size();i++) {
					if(generator.m_componentIdArray.get(i)==cluster.getM_clusterId()){
						CMDLineSegment linesegm =generator.m_lineSegmentPointArray.get(i);
						
						for (int j = 0; j < linesegm.m_linesegment.length-1; i++) {
							String linestring = "'Linestring(";
							for (int k = 0; k < generator.m_document.m_nDimensions; k++) {
								j=k+j;
								double x = linesegm.m_linesegment[j].getM_coordinate(0);
								double y = linesegm.m_linesegment[j].getM_coordinate(1);
								linestring = linestring.concat(x +" "+ y +", ");
							}
							linestring = linestring.concat(")'");
							linestring = linestring.replace(", )", ")");
							//System.out.println("insert into representativeness_result (resultid, line) values ("+
							//cluster.getM_clusterId()+","+linestring+")");
							bw.write("\ninsert into traclus_result (resultid, clusterid, line) values ("+
									resultid+","+cluster.getM_clusterId()+","+linestring+");"+"\n");
							
						}						
					}
					
				}
			}
			
			/*for (Cluster cluster : m_clusterList) {
				System.out.println(cluster.getM_clusterId()+" tem "+generator.m_lineSegmentClusters[cluster.getM_clusterId()].nTrajectories);
				for (Cluster cluster2 : m_clusterList) {
					int count=0;
					if(cluster.getM_clusterId()!=cluster2.getM_clusterId()){
						for (Integer traj1 : generator.m_lineSegmentClusters[cluster.getM_clusterId()].trajectoryIdList) {
							for (Integer traj2 : generator.m_lineSegmentClusters[cluster2.getM_clusterId()].trajectoryIdList) {
								if(traj1==traj2) count++;
							}
						}
					}
					System.out.println("Para o cluster "+cluster.getM_clusterId()+" tem "+count+" iguais a "+cluster2.getM_clusterId());
				}
				
			}*/
			
			//representatives
			for (Cluster cluster : m_clusterList) {
				//bw.write(cluster.getM_clusterId());
				LineSegmentCluster lineSegCluster = generator.m_lineSegmentClusters[cluster.getM_clusterId()];
				String linestring = "'Linestring(";
				boolean isrepresentative=true;
				for (int i=0;i<lineSegCluster.clusterPointArray.size();i++) {
						CMDPoint linesegm =lineSegCluster.clusterPointArray.get(i);
						double x = linesegm.getM_coordinate(0);
						double y = linesegm.getM_coordinate(1);
						linestring = linestring.concat(x +" "+ y +", ");
				}
						linestring = linestring.concat(")'");
						linestring = linestring.replace(", )", ")");
						bw.write("\ninsert into traclus_result (resultid, line, isrepresentative) values ("+
								resultid+","+linestring+","+isrepresentative+");"+"\n");
							
					
				}
			
			/*for (Trajectory trajectory : generator.m_document.m_trajectoryList) {
				String linestring = "'Linestring(";
				for (int i = 0; i < trajectory.getM_partitionPointArray().size(); i++) {
					double x = trajectory.getM_partitionPointArray().get(i).getM_coordinate(0);
					double y = trajectory.getM_partitionPointArray().get(i).getM_coordinate(1);
					linestring = linestring.concat(x +" "+ y +", ");
				}
				linestring = linestring.concat(")'");
				linestring = linestring.replace(", )", ")");
				bw.write("\ninsert into traclus_result (resultid, line) values ("+
						51+","+linestring+");"+"\n");
			}*/
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

		
		return true;		
	}

	
	
/*	Parameter onEstimateParameter()
	{
		Parameter p = new Parameter();
		
		ClusterGen generator = new ClusterGen(this,dm);

		if (!generator.partitionTrajectory())
		{
			System.out.println("Unable to partition a trajectory\n");
			return null;
		}

		//if (!generator.estimateParameterValue(epsParam, minLnsParam))
		if (!generator.estimateParameterValue(p))
		{
			System.out.println("Unable to calculate the entropy\n");
			return null;
		}
		

		return p;
	}*/

	

}
