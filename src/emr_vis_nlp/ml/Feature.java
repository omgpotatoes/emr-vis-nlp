
package emr_vis_nlp.ml;

import java.util.List;

/**
 * Represents a machine learning feature from a MLPredictor. Features are 
 * document-independent, instead representing a property used by the ML 
 * classifier to assign a class, along with a weight value.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class Feature {
    
    private FeatureType type;
    
    private String featureText;
    
    // average weight for feature across all possible classes
    private double avgWeight;
    
    // names of all possible classes for this feature's model
    private List<String> classVals;
    
    // weights of this feature for each of the possible classes
    private List<Double> classWeights;

    public Feature(FeatureType type, String featureText, List<String> classVals, List<Double> classWeights) {
        this.type = type;
        this.featureText = featureText;
        this.classVals = classVals;
        this.classWeights = classWeights;
        
        avgWeight = 0.;
        for (int c=0; c<classWeights.size(); c++){
            avgWeight += classWeights.get(c);
        }
        avgWeight /= (double) classWeights.size();
        
    }

    public double getAvgWeight() {
        return avgWeight;
    }

    public List<String> getClassVals() {
        return classVals;
    }

    public List<Double> getClassWeights() {
        return classWeights;
    }

    public String getFeatureText() {
        return featureText;
    }

    public FeatureType getType() {
        return type;
    }
    
    @Override
    public String toString() {
        if (type == FeatureType.NGRAM) {
            return featureText;
        } else if (type == FeatureType.REGEXP) {
            return featureText;
        } else {
            // include conditions for other FeatureTypes above here...
            return featureText;
        }
    }
    
    
}