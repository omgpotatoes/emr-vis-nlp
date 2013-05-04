/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package emr_vis_nlp.view.glasspane;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.model.Document;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AbstractDocument;
import prefuse.visual.VisualItem;

/**
 *
 * @author conrada
 */
public class GlassPaneTextPanel extends javax.swing.JPanel implements MouseListener {

    /**
     * governing controller
     */
    private MainController controller;
    /**
     * glasspane in which this panel is embedded
     */
    private MainViewGlassPane glasspane;
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
     * Creates new form MainGlassPane
     */
    public GlassPaneTextPanel(MainViewGlassPane glasspane, Document doc, int docGlobalID, int x, int y, int width, int height) {
        super();
        initComponents();
        this.glasspane = glasspane;
        this.doc = doc;
        this.docGlobalID = docGlobalID;
        this.controller = MainController.getMainController();
        
        // build custom table
        rebuildDocDetailsTable();

        // build text area
        rebuildTextArea();

        // initialize custom listeners
        initListeners();
        
        setBounds(x, y, width, height);
        //repaint();
        
    }
    
    private void initListeners() {

        // row selection listener on details table
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

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
        jTable1.setModel(docDetailsTableModel);

        // build sorter
        docDetailsTableModelSorter = new TableRowSorter<>(docDetailsTableModel);
        jTable1.setRowSorter(docDetailsTableModelSorter);

    }

    public void rebuildTextArea() {

        // check to see whether any rows are selected
        int selectedRowRaw = jTable1.getSelectedRow();

        // get abst doc from text area
        AbstractDocument abstDoc = (AbstractDocument) (jTextPane1.getStyledDocument());

        if (selectedRowRaw != -1) {

            int selectedRow = selectedRowRaw;
            try {
                selectedRow = jTable1.convertRowIndexToModel(selectedRowRaw);
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
                System.out.println("err: something out of bounds in DocFocusWindow.rebuildTextArea: selectedRowRaw="+selectedRowRaw+", rowCount="+jTable1.getRowCount());
            }

        } else {

            // request designated text area from controller, -1 should indicate to not use any attributes
            controller.writeDocTextWithHighlights(abstDoc, docGlobalID, -1);

        }
        
        jTextPane1.setCaretPosition(0);

    }
    
    public AbstractDocument getAbstDoc() {
        return (AbstractDocument) jTextPane1.getStyledDocument();
    }
    
    public String getSelectedText() {
        return jTextPane1.getSelectedText();
    }
    
    public void setColor(Color color) {
//        jTable1.setBackground(color);
//        jTextPane1.setBackground(color);
//        jTextPane1.setForeground(color);
        setBackground(color);
//        setForeground(color);
        jSplitPane1.setBackground(color);
//        jSplitPane1.setForeground(color);
        jScrollPaneText.getViewport().setBackground(color);
//        jScrollPaneText.getViewport().setForeground(color);
        jScrollPaneTable.getViewport().setBackground(color);
//        jScrollPaneTable.getViewport().setForeground(color);
    }

    
    @Override
    public void mouseClicked(MouseEvent e) {
//        glasspane.hidePane(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // if right-click, hide pane
        // note: this method should no longer be invoked, the scrollpane should capture mousepress events
//        if (isVisible() && SwingUtilities.isRightMouseButton(e)) {
//            glasspane.hidePane();
//        }
        
        glasspane.hidePane(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mouseEntered(MouseEvent e) {
                // hide glasspane
//        hidePane();
//        // debug
//        System.out.println("debug: " + this.getClass().getName() + ": hiding glasspane");
    }

    @Override
    public void mouseExited(MouseEvent e) {
//        // hide glasspane
//        hidePane();
//        // debug
//        System.out.println("debug: " + this.getClass().getName() + ": hiding glasspane");
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPaneText = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jScrollPaneTable = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        jSplitPane1.setDividerLocation(325);

        jTextPane1.setBackground(new java.awt.Color(240, 240, 240));
        jScrollPaneText.setViewportView(jTextPane1);

        jSplitPane1.setLeftComponent(jScrollPaneText);

        jTable1.setBackground(new java.awt.Color(240, 240, 240));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPaneTable.setViewportView(jTable1);

        jSplitPane1.setRightComponent(jScrollPaneTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPaneTable;
    private javax.swing.JScrollPane jScrollPaneText;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables
}
