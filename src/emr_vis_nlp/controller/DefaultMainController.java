package emr_vis_nlp.controller;

import emr_vis_nlp.model.*;
import emr_vis_nlp.model.mpqa_colon.Document;
import emr_vis_nlp.view.DocFocusPopup;
import emr_vis_nlp.view.doc_map.DocumentTreeMapView;
import emr_vis_nlp.view.MainView;
import java.awt.Color;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            ((DocFocusPopup) popup).rebuildDocDetailsTable();
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
            System.out.println("err: could not reset doc text for doc " + globalDocId + " focus window");
        }

        String docText = model.getAllDocuments().get(globalDocId).getText();

        
        
        if (globalAttrId != -1) {

            int maxFontSize = 28;
            int minFontSize = 12;
            
            // TODO incorporate nlp back-end prediction models
            // for now, just return basic text
            SimpleAttributeSet attrSet = new SimpleAttributeSet();
            StyleConstants.setFontSize(attrSet, minFontSize);
            try {
                abstDoc.insertString(abstDoc.getLength(), docText, attrSet);
            } catch (BadLocationException e) {
                e.printStackTrace();
                System.out.println("err: could not update doc text for doc " + globalDocId + " focus window");
            }








//            // from annotator.MainWindow: (code to adapt:)
//            
//            // if feature is a regexp
//            if (isSimpleSQVar) {
//                // highlight regexp
//                Pattern varRegExpPattern = Pattern.compile(simpleSQVarRegExpStr);
//                Matcher varRegExpMatcher = varRegExpPattern.matcher(docText);
//                boolean hasPattern = varRegExpMatcher.find();
//
//
//                // strategy: while hasPattern == true, continue to look for matches; 
//                //  store start, end match indices in a list
//                List<Integer> startIndices = new ArrayList<>();
//                List<Integer> endIndices = new ArrayList<>();
//
//                while (hasPattern) {
//
//                    int start = varRegExpMatcher.start();
//                    int end = varRegExpMatcher.end();
//                    String matchedSubstring = varRegExpMatcher.group();
//
//                    // debug
//                    System.out.println("debug: found match in doc " + activeDataset.getDocuments().get(selectedDocumentIndex).getName() + " for attr " + selectedAttr + ": \"" + matchedSubstring + "\"");
//
//                    startIndices.add(start);
//                    endIndices.add(end);
//
//                    hasPattern = varRegExpMatcher.find();
//                }
//
//                int lastEndIndex = 0;
//                if (startIndices.size() > 0) {
//
//                    while (startIndices.size() > 0) {
//
//                        // iterate through indices, writing the previous unmatched
//                        // portion and following matched portion
//
//                        int plainIndexStart = lastEndIndex;
//                        int plainIndexEnd = startIndices.remove(0);
//                        int matchedIndexEnd = endIndices.remove(0);
//
//                        // unmatched
//                        try {
//                            int fontSize = minFontSize;
//                            SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                            StyleConstants.setFontSize(attrSet, fontSize);
//                            abstDoc.insertString(abstDoc.getLength(), docText.substring(plainIndexStart, plainIndexEnd),
//                                    attrSet);
//                        } catch (BadLocationException e) {
//                            e.printStackTrace();
//                            System.out.println("err: could not add unweighted term to report panel: "
//                                    + docText.substring(plainIndexStart,
//                                    plainIndexEnd));
//                        }
//
//                        // matched
//                        try {
////						double weight = 1.0;
//                            double weight = 0.8;
//                            int fontSize = (int) (maxFontSize * weight);
//                            if (fontSize < minFontSize) {
//                                fontSize = minFontSize;
//                            }
//                            SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                            StyleConstants.setFontSize(attrSet, fontSize);
//                            StyleConstants.setBackground(attrSet, new Color(0, 255,
//                                    255, (int) (255 * weight)));
//                            abstDoc.insertString(abstDoc.getLength(), docText.substring(plainIndexEnd, matchedIndexEnd),
//                                    attrSet);
//                        } catch (BadLocationException e) {
//                            e.printStackTrace();
//                            System.out.println("err: could not add weighted term to report panel: "
//                                    + docText.substring(plainIndexEnd,
//                                    matchedIndexEnd));
//                        }
//
//                        lastEndIndex = matchedIndexEnd;
//
//                    }
//
//                    // print the last bit of unmatched text, if present (should be)
//                    try {
//                        int fontSize = minFontSize;
//                        SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                        StyleConstants.setFontSize(attrSet, fontSize);
//                        abstDoc.insertString(abstDoc.getLength(), docText.substring(lastEndIndex),
//                                attrSet);
//                    } catch (BadLocationException e) {
//                        e.printStackTrace();
//                        System.out.println("err: could not add unweighted term to report panel: "
//                                + docText.substring(lastEndIndex));
//                    }
//
//                } else {
//                    // regexp doesn't match, so just load plain doc
//                    try {
//                        int fontSize = minFontSize;
//                        SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                        StyleConstants.setFontSize(attrSet, fontSize);
//                        abstDoc.insertString(abstDoc.getLength(), docText, attrSet);
//                    } catch (BadLocationException e) {
//                        e.printStackTrace();
//                        System.out.println("err: could not load plain doc in panel (regexp not matched)");
//                    }
//                }
//            } else {
//                // highlight sufficiently-weighted terms, if any
//                Scanner docTextLineSplitter = new Scanner(docText);
//                while (docTextLineSplitter.hasNextLine()) {
//                    String line = docTextLineSplitter.nextLine();
//                    Scanner lineSplitter = new Scanner(line);
//                    while (lineSplitter.hasNext()) {
//
//                        String term = lineSplitter.next();
//
//                        // if term is highly weighted, draw with emphasis;
//                        // otherwise, draw normally
//                        double weight = 0.;
//                        double weightDiffMult = 1.3; // the larger this is, the
//                        // higher the threshold for
//                        // highlighting
//                        if (!abnormalNameMap.containsKey(selectedAttr)
//                                && predictionNameMap.containsKey(selectedAttr)
//                                && ((termWeightMap.containsKey(term) && (weight = termWeightMap.get(term)) > (maxWeight - ((maxWeight - minWeight) / weightDiffMult))) || (termWeightMap.containsKey(term.toLowerCase()) && (weight = termWeightMap.get(term.toLowerCase())) > (maxWeight - ((maxWeight - minWeight) / weightDiffMult))))) {
//
//                            // if term is weighted sufficiently for the indicator
//                            try {
//                                int fontSize = (int) (maxFontSize * (weight / maxWeight));
//                                if (fontSize < minFontSize) {
//                                    fontSize = minFontSize;
//                                }
//                                SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                                StyleConstants.setFontSize(attrSet, fontSize);
//                                StyleConstants.setBackground(attrSet, new Color(0,
//                                        255, 255,
//                                        (int) (255 * (weight / maxWeight))));
//                                abstDoc.insertString(abstDoc.getLength(), term
//                                        + " ", attrSet);
//                            } catch (BadLocationException e) {
//                                e.printStackTrace();
//                                System.out.println("err: could not add weighted term to report panel: "
//                                        + term);
//                            }
//
//                        } else {
//
//                            // term is not weighted sufficiently for indicator
//                            try {
//                                int fontSize = minFontSize;
//                                SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                                StyleConstants.setFontSize(attrSet, fontSize);
//                                abstDoc.insertString(abstDoc.getLength(), term
//                                        + " ", attrSet);
//                            } catch (BadLocationException e) {
//                                e.printStackTrace();
//                                System.out.println("err: could not add unweighted term to report panel: "
//                                        + term);
//                            }
//
//                        }
//
//                    }
//
//                    // add a newline at the end of the line
//                    try {
//                        int fontSize = minFontSize;
//                        SimpleAttributeSet attrSet = new SimpleAttributeSet();
//                        StyleConstants.setFontSize(attrSet, fontSize);
//                        abstDoc.insertString(abstDoc.getLength(), "\n", attrSet);
//                    } catch (BadLocationException e) {
//                        e.printStackTrace();
//                        System.out.println("err: could not add newline to report panel");
//                    }
//
//                }
//            }



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
            ((DocFocusPopup) popup).rebuildDocDetailsTable();
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
        TreeMapSelectorTableModel treeMapSelectorTableModel = (TreeMapSelectorTableModel) docTreeMapSelectionModel;
        List<String> currentSelectedAttrs = treeMapSelectorTableModel.getSelectedAttributeList();

        // feed selected attrs to the treemap
        //DocumentTreeMapView.updatePanelWithNewTreemap(docTreeMapViewComponent, model.getAllDocuments(), model.getAllSelectedDocuments(), currentSelectedAttrs);
        view.orderedAttrSelectionChanged();

    }
}
