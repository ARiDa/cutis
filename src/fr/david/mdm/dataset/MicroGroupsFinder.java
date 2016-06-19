package fr.david.mdm.dataset;

import fr.david.mdm.distances.trajectory.DirectionDistanceTrajectoryAsSet;
import fr.david.mdm.distances.trajectory.DirectionSpaceTimeDistance;
import fr.david.mdm.distances.trajectory.TimeSimilarityTrajectoryAsSet;
import fr.david.mdm.kafka.SimpleTrajectoryConsumer;
import fr.david.mdm.microgroup.MaintainMicroGroup;
import fr.david.mdm.microgroup.MicroGroupObject;
import fr.david.mdm.microgroup.clustering.SubTrajectoryClustering;
import fr.david.mdm.models.Cluster;
import fr.david.mdm.models.MicroGroup;
import fr.david.mdm.models.MovingObject;
import fr.david.mdm.models.TrajectoryAsSet;
import fr.david.mdm.tools.Hilbert_Segment_Data;
import fr.david.mdm.tools.Point;
import scala.Responder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

import com.vividsolutions.jts.geom.Coordinate;

public class MicroGroupsFinder {
	
	private DirectionSpaceTimeDistance d; 
    private MaintainMicroGroup<TrajectoryAsSet> mmg;
    private DBManager db;
	private HashMap<Integer, MovingObject> movingObjectSetLastTimeWindow;
	private Vector<TrajectoryAsSet> movingobject_updated;
	private Vector<TrajectoryAsSet> movingobject_new;
	private Vector<TrajectoryAsSet> movingobject_old;
	private String log;
	public HashMap<Integer, MovingObject> movingObjectSetCurrentTimeWindow;
	private ArrayList<TrajectoryAsSet> outliers;
	
	/*Microgroup parameters*/
	private double eps = 0.004;
	private double sigma = 0.004;
	private int minPoints = 20;
	private double rho = 60.49; 
	
	/*Distance Function weights*/
	private double w_theta = 0.5;
	private double w_lambda = 0.5;
	
	/*Connection details to get data stream*/
	private String initialDate = "2008-02-04 17:30:00";
	private Integer timeWindowSize = 420;//420 (mg e clusters); //time window in seconds
	private Integer rateSTraj = timeWindowSize/60;
	private Integer timeout =10000000; //210 (mg e clusters); //in second, timeout to delete
	
	/*To consume streams from kafka*/
	private String zookeeper;
	private String groupId;
	private String topic;
	private SimpleTrajectoryConsumer simpleHLConsumer;
	
	
	public MicroGroupsFinder(double eps, double sigma, int minPoints, double rho, 
			String initialDate, String server_ip , int server_port, final String topic, int timeWindowSize) {
		
		this.zookeeper=server_ip+":"+server_port;
		this.groupId="1";
		this.topic=topic;
		
		final MicroGroupsFinder object = this;
		
		new Thread(new Runnable() {
		    public void run() {
		    	simpleHLConsumer = new SimpleTrajectoryConsumer(zookeeper, groupId, topic, object);
				new Thread(simpleHLConsumer).start();
		    }
		}).start();
		
		System.out.println("Microgroup Finder");
		d = new DirectionSpaceTimeDistance(new DirectionDistanceTrajectoryAsSet(),
				new TimeSimilarityTrajectoryAsSet(),w_theta ,w_lambda);
		mmg = new MaintainMicroGroup<TrajectoryAsSet>(d);
		db = new DBManager("localhost",5433, "tdrive");
		mmg.setDBManager(db);
		
		
		/*Connection details to get data stream**/
		this.initialDate=initialDate+":00";
		this.timeWindowSize=timeWindowSize;
		this.rateSTraj = timeWindowSize/60;		
		
		/*Parameters settings*/
		this.eps=eps;
		this.sigma=sigma;
		this.minPoints=minPoints;
		this.rho=rho;		
		
		log="log...";
			
	}
	
	public MicroGroupsFinder() {
		d = new DirectionSpaceTimeDistance(new DirectionDistanceTrajectoryAsSet(),
				new TimeSimilarityTrajectoryAsSet(),w_theta ,w_lambda);
		mmg = new MaintainMicroGroup<TrajectoryAsSet>(d);
		db = new DBManager();
		mmg.setDBManager(db);
	}
	
