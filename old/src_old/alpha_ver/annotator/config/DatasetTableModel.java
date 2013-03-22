//
//package annotator.config;
//
//import annotator.MainWindow;
//import javax.swing.table.AbstractTableModel;
//
///**
// * Backing table model for the dataset selection pane.
// *
// * @author alexander.p.conrad@gmail.com
// */
//public class DatasetTableModel extends AbstractTableModel {
//
//    private String[] columnNames = {"dataset", "enabled?"};
//    
//    // row corresponds to position in list
//    //  if multiple datasets enabled, keep subtracting off previous list lens
//    //  to get to index into current list
//    
//    @Override
//    public int getRowCount() {
//        return MainWindow.activeDatasets.size();
//    }
//
//    @Override
//    public int getColumnCount() {
//        return columnNames.length;
//    }
//
//    @Override
//    public Object getValueAt(int rowIndex, int columnIndex) {
//        if (columnIndex == 0) {
//            return MainWindow.activeDatasets.get(rowIndex).getName();
//        } else if (columnIndex == 1) {
//            return MainWindow.activeDatasets.get(rowIndex).isActive();
//        } else {
//            // shouldn't happen
//            assert false;
//            return null;
//        }
//        
//    }
//    
//    @Override
//    public Class getColumnClass(int c) {
//        if (c == 0) {
//            return String.class;
//        } else if (c == 1) {
//            return Boolean.class;
//        } else {
//            // shouldn't happen
//            assert false;
//            return null;
//        }
//    }
//
//    @Override
//    public boolean isCellEditable(int row, int col) {
//        
//        // for now, leave name uneditable
//        if (col == 1) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public void setValueAt(Object value, int row, int col) {
//        
//        // update the back-end data
//        // only thing that should be editable is "enabled"
//        
//        if (col == 1) {
//            
//            // value should be of type Boolean
//            Boolean boolVal = (Boolean)value;
//            
//            boolean prevVal = MainWindow.activeDatasets.get(row).isActive();
//            // if value == false, then we need to delete all rows from 
//            //  documentTableModel corresponding to this dataset
//            //   note: only delete if dataset was previously enabled
//            if (prevVal == true) {
//                ((DocumentTableModel)MainWindow.documentTableModel).deleteRowsForDataset(row);
//            }
//            
//            MainWindow.activeDatasets.get(row).setActive(boolVal);
//            
//            
//            fireTableCellUpdated(row, col);
//            
//            
//            // now, update the document table
//            ((DocumentTableModel)MainWindow.documentTableModel).updateAllRows();
//            
//            
//        }
//    }
//    
//    public void updateAllRows() {
//        fireTableDataChanged();
//    }
//    
//}
//
