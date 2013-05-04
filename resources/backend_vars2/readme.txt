There are 3 types of file
	models (learned classifiers): the front end can load and performing predicting on a specific record
		naming convention:
			<variable name>-fold<fold number>of<total folds>-model.model			
				for example: any-adenoma-fold0of5-model.model means this file is the model of variable any-adenoma, the first fold among 5 folds for cross validation
	weights (feature weights): the front end can load and use for high lighting keywords
		naming convention:
			<variable name>-fold<fold number>of<total folds>-featureWeight-trainSet.csv
				for example: any-adenoma-fold0of5-featureWeight-trainSet.csv means this file contains features' weights of variable any-adenoma, the first fold among 5 folds for cross validation
		content: comma-separate-value, first row cotains keywords, second row contains corresponding weights
	reportIDs (reportID in each fold): the front end can use this information to know which reports are used in a specific model
		naming convention:
			<variable name>-fold<fold number>of<total folds>-reportID-<trainSet or testSet>.csv
			for example: any-adenoma-fold0of5-reportID-testSet.csv means this file contains report IDs of variable any-adenoma in the testSet of first fold among 5 folds for cross validation
		content: comma-separate-value, each line contain a reportID