	public Vector<TrajectoryAsSet> computeTimeWindowData(int time_window) throws ParseException{
		
		log="";
		
		HashMap<Integer, MovingObject> movingObjectSetCurrentTimeWindow;	    
	    MovingObject mo_current, mo_last;
	    Collection<Integer> keys;
	    
	    /*Initial and End time for time window
	     * */
	    
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	    Date parsedInitialDate = dateFormat.parse(initialDate); 
	    long t=parsedInitialDate.getTime();
	    Timestamp t_final,t_initial;
	    long miliseconds;
	    Date date_t;  
			
		miliseconds = t + (time_window*timeWindowSize*1000);
		date_t = new Date(miliseconds); //in milisseconds
		t_initial = new java.sql.Timestamp(date_t.getTime());
		
		//if(time_window+1==qtdTimeWindow) miliseconds = t + (trackingTime*1000); else
		miliseconds = t + ((time_window+1)*timeWindowSize*1000);
		date_t  = new Date(miliseconds);		    
		t_final = new java.sql.Timestamp(date_t.getTime());
		    
			
		movingObjectSetCurrentTimeWindow = db.getTrajectories(t_initial, t_final, time_window, rateSTraj);
		    
		    
		System.out.println("-------------------------------");
		log=log.concat("Start: "+t_initial+" End: "+t_final);
		System.out.println("Start: "+t_initial+" End: "+t_final);
		movingobject_new = new Vector<TrajectoryAsSet>(); 
		if(movingObjectSetLastTimeWindow!=null){
	    	
			movingobject_updated = new Vector<TrajectoryAsSet>(); 
	    	movingobject_old = new Vector<TrajectoryAsSet>();
	    	
	    	keys = movingObjectSetLastTimeWindow.keySet(); 
		    	
		    for (Integer mo_id : keys) {
					if((mo_current=movingObjectSetCurrentTimeWindow.get(mo_id)) != null){
						//update its position
						movingobject_updated.add(new TrajectoryAsSet(mo_current.getIdMO(),mo_current.getIdMO(),mo_current.getTime_array(),mo_current.getCoord_array()));
					}
					else{
						mo_last = movingObjectSetLastTimeWindow.get(mo_id);
						if(t_final.getTime()-mo_last.getTime_array().get(mo_last.getTime_array().size()-1)>=timeout*1000){
							//remove because of timeout
							movingobject_old.add(new TrajectoryAsSet(mo_last.getIdMO(),mo_last.getIdMO(),mo_last.getTime_array(),mo_last.getCoord_array()));
						}
						else{
							//predict the sub-trajectory -update
							ArrayList<Coordinate> coordinate_list = new ArrayList<Coordinate>();
							ArrayList<Long> time_arrray = new ArrayList<Long>();
							
							boolean isStopped = false;
							Coordinate coord_1 = mo_last.getCoord_array().get(mo_last.getCoord_array().size()-2);
							Coordinate coord_2 = mo_last.getCoord_array().get(mo_last.getCoord_array().size()-1);
							long time_1 = mo_last.getTime_array().get(mo_last.getTime_array().size()-2);
							long time_2 = mo_last.getTime_array().get(mo_last.getTime_array().size()-1);
							
																			
							long desiredTime = t_initial.getTime(); 							
							Coordinate coord_initalTime = linearInterpolation(coord_1,coord_2,time_1, time_2, desiredTime);
							desiredTime = t_final.getTime(); 
							Coordinate coord_finalTime = linearInterpolation(coord_1,coord_2,time_1, time_2, desiredTime);

							if(coord_initalTime==null || coord_finalTime==null) {
								isStopped=true;
							}
							else{
								coordinate_list.add(coord_initalTime);
								time_arrray.add(t_initial.getTime());
								coordinate_list.add(coord_finalTime);
								time_arrray.add(t_final.getTime());
							}
							
							if(!isStopped){
								movingobject_updated.add(new TrajectoryAsSet(mo_last.getIdMO(),mo_last.getIdMO(),time_arrray,coordinate_list));
								movingObjectSetCurrentTimeWindow.put(mo_id, mo_last);
							}							
						}
					}
				}
		    }
		    
		  //new moving object
	    	keys =movingObjectSetCurrentTimeWindow.keySet(); 
	    	
	    	for (Integer mo_id : keys) {
				if(movingObjectSetLastTimeWindow==null || movingObjectSetLastTimeWindow.get(mo_id)==null){
					mo_current=movingObjectSetCurrentTimeWindow.get(mo_id);
					movingobject_new.add(new TrajectoryAsSet(mo_current.getIdMO(),mo_current.getIdMO(),mo_current.getTime_array(),mo_current.getCoord_array()));
				}
			}
	    	System.out.println("#New moving object: "+movingobject_new.size());
	    	//log=log.concat("\n#new moving objects: "+movingobject_new.size());
	    	
	    	Vector<TrajectoryAsSet> all_objects = new Vector<TrajectoryAsSet>(movingobject_new) ;
	    	if(movingobject_updated!=null && !movingobject_updated.isEmpty()){
	    		if(!movingobject_old.isEmpty()){
	    			System.out.println("#Deleted moving object: "+movingobject_old.size());
	    			//log=log.concat("\n#old moving objects: "+movingobject_old.size());
	    		}
		    	System.out.println("#Updated moving object: "+movingobject_updated.size());
		    	//log=log.concat("\n#updated moving objects: "+movingobject_updated.size());
		    	all_objects.addAll(movingobject_updated);
	    	}    
	    	
	    	movingObjectSetLastTimeWindow = new HashMap<Integer, MovingObject>();
	 	    movingObjectSetLastTimeWindow.putAll(movingObjectSetCurrentTimeWindow);
	    	
	    	return all_objects;
	}
    
