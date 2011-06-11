package com.dellingertechnologies.javajukebox;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.jettison.json.JSONObject;

@Path("/jukebox")
public class JukeboxResource {

	@GET
	@Path("status")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject status() throws Exception{
		Jukebox jukebox = Jukebox.getInstance();
		JSONObject response = new JSONObject();
		response.put("time", System.currentTimeMillis());
		response.put("playing", jukebox.isPlaying());
		response.put("status", jukebox.getCurrentState().toString());
		response.put("current", jukebox.getCurrentFileProperties());
		response.put("progress", jukebox.getCurrentProgress());
		return response;
	}
	
	@GET
	@Path("skip")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject skipTrack() throws Exception{
		boolean result = Jukebox.getInstance().playNextTrack();
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
		response.append("volume", Jukebox.getInstance().getVolume());
		response.append("gain", Jukebox.getInstance().getPlayer().getGainValue());
		response.append("maxgain", Jukebox.getInstance().getPlayer().getMaximumGain());
		response.append("mingain", Jukebox.getInstance().getPlayer().getMinimumGain());
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
}
