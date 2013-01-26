
package annotator.annotator;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import annotator.MainWindow;
import annotator.data.Dataset;
import annotator.data.Document;
import annotator.data.DocumentMedColon;

/**
 * 
 * Tree structure for displaying correlations between variables and 
 * indicators, for the sake of identifying redundancies.
 * 
 * var/indi name -> directly-influenced vars/indis (& "other" cat) -> expand "other" cat
 * 
 * @author alexander.p.conrad@gmail.com
 */
public class JTreeIndiVarCorrs extends DefaultTreeModel {
    

    private static Map<String, List<String>> varIndiDeps = null;
    
    
    public JTreeIndiVarCorrs(DefaultMutableTreeNode topLevelTreeNode) {
        super(topLevelTreeNode);
    }
    
    
//    
//    public static DefaultMutableTreeNode buildTreeNodeLabel(DataTag dataTag) {
//        
//        DefaultMutableTreeNode treeNodeLabel = new DefaultMutableTreeNode(dataTag);
//        
//        List<DataTag> childDataTags = dataTag.getChildTags();
//        for (int i=0; i<childDataTags.size(); i++) {
//            DataTag childDataTag = childDataTags.get(i);
//            DefaultMutableTreeNode childTreeNodeLabel = buildTreeNodeLabel(childDataTag);
//            treeNodeLabel.add(childTreeNodeLabel);
//        }
//        
//        return treeNodeLabel;
//        
//    }
//    
    
    
    public static JTreeIndiVarCorrs buildColonoscopyCorrTree() {
        
        VarIndiCorr rootVarIndiCorr = new VarIndiCorr("colonoscopy_root", null);
        DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode(rootVarIndiCorr);
        
        // build list of indicators from hashmap
        Map<String, List<String>> varIndiDeps = getListOfVarIndiDeps();
        Set<String> indicators = varIndiDeps.keySet();
        
        Dataset activeDataset = MainWindow.activeDataset;
        
		if (activeDataset != null) {
			
			List<Document> docs = activeDataset.getDocuments();

			for (String indicatorName : indicators) {

				VarIndiCorr indiCorr = new VarIndiCorr(indicatorName,
						rootVarIndiCorr);
				DefaultMutableTreeNode treeIndi = new DefaultMutableTreeNode(indiCorr);
				treeRoot.add(treeIndi);

				// iterate over dataset, build list of vals for indicator
				List<Double> indiVals = new ArrayList<>();
				for (Document doc : docs) {
					
					DocumentMedColon docMed = (DocumentMedColon)doc;
					
					try {
						double indiVal = docMed.getIndicators().get(indicatorName);
//						if (indiVal != null) {
							indiVals.add(indiVal);
//						}
					} catch (NumberFormatException e) {
						// debug
						System.out.println("debug: JTreeIndiVarCorrs.buildColonoscopyCorrTree: weirdly formatted value for indicator "+indicatorName+" in doc "+docMed.getName());
//						e.printStackTrace();
						indiVals.add(-1.);
					} catch (NullPointerException e) {
						// debug
						System.out.println("debug: JTreeIndiVarCorrs.buildColonoscopyCorrTree: no value for indicator "+indicatorName+" in doc "+docMed.getName());
//						e.printStackTrace();
						indiVals.add(-1.);
					}
					
				}
				

				// build corrs for each associated var
				List<String> corrVars = varIndiDeps.get(indicatorName);
				List<List<Double>> corrVarVals = new ArrayList<>();
				for (String varName : corrVars) {

					// iterate over dataset; build list of vals for var
					// @TODO cache these results to speed things up
					List<Double> varVals = new ArrayList<>();
					corrVarVals.add(varVals);
					for (Document doc : docs) {

						DocumentMedColon docMed = (DocumentMedColon)doc;
						
						// if variable / indicator isn't present, assume "not eligible" (-1);
						try {
							double varVal = Double.parseDouble(docMed.getVars().get(varName));
//							if (indiVal != null) {
								varVals.add(varVal);
//							}
						} catch (NumberFormatException e) {
							// debug
							System.out.println("debug: JTreeIndiVarCorrs.buildColonoscopyCorrTree: weirdly formatted value for variable "+varName+" in doc "+docMed.getName());
//							e.printStackTrace();
							varVals.add(-1.);
						} catch (NullPointerException e) {
							// debug
							System.out.println("debug: JTreeIndiVarCorrs.buildColonoscopyCorrTree: no value for variable "+varName+" in doc "+docMed.getName());
//							e.printStackTrace();
							varVals.add(-1.);
						}
						
					}

					// do correlation computation
					double correlation = computeCorrelation(indiVals, varVals);
					// debug
					System.out.println("debug: JTreeIndiVarCorrs.buildColonoscopyCorrTree: correlation for indicator "+indicatorName+" and variable "+varName+": "+correlation);
					VarIndiCorr varIndiLeaf = new VarIndiCorr(varName, indiCorr, correlation);
					indiCorr.addRelatedVarIndi(varIndiLeaf);
					DefaultMutableTreeNode treeVar = new DefaultMutableTreeNode(varIndiLeaf);
					treeIndi.add(treeVar);

				}

			}

        }
        
		JTreeIndiVarCorrs tree = new JTreeIndiVarCorrs(treeRoot);
		return tree;
        
    }
    
    
    public static Map<String, List<String>> getListOfVarIndiDeps() {
    	
    	if (varIndiDeps != null) {
    		return varIndiDeps;
    	}
    	
    	varIndiDeps = new HashMap<>();
    	
    	// populate map manually using rules from Phuong's paper
        
        String name = "Indicator_2";
        List<String> nameList = new ArrayList<>();
        nameList.add("VAR_Nursing_Reports");
        nameList.add("VAR_ASA");
        varIndiDeps.put(name, nameList);
        
        name = "Indicator_21";
    	nameList = new ArrayList<>();
        nameList.add("VAR_Nursing_Reports");
        nameList.add("VAR_Informed_consent");
        varIndiDeps.put(name, nameList);
        
        name = "Indicator_11";
    	nameList = new ArrayList<>();
        nameList.add("VAR_Procedure_aborted");
        nameList.add("VAR_Cecum_(reached_it)");
        nameList.add("VAR_Prep_adequate");
        nameList.add("VAR_Any_adenoma");
        varIndiDeps.put(name, nameList);
        
        name = "Indicator_16";
    	nameList = new ArrayList<>();
        nameList.add("VAR_Prep_adequate");
        varIndiDeps.put(name, nameList);
        
        name = "Indicator_17";
    	nameList = new ArrayList<>();
        nameList.add("VAR_Withdraw_time");
        varIndiDeps.put(name, nameList);
        
        name = "Indicator_19";
    	nameList = new ArrayList<>();
        nameList.add("VAR_Prep_adequate");
        nameList.add("VAR_Procedure_aborted");
        nameList.add("VAR_cecal_landmark");
        varIndiDeps.put(name, nameList);
        
        name = "Indicator_3.1";
    	nameList = new ArrayList<>();
        nameList.add("VAR_Indication_type");  // this is an abnormal var; how do we incorporate its values?
        nameList.add("VAR_Indication_Type_2");  // this is an abnormal var; how do we incorporate its values?
        nameList.add("VAR_Indication_Type_3");  // this is an abnormal var; how do we incorporate its values?
        nameList.add("VAR_Biopsy");
        varIndiDeps.put(name, nameList);
        
        return varIndiDeps;
    	
    }
    