	private Vector<TrajectoryAsSet> computeTimeWindowData(HashMap<Integer, MovingObject> movingObjectSetCurrentTimeWindow, int time_window) throws ParseException{
		
		log="";
			    
	    MovingObject mo_current, mo_last;
	    Collection<Integer> keys;
	    
	    /*Initial and End time for time window
	     * */
	    
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	    Date parsedInitialDate = dateFormat.parse(initialDate); 
	    long t=parsedInitialDate.getTime();
	    Timestamp t_final,t_initial;
	    long miliseconds;
	    Date date_t;  
			
		miliseconds = t + (time_window*timeWindowSize*1000);
		date_t = new Date(miliseconds); //in milisseconds
		t_initial = new java.sql.Timestamp(date_t.getTime());
		
		//if(time_window+1==qtdTimeWindow) miliseconds = t + (trackingTime*1000); else
		miliseconds = t + ((time_window+1)*timeWindowSize*1000);
		date_t  = new Date(miliseconds);		    
		t_final = new java.sql.Timestamp(date_t.getTime());
		    
		System.out.println("-------------------------------");
		log=log.concat("Start: "+t_initial+" End: "+t_final);
		System.out.println("Start: "+t_initial+" End: "+t_final);
		movingobject_new = new Vector<TrajectoryAsSet>(); 
		
		if(movingObjectSetLastTimeWindow!=null){
	    	
			movingobject_updated = new Vector<TrajectoryAsSet>(); 
	    	movingobject_old = new Vector<TrajectoryAsSet>();
	    	
	    	keys = movingObjectSetLastTimeWindow.keySet(); 
		    	
		    for (Integer mo_id : keys) {
		    	if((mo_current=movingObjectSetCurrentTimeWindow.get(mo_id)) != null){
					//update its position
					movingobject_updated.add(new TrajectoryAsSet(mo_current.getIdMO(),mo_current.getIdMO(),mo_current.getTime_array(),mo_current.getCoord_array()));
				}
				else{
					mo_last = movingObjectSetLastTimeWindow.get(mo_id);
					if(t_final.getTime()-mo_last.getTime_array().get(mo_last.getTime_array().size()-1)>=timeout*1000){
						//remove because of timeout
						movingobject_old.add(new TrajectoryAsSet(mo_last.getIdMO(),mo_last.getIdMO(),mo_last.getTime_array(),mo_last.getCoord_array()));
					}
					else{
						//predict the sub-trajectory -update
						ArrayList<Coordinate> coordinate_list = new ArrayList<Coordinate>();
						ArrayList<Long> time_arrray = new ArrayList<Long>();
						
						
						boolean isStopped = false;
						Coordinate coord_1 = mo_last.getCoord_array().get(mo_last.getCoord_array().size()-2);
						Coordinate coord_2 = mo_last.getCoord_array().get(mo_last.getCoord_array().size()-1);
						long time_1 = mo_last.getTime_array().get(mo_last.getTime_array().size()-2);
						long time_2 = mo_last.getTime_array().get(mo_last.getTime_array().size()-1);
						
																		
						long desiredTime = t_initial.getTime(); 							
						Coordinate coord_initalTime = linearInterpolation(coord_1,coord_2,time_1, time_2, desiredTime);
						desiredTime = t_final.getTime(); 
						Coordinate coord_finalTime = linearInterpolation(coord_1,coord_2,time_1, time_2, desiredTime);

						if(coord_initalTime==null || coord_finalTime==null) {
							isStopped=true;
						}
						else{
							coordinate_list.add(coord_initalTime);
							time_arrray.add(t_initial.getTime());
							coordinate_list.add(coord_finalTime);
							time_arrray.add(t_final.getTime());
						}
						
						if(!isStopped){
							movingobject_updated.add(new TrajectoryAsSet(mo_last.getIdMO(),mo_last.getIdMO(),time_arrray,coordinate_list));
							movingObjectSetCurrentTimeWindow.put(mo_id, mo_last);
						}							
					}
				}
			}
	    }
		    
	    //new moving object
    	keys =movingObjectSetCurrentTimeWindow.keySet(); 
    	for (Integer mo_id : keys) {
			if(movingObjectSetLastTimeWindow==null || movingObjectSetLastTimeWindow.get(mo_id)==null){
				mo_current=movingObjectSetCurrentTimeWindow.get(mo_id);
				movingobject_new.add(new TrajectoryAsSet(mo_current.getIdMO(),mo_current.getIdMO(),mo_current.getTime_array(),mo_current.getCoord_array()));
			}
		}
    	System.out.println("#New moving objects: "+movingobject_new.size());
    	
    	Vector<TrajectoryAsSet> all_objects = new Vector<TrajectoryAsSet>(movingobject_new) ;
    	if(movingobject_updated!=null && !movingobject_updated.isEmpty()){
    		if(!movingobject_old.isEmpty()){
    			System.out.println("#Deleted de moving object: "+movingobject_old.size());
    			//log=log.concat("\n#old moving objects: "+movingobject_old.size());
    		}
	    	System.out.println("#Updated moving object: "+movingobject_updated.size());
	    	//log=log.concat("\n#updated moving objects: "+movingobject_updated.size());
	    	all_objects.addAll(movingobject_updated);
    	}    
    	
    	movingObjectSetLastTimeWindow = new HashMap<Integer, MovingObject>();
 	    movingObjectSetLastTimeWindow.putAll(movingObjectSetCurrentTimeWindow);
    	
    	return all_objects;
	}
	
	

