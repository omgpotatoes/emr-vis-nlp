
package emr_vis_nlp.view;

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

    private JScrollPane jScrollPaneDocText;
    private JTextPane jTextPaneDocText;
    private int width = 500;
    private int height = 500;

    public MainViewGlassPane() {
        super();
        setLayout(null);
        jScrollPaneDocText = new JScrollPane();
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
        // do nothing
    }
}
