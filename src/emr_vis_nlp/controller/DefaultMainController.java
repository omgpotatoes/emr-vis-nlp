
package emr_vis_nlp.controller;

import emr_vis_nlp.model.MainModel;
import emr_vis_nlp.model.MpqaColonMainModel;
import emr_vis_nlp.view.DocumentTreeMapView;
import emr_vis_nlp.view.MainView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.table.TableModel;

/**
 *
 * @author alexander.p.conrad@gmail.com
 */
public class DefaultMainController implements MainController {
    
    /* main model controlled by this */
    private MainModel model;
    /* main view controlled by this */
    private MainView view;
    
    @Override
    public void setModel(MainModel model) {
        this.model = model;
    }
    
    @Override
    public void setView(MainView view) {
        this.view = view;
    }
    
    @Override
    public void loadDoclist(File file) {
        
        // load new model from doclist
        // TODO add support for dataset types beyond MpqaColon
        model = new MpqaColonMainModel(this);
        model.loadDataFromDoclist(file);
        
        // update view
        // TODO
        view.resetAllViews();
        
    }
    
    /**
     * Filters documents according to presence of string.
     * 
     * @param str 
     */
    @Override
    public void applySimpleStringFilter(String str) {
        model.applySimpleStringFilter(str);
        // update components as appropriate
        // TODO
        view.resetAllViews();
        
    }
    
    /**
     * Supplies a new table model to the visualization, in the event that data 
     * is changed or a new filter is applied.
     * 
     * @return 
     */
    @Override
    public TableModel buildSimpleDocTableModel() {
        TableModel docTableModel = model.buildSimpleDocTableModel();
        return docTableModel;
    }
    
    /**
     * Supplies a new table model for the attribute selection table.
     * 
     * @return 
     */
    @Override
    public TableModel buildSimpleAttrSelectionTableModel() {
        TableModel attrSelectionTableModel = model.buildSimpleAttrSelectionTableModel();
        return attrSelectionTableModel;
    }
    
    @Override
    public void attributeSelectionUpdated(List<Boolean> newSelectedAttributes) {
        // update selection in model
        model.setSelectedAttributes(newSelectedAttributes);
        
        // indicate to main view that all relevant displays should be redrawn / rebuilt
        view.attributeSelectionChanged();
        
    }
    
    @Override
    public JComponent buildDocTreeMapViewComponent() {
        
        // get selected attributes
        // FIXME provide proper ordering controls!
//        List<String> selectedAttrsForTree = new ArrayList<>();
//        List<String> allAttributes = model.getAllAttributes();
//        List<Boolean> allSelectedAttributes = model.getAllSelectedAttributes();
//        for (int a=0; a<allAttributes.size(); a++) {
//            String attr = allAttributes.get(a);
//            boolean isSelected = allSelectedAttributes.get(a);
//            if (isSelected) {
//                selectedAttrsForTree.add(attr);
//            }
//        }
        
        
        List<String> selectedAttrsForTree = new ArrayList<>();
        selectedAttrsForTree.add("Indicator_1");
        selectedAttrsForTree.add("Indicator_21");
        selectedAttrsForTree.add("Indicator_19");
        
        JComponent docTreeMapViewComponent = DocumentTreeMapView.buildNewTreeMapComponent(model.getAllDocuments(), model.getAllSelectedDocuments(), selectedAttrsForTree);
        return docTreeMapViewComponent;
        
    }
    
}
