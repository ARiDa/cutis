package fr.david.mdm.distances.trajectory;

import fr.david.mdm.models.TrajectoryAsSet;

public class TraclusDistance  {//extends DistanceHelper<TrajectoryAsSet> {

	//@Override
	public double distance(TrajectoryAsSet o1, TrajectoryAsSet o2) {
		
		double[] s1 = new double[2];
		s1[0] = o1.getCoordArray().get(0).x;
		s1[1] = o1.getCoordArray().get(0).y;
		
		double[] e1 = new double[2];
		e1[0] = o1.getCoordArray().get(o1.getCoordArray().size()-1).x;
		e1[1] = o1.getCoordArray().get(o1.getCoordArray().size()-1).y;
		
		
		double[] s2 = new double[2];
		s2[0] = o2.getCoordArray().get(0).x;
		s2[1] = o2.getCoordArray().get(0).y;
		
		double[] e2 = new double[2];
		e2[0] = o2.getCoordArray().get(o2.getCoordArray().size()-1).x;
		e2[1] = o2.getCoordArray().get(o2.getCoordArray().size()-1).y;
		
		
		return subComputeDistanceBetweenTwoLineSegments(s1,e1,s2,e2);
	}
	
	/*private double measurePerpendicularDistance(double[] s1,
			double[] e1, double[] s2,double[] e2) {

		// we assume that the first line segment is longer than the second one
		double distance1;	// the distance from a start point to the cluster component
		double distance2;	// the distance from an end point to the cluster component

		distance1 = measureDistanceFromPointToLineSegment(s1, e1, s2);
		distance2 = measureDistanceFromPointToLineSegment(s1, e1, e2);

		// if the first line segment is exactly the same as the second one, 
		// the perpendicular distance should be zero
		if (distance1 == 0.0 && distance2 == 0.0) return 0.0;

		// return (d1^2 + d2^2) / (d1 + d2) as the perpendicular distance
		return ((Math.pow(distance1, 2) + Math.pow(distance2, 2)) / (distance1 + distance2));

	}*/
	
	private double subComputeDistanceBetweenTwoLineSegments(double[] startPoint1,
			double[] endPoint1, double[] startPoint2, double[] endPoint2) {
		double perpendicularDistance;
		double parallelDistance;
		double angleDistance;
		double perDistance1, perDistance2;
		double parDistance1, parDistance2;
		double length1, length2;	
		double m_coefficient;
		//double[] m_projectionPoint = new double[startPoint1.length];

		// the length of the first line segment
		length1 = measureDistanceFromPointToPoint(startPoint1, endPoint1);
		// the length of the second line segment
		length2 = measureDistanceFromPointToPoint(startPoint2, endPoint2);

		// compute the perpendicular distance and the parallel distance
		// START ...
		if (length1 > length2)
		{
			perDistance1 = measureDistanceFromPointToLineSegment(startPoint1, endPoint1, startPoint2);
			m_coefficient = computeCoefficient(startPoint1, endPoint1, startPoint2);
			if (m_coefficient < 0.5) {
				parDistance1 = measureDistanceFromPointToPoint(startPoint1, projectionPoint(startPoint1, endPoint1, startPoint2));
			}
			else parDistance1 = measureDistanceFromPointToPoint(endPoint1, projectionPoint(startPoint1, endPoint1, startPoint2));

			perDistance2 = measureDistanceFromPointToLineSegment(startPoint1, endPoint1, endPoint2);
			m_coefficient = computeCoefficient(startPoint1, endPoint1, endPoint2);
			if (m_coefficient < 0.5) parDistance2 = measureDistanceFromPointToPoint(startPoint1, projectionPoint(startPoint1, endPoint1, endPoint2));
			else parDistance2 = measureDistanceFromPointToPoint(endPoint1, projectionPoint(startPoint1, endPoint1, endPoint2));
		} else {
			perDistance1 = measureDistanceFromPointToLineSegment(startPoint2, endPoint2, startPoint1);
			m_coefficient = computeCoefficient(startPoint2, endPoint2, startPoint1);
			if (m_coefficient < 0.5) parDistance1 = measureDistanceFromPointToPoint(startPoint2, projectionPoint(startPoint2, endPoint2, startPoint1));
			else parDistance1 = measureDistanceFromPointToPoint(endPoint2, projectionPoint(startPoint2, endPoint2, startPoint1));

			perDistance2 = measureDistanceFromPointToLineSegment(startPoint2, endPoint2, endPoint1);
			m_coefficient = computeCoefficient(startPoint2, endPoint2, endPoint1);
			if (m_coefficient < 0.5) parDistance2 = measureDistanceFromPointToPoint(startPoint2,  projectionPoint(startPoint2, endPoint2, endPoint1));
			else parDistance2 = measureDistanceFromPointToPoint(endPoint2, projectionPoint(startPoint2, endPoint2, endPoint1));			
			
		}

		// compute the perpendicular distance; take (d1^2 + d2^2) / (d1 + d2)
		if (!(perDistance1 == 0.0 && perDistance2 == 0.0)) 
			perpendicularDistance = ((Math.pow(perDistance1, 2) + Math.pow(perDistance2, 2)) / (perDistance1 + perDistance2));
		else
			perpendicularDistance = 0.0;

		// compute the parallel distance; take the minimum
		parallelDistance = (parDistance1 < parDistance2) ? parDistance1 : parDistance2;
		// ... END
		
		// compute the angle distance
		// START ...
		// MeasureAngleDisntance() assumes that the first line segment is longer than the second one
		if (length1 > length2)
			angleDistance = measureAngleDistance(startPoint1, endPoint1, startPoint2, endPoint2);
		else
			angleDistance = measureAngleDistance(startPoint2, endPoint2, startPoint1, endPoint1);
		// ... END

		return (0.5*perpendicularDistance + 0*parallelDistance + 0.5*angleDistance);
		
	}

	
	private double measureDistanceFromPointToLineSegment(double[] s,
			double[] e, double[] p) {

		double[] m_projectionPoint = projectionPoint(s,e,p);

		// return the distance between the projection point and the given point
		return measureDistanceFromPointToPoint(p, m_projectionPoint);

	}
	
