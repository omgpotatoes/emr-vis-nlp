
package emr_vis_nlp.view.doc_grid;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import prefuse.Constants;
import prefuse.render.AbstractShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.visual.VisualItem;

/**
 * Renderer for the variable-sized axes used by the DocumentGrid view.
 *
 * Based heavily upon prefuse.render.AxisRenderer (with substantial modifications).
 * 
 * @author alexander.p.conrad
 */
public class DocumentGridAxisRenderer extends AbstractShapeRenderer {
    
    
    private Line2D      m_line = new Line2D.Double();
    private Rectangle2D m_box  = new Rectangle2D.Double();
    
    // don't need to worry about alignment;
    //  !isX == left & center (with offsets to put labels in middle!)
    //  isX == center & bottom (again, with appropriate offsets!)
//    private int m_xalign;
//    private int m_yalign;
    
    private int m_ascent;
    
    private int m_xalign_vert;
    private int m_yalign_vert;
    private int m_xalign_horiz;
    private int m_yalign_horiz;
    
    private DocumentGridLayout docGridLayout;  // pointer to DocumentGridLayout for which this instance is drawing the axes
    
    public DocumentGridAxisRenderer(DocumentGridLayout docGridLayout) {
        this.docGridLayout = docGridLayout;
        m_xalign_horiz = Constants.LEFT;
        m_yalign_horiz = Constants.CENTER;
        m_xalign_vert = Constants.CENTER;
        m_yalign_vert = Constants.BOTTOM;
    }
    
    
    /**
     * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
     */
    @Override
    protected Shape getRawShape(VisualItem item) {
        double x1 = item.getDouble(VisualItem.X);
        double y1 = item.getDouble(VisualItem.Y);
        double x2 = item.getDouble(VisualItem.X2);
        double y2 = item.getDouble(VisualItem.Y2);
        boolean isX = item.getBoolean(DocumentGridAxisLayout.IS_X);
        double midPoint = item.getDouble(DocumentGridAxisLayout.MID_POINT);
//        m_line.setLine(x1,y1,x2,y2);
        // horizontal or vertical coords should be manually held constant so that fisheye works properly
        if (isX) {
            // vertical line
            m_line.setLine(x1,y1,x1,y2);
        } else {
            // horizontal line 
            m_line.setLine(x1,y1,x2,y1);
        }
        
        if ( !item.canGetString(VisualItem.LABEL) )
            return m_line;
        
        String label = item.getString(VisualItem.LABEL);
        if ( label == null ) return m_line;
        
        FontMetrics fm = DEFAULT_GRAPHICS.getFontMetrics(item.getFont());
        m_ascent = fm.getAscent();
        int h = fm.getHeight();
        int w = fm.stringWidth(label);
        
        double tx, ty;
        
        if (isX) {
            // vertical axis
            // get text x-coord, center at midPoint
//            tx = x1 + (x2-x1)/2 - w/2;
//            tx = midPoint + (x1+midPoint)/2 - w/2;
            tx = x1 + midPoint/2 - w/2;
            // get text y-coord
            ty = y2-h;
        } else {
            // horiz axis
            // get text x-coord
            tx = x1 - w - 2;
            // get text y-coord, center at midPoint
//            ty = y1 + (y2-y1)/2 - h/2;
            ty = y1 + midPoint/2 - h/2;
        }
        
        // don't have to worry about switching;
//        // get text x-coord
//        switch ( m_xalign ) {
//        case Constants.FAR_RIGHT:
//            tx = x2 + 2;
//            break;
//        case Constants.FAR_LEFT:
//            tx = x1 - w - 2;
//            break;
//        case Constants.CENTER:
//            tx = x1 + (x2-x1)/2 - w/2;
//            break;
//        case Constants.RIGHT:
//            tx = x2 - w;
//            break;
//        case Constants.LEFT:
//        default:
//            tx = x1;
//        }
//        // get text y-coord
//        switch ( m_yalign ) {
//        case Constants.FAR_TOP:
//            ty = y1-h;
//            break;
//        case Constants.FAR_BOTTOM:
//            ty = y2;
//            break;
//        case Constants.CENTER:
//            ty = y1 + (y2-y1)/2 - h/2;
//            break;
//        case Constants.TOP:
//            ty = y1;
//            break;
//        case Constants.BOTTOM:
//        default:
//            ty = y2-h; 
//        }
        
        m_box.setFrame(tx,ty,w,h);
        return m_box;
    }
    
    /**
     * @see prefuse.render.Renderer#render(java.awt.Graphics2D,
     * prefuse.visual.VisualItem)
     */
    @Override
    public void render(Graphics2D g, VisualItem item) {
        Shape s = getShape(item);
        GraphicsLib.paint(g, item, m_line, getStroke(item), getRenderType(item));

        // check if we have a text label, if so, render it 
        String str;
        if (item.canGetString(VisualItem.LABEL)) {
            str = (String) item.getString(VisualItem.LABEL);
            if (str != null && !str.equals("")) {
                float x = (float) m_box.getMinX();
                float y = (float) m_box.getMinY() + m_ascent;

                // draw label background 
                GraphicsLib.paint(g, item, s, null, RENDER_TYPE_FILL);

                AffineTransform origTransform = g.getTransform();
                AffineTransform transform = this.getTransform(item);
                if (transform != null) {
                    g.setTransform(transform);
                }

                g.setFont(item.getFont());
                g.setColor(ColorLib.getColor(item.getTextColor()));
                
                if (!(str.length() > 5 && str.substring(str.length() - 5, str.length()).equals("_last"))) {
                    
                    g.setColor(Color.WHITE);
                    // TODO properly hunt down source of null str! for now, triage
                    if (str != null) {
                        // bump y down by appropriate amount
                        FontMetrics fm = g.getFontMetrics(item.getFont());
                        int strHeight = fm.getAscent();
//                        g.drawString(str, x, y);
                        g.drawString(str, x, y+strHeight);
                    }
                    
                    if (transform != null) {
                        g.setTransform(origTransform);
                    }
                }
            }
        }
    }

    /**
     * @see prefuse.render.Renderer#locatePoint(java.awt.geom.Point2D, prefuse.visual.VisualItem)
     */
    public boolean locatePoint(Point2D p, VisualItem item) {
        Shape s = getShape(item);
        if ( s == null ) {
            return false;
        } else if ( s == m_box && m_box.contains(p) ) {
            return true;
        } else {
            double width = Math.max(2, item.getSize());
            double halfWidth = width/2.0;
            return s.intersects(p.getX()-halfWidth,
                                p.getY()-halfWidth,
                                width,width);
        }
    }

    /**
     * @see prefuse.render.Renderer#setBounds(prefuse.visual.VisualItem)
     */
    public void setBounds(VisualItem item) {
        if ( !m_manageBounds ) return;
        Shape shape = getShape(item);
        if ( shape == null ) {
            item.setBounds(item.getX(), item.getY(), 0, 0);
        } else if ( shape == m_line ) {
            GraphicsLib.setBounds(item, shape, getStroke(item));
        } else {
            m_box.add(m_line.getX1(),m_line.getY1());
            m_box.add(m_line.getX2(),m_line.getY2());
            item.setBounds(m_box.getMinX(), m_box.getMinY(),
                           m_box.getWidth(), m_box.getHeight());
        }
    }
    
    
    
}
