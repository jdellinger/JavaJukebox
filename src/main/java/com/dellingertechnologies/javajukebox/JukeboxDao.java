package com.dellingertechnologies.javajukebox;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.DataSourceConnectionFactory;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.xml.sax.InputSource;

public class JukeboxDao {

	private Connection conn;
	private String path;
	private DataSource datasource;
	private Database database;

	public JukeboxDao(String path) throws Exception{
		this.path = path;
		datasource = createDataSource();
		validateTables();
	}
	
	private void validateTables() throws Exception {
		Connection conn = datasource.getConnection();
		DatabaseMetaData metaData = conn.getMetaData();
		ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
		if(tables.next()){
			//check them
		}else{
			createTables();
		}
		tables.close();
		conn.close();
	}

	private void createTables() throws Exception {
		database = new DatabaseIO().read(new InputSource(this.getClass().getResourceAsStream("/db/version1.xml")));
		Platform platform = PlatformFactory.createNewPlatformInstance(datasource);
		platform.createTables(database, false, false);
	}

	private DataSource createDataSource() {
		EmbeddedDataSource ds = new EmbeddedDataSource();
		ds.setDatabaseName(path);
		ds.setCreateDatabase("create");
		return ds;
	}

	public void shutdown(){
		try{
			DriverManager.getConnection("jdbc:derby:"+path+";shutdown=true");
		}catch(Exception e){}
	}
}
