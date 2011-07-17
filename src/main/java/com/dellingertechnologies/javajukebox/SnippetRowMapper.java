package com.dellingertechnologies.javajukebox;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.dellingertechnologies.javajukebox.model.Snippet;

public class SnippetRowMapper implements RowMapper<Snippet> {
	public Snippet mapRow(ResultSet rs, int idx) throws SQLException {
		Snippet snippet = new Snippet();
		snippet.setId(rs.getInt("id"));
		snippet.setTrackId(rs.getInt("track_id"));
		snippet.setTitle(rs.getString("title"));
		snippet.setToken(rs.getString("token"));
		snippet.setStartPosition(rs.getLong("start_position"));
		snippet.setEndPosition(rs.getLong("end_position"));
		return snippet;
	}
}