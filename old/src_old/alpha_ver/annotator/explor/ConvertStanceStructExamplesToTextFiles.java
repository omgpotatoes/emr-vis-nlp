package annotator.explor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import annotator.data.DataTag;
import annotator.data.DataTagset;
import annotator.data.Dataset;

/**
 * 
 * builds text files for the text examples associated with each arg in a stance structure, in order to run the discourse parser
 * 
 * @author alexander.p.conrad@gmail.com
 *
 */
public class ConvertStanceStructExamplesToTextFiles {
	
	
	
	public static void buildTextFiles(String stanceStructPath, String textFileDir) {
		
		Dataset dataset = Dataset.loadDatasetFromDoclist(new File(stanceStructPath));
		DataTagset tagset = dataset.getTagset();
		
		// for now, just assume 3 levels: root, side, argument
		DataTag rootTag = tagset.getTopLevelTag();
		List<DataTag> sideTags = rootTag.getChildTags();
		
		for (DataTag sideTag : sideTags) {
			
			List<DataTag> argTags = sideTag.getChildTags();
			
			// build a datafile for each arg tag
			for (DataTag argTag : argTags) {
				buildTextFileArgument(argTag, textFileDir);
			}
			
			
		}
		
		
		
		
		
	}
	
	public static void buildTextFileArgument(DataTag argTag, String dir) {
		
		String name = argTag.getAttributes().get("name");
		String text = argTag.getElaborationText();
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
	
	
	
	public static void main(String[] args) {
		buildTextFiles("/afs/cs.pitt.edu/usr0/conrada/private/Dropbox/doclist_deathpenalty_devel.xml", "/afs/cs.pitt.edu/usr0/conrada/private/Dropbox/data_blogs_deathpenalty/tagset_deathpenalty_argument_text");
		
	}
	
	
}
