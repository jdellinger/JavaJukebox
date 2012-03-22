package com.dellingertechnologies.javajukebox.services;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.dellingertechnologies.javajukebox.Jukebox;
import com.dellingertechnologies.javajukebox.model.Track;
import com.dellingertechnologies.javajukebox.model.User;

@Path("/jukebox")
public class JukeboxResource {

	@GET
	@Path("status")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject status(@Context HttpServletRequest request) throws Exception{
		Jukebox jukebox = Jukebox.getInstance();
		JSONObject response = new JSONObject();
		response.put("time", System.currentTimeMillis());
		response.put("playing", jukebox.isPlaying());
		response.put("track", toJSON(jukebox.getCurrentTrack()));
		response.put("volume", Jukebox.getInstance().getVolume());
		
		long frame = (Long)jukebox.getCurrentProgress().get("mp3.frame");
		int totalFrames = (Integer)jukebox.getCurrentFileProperties().get("mp3.length.frames");
		double progress = totalFrames > 0 ? frame*1.0/totalFrames : 0;
		response.put("progress", progress);
		response.put("rating", Jukebox.getInstance().getRating(request.getRemoteAddr()));
		response.put("details", jukebox.getCurrentProgress());
        response.put("explicitMode", jukebox.isExplicitMode());
		return response;
	}
	
	private JSONObject toJSON(Track track) throws Exception {
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

	@GET
	@Path("skip")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject skipTrack() throws Exception{
		boolean result = Jukebox.getInstance().skipTrack();
		return new JSONObject().put("result", result);
	}

	@GET
	@Path("pause")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject pauseTrack() throws Exception{
		boolean result = Jukebox.getInstance().pauseTrack();
		return new JSONObject().put("result", result);
	}

	@GET
	@Path("resume")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject resumeTrack() throws Exception{
		boolean result = Jukebox.getInstance().resumeTrack();
		return new JSONObject().put("result", result);
	}

	@GET
	@Path("volume")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject volume() throws Exception{
		JSONObject response = new JSONObject();
		response.put("volume", Jukebox.getInstance().getVolume());
		response.put("gain", Jukebox.getInstance().getPlayer().getGainValue());
		response.put("maxgain", Jukebox.getInstance().getPlayer().getMaximumGain());
		response.put("mingain", Jukebox.getInstance().getPlayer().getMinimumGain());
		return response;
	}
	
	@GET
	@Path("restart")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject restart() throws Exception{
		boolean result = Jukebox.getInstance().restartTrack();
		return new JSONObject().put("result", result);
	}

	@GET
	@Path("shutdown")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject shutdown() throws Exception{
		Jukebox.getInstance().powerOff();
		return new JSONObject().put("result", true);
	}

	@POST
	@Path("volume")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject updateVolume(@FormParam("volume") double volume) throws Exception{
		Jukebox.getInstance().setVolume(volume);
		return volume();
	}

	@GET
	@Path("like")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject likeTrack(@Context HttpServletRequest request) throws Exception{
		Jukebox.getInstance().likeCurrentTrack(request.getRemoteAddr());
		return new JSONObject()
			.put("result", true)
			.put("rating", Jukebox.getInstance().getRating(request.getRemoteAddr()));
	}

	@GET
	@Path("dislike")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject dislikeTrack(@Context HttpServletRequest request) throws Exception{
		Jukebox.getInstance().dislikeCurrentTrack(request.getRemoteAddr());
		return new JSONObject()
			.put("result", true)
			.put("rating", Jukebox.getInstance().getRating(request.getRemoteAddr()));
	}

	@POST
	@Path("explicit")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject flagAsExplicitTrack() throws Exception{
		Jukebox.getInstance().explicitTrack(true);
		return new JSONObject()
			.put("result", true)
			.put("explicit", Jukebox.getInstance().getCurrentTrack().isExplicit());
	}

	@GET
	@Path("queue/add")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject addToQueue(@QueryParam("num") int numToAdd, @QueryParam("id") int idToAdd) throws Exception{
		boolean result = false;
		if(idToAdd > 0){
			Jukebox.getInstance().addTrackToQueue(idToAdd);
			result = true;
		}else if(numToAdd > 0){
			int numberOfTracks = numToAdd > 0 ? numToAdd : 1;
			Jukebox.getInstance().addToQueue(numberOfTracks);
			result = true;
		}
		return new JSONObject().put("result", result);
	}
	
	@GET
	@Path("queue/remove")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject removeFromQueue(@QueryParam("id") int trackId) throws Exception{
		Jukebox.getInstance().removeTrackFromQueue(trackId);
		return new JSONObject().put("result", true);
	}
	
	@GET
	@Path("queue")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject getQueue() throws Exception{
		List<Track> tracks = Jukebox.getInstance().getQueue();
		JSONArray jsonTracks = new JSONArray();
		for(Track track : tracks){
			jsonTracks.put(toJSON(track));
		}
		return new JSONObject().put("queue", jsonTracks);
	}

	@GET
	@Path("search")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject searchTracks(@QueryParam("text") String searchText) throws Exception {
		List<Track> tracks = Jukebox.getInstance().searchTracks(searchText);
		JSONArray jsonTracks = new JSONArray();
		for (Track track : tracks) {
			jsonTracks.put(toJSON(track));
		}
		return new JSONObject().put("results", jsonTracks);
	}
}
