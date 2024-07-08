package softtest.dts.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import softtest.config.java.Config;

public class Log4jInit { 
	static Logger logger = Logger.getLogger(Log4jInit.class); 
	
	public Log4jInit() 
	{ } 
	
	public void init(String projectName){ 
		long start = System.currentTimeMillis();
		Config.LOG_FILE=projectName+".log";
		Properties props = new Properties(); 
		try { 
			File directory = new File("");
			String proFilePath = directory.getCanonicalPath() ; 
			File log4jFile=new File(proFilePath,"log4j.properties");
			FileInputStream istream = new FileInputStream(log4jFile); 
			props.load(istream); 
			istream.close(); 
			props.setProperty("log4j.appender.R.File",".\\log\\"+Config.LOG_FILE); 
			FileOutputStream outstream = new FileOutputStream(log4jFile); 
			props.save(outstream, "");
			outstream.close();
			PropertyConfigurator.configure(props); 
		} catch (IOException e) { 
				return; 
		} 
	} 
} 


