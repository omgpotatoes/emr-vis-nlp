package emr_vis_nlp.main;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.view.doc_grid.DocGridTableSelectorModel;
import emr_vis_nlp.view.doc_grid.DocumentGrid;
import emr_vis_nlp.view.doc_table.AttrTableModel;
import emr_vis_nlp.view.doc_table.DocTableModel;
import emr_vis_nlp.view.glasspane.MainViewGlassPane;
import emr_vis_nlp.view.var_bar_chart.JTableCombos;
import emr_vis_nlp.view.var_bar_chart.VarDatasetRatioRenderer;
import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import prefuse.util.FontLib;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JSearchPanel;
import prefuse.util.ui.UILib;

/**
 * Main top-level tab-based view for the emr-vis-nlp system.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class MainTabbedView extends javax.swing.JFrame {

    /**
     * this MainView's governing MainController
     */
    private MainController controller;
    /**
     * backing model for the simple document-oriented table view
     */
    private TableModel docTableModel;
    /**
     * sorter for the document-oriented table view
     */
    private TableRowSorter<TableModel> docTableModelSorter;
    /**
     * backing model for the simple attribute selection table
     */
    private AttrTableModel attrSelectionTableModel;
    /**
     * backing model for the document grid attribute selection table
     */
    private DocGridTableSelectorModel docGridSelectionTableModel;
    /*
     * glasspane onto which various views may write
     */
    private MainViewGlassPane glassPane;
    
    /*
     * object for document-grid layout
     */
    private DocumentGrid documentGrid;
    /*
     * panel for supplying a search query
     */
    private JSearchPanel search;
    
    /**
     * Creates new form MainTabbedView
     */
    public MainTabbedView() {
        super("emr-vis-nlp | main");

        this.controller = MainController.getMainController();

        // initialize GUI components
        initComponents();

        // initialize event listeners
        initListeners();
        
        // init glasspane
//        glassPane = new MainViewGlassPane();
        glassPane = MainViewGlassPane.getGlassPane();
        glassPane.addMouseListener(glassPane);
        setGlassPane(glassPane);

    }

    private void initListeners() {
        // area for explicitly initializing custom listeners
        
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelDocTable = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableSimpleDocs = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableAttrSelection = new javax.swing.JTable();
        jButtonSelectNoneDocTable = new javax.swing.JButton();
        jButtonSelectAllDocTable = new javax.swing.JButton();
        jPanelDocGrid = new javax.swing.JPanel();
        jSplitPaneDocGrid = new javax.swing.JSplitPane();
        jPanelDocGridDummy = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTableAttrSelection3 = new JTableCombos();
        jButtonSelectAllDocGrid = new javax.swing.JButton();
        jButtonSelectNoneDocGrid = new javax.swing.JButton();
        jButtonReset = new javax.swing.JButton();
        jPanelSearchContainer = new javax.swing.JPanel();
        jProgressBar2 = new javax.swing.JProgressBar();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemLoadDataset = new javax.swing.JMenuItem();
        jMenuML = new javax.swing.JMenu();
        jMenuItemLoadPredictor = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("emr-vis-nlp");

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jTableSimpleDocs.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jTableSimpleDocs.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTableSimpleDocs.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTableSimpleDocs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableSimpleDocsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTableSimpleDocs);

        jTableAttrSelection.setAutoCreateRowSorter(true);
        jTableAttrSelection.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Attribute Name", "Enabled?"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTableAttrSelection);

        jButtonSelectNoneDocTable.setText("Select None");
        jButtonSelectNoneDocTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectNoneDocTableActionPerformed(evt);
            }
        });

        jButtonSelectAllDocTable.setText("Select All");
        jButtonSelectAllDocTable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectAllDocTableActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelDocTableLayout = new javax.swing.GroupLayout(jPanelDocTable);
        jPanelDocTable.setLayout(jPanelDocTableLayout);
        jPanelDocTableLayout.setHorizontalGroup(
            jPanelDocTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDocTableLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDocTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                    .addComponent(jButtonSelectNoneDocTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonSelectAllDocTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelDocTableLayout.setVerticalGroup(
            jPanelDocTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelDocTableLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelDocTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
                    .addGroup(jPanelDocTableLayout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSelectAllDocTable)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSelectNoneDocTable)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Document Table", jPanelDocTable);

        jPanelDocGrid.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jPanelDocGridPropertyChange(evt);
            }
        });

        jSplitPaneDocGrid.setDividerLocation(350);
        jSplitPaneDocGrid.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jSplitPaneDocGridPropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanelDocGridDummyLayout = new javax.swing.GroupLayout(jPanelDocGridDummy);
        jPanelDocGridDummy.setLayout(jPanelDocGridDummyLayout);
        jPanelDocGridDummyLayout.setHorizontalGroup(
            jPanelDocGridDummyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 439, Short.MAX_VALUE)
        );
        jPanelDocGridDummyLayout.setVerticalGroup(
            jPanelDocGridDummyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 541, Short.MAX_VALUE)
        );

        jSplitPaneDocGrid.setRightComponent(jPanelDocGridDummy);

        jTableAttrSelection3.setAutoCreateRowSorter(true);
        jTableAttrSelection3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Attribute Name", "Enabled?"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableAttrSelection3.setDefaultRenderer(JPanel.class, new VarDatasetRatioRenderer());
        jScrollPane4.setViewportView(jTableAttrSelection3);

        jButtonSelectAllDocGrid.setText("Select All");
        jButtonSelectAllDocGrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectAllDocGridActionPerformed(evt);
            }
        });

        jButtonSelectNoneDocGrid.setText("Select None");
        jButtonSelectNoneDocGrid.setEnabled(false);
        jButtonSelectNoneDocGrid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectNoneDocGridActionPerformed(evt);
            }
        });

        jButtonReset.setText("Reset");
        jButtonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelSearchContainerLayout = new javax.swing.GroupLayout(jPanelSearchContainer);
        jPanelSearchContainer.setLayout(jPanelSearchContainerLayout);
        jPanelSearchContainerLayout.setHorizontalGroup(
            jPanelSearchContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanelSearchContainerLayout.setVerticalGroup(
            jPanelSearchContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 32, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelSearchContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonSelectAllDocGrid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonReset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonSelectNoneDocGrid, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                    .addComponent(jProgressBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelSearchContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSelectAllDocGrid)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSelectNoneDocGrid)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonReset)
                .addGap(18, 18, 18)
                .addComponent(jProgressBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jSplitPaneDocGrid.setLeftComponent(jPanel1);

        javax.swing.GroupLayout jPanelDocGridLayout = new javax.swing.GroupLayout(jPanelDocGrid);
        jPanelDocGrid.setLayout(jPanelDocGridLayout);
        jPanelDocGridLayout.setHorizontalGroup(
            jPanelDocGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneDocGrid, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanelDocGridLayout.setVerticalGroup(
            jPanelDocGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneDocGrid, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jTabbedPane1.addTab("Document Grid", jPanelDocGrid);

        jTabbedPane1.setSelectedIndex(1);

        jMenuFile.setText("File");

        jMenuItemLoadDataset.setText("Open Dataset from Doclist...");
        jMenuItemLoadDataset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoadDatasetActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemLoadDataset);

        jMenuBar1.add(jMenuFile);

        jMenuML.setText("ML");

        jMenuItemLoadPredictor.setText("Load Prediction Model From File...");
        jMenuItemLoadPredictor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoadPredictorActionPerformed(evt);
            }
        });
        jMenuML.add(jMenuItemLoadPredictor);

        jMenuBar1.add(jMenuML);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("main tabular view");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemLoadDatasetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoadDatasetActionPerformed
        int returnVal = jFileChooser1.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser1.getSelectedFile();

            // send doclist file to controller, instruct it to load new model
            controller.setModelFromDoclist(file);

        } else {
            System.out.println("debug: \"Choose Doclist\" action cancelled by user");
        }
    }//GEN-LAST:event_jMenuItemLoadDatasetActionPerformed

    private void jMenuItemLoadPredictorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoadPredictorActionPerformed
        
        // load a new MLPredictor from file
        int returnVal = jFileChooser1.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser1.getSelectedFile();

            // send doclist file to controller, instruct it to load new model
            controller.setPredictor(file);

        } else {
            System.out.println("debug: \"Load Predictor\" action cancelled by user");
        }
        
        
        
        
    }//GEN-LAST:event_jMenuItemLoadPredictorActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged

        // if selection is doc grid, redraw at appropriate size
        if (jTabbedPane1.getSelectedIndex() == 2) {
            updateDocumentGridSize();
        }
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void jPanelDocGridPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jPanelDocGridPropertyChange
        updateDocumentGridSize();
    }//GEN-LAST:event_jPanelDocGridPropertyChange

    private void jSplitPaneDocGridPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jSplitPaneDocGridPropertyChange
        updateDocumentGridSize();
    }//GEN-LAST:event_jSplitPaneDocGridPropertyChange

    private void jButtonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetActionPerformed
        controller.resetDocGridView();
    }//GEN-LAST:event_jButtonResetActionPerformed

    private void jButtonSelectNoneDocGridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectNoneDocGridActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonSelectNoneDocGridActionPerformed

    private void jButtonSelectAllDocGridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectAllDocGridActionPerformed
        controller.enableAllDocs();
    }//GEN-LAST:event_jButtonSelectAllDocGridActionPerformed

    private void jButtonSelectAllDocTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectAllDocTableActionPerformed
        if (attrSelectionTableModel != null) {
            attrSelectionTableModel.selectAll();
        }
    }//GEN-LAST:event_jButtonSelectAllDocTableActionPerformed

    private void jButtonSelectNoneDocTableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectNoneDocTableActionPerformed
        if (attrSelectionTableModel != null) {
            attrSelectionTableModel.selectNone();
        }
    }//GEN-LAST:event_jButtonSelectNoneDocTableActionPerformed

    private void jTableSimpleDocsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableSimpleDocsMouseClicked

        // listens for double-click on an item from the table, in order to load the details window for the given document
        int numClicks = evt.getClickCount();
        // get selected table index
        int selectedDocRowIndexRaw = jTableSimpleDocs.getSelectedRow();

        if (numClicks >= 2) {

            int selectedDocRowIndexModel = jTableSimpleDocs.convertRowIndexToModel(selectedDocRowIndexRaw);
            int globalDocIndex = selectedDocRowIndexModel;
            try {
                globalDocIndex = ((DocTableModel) docTableModel).getGlobalIndexForModelRow(selectedDocRowIndexModel);
            } catch (ClassCastException e) {
                e.printStackTrace();
                System.out.println("err?: could not cast docTableModel?");
            }

            // debug
//            System.out.println("debug: double++-click (" + numClicks + ") on selected row: " + globalDocIndex +" (raw="+selectedDocRowIndexRaw+", model="+selectedDocRowIndexModel+")");

//            // create and display new popup for selected document
//            final JFrame newPopup = controller.buildDocDetailsWindow(globalDocIndex);
//            java.awt.EventQueue.invokeLater(new Runnable() {
//                public void run() {
//                    newPopup.setVisible(true);
//                }
//            });

            // let the controller handle this
            controller.displayDocDetailsWindow(globalDocIndex);

        }
    }//GEN-LAST:event_jTableSimpleDocsMouseClicked
    
    
    
    /******* document-grid-related methods *******/
    
    public void updateDocumentGridSize() {
        // update size of document grid
        if (documentGrid != null) {
            int newWidth = documentGrid.getWidth();
            int newHeight = documentGrid.getHeight();
            documentGrid.resetSize(newWidth, newHeight);
        }
        // also, update the selection table (will need to redraw VarBarChartForCells)
        if (docGridSelectionTableModel != null) {
            docGridSelectionTableModel.resetVarBarCharts();
        }
    }
    
    public void rebuildDocumentGridView() {
        documentGrid = controller.buildDocumentGrid();
        jSplitPaneDocGrid.setBottomComponent(documentGrid);
//        nestedGrid = controller.buildNestedGrid();
//        jSplitPaneDocGrid.setBottomComponent(nestedGrid);
        
//        boolean enableFisheye = jToggleButtonFisheye.isSelected();
//        controller.setFisheyeEnabled(enableFisheye);
        updateDocumentGridSize();
//        controller.updateDocumentGrid();
//        updateDocumentGridSize();
        
        // rebuild the JSearchPanel
        // adopted from TreeMap.java demo
        if (documentGrid != null) {
            search = controller.getDocumentGrid().getSearchQuery().createSearchPanel();
            search.setShowResultCount(true);
            search.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 0));
            search.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));
            final JFastLabel title = new JFastLabel("");
            title.setVerticalAlignment(SwingConstants.BOTTOM);
