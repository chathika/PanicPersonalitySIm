package manualTracker.uiFramework;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

public class OptionsActions implements ActionListener , ItemListener 
{
	// main frame
  	TrackerFrame trackerFrame = null;
  	TrackerActions trackerActions;
  	GlobalSettings globalSettings = null;
  
  	public void itemStateChanged(ItemEvent event) 
  	{
    	if(globalSettings == null)
    	{
      		globalSettings = GlobalSettings.getInstance();
      	}
    
    	String name = "";
    	if(event.getItem() instanceof JCheckBoxMenuItem)
    	{
      		name = ((JCheckBoxMenuItem)event.getItem()).getName();
      	}
      	
    	if(name.equals(TrackerFrame.GRID_ON))
    	{
      		globalSettings.gridOn = ! globalSettings.gridOn; 
      		if(trackerActions.displayComponent != null)
      		{
        		trackerActions.displayComponent.repaint();
        	}
    	}
  	}
  
	public void actionPerformed(ActionEvent event) 
  	{
  		if(globalSettings == null)
  		{
      		globalSettings = GlobalSettings.getInstance();
      	}
    
    	String actionCommand = event.getActionCommand();
    
    	if(actionCommand.equals(TrackerFrame.GRID))
    	{
      		globalSettings.gridOn = ! globalSettings.gridOn; 
      		
      		if(trackerActions.displayComponent != null)
        	{
        		trackerActions.displayComponent.repaint();
        	}
    	}
    	else if(actionCommand.equals(TrackerFrame.CURSOR_SIZE))
    	{
      		Integer t = new Integer(GlobalSettings.getInstance().markerHeight);
      		
      		try
      		{
        		String val = JOptionPane.showInputDialog(trackerFrame, t);
        		if(val != null)
        		{
          			GlobalSettings.getInstance().markerHeight = Integer.parseInt(val);
          			GlobalSettings.getInstance().markerWidth = Integer.parseInt(val);
          			GlobalSettings.getInstance().saveToFile();  
        		}
      		}
      		catch(Exception ex)
      		{
      		}
    	}
    	else if(actionCommand.equals(TrackerFrame.GRID_VSIZE))
    	{
      		Integer t = new Integer(GlobalSettings.getInstance().gridVerticalSize);
      		
      		try
      		{
        		String val = JOptionPane.showInputDialog(trackerFrame, t);
        		if(val != null)
        		{
          			globalSettings.gridVerticalSize = Integer.parseInt(val);
          			GlobalSettings.getInstance().saveToFile();  
        		}
      		}
      		catch(Exception ex)
      		{
      		}
    	}  
    	else if(actionCommand.equals(TrackerFrame.GRID_HSIZE))
    	{      
    		Integer t = new Integer(GlobalSettings.getInstance().gridHorizontalSize);
    		
      		try
      		{
        		String val = JOptionPane.showInputDialog(trackerFrame, t);
        		if(val != null)
        		{
          			globalSettings.gridHorizontalSize = Integer.parseInt(val);
          			GlobalSettings.getInstance().saveToFile();  
        		}
      		}
      		catch(Exception ex)
      		{
      		}
    	}
    	else if(actionCommand.equals(TrackerFrame.MARKER_COLOR))
    	{
			System.err.println("INPUT for dialog"+GlobalSettings.getInstance().markerColor);      
      		Color markerColor = JColorChooser.showDialog(trackerFrame, "Cursor color", GlobalSettings.getInstance().markerColor);
			System.err.println(""+markerColor);      
			
      		if(markerColor != null)
      		{
        		GlobalSettings.getInstance().markerColor = markerColor;
        		GlobalSettings.getInstance().saveToFile();  
      		}
    	}
    	else if(actionCommand.equals(TrackerFrame.MARKER_CHANGED_COLOR))
    	{
        	System.err.println("INPUT for dialog"+GlobalSettings.getInstance().markerChangedColor);
            Color markerChangedColor = JColorChooser.showDialog(trackerFrame, "Cursor color", GlobalSettings.getInstance().markerChangedColor);
        	System.err.println(""+markerChangedColor);
        	
            if(markerChangedColor != null)
            {
            	GlobalSettings.getInstance().markerChangedColor = markerChangedColor;
                GlobalSettings.getInstance().saveToFile();  
            }
        }
        else if(actionCommand.equals(TrackerFrame.HEAD_GRID_VSIZE))
        {
      		Integer t = new Integer(GlobalSettings.getInstance().headCountGridVerticalSize);
      		
      		try
      		{
        		String val = JOptionPane.showInputDialog(trackerFrame, t);
        		
        		if(val != null)
        		{
          			globalSettings.headCountGridVerticalSize = Integer.parseInt(val);
          			GlobalSettings.getInstance().saveToFile();  
        		}
      		}
      		catch(Exception ex)
      		{
      		}
    	}  
    	else if(actionCommand.equals(TrackerFrame.HEAD_GRID_HSIZE))
    	{
      		Integer t = new Integer(GlobalSettings.getInstance().headCountGridHorizontalSize);
      
      		try
      		{
        		String val = JOptionPane.showInputDialog(trackerFrame, t);
        		if(val != null)
        		{
          			globalSettings.headCountGridHorizontalSize = Integer.parseInt(val);
          			GlobalSettings.getInstance().saveToFile();  
        		}
      		}
      		catch(Exception ex)
      		{
      		}
    	}
    	else if(actionCommand.equals(TrackerFrame.GRID_COLOR))
    	{
        	Color gridColor = JColorChooser.showDialog(trackerFrame, "Grid color", GlobalSettings.getInstance().gridColor);
        
        	if(gridColor != null)
        	{
          		GlobalSettings.getInstance().gridColor = gridColor;
          		GlobalSettings.getInstance().saveToFile();  
        	}
      	}
    	else if(actionCommand.equals(TrackerFrame.INPUT_FRAME_SKIP))
    	{
      		Integer t = new Integer(GlobalSettings.getInstance().frameSkipOnInput);
      
      		try
      		{
        		String val = JOptionPane.showInputDialog(trackerFrame, t);
        
        		if(val != null)
        		{
          			globalSettings.frameSkipOnInput = Integer.parseInt(val);
          			GlobalSettings.getInstance().saveToFile();  
        		}
      		}
      		catch(Exception ex)
      		{
      		}
    	}
    	else if(actionCommand.equals(TrackerFrame.OUTPUT_FRAME_SKIP))
    	{
      		Integer t = new Integer(GlobalSettings.getInstance().frameSkipOnOutput);
      
      		try
      		{
        		String val = JOptionPane.showInputDialog(trackerFrame, t);
        
        		if(val != null)
        		{
          			globalSettings.frameSkipOnOutput = Integer.parseInt(val);
          			GlobalSettings.getInstance().saveToFile();  
        		}
      		}
      		catch(Exception ex)
      		{
      		}
    	}
    	else if(actionCommand.equals(TrackerFrame.SELECTION_RADIUS))
    	{
      		Integer t = new Integer(GlobalSettings.getInstance().selectionRadius);
      		
      		try
      		{
        		String val = JOptionPane.showInputDialog(trackerFrame, t);
        	
        		if(val != null)
        		{
          			globalSettings.selectionRadius = Integer.parseInt(val);
          			GlobalSettings.getInstance().saveToFile();  
        		}
      		}
      		catch(Exception ex)
      		{
      		}
    	}
    	else if(actionCommand.equals(TrackerFrame.IMAGE_EXPORT_FORMAT))
    	{
        	String t = GlobalSettings.getInstance().imageExportFormat;
			System.err.println("--------"+t);        
        	try
        	{
          		String val = JOptionPane.showInputDialog(trackerFrame, t);
          		val = val.trim();
          		
          		if(val != null && val.length() > 0)
          		{
            		globalSettings.imageExportFormat = val;
            		GlobalSettings.getInstance().saveToFile();  
          		}
        	}
        	catch(Exception ex)
        	{
        	}
      	}
  	}
}
    