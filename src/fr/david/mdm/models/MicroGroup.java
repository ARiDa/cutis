package fr.david.mdm.models;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;

import fr.david.mdm.Enum.Evolution;

@XmlRootElement(name="microgroup")
public class MicroGroup<T> {

	@XmlElement(required=true)
	private int idMicroGroup;
	@XmlElement(required=true)
	private ArrayList<T> objects;
	@XmlElement
	private T representative;
	@XmlElement
	private double vote;
	@XmlElement
	private int size;
	@XmlElement
	private boolean visitedCluster;
	@XmlElement(required=true)
	private Color color;
	@XmlElement(required=true)
	private Evolution evolution;
	@XmlElement
	private double radius;
	
	public MicroGroup(int id) {
		objects = new  ArrayList<T>();
		setIdMicroGroup(id);
		setVote(Double.NEGATIVE_INFINITY);
		setSize(0);
		setVisitedCluster(false);	
		this.evolution=Evolution.APPEARS;
	}
	
	public void addMicroGroupObject(T microgroupobject){
		objects.add(microgroupobject);
	}

	@XmlElement (name="idMicroGroup")
	public int getIdMicroGroup() {
		return idMicroGroup;
	}

	public  ArrayList<T> getAllMicroGroupObject() {
		return objects;
	}
	
	public void setIdMicroGroup(int idMicroGroup) {
		this.idMicroGroup = idMicroGroup;
	}

	public T getRepresentative() {
		return representative;
	}

	public void setRepresentative(T representative) {
		this.representative = representative;
	}

	@JsonIgnore
	public double getVote() {
		return vote;
	}

	public void setVote(double vote) {
		this.vote = vote;
	}

	@JsonIgnore
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public void removeMicroGroupObject(T mgo){
		Collection<T> o = new Vector<T>();
		o.addAll(objects);
		
		for (T t : o) {
			if(mgo.equals(t)) {
				objects.remove(t);
				return;
			}
		}
	}

	public boolean isVisitedCluster() {
		return visitedCluster;
	}

	public void setVisitedCluster(boolean visited) {
		this.visitedCluster = visited;
	}
	
	public void setColor(Color color){
		this.color=color;
	}
	
	@XmlElement (name="color")
	public Color getColor(){
		return color;
	}
	
	public Evolution getEvolution(){
		return evolution;
	}
	
	public void setEvolution(Evolution evolution){
		this.evolution=evolution;
	}
	
	@XmlElementWrapper
    @XmlElementRef(name="trajectories")
	public ArrayList<T> getObjects() {
		return objects;
	}

	public void setObjects(ArrayList<T> objects) {
		this.objects = objects;
	}
	
	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	
}
