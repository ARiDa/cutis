package fr.david.mdm.longestduration.flock;

import java.util.ArrayList;
import java.util.Calendar;

import com.vividsolutions.jts.geom.Coordinate;

import fr.david.mdm.models.TrajectoryAsSet;

public class TrajectoryEntry {
	protected TrajectoryAsSet trajectory;
    public ArrayList<Integer> neighborsIdxList;
    public boolean isInFlock;

    public TrajectoryEntry(TrajectoryAsSet trajectory) {
        this.trajectory = trajectory;
    }

    public void print() {
        ArrayList<Coordinate> coord_array = trajectory.getCoordArray();
        ArrayList<Long> ts_array = trajectory.getTsArray();
        int size = coord_array.size();
        Coordinate coordinate;
        for (int i=0; i<size;i++) {
        	coordinate = coord_array.get(i);
        	System.out.println("x,y,timeSlice: " + coordinate.x + "," + coordinate.y + "," + ts_array.get(i));
		}
        System.out.println("Is it in a flock? " + this.isInFlock);
        System.out.print("neighbors: ");
        if (this.neighborsIdxList != null) {
            for (int i = 0; i < this.neighborsIdxList.size(); ++i) {
                System.out.print(this.neighborsIdxList.get(i) + "\t");
            }
        }
        System.out.println();
    }
}
