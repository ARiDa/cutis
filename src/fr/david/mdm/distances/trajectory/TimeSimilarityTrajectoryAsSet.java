package fr.david.mdm.distances.trajectory;

import java.util.ArrayList;
import com.vividsolutions.jts.geom.Coordinate;

import fr.david.mdm.distances.trajectory.DistanceHelper;
import fr.david.mdm.models.TrajectoryAsSet;

public class TimeSimilarityTrajectoryAsSet extends DistanceHelper<TrajectoryAsSet>{

	//static final Logger debugLog = Logger.getLogger(TimeSimilarityTrajectoryAsSet.class);
	
	@Override
	public double distance(TrajectoryAsSet t1, TrajectoryAsSet t2) {
		long endTime, startTime;
		//array de tempo de cada trajetoria
		ArrayList<Long> array_t1 = t1.getTsArray(), array_t2 = t2.getTsArray();
		//diference between the finalTime and initialTime
		double deltat = 0, deltaT=0;
		
		double areaT1=Double.POSITIVE_INFINITY;
		Coordinate c1,c2, c3 = null, c4 = null;
		
		//no caso de nao haver intersecao temporal entre as trajetorias
		if(array_t1.get(0)>array_t2.get(array_t2.size()-1) || array_t2.get(0)>array_t1.get(array_t1.size()-1)) return Double.POSITIVE_INFINITY;
				
		//escolhe ponto de inicio na comparacao
		if(array_t1.get(0)>array_t2.get(0)) startTime = array_t1.get(0);
		else startTime = array_t2.get(0);
		
		//escolhe ponto de fim na comparacao
		if(array_t1.get(array_t1.size()-1)<array_t2.get(array_t2.size()-1)) 
			endTime = array_t1.get(array_t1.size()-1);
		else endTime = array_t2.get(array_t2.size()-1);
		
		deltaT = endTime - startTime;
		ArrayList<Coordinate> g1 = t1.getCoordArray();
		ArrayList<Coordinate> g2 = t2.getCoordArray();
		
    	//onde comeca a intersecao entre as trajetorias (j eh index para t2 e i eh index para t1)
		int i = 0,j = 0;
    	if(array_t2.get(j)==startTime){
    		while(array_t1.get(i)<startTime) i++;
    	}
    	else{
    		while(array_t2.get(j)<startTime) j++;
    	}
		
    	
    	//debugLog.info("Comparing trajectories..."+t1.getTid()+" with "+t2.getTid());
		//while(i<=array_t1.size()-1 && j<=array_t2.size()-1 && array_t1.get(i)<=endTime && array_t2.get(j)<=endTime){   
		do{   
			
			//System.out.println(array_t1.get(i) +" "+array_t2.get(j));
	        
			if(array_t1.get(i)<array_t2.get(j)){
				c1 = g1.get(i);
				c2 = linearInterpolation(g2.get(j-1), g2.get(j), array_t2.get(j-1), 
						array_t2.get(j), array_t1.get(i));
				if(c1!=null && c2!=null && i+1<array_t1.size()){
					//os proximos pontos que farao parte do somatorio
					if(array_t1.get(i+1)<array_t2.get(j)){
						c3 = g1.get(i+1);
						c4 = linearInterpolation(c2, g2.get(j), array_t1.get(i), 
								array_t2.get(j), array_t1.get(i+1));
						deltat = Math.abs(array_t1.get(i+1)-array_t1.get(i));
					}
					else if(array_t1.get(i+1)>array_t2.get(j)){
						c4 = g2.get(j);
						c3 = linearInterpolation(c1, g1.get(i+1), array_t1.get(i), 
								array_t1.get(i+1), array_t2.get(j));
						
						deltat = Math.abs(array_t2.get(j)-array_t1.get(i));
					}
					else {
						c3 = g1.get(i+1);
						c4 = g2.get(j);
						deltat = Math.abs(array_t1.get(i+1)-array_t1.get(i));

					}
				}
				i++;

			}
			else if(array_t1.get(i)>array_t2.get(j)){
				c2 = g2.get(j);
				c1 = linearInterpolation(g1.get(i-1), g1.get(i), array_t1.get(i-1), 
						array_t1.get(i), array_t2.get(j));
				if(c1!=null && c2!=null  && j+1<array_t2.size()){
					//os proximos pontos que farao parte da regra do trapezio
					if(array_t1.get(i)<array_t2.get(j+1)){
						c3 = g1.get(i);
						c4 = linearInterpolation(g2.get(j), g2.get(j+1), array_t2.get(j), 
								array_t2.get(j+1), array_t1.get(i));
						deltat = Math.abs(array_t1.get(i)-array_t2.get(j));
					}
					else if(array_t1.get(i)>array_t2.get(j+1)){
						c4 = g2.get(j+1);
						c3 = linearInterpolation(c1, g1.get(i),array_t2.get(j), 
								array_t1.get(i), array_t2.get(j+1));
						deltat = Math.abs(array_t2.get(j+1)-array_t2.get(j));
					}
					else {
						c3 = g1.get(i);
						c4 = g2.get(j+1);
						deltat = Math.abs(array_t2.get(j+1)-array_t2.get(j));
					}
				}					
				j++;
			}
			else {
				c1 = g1.get(i);
				c2 = g2.get(j);
				
				if(c1!=null && c2!=null && i+1<array_t1.size()  && j+1<array_t2.size()){
					//os proximos pontos que farao parte da regra do trapezio
					if(array_t1.get(i+1)<array_t2.get(j+1)){
						c3 = g1.get(i+1);
						c4 = linearInterpolation(g2.get(j), g2.get(j+1), array_t2.get(j), 
								array_t2.get(j+1), array_t1.get(i+1));
						deltat = Math.abs(array_t1.get(i+1)-array_t1.get(i));
					}
					else if(array_t1.get(i+1)>array_t2.get(j+1)){
						c4 = g2.get(j+1);
						c3 = linearInterpolation(g1.get(i), g1.get(i+1), array_t1.get(i), 
								array_t1.get(i+1), array_t2.get(j+1));
						deltat = Math.abs(array_t2.get(j+1)-array_t2.get(j));
					}
					else {
						c3 = g1.get(i+1);
						c4 = g2.get(j+1);
						deltat = Math.abs(array_t2.get(j+1)-array_t2.get(j));
					}
				}							
				i++;
				j++;
			}
			
			
			if(c1!=null && c2!=null && c3!=null && c4!=null){
				if(areaT1==Double.POSITIVE_INFINITY){
					areaT1=0;
					//areaT2=0;
				}
				
				//debugLog.info("Trajectory "+t1.getTid()+" points "+c1.x+" "+c1.y+" ; "+c3.x+" "+c3.y);
				//debugLog.info("Trajectory "+t2.getTid()+" points "+c2.x+" "+c2.y+" ; "+c4.x+" "+c4.y);
				areaT1 += (getDistance(c1, c2) + getDistance(c3, c4))*deltat;
				
			}				
			if(!(i<array_t1.size() && j<array_t2.size())) break;
		}while(array_t1.get(i)<endTime || array_t2.get(j)<endTime);
		
		
		//System.out.println("Distancia TimeSpace: "+(areaT1/deltaT)/2);
		return (areaT1/deltaT)/2;
	}
	
	public Coordinate linearInterpolation(Coordinate c1, Coordinate c2, long t1, long t2, long desiredTime){
		//assumindo velocidade constante
		if((c2.x - c1.x)==0 && (c2.y - c1.y)==0) return null;
		double v = (c2.x-c1.x)/Math.abs(t2-t1);
		double x = c1.x + v*(desiredTime-t1); //valor de x
		
		v = (c2.y-c1.y)/Math.abs(t2-t1);
		double y = c1.y + v*(desiredTime-t1); //valor de y
		//double y = c1.y+(c2.y-c1.y)*((x-c1.x)/(c2.x - c1.x));
		Coordinate c3 = new Coordinate(x, y);
		//System.out.println("Linear Interpolation: time desired:"+desiredTime +" x: "+x+" y: "+y+" valor deno:"+(c2.x - c1.x));
		return c3;
	}
	
	public static double getDistance(Coordinate c1, Coordinate c2) {			
		return Math.sqrt(Math.pow(c1.x-c2.x, 2)+ Math.pow(c1.y-c2.y, 2));
	}

}
