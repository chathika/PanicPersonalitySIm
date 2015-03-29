/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crowdsimulation.maps;

import crowdsimulation.*;
import crowdsimulation.actioncontroller.cellularautomata.*;
import crowdsimulation.entities.obstacle.*;
import sim.util.*;
import sim.field.grid.*;
import java.util.*;
import java.io.*;
import math.*;
import crowdsimulation.entities.*;
/**
 *
 * @author ajolly
 */
public class DijkstraMap extends DoubleGrid2D {

    private  ArrayList<vPoint> Q = new ArrayList();
    private SparseGrid2D obstacleGrid;
    private double max = 0;
    
    public DijkstraMap(int col, int row) {
        //Set all nodes distance to Source to Inifinity
        super(col, row, Double.POSITIVE_INFINITY);
        this.populateQ(col,row);
    }
    public DijkstraMap(SparseGrid2D objGrid) {
        //Set all nodes distance to Source to Inifinity
        super(objGrid.getWidth(), objGrid.getHeight(), Double.POSITIVE_INFINITY);
        obstacleGrid = objGrid;
        this.populateQ(obstacleGrid.getWidth(),obstacleGrid.getHeight());
    }
    private void populateQ(int col, int row) {
        for (int i = 0; i < col; i++) {
            for (int j = 0; j < row; j++) {
                Q.add(new vPoint(i, j, this.get(i, j)));
            }
        }
    }
    
    public void DijkstraAlg(Int2D source) {
        //Set the source node distance to 0
         for (int i = 0; i < Q.size(); i++) {
            vPoint testPt = (vPoint) Q.get(i);
            //System.out.println("TestPt " + testPt.x + " , " + testPt.y + " SourcePt " + source.x + " , " + source.y);
            if ((testPt.x == source.x) && (testPt.y == source.y)) {
              Q.get(i).dist = 0;
              //System.out.println("Found Source " + testPt.x + " , " + testPt.y);
              break;
            }
        }
        //System.out.println("Past Connections");
        //for (int i = 0; i < theConn.size(); i++) {
        //    int index = theConn.get(i);
        //    Q.get(index).displayvPoint();
        //}
        //System.out.println("Finished");
        int numIterations = 0;
        int oriQueueSize = Q.size();
        while (!Q.isEmpty()) {
            //System.out.println("Q Size : " + Q.size() + " numIterations " + numIterations);
            Collections.sort(Q);
            vPoint u = Q.get(0);
            removeFromList();
            ArrayList<Integer> theConn = getConnections(Q, u.x, u.y, source);
            System.out.println("Connections Size : " + theConn.size());
            for(int i = 0; i < theConn.size(); i++){
                //distance to neighbor node always 1 
                vPoint v = Q.get(theConn.get(i));
                double alt = u.dist + getDist(u,v);
                if (alt <  Q.get(theConn.get(i)).dist){
                    Q.get(theConn.get(i)).dist = alt;
                }
            }
           numIterations++;
           
           if(numIterations > oriQueueSize){
               System.out.println("We have a problem with infinite loop");
               break;
           }
        }
        this.add(-max);
        this.multiply(-1);
        //this.write2cvs("../logs/DijkstraMap.csv");
    }
    
    private double getDist(vPoint u, vPoint v){
        return Math.sqrt((u.x - v.x)*(u.x - v.x) + (u.y - v.y)*(u.y - v.y));
    }
    private void removeFromList(){
       // System.out.println("Qloc " + Q.get(0).x + " , " + Q.get(0).y + " distance: " +  Q.get(0).dist);
        this.set(Q.get(0).x, Q.get(0).y,  Q.get(0).dist);
        if (Q.get(0).dist > max && Q.get(0).dist != Double.POSITIVE_INFINITY){
            max = Q.get(0).dist;
        }
        Q.remove(0);
    }
    private ArrayList<Integer> getConnections(ArrayList<vPoint> Q, int x, int y, Int2D source) {
        ArrayList theConnInd = new ArrayList();
        // and to simplify the for-loop, just include the cell itself
        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                Int2D testPt = new Int2D(x - dx, y - dy);
                //System.out.println("getConnections testPt " + testPt.x + "," + testPt.y);
                for (int i = 0; i < Q.size(); i++) {
                    if ((testPt.x == Q.get(i).x) && (testPt.y == Q.get(i).y)) {
                        System.out.println("getConnections Found Neighbor " + testPt.x + "," + testPt.y);
                        Bag theObjs;
                        try {
                            theObjs = obstacleGrid.getObjectsAtLocation(testPt.x, testPt.y);
                        } catch (Exception e) {
                            System.out.println("Caught Exception :");
                            System.out.println(e.toString());
                            theObjs = null;
                        }
                        if (!((testPt.x == x) && (testPt.y == y))) {
                            if (theObjs != null) {
                                if (theObjs.isEmpty()) {
                                    System.out.println("Adding");
                                    theConnInd.add(i);
                                }
                            } else { theConnInd.add(i); }
                        }
                    }
                }
            }
            if (theConnInd.size() == 8) {
                System.out.println("Found All Neighbors");
                break;
            }
        }
        return theConnInd;
    }
    private void printDblGrid() {
        int theWidth = this.getWidth();           
        int theHeight = this.getHeight();
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
        for (int row = 0; row < theHeight; row++) {
            for (int col = 0; col < theWidth; col++) {
                System.out.print(df.format(this.get(col,row)) + " ");
            }
            System.out.println();
        }
        System.out.println("-----------------------------------------------------------");
    }
    public void write2cvs(String fileName) {

        try {
            System.out.println("Writing to file");
            PrintWriter writer;
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            writer = new PrintWriter(new FileOutputStream(fileName));
            int theWidth = this.getWidth();
            int theHeight = this.getHeight();
            java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
            for (int row = 0; row < theHeight; row++) {
                StringBuffer data = new StringBuffer();
                for (int col = 0; col < theWidth; col++) {
                    data.append(df.format(this.get(col, row)) + ",");
                }
                writer.println(data.toString());
                writer.flush();
            }
            writer.close();
        } catch (IOException ioe) {
            System.err.println("Could not create log file " + fileName);
            ioe.printStackTrace();
        }
    }
    
}//end class

class vPoint implements Comparable {

    public int x;
    public int y;
    public double dist;

    public vPoint(int x, int y, double val) {
        this.x = x;
        this.y = y;
        this.dist = val;
    }

    public int compareTo(Object anotherPoint) throws ClassCastException {
        if (!(anotherPoint instanceof vPoint)) {
            throw new ClassCastException("A vPoint object expected.");
        }
        if (((vPoint) anotherPoint).dist > dist) {
            return -1;
        } else if (((vPoint) anotherPoint).dist < dist) {
            return 1;
        } else {
            return 0;
        }
    }

    public void displayvPoint() {
        System.out.print(" x pos: " + x);
        System.out.print(", y pos: " + y);
        System.out.println(", dist: " + dist);
    }
}