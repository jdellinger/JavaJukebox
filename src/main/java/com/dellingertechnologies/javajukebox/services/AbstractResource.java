package com.dellingertechnologies.javajukebox.services;

import java.io.File;

import com.dellingertechnologies.javajukebox.model.TrackInfo;
import org.codehaus.jettison.json.JSONObject;

import com.dellingertechnologies.javajukebox.Jukebox;
import com.dellingertechnologies.javajukebox.model.Snippet;
import com.dellingertechnologies.javajukebox.model.Track;
import com.dellingertechnologies.javajukebox.model.User;

public class AbstractResource {

	public AbstractResource() {
		super();
	}

	protected JSONObject toJSON(Track track) throws Exception {
		Jukebox jukebox = Jukebox.getInstance();
		JSONObject json = new JSONObject();
		if(track != null){
			json.put("id", track.getId());
			json.put("title", track.getTitle());
			json.put("album", track.getAlbum());
			json.put("artist", track.getArtist());
			json.put("likes", track.getLikes());
			json.put("dislikes", track.getDislikes());
			json.put("skips", track.getSkips());
			json.put("plays", track.getPlays());
			json.put("lastplayed", track.getLastPlayed());
			json.put("explicit", track.isExplicit());
	
			String relativePath = new File(track.getPath()).getCanonicalPath();
			String directoryPath = jukebox.getDirectory().getCanonicalPath()+File.separator;
			json.put("file", relativePath.replace(directoryPath, ""));
			
			json.put("user", toJSON(track.getUser()));
		}
		return json;
	}

	private JSONObject toJSON(User user) throws Exception {
		JSONObject json = new JSONObject();
		if(user != null){
			json.put("username", user.getUsername());
			json.put("gravatarId", user.getGravatarId());
		}
		return json;
	}

	protected JSONObject toJSON(Snippet s) throws Exception {
		JSONObject json = new JSONObject();
		json.put("id", s.getId());
		json.put("trackId", s.getTrackId());
		json.put("title", s.getTitle());
		json.put("token", s.getToken());
		json.put("startPosition", s.getStartPosition());
		json.put("endPosition", s.getEndPosition());
		json.put("track", toJSON(s.getTrack()));
		return json;
	}
    
    protected JSONObject toJSON(TrackInfo ti) throws Exception {
        JSONObject json = new JSONObject();
        json.put("trackId", ti.getTrackId());
        json.put("duration", ti.getDuration());
        json.put("durationDisplay", ti.getDurationDisplay());
        json.put("bytes", ti.getBytes());
        return json;
    }

}