package fr.david.mdm.models;

import java.awt.Color;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;

@XmlRootElement(name="microgroupflock")
public class MicroGroupFlock {

	@XmlElement(required=true)
	private int flock_id;
	@XmlElement
	private double radius;
	@XmlElement(required=true)
	private ArrayList<TrajectoryAsSet> moving_object_array;
	@XmlElement
	private ArrayList<Integer> time_window_array;
	@XmlElement
	private boolean visited;
	@XmlElement(required=true)
	private Color color;
	
	public MicroGroupFlock(int flock_id, double radius, ArrayList<TrajectoryAsSet> moving_object_array, int start_time_window, Color color) {
		this.setFlock_id(flock_id);
		this.radius=radius;
		this.moving_object_array = new ArrayList<TrajectoryAsSet>(moving_object_array);
		this.time_window_array = new ArrayList<Integer>();
		this.time_window_array.add(start_time_window);
		this.setVisited(true);
		this.color=color;
	}
	
	@JsonIgnore
	public double getRadius() {
		return radius;
	}


	public void setRadius(double radius) {
		this.radius = radius;
	}

	@XmlElementWrapper
    @XmlElementRef(name="trajectories")
	public ArrayList<TrajectoryAsSet> getMoving_object_array() {
		return moving_object_array;
	}


	public void setMoving_object_array(ArrayList<TrajectoryAsSet> moving_object_array) {
		this.moving_object_array = moving_object_array;
	}

	@JsonIgnore
	public ArrayList<Integer> getTime_window_array() {
		return time_window_array;
	}


	public void setTime_window_array(ArrayList<Integer> time_window_array) {
		this.time_window_array = time_window_array;
	}

	@XmlElement (name="idMicroGroupFlock")
	public int getFlock_id() {
		return flock_id;
	}


	public void setFlock_id(int flock_id) {
		this.flock_id = flock_id;
	}

	@JsonIgnore
	public boolean isVisited() {
		return visited;
	}


	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	public void addTimeWindow(int timewindow){
		time_window_array.add(timewindow);
	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
