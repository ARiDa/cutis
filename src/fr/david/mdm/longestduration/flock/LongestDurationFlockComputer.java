package fr.david.mdm.longestduration.flock;

import fr.david.mdm.dataset.MicroGroupsFinder;
import fr.david.mdm.distances.trajectory.DistanceHelper;
import fr.david.mdm.distances.trajectory.DistanceMeter;
import fr.david.mdm.models.TrajectoryAsSet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import com.vividsolutions.jts.geom.Coordinate;


/**
 * Computing Longest Duration Flocks in Trajectory Data
 * @author Ticiana
 *
 */

public class LongestDurationFlockComputer {
    private double RADIUS;
    private int MIN_POINTS;
    private int MIN_TIME_SLICES;
    private int TIME_INTERVAL_IN_INPUT_FILE;
    private DistanceMeter<TrajectoryAsSet> dm;
    
    public LongestDurationFlockComputer(double radius, int min_points, int min_time_slides, int time_interval_in_input_file,
    		DistanceHelper<TrajectoryAsSet> dm) {
    	 RADIUS = radius;
         MIN_POINTS = min_points;
         MIN_TIME_SLICES = min_time_slides;
         TIME_INTERVAL_IN_INPUT_FILE = time_interval_in_input_file;
         this.dm = dm;
	}

