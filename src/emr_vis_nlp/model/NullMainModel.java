package emr_vis_nlp.model;

import emr_vis_nlp.controller.MainController;
import java.io.File;

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
        // shouldn't happen
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadDataFromDoclist(File doclist) {
        // shouldn't happen
        throw new UnsupportedOperationException();
    }

    @Override
    public void applySimpleStringFilter(String str) {
        // shouldn't happen
        throw new UnsupportedOperationException();
    }

    @Override
    public DocTableModel buildSimpleDocTableModel() {
        // shouldn't happen
        throw new UnsupportedOperationException();
    }
    
    @Override
    public AttrTableModel buildSimpleAttrSelectionTableModel() {
        // shouldn't happen
        throw new UnsupportedOperationException();
    }
}
