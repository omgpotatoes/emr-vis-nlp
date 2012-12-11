package emr_vis_nlp.model;

import emr_vis_nlp.controller.MainController;
import java.io.File;
import java.util.List;

/**
 * Empty model for use when no dataset is loaded.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class NullMainModel implements MainModel {

    /**
     * this MainModel's governing MainController
     */
    private MainController controller;

    public NullMainModel(MainController controller) {

        this.controller = controller;

    }

    @Override
    public void loadDataFromDoclist(String doclistPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadDataFromDoclist(File doclist) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applySimpleStringFilter(String str) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DocTableModel buildSimpleDocTableModel() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public AttrTableModel buildSimpleAttrSelectionTableModel() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setSelectedAttributes(List<Boolean> selectedAttributes) {
        throw new UnsupportedOperationException();
    }
    
}
