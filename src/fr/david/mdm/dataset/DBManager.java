package fr.david.mdm.dataset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import com.vividsolutions.jts.geom.Coordinate;

import fr.david.mdm.models.MovingObject;
import fr.david.mdm.tools.Point;
import fr.david.mdm.tools.SegmentData;



public class DBManager {
	private ResultSet rs;
	private Connection c;
	private SegmentData segmentData;
	
	public DBManager(String server_ip, int server_port, String server_database) {
		c = new ConnectionFactory().getConnection(server_ip,server_port,"postgres","postgres",server_database); 
		//c = new ConnectionFactory().getConnection("localhost","postgres","postgres","taxisimples"); 
		//c = new ConnectionFactory().getConnection("localhost","postgres","postgres","tdrive"); 
		//c = new ConnectionFactory().getConnection("200.19.187.72","postgres","aridapostgres12","data_traffic");
	}
	
	public DBManager() {
		c = new ConnectionFactory().getConnection("localhost",5433,"postgres","postgres","tdrive"); 
		//c = new ConnectionFactory().getConnection("localhost","postgres","postgres","taxisimples"); 
		//c = new ConnectionFactory().getConnection("localhost","postgres","postgres","tdrive"); 
		//c = new ConnectionFactory().getConnection("200.19.187.72","postgres","aridapostgres12","data_traffic");
	}
	
	public static double latitudeY(double latitude) {
		return 6378137*java.lang.Math.log(java.lang.Math.tan(java.lang.Math.PI / 4 + 0.5
				* java.lang.Math.toRadians(latitude)));
	}

	public static double longitudeX(double longitude){
		return 6378137 * java.lang.Math.toRadians(longitude);
	}
	

