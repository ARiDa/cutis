package fr.david.mdm.microgroup;

import fr.david.mdm.Enum.Evolution;
import fr.david.mdm.Enum.State;
import fr.david.mdm.dataset.DBManager;
import fr.david.mdm.distances.trajectory.DistanceHelper;
import fr.david.mdm.distances.trajectory.DistanceMeter;
import fr.david.mdm.models.MicroGroup;
import fr.david.mdm.models.TrajectoryAsSet;
import fr.david.mdm.tools.Point;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;



public class MaintainMicroGroup<T> {

	private DistanceMeter<T> dm;
	private Collection<T> outliers;
	private HashMap<T, MicroGroupObject<T>> subTrajecTimeWindow = new HashMap<T, MicroGroupObject<T>>();
	private HashMap<Integer, MicroGroup<T>> microGroupHash = new HashMap<Integer, MicroGroup<T>>();
	private int idGroup;
	private HashMap<Integer, ArrayList<Point>> candidates;
	private int time_window_id;
	private DBManager dbmanager;
	
	private int count_split;
	private int count_dissapear;
	private int count_appear;
	private int count_survive;
		
	public MaintainMicroGroup(DistanceHelper<T> dm) {
		this.dm=dm;
		microGroupHash = new HashMap<Integer, MicroGroup<T>>();
		idGroup = 0;		
	}
	
	//called before processing a time window
	public void setUp(Collection<T> objects){
		MicroGroupObject<T> mgo;
		for (T t : objects) {
			mgo = new MicroGroupObject<T>(t);
			subTrajecTimeWindow.put(t, mgo);
		}
		outliers = new ArrayList<T>();
		count_dissapear = 0;
		count_split=0;
		count_appear = 0;
		count_survive = 0;
	}
	
	public void incrementalSetUpSystem(Collection<T> newObjects, Collection<T> updateobjects, Collection<T> oldobjects){
		deleteObjectsFromSystem(oldobjects);
		updateTrajectory(updateobjects);
		setUp(newObjects);
	}
	
