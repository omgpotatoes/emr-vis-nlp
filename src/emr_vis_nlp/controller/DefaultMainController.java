
package emr_vis_nlp.controller;

import emr_vis_nlp.model.MainModel;
import emr_vis_nlp.model.MpqaColonMainModel;
import emr_vis_nlp.view.MainView;
import java.io.File;

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
    
    
}
