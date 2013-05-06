
package emr_vis_nlp.view.doc_grid;

import emr_vis_nlp.controller.MainController;
import java.awt.Color;
import java.awt.Font;
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
public class DocumentGridLayoutNested extends Layout {

    
    private static final String X_LABEL = DocumentGrid.X_LABEL;
    private static final String Y_LABEL = DocumentGrid.Y_LABEL;
    
    public static final String HEIGHT_FOCUS = DocumentGridTable.HEIGHT_FOCUS;
    public static final String WIDTH_FOCUS = DocumentGridTable.WIDTH_FOCUS;
    public static final String SELECT_FOCUS = DocumentGridTable.SELECT_FOCUS;
    public static final String GLOBAL_ROW = DocumentGridTable.GLOBAL_ROW;
    public static final String GLOBAL_COL = DocumentGridTable.GLOBAL_COL;
    public static final String WIDTH = DocumentGridTable.WIDTH;
    public static final String HEIGHT = DocumentGridTable.HEIGHT;
    public static final String WIDTH_START = DocumentGridTable.WIDTH_START;
    public static final String HEIGHT_START = DocumentGridTable.HEIGHT_START;
    public static final String WIDTH_END = DocumentGridTable.WIDTH_END;
    public static final String HEIGHT_END = DocumentGridTable.HEIGHT_END;
    
