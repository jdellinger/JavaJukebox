package com.dellingertechnologies.javajukebox.model;

public class Snippet {

	private int id;
	private String title; 
	private String token;
	private int trackId;
	private long startPosition;
	private long endPosition;
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getTrackId() {
		return trackId;
	}
	public void setTrackId(int trackId) {
		this.trackId = trackId;
	}
	public long getStartPosition() {
		return startPosition;
	}
	public void setStartPosition(long startPosition) {
		this.startPosition = startPosition;
	}
	public long getEndPosition() {
		return endPosition;
	}
	public void setEndPosition(long endPosition) {
		this.endPosition = endPosition;
	}
	
}
