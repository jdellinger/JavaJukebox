package com.dellingertechnologies.javajukebox.model;

public class TrackInfo {
    
    private long duration;
    private String durationDisplay;
    private long bytes;
    private int trackId;

    public TrackInfo(int trackId, long duration, long bytes){
        this.trackId = trackId;
        this.duration = duration;
        this.bytes = bytes;
        initialize();
    }

    private void initialize() {
        long originalSeconds = duration/1000000;
        long minutes = originalSeconds/60;
        long seconds = originalSeconds % 60;
        durationDisplay = String.format("%d:%02d", minutes, seconds);
    }

    public long getDuration(){
        return duration;
    }

    public String getDurationDisplay() {
        return durationDisplay;
    }

    public long getBytes() {
        return bytes;
    }

    public int getTrackId() {
        return trackId;
    }
}
