package emr_vis_nlp.view.var_bar_chart;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.ml.PredictionCertaintyTuple;
import emr_vis_nlp.view.doc_grid.DocGridTableSelectorModel;
import emr_vis_nlp.model.Document;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

/**
 *
 * Simple class for displaying a barchart for a particular variable for a
 * particular data subset. Meant to be used as a cell in DatasetVarsTableModel, 
 * rather than as a standalone display
 *
 * @author alexander.p.conrad@gmail.com, loosely based on example provided at
 * http://www.roseindia.net/java/example/java/swing/draw-simple-bar-chart.shtml
 */
public class VarBarChartForCell extends JPanel {

    // TODO : draw bars for whole dataset in background (faint colors), bars for selected subset overtop (saturated).
    
    // governing controller
    private MainController controller;
    
    // name of attribute being represented with this object
    private String attrName;
    // list of values for this attribute
    private List<String> attrVals;
    // list of documents being represented in this table
    private List<Document> allDocs;
    // list of all cols for cell
    private List<VarBarChartColumn> allCols;
    // current tooltiptext
    private String toolTipText = "";

    public VarBarChartForCell(String attrName, List<String> attrVals, List<Document> allDocs) {
        super();
        this.attrName = attrName;
        this.attrVals = attrVals;
        this.allDocs = allDocs;
        this.controller = MainController.getMainController();
        rebuildComponents();
    }

    public void rebuildComponents() {
        
        removeAll();

        allCols = new ArrayList<>();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

//        Map<String, Boolean> abnormalNameMap = DocGridTableSelectorModel.getAbnormalNameMap();

        // draw the basic outline of the box

//        if (!attrName.equals("") && !abnormalNameMap.containsKey(attrName)) {
        if (!attrName.equals("")) {
            // get scores for all docs
//            List<String> valList = DatasetTermTranslator.getDefaultValList();
            List<String> valList = attrVals;
            String[] vals = new String[valList.size()];
            for (int v = 0; v < valList.size(); v++) {
                vals[v] = valList.get(v);
            }
            Map<String, Integer> valCountMap = new HashMap<>();
            for (String val : vals) {
                valCountMap.put(val, 0);
            }

            // count up number of times each value occurs
            for (int d = 0; d < allDocs.size(); d++) {

                Document doc = allDocs.get(d);
                Map<String, String> attributes = doc.getAttributes();

                // use manual annotation if it exists, otherwise use prediction
                if (attributes.containsKey(attrName)) {

                    String val = attributes.get(attrName) + "";
                    if (valCountMap.containsKey(val)) {
                        valCountMap.put(val, valCountMap.get(val) + 1);
                    } else {
                        // shouldn't happen
                        System.err.println("VarBarChartForCell: encountered abnormal val \"" + val + "\" for attr \"" + attrName + "\" in doc " + doc.getName());
                    }

                } else {

                    // name isn't valid or we don't have annotation, so we should get prediction
                    PredictionCertaintyTuple prediction = controller.getPrediction(d, attrName);
                    String val = prediction.getValue();
                    if (valCountMap.containsKey(val)) {
                        valCountMap.put(val, valCountMap.get(val) + 1);
                    } else {
                        System.err.println("VarBarChartForCell: encountered abnormal val \"" + val + "\" for attr \"" + attrName + "\" predicted for doc " + doc.getName());
                    }
//                    System.err.println("VarBarChartForCell: could not find attribute \"" + attrName + "\" for doc: " + doc.getName());
//                    assert false;

                }

            }

            // find fraction for each value
            int totalVals = 0;
            int[] valCounts = new int[vals.length];
            double[] valPercs = new double[vals.length];
//            double[] valPercsSelected = new double[vals.length];
            for (int v = 0; v < vals.length; v++) {
                String val = vals[v];
                valCounts[v] = valCountMap.get(val);
                totalVals += valCounts[v];
            }

            // TODO : draw the faint background bars for the whole dataset
//            Font labelFont = new Font("Book Antiqua", Font.PLAIN, 10);
//            FontMetrics labelFontMetrics = graphics.getFontMetrics(labelFont);
            
            int labelHeight = 0;
            add(Box.createRigidArea(new Dimension(1, 100)));
            for (int v = 0; v < vals.length; v++) {

                int height = (int) (((double) valCounts[v] / (double) allDocs.size()) * (100 - labelHeight)); // base size of box on size of whole dataset, to illustrate sparsely-instantiated vars
                valPercs[v] = ((double) valCounts[v] / (double) allDocs.size());
                int width = 100 / vals.length;

                VarBarChartColumn varBarChartCol = new VarBarChartColumn(attrName, vals[v], width, height, valPercs[v]);
                add(varBarChartCol);
                allCols.add(varBarChartCol);
                add(Box.createRigidArea(new Dimension(1, 100)));

            }

            // update tooltip text
//            toolTipText = attrName+":  "+((int)(100*valPercs[0]))+"% N/A, "+((int)(100*valPercs[1]))+"% Fail, "+((int)(100*valPercs[2]))+"% Pass (click to select)";
            toolTipText = attrName + ": ";
            for (int i = 0; i < attrVals.size(); i++) {
                String attrVal = attrVals.get(i);
                toolTipText += ((int) (100 * valPercs[i])) + "% " + attrVal;
                if (i < attrVals.size() - 1) {
                    toolTipText += ", ";
                } else {
                    toolTipText += " (click to select/de-select)";
                }
            }

            setToolTipText(toolTipText);

        }
        
    }
    
