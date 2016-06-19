package fr.david.mdm.dynamic.flock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import fr.david.mdm.distances.trajectory.DirectionDistanceTrajectoryAsSet;
import fr.david.mdm.distances.trajectory.DirectionSpaceTimeDistance;
import fr.david.mdm.distances.trajectory.TimeSimilarityTrajectoryAsSet;
import fr.david.mdm.microgroup.MicroGroupObject;
import fr.david.mdm.models.MicroGroup;
import fr.david.mdm.models.MicroGroupFlock;
import fr.david.mdm.models.TrajectoryAsSet;

public class MaintainMicroGroupFlock {

	private DirectionSpaceTimeDistance d; 
	private double radius_parameter;
	private int k_parameter;
	private int m_parameter; 
	private HashMap<Integer, MicroGroupFlock> microGroupFlockHash;
	private Collection<MicroGroupFlock> endedFlocks;
	
	/*Distance Function weights*/
	private double w_theta = 0.5;
	private double w_lambda = 0.5;
	
	public MaintainMicroGroupFlock(double radius_parameter, int k_parameter, int m_parameter) {
		this.radius_parameter=radius_parameter;
		this.k_parameter=k_parameter;
		this.m_parameter=m_parameter;
		microGroupFlockHash = new HashMap<Integer, MicroGroupFlock>();
		endedFlocks = new Vector<MicroGroupFlock>();
		
		d = new DirectionSpaceTimeDistance(new DirectionDistanceTrajectoryAsSet(),
				new TimeSimilarityTrajectoryAsSet(),w_theta ,w_lambda);
	}
	
	public <T> void initializeFlocks(HashMap<Integer, MicroGroup<T>> microGroupHash, HashMap<T, MicroGroupObject<T>> microGroupObjectHash, int time_window){
		
		MicroGroupFlock micro_group_flock; //current flock
		ArrayList<TrajectoryAsSet> moving_object_array; //to store the moving objects in the flock
		
		Collection<MicroGroup<T>> micro_group_array = microGroupHash.values(); //all the micro-groups
		ArrayList<T> micro_group_moving_objects; //to store the micro-group's elements
		MicroGroupObject<T> microGroupObject; //to represent the micro-group object
		
		
		for (MicroGroup<T> micro_group : micro_group_array) {
			if(micro_group.getSize()>=m_parameter){
				
				moving_object_array = new ArrayList<TrajectoryAsSet>();
				micro_group_moving_objects = micro_group.getAllMicroGroupObject();
				
				//check basic condition to start a flock candidate
				if(micro_group.getRadius() <= radius_parameter){
					for (T tid : micro_group_moving_objects) {
						moving_object_array.add((TrajectoryAsSet) tid);
					}				
					micro_group_flock = new MicroGroupFlock(micro_group.getIdMicroGroup(), micro_group.getRadius(), moving_object_array, time_window,
							micro_group.getColor());
					microGroupFlockHash.put(micro_group.getIdMicroGroup(),micro_group_flock);
					System.out.println(micro_group.getIdMicroGroup()+" tem "+micro_group.getSize()+" e tem raio "+micro_group.getRadius());

				}
				
				//case where micro-groups has radius greater than 
				else{
					for (T movingObject : micro_group_moving_objects) {
						microGroupObject = microGroupObjectHash.get(movingObject);						
						if(microGroupObject.getReachabilityDistance()<=radius_parameter) moving_object_array.add(((TrajectoryAsSet) movingObject));
					}
					
					if(moving_object_array.size()>=m_parameter){
						micro_group_flock = new MicroGroupFlock(micro_group.getIdMicroGroup(), micro_group.getRadius(), moving_object_array, time_window,
								micro_group.getColor());
						microGroupFlockHash.put(micro_group.getIdMicroGroup(),micro_group_flock);
					}
					
				}
			}//end first if
			
		}//end for
		
	}
	
