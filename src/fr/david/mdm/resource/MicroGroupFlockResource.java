package fr.david.mdm.resource;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import fr.david.mdm.dataset.MicroGroupFlockFinder;
import fr.david.mdm.dataset.MicroGroupsFinder;
import fr.david.mdm.models.MicroGroup;
import fr.david.mdm.models.MicroGroupException;
import fr.david.mdm.models.MicroGroupFlock;
import fr.david.mdm.models.MicroGroupFlockException;
import fr.david.mdm.models.RequestBody;
import fr.david.mdm.models.TrajectoryAsSet;
import fr.david.mdm.tools.CaptureScreen;

@Path("/microgroupflocksonxml")
public class MicroGroupFlockResource {
	
	@Context ServletContext context;
	
	public MicroGroupFlockResource() {
		
	}
	
	@SuppressWarnings("unchecked")
	@Path("/initMicroGroupFlockService")
	@Consumes(MediaType.APPLICATION_JSON)
	@POST
	public void initMicroGroupFlockService(RequestBody parameters) {
		MicroGroupFlockFinder microgroupflocks_finder = new MicroGroupFlockFinder(parameters.eps, 
				parameters.sigma, parameters.minpoints, parameters.mgradius, parameters.duration, parameters.date_start_time,
				parameters.database_ip, parameters.database_port, parameters.database_name, parameters.time_window_size,
			    parameters.size, parameters.radius);
		Integer current_time_window = 0;
		
		context.setAttribute("time_window", (current_time_window));
		context.setAttribute("finder", microgroupflocks_finder);
	}
	

	@SuppressWarnings("unchecked")
	@Path("/log")
	@GET	
	@Produces(MediaType.TEXT_PLAIN)
	public String getLogDetails(){
		
		MicroGroupFlockFinder microgroupflocks_finder = (MicroGroupFlockFinder) context.getAttribute("finder");
		
		if ( microgroupflocks_finder == null ) {
			return "There is no log";
		}
		else return microgroupflocks_finder.getLog();
	}
	
	@SuppressWarnings("unchecked")
	@Path("/all_flock")
	@GET	
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<MicroGroupFlock> getAllMicroGroupDetails(){
		
		HashMap<Integer, MicroGroupFlock> microGroupFlockSet = (HashMap<Integer, MicroGroupFlock>) context.getAttribute("flocks");
		MicroGroupFlockFinder microgroupflocks_finder = (MicroGroupFlockFinder) context.getAttribute("finder");
		Integer current_time_window = (Integer) context.getAttribute("time_window");
		
		if ( microGroupFlockSet == null ) {
			//microgroupflocks_finder = new MicroGroupFlockFinder();
			current_time_window = 0;
			try {
				microGroupFlockSet = microgroupflocks_finder.computeTimeWindowFlocks(current_time_window);
			} catch (Exception e) {
				throw new MicroGroupFlockException("Could not find flocks!");
			}
		}
		context.setAttribute("flocks", microGroupFlockSet);
		context.setAttribute("time_window", (current_time_window));
		context.setAttribute("finder", microgroupflocks_finder);
		
		ArrayList<MicroGroupFlock> microgroup_array = new ArrayList<MicroGroupFlock>(microGroupFlockSet.values());
		return microgroup_array;
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Path("/post_flock")
	public String computeNextTimeWindow() {
		
		HashMap<Integer, MicroGroupFlock> microGroupFlockSet;
		MicroGroupFlockFinder microgroupflocks_finder = (MicroGroupFlockFinder) context.getAttribute("finder");
		Integer current_time_window = (Integer) context.getAttribute("time_window");
		
		try {
			current_time_window=current_time_window+1;
			microGroupFlockSet = microgroupflocks_finder.computeTimeWindowFlocks(current_time_window);
			
			context.setAttribute("flocks", microGroupFlockSet);
			context.setAttribute("time_window", (current_time_window));
			context.setAttribute("finder", microgroupflocks_finder);
			
		} catch (Exception e) {
			throw new MicroGroupFlockException("Could not find flocks!");
		}
		return current_time_window + " current time window sucessfull done!.";
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

}
