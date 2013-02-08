package annotator.visualizer;

import annotator.MainWindow;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Backing table model for docs in currently selected cluster
 *
 * @author alexander.p.conrad@gmail.com
 */
public class ClusterDocsTableModel extends AbstractTableModel {

    private String[] columnNames = {"name"};
    private List<String> docNames;
    private List<Integer> docIndices;

    public ClusterDocsTableModel() {
        super();

        if (MainWindow.activeDataset != null) {
            docNames = MainWindow.window.getCurrentClusterDocNames();
            docIndices = MainWindow.window.getCurrentClusterDocIndices();
        } else {
            docNames = new ArrayList<>();
            docIndices = new ArrayList<>();
        }

    }

    @Override
    public int getRowCount() {
        return docNames.size();
    }
    
    @Override
    public Class getColumnClass(int c) {

        if (c == 0) {
            return String.class;
        }

        return null;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        if (columnIndex == 0) {
            return docNames.get(rowIndex);
        }

        return null;

    }
    
    @Override
    public boolean isCellEditable(int row, int col) {

        return false;

    }
    
    public int translateTableIndexToGlobalIndex(int tableIndex) {
        return docIndices.get(tableIndex);
    }
    
    public void updateAllRows() {
        
        if (MainWindow.activeDataset != null) {
            docNames = MainWindow.window.getCurrentClusterDocNames();
            docIndices = MainWindow.window.getCurrentClusterDocIndices();
        } else {
            docNames = new ArrayList<>();
            docIndices = new ArrayList<>();
        }
        
        fireTableDataChanged();
        
    }
    
}
