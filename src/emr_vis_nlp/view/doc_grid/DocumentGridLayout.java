
package emr_vis_nlp.view.doc_grid;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import prefuse.Constants;
import prefuse.action.layout.Layout;
import prefuse.render.AbstractShapeRenderer;
import prefuse.visual.VisualItem;

/**
 * Performs layout-oriented functions for the DocumentGrid view. Namely, splits 
 * the Visualization up into (currently equal-sized) regions for the attributes 
 * on the x and y axes, creating a grid. Documents (VisualItems) are then 
 * organized into the appropriate grid squares, arranged as 
 * grids-within-a-grid.
 * 
 * Code partially adapted from prefuse.action.layout.AxisLayout
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocumentGridLayout extends Layout {

    
    // screen coordinate range
    private double x_min;
    private double x_max;
    private double x_range;
    private double y_min;
    private double y_max;
    private double y_range;
    private Rectangle2D bounds;
    
    // attributes for plotting points
    private String xAttr;
    private String yAttr;
    private List<String> xCats;
    private List<String> yCats;
    private Map<String, Integer> xCatPositionMap;
    private Map<String, Integer> yCatPositionMap;
    
    // buffer between items
    private static double buffer = 5.;
    private static double valueBuffer = 10.;
    
    public DocumentGridLayout(String group, String xAttr, String yAttr, List<String> xCats, List<String> yCats) {
        super(group);
        this.xAttr = xAttr;
        this.yAttr = yAttr;
        this.xCats = xCats;
        this.yCats = yCats;
        
        // build category maps, mapping value to position (for quick lookup)
        xCatPositionMap = new HashMap<>();
        for (int i=0; i<xCats.size(); i++) {
            xCatPositionMap.put(xCats.get(i), i);
        }
        yCatPositionMap = new HashMap<>();
        for (int i=0; i<yCats.size(); i++) {
            yCatPositionMap.put(yCats.get(i), i);
        }
        
        // update bounding box info
//        setMinMax();  // update at layout-time, if we update at construction we will get null pointers
    }
    
    
    
    @Override
    public void run(double d) {
//        TupleSet ts = m_vis.getGroup(m_group);
        setMinMax();
//        categoricalLayout(ts);
        categoricalLayout();
    }
    
    /**
     * Set the minimum and maximum pixel values.
     */
    private void setMinMax() {
        bounds = getLayoutBounds();
        x_min = bounds.getMinX();
        x_max = bounds.getMaxX();
        x_range = x_max - x_min;
        y_min = bounds.getMinY();
        y_max = bounds.getMaxY();
        y_range = y_max - y_min;
    }
    
    protected void categoricalLayout() {
        
        Iterator iter = m_vis.items(m_group);  // optionally, can add predicatefilter as second argument, if needed
        int numItems = m_vis.size(m_group);
        
        // keep track of number of items positioned (so far) in each grid cell
        int[][] gridCellItemCount = new int[xCats.size()][yCats.size()];
        
        // get height, width vals for items based on # per row, # possible rows, size of bounds
        setMinMax();
        double cellWidth = x_range / xCats.size() - valueBuffer*2;
        double cellHeight = y_range / yCats.size() - valueBuffer*2;
        
        // given number of items, compute # per row in cell
        // assume worst-case of all docs going to single cell
        // TODO go beyond square, adapt to # of categories, # of items per categories, bounds sizes, etc.
        int itemsPerCellRow = (int)Math.sqrt(numItems);
        int rowsPerCell = itemsPerCellRow+1;
        
        double itemWidth = (cellWidth / Math.sqrt(numItems)) - 2*buffer;
        double itemHeight = (cellHeight / (Math.sqrt(numItems)+1)) - 2*buffer;
        
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            // get category values for the target attributes for item
            // note: fields should always be populated; we shouldn't have to check whether they're available 1st (but safest to do it anyway)
            String xAttrVal = "";
            if (item.canGetString(xAttr)) xAttrVal = item.getString(xAttr);
            String yAttrVal = "";
            if (item.canGetString(yAttr)) yAttrVal = item.getString(yAttr);
            // get position of each category value on the {x,y} axis
            int xAttrPos = -1;
            if (xCatPositionMap.containsKey(xAttrVal)) xAttrPos = xCatPositionMap.get(xAttrVal);
            int yAttrPos = -1;
            if (yCatPositionMap.containsKey(yAttrVal)) yAttrPos = yCatPositionMap.get(yAttrVal);
            
            if (xAttrPos != -1 && yAttrPos != -1) {
                
                int numInCell = gridCellItemCount[xAttrPos][yAttrPos]++;
                int cellRow = numInCell / itemsPerCellRow;
                int cellCol = numInCell % itemsPerCellRow;
                
                // compute actual position on the screen
                double cellStartX = x_min + ((valueBuffer*2 + cellWidth)*xAttrPos) + valueBuffer;
                double cellStartY = y_min + ((valueBuffer*2 + cellHeight)*yAttrPos) + valueBuffer;
//                double cellStartX = x_min + (cellWidth*xAttrPos);
//                double cellStartY = y_min + (cellHeight*yAttrPos);
                
                double withinCellOffsetX = cellCol * (buffer*2 + itemWidth) + buffer;
                double withinCellOffsetY = cellRow * (buffer*2 + itemHeight) + buffer;
//                double itemSize = item.getSize();
////                item.setSize(2);
//                itemSize = item.getSize();
//                item.setShape(Constants.SHAPE_RECTANGLE);
//                double withinCellOffsetX = cellCol * (buffer + itemSize);
//                double withinCellOffsetY = cellRow * (buffer + itemSize);
                
                // position actual item; also adjust size
                double positionX = cellStartX+withinCellOffsetX;
                double positionY = cellStartY+withinCellOffsetY;
//                item.setBounds(positionX, positionY, itemWidth, itemHeight);
//              item.setBounds(0, 0, x_max, y_max);  
                item.setBounds(Double.MIN_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);  
                
//                item.setX(positionX);
//                item.setY(positionY);
//                item.setSize(itemWidth * itemHeight);
                setX(item, null, positionX);
                setY(item, null, positionY);
                
            } else {
                // cell undefined for x or y; shouldn't happen
                assert false: "cell undefined: xAttrVal="+xAttrVal+", xAttrPos="+xAttrPos+", yAttrVal="+yAttrVal+", yAttrPos="+yAttrPos;
            }
            
            
        }
        
    }

    
    
    
    
    
    
    
    
    
    
    public String getxAttr() {
        return xAttr;
    }

    public List<String> getxCats() {
        return xCats;
    }

    public void setXAxis(String xAttr, List<String> xCats) {
        this.xAttr = xAttr;
        this.xCats = xCats;
        
        xCatPositionMap = new HashMap<>();
        for (int i=0; i<xCats.size(); i++) {
            xCatPositionMap.put(xCats.get(i), i);
        }
    }

    public String getyAttr() {
        return yAttr;
    }

    public List<String> getyCats() {
        return yCats;
    }

    public void setYAxis(String yAttr, List<String> yCats) {
        this.yAttr = yAttr;
        this.yCats = yCats;
        
        yCatPositionMap = new HashMap<>();
        for (int i=0; i<yCats.size(); i++) {
            yCatPositionMap.put(yCats.get(i), i);
        }
    }
    
}
