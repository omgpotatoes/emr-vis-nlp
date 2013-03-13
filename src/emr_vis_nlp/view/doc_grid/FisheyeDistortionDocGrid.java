
package emr_vis_nlp.view.doc_grid;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import prefuse.action.distortion.FisheyeDistortion;

/**
 * Heavily based on prefuse.action.distortion.FisheyeDistortion .
 *
 * @author alexander.p.conrad@gmail.com
 */
public class FisheyeDistortionDocGrid extends FisheyeDistortion {
    
    private double  dx, dy;   // distortion factors
    private double  sz; // size factor
    private Rectangle2D m_bbox;
    
    private double mx, my; // magnification factor (for bifocal)
    private double rx, ry; // magnification ranges (for bifocal)
    
    /**
     * Create a new FisheyeDistortion with default distortion factor.
     */
    public FisheyeDistortionDocGrid(Rectangle2D bbox) {
        this(3, 3, bbox);
    }
    
    /**
     * Create a new FisheyeDistortion with the given distortion factor
     * for use along both the x and y directions.
     * @param dfactor the distortion factor (same for both axes)
     */
    public FisheyeDistortionDocGrid(double dfactor, double sfactor, Rectangle2D bbox) {
        this(dfactor, dfactor, sfactor, bbox);
    }
    
    /**
     * Create a new FisheyeDistortion with the given distortion factors
     * along the x and y directions.
     * @param xfactor the distortion factor along the x axis
     * @param yfactor the distortion factor along the y axis
     */
    public FisheyeDistortionDocGrid(double xfactor, double yfactor, double sfactor, Rectangle2D bbox) {
        super();
        m_bbox = bbox;
        sz = sfactor;
        dx = xfactor;
        dy = yfactor;
        m_distortX = dx > 0;
        m_distortY = dy > 0;
        
        // for bifocal
        rx = 0.1;
        mx = 3;
        ry = 0.1;
        my = 3;
        
    }
    
    /**
     * Returns the distortion factor for the x-axis.
     * @return returns the distortion factor for the x-axis.
     */
    public double getXDistortionFactor() {
        return dx;
    }

    /**
     * Sets the distortion factor for the x-axis.
     * @param d The distortion factor to set.
     */
    public void setXDistortionFactor(double d) {
        dx = d;
        m_distortX = dx > 0;
    }
    
    /**
     * Returns the distortion factor for the y-axis.
     * @return returns the distortion factor for the y-axis.
     */
    public double getYDistortionFactor() {
        return dy;
    }

    /**
     * Sets the distortion factor for the y-axis.
     * @param d The distortion factor to set.
     */
    public void setYDistortionFactor(double d) {
        dy = d;
        m_distortY = dy > 0;
    }
    
    /**
     * @see prefuse.action.distortion.Distortion#distortX(double, java.awt.geom.Point2D, java.awt.geom.Rectangle2D)
     */
    protected double distortX(double x, Point2D anchor, Rectangle2D bounds) {
        bounds = m_bbox;
        return fisheye(x,anchor.getX(),dx,bounds.getMinX(),bounds.getMaxX());
    }
    
    /**
     * @see prefuse.action.distortion.Distortion#distortY(double, java.awt.geom.Point2D, java.awt.geom.Rectangle2D)
     */
    protected double distortY(double y, Point2D anchor, Rectangle2D bounds) {
        bounds = m_bbox;
        return fisheye(y,anchor.getY(),dy,bounds.getMinY(),bounds.getMaxY());
    }
    
    protected double distortSize(Rectangle2D bbox, double x, double y, Point2D anchor, Rectangle2D bounds) { 
//        return distortSizeBifocal(bbox, x, y, anchor, bounds);
        return distortSizeFisheye(bbox, x, y, anchor, bounds);
    }
    
