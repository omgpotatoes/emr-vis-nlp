package annotator;

import annotator.lrn.DistanceFunctionCosine;
import annotator.lrn.TermScoreTuple;
import annotator.data.DataTag;
import annotator.data.TextInstanceArguing;
import annotator.data.Dataset;
import annotator.data.Document;
import annotator.data.TextInstance;
import annotator.data.DocumentArguing;
import annotator.annotator.DocumentVarsTableModel;
import annotator.annotator.JTreeIndiVarCorrs;
import annotator.annotator.JTreeLabels;
import annotator.annotator.TextInstanceTableModel;
import annotator.backend.RuntimeIndicatorPrediction;
import annotator.config.DocumentTableModel;
import annotator.data.*;
import annotator.lrn.*;
import annotator.visualizer.*;

import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;
import prefuse.Display;
import prefuse.Visualization;

/**
 *
 * @author alexander.p.conrad@gmail.com
 */
public class MainWindow extends javax.swing.JFrame {

    public static MainWindow window;
    public static final int VIS_WIDTH = 950;
    public static final int VIS_HEIGHT = 250;
    public static final int RULE_WIDTH = VIS_WIDTH;
    public static final int RULE_HEIGHT = VIS_HEIGHT;
    // multi-dataset model, not under consideration right now
    //public static List<Dataset> activeDatasets = null;
    //public static TableModel datasetTableModel = null;
    public static Dataset activeDataset = null;
    public static TableModel documentTableModel = null;
    public static TableModel textInstanceTableModel = null;
    public static boolean isAnnotatorLoaded = false;
    public static int selectedDocumentIndex = -1;
    public static int selectedTextIndex = -1;
    public static boolean unsavedChanges = false;
    JPanel jPanelVisualizerRulebuilderContainer;
    // for vanilla displays
    public static Display prefuseZoneForceDisplay;
    public static Display prefuseLSA2dDisplay;
    public static TextSimilarityClustering clusterer;
    public static DistanceFunctionCosine distFuncCosine;
    // for display with controls
    public static TableModel datasetVarsTableModel;
    public static TableModel documentVarsTableModel;
    public static Display prefuseLSA2dDisplayControlled;
    public static TableModel clusterDocsTableModel;
    public static boolean customSelectionModeEnabled = false;
//    public static VarBarChart varBarChart1;
//    public static VarBarChart varBarChart2;
//    public static VarBarChart varBarChart3;
//    public static VarBarChart varBarChart4;

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();
        initializeCustomPanels();
        uncheckEditorRadioButtons();
        uncheckVisualizerRadioButtons();
        RuntimeIndicatorPrediction.initRuntimeIndicatorPredictor();  // initializes predictor for biovis 
        jRadioButtonMenuEditorAnnotator.setSelected(true);
        jRadioButtonMenuVisualizer2d.setSelected(true);

        // @TODO move this somewhere more appropriate; post-initialization?
//        TableColumn summaryCol = jTable2dPlotControls.getColumnModel().getColumn(3);
//        summaryCol.setCellRenderer(new VarDatasetRatioRenderer());
        VarDatasetRatioRenderer boxplotRenderer = new VarDatasetRatioRenderer();
        jTable2dPlotControls.setDefaultRenderer(JPanel.class, boxplotRenderer);

//        ((VarBarChart) jPanelVarBarChart1).setAttrNameBox(jComboBoxVarBarChart1);
//        ((VarBarChart) jPanelVarBarChart2).setAttrNameBox(jComboBoxVarBarChart2);
//        ((VarBarChart) jPanelVarBarChart3).setAttrNameBox(jComboBoxVarBarChart3);
//        ((VarBarChart) jPanelVarBarChart4).setAttrNameBox(jComboBoxVarBarChart4);

        // add listeners
        // listener for document selection
        jTableDocuments.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                int firstIndex = e.getFirstIndex();
                int lastIndex = e.getLastIndex();
                boolean isAdjusting = e.getValueIsAdjusting();

                if (lsm.isSelectionEmpty()) {
                    //output.append(" <none>");
                } else {
                    // Find out which indexes are selected.
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();

                    // minIndex should == maxIndex, since model restricted to selecting one row
                    if (minIndex == maxIndex) {

                        selectedDocumentIndex = minIndex;
                        // debug
                        System.out.println("debug: setting selected document to " + minIndex);

                        // reload text tables
                        if (activeDataset.getType().equals(activeDataset.DATASET_TYPE_ARGSUBJ)) {
                            // if arguing
                            ((TextInstanceTableModel) textInstanceTableModel).updateAllRows();
                        } else if (activeDataset.getType().equals(activeDataset.DATASET_TYPE_COLON) && !customSelectionModeEnabled) {

                            // if bioviz, no custom selection
                            if (selectedDocumentIndex == -1) {
                                jTextPaneSelectedDocText.setText("");
                            } else {
                                jTextPaneSelectedDocText.setText(activeDataset.getDocuments().get(selectedDocumentIndex).getText());

                                ((DocumentVarsTableModel) documentVarsTableModel).updateAllRows();
                                ((ClusterDocsTableModel) clusterDocsTableModel).updateAllRows();
                                RuntimeIndicatorPrediction.buildTemporaryFileForText(activeDataset.getDocuments().get(selectedDocumentIndex).getParsedText());
                                RuntimeIndicatorPrediction.predictIndicatorsForTempFile();

                            }

                            // set selection also in clusterDocsTableModel (if we can find the name)
                            String name = activeDataset.getDocuments().get(selectedDocumentIndex).getName();
                            System.out.println("debug: jTableDocuments listener: selectedDocName: " + name);
                            for (int r = 0; r < jTableClusterDocs.getRowCount(); r++) {
                                String nameAtRow = (String) jTableClusterDocs.getValueAt(r, 0);
                                if (name.trim().equalsIgnoreCase(nameAtRow.trim())) {
                                    // debug
                                    System.out.println("debug: found doc " + name + " in jTableClusterDocs at " + r);
                                    jTableClusterDocs.setRowSelectionInterval(r, r);
                                    break;
                                }
                            }

                            // update bar charts
                            refreshAllBarCharts();

                        } else if (activeDataset.getType().equals(activeDataset.DATASET_TYPE_COLON) && customSelectionModeEnabled) {

                            // if bioviz, custom selection
                            if (selectedDocumentIndex == -1) {
                                jTextPaneSelectedDocText.setText("");
                            } else {
                                jTextPaneSelectedDocText.setText(activeDataset.getDocuments().get(selectedDocumentIndex).getText());

                                ((DocumentVarsTableModel) documentVarsTableModel).updateAllRows();
                                ((ClusterDocsTableModel) clusterDocsTableModel).updateAllRows();
                                RuntimeIndicatorPrediction.buildTemporaryFileForText(activeDataset.getDocuments().get(selectedDocumentIndex).getParsedText());
                                RuntimeIndicatorPrediction.predictIndicatorsForTempFile();

                            }

                            // set selection also in clusterDocsTableModel (if we can find the name)
                            String name = activeDataset.getDocuments().get(selectedDocumentIndex).getName();
                            System.out.println("debug: jTableDocuments listener: selectedDocName: " + name);
                            for (int r = 0; r < jTableClusterDocs.getRowCount(); r++) {
                                String nameAtRow = (String) jTableClusterDocs.getValueAt(r, 0);
                                if (name.trim().equalsIgnoreCase(nameAtRow.trim())) {
                                    // debug
                                    System.out.println("debug: found doc " + name + " in jTableClusterDocs at " + r);
                                    jTableClusterDocs.setRowSelectionInterval(r, r);
                                    break;
                                }
                            }

                            // update bar charts
                            refreshAllBarCharts();

                        }

                    }
                }

