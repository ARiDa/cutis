package fr.david.mdm.models;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

public class MovingObject {

	private int idMO;
	private ArrayList<Coordinate> coord_array;
	private ArrayList<Long> time_array;
	
	public MovingObject(int idMO, ArrayList<Coordinate> coord_array, ArrayList<Long> time_array){
		setIdMO(idMO);
		setCoord_array(coord_array);
		setTime_array(time_array);
	}
	
	public int getIdMO() {
		return idMO;
	}
	public void setIdMO(int idMO) {
		this.idMO = idMO;
	}

	public ArrayList<Coordinate> getCoord_array() {
		return coord_array;
	}

	public void setCoord_array(ArrayList<Coordinate> coord_array) {
		this.coord_array = coord_array;
	}

	public ArrayList<Long> getTime_array() {
		return time_array;
	}

	public void setTime_array(ArrayList<Long> time_array) {
		this.time_array = time_array;
	}
	
}
