package manualTracker.uiFramework;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.border.Border;

public class TrackerFrame extends JFrame
{  
	static final long serialVersionUID = 0;
  	static final String PREVIOUS = "step-back";
	static final String PAUSE = "pause";
	static final String REWIND = "rew";
	static final String FORWARD = "ff";
	static final String NEXT = "step-fwd";
	static final String INFO = "info";
	static final String STATUS_OK = "status_ok";
	static final String UNDO = "undo";
	static final String REDO = "redo";
	static final String CANCEL = "cross";
	static final String PLAY = "play";
	static final String WARNING = "warning";
	static final String LOAD = "open";
	static final String STOP = "stop";
	static final String SAVE = "save";
	static final String SAVE_AS = "saveAs";
	static final String EDIT_EXISTING = "EditExisting";
	static final String SNAPSHOT = "ksnapshot";
	static final String TAKESNAPSHOTS = "takesnapshots";
	static final String SELECT = "select";
	
	static final String MARKER1_IMG = "marker1";
	static final String MARKER2_IMG = "marker2";
	static final String MARKER3_IMG = "marker3";
	static final String MARKER4_IMG = "marker4";
	static final String MARKER5_IMG = "marker5";
	static final String MARKER6_IMG = "marker6";
	static final String MARKER7_IMG = "marker7";
	static final String MARKER8_IMG = "marker8";
  
	static final String EXIT = "exit";
	static final String CURSOR_SIZE = "cursorSize";
	static final String GRID = "grid";
	static final String GRID_ON = "gridOn";
	static final String GRID_VSIZE = "gridVSize";
	static final String GRID_HSIZE = "gridHSize";
	static final String INPUT_FRAME_SKIP = "INPUT_FRAME_SKIP";
	static final String OUTPUT_FRAME_SKIP = "OUTPUT_FRAME_SKIP";
	static final String MARKER_COLOR = "MARKER_COLOR";
	static final String MARKER_CHANGED_COLOR = "MARKER_CHANGED_COLOR";
	static final String HEAD_GRID_VSIZE = "headGridVSize";
	static final String HEAD_GRID_HSIZE = "headGridHSize";
	static final String GRID_COLOR = "GRID_COLOR";
	static final String SELECTION_RADIUS = "selectionRadius";
	static final String IMAGE_EXPORT_FORMAT = "imageExportFormat";
    
	static final String OPEN_EXISTING = "OPEN_EXISTING"; 
	static final String SAVE_ACTIONSET = "SAVE_ACTIONSET"; 
	static final String SAVE_HEADCOUNT0 = "SAVE_HEADCOUNT0"; 
  
	private TrackerActions trackerActions;
	private OptionsActions optionsActions;
	private JScrollPane projectionScroll;
	private JPanel workPanel;
	private ClassLoader cl = null;

  	public void init()
  	{
    	System.err.println("MANUAL TRACKED DEBUG");    
	    cl = this.getClass().getClassLoader();
	    trackerActions = new TrackerActions();
	    trackerActions.trackerFrame = this;
		optionsActions = new OptionsActions();
	    optionsActions.trackerFrame = this;
	    optionsActions.trackerActions = trackerActions; 
	    Container contentPane = getContentPane();

	    workPanel = new JPanel();
	
	   	workPanel.setLayout(new BorderLayout());
	    contentPane.add(workPanel, BorderLayout.CENTER);
	    setJMenuBar(createMenuBar());
	    
	    JToolBar toolBar = new JToolBar("Video Actions");
	    addButtons(toolBar);
	    contentPane.add(toolBar, BorderLayout.PAGE_START);
	    
	    toolBar = new JToolBar("Video Actions");
	    addMarkerButtons(toolBar);
	    contentPane.add(toolBar, BorderLayout.PAGE_END);
    
    	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    	//Display the window.
    	pack();
	    setLocationRelativeTo(null); //center it
	    setVisible(true);
	}
  
