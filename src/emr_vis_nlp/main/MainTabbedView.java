
package emr_vis_nlp.main;

import emr_vis_nlp.controller.DefaultMainController;
import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.model.MainModel;
import emr_vis_nlp.model.NullMainModel;
import emr_vis_nlp.view.MainView;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Main tab-based view for the emr-vis-nlp system.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class MainTabbedView extends javax.swing.JFrame implements MainView {

    /**
     * this MainView's governing MainController
     */
    private MainController controller;
    /** backing model for the simple document-oriented table view */
    private TableModel docTableModel;
    /** sorter for the document-oriented table view */
    private TableRowSorter<TableModel> docTableModelSorter;
    /** backing model for the simple attribute selection table */
    private TableModel attrSelectionTableModel;
    
    /**
     * Creates new form MainTabbedView
     */
    public MainTabbedView(MainController controller) {
        
        this.controller = controller;
        
        // initialize GUI components
        initComponents();
        
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
        jPanel1 = new javax.swing.JPanel();
        jTextFieldSearch = new javax.swing.JTextField();
        jLabelSearch = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableSimpleDocs = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableAttrSelection = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemLoadDataset = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(800, 600));

        jTextFieldSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldSearchActionPerformed(evt);
            }
        });

        jLabelSearch.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelSearch.setText("Search:");

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabelSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldSearch))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 598, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabelSearch)
                            .addComponent(jTextFieldSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Document Table", jPanel1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 795, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 543, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Document Map", jPanel2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 795, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 543, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Attribute Graph", jPanel3);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 795, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 543, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Attribute Graph 2", jPanel4);

        jMenuFile.setText("File");

        jMenuItemLoadDataset.setText("Open Dataset from Doclist...");
        jMenuItemLoadDataset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoadDatasetActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuItemLoadDataset);

        jMenuBar1.add(jMenuFile);

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
            controller.loadDoclist(file);

        } else {
            System.out.println("debug: \"Choose Doclist\" action cancelled by user");
        }
    }//GEN-LAST:event_jMenuItemLoadDatasetActionPerformed

    private void jTextFieldSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldSearchActionPerformed
        
        // whenever text is updated, perform filtering
        String currentText = jTextFieldSearch.getText();
        
        if (docTableModelSorter != null) {
            
        }
        
        
        // TODO proper filtering which first updates the controller
        
    }//GEN-LAST:event_jTextFieldSearchActionPerformed

    public void rebuildDocumentTable() {
        
        // query for new backend for the simple document table, load
        docTableModel = controller.buildSimpleDocTableModel();
        jTableSimpleDocs.setModel(docTableModel);
        
        // build sorter for the table
        docTableModelSorter = new TableRowSorter<>(docTableModel);
        jTableSimpleDocs.setRowSorter(docTableModelSorter);
        
    }
    
    @Override
    public void resetAllViews() {
        
        // reset all views in this MainView; 
        // query MainController for new data (model may have changed)
        
        // rebuild simple document table view
        rebuildDocumentTable();
        
        // query for new backend for attribute table, load
        TableModel newAttrSelectionTableModel = controller.buildSimpleAttrSelectionTableModel();
        attrSelectionTableModel = newAttrSelectionTableModel;
        jTableAttrSelection.setModel(attrSelectionTableModel);
        
        // TODO
        
        
        
    }
    
    @Override
    public void attributeSelectionChanged() {
        
        // update relevant panes
        
        // update simple document table
        // ask controller for a new model, with current column selections
        rebuildDocumentTable();
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        // setup controller
        MainController controller = new DefaultMainController();
        
        // setup model
        MainModel model = new NullMainModel(controller);
        controller.setModel(model);
        
        // setup view
        final MainView mainView = new MainTabbedView(controller);
        controller.setView(mainView);
        
        
        
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
        
        
        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                ((MainTabbedView)mainView).setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabelSearch;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuItemLoadDataset;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableAttrSelection;
    private javax.swing.JTable jTableSimpleDocs;
    private javax.swing.JTextField jTextFieldSearch;
    // End of variables declaration//GEN-END:variables
}
