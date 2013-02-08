/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annotator.visualizer;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import prefuse.action.layout.Layout;
import prefuse.data.Tuple;
import prefuse.visual.DecoratorItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author harry hochheiser
 */
public class EdgeLabelLayout extends Layout {

    public EdgeLabelLayout(String group) {
        super(group);
    }

    public void run(double frac) {
        @SuppressWarnings("unchecked")
        Iterator<Tuple> iter = (Iterator<Tuple>) m_vis.items(m_group);
        while (iter.hasNext()) {
            DecoratorItem decorator = (DecoratorItem) iter.next();
            VisualItem decoratedItem = decorator.getDecoratedItem();
            Rectangle2D bounds = decoratedItem.getBounds();

            double x = bounds.getCenterX();
            double y = bounds.getCenterY();

            //modification to move edge labels more to the arrow head
            double x2 = 0, y2 = 0;
            if (decoratedItem instanceof EdgeItem) {
                VisualItem dest = ((EdgeItem) decoratedItem).getTargetItem();
                x2 = dest.getX();
                y2 = dest.getY();
                x = (x + x2) / 2;
                y = (y + y2) / 2;
            }


            setX(decorator, null, x);
            setY(decorator, null, y);
        }
    }

}