    public void findAllFlocks(HashMap<Integer, Vector<TrajectoryAsSet>> objects, String outputFile) throws Exception {
       
        //store the trajectories for all time windows
        ArrayList<ObjectEntry> objList = new ArrayList<ObjectEntry>();
       
        FileWriter outFile = new FileWriter(outputFile);
        BufferedWriter bufWrite = new BufferedWriter(outFile);
              
        //store the time windows' trajectories of one moving object 
        ArrayList<TrajectoryEntry> trajectoryEntryList;
        
        for (Integer traj_id : objects.keySet()) {
        	trajectoryEntryList =  new ArrayList<TrajectoryEntry>();
        	for (TrajectoryAsSet trajectory : objects.get(traj_id))             	
        		trajectoryEntryList.add(new TrajectoryEntry(trajectory));
        	objList.add(new ObjectEntry(String.valueOf(traj_id), trajectoryEntryList));
    	}
		       
        //neighbors trajectories in all time windows from one moving object 
        ArrayList<NeighborsPerTimeSlice> neighborsPerTime = null;
        //neighbors trajectories in all time window
        ArrayList<NeighborsPerTimeSlice> neighborsPerTimeAll = new ArrayList<NeighborsPerTimeSlice>();
        int objCount = objList.size();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < objCount; ++i) {
        	//for each moving object
            neighborsPerTime = computeSpatialRange((ObjectEntry)objList.get(i), objList);
            mergeAdjSlices(neighborsPerTime);
            markObjsInFlock(objList, neighborsPerTime, i);
            computeExtentOfSegment(objList, neighborsPerTime, i);
            System.out.println("Object "+i+" has "+neighborsPerTime.size());
            //the final result is in neighborsPerTimeAll
            for (int j = 0; j < neighborsPerTime.size(); ++j) {
                neighborsPerTimeAll.add(neighborsPerTime.get(j));
            }
            
        }
        Collections.sort(neighborsPerTimeAll);
        int flockCnt = 0;
        for (int i2 = 0; i2 < neighborsPerTimeAll.size(); ++i2) {
            if (!((NeighborsPerTimeSlice)neighborsPerTimeAll.get(i2)).print(MIN_TIME_SLICES, TIME_INTERVAL_IN_INPUT_FILE, bufWrite, objList, RADIUS)) continue;
            ++flockCnt;
        }
        System.out.println(flockCnt);
        bufWrite.close();
        System.out.println("running time: " + (System.currentTimeMillis() - startTime));
    }

    public ArrayList<NeighborsPerTimeSlice> computeSpatialRange(ObjectEntry currObj, ArrayList<ObjectEntry> objList) {
        int j;
        //neighbors trajectories in all time windows from one moving object 
        ArrayList<NeighborsPerTimeSlice> neighborsPerTime = new ArrayList<NeighborsPerTimeSlice>();
        for (j = 0; j < currObj.trajectoryList.size(); ++j) {
            long currTimeSlice = currObj.trajectoryList.get((int)j).trajectory.getTimeWindow();
            neighborsPerTime.add(new NeighborsPerTimeSlice(currTimeSlice));
            if (currObj.trajectoryList.get((int)j).isInFlock) continue;
            currObj.trajectoryList.get((int)j).neighborsIdxList = new ArrayList();
            neighborsPerTime.get((int)j).neighborsIdxList = new ArrayList();
            
          //for all moving object
            for (int k = 0; k < objList.size(); ++k) {
                if (currObj.id.equals(objList.get((int)k).id)) continue;
                //for moving object k, scan all trajectories in all time windows
                for (int m = 0; m < objList.get((int)k).trajectoryList.size(); ++m) {
                    long otherTimeSlice = objList.get((int)k).trajectoryList.get((int)m).trajectory.getTimeWindow();
                    if (currTimeSlice != otherTimeSlice || Math.pow(dm.distance(currObj.trajectoryList.get((int)j).trajectory, objList.get((int)k).trajectoryList.get((int)m).trajectory),2) > RADIUS * RADIUS) continue;
                    currObj.trajectoryList.get((int)j).neighborsIdxList.add(new Integer(k));
                    neighborsPerTime.get((int)j).neighborsIdxList.add(new Integer(k));
                }
            }
        }
        for (j = 0; j < neighborsPerTime.size(); ++j) {
            if (neighborsPerTime.get((int)j).neighborsIdxList != null) continue;
            neighborsPerTime.remove(j);
            --j;
        }
        return neighborsPerTime;
    }

    //neighbors from one moving object in all time window
    public void mergeAdjSlices(ArrayList<NeighborsPerTimeSlice> neighborsPerTime) {
        int i;
        int toProcessCount = 0;
        for (i = 0; i < neighborsPerTime.size(); ++i) {
            boolean matchedCount = false;
            if (i == neighborsPerTime.size() - 1) {
                if (neighborsPerTime.get((int)i).neighborsIdxList.size() < MIN_POINTS - 1) {
                    neighborsPerTime.remove(i);
                    continue;
                }
                if (i != 0 && neighborsPerTime.get((int)(i - 1)).endTime == neighborsPerTime.get((int)i).endTime) {
                    neighborsPerTime.remove(i);
                    continue;
                }
                neighborsPerTime.get((int)i).isProcessed = true;
                continue;
            }
            //until find a density neighborhood
            if (neighborsPerTime.get((int)i).neighborsIdxList.size() < MIN_POINTS - 1) {
                neighborsPerTime.remove(i);
                --i;
                continue;
            }
            //density neighborhood in i, check if it continuos in i+1
            if (neighborsPerTime.get((int)(i + 1)).neighborsIdxList.size() < MIN_POINTS - 1) {
                if (i != 0 && neighborsPerTime.get((int)(i - 1)).endTime == neighborsPerTime.get((int)i).endTime) {
                    neighborsPerTime.remove(i);
                    continue;
                }
                neighborsPerTime.get((int)i).isProcessed = true;
                continue;
            }
            //find a start flock from i, i+1,...
            //"i" is one time window before "i+1"
            if (neighborsPerTime.get((int)i).endTime + 1 == neighborsPerTime.get((int)(i + 1)).startTime || neighborsPerTime.get((int)i).endTime >= neighborsPerTime.get((int)(i + 1)).startTime) {
            	//the current flock
                NeighborsPerTimeSlice tempNPTS = new NeighborsPerTimeSlice();
                tempNPTS.startTime = neighborsPerTime.get((int)i).startTime;
                tempNPTS.endTime = neighborsPerTime.get((int)(i + 1)).endTime;
                tempNPTS.neighborsIdxList = new ArrayList();
                block1 : for (int j = 0; j < neighborsPerTime.get((int)i).neighborsIdxList.size(); ++j) {
                    for (int k = 0; k < neighborsPerTime.get((int)(i + 1)).neighborsIdxList.size(); ++k) {
                        if (!neighborsPerTime.get((int)i).neighborsIdxList.get(j).equals(neighborsPerTime.get((int)(i + 1)).neighborsIdxList.get(k))) continue;
                        tempNPTS.neighborsIdxList.add(neighborsPerTime.get((int)i).neighborsIdxList.get(j)); //os obj que continuam vizinhos do obj corrente em i+1
                        continue block1;
                    }
                }
                if (tempNPTS.neighborsIdxList.size() >= MIN_POINTS - 1) {
                    neighborsPerTime.remove(i);
                    neighborsPerTime.add(i, tempNPTS); //soh considera pertencente ao flock, os objetos que estao na intersecao entre i e i+1
                    continue;
                }
                if (i != 0 && neighborsPerTime.get((int)(i - 1)).endTime == neighborsPerTime.get((int)i).endTime) {
                    neighborsPerTime.remove(i);
                    continue;
                }
                neighborsPerTime.get((int)i).isProcessed = true;
                continue;
            }
            if (i != 0 && neighborsPerTime.get((int)(i - 1)).endTime == neighborsPerTime.get((int)i).endTime) {
                neighborsPerTime.remove(i);
                continue;
            }
            neighborsPerTime.get((int)i).isProcessed = true;
        }
        for (i = 0; i < neighborsPerTime.size(); ++i) {
            if (neighborsPerTime.get((int)i).isProcessed) continue;
            ++toProcessCount;
        }
        if (toProcessCount > 1) {
            mergeAdjSlices(neighborsPerTime);
        }
    }

    public void markObjsInFlock(ArrayList<ObjectEntry> objList, ArrayList<NeighborsPerTimeSlice> neighborsPerTime, int baseIdx) {
        for (int i = 0; i < neighborsPerTime.size(); ++i) {
            if (neighborsPerTime.get((int)i).endTime - neighborsPerTime.get((int)i).startTime + 1 < (long)MIN_TIME_SLICES) continue;
            for (int j = 0; j < neighborsPerTime.get((int)i).neighborsIdxList.size(); ++j) {
                int currNeighborIdx = neighborsPerTime.get((int)i).neighborsIdxList.get(j);
                for (int k = 0; k < objList.get((int)currNeighborIdx).trajectoryList.size(); ++k) {
                    TrajectoryEntry trajectoryEnt = objList.get((int)currNeighborIdx).trajectoryList.get(k);
                    if (trajectoryEnt.trajectory.getTimeWindow() > neighborsPerTime.get((int)i).endTime || trajectoryEnt.trajectory.getTimeWindow() < neighborsPerTime.get((int)i).startTime) continue;
                    trajectoryEnt.isInFlock = true;
                }
            }
        }
    }

    public void computeExtentOfSegment(ArrayList<ObjectEntry> objList, ArrayList<NeighborsPerTimeSlice> neighborsPerTime, int baseIdx) {
        double maxX;
        double extent;
        ArrayList<Double> xList;
        double minX;
        int j;
        double minY;
        double maxY;
        int k;
        ArrayList<Double> yList;
        double shortestExtent = 0.0;
        for (j = 0; j < neighborsPerTime.size(); ++j) {
            neighborsPerTime.get((int)j).baseIdx = Integer.parseInt(objList.get(baseIdx).id);
            
            neighborsPerTime.get((int)j).heightList = new ArrayList();
            neighborsPerTime.get((int)j).widthList = new ArrayList();
            //scan all objects in a flock having as a center one moving object
            for (int i = 0; i < neighborsPerTime.get((int)j).neighborsIdxList.size(); ++i) {
                xList = new ArrayList();
                yList = new ArrayList();
                int currIdx = neighborsPerTime.get((int)j).neighborsIdxList.get(i);
                ObjectEntry currObj = objList.get(currIdx);
                for (k = 0; k < currObj.trajectoryList.size(); ++k) {
                    long currTime = currObj.trajectoryList.get((int)k).trajectory.getTimeWindow();
                    if (currTime < neighborsPerTime.get((int)j).startTime || currTime > neighborsPerTime.get((int)j).endTime) continue;
                    
                    Coordinate c_start = currObj.trajectoryList.get((int)k).trajectory.getCoordArray().get(0);
                    int size = currObj.trajectoryList.get((int)k).trajectory.getCoordArray().size();
                    Coordinate c_end = currObj.trajectoryList.get((int)k).trajectory.getCoordArray().get(size-1);
                    xList.add(c_start.x);
                    xList.add(c_end.x);
                    yList.add(c_start.y);
                    yList.add(c_end.y);                    
                }
                maxX = minX = ((Double)xList.get(0)).doubleValue();
                for (k = 1; k < xList.size(); ++k) {
                    if (maxX < (Double)xList.get(k)) {
                        maxX = (Double)xList.get(k);
                        continue;
                    }
                    if (minX <= (Double)xList.get(k)) continue;
                    minX = (Double)xList.get(k);
                }
                maxY = minY = ((Double)yList.get(0)).doubleValue();
                for (k = 1; k < yList.size(); ++k) {
                    if (maxY < (Double)yList.get(k)) {
                        maxY = (Double)yList.get(k);
                        continue;
                    }
                    if (minY <= (Double)yList.get(k)) continue;
                    minY = (Double)yList.get(k);
                }
                neighborsPerTime.get((int)j).heightList.add(maxY - minY);
                neighborsPerTime.get((int)j).widthList.add(maxX - minX);
                double d = extent = maxY - minY > maxX - minX ? maxY - minY : maxX - minX;
                if (i != 0 && shortestExtent <= extent) continue;
                shortestExtent = extent;
            }
            neighborsPerTime.get((int)j).shortestExtent = shortestExtent;
        }
        ObjectEntry baseObj = objList.get(baseIdx);
        for (j = 0; j < neighborsPerTime.size(); ++j) {
            xList = new ArrayList<Double>();
            yList = new ArrayList<Double>();
            for (int i = 0; i < baseObj.trajectoryList.size(); ++i) {
                if (baseObj.trajectoryList.get((int)i).trajectory.getTimeWindow() < neighborsPerTime.get((int)j).startTime || baseObj.trajectoryList.get((int)i).trajectory.getTimeWindow() > neighborsPerTime.get((int)j).endTime) continue;
                
                Coordinate c_start = baseObj.trajectoryList.get((int)i).trajectory.getCoordArray().get(0);
                int size = baseObj.trajectoryList.get((int)i).trajectory.getCoordArray().size();
                Coordinate c_end = baseObj.trajectoryList.get((int)i).trajectory.getCoordArray().get(size-1);
                xList.add(c_start.x);
                xList.add(c_end.x);
                yList.add(c_start.y);
                yList.add(c_end.y);
            }
            maxX = minX = ((Double)xList.get(0)).doubleValue();
            for (k = 1; k < xList.size(); ++k) {
                if (maxX < (Double)xList.get(k)) {
                    maxX = (Double)xList.get(k);
                    continue;
                }
                if (minX <= (Double)xList.get(k)) continue;
                minX = (Double)xList.get(k);
            }
            maxY = minY = ((Double)yList.get(0)).doubleValue();
            for (k = 1; k < yList.size(); ++k) {
                if (maxY < (Double)yList.get(k)) {
                    maxY = (Double)yList.get(k);
                    continue;
                }
                if (minY <= (Double)yList.get(k)) continue;
                minY = (Double)yList.get(k);
            }
            neighborsPerTime.get((int)j).height = maxY - minY;
            neighborsPerTime.get((int)j).width = maxX - minX;
            double d = extent = maxY - minY > maxX - minX ? maxY - minY : maxX - minX;
            if (neighborsPerTime.get((int)j).shortestExtent <= extent) continue;
            neighborsPerTime.get((int)j).shortestExtent = extent;
        }
    }
   
}