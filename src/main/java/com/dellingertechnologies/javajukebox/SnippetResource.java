package com.dellingertechnologies.javajukebox;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.dellingertechnologies.javajukebox.model.Snippet;

@Path("/snippet")
public class SnippetResource {

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

	private JSONObject toJSON(Snippet s) throws Exception {
		JSONObject json = new JSONObject();
		json.put("id", s.getId());
		json.put("trackId", s.getTrackId());
		json.put("title", s.getTitle());
		json.put("token", s.getToken());
		json.put("startPosition", s.getStartPosition());
		json.put("endPosition", s.getEndPosition());
		return json;
	}

}
