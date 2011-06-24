package com.dellingertechnologies.javajukebox;

import javax.sql.DataSource;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Locator {

	private static Locator _instance;

	private BeanFactory beanfactory;
	private Locator(){
		beanfactory = new ClassPathXmlApplicationContext("jukebox-context.xml");
	}
	
	private static synchronized Locator getInstance() {
		if(_instance == null){
			_instance = new Locator();
		}
		return _instance;
	}
	
	private BeanFactory getBeanFactory(){
		return beanfactory;
	}
	
	public static DataSource getDataSource(){
		return getInstance().getBeanFactory().getBean("datasource", DataSource.class);
	}
}
