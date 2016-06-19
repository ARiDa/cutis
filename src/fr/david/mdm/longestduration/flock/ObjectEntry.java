package fr.david.mdm.longestduration.flock;

import java.util.ArrayList;

public class ObjectEntry {
    public String id;
    public ArrayList<TrajectoryEntry> trajectoryList;

   
    public ObjectEntry(String id_, ArrayList<TrajectoryEntry> trajectoryList_) {
        this.id = id_;
        this.trajectoryList = trajectoryList_;
    }

    public void print() {
        System.out.println("id: " + this.id);
        for (int i = 0; i < this.trajectoryList.size(); ++i) {
            this.trajectoryList.get(i).print();
        }
    }
}