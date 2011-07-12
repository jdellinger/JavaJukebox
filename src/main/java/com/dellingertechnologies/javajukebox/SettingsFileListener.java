package com.dellingertechnologies.javajukebox;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.monitor.FilesystemAlterationObserver;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dellingertechnologies.javajukebox.model.User;

public class SettingsFileListener implements FilesystemAlterationListener {

	private JukeboxDao dao;
	private static Log log = LogFactory.getLog(SettingsFileListener.class);
	
	public SettingsFileListener(JukeboxDao dao){
		this.dao = dao;
	}
	
	public void onDirectoryChange(File arg0) {
	}

	public void onDirectoryCreate(File arg0) {
	}

	public void onDirectoryDelete(File arg0) {
	}

	public void onFileChange(File file) {
		if(file.exists() && file.canRead()){
			//load settings
			List<User> existingUsers = dao.getUsers();
			existingUsers.remove(User.DEFAULT);
			try{
				Properties p = new Properties();
				p.load(FileUtils.openInputStream(file));
				String[] users = StringUtils.split(p.getProperty("users").toLowerCase(), ",");
				for(String username : users){
					User user = new User(username);
					user.setGravatarId(p.getProperty(username+".gravatarId"));
					dao.addOrUpdateUser(user);
					existingUsers.remove(user);
				}
			}catch(Exception e){
				log.warn("Exception processing settings.jbx file: "+file.getPath());
			}
			for(User user : existingUsers){
				dao.deleteUser(user);
			}
		}
	}

	public void onFileCreate(File arg0) {
	}

	public void onFileDelete(File arg0) {
	}

	public void onStart(FilesystemAlterationObserver arg0) {
	}

	public void onStop(FilesystemAlterationObserver arg0) {
	}

}