  	public JMenuBar createMenuBar() 
  	{
    	JMenuBar menuBar;
    	JMenuItem menuItem;

    	//Create the menu bar.
    	menuBar = new JMenuBar();

    	//Build the first menu.
    	JMenu menu = new JMenu("File");
    	menuBar.add(menu);

    	//a group of JMenuItems
    	menuItem = new JMenuItem("Open media", KeyEvent.VK_O);
    	menuItem.addActionListener(trackerActions);
    	menuItem.setActionCommand(LOAD);
    	menu.add(menuItem);
    
   	 	menuItem = new JMenuItem("Edit work", KeyEvent.VK_E);
    	menuItem.addActionListener(trackerActions);
	    menuItem.setActionCommand(EDIT_EXISTING);
	    menu.add(menuItem);
	    menuItem = new JMenuItem("Save", KeyEvent.VK_S);
	    menuItem.addActionListener(trackerActions);
	    menuItem.setActionCommand(SAVE);
	    menu.add(menuItem);
    
	    menuItem = new JMenuItem("Save as", KeyEvent.VK_A);
	    menuItem.addActionListener(trackerActions);
	    menuItem.setActionCommand(SAVE_AS);
	    menu.add(menuItem);
    
    	menu.add(new JSeparator());

	    menuItem = new JMenuItem("Take Snapshot Sequence", KeyEvent.VK_T);
	    menuItem.addActionListener(trackerActions);
	    menuItem.setActionCommand(TAKESNAPSHOTS);
	    menu.add(menuItem);
	    
	    menu.add(new JSeparator());
	    
	    menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
	    menuItem.addActionListener(trackerActions);
	    menuItem.setActionCommand(EXIT);
	    menu.add(menuItem);

	    //Build second menu in the menu bar.
	    menu = new JMenu("Options");
	    //a group of JMenuItems
	    menuItem = new JMenuItem("Cursor size options", KeyEvent.VK_O);
	    menuItem.addActionListener(optionsActions);
	    menuItem.setActionCommand(CURSOR_SIZE);
	    menu.add(menuItem);
	    
	    menuItem = new JMenuItem("Marker color", KeyEvent.VK_C);
	    menuItem.addActionListener(optionsActions);
	    menuItem.setActionCommand(MARKER_COLOR);
	    menu.add(menuItem);
    
	    menuItem = new JMenuItem("Marker MOVED color", KeyEvent.VK_C);
	    menuItem.addActionListener(optionsActions);
	    menuItem.setActionCommand(MARKER_CHANGED_COLOR);
	    menu.add(menuItem);
	    menu.add(new JSeparator());
	    
	    menuItem = new JMenuItem("Grid vertical mesh size options", KeyEvent.VK_V);
	    menuItem.addActionListener(optionsActions);
	    menuItem.setActionCommand(GRID_VSIZE);
	    menu.add(menuItem);
    
	    menuItem = new JMenuItem("Grid horizontal mesh size options", KeyEvent.VK_H);
	    menuItem.addActionListener(optionsActions);
	    menuItem.setActionCommand(GRID_HSIZE);
	    menu.add(menuItem);
	    
	    menuItem = new JMenuItem("Grid color", KeyEvent.VK_C);
	    menuItem.addActionListener(optionsActions);
	    menuItem.setActionCommand(GRID_COLOR);
	    menu.add(menuItem);
    
	    menu.add(new JSeparator());
	    
	    menuItem = new JMenuItem("Frame skip on input", KeyEvent.VK_S);
	    menuItem.addActionListener(optionsActions);
	    menuItem.setActionCommand(INPUT_FRAME_SKIP);
	    menu.add(menuItem);
	    
	    menu.add(new JSeparator());
    
	    menuItem = new JMenuItem("Selection radius", KeyEvent.VK_R);
	    menuItem.addActionListener(optionsActions);
	    menuItem.setActionCommand(SELECTION_RADIUS);
	    menu.add(menuItem);
	    
	    menu.add(new JSeparator());
	    menuItem = new JMenuItem("Image export format", KeyEvent.VK_E);
	    menuItem.addActionListener(optionsActions);
	    menuItem.setActionCommand(IMAGE_EXPORT_FORMAT);
	    menu.add(menuItem);
    
    	menuBar.add(menu);
    
	    menu = new JMenu("Conversions");
	    menuBar.add(menu);
	    menuItem = new JMenuItem("Open exiting output", KeyEvent.VK_O);
	    menuItem.addActionListener(trackerActions);
	    menuItem.setActionCommand(OPEN_EXISTING);
	    menu.add(menuItem);
	    
	    menuItem = new JMenuItem("Save as ActionSetData", KeyEvent.VK_A);
	    menuItem.addActionListener(trackerActions);
	    menuItem.setActionCommand(SAVE_ACTIONSET);
	    menu.add(menuItem);
    
	    menu.add(new JSeparator());
	    
	    menuItem = new JMenuItem("Head Count Grid vertical count", KeyEvent.VK_V);
	    menuItem.addActionListener(optionsActions);
	    menuItem.setActionCommand(HEAD_GRID_VSIZE);
	    menu.add(menuItem);
	    
	    menuItem = new JMenuItem("Head Count Grid horizontal count", KeyEvent.VK_H);
	    menuItem.addActionListener(optionsActions);
	    menuItem.setActionCommand(HEAD_GRID_HSIZE);
	    menu.add(menuItem);
    
	    menuItem = new JMenuItem("Frame skip on output", KeyEvent.VK_S);
	    menuItem.addActionListener(optionsActions);
	    menuItem.setActionCommand(OUTPUT_FRAME_SKIP);
	    menu.add(menuItem);
	    
	    menuItem = new JMenuItem("Save as HeadCount 0", KeyEvent.VK_C);
	    menuItem.addActionListener(trackerActions);
	    menuItem.setActionCommand(SAVE_HEADCOUNT0);
	    menu.add(menuItem);
	
		return menuBar;
	}