    /**
     * Intermediate method for connecting the mouseevents captured by the JTable to the event handling code in the actual cell itself.
     * 
     * @param cell index indicating which of the (3?) cells the click took place in.
     */
    public void clickOnCell(int cell) {
        allCols.get(cell).mouseClicked(null);
    }
    
    /**
     * Resets the highlight state on all cells in this VarBarChart.
     */
    public void unhighlightCells() {
        for (int c=0; c<allCols.size(); c++) {
            allCols.get(c).setIsHighlighted(false);
        }
    }
    
    /**
     * Resets the enabled state on all cells in this VarBarChart.
     */
    public void enableAllCells() {
        for (int c=0; c<allCols.size(); c++) {
            allCols.get(c).setIsEnabled(true);
        }
    }

    public int getNumVals() {
        if (attrVals != null) {
            return attrVals.size();
        }
        return 0;
    }

    @Override
    public String toString() {
        return toolTipText;
    }
    
    // warning! mouse events are being intercepted by the JTable in which this VarBarChart is embedded!
    class VarBarChartColumn extends JPanel implements MouseListener {
        
        public final Color colorDefault = Color.blue;
        public final Color colorHighlighted = Color.RED;
        public final Color colorDisabled = Color.gray;
        
        private String attrName;
        private String attrVal;
        
        private int x;
        private int y;
        private int width;
        private int height;
        
        private double fracFull;
        
        private boolean isEnabled;
        private boolean isHighlighted;
        
        public VarBarChartColumn(String attrName, String attrVal, int width, int height, double fracFull) {
            super();
            this.attrName = attrName;
            this.attrVal = attrVal;
            this.width = width;
            this.height = height;
            this.fracFull = fracFull;
            isEnabled = true;
            isHighlighted = false;
            
//            setAlignmentY(Component.BOTTOM_ALIGNMENT);
            setMinimumSize(new Dimension(0, 0));
            setPreferredSize(new Dimension(width, height));
            setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
            
            addMouseListener(this);
            
        }
        
        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Dimension dim = getSize();
            int clientWidth = dim.width;
            int clientHeight = dim.height;
            
            // draw from base up through filled frac

            int left = 0;
            int top = clientHeight - (int)(clientHeight * fracFull);
            int height = (int) (clientHeight * fracFull);
            int width = clientWidth;
            
            
            
            if (!isEnabled) {
                graphics.setColor(colorDisabled);
                graphics.fillRect(left, top, width, height);
            } else if (isHighlighted) {
                graphics.setColor(colorHighlighted);
                graphics.fillRect(left, top, width, height);
            } else {
                graphics.setColor(colorDefault);
                graphics.fillRect(left, top, width, height);
            }

        }

        public void setIsEnabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
        }
        
        public void setIsHighlighted(boolean isHighlighted) {
            this.isHighlighted = isHighlighted;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // toggle select / deselect for applicable docs
            // warning: mouse event may be null! (if we're getting click info from table)
            // debug
            System.out.println("debug: varBarChartColumn: mouseClicked for attrName = "+attrName+", attrVal = "+attrVal);
            if (isEnabled) {
                // disable designated docs
                isEnabled = false;
                controller.disableDocsWithAttrVal(attrName, attrVal);
                
            } else {
                // enable designated docs
                isEnabled = true;
                controller.enableDocsWithAttrVal(attrName, attrVal);
            }
            
            repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            // do nothing
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // do nothing
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // highlight docs in group
            // note: this method does not currently work, because events are intercepted by the containing JTable!
            // debug
//            System.out.println("debug: varBarChartColumn: mouseEntered for attrName = "+attrName+", attrVal = "+attrVal);
//            isHighlighted = true;
//            controller.highlightDocsWithAttrVal(attrName, attrVal);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // disable doc highlight for docs in group
            // note: this method does not currently work, because events are intercepted by the containing JTable!
            // debug
//            System.out.println("debug: varBarChartColumn: mouseExited for attrName = "+attrName+", attrVal = "+attrVal);
//            isHighlighted = false;
//            controller.unhighlightAllDocs();
        }
        
        
        
    }
    
}
