package fr.david.mdm.traclus.clustering;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

import fr.david.mdm.distances.trajectory.DirectionDistanceTrajectoryAsSet;
import fr.david.mdm.distances.trajectory.TimeSimilarityTrajectoryAsSet;
import fr.david.mdm.models.TrajectoryAsSet;


public class DirectionSpaceTimeDistanceTraclus {
	public TimeSimilarityTrajectoryAsSet distanceTimeSpace;
	public DirectionDistanceTrajectoryAsSet distanceDirection;
	
	public DirectionSpaceTimeDistanceTraclus(){
		distanceTimeSpace = new TimeSimilarityTrajectoryAsSet(); 
		distanceDirection = new DirectionDistanceTrajectoryAsSet();
	}
	
	public double distanceSpatialTime(CMDPoint s1,
			CMDPoint e1, CMDPoint s2,CMDPoint e2) {		
		
		
		ArrayList<Long> time_array = new ArrayList<Long>();
		ArrayList<Coordinate> coord_array = new ArrayList<Coordinate>();
		time_array.add(s1.m_time);
		time_array.add(e1.m_time);
		
		coord_array.add(new Coordinate(s1.m_coordinate[0], s1.m_coordinate[1]));
		coord_array.add(new Coordinate(e1.m_coordinate[0], e1.m_coordinate[1]));

		TrajectoryAsSet t1 = new TrajectoryAsSet(1,1, time_array,coord_array);
		
		time_array = new ArrayList<Long>();
		coord_array = new ArrayList<Coordinate>();
		time_array.add(s2.m_time);
		time_array.add(e2.m_time);
		
		coord_array.add(new Coordinate(s2.m_coordinate[0], s2.m_coordinate[1]));
		coord_array.add(new Coordinate(e2.m_coordinate[0], e2.m_coordinate[1]));
		TrajectoryAsSet t2 = new TrajectoryAsSet(2,2, time_array,coord_array);
		
		return distanceTimeSpace.distance(t1,t2);
	}

	
	public double distanceAngular(CMDPoint s1,
			CMDPoint e1, CMDPoint s2,CMDPoint e2) {
		ArrayList<Long> time_array = new ArrayList<Long>();
		ArrayList<Coordinate> coord_array = new ArrayList<Coordinate>();
		time_array.add(s1.m_time);
		time_array.add(e1.m_time);
		coord_array.add(new Coordinate(s1.m_coordinate[0], s1.m_coordinate[1]));
		coord_array.add(new Coordinate(e1.m_coordinate[0], e1.m_coordinate[1]));

		TrajectoryAsSet t1 = new TrajectoryAsSet(1,1, time_array,coord_array);
		
		time_array = new ArrayList<Long>();
		coord_array = new ArrayList<Coordinate>();
		time_array.add(s2.m_time);
		time_array.add(e2.m_time);
		coord_array.add(new Coordinate(s2.m_coordinate[0], s2.m_coordinate[1]));
		coord_array.add(new Coordinate(e2.m_coordinate[0], e2.m_coordinate[1]));
		TrajectoryAsSet t2 = new TrajectoryAsSet(2,2, time_array,coord_array);
		return distanceDirection.distanceWithInterpolation(t1, t2);
	}

	public double distance(CMDPoint s1,
			CMDPoint e1, CMDPoint s2,CMDPoint e2) {
		return 0.5*distanceSpatialTime(s1, e1, s2, e2)+0.5*distanceAngular(s1, e1, s2, e2);
	}


	public double length(CMDPoint point1, CMDPoint point2) {
		int nDimensions = point1.getM_nDimensions();
		double squareSum = 0.0;
		
		for(int i=0; i<nDimensions; i++) {
			squareSum += Math.pow((point2.getM_coordinate(i)
					- point1.getM_coordinate(i)), 2);
		}
		return Math.sqrt(squareSum);
	}

}
