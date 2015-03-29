package manualTracker.uiFramework;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import manualTracker.postProcessors.ActionDataSetIO;
import manualTracker.postProcessors.HeadCount_0;
import manualTracker.postProcessors.InternalIO;
import manualTracker.data.DataContainer;
import manualTracker.data.PointData;
import manualTracker.data.FrameData;
import manualTracker.renderer.VideoFrameAccess;
import manualTracker.renderer.ProjectionScreen;
import manualTracker.uiFramework.TrackerActions;

public class TrackerActions implements ActionListener 
{
	static final String LOAD_TITLE = "Read video data from:";
  	static final String SAVE_TITLE = "Save work output as:";
  
  	// main frame
  	TrackerFrame trackerFrame = null;
  	//OptionsActions optionsActions = null;
  	// media processor
  	VideoFrameAccess videoFrameAccess = null;
  	// user mouse input
  	UserMouseFrameActions userMouseFrameActions = null;
  	DataContainer dataContainer = null;
  	ProjectionScreen displayComponent = null;
  	VideoFrameEventHandler videoFrameEventHandler = null;
  
  
	/**
	* User actions handler
	*/
  	public void actionPerformed(ActionEvent event) 
  	{
    	//System.err.println(event.getActionCommand());
    	String actionCommand = event.getActionCommand();
   
        if(actionCommand.equals(TrackerFrame.EXIT))
        {
      		System.exit(0);
    	}
    	else if(actionCommand.equals(TrackerFrame.SELECT))
    	{
      		videoFrameEventHandler.currentMarkerId = VideoFrameEventHandler.SELECT;
      		videoFrameEventHandler.resetDragging();
    	}
    	else if(actionCommand.equals(TrackerFrame.CANCEL))
    	{
      		videoFrameEventHandler.currentMarkerId = VideoFrameEventHandler.REMOVE;
      		videoFrameEventHandler.resetDragging();
    	}
    	else if(actionCommand.equals(TrackerFrame.MARKER1_IMG))
    	{
      		videoFrameEventHandler.currentMarkerId = 1;
      		videoFrameEventHandler.resetDragging();
    	}
    	else if(actionCommand.equals(TrackerFrame.MARKER2_IMG))
    	{
        	videoFrameEventHandler.currentMarkerId = 2;
        	videoFrameEventHandler.resetDragging();
    	}
    	else if(actionCommand.equals(TrackerFrame.MARKER3_IMG))
    	{
        	videoFrameEventHandler.currentMarkerId = 3;
        	videoFrameEventHandler.resetDragging();
    	}
    	else if(actionCommand.equals(TrackerFrame.MARKER4_IMG))
    	{
        	videoFrameEventHandler.currentMarkerId = 4;
        	videoFrameEventHandler.resetDragging();
    	}
    	else if(actionCommand.equals(TrackerFrame.MARKER5_IMG))
    	{
        	videoFrameEventHandler.currentMarkerId = 5;
        	videoFrameEventHandler.resetDragging();
    	}
    	else if(actionCommand.equals(TrackerFrame.MARKER6_IMG))
    	{
        	videoFrameEventHandler.currentMarkerId = 6;
        	videoFrameEventHandler.resetDragging();
    	}
    	else if(actionCommand.equals(TrackerFrame.MARKER7_IMG))
    	{
        	videoFrameEventHandler.currentMarkerId = 7;
         	videoFrameEventHandler.resetDragging();
    	}
    	else if(actionCommand.equals(TrackerFrame.MARKER8_IMG))
    	{
        	videoFrameEventHandler.currentMarkerId = 8;
        	videoFrameEventHandler.resetDragging();
    	}
    	else if(actionCommand.equals(TrackerFrame.PREVIOUS))
    	{
      		videoFrameEventHandler.skipRequested(-GlobalSettings.getInstance().frameSkipOnInput);
      		for(int i = 0; i < GlobalSettings.getInstance().frameSkipOnInput; i++)
      		{
        		videoFrameAccess.skip(-1);
      		}
    	}
    	else if(actionCommand.equals(TrackerFrame.PAUSE))
    	{
      		videoFrameAccess.pause();
    	}
    	else if(actionCommand.equals(TrackerFrame.REWIND))
    	{
      		videoFrameEventHandler.skipRequested(0);
      		videoFrameAccess.rewind();
    	}
    	else if(actionCommand.equals(TrackerFrame.FORWARD))
    	{
      		videoFrameEventHandler.skipRequested(0);
      		videoFrameAccess.wind();
    	}
    	else if(actionCommand.equals(TrackerFrame.NEXT))
    	{
      		videoFrameEventHandler.skipRequested(GlobalSettings.getInstance().frameSkipOnInput);
      		for(int i = 0; i < GlobalSettings.getInstance().frameSkipOnInput; i++)
      		{
        		videoFrameAccess.skip(1);
      		}
    	}
    	else if(actionCommand.equals(TrackerFrame.INFO))
    	{
      		JDialog menuDialog = new JDialog(trackerFrame, false);
      		menuDialog.setJMenuBar(trackerFrame.createMenuBar());
      		menuDialog.setSize(new Dimension(150, 80));
      		menuDialog.setVisible(true);
    	}
    	else if(actionCommand.equals(TrackerFrame.STATUS_OK));
    	else if(actionCommand.equals(TrackerFrame.UNDO));
    	else if(actionCommand.equals(TrackerFrame.REDO));
    	else if(actionCommand.equals(TrackerFrame.PLAY))
    	{
      		videoFrameEventHandler.skipRequested(0);
      		videoFrameAccess.start();
    	}
    	else if(actionCommand.equals(TrackerFrame.WARNING));
    	else if(actionCommand.equals(TrackerFrame.LOAD))
    	{
      		loadMedia(false);
    	}  
    	else if(actionCommand.equals(TrackerFrame.EDIT_EXISTING))
    	{
        	loadMedia(true);
      	}  
    	else if(actionCommand.equals(TrackerFrame.STOP))
    	{
      		videoFrameAccess.stop();
    	}
    	if(actionCommand.equals(TrackerFrame.OPEN_EXISTING))
    	{
      		openExisting();
    	}
    	if(actionCommand.equals(TrackerFrame.SAVE))
    	{
      		saveData();
    	}
    	if(actionCommand.equals(TrackerFrame.SAVE_AS))
    	{
      		File file = getFileName(true);
      		if(file == null)
      		{
        		return;
      		}
      		saveData(file.getAbsolutePath());
    	}
    	if(actionCommand.equals(TrackerFrame.SAVE_ACTIONSET))
    	{
      		saveActionSet();
    	}
    	if(actionCommand.equals(TrackerFrame.SAVE_HEADCOUNT0))
    	{
      		saveHeadCount();
    	}
    	if(actionCommand.equals(TrackerFrame.SNAPSHOT))
    	{
      		singleSnapshot();
    	}
    	if(actionCommand.equals(TrackerFrame.TAKESNAPSHOTS))
    	{
      		snapshotSeries();
    	}
    }
 
