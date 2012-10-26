package com.dellingertechnologies.javajukebox;

import com.dellingertechnologies.javajukebox.model.Track;
import com.dellingertechnologies.javajukebox.model.TrackInfo;
import javazoom.jlgui.basicplayer.*;

import java.io.File;
import java.util.Map;

public class TrackExaminer implements BasicPlayerListener {
    
    BasicPlayer player;
    private Track track;
    private Map properties;

    public TrackExaminer(Track t) {
        this.track = t;
        player = new BasicPlayer();
        player.addBasicPlayerListener(this);
        try {
            player.open(new File(track.getPath()));
            player.seek(0);
        } catch (BasicPlayerException e) {
            e.printStackTrace();
        }
    }

    public TrackInfo getTrackInfo(){
        return new TrackInfo(track.getId(), getDuration(), getBytes());
    }
    protected long getDuration(){
        return (Long) properties.get("duration");
    }
    
    protected int getBytes(){
        return (Integer) properties.get("mp3.length.bytes");
    }
    
    public void opened(Object o, Map map) {
        this.properties = map;
        System.out.println("opened");
//        System.out.println("opened");
//        "audio.length.frames";
//        "mp3.framesize.bytes";
//        "mp3.length.frames";
//        "mp3.bitrate.nominal.bps";
//        "audio.length.bytes";
//        "audio.framerate.fps";
//        "mp3.length.bytes";
//        "duration";
    }

    public void progress(int i, long l, byte[] bytes, Map map) {
    }

    public void stateUpdated(BasicPlayerEvent basicPlayerEvent) {
    }

    public void setController(BasicController basicController) {
    }
}
