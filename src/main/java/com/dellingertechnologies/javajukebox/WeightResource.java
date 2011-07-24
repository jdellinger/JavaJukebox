package com.dellingertechnologies.javajukebox;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONObject;

import com.dellingertechnologies.javajukebox.model.Track;

@Path("/weights")
public class WeightResource {

	
	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject status(@Context HttpServletRequest request) throws Exception{
		Map<Track,Double> weights = Jukebox.getInstance().weightedFinder.getWeights();
		JSONObject w = new JSONObject();
		for(Map.Entry<Track, Double> entry : weights.entrySet()){
			w.put(String.valueOf(entry.getKey().getId()), entry.getValue());
		}
		return w;
	}


}
