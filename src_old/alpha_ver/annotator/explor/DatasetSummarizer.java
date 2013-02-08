package annotator.explor;

import java.io.File;

import annotator.data.Dataset;
import annotator.data.Document;
import annotator.data.TextInstance;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * Simple class to print summary information concerning a dataset.
 *
 * @author alexander.p.conrad@gmail.com
 *
 */
public class DatasetSummarizer {

    public static void printDatasetSummaryInfo(String doclistPath, String datasetName) {


        File file = new File(doclistPath);

        // read selected files, update panels
        Dataset activeDataset = Dataset.loadDatasetFromDoclist(file);

        List<Document> docs = activeDataset.getDocuments();

        int docCounter = docs.size();
        int docWithArgCounter = 0;
        int paraCounter = 0;
        int paraWithArgCounter = 0;
        Map<String, Integer> argumentMapCounter = new HashMap<>();

        for (Document doc : docs) {

            List<TextInstance> paras = doc.getTextInstances();

            boolean docContainsArg = false;
            for (TextInstance para : paras) {
                paraCounter++;
                String argName = "";
                if (para.getAttributes().containsKey("argument") && !(argName = para.getAttributes().get("argument").trim()).equals("")) {
                    docContainsArg = true;
                    paraWithArgCounter++;
                    if (!argumentMapCounter.containsKey(argName)) {
                        argumentMapCounter.put(argName, 1);
                    } else {
                        argumentMapCounter.put(argName, argumentMapCounter.get(argName) + 1);
                    }
                }
            }
            if (docContainsArg) {
                docWithArgCounter++;
            }

        }

        System.out.println("\n\n for dataset " + datasetName + ":");
        System.out.println("#docs=" + docCounter + ", #docsWithArgument=" + docWithArgCounter);
        System.out.println("#paras=" + paraCounter + ", #parasWithArgument=" + paraWithArgCounter);
        System.out.println("argument counts:");
        Set<String> argNames = argumentMapCounter.keySet();
        for (String argName : argNames) {
            System.out.println("\targument=\"" + argName + "\", count=" + argumentMapCounter.get(argName));
        }


    }

    public static void main(String[] args) {
        
        String doclistRoot = "D:\\Users\\conrada\\Dropbox\\";
        printDatasetSummaryInfo(doclistRoot+"doclist_deathpenalty_devel.xml", "deathpenalty_devel");
        printDatasetSummaryInfo(doclistRoot+"doclist_deathpenalty_expr.xml", "deathpenalty_expr");
        printDatasetSummaryInfo(doclistRoot+"doclist_illimmigration_devel.xml", "illimmigration_devel");
        printDatasetSummaryInfo(doclistRoot+"doclist_illimmigration_expr.xml", "illimmigration_expr");
        printDatasetSummaryInfo(doclistRoot+"doclist_altenergy_devel.xml", "altenergy_devel");
        printDatasetSummaryInfo(doclistRoot+"doclist_altenergy_expr.xml", "altenergy_expr");
        
    }
    
}
