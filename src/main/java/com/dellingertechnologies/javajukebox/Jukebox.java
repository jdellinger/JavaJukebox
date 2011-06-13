package com.dellingertechnologies.javajukebox;

import java.io.File;
import java.util.Collection;
import java.util.Map;

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
import org.apache.commons.lang.math.NumberUtils;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.sun.jersey.spi.container.servlet.ServletContainer;

public class Jukebox implements BasicPlayerListener {

	private Server server;
	private BasicPlayer player;
	private TrackFinder finder;
	private BasicPlayerEvent currentState;
	private Map currentFile;
	private Map currentProgress;
	private int port;
	private File directory;
	private boolean playing = false;

	private static int DEFAULT_PORT = 9999;
	private static String DEFAULT_DIRECTORY = "music";
	
	private static Jukebox jukebox;
	private JukeboxDao dao;
	private String jukeboxHome;
	private double lastVolume;
	private File currentTrack;

	public static void main(String[] args) throws Exception {
		CommandLine cmd = null;
		try{
			cmd = parseOptions(args);
		}catch(ParseException e){
			e.printStackTrace();
		}
		Jukebox.getInstance().initialize(cmd);
		Jukebox.getInstance().powerOn();
	}

	private void initialize(CommandLine cmd) throws Exception {
		this.port = cmd.hasOption('p') ? NumberUtils.toInt(cmd.getOptionValue('p')) : DEFAULT_PORT;
		this.directory = cmd.hasOption('d') ? new File(cmd.getOptionValue('d')) : new File(DEFAULT_DIRECTORY);
		this.jukeboxHome = System.getProperty("JUKEBOX_HOME", ".");
		System.out.println("directory: "+directory);

		initializeDatabase();
		initializeJukebox();
		initializeServer();
	}

	private static CommandLine parseOptions(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("p", true, "Port for web server to accept requests");
		options.addOption("d", true, "Root directory where music files are located");
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
		player = new BasicPlayer();
		player.addBasicPlayerListener(this);
		finder = new RandomFSTrackFinder(directory);
	}

	private void initializeServer() throws Exception {
		ServletHolder restServices = new ServletHolder(ServletContainer.class);

		restServices.setInitParameter(
				"com.sun.jersey.config.property.resourceConfigClass",
				"com.sun.jersey.api.core.PackagesResourceConfig");
		restServices.setInitParameter("com.sun.jersey.config.property.packages",
				"com.dellingertechnologies.javajukebox");

		server = new Server(port);
		Context context = new Context(server, "/service", Context.SESSIONS);
		context.addServlet(restServices, "/*");
		
		ResourceHandler contentHandler = new ResourceHandler();
		contentHandler.setResourceBase(jukeboxHome + "/content");
		server.addHandler(contentHandler);
		server.start();
	}

	private void initializeDatabase() throws Exception {
		dao = new JukeboxDao(jukeboxHome+"/db");
	}

	public void powerOn() throws BasicPlayerException {
		playNextTrack();
		player.setGain(1.0);
		this.lastVolume = 1.0;
	}

	public void powerOff() throws Exception {
		player.stop();
		dao.cleanup();
		server.stop();
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
		display("opened : " + properties.toString());
	}

	public void progress(int bytesread, long microseconds, byte[] pcmdata,
			Map properties) {
		this.currentProgress = properties;
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
		display("stateUpdated : " + event.toString());
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
			if(player.getStatus() == BasicPlayer.PAUSED){
				player.resume();
				return true;
			}
			return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public boolean playNextTrack() {
		try {
			player.stop();
			File track = finder.nextTrack();
			player.open(track);
			currentTrack = track;
			player.play();
			player.setGain(lastVolume);
		} catch (Exception e) {
			e.printStackTrace();
			return playNextTrack();
		}
		return true;
	}

	private void display(Object obj) {
		if (obj != null) {
			System.out.println(obj.toString());
		}
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

	public File getCurrentFile() {
		return currentTrack;
	}

	public File getDirectory() {
		return directory;
	}
}
