
package emr_vis_nlp.model;

import emr_vis_nlp.model.mpqa_colon.Document;
import java.io.File;
import java.util.List;
import java.util.Map;

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
    
    public AttrTableModel buildSimpleAttrSelectionTableModelFocusOnly();
    
    public void setSelectedAttributes(List<Boolean> selectedAttributes);
    
    public List<Document> getAllDocuments();
    
    public List<Boolean> getAllSelectedDocuments();
    
    public List<String> getAllAttributes();
    
    public List<Boolean> getAllSelectedAttributes();
    
    public Map<String, PredictionCertaintyTuple> getPredictionsForDoc(int globalDocId);
    
}