	//maintained micro_groups, maintained micro_group_objects, current time_window
	public <T> void incrementalMaintenanceFlocks(HashMap<Integer, MicroGroup<T>> microGroupHash, HashMap<T, MicroGroupObject<T>> microGroupObjectHash, int time_window){
		
		//maintained micro_groups
		Collection<Integer> microgroup_id_array = microGroupHash.keySet();
		ArrayList<TrajectoryAsSet> mg_mo_arr; //to store the micro-group's elements
		ArrayList<TrajectoryAsSet> mg_flock_mo_arr; //to store the micro-group flock's elements
		ArrayList<T> mg_mo_array_aux;
		Collection<MicroGroupFlock> microgroupflock_array = microGroupFlockHash.values();
		
		int max_count, count;
		MicroGroupFlock flock_max = null, current_flock;
		
		
		for (Integer mgid : microgroup_id_array) {
			
			//microgroup associates to a flock - in this case the microgroup survived
			if(microGroupFlockHash.containsKey(mgid)){
				mg_mo_arr = new ArrayList<TrajectoryAsSet>();
				
				current_flock = microGroupFlockHash.get(mgid);
				mg_mo_array_aux = microGroupHash.get(mgid).getAllMicroGroupObject();
				mg_flock_mo_arr = current_flock.getMoving_object_array();
				
				//comparar os objetos do microgroupflock com os objetos do microgroup, apenas o que estao dentro do raio da repres.
				for (T t : mg_mo_array_aux) {
					if(mg_flock_mo_arr.contains(((TrajectoryAsSet) t)) && 
							microGroupObjectHash.get(t).getReachabilityDistance()<=radius_parameter){
						mg_mo_arr.add(((TrajectoryAsSet) t)); 
					}
				}
				
				//it continuous to be a flock
				if(mg_mo_arr.size()>=m_parameter){
					current_flock.setMoving_object_array(mg_mo_arr);
					current_flock.addTimeWindow(time_window);
					System.out.println("Flockid "+mgid+" survives!");
				}
				//it is not dense to be a flock anymore
				else{
					microGroupFlockHash.remove(mgid);
					//it is a longest duration flock
					if(current_flock.getTime_window_array().size()>=k_parameter){
						endedFlocks.add(current_flock);
					}
					continue;
				}	
				current_flock.setVisited(true);
			}
			//a new micro-group can initiate new flock candidate
			//or because of slipt it can have a different mgid of a flock candidate 
			else{
				mg_mo_array_aux = microGroupHash.get(mgid).getAllMicroGroupObject();
				max_count = Integer.MIN_VALUE;
				
				//for each flock which is not associate to a mg
				for (MicroGroupFlock flock : microgroupflock_array) {
					
					if(!microGroupHash.containsKey(flock.getFlock_id())){
						
						mg_flock_mo_arr = flock.getMoving_object_array();
						//find the flock candidate which matches more with the mg
						count = matches(mg_flock_mo_arr,mg_mo_array_aux);
						if(count >= m_parameter && count > max_count){
							max_count = count;
							flock_max = flock;
						}							
					}//end if
				}//end for
					
				//flock which matches more with the mg was found (a possible microgroup split)
				if(max_count!=Integer.MIN_VALUE){
					
					mg_mo_arr = new ArrayList<TrajectoryAsSet>();
					
					mg_flock_mo_arr = flock_max.getMoving_object_array();
					
					for (T t : mg_mo_array_aux) {
						if(mg_flock_mo_arr.contains(((TrajectoryAsSet) t))){
							mg_mo_arr.add(((TrajectoryAsSet) t)); 
						}
					}
					
					
					//se tem objetos com raio dentro do permitido
					if(checkTheRadius(mg_mo_arr) <= radius_parameter){
						
						//remove the outdated flock
						microGroupFlockHash.remove(flock_max.getFlock_id());
						
						flock_max.setMoving_object_array(mg_mo_arr);
						flock_max.addTimeWindow(time_window);
						flock_max.setFlock_id(mgid);
						flock_max.setVisited(true);
						flock_max.setColor(microGroupHash.get(mgid).getColor());						
						microGroupFlockHash.put(mgid,flock_max);
						
						System.out.println("Flockid "+mgid+" from a split!");
					}
					
					//se os objetos nao estao dentro do raio de um flock existente, podemos criar ainda um novo flock com
					//os objetos do micro-group
					else{
						createNewFlock(microGroupHash.get(mgid), microGroupHash, microGroupObjectHash, time_window);
					}
					
				}
				
				//caso haja um novo microgroup que nao intersepte com nenhum flock
				else{
					createNewFlock(microGroupHash.get(mgid), microGroupHash, microGroupObjectHash, time_window);
				}
						
			}//end else
			
		}		
		
		//maintained micro_group_flocks
		Collection<MicroGroupFlock> mg_flock_array =  new Vector<MicroGroupFlock>(microGroupFlockHash.values());
		for (MicroGroupFlock mg_flock : mg_flock_array) {
			if(!mg_flock.isVisited()){ //flock candidate finished in the current time window
				//it is a longest duration flock
				if(mg_flock.getTime_window_array().size() >= k_parameter) endedFlocks.add(mg_flock);
				microGroupFlockHash.remove(mg_flock.getFlock_id());
			}
		}//end for
		
		//for the next time window
		setUpFlocks();
		
	}
	
