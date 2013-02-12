package emr_vis_nlp.view;

import emr_vis_nlp.controller.MainController;
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

    // idea: draw bars for whole dataset in background (faint colors), 
    //  draw bars for selected subset overtop (saturated).
    private String attrName;
    // governing controller
    private MainController controller;
    // list of documents being represented in this table
    private List<Document> allDocs;
    // list of all cols for cell
    private List<VarBarChartColumn> allCols;

    public VarBarChartForCell(MainController controller, String attrName, List<Document> allDocs) {
        super();
        this.attrName = attrName;
        this.allDocs = allDocs;
        this.controller = controller;
        rebuildComponents();
    }

    public VarBarChartForCell() {
        this.attrName = "";
        allDocs = new ArrayList<>();
        allCols = new ArrayList<>();
    }

    public void rebuildComponents() {
        
        removeAll();
        
        allCols = new ArrayList<>();
//        setLayout(null);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
//        Dimension dim = getSize();
//        int clientWidth = dim.width;
//        int clientHeight = dim.height;

        Map<String, Boolean> abnormalNameMap = DocGridTableSelectorModel.getAbnormalNameMap();
        
        // draw the basic outline of the box

        if (!attrName.equals("") && !abnormalNameMap.containsKey(attrName)) {
            // get scores for all docs
            //  assume that docs are only of biomed type
//            String[] vals = {"-1", "0", "1"};
//            String[] vals = {"not_eligible", "eligible", "pass"};
            String[] vals = {"N/A", "Fail", "Pass"};  // TODO make these model-independent!
//            String[] vals = DatasetTermTranslator.getDefaultValList();
            Map<String, Integer> valCountMap = new HashMap<>();
            for (String val : vals) {
                valCountMap.put(val, 0);
            }
//            int barWidth = clientWidth / vals.length;

            for (int d = 0; d < allDocs.size(); d++) {

                Document doc = allDocs.get(d);
                Map<String, String> attributes = doc.getAttributes();

                if (attributes.containsKey(attrName)) {

                    String val = attributes.get(attrName) + "";
                    if (valCountMap.containsKey(val)) {
                        valCountMap.put(val, valCountMap.get(val) + 1);
                    } else {
                        // shouldn't happen
                        System.err.println("VarBarChartForCell: encountered abnormal val \"" + val + "\" for attr \"" + attrName + "\" in doc " + doc.getName());
                    }

                } else {

                    // name isn't valid, shouldn't happen
                    System.err.println("VarBarChartForCell: could not find attribute \"" + attrName + "\" for doc: " + doc.getName());
                    assert false;

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

            // draw the faint background bars for the whole dataset
//            Font labelFont = new Font("Book Antiqua", Font.PLAIN, 10);
//            FontMetrics labelFontMetrics = graphics.getFontMetrics(labelFont);

            //int labelHeight = 18;
            int labelHeight = 0;
//            Insets insets = getInsets();
//            Dimension dimension = getSize();
//            add(Box.createHorizontalGlue());
            add(Box.createRigidArea(new Dimension(1,100)));
            for (int v = 0; v < vals.length; v++) {

//                //int left = v * barWidth + 1;
//            	int left = v * barWidth;
//                int top = clientHeight - labelHeight;
//
//                //int height = (int) (((double) valCounts[v] / (double) totalVals) * (clientHeight - labelHeight));
//                int height = (int) (((double) valCounts[v] / (double) allDocs.size()) * (clientHeight - labelHeight)); // base size of box on size of whole dataset, to illustrate sparsely-instantiated vars
//                valPercs[v] = ((double) valCounts[v] / (double) allDocs.size());
//                height = -height;
//                //int width = barWidth - 2;
//                int width = barWidth;
//
//                // if vals for clientWidth, clientHeight are 0, just do as fractions out of 100; BoxLayout should take care of sizing
//                clientWidth=0;clientHeight=0;
//                if (clientWidth == 0 || clientHeight == 0) {
                    int height = (int) (((double) valCounts[v] / (double) allDocs.size()) * (100 - labelHeight)); // base size of box on size of whole dataset, to illustrate sparsely-instantiated vars
                    valPercs[v] = ((double) valCounts[v] / (double) allDocs.size());
//                    height = -height;
                    //int width = barWidth - 2;
                    int width = 100 / vals.length;
//                }
                
                
                // debug
                //System.out.println("debug: VarBarChart: drawing box: " + left + ", " + (top+height) + ", " + width + ", " + (-height));

//                graphics.setColor(Color.gray);
                
//                graphics.setColor(Color.blue);
//                graphics.fillRect(left, top + height, width, -height);
                
                //graphics.setColor(Color.black);
                //graphics.drawRect(left, top, width, height);
                
//                VarBarChartColumn varBarChartCol = new VarBarChartColumn(attrName, vals[v], left + insets.left, top + insets.top, width, height);
//                VarBarChartColumn varBarChartCol = new VarBarChartColumn(attrName, vals[v], left + insets.left, top + height + insets.top, width, -height);
                
                VarBarChartColumn varBarChartCol = new VarBarChartColumn(attrName, vals[v], width, height, valPercs[v]);
                add(varBarChartCol);
                allCols.add(varBarChartCol);
                add(Box.createRigidArea(new Dimension(1, 100)));
//                add(Box.createHorizontalGlue());

//                JLabel label = new JLabel("sometext");
//                label.setAlignmentY(Component.BOTTOM_ALIGNMENT);
//                label.setPreferredSize(new Dimension(width, height));
//                this.add(label);
                
                
//                int q = clientHeight - labelFontMetrics.getDescent();
//                int labelWidth = labelFontMetrics.stringWidth(vals[v]);
//                int p = v * barWidth + (barWidth - labelWidth) / 2;
//                graphics.setColor(Color.black);
//                graphics.drawString(vals[v], p, q);

            }


//            // old alpha code for incorporating cluster selection no longer needed; see previous version's code if needed
            
            // update tooltip text
            setToolTipText(attrName+":  "+((int)(100*valPercs[0]))+"% N/A, "+((int)(100*valPercs[1]))+"% Fail, "+((int)(100*valPercs[2]))+"% Pass");
            
        }
        
//        setOpaque(false);
//        repaint();
        
    }
    
//    @Override
//    public void paintComponent(Graphics graphics) {
//        super.paintComponent(graphics);
//
//        Dimension dim = getSize();
//        int clientWidth = dim.width;
//        int clientHeight = dim.height;
//
//        Map<String, Boolean> abnormalNameMap = DocGridTableSelectorModel.getAbnormalNameMap();
//        
//        // draw the basic outline of the box
//
//        if (!attrName.equals("") && !abnormalNameMap.containsKey(attrName)) {
//            // get scores for all docs
//            //  assume that docs are only of biomed type
////            String[] vals = {"-1", "0", "1"};
////            String[] vals = {"not_eligible", "eligible", "pass"};
//            String[] vals = {"N/A", "Fail", "Pass"};  // TODO make these model-independent!
//            Map<String, Integer> valCountMap = new HashMap<>();
//            for (String val : vals) {
//                valCountMap.put(val, 0);
//            }
//            int barWidth = clientWidth / vals.length;
//
//            for (int d = 0; d < allDocs.size(); d++) {
//
//                Document doc = allDocs.get(d);
//                Map<String, String> attributes = doc.getAttributes();
//
//                if (attributes.containsKey(attrName)) {
//
//                    String val = attributes.get(attrName) + "";
//                    if (valCountMap.containsKey(val)) {
//                        valCountMap.put(val, valCountMap.get(val) + 1);
//                    } else {
//                        // shouldn't happen
//                        System.err.println("VarBarChartForCell: encountered abnormal val \"" + val + "\" for attr \"" + attrName + "\" in doc " + doc.getName());
//                    }
//
//                } else {
//
//                    // name isn't valid, shouldn't happen
//                    System.err.println("VarBarChartForCell: could not find attribute \"" + attrName + "\" for doc: " + doc.getName());
//                    assert false;
//
//                }
//
//            }
//
//            // find fraction for each value
//            int totalVals = 0;
//            int[] valCounts = new int[vals.length];
//            double[] valPercs = new double[vals.length];
////            double[] valPercsSelected = new double[vals.length];
//            for (int v = 0; v < vals.length; v++) {
//                String val = vals[v];
//                valCounts[v] = valCountMap.get(val);
//                totalVals += valCounts[v];
//            }
//
//            // draw the faint background bars for the whole dataset
////            Font labelFont = new Font("Book Antiqua", Font.PLAIN, 10);
////            FontMetrics labelFontMetrics = graphics.getFontMetrics(labelFont);
//
//            //int labelHeight = 18;
//            int labelHeight = 0;
//            for (int v = 0; v < vals.length; v++) {
//
//                //int left = v * barWidth + 1;
//            	int left = v * barWidth;
//                int top = clientHeight - labelHeight;
//
//                //int height = (int) (((double) valCounts[v] / (double) totalVals) * (clientHeight - labelHeight));
//                int height = (int) (((double) valCounts[v] / (double) allDocs.size()) * (clientHeight - labelHeight)); // base size of box on size of whole dataset, to illustrate sparsely-instantiated vars
//                valPercs[v] = ((double) valCounts[v] / (double) allDocs.size());
//                height = -height;
//                //int width = barWidth - 2;
//                int width = barWidth;
//
//                // debug
//                //System.out.println("debug: VarBarChart: drawing box: " + left + ", " + (top+height) + ", " + width + ", " + (-height));
//
////                graphics.setColor(Color.gray);
//                graphics.setColor(Color.blue);
//                graphics.fillRect(left, top + height, width, -height);
//                //graphics.setColor(Color.black);
//                //graphics.drawRect(left, top, width, height);
//
////                int q = clientHeight - labelFontMetrics.getDescent();
////                int labelWidth = labelFontMetrics.stringWidth(vals[v]);
////                int p = v * barWidth + (barWidth - labelWidth) / 2;
////                graphics.setColor(Color.black);
////                graphics.drawString(vals[v], p, q);
//
//            }
//
//
////            // old alpha code for incorporating cluster selection no longer needed; see previous version's code if needed
//            
//            // update tooltip text
//            setToolTipText(attrName+":  "+((int)(100*valPercs[0]))+"% N/A, "+((int)(100*valPercs[1]))+"% Fail, "+((int)(100*valPercs[2]))+"% Pass");
//            
//        }
//        
////        setOpaque(false);
//
//    }
    
    public void clickOnCell(int cell) {
        allCols.get(cell).mouseClicked(null);
    }
    
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
        
//        public VarBarChartColumn(String attrName, String attrVal, int x, int y, int width, int height) {
//            this.attrName = attrName;
//            this.attrVal = attrVal;
//            this.x = x;
//            this.y = y;
//            this.height = height;
//            this.width = width;
//            isEnabled = true;
//            isHighlighted = false;
//            
//            setSize(width, height);
//            if (isEnabled) {
//                if (isHighlighted) {
//                    setBackground(colorHighlighted);
//                } else {
//                    setBackground(colorDefault);
//                }
//            } else {
//                setBackground(colorDisabled);
//            }
//            setOpaque(true);
//            setBorder(BorderFactory.createLineBorder(Color.black));
//            
//            setBounds(x, y, width, height);
//            repaint();
//        }
        
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
//            setMaximumSize(new Dimension(width, height));
//            if (isEnabled) {
//                if (isHighlighted) {
//                    setBackground(colorHighlighted);
//                } else {
//                    setBackground(colorDefault);
//                }
//            } else {
//                setBackground(colorDisabled);
//            }
//            setOpaque(true);
//            setBorder(BorderFactory.createLineBorder(Color.black));
//            setBounds(x, y, width, height);
//            repaint();
            
            addMouseListener(this);
            
        }
        
//        public void refresh() {
//            
//            setSize(width, height);
//            if (isEnabled) {
//                if (isHighlighted) {
//                    setBackground(colorHighlighted);
//                } else {
//                    setBackground(colorDefault);
//                }
//            } else {
//                setBackground(colorDisabled);
//            }
//            setOpaque(true);
//            setBorder(BorderFactory.createLineBorder(Color.black));
//            
//            setBounds(x, y, width, height);
//            repaint();
//        }
        
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
                // TODO
//                isEnabled = false;
                
                // for testing, just highlight
                if (isHighlighted) {
                    controller.unhighlightAllDocs();
                    isHighlighted = false;
                } else {
                    controller.highlightDocsWithAttrVal(attrName, attrVal);
                    isHighlighted = true;
                }
            } else {
                // enable designated docs
                // TODO
//                isEnabled = true;
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
            // debug
            System.out.println("debug: varBarChartColumn: mouseEntered for attrName = "+attrName+", attrVal = "+attrVal);
            isHighlighted = true;
            controller.highlightDocsWithAttrVal(attrName, attrVal);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            // disable doc highlight for docs in group
            // debug
            System.out.println("debug: varBarChartColumn: mouseExited for attrName = "+attrName+", attrVal = "+attrVal);
            isHighlighted = false;
            controller.unhighlightAllDocs();
        }
        
        
        
    }
    
}
