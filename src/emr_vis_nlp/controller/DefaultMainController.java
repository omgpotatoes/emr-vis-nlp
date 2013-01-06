
package emr_vis_nlp.controller;

import emr_vis_nlp.model.*;
import emr_vis_nlp.model.mpqa_colon.Document;
import emr_vis_nlp.view.DocFocusPopup;
import emr_vis_nlp.view.DocumentTreeMapView;
import emr_vis_nlp.view.MainView;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.table.TableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DefaultMainController implements MainController {
    
    private static MainController mainController = null;
    
    /** main model controlled by this */
    private MainModel model;
    /** main view controlled by this */
    private MainView view;
    /** list of active popup windows */
    private List<JFrame> activePopups;
    /** list of active popup window doc IDs, for convenience */
    private List<Integer> activePopupIDs;
    
    /** current treemap view component (if any) */
    private JComponent docTreeMapViewComponent = null;
    /** current treemap view attr selection model (if any) */
    private TableModel docTreeMapSelectionModel = null;
    
    public DefaultMainController() {
        activePopups = new ArrayList<>();
        activePopupIDs = new ArrayList<>();
        mainController = this;
    }
    
    @Override
    public void setModel(MainModel model) {
        this.model = model;
    }
    
    @Override
    public void setView(MainView view) {
        this.view = view;
    }
    
    @Override
    public void loadDoclist(File file) {
        
        // load new model from doclist
        // TODO add support for dataset types beyond MpqaColon
        model = new MpqaColonMainModel(this);
        model.loadDataFromDoclist(file);
        
        // update view
        // TODO
        view.resetAllViews();
        
    }
    
    /**
     * Filters documents according to presence of string.
     * 
     * @param str 
     */
    @Override
    public void applySimpleStringFilter(String str) {
        model.applySimpleStringFilter(str);
        // update components as appropriate
        // TODO
        view.resetAllViews();
        
    }
    
    /**
     * Supplies a new table model to the visualization, in the event that data 
     * is changed or a new filter is applied.
     * 
     * @return 
     */
    @Override
    public TableModel buildSimpleDocTableModel() {
        TableModel docTableModel = model.buildSimpleDocTableModel();
        return docTableModel;
    }
    
    /**
     * Supplies a new table model for the attribute selection table.
     * 
     * @return 
     */
    @Override
    public TableModel buildSimpleAttrSelectionTableModel() {
        TableModel attrSelectionTableModel = model.buildSimpleAttrSelectionTableModel();
        return attrSelectionTableModel;
    }
    
    @Override
    public TableModel buildSimpleAttrSelectionTableModelFocusOnly() {
        throw new UnsupportedOperationException("Not supported yet.");
//        TableModel attrSelectionTableModel = model.buildSimpleAttrSelectionTableModelFocusOnly();
//        return attrSelectionTableModel;
    }
    
    @Override
    public void attributeSelectionUpdated(List<Boolean> newSelectedAttributes) {
        // update selection in model
        model.setSelectedAttributes(newSelectedAttributes);
        
        // indicate to main view that all relevant displays should be redrawn / rebuilt
        view.attributeSelectionChanged();
        
        // rebuild table models for all active document popups
        for (JFrame popup : activePopups) {
            ((DocFocusPopup)popup).rebuildDocDetailsTable();
        }
        
    }
    
    @Override
    public JComponent buildDocTreeMapViewComponent() {
        
        // get selected attributes
        // FIXME provide proper ordering controls!
//        List<String> selectedAttrsForTree = new ArrayList<>();
//        List<String> allAttributes = model.getAllAttributes();
//        List<Boolean> allSelectedAttributes = model.getAllSelectedAttributes();
//        for (int a=0; a<allAttributes.size(); a++) {
//            String attr = allAttributes.get(a);
//            boolean isSelected = allSelectedAttributes.get(a);
//            if (isSelected) {
//                selectedAttrsForTree.add(attr);
//            }
//        }
        
        
        List<String> selectedAttrsForTree = new ArrayList<>();
        if (docTreeMapSelectionModel != null) {
            TreeMapSelectorTableModel treeMapSelectorTableModel = (TreeMapSelectorTableModel)docTreeMapSelectionModel;
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
    
    @Override
    public TableModel buildSimpleTreeMapSelectionTableModel() {
        
        TableModel newDocTreeMapSelectionTableModel = model.buildSimpleTreeMapSelectionTableModel();
        docTreeMapSelectionModel = newDocTreeMapSelectionTableModel;
        return docTreeMapSelectionModel;
    }

    @Override
    public TableModel buildAttrAndPredictionModelForDoc(int docGlobalId) {
        
        Document doc = model.getAllDocuments().get(docGlobalId);
        List<String> allAttributes = model.getAllAttributes();
        List<Boolean> allAttributesEnabled = model.getAllSelectedAttributes();
        
        // TODO proper integration with nlp back-end prediction models
        Map<String, PredictionCertaintyTuple> attrPredictionMap = new HashMap<>();
        
        TableModel docDetailsTableModel = new DocDetailsTableModel(this, doc, docGlobalId, attrPredictionMap, allAttributes, allAttributesEnabled);
        return docDetailsTableModel;
        
    }

    @Override
    public void writeDocTextWithHighlights(AbstractDocument abstDoc, int globalDocId, int globalAttrId) {
        
        try {
            abstDoc.remove(0, abstDoc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
            System.out.println("err: could not reset doc text for doc "+globalDocId+" focus window");
        }
        
        String docText = model.getAllDocuments().get(globalDocId).getText();
        
        if (globalAttrId != -1) {
            
            // TODO incorporate nlp back-end prediction models
            // for now, just return basic text
            int maxFontSize = 28;
            int minFontSize = 12;
            SimpleAttributeSet attrSet = new SimpleAttributeSet();
            StyleConstants.setFontSize(attrSet, minFontSize);
            try {
                abstDoc.insertString(abstDoc.getLength(), docText, attrSet);
            } catch (BadLocationException e) {
                e.printStackTrace();
                System.out.println("err: could not update doc text for doc " + globalDocId + " focus window");
            }

        } else {
            
            // write plaintext for doc
            int maxFontSize = 28;
            int minFontSize = 12;
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

    @Override
    public void documentAttributesUpdated(int docGlobalID) {
        
        // refresh all relevant tables
        view.attributeSelectionChanged();
        
        // rebuild table models for all active document popups
        for (JFrame popup : activePopups) {
            ((DocFocusPopup)popup).rebuildDocDetailsTable();
        }
        
    }
    
    @Override
    public JFrame buildDocDetailsWindow(int docGlobalID) {
        
        // TODO first check whether doc already has a popup
        
        Document doc = model.getAllDocuments().get(docGlobalID);
        JFrame popup = new DocFocusPopup(this, doc, docGlobalID);
        activePopups.add(popup);
        activePopupIDs.add(docGlobalID);
        return popup;
        
    }
    
    @Override
    public void displayDocDetailsWindow(int docGlobalID) {
        
        // if window is already open for doc, bring to front
        int docGlobalIdIndex = -1;
        for (int p=0; p<activePopupIDs.size(); p++) {
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
    
    @Override
    public boolean removeDocDetailsWindow(JFrame popup) {
        
        if (activePopups.contains(popup)) {
            int popupIndex = activePopups.indexOf(popup);
            activePopups.remove(popup);
            activePopupIDs.remove(popupIndex);
            return true;
        }
        
        return false;
        
    }
    
    @Override
    public void updateTreeMapAttributes() {
        
        // get selected attrs from the table model
        TreeMapSelectorTableModel treeMapSelectorTableModel = (TreeMapSelectorTableModel)docTreeMapSelectionModel;
        List<String> currentSelectedAttrs = treeMapSelectorTableModel.getSelectedAttributeList();
        
        // feed selected attrs to the treemap
        //DocumentTreeMapView.updatePanelWithNewTreemap(docTreeMapViewComponent, model.getAllDocuments(), model.getAllSelectedDocuments(), currentSelectedAttrs);
        view.orderedAttrSelectionChanged();
        
    }
    
}
