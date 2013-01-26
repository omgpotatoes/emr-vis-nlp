
package annotator.annotator;

import annotator.annotator.JTreeLabels;
import annotator.data.DataTag;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * JTree specifically for labels in annotator.
 *
 * @author conrada
 */
public class JTreeLabels extends DefaultTreeModel {
    
    public JTreeLabels(DefaultMutableTreeNode topLevelTreeNode) {
        super(topLevelTreeNode);
    }
    
    
    
    
    
    public static JTreeLabels buildJTreeLabels(DataTag topLevelDataTag) {
        
        // recursively build labels
        DefaultMutableTreeNode topLevelTreeNode = buildTreeNodeLabel(topLevelDataTag);
        JTreeLabels treeLabels = new JTreeLabels(topLevelTreeNode);
        
        return treeLabels;
        
    }
    
    public static DefaultMutableTreeNode buildTreeNodeLabel(DataTag dataTag) {
        
        DefaultMutableTreeNode treeNodeLabel = new DefaultMutableTreeNode(dataTag);
        
        List<DataTag> childDataTags = dataTag.getChildTags();
        for (int i=0; i<childDataTags.size(); i++) {
            DataTag childDataTag = childDataTags.get(i);
            DefaultMutableTreeNode childTreeNodeLabel = buildTreeNodeLabel(childDataTag);
            treeNodeLabel.add(childTreeNodeLabel);
        }
        
        return treeNodeLabel;
        
    }
    
}
