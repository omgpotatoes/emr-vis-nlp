
package emr_vis_nlp.view.doc_grid;

import emr_vis_nlp.controller.MainController;
import java.awt.geom.Rectangle2D;
import java.util.*;
import prefuse.Constants;
import prefuse.action.layout.Layout;
import prefuse.visual.VisualItem;

/**
 * Performs layout-oriented functions for the DocumentGrid view. Namely, splits 
 * the Visualization up into (currently equal-sized) regions for the attributes 
 * on the x and y axes, creating a grid. Documents (VisualItems) are then 
 * organized into the appropriate grid squares, arranged as 
 * grids-within-a-grid.
 * 
 * Code partially adapted from prefuse.action.layout.AxisLayout, AxisLabelLayout
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocumentGridLayout extends Layout {

    
    private static final String X_LABEL = DocumentGrid.X_LABEL;
    private static final String Y_LABEL = DocumentGrid.Y_LABEL;
    
    private MainController controller;
    
    // screen coordinate range
    private double x_min;
    private double x_max;
    private double x_range;
    private double y_min;
    private double y_max;
    private double y_range;
    private Rectangle2D bounds;
    
    // attributes for plotting points
    // TODO eliminate this group of lists by defining new objects for category positioning information for each category?
    // name of attribute on x-axis
    private String xAttr;
    // name of arrtibute on y-axis
    private String yAttr;
    // list of categorical values for x-axis attribute
    private List<String> xCats;
    // list of categorical values for y-axis attribute
    private List<String> yCats;
    // list of Visualization coordinates where the region corresponding to each of the x-axis categories should begin
    private List<Integer> xCatPositions;
    // list of Visualization coordinates where the region corresponding to each of the y-axis categories should begin
    private List<Integer> yCatPositions;
    // list of Visualization coordinate ranges for the region corresponding to each of the x-axis categories
    private List<Integer> xCatRegionSizes;
    // list of Visualization coordinate ranges for the region corresponding to each of the y-axis categories
    private List<Integer> yCatRegionSizes;
    // map from x-attribute category name to its index in the other lists
    private Map<String, Integer> xCatPositionMap;
    // map from y-attribute category name to its index in the other lists
    private Map<String, Integer> yCatPositionMap;
    // counts for number of documents in each cell created by the x- and y-attribute categories
    private int[][] gridCellItemCount;
    
    // buffer between items (when using static positioning)
    private static double buffer = 8.;
    
    // buffer on edges of each region, as % of region
    private static double regionBuff = 0.15;
    
    
    /**
     * 
     * 
     * @param controller the governing MainController for this instance
     * @param group
     * @param xAttr
     * @param yAttr
     * @param xCats
     * @param yCats 
     */
    public DocumentGridLayout(MainController controller, String group, String xAttr, String yAttr, List<String> xCats, List<String> yCats) {
        super(group);
        this.controller = controller;
        
        // perform initial attribute assignment
        setXAxis(xAttr, xCats);
        setYAxis(yAttr, yCats);
        
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
        // TODO perform buffering based on % of cell, not on fixed pixel counts!
        int xBufferMin = 30;
        int yBufferMin = 5;
        int xBufferMax = 5;
        int yBufferMax = 40;
        bounds = getLayoutBounds();
        x_min = bounds.getMinX() + xBufferMin;
        x_max = bounds.getMaxX() - xBufferMax;
        x_range = x_max - x_min;
        y_min = bounds.getMinY() + yBufferMin;
        y_max = bounds.getMaxY() - yBufferMax;
        y_range = y_max - y_min;
    }
    
    protected void categoricalLayout() {
        
        Iterator iter = m_vis.items(m_group);  // optionally, can add predicatefilter as second argument, if needed
        int numItems = m_vis.size(m_group);
        
        // populate initial grid, so we can calculate % sizes of each row, col
        int[][] gridCellItemCountInit = new int[xCats.size()][yCats.size()];
        int[] rowItemCountInit = new int[yCats.size()];
        int[] colItemCountInit = new int[xCats.size()];
        int totalItemCount = 0;
//        double avgHeight = 0;
//        double avgWidth = 0;
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
//            avgHeight += item.getBounds().getHeight();
//            avgWidth += item.getBounds().getWidth();
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
                 gridCellItemCountInit[xAttrPos][yAttrPos]++;
                 rowItemCountInit[yAttrPos]++;
                 colItemCountInit[xAttrPos]++;
                 totalItemCount++;
            }
        }
        