	//pegar pontos na tabela para clusterizacao
	public HashMap<Integer, MovingObject> getTrajectories(Timestamp initalTime, Timestamp finalTime, int count, Integer rateSTraj) 
						{
		HashMap<Integer, MovingObject> movingObjectTimeWindow = new HashMap<Integer, MovingObject>();
		MovingObject mo;
		try{
		
		
		ArrayList<Coordinate> coordinate_list = new ArrayList<Coordinate>();
		ArrayList<Long> time_arrray = new ArrayList<Long>();
		long time_1, time_2;
		Coordinate coord_1, coord_2;
		int i;
		long timeWindowSize = finalTime.getTime()-initalTime.getTime();
		int countRateTrajec = 0;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");;
		
		BufferedWriter buffWrite = new BufferedWriter(new FileWriter(new File("timewindow-"+count+".csv")));
		//buffWrite.write("id;latitude;longitude;time;count"+"\n");
		buffWrite.write("id;id;longitude;latitude"+"\n");
		
		
		/*PARA O DADOS DO TDRIVE */
		 String sql = "select id, latitude, longitude, date_time from taxi "+
				"where date_time >= ?  and date_time <= ? "+
				"order by id,date_time";
		 
		PreparedStatement stmt = c.prepareStatement(sql, rs.TYPE_SCROLL_INSENSITIVE, rs.CONCUR_READ_ONLY);
		stmt.setTimestamp(1, initalTime);
		stmt.setTimestamp(2, finalTime);

		rs = stmt.executeQuery();
		
		String[] line = new String[4];
		String[] ant = new String[4];
		boolean isStopped=false;
		
		if(rs.next()){
			ant[0] = rs.getString(1); //ordem
			ant[1] = rs.getString(2); //latitude
			ant[2] = rs.getString(3); //longitude		
			ant[3] = rs.getString(4); //data_hora  
				
		}
		
		while(rs.next()){	
			
			line[0] = rs.getString(1);
			line[1] = rs.getString(2);
			line[2] = rs.getString(3);
			line[3] = rs.getString(4);  
			
						
			if(!ant[0].contentEquals(line[0]) || differentTraj(ant[3], line[3])){     
				if(!coordinate_list.isEmpty()){
					while(countRateTrajec<=rateSTraj && !isStopped){		
						
						//coord = new Coordinate(longitudeX(Double.parseDouble(ant[2])), latitudeY(Double.parseDouble(ant[1])));
						coord_1 = coordinate_list.get(coordinate_list.size()-1);
						coord_2 = new Coordinate(Double.parseDouble(ant[2]), Double.parseDouble(ant[1]));
						
						long desiredTime = initalTime.getTime()+((timeWindowSize/rateSTraj)*countRateTrajec);
						
						time_1 = time_arrray.get(time_arrray.size()-1);
						time_2 = dateFormat.parse(ant[3]).getTime();
						
						coord_1 = linearInterpolation(coord_1,coord_2,time_1, time_2, desiredTime);
						if(coord_1==null) {
							isStopped=true;
							//System.out.println("Stopped "+ant[0]);
						}
						
						coordinate_list.add(coord_1);
						time_arrray.add(desiredTime);
						
						countRateTrajec++;	
					}
					
					if(!isStopped){
						if(!coordinate_list.isEmpty()){
							buffWrite.write("\n");
							buffWrite.write(ant[0]+" "+coordinate_list.size());
						}
						
						for (i = 0; i < coordinate_list.size(); i++) {		
							//buffWrite.write(ant[0]+";"+coordinate_list.get(i).y+";"+coordinate_list.get(i).x+";"+time_arrray.get(i)+";"+count+"\n");
							buffWrite.write(" "+coordinate_list.get(i).x+" "+coordinate_list.get(i).y+" "+time_arrray.get(i));
							
						}
						mo = new MovingObject(Integer.parseInt(ant[0]), coordinate_list,time_arrray);
						movingObjectTimeWindow.put(Integer.parseInt(ant[0]), mo);
					}
					
					
					//current_traj = new TrajectoryAsSet(Integer.parseInt(ant[0]),Integer.parseInt(ant[0]), time_arrray, new ArrayList<Coordinate>(coordinate_list));
					//traj_array.add(current_traj);
					countRateTrajec = 0;
				}
				
				//jah faz parte da proxima trajetoria
				coordinate_list = new ArrayList<Coordinate>();
				time_arrray = new ArrayList<Long>();
				countRateTrajec = 0;
				 isStopped = false;
				
				ant[0] = line[0];
				ant[1] = line[1];
				ant[2] = line[2];
				ant[3] = line[3];
			}
			else{
		        time_2 = dateFormat.parse(line[3]).getTime();
				while(time_2>(initalTime.getTime()+((timeWindowSize/rateSTraj)*countRateTrajec)) && !isStopped){
					long desiredTime = initalTime.getTime()+((timeWindowSize/rateSTraj)*countRateTrajec);
					
					//coord = new Coordinate(longitudeX(Double.parseDouble(ant[2])), latitudeY(Double.parseDouble(ant[1])));
					coord_1 = new Coordinate(Double.parseDouble(ant[2]), Double.parseDouble(ant[1]));
					coord_2 = new Coordinate(Double.parseDouble(line[2]), Double.parseDouble(line[1]));
					
					time_1 = dateFormat.parse(ant[3]).getTime();
					coord_1 = linearInterpolation(coord_1,coord_2,time_1, time_2, desiredTime);
					if(coord_1==null) {
						isStopped=true;
						//System.out.println("Stopped "+ant[0]);
					}
					
					coordinate_list.add(coord_1);
					time_arrray.add(desiredTime);
					
					countRateTrajec++;		        
					
				}
				ant[0] = line[0];
				ant[1] = line[1];
				ant[2] = line[2];
				ant[3] = line[3];   
				
			}
		}
		if(!coordinate_list.isEmpty()){
			while(countRateTrajec<=rateSTraj && !isStopped){
				coord_1 = coordinate_list.get(coordinate_list.size()-1);
				coord_2 = new Coordinate(Double.parseDouble(ant[2]), Double.parseDouble(ant[1]));
				
				long desiredTime = initalTime.getTime()+((timeWindowSize/rateSTraj)*countRateTrajec);
				
				time_1 = time_arrray.get(time_arrray.size()-1);
				time_2 = dateFormat.parse(ant[3]).getTime();
				
				coord_1 = linearInterpolation(coord_1,coord_2,time_1, time_2, desiredTime);
				if(coord_1==null) {
					isStopped=true;
					//System.out.println("Stopped "+ant[0]);
				}
				
				coordinate_list.add(coord_1);
				time_arrray.add(desiredTime);
				
				countRateTrajec++;	
			}
			
			if(!coordinate_list.isEmpty() && !isStopped){
				buffWrite.write("\n");
				buffWrite.write(ant[0]+" "+coordinate_list.size());
			}
			
			for (i = 0; i < coordinate_list.size() && !isStopped; i++) {		
				//buffWrite.write(ant[0]+";"+coordinate_list.get(i).y+";"+coordinate_list.get(i).x+";"+time_arrray.get(i)+";"+count+"\n");
				buffWrite.write(" "+coordinate_list.get(i).x+" "+coordinate_list.get(i).y+" "+time_arrray.get(i));
				
			}
			//for (i= 0; i < coordinate_list.size() && !isStopped; i++) {					
			//	buffWrite.write(ant[0]+";"+coordinate_list.get(i).y+";"+coordinate_list.get(i).x+";"+time_arrray.get(i)+";"+count+"\n");
			//}
			if(!isStopped){
				mo = new MovingObject(Integer.parseInt(ant[0]), coordinate_list,time_arrray);
				movingObjectTimeWindow.put(Integer.parseInt(ant[0]), mo);
			}
			

			//current_traj = new TrajectoryAsSet(Integer.parseInt(ant[0]),Integer.parseInt(ant[0]),time_arrray, new ArrayList<Coordinate>(coordinate_list));
			//traj_array.add(current_traj);
		}
		
		stmt.close();
	 	rs.close();
	 	
		buffWrite.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return movingObjectTimeWindow;
	 	
	 	//return traj_array;
	}

	
	//pegar pontos na tabela para clusterizacao
	public HashMap<Integer, MovingObject> getTrajectoriesFromDisk(Timestamp initalTime, Timestamp finalTime, int count) 
						{
		HashMap<Integer, MovingObject> movingObjectTimeWindow = new HashMap<Integer, MovingObject>();
		MovingObject mo;
		try{
		
		
		ArrayList<Coordinate> coordinate_list = new ArrayList<Coordinate>();
		ArrayList<Long> time_arrray = new ArrayList<Long>();
		long time_1, time_2;
		Coordinate coord_1, coord_2;
		int i;
		long timeWindowSize = finalTime.getTime()-initalTime.getTime();
		int countRateTrajec = 0;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");;
		
		BufferedWriter buffWrite = new BufferedWriter(new FileWriter(new File("timewindow-"+count+".csv")));
		//buffWrite.write("id;latitude;longitude;time;count"+"\n");
		buffWrite.write("id;id;longitude;latitude"+"\n");
		
		
		/*PARA O DADOS DO TDRIVE */
		 String sql = "select id, latitude, longitude, date_time from taxi "+
				"where date_time >= ?  and date_time <= ? "+
				"order by id,date_time";
		 
		PreparedStatement stmt = c.prepareStatement(sql, rs.TYPE_SCROLL_INSENSITIVE, rs.CONCUR_READ_ONLY);
		stmt.setTimestamp(1, initalTime);
		stmt.setTimestamp(2, finalTime);

		rs = stmt.executeQuery();
		
		String[] line = new String[4];
		String[] ant = new String[4];
		boolean isStopped=false;
		
		if(rs.next()){
			ant[0] = rs.getString(1); //ordem
			ant[1] = rs.getString(2); //latitude
			ant[2] = rs.getString(3); //longitude		
			ant[3] = rs.getString(4); //data_hora  
				
		}
		
		while(rs.next()){	
			
			line[0] = rs.getString(1);
			line[1] = rs.getString(2);
			line[2] = rs.getString(3);
			line[3] = rs.getString(4);  
			
						
			if(!ant[0].contentEquals(line[0]) || differentTraj(ant[3], line[3])){     
				if(!coordinate_list.isEmpty() && coordinate_list.size()>=2){
						
						buffWrite.write("\n");
						buffWrite.write(ant[0]+" "+coordinate_list.size());
												
						for (i = 0; i < coordinate_list.size(); i++) {		
							//buffWrite.write(ant[0]+";"+coordinate_list.get(i).y+";"+coordinate_list.get(i).x+";"+time_arrray.get(i)+";"+count+"\n");
							buffWrite.write(" "+coordinate_list.get(i).x+" "+coordinate_list.get(i).y+" "+time_arrray.get(i));
							
						}
						mo = new MovingObject(Integer.parseInt(ant[0]), coordinate_list,time_arrray);
						movingObjectTimeWindow.put(Integer.parseInt(ant[0]), mo);
				}
				
				//jah faz parte da proxima trajetoria
				coordinate_list = new ArrayList<Coordinate>();
				time_arrray = new ArrayList<Long>();
				
				ant[0] = line[0];
				ant[1] = line[1];
				ant[2] = line[2];
				ant[3] = line[3];
			}
			else{	
					coord_1 = new Coordinate(Double.parseDouble(ant[2]), Double.parseDouble(ant[1]));
					time_1 = dateFormat.parse(ant[3]).getTime();
									
					if(!coordinate_list.isEmpty()){
						Coordinate previous = coordinate_list.get(coordinate_list.size()-1);
						if(previous.x!=coord_1.x || previous.y!=coord_1.y){
							coordinate_list.add(coord_1);
							time_arrray.add(time_1);
						}
						
					}
					else{
						coordinate_list.add(coord_1);
						time_arrray.add(time_1);
					}
					
			}
			ant[0] = line[0];
			ant[1] = line[1];
			ant[2] = line[2];
			ant[3] = line[3];   
			
		}
		if(!coordinate_list.isEmpty()&& coordinate_list.size()>=2){
			
			coord_1 = new Coordinate(Double.parseDouble(ant[2]), Double.parseDouble(ant[1]));
			time_1 = dateFormat.parse(ant[3]).getTime();
							
			if(!coordinate_list.isEmpty()){
				Coordinate previous = coordinate_list.get(coordinate_list.size()-1);
				if(previous.x!=coord_1.x || previous.y!=coord_1.y){
					coordinate_list.add(coord_1);
					time_arrray.add(time_1);
				}
				
			}
			
			buffWrite.write("\n");
			buffWrite.write(ant[0]+" "+coordinate_list.size());
									
			for (i = 0; i < coordinate_list.size(); i++) {		
				//buffWrite.write(ant[0]+";"+coordinate_list.get(i).y+";"+coordinate_list.get(i).x+";"+time_arrray.get(i)+";"+count+"\n");
				buffWrite.write(" "+coordinate_list.get(i).x+" "+coordinate_list.get(i).y+" "+time_arrray.get(i));
				
			}
			mo = new MovingObject(Integer.parseInt(ant[0]), coordinate_list,time_arrray);
			movingObjectTimeWindow.put(Integer.parseInt(ant[0]), mo);
			
		}
		
		stmt.close();
	 	rs.close();
	 	
		buffWrite.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return movingObjectTimeWindow;
	 	
	 	//return traj_array;
	}
	
	public long[] calculateFixedTimestamp(Timestamp initalTime, Timestamp finalTime, Integer rateSTraj){
		long timeWindowSize = finalTime.getTime()-initalTime.getTime();
		
		long[] fixedTimestamp = new long[rateSTraj+1];
		fixedTimestamp[0]=initalTime.getTime();
		fixedTimestamp[rateSTraj]=finalTime.getTime();
		
		for (int i = 1; i < rateSTraj; i++) {
			fixedTimestamp[i] = initalTime.getTime()+((timeWindowSize/rateSTraj)*i);
		}	
		
		return fixedTimestamp;
		
	}
	
	
	//pegar pontos na tabela para clusterizacao
	public HashMap<Integer, MovingObject> getTrajectoriesFixedInperpolation
			(Timestamp initalTime, Timestamp finalTime, int count, Integer rateSTraj) {
		
		
		
		//vector with all timestamp to interpolate the trajectories
		long[] fixedTimestamp = calculateFixedTimestamp(initalTime, finalTime, rateSTraj);
		
		HashMap<Integer, MovingObject> movingObjectTimeWindow = new HashMap<Integer, MovingObject>();
		MovingObject mo;
		try{
				
			ArrayList<Coordinate> coordinate_list = new ArrayList<Coordinate>();
			ArrayList<Long> time_arrray = new ArrayList<Long>();
			long time_1, time_2;
			Coordinate coord_1, coord_2;
			int i;
		
			int countRateTrajec = 0;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");;
		
			BufferedWriter buffWrite = new BufferedWriter(new FileWriter(new File("timewindow-"+count+".csv")));
			//buffWrite.write("id;latitude;longitude;time;count"+"\n");
			buffWrite.write("id;id;longitude;latitude"+"\n");
		
		
			/*PARA O DADOS DO TDRIVE */
			 String sql = "select id, latitude, longitude, date_time from taxi "+
					"where date_time >= ?  and date_time <= ? "+
					"order by id,date_time";
		 
			 PreparedStatement stmt = c.prepareStatement(sql, rs.TYPE_SCROLL_INSENSITIVE, rs.CONCUR_READ_ONLY);
			 stmt.setTimestamp(1, initalTime);
			 stmt.setTimestamp(2, finalTime);

			 rs = stmt.executeQuery();
		
			 String[] line = new String[4];
			 String[] ant = new String[4];
			 boolean isStopped=false;
		
			if(rs.next()){
				ant[0] = rs.getString(1); //ordem
				ant[1] = rs.getString(2); //latitude
				ant[2] = rs.getString(3); //longitude		
				ant[3] = rs.getString(4); //data_hora  
					
			}
		
			while(rs.next()){	
				
				line[0] = rs.getString(1);
				line[1] = rs.getString(2);
				line[2] = rs.getString(3);
				line[3] = rs.getString(4);  
				
							
				if(!ant[0].contentEquals(line[0]) || differentTraj(ant[3], line[3])){     
					if(coordinate_list.size()>=2){
						buffWrite.write("\n");
						buffWrite.write(ant[0]+" "+coordinate_list.size());
						
						for (i = 0; i < coordinate_list.size(); i++) {		
							buffWrite.write(" "+coordinate_list.get(i).x+" "+coordinate_list.get(i).y+" "+time_arrray.get(i));
						}
						mo = new MovingObject(Integer.parseInt(ant[0]), coordinate_list,time_arrray);
						movingObjectTimeWindow.put(Integer.parseInt(ant[0]), mo);
					}
					countRateTrajec = 0;
								
					//jah faz parte da proxima trajetoria
					coordinate_list = new ArrayList<Coordinate>();
					time_arrray = new ArrayList<Long>();
					countRateTrajec = 0;
					isStopped = false;
					
					ant[0] = line[0];
					ant[1] = line[1];
					ant[2] = line[2];
					ant[3] = line[3];
				}
				else{
			        time_2 = dateFormat.parse(line[3]).getTime();
			        coord_1 = new Coordinate(Double.parseDouble(ant[2]), Double.parseDouble(ant[1]));
					time_1 = dateFormat.parse(ant[3]).getTime();
					
					
					while(countRateTrajec<=rateSTraj && time_1>fixedTimestamp[countRateTrajec]) {
						countRateTrajec++;
					}
					
					if(countRateTrajec<=rateSTraj &&  time_1==fixedTimestamp[countRateTrajec]){
						coordinate_list.add(coord_1);
						time_arrray.add(time_1);							
						countRateTrajec++;
					}				
			        
					coord_2 = new Coordinate(Double.parseDouble(line[2]), Double.parseDouble(line[1]));
				
					while(countRateTrajec<=rateSTraj && time_2>=fixedTimestamp[countRateTrajec] && !isStopped){
						Coordinate coord_interpolated = linearInterpolation(coord_1,coord_2,time_1, time_2, fixedTimestamp[countRateTrajec]);
						if(coord_interpolated==null) {
							isStopped=true;
							//System.out.println("Stopped "+ant[0]);
						}
						
						else{
							coordinate_list.add(coord_interpolated);
							time_arrray.add(fixedTimestamp[countRateTrajec]);
						}					
						countRateTrajec++;
					}
					ant[0] = line[0];
					ant[1] = line[1];
					ant[2] = line[2];
					ant[3] = line[3];   
					
				}
			}	
			
			if(!coordinate_list.isEmpty()){
				if(coordinate_list.size()>=2){
					buffWrite.write("\n");
					buffWrite.write(ant[0]+" "+coordinate_list.size());
					
					for (i = 0; i < coordinate_list.size(); i++) {		
						//buffWrite.write(ant[0]+";"+coordinate_list.get(i).y+";"+coordinate_list.get(i).x+";"+time_arrray.get(i)+";"+count+"\n");
						buffWrite.write(" "+coordinate_list.get(i).x+" "+coordinate_list.get(i).y+" "+time_arrray.get(i));
						
					}
					mo = new MovingObject(Integer.parseInt(ant[0]), coordinate_list,time_arrray);
					movingObjectTimeWindow.put(Integer.parseInt(ant[0]), mo);
				}
			}
			
			stmt.close();
		 	rs.close();
		 	
			buffWrite.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return movingObjectTimeWindow;
	 	
	 	//return traj_array;
	}

	
	private boolean differentTraj(String firstDate, String secondDate) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Date date1 = new Date(df.parse(firstDate).getTime());
		Date date2 = new Date(df.parse(secondDate).getTime());
		int countMinutes1, countMinutes2;
		
		if(date1.getDay()==date2.getDay() && date1.getMonth()==date2.getMonth() && date1.getDay()==date2.getDay()){
			countMinutes1 = date1.getHours()*60+date1.getMinutes();
			countMinutes2 = date2.getHours()*60+date2.getMinutes();
			if(countMinutes1+10<countMinutes2) return true;
		}
		
		return false;
	}
	
	
	public Coordinate linearInterpolation(Coordinate c1, Coordinate c2, long t1, long t2, long desiredTime){
		//assumindo velocidade constante
		if((c2.x - c1.x)==0 && (c2.y-c1.y)==0) return null; //null
		if(t1==t2) return null;
		double v = (c2.x-c1.x)/Math.abs(t2-t1);
		double x = c1.x + v*(desiredTime-t1); //valor de x
		
		v = (c2.y-c1.y)/Math.abs(t2-t1);
		double y = c1.y + v*(desiredTime-t1); //valor de y
		
		Coordinate c3 = new Coordinate(x, y);
		//System.out.println("Linear Interpolation: time desired:"+desiredTime +" x: "+x+" y: "+y+" valor deno:"+(c2.x - c1.x));
		return c3;
	}
	
	
	/*Insert the data into the index
	 * */
	public void maintainBTreeIndex(String tableName, HashMap<Integer, ArrayList<Point>> hilbertMap){
		String insert_aux = "insert into "+tableName+" values(";
		String insert;
		
		Statement stmt;
		try {
			stmt = c.createStatement();
			ArrayList<Point> densityPoints;
			
			for (Integer hilbertIndex : hilbertMap.keySet()) {
				densityPoints = hilbertMap.get(hilbertIndex);
				
				for (Point point : densityPoints) {
					insert = insert_aux.concat(hilbertIndex+","+point.time+","+point.tid+");");
					stmt.executeUpdate(insert);
				}
			}
			stmt.executeUpdate("create index "+tableName+"_index on "+tableName+" using btree(cid,time_id);");
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
	
	public void deletePreviusIndex(String tableName, int time_window){
		Statement stmt;
		try {
			stmt = c.createStatement();
			stmt.executeUpdate("drop index if exists "+tableName+"_index;");
			stmt.close();
			stmt = c.createStatement();
			stmt.executeUpdate("delete from index_table_result"+time_window);
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/* Query the neighbors using the index for free movement
	 * */
	public Collection<Integer> queryIndex(ArrayList<Point> candidatePoints, int time_window) throws SQLException {
		 String sql = "select distinct trajid from index_table_result"+time_window+
				" where ";
		 Set<Integer> cid_adjacent_list;
		 
		 for (Point point : candidatePoints) {
			sql = sql.concat("(cid ="+point.cid+"  and time_id = "+point.time+") or ");
			
			//to the adjacent cells of point.cid			
			cid_adjacent_list = segmentData.getAllCidAdjacents(point.cid);
			for (Integer cid_adjacent : cid_adjacent_list) {
				sql = sql.concat("(cid ="+cid_adjacent+"  and time_id = "+point.time+") or ");
			}			
		}
		
		sql = sql.concat(";");
		sql = sql.replace("or ;", ";");
		//System.out.println(sql);
		
		PreparedStatement stmt;
		Set<Integer> collection = new HashSet<Integer>();
				
		stmt = c.prepareStatement(sql, rs.TYPE_SCROLL_INSENSITIVE, rs.CONCUR_READ_ONLY);
		rs = stmt.executeQuery();
		String line;
		
		while(rs.next()){	
			line = rs.getString(1);
			collection.add(Integer.parseInt(line));
		}
		stmt.close();
		rs.close();
		return collection;		
	}
	
	
	public SegmentData getSegmentData() {
		return segmentData;
	}

	public void setSegmentData(SegmentData segmentData) {
		this.segmentData = segmentData;
	}
	
}
