package com.dellingertechnologies.javajukebox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PlayMonitor implements Runnable {

    Jukebox jukebox;
    private static int THRESHOLD = 5;
    private long lastUpdated = System.currentTimeMillis();
    private long lastbytesread = 0;

    private Log log = LogFactory.getLog(PlayMonitor.class);

    public PlayMonitor(Jukebox jukebox){
        this.jukebox = jukebox;
    }

    public void run() {
        if(jukebox.isPlaying() && hasNotUpdatedRecently()){
            jukebox.playNextTrack();
        }
    }

    private boolean hasNotUpdatedRecently() {
        int seconds = (int) (System.currentTimeMillis()-lastUpdated)/1000;
        return (lastbytesread > 0 && seconds > THRESHOLD) ? true : false;
    }

    public void reset(){
        this.lastbytesread = 0L;
        lastUpdated = System.currentTimeMillis();
    }
    
    public void update(long bytesread){
        log.debug("update playermonitor "+bytesread);
        if(bytesread > lastbytesread){
            lastbytesread = bytesread;
            lastUpdated = System.currentTimeMillis();
        }
    }
}
