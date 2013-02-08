
package annotator.visualizer;

import annotator.MainWindow;
import java.util.Iterator;
import prefuse.Visualization;
import prefuse.action.GroupAction;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.tuple.TupleSet;

/**
 *
 * @author conrada
 */
public class NodeClickAction extends GroupAction {

    Visualization vis;

    public NodeClickAction(String graphGroup, Visualization vis) {
        super(graphGroup);
        this.vis = vis;
    }
    
    public void run(double frac) {
        
        // debug
        //System.out.println("debug: NodeClickAction running...");
        
        TupleSet focus = vis.getGroup(Visualization.FOCUS_ITEMS);
        
        // make sure something is selected
        if (focus == null || focus.getTupleCount() == 0 ) {
            // debug
            //System.out.println("debug: no nodes in focus");
            return;
        }

        Graph g = (Graph)vis.getGroup(m_group);
        Node f = null;
        Iterator tuples = focus.tuples();
        while (tuples.hasNext() && !g.containsTuple(f=(Node)tuples.next()))
        {
            f = null;
        }
        
        // couldn't find selected node?
        if ( f == null ) {
            // debug
            System.out.println("debug: could not find focused node in graph");
            assert false: "couldn't find selected node in graph?";
            return;
        }
        
        int docIndex = (int)f.get("index");
        MainWindow.window.setSelectedDocFromVisualization(docIndex);
        
    }
}