	//only for unvisited objects
	//to hilbert index is useful for MicroGroup Maintenance with Index implementation
	public void initializeMicroGroup(Collection<T> objects, double eps, int minPoints, double rho, double sigma) throws SQLException{
			
		T t;
		double vote=0, newVote;
		int groupSize;
		MicroGroup<T> g;
		
		
		//while((t = getUnvisitedPoint(objects,eps, minPoints)) != null){	
		//TODO to hilbert curve
		while((t = getUnvisitedPoint(eps, minPoints,objects)) != null){
			
			MicroGroupObject<T> current = subTrajecTimeWindow.get(t);
			groupSize=0;
			
			
			//Collection<T> neighbors = dm.neighbors(t,subTrajecTimeWindow.keySet(),eps);
			//TODO to hilbert curve
			Collection<T> neighbors = neighbors(candidates.get(((TrajectoryAsSet) t).getTid()),t,eps);
			
			//TODO to hilbert curve
			removeInCandidates(((TrajectoryAsSet) t).getTid());
			
			if(neighbors.size() >= minPoints){		
			
				current.setProcessed(State.VISITED);
				current.setIdMicroGroup(idGroup);
				current.setReachabilityDistance(0); //current is the representative
					
				//new microgroup is initiated
				count_appear++;
				g = new MicroGroup<T>(idGroup);
				g.addMicroGroupObject(t);
				g.setRepresentative(t);
				objects.remove(t);
										
				
				for (T t2 : neighbors) {
					MicroGroupObject<T> next = subTrajecTimeWindow.get(t2);
					MicroGroup<T> microGroupNext = microGroupHash.get(next.getIdMicroGroup());
						
					if(microGroupNext!=null && microGroupNext.getRepresentative()!=null && microGroupNext.getRepresentative().equals(t2));//the trajectory is representative in another microgroup
					else{
						if(next.getProcessed()!=State.VISITED){								
							groupSize++;
							next.setProcessed(State.VISITED);
						
							if(next.getIdMicroGroup()!=-1){
								microGroupNext.removeMicroGroupObject(t2);;
							}
							next.setReachabilityDistance(dm.distance(t, t2));
							next.setIdMicroGroup(idGroup);
							g.addMicroGroupObject(t2);
							objects.remove(t2); 
							
							//TODO to hilbert curve
							removeInCandidates(((TrajectoryAsSet) t2).getTid()); 
														
						}
					}					
						
				}					
					
				Collection<T> members = new Vector<T>(); 
				members.addAll(objects);
				for (T key : members) {
					MicroGroupObject<T> next = subTrajecTimeWindow.get(key);
					if(next.processed!=State.VISITED){
						newVote = voting(t, key, sigma);
						if(newVote>=rho){
							next.setProcessed(State.VISITED);
							next.setIdMicroGroup(idGroup);
							next.setReachabilityDistance(dm.distance(t, key)); 
							g.addMicroGroupObject(key);
							objects.remove(key);
							vote+=newVote;
							groupSize++;
							
							//TODO to hilbert curve
							removeInCandidates(((TrajectoryAsSet) key).getTid());			
						}	
					}						
				}
					
				g.setVote(vote);
				g.setSize(groupSize);
				microGroupHash.put(idGroup,g);
				idGroup=idGroup+1;
			}
			else{
				current.setProcessed(State.UNGROUPED);
				current.setIdMicroGroup(-1);
			}
		}		
	}
	
	
	//incremental maintenance of all micro-groups
	//to hilbert index is useful for MicroGroup Maintenance with Index implementation
	public void incrementalMaintenanceSystem(double eps, int minPoints, double rho, double sigma) throws SQLException{
		
		Collection<MicroGroup<T>> mg_array =  new Vector<MicroGroup<T>>();
		mg_array.addAll(microGroupHash.values());		
		
		for (MicroGroup<T> mg : mg_array) {
			mg.setVisitedCluster(false);
			if(mg.getRepresentative()==null){
				if(electAnotherRepresentative(mg,eps,minPoints)){
					incrementalMaintenanceMicroGroup(mg,eps,minPoints,rho,sigma); 
				}
			}
			else incrementalMaintenanceMicroGroup(mg,eps,minPoints,rho,sigma);
			
		}
		
		//for the outliers
		Vector<MicroGroupObject<T>> mgo_array =  new Vector<MicroGroupObject<T>>();
		for (MicroGroupObject<T> microGroupObject : subTrajecTimeWindow.values()) {
			if(microGroupObject.processed!=State.VISITED) mgo_array.add(microGroupObject);
		}

		for (MicroGroup<T> mg : microGroupHash.values()) {
			reachedByvoting(mg,mgo_array, rho, sigma);			
		}

		//set of trajectories which could not be reached by any existing micro-group
		for (MicroGroupObject<T> mgo : mgo_array) {
			outliers.add(mgo.originalObject);
		}
		
		idGroup=idGroup+1;
		initializeMicroGroup(outliers, eps, minPoints, rho, sigma);
	}
	
	
	//set trajectories to any existing micro-group according to the voting
	private void reachedByvoting(MicroGroup<T> mg, Vector<MicroGroupObject<T>> mgo_array,double rho, double sigma) {
		int i=0;
		double distance, voting;
		T representative = mg.getRepresentative();
		MicroGroupObject<T> movingobject;
		while(i<mgo_array.size()){
			movingobject = mgo_array.get(i);
			distance = dm.distance(representative, movingobject.originalObject);
			voting = (1/(sigma*Math.sqrt(2*Math.PI)))*Math.exp( ((-1)*Math.pow(distance,2))/(2*Math.pow(sigma, 2)) );
			if(voting>=rho) {
				mgo_array.remove(i);
				movingobject.setProcessed(State.VISITED);
				movingobject.setIdMicroGroup(mg.getIdMicroGroup());
				movingobject.setReachabilityDistance(distance); 
				mg.addMicroGroupObject(movingobject.originalObject);
				mg.setSize((mg.getSize()+1));
				
				//TODO to hilbert curve
				removeInCandidates(((TrajectoryAsSet) movingobject.originalObject).getTid()); 				
			}
			else i++;
		}
		
	}
	