    /**
     * @see prefuse.action.distortion.Distortion#distortSize(java.awt.geom.Rectangle2D, double, double, java.awt.geom.Point2D, java.awt.geom.Rectangle2D)
     */
    protected double distortSizeFisheye(Rectangle2D bbox, double x, double y, Point2D anchor, Rectangle2D bounds) { 
        if ( !m_distortX && !m_distortY ) return 1.;
        double fx=1, fy=1;
        
        // ignore bbox, bounds parameters, use bbox assigned at instantiation?
//        bbox = m_bbox;
//        // debug
//        System.out.println("debug: "+this.getClass().getName()+": old bbox: "+bbox.toString());
        bbox = new Rectangle((int)x, (int)y, 15, 15);  // TODO get proper size info into this method!
//        // debug
//        System.out.println("debug: "+this.getClass().getName()+": new bbox: "+bbox.toString());
//        // debug
//        System.out.println("debug: "+this.getClass().getName()+": old bounds: "+bounds.toString());
        bounds = m_bbox;
//        // debug
//        System.out.println("debug: "+this.getClass().getName()+": new bounds: "+bounds.toString()+"\n\n");
        
        if ( m_distortX ) {
            double ax = anchor.getX();  // ax == coord of anchor
            double minX = bbox.getMinX(), maxX = bbox.getMaxX();  // minx == smallest poss. val, maxx == max poss. val.
            double xx = (Math.abs(minX-ax) > Math.abs(maxX-ax) ? minX : maxX);  // xx == minx or maxx, whichever is farther from anchor
            if ( xx < bounds.getMinX() || xx > bounds.getMaxX() )  // ensure that anchor is within space (at boundaries)
                xx = (xx==minX ? maxX : minX);
//            fx = fisheyeNoEdge(xx,ax,dx,bounds.getMinX(),bounds.getMaxX());
            fx = fisheye(xx,ax,dx,bounds.getMinX(),bounds.getMaxX());
//            fx = fisheye(x, ax, dx, bbox.getMinX(), bbox.getMaxX());

                        fx = Math.abs(x-fx)/bbox.getWidth();  // (distance between item and fisheye coordd) / region size
        }

        if ( m_distortY ) {
            double ay = anchor.getY();
            double minY = bbox.getMinY(), maxY = bbox.getMaxY();
            double yy = (Math.abs(minY-ay) > Math.abs(maxY-ay) ? minY : maxY);
            if ( yy < bounds.getMinY() || yy > bounds.getMaxY() )
                yy = (yy==minY ? maxY : minY);
//            fy = fisheyeNoEdge(yy,ay,dy,bounds.getMinY(),bounds.getMaxY());
            fy = fisheye(yy,ay,dy,bounds.getMinY(),bounds.getMaxY());
//            fy = fisheye(y, ay, dy, bbox.getMinY(), bbox.getMaxY());

                        fy = Math.abs(y-fy)/bbox.getHeight();
        }
        
//        double sf = (!m_distortY ? fx : (!m_distortX ? fy : Math.min(fx,fy)));
        // take average instead of min?
        double sf = (fx+fy)/2.;
        if (Double.isInfinite(sf) || Double.isNaN(sf)) {
            return 1.;
        } else {
            double factor = sz*sf;
//            if (factor > 1.)
//                System.out.println("debug: "+this.getClass().getName()+": returning distortion factor > 1. :"+factor);
            return factor;
        }
    }
    
