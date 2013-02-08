package annotator.visualizer;

import annotator.MainWindow;
import annotator.data.Dataset;
import annotator.data.Document;
import annotator.data.DocumentMedColon;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.tc33.jheatchart.HeatChart;

/**
 *
 * JHeatmap interface class for building a JHeatmap to represent summary info
 * across whole dataset
 *
 * @author alexander.p.conrad@gmail.com
 */
public class JHeatmapSummary extends HeatChart {

    private static Map<String, List<String>> varIndiDeps = null;

    public JHeatmapSummary(double[][] zValues) {
        super(zValues);
    }

    public static JHeatmapSummary buildHeatmapForActiveDataset() {

        // if null dataset, build empty heatmap
        if (MainWindow.activeDataset == null) {
            return new JHeatmapSummary((new double[][]{}));
        }

        Dataset activeDataset = MainWindow.activeDataset;
        List<Document> docs = activeDataset.getDocuments();

        // build heatmap for dataset
        // build list of indicators from hashmap
        Map<String, List<String>> varIndiDeps = getListOfVarIndiDeps();
        Set<String> indicators = varIndiDeps.keySet();
        // get all vars, put in single list
        Map<String, Boolean> varMap = new HashMap<>();
        for (String indicator : indicators) {
            List<String> indiVars = varIndiDeps.get(indicator);
            for (String varName : indiVars) {
                if (!varMap.containsKey(varName)) {
                    varMap.put(varName, Boolean.TRUE);
                }
            }
        }
        Set<String> vars = varMap.keySet();
        List<String> allAttrs = new ArrayList<>();
        for (String indicator : indicators) {
            allAttrs.add(indicator);
        }
        for (String var : vars) {
            allAttrs.add(var);
        }

        // get corrs between all indis / vars

        double[][] correlationZValues = new double[allAttrs.size()][allAttrs.size()];
        int attr1Counter = 0;
        for (String attr1 : allAttrs) {
        	
            // iterate over dataset, build list of vals for attr
            List<Double> attrVals1 = new ArrayList<>();
            for (Document doc : docs) {

                DocumentMedColon docMed = (DocumentMedColon) doc;

                try {
                    double attrVal = -1.;
                    if (docMed.getIndicators().containsKey(attr1)) {
                        attrVal = docMed.getIndicators().get(attr1);
                    } else if (docMed.getVars().containsKey(attr1)) {
                        attrVal = Double.parseDouble(docMed.getVars().get(attr1));
                    } else {
                        // doesn't contain indi / val
                        // debug
                        System.out.println("debug: JHeatmapSummary: doc " + doc.getName() + " does not contain attr " + attr1);
                        // extra debug:
//                        System.out.println("debug: indis:\n"+docMed.getIndicators().toString()+"\nvars:\n"+docMed.getVars().toString());
                        if (attr1.equals("VAR_Biopsy")) {
                        	System.out.println("debug: vars: "+docMed.getVars().toString());
                        }
                    }
//						if (indiVal != null) {
                    attrVals1.add(attrVal);
//						}
                } catch (NumberFormatException e) {
                    // debug
                    System.out.println("debug: JHeatmapSummary: weirdly formatted value for attr " + attr1 + " in doc " + docMed.getName() + ": "+docMed.getVars().get(attr1));
//						e.printStackTrace();
                    attrVals1.add(-1.);
                } catch (NullPointerException e) {
                    // debug
                    System.out.println("debug: JHeatmapSummary: no value for attr " + attr1 + " in doc " + docMed.getName());
//						e.printStackTrace();
                    attrVals1.add(-1.);
                }

            }

            int attr2Counter = 0;
            for (String attr2 : allAttrs) {

                // iterate over dataset, build list of vals for attr
                List<Double> attrVals2 = new ArrayList<>();
                for (Document doc : docs) {

                    DocumentMedColon docMed = (DocumentMedColon) doc;

                    try {
                        double attrVal = -1.;
                        if (docMed.getIndicators().containsKey(attr2)) {
                            attrVal = docMed.getIndicators().get(attr2);
                        } else if (docMed.getVars().containsKey(attr2)) {
                            attrVal = Double.parseDouble(docMed.getVars().get(attr2));
                        } else {
                            // doesn't contain indi / val
                            // debug
                            System.out.println("debug: JHeatmapSummary: doc " + doc.getName() + " does not contain attr " + attr2);
                            // extra debug:
//                            System.out.println("debug: indis:\n"+docMed.getIndicators().toString()+"\nvars:\n"+docMed.getVars().toString());
							if (attr2.equals("VAR_Biopsy")) {
								System.out.println("debug: vars: " + docMed.getVars().toString());
							}
                        }
//						if (indiVal != null) {
                        attrVals2.add(attrVal);
//						}
                    } catch (NumberFormatException e) {
                        // debug
                        System.out.println("debug: JHeatmapSummary: weirdly formatted value for attr " + attr2 + " in doc " + docMed.getName() + ": "+docMed.getVars().get(attr2));
//						e.printStackTrace();
                        attrVals2.add(-1.);
                    } catch (NullPointerException e) {
                        // debug
                        System.out.println("debug: JHeatmapSummary: no value for attr " + attr2 + " in doc " + docMed.getName());
//						e.printStackTrace();
                        attrVals2.add(-1.);
                    }

                }
                
                // compute, store correlation
                double correlation = computeCorrelation(attrVals1, attrVals2);
                // debug
                System.out.println("debug: JHeatmapSummary: correlation for attr1 " + attr1 + " and attr2 " + attr2 + ": " + correlation);
                correlationZValues[attr1Counter][attr2Counter] = correlation;

                attr2Counter++;
            }
            
            attr1Counter++;
        }

        // build heatmap
        JHeatmapSummary heatmapSummary = new JHeatmapSummary(correlationZValues);
        heatmapSummary.setXValues(allAttrs.toArray());
        heatmapSummary.setYValues(allAttrs.toArray());
        heatmapSummary.setHighValueColour(Color.RED);
        heatmapSummary.setLowValueColour(Color.BLACK);
        
        return heatmapSummary;

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
    
    
    // for testing only!
    public static void printSelectedVarsAndIndis() {
    	
    	Map<String, List<String>> varIndiDeps = getListOfVarIndiDeps();
        Set<String> indicators = varIndiDeps.keySet();
        // get all vars, put in single list
        Map<String, Boolean> varMap = new HashMap<>();
        for (String indicator : indicators) {
            List<String> indiVars = varIndiDeps.get(indicator);
            for (String varName : indiVars) {
                if (!varMap.containsKey(varName)) {
                    varMap.put(varName, Boolean.TRUE);
                }
            }
        }
        Set<String> vars = varMap.keySet();
        List<String> allAttrs = new ArrayList<>();
        for (String indicator : indicators) {
            allAttrs.add(indicator);
        }
        for (String var : vars) {
            allAttrs.add(var);
        }
        
        for (String attr : allAttrs) {
        	System.out.println("selectedVarAndIndiList.add(\""+attr+"\");");
        }
    	
    }
    
    // for testing only!
    public static void main(String[] args) {
        
    	printSelectedVarsAndIndis();
    	
    	
//    	// setting activeDataset this was is normally a Very Bad Idea! Only do this for testing!
//    	File file = new File("/afs/cs.pitt.edu/usr0/conrada/private/Dropbox/doclist_biovis_colonoscopy1.xml");
//    	MainWindow.activeDataset = Dataset.loadDatasetFromDoclist(file);
//    	
//    	// build and display heatmap in separate window
//    	JHeatmapSummary summaryHeatmap = buildHeatmapForActiveDataset();
//    	try {
//    		summaryHeatmap.saveToFile(new File("temp_heatmap.png"));
//    	} catch (IOException e) {
//    		e.printStackTrace();
//    		System.out.println("error saving heatmap to file: "+"temp_heatmap.png");
//    	}
        
        
    }
    
}
