
package emr_vis_nlp.ml;

import java.util.List;

/**
 * Represents a general attribute / property of interest which a back-end model
 * may be predicting.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class Attribute {

    private AttributeType attributeType;
    
    private String name;
    
    private String elaboration;
    
    private List<String> legalValues;

    public Attribute(AttributeType attributeType, String name, String elaboration, List<String> legalValues) {
        this.attributeType = attributeType;
        this.name = name;
        this.elaboration = elaboration;
        this.legalValues = legalValues;
    }
    
    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public String getElaboration() {
        return elaboration;
    }

    public void setElaboration(String elaboration) {
        this.elaboration = elaboration;
    }

    public List<String> getLegalValues() {
        return legalValues;
    }

    public void setLegalValues(List<String> legalValues) {
        this.legalValues = legalValues;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
    
    
    
    
    public static enum AttributeType {
        N_GRAM,
        PATTERN,
        ONTOLOGY_ITEM,
        NUMERIC,
        STRING;
    }
    
}