    protected double distortSizeBifocal(Rectangle2D bbox, double x, double y, 
            Point2D anchor, Rectangle2D bounds)
    {
        
        // ignore bbox, bounds parameters, use bbox assigned at instantiation?
//        bbox = m_bbox;
//        // debug
//        System.out.println("debug: "+this.getClass().getName()+": old bbox: "+bbox.toString());
        bbox = new Rectangle((int)x, (int)y, 15, 15);  // TODO get proper size info into this method!
//        // debug
//        System.out.println("debug: "+this.getClass().getName()+": new bbox: "+bbox.toString());
//        // debug
//        System.out.println("debug: "+this.getClass().getName()+": old bounds: "+bounds.toString());
        bounds = m_bbox;
//        // debug
//        System.out.println("debug: "+this.getClass().getName()+": new bounds: "+bounds.toString()+"\n\n");
        
        
        
        boolean xmag = false, ymag = false;
        double m;
        
        if ( m_distortX ) {
            double cx = bbox.getCenterX(), ax = anchor.getX();
            double minX = bounds.getMinX(), maxX = bounds.getMaxX();
            m = (cx<ax ? ax-minX : maxX-ax);
            if ( m == 0 ) m = maxX-minX;
            if ( Math.abs(cx-ax) <= rx*m )
                xmag = true;
        }
        
        if ( m_distortY ) {
            double cy = bbox.getCenterY(), ay = anchor.getY();
            double minY = bounds.getMinY(), maxY = bounds.getMaxY();
            m = (cy<ay ? ay-minY : maxY-ay);
            if ( m == 0 ) m = maxY-minY;
            if ( Math.abs(cy-ay) <= ry*m )
                ymag = true;
        }
        
        if ( xmag && !m_distortY ) {
            return mx;
        } else if ( ymag && !m_distortX ) {
            return my;
        } else if ( xmag && ymag ) {
            return Math.min(mx,my);
        } else {
            return Math.min((1-rx*mx)/(1-rx), (1-ry*my)/(1-ry));
        }
    }
    
    private double fisheye(double x, double a, double d, double min, double max) {
        if ( d != 0 ) {
            boolean left = x<a;  // is item to left of anchor?
            double v, m = (left ? a-min : max-a);  // m == distance from anchor to nearest side
            if ( m == 0 ) m = max-min;  // (if no anchor, m == distance between sides)
            // ignore distance to side?
//            m = max-min;
            v = Math.abs(x - a) / m;  // v == (distance between item and anchor) / (distance from anchor to nearest side)
            v = (d+1)/(d+(1/v));  // v == (dist. distort factor + 1) / (dist. distort factor + (1 / ((distance between item and anchor) / (distance from anchor to nearest side))))
            return (left?-1:1)*m*v + a;  // (if item to left of anchor -1 else 1) * (distance between sides) * v + anhcor position)
        } else {
            return x;
        }
    }
    
    // same as fisheye from FisheyeDistortion, but does not care about distance of anchor from walls
    private double fisheyeNoEdge(double x, double a, double d, double min, double max) {
        if ( d != 0 ) {
            boolean left = x<a;  // is item to left of anchor?
            double v, m = (left ? a-min : max-a);  // m == distance from anchor to nearest side
            if ( m == 0 ) m = max-min;  // (if no anchor, m == distance between sides)
            // ignore distance to side?
//            m = max-min;
            m=1.1;
            v = Math.abs(x - a) / m;  // v == (distance between item and anchor) / (distance from anchor to nearest side)
            v = (d+1)/(d+(1/v));  // v == (dist. distort factor + 1) / (dist. distort factor + (1 / ((distance between item and anchor) / (distance from anchor to nearest side))))
            return (left?-1:1)*m*v + a;  // (if item to left of anchor -1 else 1) * (distance between sides) * v + anhcor position)
        } else {
            return x;
        }
    }
    
    // from BifocalDistortion
    private double bifocal(double x, double a, double r, 
                           double mag, double min, double max)
    {
        double m = (x<a ? a-min : max-a);
        if ( m == 0 ) m = max-min;
        double v = x - a, s = m*r;
        if ( Math.abs(v) <= s ) {  // in focus
            return x = v*mag + a;
        } else {                   // out of focus
            double bx = r*mag;
            x = ((Math.abs(v)-s) / m) * ((1-bx)/(1-r));
            return (v<0?-1:1)*m*(x + bx) + a;
        }
    }
    
    
}
