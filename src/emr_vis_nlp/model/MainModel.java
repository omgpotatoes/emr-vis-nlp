
package emr_vis_nlp.model;

import java.io.File;

/**
 *
 * @author alexander.p.conrad@gmail.com
 */
public interface MainModel {
    
    public void loadDataFromDoclist(String doclistPath);
    public void loadDataFromDoclist(File doclist);
    
}
