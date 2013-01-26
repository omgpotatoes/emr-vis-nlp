
package emr_vis_nlp.model;

import java.io.File;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableModel;

/**
 * Interface which should be implemented by all classes seeking to act as 
 * the model in the MVC design pattern for the emr-vis-nlp system.
 *
 * @author alexander.p.conrad@gmail.com
 */
public interface MainModel {
    
    // basic model functionality
    /** load model from doclist file */
    // TODO move out of interface! into implementing class of MainModel!
//    public void loadDataFromDoclist(String doclistPath);
    /** load model from doclist file */
    // TODO move out of interface! into implementing class of MainModel!
//    public void loadDataFromDoclist(File doclist);
    /** list of all documents in model */
    public List<Document> getAllDocuments();
    /** list of flags as to which documents in model are currently "enabled" (disabled documents should not be displayed in the visualization) */
    public List<Boolean> getAllSelectedDocuments();
    /** list of all attribute names in model */
    public List<String> getAllAttributes();
    /** list of flags as to which attributes in model are currently "enabled" (disabled attributes should not be displayed in the visualization) */
    public List<Boolean> getAllSelectedAttributes();
    /** map of all possible values & their counts for a given attribute in the model */
    public Map<String, Integer> getAttributeValueCountMap(String attrName);
    /** enable selected documents in model */
    public void setSelectedAttributes(List<Boolean> selectedAttributes);
    
    // for machine learning
    public Map<String, PredictionCertaintyTuple> getPredictionsForDoc(int globalDocId);
    
    // miscellaneous
    /** applies simple string as filter to set enabled / disabled docs */
    // TODO eliminate method? move to controller / visualization side?
//    public void applySimpleStringFilter(String str);
    
    // visualization-oriented model builder methods
    // TODO : move to controller!
//    public DocTableModel buildSimpleDocTableModel();
//    public AttrTableModel buildSimpleAttrSelectionTableModel();
//    public TableModel buildSimpleTreeMapSelectionTableModel();
//    public AttrTableModel buildSimpleAttrSelectionTableModelFocusOnly();
//    public TableModel buildSimpleDocGridSelectionTableModel();
    
}
