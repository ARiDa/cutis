package fr.david.mdm.kafka;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import kafka.javaapi.producer.Producer;
import org.apache.log4j.Logger;

import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class FireStreams {
/**
 * TruckEventsProducer class simulates the real time truck event generation.
 */

    private static final Logger LOG = Logger.getLogger(FireStreams.class);

    public static void main(String[] args) throws IOException {
              
    	// localhost:9092 localhost:2181 trajevent 5 mg 480000
    	if(args.length!=6){
    		System.out.println("Enter with ip_kafka:port_kafka ip_zookeeper:port_zookeeper topic #timewindows cutis_method(mg, flock) time_window_inmiliseconds");
    		System.exit(-1);
    	}
    
        
        Properties props = new Properties();
        props.put("metadata.broker.list", args[0]); //lider do topico
        props.put("zookeeper.connect", args[1]); //zk.connect
        
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("request.required.acks", "1"); //requer do broker confirmacao que a mensagem foi enviada

        String TOPIC = args[2];
        ProducerConfig config = new ProducerConfig(props);

        Producer<String, String> producer = new Producer<String, String>(config);

        //total number of time window
        int count=Integer.parseInt(args[3]);
        int i=0;
        String method = args[4];
        long timeout = Long.parseLong(args[5]);
                
        String trajectoryMessage = "";
        
        while(i < count){
        	  //capturando os dados das trajetorias
        	 String file = "C:/Users/Ticiana/workspace/mdm-demo-web/kafka_demo_"+method+"/timewindow_kafka_demo"+i+".csv";
             BufferedReader br = new BufferedReader(new FileReader(file));
        	 while((trajectoryMessage=br.readLine())!=null)
             {
             	try {
                     KeyedMessage<String, String> data = new KeyedMessage<String, String>(TOPIC, String.valueOf(i), trajectoryMessage);
                     LOG.info("Time Window #: " + i + "TrajData" + trajectoryMessage);
                     producer.send(data);
                     System.out.println(trajectoryMessage);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
        	 System.out.println("/----------------------------------------------");
        	 System.out.println("FINISH TO FIRE THE STREAMS OF TIME WINDOW "+i);
        	 System.out.println("-----------------------------------------------/");
        	 i++;
        	 try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
       
        	        
        //producer.close();
    }
}