//        avgHeight /= numItems;
//        avgWidth /= numItems;
        
        // keep track of number of items positioned (so far) in each grid cell
        gridCellItemCount = new int[xCats.size()][yCats.size()];
        
        // reset iterator (2nd pass will position doc items)
        iter = m_vis.items(m_group);
        
        // get height, width vals for items based on # per row, # possible rows, size of bounds
        setMinMax();
//        double frameBufferX = 25;  // to ensure that we stay within the bounds of the frame
//        double frameBufferY = 50;
//        double generalCellWidth = (x_range-frameBufferX) / xCats.size();
//        double generalCellHeight = (y_range-frameBufferY) / yCats.size();
        // size of item: (assume most-efficient square layout) ((x_range - buffer * numCats) / (sqrt[#items] + buffer))
        //  for now, artificially shrink item size (till we can fix issue concerning region overrun)
        double itemSizeMult = 1.6;
//        double itemWidth = (x_range - buffer * xCats.size()) / ((Math.sqrt(numItems) + xCats.size())*itemSizeMult) - buffer;
//        double itemHeight = (y_range - buffer * yCats.size()) / ((Math.sqrt(numItems) + yCats.size())*itemSizeMult) - buffer;
        double itemWidth = 5;
        double itemHeight = 5;
//        double itemWidth = avgWidth;
//        double itemHeight = avgHeight;

        // compute region sizes for each row, column cell
        xCatRegionSizes = new ArrayList<>();
        xCatPositions = new ArrayList<>();
        for (int i=0; i<xCats.size(); i++) {
//            int regionSize = (int)((x_range - frameBufferX) * ((double)colItemCountInit[i]/(double)totalItemCount));
            int regionSize = (int)((x_range) * ((double)colItemCountInit[i]/(double)totalItemCount));
            if (regionSize < 2*buffer + itemWidth) {
                regionSize = (int)(2*buffer + itemWidth);
            }
            xCatRegionSizes.add(regionSize);
            double regionSizeSum = x_min;  // initial buffer
            for (int j=0; j<i; j++) {
                regionSizeSum += xCatRegionSizes.get(j);
            }
            xCatPositions.add((int)regionSizeSum);
        }
        yCatRegionSizes = new ArrayList<>();
        yCatPositions = new ArrayList<>();
        for (int i=0; i<yCats.size(); i++) {
//            int regionSize = (int)((y_range - frameBufferY) * ((double)rowItemCountInit[i]/(double)totalItemCount));
            int regionSize = (int)((y_range) * ((double)rowItemCountInit[i]/(double)totalItemCount));
            // to ensure we have enough rom for at least 1 item
            if (regionSize < 2*buffer + itemHeight) {
                regionSize = (int)(2*buffer + itemHeight);
            }
            yCatRegionSizes.add((int)(regionSize));
            double regionSizeSum = y_min;  // initial buffer
            for (int j=0; j<i; j++) {
                regionSizeSum += yCatRegionSizes.get(j);
            }
            yCatPositions.add((int)regionSizeSum);
        }
        
        // given number of items, compute # per row in cell
        // assume worst-case of all docs going to single cell
        // TODO go beyond square, adapt to # of categories, # of items per categories, bounds sizes, etc.
        List<Integer> itemsPerCellRowPerCol = new ArrayList<>();
        // for each col, compute as (max # items in single full-view row) * (fraction of fullview occupied by this col)
        //  idea: even if all items in smallest col, make sure they can fit!
        for (int c=0; c<xCats.size(); c++) {
            // find row with max # items
            int maxItemCount = -1;
            int maxItemIndex = -1;
            for (int r=0; r<rowItemCountInit.length; r++) {
                int itemsInRow = rowItemCountInit[r];
                if (itemsInRow > maxItemCount) {
                    maxItemCount = itemsInRow;
                    maxItemIndex = r;
                }
            }
            // items per cell row (in a given col)
            //  = (width of cell) / (width of item [+ buffer])
            // temporarily amplify item sizes (TODO: more principled item size calculation)
//            int itemsPerColCellRow = (int)(xCatRegionSizes.get(c) / itemWidth);
            int itemsPerColCellRow = (int)(xCatRegionSizes.get(c) / (itemWidth * itemSizeMult));
//            int itemsPerColCellRow = (int)(xCatRegionSizes.get(c) / (itemWidth));
            if (itemsPerColCellRow == 0) itemsPerColCellRow = 1;
            itemsPerCellRowPerCol.add(itemsPerColCellRow);
        }
        
