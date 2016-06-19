package fr.david.mdm.distances.trajectory;

import java.util.Collection;

public interface DistanceMeter<T> {
	public double distance(T o1, T o2);
	public Collection<T> neighbors(T core, Collection<T> objects, double eps);
	
}
