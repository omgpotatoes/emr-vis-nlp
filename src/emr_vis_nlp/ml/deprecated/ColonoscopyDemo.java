package emr_vis_nlp.ml.deprecated;

import java.io.File;
import java.util.Map;

/**
 * @deprecated 
 */
public class ColonoscopyDemo {
	protected String m_modelPath = "resources/backend/Models";
	protected String[] m_fnModels = {"Indicator2.model", "Indicator3.model",
			"Indicator6.model", "Indicator7.model",
			"Indicator9.model", "Indicator10.model",
			"Indicator11.model", "Indicator12.model",
			"Indicator15.model", "Indicator17.model"};
	protected ColonoscopyModel[] m_models;
	
	public ColonoscopyDemo(){
		super();
		try {
			this.m_models = this.loadModels();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ColonoscopyDemo(String modelPath){
		super();
		this.m_modelPath = modelPath;
		try {
			this.m_models = this.loadModels();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ColonoscopyModel[] loadModels() throws Exception{
		ColonoscopyModel[] models = new ColonoscopyModel[this.m_fnModels.length];
		String fnModel;
		for(int i = 0; i < this.m_fnModels.length; i++){
			if(this.m_modelPath.length() > 0)
				fnModel = this.m_modelPath + File.separator + this.m_fnModels[i];
			else
				fnModel = this.m_fnModels[i];
			models[i] = new ColonoscopyModel();
			models[i].loadModel(fnModel);
		}
		
		return models;
	}
	
	public double[][] predictInstance(String fnInstance) throws Exception{
		double[][] predictions = new double[this.m_fnModels.length][3];
		
                // each of these models corresponds to one of the indicators
		for(int i = 0; i < this.m_fnModels.length; i++){
			predictions[i] = this.m_models[i].predictInstanceDistribution(fnInstance);
		}
		
		return predictions;
	}
        
        public Map<String, Double> getTermWeightMapForModelIndex(int modelIndex) {
            
            ColonoscopyModel model = m_models[modelIndex];
            Map<String, Double> termWeights = model.getTermWeights();
            return termWeights;
            
        }
        
}
