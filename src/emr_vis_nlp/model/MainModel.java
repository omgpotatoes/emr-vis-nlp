
package emr_vis_nlp.model;

import java.io.File;
import java.util.List;

/**
 *
 * @author alexander.p.conrad@gmail.com
 */
public interface MainModel {
    
    public void loadDataFromDoclist(String doclistPath);
    
    public void loadDataFromDoclist(File doclist);
    
    public void applySimpleStringFilter(String str);
    
    public DocTableModel buildSimpleDocTableModel();
    
    public AttrTableModel buildSimpleAttrSelectionTableModel();
    
    public void setSelectedAttributes(List<Boolean> selectedAttributes);
    
}