  	protected static ImageIcon createImageIcon(String path) 
  	{
    	if (path != null)
    	{
        	return new ImageIcon(path);
    	} 
    	else 
    	{
        	System.err.println("Couldn't find file: " + path);
        	return null;
    	}
  	}
  
  	protected void addMarkerButtons(JToolBar toolBar) 
  	{
    	JButton button = null;
    
    	button = makeNavigationButton(SELECT, SELECT, "Select to move", "Select to move");
    	toolBar.add(button);
    	
    	button = makeNavigationButton(MARKER1_IMG, MARKER1_IMG, "Set marker 1", "Marker 1");
    	toolBar.add(button);
    	
    	button = makeNavigationButton(MARKER2_IMG, MARKER2_IMG, "Set marker 2", "Marker 2");
    	toolBar.add(button);
    
    	button = makeNavigationButton(MARKER3_IMG, MARKER3_IMG, "Set marker 3", "Marker 3");
    	toolBar.add(button);
    	
    	button = makeNavigationButton(MARKER4_IMG, MARKER4_IMG, "Set marker 4", "Marker 4");
    	toolBar.add(button);
    	
    	button = makeNavigationButton(MARKER5_IMG, MARKER5_IMG, "Set marker 5", "Marker 5");
    	toolBar.add(button);
    	
    	button = makeNavigationButton(MARKER6_IMG, MARKER6_IMG, "Set marker 6", "Marker 6");
    	toolBar.add(button);
    	
 		button = makeNavigationButton(MARKER7_IMG, MARKER7_IMG, "Set marker 7", "Marker 7");
    	toolBar.add(button);
    	
    	button = makeNavigationButton(MARKER8_IMG, MARKER8_IMG, "Set marker 8", "Marker 8");
    	toolBar.add(button);
    
    	button = makeNavigationButton(GRID, GRID, "Turn grid on/off", "Turn grid on/off");
    	button.removeActionListener(trackerActions); 
    		
    	button.addActionListener(optionsActions);
    	toolBar.add(button);
    
    	button = makeNavigationButton(SNAPSHOT, SNAPSHOT, "Take single snapshot", "Take single snapshot");
    	toolBar.add(button);
    }
  
