package com.manheim.javajukebox;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

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
	
	public File nextTrack() {
		int idx = (int) Math.round(Math.random()*(files.size()-1));
		return files.get(idx);
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
