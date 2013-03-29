
package emr_vis_nlp.ml;

/**
 * Tuple representing back-end prediction and associated certainty for a given attribute.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class PredictionCertaintyTuple {
    
    private String attribute;
    private String value;
    private double cert;

    public PredictionCertaintyTuple(String attribute, String value, double cert) {
        this.attribute = attribute;
        this.value = value;
        this.cert = cert;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public double getCert() {
        return cert;
    }

    public void setCert(double cert) {
        this.cert = cert;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    
    
}