	public HashMap<Integer, MicroGroup<TrajectoryAsSet>> computeTimeWindowMicroGroups (int time_window) throws SQLException, ParseException {   	
		
		int milliseconds, seconds, minutes, hours;
		String responseTime;
		
		 
		//for the demo
		movingObjectSetCurrentTimeWindow = new HashMap<Integer, MovingObject>(simpleHLConsumer.movingObjectTimeWindow);
		computeTimeWindowData(movingObjectSetCurrentTimeWindow, time_window);
		
		
		 //System.out.println("Fini!"+" #movingobjects "+movingObjectSetCurrentTimeWindow.keySet().size());
		 //movingObjectSetCurrentTimeWindow=null;
		 
		Vector<TrajectoryAsSet> all_objects = new Vector<TrajectoryAsSet>(movingobject_new) ;
    	if(movingobject_updated!=null && !movingobject_updated.isEmpty()){	    	
	    	all_objects.addAll(movingobject_updated);
    	}		
		
		//TODO hilbert curve for free movement	  
		Hilbert_Segment_Data segment = new Hilbert_Segment_Data();
		segment.trajectories2Index(all_objects, eps);
		long starttime=System.currentTimeMillis();
		db.deletePreviusIndex("index_table_result"+time_window, time_window);
		db.maintainBTreeIndex("index_table_result"+time_window, segment.IndexMap);
		HashMap<Integer, ArrayList<Point>> candidates = segment.getRepresentativeCandidates(minPoints);
		long finaltime=System.currentTimeMillis();
		
		milliseconds = (int) (finaltime-starttime);
		seconds = (int) (milliseconds / 1000) % 60 ;
		minutes = (int) ((milliseconds / (1000*60)) % 60);
		hours   = (int) ((milliseconds / (1000*60*60)) % 24);
		milliseconds = milliseconds - (1000*(3600*hours + 60*minutes + seconds));
		responseTime = String.format("%d hour(s), %d min, %d sec, %d milis", hours, minutes, seconds, milliseconds);
		
		System.out.println("Running time to build index: "+responseTime);
		log=log.concat("\nRunning time to build index: "+responseTime);
		db.setSegmentData(segment);
		    	
		
		/**
		 * To create micro-grupos **/
		
		
		//TODO to hilbert curve	    	
		HashMap<Integer, ArrayList<Point>> candidatesToClusteringPhase = new HashMap<Integer, ArrayList<Point>>();	    	
		candidatesToClusteringPhase.putAll(candidates);
		
		mmg.setTime_window_id(time_window);
		
		if(movingobject_updated!=null){
			  	
			//TODO to hilbert curve
			mmg.setCandidates(candidates); 
			
	    	log=log.concat("\n#new moving objects: "+movingobject_new.size());
	    	log=log.concat("\n#old moving objects: "+movingobject_old.size());
			log=log.concat("\n#updated moving objects: "+movingobject_updated.size());
			
			mmg.incrementalSetUpSystem(movingobject_new, movingobject_updated,movingobject_old);
			
			starttime=System.currentTimeMillis();
			mmg.incrementalMaintenanceSystem(eps, minPoints, rho, sigma);
			finaltime=System.currentTimeMillis();
		}
		else{
			mmg.setUp(movingobject_new);
	    	log=log.concat("\n#new moving objects: "+movingobject_new.size());
			
			//TODO to hilbert curve
			mmg.setCandidates(candidates);
			
			starttime=System.currentTimeMillis();
			mmg.initializeMicroGroup(movingobject_new,eps,minPoints,rho,sigma);
			finaltime=System.currentTimeMillis();
		}
		
		milliseconds = (int) (finaltime-starttime);
		seconds = (int) (milliseconds / 1000) % 60 ;
		minutes = (int) ((milliseconds / (1000*60)) % 60);
		hours   = (int) ((milliseconds / (1000*60*60)) % 24);
		milliseconds = milliseconds - (1000*(3600*hours + 60*minutes + seconds));
		responseTime = String.format("%d hour(s), %d min, %d sec, %d milis", hours, minutes, seconds, milliseconds);
		
		System.out.println("time to create micro-groups: "+responseTime);
		log=log.concat("\nRunning time to maintain micro-groups: "+responseTime);
		System.out.println("#micro-grupos:"+mmg.getMicroGroupSet().size());	
		log=log.concat("\n#micro-groups:"+mmg.getMicroGroupSet().size());
		log = log.concat(mmg.toLogCount());
		mmg.discoverOutliers();
		outliers = new ArrayList<TrajectoryAsSet>(mmg.getOutlierSet());
		System.out.println("#outliers: "+outliers.size());
		log=log.concat("\n#outliers: "+outliers.size());
				
		//TODO this is for FLOCKS!!
		mmg.radiousMicrogroupComputation();
			    
	    return mmg.getMicroGroupHash();
	}
	