                jTreeLabelChooser.clearSelection();
                jTextAreaTextEditor.setText("");
                jTextAreaElaboration.setText("");

            }
        });

        // listener for text selection (annotator only)
        jTableTextChooser.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                int firstIndex = e.getFirstIndex();
                int lastIndex = e.getLastIndex();
                boolean isAdjusting = e.getValueIsAdjusting();

                if (lsm.isSelectionEmpty()) {
                    //output.append(" <none>");
                } else {
                    // Find out which indexes are selected.
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();

                    // minIndex should == maxIndex, since model restricted to selecting one row
                    if (minIndex == maxIndex) {

                        selectedTextIndex = minIndex;
                        // debug
                        System.out.println("debug: setting selected text to " + minIndex);

                        // load text into field
                        jTextAreaTextEditor.setText(activeDataset.getDocuments().get(selectedDocumentIndex).getTextInstances().get(selectedTextIndex).getTextStr());

                        // set unclear
                        boolean unclear = false;
                        if (activeDataset.getDocuments().get(selectedDocumentIndex).getTextInstances().get(selectedTextIndex).getAttributes().containsKey("unclear")
                                && activeDataset.getDocuments().get(selectedDocumentIndex).getTextInstances().get(selectedTextIndex).getAttributes().get("unclear").equals("true")) {
                            jCheckBoxLabelUnclear.setSelected(true);
                        } else {
                            jCheckBoxLabelUnclear.setSelected(false);
                        }

                    }
                }

                // clear label selection (ideally, find the label that this 
                //  new text instance is assigned and set selection to that, 
                //  but for now just clear)
                jTreeLabelChooser.clearSelection();

            }
        });

        // listener for label selection (annotator only)
        jTreeLabelChooser.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTreeLabelChooser.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {

                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTreeLabelChooser.getLastSelectedPathComponent();

                if (selectedNode == null) {
                    return;
                }

                DataTag selectedNodeData = (DataTag) selectedNode.getUserObject();

                // if leaf:
                if (selectedNode.isLeaf()) {

                    // update elaboration field
//                    if (selectedNodeData.getAttributes().containsKey("elaboration")) {
//                        jTextAreaElaboration.setText(selectedNodeData.getAttributes().get("elaboration"));
//
//                    } else {
//                        jTextAreaElaboration.setText("");
//                    }
                    jTextAreaElaboration.setText(selectedNodeData.getElaborationText());

                    // update labels for selected text element; trace back through parents
                    //  add attribute for each type: (type, name) pairs

                    //Map<String, String> instAttrVals = new HashMap<>();

                    if (selectedDocumentIndex != -1 && selectedTextIndex != -1) {
                        TextInstance currentInstance = activeDataset.getDocuments().get(selectedDocumentIndex).getTextInstances().get(selectedTextIndex);
                        DataTag currentData = selectedNodeData;
                        //instAttrVals.put(currentData.getType(), currentData.getAttributes().get("name"));
                        currentInstance.getAttributes().put(currentData.getType(), currentData.getAttributes().get("name"));
                        while (currentData.hasParentTag()) {
                            currentData = currentData.getParentTag();
                            currentInstance.getAttributes().put(currentData.getType(), currentData.getAttributes().get("name"));
                        }
                        ((TextInstanceTableModel) textInstanceTableModel).updateAllRows();
                    } else {
                        // debug
                        System.out.println("debug: no document and/or text selected, no attributes updated");
                    }

                } else {
                    jTextAreaElaboration.setText("");
                }

            }
        });

        // listener for within-cluster document selection 
        jTableClusterDocs.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                int firstIndex = e.getFirstIndex();
                int lastIndex = e.getLastIndex();
                boolean isAdjusting = e.getValueIsAdjusting();

                if (lsm.isSelectionEmpty()) {
                    //output.append(" <none>");
                } else {
                    // Find out which indexes are selected.
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();

                    // minIndex should == maxIndex, since model restricted to selecting one row
                    if (minIndex == maxIndex) {

                        // need to translate from minIndex to actual index
                        minIndex = ((ClusterDocsTableModel) clusterDocsTableModel).translateTableIndexToGlobalIndex(minIndex);
                        selectedDocumentIndex = minIndex;
                        // debug
                        System.out.println("debug: setting selected document to " + minIndex);

                        // reload text tables
                        if (activeDataset.getType().equals(activeDataset.DATASET_TYPE_ARGSUBJ)) {
                            // if arguing
                            ((TextInstanceTableModel) textInstanceTableModel).updateAllRows();
                        } else if (activeDataset.getType().equals(activeDataset.DATASET_TYPE_COLON)) {
                            // if bioviz
                            if (selectedDocumentIndex == -1) {
                                jTextPaneSelectedDocText.setText("");
                            } else {
                                jTextPaneSelectedDocText.setText(activeDataset.getDocuments().get(selectedDocumentIndex).getText());

                                jTableDocuments.setRowSelectionInterval(selectedDocumentIndex, selectedDocumentIndex);
                                ((DocumentVarsTableModel) documentVarsTableModel).updateAllRows();
                                ((ClusterDocsTableModel) clusterDocsTableModel).updateAllRows();
                                RuntimeIndicatorPrediction.buildTemporaryFileForText(activeDataset.getDocuments().get(selectedDocumentIndex).getParsedText());
                                RuntimeIndicatorPrediction.predictIndicatorsForTempFile();

                            }
                        }

                    }
                }

                jTreeLabelChooser.clearSelection();
                jTextAreaTextEditor.setText("");
                jTextAreaElaboration.setText("");



            }
        });

        // listener for var/indi selection change (bioviz)
        jTableSelectedDocVars.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                int firstIndex = e.getFirstIndex();
                int lastIndex = e.getLastIndex();
                boolean isAdjusting = e.getValueIsAdjusting();

                if (lsm.isSelectionEmpty()) {
                    //output.append(" <none>");
                } else {
                    // Find out which indexes are selected.
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();

                    // minIndex should == maxIndex, since model restricted to selecting one row
                    if (minIndex == maxIndex) {

                        // watch out; this should be the model name, not the actual displayed attr name!
                        String selectedAttr = ((DocumentVarsTableModel) documentVarsTableModel).getSelectedAttr(minIndex);

                        // debug
                        System.out.println("debug: selectedAttr: " + selectedAttr);
                        rebuildBioDocTextWithHighlights(selectedAttr);

                    }


                }



            }
        });

        // for window resize
        this.addComponentListener(new ComponentListener() {

            public void componentResized(ComponentEvent e) {
                int prefusePanelWidth = jPanelVisualizer2dPlotPrefusePanel.getSize().width;  //.getWidth();
                int prefusePanelHeight = jPanelVisualizer2dPlotPrefusePanel.getSize().height;  //.getHeight();
                // debug
                //System.out.println("debug: resizing prefuseLSA2dDisplayControlled (window): " + prefusePanelWidth + ", " + prefusePanelHeight);
                prefuseLSA2dDisplayControlled.setSize(prefusePanelWidth, prefusePanelHeight);
            }

            public void componentHidden(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentShown(ComponentEvent e) {
            }
        });



        int prefusePanelWidth = jPanelVisualizer2dPlotPrefusePanel.getSize().width;  //.getWidth();
        int prefusePanelHeight = jPanelVisualizer2dPlotPrefusePanel.getSize().height;  //.getHeight();
        // debug
        //System.out.println("debug: resizing prefuseLSA2dDisplayControlled: " + prefusePanelWidth + ", " + prefusePanelHeight);
        prefuseLSA2dDisplayControlled.setSize(VIS_WIDTH, VIS_HEIGHT);

        activateAppropriateGUIComponents();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooserDoclist = new javax.swing.JFileChooser();
        jRadioButtonMenuLoadAnnotator = new javax.swing.JRadioButtonMenuItem();
        jFileChooserNewDoc = new javax.swing.JFileChooser();
        jPanelEditorDummy = new javax.swing.JPanel();
        jPanelVisualizerDummy = new javax.swing.JPanel();
        jPanelVisualizer2dPlot = new javax.swing.JPanel();
        jPanelBiovisTextData = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPaneClusterText = new javax.swing.JScrollPane();
        jTextPaneClusterText = new javax.swing.JTextPane();
        jScrollPaneClusterDocs = new javax.swing.JScrollPane();
        jTableClusterDocs = new javax.swing.JTable();
        jSplitPane3 = new javax.swing.JSplitPane();
        jScrollPaneSelectedDocText = new javax.swing.JScrollPane();
        jTextPaneSelectedDocText = new javax.swing.JTextPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPaneSelectedDocVars = new javax.swing.JScrollPane();
        jTableSelectedDocVars = new javax.swing.JTable();
        jButtonSaveDocPredIndis = new javax.swing.JButton();
        jPanelVisualizer2dPlotControls = new javax.swing.JPanel();
        jSplitPane5 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jComboBox2dTextUse = new javax.swing.JComboBox();
        jScrollPane2dPlotControls = new javax.swing.JScrollPane();
        jTable2dPlotControls = new javax.swing.JTable();
        jSpinnerNumClusters = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jToggleButtonCustomSelection = new javax.swing.JToggleButton();
        jButtonDeselectVars = new javax.swing.JButton();
        jButton2dPlotRebuild = new javax.swing.JButton();
        jPanelVisualizer2dPlotPrefusePanel = new javax.swing.JPanel();
        jPanelAnnotatorVarIndiCorrs = new javax.swing.JPanel();
        jScrollPaneVarIndiCorrs = new javax.swing.JScrollPane();
        jTreeVarIndiCorrs = new javax.swing.JTree();
        jSplitPaneMain = new javax.swing.JSplitPane();
        jPanelDatasets = new javax.swing.JPanel();
        jScrollPaneDocuments = new javax.swing.JScrollPane();
        jTableDocuments = new javax.swing.JTable();
        jButtonNewDoc = new javax.swing.JButton();
        jPanelContent = new javax.swing.JPanel();
        jSplitPaneContent = new javax.swing.JSplitPane();
        jPanelSummaryVisContainer = new javax.swing.JPanel();
        jPanelSummaryVis = new javax.swing.JPanel();
        jSliderEdgeSimThreshold = new javax.swing.JSlider();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jSpinnerNumClustersForceVis = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jPanelTextView = new javax.swing.JPanel();
        jSplitPaneAnnotator = new javax.swing.JSplitPane();
        jSplitPaneAnnotator2 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPaneTextEditor = new javax.swing.JScrollPane();
        jTextAreaTextEditor = new javax.swing.JTextArea();
        jButtonAddTextInstance = new javax.swing.JButton();
        jButtonUpdateText = new javax.swing.JButton();
        jSplitPane4 = new javax.swing.JSplitPane();
        jPanelLabelChooser = new javax.swing.JPanel();
        jCheckBoxLabelUnclear = new javax.swing.JCheckBox();
        jButtonNext = new javax.swing.JButton();
        jScrollPaneElaboration = new javax.swing.JScrollPane();
        jTextAreaElaboration = new javax.swing.JTextArea();
        jCheckBoxArguing = new javax.swing.JCheckBox();
        jButtonClearArgument = new javax.swing.JButton();
        jScrollPaneLabelChooser = new javax.swing.JScrollPane();
        jTreeLabelChooser = new javax.swing.JTree();
        jScrollPaneTextChooser = new javax.swing.JScrollPane();
        jTableTextChooser = new javax.swing.JTable();
        jMenuBarMain = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemLoadDoclist = new javax.swing.JMenuItem();
        jMenuItemSaveAllDocuments = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        jMenuVisualizer = new javax.swing.JMenu();
        jRadioButtonMenuVisualizerNone = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuVisualizerRulebuilder = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuVisualizer2d = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuVisualizerLSA2d = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuVisualizerLSA2dWControls = new javax.swing.JRadioButtonMenuItem();
        jMenuEditor = new javax.swing.JMenu();
        jRadioButtonMenuEditorNone = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuEditorAnnotator = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuEditorBiovis = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuEditorBiovisCorrelation = new javax.swing.JRadioButtonMenuItem();

        jFileChooserDoclist.setDialogTitle("Load Doclist...");
        jFileChooserDoclist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFileChooserDoclistActionPerformed(evt);
            }
        });

        jRadioButtonMenuLoadAnnotator.setText("Load Annotator");
        jRadioButtonMenuLoadAnnotator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuLoadAnnotatorActionPerformed(evt);
            }
        });

        jFileChooserNewDoc.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        jFileChooserNewDoc.setDialogTitle("Save New Document As...");

        javax.swing.GroupLayout jPanelEditorDummyLayout = new javax.swing.GroupLayout(jPanelEditorDummy);
        jPanelEditorDummy.setLayout(jPanelEditorDummyLayout);
        jPanelEditorDummyLayout.setHorizontalGroup(
            jPanelEditorDummyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanelEditorDummyLayout.setVerticalGroup(
            jPanelEditorDummyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanelVisualizerDummyLayout = new javax.swing.GroupLayout(jPanelVisualizerDummy);
        jPanelVisualizerDummy.setLayout(jPanelVisualizerDummyLayout);
        jPanelVisualizerDummyLayout.setHorizontalGroup(
            jPanelVisualizerDummyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanelVisualizerDummyLayout.setVerticalGroup(
            jPanelVisualizerDummyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jPanelVisualizer2dPlot.add(prefuseLSA2dDisplay);

        javax.swing.GroupLayout jPanelVisualizer2dPlotLayout = new javax.swing.GroupLayout(jPanelVisualizer2dPlot);
        jPanelVisualizer2dPlot.setLayout(jPanelVisualizer2dPlotLayout);
        jPanelVisualizer2dPlotLayout.setHorizontalGroup(
            jPanelVisualizer2dPlotLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanelVisualizer2dPlotLayout.setVerticalGroup(
            jPanelVisualizer2dPlotLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jSplitPane1.setDividerLocation(500);

        jSplitPane2.setDividerLocation(400);

        jScrollPaneClusterText.setViewportView(jTextPaneClusterText);

        jSplitPane2.setLeftComponent(jScrollPaneClusterText);

        jTableClusterDocs.setModel(clusterDocsTableModel);
        jScrollPaneClusterDocs.setViewportView(jTableClusterDocs);

        jSplitPane2.setRightComponent(jScrollPaneClusterDocs);

        jSplitPane1.setLeftComponent(jSplitPane2);

        jSplitPane3.setDividerLocation(225);

        jScrollPaneSelectedDocText.setViewportView(jTextPaneSelectedDocText);

        jSplitPane3.setLeftComponent(jScrollPaneSelectedDocText);

        jTableSelectedDocVars.setModel(documentVarsTableModel);
        jTableSelectedDocVars.getColumnModel().getColumn(0).setPreferredWidth(200);  jTableSelectedDocVars.getColumnModel().getColumn(1).setPreferredWidth(75);  jTableSelectedDocVars.getColumnModel().getColumn(2).setPreferredWidth(75);
        jScrollPaneSelectedDocVars.setViewportView(jTableSelectedDocVars);

        jButtonSaveDocPredIndis.setText("Save Indicators");
        jButtonSaveDocPredIndis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveDocPredIndisActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneSelectedDocVars, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 42, Short.MAX_VALUE)
                .addComponent(jButtonSaveDocPredIndis))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPaneSelectedDocVars, javax.swing.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSaveDocPredIndis))
        );

        jSplitPane3.setRightComponent(jPanel2);

        jSplitPane1.setRightComponent(jSplitPane3);

        javax.swing.GroupLayout jPanelBiovisTextDataLayout = new javax.swing.GroupLayout(jPanelBiovisTextData);
        jPanelBiovisTextData.setLayout(jPanelBiovisTextDataLayout);
        jPanelBiovisTextDataLayout.setHorizontalGroup(
            jPanelBiovisTextDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelBiovisTextDataLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelBiovisTextDataLayout.setVerticalGroup(
            jPanelBiovisTextDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelBiovisTextDataLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanelVisualizer2dPlotControls.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jPanelVisualizer2dPlotControlsPropertyChange(evt);
            }
        });

        jSplitPane5.setDividerLocation(600);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("use text for:");

        jComboBox2dTextUse.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "position", "color", "none", "both" }));
        jComboBox2dTextUse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2dTextUseActionPerformed(evt);
            }
        });

        jTable2dPlotControls.setModel(datasetVarsTableModel);
        jTable2dPlotControls.getColumnModel().getColumn(0).setPreferredWidth(200);  jTable2dPlotControls.getColumnModel().getColumn(1).setPreferredWidth(75);  jTable2dPlotControls.getColumnModel().getColumn(2).setPreferredWidth(75);  jTable2dPlotControls.getColumnModel().getColumn(3).setPreferredWidth(200);
        jScrollPane2dPlotControls.setViewportView(jTable2dPlotControls);

        jSpinnerNumClusters.setModel(new javax.swing.SpinnerNumberModel(5, 0, 10, 1));

        jLabel1.setText("clusters");

        jToggleButtonCustomSelection.setText("Custom Selection");
        jToggleButtonCustomSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonCustomSelectionActionPerformed(evt);
            }
        });

        jButtonDeselectVars.setText("Deselect All");
        jButtonDeselectVars.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeselectVarsActionPerformed(evt);
            }
        });

        jButton2dPlotRebuild.setText("Rebuild Visualization");
        jButton2dPlotRebuild.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2dPlotRebuildActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2dPlotControls)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox2dTextUse, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jSpinnerNumClusters, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButtonCustomSelection, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDeselectVars)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2dPlotRebuild, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox2dTextUse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2dPlotControls, javax.swing.GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2dPlotRebuild)
                    .addComponent(jSpinnerNumClusters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jButtonDeselectVars)
                    .addComponent(jToggleButtonCustomSelection)))
        );

        jSplitPane5.setRightComponent(jPanel3);

        jPanelVisualizer2dPlotPrefusePanel.add(prefuseLSA2dDisplayControlled);
        jPanelVisualizer2dPlotPrefusePanel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jPanelVisualizer2dPlotPrefusePanelPropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanelVisualizer2dPlotPrefusePanelLayout = new javax.swing.GroupLayout(jPanelVisualizer2dPlotPrefusePanel);
        jPanelVisualizer2dPlotPrefusePanel.setLayout(jPanelVisualizer2dPlotPrefusePanelLayout);
        jPanelVisualizer2dPlotPrefusePanelLayout.setHorizontalGroup(
            jPanelVisualizer2dPlotPrefusePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 599, Short.MAX_VALUE)
        );
        jPanelVisualizer2dPlotPrefusePanelLayout.setVerticalGroup(
            jPanelVisualizer2dPlotPrefusePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 433, Short.MAX_VALUE)
        );

        jSplitPane5.setLeftComponent(jPanelVisualizer2dPlotPrefusePanel);

        javax.swing.GroupLayout jPanelVisualizer2dPlotControlsLayout = new javax.swing.GroupLayout(jPanelVisualizer2dPlotControls);
        jPanelVisualizer2dPlotControls.setLayout(jPanelVisualizer2dPlotControlsLayout);
        jPanelVisualizer2dPlotControlsLayout.setHorizontalGroup(
            jPanelVisualizer2dPlotControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane5)
        );
        jPanelVisualizer2dPlotControlsLayout.setVerticalGroup(
            jPanelVisualizer2dPlotControlsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane5)
        );

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("colonoscopy_var-indi_coors");
        jTreeVarIndiCorrs.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPaneVarIndiCorrs.setViewportView(jTreeVarIndiCorrs);

        javax.swing.GroupLayout jPanelAnnotatorVarIndiCorrsLayout = new javax.swing.GroupLayout(jPanelAnnotatorVarIndiCorrs);
        jPanelAnnotatorVarIndiCorrs.setLayout(jPanelAnnotatorVarIndiCorrsLayout);
        jPanelAnnotatorVarIndiCorrsLayout.setHorizontalGroup(
            jPanelAnnotatorVarIndiCorrsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAnnotatorVarIndiCorrsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneVarIndiCorrs, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelAnnotatorVarIndiCorrsLayout.setVerticalGroup(
            jPanelAnnotatorVarIndiCorrsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAnnotatorVarIndiCorrsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneVarIndiCorrs, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setName("mainFrame");

        jSplitPaneMain.setDividerLocation(160);

        jTableDocuments.setModel(documentTableModel);
        jTableDocuments.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTableDocuments.getColumnModel().getColumn(0).setPreferredWidth(200);  jTableDocuments.getColumnModel().getColumn(1).setPreferredWidth(75);
        jScrollPaneDocuments.setViewportView(jTableDocuments);

        jButtonNewDoc.setText("New Doc");
        jButtonNewDoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewDocActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelDatasetsLayout = new javax.swing.GroupLayout(jPanelDatasets);
        jPanelDatasets.setLayout(jPanelDatasetsLayout);
        jPanelDatasetsLayout.setHorizontalGroup(
            jPanelDatasetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneDocuments, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanelDatasetsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonNewDoc)
                .addContainerGap(64, Short.MAX_VALUE))
        );
        jPanelDatasetsLayout.setVerticalGroup(
            jPanelDatasetsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDatasetsLayout.createSequentialGroup()
                .addComponent(jScrollPaneDocuments, javax.swing.GroupLayout.DEFAULT_SIZE, 665, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonNewDoc)
                .addContainerGap())
        );

        jSplitPaneMain.setLeftComponent(jPanelDatasets);

        jSplitPaneContent.setDividerLocation(300);
        jSplitPaneContent.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPaneContent.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSplitPaneContentPropertyChange(evt);
            }
        });

        jPanelSummaryVisContainer.add(prefuseZoneForceDisplay);

        javax.swing.GroupLayout jPanelSummaryVisLayout = new javax.swing.GroupLayout(jPanelSummaryVis);
        jPanelSummaryVis.setLayout(jPanelSummaryVisLayout);
        jPanelSummaryVisLayout.setHorizontalGroup(
            jPanelSummaryVisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 787, Short.MAX_VALUE)
        );
        jPanelSummaryVisLayout.setVerticalGroup(
            jPanelSummaryVisLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 244, Short.MAX_VALUE)
        );

        jSliderEdgeSimThreshold.setOrientation(javax.swing.JSlider.VERTICAL);
        jSliderEdgeSimThreshold.setValue(25);
        jSliderEdgeSimThreshold.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderEdgeSimThresholdStateChanged(evt);
            }
        });
        jSliderEdgeSimThreshold.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                jSliderEdgeSimThresholdCaretPositionChanged(evt);
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("edge threshold");

        jButton1.setText("Refresh");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jSpinnerNumClustersForceVis.setModel(new javax.swing.SpinnerNumberModel(10, 1, 100, 1));

        jLabel3.setText("clusters");

        javax.swing.GroupLayout jPanelSummaryVisContainerLayout = new javax.swing.GroupLayout(jPanelSummaryVisContainer);
        jPanelSummaryVisContainer.setLayout(jPanelSummaryVisContainerLayout);
        jPanelSummaryVisContainerLayout.setHorizontalGroup(
            jPanelSummaryVisContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSummaryVisContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelSummaryVis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelSummaryVisContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanelSummaryVisContainerLayout.createSequentialGroup()
                        .addComponent(jSpinnerNumClustersForceVis, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3))
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSliderEdgeSimThreshold, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelSummaryVisContainerLayout.setVerticalGroup(
            jPanelSummaryVisContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSummaryVisContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSummaryVisContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelSummaryVis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanelSummaryVisContainerLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSliderEdgeSimThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelSummaryVisContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jSpinnerNumClustersForceVis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)))
                .addContainerGap(42, Short.MAX_VALUE))
        );

        jSplitPaneContent.setTopComponent(jPanelSummaryVisContainer);

        jSplitPaneAnnotator.setDividerLocation(180);

        jSplitPaneAnnotator2.setDividerLocation(240);

        jTextAreaTextEditor.setColumns(20);
        jTextAreaTextEditor.setLineWrap(true);
        jTextAreaTextEditor.setRows(5);
        jTextAreaTextEditor.setWrapStyleWord(true);
        jScrollPaneTextEditor.setViewportView(jTextAreaTextEditor);

        jButtonAddTextInstance.setText("New Text");
        jButtonAddTextInstance.setToolTipText("");
        jButtonAddTextInstance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddTextInstanceActionPerformed(evt);
            }
        });

        jButtonUpdateText.setText("UpdateText");
        jButtonUpdateText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUpdateTextActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAddTextInstance)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonUpdateText)
                .addContainerGap(36, Short.MAX_VALUE))
            .addComponent(jScrollPaneTextEditor)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPaneTextEditor, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonAddTextInstance)
                    .addComponent(jButtonUpdateText))
                .addContainerGap())
        );

        jSplitPaneAnnotator2.setLeftComponent(jPanel1);

        jSplitPane4.setDividerLocation(220);
        jSplitPane4.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jCheckBoxLabelUnclear.setText("unclear?");
        jCheckBoxLabelUnclear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxLabelUnclearActionPerformed(evt);
            }
        });

        jButtonNext.setText("Next");

        jTextAreaElaboration.setColumns(20);
        jTextAreaElaboration.setLineWrap(true);
        jTextAreaElaboration.setRows(5);
        jTextAreaElaboration.setWrapStyleWord(true);
        jScrollPaneElaboration.setViewportView(jTextAreaElaboration);

        jCheckBoxArguing.setText("isArguing?");
        jCheckBoxArguing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxArguingActionPerformed(evt);
            }
        });

        jButtonClearArgument.setText("Clear Argument");
        jButtonClearArgument.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearArgumentActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelLabelChooserLayout = new javax.swing.GroupLayout(jPanelLabelChooser);
        jPanelLabelChooser.setLayout(jPanelLabelChooserLayout);
        jPanelLabelChooserLayout.setHorizontalGroup(
            jPanelLabelChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLabelChooserLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxLabelUnclear)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxArguing)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 119, Short.MAX_VALUE)
                .addComponent(jButtonClearArgument)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonNext)
                .addContainerGap())
            .addComponent(jScrollPaneElaboration)
        );
        jPanelLabelChooserLayout.setVerticalGroup(
            jPanelLabelChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLabelChooserLayout.createSequentialGroup()
                .addComponent(jScrollPaneElaboration, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLabelChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonNext)
                    .addComponent(jButtonClearArgument)
                    .addComponent(jCheckBoxLabelUnclear)
                    .addComponent(jCheckBoxArguing))
                .addContainerGap())
        );

        jSplitPane4.setBottomComponent(jPanelLabelChooser);

        treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("stance_structure_root");
        jTreeLabelChooser.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPaneLabelChooser.setViewportView(jTreeLabelChooser);

        jSplitPane4.setLeftComponent(jScrollPaneLabelChooser);

        jSplitPaneAnnotator2.setRightComponent(jSplitPane4);

        jSplitPaneAnnotator.setRightComponent(jSplitPaneAnnotator2);

        jTableTextChooser.setModel(textInstanceTableModel);
        jTableTextChooser.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTableDocuments.getColumnModel().getColumn(0).setPreferredWidth(100);  jTableDocuments.getColumnModel().getColumn(1).setPreferredWidth(80);
        jScrollPaneTextChooser.setViewportView(jTableTextChooser);

        jSplitPaneAnnotator.setLeftComponent(jScrollPaneTextChooser);

        javax.swing.GroupLayout jPanelTextViewLayout = new javax.swing.GroupLayout(jPanelTextView);
        jPanelTextView.setLayout(jPanelTextViewLayout);
        jPanelTextViewLayout.setHorizontalGroup(
            jPanelTextViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneAnnotator, javax.swing.GroupLayout.DEFAULT_SIZE, 924, Short.MAX_VALUE)
        );
        jPanelTextViewLayout.setVerticalGroup(
            jPanelTextViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneAnnotator, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
        );

        jSplitPaneContent.setRightComponent(jPanelTextView);

        javax.swing.GroupLayout jPanelContentLayout = new javax.swing.GroupLayout(jPanelContent);
        jPanelContent.setLayout(jPanelContentLayout);
        jPanelContentLayout.setHorizontalGroup(
            jPanelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneContent)
        );
        jPanelContentLayout.setVerticalGroup(
            jPanelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneContent)
        );

        jSplitPaneMain.setRightComponent(jPanelContent);

        jMenuFile.setText("File");

        jMenuItemLoadDoclist.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemLoadDoclist.setText("Load Doclist");
        jMenuItemLoadDoclist.setToolTipText("");
        jMenuItemLoadDoclist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoadDoclistActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemLoadDoclist);

        jMenuItemSaveAllDocuments.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSaveAllDocuments.setText("Save All Documents");
        jMenuItemSaveAllDocuments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveAllDocumentsActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemSaveAllDocuments);
        jMenuFile.add(jSeparator1);

        jMenuItemExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemExit);

        jMenuBarMain.add(jMenuFile);

        jMenuEdit.setText("Edit");
        jMenuBarMain.add(jMenuEdit);

        jMenuVisualizer.setText("Visualizer");

        jRadioButtonMenuVisualizerNone.setSelected(true);
        jRadioButtonMenuVisualizerNone.setText("No Visualizer");
        jRadioButtonMenuVisualizerNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuVisualizerNoneActionPerformed(evt);
            }
        });
        jMenuVisualizer.add(jRadioButtonMenuVisualizerNone);

        jRadioButtonMenuVisualizerRulebuilder.setSelected(true);
        jRadioButtonMenuVisualizerRulebuilder.setText("Load Rulebuilder");
        jRadioButtonMenuVisualizerRulebuilder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuVisualizerRulebuilderActionPerformed(evt);
            }
        });
        jMenuVisualizer.add(jRadioButtonMenuVisualizerRulebuilder);

        jRadioButtonMenuVisualizer2d.setSelected(true);
        jRadioButtonMenuVisualizer2d.setText("Load Force Zone Visualizer");
        jRadioButtonMenuVisualizer2d.setToolTipText("");
        jRadioButtonMenuVisualizer2d.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuVisualizer2dActionPerformed(evt);
            }
        });
        jMenuVisualizer.add(jRadioButtonMenuVisualizer2d);

        jRadioButtonMenuVisualizerLSA2d.setSelected(true);
        jRadioButtonMenuVisualizerLSA2d.setText("Load 2d Scatterplot Visualizer");
        jRadioButtonMenuVisualizerLSA2d.setToolTipText("");
        jRadioButtonMenuVisualizerLSA2d.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuVisualizerLSA2dActionPerformed(evt);
            }
        });
        jMenuVisualizer.add(jRadioButtonMenuVisualizerLSA2d);

        jRadioButtonMenuVisualizerLSA2dWControls.setSelected(true);
        jRadioButtonMenuVisualizerLSA2dWControls.setText("Load 2d Scatterplot w/ Controls");
        jRadioButtonMenuVisualizerLSA2dWControls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuVisualizerLSA2dWControlsActionPerformed(evt);
            }
        });
        jMenuVisualizer.add(jRadioButtonMenuVisualizerLSA2dWControls);

        jMenuBarMain.add(jMenuVisualizer);

        jMenuEditor.setText("Editor");

        jRadioButtonMenuEditorNone.setText("No Editor");
        jRadioButtonMenuEditorNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuEditorNoneActionPerformed(evt);
            }
        });
        jMenuEditor.add(jRadioButtonMenuEditorNone);

        jRadioButtonMenuEditorAnnotator.setSelected(true);
        jRadioButtonMenuEditorAnnotator.setText("Load Annotation Editor");
        jRadioButtonMenuEditorAnnotator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuEditorAnnotatorActionPerformed(evt);
            }
        });
        jMenuEditor.add(jRadioButtonMenuEditorAnnotator);

        jRadioButtonMenuEditorBiovis.setSelected(true);
        jRadioButtonMenuEditorBiovis.setText("Load Biovis Text Panel");
        jRadioButtonMenuEditorBiovis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuEditorBiovisActionPerformed(evt);
            }
        });
        jMenuEditor.add(jRadioButtonMenuEditorBiovis);

        jRadioButtonMenuEditorBiovisCorrelation.setSelected(true);
        jRadioButtonMenuEditorBiovisCorrelation.setText("Load Biovis Correlation Panel");
        jRadioButtonMenuEditorBiovisCorrelation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuEditorBiovisCorrelationActionPerformed(evt);
            }
        });
        jMenuEditor.add(jRadioButtonMenuEditorBiovisCorrelation);

        jMenuBarMain.add(jMenuEditor);

        setJMenuBar(jMenuBarMain);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPaneMain)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPaneMain)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemLoadDoclistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoadDoclistActionPerformed
        int returnVal = jFileChooserDoclist.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooserDoclist.getSelectedFile();

            // read selected files, update panels
            activeDataset = Dataset.loadDatasetFromDoclist(file);
            selectedDocumentIndex = 1;
            //((DatasetTableModel)datasetTableModel).updateAllRows();
            ((DocumentTableModel) documentTableModel).updateAllRows();

            if (activeDataset.getType().equals(Dataset.DATASET_TYPE_ARGSUBJ)) {
                // update annotation area (only if arguing)
                TreeModel labelChooserModel = JTreeLabels.buildJTreeLabels(activeDataset.getTagset().getTopLevelTag());
                jTreeLabelChooser.setModel(labelChooserModel);
                ((TextInstanceTableModel) textInstanceTableModel).resetColumnNames();
            } else if (activeDataset.getType().equals(Dataset.DATASET_TYPE_COLON)) {
                // update 2d control area (only if biovis)
                ((DatasetVarsTableModel) datasetVarsTableModel).updateAllRows();
                // build list of all attrs present in any docs

                // reset bioviz text panel

                // rebuild list of attrs
//                Vector<String> normalAttrs = DatasetVarsTableModel.removeAbnormalNames(((DatasetMedColonDoclist) activeDataset).getAllVarsAndIndis());
//                jComboBoxVarBarChart1.setModel(new DefaultComboBoxModel(normalAttrs));
//                jComboBoxVarBarChart1.setSelectedIndex(0);
//                jComboBoxVarBarChart2.setModel(new DefaultComboBoxModel(normalAttrs));
//                jComboBoxVarBarChart2.setSelectedIndex(1);
//                jComboBoxVarBarChart3.setModel(new DefaultComboBoxModel(normalAttrs));
//                jComboBoxVarBarChart3.setSelectedIndex(2);
//                jComboBoxVarBarChart4.setModel(new DefaultComboBoxModel(normalAttrs));
//                jComboBoxVarBarChart4.setSelectedIndex(3);

                // pre-load parsing module

                // load the model for indi/var correlations (if we're in bioviz mode)

                jTreeVarIndiCorrs.setModel(JTreeIndiVarCorrs.buildColonoscopyCorrTree());


            }

            // update visualization 
            distFuncCosine = new DistanceFunctionCosine(activeDataset.getDatasetTermCountMap());
