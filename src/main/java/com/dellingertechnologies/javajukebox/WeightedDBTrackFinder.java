package com.dellingertechnologies.javajukebox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.dellingertechnologies.javajukebox.model.Track;

public class WeightedDBTrackFinder implements TrackFinder {

	private JukeboxDao dao;
	private Map<Track,Double> weights = new HashMap<Track, Double>();
	
	private double baseFitness = 0;
	private Random random = null;
	
	private Log log = LogFactory.getLog(WeightedDBTrackFinder.class);
	
	public WeightedDBTrackFinder(JukeboxDao dao){
		this.dao = dao;
		random = new Random(System.currentTimeMillis());
		ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
		es.scheduleAtFixedRate(new Runnable(){
			public void run() {
				refreshWeights();
			}
		}, 0, 30, TimeUnit.MINUTES);
	}
	
	private void refreshWeights() {
		Map<Track,Double> tmpWeights = new HashMap<Track, Double>();
		List<Track> tracks = dao.getTracks();
		double totalFitness = 0;
		double minFitness = Integer.MAX_VALUE;
		for(Track track : tracks){
			double fitness = getFitness(track, tracks.size());
			totalFitness += fitness;
			minFitness = fitness < minFitness ? fitness : minFitness;
		}
		baseFitness = 1 - minFitness;
		double modifiedTotalFitness = totalFitness + (baseFitness * tracks.size());
		for(Track track : tracks){
			double fitness = getFitness(track, tracks.size());
			double modifiedFitness = baseFitness + fitness;
			tmpWeights.put(track, modifiedFitness/modifiedTotalFitness);
//			log.debug(StringUtils.join(
//					new Object[]{
//							track.getId(),
//							fitness,
//							modifiedFitness,
//							totalFitness,
//							modifiedTotalFitness
//					}
//					,":"));
		}
		synchronized(this){
			weights = tmpWeights;
		}
	}

	private double getFitness(Track track, int count) {
		// likeFactor: for 20 likes, should be approx. 1/100 odds for selection if all others have fitness of 1
		double likeFactor = count/300;
		double dislikeFactor = likeFactor/2;
		double modLikes = track.getLikes() > 0 ? likeFactor*Math.log(track.getLikes()) : 0;
		double modDislikes = track.getDislikes() > 0 ? dislikeFactor*Math.log(track.getDislikes()) : 0;
		double modSkips = .1 * track.getSkips();
		DateTime hourAgo = new DateTime().minusHours(1);
		DateTime lastPlayed = new DateTime(track.getLastPlayed());
		double modTime = 0;
		if(lastPlayed.isAfter(hourAgo)){
			modTime = 100;
		}
		return modLikes - modDislikes - modSkips - modTime;
	}

	public Track nextTrack() {
		double randomWeight = random.nextDouble();
		double currentWeight = 0;
		synchronized(this){
			for(Map.Entry<Track, Double> weight : weights.entrySet()){
				currentWeight += weight.getValue();
				if(currentWeight >= randomWeight){
					return weight.getKey();
				}
			}
		}
		return null;
	}

}
