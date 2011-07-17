package com.dellingertechnologies.javajukebox;

import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import com.dellingertechnologies.javajukebox.model.Snippet;
import com.dellingertechnologies.javajukebox.model.Track;

public class SnippetPlayer implements BasicPlayerListener {

	private Snippet snippet;
	private Track track;
	private double volume;
	private Log log = LogFactory.getLog(SnippetPlayer.class);
	private volatile boolean done;
	private BasicPlayer player;
	
	public SnippetPlayer(Snippet s, Track t, double volume){
		this.snippet = s;
		this.track = t;
		this.volume = volume;
	}
	
	public void play() {
		try{
			done = false; 
			player = new BasicPlayer();
			player.addBasicPlayerListener(this);
			player.open(new File(track.getPath()));
			player.seek(snippet.getStartPosition());
			player.play();
			player.setGain(volume);
			while(!done){
				Thread.sleep(100);
			}
			player.stop();
		}catch(Exception e){
			log.warn("Could not play snippet", e);
		}
	}

	public void opened(Object arg0, Map arg1) {
	}

	public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {
		if(bytesread > snippet.getEndPosition()){
			done = true;
		}
	}

	public void setController(BasicController controller) {
	}

	public void stateUpdated(BasicPlayerEvent event) {
		if(event.getCode() == BasicPlayerEvent.STOPPED ||
				event.getCode() == BasicPlayerEvent.EOM){
			done = true;
		}
	}
}
