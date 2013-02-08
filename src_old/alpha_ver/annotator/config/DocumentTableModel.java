package annotator.config;

import annotator.MainWindow;
import annotator.data.Dataset;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Backing table model for the document selection pane.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocumentTableModel extends AbstractTableModel {

    private String[] columnNames = {"document unit", "active?"};

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    @Override
    public int getRowCount() {
//        int rowCounter = 0;
//        for (int d = 0; d < activeDatasets.size(); d++) {
//            rowCounter += activeDatasets.get(d).getDocuments().size();
//        }
//        return rowCounter;
        
        if (MainWindow.activeDataset != null) {
            return MainWindow.activeDataset.getDocuments().size();
        } return 0;
        
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        
//        // @TODO fix the dataset table
//        // WARNING: deselcting a dataset doesn't work!
//        //  for now, just disabling the dataset table, it's not too important
//        
//        if (activeDatasets.size() > 0) {
//            // loop through the datasets
//            Dataset currentDataset = activeDatasets.get(0);
//            int datasetCounter = 0;
//            int previousRowCounter = 0;
//            while (true) {
//                if (currentDataset.isActive()) {
//                    if (rowIndex - previousRowCounter < currentDataset.getDocuments().size()) {
//                        if (columnIndex == 0) {
//                            return currentDataset.getDocuments().get(rowIndex - previousRowCounter).getName();
//                        } else if (columnIndex == 1) {
//                            return currentDataset.getDocuments().get(rowIndex - previousRowCounter).isActive();
//                        } else {
//                            // shouldn't happen
//                            assert false;
//                            return null;
//                        }
//                    } else {
//                        // otherwise, must be in a later dataset
//                        previousRowCounter += currentDataset.getDocuments().size();
//                        datasetCounter++;
//                        currentDataset = activeDatasets.get(datasetCounter);
//                    }
//                } else {
//                    datasetCounter++;
//                    try {
//                        currentDataset = activeDatasets.get(datasetCounter);
//                    } catch (IndexOutOfBoundsException e) {
//                        return null;
//                    }
//                }
//            }
//        } else {
//            // datasets are empty, so this shouldn't be called
//            assert false;
//            return null;
//        }
        
        if (columnIndex == 0) {
            return MainWindow.activeDataset.getDocuments().get(rowIndex).getName();
        } else if (columnIndex == 1) {
            return MainWindow.activeDataset.getDocuments().get(rowIndex).isActive();
        } else {
            // shouldn't happen
            assert false;
            return null;
        }
        

    }

    @Override
    public Class getColumnClass(int c) {
        if (c == 0) {
            return String.class;
        } else if (c == 1) {
            return Boolean.class;
        } else {
            // shouldn't happen
            assert false;
            return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {

        // for now, leave name uneditable
        if (col == 1) {
            //return true;
            return false;
        } else {
            return false;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {

//        // update the back-end data
//        // only thing that should be editable is "enabled"
//
//        if (activeDatasets.size() > 0) {
//            // loop through the datasets
//            Dataset currentDataset = activeDatasets.get(0);
//            int datasetCounter = 0;
//            int previousRowCounter = 0;
//            while (true) {
//                if (currentDataset.isActive()) {
//                    if (row - previousRowCounter < currentDataset.getDocuments().size()) {
//                        if (col == 0) {
//                            currentDataset.getDocuments().get(row - previousRowCounter).setName((String) value);
//                        } else if (col == 1) {
//                            currentDataset.getDocuments().get(row - previousRowCounter).setActive((Boolean) value);
//                        } else {
//                            // shouldn't happen
//                            assert false;
//                        }
//                    } else {
//                        // otherwise, must be in a later dataset
//                        previousRowCounter += currentDataset.getDocuments().size();
//                        datasetCounter++;
//                        currentDataset = activeDatasets.get(datasetCounter);
//                    }
//                } else {
//                    datasetCounter++;
//                    try {
//                        currentDataset = activeDatasets.get(datasetCounter);
//                    } catch (IndexOutOfBoundsException e) {
//                        //return null;
//                        assert false;
//                        break;
//                    }
//                }
//            }
//        } else {
//            // datasets are empty, so this shouldn't be called
//            assert false;
//        }
//
//        fireTableCellUpdated(row, col);

        if (col == 0) {
            MainWindow.activeDataset.getDocuments().get(row).setName((String) value);
        } else if (col == 1) {
            MainWindow.activeDataset.getDocuments().get(row).setActive((Boolean) value);
        } else {
            // shouldn't happen
            assert false;
        }
        
        fireTableCellUpdated(row, col);
        
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
