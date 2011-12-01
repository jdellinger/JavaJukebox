package com.dellingertechnologies.javajukebox;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class ShutdownProcessor {

	private String jukeboxHome;
	private Log log = LogFactory.getLog(ShutdownProcessor.class);
	private int port;

	public ShutdownProcessor(CommandLine cmd) {
		jukeboxHome = System.getProperty("JUKEBOX_HOME", ".");
		port = determinePort(cmd);
	}

	private int determinePort(CommandLine cmd) {
		int port = 0;
		try{
			File portFile = new File(jukeboxHome, "tmp/jukebox.port");
			port = NumberUtils.toInt(FileUtils.readFileToString(portFile), 0);
		}catch(Exception e){
			log.warn("Exception occurred trying to determine the port used by jukebox");
		}
		if(port == 0 && cmd.hasOption('p')){
			port = NumberUtils.toInt(cmd.getOptionValue('p'));
		}
		return port;
	}

	public void process() {
		if(port != 0){
			Client client = Client.create();
			String shutdownUrl = String.format("http://localhost:%s/service/jukebox/shutdown", port);
			WebResource resource = client.resource(shutdownUrl);
			resource.get(String.class);
		}
	}

}
