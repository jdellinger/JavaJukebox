package com.dellingertechnologies.javajukebox;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONObject;

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
}