	//incremental maintenance of one micro-group
	private void incrementalMaintenanceMicroGroup(MicroGroup<T> g, double eps, int minPoints, double rho, double sigma) throws SQLException{
			
		T representative = g.getRepresentative();
		
		//Collection<T> neighbors = dm.neighbors(representative,subTrajecTimeWindow.keySet(),eps);		
		//TODO to hilbert curve
		Collection<T> neighbors;
		if(candidates.containsKey(((TrajectoryAsSet) representative).getTid())){
			neighbors = neighbors(candidates.get(((TrajectoryAsSet) representative).getTid()),representative,eps);
		}
		else {
			neighbors = dm.neighbors(representative,subTrajecTimeWindow.keySet(),eps);
		}

		double vote=0, newVote;
		MicroGroupObject<T> mgo_representative, mgo_member;
		int groupSize=0;
		int groupId;
		
		//TODO to hilbert curve
		removeInCandidates(((TrajectoryAsSet) representative).getTid());
		
		if((neighbors.size()) >= minPoints){
			
				/**
				 * Micro group survives
				 */
				count_survive++;
				g.setEvolution(Evolution.SURVIVES);
				mgo_representative = subTrajecTimeWindow.get(representative);
				mgo_representative.setProcessed(State.VISITED);
				mgo_representative.setReachabilityDistance(0);
				
				for (T next : neighbors) {
					mgo_member = subTrajecTimeWindow.get(next);
					groupId=mgo_member.idMicroGroup;
					
					if(groupId!=-1 && microGroupHash.get(groupId).getRepresentative()!=null && microGroupHash.get(groupId).getRepresentative().equals(next)){ }//it is already representative in another micro-group
					else{
						if(mgo_member.getProcessed()!=State.VISITED){
							
							//TODO to hilbert curve
							removeInCandidates(((TrajectoryAsSet) next).getTid());
							
							groupSize++;
							mgo_member.setProcessed(State.VISITED);
							mgo_member.setReachabilityDistance(dm.distance(representative, next)); //depois retirar pois ja foi computada TODO
							if(groupId!=g.getIdMicroGroup())
							{
								if(groupId!=-1){
									microGroupHash.get(groupId).removeMicroGroupObject(next);
								}
								mgo_member.setIdMicroGroup(g.getIdMicroGroup());
								g.addMicroGroupObject(next);
							}						
						}
					}									
				}
				
				Collection<T> members = new Vector<T>(g.getAllMicroGroupObject());
				for (T next : members) {
					
					mgo_member = subTrajecTimeWindow.get(next);
										
					if(mgo_member.processed!=State.VISITED){
						newVote = voting(representative,next, sigma);
						if(newVote>=rho){
							
							//TODO to hilbert curve
							removeInCandidates(((TrajectoryAsSet) next).getTid());
							
							mgo_member.setProcessed(State.VISITED);
							vote+=newVote;
							groupSize++;
							mgo_member.setReachabilityDistance(dm.distance(representative,next));
						}
						else{
							g.removeMicroGroupObject(next);
							mgo_member.idMicroGroup=-1;
						}
					}
				}			
				
				g.setVote(vote);
				g.setSize(groupSize);
				
			}
			else{
				/**
				 * Micro group splits 
				 */
				count_split++;
				microGroupHash.remove(g.getIdMicroGroup());
				g.setEvolution(Evolution.SPLITS);
				Collection<T> members = new Vector<T>();
				members.addAll(g.getAllMicroGroupObject());
				for (T next : members) {
					mgo_member = subTrajecTimeWindow.get(next);
					mgo_member.idMicroGroup=-1;
				}
				idGroup=idGroup+1;
			}
		
	}	

	//gaussian function for a set and one element
	private double voting(T current, Collection<T> neighbors, double sigma){
		double vote=0;
		int count=0;
		MicroGroupObject<T> mgo;
		double distance;
		for (T t : neighbors) {
			mgo = subTrajecTimeWindow.get(t);
			
			if(mgo.processed!=State.VISITED){
				distance = dm.distance(current, t);
				vote+=(1/(sigma*Math.sqrt(2*Math.PI)))*Math.exp( ((-1)*Math.pow(distance,2))/(2*Math.pow(sigma, 2)) );
				count++;
			}
		}
		return vote/(count);
	}
	
