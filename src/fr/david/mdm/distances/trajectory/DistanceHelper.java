package fr.david.mdm.distances.trajectory;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import fr.david.mdm.distances.trajectory.DistanceHelper;

public abstract class DistanceHelper<T> implements DistanceMeter<T> {

	abstract public double distance(T o1, T o2);

	/** 
	 * Helper function to find all the neighbors of a given core object.
	 * In this implementation no optimization is used, and the search procedure is 
	 * linear in the number of objects (thus, quadratic complexity for clustering).
	 * 
	 * @see fr.david.kdd.optics.DistanceMeter#neighbors(java.lang.Object, java.util.Collection, double)
	 */
	public Collection<T> neighbors(T core, Collection<T> objects, double eps) {
		Vector<T> neighbors = new Vector<T>();
		final Hashtable<T, Double> distances = new Hashtable<T, Double>();
		
		for(Iterator<T> i = objects.iterator(); i.hasNext(); ){
			T o = i.next();
			if((!o.equals(core))){
				double dist = distance(o, core);
				//System.out.println("Core Object:"+ ((TrajectoryAsSet) core).getTid()+" distance to:"+((TrajectoryAsSet) o).getTid()+" is: "+dist);
				if (dist <=eps){
					distances.put(o, dist);
					neighbors.add(o);
				}
			}
		}
		
		Collections.sort(neighbors, new Comparator<T>(){
			public int compare(T o1, T o2) {
				return distances.get(o1).compareTo(distances.get(o2));
			}
		});
		return neighbors;
	}
}
