/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package crowdsimulation.entities;

import crowdsimulation.entities.individual.Individual;
import math.Vector2D;
import sim.field.continuous.Continuous2D;

/**
 *
 * @author ganil
 */
public class KaupLineWaypoint extends LineWaypoint {

    private boolean DEBUG = false;
    double x1;
    double y1;
    double x2;
    double y2;

    public KaupLineWaypoint(Continuous2D terrain, double x, double y, double length, double angle) {
        super(terrain, x, y, length, angle);
        init();
    }

    public KaupLineWaypoint(Continuous2D terrain, double x, double y, double length, double angle, double left, double right) {
        super(terrain, x, y, length, angle, left, right);
        init();
    }

    void init() {
        x1 = x + Math.cos(angle) * length * leftTarget;
        y1 = y + Math.sin(angle) * length * leftTarget;
        x2 = x + Math.cos(angle) * length * rightTarget;
        y2 = y + Math.sin(angle) * length * rightTarget;
        System.out.println("Radius = " + getRadius());
    }

    @Override
    public Vector2D getTargetPoint(Individual ind) {

        Vector2D super_target = super.getTargetPoint(ind);
        if(ind.getLocation().distance(super_target) < getRadius()) {
            if (DEBUG) System.out.println(ind.getID()+ ", ST " +super_target);
            return super_target;
        }
        double ind_x = ind.getLocation().x;
        double ind_y = ind.getLocation().y;
        Vector2D ind_direction_wierd = ind.getVelocity().normalize();
        if (DEBUG) System.out.println(ind.getID()+ ", D " +ind_direction_wierd);
        Vector2D ind_direction = new Vector2D(ind_direction_wierd.x,-ind_direction_wierd.y);
        Vector2D first_end = new Vector2D(x1 - ind_x, ind_y - y1).normalize();
        Vector2D second_end = new Vector2D(x2 - ind_x, ind_y - y2).normalize();

        double second_angle = second_end.getAngle();
        double ind_angle = ind_direction.getAngle();
        double first_angle = first_end.getAngle();

        if (DEBUG)  System.out.println(ind.getID()+ ", First angle = "+first_angle + ", second angle = "+ second_angle);
        //double angle_diff = (second_angle - ind_angle + 2 * Math.PI) % (2 * Math.PI);
        //if (DEBUG) System.out.println (second_angle+", "+ ind_angle+" ");
        if (Vector2D.isAngleInSmallestArc(second_angle,first_angle,ind_angle)) {
            double scale = (first_end.magnitude()+ second_end.magnitude())/2;
            if (DEBUG) {
                //System.out.println("I" + new Vector2D(x2, y2));
            }
            if (DEBUG) System.out.println(ind.getID()+ ", T " +new Vector2D(ind_x + ind_direction_wierd.x * scale, ind_y + ind_direction_wierd.y * scale));
            return new Vector2D(ind_x + ind_direction_wierd.x * scale, ind_y + ind_direction_wierd.y * scale);
            //return new Vector2D(ind_x + ind_direction.x * scale, ind_y + ind_direction.y * scale).add(super.getTargetPoint(ind));
        } else {
            Vector2D r = getCenter();
            if (DEBUG) System.out.println(ind.getID()+ ", TC " +r);
            return r;

        }
        
    }

    public double angleBetween(Vector2D v1, Vector2D v2) {
        double a1 = v1.getAngle();
        double a2 = v2.getAngle();
        double ab = a2 - a1;
        if (ab < -Math.PI) {
            ab += 2 * Math.PI;
        } else if (ab > Math.PI) {
            ab -= 2 * Math.PI;
        }
        return ab;
    }
}
