package fr.david.mdm.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.vividsolutions.jts.geom.Coordinate;

import fr.david.mdm.models.TrajectoryAsSet;

public class Hilbert_Segment_Data extends SegmentData{
	
	
	//maps the trajectories to their hilbert index
	public void trajectories2Index(Vector<TrajectoryAsSet> objects, double eps){
		TrajectoryAsSet trajectory;
		Hilbert_SegmentPoint segment = new Hilbert_SegmentPoint();
		Coordinate c;
		ArrayList<Point> grid_cells = new ArrayList<Point>();
		Point p;
		int hilbertIndex;
		HilbertConversion Hilbertconversion = new HilbertConversion();
		ArrayList<Point> densityObjects = new ArrayList<Point>();
		
		for (int i = 0; i < objects.size(); i++) {
			trajectory = objects.get(i);
						
			for (int j = 0; j < trajectory.getCoordArray().size(); j++) {
				c = trajectory.getCoordArray().get(j);			
				p = segment.set(trajectory.getTid(), c.x, c.y,  trajectory.getTsArrayElement(j),eps);
				grid_cells.add(p);				
			}		
		}
		
		this.n = (int) Math.pow(2, (int) Math.ceil( (Math.log(segment.maxIndex))/(Math.log(2)) ));
				
		for (Point point : grid_cells) {
			
			hilbertIndex = Hilbertconversion.xy2d(n, point);
			point.cid=hilbertIndex;
						
			if(IndexMap.containsKey(hilbertIndex)) {
				IndexMap.get(hilbertIndex).add(point);
			}
			else {
				densityObjects = new ArrayList<Point>();
				densityObjects.add(point);
				IndexMap.put(hilbertIndex, densityObjects);
			}			
		}		
	}
	
	public HashMap<Integer, ArrayList<Point>> getRepresentativeCandidates(int minPoints){
		HashMap<Integer, ArrayList<Point>> representativeCandidates = new HashMap<Integer, ArrayList<Point>>();
		
		Set<Integer> keys = IndexMap.keySet();	//all hilbert_indexes	
		for (Integer hilbertIndex : keys) {
			if(IndexMap.get(hilbertIndex).size()>=minPoints){
				
				//candidates_list receite all point in the dense cell
				for (Point p : IndexMap.get(hilbertIndex)) {					
					if(representativeCandidates.containsKey(p.tid)) {
						representativeCandidates.get(p.tid).add(p);
					}
					else {
						ArrayList<Point> densityObjects = new ArrayList<Point>();
						densityObjects.add(p);
						representativeCandidates.put(p.tid, densityObjects);
					}	
				}
				
				//adjacent_cells
				Set<Integer> cid_adjacents = getAllCidAdjacents(hilbertIndex);				
				for (Integer hilbertIndex_adjacent : cid_adjacents) {
					for (Point p : IndexMap.get(hilbertIndex_adjacent)) {
						if(representativeCandidates.containsKey(p.tid)) {
							representativeCandidates.get(p.tid).add(p);
						}
						else {
							ArrayList<Point> densityObjects = new ArrayList<Point>();
							densityObjects.add(p);
							representativeCandidates.put(p.tid, densityObjects);
						}
					}
				}
			}				
		}
	
		return representativeCandidates;
	}

	public void print(int minPoints){
		Set<Integer> keys = IndexMap.keySet();		
		for (Integer key : keys) {
			if(IndexMap.get(key).size()>=minPoints) System.out.println("HilbertIndex "+key+" has "+IndexMap.get(key).size());
		}
		
	}
	
	public Set<Integer> getAllCidAdjacents(int hilbertIndex){
		Set<Integer> cid_adjacents = new HashSet<Integer>();
		HilbertConversion hilbertConversion = new HilbertConversion();
		Point p_adj;
		int hilbertIndex_adjacent;
		
		//adjacent_cells
		Point p_hilbertIndex_adj = new Point(0,0);
		hilbertConversion.d2xy(this.n, hilbertIndex, p_hilbertIndex_adj);
		
		//adjacent below
		//y-1
		if(p_hilbertIndex_adj.y!=0){
			//x-1
			if(p_hilbertIndex_adj.x!=0){
				p_adj = new Point(p_hilbertIndex_adj.x-1, p_hilbertIndex_adj.y-1);
				hilbertIndex_adjacent = hilbertConversion.xy2d(this.n, p_adj);				
				if(IndexMap.containsKey(hilbertIndex_adjacent)) cid_adjacents.add(hilbertIndex_adjacent);
			}
			
			//x
			p_adj = new Point(p_hilbertIndex_adj.x, p_hilbertIndex_adj.y-1);
			hilbertIndex_adjacent = hilbertConversion.xy2d(this.n, p_adj);			
			if(IndexMap.containsKey(hilbertIndex_adjacent)) cid_adjacents.add(hilbertIndex_adjacent);

			//x+1
			p_adj = new Point(p_hilbertIndex_adj.x+1, p_hilbertIndex_adj.y-1);
			hilbertIndex_adjacent = hilbertConversion.xy2d(this.n, p_adj);			
			if(IndexMap.containsKey(hilbertIndex_adjacent)) cid_adjacents.add(hilbertIndex_adjacent);
		}
		
		//adjacent above
		//y+1
		
		//x-1
		if(p_hilbertIndex_adj.x!=0){
			p_adj = new Point(p_hilbertIndex_adj.x-1, p_hilbertIndex_adj.y+1);
			hilbertIndex_adjacent = hilbertConversion.xy2d(this.n, p_adj);			
			if(IndexMap.containsKey(hilbertIndex_adjacent)) cid_adjacents.add(hilbertIndex_adjacent);
		}
		//x
		p_adj = new Point(p_hilbertIndex_adj.x, p_hilbertIndex_adj.y+1);
		hilbertIndex_adjacent = hilbertConversion.xy2d(this.n, p_adj);			
		if(IndexMap.containsKey(hilbertIndex_adjacent)) cid_adjacents.add(hilbertIndex_adjacent);
				
		//x+1
		p_adj = new Point(p_hilbertIndex_adj.x+1, p_hilbertIndex_adj.y+1);
		hilbertIndex_adjacent = hilbertConversion.xy2d(this.n, p_adj);		
		if(IndexMap.containsKey(hilbertIndex_adjacent)) cid_adjacents.add(hilbertIndex_adjacent);
		
		//adjacent besides
		//y
		//x-1
		if(p_hilbertIndex_adj.x!=0){
			p_adj = new Point(p_hilbertIndex_adj.x-1, p_hilbertIndex_adj.y);
			hilbertIndex_adjacent = hilbertConversion.xy2d(this.n, p_adj);
			if(IndexMap.containsKey(hilbertIndex_adjacent)) cid_adjacents.add(hilbertIndex_adjacent);				
		}
		//x+1
		p_adj = new Point(p_hilbertIndex_adj.x+1, p_hilbertIndex_adj.y);
		hilbertIndex_adjacent = hilbertConversion.xy2d(this.n, p_adj);		
		if(IndexMap.containsKey(hilbertIndex_adjacent)) cid_adjacents.add(hilbertIndex_adjacent);
		
		
		return cid_adjacents;
	}
	
}
