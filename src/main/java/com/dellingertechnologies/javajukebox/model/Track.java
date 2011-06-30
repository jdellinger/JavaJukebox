package com.dellingertechnologies.javajukebox.model;

import java.util.Date;

public class Track {

	private int id;
	private String title;
	private String album;
	private String artist;
	private String path;
	private long checksum;
	private int likes;
	private int dislikes;
	private int skips;
	private int plays;
	private Date lastPlayed;
	private boolean explicit = false;
	private boolean enabled = true;
	private User user;
	
	public void incrementSkips(){
		this.skips++;
	}
	public void incrementPlays(){
		this.plays++;
	}
	public void incrementLikes(){
		this.likes++;
	}
	public void incrementDislikes(){
		this.dislikes++;
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
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public long getChecksum() {
		return checksum;
	}
	public void setChecksum(long checksum) {
		this.checksum = checksum;
	}
	public int getLikes() {
		return likes;
	}
	public void setLikes(int likes) {
		this.likes = likes;
	}
	public int getDislikes() {
		return dislikes;
	}
	public void setDislikes(int dislikes) {
		this.dislikes = dislikes;
	}
	public int getSkips() {
		return skips;
	}
	public void setSkips(int skips) {
		this.skips = skips;
	}
	public int getPlays() {
		return plays;
	}
	public void setPlays(int plays) {
		this.plays = plays;
	}
	public Date getLastPlayed() {
		return lastPlayed;
	}
	public void setLastPlayed(Date lastPlayed) {
		this.lastPlayed = lastPlayed;
	}
	public boolean isExplicit() {
		return explicit;
	}
	public void setExplicit(boolean explicit) {
		this.explicit = explicit;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public boolean isEnabled() {
		return enabled;
	}
	
}
