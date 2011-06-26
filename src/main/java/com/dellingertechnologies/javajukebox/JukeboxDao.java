package com.dellingertechnologies.javajukebox;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.xml.sax.InputSource;

import com.dellingertechnologies.javajukebox.model.Track;

public class JukeboxDao {

	private File dir;
	private Database database;
	private NetworkServerControl derbyServer;

	public JukeboxDao(File dir) throws Exception{
		this.dir = dir;
		System.setProperty("jukebox.db.path", dir.getAbsolutePath());
		startDerbyServer();
		validateTables();
	}
	
	private void startDerbyServer() {
		try{
			derbyServer = new NetworkServerControl();
			derbyServer.start(null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void stopDerbyServer() {
		try{
			if(derbyServer != null)
				derbyServer.shutdown();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void validateTables() throws Exception {
		Connection conn = Locator.getDataSource().getConnection();
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
		Platform platform = PlatformFactory.createNewPlatformInstance(Locator.getDataSource());
		platform.createTables(database, false, false);
	}

	public void shutdown(){
		stopDerbyServer();
		((EmbeddedDataSource)Locator.getDataSource()).setShutdownDatabase("shutdown");
	}
	
	private JdbcTemplate getTemplate(){
		return new JdbcTemplate(Locator.getDataSource());
	}
	
	public boolean hasTracks(boolean enabledOnly){
		return numberOfTracks(enabledOnly) > 0;
	}
	
	public int numberOfTracks(boolean enabledOnly){
		return getTemplate().queryForInt("select count(1) from tracks where enabled = ?", enabledOnly);
	}
	
	public Track getTrack(int id){
		return getTemplate().queryForObject("select * from tracks where id = ?", new Object[]{id}, new TrackRowMapper());
	}

	public Track getRandomTrack(){
		return getTemplate().queryForObject("select * from tracks where enabled = ? order by random() fetch first row only", new Object[]{true}, new TrackRowMapper());
	}
	
	public void addOrUpdateTrack(Track track) {
		int id = 0;
		try{
			id = getTemplate().queryForInt("select id from tracks where checksum = ?", track.getChecksum());
		}catch(DataAccessException dae){}
		if(id > 0){
			String updateSql = "update tracks set title = ?, album = ?, artist = ?, path = ?, checksum = ?, likes = ?, dislikes = ?, plays = ?, skips = ?, lastplayed = ?, explicit = ?, enabled = ? where id = ?";
			getTemplate().update(updateSql,
					new Object[]{
						track.getTitle(),
						track.getAlbum(),
						track.getArtist(),
						track.getPath(),
						track.getChecksum(),
						track.getLikeCount(),
						track.getDislikeCount(),
						track.getPlayCount(),
						track.getSkipCount(),
						track.getLastPlayed(),
						track.isExplicit(),
						track.isEnabled(),
						id
					},
					new int[]{
						Types.VARCHAR,
						Types.VARCHAR,
						Types.VARCHAR,
						Types.VARCHAR,
						Types.NUMERIC,
						Types.NUMERIC,
						Types.NUMERIC,
						Types.NUMERIC,
						Types.NUMERIC,
						Types.TIMESTAMP,
						Types.BOOLEAN,
						Types.BOOLEAN,
						Types.NUMERIC
					}
			);
		}else{
			String insertSql = "insert into tracks (title, album, artist, path, checksum, likes, dislikes, plays, skips, lastplayed, explicit, enabled) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			getTemplate().update(insertSql,
					new Object[]{
						track.getTitle(),
						track.getAlbum(),
						track.getArtist(),
						track.getPath(),
						track.getChecksum(),
						track.getLikeCount(),
						track.getDislikeCount(),
						track.getPlayCount(),
						track.getSkipCount(),
						track.getLastPlayed(),
						track.isExplicit(),
						track.isEnabled()
					},
					new int[]{
						Types.VARCHAR,
						Types.VARCHAR,
						Types.VARCHAR,
						Types.VARCHAR,
						Types.NUMERIC,
						Types.NUMERIC,
						Types.NUMERIC,
						Types.NUMERIC,
						Types.NUMERIC,
						Types.TIMESTAMP,
						Types.BOOLEAN,
						Types.BOOLEAN
					}
			);
		}
	}
}
