package emr_vis_nlp.view;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.model.Document;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AbstractDocument;

/**
 *
 * @author conrada
 */
public class DocFocusPopup extends javax.swing.JFrame {

    /** number of DocFocusInternalFrames currently open */
    protected static int openFrameCount = 0;
    protected static final int xOffset = 30, yOffset = 30;
    protected static final int initWidth = 800, initHeight = 500;
    
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
     * Creates new form DocFocusPopup
     */
    public DocFocusPopup(Document doc, int docGlobalID) {
        super("document "+doc.getName());
        
        this.controller = MainController.getMainController();
        this.doc = doc;
        this.docGlobalID = docGlobalID;

        initComponents();

        // build custom table
        rebuildDocDetailsTable();

        // build text area
        rebuildTextArea();

        // initialize custom listeners
        initListeners();
        
        setSize(initWidth, initHeight);
        setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
        openFrameCount++;
        
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

        jFrame1 = new javax.swing.JFrame();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPaneDocText = new javax.swing.JScrollPane();
        jTextPaneDocText = new javax.swing.JTextPane();
        jScrollPaneDocDetails = new javax.swing.JScrollPane();
        jTableDocDetails = new javax.swing.JTable();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jSplitPane1.setDividerLocation(375);

        jScrollPaneDocText.setViewportView(jTextPaneDocText);

        jSplitPane1.setLeftComponent(jScrollPaneDocText);

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
        jScrollPaneDocDetails.setViewportView(jTableDocDetails);

        jSplitPane1.setRightComponent(jScrollPaneDocDetails);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 528, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        
        // tell controller to stop monitoring this window
        boolean removalSuccess = controller.removeDocDetailsWindow(this);
        // debug
//        System.out.println("debug: doc " + docGlobalID + " popup removed from controller: " + removalSuccess);
        
    }//GEN-LAST:event_formWindowClosing

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFrame jFrame1;
    private javax.swing.JScrollPane jScrollPaneDocDetails;
    private javax.swing.JScrollPane jScrollPaneDocText;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTableDocDetails;
    private javax.swing.JTextPane jTextPaneDocText;
    // End of variables declaration//GEN-END:variables
}
