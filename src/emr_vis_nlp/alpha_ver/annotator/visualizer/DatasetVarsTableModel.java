package annotator.visualizer;

import annotator.MainWindow;
import annotator.data.DatasetMedColonDoclist;
import annotator.data.Document;
import annotator.data.DocumentMedColon;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.apache.pdfbox.util.operator.SetCharSpacing;

/**
 * Backing table model for prototype visualization controls.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DatasetVarsTableModel extends AbstractTableModel {

//    private String[] columnNames = {"attribute", "2d position", "cluster"};
//    private String[] columnNames = {"attribute", "2d position", "cluster", "val -1", "val 0", "val 1"};
	private String[] columnNames = {"attribute", "2d position", "cluster", "distribution (not-eligible, eligible, pass)"};
    private List<Boolean> selectedFor2dPosition;
    private List<Boolean> selectedForCluster;
    private List<String> attrNames;
    // list of non-categorical vars; these should not be selectable for purposes of layout
    private Map<String, Boolean> abnormalVarNames;
    
    private static Map<String, Boolean> abnormalNamesMap = null;
    private static Map<String, Boolean> predictionNamesMap = null;
    
    // list of varBarCharts to be displayed within the table
    private List<JPanel> distribDisplays;

    public DatasetVarsTableModel() {
        super();
        attrNames = new ArrayList<>();
        if (MainWindow.activeDataset != null) {
//            attrNames.addAll(((DatasetMedColonDoclist) MainWindow.activeDataset).getAllVarsAndIndis());
        	attrNames.addAll(((DatasetMedColonDoclist) MainWindow.activeDataset).getSelectedVarsAndIndis());
        } else {
//            attrNames = new ArrayList<>();
        }
        //attrNames.add(0, "<text>");

        selectedFor2dPosition = new ArrayList<>();
        selectedForCluster = new ArrayList<>();

        populateAbnormalVarNames();
        distribDisplays = new ArrayList<>();
        for (int a = 0; a < attrNames.size(); a++) {
//            if (a == 0) {
//                selectedFor2dPosition.add(true);
//                selectedForCluster.add(false);
//            } else {
            String name = attrNames.get(a);
            selectedFor2dPosition.add(false);
            if (abnormalVarNames.containsKey(name)) {
                selectedForCluster.add(false);
            } else {
                selectedForCluster.add(true);
            }
            // build varBarChart
            VarBarChartForCell barChart = new VarBarChartForCell(name);
            distribDisplays.add(barChart);
//            }
        }
        


    }
    
    public static Map<String, Boolean> getPredictionNameMap() {
    	
    	if (predictionNamesMap != null) {
    		return predictionNamesMap;
    	}
    	
    	predictionNamesMap = new HashMap<>();
    	
    	predictionNamesMap.put("Indicator_24", true);
    	predictionNamesMap.put("Indicator_23", true);
    	predictionNamesMap.put("Indicator_19", true);
    	predictionNamesMap.put("Indicator_16", true);
    	predictionNamesMap.put("Indicator_17", true);
    	predictionNamesMap.put("Indicator_11", true);
    	predictionNamesMap.put("Indicator_12", true);
    	predictionNamesMap.put("Indicator_2", true);
    	predictionNamesMap.put("Indicator_3.1", true);
    	predictionNamesMap.put("Indicator_21", true);

    	predictionNamesMap.put("Indicator_24".toLowerCase(), true);
    	predictionNamesMap.put("Indicator_23".toLowerCase(), true);
    	predictionNamesMap.put("Indicator_19".toLowerCase(), true);
    	predictionNamesMap.put("Indicator_16".toLowerCase(), true);
    	predictionNamesMap.put("Indicator_17".toLowerCase(), true);
    	predictionNamesMap.put("Indicator_11".toLowerCase(), true);
    	predictionNamesMap.put("Indicator_12".toLowerCase(), true);
    	predictionNamesMap.put("Indicator_2".toLowerCase(), true);
    	predictionNamesMap.put("Indicator_3.1".toLowerCase(), true);
    	predictionNamesMap.put("Indicator_21".toLowerCase(), true);
    	
    	return predictionNamesMap; 
    	
    }

    public static Map<String, Boolean> getAbnormalNameMap() {

        if (abnormalNamesMap != null) {
            return abnormalNamesMap;
        }

        Map<String, Boolean> abnormalVarNames = new HashMap<>();

        abnormalVarNames.put("VAR_Indication_type", true);
        abnormalVarNames.put("VAR_Indication_Type_2", true);
        abnormalVarNames.put("VAR_Indication_Type_3", true);
        abnormalVarNames.put("VAR_Pathology_Report_#", true);
        abnormalVarNames.put("VAR_Polyp_size_largest", true);
        abnormalVarNames.put("VAR_Polyp_size_path", true);
        abnormalVarNames.put("VAR_Follow-up_time", true);
        abnormalVarNames.put("VAR_Nursing_Reports", true);
        abnormalVarNames.put("VAR_FH", true);
        abnormalVarNames.put("VAR_ASA", true);
        
        // new abnormals, based on debugging from varbarchart
        abnormalVarNames.put("VAR_Ileo-cecal_valve", true);
        abnormalVarNames.put("VAR_Prep_adequate", true);
        abnormalVarNames.put("VAR_Any_complication", true);
        abnormalVarNames.put("VAR_Informed_consent", true);
        abnormalVarNames.put("VAR_Appendiceal_orifice", true);
        abnormalVarNames.put("VAR_Prev_colonoscopy", true);
        abnormalVarNames.put("VAR_No_polyp", true);
        abnormalVarNames.put("VAR_No_Pathology_Report", true);

        
        // lowercase versions
        abnormalVarNames.put("VAR_Indication_type".toLowerCase(), true);
        abnormalVarNames.put("VAR_Indication_Type_2".toLowerCase(), true);
        abnormalVarNames.put("VAR_Indication_Type_3".toLowerCase(), true);
        abnormalVarNames.put("VAR_Pathology_Report_#".toLowerCase(), true);
        abnormalVarNames.put("VAR_Polyp_size_largest".toLowerCase(), true);
        abnormalVarNames.put("VAR_Polyp_size_path".toLowerCase(), true);
        abnormalVarNames.put("VAR_Follow-up_time".toLowerCase(), true);
        abnormalVarNames.put("VAR_Nursing_Reports".toLowerCase(), true);
        abnormalVarNames.put("VAR_FH".toLowerCase(), true);
        abnormalVarNames.put("VAR_ASA".toLowerCase(), true);
        
        // new abnormals, based on debugging from varbarchart
        abnormalVarNames.put("VAR_Ileo-cecal_valve".toLowerCase(), true);
        abnormalVarNames.put("VAR_Prep_adequate".toLowerCase(), true);
        abnormalVarNames.put("VAR_Any_complication".toLowerCase(), true);
        abnormalVarNames.put("VAR_Informed_consent".toLowerCase(), true);
        abnormalVarNames.put("VAR_Appendiceal_orifice".toLowerCase(), true);
        abnormalVarNames.put("VAR_Prev_colonoscopy".toLowerCase(), true);
        abnormalVarNames.put("VAR_No_polyp".toLowerCase(), true);
        abnormalVarNames.put("VAR_No_Pathology_Report".toLowerCase(), true);
        

        abnormalNamesMap = abnormalVarNames;

        return abnormalVarNames;

    }

    public static Vector<String> removeAbnormalNames(List<String> names) {

        Vector<String> normalNames = new Vector<>();  // using Vector for the sake of DefaultComboBoxModel
        Map<String, Boolean> abnormalNameMap = getAbnormalNameMap();

        for (int n = 0; n < names.size(); n++) {

            String name = names.get(n);
            if (!abnormalNameMap.containsKey(name.toLowerCase())) {
                normalNames.add(name);
            }

        }

        return normalNames;

    }

    private void populateAbnormalVarNames() {

        abnormalVarNames = getAbnormalNameMap();

    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public int getRowCount() {
        return attrNames.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        if (columnIndex == 0) {
            return attrNames.get(rowIndex);
        } else if (columnIndex == 1) {
            return selectedFor2dPosition.get(rowIndex);
        } else if (columnIndex == 2) {
            return selectedForCluster.get(rowIndex);
        } else if (columnIndex == 3) {
        	// return distrib display
        	return distribDisplays.get(rowIndex);
//        } else if (columnIndex == 3) {
//            // todo streamline this!
//            return ((String[]) buildCellVizForAttr(attrNames.get(rowIndex)))[0];
//        } else if (columnIndex == 4) {
//            // todo streamline this!
//            return ((String[]) buildCellVizForAttr(attrNames.get(rowIndex)))[1];
//        } else if (columnIndex == 5) {
//            // todo streamline this!
//            return ((String[]) buildCellVizForAttr(attrNames.get(rowIndex)))[2];
        } else {
            return null;
        }

    }

    @Override
    public boolean isCellEditable(int row, int col) {

        // for now, only let us change the vars / indicators that we use for clustering
        if (col == 0) {
            return false;
        }

        // disable clustering when custom selection mode is turned on
        if (col == 2 && MainWindow.customSelectionModeEnabled) {
            return false;
        }
        
        // don't edit col3, is image
        if (col == 3) {
        	return false;
        }

//        if (col == 1) {
//            return false;
//        } if (col == 2 && row == 0) {
//            return false;
//        }

        // don't let them edit the abnormal var rows
        String attrName = attrNames.get(row);
        if (abnormalVarNames.containsKey(attrName)) {
            return false;
        } else if (col == 1 || col == 2) {
        	return true;
        }

        return false;

    }

    @Override
    public Class getColumnClass(int c) {

        if (c == 0) {
            return String.class;
        } else if (c == 1) {
            return Boolean.class;
        } else if (c == 2) {
            return Boolean.class;
        } else if (c == 3) {
        	//return VarBarChartForCell.class;
        	return JPanel.class;
        }
//        } else if (c == 3) {
//            return String.class;
//        } else if (c == 4) {
//            return String.class;
//        } else if (c == 5) {
//            return String.class;
//        }
        
        return Boolean.class;

    }

    @Override
    public void setValueAt(Object value, int row, int col) {

        if (col == 1) {
            selectedFor2dPosition.remove(row);
            selectedFor2dPosition.add(row, (Boolean) value);
        } else if (col == 2) {
            selectedForCluster.remove(row);
            selectedForCluster.add(row, (Boolean) value);
        }


    }

    /**
     *
     * @return list of names of attributes that are currently selected for
     * clustering
     */
    public List<String> getActiveClusterAttrs() {

        List<String> activeAttrs = new ArrayList<>();
        for (int a = 0; a < attrNames.size(); a++) {
            String attrName = attrNames.get(a);
            Boolean attrClusterVal = selectedForCluster.get(a);
            if (attrClusterVal) {
                activeAttrs.add(attrName);
            }
        }
        return activeAttrs;

    }

    /**
     *
     * @return list of names of attributes that are currently selected for LSA
     * layout
     */
    public List<String> getActiveLayoutAttrs() {

        List<String> activeAttrs = new ArrayList<>();
        for (int a = 0; a < attrNames.size(); a++) {
            String attrName = attrNames.get(a);
            Boolean attrLayoutVal = selectedFor2dPosition.get(a);
            if (attrLayoutVal) {
                activeAttrs.add(attrName);
            }
        }
        return activeAttrs;

    }

    public void deselectAll() {

        for (int i = 0; i < selectedFor2dPosition.size(); i++) {
            selectedFor2dPosition.remove(i);
            selectedFor2dPosition.add(i, false);
            selectedForCluster.remove(i);
            selectedForCluster.add(i, false);
        }

        fireTableDataChanged();

    }

    public void updateAllRows() {

        attrNames = new ArrayList<>();
        if (MainWindow.activeDataset != null) {
//            attrNames.addAll(((DatasetMedColonDoclist) MainWindow.activeDataset).getAllVarsAndIndis());
            attrNames.addAll(((DatasetMedColonDoclist) MainWindow.activeDataset).getSelectedVarsAndIndis());
        } else {
//            attrNames = new ArrayList<>();
        }
        //attrNames.add(0, "<text>");

        selectedFor2dPosition = new ArrayList<>();
        selectedForCluster = new ArrayList<>();
        
        distribDisplays = new ArrayList<>();

        for (int a = 0; a < attrNames.size(); a++) {
//            if (a == 0) {
//                selectedFor2dPosition.add(true);
//                selectedForCluster.add(false);
//            } else {
            String name = attrNames.get(a);
            selectedFor2dPosition.add(false);
            if (abnormalVarNames.containsKey(name)) {
                selectedForCluster.add(false);
            } else {
                selectedForCluster.add(true);
            }
            // build varBarChart
            VarBarChartForCell barChart = new VarBarChartForCell(name);
            distribDisplays.add(barChart);
//            }
        }

        fireTableDataChanged();
        //fireTableStructureChanged();
    }
    
    /**
     * same as updateAllRows, but only refreshes existing attrs, doesn't rebuild the attr lists or reset checkboxes
     */
    public void refreshAllRows() {
    	

//        attrNames = new ArrayList<>();
//        if (MainWindow.activeDataset != null) {
//            attrNames.addAll(((DatasetMedColonDoclist) MainWindow.activeDataset).getAllVarsAndIndis());
//        } else {
//            attrNames = new ArrayList<>();
//        }
        //attrNames.add(0, "<text>");

//        selectedFor2dPosition = new ArrayList<>();
//        selectedForCluster = new ArrayList<>();
        
        distribDisplays = new ArrayList<>();

        for (int a = 0; a < attrNames.size(); a++) {
//            if (a == 0) {
//                selectedFor2dPosition.add(true);
//                selectedForCluster.add(false);
//            } else {
            String name = attrNames.get(a);
//            selectedFor2dPosition.add(false);
            if (abnormalVarNames.containsKey(name)) {
//                selectedForCluster.add(false);
            } else {
//                selectedForCluster.add(true);
            }
            // build varBarChart
            VarBarChartForCell barChart = new VarBarChartForCell(name);
            distribDisplays.add(barChart);
//            }
        }

        fireTableDataChanged();
        //fireTableStructureChanged();
        
    }

    /**
     *
     *
     * @param attrName
     * @param val
     * @return
     */
    public static Object[] buildCellVizForAttr(String attrName) {

        String[] tempCellVals = new String[3];

        Map<String, Boolean> abnormalNameMap = getAbnormalNameMap();

//        if (!val.equals("-1") && !val.equals("0") && !val.equals("1")) {
//            System.err.println("DatasetVarsTableModel.buildCellVizForAttr: invalid val: "+val);
//            return new Integer(0);
//        }

        if (!attrName.equals("") && MainWindow.activeDataset != null && !abnormalNameMap.containsKey(attrName)) {
            // get scores for all docs
            //  assume that attrName can only be for one of the strict categorical attrs
            //  assume that docs are only of biomed type
            String[] vals = {"-1", "0", "1"};
            Map<String, Integer> valCountMap = new HashMap<>();
            for (String val : vals) {
                valCountMap.put(val, 0);
            }

            //int barWidth = clientWidth / vals.length;

            List<Document> allDocs = MainWindow.activeDataset.getDocuments();
            int totalDocCount = allDocs.size();

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
//            int totalVals = 0;
            int[] valCounts = new int[vals.length];
            for (int v = 0; v < vals.length; v++) {
                String val = vals[v];
                valCounts[v] = valCountMap.get(val);
//                totalVals += valCounts[v];
            }

            // draw the faint background bars for the whole dataset
//            Font labelFont = new Font("Book Antiqua", Font.PLAIN, 10);
//            FontMetrics labelFontMetrics = graphics.getFontMetrics(labelFont);
//
//            int labelHeight = 18;
//            for (int v = 0; v < vals.length; v++) {
//
//                int left = v * barWidth + 1;
//                int top = clientHeight - labelHeight;
//
////                int height = (int) (((double) valCounts[v] / (double) totalVals) * (clientHeight - labelHeight));
//                int height = (int) (((double) valCounts[v] / (double) totalDocCount) * (clientHeight - labelHeight));
//                height = -height;
//                int width = barWidth - 2;
//
//                // debug
//                //System.out.println("debug: VarBarChart: drawing box: " + left + ", " + (top+height) + ", " + width + ", " + (-height));
//
//                graphics.setColor(Color.gray);
//                graphics.fillRect(left, top+height, width, -height);
//                //graphics.setColor(Color.black);
//                //graphics.drawRect(left, top, width, height);
//
//                int q = clientHeight - labelFontMetrics.getDescent();
//                int labelWidth = labelFontMetrics.stringWidth(vals[v]);
//                int p = v * barWidth + (barWidth - labelWidth) / 2;
//                graphics.setColor(Color.black);
//                graphics.drawString(vals[v], p, q);
//
//            }


            // for testing only
            for (int v = 0; v < vals.length; v++) {
                tempCellVals[v] = (100 * (double) valCounts[v] / (double) totalDocCount) + "%";
            }


            // build separate filled-in portions for each cluster




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

                        // @TODO handle all clusters, not just the cluster of the selected doc
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

                        } else {
                            // in different cluster
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

//                // draw the foreground boxes for the selected cluster / group of docs
//                for (int v = 0; v < vals.length; v++) {
//                    
//                    //int left = v * barWidth + 4;
//                    int left = v * barWidth + 1;
//                    int top = clientHeight - labelHeight;
//
//                    //int height = (int) (((double) clusterValCounts[v] / (double) totalVals) * (clientHeight - labelHeight));
//                    int height = (int) (((double) clusterValCounts[v] / (double) totalDocCount) * (clientHeight - labelHeight));
//                    height = -height;
//                    //int width = barWidth - 8;
//                    int width = barWidth - 2;
//
//                    // debug
//                    //System.out.println("debug: VarBarChart: drawing box: " + left + ", " + (top+height) + ", " + width + ", " + (-height));
//
//                    graphics.setColor(Color.blue);
//                    //graphics.fillRect(left, top, width, height);
//                    graphics.fillRect(left, top+height, width, -height);
//                    //graphics.setColor(Color.black);
//                    //graphics.drawRect(left, top, width, height);
//
//
//                }

                // for testing only
                for (int v = 0; v < vals.length; v++) {
                    tempCellVals[v] += " (" + ( 100 * (double) clusterValCounts[v] / (double) totalDocCount) + "%)";
                }


            }

        }

        return tempCellVals;

    }
}