//        double itemWidth = (cellWidth / Math.sqrt(numItems)) - 2*buffer;
//        double itemHeight = (cellHeight / (Math.sqrt(numItems)+1)) - 2*buffer;
        
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
                int itemsPerColCellRow = itemsPerCellRowPerCol.get(xAttrPos);
                int cellRow = numInCell / itemsPerColCellRow;
                int cellCol = numInCell % itemsPerColCellRow;
                
                // compute actual position on the screen
                double cellStartX = xCatPositions.get(xAttrPos);
                double cellStartY = yCatPositions.get(yAttrPos);
                
                double cellBufferX = xCatRegionSizes.get(xAttrPos) * regionBuff;
                double cellBufferY = yCatRegionSizes.get(yAttrPos) * regionBuff;
                // TODO
                
                // if we have predictions, use them! otherwise, do default
                double withinCellOffsetX = cellCol * (buffer + itemWidth) + buffer;
                double withinCellOffsetY = cellRow * (buffer + itemHeight) + buffer;
                // dynamically buffer: 20% of cell?
//                double bufferPerc = 0.2;
                if (controller.hasPrediction(item.getInt(DocumentGridTable.NODE_ID),xAttr)) {
//                    withinCellOffsetX = xCatRegionSizes.get(xAttrPos) * controller.getPrediction(item.getInt(DocumentGridTable.NODE_ID),xAttr).getCert();
//                    withinCellOffsetX = (xCatRegionSizes.get(xAttrPos)*bufferPerc) + (xCatRegionSizes.get(xAttrPos) - (xCatRegionSizes.get(xAttrPos)*2*bufferPerc)) * controller.getPrediction(item.getInt(DocumentGridTable.NODE_ID),xAttr).getCert();
                    withinCellOffsetX = cellBufferX + xCatRegionSizes.get(xAttrPos) * controller.getPrediction(item.getInt(DocumentGridTable.NODE_ID),xAttr).getCert() * (1 - (2*regionBuff));
                }
                if (controller.hasPrediction(item.getInt(DocumentGridTable.NODE_ID),yAttr)) {
//                    withinCellOffsetY = yCatRegionSizes.get(yAttrPos) * controller.getPrediction(item.getInt(DocumentGridTable.NODE_ID),yAttr).getCert();
//                    withinCellOffsetY = (yCatRegionSizes.get(yAttrPos)*bufferPerc) + (yCatRegionSizes.get(yAttrPos) - (yCatRegionSizes.get(yAttrPos)*2*bufferPerc)) * controller.getPrediction(item.getInt(DocumentGridTable.NODE_ID),yAttr).getCert();
                    withinCellOffsetY = cellBufferY + yCatRegionSizes.get(yAttrPos) * controller.getPrediction(item.getInt(DocumentGridTable.NODE_ID),yAttr).getCert() * (1 - (2*regionBuff));
                }
                
                // position actual item; also adjust size
                double positionX = cellStartX+withinCellOffsetX;
                double positionY = cellStartY+withinCellOffsetY;
                
                // set actual item position properties
