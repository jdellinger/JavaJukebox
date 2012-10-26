package com.dellingertechnologies.javajukebox.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class TrackInfoTest {
    
    @Test
    public void durationDisplayShouldWork(){
        assertEquals("0:45", new TrackInfo(0, 1000000*45, 0).getDurationDisplay());
        assertEquals("1:15", new TrackInfo(0, 1000000*75, 0).getDurationDisplay());
        assertEquals("1:00", new TrackInfo(0, 1000000*60, 0).getDurationDisplay());
    }
}