    public static double computeCorrelation(List<Double> xVals, List<Double> yVals) {
    	
    	// means
    	double xMean = 0.;
    	for (int i=0; i<xVals.size(); i++) {
    		xMean += xVals.get(i);
    	}
    	xMean = xMean / (double)xVals.size();
    	
    	double yMean = 0.;
    	for (int i=0; i<yVals.size(); i++) {
    		yMean += yVals.get(i);
    	}
    	yMean = yMean / (double)yVals.size();
    	
    	// standard deviations
    	double xStdDev;
    	double xSqSum = 0.;
    	for (int i=0; i<xVals.size(); i++) {
    		xSqSum += Math.pow((xVals.get(i) - xMean), 2);
    	}
    	xSqSum = xSqSum / (double)xVals.size();
    	xStdDev = Math.sqrt(xSqSum);
    	
    	double yStdDev;
    	double ySqSum = 0.;
    	for (int i=0; i<yVals.size(); i++) {
    		ySqSum += Math.pow((yVals.get(i) - yMean), 2);
    	}
    	ySqSum = ySqSum / (double)yVals.size();
    	yStdDev = Math.sqrt(ySqSum);
    	
    	// numerator
    	// note: x and y need same ## of examples!
    	if (xVals.size() != yVals.size()) {
    		assert false;
    		System.err.println("err: JTreeIndiVarCoors.computeCorrelation: x and y don't have same number of observations! "+xVals.size()+" vs "+yVals.size());
    		return 0;
    	}
    	
    	int numObs = xVals.size();
    	double prodSum = 0.;
    	for (int i=0; i<numObs; i++) {
    		prodSum += (xVals.get(i) - xMean) * (yVals.get(i) - yMean);
    	}
    	double numerator = prodSum / (double)numObs;
    	
    	double correlation = numerator / (xStdDev * yStdDev);
    	
    	// debug
    	// debug
    	System.out.println("debug: correlation comp: xMean="+xMean+", yMean="+yMean+", xStdDev="+xStdDev+", yStdDev="+yStdDev+", numObs="+numObs+", numerator="+numerator);
    	System.out.println("debug: corr="+correlation);
    	
    	return correlation;
    	
    }
    
}
