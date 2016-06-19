package fr.david.mdm.kafka;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.vividsolutions.jts.geom.Coordinate;

import fr.david.mdm.dataset.MicroGroupsFinder;
import fr.david.mdm.models.MovingObject;

public class SimpleTrajectoryConsumer implements Runnable{

    private final ConsumerConnector consumer;
    private final String topic;
    public HashMap<Integer, MovingObject> movingObjectTimeWindow;
    public ArrayList<HashMap<Integer, MovingObject>> timeWindowList;
    
    public  MicroGroupsFinder microgroupfinder;
   
    public SimpleTrajectoryConsumer(String zookeeper, String groupId, String topic, MicroGroupsFinder object) {
        Properties props = new Properties();
        props.put("zookeeper.connect", zookeeper);
        props.put("group.id", groupId);
        props.put("zookeeper.session.timeout.ms", "50000");
        props.put("zookeeper.sync.time.ms", "250");
        props.put("auto.commit.interval.ms", "1000");
        props.put("consumer.timeout.ms", "100000");
        props.put("reconnect.time.interval.ms", "10000");

        consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
        this.topic = topic;
        
        timeWindowList = new ArrayList<HashMap<Integer, MovingObject>>();
        movingObjectTimeWindow = new HashMap<Integer, MovingObject>();
        
        microgroupfinder = object;
    }

    public void run() {
        Map<String, Integer> topicCount = new HashMap<>();
        topicCount.put(topic, 1);

        ArrayList<Coordinate> coordinate_list;
		ArrayList<Long> time_arrray;
		
		MovingObject mo;
		int index;
		int mo_id;
		long time;
		Coordinate coord;
        
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerStreams = consumer.createMessageStreams(topicCount);
        List<KafkaStream<byte[], byte[]>> streams = consumerStreams.get(topic);
        int count=0;
        for (final KafkaStream stream : streams) {
        	System.out.println("Kakfa consumer started!!");
        	
            ConsumerIterator<byte[], byte[]> it = stream.iterator();
            
            while(true){
            	 System.out.println("Kafka consumer can start to handle the streams!");
            	  movingObjectTimeWindow = new HashMap<Integer, MovingObject>();
                 
                 try{
                 	 while (it.hasNext()) {
                      	//trajid #sampleRate {x, y, time}
                      	
                      	coordinate_list = new ArrayList<Coordinate>();
          				time_arrray = new ArrayList<Long>();
          			
          				String message = new String(it.next().message());
          				System.out.println(message.toString());

          				String[] data = message.split(" ");
          				mo_id = Integer.parseInt(data[0]);
          				index = 2;
          				
          				while((index+2) <= (data.length-1)){
          					coord = new Coordinate(Double.parseDouble(data[index]), Double.parseDouble(data[index+1]));
          					coordinate_list.add(coord);
          					
          					time = Long.parseLong(data[index+2]);
          					time_arrray.add(time);
          					
          					index +=3;
          				}
          				
          				mo = new MovingObject(mo_id, coordinate_list,time_arrray);
          				movingObjectTimeWindow.put(mo_id, mo); 
          				//System.out.println("stream: "+count);
          				//count++;
                      }
                 	//timeWindowList.add(movingObjectTimeWindow);
                 	System.out.println("começou nova chuva de stream!!");
                 	Thread.sleep(30000);
                 }
                 catch(Exception e){
                 	//e.printStackTrace();
                 }
                 
            }
            
           
        }
        if (consumer != null) {
            consumer.shutdown();
        }
        
        
       // return movingObjectTimeWindow;
    }
    
   /*public static void main(String[] args) {
    	
    	args = new String[4];
		args[0]= "localhost:2181";
    	args[1] = "1";
    	args[2] = "trajevent";
    	args[3] = "1";
    	
        String topic = "trajevent";
        SimpleTrajectoryConsumer simpleHLConsumer = new SimpleTrajectoryConsumer("localhost:2181", "1", topic);
        simpleHLConsumer.run();
    }*/

}
