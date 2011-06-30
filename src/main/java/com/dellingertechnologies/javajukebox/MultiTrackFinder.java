package com.dellingertechnologies.javajukebox;

import com.dellingertechnologies.javajukebox.model.Track;

public class MultiTrackFinder implements TrackFinder {

	private TrackFinder[] finders;
	public MultiTrackFinder(TrackFinder...finders){
		this.finders = finders;
	}
	
	public Track nextTrack() {
		if(finders == null || finders.length == 0){
			return null;
		}
		for(TrackFinder finder : finders){
			Track track = finder.nextTrack();
			if(track != null){
				return track;
			}
		}
		return null;
	}

}
