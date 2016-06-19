package fr.david.mdm.distances.trajectory;

import fr.david.mdm.models.TrajectoryAsSet;

public class DirectionSpaceTimeDistance extends DistanceHelper<TrajectoryAsSet>{

	private DistanceHelper<TrajectoryAsSet> dm_theta;
	private DistanceHelper<TrajectoryAsSet> dm_lambda;
	private double w_theta;
	private double w_lambda;
	
	public DirectionSpaceTimeDistance(DistanceHelper<TrajectoryAsSet> dm_theta,DistanceHelper<TrajectoryAsSet> dm_lambda,
			double w_theta, double w_lambda) {
		this.w_lambda=w_lambda;
		this.w_theta=w_theta;
		this.dm_lambda=dm_lambda;
		this.dm_theta=dm_theta;
	}
	
	@Override
	public double distance(TrajectoryAsSet o1, TrajectoryAsSet o2) {
		return w_theta*dm_theta.distance(o1, o2) + w_lambda*dm_lambda.distance(o1, o2);
	}

	
}
