package fr.david.mdm.models;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RequestBody {

	/*Server connection*/
	@XmlElement public String database_name;
	@XmlElement public String database_ip;
	@XmlElement public Integer database_port;
	@XmlElement public String date_start_time;
	@XmlElement public Integer time_window_size;
	
	/*Micro-group*/
	@XmlElement public Double eps;
	@XmlElement public Integer minpoints;
	@XmlElement public Double sigma;
	@XmlElement public Double mgradius;
	
	/*Flocks*/
	@XmlElement public Integer duration;
	@XmlElement public Integer size;
	@XmlElement public Double radius;
	
}
