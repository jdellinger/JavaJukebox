package com.dellingertechnologies.javajukebox;

import com.dellingertechnologies.javajukebox.model.Track;

public class QueueTrackFinder implements TrackFinder {

	private JukeboxDao dao;

	public QueueTrackFinder(JukeboxDao dao){
		this.dao = dao;
	}
	
	public Track nextTrack() {
		return dao.popFromQueue();
	}

}
