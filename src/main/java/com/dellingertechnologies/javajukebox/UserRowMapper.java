package com.dellingertechnologies.javajukebox;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.dellingertechnologies.javajukebox.model.Track;
import com.dellingertechnologies.javajukebox.model.User;

public class UserRowMapper implements RowMapper<User> {
	public User mapRow(ResultSet rs, int idx) throws SQLException {
		User user = new User(rs.getString("username"));
		user.setGravatarId(rs.getString("gravatar_id"));
		user.setEnabled(rs.getBoolean("enabled"));
		return user;
	}
}