	/**
	* Handles changes that occur when frame has moved in any direction
	* @param frameId
	*/
	
  	public void newFrameEvent(int frameId)
  	{
    	videoFrameEventHandler.newFrameEvent(frameId);
  	}
	
	/**
	* 
	* @param x
	* @param y
	*/
	
  	public void mouseClicked(int x, int y)
  	{
    	displayComponent.click = true;
  	}
  	public void endReached()
  	{
    	JOptionPane.showMessageDialog(trackerFrame, "End of Media");
  	}
  	void singleSnapshot()
  	{
    	File name = getFileName(true);    
    	if(name == null)
    	{
      		return;
    	}
    	if(videoFrameEventHandler == null)
    	{
      		return;
    	}
    	int id = videoFrameEventHandler.getCurentFrameId();
    	String path = name.getAbsolutePath();
    	videoFrameEventHandler.takeSnapshot(path+id);
  	}
  	void snapshotSeries()
  	{
    	File name = getFileName(true);    
    	if(name == null)
    	{
      		return;
    	}
    	if(videoFrameEventHandler == null)
    	{
      		return;
    	}
    
    	String path = name.getAbsolutePath();
    
    	videoFrameAccess.rewind();
    	videoFrameEventHandler.ignoreEvents = true;    
    	int skipFrames = GlobalSettings.getInstance().frameSkipOnInput;
    	int id = videoFrameEventHandler.getCurentFrameId();
    	while(true)
    	{
      		videoFrameEventHandler.takeSnapshot(path+id);
      		videoFrameAccess.skip(skipFrames);

      		if(id == videoFrameEventHandler.getCurentFrameId())
      		{
        		break;
      		}
      		id = videoFrameEventHandler.getCurentFrameId();
    	}
    	videoFrameEventHandler.ignoreEvents = false;    
    	JOptionPane.showMessageDialog(trackerFrame, "DONE");
  	}

