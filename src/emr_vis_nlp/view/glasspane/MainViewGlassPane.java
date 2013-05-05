
package emr_vis_nlp.view.glasspane;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.model.Document;
import emr_vis_nlp.view.doc_grid.DocumentGridTable;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import prefuse.visual.VisualItem;
import weka.attributeSelection.GainRatioAttributeEval;

/**
 * This JComponent is designed to serve as a MainView's Glasspane; that is, to 
 * be loaded on-demand to do custom positioning of custom elements overtop of 
 * the regular view. 
 * 
 * @see http://docs.oracle.com/javase/tutorial/uiswing/components/rootpane.html
 * @author alexander.p.conrad@gmail.com
 */
public class MainViewGlassPane extends JComponent implements MouseListener {
    
    private static MainViewGlassPane glassPane = null;
    
//    private JScrollPane jScrollPaneDocText;
//    private JTextPane jTextPaneDocText;
    private GlassPaneTextPanel mainPanel = null;
    private int width = 450;
    private int height = 400;
    private int xInset = 0;
    private int yInset = 0;
    
    private boolean mouseWasClicked = false;
    private VisualItem currentItem = null;
    
    // color for the pane
    private Color backgroundColor;
    
    public static MainViewGlassPane getGlassPane() {
        if (glassPane == null) {
            glassPane = new MainViewGlassPane();
        }
        return glassPane;
    }
    
    private MainViewGlassPane() {
        super();
        // manually position the JScrollPane within the GlassPane as needed
        setLayout(null);
    }

    public AbstractDocument getAbstDoc() {
        if (mainPanel != null) {
            return mainPanel.getAbstDoc();
        }
        return null;
    }
    
    /**
     * Make appear at the designated coordinates, and with the designated size, the appropriate multi-function pane
     * 
     * @param x
     * @param y
     * @param w
     * @param h
     * @param item the VisualItem whose details are to be contained in this pane
     */
    public void displaySizedPane(int x, int y, int w, int h, VisualItem item) {
        
        // create new GlassPaneTextPanel, position appropriately within glasspane
        currentItem = item;
        int docId = item.getInt(DocumentGridTable.NODE_ID);
        Document doc = MainController.getMainController().getDocument(docId);
        mainPanel = new GlassPaneTextPanel(this, doc, docId, x, y, w, h);
        
        // do layout, make visible
        removeAll();
        setLayout(null);
        add(mainPanel);
        setVisible(true);
        
    }

    public void hidePane(MouseEvent e) {
        // get the highlighted text
        String selectedText = "";
        if (mainPanel != null) {
            String text = mainPanel.getSelectedText();
            if (text != null && !text.trim().isEmpty()) {
                selectedText = text.trim().toLowerCase();
                // update selected text 
                MainController.getMainController().setSearchText(selectedText);
            }
        }
        // debug
        System.out.println("debug: "+this.getClass().getName()+": selectedText = \""+selectedText+"\"");
        setVisible(false);
        
        // send signal to docgrid that glyph was pressed, in order to do sizing restore
        MainController.getMainController().getDocSelectControl().disableNextZoomOnItem(currentItem);
        VisualItem clickedItem = MainController.getMainController().getDocSelectControl().findClickedItem(e);
        if (clickedItem != null) {
            // if an item was clicked, pass that item 
            MainController.getMainController().getDocSelectControl().itemClicked(clickedItem, e);
        } else {
            // if no item was clicked, pass the focusitem so that it will become deselected
            MainController.getMainController().getDocSelectControl().itemClicked(currentItem, e);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        hidePane(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }
    

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
    public int getPopupWidth() {
        return width;
    }
    
    public int getPopupHeight() {
        return height;
    }

    public boolean wasMouseClicked() {
        return mouseWasClicked;
    }

    public void setWasMouseClicked(boolean mouseWasClicked) {
        this.mouseWasClicked = mouseWasClicked;
    }

    public void setBackgroundColor(Color color) {
        backgroundColor = color;
        if (mainPanel != null) {
            mainPanel.setColor(color);
        }
    }
    
}
