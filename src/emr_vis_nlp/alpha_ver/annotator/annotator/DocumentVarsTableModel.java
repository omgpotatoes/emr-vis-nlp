package annotator.annotator;

import annotator.MainWindow;
import annotator.backend.RuntimeIndicatorPrediction;
import annotator.data.DatasetMedColonDoclist;
import annotator.data.Document;
import annotator.data.DocumentMedColon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

/**
 * Backing table model for selected document's vars.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocumentVarsTableModel extends AbstractTableModel {
    
    private String[] columnNames = {"attribute", "value", "prediction", "certainty"};
    
    private int[] predictedIndicatorScores = null;
    private double[] predictedIndicatorCerts = null;
    
    private Map<Integer, Integer> attrIndexToPredictionMap = null;
    
    public DocumentVarsTableModel() {
        super();
        predictedIndicatorScores = new int[0];
        predictedIndicatorCerts = new double[0];
        attrIndexToPredictionMap = new HashMap<>();
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
        
        if (MainWindow.activeDataset != null && MainWindow.activeDataset.getType().equals(MainWindow.activeDataset.DATASET_TYPE_COLON)) {
//            List<String> allVarsAndIndis = ((DatasetMedColonDoclist) MainWindow.activeDataset).getAllVarsAndIndis();
            List<String> allVarsAndIndis = ((DatasetMedColonDoclist) MainWindow.activeDataset).getSelectedVarsAndIndis();
            return allVarsAndIndis.size();
        }
        return 0;
        
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        
        if (MainWindow.activeDataset.getType().equals(MainWindow.activeDataset.DATASET_TYPE_COLON)) {
//            List<String> allVarsAndIndis = ((DatasetMedColonDoclist) MainWindow.activeDataset).getAllVarsAndIndis();
        	List<String> allVarsAndIndis = ((DatasetMedColonDoclist) MainWindow.activeDataset).getSelectedVarsAndIndis();
            if (rowIndex != -1) {
                String key = allVarsAndIndis.get(rowIndex);
                if (columnIndex == 0) {
                    // name of attr
                    return key;
                } else if (columnIndex == 1) {
                    // current value of attr
                    // look it up in current document
                    Document selectedDocument = MainWindow.window.getSelectedDocument();
                    if (selectedDocument != null) {
                        DocumentMedColon medDoc = (DocumentMedColon) selectedDocument;
                        if (medDoc.getIndicators().containsKey(key)) {
                            return medDoc.getIndicators().get(key);
                        } else if (medDoc.getVars().containsKey(key)) {
                            return medDoc.getVars().get(key);
                        } 
                    } 
                } else if (columnIndex == 2) {
                    // predicted value (if applicable)
                    if (attrIndexToPredictionMap.containsKey(rowIndex)) {
                    	return ""+predictedIndicatorScores[attrIndexToPredictionMap.get(rowIndex)];
                    }
                } else if (columnIndex == 3) {
                    // certainty score (if applicable)
                    if (attrIndexToPredictionMap.containsKey(rowIndex)) {
                    	return ""+predictedIndicatorCerts[attrIndexToPredictionMap.get(rowIndex)];
                    }
                }
            } 
        }
        
        return "";
        
        
    }
    
    public String getSelectedAttr(int rowIndex) {
        
        if (MainWindow.activeDataset.getType().equals(MainWindow.activeDataset.DATASET_TYPE_COLON)) {
//            List<String> allVarsAndIndis = ((DatasetMedColonDoclist) MainWindow.activeDataset).getAllVarsAndIndis();
        	List<String> allVarsAndIndis = ((DatasetMedColonDoclist) MainWindow.activeDataset).getSelectedVarsAndIndis();
            if (rowIndex != -1) {
                //String key = allVarsAndIndis.get(rowIndex);
                if (attrIndexToPredictionMap.containsKey(rowIndex)) {
                    int attrIndex = attrIndexToPredictionMap.get(rowIndex);
                    String attrName = RuntimeIndicatorPrediction.predictedIndicatorNames[attrIndex];
                    
                    return attrName;
                } else {
                	// isn't in prediction map, so just return raw string?
                	// (couldn't we just do this in the general case anyway?)
                	return allVarsAndIndis.get(rowIndex);
                	
                }
            }
        }
        return "";
        
    }
    
    @Override
    public boolean isCellEditable(int row, int col) {

        // for now, leave everything uneditable except for value
        // nevermind
        if (col == 1) {
            return true;
        }
        return false;
        
    }
    
    @Override
    public Class getColumnClass(int c) {
        
//        if (c == 2) {
//            return Double.class;
//        }
        
        return String.class;
    }
    
    @Override
    public void setValueAt(Object value, int row, int col) {

        // @TODO make this more robust, do proper type checking
        if (col == 1) {
//            String key = ((DatasetMedColonDoclist) MainWindow.activeDataset).getAllVarsAndIndis().get(row);
            String key = ((DatasetMedColonDoclist) MainWindow.activeDataset).getSelectedVarsAndIndis().get(row);
            DocumentMedColon doc = (DocumentMedColon) MainWindow.window.getSelectedDocument();
            doc.getVars().put(key, value + "");
            doc.getIndicators().put(key, Integer.parseInt(value + ""));
        }
        
    }
    
    
    public void updateAllRows() {
        
    	// rebuild map of predicted indicators
    	rebuildAttrIndexToPredictionMap();
    	
        // reload the predicted scores for the selected document
    	String parsedText = MainWindow.window.getSelectedDocument().getParsedText();
        RuntimeIndicatorPrediction.buildTemporaryFileForText(parsedText);
        // d1 = indicator; d2 = probs. for each val [-1, 0, 1]
        double[][] predictions = RuntimeIndicatorPrediction.predictIndicatorsForTempFile();
        predictedIndicatorScores = new int[predictions.length];
        predictedIndicatorCerts = new double[predictions.length];
        // find largest value for each indicator; store value and certainty
        for (int p=0; p<predictions.length; p++) {
        	double negOneVal = predictions[p][0];
        	double zeroVal = predictions[p][1];
        	double oneVal = predictions[p][2];
        	
        	if (negOneVal >= zeroVal && negOneVal >= oneVal) {
        		predictedIndicatorScores[p] = -1;
        		predictedIndicatorCerts[p] = negOneVal;
        	} else if (zeroVal >= negOneVal && zeroVal >= oneVal) {
        		predictedIndicatorScores[p] = 0;
        		predictedIndicatorCerts[p] = zeroVal;
        	} else {
        		predictedIndicatorScores[p] = 1;
        		predictedIndicatorCerts[p] = oneVal;
        	}
        }
        
        fireTableDataChanged();
        //fireTableStructureChanged();
    }
    
    public void rebuildAttrIndexToPredictionMap() {
        
        //  map:  -> row index in table
    	attrIndexToPredictionMap = new HashMap<>();
//    	List<String> allVarsAndIndis = ((DatasetMedColonDoclist) MainWindow.activeDataset).getAllVarsAndIndis();
    	List<String> allVarsAndIndis = ((DatasetMedColonDoclist) MainWindow.activeDataset).getSelectedVarsAndIndis();
    	String[] predictedIndicatorNames = RuntimeIndicatorPrediction.predictedIndicatorNames;
    	for (int n=0; n<predictedIndicatorNames.length; n++) {
    		String predictedName = predictedIndicatorNames[n];
    		boolean foundMatch = false;
    		for (int a=0; a<allVarsAndIndis.size(); a++) {
    			String attrName = allVarsAndIndis.get(a);
    			if (predictedName.equalsIgnoreCase(attrName)) {
    				attrIndexToPredictionMap.put(a, n);
    				foundMatch = true;
    				break;
    			}
    		}
    		if (!foundMatch) {
    			System.err.println("DocumentVarsTableModel: could not find predicted indicator \""+predictedName+"\" in attribute list!");
    			assert false;
    		}
    	}
        
    }
    
}
