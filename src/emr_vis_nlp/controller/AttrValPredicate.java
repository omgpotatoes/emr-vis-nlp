
package emr_vis_nlp.controller;

import java.util.Objects;

/**
 * Tuple class for storing an attribute name and a target value.
 *
 * @author alexander.p.conrad
 */
public class AttrValPredicate {
    
    private String attrName;
    private String attrVal;

    public AttrValPredicate(String attrName, String attrVal) {
        this.attrName = attrName;
        this.attrVal = attrVal;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getAttrVal() {
        return attrVal;
    }

    public void setAttrVal(String attrVal) {
        this.attrVal = attrVal;
    }
    
    public String getTrueStrPred() {
        return attrName+" == '"+attrVal+"'";
    }
    
    public String getFalseStrPred() {
        return "["+attrName+"] != '"+attrVal+"'";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AttrValPredicate other = (AttrValPredicate) obj;
        if (!Objects.equals(this.attrName, other.attrName)) {
            return false;
        }
        if (!Objects.equals(this.attrVal, other.attrVal)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.attrName);
        hash = 41 * hash + Objects.hashCode(this.attrVal);
        return hash;
    }
    
    
    
    
    
}