	private double checkTheRadius(ArrayList<TrajectoryAsSet> mg_mo_arr) {
		double maxDistance = Double.MIN_VALUE;
		double distance;
		
		for (TrajectoryAsSet trajectoryAsSet1 : mg_mo_arr) {
			for (TrajectoryAsSet trajectoryAsSet2 : mg_mo_arr) {
				
				if(trajectoryAsSet1.getTid() != trajectoryAsSet2.getTid()){
					
					distance = d.distance(trajectoryAsSet1, trajectoryAsSet2);
					if(distance>=maxDistance){
						maxDistance = distance;
					}
				}
			}
		}
		return (maxDistance/2);
	}

	private <T> void createNewFlock(MicroGroup<T> microgroup, HashMap<Integer, MicroGroup<T>> microGroupHash, 
			HashMap<T, MicroGroupObject<T>> microGroupObjectHash, int time_window) {
		if(microgroup.getSize() >= m_parameter){
			MicroGroup<T> micro_group = microGroupHash.get(microgroup.getIdMicroGroup());
			
			ArrayList<TrajectoryAsSet> mg_mo_arr = new ArrayList<TrajectoryAsSet>();
			ArrayList<T> mg_mo_array_aux = micro_group.getAllMicroGroupObject();
			
			MicroGroupFlock current_flock;
			//check basic condition to start a flock candidate
			if(micro_group.getRadius() <= radius_parameter){
				for (T tid : mg_mo_array_aux) {
					mg_mo_arr.add((TrajectoryAsSet) tid);
				}				
				current_flock = new MicroGroupFlock(micro_group.getIdMicroGroup(), micro_group.getRadius(), 
						mg_mo_arr, time_window, micro_group.getColor());
				microGroupFlockHash.put(micro_group.getIdMicroGroup(),current_flock);
				System.out.println("Flockid "+micro_group.getIdMicroGroup()+" new!");
				return;
			}
			
			//case where micro-groups has radius greater than 
			else{
				for (T movingObject : mg_mo_array_aux) {
					MicroGroupObject<T> microGroupObject = microGroupObjectHash.get(movingObject);						
					if(microGroupObject.getReachabilityDistance()<=radius_parameter) mg_mo_arr.add(((TrajectoryAsSet) movingObject));
				}
				
				if(mg_mo_arr.size()>=m_parameter){
					current_flock = new MicroGroupFlock(micro_group.getIdMicroGroup(), micro_group.getRadius(), mg_mo_arr, time_window,
							micro_group.getColor());
					microGroupFlockHash.put(micro_group.getIdMicroGroup(),current_flock);
					System.out.println("Flockid "+micro_group.getIdMicroGroup()+" new!");
					return;
				}
				
			}
			
		}//end if
		
	}

	private <T> int matches(ArrayList<TrajectoryAsSet> mg_flock_mo_arr, ArrayList<T> mg_mo_array_aux) {
		int count = 0;
		
		for (T t : mg_mo_array_aux) {
			if(mg_flock_mo_arr.contains(t)) count++;
		}
		
		return count;
	}

	//call before each iteration
	public void setUpFlocks(){
		for (MicroGroupFlock micro_group_flock : microGroupFlockHash.values()) {
			micro_group_flock.setVisited(false);
		}
	}
	
	
	public HashMap<Integer, MicroGroupFlock> getMicroGroupFlockHash() {
		return microGroupFlockHash;
	}

	public void setMicroGroupFlockHash(HashMap<Integer, MicroGroupFlock> microGroupFlockHash) {
		this.microGroupFlockHash = microGroupFlockHash;
	}
	
	public double getRadius_parameter() {
		return radius_parameter;
	}

	public void setRadius_parameter(double radius_parameter) {
		this.radius_parameter = radius_parameter;
	}

	public int getK_parameter() {
		return k_parameter;
	}

	public void setK_parameter(int k_parameter) {
		this.k_parameter = k_parameter;
	}

	public int getM_parameter() {
		return m_parameter;
	}

	public void setM_parameter(int m_parameter) {
		this.m_parameter = m_parameter;
	}
	
	public Collection<MicroGroupFlock> getEndedFlocks(){
		return endedFlocks;
	}
	
	
		
}
