package com.dellingertechnologies.javajukebox;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.dellingertechnologies.javajukebox.model.Track;
import com.dellingertechnologies.javajukebox.model.User;

public class TrackRowMapper implements RowMapper<Track> {
	public Track mapRow(ResultSet rs, int idx) throws SQLException {
		Track track = new Track();
		track.setId(rs.getInt("id"));
		track.setTitle(rs.getString("title"));
		track.setAlbum(rs.getString("album"));
		track.setArtist(rs.getString("artist"));
		track.setPath(rs.getString("path"));
		track.setChecksum(rs.getLong("checksum"));
		track.setLikes(rs.getInt("likes"));
		track.setDislikes(rs.getInt("dislikes"));
		track.setPlays(rs.getInt("plays"));
		track.setExplicit(rs.getBoolean("explicit"));
		track.setLastPlayed(rs.getTimestamp("lastplayed"));
		track.setSkips(rs.getInt("skips"));
		track.setEnabled(rs.getBoolean("enabled"));
		User user = new User(rs.getString("username"));
		user.setGravatarId(rs.getString("gravatar_id"));
		user.setEnabled(rs.getBoolean("user_enabled"));
		track.setUser(user);
		return track;
	}
}