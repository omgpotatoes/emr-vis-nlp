
package emr_vis_nlp.model;

import java.util.List;
import java.util.Map;

/**
 * Interface which should be implemented by all classes seeking to act as  the
 * model in the MVC design pattern for the emr-vis-nlp system. Specifically,
 * MainModels are responsible for loading the back-end raw data, along with its
 * manual annotations and attribute list (if any), which will be manipulated
 * through the interface. MainModels are also responsible for keeping track of
 * which documents and attributes within the dataset are currently enabled.
 * 
 * Note: Neither MainModel nor any of the contents of the emr_vis_nlp.model
 * package is responsible for back-end NLP predictions. All NLP and ML
 * components should be stored in the emr_vis_nlp.ml package.
 * @see emr_vis_nlp.ml.MLPredictor
 * 
 *
 * TODO: interface could be consolidated, refactored to have less risk of
 * exposure of backing datastructures, ie, ensure that access to the document
 * list is controlled, etc.
 *
 * @author alexander.p.conrad@gmail.com
 */
public interface MainModel {
    
    /**
     * 
     * @return List containing all Documents from this data source
     */
    public List<Document> getAllDocuments();
    
    /**
     * 
     * @return List of boolean indicators as to which documents (as returned by getAllDocuments()) are currently enabled.
     */
    public List<Boolean> getIsDocumentEnabledList();
    
    /** 
     * 
     * @return List of names for all attributes in model
     */
    public List<String> getAllAttributeNames();
    
    /** 
     * 
     * @param attrName String name of an attribute from this model
     * @return map of all possible values & their counts for a given attribute in the model
     */
    public Map<String, Integer> getAttributeValueCountMap(String attrName);
    
    /**
     * Updates target document's target attribute with the designated value.
     * 
     * @param globalDocID global integer identifier for document (index into getAllDocuments())
     * @param docAttr name of an attribute used in this model
     * @param docAttrVal the manually-annotated value which this should have for this document
     * @return true if update was completed successfully, false otherwise
     */
    public boolean updateDocumentAttr(int globalDocID, String docAttr, String docAttrVal);
    
    /**
     * 
     * @param globalDocID global integer identifier for document (index into getAllDocuments())
     * @param isEnabled boolean flag as to whether or not this document should be enabled
     */
    public void setIsDocumentEnabled(int globalDocID, boolean isEnabled);
    
    /**
     * Returns whether a manual annotation exists in the given document for the given attribute.
     * 
     * @param globalDocId global integer identifier for document (index into getAllDocuments())
     * @param attrName name of an attribute used in this model
     * @return true if a manual annotation exists in the document for the given attribute, false otherwise
     */
    public boolean hasManAnn(int globalDocId, String attrName);
    
    /**
     * Provides the manual annotation for the given document for the given attribute.
     * 
     * @param globalDocId global integer identifier for document (index into getAllDocuments())
     * @param attrName name of an attribute used in this model
     * @return the value of the manual annotation in the document for the given attribute if it exists, null otherwise
     */
    public String getManAnn(int globalDocId, String attrName);
    
}
