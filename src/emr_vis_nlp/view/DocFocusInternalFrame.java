package emr_vis_nlp.view;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.model.Document;
import java.beans.PropertyVetoException;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AbstractDocument;

/**
 * Popup window for displaying details about a given target document.
 * 
 * code adapted (in small part) from MyInternalFrame Java demo.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocFocusInternalFrame extends javax.swing.JInternalFrame {
    
    /** number of DocFocusInternalFrames currently open */
    protected static int openFrameCount = 0;
    protected static final int xOffset = 30, yOffset = 30;

    /**
     * governing controller
     */
    private MainController controller;
    /**
     * Document being represented by this window
     */
    private Document doc;
    /**
     * global model id for this doc
     */
    private int docGlobalID;
    /**
     * backing model for the details table
     */
    private TableModel docDetailsTableModel;
    /**
     * backing sorter for the details table
     */
    private TableRowSorter<TableModel> docDetailsTableModelSorter;

    /**
     * Creates new form DocFocusWindow
     */
    public DocFocusInternalFrame(MainController controller, Document doc, int docGlobalID) {
        super("document "+doc.getName(), true, true, true, true);
        
        this.controller = controller;
        this.doc = doc;
        this.docGlobalID = docGlobalID;

        initComponents();

        // build custom table
        rebuildDocDetailsTable();

        // build text area
        rebuildTextArea();

        // initialize custom listeners
        initListeners();
        
        setSize(600, 300);
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
        
    }

    private void initListeners() {

        // row selection listener on details table
        jTableDocDetails.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                int firstIndex = e.getFirstIndex();
                int lastIndex = e.getLastIndex();
                boolean isAdjusting = e.getValueIsAdjusting();

                if (lsm.isSelectionEmpty()) {
                    //output.append(" <none>");
                } else {
                    // find out which indexes are selected.
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();

                    // minIndex should == maxIndex, since model restricted to selecting one row
                    if (minIndex == maxIndex) {

                        rebuildTextArea();

                    }

                }

            }
        });



    }

    public void rebuildDocDetailsTable() {

        docDetailsTableModel = controller.buildAttrAndPredictionModelForDoc(docGlobalID);
        jTableDocDetails.setModel(docDetailsTableModel);

        // build sorter
        docDetailsTableModelSorter = new TableRowSorter<>(docDetailsTableModel);
        jTableDocDetails.setRowSorter(docDetailsTableModelSorter);

    }

    public void rebuildTextArea() {

        // check to see whether any rows are selected
        int selectedRowRaw = jTableDocDetails.getSelectedRow();

        // get abst doc from text area
        AbstractDocument abstDoc = (AbstractDocument) (jTextPaneDocText.getStyledDocument());

        if (selectedRowRaw != -1) {

            int selectedRow = selectedRowRaw;
            try {
                selectedRow = jTableDocDetails.convertRowIndexToModel(selectedRowRaw);
                int globalIndex = selectedRow;
                try {
                    globalIndex = ((DocDetailsTableModel) docDetailsTableModel).getGlobalAttrIndexForModelRow(selectedRow);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    System.out.println("err?: DocFocusWindow.rebuildTextArea: could not cast to DocDetailsTableModel?");
                }

                // request designated text area from controller
                controller.writeDocTextWithHighlights(abstDoc, docGlobalID, globalIndex);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                System.out.println("err: something out of bounds in DocFocusWindow.rebuildTextArea: selectedRowRaw="+selectedRowRaw+", rowCount="+jTableDocDetails.getRowCount());
            }

        } else {

            // request designated text area from controller, -1 should indicate to not use any attributes
            controller.writeDocTextWithHighlights(abstDoc, docGlobalID, -1);

        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPaneDocDetails = new javax.swing.JSplitPane();
        jScrollPaneDocDetails = new javax.swing.JScrollPane();
        jTableDocDetails = new javax.swing.JTable();
        jScrollPaneDocText = new javax.swing.JScrollPane();
        jTextPaneDocText = new javax.swing.JTextPane();
        jLabelDocName = new javax.swing.JLabel();
        jTextFieldDocName = new javax.swing.JTextField();
        jButtonClose = new javax.swing.JButton();

        jSplitPaneDocDetails.setDividerLocation(200);

        jTableDocDetails.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jTableDocDetails.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPaneDocDetails.setViewportView(jTableDocDetails);

        jSplitPaneDocDetails.setRightComponent(jScrollPaneDocDetails);
        jSplitPaneDocDetails.setLeftComponent(jScrollPaneDocText);
        jSplitPaneDocDetails.setRightComponent(jTextPaneDocText);

        jLabelDocName.setText("document:");

        jButtonClose.setLabel("Close");
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelDocName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextFieldDocName, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonClose)
                .addContainerGap())
            .addComponent(jSplitPaneDocDetails, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabelDocName)
                    .addComponent(jTextFieldDocName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonClose))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPaneDocDetails, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCloseActionPerformed

        // tell controller to stop monitoring this window
//        boolean removalSuccess = controller.removeDocDetailsWindow(this);
        // debug
//        System.out.println("debug: doc " + docGlobalID + " popup removed from controller: " + removalSuccess);

        // close window
        try {
            setClosed(true);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
            System.out.println("err: could not close DocFocusWindow for document: " + doc.getName());
        }

    }//GEN-LAST:event_jButtonCloseActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonClose;
    private javax.swing.JLabel jLabelDocName;
    private javax.swing.JScrollPane jScrollPaneDocDetails;
    private javax.swing.JScrollPane jScrollPaneDocText;
    private javax.swing.JSplitPane jSplitPaneDocDetails;
    private javax.swing.JTable jTableDocDetails;
    private javax.swing.JTextField jTextFieldDocName;
    private javax.swing.JTextPane jTextPaneDocText;
    // End of variables declaration//GEN-END:variables
}