//            clusterer = new TextSimilarityClustering(activeDataset.getDatasetTermCountMap().keySet());
//            int numClusters = 10;
//            List<Integer> clusterAssignments = clusterer.clusterDocumentsByCosine(activeDataset.getDocuments(), numClusters);
//            // debug
//            for (int d=0; d<clusterAssignments.size(); d++) {
//                System.out.println("debug: cluster for doc "+activeDataset.getDocuments().get(d).getName()+": "+clusterAssignments.get(d));
//            }

            // debug
            // try catch block for debugging only
            //try { // temporary, remove this catch block eventually
            initializeVisualizationPanel();
            //} catch (NullPointerException e) {
            //    e.printStackTrace();
            //}
            activateAppropriateGUIComponents();

        } else {
            System.out.println("debug: \"Choose Doclist\" action cancelled by user");
        }
    }//GEN-LAST:event_jMenuItemLoadDoclistActionPerformed

    private void jFileChooserDoclistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileChooserDoclistActionPerformed
    }//GEN-LAST:event_jFileChooserDoclistActionPerformed

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
        // prompt to save?
        if (unsavedChanges) {
            // @TODO monitor for unsaved changes, prompt to save, make the "X" come here instead of instaclose (setDefaultCloseOperation, might be in designer)
        }
        // exit program cleanly
        System.exit(0);
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jRadioButtonMenuLoadAnnotatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuLoadAnnotatorActionPerformed
        // @TODO
        // warning: swapping out panels doesn't currently work
