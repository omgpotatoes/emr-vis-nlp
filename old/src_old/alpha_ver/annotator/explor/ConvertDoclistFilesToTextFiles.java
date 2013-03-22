
package annotator.explor;

import annotator.data.Dataset;
import annotator.data.Document;
import annotator.data.TextInstance;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * 
 * converts xml datafiles as specified in a doclist to plaintext, for the purpose of discourse parsing
 *
 * @author alexander.p.conrad@gmail.com
 */
public class ConvertDoclistFilesToTextFiles {
    
	
	public static void buildTextFiles(String stanceStructPath, String textFileDir) {
		
		Dataset dataset = Dataset.loadDatasetFromDoclist(new File(stanceStructPath));
		List<Document> documents = dataset.getDocuments();
		
		// for now, just assume 3 levels: root, side, argument
                for (Document document : documents) {
                    buildTextFile(document, textFileDir);
                }
		
	}
	
	public static void buildTextFile(Document document, String dir) {
		
		String name = document.getName();
                List<TextInstance> textNodes = document.getTextInstances();
		String text = "";
                for (TextInstance textNode : textNodes) {
                    String nodeText = textNode.getTextStr();
                    // do a little processing, so discourse parser doesn't get too confused
                    if (nodeText.charAt(0) == '>') {
                        nodeText = nodeText.substring(1);
                    }
                    nodeText = fixEncoding(nodeText);
                    text += nodeText + "\n\n";
                }
		String path = dir+"/"+name+".txt";
		
		try {
			
			System.out.println("writing to file "+path);
			FileWriter writer = new FileWriter(path);
			writer.write(text);
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("error writing to file "+path);
		}
		
	}
	
	public static String fixEncoding(String origStr) {
            origStr = origStr.replaceAll("â€™", "'");
            origStr = origStr.replaceAll("â€“", "-");
            origStr = origStr.replaceAll("â€”", "-");
            origStr = origStr.replaceAll("â€œ", "\"");
            origStr = origStr.replaceAll("â€&#157", "\"");
            origStr = origStr.replaceAll("â€&", "\"");
            origStr = origStr.replaceAll("â€¦", "-");
            //origStr = origStr.replaceAll("â€¦", "...");
            origStr = origStr.replaceAll("â€", "\"");
            origStr = origStr.replaceAll("Ã¯", "i");
            origStr = origStr.replaceAll("Â", " ");
            //origStr = origStr.replaceAll("", "");
            return origStr;
        }
        
        
	
	public static void main(String[] args) {
            //String dropboxRoot = "/afs/cs.pitt.edu/usr0/conrada/private/Dropbox/";
            String dropboxRoot = "D:\\Users\\conrada\\Dropbox\\";
		buildTextFiles(dropboxRoot+"doclist_deathpenalty_devel.xml", dropboxRoot+"data_blogs_deathpenalty/devel");
		
	}
	
	
}