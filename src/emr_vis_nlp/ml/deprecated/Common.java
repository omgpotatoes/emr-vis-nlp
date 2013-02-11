package emr_vis_nlp.ml.deprecated;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.System;

public class Common {
	public static String getOSPath(String[] paths) throws Exception{
		String path = "";
		for(int iFile = 0; iFile < paths.length - 1; iFile++){
			path +=  paths[iFile] + File.separator;
		}
		path += paths[paths.length - 1];
		
		return path;
	}
	
	public static String getExecutingPath() throws Exception{
		return System.getProperty("user.dir");
	}
	
	public static String getParentDirectoryRecursive(String directory, int offset){
		File fParent = new File(directory);
		for(int i = 0; i < offset; i++){
			fParent = fParent.getParentFile();
		}
		return fParent.getAbsolutePath();
	}
	
	public static FeatureWeight[] sortWeights(double[] weights){
		FeatureWeight[] fWeights = new FeatureWeight[weights.length];
		//init feature weight
		for(int i = 0; i < weights.length; i++){
			fWeights[i] = new FeatureWeight(i, weights[i]);
		}
		//sort
		Mergesort.mergesort(fWeights, 0, weights.length, false);
		return fWeights;
	}
	
	public static void saveTextFile(String fileName, String text){
		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter( new FileWriter(fileName));
			writer.write(text);

		}
		catch ( IOException e)
		{
		}
		finally
		{
			try
			{
				if ( writer != null)
					writer.close( );
			}
			catch ( IOException e)
			{
			}
		}
	}
	
	public static boolean fileExists(String fileName){
		File file = new File(fileName);
		return file.exists();
	}
	
	public static String loadTextFile(String fileName) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String text = "";
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			text = sb.toString();
		} finally {
			br.close();
		}

		return text;
	}
}