	/**
	* 
	*
	*/
	
  	void saveData()
  	{
    	saveData(dataContainer.outputFilename);
  	}
  	
	/**
	* 
	*
	*/
	
  	void saveData(String name)
  	{
    	try
    	{
      		File outfile = new File(name);
      		dataContainer.outputFilename = name;
      		if(outfile.exists())
      		{
      			outfile.delete();
      		}
      		outfile.createNewFile();
      		FileWriter fileWriter = new FileWriter(outfile);
      		InternalIO dataIO = new InternalIO();
      		dataIO.writeData(fileWriter, dataContainer);
      		JOptionPane.showMessageDialog(trackerFrame, "SAVED");
    	}
	    catch(Exception ex)
	    {
	    	ex.printStackTrace();
	      	JOptionPane.showMessageDialog(trackerFrame, ex.getMessage());
	      	return;
	    }
    }
    
	/**
	* 
	*
	*/
	
  	void saveActionSet()
  	{
    	try
    	{
      		File outfile = getFileName(true);
      		if(outfile == null)
      		{
        		return;
      		}
      		if(outfile.exists())
      		{
        		outfile.delete();
      		}
      		outfile.createNewFile();
      		FileWriter fileWriter = new FileWriter(outfile);
      		ActionDataSetIO dataIO = new ActionDataSetIO();
      		dataIO.writeData(fileWriter, dataContainer);
    	  	JOptionPane.showMessageDialog(trackerFrame, "SAVED");
    	}
    	catch(Exception ex)
    	{
      		ex.printStackTrace();
      		JOptionPane.showMessageDialog(trackerFrame, ex.getMessage());
      		return;
    	}
  	}
  	
	/**
	* 
	*
	*/
	
	void saveHeadCount()
	{
    	try
    	{
      		File outfile = getFileName(true);
      		if(outfile == null)
      		{
        		return;
      		}
      		if(outfile.exists())
      		{
        		outfile.delete();
      		}
      		outfile.createNewFile();
      		FileWriter fileWriter = new FileWriter(outfile);
      		HeadCount_0 dataIO = new HeadCount_0();
      
      		dataIO.writeData(fileWriter, dataContainer);
	      JOptionPane.showMessageDialog(trackerFrame, "SAVED");
    	}
    	catch(Exception ex)
    	{
      		ex.printStackTrace();
      		JOptionPane.showMessageDialog(trackerFrame, ex.getMessage());
      		return;
    	}
  	}

	/**
	* 
	*
	*/
	
  	void openExisting()
  	{
    	try
    	{
      		File infile = getFileName(false);
      		if(infile == null)
      		{
        		return;
      		}
      		dataContainer = new DataContainer();
      		InternalIO.readFile(infile, dataContainer);
    	}
    	catch(Exception ex)
    	{
      		ex.printStackTrace();
      		JOptionPane.showMessageDialog(trackerFrame, ex.getMessage());
      		return;
    	}
  	}

	/**
	* 
	* @param isOut
	* @return
	*/
	
  	File getFileName(boolean isOut)
  	{
    	JFileChooser chooser = new JFileChooser();
    	if(isOut)
    	{
      		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
    	}
    	else
    	{
      		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
    	}
    	chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
    	int returnVal = chooser.showDialog(trackerFrame, (isOut ? "Save" : "Open"));
    	if(returnVal != JFileChooser.APPROVE_OPTION)
    	{
      		return null;
    	}
    	return new File(chooser.getSelectedFile().getAbsolutePath());
  	}  

