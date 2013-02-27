
package emr_vis_nlp.view;

import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;

/**
 *
 * @author alexander.p.conrad@gmail.com
 */
public class MainViewGlassPane extends JComponent implements MouseListener {

    private static MainViewGlassPane glassPane;
    
    private JScrollPane jScrollPaneDocText;
    private JTextPane jTextPaneDocText;
    private int width = 450;
    private int height = 400;
    private int xInset = 0;
    private int yInset = 0;

    public MainViewGlassPane() {
        super();
        glassPane = this;
        setLayout(null);
        jScrollPaneDocText = new JScrollPane();
        jScrollPaneDocText.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
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
                // hide glasspane
                glassPane.setVisible(false);
                // debug
                System.out.println("debug: "+this.getClass().getName()+": hiding glasspane");
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
    }
    
    public void displaySizedPane(int x, int y, int w, int h) {
//        setBounds(x, y, w, h);
        
        jScrollPaneDocText.setBounds(x, y, w, h);
        setVisible(true);
        jScrollPaneDocText.repaint();
    }

    public void hidePane() {
        setVisible(false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // if right-click, hide pane
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
        // do nothing
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // hide pane
//        setVisible(false);
        // TODO resume actions in visualization?
    }
    
    public int getPopupWidth() {
        return width;
    }
    
    public int getPopupHeight() {
        return height;
    }
    
    public void updateVisualizationInset(int x, int y) {
        
    }
    
}