//        jPanelTextView.removeAll();
//        if (!isAnnotatorLoaded) {
//            System.out.println("debug: loading annotator onto screen");
//            jPanelTextView.add(jPanelAnnotator);
//            isAnnotatorLoaded = true;
//            // debug
//        } else {
//            
//        }
//        jPanelTextView.validate();
    }//GEN-LAST:event_jRadioButtonMenuLoadAnnotatorActionPerformed

    private void jMenuItemSaveAllDocumentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveAllDocumentsActionPerformed

        // like the name says, iterate over and save all documents
        List<Document> documents = activeDataset.getDocuments();
        for (Document document : documents) {
            document.writeDoc();
        }

        // also, we should save changes to the doclist
        activeDataset.writeDoclist();


        unsavedChanges = false;

    }//GEN-LAST:event_jMenuItemSaveAllDocumentsActionPerformed

    private void jButtonNewDocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewDocActionPerformed

        // add a new doc to the activeDataset, refresh tables
        // @TODO: make more general, be able to handle non-arguing docs too
        // @TODO: disable if no dataset is loaded (or, create new dataset on init)
        // @TODO: select the new doc automatically

        int returnVal = jFileChooserNewDoc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooserNewDoc.getSelectedFile();

            // initialize a new empty file
            DocumentArguing newDoc = new DocumentArguing(file.getName(), file.getAbsolutePath());
            activeDataset.getDocuments().add(newDoc);
            ((DocumentTableModel) documentTableModel).updateAllRows();
            jTableDocuments.setRowSelectionInterval(jTableDocuments.getRowCount() - 1, jTableDocuments.getRowCount() - 1);

        } else {
            System.out.println("debug: \"New Document\" action cancelled by user");
        }


    }//GEN-LAST:event_jButtonNewDocActionPerformed

    private void jButtonAddTextInstanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddTextInstanceActionPerformed

        // creates a new text instance in the currently selected document
        // @TODO: make more general, be able to handle non-arguing docs too
        TextInstanceArguing newTextInstance = new TextInstanceArguing();
        activeDataset.getDocuments().get(selectedDocumentIndex).getTextInstances().add(newTextInstance);
        ((TextInstanceTableModel) textInstanceTableModel).updateAllRows();
        jTableTextChooser.setRowSelectionInterval(jTableTextChooser.getRowCount() - 1, jTableTextChooser.getRowCount() - 1);

    }//GEN-LAST:event_jButtonAddTextInstanceActionPerformed

    private void jButtonUpdateTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUpdateTextActionPerformed

        // get text currently in field and save to currentle selected textinst
        if (selectedDocumentIndex != -1 && selectedTextIndex != -1) {
            activeDataset.getDocuments().get(selectedDocumentIndex).getTextInstances().get(selectedTextIndex).setTextStr(jTextAreaTextEditor.getText());
            ((TextInstanceTableModel) textInstanceTableModel).fireTableRowsUpdated(selectedTextIndex, selectedTextIndex);
        }

    }//GEN-LAST:event_jButtonUpdateTextActionPerformed

    private void jCheckBoxLabelUnclearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxLabelUnclearActionPerformed

        // set currently selected text instance to the value of this checkbox
        if (selectedDocumentIndex != -1 && selectedTextIndex != -1) {
            boolean unclear = jCheckBoxLabelUnclear.isSelected();
            activeDataset.getDocuments().get(selectedDocumentIndex).getTextInstances().get(selectedTextIndex).getAttributes().put("unclear", "" + unclear);
            ((TextInstanceTableModel) textInstanceTableModel).fireTableRowsUpdated(selectedTextIndex, selectedTextIndex);
        }

    }//GEN-LAST:event_jCheckBoxLabelUnclearActionPerformed

    private void jButtonClearArgumentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearArgumentActionPerformed

        // clears the argument, aspect, and side values for selected text
        // clears the selection from the tree
        if (selectedDocumentIndex != -1 && selectedTextIndex != -1) {
            boolean unclear = jCheckBoxLabelUnclear.isSelected();
            Map<String, String> attributes = activeDataset.getDocuments().get(selectedDocumentIndex).getTextInstances().get(selectedTextIndex).getAttributes();
            attributes.put("argument", "");
            attributes.put("aspect", "");
            attributes.put("category", "");
            attributes.put("side", "");
            jTreeLabelChooser.clearSelection();
            ((TextInstanceTableModel) textInstanceTableModel).fireTableRowsUpdated(selectedTextIndex, selectedTextIndex);
        }
        jTreeLabelChooser.clearSelection();
        jTextAreaElaboration.setText("");

    }//GEN-LAST:event_jButtonClearArgumentActionPerformed

    private void jCheckBoxArguingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxArguingActionPerformed

        // set "arguing" attribute
        if (selectedDocumentIndex != -1 && selectedTextIndex != -1) {
            boolean unclear = jCheckBoxArguing.isSelected();
            activeDataset.getDocuments().get(selectedDocumentIndex).getTextInstances().get(selectedTextIndex).getAttributes().put("arguing", "" + unclear);
            ((TextInstanceTableModel) textInstanceTableModel).fireTableRowsUpdated(selectedTextIndex, selectedTextIndex);
        }

    }//GEN-LAST:event_jCheckBoxArguingActionPerformed

    private void jRadioButtonMenuEditorNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuEditorNoneActionPerformed
        switchEditorPanelContents(0);
    }//GEN-LAST:event_jRadioButtonMenuEditorNoneActionPerformed

    private void jRadioButtonMenuEditorAnnotatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuEditorAnnotatorActionPerformed
        switchEditorPanelContents(1);
    }//GEN-LAST:event_jRadioButtonMenuEditorAnnotatorActionPerformed

    private void jRadioButtonMenuVisualizerNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuVisualizerNoneActionPerformed
        switchVisualPanelContents(0);
    }//GEN-LAST:event_jRadioButtonMenuVisualizerNoneActionPerformed

    private void jRadioButtonMenuVisualizerRulebuilderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuVisualizerRulebuilderActionPerformed
        switchVisualPanelContents(1);
    }//GEN-LAST:event_jRadioButtonMenuVisualizerRulebuilderActionPerformed

    private void jRadioButtonMenuVisualizer2dActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuVisualizer2dActionPerformed
        switchVisualPanelContents(2);
    }//GEN-LAST:event_jRadioButtonMenuVisualizer2dActionPerformed

    private void jRadioButtonMenuVisualizerLSA2dActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuVisualizerLSA2dActionPerformed
        switchVisualPanelContents(3);
    }//GEN-LAST:event_jRadioButtonMenuVisualizerLSA2dActionPerformed

    private void jRadioButtonMenuEditorBiovisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuEditorBiovisActionPerformed
        switchEditorPanelContents(2);
    }//GEN-LAST:event_jRadioButtonMenuEditorBiovisActionPerformed

    private void jButton2dPlotRebuildActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2dPlotRebuildActionPerformed

        //((DatasetVarsTableModel)datasetVarsTableModel).updateAllRows();
        LSA2dVisualizer.rebuildCustomLSA2dVisualizer();

    }//GEN-LAST:event_jButton2dPlotRebuildActionPerformed

    private void jRadioButtonMenuVisualizerLSA2dWControlsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuVisualizerLSA2dWControlsActionPerformed
        switchVisualPanelContents(4);
    }//GEN-LAST:event_jRadioButtonMenuVisualizerLSA2dWControlsActionPerformed

    private void jSliderEdgeSimThresholdStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderEdgeSimThresholdStateChanged
        // debug
