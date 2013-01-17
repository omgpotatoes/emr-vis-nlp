package emr_vis_nlp.ml;

/**
 * Enum representing the different types of ML Features we could be trying to
 * represent in a given document.
 *
 * @author alexander.p.conrad@gmail.com
 */
public enum FeatureType {

    NGRAM("ngram"),
    REGEXP("regexp"),;
    private String name;

    FeatureType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "FeatureType{" + "name=" + name + '}';
    }
}
