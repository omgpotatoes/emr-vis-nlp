
package emr_vis_nlp.model;

import emr_vis_nlp.controller.MainController;
import emr_vis_nlp.model.mpqa_colon.Dataset;
import java.io.File;

/**
 *
 * @author alexander.p.conrad@gmail.com
 */
public class MpqaColonMainModel implements MainModel {
    
    private Dataset dataset;
    
    /**
     * this MainModel's governing MainController
     */
    private MainController controller;
    
    public MpqaColonMainModel(MainController controller) {
        
        this.controller = controller;
        
    }
    
    @Override
    public void loadDataFromDoclist(String doclistPath) {
        dataset = Dataset.loadDatasetFromDoclist(doclistPath);
    }
    
    @Override
    public void loadDataFromDoclist(File doclist) {
        dataset = Dataset.loadDatasetFromDoclist(doclist);
    }
    
}
