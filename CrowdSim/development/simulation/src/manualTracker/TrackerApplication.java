package manualTracker;

import manualTracker.uiFramework.TrackerFrame;

public class TrackerApplication 
{
	public static long serialVersionUID = 1; 
		
  /**
   * Main program
   */
   
  	public static void main(String [] args) //main function that runs the whole application
  	{
    	String saxDriverName = null;
    	boolean saxDriverLoaded = false;
    	
    	if(! saxDriverLoaded)
    	{ 
      		try
      		{
        		saxDriverName = "com.sun.org.apache.xerces.internal.parsers.SAXParser";
        		Class.forName(saxDriverName);
        		saxDriverLoaded = true;
      		}
      		catch(ClassNotFoundException ex)
      		{
      		}
    	}
    	if(! saxDriverLoaded)
    	{ 
      		try
      		{
        		saxDriverName = "org.apache.crimson.parser.XMLReaderImpl";
        		Class.forName(saxDriverName);
        		saxDriverLoaded = true;
      		}
      		catch(ClassNotFoundException ex)
      		{
      		}
    	}
    	if(! saxDriverLoaded)
    	{ 
      		try
      		{
	        	saxDriverName = "org.apache.xerces.parsers.SAXParser";
	        	Class.forName(saxDriverName);
	        	saxDriverLoaded = true;
      		}
      		catch(ClassNotFoundException ex)
      		{
      		}
    	}
    	if(! saxDriverLoaded)
    	{ 
      		try
      		{
		        saxDriverName = "oracle.xml.parser.v2.SAXParser";
		        Class.forName(saxDriverName);
		        saxDriverLoaded = true;
      		}
      		catch(ClassNotFoundException ex)
      		{
     		}
    	}
    	if(! saxDriverLoaded)
    	{ 
      		try
      		{
        		saxDriverName = "gnu.xml.aelfred2.XmlReader";
		        Class.forName(saxDriverName);
		        saxDriverLoaded = true;
      		}
      		catch(ClassNotFoundException ex)
      		{
      		}
   		 }
    	if(! saxDriverLoaded)
    	{ 
      		try
      		{
		        saxDriverName = "gnu.xml.aelfred2.SAXDriver";
		        Class.forName(saxDriverName);
		        saxDriverLoaded = true;
      		}
      		catch(ClassNotFoundException ex)
      		{
      		}
    	}
    	if(saxDriverLoaded)
    	{
      		System.setProperty("org.xml.sax.driver", saxDriverName);
    	}
    	else
    	{
	      System.err.println("ERROR loading SAX driver. Please, put on classpath any of following:\n"+
	      "\torg.apache.crimson.parser.XMLReaderImpl\n" +
	      "\torg.apache.xerces.parsers.SAXParser\n" +
	      "\toracle.xml.parser.v2.SAXParser\n" +
	      "\tgnu.xml.aelfred2.XmlReader\n" +
	      "\tgnu.xml.aelfred2.SAXDriver\n" +
	      "\n\n====== Press RETURN to continue");
      		try
      		{
        		System.in.read();
      		}
      		catch(Exception ex)
      		{
      		}
     		System.exit(1);
    	}
          
    	javax.swing.SwingUtilities.invokeLater(new Runnable() 
    	{
      		public void run() 
      		{
		        TrackerFrame trackerFrame = new TrackerFrame();
		        trackerFrame.init();
      		}
    	});
  	}
}