	private double[] projectionPoint(double[] s,
			double[] e, double[] p){
		int nDimensions = p.length;
		double[] m_projectionPoint = new double[nDimensions];
		
		double[] m_vector2 = new double[nDimensions];
		
		for (int i = 0; i < nDimensions; i++)
		{
			m_vector2[i]= e[i] - s[i];
		}
		
		double m_coefficient = computeCoefficient(s,e,p);
		// the projection on the cluster component from a given point
		// NOTE: the variable m_projectionPoint is declared as a member variable

		for (int i = 0; i < nDimensions; i++)
			m_projectionPoint[i]= s[i] + m_coefficient * m_vector2[i]; //compute ps
		
		return m_projectionPoint;
	}
	
	
	
	private double computeCoefficient(double[] s,
			double[] e, double[] p) {
		// NOTE: the variables m_vector1 and m_vector2 are declared as member variables

		// construct two vectors as follows
		// 1. the vector connecting the start point of the cluster component and a given point
		// 2. the vector representing the cluster component
		int nDimensions = p.length;
		double[] m_vector1 = new double[nDimensions];
		double[] m_vector2 = new double[nDimensions];
		
		for (int i = 0; i < nDimensions; i++)
		{
			m_vector1[i]= p[i] - s[i];
			m_vector2[i]= e[i] - s[i];
		}

		// a coefficient (0 <= b <= 1)
		double m_coefficient = computeInnerProduct(m_vector1, m_vector2) / computeInnerProduct(m_vector2, m_vector2); //compute u1
		
		return m_coefficient;

	}

	private double measureDistanceFromPointToPoint(double[] point1, double[] point2) {
		
		int nDimensions = point1.length;
		double squareSum = 0.0;
		
		for(int i=0; i<nDimensions; i++) {
			squareSum += Math.pow((point2[i]
					- point1[i]), 2);
		}
		return Math.sqrt(squareSum);
		
	}
	
	private double computeInnerProduct(double[] vector1, double[] vector2) {
		int nDimensions = vector1.length;
		double innerProduct = 0.0;
		
		for(int i=0; i<nDimensions; i++) {
			innerProduct += (vector1[i] * vector2[i]);
		}
		
		return innerProduct;
	}
	private double measureAngleDistance(double[] s1,
			double[] e1, double[] s2, double[] e2) {
		
		int nDimensions = s1.length;
		double[] m_vector1 = new double[nDimensions];
		double[] m_vector2 = new double[nDimensions];
		
		// NOTE: the variables m_vector1 and m_vector2 are declared as member variables

		// construct two vectors representing the cluster component and a line segment, respectively
		for (int i = 0; i < nDimensions; i++) {
			
			m_vector1[i]= e1[i]-s1[i];
			m_vector2[i]= e2[i]-s2[i];			
		}
		
		// we assume that the first line segment is longer than the second one
		// i.e., vectorLength1 >= vectorLength2
		double vectorLength1 = computeVectorLength(m_vector1);
		double vectorLength2 = computeVectorLength(m_vector2);

		// if one of two vectors is a point, the angle distance becomes zero
		if (vectorLength1 == 0.0 || vectorLength2 == 0.0) return 0.0;
		
		// compute the inner product of the two vectors
		double innerProduct = computeInnerProduct(m_vector1, m_vector2);

		// compute the angle between two vectors by using the inner product
		double cosTheta = innerProduct / (vectorLength1 * vectorLength2);
		// compensate the computation error (e.g., 1.00001)
		// cos(theta) should be in the range [-1.0, 1.0]
		// START ...
		if (cosTheta > 1.0) cosTheta = 1.0; 
		if (cosTheta < -1.0) cosTheta = -1.0;
		// ... END
		
		//TODO by me
		//double sinTheta = Math.sqrt(1 - Math.pow(cosTheta, 2));
		//return (vectorLength2 * sinTheta);	
		double theta = Math.acos(cosTheta);
		if(0<=theta && theta<(Math.PI/2)) return vectorLength2*Math.sin(theta);
		if((Math.PI/2)<=theta && theta<=Math.PI) return vectorLength2;
		return Double.NaN;
		
		
		// if 90 <= theta <= 270, the angle distance becomes the length of the line segment
		// if (cosTheta < -1.0) sinTheta = 1.0;

						
	}
	
	private double computeVectorLength(double[] vector) {
		
		int nDimensions = vector.length;
		double squareSum = 0.0;
		
		for(int i=0; i<nDimensions; i++) {
			squareSum += Math.pow(vector[i], 2);
		}
		
		return Math.sqrt(squareSum);		
	}
	

}
