package fr.david.mdm.microgroup.clustering;
import fr.david.mdm.dataset.DBManager;
import fr.david.mdm.distances.trajectory.DistanceHelper;
import fr.david.mdm.distances.trajectory.DistanceMeter;
import fr.david.mdm.models.Cluster;
import fr.david.mdm.models.MicroGroup;
import fr.david.mdm.models.TrajectoryAsSet;
import fr.david.mdm.tools.Point;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class SubTrajectoryClustering<T> {

	private DistanceMeter<T> dm;
	
	//TODO to hilbert curve
	private HashMap<Integer, ArrayList<Point>> objects_hilbert_curve;
	
	public SubTrajectoryClustering(DistanceHelper<T> dm) {
		this.dm=dm;
	}
	
	/***
	 * @param eps
	 * @param minPoints
	 * @param microgroup_hash : existing micro-groups with its radius
	 * @param datasetObjects : all trajectories
	 * @param db :  for clustering processing with index
	 * @param time_window_id 
	 * @return
	 */
	public ArrayList<Cluster<T>> clustering(double eps, int minPoints,HashMap<MicroGroup<T>,Double> microgroup_hash,Collection<T> datasetObjects, DBManager db, int time_window_id){		
		MicroGroup<T> mg;
		Cluster<T> c;
		ArrayList<Cluster<T>> cluster_array = new ArrayList<Cluster<T>>();
		ArrayList<MicroGroup<T>> microgroup_array = new ArrayList<MicroGroup<T>>(microgroup_hash.keySet());
		int idCluster = 0;
		boolean found=false;
		
		
		ArrayList<MicroGroup<T>> candidates = null;
		Collection<T> neighbors = null;
		
		while(!microgroup_array.isEmpty()){
			mg = microgroup_array.remove(0);
			c = new Cluster<T>(idCluster);
			cluster_array.add(c);
			c.addClusterMember(mg);
			mg.setColor(c.getColor().brighter());
			while((mg=unvisitedMG(c))!=null){
				mg.setVisitedCluster(true);
				candidates = mergeCandidate(mg, microgroup_array, eps,microgroup_hash);
							
				
				if(!candidates.isEmpty()){
					ArrayList<T> mg_members= mg.getAllMicroGroupObject();
					for (T t : mg_members) {
						
						//neighbors = dm.neighbors(t, datasetObjects, eps);
						
						// TODO to hilbert curve
						if(objects_hilbert_curve.containsKey(((TrajectoryAsSet) t).getTid())){
							try {
								neighbors = neighbors(db, objects_hilbert_curve.get(((TrajectoryAsSet) t).getTid()), t, eps, time_window_id, datasetObjects);
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
						else {
							neighbors = dm.neighbors(t, datasetObjects, eps);
						}						
						
						if(neighbors.size()>=minPoints){ 
							int i=0;							
							while(i<candidates.size()) {
								MicroGroup<T> candidate = candidates.get(i);
								ArrayList<T> candidate_members = candidate.getAllMicroGroupObject();
								for (T t2 : candidate_members) {
									found=false;
									if(dm.distance(t, t2)<=eps){
										
										//neighbors = dm.neighbors(t2, datasetObjects, eps);		
										
										// TODO to hilbert curve
										if(objects_hilbert_curve.containsKey(((TrajectoryAsSet) t2).getTid())){
											try {
												neighbors = neighbors(db, objects_hilbert_curve.get(((TrajectoryAsSet) t2).getTid()), t2, eps, time_window_id, datasetObjects);
											} catch (SQLException e) {
												e.printStackTrace();
											}
										}
										else {
											neighbors = dm.neighbors(t2, datasetObjects, eps);
										}
																				
										if(neighbors.size()>=minPoints) {
											candidates.remove(i);
											microgroup_array.remove(candidate);
											c.addClusterMember(candidate);
											candidate.setColor(c.getColor().brighter());
											found=true;
										}
									}
									if(found) break;
								}
								if(!found)i++;
							}//end while
						}//end if
						if(candidates.isEmpty()) break;
					}//end for
				}//end if
			}
			idCluster++;			
		}
				
		return cluster_array;		
	}

	private MicroGroup<T> unvisitedMG(Cluster<T> c) {
		Collection<MicroGroup<T>> mg_array = c.getClusterMembers();
		for (MicroGroup<T> t : mg_array) {
			if(t.isVisitedCluster()==false) return t;
		}			

		return null;
	}
	
	private ArrayList<MicroGroup<T>> mergeCandidate(MicroGroup<T> mg, ArrayList<MicroGroup<T>> microgroup_array,double eps,HashMap<MicroGroup<T>,Double> microgroup_hash){
		
		ArrayList<MicroGroup<T>> candidates = new ArrayList<MicroGroup<T>>();
		
		double distance;
		for (MicroGroup<T> candidate : microgroup_array) {
			distance = dm.distance(mg.getRepresentative(), candidate.getRepresentative());
			if(distance<=(microgroup_hash.get(mg)+microgroup_hash.get(candidate))){ //distance<=2*eps &&  
				candidates.add(candidate);
			}						
		}		
		return candidates;
		
	}
	
	//TODO to hilbert curve
	//search for neighbors using index
	private Collection<T> neighbors(DBManager db, ArrayList<Point> points_candidate, T candidate, 
			double eps, int time_window_id, Collection<T> datasetObjects) throws SQLException{
		
		ArrayList<T> neighbors = new ArrayList<T>();
		T candidate_neighboor;
		//id of neighboor objects (indicated by index)
		Collection<Integer> id_neighbors_result = db.queryIndex(points_candidate, time_window_id);
		for (Integer id_candidate_neighboor : id_neighbors_result) {
			if(id_candidate_neighboor!=((TrajectoryAsSet) candidate).getTid()){
				candidate_neighboor = getTObject(id_candidate_neighboor, datasetObjects);
				if(dm.distance(candidate, candidate_neighboor)<=eps) neighbors.add(candidate_neighboor);
			}
		}		
		return neighbors;		
	}
	
	//TODO to hilbert curve
	private T getTObject(Integer tid, Collection<T> objects) {
		for (T t : objects) {
			if(((TrajectoryAsSet) t).getTid()==tid) return  t;
		}
		return null;
	}

	
	public HashMap<Integer, ArrayList<Point>> getObjectsHilbert() {
		return objects_hilbert_curve;
	}

	public void setObjectsHilbert(HashMap<Integer, ArrayList<Point>> objects_hilbert_curve) {
		this.objects_hilbert_curve = objects_hilbert_curve;
	}
}
