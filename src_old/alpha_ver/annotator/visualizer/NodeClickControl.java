package annotator.visualizer;

import annotator.MainWindow;
import java.awt.event.MouseEvent;
import prefuse.controls.ControlAdapter;
import prefuse.visual.VisualItem;

/**
 *
 * @author alexander.p.conrad@gmail.com
 */
public class NodeClickControl extends ControlAdapter {

//    @Override
//    public void itemClicked(VisualItem item, MouseEvent e) {
//
//        int index = -1;
//        try {
//
//            index = (int) item.get("index");
//
//            // debug
//            System.out.println("debug: NodeClickControl.itemClicked: doc " + index + " (" + MainWindow.activeDataset.getDocuments().get(index).getName() + ")");
//
//            MainWindow.setSelectedDocFromVisualization(index);
//
//        } catch (ArrayIndexOutOfBoundsException ex) {
//            // user may have clicked on a non-node item, such as a cluster
//            // debug
//            System.out.println("debug: non-node item clicked");
//        }
//
//    }

    @Override
    public void itemPressed(VisualItem item, MouseEvent e) {

        int index = -1;
        try {

            index = (int) item.get("index");
            // debug
            System.out.println("debug: NodeClickControl.itemPressed: doc " + index + " (" + MainWindow.activeDataset.getDocuments().get(index).getName() + ")");

            MainWindow.window.setSelectedDocFromVisualization(index);

        } catch (ArrayIndexOutOfBoundsException ex) {
            // user may have clicked on a non-node item, such as a cluster
            // debug
            System.out.println("debug: non-node item pressed");
        }

    }
}