	//gaussian function for 2 elements
	private double voting(T current, T neighbor, double sigma){
		double distance = dm.distance(current, neighbor);
		return (1/(sigma*Math.sqrt(2*Math.PI)))*Math.exp( ((-1)*Math.pow(distance,2))/(2*Math.pow(sigma, 2)) );		
	}
		
		
	//choose a new representative trajectory without using index
	private T getUnvisitedPoint(Collection<T> members, double eps, int minPoints) {		
		ArrayList<T> objs = new ArrayList<T>();
		for (T t : members) {
			if(subTrajecTimeWindow.get(t).getProcessed()==State.UNVISITED) objs.add(t);
		}
		
		if(objs.isEmpty()) return null;	
		T[] array = (T[]) objs.toArray();
		int rnd = (int) (Math.random()*objs.size());
		return array[(rnd+objs.size())%objs.size()];
	}
	
	//choose a new representative trajectory by using the index
	private T getUnvisitedPoint(double eps, int minPoints,Collection<T> members) {
		
		if(candidates.isEmpty()) return null;	
		int size = candidates.size();
		int rnd = (int) (Math.random()*size);
		
		int tid = (Integer) candidates.keySet().toArray()[(rnd+size)%size];				
		return getTObjectAndRemove(tid,members,eps, minPoints);
	}
	
	private T getTObjectAndRemove(int tid, Collection<T> members,double eps, int minPoints) {
		for (T t : members) {
			if(((TrajectoryAsSet) t).getTid()==tid) {
				if (subTrajecTimeWindow.get(t).getProcessed()==State.UNVISITED) return t;
				else {
					candidates.remove(tid);
					return getUnvisitedPoint(eps, minPoints,members);
				}
			}
		}
		
		return null;
	}
	
	public void discoverOutliers(){
		outliers = new Vector<T>();
		for (MicroGroupObject<T> mgo : subTrajecTimeWindow.values()) {
			if(mgo.processed!=State.VISITED){
				outliers.add(mgo.originalObject);
			}
		}
	}

	/**
	 * DELETE AND UPDATE OBJECTS AT EACH TIME WINDOW 
	 * */
	
	//delete moving object trajectories from the system
	private void deleteObjectsFromSystem(Collection<T> oldobjects){
		MicroGroupObject<T> mgo;
		MicroGroup<T> g;
		int count=0;
		
		for (T t : oldobjects) {
			mgo=remove(t);
			if(mgo!=null)
			{
				g = microGroupHash.get(mgo.idMicroGroup);
				//it can be outlier when g==null
				if(g!=null){
					g.removeMicroGroupObject(t);
					//check if the deleted moving object is a representative
					if(g.getRepresentative()!=null){
						if(((TrajectoryAsSet) g.getRepresentative()).getTid()==((TrajectoryAsSet) t).getTid()){
							g.setRepresentative(null);
						}
					}					
				}				
				count++;
			}
		}
	}
	
	private MicroGroupObject<T> remove(T t) {
		Collection<T> mg_array =  new Vector<T>();
		mg_array.addAll(subTrajecTimeWindow.keySet());
		for (T t2: mg_array) {
			if(t2.equals(t)){
				return subTrajecTimeWindow.remove(t2);
			}
		}
		
		return null;
	}
	