	/**
	* 
	* @param toolBar
	*/
	
  	protected void addButtons(JToolBar toolBar) 
  	{
    	JButton button = null;

    	button = makeNavigationButton(LOAD, LOAD, "Load new media", "Load");
    	toolBar.add(button);
    
    	button = makeNavigationButton(SAVE, SAVE, "Save results", "Save");
    	toolBar.add(button);
    
    	button = makeNavigationButton(REWIND, REWIND, "Forward to something-or-other", "Next");
    	toolBar.add(button);
    
		button = makeNavigationButton(PREVIOUS, PREVIOUS, "Back to previous something-or-other", "Previous");
    	toolBar.add(button);

    	button = makeNavigationButton(PLAY, PLAY, "Forward to something-or-other", "Next");
    	toolBar.add(button);

    	button = makeNavigationButton(PAUSE, PAUSE, "Up to something-or-other", "Up");
    	toolBar.add(button);

    	button = makeNavigationButton(NEXT, NEXT, "Forward to something-or-other", "Next");
    	toolBar.add(button);

    	button = makeNavigationButton(FORWARD, FORWARD, "Forward to something-or-other", "Next");
    	toolBar.add(button);
    
    	button = makeNavigationButton(INFO, INFO, "Launch menu dialog", "Next");
    	toolBar.add(button);
    
    	button = makeNavigationButton(STATUS_OK, STATUS_OK, "Forward to something-or-other", "Next");
    	toolBar.add(button);
    
    	button = makeNavigationButton(CANCEL, CANCEL, "Forward to something-or-other", "Next");
    	toolBar.add(button);
    
    	button = makeNavigationButton(WARNING, WARNING, "Forward to something-or-other", "Next");
    	toolBar.add(button);
        
    	button = makeNavigationButton(UNDO, UNDO, "Forward to something-or-other", "Next");
    	toolBar.add(button);
    
    	button = makeNavigationButton(REDO, REDO, "Forward to something-or-other", "Next");
    	toolBar.add(button);
    }

  	protected JButton makeNavigationButton(String imageName, String actionCommand, String toolTipText, String altText)
  	{
    	//Look for the image.
    	String imgLocation = "images/manualTracker/" + imageName + ".gif";

 		System.err.println("imgLocation "+imgLocation);    
 
    	//Create and initialize the button.
    	JButton button = new JButton();
    	button.setActionCommand(actionCommand);
    	button.setToolTipText(toolTipText);
    	button.addActionListener(trackerActions);

    	if (imgLocation != null)  //image found
    	{    
      		try
      		{
      			System.err.println("Image "+imgLocation+"-->"+cl.getResource(imgLocation));
	        	button.setIcon(new ImageIcon(cl.getResource(imgLocation), altText));
      		}
      		catch(Throwable ex)
      		{
        		System.err.println("Resource  " + imgLocation);
        		ex.printStackTrace();
      		}
    	} 
    	else //no image found
    	{                                     
        	button.setText(altText);
        	System.err.println("Resource not found: " + imgLocation);
    	}
		
		return button;
  	}

  	void setProjectionScroll(Component displayComponent)
  	{
    	Border lineBorder = BorderFactory.createLineBorder(Color.BLACK, 2);
    	if(projectionScroll != null)
    	{
      		workPanel.remove(projectionScroll);
    	}
    	projectionScroll = new JScrollPane();
    	workPanel.add(projectionScroll, BorderLayout.CENTER);
    	workPanel.setBorder(lineBorder);
   	 	projectionScroll.setViewportView(displayComponent);
    	pack();
  	}
}
