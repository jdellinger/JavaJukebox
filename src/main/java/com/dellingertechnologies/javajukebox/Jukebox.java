package com.dellingertechnologies.javajukebox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.dellingertechnologies.javajukebox.finders.MultiTrackFinder;
import com.dellingertechnologies.javajukebox.finders.QueueTrackFinder;
import com.dellingertechnologies.javajukebox.finders.WeightedDBTrackFinder;
import com.dellingertechnologies.javajukebox.model.Snippet;
import com.dellingertechnologies.javajukebox.model.Track;
import com.dellingertechnologies.javajukebox.model.User;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class Jukebox implements BasicPlayerListener {

	private Server server;
	private BasicPlayer player;
	private BasicPlayerEvent currentState;
	private Map currentFile;
	private Map currentProgress;
	private int port;
	private File directory;
	private boolean playing = false;

	private static int DEFAULT_PORT = 9999;
	private static String DEFAULT_DIRECTORY = "/music";
	private static String TMP_DIRECTORY = "/tmp";
	private static long TRACK_INTERVAL_MINUTES = 30;
	
	private static Jukebox jukebox;
	private JukeboxDao dao;
	private String jukeboxHome;
	private double lastVolume;
	private Track currentTrack;
	private File databaseDirectory;
	private Map<String,String> ratingHostCache = new HashMap<String,String>();
	private TrackFinder queueFinder;
	private MultiTrackFinder playFinder;
    private ScheduledExecutorService es = Executors.newScheduledThreadPool(3);

	private Queue<String> snippetQueue = new LinkedList<String>();

	private Log log = LogFactory.getLog(Jukebox.class);
	private FilesystemAlterationMonitor fam;
	private boolean isPlayingSnippet;
	public WeightedDBTrackFinder weightedFinder;
    private boolean explicitMode;

    public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		CommandLine cmd = null;
		try{
			cmd = parseOptions(args);
		}catch(ParseException e){
			e.printStackTrace();
		}
		if(isShutdownRequested(cmd)){
			new ShutdownProcessor(cmd).process();
		}else{
			Jukebox.getInstance().initialize(cmd);
			Jukebox.getInstance().powerOn();
		}
	}

	private static boolean isShutdownRequested(CommandLine cmd) {
		return cmd.hasOption("shutdown");
	}

	private void initialize(CommandLine cmd) throws Exception {
		this.jukeboxHome = System.getProperty("JUKEBOX_HOME", ".");
		this.port = cmd.hasOption('p') ? NumberUtils.toInt(cmd.getOptionValue('p')) : DEFAULT_PORT;
		this.directory = cmd.hasOption('d') ? new File(cmd.getOptionValue('d')) : new File(jukeboxHome + DEFAULT_DIRECTORY);
		this.databaseDirectory = cmd.hasOption("db") ? new File(cmd.getOptionValue("db"), "db") : new File(directory, "db");
        this.explicitMode = cmd.hasOption("x") ? true : false;
		log.info("Using directory: "+directory);
        log.info("Explicit mode: "+explicitMode);

		fam = new FilesystemAlterationMonitor();

		initializeDatabase();
		initializeJukebox();
		initializeServer();
	}

	private static CommandLine parseOptions(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("p", true, "Port for web server to accept requests");
		options.addOption("d", true, "Root directory where music files are located");
		options.addOption("db", true, "Root directory where database directory is located");
        options.addOption("x", false, "Run in explicit mode to allow explicit tracks to be played");
		options.addOption("shutdown", false, "Shutdown running server");
		CommandLineParser parser = new PosixParser();
		return parser.parse( options, args);
	}

	public static Jukebox getInstance() throws Exception {
		
		if (jukebox == null) {
			jukebox = new Jukebox();
		}
		return jukebox;
	}

	private void initializeJukebox() throws BasicPlayerException {
		log.info("Initializing jukebox...");
		player = new BasicPlayer();
		player.addBasicPlayerListener(this);
//		finder = new RandomFSTrackFinder(directory);
//		finder = new RandomDBTrackFinder(dao);
//		finder = new MultiTrackFinder(
//				new QueueTrackFinder(dao),
//				new RandomDBTrackFinder(dao));
		weightedFinder = new WeightedDBTrackFinder(dao); 
		playFinder = new MultiTrackFinder(
				new QueueTrackFinder(dao),
				weightedFinder);
		queueFinder = weightedFinder;
	}

	private void initializeServer() throws Exception {
		log.info("Initializing server...");
		Logger.getLogger("com.sun").setLevel( Level.SEVERE );
		ServletHolder restServices = new ServletHolder(ServletContainer.class);

		restServices.setInitParameter(
				"com.sun.jersey.config.property.resourceConfigClass",
				"com.sun.jersey.api.core.PackagesResourceConfig");
		restServices.setInitParameter("com.sun.jersey.config.property.packages",
				"com.dellingertechnologies.javajukebox");

		writePort();
		server = new Server(port);
		Context context = new Context(server, "/service", Context.SESSIONS);
		context.addServlet(restServices, "/*");
		
		ResourceHandler contentHandler = new ResourceHandler();
		contentHandler.setResourceBase(jukeboxHome + "/content");
		server.addHandler(contentHandler);
		server.start();
	}

	private void writePort() throws IOException {
		File portFile = new File(jukeboxHome+TMP_DIRECTORY, "jukebox.port");
		portFile.getParentFile().mkdirs();
		FileUtils.writeStringToFile(portFile, String.valueOf(port));
	}

	private void initializeDatabase() throws Exception {
		log.info("Initializing database...");
		dao = new JukeboxDao(databaseDirectory);
		initializeSettings();
		TrackScanner scanner = new TrackScanner(directory, dao);
		if(dao.hasTracks(true)){
			es.scheduleAtFixedRate(scanner, 0, TRACK_INTERVAL_MINUTES, TimeUnit.MINUTES);
		}else{
			log.info("No tracks in db...loading tracks before startup");
			Future<?> f = es.submit(scanner);
			f.get();
			es.scheduleAtFixedRate(scanner, TRACK_INTERVAL_MINUTES, TRACK_INTERVAL_MINUTES, TimeUnit.MINUTES);
		}
		writeIJProperties();
	}
	
	private void writeIJProperties() throws IOException {
		File file = new File(jukeboxHome+TMP_DIRECTORY, "ij.properties");
		file.getParentFile().mkdirs();
		FileUtils.writeStringToFile(file, String.format("ij.database=jdbc:derby://localhost:1527/%s", databaseDirectory.getAbsolutePath()));
	}

	private void initializeSettings(){
		log.info("Initializing settings...");
		dao.addOrUpdateUser(User.DEFAULT);
		File settingsFile = new File(directory, "settings.jbx");
		SettingsFileListener settingsFileListener = new SettingsFileListener(dao);
		settingsFileListener.onFileChange(settingsFile);
		
		fam.addListener(settingsFile, settingsFileListener);
	}

	public void powerOn() throws BasicPlayerException {
		log.info("Jukebox...powering up");
		fam.start();
		playNextTrack();
		player.setGain(1.0);
		this.lastVolume = 1.0;
	}

	public void powerOff() throws Exception {
		log.info("Jukebox...shutting down");
		es.submit(new Runnable() {

            public void run() {
                try {
                    stopPlayer(player);
                    fam.stop();
                    dao.shutdown();
                    server.stop();
                } catch (Exception e) {
                    log.warn("Exception shutting down server");
                }
                System.exit(0);
            }
        });
	}

	public BasicPlayer getPlayer() {
		return player;
	}

	public boolean isPlaying(){
		return playing;
	}
	
	public double getVolume(){
		double minGainDB = player.getMinimumGain();
        double ampGainDB = ((10.0f / 20.0f) * player.getMaximumGain()) - player.getMinimumGain();
        double cste = Math.log(10.0) / 20;
        return (Math.exp( ((player.getGainValue() - minGainDB) * cste)) - 1) / (Math.exp(cste * ampGainDB) - 1);
	}
	
	public void opened(Object resource, Map properties) {
		this.currentFile = properties;
		log.debug("opened : " + properties.toString());
	}

	public void progress(int bytesread, long microseconds, byte[] pcmdata,
			Map properties) {
		this.currentProgress = properties;
		checkSnippets();
	}

	private void checkSnippets() {
		String token = snippetQueue.poll();
		if(token != null){
			playSnippet(token);
		}
	}

	public boolean isPlayingSnippet(){
		return isPlayingSnippet;
	}
	
	public void setController(BasicController controller) {

	}

	public void stateUpdated(BasicPlayerEvent event) {
		this.currentState = event;
		
		switch(event.getCode()){
			case BasicPlayerEvent.EOM:
				playNextTrack();
				break;
			case BasicPlayerEvent.GAIN:
				break;
			case BasicPlayerEvent.OPENED:
				break;
			case BasicPlayerEvent.OPENING:
				break;
			case BasicPlayerEvent.PAN:
				break;
			case BasicPlayerEvent.PAUSED:
				playing = false;
				break;
			case BasicPlayerEvent.PLAYING:
				playing = true;
				break;
			case BasicPlayerEvent.RESUMED:
				playing = true;
				break;
			case BasicPlayerEvent.SEEKED:
				break;
			case BasicPlayerEvent.SEEKING:
				break;
			case BasicPlayerEvent.STOPPED:
				playing = false;
				break;
			case BasicPlayerEvent.UNKNOWN:
				break;
		}
		log.debug("stateUpdated : " + event.toString());
	}

	public boolean pauseTrack() {
		try{
			if(player.getStatus() == BasicPlayer.PLAYING){
				player.pause();
				return true;
			}
			return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean resumeTrack() {
		try{
			if(player.getStatus() == BasicPlayer.PAUSED && !isPlayingSnippet()){   
				player.resume();
				return true;
			}
			return false;
		}catch(Exception e){
			log.warn("Exception resuming track", e);
			return false;
		}
	}

	public boolean skipTrack() {
		if(!isPlayingSnippet()){
			currentTrack.incrementSkips();
			dao.addOrUpdateTrack(currentTrack);
			return playNextTrack();
		}else {
			return false;
		}
	}
	public boolean playNextTrack() {
		Track track = null;
        stopPlayer(player);
        boolean nextTrackPlaying = false;
        while (!nextTrackPlaying) {
            track = playFinder.nextTrack();
            if (track != null && (!track.isExplicit() || explicitMode)) {
                File trackFile = new File(track.getPath());
                if (trackFile.exists() && trackFile.canRead()) {
                    try {
                        player.open(trackFile);
                        currentTrack = track;
                        clearRatingCache();
                        player.play();
                        player.setGain(lastVolume);
                        currentTrack.incrementPlays();
                        currentTrack.setLastPlayed(new Date());
                        dao.addOrUpdateTrack(currentTrack);
                        nextTrackPlaying = true;
                    } catch (BasicPlayerException e) {
                        stopPlayer(player);
                        disableTrack(track);
                        log.warn("Exception occurred playing track", e);
                    }
                } else {
                    disableTrack(track);
                }
            }
        }
        return nextTrackPlaying;
	}

    private void disableTrack(Track track) {
        track.setEnabled(false);
        dao.addOrUpdateTrack(track);
    }

    private void stopPlayer(BasicPlayer player){
        try{
            player.stop();
        }catch(BasicPlayerException bpe){
            log.warn("Exception stopping player");
        }
    }
	public List<Track> searchTracks(String searchText) {
		return dao.searchTracks(searchText);
	}

	private void clearRatingCache() {
		ratingHostCache.clear();
	}

	private synchronized void sleep(long delay) {
		try {
			wait(delay);
		} catch (InterruptedException ie1) {
			/* ignore. */
		}
	}

	public Map getCurrentProgress(){
		return this.currentProgress;
	}
	
	public Map getCurrentFileProperties() {
		return this.currentFile;
	}

	public BasicPlayerEvent getCurrentState() {
		return currentState;
	}

	public void setVolume(double volume) throws BasicPlayerException {
		player.setGain(volume);
		this.lastVolume=volume;
	}

	public Track getCurrentTrack() {
		return currentTrack;
	}

	public File getDirectory() {
		return directory;
	}

    public boolean isExplicitMode() {
        return explicitMode;
    }

	public void likeCurrentTrack(String remoteAddress){
		if(canAddRating(remoteAddress)){
			addToRatingCache(remoteAddress, "LIKE");
			currentTrack.incrementLikes();
			dao.addOrUpdateTrack(currentTrack);
		}
	}

	private void addToRatingCache(String remoteAddress, String rating) {
		ratingHostCache.put(remoteAddress, rating);
	}

	private boolean canAddRating(String remoteAddress) {
		if(remoteAddress == null){
			return false;
		}else{
			return !ratingHostCache.containsKey(remoteAddress);
		}
	}

	public void dislikeCurrentTrack(String remoteAddress){
		if(canAddRating(remoteAddress)){
			addToRatingCache(remoteAddress, "DISLIKE");
			currentTrack.incrementDislikes();
			dao.addOrUpdateTrack(currentTrack);
		}
	}

	public void explicitTrack(boolean b) {
		currentTrack.setExplicit(true);
		dao.addOrUpdateTrack(currentTrack);
        if(!explicitMode){
            skipTrack();
        }
	}

	public String getRating(String remoteAddress) {
		return ratingHostCache.get(remoteAddress);
	}

	public void addToQueue(int numberOfTracks) {
		for(int i=0;i<numberOfTracks;i++){
			Track track = queueFinder.nextTrack();
			if(track != null && track.getId()>0){
				addTrackToQueue(track.getId());
			}
		}
	}

	public List<Track> getQueue() {
		return dao.getQueue();
	}

	public void addTrackToQueue(int idToAdd) {
		dao.addTrackToQueue(idToAdd);
	}

	public void removeTrackFromQueue(int trackId) {
		dao.removeTrackFromQueue(trackId);
	}

	public boolean enqueueSnippet(String token) {
		return snippetQueue.offer(token);
	}
	
	public void clearSnippetQueue(){
		snippetQueue.clear();
	}
	
	public boolean playSnippet(String token) {
		Snippet snippet = dao.getSnippetByToken(token);
		if (snippet != null) {
			Track track = dao.getTrack(snippet.getTrackId());
			if (track != null) {
				isPlayingSnippet = true;
				pauseTrack();
				new SnippetPlayer(snippet, track, lastVolume).play();
				isPlayingSnippet = false;
				resumeTrack();
				return true;
			}
		}
		return false;
	}

	public boolean restartTrack() {
		if(!isPlayingSnippet()){
			try{
				long bytes = ((Long)getCurrentProgress().get("mp3.position.byte")).longValue();
				player.seek(-bytes);
			}catch(Exception e){
				log.warn("Could not seek to start of file", e);
			}
			return true;
		}else{
			return false;
		}
	}

	public List<Snippet> getSnippetsQueue() {
		List<Snippet> snippets = new ArrayList<Snippet>();
		for(String token : snippetQueue.toArray(new String[]{})){
			snippets.add(dao.getSnippetByToken(token));
		}
		return snippets;
	}

	public List<Snippet> getSnippets() {
		return dao.getSnippets();
	}
}