	//update moving object trajectories in the system
	private void updateTrajectory(Collection<T> updateobjects){
		MicroGroupObject<T> mgo;
		MicroGroup<T> g;
		int count=0;
		for (T t : updateobjects) {
			if((mgo=remove(t))!=null) {
				mgo.setOriginalObject(t);
				subTrajecTimeWindow.put(t, mgo);
				mgo.setProcessed(State.UNVISITED);
				
				g = microGroupHash.get(mgo.idMicroGroup);
				//tem um microgroup
				if(g!=null){
					g.removeMicroGroupObject(t);
					g.addMicroGroupObject(t);
					//update de um moving object que eh a trajetoria representativa
					if(g.getRepresentative()!=null){
						if(((TrajectoryAsSet) g.getRepresentative()).getTid()==((TrajectoryAsSet) t).getTid()){
							g.setRepresentative(t);
						}
					}
				}
				count++;
			}
		}
	}
	
	
	private boolean electAnotherRepresentative(MicroGroup<T> g, double eps,int minPoints){
		Collection<T> members = g.getAllMicroGroupObject();
		Collection<T> neighbors;
		MicroGroupObject<T> mgo_member;
		
		for (T t : members) {
			neighbors = dm.neighbors(t,members,eps);
			if(neighbors.size()>=minPoints) {
				g.setRepresentative(t);
				return true;
			}
		}
		
		/**
		 * Micro-group disappears
		 */
		count_dissapear++;
		microGroupHash.remove(g.getIdMicroGroup());
		g.setEvolution(Evolution.DISAPPEARS);
		for (T next : members) {
			mgo_member = subTrajecTimeWindow.get(next);
			mgo_member.idMicroGroup=-1;
		}
		return false;
	}
	
	
	//find the neighbors using the index
	private Collection<T> neighbors(ArrayList<Point> candidatePoints, T candidate, double eps) throws SQLException{
		
		ArrayList<T> neighbors = new ArrayList<T>();
		T candidate_neighboor;
		//id of neighboor objects (indicated by index)
		Collection<Integer> id_neighbors_result = dbmanager.queryIndex(candidatePoints, time_window_id);
		for (Integer id_candidate_neighboor : id_neighbors_result) {
			if(id_candidate_neighboor!=((TrajectoryAsSet) candidate).getTid()){
				candidate_neighboor = getTObject(id_candidate_neighboor);
				if(dm.distance(candidate, candidate_neighboor)<=eps) neighbors.add(candidate_neighboor);
			}			
		}
		
		return neighbors;		
	}
	
	private void removeInCandidates(int tid) {
		candidates.remove(tid);	
	}		
	
	//this method is useful before clustering processing
	//microgroup e seu radius - microgroup_hash
	public HashMap<MicroGroup<T>,Double> radiousMicrogroupComputation(){
		HashMap<MicroGroup<T>, Double> microgroup_hash = new HashMap<MicroGroup<T>, Double>();
		double mg_radius,mg_reachabilityDistance;
		
		for (MicroGroup<T> mg : microGroupHash.values()) {
			mg_radius=Double.NEGATIVE_INFINITY;
			for (T t : mg.getAllMicroGroupObject()) {
				mg_reachabilityDistance = subTrajecTimeWindow.get(t).reachabilityDistance;
				if(mg_radius<mg_reachabilityDistance) mg_radius=mg_reachabilityDistance;
			}
			microgroup_hash.put(mg, mg_radius);
			mg.setRadius(mg_radius);
		}
		
		return microgroup_hash;
	}
	
	
	/*
	 * GET AND SET 
	 * **/
		
	public Collection<MicroGroup<T>> getMicroGroupSet(){
		return microGroupHash.values();
	}

	public Collection<T> getOutlierSet(){
		return outliers;
	}
	
	private T getTObject(Integer tid) {
		Collection<T> objects = subTrajecTimeWindow.keySet();
		
		for (T t : objects) {
			if(((TrajectoryAsSet) t).getTid()==tid) return  t;
		}
		return null;
	}

	public int getTime_window_id() {
		return time_window_id;
	}

	public void setTime_window_id(int time_window_id) {
		this.time_window_id = time_window_id;
	}
	
	public void setCandidates(HashMap<Integer, ArrayList<Point>> candidates) {
		this.candidates = candidates;
	}
	
	public HashMap<Integer, ArrayList<Point>> getCandidates() {
		return candidates;
	}
		
	public void setDBManager(DBManager db){
		this.dbmanager=db;
	}
	
	public HashMap<Integer, MicroGroup<T>> getMicroGroupHash() {
		return microGroupHash;
	}

	public void setMicroGroupHash(HashMap<Integer, MicroGroup<T>> microGroupHash) {
		this.microGroupHash = microGroupHash;
	}

	public HashMap<T, MicroGroupObject<T>> getSubTrajecTimeWindow() {
		return subTrajecTimeWindow;
	}
	
	public String toLogCount(){
		return "\n#mg survives: "+count_survive+"\n#mg appears: "+count_appear+"\n#mg splits: "+count_split+" \n#mg disappears: "+count_dissapear;
	}
		
}