//        final JFastLabel title = new JFastLabel("                 ");
//        title.setPreferredSize(new Dimension(350, 20));
//        title.setVerticalAlignment(SwingConstants.BOTTOM);
//        title.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
//        title.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));
//        Box box = UILib.getBox(new Component[]{title,search}, true, 10, 3, 0);
            Box box = UILib.getBox(new Component[]{search}, true, 0, 0, 0);
            // TODO fix visibility!!
            jPanelSearchContainer.removeAll();
            jPanelSearchContainer.invalidate();
            jPanelSearchContainer.setLayout(new FlowLayout());
            jPanelSearchContainer.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            jPanelSearchContainer.add(box);
//        jPanelSearchContainer.add(new JButton("testbutton")); // for testing only
            jPanelSearchContainer.revalidate();
            UILib.setColor(jPanelSearchContainer, this.getBackground(), Color.black);
//        repaint();
        }
    }
    
    
    
    /******* document-table-related methods *******/
    
    public void rebuildDocumentTable() {

        // query for new backend for the simple document table, load
        docTableModel = controller.buildSimpleDocTableModel();
        jTableSimpleDocs.setModel(docTableModel);

        // build sorter for the table
        docTableModelSorter = new TableRowSorter<>(docTableModel);
        jTableSimpleDocs.setRowSorter(docTableModelSorter);

        // TODO build filter for table
        // idea: rather than writing custom filter, simply set "text" as a 
        //  row, allow for sorting in this manner?
        // similarly, allow for sorting by values, by augmenting attribute 
        //  cells with attr name?

    }

    
    
    /******* attribute-related methods? *******/
    
    public void resetAttributeSelectionTable() {

        // tableModel for document table-based view
        AttrTableModel newAttrSelectionTableModel = controller.buildSimpleAttrSelectionTableModel();
//        TableModel newAttrSelectionTableModel = controller.buildSimpleAttrSelectionTableModelFocusOnly();
        attrSelectionTableModel = newAttrSelectionTableModel;
        jTableAttrSelection.setModel(attrSelectionTableModel);
        
        // tableModel for grid-based view
        //jTableAttrSelection2.setModel(attrSelectionTableModel);
        DocGridTableSelectorModel newDocGridSelectionTableModel = controller.buildSimpleDocGridSelectionTableModel();
        docGridSelectionTableModel = newDocGridSelectionTableModel;
        jTableAttrSelection3.setModel(docGridSelectionTableModel);

    }
    
    
    
    /******* glasspane-related views *******/
    
    @Override
    public MainViewGlassPane getGlassPane() {
        return glassPane;
    }

    
    
    
    
    public void resetAllViews() {

        // reset all views in this MainView; 
        // query MainController for new data (model may have changed)

        // rebuild simple document table view
        rebuildDocumentTable();

        // query for new backend for attribute table, load
        resetAttributeSelectionTable();

        // reload document grid view
        rebuildDocumentGridView();
        
        // TODO layout additional panels as-needed...

    }
    
    public void attributeSelectionChanged() {

        // update relevant panes

        // update simple document table
        // ask controller for a new model, with current column selections
        rebuildDocumentTable();
        // update document grid
        rebuildDocumentGridView();

    }
    
    public void axisAttrSelectionChanged() {
        // update relevant panes
//        // TODO don't rebuild whole grid, just update existing grid! (for animation)
          // ^ must fix bug in axislayout; what if # of categories differs between previous and current selected attrs?
        rebuildDocumentGridView();
//        controller.updateDocumentGrid();
        updateDocumentGridSize();
    }
    
    public void setSearchText(String text) {
        if (search != null) {
            search.setQuery(text);
        }
    }
    
    public void startProgressBar() {
        jProgressBar2.setIndeterminate(true);
    }
    
    public void stopProgressBar() {
        jProgressBar2.setIndeterminate(false);
//        jProgressBar2.setValue(0);
    }
    

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        
        // simply use the system's look and feel