	public HashMap<Integer, MicroGroup<TrajectoryAsSet>> computeTimeWindowMicroGroupsForFlocks (int time_window) throws SQLException, ParseException {   	
		
		int milliseconds, seconds, minutes, hours;
		String responseTime;
		
		//TODO só para testar o flock sem o demo DPS RETIRARRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR 
		computeTimeWindowData(time_window);
		 
		//for the demo
		//movingObjectSetCurrentTimeWindow = new HashMap<Integer, MovingObject>(simpleHLConsumer.movingObjectTimeWindow);
		//computeTimeWindowData(movingObjectSetCurrentTimeWindow, time_window);
		
		
		 //System.out.println("Fini!"+" #movingobjects "+movingObjectSetCurrentTimeWindow.keySet().size());
		 //movingObjectSetCurrentTimeWindow=null;
		 
		Vector<TrajectoryAsSet> all_objects = new Vector<TrajectoryAsSet>(movingobject_new) ;
    	if(movingobject_updated!=null && !movingobject_updated.isEmpty()){	    	
	    	all_objects.addAll(movingobject_updated);
    	}		
		
		//TODO hilbert curve for free movement	  
		Hilbert_Segment_Data segment = new Hilbert_Segment_Data();
		segment.trajectories2Index(all_objects, eps);
		long starttime=System.currentTimeMillis();
		db.deletePreviusIndex("index_table_result"+time_window, time_window);
		db.maintainBTreeIndex("index_table_result"+time_window, segment.IndexMap);
		HashMap<Integer, ArrayList<Point>> candidates = segment.getRepresentativeCandidates(minPoints);
		long finaltime=System.currentTimeMillis();
		
		milliseconds = (int) (finaltime-starttime);
		seconds = (int) (milliseconds / 1000) % 60 ;
		minutes = (int) ((milliseconds / (1000*60)) % 60);
		hours   = (int) ((milliseconds / (1000*60*60)) % 24);
		milliseconds = milliseconds - (1000*(3600*hours + 60*minutes + seconds));
		responseTime = String.format("%d hour(s), %d min, %d sec, %d milis", hours, minutes, seconds, milliseconds);
		
		System.out.println("Running time to build index: "+responseTime);
		log=log.concat("\nRunning time to build index: "+responseTime);
		db.setSegmentData(segment);
		    	
		
		/**
		 * To create micro-grupos **/
		
		
		//TODO to hilbert curve	    	
		HashMap<Integer, ArrayList<Point>> candidatesToClusteringPhase = new HashMap<Integer, ArrayList<Point>>();	    	
		candidatesToClusteringPhase.putAll(candidates);
		
		mmg.setTime_window_id(time_window);
		
		if(movingobject_updated!=null){
			  	
			//TODO to hilbert curve
			mmg.setCandidates(candidates); 
			
	    	log=log.concat("\n#new moving objects: "+movingobject_new.size());
	    	log=log.concat("\n#old moving objects: "+movingobject_old.size());
			log=log.concat("\n#updated moving objects: "+movingobject_updated.size());
			
			mmg.incrementalSetUpSystem(movingobject_new, movingobject_updated,movingobject_old);
			
			starttime=System.currentTimeMillis();
			mmg.incrementalMaintenanceSystem(eps, minPoints, rho, sigma);
			finaltime=System.currentTimeMillis();
		}
		else{
			mmg.setUp(movingobject_new);
	    	log=log.concat("\n#new moving objects: "+movingobject_new.size());
			
			//TODO to hilbert curve
			mmg.setCandidates(candidates);
			
			starttime=System.currentTimeMillis();
			mmg.initializeMicroGroup(movingobject_new,eps,minPoints,rho,sigma);
			finaltime=System.currentTimeMillis();
		}
		
		milliseconds = (int) (finaltime-starttime);
		seconds = (int) (milliseconds / 1000) % 60 ;
		minutes = (int) ((milliseconds / (1000*60)) % 60);
		hours   = (int) ((milliseconds / (1000*60*60)) % 24);
		milliseconds = milliseconds - (1000*(3600*hours + 60*minutes + seconds));
		responseTime = String.format("%d hour(s), %d min, %d sec, %d milis", hours, minutes, seconds, milliseconds);
		
		System.out.println("time to create micro-groups: "+responseTime);
		log=log.concat("\nRunning time to maintain micro-groups: "+responseTime);
		System.out.println("#micro-grupos:"+mmg.getMicroGroupSet().size());	
		log=log.concat("\n#micro-groups:"+mmg.getMicroGroupSet().size());
		log = log.concat(mmg.toLogCount());
		mmg.discoverOutliers();
		outliers = new ArrayList<TrajectoryAsSet>(mmg.getOutlierSet());
		System.out.println("#outliers: "+outliers.size());
		log=log.concat("\n#outliers: "+outliers.size());
				
		//TODO this is for FLOCKS!!
		mmg.radiousMicrogroupComputation();
			    
	    return mmg.getMicroGroupHash();
	}
	

