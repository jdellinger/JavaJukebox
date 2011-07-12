package com.dellingertechnologies.javajukebox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dellingertechnologies.javajukebox.model.Track;
import com.dellingertechnologies.javajukebox.model.User;
import com.mpatric.mp3agic.Mp3File;

public class TrackScanner implements Runnable {

	private File baseDir;
	private JukeboxDao dao;

	private Log log = LogFactory.getLog(TrackScanner.class);
	
	private FilenameFilter mp3Filter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith("mp3");
		}
	};
	private FilenameFilter jbxFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.toLowerCase().equals("user.jbx");
		}
	};

	public TrackScanner(File baseDir, JukeboxDao dao) {
		this.baseDir = baseDir;
		this.dao = dao;
	}

	public void run() {
		List<FileWrapper> files = new ArrayList<FileWrapper>();
		loadFiles(files, baseDir, User.DEFAULT);
		
		ExecutorService es = Executors.newSingleThreadExecutor();
		try {
			Future<List<Track>> future = es.submit(new TrackProcessor(files));
			List<Track> tracks = future.get();
			for (Track track : tracks) {
				int id = dao.getTrackIdByChecksum(track.getChecksum());
				if(id == 0){
					dao.addOrUpdateTrack(track);
				}else{
					Track existingTrack = dao.getTrack(id);
					existingTrack.setPath(track.getPath());
					existingTrack.setEnabled(true);
					existingTrack.setUser(track.getUser());
					dao.addOrUpdateTrack(existingTrack);
				}
			}
		} catch (Exception e) {
			log.error("Exception loading tracks", e);
		}
		es.shutdown();
	}

	private void loadFiles(List<FileWrapper> files, File parent, User parentUser) {
		if (parent != null && parent.isDirectory()) {
			User user = checkForUser(parent, parentUser);
			for (File file : parent.listFiles()) {
				if (file.isDirectory()) {
					loadFiles(files, file, user);
				} else if (mp3Filter.accept(parent, file.getName())) {
					files.add(new FileWrapper(file, user));
				}
			}
		}
	}

	private User checkForUser(File parent, User parentUser) {
		User user = dao.getUserByUsername(parent.getName().toLowerCase());
		if(user == null){
			user = parentUser;
		}
		return user;
	}

	private class TrackProcessor implements Callable<List<Track>> {
		List<FileWrapper> files;
		private boolean verbose = false;
		
		public TrackProcessor(List<FileWrapper> files){
			this(files, false);
		}
		public TrackProcessor(List<FileWrapper> files, boolean verbose){
			this.files = files;
			this.verbose = verbose;
		}
		public List<Track> call() throws Exception {
			long start = System.currentTimeMillis();
			log.info("Starting scan...");
			List<Track> tracks = new ArrayList<Track>();
			int i = 1;
			int totalCount = files.size();
			for (FileWrapper fileWrapper : files) {
				try {
					 Map p = readTags(fileWrapper.file);
					Track track = new Track();
					track.setTitle(StringUtils.trimToEmpty((String) p
							.get("title")));
					track.setAlbum(StringUtils.trimToEmpty((String) p
							.get("album")));
					track.setArtist(StringUtils.trimToEmpty((String) p
							.get("author")));
					 track.setChecksum(createChecksum(fileWrapper.file));
					track.setPath(fileWrapper.file.getAbsolutePath());
					track.setUser(fileWrapper.user);
					tracks.add(track);
				} catch (Exception e) {
					log.warn("Problem scanning file: "
							+ fileWrapper.file.getPath());
				}
				if(verbose  && i%100 == 0)
					log.info("Scanning files..."+i+"/"+totalCount);
				i++;
			}
			long seconds = (long) ((System.currentTimeMillis() - start) / 1000.0);
			log.info("Scan complete..." + tracks.size()
					+ " tracks in " + seconds + " seconds");
			return tracks;
		}

		private Map readTags(File file) {
			HashMap<String, String> tags = new HashMap<String, String>();
			try{
				Mp3File mp3file = new Mp3File(file.getAbsolutePath(), false);
				if(mp3file.hasId3v1Tag()){
					tags.put("title", mp3file.getId3v1Tag().getTitle());
					tags.put("author", mp3file.getId3v1Tag().getArtist());
					tags.put("album", mp3file.getId3v1Tag().getAlbum());
				}else if(mp3file.hasId3v2Tag()){
					tags.put("title", mp3file.getId3v2Tag().getTitle());
					tags.put("author", mp3file.getId3v2Tag().getArtist());
					tags.put("album", mp3file.getId3v2Tag().getAlbum());
				}
			}catch(Exception e){
				log.warn("Exception reading tags for " + file.getPath(), e);
			}
			return tags;
		}

		private long createChecksum(File file) {
			long result = -1;
			try {
				Checksum checksum = new CRC32();
				FileInputStream fis = new FileInputStream(file);
				FileChannel fic = fis.getChannel();
				
				int bytesToRead = 20*1024;
				ByteBuffer buffer = ByteBuffer.allocate(bytesToRead);
				int bytesRead = fic.read(buffer);
				if(bytesRead > 0){
					buffer.flip();
					checksum.update(buffer.array(), 0, bytesRead);
				}
				fic.close();
				fis.close();

				result = checksum.getValue();
			} catch (Exception e) {
				log.warn("Exception creating checksum", e);
			}
			return result;
		}
	}

	private class FileWrapper {
		File file;
		User user;
		public FileWrapper(File file, User user) {
			this.file = file;
			this.user = user;
		}
	}
}