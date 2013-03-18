
package emr_vis_nlp.view;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.view.doc_grid.DocGridDragControl;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import prefuse.visual.VisualItem;

/**
 *
 * @author alexander.p.conrad@gmail.com
 */
public class MainViewGlassPane extends JComponent implements MouseListener {

    // TODO this should be a singleton, since we can have only 1 glasspane defined for the jframe at a time?
    private static MainViewGlassPane glassPane;
    
    private JScrollPane jScrollPaneDocText;
    private JTextPane jTextPaneDocText;
    private int width = 450;
    private int height = 400;
    private int xInset = 0;
    private int yInset = 0;
    
    private boolean mouseWasClicked = false;
    private VisualItem currentItem = null;

    public MainViewGlassPane() {
        super();
        glassPane = this;
        // manually position the JScrollPane within the GlassPane
        setLayout(null);
        jScrollPaneDocText = new JScrollPane();
        jScrollPaneDocText.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    // set flag (so that glasspane isn't immediately re-triggered)
                    mouseWasClicked = true;
                    // hide glasspane
                    hidePane();
                    // debug
                    System.out.println("debug: " + this.getClass().getName() + ": hiding glasspane, mouse-press");
                    // pass event to DocGridDragControl
                    DocGridDragControl.control.itemPressed(currentItem, e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // hide glasspane
                hidePane();
                // debug
                System.out.println("debug: "+this.getClass().getName()+": hiding glasspane, mouse-out");
            }
        });
        
        jScrollPaneDocText.setBounds(0, 0, width, height);
        jTextPaneDocText = new JTextPane();
        jScrollPaneDocText.setViewportView(jTextPaneDocText);
        add(jScrollPaneDocText);
        setVisible(false);
    }

    public AbstractDocument getAbstDoc() {
        return (AbstractDocument) jTextPaneDocText.getStyledDocument();
    }

    public void displayPaneAtPoint(int x, int y) {
        jScrollPaneDocText.setBounds(x, y, width, height);
        setVisible(true);
        jScrollPaneDocText.repaint();
        jScrollPaneDocText.getHorizontalScrollBar().setValue(0);
        jScrollPaneDocText.getVerticalScrollBar().setValue(0);
    }
    
    public void displaySizedPane(int x, int y, int w, int h, VisualItem item) {
//        setBounds(x, y, w, h);
        currentItem = item;
        jScrollPaneDocText.setBounds(x, y, w, h);
        setVisible(true);
        jScrollPaneDocText.repaint();
//        jScrollPaneDocText.getHorizontalScrollBar().setValue(0);
//        jScrollPaneDocText.getVerticalScrollBar().setValue(jScrollPaneDocText.getVerticalScrollBar().getMinimum());
        jTextPaneDocText.setCaretPosition(0);
    }

    public void hidePane() {
        setVisible(false);
        // get the highlighted text
        String selectedText = "";
        if (jTextPaneDocText != null) {
            String text = jTextPaneDocText.getSelectedText();
            if (text != null) {
                selectedText = text.toLowerCase().trim();
                // update selected text 
                MainController.getMainController().setSearchText(selectedText);
            }
        }
        // debug
//        System.out.println("debug: "+this.getClass().getName()+": selectedText = \""+selectedText+"\"");
        MainController.getMainController().getDocumentGrid().enableMouseListeners();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // if right-click, hide pane
        // note: this method should no longer be invoked, the scrollpane should capture mousepress events
        if (isVisible() && SwingUtilities.isRightMouseButton(e)) {
            hidePane();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mouseEntered(MouseEvent e) {
                // hide glasspane
        hidePane();
        // debug
        System.out.println("debug: " + this.getClass().getName() + ": hiding glasspane");
    }

    @Override
    public void mouseExited(MouseEvent e) {
//        // hide glasspane
//        hidePane();
//        // debug
//        System.out.println("debug: " + this.getClass().getName() + ": hiding glasspane");
    }
    
    public int getPopupWidth() {
        return width;
    }
    
    public int getPopupHeight() {
        return height;
    }
    
    public void updateVisualizationInset(int x, int y) {
        // not yet implemented
    }

    public boolean wasMouseClicked() {
        return mouseWasClicked;
    }

    public void setWasMouseClicked(boolean mouseWasClicked) {
        this.mouseWasClicked = mouseWasClicked;
    }
    
}
