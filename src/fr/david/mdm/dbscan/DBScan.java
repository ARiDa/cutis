package fr.david.mdm.dbscan;

import fr.david.mdm.dataset.DBManager;
import fr.david.mdm.distances.trajectory.DistanceHelper;
import fr.david.mdm.distances.trajectory.DistanceMeter;
import fr.david.mdm.models.TrajectoryAsSet;
import fr.david.mdm.tools.Point;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
import com.vividsolutions.jts.geom.Coordinate;


public class DBScan<T> {

	//static final Logger log = Logger.getLogger(DBScan.class);
	protected double infinity = Double.POSITIVE_INFINITY;
	private DistanceMeter<T> dm;
	private Collection<T> objects;
	public HashMap<T, ClusterObjectDBScan<T>> cos;
	public int numberClusters;
	public String resultFile;
	
	//TODO to hilbert curve
	private DBManager dbmanager;
	private int time_window_id;
	private HashMap<Integer, ArrayList<Point>> candidates; //all trajectories with hilbertInformation
	
	public DBScan(DistanceHelper<T> dm, Collection<T> objects, DBManager db) throws IOException {
		this.dm = dm;
		this.objects=objects;
		this.dbmanager=db;
	}
	
	/*Without index in the clustering algorithm*/
	public DBScan(DistanceHelper<T> dm, Collection<T> objects) throws IOException {
		this.dm = dm;
		this.objects=objects;
	}
	
	
	public static Vector<TrajectoryAsSet> loadPoints(String f){
		String l;
		String[] line;		
		Double pX, pY;
		Vector<TrajectoryAsSet> traj_array = new Vector<TrajectoryAsSet>();
		TrajectoryAsSet current_traj;
		ArrayList<Coordinate> coordinate_list = new ArrayList<Coordinate>();
		ArrayList<Long> time_arrray= new ArrayList<Long>();
		BufferedReader br;
		String trajId="";
		long time;
		//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");;
		int index=1;

		try {
			br = new BufferedReader(new FileReader(f));
			l = br.readLine(); //header		
			
			if((l = br.readLine())!=null){
				line = l.split(" ");
				//pY = latitudeY(Double.parseDouble(line[0]));
				//pX = longitudeX(Double.parseDouble(line[1])); 

				
				while(index<=Math.ceil(line.length/4)){
					trajId = line[0];
					pY = Double.parseDouble(line[3*(index-1)+3]);
					pX = Double.parseDouble(line[3*(index-1)+2]); 
					
					if(!pY.isNaN() && !pX.isNaN()){
						//time = dateFormat.parse(line[3]).getTime();
						time = Long.parseLong(line[3*(index-1)+4]);
						coordinate_list.add(new Coordinate(pX,pY));
						time_arrray.add(time);

					}
					index++;
				}
					
			}
			
			while((l = br.readLine())!=null){	

				line = l.split(" ");
				//pY = latitudeY(Double.parseDouble(line[0]));
				//pX = longitudeX(Double.parseDouble(line[1])); 

				index=1;
				while(index<=(line.length/4)){
					pY = Double.parseDouble(line[3*(index-1)+3]);
					pX = Double.parseDouble(line[3*(index-1)+2]);
	
					if(!pY.isNaN() && !pX.isNaN()){
						
						if(trajId.contentEquals(line[0])){
							//pertencem a mesma trajetoria 
							//time = dateFormat.parse(line[3]).getTime();
							time = Long.parseLong(line[3*(index-1)+4]);
							
							coordinate_list.add(new Coordinate(pX,pY));
							time_arrray.add(time);
							
						}					
						else{
							//termina a trajetoria anterior
							current_traj = new TrajectoryAsSet(Integer.parseInt(trajId),Integer.parseInt(trajId), time_arrray, coordinate_list);
							traj_array.add(current_traj);
							
							//dados da trajetoria corrente
							coordinate_list = new ArrayList<Coordinate>();
							time_arrray = new ArrayList<Long>();
							
							trajId = line[0];
							//time = dateFormat.parse(line[3]).getTime();
							time = Long.parseLong(line[3*(index-1)+4]);
							
							coordinate_list.add(new Coordinate(pX,pY));
							time_arrray.add(time);
						}				
					}
					index++;
				}
			}
		br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 

		return traj_array;
	}
	
	public void dbscan(double eps, int minPoints,String resultFile){
		ArrayList<Long> time_array;
		ArrayList<Coordinate> coord_array;
		
		try {
			
			//to store clusters on file
			BufferedWriter buffWrite = new BufferedWriter(new FileWriter(new File(resultFile))); 
			buffWrite.write("id;latitude;longitude;time;cluster;iscore"+"\n");
			
			//keep the moving object trajectory as a clusterObject
			//HashMap<T, ClusterObjectDBScan<T>> cos = new HashMap<T, ClusterObjectDBScan<T>>();
			cos = new HashMap<T, ClusterObjectDBScan<T>>();
			
		//	log.debug("Creating the ClusterObjects...");
			for (T t : objects) {
				cos.put(t, new ClusterObjectDBScan<T>(t));
			}
			
			int cluster = -1;
			T t;
						

			while((t = getUnvisitedPoint(cos)) != null){
			//	log.debug(((TrajectoryAsSet) t).getTid() + ": Visiting the object");
				
				ClusterObjectDBScan<T> current = cos.get(t);
				current.setProcessed(true);
			//	log.debug("Retrieving its ClusterObject Neighbors");

				
				//TODO to hilbert curve
				Collection<T> neighbors = neighbors(candidates.get(((TrajectoryAsSet) t).getTid()), t, eps);
				//Collection<T> neighbors = dm.neighbors(t,objects,eps);
				
				//System.out.println("Core Object:"+ ((TrajectoryAsSet) t).getTid()+" #neighbours:"+neighbors.size());
				
				if(neighbors.size() < minPoints){
					current.setIdCluster(-1);
				//	log.debug(((TrajectoryAsSet) current.originalObject).getTid()+": isCore="+current.isCore+" cluster="+current.getIdCluster());
					
					//put on the csv the moving object core
					/*time_array = ((TrajectoryAsSet)current.originalObject).getTsArray();
					coord_array = ((TrajectoryAsSet)current.originalObject).getCoordArray();
					
					for (int i = 0; i < coord_array.size(); i++) {
						buffWrite.write(((TrajectoryAsSet) current.originalObject).getTid()+";"+coord_array.get(i).y+";"
								+coord_array.get(i).x+";"+time_array.get(i)+";"+current.getIdCluster()+";"+current.isCore+"\n");
					}*/					
					
				} else {
					
					cluster++;					
					current.setIdCluster(cluster);
					current.setIsCore(true);
					current.reachabilityDistance=0;
					
				//	log.debug(((TrajectoryAsSet) current.originalObject).getTid()+": isCore="+current.isCore+" cluster="+current.getIdCluster());
					
					//put on the csv the moving object core
					time_array = ((TrajectoryAsSet)current.originalObject).getTsArray();
					coord_array = ((TrajectoryAsSet)current.originalObject).getCoordArray();
					
					for (int i = 0; i < coord_array.size(); i++) {
						buffWrite.write(((TrajectoryAsSet) current.originalObject).getTid()+";"+coord_array.get(i).y+";"
								+coord_array.get(i).x+";"+time_array.get(i)+";"+current.getIdCluster()+";"+current.isCore+"\n");
					}					
					
					for (T t2 : neighbors) {
						//TODO set reachability distance
						
						if(!cos.get(t2).isProcessed() || cos.get(t2).getIdCluster()==-1) cos.get(t2).setReachabilityDistance(dm.distance(t, t2));
					}
					
					//expande os vizinhos passando as trajetorias como objetos de clusters
					expandCluster(neighbors, cluster, eps, minPoints, t,buffWrite); //buffWrite
				}
				
					
			}
			
			for (T traj : objects) {
				ClusterObjectDBScan<T> current = cos.get(traj);
				if(current.idCluster==-1){
					time_array = ((TrajectoryAsSet)current.originalObject).getTsArray();
					coord_array = ((TrajectoryAsSet)current.originalObject).getCoordArray();
					
					for (int i = 0; i < coord_array.size(); i++) {
						buffWrite.write(((TrajectoryAsSet) current.originalObject).getTid()+";"+coord_array.get(i).y+";"
								+coord_array.get(i).x+";"+time_array.get(i)+";"+current.getIdCluster()+";"+current.isCore+"\n");
					}
				}
			}
			
			buffWrite.close();
			numberClusters = cluster+1;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}
	}
	
	private void expandCluster(Collection<T> objectsNeigh, int cluster, double eps, int minPoints, T previous, BufferedWriter buffWrite) throws IOException, SQLException{
		
		ArrayList<T> neighbors = new ArrayList<T>();		
		neighbors.addAll(objectsNeigh);
		
		T t2;
		boolean belongs;
		Collection<T> nextNeighbors;
		ArrayList<Long> time_array;
		ArrayList<Coordinate> coord_array;
		
		for (int i = 0; i < neighbors.size(); i++) {
			T t = neighbors.get(i);
			ClusterObjectDBScan<T> next = cos.get(t);
			
			//expands each neighbor if it is not processed
			if(!next.isProcessed()){
				next.setProcessed(true);
				
				//TODO to hilbert curve
				//nextNeighbors = dm.neighbors(t,objects, eps);
				nextNeighbors = neighbors(candidates.get(((TrajectoryAsSet) t).getTid()), t, eps);
				
				if(nextNeighbors.size() >= minPoints) {
					next.setIsCore(true);
					
					for(T t1: nextNeighbors){
						
						belongs = false;
						for (int j = 0; j < neighbors.size() && !belongs; j++) {
							t2 = neighbors.get(j);
							//case nextNeighbor is already on neighbor set
							if(((TrajectoryAsSet)t1).getTid()==((TrajectoryAsSet)t2).getTid()) belongs=true;
						}
						if(!belongs){
							neighbors.add(t1);
							//TODO set reachability distance
							if(!cos.get(t1).isProcessed() || cos.get(t1).getIdCluster()==-1) cos.get(t1).setReachabilityDistance(dm.distance(t, t1));
						}
					}	
				}
			}
			if(next.idCluster==-1){
				next.setIdCluster(cluster);
								
			//	log.debug(((TrajectoryAsSet) next.originalObject).getTid()+": isCore="+next.isCore+" cluster="+next.getIdCluster()+" distance: "+dm.distance(t, previous));
				
				//put on the csv the moving object core
				time_array = ((TrajectoryAsSet)next.originalObject).getTsArray();
				coord_array = ((TrajectoryAsSet)next.originalObject).getCoordArray();
				
				//put on the csv the clusters
				for (int j = 0; j < coord_array.size(); j++) {
					buffWrite.write(((TrajectoryAsSet) next.originalObject).getTid()+";"+coord_array.get(j).y+";"
						+coord_array.get(j).x+";"+time_array.get(j)+";"+next.getIdCluster()+";"+next.isCore+"\n");
				}
			}
								
		}
	}


	private T getUnvisitedPoint(HashMap<T, ClusterObjectDBScan<T>> cos) {

		Collection<ClusterObjectDBScan<T>> values = cos.values();
		
		for (ClusterObjectDBScan<T> clusterObjectDBScan : values) {
			if(!clusterObjectDBScan.isProcessed()) return clusterObjectDBScan.getOriginalObject();
		}			

		return null;
	}
	
	public double getAverageSSQ(){
		double ssq = 0;
		int count=0;
		
		for (ClusterObjectDBScan<T> o : cos.values()) {
			if(o.getIdCluster()!=-1){
				count++;
				ssq+= Math.pow(o.reachabilityDistance,2); 
			}
			
		}
		
		return ssq/count;
	}
	
	//TODO to hilbert curve
	//search for neighbors using index
	public Collection<T> neighbors(ArrayList<Point> candidatePoints, T candidate, double eps) throws SQLException{
		
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
		
	private T getTObject(Integer tid) {
			
		for (T t : objects) {
			if(((TrajectoryAsSet) t).getTid()==tid) return  t;
		}
		return null;
	}
	
	//TODO to hilbert curve
	public void setTimeWindow(int time_window){
		this.time_window_id=time_window;
	}
	
	//TODO to hilbert curve
	public void setCandidates(HashMap<Integer, ArrayList<Point>> candidates) {
		this.candidates = candidates;
	}
	
	//TODO to hilbert curve
	public HashMap<Integer, ArrayList<Point>> getCandidates() {
		return candidates;
	}

	public HashMap<T, ClusterObjectDBScan<T>> getCos() {
		return cos;
	}
	
	
	
}
