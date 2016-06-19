package fr.david.mdm.resource;

import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import fr.david.mdm.dataset.MicroGroupsFinder;
import fr.david.mdm.models.Cluster;
import fr.david.mdm.models.MicroGroup;
import fr.david.mdm.models.MicroGroupException;
import fr.david.mdm.models.RequestBody;
import fr.david.mdm.models.TrajectoryAsSet;
import fr.david.mdm.tools.CaptureScreen;

@Path("/microgroupsonxml")
public class MicroGroupResource<T> {
	
	@Context ServletContext context;
	
	public MicroGroupResource() {
		
	}
	
	@SuppressWarnings("unchecked")
	@Path("/initMicroGroupService")
	@Consumes(MediaType.APPLICATION_JSON)
	@POST
	public void initMicroGroupService(RequestBody parameters){		
			
		MicroGroupsFinder microgroups_finder = new MicroGroupsFinder(parameters.eps, 
				parameters.sigma, parameters.minpoints, parameters.mgradius, parameters.date_start_time,
				parameters.database_ip, parameters.database_port, parameters.database_name, parameters.time_window_size);
		Integer current_time_window = 0;
		
		context.setAttribute("time_window", (current_time_window));
		context.setAttribute("finder", microgroups_finder);
	}
	
	
	@SuppressWarnings("unchecked")
	@Path("/{mid}")
	@GET	
	@Produces(MediaType.APPLICATION_JSON)
	public MicroGroup<TrajectoryAsSet> getMicroGroupDetailsById(@PathParam("mid") String mid) {
		
		HashMap<Integer, MicroGroup<TrajectoryAsSet>> microGroupSet = (HashMap<Integer, MicroGroup<TrajectoryAsSet>>) context.getAttribute("microgroups");
		MicroGroupsFinder microgroups_finder = (MicroGroupsFinder) context.getAttribute("finder");
		Integer current_time_window = (Integer) context.getAttribute("time_window");
		
		if ( microGroupSet == null ) {
			microgroups_finder = new MicroGroupsFinder();		
			current_time_window = 0;
			try {
				microGroupSet = microgroups_finder.computeTimeWindowMicroGroups(current_time_window);
			} catch (Exception e) {
				throw new MicroGroupException("Could not find micro-groups!");
			}
		}
		context.setAttribute("microgroups", microGroupSet);
		context.setAttribute("time_window", (current_time_window));
		context.setAttribute("finder", microgroups_finder);
				
		if(microGroupSet.containsKey(Integer.parseInt(mid))) return microGroupSet.get(Integer.parseInt(mid));
		else {
			throw new MicroGroupException("Trajectory with id = "+mid+" not found.");
		}
	} 
	
	@SuppressWarnings("unchecked")
	@Path("/log")
	@GET	
	@Produces(MediaType.TEXT_PLAIN)
	public String getLogDetails(){
		
		MicroGroupsFinder microgroups_finder = (MicroGroupsFinder) context.getAttribute("finder");
		
		if ( microgroups_finder == null ) {
			return "There is no log";
		}
		else return microgroups_finder.getLog();
	}
	
