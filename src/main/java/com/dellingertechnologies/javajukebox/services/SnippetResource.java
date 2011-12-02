package com.dellingertechnologies.javajukebox.services;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

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

}