    static int TEXT_X_OFFSET = 1;
    static int TEXT_Y_OFFSET = 10;
    static int DEFAULT_ANIMATION_MILLIS = 250;
    static float FOCUS_SIZE_PERCENT = 0.45f;
    static Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 10);
    static double REGION_AREA_SMOOTH = 0.08;
    
    private int lastZoomWidth = -1;
    private int lastZoomHeight = -1;
    
    // axis layout related to this grid
    private DocumentGridAxisLayout docGridAxisLayout = null;
    
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
    // TODO : eliminate this group of lists by defining new objects for category positioning information for each category?
    // name of attribute on x-axis
    private String xAttr;
    // name of arrtibute on y-axis
    private String yAttr;
    // list of categorical values for x-axis attribute
    private List<String> attrValsX;
    // list of categorical values for y-axis attribute
    private List<String> attrValsY;
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
    public DocumentGridLayoutNested(String group, String xAttr, String yAttr, List<String> xCats, List<String> yCats) {
        super(group);
        this.controller = MainController.getMainController();
        
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
    
    /**
     * This method performs the bulk of the layout-related work. Two types of
     * layouts are computed here: 1. a row-based layout, where glyphs are
     * arranged adjacently to each other in order to make most-efficient use of
     * the space 2. within each cell, documents are arranged and sized in order
     * to make the most-efficient use of the space
     *
     */
    protected void categoricalLayout() {

        xCatPositions = new ArrayList<>();
        yCatPositions = new ArrayList<>();
        xCatRegionSizes = new ArrayList<>();
        yCatRegionSizes = new ArrayList<>();

        Iterator iter = m_vis.items(m_group);  // optionally, can add predicatefilter as second argument, if needed
        int numItems = m_vis.size(m_group);

        int[][] gridCellItemCountInit = new int[attrValsX.size()][attrValsY.size()];
        int[] rowItemCountInit = new int[attrValsY.size()];
        int[] colItemCountInit = new int[attrValsX.size()];
        double[] regionPercsX = new double[attrValsX.size()];
        double[] regionPercsY = new double[attrValsY.size()];
        List<VisualItem>[][] gridCellItems = new List[attrValsX.size()][attrValsY.size()];
        int totalItemCount = 0;
//        double avgHeight = 0;
//        double avgWidth = 0;
        while (iter.hasNext()) {
            VisualItem item = (VisualItem) iter.next();
            // reset item's location
            item.setX(-1.);
            item.setY(-1.);
            // get category values for the target attributes for item
            // note: fields should always be populated; we shouldn't have to check whether they're available 1st (but safest to do it anyway)
            String xAttrVal = "";
            if (item.canGetString(xAttr)) {
                xAttrVal = item.getString(xAttr);
            }
            String yAttrVal = "";
            if (item.canGetString(yAttr)) {
                yAttrVal = item.getString(yAttr);
            }
            // get position of each category value on the {x,y} axis
            int xAttrPos = -1;
            if (xCatPositionMap.containsKey(xAttrVal)) {
                xAttrPos = xCatPositionMap.get(xAttrVal);
            }
            int yAttrPos = -1;
            if (yCatPositionMap.containsKey(yAttrVal)) {
                yAttrPos = yCatPositionMap.get(yAttrVal);
            }
            if (xAttrPos != -1 && yAttrPos != -1) {
                gridCellItemCountInit[xAttrPos][yAttrPos]++;
                rowItemCountInit[yAttrPos]++;
                colItemCountInit[xAttrPos]++;
                totalItemCount++;
                if (gridCellItems[xAttrPos][yAttrPos] == null) {
                    gridCellItems[xAttrPos][yAttrPos] = new ArrayList<>();
                }
                gridCellItems[xAttrPos][yAttrPos].add(item);

            }
        }

        // compute percentages of full area for each region
        for (int x = 0; x < attrValsX.size(); x++) {
            int valCounter = 0;
            for (int y = 0; y < attrValsY.size(); y++) {
                if (gridCellItems[x][y] != null) {
                    valCounter += gridCellItems[x][y].size();
                }
            }
            // NOTE : regionPercs considers all documents, not just enabled; is this what we want?
            regionPercsX[x] = ((double) valCounter / (double) numItems) * (1. - (REGION_AREA_SMOOTH * attrValsX.size())) + REGION_AREA_SMOOTH;
        }

        for (int y = 0; y < attrValsY.size(); y++) {
            int valCounter = 0;
            for (int x = 0; x < attrValsX.size(); x++) {
                if (gridCellItems[x][y] != null) {
                    valCounter += gridCellItems[x][y].size();
                }
            }
            // NOTE: regionPercs considers all documents, not just enabled; is this what we want?
            regionPercsY[y] = ((double) valCounter / (double) numItems) * (1. - (REGION_AREA_SMOOTH * attrValsY.size())) + REGION_AREA_SMOOTH;
        }

        // compute size for each glyph such that size is used as efficiently as possible!
        //  idea: could we frame this as a linear programming optimization problem?

        double totalArea = x_range * y_range;
        double glyphArea = totalArea / (double) numItems;
        int glyphAreaPx = (int) glyphArea;
        int glyphWidthPx = (int) Math.sqrt(glyphArea);
        int glyphHeightPx = (int) Math.sqrt(glyphArea);


        // iteratively shirink glyph area until glyphs fit in all nested grids
        int glyphWidthShrinkIncrement = 1;
        int glyphHeightShrinkIncrement = 1;
        boolean doAllGlyphsFit;
        int numGaps;
        if (glyphWidthPx != 0 && glyphHeightPx != 0) {
            do {
                doAllGlyphsFit = true;
                for (int x = 0; x < attrValsX.size(); x++) {
                    numGaps = attrValsX.size() - 1;
                    int pxInColRegion = (int) ((x_range - numGaps * glyphWidthPx) * regionPercsX[x]);
                    int glyphsPerRowWithinColRegion = pxInColRegion / glyphWidthPx;

                    for (int y = 0; y < attrValsY.size(); y++) {
                        numGaps = attrValsY.size() - 1;
                        int pxInRowRegion = (int) ((y_range - numGaps * glyphHeightPx) * regionPercsY[y]);
                        int glyphsPerColWithinRow = pxInRowRegion / glyphHeightPx;
                        int numRowsInNestedGrid = glyphsPerColWithinRow;

                        // see if this cell can fit all its glyphs; if not, shrink glyphs
                        int maxNumGlyphsInCell = glyphsPerRowWithinColRegion * numRowsInNestedGrid;
                        if (gridCellItems[x][y] != null) {
                            int numGlyphsInCell = gridCellItems[x][y].size();
                            if (numGlyphsInCell > maxNumGlyphsInCell) {
                                glyphWidthPx -= glyphWidthShrinkIncrement;
                                glyphHeightPx -= glyphHeightShrinkIncrement;
                                doAllGlyphsFit = false;
                                break;
                            }
                        }

                    }
                }
            } while (!doAllGlyphsFit);
        } else {
            // if this display has zero size, assign arbitrary widths and heights to avoid exceptions (since nothing will be displayed, view isn't active)
            glyphWidthPx = 60;
            glyphHeightPx = 60;
            glyphAreaPx = glyphHeightPx * glyphWidthPx;
        }

        // for each column, determine # of items allowed for inner row
        int[] glyphsPerInnerRowInCol = new int[attrValsY.size()];
        for (int y = 0; y < attrValsY.size(); y++) {
            int pxInRegion = (int) (x_range * regionPercsY[y]);
            glyphsPerInnerRowInCol[y] = pxInRegion / glyphWidthPx;
        }

        // TODO : sort by certainties: 
        //  for each nested grid, sort first by the y-attr certainty (for predictions; manual annotations automatically pushed to front of list)
        //  to build an inner row of n items, take next n nodes from this sorted list
        //   next, to lay out row itself, sort the list of n nodes according to x-attr certainty
        // (but for immediate testing, leave sorting out until later)


        // compute position information for each DocGridNode (null node idea should not be necessary)
        // compute max ## of rows, cols
        int globalRowCount = (int) (y_range / glyphHeightPx);
        int globalColCount = (int) (x_range / glyphWidthPx);

        // pre-compute item counts at which we should switch to next cell
        // make sure to account for blank glyph space between cats!
        int[] gridEndingGlyphCountsX = new int[attrValsX.size()];
        int counter = 0;
        numGaps = attrValsX.size() - 1;
        for (int x = 0; x < attrValsX.size(); x++) {
            int pxInRegion = (int) ((x_range - numGaps * glyphWidthPx) * regionPercsX[x]);
            int glyphsPerRowWithinCol = pxInRegion / glyphWidthPx;
            if (glyphsPerRowWithinCol == 0) {
                glyphsPerRowWithinCol = 1;
            }
            counter += glyphsPerRowWithinCol;
            gridEndingGlyphCountsX[x] = counter;
        }
        numGaps = attrValsY.size() - 1;
        int[] gridEndingGlyphCountsY = new int[attrValsY.size()];
        counter = 0;
        for (int y = 0; y < attrValsY.size(); y++) {
            int pxInRegion = (int) ((y_range - numGaps * glyphHeightPx) * regionPercsY[y]);
            int glyphsPerColWithinRow = pxInRegion / glyphHeightPx;
            if (glyphsPerColWithinRow == 0) {
                glyphsPerColWithinRow = 1;
            }
            counter += glyphsPerColWithinRow;
            gridEndingGlyphCountsY[y] = counter;
        }

        // keep track of how far along we are for each of the meta-grids
        int[][] documentGlyphGridCounter = new int[attrValsX.size()][attrValsY.size()];
        int metaGridY = 0;
        int metaGridYThresh = gridEndingGlyphCountsY[metaGridY]; // threshold at which we'll jump to next cell
        int metaGridX = 0;
        int metaGridXThresh = gridEndingGlyphCountsX[metaGridX];

        // pre-compute global row, col at which we have a focus (if any)
        int widthFocusCol = -1;
        int heightFocusRow = -1;
        for (int currentRow = 0; currentRow < globalRowCount; currentRow++) {

            if (currentRow >= metaGridYThresh) {
                metaGridY++;
                if (metaGridY < gridEndingGlyphCountsY.length) {
                    metaGridYThresh = gridEndingGlyphCountsY[metaGridY];
                } else {
                    break;  // end of the rows; this may happen if we have leftover buffer space
                }
            }

            // reset column
            metaGridX = 0;
            metaGridXThresh = gridEndingGlyphCountsX[metaGridX];

            for (int currentCol = 0; currentCol < globalColCount; currentCol++) {

                // figure out which meta-grid we are in
                if (currentCol >= metaGridXThresh) {
                    metaGridX++;
                    if (metaGridX < gridEndingGlyphCountsX.length) {
                        metaGridXThresh = gridEndingGlyphCountsX[metaGridX];
                    } else {
                        break;  // end of the row; this may happen if we have leftover buffer space
                    }
                }

                List<VisualItem> metaGridNodes = gridCellItems[metaGridX][metaGridY];
                int metaGridNext = documentGlyphGridCounter[metaGridX][metaGridY];

                // see if we have more glyphs in this meta-grid to display
                if (metaGridNodes != null && metaGridNext < metaGridNodes.size()) {

                    // see if this node has any focus
                    VisualItem thisNode = metaGridNodes.get(metaGridNext);

                    if (thisNode.canGetBoolean(HEIGHT_FOCUS) && thisNode.getBoolean(HEIGHT_FOCUS)) {
                        // heightFocus should not have been set, or if so, it should have been set to same value
                        if (heightFocusRow != -1 && heightFocusRow != currentRow) {
                            System.out.println("err: " + this.getClass().getName() + ": conflicting heightFocusRows: " + heightFocusRow + " vs. " + currentRow);
                        }
                        heightFocusRow = currentRow;
                    }

                    if (thisNode.canGetBoolean(WIDTH_FOCUS) && thisNode.getBoolean(WIDTH_FOCUS)) {
                        // heightFocus should not have been set, or if so, it should have been set to same value
                        if (widthFocusCol != -1 && widthFocusCol != currentCol) {
                            System.out.println("err: " + this.getClass().getName() + ": conflicting widthFocusCols: " + widthFocusCol + " vs. " + currentCol);
                        }
                        widthFocusCol = currentCol;
                    }

                    // increment counter
                    documentGlyphGridCounter[metaGridX][metaGridY]++;
                }

            }

        }

        // perform the actual node placement
        // pre-compute widths for when there's a focus
        double focusHeight = y_range * FOCUS_SIZE_PERCENT;
        lastZoomHeight = (int) focusHeight;
        double focusWidth = x_range * FOCUS_SIZE_PERCENT;
        lastZoomWidth = (int) focusWidth;
        // divide by number of global rows/cols
        double collapsedHeight = (y_range - focusHeight) / (globalRowCount + regionPercsY.length - 1);
        double collapsedWidth = (x_range - focusWidth) / (globalColCount + regionPercsX.length - 1);

        // store positions at which regions end, where boundaries should be drawn
        int regionAccumulatorX = (int) x_min;
        int regionAccumulatorY = (int) y_min;
        xCatPositions.add((int) regionAccumulatorX);
        yCatPositions.add((int) regionAccumulatorY);

        // reset counters
        documentGlyphGridCounter = new int[attrValsX.size()][attrValsY.size()];
        metaGridY = 0;
        metaGridYThresh = gridEndingGlyphCountsY[metaGridY]; // threshold at which we'll jump to next cell
        metaGridX = 0;
        metaGridXThresh = gridEndingGlyphCountsX[metaGridX];

        // track our current x, y positions
        double xOffset = x_min;
        double yOffset = y_min;


        for (int currentRow = 0; currentRow < globalRowCount; currentRow++) {

            // figure out which meta-grid we are in
            if (currentRow >= metaGridYThresh) {
                metaGridY++;
                // add a buffer, to ensure gap between groups
                yOffset += glyphHeightPx;
                regionAccumulatorY += glyphHeightPx;

                // remember where gap was located (so long as we're not off the grid)
                if (metaGridY <= gridEndingGlyphCountsY.length) {
                    yCatPositions.add((int) yOffset);
                    yCatRegionSizes.add(regionAccumulatorY);
                    regionAccumulatorY = 0;
                }

                if (metaGridY < gridEndingGlyphCountsY.length) {
                    metaGridYThresh = gridEndingGlyphCountsY[metaGridY];
                } else {
                    break;  // end of the rows; this may happen if we have leftover buffer space
                }
            }

            // reset column
            metaGridX = 0;
            metaGridXThresh = gridEndingGlyphCountsX[metaGridX];
            regionAccumulatorX = 0;

            // determine what height to use
            double height = glyphHeightPx;
            if (currentRow == heightFocusRow) {
                height = focusHeight;
            } else if (heightFocusRow != -1) {
                height = collapsedHeight;
            }

            for (int currentCol = 0; currentCol < globalColCount; currentCol++) {

                // figure out which meta-grid we are in
                if (currentCol >= metaGridXThresh) {
                    metaGridX++;
                    // add a buffer, to ensure gap between groups
                    xOffset += glyphWidthPx;
                    regionAccumulatorX += glyphWidthPx;

                    // if we're in first row (no need to be redundant), remember where gap was located (so long as we're not off the grid)
                    if (metaGridX <= gridEndingGlyphCountsX.length && currentRow == 0) {
                        xCatPositions.add((int) xOffset);
                        xCatRegionSizes.add(regionAccumulatorX);
                        regionAccumulatorX = 0;
                    }

                    if (metaGridX < gridEndingGlyphCountsX.length) {
                        metaGridXThresh = gridEndingGlyphCountsX[metaGridX];
                    } else {
                        break;  // end of the row; this may happen if we have leftover buffer space
                    }
                }

                // determine what width to use
                double width = glyphWidthPx;
                if (currentCol == widthFocusCol) {
                    width = focusWidth;
                } else if (widthFocusCol != -1) {
                    width = collapsedWidth;
                }

                List<VisualItem> metaGridNodes = gridCellItems[metaGridX][metaGridY];
                int metaGridNext = documentGlyphGridCounter[metaGridX][metaGridY];

                // see if we have more glyphs in this meta-grid to display
                if (metaGridNodes != null && metaGridNext < metaGridNodes.size()) {
                    // if so, add; otherwise, skip
                    VisualItem docGridNode = metaGridNodes.get(metaGridNext);
                    // store its global position info
                    docGridNode.setInt(GLOBAL_COL, currentCol);
                    docGridNode.setInt(GLOBAL_ROW, currentRow);
                    // set the Start* fields for animation
                    double oldX = docGridNode.getDouble(VisualItem.X);
                    double oldY = docGridNode.getDouble(VisualItem.Y);
                    double oldW = docGridNode.getDouble(WIDTH);
                    double oldH = docGridNode.getDouble(HEIGHT);
                    docGridNode.setStartX(oldX);
                    docGridNode.setStartY(oldY);
                    docGridNode.setDouble(WIDTH_START, oldW);
                    docGridNode.setDouble(HEIGHT_START, oldH);

                    docGridNode.set(VisualItem.X, xOffset);
                    docGridNode.setEndX(xOffset);
                    docGridNode.set(VisualItem.Y, yOffset);
                    docGridNode.setEndY(yOffset);
                    // i can find no direct relationship between the "size" value and the actual size in pixels of what's drawn to the screen; this is very important for my application
                    docGridNode.setSize(width * height);  // size no longer being used for rendering (because it seems bizarrly unrelated to pixel size?), but we need it to be set in order for listeners to work properly
                    docGridNode.setEndSize(width * height);
                    
                    docGridNode.setDouble(WIDTH, width);
                    docGridNode.setDouble(WIDTH_END, width);
                    docGridNode.setDouble(HEIGHT, height);
                    docGridNode.setDouble(HEIGHT_END, height);
                    docGridNode.setBounds(xOffset, yOffset, width, height);
                    docGridNode.setShape(Constants.SHAPE_RECTANGLE);

                    // increment counter
                    documentGlyphGridCounter[metaGridX][metaGridY]++;
                }

                // update offsets regardless
                xOffset += width;
                regionAccumulatorX += width;

            }

            // advance to next row
            xOffset = x_min;
            yOffset += height;
            regionAccumulatorY += height;

        }
        
        // if we didn't advance through all of the categories, ensure that we add appropriate entries to *CatPositions, *RegionSizes so that we don't throw out of bounds exceptions later!
        // for region sizes: sum total in array, subtract from total area, assign remainder to missing region (0 for subsequent regions?)
        // for positions: simply assign max value for each additional needed category?
        
        while (xCatPositions.size() < attrValsX.size()) {
            xCatPositions.add((int)x_max);
        }
        while (yCatPositions.size() < attrValsY.size()) {
            yCatPositions.add((int)y_max);
        }
        while (xCatRegionSizes.size() < attrValsX.size()) {
            xCatRegionSizes.add(0);
        }
        while (yCatRegionSizes.size() < attrValsY.size()) {
            yCatRegionSizes.add(0);
        }

        // if we've got an associated axisLayout, run it now
        if (docGridAxisLayout != null) {
            docGridAxisLayout.setVisualization(m_vis);
            docGridAxisLayout.docGridLayoutUpdated();
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
        return attrValsX;
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
        this.attrValsX = xCats;
        
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
        return attrValsY;
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
        this.attrValsY = yCats;
        
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

    public void setAxisLayout(DocumentGridAxisLayout docGridAxisLayout) {
        this.docGridAxisLayout = docGridAxisLayout;
    }
    
}
