package emr_vis_nlp.model;

import emr_vis_nlp.controller.MainController;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/**
 * Backing table model for the attribute details table in the document details
 * popup window. Should be responsive to attribute selections in the main views.
 *
 * general design: 1 row per (predicted) attribute, 3 cols: actual, predicted,
 * certainty
 *
 * Controller should maintain a list of all active DocDetailsTableModels, so
 * that they can all be updated on main window update.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocDetailsTableModel extends AbstractTableModel {

    /**
     * governing controller for this model
     */
    private MainController controller;
    
    /**
     * document which this table is representing
     */
    private Document thisDoc;
    /**
     * global index by which doc is referenced by the main model
     */
    private int thisDocGlobalIndex;
    /**
     * map for this document's attributes
     */
    private Map<String, String> docAttributeMap;
    /**
     * map containing back-end nlp predictions for this document
     */
    private Map<String, PredictionCertaintyTuple> attrPredictionMap;
    /**
     * list of all attributes
     */
    private List<String> allAttributes;
    /**
     * list of attributes to be displayed
     */
    private List<String> visibleAttributes;
    /**
     * indices from visible attributes back to backing model
     */
    private List<Integer> attributeIndices;
    /**
     * names of columns (nominally, will be 4: name, man val, pred val, pred
     * cert)
     */
    private List<String> colNames;

    public DocDetailsTableModel(MainController controller, Document thisDoc, int thisDocGlobalIndex, Map<String, PredictionCertaintyTuple> attrPredictionMap, List<String> allAttributes, List<Boolean> allAttributesEnabled) {
        this.controller = controller;
        this.thisDoc = thisDoc;
        this.thisDocGlobalIndex = thisDocGlobalIndex;
        this.attrPredictionMap = attrPredictionMap;
        this.allAttributes = allAttributes;
        docAttributeMap = thisDoc.getAttributes();

        visibleAttributes = new ArrayList<>();
        attributeIndices = new ArrayList<>();
        for (int a = 0; a < allAttributes.size(); a++) {
            String attribute = allAttributes.get(a);
            boolean isAttrEnabled = allAttributesEnabled.get(a);
            if (isAttrEnabled) {
                visibleAttributes.add(attribute);
                attributeIndices.add(a);
            }
        }

        colNames = new ArrayList<>();
        colNames.add("name");
        colNames.add("prediction");
        colNames.add("certainty");
        colNames.add("value");

    }

    @Override
    public int getRowCount() {
        if (visibleAttributes != null) {
            return visibleAttributes.size();
        }
        return 0;
    }

    @Override
    public int getColumnCount() {
        if (colNames != null) {
            return colNames.size();
        }
        return 0;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (colNames != null) {
            return colNames.get(columnIndex);
        }
        return "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        String name = visibleAttributes.get(rowIndex);
        switch (columnIndex) {
            case 0:
                // name
                return name;
//                break;
            case 1:
                // prediction
                if (attrPredictionMap.containsKey(name)) {
                    return attrPredictionMap.get(name).getValue();
                } else {
                    return "";
                }
//                break;
            case 2:
                // cert
                if (attrPredictionMap.containsKey(name)) {
                    return attrPredictionMap.get(name).getCert();
                } else {
                    return 0.;
                }
//                break;
            case 3:
                // value
                if (docAttributeMap.containsKey(name)) {
                    return docAttributeMap.get(name);
                } else {
                    return "";
                }
//                break;
            default:
                // shouldn't happen
                System.err.println("invalid column in DocDetailsTableModel: "+columnIndex);
                return "";
        }

    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                // name
                return false;
            case 1:
                // prediction
                return false;
            case 2:
                // cert
                return false;
            case 3:
                // value
                return true;
            default:
                // shouldn't happen
                System.err.println("invalid column in DocDetailsTableModel: "+columnIndex);
                return false;
        }
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                // name
                return String.class;
            case 1:
                // prediction
                return String.class;
            case 2:
                // cert
                return String.class;
            case 3:
                // value
                return String.class;
            default:
                // shouldn't happen
                System.err.println("invalid column in DocDetailsTableModel: "+columnIndex);
                return String.class;
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        
        String valueStr = (String)value;
        switch (columnIndex) {
            case 0:
                // name
                return;
            case 1:
                // prediction
                return;
            case 2:
                // cert
                return;
            case 3:
                // value
                String name = visibleAttributes.get(rowIndex);
//                controller.setValueForDocAttr(valueStr, thisDocGlobalIndex, name);
                synchronized (docAttributeMap) {
                    docAttributeMap.put(name, valueStr); // should not directly manipulate the document attr map in this class?
                }
                controller.documentAttributesUpdated(thisDocGlobalIndex);
                break;
            default:
                // shouldn't happen
                System.err.println("invalid column in DocDetailsTableModel: "+columnIndex);
                return;
        }
        
    }
    
    public int getGlobalAttrIndexForModelRow(int localIndex) {
        return attributeIndices.get(localIndex);
    }
    
}
