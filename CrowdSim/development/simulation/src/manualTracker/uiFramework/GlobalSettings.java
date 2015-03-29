package manualTracker.uiFramework;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class GlobalSettings 
{
	public int markerWidth = 6; //default marker width	
  	public int markerHeight = 6; //default marker height
  	public int frameSkipOnInput = 10; //default frame skip input
  	public int frameSkipOnOutput = 10; //default frame skip output
  	public int selectionRadius = 4; //default selection radius
  	public boolean gridOn = false; //default settings for grid
  	public int gridVerticalSize = 30; //default vertical size for grid
  	public int gridHorizontalSize = 30; //default horizontal size for grid
  	public int headCountGridVerticalSize = 3; //default head count grid vertical size
  	public int headCountGridHorizontalSize = 4; //default head cound grid horizontal size
  	public String imageExportFormat = "jpg"; //default picture format
  	public Color markerColor = Color.WHITE; //default marker color
  	public Color markerChangedColor = Color.ORANGE; //default marker color when changed
  	public Color gridColor = Color.RED; //default grid color
  	static GlobalSettings instance = null;
  
  	public static final String APPLICATION_NAME = "ManualTracker";
  
  	public static GlobalSettings getInstance()
  	{
    	if(instance == null)
    	{
      		instance = new GlobalSettings();
      		instance.readFromFile();
    	}
    	return instance;
  	}

	/**
	* 
	* @param filename
	*/
  
  	public void readFromFile()
  	{
    	readFromFile(System.getProperty("user.dir")+ System.getProperty("file.separator")+APPLICATION_NAME+".properties");
  	}
  	public void readFromFile(String filename)
  	{
    	Properties properties = new Properties();
			    	
    	try
    	{
	      	FileInputStream input = new FileInputStream(filename);
			properties.load(input);
			markerWidth = Integer.parseInt(properties.getProperty("markerWidth"));
			markerHeight = Integer.parseInt(properties.getProperty("markerHeight"));
			gridVerticalSize = Integer.parseInt(properties.getProperty("gridVerticalSize"));
			gridHorizontalSize = Integer.parseInt(properties.getProperty("gridHorizontalSize"));	      
			headCountGridVerticalSize = Integer.parseInt(properties.getProperty("headCountGridVerticalSize"));
			headCountGridHorizontalSize = Integer.parseInt(properties.getProperty("headCountGridHorizontalSize"));      
			imageExportFormat = properties.getProperty("imageExportFormat");
			imageExportFormat = imageExportFormat.trim();
	
      	  	if(imageExportFormat == null || imageExportFormat.length() == 0)
      	  	{
        		imageExportFormat = "jpg";
      	  	}
      
      		markerColor = Color.decode(properties.getProperty("markerColor"));
      		gridColor = Color.decode(properties.getProperty("gridColor"));
      
    	}
    	catch(Exception ex)
    	{
      		System.err.println(ex.getMessage());
    	}
  	}
	
	/**
	* 
	* @param filename
	*/
	
  	public void saveToFile()
  	{
    	saveToFile(System.getProperty("user.dir")+ System.getProperty("file.separator")+APPLICATION_NAME+".properties");
  	}
  	
  	public void saveToFile(String filename)
  	{
    	Properties properties = new Properties();
    	try
    	{
			FileOutputStream output = new FileOutputStream(filename);
			properties.setProperty("markerWidth", ""+markerWidth);
			properties.setProperty("markerHeight", ""+markerHeight);
			properties.setProperty("gridVerticalSize", ""+gridVerticalSize);
			properties.setProperty("gridHorizontalSize", ""+gridHorizontalSize);
			properties.setProperty("markerColor", ""+markerColor.getRGB());
			properties.setProperty("gridColor", ""+gridColor.getRGB());

      		properties.setProperty("headCountGridVerticalSize", ""+headCountGridVerticalSize);
      		properties.setProperty("headCountGridHorizontalSize", ""+headCountGridHorizontalSize);
      
      		if(imageExportFormat == null)
      		{
        		imageExportFormat = "jpg";
      		}
      
      		properties.setProperty("imageExportFormat", ""+imageExportFormat);
      
      		properties.store(output, "Options settings");
    	}
    	catch(Exception ex)
    	{
      		System.err.println(ex.getMessage());
    	}
  	}
}
