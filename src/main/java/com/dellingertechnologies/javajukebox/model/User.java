package com.dellingertechnologies.javajukebox.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class User {

	public static final User DEFAULT = new User("default");
	
	private String username;
	private String gravatarId;
	private boolean enabled = true;

	public User(String username) {
		this.username = username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setGravatarId(String gravatarId) {
		this.gravatarId = gravatarId;
	}

	public String getGravatarId() {
		return gravatarId;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean equals(Object obj) {
		User user = (User) obj;
		return new EqualsBuilder().append(getUsername(), user.getUsername()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(23, 19).append(getUsername()).toHashCode();
	}
	
}
