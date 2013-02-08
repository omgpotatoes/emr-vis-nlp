
package annotator.annotator;

import annotator.MainWindow;
import annotator.data.DataTagStanceStruct;
import annotator.data.TextInstance;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Backing model for the list of text instances.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class TextInstanceTableModel extends AbstractTableModel {

    //private String[] columnNames = {"text unit", "label"};
    private String[] columnNames;
    
    public TextInstanceTableModel() {
        super();
        columnNames = new String[2];
        columnNames[0] = "text";
        columnNames[1] = "attributes";
    }
    
    public void resetColumnNames() {
        
        if (MainWindow.activeDataset != null) {
            List<String> newColumnNames = ((DataTagStanceStruct)MainWindow.activeDataset.getTagset().getTopLevelTag()).getTaglistNames();
            columnNames = new String[newColumnNames.size()+1];
            columnNames[0] = "text";
            for (int i=0; i<newColumnNames.size(); i++) {
                columnNames[i+1] = newColumnNames.get(i);
            }
        }
        fireTableStructureChanged();
        
    }
    
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    @Override
    public int getRowCount() {
        
        if (MainWindow.activeDataset != null && MainWindow.activeDataset.getDocuments() != null && MainWindow.selectedDocumentIndex != -1) {
            return MainWindow.activeDataset.getDocuments().get(MainWindow.selectedDocumentIndex).getTextInstances().size();
        } return 0;
        
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        
        if (rowIndex != -1) {
            TextInstance textInstance = MainWindow.activeDataset.getDocuments().get(MainWindow.selectedDocumentIndex).getTextInstances().get(rowIndex);
            if (columnIndex == 0) {
                return textInstance.getTextStr();
            } else if (columnIndex > 0) {
                if (textInstance.getAttributes().containsKey(columnNames[columnIndex])) {
                    return textInstance.getAttributes().get(columnNames[columnIndex]);
                } else {
                    return "";
                }
            } else {
                // shouldn't happen
                assert false;
                return null;
            }
        } else {
            return "";
        }
        

    }

    @Override
    public Class getColumnClass(int c) {
//        if (c == 0) {
//            return String.class;
//        } else if (c == 1) {
//            return String.class;
//        } else {
//            // shouldn't happen
//            assert false;
//            return null;
//        }
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {

//        // for now, leave name uneditable
//        if (col == 1) {
//            return false;
//        } else {
//            return false;
//        }
        
        return false;
        
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        
//        if (row != -1) {
//            TextInstance textInstance = MainWindow.activeDataset.getDocuments().get(MainWindow.selectedDocumentIndex).getTextInstances().get(row);
//            if (col == 0) {
//                textInstance.setTextStr((String) value);
//            } else if (col == 1) {
//                textInstance.getAttributes().put("argument", (String)value);
//            } else {
//                // shouldn't happen
//                assert false;
//            }
//        }
//        
//        fireTableCellUpdated(row, col);
        
        // disable direct cell editing for now
        
    }
    
//    public void deleteRowsForDataset(int datasetRow) {
//        // identify and remove all rows corresponding to this dataset
//        Dataset currentDataset = activeDatasets.get(0);
//        int datasetCounter = 0;
//        int previousRowCounter = 0;
//        while (datasetCounter < datasetRow) {
//            if (currentDataset.isActive()) {
//                previousRowCounter += currentDataset.getDocuments().size();
//            }
//            datasetCounter++;
//            currentDataset = activeDatasets.get(datasetCounter);
//        }
//        
//        int startRow = previousRowCounter;
//        int endRow = previousRowCounter + currentDataset.getDocuments().size();
//        
//        fireTableRowsDeleted(startRow, endRow);
//
//    }
    
    public void updateAllRows() {
        
        fireTableDataChanged();
        //fireTableStructureChanged();
    }
    
}
