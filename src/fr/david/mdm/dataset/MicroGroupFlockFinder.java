package fr.david.mdm.dataset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.Buffer;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.text.html.MinimalHTMLWriter;

import fr.david.mdm.dynamic.flock.MaintainMicroGroupFlock;
import fr.david.mdm.models.Cluster;
import fr.david.mdm.models.MicroGroup;
import fr.david.mdm.models.MicroGroupFlock;
import fr.david.mdm.models.TrajectoryAsSet;

public class MicroGroupFlockFinder {

	private MicroGroupsFinder microgroup_finder;
	private MaintainMicroGroupFlock mmg_flock;
	private int duration_flock = 2;
	private String log;
	private int size_flock;
	private double radius_flock; 
	
	public MicroGroupFlockFinder(double eps, double sigma, int minPoints, double rho, int duration_flock, 
			String initialDate, String server_ip , int server_port, String server_database, 
			int timeWindowSize, int size_flock, double radius_flock) {
		
		this.microgroup_finder = new MicroGroupsFinder(eps, sigma, minPoints, rho, initialDate, 
				server_ip, server_port, server_database, timeWindowSize);
		this.duration_flock=duration_flock;
		this.size_flock = size_flock;
		this.radius_flock = radius_flock;
		this.mmg_flock = new  MaintainMicroGroupFlock(radius_flock, duration_flock, size_flock);
		this.log="log...";
	}
	
	/*public MicroGroupFlockFinder(){
		this.duration_flock=2;
		this.microgroup_finder=new MicroGroupsFinder();
		this.mmg_flock = new MaintainMicroGroupFlock(microgroup_finder.getEps(), duration_flock, microgroup_finder.getMinPoints());
	}*/
	 
	//computation of micro-groups before start to find flocks
	public HashMap<Integer, MicroGroupFlock> computeTimeWindowFlocks(int time_window) throws SQLException, ParseException, IOException{
		
		//TODO só para testar o flock sem o demo DPS RETIRARRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR
		//alterei lah soh pra testar
		HashMap<Integer, MicroGroup<TrajectoryAsSet>> microgroups = microgroup_finder.computeTimeWindowMicroGroupsForFlocks(time_window);
		log=microgroup_finder.getLog();
		
		long startime, endtime;
		int milliseconds, seconds, minutes, hours;
		String responseTime;
		
		if(time_window==0){
			startime = System.currentTimeMillis();
			this.mmg_flock.initializeFlocks(microgroups, this.microgroup_finder.getMicroGroupObjectHash(), time_window);
			endtime = System.currentTimeMillis();
		}
		else{
			startime = System.currentTimeMillis();
			this.mmg_flock.incrementalMaintenanceFlocks(microgroups, this.microgroup_finder.getMicroGroupObjectHash(), time_window);
			endtime = System.currentTimeMillis();
		}
		
		
		milliseconds = (int) (endtime-startime);
		seconds = (int) (milliseconds / 1000) % 60 ;
		minutes = (int) ((milliseconds / (1000*60)) % 60);
		hours   = (int) ((milliseconds / (1000*60*60)) % 24);
		milliseconds = milliseconds - (1000*(3600*hours + 60*minutes + seconds));
		responseTime = String.format("%d hour(s), %d min, %d sec, %d milis", hours, minutes, seconds, milliseconds);
			
		System.out.println("\nRunning time to create MG flocks: "+responseTime);
		log=log.concat("\nRunning time to create MG flocks:"+ responseTime);
		
		HashMap<Integer, MicroGroupFlock> flocks = new HashMap<>(mmg_flock.getMicroGroupFlockHash());
		MicroGroupFlock flock_aux;
		
		for (Integer flockid : flocks.keySet()) {
			flock_aux = flocks.get(flockid);
			if(flock_aux.getTime_window_array().size()>=mmg_flock.getK_parameter()){
				System.out.println("Current Flock: "+flockid+" has "+
						flock_aux.getMoving_object_array().size()+" MO(s) during "+flock_aux.getTime_window_array().size()+" time window(s)");
				log = log.concat("\nCurrent Flock: "+flockid+" has "+
						flock_aux.getMoving_object_array().size()+" MO(s) during "+flock_aux.getTime_window_array().size()+" time window(s)");
				
			}
			else{
				System.out.println("Flock Candidate: "+flockid+" has "+
						flock_aux.getMoving_object_array().size()+" MO(s) during "+flock_aux.getTime_window_array().size()+" time window(s)");
				log = log.concat("\nFlock Candidate: "+flockid+" has "+
						flock_aux.getMoving_object_array().size()+" MO(s) during "+flock_aux.getTime_window_array().size()+" time window(s)");
				
			}
			
		}
		
		if(time_window >= mmg_flock.getK_parameter()){
			for (MicroGroupFlock flock : this.mmg_flock.getEndedFlocks()) {
				log = log.concat("\nFinish Flock: "+flock.getFlock_id()+" has "+
						flock.getMoving_object_array().size()+" MO(s) during past "+flock.getTime_window_array().size()+" time window(s)");
					
				System.out.println("\nFinish Flock: "+flock.getFlock_id()+" has "+
						flock.getMoving_object_array().size()+" MO(s) during past "+flock.getTime_window_array().size()+" time window(s)");

				flocks.put(flock.getFlock_id(), flock);
			}
		}
		
		if(time_window==4){
			 FileWriter outFile = new FileWriter("flocks.txt");
		     BufferedWriter bufWrite = new BufferedWriter(outFile);
			
			for (MicroGroupFlock flock : this.mmg_flock.getEndedFlocks()) {
				System.out.println("Flock: "+flock.getFlock_id());
				String time_window_string = ""+flock.getFlock_id();
				for (Integer timeWindow : flock.getTime_window_array()) {
					time_window_string = time_window_string.concat(";"+timeWindow);
				}
				for (TrajectoryAsSet t_mo :flock.getMoving_object_array()) {
					bufWrite.write(time_window_string+";"+t_mo.getTid()+"\n");		
				}				
				//flock.getMoving_object_array().size()+" MO during "+flock.getTime_window_array().size()+" time windows");
				//log = log.concat("Flock: "+flock.getFlock_id()+" has "+
					//	flock.getMoving_object_array().size()+" MO during "+flock.getTime_window_array().size()+" time windows");
			}
			
			for (Integer flockid : flocks.keySet()) {
				flock_aux = flocks.get(flockid);
				if(flock_aux.getTime_window_array().size()>=mmg_flock.getK_parameter()){
					System.out.println("Flock: "+flock_aux.getFlock_id());
					String time_window_string = ""+flock_aux.getFlock_id();
					for (Integer timeWindow : flock_aux.getTime_window_array()) {
						time_window_string = time_window_string.concat(";"+timeWindow);
					}
					for (TrajectoryAsSet t_mo :flock_aux.getMoving_object_array()) {
						bufWrite.write(time_window_string+";"+t_mo.getTid()+"\n");		
					}
				}							
			}
			bufWrite.close();

		}
		
		
		return flocks;
	}
	
	public String getLog() {
		return log;
	}
	
	public static void main(String[] args) throws ParseException, SQLException, IOException {
		MicroGroupFlockFinder mgf = new MicroGroupFlockFinder(0.004, 0.004, 20, 60.49, 2, 
				"2008-02-04 17:30:00", "localhost" , 2181, "trajevent", 
				420, 20, 0.004);
		for (int i = 0; i < 5; i++) {
			mgf.computeTimeWindowFlocks(i);		
		}
		
	}
	
}