//        try {
//            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(MainTabbedView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(MainTabbedView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(MainTabbedView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(MainTabbedView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
        
        try {
            // Set System L&F
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} 
	catch (Exception e) {
	    System.err.println("Could not set Look and Feel");
	    e.printStackTrace();
	    System.exit(-1);
	}
        
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
            java.util.logging.Logger.getLogger(MainTabbedView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainTabbedView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainTabbedView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainTabbedView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        
        // setup controller
        MainController controller = MainController.getMainController();

        // setup view
        final MainTabbedView mainView = new MainTabbedView();
        controller.setView(mainView);

        // if doclist was passed on command line, load it
        // TODO do proper command-line parsing, ie via Apache Commons
        if (args.length != 0) {
            String doclistPathFromArgs = args[0];
            controller.setModelFromDoclist(new File(doclistPathFromArgs));
            
        }

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                ((MainTabbedView) mainView).setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonReset;
    private javax.swing.JButton jButtonSelectAllDocGrid;
    private javax.swing.JButton jButtonSelectAllDocTable;
    private javax.swing.JButton jButtonSelectNoneDocGrid;
    private javax.swing.JButton jButtonSelectNoneDocTable;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuItemLoadDataset;
    private javax.swing.JMenuItem jMenuItemLoadPredictor;
    private javax.swing.JMenu jMenuML;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelDocGrid;
    private javax.swing.JPanel jPanelDocGridDummy;
    private javax.swing.JPanel jPanelDocTable;
    private javax.swing.JPanel jPanelSearchContainer;
    private javax.swing.JProgressBar jProgressBar2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSplitPane jSplitPaneDocGrid;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableAttrSelection;
    private javax.swing.JTable jTableAttrSelection3;
    private javax.swing.JTable jTableSimpleDocs;
    // End of variables declaration//GEN-END:variables
}
