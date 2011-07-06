package com.dellingertechnologies.javajukebox;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.xml.sax.InputSource;

import com.dellingertechnologies.javajukebox.model.Track;

public class JukeboxDao {

	private File dir;
	private Database database;
	private NetworkServerControl derbyServer;

	private Log log = LogFactory.getLog(JukeboxDao.class);
	
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
			log.error("Exception starting derby server", e);
		}
	}
	
	private void stopDerbyServer() {
		try{
			if(derbyServer != null)
				derbyServer.shutdown();
		}catch(Exception e){
			log.error("Exception stopping derby server", e);
		}
	}

	private void validateTables() throws Exception {
		Connection conn = Locator.getDataSource().getConnection();
		DatabaseMetaData metaData = conn.getMetaData();
		ResultSet tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
		if(tables.next()){
			alterTables();
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

	private void alterTables() throws Exception {
		database = new DatabaseIO().read(new InputSource(this.getClass().getResourceAsStream("/db/version1.xml")));
		Platform platform = PlatformFactory.createNewPlatformInstance(Locator.getDataSource());
		platform.alterTables(database, false);
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

	public int getTrackIdByChecksum(long checksum){
		int id = 0;
		try{
			id = getTemplate().queryForInt("select id from tracks where checksum = ?", checksum);
		}catch(DataAccessException dae){}
		return id;
	}
	
	public void addOrUpdateTrack(Track track) {
		int id = getTrackIdByChecksum(track.getChecksum());
		if(id > 0){
			String updateSql = "update tracks set title = ?, album = ?, artist = ?, path = ?, checksum = ?, likes = ?, dislikes = ?, plays = ?, skips = ?, lastplayed = ?, explicit = ?, enabled = ? where id = ?";
			getTemplate().update(updateSql,
					new Object[]{
						track.getTitle(),
						track.getAlbum(),
						track.getArtist(),
						track.getPath(),
						track.getChecksum(),
						track.getLikes(),
						track.getDislikes(),
						track.getPlays(),
						track.getSkips(),
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
						track.getLikes(),
						track.getDislikes(),
						track.getPlays(),
						track.getSkips(),
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

	public int numberOfTracksInQueue(){
		return getTemplate().queryForInt("select count(1) from queue");
	}
	
	public boolean hasTracksInQueue(){
		return numberOfTracksInQueue() > 0;
	}
	
	public Track popFromQueue() {
		Track track = null;
		if(hasTracksInQueue()){
			int queue_id = getTemplate().queryForInt("select id from queue order by id asc fetch first row only");
			if(queue_id > 0){
				track = getTemplate().queryForObject("select t.* from queue q join tracks t on q.track_id = t.id where q.id = ? and t.enabled = 1", new Object[]{queue_id}, new TrackRowMapper());
			}
			getTemplate().update("delete from queue where id = ?", queue_id);
		}
		return track;
	}
	
	public void addTrackToQueue(int id){
		if (id > 0) {
			getTemplate().update("insert into queue (track_id) values (?)", id);
		}
	}
	
	public List<Track> getTracks(){
		TrackQuery q = new TrackQuery();
		q.setDataSource(Locator.getDataSource());
		q.setSql("select * from tracks where enabled = 1");
		return q.getTracks();
	}
	
	public List<Track> getQueue(){
		TrackQuery q = new TrackQuery();
		q.setDataSource(Locator.getDataSource());
		q.setSql("select t.* from queue q join tracks t on q.track_id = t.id where t.enabled = 1 order by q.id asc");
		return q.getTracks();
	}

	public void removeTrackFromQueue(int trackId) {
		getTemplate().update("delete from queue where track_id = ?", new Object[]{trackId});
	}
}

class TrackQuery extends MappingSqlQuery<Track> {

	private RowMapper<Track> mapper = new TrackRowMapper();
	
	public List<Track> getTracks(){
		return execute();
	}
	@Override
	protected Track mapRow(ResultSet rs, int idx) throws SQLException {
		return mapper.mapRow(rs, idx);
	}
	
}
