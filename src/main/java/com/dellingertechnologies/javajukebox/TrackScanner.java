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
import java.util.concurrent.FutureTask;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import org.apache.commons.lang.StringUtils;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import com.dellingertechnologies.javajukebox.model.Track;
import com.mpatric.mp3agic.Mp3File;

public class TrackScanner  {

	private File baseDir;
	private JukeboxDao dao;

	private FilenameFilter mp3Filter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith("mp3");
		}
	};

	public TrackScanner(File baseDir, JukeboxDao dao) {
		this.baseDir = baseDir;
		this.dao = dao;
	}

	public void loadTracks() {
		List<File> files = new ArrayList<File>();
		loadFiles(files, baseDir);
		
		ExecutorService es = Executors.newSingleThreadExecutor();
		try {
			Future<List<Track>> future = es.submit(new TrackProcessor(files));
			List<Track> tracks = future.get();
			for (Track track : tracks) {
				dao.addOrUpdateTrack(track);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		es.shutdown();
	}

	private void loadFiles(List<File> files, File parent) {
		if (parent != null && parent.isDirectory()) {
			for (File file : parent.listFiles()) {
				if (file.isDirectory()) {
					loadFiles(files, file);
				} else if (mp3Filter.accept(parent, file.getName())) {
					files.add(file);
				}
			}
		}
	}

	private class TrackProcessor implements Callable<List<Track>> {
		List<File> files;
		public TrackProcessor(List<File> files){
			this.files = files;
		}
		public List<Track> call() throws Exception {
			long start = System.currentTimeMillis();
			System.out.println("Starting scan...");
			List<Track> tracks = new ArrayList<Track>();
			int i = 1;
			int totalCount = files.size();
			for (File file : files) {
				try {
					 Map p = readTags(file);
					Track track = new Track();
					track.setTitle(StringUtils.trimToEmpty((String) p
							.get("title")));
					track.setAlbum(StringUtils.trimToEmpty((String) p
							.get("album")));
					track.setArtist(StringUtils.trimToEmpty((String) p
							.get("author")));
					 track.setChecksum(createChecksum(file));
					track.setPath(file.getAbsolutePath());
					// track.setUser("");
					tracks.add(track);
				} catch (Exception e) {
					System.out.println("Problem scanning file: "
							+ file.getPath());
				}
				if(i%100 == 0)
					System.out.println("Scanning files..."+i+"/"+totalCount);
				i++;
			}
			long seconds = (long) ((System.currentTimeMillis() - start) / 1000.0);
			System.out.println("Scan complete..." + tracks.size()
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
				e.printStackTrace();
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
				e.printStackTrace();
			}
			return result;
		}
	}

}