	@SuppressWarnings("unchecked")
	@Path("/captureScreen")
	@GET	
	@Produces(MediaType.TEXT_PLAIN)
	public String getCaptureScreen(){
		
		CaptureScreen capture = new CaptureScreen();
		try {
			capture.captureScreen("C:/Users/Ticiana/workspace/mdm-demo-web/kafka_demo/screenshot/result_"+System.currentTimeMillis()+".png");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "sucesso";
	}
	
	
	@SuppressWarnings("unchecked")
	@Path("/all_mg")
	@GET	
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<MicroGroup<TrajectoryAsSet>> getAllMicroGroupDetails(){
		
		HashMap<Integer, MicroGroup<TrajectoryAsSet>> microGroupSet = (HashMap<Integer, MicroGroup<TrajectoryAsSet>>) context.getAttribute("microgroups");
		MicroGroupsFinder microgroups_finder = (MicroGroupsFinder) context.getAttribute("finder");
		Integer current_time_window = (Integer) context.getAttribute("time_window");
		ArrayList<TrajectoryAsSet> outliers = (ArrayList<TrajectoryAsSet>) context.getAttribute("outliers");
		
		if ( microGroupSet == null ) {
			//microgroups_finder = new MicroGroupsFinder();
			current_time_window = 0;
			
			try {
				microGroupSet = microgroups_finder.computeTimeWindowMicroGroups(current_time_window);
				outliers = microgroups_finder.getOutliers();
			} catch (Exception e) {
				e.printStackTrace();
				throw new MicroGroupException("Could not find micro-groups!");
			}
			
			context.setAttribute("outliers", outliers);
			context.setAttribute("microgroups", microGroupSet);
			context.setAttribute("time_window", (current_time_window));
			context.setAttribute("finder", microgroups_finder);
		}	
		
		ArrayList<MicroGroup<TrajectoryAsSet>> microgroup_array = new ArrayList<MicroGroup<TrajectoryAsSet>>(microGroupSet.values());
		return microgroup_array;
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Path("/post_mg")
	public String computeNextTimeWindow() {
		
		HashMap<Integer, MicroGroup<TrajectoryAsSet>> microGroupSet;
		MicroGroupsFinder microgroups_finder = (MicroGroupsFinder) context.getAttribute("finder");
		Integer current_time_window = (Integer) context.getAttribute("time_window");
		ArrayList<TrajectoryAsSet> outliers = (ArrayList<TrajectoryAsSet>) context.getAttribute("outliers");
		
		try {
			current_time_window=current_time_window+1;
			microGroupSet = microgroups_finder.computeTimeWindowMicroGroups(current_time_window);
			outliers = microgroups_finder.getOutliers();
			
			context.setAttribute("outliers", outliers);
			context.setAttribute("microgroups", microGroupSet);
			context.setAttribute("time_window", (current_time_window));
			context.setAttribute("finder", microgroups_finder);
			
		} catch (Exception e) {
			throw new MicroGroupException("Could not find micro-groups!");
		}
		return current_time_window + " current time window sucessfull done!.";
	}
	
	@SuppressWarnings("unchecked")
	@Path("/all_outliers")
	@GET	
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<TrajectoryAsSet> getOutliersDetails(){
		
		
		ArrayList<TrajectoryAsSet> outliers = (ArrayList<TrajectoryAsSet>) context.getAttribute("outliers");
		
		if ( outliers == null ) {
			outliers = new ArrayList<TrajectoryAsSet>();
			context.setAttribute("outliers", outliers);
		}	
		
		return outliers;
	}
	
	@SuppressWarnings("unchecked")
	@Path("/all_clusters")
	@GET	
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<Cluster<TrajectoryAsSet>> getAllClusterDetails(){
		
		ArrayList<Cluster<TrajectoryAsSet>> clusterSet =  (ArrayList<Cluster<TrajectoryAsSet>>) context.getAttribute("clusters");
		MicroGroupsFinder microgroups_finder = (MicroGroupsFinder) context.getAttribute("finder");
		Integer current_time_window = (Integer) context.getAttribute("time_window");
		ArrayList<TrajectoryAsSet> outliers = (ArrayList<TrajectoryAsSet>) context.getAttribute("outliers");
		
		if ( clusterSet == null ) {
			//microgroups_finder = new MicroGroupsFinder();
			current_time_window = 0;
			try {
				clusterSet = microgroups_finder.computeTimeWindowSubTrajectoryClusters(current_time_window);
				outliers = microgroups_finder.getOutliers();
			} catch (Exception e) {
				throw new MicroGroupException("Could not find micro-groups!");
			}
		}
		
		context.setAttribute("clusters", clusterSet);
		context.setAttribute("time_window", (current_time_window));
		context.setAttribute("finder", microgroups_finder);
		context.setAttribute("outliers", outliers);
		
		
		return clusterSet;
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Path("/post_clusters")
	public String computeNextTimeWindowClusters() {
		
		ArrayList<Cluster<TrajectoryAsSet>> clusterSet;
		MicroGroupsFinder microgroups_finder = (MicroGroupsFinder) context.getAttribute("finder");
		Integer current_time_window = (Integer) context.getAttribute("time_window");
		ArrayList<TrajectoryAsSet> outliers = (ArrayList<TrajectoryAsSet>) context.getAttribute("outliers");
		
		try {
			current_time_window=current_time_window+1;
			clusterSet = microgroups_finder.computeTimeWindowSubTrajectoryClusters(current_time_window);
			outliers = microgroups_finder.getOutliers();
			
			context.setAttribute("outliers", outliers);
			context.setAttribute("clusters", clusterSet);
			context.setAttribute("time_window", (current_time_window));
			context.setAttribute("finder", microgroups_finder);
			
		} catch (Exception e) {
			throw new MicroGroupException("Could not find clusters!");
		}
		return current_time_window + " current time window sucessfull done!.";
	}
	

}
