package com.dellingertechnologies.javajukebox.finders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.dellingertechnologies.javajukebox.JukeboxDao;
import com.dellingertechnologies.javajukebox.TrackFinder;
import com.dellingertechnologies.javajukebox.model.Track;

public class WeightedDBTrackFinder implements TrackFinder {

	private JukeboxDao dao;
	private Map<Track,Double> weights = new HashMap<Track, Double>();
	private double calculatedTotal;
	
	private Random random = null;
	
	private Log log = LogFactory.getLog(WeightedDBTrackFinder.class);
	private CircularFifoBuffer recentlyReturned = new CircularFifoBuffer(5);
	
	public WeightedDBTrackFinder(JukeboxDao dao){
		this.dao = dao;
		random = new Random(System.currentTimeMillis());
		ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
		Runnable refreshWeights = new Runnable(){
			public void run() {
				refreshWeights();
			}
		};
		Future<?> future = es.submit(refreshWeights);
		try{
			future.get();
		}catch(Exception e){
			log.warn("Exception refreshing weights", e);
		}
		es.scheduleAtFixedRate(refreshWeights, 0, 3, TimeUnit.MINUTES);
	}
	
	private void refreshWeights() {
		Map<Track,Double> tmpWeights = new HashMap<Track, Double>();
		List<Track> tracks = dao.getTracks();
		double totalFitness = 0;
		double minFitness = Integer.MAX_VALUE;
		Map<Track,Double> tfs = new HashMap<Track,Double>();
		for(Track track : tracks){
			double f = getFitness(track, tracks.size());
			tfs.put(track, f);
			minFitness = f < minFitness ? f : minFitness;
		}
		for(Map.Entry<Track, Double> entry : tfs.entrySet()){
			double f = entry.getValue();
			f = f - minFitness + 1;
			entry.setValue(f);
			totalFitness += f;
		}
		
		for(Map.Entry<Track, Double> entry : tfs.entrySet()){
			tmpWeights.put(entry.getKey(), entry.getValue());
		}
//			log.debug(StringUtils.join(
//					new Object[]{
//							track.getId(),
//							fitness,
//							modifiedFitness,
//							totalFitness,
//							modifiedTotalFitness
//					}
//					,":"));
		synchronized(this){
			weights = tmpWeights;
			calculatedTotal = totalFitness;
		}
	}

	protected double getFitness(Track track, int count) {
		// likeFactor: for 20 likes, should be approx. 1/100 odds for selection if all others have fitness of 1
		double likeFactor = count/300.0;
		double dislikeFactor = likeFactor/2.0;
		double skipFactor = dislikeFactor/10.0;
		double lastPlayedModifier = likeFactor*2.5;
		
		double modLikes = track.getLikes() > 0 ? likeFactor*Math.log(track.getLikes()+1) : 0.0;
		double modDislikes = track.getDislikes() > 0.0 ? dislikeFactor*Math.log(track.getDislikes()+1) : 0.0;
		double modSkips = track.getSkips() > 0.0 ? skipFactor*Math.log(track.getSkips()+1) : 0.0;
		
		double modTime = 0.0;
		if(track.getLastPlayed() != null){
			DateTime hourAgo = new DateTime().minusHours(1);
			DateTime lastPlayed = new DateTime(track.getLastPlayed());
			if(lastPlayed.isAfter(hourAgo)){
				modTime = lastPlayedModifier;
			}
		}
		return modLikes - modDislikes - modSkips - modTime;
	}

	public Map<Track,Double> getWeights(){
		return weights;
	}
	
	public Track nextTrack() {
		double randomWeight = random.nextDouble()*calculatedTotal;
		double currentWeight = 0;
		synchronized(this){
			for(Map.Entry<Track, Double> entry : weights.entrySet()){
				currentWeight += entry.getValue();
				Track t = entry.getKey();
				if(currentWeight >= randomWeight && !recentlyReturned.contains(t)){
					recentlyReturned .add(t);
					return t;
				}
			}
		}
		return null;
	}

}
