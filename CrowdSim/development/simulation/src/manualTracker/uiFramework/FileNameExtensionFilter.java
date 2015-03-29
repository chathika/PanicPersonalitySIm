package manualTracker.uiFramework;

import java.io.File;
import java.io.FilenameFilter;

public class FileNameExtensionFilter implements FilenameFilter 
{
	String[] extensions = null;
	
	public FileNameExtensionFilter(String name, String[] extensions)
	{
    	this.extensions = extensions;
	}
  
	public boolean accept(File dir, String name) 
	{

    	for(int i=0; i < extensions.length; i++)
    	{
    		if(name.endsWith("."+extensions))
        	return true;
    	}
    	return false;
  	}
}
