package com.dellingertechnologies.javajukebox.services;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.dellingertechnologies.javajukebox.model.Track;
import com.dellingertechnologies.javajukebox.model.TrackInfo;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.dellingertechnologies.javajukebox.Jukebox;
import com.dellingertechnologies.javajukebox.model.Snippet;

@Path("/snippet")
public class SnippetResource extends AbstractResource{

	@GET
	@Path("play")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject playSnippet(@QueryParam("token") String token) throws Exception{
		boolean result = Jukebox.getInstance().enqueueSnippet(token);
		return new JSONObject().put("result", result);
	}

	@GET
	@Path("clear")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject clearSnippets() throws Exception{
		Jukebox.getInstance().clearSnippetQueue();
		return new JSONObject().put("result", true);
	}
	
	@GET
	@Path("queue")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listSnippetsQueue() throws Exception{
		List<Snippet> snippets = Jukebox.getInstance().getSnippetsQueue();
		JSONArray json = new JSONArray();
		for(Snippet s : snippets){
			json.put(toJSON(s));
		}
		return new JSONObject().put("snippets", json);
	}

	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject listSnippets() throws Exception{
		List<Snippet> snippets = Jukebox.getInstance().getSnippets();
		JSONArray json = new JSONArray();
		for(Snippet s : snippets){
			json.put(toJSON(s));
		}
		return new JSONObject().put("snippets", json);
	}
    
    @GET
    @Path("track")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject trackInfo(@QueryParam("id") int trackId) throws Exception {
        Track track = Jukebox.getInstance().lookupTrack(trackId);
        TrackInfo trackInfo = Jukebox.getInstance().examineTrack(track);

        JSONObject response = new JSONObject();
        response.put("track", toJSON(track));
        response.put("trackInfo", toJSON(trackInfo));
        return response;
    }

    @GET
    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject testSnippet(
            @QueryParam("id") int trackId,
            @QueryParam("start") int start,
            @QueryParam("end") int end) throws Exception {
        Snippet snippet = new Snippet();
        snippet.setTrackId(trackId);
        snippet.setStartPosition(start);
        snippet.setEndPosition(end);
        Jukebox.getInstance().playTestSnippet(snippet);
        return new JSONObject().put("result", true);
    }

    @GET
    @Path("save")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject saveSnippet(
            @QueryParam("id") int trackId,
            @QueryParam("start") int start,
            @QueryParam("end") int end,
            @QueryParam("name") String name,
            @QueryParam("token") String token) throws Exception {
        Snippet snippet = new Snippet();
        snippet.setTrackId(trackId);
        snippet.setStartPosition(start);
        snippet.setEndPosition(end);
        snippet.setTitle(name);
        snippet.setToken(token);
        Jukebox.getInstance().saveSnippet(snippet);
        return new JSONObject().put("result", true);
    }
}
