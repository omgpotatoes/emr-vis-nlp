package annotator.visualizer;

import annotator.MainWindow;
import annotator.data.Document;
import annotator.data.DocumentMedColon;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 *
 * Simple class for displaying a barchart for a particular variable for a
 * particular data subset.
 *
 * @author alexander.p.conrad@gmail.com, loosely based on example provided at
 * http://www.roseindia.net/java/example/java/swing/draw-simple-bar-chart.shtml
 */
public class VarBarChart extends JPanel {

    // idea: draw bars for whole dataset in background (faint colors), 
    //  draw bars for selected subset overtop (saturated).
    // get selected variable from drop-down menu
    JComboBox attrNameBox;

    public VarBarChart(JComboBox attrNameBox) {
        this.attrNameBox = attrNameBox;

    }

    public VarBarChart() {
        this.attrNameBox = null;

    }

    public void setAttrNameBox(JComboBox attrNameBox) {
        this.attrNameBox = attrNameBox;
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        String attrName = (String) (attrNameBox.getSelectedItem()); // get name from selector option

        Dimension dim = getSize();
        int clientWidth = dim.width;
        int clientHeight = dim.height;


        // draw the basic outline of the box




        if (!attrName.equals("") && MainWindow.activeDataset != null) {
            // get scores for all docs
            //  assume that attrName can only be for one of the strict categorical attrs
            //  assume that docs are only of biomed type
            String[] vals = {"-1", "0", "1"};
            Map<String, Integer> valCountMap = new HashMap<>();
            for (String val : vals) {
                valCountMap.put(val, 0);
            }
            int barWidth = clientWidth / vals.length;

            List<Document> allDocs = MainWindow.activeDataset.getDocuments();

            for (int d = 0; d < allDocs.size(); d++) {

                DocumentMedColon doc = (DocumentMedColon) (allDocs.get(d));
                Map<String, String> vars = doc.getVars();
                Map<String, Integer> indicators = doc.getIndicators();

                if (indicators.containsKey(attrName)) {

                    String val = indicators.get(attrName) + "";
                    if (valCountMap.containsKey(val)) {
                        valCountMap.put(val, valCountMap.get(val) + 1);
                    } else {
                        // shouldn't happen
                        System.err.println("VarBarChart: encountered abnormal val \"" + val + "\" for attr \"" + attrName + "\" in doc " + doc.getName());
                    }

                } else if (vars.containsKey(attrName)) {

                    String val = vars.get(attrName);
                    if (valCountMap.containsKey(val)) {
                        valCountMap.put(val, valCountMap.get(val) + 1);
                    } else {
                        // shouldn't happen
                        System.err.println("VarBarChart: encountered abnormal val \"" + val + "\" for attr \"" + attrName + "\" in doc " + doc.getName());
                    }

                } else {

                    // name isn't valid, shouldn't happen
                    System.err.println("could not find attribute \"" + attrName + "\" for doc: " + doc.getName());
                    assert false;

                }

            }

            // find fraction for each value
            int totalVals = 0;
            int[] valCounts = new int[vals.length];
            for (int v = 0; v < vals.length; v++) {
                String val = vals[v];
                valCounts[v] = valCountMap.get(val);
                totalVals += valCounts[v];
            }

            // draw the faint background bars for the whole dataset
            Font labelFont = new Font("Book Antiqua", Font.PLAIN, 10);
            FontMetrics labelFontMetrics = graphics.getFontMetrics(labelFont);

            int labelHeight = 18;
            for (int v = 0; v < vals.length; v++) {

                int left = v * barWidth + 1;
                int top = clientHeight - labelHeight;

                int height = (int) (((double) valCounts[v] / (double) totalVals) * (clientHeight - labelHeight));
                height = -height;
                int width = barWidth - 2;

                // debug
                //System.out.println("debug: VarBarChart: drawing box: " + left + ", " + (top+height) + ", " + width + ", " + (-height));

                graphics.setColor(Color.gray);
                graphics.fillRect(left, top+height, width, -height);
                //graphics.setColor(Color.black);
                //graphics.drawRect(left, top, width, height);

                int q = clientHeight - labelFontMetrics.getDescent();
                int labelWidth = labelFontMetrics.stringWidth(vals[v]);
                int p = v * barWidth + (barWidth - labelWidth) / 2;
                graphics.setColor(Color.black);
                graphics.drawString(vals[v], p, q);

            }






            int selectedDocumentIndex = MainWindow.selectedDocumentIndex;
            if (selectedDocumentIndex != -1) {

                // index = doc index, value = cluster membership
            	List<Integer> clusterList = null;
            	int selectedClusterIndex = -1;
            	if (!MainWindow.customSelectionModeEnabled) {
            		clusterList = LSA2dVisualizer.getDatasetClusters();
                    selectedClusterIndex = clusterList.get(selectedDocumentIndex);
            	} else {
            		clusterList = LSA2dVisualizer.getCustomSelectedPoints();
            		selectedClusterIndex = 1;
            	}

                // find all other docs in this cluster
                // sum attr scores for selected cluster
                Map<String, Integer> clusterValCountMap = new HashMap<>();
                for (String val : vals) {
                    clusterValCountMap.put(val, 0);
                }
                List<Integer> docIndicesForCluster = new ArrayList<>();

                for (int d = 0; d < clusterList.size(); d++) {

                    if (d != selectedDocumentIndex) {

                        int cluster = clusterList.get(d);
                        if (cluster == selectedClusterIndex) {
                            // debug
                            //System.out.println("debug: doc " + allDocs.get(d).getName() + " in cluster " + cluster + ", adding to selected group");

                            docIndicesForCluster.add(d);

                            DocumentMedColon doc = (DocumentMedColon) (allDocs.get(d));
                            Map<String, String> vars = doc.getVars();
                            Map<String, Integer> indicators = doc.getIndicators();

                            if (indicators.containsKey(attrName)) {

                                String val = indicators.get(attrName) + "";
                                if (clusterValCountMap.containsKey(val)) {
                                    clusterValCountMap.put(val, clusterValCountMap.get(val) + 1);
                                } else {
                                    // shouldn't happen
                                    System.err.println("VarBarChart: encountered abnormal val \"" + val + "\" for attr \"" + attrName + "\" in doc " + doc.getName());
                                }

                            } else if (vars.containsKey(attrName)) {

                                String val = vars.get(attrName);
                                if (clusterValCountMap.containsKey(val)) {
                                    clusterValCountMap.put(val, clusterValCountMap.get(val) + 1);
                                } else {
                                    // shouldn't happen
                                    System.err.println("VarBarChart: encountered abnormal val \"" + val + "\" for attr \"" + attrName + "\" in doc " + doc.getName());
                                }

                            } else {

                                // name isn't valid, shouldn't happen
                                System.err.println("could not find attribute \"" + attrName + "\" for doc: " + doc.getName());
                                assert false;

                            }

                        }

                    }

                }

                int clusterTotalVals = 0;
                int[] clusterValCounts = new int[vals.length];
                for (int v = 0; v < vals.length; v++) {
                    String val = vals[v];
                    clusterValCounts[v] = clusterValCountMap.get(val);
                    clusterTotalVals += clusterValCounts[v];
                }

                // draw the foreground boxes for the selected cluster / group of docs
                for (int v = 0; v < vals.length; v++) {
                    
                    //int left = v * barWidth + 4;
                    int left = v * barWidth + 1;
                    int top = clientHeight - labelHeight;

                    int height = (int) (((double) clusterValCounts[v] / (double) totalVals) * (clientHeight - labelHeight));
                    height = -height;
                    //int width = barWidth - 8;
                    int width = barWidth - 2;

                    // debug
                    //System.out.println("debug: VarBarChart: drawing box: " + left + ", " + (top+height) + ", " + width + ", " + (-height));

                    graphics.setColor(Color.blue);
                    //graphics.fillRect(left, top, width, height);
                    graphics.fillRect(left, top+height, width, -height);
                    //graphics.setColor(Color.black);
                    //graphics.drawRect(left, top, width, height);


                }


            }

        }

    }
}
