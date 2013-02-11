package emr_vis_nlp.ml.deprecated;



public class ExportResult {
	protected String m_outputPath;
	protected String m_expCounter;
	
	public ExportResult(){
		super();
	}
	
	public ExportResult(String expCounter, String outputPath){
		super();
		m_expCounter = expCounter;
		m_outputPath = outputPath;
	}
	
	public void saveFeatureWeights(double[] weights, String measureCounter) throws Exception{
		//sort result descending
		FeatureWeight[] fWeights = Common.sortWeights(weights);		
		//save into CSV
		String firstRow = "";
		String secondRow = "";
		for(int i = 0; i < fWeights.length - 1; i++){
			//first row is feature index
			firstRow += Integer.toString(fWeights[i].index) + ",";
			//second row is feature weight
			secondRow += Double.toString(fWeights[i].weight) + ",";
		}
		firstRow += Integer.toString(fWeights[fWeights.length - 1].index);
		secondRow += Double.toString(fWeights[fWeights.length - 1].weight);
		String text = firstRow + "\n" + secondRow;
		
		String[] paths = {m_outputPath, "Weights","Exp" + m_expCounter + "_" + measureCounter + "_weights.csv"};
		String fileName = Common.getOSPath(paths);
		
		Common.saveTextFile(fileName, text);		
	}
	
	/*****
	 * classCounter, "", "", 
	 * "", predict a, predict b, predict c, ...
	 * target a, 0, 0, 0, ...
	 * target b, 0, 0, 0, ...
	 * target c, 0, 0, 0, ...
	 * 
	 * @param confusionMatrix array of confusion matrix
	 * @param classNames
	 * @param measureCounter
	 * @return
	 * @throws Exception
	 */
	public String toConfustionMatrixString(double[][] confusionMatrix, String[] classNames, String measureCounter) throws Exception{
		int nClass = classNames.length;
		//add header: measure index
		String text = measureCounter + ",";
		for(int i = 0; i < nClass - 1; i++){
			text += ",";
		}
		text += "\n";
		//add matrix header
		text += "Predicted as,";
		for(int i = 0; i < nClass - 1; i++){
			text += classNames[i] + ",";
		}
		text += classNames[nClass - 1] + "\n";
		//add matrix content
		for(int iRow = 0; iRow < nClass; iRow++){
			text += classNames[iRow] + ",";
			for(int iCol = 0; iCol < nClass - 1; iCol++){
				text += Integer.toString((int)confusionMatrix[iRow][iCol]) + ",";
			}
			text += Integer.toString((int)confusionMatrix[iRow][nClass - 1]) + "\n";
		}
		
		return text;
	}
	
	//get array of weights
	public static FeatureWeight[] getWeights(String fileName) throws Exception{
		String text = Common.loadTextFile(fileName);
		String[] lines = text.split("\n");
		String[] indices = lines[0].split(",");
		String[] weights = lines[1].split(",");
		int nWeight = indices.length;
		
		FeatureWeight[] fWeights = new FeatureWeight[nWeight];		
		for(int i = 0; i < nWeight; i++){
			fWeights[i] = new FeatureWeight(Integer.parseInt(indices[i]), Double.parseDouble(weights[i]));
		}
		
		return fWeights;
	}
}
