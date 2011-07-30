package com.dellingertechnologies.javajukebox.finders;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dellingertechnologies.javajukebox.JukeboxDao;
import com.dellingertechnologies.javajukebox.finders.WeightedDBTrackFinder;
import com.dellingertechnologies.javajukebox.model.Track;

public class WeightedDBTrackFinderTest {

	private WeightedDBTrackFinder finder;

	@Before
	public void setup(){
		JukeboxDao dao = mock(JukeboxDao.class);
		when(dao.getTracks()).thenReturn(new ArrayList<Track>());
		finder = new WeightedDBTrackFinder(dao);
	}
	
	@Test
	public void shouldFavorTracksWithMoreLikes(){
		int count = 1000;
		double f1 = finder.getFitness(createTrack(0, 0, 0, 0, 0), count);
		double f2 = finder.getFitness(createTrack(0, 1, 0, 0, 0), count);
		double f3 = finder.getFitness(createTrack(0, 2, 0, 0, 0), count);
		assertTrue(f2 > f1);
		assertTrue(f3 > f2);
	}
	
	@Test
	public void shouldFavorTracksWithFewerDislikes(){
		int count = 1000;
		double f1 = finder.getFitness(createTrack(0, 0, 0, 0, 0), count);
		double f2 = finder.getFitness(createTrack(0, 0, 1, 0, 0), count);
		double f3 = finder.getFitness(createTrack(0, 0, 2, 0, 0), count);
		assertTrue(f2 < f1);
		assertTrue(f3 < f2);
	}

	@Test
	public void shouldFavorTracksWithFewerSkips(){
		int count = 1000;
		double f1 = finder.getFitness(createTrack(0, 0, 0, 0, 0), count);
		double f2 = finder.getFitness(createTrack(0, 0, 0, 1, 0), count);
		double f3 = finder.getFitness(createTrack(0, 0, 0, 2, 0), count);
		assertTrue(f2 < f1);
		assertTrue(f3 < f2);
	}

	@Ignore
	@Test
	public void outputTrackFitnessCalculation(){
		int i=0;
		int[] seq = new int[]{0,1,2,3,4};
		System.out.println("Likes");
		for(int x : seq){
			showFitness(createTrack(i++, x, 0, 0, 0), 3000);
		}
		
		System.out.println("Dislikes");
		for(int x : seq){
			showFitness(createTrack(i++, 0, x, 0, 0), 3000);
		}
		
		System.out.println("Skips");
		for(int x : seq){
			showFitness(createTrack(i++, 0, 0, x, 0), 3000);
		}

		System.out.println("Likes/Dislikes");
		for(int x : seq){
			showFitness(createTrack(i++, x, x, 0, 0), 3000);
		}
	}

	private void showFitness(Track track, int count) {
		System.out.println(String.format("id: %s, fitness: %f", track.getId(), finder.getFitness(track, count)));
	}

	private Track createTrack(int id, int likes, int dislikes, int skips, int plays) {
		Track t = new Track();
		t.setId(id);
		t.setLikes(likes);
		t.setDislikes(dislikes);
		t.setSkips(skips);
		t.setPlays(plays);
		t.setLastPlayed(new DateTime().minusHours(5).toDate());
		return t;
	}
}
