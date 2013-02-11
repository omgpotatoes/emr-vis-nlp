
package emr_vis_nlp.view.doc_table;

import javax.swing.RowFilter;

/**
 * Custom filter to support 
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DocTableModelFilter extends RowFilter {
    
    @Override
    public boolean include(RowFilter.Entry entry) {
        
        Integer entryModelIndex = (Integer)entry.getIdentifier();
        DocTableModel model = (DocTableModel)entry.getModel();
        String text = model.getDocTextAtIndex(entryModelIndex);
        
        // TODO finish this?
        
        
        
        
        return false;
        
    }
    
}
