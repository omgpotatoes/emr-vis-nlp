
package emr_vis_nlp.controller;

import emr_vis_nlp.model.MainModel;
import emr_vis_nlp.view.MainView;
import java.io.File;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.table.TableModel;
import javax.swing.text.AbstractDocument;

/**
 *
 * @author alexander.p.conrad@gmail.com
 */
public interface MainController {
    
    public void setModel(MainModel model);
    public void setView(MainView view);
    public void loadDoclist(File file);
    
    // for document filtering
    public void applySimpleStringFilter(String str);
 
    // for simple document table view
    public TableModel buildSimpleDocTableModel();
    public TableModel buildSimpleAttrSelectionTableModel();
    public TableModel buildSimpleAttrSelectionTableModelFocusOnly();
    
    // for document details window
    public TableModel buildAttrAndPredictionModelForDoc(int docGlobalID);
    public void writeDocTextWithHighlights(AbstractDocument abstDoc, int globalDocId, int globalAttrID);
    public JFrame buildDocDetailsWindow(int docGlobalID);
    public boolean removeDocDetailsWindow(JFrame popup);
    
    public void attributeSelectionUpdated(List<Boolean> attributesEnabled);
    
    public void documentAttributesUpdated(int docGlobalID);
    
//    public DocumentTreeMapView buildDocTreeMapView();
    public JComponent buildDocTreeMapViewComponent();
    
}

