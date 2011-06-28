package com.dellingertechnologies.javajukebox;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jettison.json.JSONObject;

import com.dellingertechnologies.javajukebox.model.Track;

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
		String relativePath = new File(jukebox.getCurrentTrack().getPath()).getCanonicalPath();
		String directoryPath = jukebox.getDirectory().getCanonicalPath()+File.separator;
		response.put("file", relativePath.replace(directoryPath, ""));
		response.put("volume", Jukebox.getInstance().getVolume());
		
		long frame = (Long)jukebox.getCurrentProgress().get("mp3.frame");
		int totalFrames = (Integer)jukebox.getCurrentFileProperties().get("mp3.length.frames");
		double progress = totalFrames > 0 ? frame*1.0/totalFrames : 0;
		response.put("progress", progress);
		response.put("rating", Jukebox.getInstance().getRating(request.getRemoteAddr()));
		return response;
	}
	
	private JSONObject toJSON(Track track) throws Exception {
		JSONObject json = new JSONObject();
		if(track != null){
			json.put("title", track.getTitle());
			json.put("album", track.getAlbum());
			json.put("artist", track.getArtist());
			json.put("likes", track.getLikeCount());
			json.put("dislikes", track.getDislikeCount());
			json.put("skips", track.getSkipCount());
			json.put("plays", track.getPlayCount());
			json.put("explicit", track.isExplicit());
		}
		return json;
	}

	@GET
	@Path("skip")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject skipTrack() throws Exception{
		boolean result = Jukebox.getInstance().skipTrack();
		return new JSONObject().append("result", result);
	}

	@GET
	@Path("pause")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject pauseTrack() throws Exception{
		boolean result = Jukebox.getInstance().pauseTrack();
		return new JSONObject().append("result", result);
	}

	@GET
	@Path("resume")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject resumeTrack() throws Exception{
		boolean result = Jukebox.getInstance().resumeTrack();
		return new JSONObject().append("result", result);
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
		long bytes = ((Long)Jukebox.getInstance().getCurrentProgress().get("mp3.position.byte")).longValue();
		long result = Jukebox.getInstance().getPlayer().seek(-bytes);
		return new JSONObject().append("result", result);
	}

	@GET
	@Path("shutdown")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject shutdown() throws Exception{
		Jukebox.getInstance().powerOff();
		return new JSONObject().append("result", true);
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
			.append("result", true)
			.append("rating", Jukebox.getInstance().getRating(request.getRemoteAddr()));
	}

	@GET
	@Path("dislike")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject dislikeTrack(@Context HttpServletRequest request) throws Exception{
		Jukebox.getInstance().dislikeCurrentTrack(request.getRemoteAddr());
		return new JSONObject()
			.append("result", true)
			.append("rating", Jukebox.getInstance().getRating(request.getRemoteAddr()));
	}

	@POST
	@Path("explicit")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject flagAsExplicitTrack() throws Exception{
		Jukebox.getInstance().explicitTrack(true);
		return new JSONObject()
			.append("result", true)
			.append("explicit", Jukebox.getInstance().getCurrentTrack().isExplicit());
	}

}
