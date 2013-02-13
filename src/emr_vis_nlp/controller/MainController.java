package emr_vis_nlp.controller;

import emr_vis_nlp.view.DocDetailsTableModel;
import emr_vis_nlp.view.doc_map.TreeMapSelectorTableModel;
import emr_vis_nlp.view.doc_table.DocTableModel;
import emr_vis_nlp.view.doc_table.AttrTableModel;
import emr_vis_nlp.view.doc_grid.DocGridTableSelectorModel;
import emr_vis_nlp.model.*;
import emr_vis_nlp.view.*;
import emr_vis_nlp.view.doc_grid.DocumentGrid;
import emr_vis_nlp.view.doc_grid.DocumentGridTable;
import emr_vis_nlp.view.doc_map.DocumentTreeMapView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.table.TableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * Fulfills the Controller role of the MVC design pattern. Responsible for all
 * communication and coordination among the backing MainModel and the front-end
 * MainView.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class MainController {

    private static MainController mainController = null;
    /**
     * main model controlled by this
     */
    private MainModel model;
    /**
     * main view controlled by this
     */
    private MainView view;
    /**
     * list of active popup windows
     */
    private List<JFrame> activePopups;
    /**
     * list of active popup window doc IDs, for convenience
     */
    private List<Integer> activePopupIDs;
    /**
     * current treemap view component (if any)
     */
    private JComponent docTreeMapViewComponent = null;
    /**
     * current treemap view attr selection model (if any)
     */
    private TableModel docTreeMapSelectionModel = null;
    /**
     * current doc grid view attr selection model (if any)
     */
    private DocGridTableSelectorModel docGridSelectionModel = null;
    /**
     * current document grid component (if any)
     */
    private DocumentGrid documentGrid = null;
    /**
     * backing table for current document grid (if any)
     */
    DocumentGridTable documentGridTable = null;

    public MainController() {
        activePopups = new ArrayList<>();
        activePopupIDs = new ArrayList<>();
        mainController = this;
    }

    public void setModel(MainModel model) {
        this.model = model;
    }

    public void setView(MainView view) {
        this.view = view;
    }

    public void loadDoclist(File file) {

        // load new model from doclist
        // TODO add support for dataset types beyond MpqaColon
        model = new MpqaColonMainModel(this);
        ((MpqaColonMainModel) model).loadDataFromDoclist(file);

        // update view
        // TODO
        view.resetAllViews();

    }

    /**
     * Filters documents according to presence of string.
     *
     * @param str
     */
    public void applySimpleStringFilter(String str) {
        throw new UnsupportedOperationException();  // TODO
//        model.applySimpleStringFilter(str);  // no longer part of model?
        // update components as appropriate
//        view.resetAllViews();

    }

    /**
     * Supplies a new table model to the visualization, in the event that data
     * is changed or a new filter is applied.
     *
     * @return
     */
    public TableModel buildSimpleDocTableModel() {
        DocTableModel docTableModel = new DocTableModel(model.getAllDocuments(), model.getAllSelectedDocuments(), model.getAllAttributes(), model.getAllSelectedAttributes());
//        TableModel docTableModel = model.buildSimpleDocTableModel();
        return docTableModel;
    }

    /**
     * Supplies a new table model for the attribute selection table.
     *
     * @return
     */
    public AttrTableModel buildSimpleAttrSelectionTableModel() {
        AttrTableModel attrTableModel = new AttrTableModel(model.getAllAttributes(), model.getAllSelectedAttributes(), this);
//        TableModel attrSelectionTableModel = model.buildSimpleAttrSelectionTableModel();
        return attrTableModel;
    }

    public TableModel buildSimpleAttrSelectionTableModelFocusOnly() {
        throw new UnsupportedOperationException("Not supported yet.");
//        TableModel attrSelectionTableModel = model.buildSimpleAttrSelectionTableModelFocusOnly();
//        return attrSelectionTableModel;
    }

    public void attributeSelectionUpdated(List<Boolean> newSelectedAttributes) {
        // update selection in model
        model.setSelectedAttributes(newSelectedAttributes);

        // indicate to main view that all relevant displays should be redrawn / rebuilt
        view.attributeSelectionChanged();

        // rebuild table models for all active document popups
        for (JFrame popup : activePopups) {
            ((DocFocusPopup) popup).rebuildDocDetailsTable();
        }

    }

    public JComponent buildDocTreeMapViewComponent() {

        // get selected attributes from interface
        List<String> selectedAttrsForTree = new ArrayList<>();
        if (docTreeMapSelectionModel != null) {
            TreeMapSelectorTableModel treeMapSelectorTableModel = (TreeMapSelectorTableModel) docTreeMapSelectionModel;
            selectedAttrsForTree = treeMapSelectorTableModel.getSelectedAttributeList();
        } else {
            selectedAttrsForTree.add("Indicator_1");
            selectedAttrsForTree.add("Indicator_21");
            selectedAttrsForTree.add("Indicator_19");

        }

//        JComponent docTreeMapViewComponent = DocumentTreeMapView.buildNewTreeMapComponent(model.getAllDocuments(), model.getAllSelectedDocuments(), selectedAttrsForTree);
        JComponent newDocTreeMapViewComponent = DocumentTreeMapView.buildNewTreeMapOnly(this, model.getAllDocuments(), model.getAllSelectedDocuments(), selectedAttrsForTree);
        docTreeMapViewComponent = newDocTreeMapViewComponent;
        return docTreeMapViewComponent;

    }

    public TableModel buildSimpleTreeMapSelectionTableModel() {
        TreeMapSelectorTableModel newDocTreeMapSelectionTableModel = new TreeMapSelectorTableModel(model.getAllAttributes(), this);
//        TableModel newDocTreeMapSelectionTableModel = model.buildSimpleTreeMapSelectionTableModel();
        docTreeMapSelectionModel = newDocTreeMapSelectionTableModel;
        return docTreeMapSelectionModel;
    }

    public TableModel buildAttrAndPredictionModelForDoc(int docGlobalId) {

        Document doc = model.getAllDocuments().get(docGlobalId);
        List<String> allAttributes = model.getAllAttributes();
        List<Boolean> allAttributesEnabled = model.getAllSelectedAttributes();

        Map<String, PredictionCertaintyTuple> attrPredictionMap = model.getPredictionsForDoc(docGlobalId);

        TableModel docDetailsTableModel = new DocDetailsTableModel(this, doc, docGlobalId, attrPredictionMap, allAttributes, allAttributesEnabled);
        return docDetailsTableModel;

    }

    public void writeDocTextWithHighlights(AbstractDocument abstDoc, int globalDocId, String attrName) {
        
        // map attrName to int
        if (model != null) {
            List<String> attrs = model.getAllAttributes();
            int attrIndex = -1;
            for (int a=0; a<attrs.size(); a++) {
                if (attrs.get(a).equals(attrName)) {
                    attrIndex = a;
                    break;
                }
            }
            
            if (attrIndex != -1) {
                writeDocTextWithHighlights(abstDoc, globalDocId, attrIndex);
            } else {
                System.err.println("MainController.writeDocTextWithHighlights: could not find index for attribute: "+attrName);
            }
            
        }
        
    }
    
    
    public void writeDocTextWithHighlights(AbstractDocument abstDoc, int globalDocId, int globalAttrId) {

        if (model != null) {

            // delegate this to the model, if possible
            if (globalAttrId != -1 && model.canWriteDocTextWithHighlights(globalDocId, globalAttrId)) {
                model.writeDocTextWithHighlights(abstDoc, globalDocId, globalAttrId);
            } else {

                // if model doesn't support highlighting for this text, just insert plaintext
                try {
                    abstDoc.remove(0, abstDoc.getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                    System.out.println("err: could not reset doc text for doc " + globalDocId + " focus window");
                }

                int maxFontSize = 32;
                int minFontSize = 12;

                String docText = model.getAllDocuments().get(globalDocId).getText();
                SimpleAttributeSet attrSet = new SimpleAttributeSet();
                StyleConstants.setFontSize(attrSet, minFontSize);
                try {
                    abstDoc.insertString(abstDoc.getLength(), docText, attrSet);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                    System.out.println("err: could not update doc text for doc " + globalDocId + " focus window");
                }

            }

        }

    }

    public void documentAttributesUpdated(int docGlobalID) {

        // refresh all relevant tables
        view.attributeSelectionChanged();

        // rebuild table models for all active document popups
        for (JFrame popup : activePopups) {
            ((DocFocusPopup) popup).rebuildDocDetailsTable();
        }

    }

    public JFrame buildDocDetailsWindow(int docGlobalID) {

        // TODO first check whether doc already has a popup

        Document doc = model.getAllDocuments().get(docGlobalID);
        JFrame popup = new DocFocusPopup(this, doc, docGlobalID);
        activePopups.add(popup);
        activePopupIDs.add(docGlobalID);
        return popup;

    }

    public void displayDocDetailsWindow(int docGlobalID) {

        // if window is already open for doc, bring to front
        int docGlobalIdIndex = -1;
        for (int p = 0; p < activePopupIDs.size(); p++) {
            if (activePopupIDs.get(p) == docGlobalID) {
                docGlobalIdIndex = p;
                break;
            }
        }

        if (docGlobalIdIndex != -1) {
            JFrame popup = activePopups.get(docGlobalIdIndex);
            popup.toFront();
        } else {
            // else, popup new one
            final JFrame newPopup = buildDocDetailsWindow(docGlobalID);
            java.awt.EventQueue.invokeLater(new Runnable() {

                public void run() {
                    newPopup.setVisible(true);
                }
            });
        }

    }

    public boolean removeDocDetailsWindow(JFrame popup) {

        if (activePopups.contains(popup)) {
            int popupIndex = activePopups.indexOf(popup);
            activePopups.remove(popup);
            activePopupIDs.remove(popupIndex);
            return true;
        }

        return false;

    }

    public void updateTreeMapAttributes() {

        // get selected attrs from the table model
        TreeMapSelectorTableModel treeMapSelectorTableModel = (TreeMapSelectorTableModel) docTreeMapSelectionModel;
        List<String> currentSelectedAttrs = treeMapSelectorTableModel.getSelectedAttributeList();

        // feed selected attrs to the treemap
        //DocumentTreeMapView.updatePanelWithNewTreemap(docTreeMapViewComponent, model.getAllDocuments(), model.getAllSelectedDocuments(), currentSelectedAttrs);
        view.orderedAttrSelectionChanged();

    }

    public DocumentGrid buildDocumentGrid() {

        String xAxisAttrName = "Indicator_9";
        String yAxisAttrName = "Indicator_21";
        String shapeAttrName = "";
        String colorAttrName = "";
        if (docGridSelectionModel != null) {
            xAxisAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getXAxisAttribute();
            yAxisAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getYAxisAttribute();
            shapeAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getShapeAttribute();
            colorAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getColorAttribute();
        }

        // build table for grid
        documentGridTable = new DocumentGridTable(model.getAllAttributes(), model.getAllDocuments(), model.getAllSelectedDocuments());
        // build, return grid (while maintaining reference)
        documentGrid = new DocumentGrid(this, documentGridTable, xAxisAttrName, yAxisAttrName, shapeAttrName, colorAttrName);
        return documentGrid;

    }

    /**
     * similar to buildDocumentGrid(), but rather than rebuilding the grid from
     * scratch, simply updates the attributes by which the grid is laid out.
     *
     * @return
     */
    public void updateDocumentGrid() {

        String xAxisAttrName = "Indicator_9";
        String yAxisAttrName = "Indicator_21";
        String shapeAttrName = "";
        String colorAttrName = "";
        if (docGridSelectionModel != null) {
            xAxisAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getXAxisAttribute();
            yAxisAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getYAxisAttribute();
            shapeAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getShapeAttribute();
            colorAttrName = ((DocGridTableSelectorModel) docGridSelectionModel).getColorAttribute();
        }

        // build table for grid
        if (documentGrid != null) {
            documentGrid.updateXAxis(xAxisAttrName);
            documentGrid.updateYAxis(yAxisAttrName);
            documentGrid.updateShapeAttr(shapeAttrName);
            documentGrid.updateColorAttr(colorAttrName);
        }

    }

    public void updateDocGridAttributes() {

        // get selected attrs for x, y axes from tablemodel
        DocGridTableSelectorModel gridSelectorTableModel = (DocGridTableSelectorModel) docGridSelectionModel;
        String xAxisAttr = gridSelectorTableModel.getXAxisAttribute();
        String yAxisAttr = gridSelectorTableModel.getYAxisAttribute();
        String shapeAttr = gridSelectorTableModel.getShapeAttribute();
        String colorAttr = gridSelectorTableModel.getColorAttribute();

        view.axisAttrSelectionChanged();

    }

    public DocGridTableSelectorModel buildSimpleDocGridSelectionTableModel() {
        DocGridTableSelectorModel newDocGridTableModel = new DocGridTableSelectorModel(model.getAllAttributes(), this);
//        TableModel newDocGridSelectionTableModel = model.buildSimpleDocGridSelectionTableModel();
        docGridSelectionModel = newDocGridTableModel;
        return docGridSelectionModel;
    }

    /**
     * Builds a VarBarChartForCell display for a given attribute. Returned
     * display is meant to be used as cell in table.
     *
     * @param attrName
     * @return
     */
    public VarBarChartForCell getVarBarChartForCell(String attrName) {
        VarBarChartForCell varBarChart = new VarBarChartForCell(this, attrName, model.getAllDocuments());
        return varBarChart;
    }

    public void updateDocumentAttr(int docID, String docAttr, String docAttrVal) {
        // update value in model
        model.updateDocumentAttr(docID, docAttr, docAttrVal);
        // update value in applicable visual tables
        documentGridTable.setString(docID, docAttr, docAttrVal);
    }

    public void resetDocGridView() {
        if (documentGrid != null) {
            documentGrid.resetView();
        }
    }

    public boolean hasPrediction(int globalDocId, String attrName) {
        return model.hasPrediction(globalDocId, attrName);
    }

    public PredictionCertaintyTuple getPrediction(int globalDocId, String attrName) {
        return model.getPrediction(globalDocId, attrName);
    }

    /**
     * Highlights documents matching attribute value criteria in applicable
     * views.
     *
     * @param attrName
     * @param attrValue
     */
    public void highlightDocsWithAttrVal(String attrName, String attrValue) {
        // TODO apply to more general case
        // for now, assume only a single highlight set at a time
        documentGrid.setHighlightPredicate(attrName, attrValue);
    }
    
    /**
     * Unhighlights all documents in all applicable views.
     * 
     */
    public void unhighlightAllDocs() {
        documentGrid.resetHighlightPredicate();
    }
    
    public MainViewGlassPane getGlassPane() {
        if (view != null) {
            return view.getGlassPane();
        }
        return null;
    }
    
}
