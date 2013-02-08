package annotator.compatibility;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

/**
 *
 * Open a Microsoft Word document and convert it to plaintext; write the text to
 * file, after optional additional processing.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class ProcessWordMedRec {

    public static List<String> splitBigStringIntoSeparateStrings(String bigString, String pattern) {

        List<String> splitStrings = new ArrayList<String>();
        Scanner splitter = new Scanner(bigString);
        splitter.useDelimiter(pattern);
        try {
            while (splitter.hasNext()) {
                String chunk = splitter.next();
                splitStrings.add(chunk);
            }
        } catch (NoSuchElementException e) {
            System.out.println("error: pattern \"" + pattern + "\" does not appear in string");
        }

        return splitStrings;

    }

    public static void writeSplitStrings(String directory, String prefix, List<String> substrings) {

        int numSubstrings = substrings.size();

        for (int s = 0; s < substrings.size(); s++) {

            String substring = substrings.get(s);
            String filePath = directory + "/" + prefix + "_" + s + ".txt";

            try {

                FileWriter writer = new FileWriter(filePath);
                writer.write(substring);
                writer.close();
                
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("error: could not write substring " + s + " to file " + filePath);
            }
            

        }

    }

    /**
     * Convert the Word-formatted version of the file to plaintext-formatted.
     *
     * @param wordDocPath
     * @return
     */
    public static String extractWordDocToText(String wordDocPath) {

        String docContents = "";

        try {

            InputStream in = new FileInputStream(wordDocPath);
            HWPFDocument wordDoc = new HWPFDocument(in);
            WordExtractor extractor = new WordExtractor(wordDoc);

            // lots of ways to get the text (theoretically)
            //String[] paragraphs = extractor.getParagraphText();
            //String text = extractor.getTextFromPieces();
            String textClean = extractor.getText();
            docContents = textClean;

            in.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error reading Word file " + wordDocPath);
        }

        return docContents;

    }

    public static void writeStringToFile(String text, String filePath) {

        try {

            FileWriter writer = new FileWriter(filePath);
            writer.write(text);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error writing to text file " + filePath);
        }

    }

    public static void main(String[] args) {
        String wordDocPath = "D:\\Users\\conrada\\Dropbox\\bioviz\\docs\\foleynote2.gd.deid.doc";
//        String textDocPath = "D:\\Users\\conrada\\Dropbox\\bioviz\\docs\\foleynote2.gd.deid.txt";
        String contents = extractWordDocToText(wordDocPath);
//        writeStringToFile(contents, textDocPath);
        List<String> splitContents = splitBigStringIntoSeparateStrings(contents, "E_O_R");
        writeSplitStrings("D:\\Users\\conrada\\Dropbox\\bioviz\\docs\\foleynote2.gd.deid", "record", splitContents);
        
    }
}
