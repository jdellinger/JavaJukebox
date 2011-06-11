package com.dellingertechnologies.javajukebox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class JukeboxDao {

	private Connection conn;
	private String path;

	public JukeboxDao(String path) throws Exception{
		this.path = path;
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		Class.forName(driver).newInstance();
		Properties props = null;
		conn = DriverManager.getConnection("jdbc:derby:"+path+";create=true", props);
	}
	
	public void cleanup(){
		try{
			DriverManager.getConnection("jdbc:derby:"+path+";shutdown=true");
		}catch(Exception e){}
	}
}
