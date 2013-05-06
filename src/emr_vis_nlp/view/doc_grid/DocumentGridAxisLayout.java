
package emr_vis_nlp.view.doc_grid;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import prefuse.Visualization;
import prefuse.action.layout.Layout;
import prefuse.data.Schema;
import prefuse.data.tuple.TupleSet;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

/**
 * Performs layout of axis labels for a DocumentGridLayout.
 * 
 * Based heavily on prefuse.action.layout.AxisLabelLayout.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocumentGridAxisLayout extends Layout {
    
    public static final String IS_X = "is_x";
    public static final String CAT_INDEX = "cat_index";
    public static final String IS_LABEL = "is_label";
    public static final String IS_MAIN_LABEL = "is_main_label";
    public static final String CAT_LABEL = "cat_label";
    public static final String MID_POINT = "mid_point";
    
    private DocumentGridLayoutNested docGridLayout;  // pointer to DocumentGridLayout for which this instance is drawing the axes
    
    private double x_min;
    private double x_max;
    private double y_min;
    private double y_max;
    
    private boolean m_asc = true;  // are values in ascending order?
    private boolean rebuildTable = false;  // flag variable indicating whether visualItem table should be rebuilt from scratch
    
    // data from docGridLayout
    private String xName;
    private String yName;
    private List<String> xCats;
    private List<String> yCats;
    private List<Integer> xCatRegionSizes;
    private List<Integer> yCatRegionSizes;
    private List<Integer> xCatPositions;
    private List<Integer> yCatPositions;
    
    public DocumentGridAxisLayout(String group, DocumentGridLayoutNested docGridLayout) {
        super(group);
        this.docGridLayout = docGridLayout;
        xName = docGridLayout.getXAttr();
        yName = docGridLayout.getYAttr();
        xCats = docGridLayout.getXCats();
        yCats = docGridLayout.getYCats();
        xCatRegionSizes = docGridLayout.getXCatRegionSizes();
        yCatRegionSizes = docGridLayout.getYCatRegionSizes();
        xCatPositions = docGridLayout.getXCatPositions();
        yCatPositions = docGridLayout.getYCatPositions();
    }
    
    /**
     * Tells this layout to re-acquire data from the DocumentGridLayout; generally, this method should be called after an axis change.
     */
    public void docGridLayoutUpdated() {
        xName = docGridLayout.getXAttr();
        yName = docGridLayout.getYAttr();
        xCats = docGridLayout.getXCats();
        yCats = docGridLayout.getYCats();
        xCatRegionSizes = docGridLayout.getXCatRegionSizes();
        yCatRegionSizes = docGridLayout.getYCatRegionSizes();
        xCatPositions = docGridLayout.getXCatPositions();
        yCatPositions = docGridLayout.getYCatPositions();
        run();
    }
    
    /**
     * Indicates if the axis values should be presented in ascending order
     * along the axis.
     * @return true if data values increase as pixel coordinates increase,
     * false if data values decrease as pixel coordinates increase.
     */
    public boolean isAscending() {
        return m_asc;
    }
    
    /**
     * Sets if the axis values should be presented in ascending order
     * along the axis.
     * @param asc true if data values should increase as pixel coordinates
     * increase, false if data values should decrease as pixel coordinates
     * increase.
     */
    public void setAscending(boolean asc) {
        m_asc = asc;
    }
    
    private void setMinMax(List<Integer> xCatPositions, List<Integer> xCatRegionSizes, List<Integer> yCatPositions, List<Integer> yCatRegionSizes) {
        
        if (xCatPositions != null && xCatPositions.size() > 0) {
            x_min = xCatPositions.get(0);   
            x_max = x_min;
            for (Integer regionSize : xCatRegionSizes) {
                x_max += regionSize;
            }
        } else {
            x_min = 0;
            x_max = 0;
        }
        if (yCatPositions != null && yCatPositions.size() > 0) {
            y_min = yCatPositions.get(0);
            y_max = y_min;
            for (Integer regionSize : yCatRegionSizes) {
                y_max += regionSize;
            }
        } else {
            y_min = 0;
            y_max = 0;
        }
    }
    
    /**
     * 
     */
    @Override
    public void run(double frac) {
        
        VisualTable labels = getTable();
        
        // get relevant information concerning layout from docGridLayout
        // - ordered list of x, y labels
        // - heights of each region (based on number of documents contained therein, and whether a selected document is contained)
        
        // build grid and labels
        setMinMax(xCatPositions, xCatRegionSizes, yCatPositions, yCatRegionSizes);
        
        // mark previously visible labels
        // keep track of which labels we've rendered so far (so we don't create duplicates)
        Map<String, Boolean> axisMap = new HashMap<>();
        Map<String, Boolean> axisLabelMap = new HashMap<>();
        Iterator iter = labels.tuples();
        while (iter.hasNext()) {
            VisualItem item = (VisualItem) iter.next();
            reset(item);
            // get group indices
            int catIndex = (int)item.get(CAT_INDEX);
            boolean isLabel = (boolean)item.get(IS_LABEL);
            boolean isX = (boolean)item.get(IS_X);
            int value;
            int midPoint;
            String catName = "";
            if (isX) {
                if (catIndex != -1 && catIndex < xCats.size()) {
                    catName = "x_" + xCats.get(catIndex);
//                    catName = "" + xCats.get(catIndex);
                    value = xCatPositions.get(catIndex);
                    midPoint = value + (xCatRegionSizes.get(catIndex) / 2);
                } else {
                    catName = "x_last";
//                    catName = "";
                    value = xCatPositions.get(xCatPositions.size() - 1) + xCatRegionSizes.get(xCatRegionSizes.size() - 1);
//                    catIndex = xCats.size();
                    midPoint = value;
                }
            } else {
                if (catIndex != -1 && catIndex < yCats.size()) {
                    catName = "y_" + yCats.get(catIndex);
//                    catName = "" + yCats.get(catIndex);
                    value = yCatPositions.get(catIndex);
                    midPoint = value + (yCatRegionSizes.get(catIndex) / 2);
                } else {
                    catName = "y_last";
//                    catName = "";
                    value = yCatPositions.get(yCatPositions.size() - 1) + yCatRegionSizes.get(yCatRegionSizes.size() - 1);
//                    catIndex = yCats.size();
                    midPoint = value;
                }
            }
            item.set(VisualItem.LABEL, catName);
//            item.setDouble(VisualItem.VALUE, catIndex);
            item.setDouble(VisualItem.VALUE, value);
            item.setVisible(true);
            item.setEndVisible(true);
            if (!isLabel) {
//                set(item, value, b, isX);
                set(item, value, isX, midPoint);
                axisMap.put(catName, true);
            } else {
                // it's a label, so position term only, don't draw line?
//                set(item, value, b, isX);  // TODO replace with text-specific "set"
                set(item, value, isX, midPoint);
            }
        }
        
        // build labels we haven't seen yet
        
        boolean isX = true;
        VisualItem item;
        String label;
        int indexVal;
        int midPoint;
//        assert xCats.size() == xCatPositions.size();
        for (int i=0; i<xCats.size(); i++) {
            String xCat = "x_"+xCats.get(i);
//            String xCat = ""+xCats.get(i);
            
            if (!axisMap.containsKey(xCat)) {
                axisMap.put(xCat, true);
                
                indexVal = xCatPositions.get(i);
                midPoint = indexVal + (xCatRegionSizes.get(i) / 2);
                item = labels.addItem();
                label = xCat;
                item.set(CAT_INDEX, i);
                item.set(IS_X, isX);
                item.set(IS_LABEL, false);
                item.set(CAT_LABEL, label);

                item.set(VisualItem.LABEL, label);
//                item.setDouble(VisualItem.VALUE, i);
                item.setDouble(VisualItem.VALUE, indexVal);
                item.setVisible(true);
//                item.setStartVisible(true);
                item.setEndVisible(true);

                set(item, indexVal, isX, midPoint);
            }
            
        }
        // add item for last bounds
        item = labels.addItem();
        label = "x_last";
//        label = "";
        if (!axisMap.containsKey(label)) {
            axisMap.put(label, true);
            indexVal = xCatPositions.get(xCatPositions.size() - 1) + xCatRegionSizes.get(xCatRegionSizes.size() - 1);
            item.set(CAT_INDEX, -1);
            item.set(IS_X, isX);
            item.set(IS_LABEL, false);
            item.set(CAT_LABEL, label);
            item.set(VisualItem.LABEL, "x_last");
//            item.setDouble(VisualItem.VALUE, xCats.size());
            item.setDouble(VisualItem.VALUE, indexVal);
            item.setVisible(true);
//            item.setStartVisible(true);
            item.setEndVisible(true);
            set(item, indexVal, isX, indexVal);
        }
        
        isX = false;
        for (int i = 0; i < yCats.size(); i++) {
            String yCat = "y_" + yCats.get(i);
//            String yCat = "" + yCats.get(i);

            if (!axisMap.containsKey(yCat)) {
                axisMap.put(yCat, true);
                indexVal = yCatPositions.get(i);
                midPoint = indexVal + (yCatRegionSizes.get(i) / 2);
                item = labels.addItem();
                label = yCat;
                item.set(CAT_INDEX, i);
                item.set(IS_X, isX);
                item.set(IS_LABEL, false);
                item.set(CAT_LABEL, label);

                item.set(VisualItem.LABEL, label);
//                item.setDouble(VisualItem.VALUE, i);
                item.setDouble(VisualItem.VALUE, indexVal);
                item.setVisible(true);
//                item.setStartVisible(true);
                item.setEndVisible(true);
                set(item, indexVal, isX, midPoint);
            }

        }
        // add item for last bounds
        item = labels.addItem();
        label = "y_last";
//        label = "";
        if (!axisMap.containsKey(label)) {
            axisMap.put(label, true);
            indexVal = yCatPositions.get(yCatPositions.size() - 1) + yCatRegionSizes.get(yCatRegionSizes.size() - 1);
            item.set(CAT_INDEX, -1);
            item.set(IS_X, isX);
            item.set(IS_LABEL, false);
            item.set(CAT_LABEL, label);
            item.set(VisualItem.LABEL, label);
//            item.setDouble(VisualItem.VALUE, yCats.size());
            item.setDouble(VisualItem.VALUE, indexVal);
            item.setVisible(true);
//            item.setStartVisible(true);
            item.setEndVisible(true);
//            set(item, indexVal, b, isX);
            set(item, indexVal, isX, indexVal);
        }
        
        // get rid of any labels that are no longer being used
        garbageCollect(labels);
    }
    
    protected void set(VisualItem item, double xOrY, boolean onX, double midPoint) {
        if (onX) {
            // if it's an x-axis value
            PrefuseLib.updateDouble(item, VisualItem.X,  xOrY);
            PrefuseLib.updateDouble(item, VisualItem.Y,  y_min);
            PrefuseLib.updateDouble(item, VisualItem.X2, xOrY);
            PrefuseLib.updateDouble(item, VisualItem.Y2, y_max);
            // assign midpoint based on half distance between from start to next
            item.set(MID_POINT, midPoint);
        } else {
            // if it's a y-axis value
            PrefuseLib.updateDouble(item, VisualItem.X,  x_min);
            PrefuseLib.updateDouble(item, VisualItem.Y,  xOrY);
            PrefuseLib.updateDouble(item, VisualItem.X2, x_max);
            PrefuseLib.updateDouble(item, VisualItem.Y2, xOrY);
            // assign midpoint based on half distance between from start to next
            item.set(MID_POINT, midPoint);
        }
    }
    
    /**
     * Reset an axis label VisualItem
     */
    protected void reset(VisualItem item) {
        item.setVisible(false);
        item.setEndVisible(false);
        item.setStartStrokeColor(item.getStrokeColor());
        item.revertToDefault(VisualItem.STROKECOLOR);
        item.revertToDefault(VisualItem.ENDSTROKECOLOR);
        item.setStartTextColor(item.getTextColor());
        item.revertToDefault(VisualItem.TEXTCOLOR);
        item.revertToDefault(VisualItem.ENDTEXTCOLOR);
        item.setStartFillColor(item.getFillColor());
        item.revertToDefault(VisualItem.FILLCOLOR);
        item.revertToDefault(VisualItem.ENDFILLCOLOR);
    }
    
    /**
     * Remove axis labels no longer being used.
     */
    protected void garbageCollect(VisualTable labels) {
        Iterator iter = labels.tuples();
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            if ( !item.isStartVisible() && !item.isEndVisible() ) {
                labels.removeTuple(item);
            }
        }
    }
    
    /**
     * Create a new table for representing axis labels.
     */
    protected VisualTable getTable() {
        TupleSet ts = m_vis.getGroup(m_group);
        if ( ts == null || rebuildTable) {
            rebuildTable = false;
            if (ts != null) {
                m_vis.removeGroup(m_group);
            }
            Schema s = PrefuseLib.getAxisLabelSchema();
            // add specific columns for given categories
            s.addColumn(CAT_INDEX, int.class, -1);
            s.addColumn(CAT_LABEL, String.class, "");
            s.addColumn(IS_LABEL, boolean.class, false);
            s.addColumn(IS_MAIN_LABEL, boolean.class, false);
            s.addColumn(IS_X, boolean.class, false);
            s.addColumn(MID_POINT, double.class);
            VisualTable vt = m_vis.addTable(m_group, s);
            return vt;
        } else if ( ts instanceof VisualTable ) {
            return (VisualTable)ts;
        } else {
            throw new IllegalStateException(
                "Group already exists, not being used for labels");
        }
    }
    
    public void resetTable() {
        // sets flag to rebuild table from scratch on next run
        rebuildTable = true;
    }
    
}