//                item.setX(positionX);
//                item.setY(positionY);
//                item.setSize(itemWidth * itemHeight);
//                setX(item, null, positionX);
//                setY(item, null, positionY);
//                item.setShape(Constants.SHAPE_RECTANGLE);
//                item.setSize(10);
//                item.setBounds(positionX, positionY, positionX+itemWidth, positionY+itemHeight);
                item.setBounds(Double.MIN_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
//                item.setBounds(positionX, positionY, itemWidth, itemHeight);
                item.set(VisualItem.X, (double)(positionX));
                item.setEndX(positionX);
                item.set(VisualItem.Y, (double)(positionY));
                item.setEndY(positionY);
                 // TODO replace X2, Y2 with proper action-based size handling?
                item.set(VisualItem.X2, (double)(positionX+itemWidth));
                item.set(VisualItem.Y2, (double)(positionY+itemHeight));
                
            } else {
                // cell undefined for x or y; shouldn't happen
                System.err.println("DocumentGridLayout: cell undefined: xAttrVal="+xAttrVal+", xAttrPos="+xAttrPos+", yAttrVal="+yAttrVal+", yAttrPos="+yAttrPos);
                assert false: "cell undefined: xAttrVal="+xAttrVal+", xAttrPos="+xAttrPos+", yAttrVal="+yAttrVal+", yAttrPos="+yAttrPos;
            }
            
        }
        
    }

    /**
     * 
     * @return name of attribute currently on X axis.
     */
    public String getXAttr() {
        return xAttr;
    }
    
    /**
     * 
     * @return list of value names for attribute on X axis.
     */
    public List<String> getXCats() {
        return xCats;
    }

    /**
     * Updates X axis with a new attribute.
     * 
     * @param xAttr name of attribute
     * @param xCats list of value names for attribute
     */
    public void updateXAxis(String xAttr, List<String> xCats) {
        setXAxis(xAttr, xCats);
        run();
    }
    
    private void setXAxis(String xAttr, List<String> xCats) {
        this.xAttr = xAttr;
        this.xCats = xCats;
        
        xCatPositionMap = new HashMap<>();
        for (int i=0; i<xCats.size(); i++) {
            xCatPositionMap.put(xCats.get(i), i);
//            xCatPositionMap.put(xCats.get(i), xCats.size()-1-i);
        }
    }

    /**
     * 
     * @return name of attribute currently on Y axis.
     */
    public String getYAttr() {
        return yAttr;
    }

    
    /**
     * 
     * @return list of value names for attribute on Y axis.
     */
    public List<String> getYCats() {
        return yCats;
    }

    /**
     * Updates Y axis with a new attribute.
     * 
     * @param xAttr name of attribute
     * @param xCats list of value names for attribute
     */
    public void updateYAxis(String yAttr, List<String> yCats) {
        setYAxis(yAttr, yCats);
        run();
    }
    
    private void setYAxis(String yAttr, List<String> yCats) {
        this.yAttr = yAttr;
        this.yCats = yCats;
        
        yCatPositionMap = new HashMap<>();
        for (int i=0; i<yCats.size(); i++) {
            yCatPositionMap.put(yCats.get(i), i);
//            yCatPositionMap.put(yCats.get(i), yCats.size()-1-i);
        }
    }

    /**
     * 
     * @return list containing x-coords of vertical grid lines
     */
    public List<Integer> getXCatPositions() {
        return xCatPositions;
    }
    
    /**
     * 
     * @return list containing column widths between grid lines
     */
    public List<Integer> getXCatRegionSizes() {
        return xCatRegionSizes;
    }

    /**
     * 
     * @return list containing y-coords of horizontal grid lines
     */
    public List<Integer> getYCatPositions() {
        return yCatPositions;
    }

    /**
     * 
     * @return list containing row heights between grid lines
     */
    public List<Integer> getYCatRegionSizes() {
        return yCatRegionSizes;
    }
    
}