	/**
	* 
	* @param edit
	*/
	
  	void loadMedia(boolean edit)
  	{
    	JFileChooser chooser = new JFileChooser();
    	chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
    	FileNameExtensionFilter filter  = null;
    	File mediaFile = null;
    	File outputFile = null;
    	int returnVal = 0;
    
    	if(edit)
    	{
      		String[] extensions = {"xml"};
      		filter = new FileNameExtensionFilter("Archive", extensions);
      		returnVal = chooser.showDialog(trackerFrame, "Open");
      		if(returnVal != JFileChooser.APPROVE_OPTION)
      		{
        		return;
      		}
      		outputFile = chooser.getSelectedFile();
      		dataContainer = new DataContainer();
      		
      		try
      		{
        		InternalIO.readFile(new File(outputFile.getAbsolutePath()), dataContainer);
      		}
      		catch(Exception ex)
      		{
        		ex.printStackTrace();
        		return;
      		}

      		mediaFile = new File(dataContainer.inputFilename);
      		if(!mediaFile.exists())
      		{
        		JOptionPane.showMessageDialog(trackerFrame, "File does not exist on given path: \n"+dataContainer.inputFilename, "Missing file !!!", JOptionPane.ERROR_MESSAGE);
        		return;      
      		}
      		outputFile = new File(outputFile.getAbsolutePath());
      		dataContainer.outputFilename = outputFile.getAbsolutePath();
    	}
    	if(!edit)
    	{
      		String[] extensions = {"avi", "mpeg", "mov"};
      		filter = new FileNameExtensionFilter("Video", extensions);
	      	chooser.setDialogType(JFileChooser.OPEN_DIALOG);
      		returnVal = chooser.showOpenDialog(trackerFrame);
      		if(returnVal != JFileChooser.APPROVE_OPTION)
      		{
        		return;
      		}
      		mediaFile = chooser.getSelectedFile();
    	}
    	videoFrameAccess = VideoFrameAccess.getVideoFrameAccess(mediaFile.getAbsolutePath()); 
    	if(videoFrameAccess != null)
    	{
      		if(!edit)
      		{
        		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        		String[] extensions = {"xml"};
        		filter = new FileNameExtensionFilter("Archive", extensions);
	 	        String outputFileName = mediaFile.getAbsolutePath().substring(0, mediaFile.getAbsolutePath().lastIndexOf("."))+".xml";
        		chooser.setSelectedFile(new File(outputFileName ));
        		returnVal = chooser.showDialog(trackerFrame, (edit ? "Open" : "Save"));
        		if(returnVal != JFileChooser.APPROVE_OPTION)
        		{
          			return;
        		}
       	 		outputFile = chooser.getSelectedFile();
      		}
      		displayComponent = (ProjectionScreen)videoFrameAccess.getVisualComponent();
      		System.err.println("displayComponent--->"+displayComponent);
      
      		trackerFrame.setProjectionScroll(displayComponent);
      		userMouseFrameActions = new UserMouseFrameActions();

	      	displayComponent.addMouseListener(userMouseFrameActions);
      		displayComponent.addMouseMotionListener(userMouseFrameActions);
      
      		if(!edit)
      		{
        		dataContainer = new DataContainer();
        		dataContainer.inputFilename = mediaFile.getAbsolutePath();
        		dataContainer.outputFilename = outputFile.getAbsolutePath();
      		}
      
      		dataContainer.frameHSize = (int)videoFrameAccess.getFrameSize().getWidth();
      		dataContainer.frameVSize = (int)videoFrameAccess.getFrameSize().getHeight();
      
      		videoFrameAccess.setActionHandler(this);
      		videoFrameEventHandler = new VideoFrameEventHandler();
      		videoFrameEventHandler.dataContainer = dataContainer;
      		videoFrameEventHandler.setVideoFrameAccess(videoFrameAccess); 
      		userMouseFrameActions.videoFrameEventHandler = videoFrameEventHandler;
      		userMouseFrameActions.trackerFrame = trackerFrame;
    	}
  	}
}
