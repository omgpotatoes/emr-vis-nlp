package annotator.backend;


public class demo {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
            
		ColonoscopyDemo demo = new ColonoscopyDemo();
                
                // temp.arff contains one instance (and names of all the attributes on which the models were trained)
		double[][] predicts = demo.predictInstance("resources/backend/temp.arff");
		for(int i = 0; i < predicts.length; i++){
			for(int j = 0; j < 3; j++){
				System.out.print(predicts[i][j] + ",");
			}
			System.out.println();
		}
	}
}
