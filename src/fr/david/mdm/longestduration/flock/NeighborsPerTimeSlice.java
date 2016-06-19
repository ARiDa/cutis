package fr.david.mdm.longestduration.flock;
import java.io.BufferedWriter;
import java.util.ArrayList;

public class NeighborsPerTimeSlice implements Comparable {
    public long startTime;
    public long endTime;
    public ArrayList<Integer> neighborsIdxList;
    public boolean isProcessed;
    public ArrayList<Double> heightList;
    public ArrayList<Double> widthList;
    public Double height;
    public Double width;
    public Double shortestExtent;
    public int baseIdx; //object which is center of flock

    public NeighborsPerTimeSlice() {
        this.shortestExtent = 9999999.99;
    }

    public NeighborsPerTimeSlice(long currTime) {
        this.startTime = this.endTime = currTime;
    }

    public boolean print(int minTimeDuration, int timeInterval, BufferedWriter bufWrite, ArrayList<ObjectEntry> objList, double radius) throws Exception {
    	
       if (this.endTime - this.startTime + 1 >= (long)minTimeDuration) { //&& this.shortestExtent >= radius
           
            bufWrite.write("Number of Time Slices: " + (this.endTime - this.startTime + 1) + "\n");
            bufWrite.write("Start and End Time: " + this.startTime + "\t" + this.endTime + "\n");
            bufWrite.write("\n"+"Base " + this.baseIdx + "\n");
            bufWrite.write("Flock Extent:" + this.shortestExtent + "\n");
    	  
            for (int i = 0; i < this.neighborsIdxList.size(); ++i) {
            	 String sql = "insert into flocks values (";
                 sql+=this.startTime+","+this.endTime+","+this.baseIdx+",";
            	sql+=objList.get((int)this.neighborsIdxList.get((int)i).intValue()).id+","+radius+");";
            	bufWrite.write(sql+"\n");
                //bufWrite.write(String.valueOf(objList.get((int)this.neighborsIdxList.get((int)i).intValue()).id) + " ");
            }
            
            return true;
       }
       return false;
    }

    public int compareTo(Object obj) {
        if (obj instanceof NeighborsPerTimeSlice) {
            NeighborsPerTimeSlice slice = (NeighborsPerTimeSlice)obj;
            if (this.shortestExtent > slice.shortestExtent) {
                return -1;
            }
            if (this.shortestExtent < slice.shortestExtent) {
                return 1;
            }
        }
        return 0;
    }
}