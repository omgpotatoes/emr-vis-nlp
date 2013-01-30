package emr_vis_nlp.view;

import emr_vis_nlp.model.DocGridTableSelectorModel;
import emr_vis_nlp.model.Document;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

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
    protected String attrName;
    
    protected List<Document> allDocs;

    public VarBarChartForCell(String attrName, List<Document> allDocs) {
        this.attrName = attrName;
        this.allDocs = allDocs;
    }

    public VarBarChartForCell() {
        this.attrName = "";
        allDocs = new ArrayList<>();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Dimension dim = getSize();
        int clientWidth = dim.width;
        int clientHeight = dim.height;

        Map<String, Boolean> abnormalNameMap = DocGridTableSelectorModel.getAbnormalNameMap();
        
        // draw the basic outline of the box

        if (!attrName.equals("") && !abnormalNameMap.containsKey(attrName)) {
            // get scores for all docs
            //  assume that docs are only of biomed type
//            String[] vals = {"-1", "0", "1"};
//            String[] vals = {"not_eligible", "eligible", "pass"};
            String[] vals = {"N/A", "Fail", "Pass"};  // TODO make these model-independent!
            Map<String, Integer> valCountMap = new HashMap<>();
            for (String val : vals) {
                valCountMap.put(val, 0);
            }
            int barWidth = clientWidth / vals.length;

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
            for (int v = 0; v < vals.length; v++) {

                //int left = v * barWidth + 1;
            	int left = v * barWidth;
                int top = clientHeight - labelHeight;

                //int height = (int) (((double) valCounts[v] / (double) totalVals) * (clientHeight - labelHeight));
                int height = (int) (((double) valCounts[v] / (double) allDocs.size()) * (clientHeight - labelHeight)); // base size of box on size of whole dataset, to illustrate sparsely-instantiated vars
                valPercs[v] = ((double) valCounts[v] / (double) allDocs.size());
                height = -height;
                //int width = barWidth - 2;
                int width = barWidth;

                // debug
                //System.out.println("debug: VarBarChart: drawing box: " + left + ", " + (top+height) + ", " + width + ", " + (-height));

//                graphics.setColor(Color.gray);
                graphics.setColor(Color.blue);
                graphics.fillRect(left, top + height, width, -height);
                //graphics.setColor(Color.black);
                //graphics.drawRect(left, top, width, height);

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

    }
}