//        System.out.println("debug: edgeSimThreshold state change: "+evt.toString());
//        ForceZoneVisualizer.refreshForceZoneVisualizer();
    }//GEN-LAST:event_jSliderEdgeSimThresholdStateChanged

    private void jSliderEdgeSimThresholdCaretPositionChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jSliderEdgeSimThresholdCaretPositionChanged
//        ForceZoneVisualizer.refreshForceZoneVisualizer();
    }//GEN-LAST:event_jSliderEdgeSimThresholdCaretPositionChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ForceZoneVisualizer.refreshForceZoneVisualizer();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButtonDeselectVarsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeselectVarsActionPerformed
        ((DatasetVarsTableModel) datasetVarsTableModel).deselectAll();
    }//GEN-LAST:event_jButtonDeselectVarsActionPerformed

    private void jButtonSaveDocPredIndisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveDocPredIndisActionPerformed

        // on click, save indicators for selected doc
        // this control should only be available when a biomed doc is active
        if (selectedDocumentIndex != -1) {
            ((DocumentMedColon) activeDataset.getDocuments().get(selectedDocumentIndex)).writeSelectedIndicatorsToFile();
        }

    }//GEN-LAST:event_jButtonSaveDocPredIndisActionPerformed

    private void jComboBox2dTextUseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2dTextUseActionPerformed
        // don't update on change (for now)
    }//GEN-LAST:event_jComboBox2dTextUseActionPerformed

    private void jPanelVisualizer2dPlotPrefusePanelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jPanelVisualizer2dPlotPrefusePanelPropertyChange

        // size of panel may have changed; update size of prefuse display

        int prefusePanelWidth = jPanelVisualizer2dPlotPrefusePanel.getSize().width;  //.getWidth();
        int prefusePanelHeight = jPanelVisualizer2dPlotPrefusePanel.getSize().height;  //.getHeight();
        // debug
        //System.out.println("debug: resizing prefuseLSA2dDisplayControlled (event): " + prefusePanelWidth + ", " + prefusePanelHeight);
        //prefuseLSA2dDisplayControlled.setSize(prefusePanelWidth, prefusePanelHeight);
        prefuseLSA2dDisplayControlled.setSize(VIS_WIDTH, VIS_HEIGHT);

    }//GEN-LAST:event_jPanelVisualizer2dPlotPrefusePanelPropertyChange

    private void jSplitPaneContentPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSplitPaneContentPropertyChange

        // when slider position changes, update prefuse size
        int prefusePanelWidth = jPanelVisualizer2dPlotPrefusePanel.getSize().width;  //.getWidth();
        int prefusePanelHeight = jPanelVisualizer2dPlotPrefusePanel.getSize().height;  //.getHeight();
        // debug
        //System.out.println("debug: resizing prefuseLSA2dDisplayControlled (splitpane): " + prefusePanelWidth + ", " + prefusePanelHeight);
        prefuseLSA2dDisplayControlled.setSize(prefusePanelWidth, prefusePanelHeight);


    }//GEN-LAST:event_jSplitPaneContentPropertyChange

    private void jToggleButtonCustomSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonCustomSelectionActionPerformed
        customSelectionModeEnabled = jToggleButtonCustomSelection.getModel().isSelected();
        // debug
        System.out.println("debug: customSelectionModeEnabled: " + customSelectionModeEnabled);

        // reload vis?
        LSA2dVisualizer.rebuildCustomLSA2dVisualizer();

    }//GEN-LAST:event_jToggleButtonCustomSelectionActionPerformed

    private void jPanelVisualizer2dPlotControlsPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jPanelVisualizer2dPlotControlsPropertyChange

        // when slider position changes, update prefuse size
        int prefusePanelWidth = jPanelVisualizer2dPlotPrefusePanel.getSize().width;  //.getWidth();
        int prefusePanelHeight = jPanelVisualizer2dPlotPrefusePanel.getSize().height;  //.getHeight();
        // debug
        //System.out.println("debug: resizing prefuseLSA2dDisplayControlled (splitpane): " + prefusePanelWidth + ", " + prefusePanelHeight);
        prefuseLSA2dDisplayControlled.setSize(prefusePanelWidth, prefusePanelHeight);

    }//GEN-LAST:event_jPanelVisualizer2dPlotControlsPropertyChange

    private void jRadioButtonMenuEditorBiovisCorrelationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuEditorBiovisCorrelationActionPerformed
        switchEditorPanelContents(3);
    }//GEN-LAST:event_jRadioButtonMenuEditorBiovisCorrelationActionPerformed

    private void refreshAllBarCharts() {

//        jPanelVarBarChart1.repaint();
//        jPanelVarBarChart2.repaint();
//        jPanelVarBarChart3.repaint();
//        jPanelVarBarChart4.repaint();

        // refresh datasetVars table instead, since this now holds the barcharts
//        ((DatasetVarsTableModel) datasetVarsTableModel).updateAllRows();
        ((DatasetVarsTableModel) datasetVarsTableModel).refreshAllRows();

    }

    private void switchEditorPanelContents(int selection) {

        // disable all other boxes, enable corresponding box, set Right(or Bottom?) 
        //  component of jSplitPaneContent appropriately

        // 0 == none
        // 1 == annotator
        switch (selection) {
            case 0:
                uncheckEditorRadioButtons();
                jRadioButtonMenuEditorNone.setSelected(true);
                jSplitPaneContent.setBottomComponent(jPanelEditorDummy);
                break;
            case 1:
                uncheckEditorRadioButtons();
                jRadioButtonMenuEditorAnnotator.setSelected(true);
                jSplitPaneContent.setBottomComponent(jPanelTextView);
                break;
            case 2:
                // biovis text panel
                uncheckEditorRadioButtons();
                jRadioButtonMenuEditorBiovis.setSelected(true);
                jSplitPaneContent.setBottomComponent(jPanelBiovisTextData);
                break;
            case 3:
                // biovis correlation panel
                uncheckEditorRadioButtons();
                jRadioButtonMenuEditorBiovisCorrelation.setSelected(true);
                jSplitPaneContent.setBottomComponent(jPanelAnnotatorVarIndiCorrs);
                break;
            default:
                // shouldn't happen
                System.err.println("MainWindow.switchEditorPanelContents: invalid selection: " + selection);
                assert false;
        }


    }

    private void uncheckEditorRadioButtons() {
        jRadioButtonMenuEditorNone.setSelected(false);
        jRadioButtonMenuEditorAnnotator.setSelected(false);
        jRadioButtonMenuEditorBiovis.setSelected(false);
        jRadioButtonMenuEditorBiovisCorrelation.setSelected(false);
    }

    private void activateAppropriateGUIComponents() {

        // disable all controls, set panels to placeholders
        jButtonNewDoc.setEnabled(false);
        jMenuItemSaveAllDocuments.setEnabled(false);
        //jRadioButtonMenuVisualizerRulebuilder.setEnabled(false);
        jRadioButtonMenuVisualizer2d.setEnabled(false);
        jRadioButtonMenuVisualizerLSA2d.setEnabled(false);
        jRadioButtonMenuVisualizerLSA2dWControls.setEnabled(false);
        jRadioButtonMenuEditorAnnotator.setEnabled(false);
        jRadioButtonMenuEditorBiovis.setEnabled(false);

        if (activeDataset == null) {
            // switch to placeholder panels
            switchVisualPanelContents(0);
            switchEditorPanelContents(0);

        } else if (activeDataset.getType().equals(activeDataset.DATASET_TYPE_ARGSUBJ)) {
            // enable editor, force layout, basic lsa, new/save controls
            jButtonNewDoc.setEnabled(true);
            jMenuItemSaveAllDocuments.setEnabled(true);
            jRadioButtonMenuVisualizer2d.setEnabled(true);
            jRadioButtonMenuVisualizerLSA2d.setEnabled(true);
            jRadioButtonMenuEditorAnnotator.setEnabled(true);
            //switchVisualPanelContents(2);
            switchEditorPanelContents(1);

        } else if (activeDataset.getType().equals(activeDataset.DATASET_TYPE_COLON)) {
            // enable bioviz panel, advanced lsa/svd
            jRadioButtonMenuVisualizerLSA2d.setEnabled(true);
            jRadioButtonMenuVisualizerLSA2dWControls.setEnabled(true);
            jRadioButtonMenuEditorBiovis.setEnabled(true);
            switchVisualPanelContents(4);
            switchEditorPanelContents(2);

        }


    }

    private void switchVisualPanelContents(int selection) {

        // disable all other boxes, enable corresponding box, set Left(or Top?) 
        //  component of jSplitPaneContent appropriately

        // 0 == none
        // 1 == rulebuilder
        // 2 == 2d prefuse visualizer

        switch (selection) {
            case 0:
                uncheckVisualizerRadioButtons();
                jRadioButtonMenuVisualizerNone.setSelected(true);
                jSplitPaneContent.setTopComponent(jPanelVisualizerDummy);
                break;
            case 1:
                uncheckVisualizerRadioButtons();
                jRadioButtonMenuVisualizerRulebuilder.setSelected(true);
                jSplitPaneContent.setTopComponent(jPanelVisualizerRulebuilderContainer);
                jSplitPaneContent.setDividerLocation(RULE_HEIGHT + 10);
                break;
            case 2:
                uncheckVisualizerRadioButtons();
                jRadioButtonMenuVisualizer2d.setSelected(true);
                jSplitPaneContent.setTopComponent(jPanelSummaryVisContainer);
                jSplitPaneContent.setDividerLocation(VIS_HEIGHT + 10);
                break;
            case 3:
                uncheckVisualizerRadioButtons();
                jRadioButtonMenuVisualizerLSA2d.setSelected(true);
                jSplitPaneContent.setTopComponent(jPanelVisualizer2dPlot);
                jSplitPaneContent.setDividerLocation(VIS_HEIGHT + 10);
                break;
            case 4:
                uncheckVisualizerRadioButtons();
                jRadioButtonMenuVisualizerLSA2dWControls.setSelected(true);
                jSplitPaneContent.setTopComponent(jPanelVisualizer2dPlotControls);
                jSplitPaneContent.setDividerLocation(VIS_HEIGHT + 10);
                break;
            default:
                // shouldn't happen
                System.err.println("MainWindow.switchVisualPanelContents: invalid selection: " + selection);
                assert false;
        }

    }

    private void uncheckVisualizerRadioButtons() {
        jRadioButtonMenuVisualizerNone.setSelected(false);
        jRadioButtonMenuVisualizerRulebuilder.setSelected(false);
        jRadioButtonMenuVisualizer2d.setSelected(false);
        jRadioButtonMenuVisualizerLSA2d.setSelected(false);
        jRadioButtonMenuVisualizerLSA2dWControls.setSelected(false);
    }

    // for initializing openblocks, prefuse panels
    private void initializeCustomPanels() {
        jPanelVisualizerRulebuilderContainer = WorkspaceController.initializeNewWorkspace(RULE_WIDTH, RULE_HEIGHT);

    }

    private void initializeVisualizationPanel() {

        if (activeDataset.getType().equals(activeDataset.DATASET_TYPE_ARGSUBJ)) {
            ForceZoneVisualizer.rebuildForceZoneVisualizer();
        }
        LSA2dVisualizer.rebuildLSA2dVisualizer();
        // @TODO run only if dataset is med, or fix so that it handles argsubj appropriately
        if (activeDataset.getType().equals(activeDataset.DATASET_TYPE_COLON)) {

            LSA2dVisualizer.rebuildCustomLSA2dVisualizer();

        }

    }

    public void setSelectedDocFromVisualization(int docIndex) {

        // debug
        System.out.println("debug: doc " + docIndex + " (" + activeDataset.getDocuments().get(docIndex).getName() + ") selected in visualization");

        if (activeDataset.getType().equals(activeDataset.DATASET_TYPE_COLON)) {

            selectedDocumentIndex = docIndex;
            jTableDocuments.setRowSelectionInterval(docIndex, docIndex);

            Document selectedDocument = activeDataset.getDocuments().get(docIndex);

            // update the jPanelBiovizTextData stuff
            // update text
            String text = selectedDocument.getText();
            jTextPaneSelectedDocText.setText(text);

            // update vars
//            ((DocumentTableModel)documentTableModel).updateAllRows();
//            ((DocumentVarsTableModel)documentVarsTableModel).updateAllRows();

            // rebuild the term cloud (only if we're not in custom selection mode)
            if (!customSelectionModeEnabled) {
                rebuildTermcloud();

                // don't need to actually call these here methinks, looks like the
                //  event handler should catch doc switch, this would just be redundant
//            RuntimeIndicatorPrediction.buildTemporaryFileForText(activeDataset.getDocuments().get(selectedDocumentIndex).getParsedText());
//            RuntimeIndicatorPrediction.predictIndicatorsForTempFile();

                refreshAllBarCharts();
            }

        } else if (activeDataset.getType().equals(activeDataset.DATASET_TYPE_ARGSUBJ)) {

            // set selected document in the arguing annotator
            selectedDocumentIndex = docIndex;
            jTableDocuments.setRowSelectionInterval(docIndex, docIndex);

        }

    }

    public void updateTermCloudAndBarCharts() {
        rebuildTermcloud();
        refreshAllBarCharts();
        ((ClusterDocsTableModel) clusterDocsTableModel).updateAllRows();
    }

    private void rebuildBioDocTextWithHighlights(String selectedAttr) {


        // get top-ranked terms for selected indicator
        Map<String, Double> termWeightMap = RuntimeIndicatorPrediction.getTermWeightsForIndicator(selectedAttr);

        // find max, min (abs?) vals
        double maxWeight = Double.MIN_VALUE;
        double minWeight = 0;

        // @TODO make sure it's an indicator for which we're doing prediction! else we get an exception here
        Map<String, Boolean> abnormalNameMap = DatasetVarsTableModel.getAbnormalNameMap();
        Map<String, Boolean> predictionNameMap = DatasetVarsTableModel.getPredictionNameMap();
        boolean isSimpleSQVar = false;
        String simpleSQVarRegExpStr = "";
        if (!abnormalNameMap.containsKey(selectedAttr) && predictionNameMap.containsKey(selectedAttr)) {
            for (Double weight : termWeightMap.values()) {

                if (weight > maxWeight) {
                    maxWeight = weight;
                }

                if (Math.abs(weight) < minWeight) {
                    minWeight = Math.abs(weight);
                }

            }

            // debug
            System.out.println("debug: max and min weights for attr " + selectedAttr + ": " + maxWeight + ", " + minWeight);

        } else {
            // not an indicator for which we're doing prediction; so, detect for being a SimpleSQ var with patterns
            Map<String, String> simpleSQVarsToRegExp = SimpleSQMatcher.getVarNameToPatternMap();
            if (simpleSQVarsToRegExp.containsKey(selectedAttr)) {
                isSimpleSQVar = true;
                simpleSQVarRegExpStr = simpleSQVarsToRegExp.get(selectedAttr);
            }

        }


        AbstractDocument abstDoc = (AbstractDocument) (jTextPaneSelectedDocText.getStyledDocument());
        try {
            abstDoc.remove(0, abstDoc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
            System.out.println("err: could not reset doc text");
        }

        String docText = activeDataset.getDocuments().get(selectedDocumentIndex).getText();

        int maxFontSize = 28;
        int minFontSize = 12;

        if (isSimpleSQVar) {
            // highlight regexp
            Pattern varRegExpPattern = Pattern.compile(simpleSQVarRegExpStr);
            Matcher varRegExpMatcher = varRegExpPattern.matcher(docText);
            boolean hasPattern = varRegExpMatcher.find();


            // strategy: while hasPattern == true, continue to look for matches; 
            //  store start, end match indices in a list
            List<Integer> startIndices = new ArrayList<>();
            List<Integer> endIndices = new ArrayList<>();

            while (hasPattern) {

                int start = varRegExpMatcher.start();
                int end = varRegExpMatcher.end();
                String matchedSubstring = varRegExpMatcher.group();

                // debug
                System.out.println("debug: found match in doc " + activeDataset.getDocuments().get(selectedDocumentIndex).getName() + " for attr " + selectedAttr + ": \"" + matchedSubstring + "\"");

                startIndices.add(start);
                endIndices.add(end);

                hasPattern = varRegExpMatcher.find();
            }

            int lastEndIndex = 0;
            if (startIndices.size() > 0) {

                while (startIndices.size() > 0) {

                    // iterate through indices, writing the previous unmatched
                    // portion and following matched portion

                    int plainIndexStart = lastEndIndex;
                    int plainIndexEnd = startIndices.remove(0);
                    int matchedIndexEnd = endIndices.remove(0);

                    // unmatched
                    try {
                        int fontSize = minFontSize;
                        SimpleAttributeSet attrSet = new SimpleAttributeSet();
                        StyleConstants.setFontSize(attrSet, fontSize);
                        abstDoc.insertString(abstDoc.getLength(), docText.substring(plainIndexStart, plainIndexEnd),
                                attrSet);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                        System.out.println("err: could not add unweighted term to report panel: "
                                + docText.substring(plainIndexStart,
                                plainIndexEnd));
                    }

                    // matched
                    try {
//						double weight = 1.0;
                        double weight = 0.8;
                        int fontSize = (int) (maxFontSize * weight);
                        if (fontSize < minFontSize) {
                            fontSize = minFontSize;
                        }
                        SimpleAttributeSet attrSet = new SimpleAttributeSet();
                        StyleConstants.setFontSize(attrSet, fontSize);
                        StyleConstants.setBackground(attrSet, new Color(0, 255,
                                255, (int) (255 * weight)));
                        abstDoc.insertString(abstDoc.getLength(), docText.substring(plainIndexEnd, matchedIndexEnd),
                                attrSet);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                        System.out.println("err: could not add weighted term to report panel: "
                                + docText.substring(plainIndexEnd,
                                matchedIndexEnd));
                    }

                    lastEndIndex = matchedIndexEnd;

                }

                // print the last bit of unmatched text, if present (should be)
                try {
                    int fontSize = minFontSize;
                    SimpleAttributeSet attrSet = new SimpleAttributeSet();
                    StyleConstants.setFontSize(attrSet, fontSize);
                    abstDoc.insertString(abstDoc.getLength(), docText.substring(lastEndIndex),
                            attrSet);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                    System.out.println("err: could not add unweighted term to report panel: "
                            + docText.substring(lastEndIndex));
                }

            } else {
                // regexp doesn't match, so just load plain doc
                try {
                    int fontSize = minFontSize;
                    SimpleAttributeSet attrSet = new SimpleAttributeSet();
                    StyleConstants.setFontSize(attrSet, fontSize);
                    abstDoc.insertString(abstDoc.getLength(), docText, attrSet);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                    System.out.println("err: could not load plain doc in panel (regexp not matched)");
                }
            }
        } else {
            // highlight sufficiently-weighted terms, if any
            Scanner docTextLineSplitter = new Scanner(docText);
            while (docTextLineSplitter.hasNextLine()) {
                String line = docTextLineSplitter.nextLine();
                Scanner lineSplitter = new Scanner(line);
                while (lineSplitter.hasNext()) {

                    String term = lineSplitter.next();

                    // if term is highly weighted, draw with emphasis;
                    // otherwise, draw normally
                    double weight = 0.;
                    double weightDiffMult = 1.3; // the larger this is, the
                    // higher the threshold for
                    // highlighting
                    if (!abnormalNameMap.containsKey(selectedAttr)
                            && predictionNameMap.containsKey(selectedAttr)
                            && ((termWeightMap.containsKey(term) && (weight = termWeightMap.get(term)) > (maxWeight - ((maxWeight - minWeight) / weightDiffMult))) || (termWeightMap.containsKey(term.toLowerCase()) && (weight = termWeightMap.get(term.toLowerCase())) > (maxWeight - ((maxWeight - minWeight) / weightDiffMult))))) {

                        // if term is weighted sufficiently for the indicator
                        try {
                            int fontSize = (int) (maxFontSize * (weight / maxWeight));
                            if (fontSize < minFontSize) {
                                fontSize = minFontSize;
                            }
                            SimpleAttributeSet attrSet = new SimpleAttributeSet();
                            StyleConstants.setFontSize(attrSet, fontSize);
                            StyleConstants.setBackground(attrSet, new Color(0,
                                    255, 255,
                                    (int) (255 * (weight / maxWeight))));
                            abstDoc.insertString(abstDoc.getLength(), term
                                    + " ", attrSet);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                            System.out.println("err: could not add weighted term to report panel: "
                                    + term);
                        }

                    } else {

                        // term is not weighted sufficiently for indicator
                        try {
                            int fontSize = minFontSize;
                            SimpleAttributeSet attrSet = new SimpleAttributeSet();
                            StyleConstants.setFontSize(attrSet, fontSize);
                            abstDoc.insertString(abstDoc.getLength(), term
                                    + " ", attrSet);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                            System.out.println("err: could not add unweighted term to report panel: "
                                    + term);
                        }

                    }

                }

                // add a newline at the end of the line
                try {
                    int fontSize = minFontSize;
                    SimpleAttributeSet attrSet = new SimpleAttributeSet();
                    StyleConstants.setFontSize(attrSet, fontSize);
                    abstDoc.insertString(abstDoc.getLength(), "\n", attrSet);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                    System.out.println("err: could not add newline to report panel");
                }

            }
        }

//        int numTopTuples = 40;
////            String topTupleText = "";
//        double maxScore = clusterTuples.get(0).getScore();
//        int maxFontSize = 44;
//        for (int t = 0; t < numTopTuples; t++) {
//            TermScoreTuple tuple = clusterTuples.get(t);
//            double score = tuple.getScore();
//            String term = tuple.getTerm();
//            
//            int fontSize = (int) (maxFontSize * (score / maxScore));
//            SimpleAttributeSet attrSet = new SimpleAttributeSet();
//            StyleConstants.setFontSize(attrSet, fontSize);
//            try {
//                abstDoc.insertString(abstDoc.getLength(), term + ", ", attrSet);
//            } catch (BadLocationException e) {
//                e.printStackTrace();
//                System.out.println("err: could not add term to termcloud: " + tuple.toString());
//            }
////                topTupleText += clusterTuples.get(t).toString() + "\n";
//        }

    }

    private void rebuildTermcloud() {

        // get cluster info
        List<Integer> clusterList = null;  // index == doc index, value == cluster membership
        List<TermScoreTuple> clusterTuples = null;
        if (!customSelectionModeEnabled) {
            clusterList = LSA2dVisualizer.getDatasetClusters();
            clusterTuples = LSA2dProjector.getTopTfIdfTermsForCluster(activeDataset, clusterList, getNumClustersLDA2dCustom(), LSA2dVisualizer.getDatasetClusters().get(selectedDocumentIndex));
        } else {
            // custom selection mode enabled; get set of currently selected documents 
            // 2 "clusters", selected and unselected
            clusterList = LSA2dVisualizer.getCustomSelectedPoints();
            clusterTuples = LSA2dProjector.getTopTfIdfTermsForCluster(activeDataset, clusterList, 2, 1);
        }

        AbstractDocument abstDoc = (AbstractDocument) (jTextPaneClusterText.getStyledDocument());
        try {
            abstDoc.remove(0, abstDoc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
            System.out.println("err: could not reset termcloud");
        }
        int numTopTuples = 40;
//            String topTupleText = "";

        TermScoreTuple.removeDupTuples(clusterTuples, numTopTuples * 2);

        if (clusterTuples.size() > 0) {
            double maxScore = clusterTuples.get(0).getScore();
            int maxFontSize = 44;
            for (int t = 0; t < numTopTuples; t++) {
                TermScoreTuple tuple = clusterTuples.get(t);
                double score = tuple.getScore();
                String term = tuple.getTerm();

                int fontSize = (int) (maxFontSize * (score / maxScore));
                SimpleAttributeSet attrSet = new SimpleAttributeSet();
                StyleConstants.setFontSize(attrSet, fontSize);
                try {
                    abstDoc.insertString(abstDoc.getLength(), term + ", ",
                            attrSet);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                    System.out.println("err: could not add term to termcloud: "
                            + tuple.toString());
                }
                // topTupleText += clusterTuples.get(t).toString() + "\n";
            }
//            jTextPaneClusterText.setText(topTupleText);
        } else {
            // this can happen if no proper cluster is selected, or if it's empty
            // just clear the term cloud?
            SimpleAttributeSet attrSet = new SimpleAttributeSet();
            int fontSize = 12;
            StyleConstants.setFontSize(attrSet, fontSize);
            try {
                abstDoc.insertString(abstDoc.getLength(), "",
                        attrSet);
            } catch (BadLocationException e) {
                e.printStackTrace();
                System.out.println("err: could not clear termcloud ");
            }
        }

    }

    public Document getSelectedDocument() {
        if (jTableDocuments.getSelectedRow() != -1) {
            return activeDataset.getDocuments().get(jTableDocuments.getSelectedRow());
        }
        return null;

    }

    public int getNumClustersLDA2dCustom() {
        return (Integer) jSpinnerNumClusters.getValue();
    }

    public int getNumClustersForceVis() {
        return (Integer) jSpinnerNumClustersForceVis.getValue();
    }

    public double getEdgeSimThreshold() {
        int min = jSliderEdgeSimThreshold.getMinimum();
        int max = jSliderEdgeSimThreshold.getMaximum();
        int value = jSliderEdgeSimThreshold.getValue();
        double ratio = ((double) value - min) / ((double) max - min);
        return ratio;
    }

    public boolean get2dTextUsePosition() {
        if (((String) jComboBox2dTextUse.getSelectedItem()).equalsIgnoreCase("position") || ((String) jComboBox2dTextUse.getSelectedItem()).equalsIgnoreCase("both")) {
            return true;
        }
        return false;
    }

    public boolean get2dTextUseColor() {
        if (((String) jComboBox2dTextUse.getSelectedItem()).equalsIgnoreCase("color") || ((String) jComboBox2dTextUse.getSelectedItem()).equalsIgnoreCase("both")) {
            return true;
        }
        return false;
    }

    public List<String> getAttrNamesFor2dProjection() {
        return ((DatasetVarsTableModel) datasetVarsTableModel).getActiveLayoutAttrs();
    }

    public List<String> getAttrNamesFor2dClustering() {
        return ((DatasetVarsTableModel) datasetVarsTableModel).getActiveClusterAttrs();
    }

    public List<String> getCurrentClusterDocNames() {

        if (activeDataset != null) {

            List<Integer> lastClusterIndices = null;
            int currentCluster = -1;
            if (!customSelectionModeEnabled) {
                // for now: just get the indices from the last execution of clusterer
                //lastClusterIndices = UnsupervisedClusterer.getLastClusterAssignments();
                // for uniformity: pull cluster assignments from visualizer
                lastClusterIndices = LSA2dVisualizer.getDatasetClusters();
                currentCluster = lastClusterIndices.get(selectedDocumentIndex);
            } else {
                lastClusterIndices = LSA2dVisualizer.getCustomSelectedPoints();
                currentCluster = 1;
            }

            List<String> currentClusterDocNames = new ArrayList<>();
            // build list of all other clusters in currentCluster
            for (int d = 0; d < lastClusterIndices.size(); d++) {
                int docClusterIndex = lastClusterIndices.get(d);
                if (docClusterIndex == currentCluster) {
                    currentClusterDocNames.add(activeDataset.getDocuments().get(d).getName());
                }
            }
            return currentClusterDocNames;


        } else {
            return null;
        }

    }

    public List<Integer> getCurrentClusterDocIndices() {

        if (activeDataset != null) {

            List<Integer> lastClusterIndices = null;
            int currentCluster = -1;
            if (!customSelectionModeEnabled) {
                // for now: just get the indices from the last execution of clusterer
                //lastClusterIndices = UnsupervisedClusterer.getLastClusterAssignments();
                // for uniformity: pull cluster assignments from visualizer
                lastClusterIndices = LSA2dVisualizer.getDatasetClusters();
                currentCluster = lastClusterIndices.get(selectedDocumentIndex);
            } else {
                lastClusterIndices = LSA2dVisualizer.getCustomSelectedPoints();
                currentCluster = 1;
            }

            List<Integer> currentClusterDocIndices = new ArrayList<>();
            // build list of all other clusters in currentCluster
            for (int d = 0; d < lastClusterIndices.size(); d++) {
                int docClusterIndex = lastClusterIndices.get(d);
                if (docClusterIndex == currentCluster) {
                    currentClusterDocIndices.add(d);
                }
            }
            return currentClusterDocIndices;


        } else {
            return new ArrayList<Integer>();
        }

    }

    public static void populate2dPlotControls() {
        // assumes we have a bioviz dataset loaded
        // populate table with all vars, indicators from dataset
        // also, add row for text
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * parse command-line args (if any)
         */
        // @TODO

        /*
         * initialize back-end datastructures
         */
        //activeDatasets = new ArrayList<>();
        //datasetTableModel = new DatasetTableModel();

        documentTableModel = new DocumentTableModel();
        textInstanceTableModel = new TextInstanceTableModel();
        prefuseZoneForceDisplay = new Display(new Visualization());
        prefuseZoneForceDisplay.setSize(VIS_WIDTH, VIS_HEIGHT);
        prefuseLSA2dDisplay = new Display(new Visualization());
        prefuseLSA2dDisplay.setSize(VIS_WIDTH, VIS_HEIGHT);

        datasetVarsTableModel = new DatasetVarsTableModel();
        documentVarsTableModel = new DocumentVarsTableModel();
        clusterDocsTableModel = new ClusterDocsTableModel();
        prefuseLSA2dDisplayControlled = new Display(new Visualization());
        prefuseLSA2dDisplayControlled.setSize(VIS_WIDTH, VIS_HEIGHT);

//        varBarChart1 = new VarBarChart();
//        varBarChart2 = new VarBarChart();
//        varBarChart3 = new VarBarChart();
//        varBarChart4 = new VarBarChart();

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                window = new MainWindow();
                window.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2dPlotRebuild;
    private javax.swing.JButton jButtonAddTextInstance;
    private javax.swing.JButton jButtonClearArgument;
    private javax.swing.JButton jButtonDeselectVars;
    private javax.swing.JButton jButtonNewDoc;
    private javax.swing.JButton jButtonNext;
    private javax.swing.JButton jButtonSaveDocPredIndis;
    private javax.swing.JButton jButtonUpdateText;
    private javax.swing.JCheckBox jCheckBoxArguing;
    private javax.swing.JCheckBox jCheckBoxLabelUnclear;
    private javax.swing.JComboBox jComboBox2dTextUse;
    private javax.swing.JFileChooser jFileChooserDoclist;
    private javax.swing.JFileChooser jFileChooserNewDoc;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenuBar jMenuBarMain;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenu jMenuEditor;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenuItem jMenuItemLoadDoclist;
    private javax.swing.JMenuItem jMenuItemSaveAllDocuments;
    private javax.swing.JMenu jMenuVisualizer;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelAnnotatorVarIndiCorrs;
    private javax.swing.JPanel jPanelBiovisTextData;
    private javax.swing.JPanel jPanelContent;
    private javax.swing.JPanel jPanelDatasets;
    private javax.swing.JPanel jPanelEditorDummy;
    private javax.swing.JPanel jPanelLabelChooser;
    private javax.swing.JPanel jPanelSummaryVis;
    private javax.swing.JPanel jPanelSummaryVisContainer;
    private javax.swing.JPanel jPanelTextView;
    private javax.swing.JPanel jPanelVisualizer2dPlot;
    private javax.swing.JPanel jPanelVisualizer2dPlotControls;
    private javax.swing.JPanel jPanelVisualizer2dPlotPrefusePanel;
    private javax.swing.JPanel jPanelVisualizerDummy;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuEditorAnnotator;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuEditorBiovis;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuEditorBiovisCorrelation;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuEditorNone;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuLoadAnnotator;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuVisualizer2d;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuVisualizerLSA2d;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuVisualizerLSA2dWControls;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuVisualizerNone;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuVisualizerRulebuilder;
    private javax.swing.JScrollPane jScrollPane2dPlotControls;
    private javax.swing.JScrollPane jScrollPaneClusterDocs;
    private javax.swing.JScrollPane jScrollPaneClusterText;
    private javax.swing.JScrollPane jScrollPaneDocuments;
    private javax.swing.JScrollPane jScrollPaneElaboration;
    private javax.swing.JScrollPane jScrollPaneLabelChooser;
    private javax.swing.JScrollPane jScrollPaneSelectedDocText;
    private javax.swing.JScrollPane jScrollPaneSelectedDocVars;
    private javax.swing.JScrollPane jScrollPaneTextChooser;
    private javax.swing.JScrollPane jScrollPaneTextEditor;
    private javax.swing.JScrollPane jScrollPaneVarIndiCorrs;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSlider jSliderEdgeSimThreshold;
    private javax.swing.JSpinner jSpinnerNumClusters;
    private javax.swing.JSpinner jSpinnerNumClustersForceVis;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JSplitPane jSplitPane4;
    private javax.swing.JSplitPane jSplitPane5;
    private javax.swing.JSplitPane jSplitPaneAnnotator;
    private javax.swing.JSplitPane jSplitPaneAnnotator2;
    private javax.swing.JSplitPane jSplitPaneContent;
    private javax.swing.JSplitPane jSplitPaneMain;
    private javax.swing.JTable jTable2dPlotControls;
    private javax.swing.JTable jTableClusterDocs;
    private javax.swing.JTable jTableDocuments;
    private javax.swing.JTable jTableSelectedDocVars;
    private javax.swing.JTable jTableTextChooser;
    private javax.swing.JTextArea jTextAreaElaboration;
    private javax.swing.JTextArea jTextAreaTextEditor;
    private javax.swing.JTextPane jTextPaneClusterText;
    private javax.swing.JTextPane jTextPaneSelectedDocText;
    private javax.swing.JToggleButton jToggleButtonCustomSelection;
    private javax.swing.JTree jTreeLabelChooser;
    private javax.swing.JTree jTreeVarIndiCorrs;
    // End of variables declaration//GEN-END:variables
}
