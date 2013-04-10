package emr_vis_nlp.view.nested_grid;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.model.Document;
import emr_vis_nlp.view.MainViewGlassPane;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.text.AbstractDocument;

/**
 * piccolo2d-based grid-within-grid layout for document glyphs.
 *
 * Based on (and code partially adopted from) the Piccolo2d FisheyeCalendar example
 * (http://www.piccolo2d.org/play/java/fisheyecalendar.html)
 *
 * @author alexander.p.conrad@gmail.com
 */
public class NestedFisheyeGrid extends PCanvas {

    static int TEXT_X_OFFSET = 1;
    static int TEXT_Y_OFFSET = 10;
    static int DEFAULT_ANIMATION_MILLIS = 250;
    static float FOCUS_SIZE_PERCENT = 0.35f;
    static Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 10);
    
//    public static final Color[] GLYPH_COLOR_PALETTE = {
//        new Color(176, 95, 220),
//        new Color(91, 229, 108), 
//        new Color(99, 148, 220),
//        new Color(191, 48, 48), 
//        new Color(18, 178, 37), 
//        new Color(160, 82, 45), 
//    };
    public static final Color[] GLYPH_COLOR_PALETTE = {
        new Color(196, 115, 240),  //purple (255=white)
        new Color(111, 249, 128),  //green 
        new Color(119, 168, 240),  //blue
        new Color(211, 88, 88),   //red
        new Color(38, 198, 57),  // lime? green
        new Color(180, 102, 65), // brown
    };
    
    private static MainController controller = MainController.getMainController();
    
    /**
     * all Documents to be displayed in this View
     */
    private List<Document> documents;
    /**
     * indicators for each document, as to whether or not it should be visible
     */
    private List<Boolean> documentEnabledFlags;
    /**
     * names for each attribute in this dataset
     */
    private List<String> attributeNames;
    /**
     * PNode which acts as a holder for all other glyphs
     */
    private GlyphNode glyphNode;
    
    /**
     * name of attribute currently bound to x group
     */
    private String attrNameX;
    /**
     * name of attribute currently bound to y group
     */
    private String attrNameY;
    /**
     * name of attribute currently bound to coloration?
     */
    private String attrNameColor;
    /**
     * List of color values for the x group attribute
     */
    private List<String> attrValsX;
    /**
     * List of color values for the y group attribute
     */
    private List<String> attrValsY;
    /**
     * List of color values for the color group attribute
     */
    private List<String> attrValsColor;
    
    /**
     * width for view, in pixels; initialize to reasonable size (will be overridden on update)
     */
    private int widthPx = 600;
    /**
     * height for view, in pixels
     */
    private int heightPx = 400;
    /**
     * use animation?
     */
    private boolean animate = true;
    /**
     * interactive glasspane for interacting with documents.
     */
    private MainViewGlassPane glassPane;
    // tracks the last assigned max zoom size, so we can know when the zooming operation is complete
    private int lastZoomWidth = -1;
    private int lastZoomHeight = -1;
    
    public NestedFisheyeGrid(List<String> attributeNames, List<Document> documents, List<Boolean> documentEnabledFlags, String xAxisInitName, String yAxisInitName, String colorInitName) {
        this.documents = documents;
        this.documentEnabledFlags = documentEnabledFlags;
        this.attributeNames = attributeNames;
        this.attrNameX = xAxisInitName;
        this.attrNameY = yAxisInitName;
        this.attrNameColor = colorInitName;

        updateAttrVals();
        glyphNode = new GlyphNode();
        getLayer().addChild(glyphNode);
        setZoomEventHandler(null);
        setPanEventHandler(null);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent arg0) {
                refreshVisualization(false);
            }
        });
        
        glassPane = MainViewGlassPane.getGlassPane();
        glassPane.setFisheyeGrid(this);
    }
    
    public void refreshVisualization(boolean doAnimate) {
        glyphNode.setBounds(getX(), getY(), getWidth() - 1, getHeight() - 1);
        glyphNode.layoutChildren(doAnimate);
//        invalidate();
        repaint();
    }

    /* getters and setters */
    
    public String getAttrNameColor() {
        return attrNameColor;
    }

    public void setAttrNameColor(String attrNameColor) {
        this.attrNameColor = attrNameColor;
//        refreshVisualization(animate);
    }

    public String getAttrNameX() {
        return attrNameX;
    }

    public void setAttrNameX(String attrNameX) {
        this.attrNameX = attrNameX;
//        refreshVisualization(animate);
    }

    public String getAttrNameY() {
        return attrNameY;
    }

    public void setAttrNameY(String attrNameY) {
        this.attrNameY = attrNameY;
//        refreshVisualization(animate);
    }

    public List<String> getAttrColorVals() {
        return attrValsColor;
    }

    public void setAttrColorVals(List<String> attrColorVals) {
        this.attrValsColor = attrColorVals;
    }

    public List<String> getAttrXVals() {
        return attrValsX;
    }

    public void setAttrXVals(List<String> attrXVals) {
        this.attrValsX = attrXVals;
    }

    public List<String> getAttrYVals() {
        return attrValsY;
    }

    public void setAttrYVals(List<String> attrYVals) {
        this.attrValsY = attrYVals;
    }
    
    public void updateSize(int widthPx, int heightPx) {
        this.widthPx = widthPx;
        this.heightPx = heightPx;
        refreshVisualization(animate);
    }
    
    public void refreshView() {
        refreshVisualization(animate);
    }
    
    public void updateAttrVals() {
        // TODO : this method is specific to the colonoscopy dataset; uses indicator values of "N/A" / "Fail" / "Pass"
        attrValsX = new ArrayList<>();
        attrValsX.add("N/A");
        attrValsX.add("Fail");
        attrValsX.add("Pass");
        attrValsY = new ArrayList<>();
        attrValsY.add("Pass");
        attrValsY.add("Fail");
        attrValsY.add("N/A");
        attrValsColor = new ArrayList<>();
        attrValsColor.add("Pass");
        attrValsColor.add("Fail");
        attrValsColor.add("N/A");
    }
    
    
    /**
     * PNode for holding all other PNodes within the PCanvas.
     */
    public class GlyphNode extends PNode {
        
        private List<DocGridNode> docGridNodes;
        
        // for tracking boundary locations
        private List<Double> xBoundaryCoords;
        private List<Double> yBoundaryCoords;
        
        public GlyphNode() {
            
            // perform initial layout
            buildInitialNodes();
            layoutChildren(false);

            GlyphNode.this.addInputEventListener(new PBasicInputEventHandler() {

                @Override
                public void mouseReleased(PInputEvent event) {
                    PNode node = event.getPickedNode();
                    if (node.getClass().equals(DocGridNode.class)) {
                        DocGridNode pickedGlyph = (DocGridNode) node;
                        if (pickedGlyph.hasWidthFocus() && pickedGlyph.hasHeightFocus()) {
                            setFocusGlyph(null, true);
                        } else {
                            setFocusGlyph(pickedGlyph, true);
                        }
                    }
                }
            });

        }
        
        private void buildInitialNodes() {
            docGridNodes = new ArrayList<>();
            for (int d = 0; d < documents.size(); d++) {
                Document document = documents.get(d);
                DocGridNode node = new DocGridNode(d, document, -1, -1);
                docGridNodes.add(node);
                addChild(node);
            }
        }

        protected void layoutChildren(boolean animate) {
            
//            removeAllChildren();
            
            // reset all docGridNodes, to ensure that any to-be-hidden glyphs are not shown
            for (int d=0; d<docGridNodes.size(); d++) {
                DocGridNode docGridNode = docGridNodes.get(d);
                docGridNode.setValIndexX(-1);
                docGridNode.setValIndexY(-1);
                docGridNode.setValIndexColor(-1);
                docGridNode.setGlobalX(-1);
                docGridNode.setGlobalY(-1);
            }
            
            // create DocGridNode for each Document, group according to values for attrNameX, attrNameY
            List<DocGridNode>[][] documentGlyphGrid = new List[attrValsX.size()][attrValsY.size()];

            // iterate through all documents, assigning to appropriate grid item
//            for (int d = 0; d < documents.size(); d++) {
//                Document document = documents.get(d);
            for (int d=0; d<docGridNodes.size(); d++) {
                DocGridNode docGridNode = docGridNodes.get(d);
                Document document = docGridNode.getDocument();
                String xVal = "";
                int xIndex = -1;
                if (document.getAttributes().containsKey(attrNameX)) {
                    xVal = document.getAttributes().get(attrNameX);
                    // search through values to find index of value (assumes a small number of values)
                    for (int v = 0; v < attrValsX.size(); v++) {
                        if (xVal.equalsIgnoreCase(attrValsX.get(v))) {
                            xIndex = v;
                        }
                    }
                } else {
                    // if no attribute on document, get prediction
                    // TODO
                }
                String yVal = "";
                int yIndex = -1;
                if (document.getAttributes().containsKey(attrNameY)) {
                    yVal = document.getAttributes().get(attrNameY);
                    // search through values to find index of value (assumes a small number of values)
                    for (int v = 0; v < attrValsY.size(); v++) {
                        if (yVal.equalsIgnoreCase(attrValsY.get(v))) {
                            yIndex = v;
                        }
                    }
                } else {
                    // if no attribute on document, get prediction
                    // TODO
                }
                String colorVal = "";
                int colorIndex = -1;
                if (document.getAttributes().containsKey(attrNameColor)) {
                    colorVal = document.getAttributes().get(attrNameColor);
                    // search through values to find index of value (assumes a small number of values)
                    for (int v = 0; v < attrValsColor.size(); v++) {
                        if (colorVal.equalsIgnoreCase(attrValsColor.get(v))) {
                            colorIndex = v;
                        }
                    }
                } else {
                    // if no attribute on document, get prediction
                    // TODO
                }

                // TODO : how to handle missing values, when neither a value nor prediction? define separate category?
                // for now, simply leave out
                if (xIndex != -1 && yIndex != -1) {
//                    DocGridNode node = new DocGridNode(d, document, xIndex, yIndex);
                    docGridNode.setValIndexX(xIndex);
                    docGridNode.setValIndexY(yIndex);
                    if (documentGlyphGrid[xIndex][yIndex] == null) {
                        documentGlyphGrid[xIndex][yIndex] = new ArrayList<>();
                    }
                    documentGlyphGrid[xIndex][yIndex].add(docGridNode);
//                    addChild(node);  // add child nodes to visualization earlier, at initialization only
                }
                if (colorIndex != -1) {
                    docGridNode.setValIndexColor(colorIndex);
                } else {
                    docGridNode.setValIndexColor(3);
                }

            }

            // compute % of space to give to each row / column of subgrids, based on # of documents in that row/columndouble[] regionPercsX = new double[attrValsX.size()];
            double[] regionCountsX = new double[attrValsX.size()];
            double[] regionCountsY = new double[attrValsY.size()];
            double[] regionPercsX = new double[attrValsX.size()];
            double[] regionPercsY = new double[attrValsY.size()];
            
            double regionSmoothingPerc = 0.05;  // give each region's axis 10% of the space by default
            
            for (int x = 0; x < attrValsX.size(); x++) {
                int valCounter = 0;
                for (int y = 0; y < attrValsY.size(); y++) {
                    if (documentGlyphGrid[x][y] != null) {
                        valCounter += documentGlyphGrid[x][y].size();
                    }
                }
                regionCountsX[x] = valCounter;
                // NOTE: regionPercs considers all documents, not just enabled; is this what we want?
                regionPercsX[x] = ((double) valCounter / (double) documents.size()) * (1. - (regionSmoothingPerc*attrValsX.size())) + regionSmoothingPerc;
            }
            
            for (int y = 0; y < attrValsY.size(); y++) {
                int valCounter = 0;
                for (int x = 0; x < attrValsX.size(); x++) {
                    if (documentGlyphGrid[x][y] != null) {
                        valCounter += documentGlyphGrid[x][y].size();
                    }
                }
                regionCountsY[y] = valCounter;
                // NOTE: regionPercs considers all documents, not just enabled; is this what we want?
                regionPercsY[y] = ((double) valCounter / (double) documents.size()) * (1. - (regionSmoothingPerc*attrValsY.size())) + regionSmoothingPerc;
            }

            // TODO: compute height, width for each GridNode such that size is used as efficiently as possible!
            //  idea: could we frame this as a linear programming optimization problem?
            
            // problem: when dividing up space by % glyphs, some regions will always remain too small to accomodate glyphs
            // idea: initialize each region with a fixed % of space, distribute remainder according to % glyphs
            
            // find total area, initialize glyph size by dividing up area evenly
            double totalArea = widthPx * heightPx;
            double glyphArea = totalArea / (double)docGridNodes.size();
            int glyphWidthPx = (int)Math.sqrt(glyphArea);
            int glyphHeightPx = (int)Math.sqrt(glyphArea);
            
            // iteratively shirink glyph area until glyphs fit in all nested grids
            int glyphWidthShrinkIncrement = 1;
            int glyphHeightShrinkIncrement = 1;
            boolean doAllGlyphsFit;
            int numGaps = attrValsX.size() - 1;
            if (glyphWidthPx != 0 && glyphHeightPx != 0) {
            do {
                doAllGlyphsFit = true;
                for (int x = 0; x < attrValsX.size(); x++) {
                    int pxInColRegion = (int) ((widthPx - numGaps * glyphWidthPx) * regionPercsX[x]);
                    int glyphsPerRowWithinColRegion = pxInColRegion / glyphWidthPx;

                    for (int y = 0; y < attrValsY.size(); y++) {
                        int pxInRowRegion = (int) ((heightPx - numGaps * glyphHeightPx) * regionPercsY[y]);
                        int glyphsPerColWithinRow = pxInRowRegion / glyphHeightPx;
                        int numRowsInNestedGrid = glyphsPerColWithinRow;

                        // see if this cell can fit all its glyphs; if not, shrink glyphs
                        int maxNumGlyphsInCell = glyphsPerRowWithinColRegion * numRowsInNestedGrid;
                        if (documentGlyphGrid[x][y] != null) {
                            int numGlyphsInCell = documentGlyphGrid[x][y].size();
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
                // this display has zero size; use default widths and heights to avoid exceptions (since nothing will be displayed, view isn't active)
                glyphWidthPx = 60;
                glyphHeightPx = 60;
            }
            
            
            // for now, simply use fixed-size glyphs, set number per row based on 
//            int glyphWidthPx = 60;
//            int glyphHeightPx = 60;
            // for each column, determine # of items allowed
            int[] glyphsPerInnerRowInCol = new int[attrValsY.size()];
            for (int y=0; y<attrValsY.size(); y++) {
                int pxInRegion = (int)(widthPx * regionPercsY[y]);
                glyphsPerInnerRowInCol[y] = pxInRegion / glyphWidthPx;
            }
            
            
            // TODO : sort by certainties: 
            //  for each nested grid, sort first by the y-attr certainty (for predictions; manual annotations automatically pushed to front of list)
            //  to build an inner row of n items, take next n nodes from this sorted list
            //   next, to lay out row itself, sort the list of n nodes according to x-attr certainty
            // (but for immediate testing, leave sorting out until later)
            
            
            // compute position information for each DocGridNode (null node idea should not be necessary)
            
            // compute max ## of rows, cols
            int globalRowCount = (heightPx / glyphHeightPx);
            int globalColCount = (widthPx / glyphWidthPx);
            
            // pre-compute item counts at which we should switch to next cell
            // make sure to account for blank glyph space between cats!
//            int numGaps = attrValsX.size()-1;
            int[] gridEndingGlyphCountsX = new int[attrValsX.size()];
            int counter = 0;
            for (int x=0; x<attrValsX.size(); x++) {
                int pxInRegion = (int)((widthPx-numGaps*glyphWidthPx) * regionPercsX[x]);
                int glyphsPerRowWithinCol = pxInRegion / glyphWidthPx;
                // reduce # of glyphs by 1, to ensure a buffer?
//                if (glyphsPerRowWithinCol > 1) {
//                    glyphsPerRowWithinCol--;
//                } 
                if (glyphsPerRowWithinCol == 0) {
                    glyphsPerRowWithinCol = 1;
                }
                counter += glyphsPerRowWithinCol;
                gridEndingGlyphCountsX[x] = counter;
            }
            numGaps = attrValsY.size()-1;
            int[] gridEndingGlyphCountsY = new int[attrValsY.size()];
            counter = 0;
            for (int y=0; y<attrValsY.size(); y++) {
                int pxInRegion = (int)((heightPx-numGaps*glyphHeightPx) * regionPercsY[y]);
                int glyphsPerColWithinRow = pxInRegion / glyphHeightPx;
                // reduce # of glyphs by 1, to ensure a buffer?
//                if (glyphsPerColWithinRow > 1) {
//                    glyphsPerColWithinRow--;
//                } 
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
                    
                    List<DocGridNode> metaGridNodes = documentGlyphGrid[metaGridX][metaGridY];
                    int metaGridNext = documentGlyphGridCounter[metaGridX][metaGridY];
                    
                    // see if we have more glyphs in this meta-grid to display
                    if (metaGridNodes != null && metaGridNext < metaGridNodes.size()) {
                        
                        // see if this node has any focus
                        DocGridNode thisNode = metaGridNodes.get(metaGridNext);
                        
                        if (thisNode.hasHeightFocus()) {
                            // heightFocus should not have been set, or if so, it should have been set to same value
                            if (heightFocusRow != -1 && heightFocusRow != currentRow) {
                                System.out.println("err: "+this.getClass().getName()+": conflicting heightFocusRows: "+heightFocusRow+" vs. "+currentRow);
                            }
                            heightFocusRow = currentRow;
                        }
                        
                        if (thisNode.hasWidthFocus()) {
                            // heightFocus should not have been set, or if so, it should have been set to same value
                            if (widthFocusCol != -1 && widthFocusCol != currentCol) {
                                System.out.println("err: "+this.getClass().getName()+": conflicting widthFocusCols: "+widthFocusCol+" vs. "+currentCol);
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
            double focusHeight = heightPx * FOCUS_SIZE_PERCENT;
            lastZoomHeight = (int)focusHeight;
            double focusWidth = widthPx * FOCUS_SIZE_PERCENT;
            lastZoomWidth = (int)focusWidth;
            // divide by number of global rows/cols
            double collapsedHeight = (heightPx - focusHeight) / (globalRowCount+regionPercsY.length-1);
//            double collapsedWidth = (widthPx - focusWidth) / (documents.size());
            double collapsedWidth = (widthPx - focusWidth) / (globalColCount+regionPercsX.length-1);

            // reset counters
            documentGlyphGridCounter = new int[attrValsX.size()][attrValsY.size()];
            metaGridY = 0;
            metaGridYThresh = gridEndingGlyphCountsY[metaGridY]; // threshold at which we'll jump to next cell
            metaGridX = 0;
            metaGridXThresh = gridEndingGlyphCountsX[metaGridX];
            
            // trak coordinates where we should draw boundaries
            xBoundaryCoords = new ArrayList<>();
            yBoundaryCoords = new ArrayList<>();
            // track our current x, y positions
            double xOffset = 0;
            double yOffset = 0;
            for (int currentRow = 0; currentRow < globalRowCount; currentRow++) {

                // figure out which meta-grid we are in
                if (currentRow >= metaGridYThresh) {
                    metaGridY++;
                    // add a buffer, to ensure gap between groups
//                    currentRow++;
                    yOffset += glyphHeightPx;
                    yBoundaryCoords.add(yOffset-(glyphHeightPx/2.));
                    if (metaGridY < gridEndingGlyphCountsY.length) {
                        metaGridYThresh = gridEndingGlyphCountsY[metaGridY];
                    } else {
                        break;  // end of the rows; this may happen if we have leftover buffer space
                    }
                }
                
                // reset column
                metaGridX = 0;
                metaGridXThresh = gridEndingGlyphCountsX[metaGridX];
                
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
//                        currentCol++;
                        xOffset += glyphWidthPx;
                        // take note of position, for boundary-drawing later
                        xBoundaryCoords.add(xOffset-(glyphWidthPx/2.));
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

                    List<DocGridNode> metaGridNodes = documentGlyphGrid[metaGridX][metaGridY];
                    int metaGridNext = documentGlyphGridCounter[metaGridX][metaGridY];
                    
                    // see if we have more glyphs in this meta-grid to display
                    if (metaGridNodes != null && metaGridNext < metaGridNodes.size()) {
                        // if so, add; otherwise, skip
                        DocGridNode docGridNode = metaGridNodes.get(metaGridNext);
                        // store its global position info
                        docGridNode.setGlobalX(currentCol);
                        docGridNode.setGlobalY(currentRow);
                        // add node to the visualization
                        // do this earlier, at initialization; don't add multiple times
//                        addChild(docGridNode);
                        
                        if (animate) {
                            docGridNode.animateToBounds(xOffset, yOffset, width, height, DEFAULT_ANIMATION_MILLIS).setStepRate(0);
                        } else {
                            docGridNode.setBounds(xOffset, yOffset, width, height);
                        }
                        
                        // increment counter
                        documentGlyphGridCounter[metaGridX][metaGridY]++;
                    }
                    
                    // update offsets regardless
                    xOffset += width;
                    
                }
                
                // advance to next row
                xOffset = 0;
                yOffset += height;
                
            }
            

        }

        public void setFocusGlyph(GridNode focusGlyph, boolean animate) {
            for (int i = 0; i < getChildrenCount(); i++) {
                GridNode each = (GridNode) getChild(i);
                each.setHasWidthFocus(false);
                each.setHasHeightFocus(false);
            }

            if (focusGlyph == null) {
                // if no focus is set
            } else {
                // if a focus is set
                focusGlyph.setHasWidthFocus(true);
                focusGlyph.setHasHeightFocus(true);
                
                for (int i = 0; i < getChildrenCount(); i++) {
                    GridNode each = (GridNode) getChild(i);
                    if (each.getGlobalY() == focusGlyph.getGlobalY()) {
                        each.setHasHeightFocus(true);
                    }
                    if (each.getGlobalX() == focusGlyph.getGlobalX()) {
                        each.setHasWidthFocus(true);
                    }
                }
                
            }

            layoutChildren(animate);
        }

        protected void removeNullNodes() {
            
        }
        
        
        // paint's only responsibility (right now) is to draw the boundaries
        @Override
        protected void paint(PPaintContext paintContext) {
//            super.paint(paintContext);
            Graphics2D g2 = paintContext.getGraphics();
            setPaint(Color.BLACK);
//            g2.setPaint(getPaint());
            PBounds bounds = getBoundsReference();
            Point2D origin = bounds.getOrigin();
            Dimension2D size = bounds.getSize();
            int xPos = (int)origin.getX();
            int yPos = (int)origin.getY();
            int width = (int)size.getWidth();
            int height = (int)size.getHeight();
//            g2.fill(bounds);
            for (int x = 0; x < xBoundaryCoords.size() - 1; x++) {
                int xCoord = xPos + xBoundaryCoords.get(x).intValue();
                g2.drawLine(xCoord, yPos, xCoord, height);
            }
            for (int y = 0; y < yBoundaryCoords.size() - 1; y++) {
                int yCoord = yPos + yBoundaryCoords.get(y).intValue();
                g2.drawLine(yPos, yCoord, width, yCoord);
            }
        }
        
    }


    public class GridNode extends PNode {
        
        public int BUFFER_WIDTH_PX = 5;
        public int BUFFER_HEIGHT_PX = 5;
        
        protected boolean hasWidthFocus;
        protected boolean hasHeightFocus;

        /**
         * global coordinate (in glyph-units) of this glyph's x position
         */
        private int globalX;
        /**
         * global coordinate (in glyph-units) of this glyph's y position
         */
        private int globalY;
        
        public boolean hasHeightFocus() {
            return hasHeightFocus;
        }

        public void setHasHeightFocus(boolean hasHeightFocus) {
            this.hasHeightFocus = hasHeightFocus;
        }

        public boolean hasWidthFocus() {
            return hasWidthFocus;
        }

        public void setHasWidthFocus(boolean hasWidthFocus) {
            this.hasWidthFocus = hasWidthFocus;
        }
        
        public int getGlobalX() {
            return globalX;
        }

        public void setGlobalX(int globalX) {
            this.globalX = globalX;
        }

        public int getGlobalY() {
            return globalY;
        }

        public void setGlobalY(int globalY) {
            this.globalY = globalY;
        }
        
    }

    /**
     * PNode for representing document glyphs within the nested grid view.
     */
    public class DocGridNode extends GridNode {
        
        
        /**
         * index of this document into backing documentsList
         */
        private int index;
        /**
         * backing Document object
         */
        private Document document;
        /**
         * index as to appropriate subgrid for this document for x group
         */
        private int valIndexX;
        /**
         * index as to appropriate subgrid for this document for y group
         */
        private int valIndexY;
        /**
         * index as to appropriate color for this document
         */
        private int valIndexColor;
        /**
         * 
         */
        private int bufferPx = 2;
        /**
         * 
         */
        private boolean hasGlasspaneBeenTriggered = false;
        
        public DocGridNode(int index, Document document, int valIndexX, int valIndexY) {
            this.index = index;
            this.document = document;
            this.valIndexX = valIndexX;
            this.valIndexY = valIndexY;
            setPaint(Color.GREEN);
        }

        public Document getDocument() {
            return document;
        }

        public int getIndex() {
            return index;
        }

        public int getValIndexX() {
            return valIndexX;
        }

        public void setValIndexX(int valIndexX) {
            this.valIndexX = valIndexX;
        }

        public int getValIndexY() {
            return valIndexY;
        }

        public void setValIndexY(int valIndexY) {
            this.valIndexY = valIndexY;
        }

        public int getValIndexColor() {
            return valIndexColor;
        }

        public void setValIndexColor(int valIndexColor) {
            this.valIndexColor = valIndexColor;
        }
        
        /**
         * Draw glyph along with currently-relevant text.
         * 
         * @param paintContext 
         */
        @Override
        protected void paint(PPaintContext paintContext) {
            if (valIndexX != -1 && valIndexY != -1 && getGlobalX() != -1 && getGlobalY() != -1) {
                Graphics2D g2 = paintContext.getGraphics();
                g2.setPaint(GLYPH_COLOR_PALETTE[valIndexColor]);
                PBounds bounds = getBoundsReference();
                Point2D origin = bounds.getOrigin();
                Dimension2D size = bounds.getSize();
                double xPos = origin.getX()+bufferPx;
                double yPos = origin.getY()+bufferPx;
                double width = size.getWidth()-(2*bufferPx);
                double height = size.getHeight()-(2*bufferPx);
                Rectangle2D adjustedBounds = new Rectangle((int)xPos, (int)yPos, (int)width, (int)height);
                g2.fill(adjustedBounds);
//                g2.fill(bounds);
//                g2.draw(bounds);
                g2.setFont(DEFAULT_FONT);
                g2.setColor(Color.black);
                

//                float y = (float) getY() + TEXT_Y_OFFSET;
//                float x = (float) getX() + TEXT_X_OFFSET;
//                float y = (float) adjustedBounds.getMinY() + TEXT_Y_OFFSET;
//                float x = (float) adjustedBounds.getMinX() + TEXT_X_OFFSET;
                float y = (float) getY()+bufferPx + TEXT_Y_OFFSET;
                float x = (float) getX()+bufferPx + TEXT_X_OFFSET;
//                g2.drawString(document.getName(), x, y);
                if (hasWidthFocus && hasHeightFocus) {
                    // draw full text of document
                    // TODO : properly wrap text
//                paintContext.pushClip(getBoundsReference());
//                for (int i = 0; i < lines.size(); i++) {
//                    y += 10;
//                    g2.drawString((String) lines.get(i), (float) getX() + CalendarNode.TEXT_X_OFFSET, y);
//                }
//                paintContext.popClip(getBoundsReference());
                    paintContext.pushClip(getBoundsReference());
//                    g2.drawString(document.getText(), x, y);
                    drawStringMultiline(g2, DEFAULT_FONT, document.getName()+"   \n"+document.getText(), xPos, yPos, width, height);
                    paintContext.popClip(getBoundsReference());
                    // if we're done zooming in, load the glasspane
                    if (!hasGlasspaneBeenTriggered) {
                        hasGlasspaneBeenTriggered = true;
                        AbstractDocument doc = glassPane.getAbstDoc();
                        controller.writeDocTextWithHighlights(doc, index, attrNameColor);
                        glassPane.setBackgroundColor(GLYPH_COLOR_PALETTE[valIndexColor]);
                        glassPane.displaySizedPaneTimer((int)xPos, (int)yPos, (int)width, (int)height, DEFAULT_ANIMATION_MILLIS);
                    } else {
                        // just update glasspane's position
                        glassPane.updateSizedPanePosition((int)xPos, (int)yPos, (int)width, (int)height);
                    }
                } else {
                    hasGlasspaneBeenTriggered = false;
                    // just draw the summary instead
                    paintContext.pushClip(getBoundsReference());
                    drawStringMultiline(g2, DEFAULT_FONT, controller.getDocumentSummary(index, attrNameColor), xPos, yPos, width, height);
                    paintContext.popClip(getBoundsReference());
                }
            }
        }
        
        public void drawStringMultiline(Graphics2D g, Font f, String s, double xPos, double yPos, double width, double height) {
            FontMetrics fm = g.getFontMetrics(f);
            int w = fm.stringWidth(s);
            int h = fm.getAscent();
            //g.setColor(Color.LIGHT_GRAY);
            g.setColor(Color.BLACK);
            g.setFont(f);

            Scanner lineSplitter = new Scanner(s);
            // draw as much as can fit in each item
            // read all content from scanner, storing in string lists (where each string == 1 line), each string should be as long as possible without overflowing the space
            int maxRows = (int) height / h;
            List<String> textRows = new ArrayList<>();
            while (lineSplitter.hasNextLine() && textRows.size() < maxRows) {
                String line = lineSplitter.nextLine();
                // if line is blank, insert to maintain paragraph seps
                if (line.trim().equals("")) {
                    textRows.add("");
                }
                // else, pass to inner loop
                StringBuilder currentBuilder = new StringBuilder();
                int currentStrWidth = 0;
                Scanner splitter = new Scanner(line);
                while (splitter.hasNext() && textRows.size() < maxRows) {
                    String token = splitter.next() + " ";
                    // TODO incorporate weight detection, formatting for token?
                    currentStrWidth += fm.stringWidth(token);
                    if (currentStrWidth >= width) {
                        // if string length >= glyph width, build row
                        textRows.add(currentBuilder.toString());
                        currentBuilder = new StringBuilder();
                        currentBuilder.append(token);
                        currentStrWidth = fm.stringWidth(token);
                    } else {
                        // if not yet at end of row, append to builder
                        currentBuilder.append(token);
                    }

                }

                // if we've still space and still have things to write, add them here
                if (textRows.size() < maxRows) {
                    textRows.add(currentBuilder.toString());
                    currentBuilder = new StringBuilder();
                    currentStrWidth = 0;
                }

            }

            // write each line to object
            for (int t = 0; t < textRows.size(); t++) {
                String line = textRows.get(t);
                if (fm.stringWidth(line) <= width) {
                    // ensure that string doesn't overflow the box
                    g.drawString(line, (float) (xPos), (float) (yPos) + h * (t + 1));
                }
            }

        }
    }

//    /**
//     * PNode serving as a gap-filler between different groups of DocGridNodes,
//     * or for filling in non-full grid regions.
//     */
//    public static class NullGridNode extends GridNode {
//
//        @Override
//        protected void paint(PPaintContext paintContext) {
//        }
//    }
}
