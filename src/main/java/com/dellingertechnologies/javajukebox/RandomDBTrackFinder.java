package com.dellingertechnologies.javajukebox;

import com.dellingertechnologies.javajukebox.model.Track;

public class RandomDBTrackFinder implements TrackFinder {

	private JukeboxDao dao;

	public RandomDBTrackFinder(JukeboxDao dao){
		this.dao = dao;
	}
	
	public Track nextTrack() {
		return dao.getRandomTrack();
	}

}
