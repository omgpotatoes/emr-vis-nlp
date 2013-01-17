package emr_vis_nlp.ml;

import java.util.List;

/**
 * Tuple representing certainty scores for each value of a prediction for a
 * given attribute for a given document.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class Prediction {
    
    private List<String> values;
    private List<Double> certainties;
    
    private String topValue;
    private double topCertainty;

    public Prediction(List<String> values, List<Double> certainties) {
        this.values = values;
        this.certainties = certainties;
        
        assert values.size() == certainties.size();
        double maxCert = 0.;
        String maxVal = "";
        for (int v=0; v<certainties.size(); v++) {
            double cert = certainties.get(v);
            String val = values.get(v);
            if (cert >= maxCert) {
                maxCert = cert;
                maxVal = val;
            }
        }
        topValue = maxVal;
        topCertainty = maxCert;
        
    }

    public List<Double> getCertainties() {
        return certainties;
    }

    public double getTopCertainty() {
        return topCertainty;
    }

    public String getTopValue() {
        return topValue;
    }

    public List<String> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "Prediction{" + "values=" + values + ", certainties=" + certainties + ", topValue=" + topValue + ", topCertainty=" + topCertainty + '}';
    }
    
    
    
}
