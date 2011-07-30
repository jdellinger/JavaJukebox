package com.dellingertechnologies.javajukebox.finders;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import com.dellingertechnologies.javajukebox.TrackFinder;
import com.dellingertechnologies.javajukebox.model.Track;

public class RandomFSTrackFinder implements TrackFinder {

	File musicDirectory = null;
	FilenameFilter filter = new FilenameFilter() {
		
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith("mp3");
		}
	};
	private List<File> files;
	
	public RandomFSTrackFinder(File directory){
		this.musicDirectory = directory;
		files = new ArrayList<File>();
		loadFiles(files, musicDirectory);
	}
	
	public Track nextTrack() {
		int idx = (int) Math.round(Math.random()*(files.size()-1));
		File file = files.get(idx);
		Track track = null;
		if(file != null){
			track = new Track();
			track.setPath(file.getAbsolutePath());
		}
		return track;
	}

	private void loadFiles(List<File> files, File parent) {
		if(parent != null && parent.isDirectory()){
			for(File file : parent.listFiles()){
				if(file.isDirectory()){
					loadFiles(files, file);
				}else if(filter.accept(parent, file.getName())){
					files.add(file);
				}
			}
		}
	}

}