	public ArrayList<Cluster<TrajectoryAsSet>> computeTimeWindowSubTrajectoryClusters (int time_window) throws SQLException, ParseException {   	
	    
		int milliseconds, seconds, minutes, hours;
		String responseTime;
		
		movingObjectSetCurrentTimeWindow = new HashMap<Integer, MovingObject>(simpleHLConsumer.movingObjectTimeWindow);
		computeTimeWindowData(movingObjectSetCurrentTimeWindow, time_window);
		
		//computeTimeWindowData(time_window);
		
		 System.out.println("Fini!"+" #movingobjects "+movingObjectSetCurrentTimeWindow.keySet().size());
		 //movingObjectSetCurrentTimeWindow=null;
		
		Vector<TrajectoryAsSet> all_objects = new Vector<TrajectoryAsSet>(movingobject_new) ;
    	if(movingobject_updated!=null && !movingobject_updated.isEmpty()){	    	
	    	all_objects.addAll(movingobject_updated);
    	}		
		
		//TODO hilbert curve for free movement	  
		Hilbert_Segment_Data segment = new Hilbert_Segment_Data();
		segment.trajectories2Index(all_objects, eps);
		long starttime=System.currentTimeMillis();
		db.deletePreviusIndex("index_table_result"+time_window, time_window);
		db.maintainBTreeIndex("index_table_result"+time_window, segment.IndexMap);
		HashMap<Integer, ArrayList<Point>> candidates = segment.getRepresentativeCandidates(minPoints);
		long finaltime=System.currentTimeMillis();
		
		milliseconds = (int) (finaltime-starttime);
		seconds = (int) (milliseconds / 1000) % 60 ;
		minutes = (int) ((milliseconds / (1000*60)) % 60);
		hours   = (int) ((milliseconds / (1000*60*60)) % 24);
		milliseconds = milliseconds - (1000*(3600*hours + 60*minutes + seconds));
		responseTime = String.format("%d hour(s), %d min, %d sec, %d milis", hours, minutes, seconds, milliseconds);
		
		System.out.println("Running time to build index: "+responseTime);
		log=log.concat("\nRunning time to build index: "+responseTime);
		db.setSegmentData(segment);
		    	
		
		/**
		 * To create micro-grupos **/
		
		
		//TODO to hilbert curve	    	
		HashMap<Integer, ArrayList<Point>> candidatesToClusteringPhase = new HashMap<Integer, ArrayList<Point>>();	    	
		candidatesToClusteringPhase.putAll(candidates);
		
		mmg.setTime_window_id(time_window);
		
		if(movingobject_updated!=null){
			  	
			//TODO to hilbert curve
			mmg.setCandidates(candidates); 
			
			log=log.concat("\n#new moving objects: "+movingobject_new.size());
	    	log=log.concat("\n#old moving objects: "+movingobject_old.size());
			log=log.concat("\n#updated moving objects: "+movingobject_updated.size());
			
			mmg.incrementalSetUpSystem(movingobject_new, movingobject_updated,movingobject_old);
			
			starttime=System.currentTimeMillis();
			mmg.incrementalMaintenanceSystem(eps, minPoints, rho, sigma);
			finaltime=System.currentTimeMillis();
		}
		else{
			mmg.setUp(movingobject_new);
			
			log=log.concat("\n#new moving objects: "+movingobject_new.size());
			//TODO to hilbert curve
			mmg.setCandidates(candidates);
			
			starttime=System.currentTimeMillis();
			mmg.initializeMicroGroup(movingobject_new,eps,minPoints,rho,sigma);
			finaltime=System.currentTimeMillis();
		}
		
		milliseconds = (int) (finaltime-starttime);
		seconds = (int) (milliseconds / 1000) % 60 ;
		minutes = (int) ((milliseconds / (1000*60)) % 60);
		hours   = (int) ((milliseconds / (1000*60*60)) % 24);
		milliseconds = milliseconds - (1000*(3600*hours + 60*minutes + seconds));
		responseTime = String.format("%d hour(s), %d min, %d sec, %d milis", hours, minutes, seconds, milliseconds);
		
		System.out.println("time to create micro-groups: "+responseTime);
		log=log.concat("\nRunning time to maintain micro-groups: "+responseTime);
		System.out.println("#micro-grupos:"+mmg.getMicroGroupSet().size());	
		log=log.concat("\n#micro-grupos:"+mmg.getMicroGroupSet().size());
		log = log.concat(mmg.toLogCount());
		mmg.discoverOutliers();
		outliers = new ArrayList<TrajectoryAsSet>(mmg.getOutlierSet());
		System.out.println("#outliers: "+outliers.size());
		log=log.concat("\n#outliers: "+outliers.size());
		
		HashMap<MicroGroup<TrajectoryAsSet>, Double> hash = mmg.radiousMicrogroupComputation();
		SubTrajectoryClustering<TrajectoryAsSet> subclustering = new SubTrajectoryClustering<TrajectoryAsSet>(d);
		
		//TODO to hilbert curve
		subclustering.setObjectsHilbert(candidatesToClusteringPhase);			
		
		starttime=System.currentTimeMillis();
		ArrayList<Cluster<TrajectoryAsSet>> clusters = subclustering.clustering(eps, minPoints, hash, mmg.getSubTrajecTimeWindow().keySet(),db,time_window);
		finaltime=System.currentTimeMillis();
		
		milliseconds = (int) (finaltime-starttime);
		seconds = (int) (milliseconds / 1000) % 60 ;
		minutes = (int) ((milliseconds / (1000*60)) % 60);
		hours   = (int) ((milliseconds / (1000*60*60)) % 24);
		milliseconds = milliseconds - (1000*(3600*hours + 60*minutes + seconds));
		responseTime = String.format("%d hour(s), %d min, %d sec, %d milis", hours, minutes, seconds, milliseconds);
		
		
		System.out.println("time to create MG clusters: "+responseTime);
		System.out.println("#clusters: "+clusters.size());
		log=log.concat("\nRunning time to create MG clusters: "+responseTime);
		log=log.concat("\n#clusters: "+clusters.size());
		
					
		for (Cluster<TrajectoryAsSet> cluster : clusters) {
			System.out.println("Cluster: -------"+cluster.getIdCluster());
			//log=log.concat("Cluster: -------"+cluster.getIdCluster());
			for (MicroGroup<TrajectoryAsSet> mg : cluster.getClusterMembers()) {
				System.out.println("Member:"+mg.getIdMicroGroup());
				//log=log.concat("Member:"+mg.getIdMicroGroup());
			}
		}
	    
	    return clusters;
	}
	
	
	public HashMap<TrajectoryAsSet, MicroGroupObject<TrajectoryAsSet>> getMicroGroupObjectHash(){
		return mmg.getSubTrajecTimeWindow();
	}
	
	public ArrayList<TrajectoryAsSet> getOutliers(){	
		return outliers;
	}
	
	public static Coordinate linearInterpolation(Coordinate c1, Coordinate c2, long t1, long t2, long desiredTime){
		//assumindo velocidade constante
		if((c2.x - c1.x)==0 && (c2.y-c1.y)==0) return null; 
		if(t1==t2) return null;
		double v = (c2.x-c1.x)/Math.abs(t2-t1);
		double x = c1.x + v*(desiredTime-t1); //valor de x
		
		v = (c2.y-c1.y)/Math.abs(t2-t1);
		double y = c1.y + v*(desiredTime-t1); //valor de y
		
		Coordinate c3 = new Coordinate(x, y);
		return c3;
	}

	public String getLog() {
		return log;
	}
	
	public int getMinPoints() {
		return minPoints;
	}

	public void setMinPoints(int minPoints) {
		this.minPoints = minPoints;
	}

	public double getRho() {
		return rho;
	}

	public void setRho(double rho) {
		this.rho = rho;
	}

	public double getEps() {
		return eps;
	}
	
		
}
