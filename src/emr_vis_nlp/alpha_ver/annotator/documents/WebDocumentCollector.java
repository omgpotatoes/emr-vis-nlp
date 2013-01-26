package annotator.documents;

import annotator.documents.WebsiteContentFormat;
import annotator.documents.GoogleResults;
import annotator.data.Document;
import annotator.data.DocumentArguing;
import annotator.data.TextInstance;
import annotator.data.TextInstanceArguing;
import com.google.gson.Gson;

import edu.stanford.nlp.ling.CoreAnnotations.LastTaggedAnnotation;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Handles automatic acquisition of new documents via Google's api. Also handles
 * extraction of relevant document content and conversion to new xml format for
 * selected known blog sites.
 *
 * @author alexander.p.conrad@gmail.com
 */
public class WebDocumentCollector {

    static String google = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
    static String googleBlogs = "http://ajax.googleapis.com/ajax/services/search/blogs?v=1.0&q=";
    static String charset = "UTF-8";

    public static List<URL> topNResults(String query, int n) {

        List<URL> resultsUrls = new ArrayList<>();

        // get results
        try {
            for (int s = 0; s < n; s += 4) {

                // URL queryUrl = new URL(google + URLEncoder.encode(query,
                // charset) + "&start="+s);
                URL queryUrl = new URL(googleBlogs
                        + URLEncoder.encode(query, charset) + "&start=" + s);
                // debug
                System.out.println("debug: queryUrl=" + queryUrl);
                Reader reader = new InputStreamReader(queryUrl.openStream(),
                        charset);
                GoogleResults results = new Gson().fromJson(reader,
                        GoogleResults.class);

                for (int r = 0; (r + s < n && r < 4); r++) {

                    String title = results.getResponseData().getResults().get(r).getTitle();
                    String urlString = results.getResponseData().getResults().get(r).getUrl(); // url if we're using web search
                    // String urlBlogString =
                    // results.getResponseData().getResults().get(r).getBlogUrl();
                    String urlPostString = results.getResponseData().getResults().get(r).getPostUrl(); // url if we're
                    // using blog
                    // search
                    // debug
                    System.out.println("debug: result #" + (r + s) + ": \""
                            + title + "\", url=" + urlPostString);

                    // String content =
                    // results.getResponseData().getResults().get(r).getContent();
                    // // this is generally only the first few sents from the
                    // post
                    // String html =
                    // results.getResponseData().getResults().get(r).getHtml();
                    // // doesn't seem to be used by blog search

                    URL url = new URL(urlPostString);

                    resultsUrls.add(url);

                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("exception: less than n results returned");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultsUrls;

    }

    public static List<String> downloadRawWebpages(List<URL> urls) {

        List<String> rawWebpages = new ArrayList<>();

        for (URL url : urls) {

            try {

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        new BufferedInputStream(url.openStream()), "UTF-8")); // if
                // encoding
                // problems
                // persist,
                // try
                // "UTF-16"?
                String page = "";
                String line;
                while ((line = in.readLine()) != null) {
                    page += line + "\n";
                }
                // debug
                // System.out.println("debug: page: "+page); // will be very
                // long

                rawWebpages.add(page);
                in.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return rawWebpages;

    }
    public static List<Integer> lastTextStartIndices;
    public static List<Integer> lastTextEndIndices;

    public static List<String> textInstanceExtractor(String text,
            WebsiteContentFormat format) {

        List<String> textInsts = new ArrayList<>();

        // keep track of charspans in original document!
        List<Integer> textStartIndices = new ArrayList<>();
        List<Integer> textEndIndices = new ArrayList<>();

        // 1. find substring region in which main content is located
        String startPattern = "";
        if (format.usesRegularDiv()) {

            String divClass = format.getDivClass();
            startPattern = "class=\"" + divClass + "\"";

        } else {
            System.err.println("irregular div handling not yet implemented");
            assert false;
        }

        int textStartIndex = text.indexOf(startPattern);
        int charIndexStart = textStartIndex;
        String textFollowingDiv = text;
        if (textStartIndex == -1) {
            charIndexStart = 0;
            System.err.println("startPattern=\"" + startPattern
                    + "\" not found in text");
            assert false;
        } else {
            textFollowingDiv = text.substring(textStartIndex);
        }
        // String textFollowingDiv = text.substring(textStartIndex);

        // 1b. find end of content section
        // strategy: find end of divs, use a stack to handle nested divs
        int divDepth = 1;
        int currentTextIndex = 0;
        while (divDepth > 0) {
            // find next <div , </div
            int nextDivIndex = textFollowingDiv.indexOf("<div",
                    currentTextIndex);
            int nextDivEndIndex = textFollowingDiv.indexOf("</div",
                    currentTextIndex);
            if (nextDivIndex != -1
                    && (nextDivEndIndex == -1 || nextDivIndex < nextDivEndIndex)) {
                // if there's a nextDiv and either there's no nextDivEnd
                // [shouldn't happen?] or the nextDiv comes before the
                // nextDivEnd, handle the nextDiv
                currentTextIndex = nextDivIndex + 4;
                divDepth++;
            } else if (nextDivEndIndex != -1
                    && (nextDivIndex == -1 || nextDivEndIndex < nextDivIndex)) {
                // vice versa, handle the nextDivEnd
                currentTextIndex = nextDivEndIndex + 5;
                divDepth--;
            } else {
                // shouldn't happen, unless there are neither divs nor divEnds
                System.err.println("error while searching for end of text content section; improper div usage?");
                // debug
                System.out.println("debug: nextDivEndIndex=" + nextDivEndIndex
                        + ", nextDivIndex=" + nextDivIndex
                        + ", currentTextIndex=" + currentTextIndex
                        + ", divDepth=" + divDepth);
                currentTextIndex = -1;
                break;
            }

        }
        String textContent = textFollowingDiv;
        if (currentTextIndex != -1) {
            textContent = textFollowingDiv.substring(0, currentTextIndex - 5);
        }
        // debug
        // System.out.println("debug: textContent: "+textContent);

        // 1c. mash together all lines?
        String textContentMashed = "";
        Scanner textContentSplitter = new Scanner(textContent);
        while (textContentSplitter.hasNextLine()) {
            // String line = textContentSplitter.nextLine();
            String line = textContentSplitter.nextLine().toLowerCase();
            textContentMashed += line + " ";
        }

        // 2. find and extract all <p ?? >...</p> (may be other junk in starting
        // <p> that we don't care about?)
        Scanner textContentParaSplitter = new Scanner(textContentMashed);
        textContentParaSplitter.useDelimiter("<p");
        textContentParaSplitter.next();
        while (textContentParaSplitter.hasNext()) {

            String temp = textContentParaSplitter.next();
            String paraRaw = temp;
            if (temp.charAt(0) == '>') {
                // eliminate the starting '>'
                temp = temp.substring(1);
            } else {
                // we're still in an html tag, so keep an opening '<' for html removal later
                temp = "<"+temp;
            }
            // String paraStart = "<"+temp;
            String paraStart = temp;

            int paraEndIndex = paraStart.indexOf("</p");
            int paraEndIndexRaw = paraRaw.indexOf("</p");
            if (paraEndIndex != -1) {
                // String para = paraStart.substring(1, paraEndIndex);
                String para = paraStart.substring(0, paraEndIndex);

                // find start, end indices
                int startIndex = textContentMashed.indexOf(paraRaw)
                        + charIndexStart - 2; // subtract 2 so we get opening <p
                int endIndex = 0;
                if (textContentMashed.indexOf(paraRaw) != -1) {
                    endIndex = startIndex+2 + paraEndIndexRaw+4; // add 4 so we get closing </p>
                } else {
                    // shouldn't happen
                    System.err.println("could not find index of paragraph \""
                            + paraRaw + "\" in document:\n " + textContentMashed);
                    endIndex = charIndexStart + textContentMashed.length();
                }

                textInsts.add(para);
                textStartIndices.add(startIndex);
                textEndIndices.add(endIndex);

            } else {
                // shouldn't happen
                System.err.println("unable to find end of paragraph in textSpan \""
                        + paraStart + "\"");

                // find start, end indices
                int startIndex = textContentMashed.indexOf(paraStart)
                        + charIndexStart;
                int endIndex = 0;
                if (startIndex != -1) {
                    endIndex = startIndex + paraStart.length();
                } else {
                    // shouldn't happen
                    System.err.println("could not find index of paragraph \""
                            + paraStart + "\" in document:\n "
                            + textContentMashed);
                    endIndex = charIndexStart + textContentMashed.length();
                }

                textInsts.add(paraStart);
                textStartIndices.add(startIndex);
                textEndIndices.add(endIndex);

            }
        }

        // 2b: remove other html markup junk from paragraph, convert escape
        // chars
        String markupPattern = "<.*?>"; // .*? instead of .* for non-greedy
        // matching, so we match smallest
        // instead of largest instances
        for (int p = 0; p < textInsts.size(); p++) {

            String para = textInsts.get(p);
            para = StringEscapeUtils.unescapeHtml(para);
            // add an opening '<' to match p's with arguments
//            para = "<" + para;    // should no longer need to add opening '<', this is now handled earlier
            para = para.replaceAll(markupPattern, " ").trim();
//            if (para.length() > 0 && para.charAt(0) == '<') {
//                para = para.substring(1);
//            }
//
//            para = para.trim();

            // 2c: if span isn't empty, add to list
            if (!para.equals("")) {
                textInsts.remove(p);
                textInsts.add(p, para);
            } else {
                textInsts.remove(p);
                textStartIndices.remove(p);
                textEndIndices.remove(p);
                p--;
            }

        }

        // // debug
        // System.out.println("debug: text insts:");
        // for (String para : textInsts) {
        // System.out.println("\t" + para);
        // }

        lastTextStartIndices = textStartIndices;
        lastTextEndIndices = textEndIndices;

        return textInsts;

    }

    public static Document buildPlainDoc(String docName, String docPath,
            String urlName, List<String> paras, String dataSource) {

        Document doc = new DocumentArguing(docName, docPath);
        doc.getAttributes().put("source", dataSource);
        doc.getAttributes().put("url", urlName);
        List<TextInstance> textInstances = doc.getTextInstances();
        for (int p = 0; p < paras.size(); p++) {
            String para = paras.get(p);
            TextInstance textInst = new TextInstanceArguing();
            textInst.setTextStr(para);
            textInstances.add(textInst);
        }

        return doc;

    }

    public static Document buildPlainDoc(String docName, String docPath,
            String urlName, List<String> paras, String dataSource,
            List<Integer> startIndices, List<Integer> endIndices) {

        Document doc = new DocumentArguing(docName, docPath);
        doc.getAttributes().put("source", dataSource);
        doc.getAttributes().put("url", urlName);
        List<TextInstance> textInstances = doc.getTextInstances();
        for (int p = 0; p < paras.size(); p++) {
            String para = paras.get(p);
            TextInstance textInst = new TextInstanceArguing();
            textInst.setTextStr(para);
            textInst.getAttributes().put("startIndex", startIndices.get(p) + "");
            textInst.getAttributes().put("endIndex", endIndices.get(p) + "");
            textInstances.add(textInst);
        }

        return doc;

    }

//	public static void buildGayMarriageDataset() {
//
//		// int numResultsPerSite = 125; // 100 to training, 25 (randomly chosen)
//		// to devel // api limitation: 64?
//		int numResultsPerSite = 64; // 100 to training, 25 (randomly chosen) to
//									// devel // api limitation: 64?
//		// int numDevelPerSite = 25; // since we can only take 64 per site, take
//		// fewer devel from each (14?)
//		int numDevelPerSite = 14;
//		List<Document> trainingDocs = new ArrayList<>();
//		List<Document> develDocs = new ArrayList<>();
//
//		// pro
//		List<URL> urlsAlterNet = topNResults(
//				"site:blogs.alternet.org/ gay marriage", numResultsPerSite);
//		List<URL> urlsHuffingtonPost = topNResults(
//				"site:www.huffingtonpost.com/theblog/ gay marriage",
//				numResultsPerSite);
//
//		// anti
//		List<URL> urlsTownhall = topNResults(
//				"site:townhall.com/columnists/ gay marriage", numResultsPerSite);
//		List<URL> urlsRedState = topNResults("site:redstate.com/ gay marriage",
//				numResultsPerSite);
//
//		for (int d = 0; d < 4; d++) {
//
//			List<URL> urls = null;
//			WebsiteContentFormat contentFormat = null;
//			String siteString = "";
//			String siteName = "";
//			switch (d) {
//			case 0:
//				urls = urlsAlterNet;
//				siteString = "site:blogs.alternet.org/";
//				siteName = "alternet";
//				contentFormat = WebsiteContentFormat.ALTERNET;
//				break;
//			case 1:
//				urls = urlsHuffingtonPost;
//				siteString = "site:www.huffingtonpost.com/theblog/";
//				siteName = "huffpost";
//				contentFormat = WebsiteContentFormat.HUFFPOST;
//				break;
//			case 2:
//				urls = urlsTownhall;
//				siteString = "site:townhall.com/columnists/";
//				siteName = "townhall";
//				contentFormat = WebsiteContentFormat.TOWNHALL;
//				break;
//			case 3:
//				urls = urlsRedState;
//				siteString = "site:redstate.com/";
//				siteName = "redstate";
//				contentFormat = WebsiteContentFormat.REDSTATE;
//				break;
//			default:
//				// shouldn't happen
//				System.err.println("invalid dataset selection: " + d);
//				assert false;
//				return;
//			}
//
//			List<String> pages = downloadRawWebpages(urls);
//			List<Document> docsThisDataset = new ArrayList<>();
//			List<Integer> docCounter = new ArrayList<>();
//
//			for (int p = 0; p < pages.size(); p++) {
//
//				String rawPage = pages.get(p);
//
//				// let's save the raw html too, just to be safe
//				String rawHtmlFilePath = "./gaymarriage_data/raw_html/"
//						+ siteName + "_" + p + ".html";
//				try {
//					FileWriter out = new FileWriter(rawHtmlFilePath);
//					out.write(rawPage);
//					out.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//					System.out.println("error writing raw html to file "
//							+ rawHtmlFilePath);
//				}
//
//				List<String> instances = textInstanceExtractor(rawPage,
//						contentFormat);
//				Document doc = buildPlainDoc(siteName + "_" + p,
//						"./gaymarriage_data/training/" + siteName + "_" + p
//								+ ".xml", urls.get(p).toExternalForm(),
//						instances, siteName);
//				docCounter.add(p);
//				// doc.writeDoc(); // wait till we've randomly picked out the
//				// docs
//				docsThisDataset.add(doc);
//
//			}
//
//			// randomly pick out some documents for devel
//			Random rand = new Random();
//			for (int r = 0; r < numDevelPerSite; r++) {
//				int nextDoc = rand.nextInt(docsThisDataset.size());
//				Document docToDevel = docsThisDataset.remove(nextDoc);
//				int p = docCounter.remove(nextDoc);
//				docToDevel.setDocPath("./gaymarriage_data/devel/" + siteName
//						+ "_" + p + ".xml");
//				develDocs.add(docToDevel);
//			}
//
//			trainingDocs.addAll(docsThisDataset);
//
//		}
//
//		// write all docs to disk
//
//		// build, write doclists
//		// for now, just do it manually; files are simple enough
//		String doclistTraining = "";
//		doclistTraining += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<Dataset name=\"gaymarriage_train\" type=\"datasetarguing\">\n\t<DataTagset>tagset_gaymarriage.xml</DataTagset>\n";
//		for (int d = 0; d < trainingDocs.size(); d++) {
//			Document doc = trainingDocs.get(d);
//			doc.writeDoc();
//			String docPath = doc.getDocPath();
//			String docPathEntry = "\t<Document>" + docPath + "</Document>\n";
//			doclistTraining += docPathEntry;
//		}
//		doclistTraining += "</Dataset>";
//
//		String trainDoclistPath = "doclist_gaymarriage_train.xml";
//		try {
//			FileWriter out = new FileWriter(trainDoclistPath);
//			out.write(doclistTraining);
//			out.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.out.println("error writing training doclist to file "
//					+ trainDoclistPath);
//		}
//
//		String doclistDevel = "";
//		doclistDevel += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<Dataset name=\"gaymarriage_devel\" type=\"datasetarguing\">\n\t<DataTagset>tagset_gaymarriage.xml</DataTagset>\n";
//		for (int d = 0; d < develDocs.size(); d++) {
//			Document doc = develDocs.get(d);
//			doc.writeDoc();
//			String docPath = doc.getDocPath();
//			String docPathEntry = "\t<Document>" + docPath + "</Document>\n";
//			doclistDevel += docPathEntry;
//		}
//		doclistDevel += "</Dataset>";
//
//		String develDoclistPath = "doclist_gaymarriage_devel.xml";
//		try {
//			FileWriter out = new FileWriter(develDoclistPath);
//			out.write(doclistDevel);
//			out.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.out.println("error writing devel doclist to file "
//					+ develDoclistPath);
//		}
//
//	}
//
//	public static void buildDeathPenaltyDataset() {
//
//		// int numResultsPerSite = 125; // 100 to training, 25 (randomly chosen)
//		// to devel // api limitation: 64?
//		int numResultsPerSite = 64; // 100 to training, 25 (randomly chosen) to
//									// devel // api limitation: 64?
//		// int numDevelPerSite = 25; // since we can only take 64 per site, take
//		// fewer devel from each (14?)
//		int numDevelPerSite = 14;
//		List<Document> trainingDocs = new ArrayList<>();
//		List<Document> develDocs = new ArrayList<>();
//
//		// pro
//		// List<URL> urlsAlterNet =
//		// topNResults("site:blogs.alternet.org/ death penalty",
//		// numResultsPerSite);
//		// List<URL> urlsAlterNet =
//		// topNResults("site:alternet.org/ death penalty", numResultsPerSite);
//		List<URL> urlsFireDogLake = topNResults(
//				"site:firedoglake.com death penalty", numResultsPerSite);
//		List<URL> urlsHuffingtonPost = topNResults(
//				"site:www.huffingtonpost.com/theblog/ death penalty",
//				numResultsPerSite);
//
//		// anti
//		List<URL> urlsTownhall = topNResults(
//				"site:townhall.com/columnists/ death penalty",
//				numResultsPerSite);
//		List<URL> urlsRedState = topNResults(
//				"site:redstate.com/ death penalty", numResultsPerSite);
//
//		// while we're processing documents, record mappings of docs to original
//		// URLs
//		String fileUrlMappingPath = "./data/deathpenalty_blogs/deathpenalty_file_urls.txt";
//
//		try {
//
//			FileWriter fileUrlMappingWriter = new FileWriter(fileUrlMappingPath);
//
//			for (int d = 0; d < 4; d++) {
//
//				List<URL> urls = null;
//				WebsiteContentFormat contentFormat = null;
//				String siteString = "";
//				String siteName = "";
//				switch (d) {
//				case 0:
//					// urls = urlsAlterNet;
//					// siteString = "site:blogs.alternet.org/";
//					// siteName = "alternet";
//					// contentFormat = WebsiteContentFormat.ALTERNET;
//					// break;
//					urls = urlsFireDogLake;
//					siteString = "site:firedoglake.com";
//					siteName = "firedoglake";
//					contentFormat = WebsiteContentFormat.FIREDOGLAKE;
//					break;
//				case 1:
//					urls = urlsHuffingtonPost;
//					siteString = "site:www.huffingtonpost.com/theblog/";
//					siteName = "huffpost";
//					contentFormat = WebsiteContentFormat.HUFFPOST;
//					break;
//				case 2:
//					urls = urlsTownhall;
//					siteString = "site:townhall.com/columnists/";
//					siteName = "townhall";
//					contentFormat = WebsiteContentFormat.TOWNHALL;
//					break;
//				case 3:
//					urls = urlsRedState;
//					siteString = "site:redstate.com/";
//					siteName = "redstate";
//					contentFormat = WebsiteContentFormat.REDSTATE;
//					break;
//				default:
//					// shouldn't happen
//					System.err.println("invalid dataset selection: " + d);
//					assert false;
//					return;
//				}
//
//				List<String> pages = downloadRawWebpages(urls);
//				List<Document> docsThisDataset = new ArrayList<>();
//				List<Integer> docCounter = new ArrayList<>();
//
//				for (int p = 0; p < pages.size(); p++) {
//
//					String rawPage = pages.get(p);
//					String pageUrl = urls.get(p).toString();
//
//					// let's save the raw html too, just to be safe
//					// String rawHtmlFilePath =
//					// "./data_blogs_deathpenalty/raw_html/"+ siteName + "_" + p
//					// +
//					// ".html";
//					String pageName = siteName + "_" + p + ".html";
//					String rawHtmlFilePath = "./data/deathpenalty_blogs/raw_html/"
//							+ pageName;
//					try {
//						FileWriter out = new FileWriter(rawHtmlFilePath);
//						out.write(rawPage);
//						out.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//						System.out.println("error writing raw html to file "
//								+ rawHtmlFilePath);
//					}
//
//					// also, record mapping of pageName and pageUrl
//					String fileUrlMappingLine = pageName + "\t" + pageUrl
//							+ "\n";
//					fileUrlMappingWriter.append(fileUrlMappingLine);
//
//					List<String> instances = textInstanceExtractor(rawPage,
//							contentFormat);
//					List<Integer> startIndices = lastTextStartIndices;
//					List<Integer> endIndices = lastTextEndIndices;
//					// Document doc = buildPlainDoc(siteName + "_" + p,
//					// "deathpenalty_blogs/expr/" + siteName + "_" + p + ".xml",
//					// urls.get(p).toExternalForm(), instances, siteName);
//					Document doc = buildPlainDoc(siteName + "_" + p,
//							"./data/deathpenalty_blogs/expr/" + siteName + "_"
//									+ p + ".xml", urls.get(p).toExternalForm(),
//							instances, siteName, startIndices, endIndices);
//					docCounter.add(p);
//					// doc.writeDoc(); // wait till we've randomly picked out
//					// the
//					// docs
//					docsThisDataset.add(doc);
//
//				}
//
//				// randomly pick out some documents for devel
//				Random rand = new Random();
//				for (int r = 0; r < numDevelPerSite; r++) {
//					int nextDoc = rand.nextInt(docsThisDataset.size());
//					Document docToDevel = docsThisDataset.remove(nextDoc);
//					int p = docCounter.remove(nextDoc);
//					docToDevel.setDocPath("./data/deathpenalty_blogs/devel/"
//							+ siteName + "_" + p + ".xml");
//					develDocs.add(docToDevel);
//				}
//
//				trainingDocs.addAll(docsThisDataset);
//
//			}
//                        
//                        fileUrlMappingWriter.close();
//
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.out
//					.println("error while writing document url mapping file: "
//							+ fileUrlMappingPath);
//		}
//
//		// write all docs to disk
//
//		// build, write doclists
//		// for now, just do it manually; files are simple enough
//		String doclistTraining = "";
//		doclistTraining += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<Dataset name=\"deathpenalty_expr\" type=\"datasetarguing\">\n\t<DataTagset>./data/deathpenalty_blogs/tagset_deathpenalty_condensed.xml</DataTagset>\n";
//		for (int d = 0; d < trainingDocs.size(); d++) {
//			Document doc = trainingDocs.get(d);
//			doc.writeDoc();
//			String docPath = doc.getDocPath();
//			String docPathEntry = "\t<Document>" + docPath + "</Document>\n";
//			doclistTraining += docPathEntry;
//		}
//		doclistTraining += "</Dataset>";
//
//		String trainDoclistPath = "./doclist_deathpenalty_expr.xml";
//		try {
//			FileWriter out = new FileWriter(trainDoclistPath);
//			out.write(doclistTraining);
//			out.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.out.println("error writing training doclist to file "
//					+ trainDoclistPath);
//		}
//
//		String doclistDevel = "";
//		doclistDevel += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<Dataset name=\"deathpenalty_devel\" type=\"datasetarguing\">\n\t<DataTagset>./data/deathpenalty_blogs/tagset_deathpenalty_condensed.xml</DataTagset>\n";
//		for (int d = 0; d < develDocs.size(); d++) {
//			Document doc = develDocs.get(d);
//			doc.writeDoc();
//			String docPath = doc.getDocPath();
//			String docPathEntry = "\t<Document>" + docPath + "</Document>\n";
//			doclistDevel += docPathEntry;
//		}
//		doclistDevel += "</Dataset>";
//
//		String develDoclistPath = "./doclist_deathpenalty_devel.xml";
//		try {
//			FileWriter out = new FileWriter(develDoclistPath);
//			out.write(doclistDevel);
//			out.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			System.out.println("error writing devel doclist to file "
//					+ develDoclistPath);
//		}
//
//	}
    // alternative energy
    public static void buildDataset(String searchString, String datasetName) {

        // int numResultsPerSite = 125; // 100 to training, 25 (randomly chosen)
        // to devel // api limitation: 64?
        int numResultsPerSite = 64; // 100 to training, 25 (randomly chosen) to
        // devel // api limitation: 64?
        // int numDevelPerSite = 25; // since we can only take 64 per site, take
        // fewer devel from each (14?)
        int numDevelPerSite = 14;
        List<Document> trainingDocs = new ArrayList<>();
        List<Document> develDocs = new ArrayList<>();

        // pro
        // List<URL> urlsAlterNet =
        // topNResults("site:blogs.alternet.org/ death penalty",
        // numResultsPerSite);
        // List<URL> urlsAlterNet =
        // topNResults("site:alternet.org/ death penalty", numResultsPerSite);
        List<URL> urlsFireDogLake = topNResults(
                "site:firedoglake.com " + searchString, numResultsPerSite);
        List<URL> urlsHuffingtonPost = topNResults(
                "site:www.huffingtonpost.com/theblog/ " + searchString,
                numResultsPerSite);

        // anti
        List<URL> urlsTownhall = topNResults(
                "site:townhall.com/columnists/ " + searchString,
                numResultsPerSite);
        List<URL> urlsRedState = topNResults(
                "site:redstate.com/ " + searchString, numResultsPerSite);

        // while we're processing documents, record mappings of docs to original
        // URLs
        String fileUrlMappingPath = "./data/" + datasetName + "_blogs/" + datasetName + "_file_urls.txt";

        try {

            FileWriter fileUrlMappingWriter = new FileWriter(fileUrlMappingPath);

            for (int d = 0; d < 4; d++) {

                List<URL> urls = null;
                WebsiteContentFormat contentFormat = null;
                String siteString = "";
                String siteName = "";
                switch (d) {
                    case 0:
                        // urls = urlsAlterNet;
                        // siteString = "site:blogs.alternet.org/";
                        // siteName = "alternet";
                        // contentFormat = WebsiteContentFormat.ALTERNET;
                        // break;
                        urls = urlsFireDogLake;
                        siteString = "site:firedoglake.com";
                        siteName = "firedoglake";
                        contentFormat = WebsiteContentFormat.FIREDOGLAKE;
                        break;
                    case 1:
                        urls = urlsHuffingtonPost;
                        siteString = "site:www.huffingtonpost.com/theblog/";
                        siteName = "huffpost";
                        contentFormat = WebsiteContentFormat.HUFFPOST;
                        break;
                    case 2:
                        urls = urlsTownhall;
                        siteString = "site:townhall.com/columnists/";
                        siteName = "townhall";
                        contentFormat = WebsiteContentFormat.TOWNHALL;
                        break;
                    case 3:
                        urls = urlsRedState;
                        siteString = "site:redstate.com/";
                        siteName = "redstate";
                        contentFormat = WebsiteContentFormat.REDSTATE;
                        break;
                    default:
                        // shouldn't happen
                        System.err.println("invalid dataset selection: " + d);
                        assert false;
                        return;
                }

                List<String> pages = downloadRawWebpages(urls);
                List<Document> docsThisDataset = new ArrayList<>();
                List<Integer> docCounter = new ArrayList<>();

                for (int p = 0; p < pages.size(); p++) {

                    String rawPage = pages.get(p);
                    String pageUrl = urls.get(p).toString();

                    // let's save the raw html too, just to be safe
                    // String rawHtmlFilePath =
                    // "./data_blogs_deathpenalty/raw_html/"+ siteName + "_" + p
                    // +
                    // ".html";
                    String pageName = siteName + "_" + p + ".html";
                    String rawHtmlFilePath = "./data/" + datasetName + "_blogs/raw_html/"
                            + pageName;
                    try {
                        FileWriter out = new FileWriter(rawHtmlFilePath);
                        out.write(rawPage);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("error writing raw html to file "
                                + rawHtmlFilePath);
                    }

                    // also, record mapping of pageName and pageUrl
                    String fileUrlMappingLine = pageName + "\t" + pageUrl
                            + "\n";
                    fileUrlMappingWriter.write(fileUrlMappingLine);

                    List<String> instances = textInstanceExtractor(rawPage,
                            contentFormat);
                    List<Integer> startIndices = lastTextStartIndices;
                    List<Integer> endIndices = lastTextEndIndices;
                    // Document doc = buildPlainDoc(siteName + "_" + p,
                    // "deathpenalty_blogs/expr/" + siteName + "_" + p + ".xml",
                    // urls.get(p).toExternalForm(), instances, siteName);
                    Document doc = buildPlainDoc(siteName + "_" + p,
                            "./data/" + datasetName + "_blogs/expr/" + siteName + "_"
                            + p + ".xml", urls.get(p).toExternalForm(),
                            instances, siteName, startIndices, endIndices);
                    docCounter.add(p);
                    // doc.writeDoc(); // wait till we've randomly picked out
                    // the
                    // docs
                    docsThisDataset.add(doc);

                }

                // randomly pick out some documents for devel
                Random rand = new Random();
                for (int r = 0; r < numDevelPerSite; r++) {
                    int nextDoc = rand.nextInt(docsThisDataset.size());
                    Document docToDevel = docsThisDataset.remove(nextDoc);
                    int p = docCounter.remove(nextDoc);
                    docToDevel.setDocPath("./data/" + datasetName + "_blogs/devel/"
                            + siteName + "_" + p + ".xml");
                    develDocs.add(docToDevel);
                }

                trainingDocs.addAll(docsThisDataset);

            }
            
            fileUrlMappingWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error while writing document url mapping file: "
                    + fileUrlMappingPath);
        }
        
        

        // write all docs to disk

        // build, write doclists
        // for now, just do it manually; files are simple enough
        String doclistTraining = "";
        doclistTraining += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<Dataset name=\"" + datasetName + "_expr\" type=\"datasetarguing\">\n\t<DataTagset>./data/" + datasetName + "_blogs/tagset_" + datasetName + "_condensed.xml</DataTagset>\n";
        for (int d = 0; d < trainingDocs.size(); d++) {
            Document doc = trainingDocs.get(d);
            doc.writeDoc();
            String docPath = doc.getDocPath();
            String docPathEntry = "\t<Document>" + docPath + "</Document>\n";
            doclistTraining += docPathEntry;
        }
        doclistTraining += "</Dataset>";

        String trainDoclistPath = "./doclist_" + datasetName + "_expr.xml";
        try {
            FileWriter out = new FileWriter(trainDoclistPath);
            out.write(doclistTraining);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error writing training doclist to file "
                    + trainDoclistPath);
        }

        String doclistDevel = "";
        doclistDevel += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<Dataset name=\"" + datasetName + "_devel\" type=\"datasetarguing\">\n\t<DataTagset>./data/" + datasetName + "_blogs/tagset_" + datasetName + "_condensed.xml</DataTagset>\n";
        for (int d = 0; d < develDocs.size(); d++) {
            Document doc = develDocs.get(d);
            doc.writeDoc();
            String docPath = doc.getDocPath();
            String docPathEntry = "\t<Document>" + docPath + "</Document>\n";
            doclistDevel += docPathEntry;
        }
        doclistDevel += "</Dataset>";

        String develDoclistPath = "./doclist_" + datasetName + "_devel.xml";
        try {
            FileWriter out = new FileWriter(develDoclistPath);
            out.write(doclistDevel);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error writing devel doclist to file "
                    + develDoclistPath);
        }

    }

    // illegal immigration?
    public static void main(String[] args) {

        buildDataset("death penalty", "deathpenalty");
        buildDataset("alternative energy", "altenergy");
        buildDataset("illegal immigration", "illimmigration");



    }
}
