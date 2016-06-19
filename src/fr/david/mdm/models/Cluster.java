package fr.david.mdm.models;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Cluster<T> {

	@XmlElement(required=true)
	private int idCluster;
	@XmlElement(required=true)
	private Collection<MicroGroup<T>> cluster_members;
	
	private Color color;
	
	public Cluster(int idCluster) {
		this.idCluster=idCluster;
		cluster_members = new ArrayList<MicroGroup<T>>();
		
		Random r_color = new Random();
		this.color=new Color(r_color.nextFloat(), r_color.nextFloat(), r_color.nextFloat());
	}
	
	@XmlElementWrapper
    @XmlElementRef(name="microgroups")
	public Collection<MicroGroup<T>> getCluster_members() {
		return cluster_members;
	}
	
	public void setCluster_members(Collection<MicroGroup<T>> cluster_members) {
		this.cluster_members = cluster_members;
	}

	public void setIdCluster(int idCluster) {
		this.idCluster = idCluster;
	}

	public int getIdCluster(){
		return idCluster;
	}
	
	public Collection<MicroGroup<T>> getClusterMembers(){
		return cluster_members;
	}
	
	public void addClusterMember(MicroGroup<T> newMembers){
		cluster_members.add(newMembers);
	}
	
	public void setColor(Color color){
		this.color=color;
	}
	

	@XmlElement (name="color")
	public Color getColor(){
		return color;
	}
	
}
