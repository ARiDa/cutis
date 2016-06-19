package fr.david.mdm.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import fr.david.mdm.models.TrajectoryAsSet;

public abstract class SegmentData {

	public int n;
	
	//HashMap<Integer, ArrayList<Integer>> hilbertMap;
	public HashMap<Integer, ArrayList<Point>> IndexMap;
	
	public SegmentData() {
		this.IndexMap = new HashMap<Integer, ArrayList<Point>>();
	}
	
	public abstract void trajectories2Index(Vector<TrajectoryAsSet> objects, double eps);
	public abstract HashMap<Integer, ArrayList<Point>> getRepresentativeCandidates(int minPoints);
	public abstract void print(int minPoints);
	public abstract Set<Integer> getAllCidAdjacents(int hilbertIndex);

}
