package fr.david.mdm.dataset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    
	public Connection getConnection(String ip, int port, String user, String passwd, String bd) {
        try {
        	try {
				Class.forName("org.postgresql.Driver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
        	//return DriverManager.getConnection("jdbc:postgresql://10.102.12.140:5432/postgres?autoReconnect=true","postgres","cloudufc");
            //return DriverManager.getConnection("jdbc:postgresql://"+ip+":5432/taxisimples?autoReconnect=true",user,passwd);
        //	return DriverManager.getConnection("jdbc:postgresql://"+ip+":5432/vinicius?autoReconnect=true",user,passwd);
            return DriverManager.getConnection("jdbc:postgresql://"+ip+":"+port+"/"+bd, user, passwd); //versao 9.5

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
	
}