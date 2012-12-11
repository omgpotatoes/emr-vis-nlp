
package emr_vis_nlp.controller;

import emr_vis_nlp.model.MainModel;
import emr_vis_nlp.view.MainView;
import java.io.File;
import java.util.List;
import javax.swing.table.TableModel;

/**
 *
 * @author alexander.p.conrad@gmail.com
 */
public interface MainController {
    
    public void setModel(MainModel model);
    
    public void setView(MainView view);
    
    public void loadDoclist(File file);
    
    public void applySimpleStringFilter(String str);
 
    public TableModel buildSimpleDocTableModel();
    
    public TableModel buildSimpleAttrSelectionTableModel();
    
    public void attributeSelectionUpdated(List<Boolean> attributesEnabled);
    
}
