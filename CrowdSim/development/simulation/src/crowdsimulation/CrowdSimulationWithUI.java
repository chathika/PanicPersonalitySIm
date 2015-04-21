	/*
 * $RCSfile: CrowdSimulationWithUI.java,v $ $Date: 2009/07/24 18:05:14 $
 */
package crowdsimulation;

import crowdsimulation.entities.individual.*;
import sim.engine.*;
import sim.display.*;
import sim.portrayal.continuous.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import sim.portrayal.simple.*;
import sim.portrayal.*;

/**
 * This contains the GUI information and hooks for the parameters to be shown in the GUI.
 *
 * @author $Author: dkaup $
 * @version $Revision: 1.5 $
 * $State: Exp $
 * $Date: 2009/07/24 18:05:14 $
 **/
public class CrowdSimulationWithUI extends GUIState
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The widget which the graphics are drawn upon. **/
    private Display2D display;
    /** The frame which contains the graphics. **/
    private JFrame displayFrame;
    /** Visual Map used to display the paths and misc info. **/
    private ContinuousPortrayal2D info_portrayal = new ContinuousPortrayal2D();
    /** Visual Map used to display the individuals. **/
    private ContinuousPortrayal2D portrayal = new ContinuousPortrayal2D();
    /** Visual Map used to display the obstacles. **/
    private ContinuousPortrayal2D feature_portrayal = new ContinuousPortrayal2D();
    /** String to display in the title bar of the Frame. **/
    private String name = "Crowd Simulation";

////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////

	/**
	 * This is the execution thread to run the simulation.
	 *
	 * @param args These are the command line parameters to adjust the execution 
	 *	of the simulation.
	 **/
    public static void main( String[] args )
    {
    	String configFile = null;
    	if( args.length > 0 )
    	{
    		configFile = args[0];
    	}
System.out.println(configFile);
        CrowdSimulationWithUI mav = new CrowdSimulationWithUI( configFile );
        Console c = new Console( mav );
        c.setVisible( true );
        CrowdSimulation.getInstance().go();
    }

	/**
	 * Allows the UI to get a reference to the simulation state Variables
	 *  so that they can be displayed in the GUI.
	 **/
    public Object getSimulationInspectedObject() 
    { 
    	return CrowdSimulation.getInstance().getPrimaryActionController();
    }  // non-volatile


    /**
     * Constructs a CrowdSimulationWithUI and uses a random seed of the currentTimeMillis.
     **/
    public CrowdSimulationWithUI( String configFilename )
    {
//        super( new CrowdSimulation( true, System.currentTimeMillis(), configFilename ) );
        super( new CrowdSimulation( true, 0, configFilename ) );
		System.out.println("Setting ui = "+ this);
        ((CrowdSimulation)state).setUI( this );
    }
    
    /**
     * Constructs a CrowdSimulationWithUI but with a given simulation state.
     *
     * @param state The simulation state to be used for the simulation.
     **/    
    public CrowdSimulationWithUI( SimState state ) 
    {
        super( state );
        ((CrowdSimulation)state).setUI( this );
    }

	/**
	 * Sets up the simulation then starts the execution of the simulation.
	 **/
    public void start()
    {
        super.start();
        setupPortrayals();
    }

	/**
	 * Loads a given simulation state and starts execution of the simulation.
	 *
	 * @param state The state of the simulation.
	 **/
    public void load( SimState state )
    {
        super.load( state );
        setupPortrayals();
    }
     
    /**
     * Sets up the visuals for the individuals and obstacles for the simulation.
     **/   
    public void setupPortrayals()
    {
        CrowdSimulation crowdState = (CrowdSimulation)state;
        portrayal.setField( crowdState.getWorld() );
        feature_portrayal.setField( crowdState.getTerrain() );
        info_portrayal.setField( crowdState.getWorldInfo() );
 
        // update the size of the display appropriately.
        double w = crowdState.getWorld().getWidth();
        double h = crowdState.getWorld().getHeight();

		display.insideDisplay.height = h;
		display.insideDisplay.width = w;
            
        // reschedule the displayer
        display.reset();
                
        // redraw the display
        display.repaint();
    }

	/**
	 * Initialize the GUI elements for display.
	 *
	 * @param c The controller of the simulation.
	 **/
    public void init(Controller c)
    {
        super.init(c);
        
        CrowdSimulation crowdSim = CrowdSimulation.getInstance();
        
        double width = crowdSim.getWorld().getWidth();
		double height = crowdSim.getWorld().getHeight();
		double scale = crowdSim.getVisualScale();

        // make the displayer
        display = new Display2D( width, height, this, 1 );
        display.setBackdrop( Color.white );
		display.setScale( scale );

        displayFrame = display.createFrame();
        displayFrame.setTitle( "Crowds" );
        c.registerFrame( displayFrame );   // register the frame so it appears in the "Display" list
        displayFrame.setVisible( true );
       	displayFrame.setSize( 700, 800 );
        
        display.attach( this.info_portrayal, "Paths and Misc. Information", false ); // Attach it first so it will be drawn underneath everything
        display.attach( this.portrayal, "Individuals" );
        display.attach( this.feature_portrayal, "Obstacles and Features" );
        
        HashMap portrayals = crowdSim.getPortrayals();
        Object[] portrayalKeys = portrayals.keySet().toArray();
        
        for( int i = 0; i < portrayals.size(); i++ )
        {
        	String key = (String)portrayalKeys[i];
        	display.attach( (FieldPortrayal2D)portrayals.get( key ), key );
        }
    }
    
    /**
     * The method called to clean up the resources after completion of the execution of the simulation.
     **/
    public void quit()
    {
        super.quit();
        
        if (displayFrame!=null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }

////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////

	/**
	 * Gets the Display element for the simulation.
	 *
	 * @return The Display element of the simulation.
	 **/
	public Display2D getDisplay()
	{
		return display;
	}
	
	/**
	 * Sets the Display element for the simulation.
	 *
	 * @param val The Display element of the simulation.
	 **/
	public void setDisplay( Display2D val )
	{
		this.display = val;
	}
	
	/**
	 * Gets the Frame for the simulation.
	 *
	 * @return The Frame of the simulation.
	 **/
	public JFrame getFrame()
	{
		return displayFrame;
	}
	
	/**
	 * Sets the Frame for the simulation.
	 *
	 * @param val The Frame of the simulation.
	 **/
	public void setFrame( JFrame val )
	{
		this.displayFrame = val;
	}
    
}
