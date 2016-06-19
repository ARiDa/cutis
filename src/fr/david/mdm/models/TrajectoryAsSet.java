package fr.david.mdm.models;



import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

@XmlRootElement(name="trajectory")
public class TrajectoryAsSet implements Comparable<TrajectoryAsSet>{
	
	@XmlElement
	protected int uid;
	@XmlElement (required=true)
	protected int tid;
	@XmlElement
	protected LineString geom = null;
	@XmlElement (required=true)
	protected ArrayList<Long> ts_array = null;
	@XmlElement(required=true)
	protected ArrayList<Coordinate> coord_array = null;
	@XmlElement
	protected int timeWindow;
	
	
	
	//for free movement
	public TrajectoryAsSet(int _uid, int _tid, Geometry _geom, ArrayList<Long> time_array,
			ArrayList<Coordinate> coord_array) {
		uid = _uid;
		tid = _tid;
		geom = (LineString) _geom;
		ts_array = time_array;
		this.coord_array = coord_array;
		
	}
	
	
	public TrajectoryAsSet(int _uid, int _tid, ArrayList<Long> time_array,ArrayList<Coordinate> coord_array) {
		uid = _uid;
		tid = _tid;
		ts_array = time_array;
		this.coord_array = coord_array;
	}


	@JsonIgnore
	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	@XmlElement (name="tid")
	public int getTid() {
		return tid;
	}

	public void setTid(int tid) {
		this.tid = tid;
	}

	@JsonIgnore
	public LineString getGeom() {
		return geom;
	}

	public void setGeom(LineString geom) {
		this.geom = geom;
	}

	@JsonIgnore
	public ArrayList<Long> getTsArray() {
		return ts_array;
	}
	
	public Long getTsArrayElement(int i) {
		return ts_array.get(i);
	}

	public void setTsStartElement(int i, Long ts_start) {
		this.ts_array.set(i, ts_start);
	}
	
	public void setCoordElement(int i, Coordinate newCoord) {
		coord_array.set(i, newCoord);
	}
	
	@XmlElement (name="coord_array")
	public ArrayList<Coordinate> getCoordArray() {
		return coord_array;
	}
	

	@Override
	public int compareTo(TrajectoryAsSet o) {
		return Integer.compare(tid, o.tid);
	}
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TrajectoryAsSet))
			return false;
		TrajectoryAsSet co = (TrajectoryAsSet) o;
		if (o != null){
			return getTid()==co.getTid();
		}
		return super.equals(o);
	}
	
	@JsonIgnore
	public int getTimeWindow() {
		return timeWindow;
	}

	public void setTimeWindow(int timewindow) {
		this.timeWindow=timewindow;
	}
	

}
