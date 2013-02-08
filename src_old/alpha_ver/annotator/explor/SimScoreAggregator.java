package annotator.explor;

import annotator.explor.SimScoreMatchTuple;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * 
 * Evaluates the similarity scores as returned by Mihai's system wrt. similarity
 * of arguments expressed, as judged by procon.org-based stance struct. Pairs
 * consist of paragraph from blog, paragraph from stance struct example.
 * 
 * scores: 1.0 == same argument 0.99 == same argument, but "unclear" flag is set
 * in blog para 0.8 == same category and side 0.79 == '' '', unclear 0.6 == same
 * category, different side 0.59 == '' '', unclear 0.4 == different category,
 * same side (also contains unclears, due to bug) X 0.39 == '' '', unclear (not
 * true due to bug in code; this got assigned a 0.4) 0.0 == different category,
 * different side
 * 
 * @TODO: make sure we properly handle exponential notation (ie: 4.051296E-4)
 * @note: much smaller similarity scores for paragraph-level; for many metrics,
 *        it consistently rounds down to 0. problem? Or, just indicates what
 *        metrics we can't use?
 * 
 * @author alexander.p.conrad@gmail.com
 */
public class SimScoreAggregator {

	/*
	 * 
	 * Here is a short description of what the archive contains:
	 * 
	 * a. scores-filtering-raw-avgnorm.txt (here we compared words/tokens in
	 * their initial/raw form and normalizes by the average length of the two
	 * sentences) b. scores-filtering-raw-maxnorm.txt (here we compared
	 * words/tokens in their initial/raw form and normalizes by the maximum
	 * length of the two sentences) c. scores-filtering-lemma-avgnorm.txt (here
	 * we compared words/tokens in their base/lemma form and normalizes by the
	 * average length of the two sentences)
	 * 
	 * In these files, the first three columns are the initial values from
	 * score.txt. The next columns represent various measures of the system. I
	 * briefly describe them below:
	 * 
	 * 1. unweighted unigrams of all words and punctuation 2. unweighted
	 * unigrams of all words and punctuation (with token frequency) 3.
	 * unweighted unigrams of all words 4. unweighted unigrams of all words and
	 * punctuation (with token frequency) 5. unweighted unigrams of content
	 * words only 6. unweighted bigrams of all words and punctuation 7.
	 * unweighted bigrams of all words 8. unweighted bigrams of content words
	 * only 9. entropy-weight unigrams of words 10. idf-weight unigrams of words
	 */

	public static final String SIM_DIRS_ROOT = "/afs/cs.pitt.edu/usr0/conrada/private/Dropbox/data/";
	// public static final String SIM_DIRS_ROOT =
	// "D:\\Users\\conrada\\Dropbox\\data\\";
	public static final String[] SIM_DATASETS = { "illimmigration",
			"altenergy", "deathpenalty", };
	public static final String SIM_DIR_P2 = "_blogs/scores/";
	public static final String[] SIM_FILENAMES_P2 = { "-lemma-avgnorm.txt",
			"-raw-avgnorm.txt", "-raw-maxnorm.txt", };
	public static final String[] SIM_FILENAMES_P2_NORAWAVG = { "-lemma-avgnorm.txt", "-raw-maxnorm.txt", };

	/**
	 * 
	 * Iteratively find the top-sim paragraphs, gradually build up average vals.
	 * Only use IDF scoring with lemmatization, since this performeds best in
	 * previous sim experiment.
	 * 
	 * @param path
	 * @param name
	 */
	public static List<String> readAndAnalyzeSimFileTopMatchesIterative(String path,
			String name, int sim, String metricName, String datasetName) {
		
		System.out.println("\nbeginning iterative sim computation for symType="+sim+"...");

		// list of all valid sim levels
		List<Double> simLevels = new ArrayList<>();
		simLevels.add(1.0);
		simLevels.add(0.99);
		simLevels.add(0.8);
		simLevels.add(0.79);
		simLevels.add(0.6);
		simLevels.add(0.59);
		simLevels.add(0.4);
		simLevels.add(0.39); // won't actually be present, b/c of bug
		simLevels.add(0.0);

		// # of similarity metrics we get per file
		int numScoreConfigs = 10;

		// for each text para, build a SimScoreMatchTuple
		List<SimScoreMatchTuple> scoreMatchTuples = new ArrayList<>();

		// since we have so many classes, handle as hashmap;
		// during document reading just store counts in map, merge
		// as desired after
		// int numPairs = 0;
		// Map<Double, List<Integer>> simLevelToNumPairsMap = new HashMap<>();
		// for (Double simLevel : simLevels) {
		// List<Integer> simLevelNumPairs = new ArrayList<>();
		// for (int i = 0; i < numScoreConfigs; i++) {
		// simLevelNumPairs.add(0);
		// }
		// simLevelToNumPairsMap.put(simLevel, simLevelNumPairs);
		// }
		//
		// // note: all these should be computed ONLY for the top match for each
		// text paragraph!
		//
		// List<Double> scoreSums = new ArrayList<>();
		// for (int i = 0; i < numScoreConfigs; i++) {
		// scoreSums.add(0.);
		// }
		// Map<Double, List<Double>> simLevelToScoreSums = new HashMap<>();
		// for (Double simLevel : simLevels) {
		// List<Double> simLevelScoreSums = new ArrayList<>();
		// for (int i = 0; i < numScoreConfigs; i++) {
		// simLevelScoreSums.add(0.);
		// }
		// simLevelToScoreSums.put(simLevel, simLevelScoreSums);
		// }
		//
		// Map<Double, List<Integer>> simLevelToNumMatches = new HashMap<>();
		// for (Double simLevel : simLevels) {
		// List<Integer> simLevelNumMatches= new ArrayList<>();
		// for (int i = 0; i < numScoreConfigs; i++) {
		// simLevelNumMatches.add(0);
		// }
		// simLevelToNumMatches.put(simLevel, simLevelNumMatches);
		// }
		//
		//
		//
		//
		// // counts for number of text paras that actually have some match
		// List<Integer> numParasWithSomeArgMatchConfigs = new ArrayList<>();
		// List<Integer> numParasWithSomeCatMatchConfigs = new ArrayList<>();
		//
		// // counts for number of text paras who match on highest scores
		// List<Integer> numParasWithBestArgMatchConfigs = new ArrayList<>();
		// List<Integer> numParasWithBestCatMatchConfigs = new ArrayList<>();
		//
		// for (int i=0; i<numScoreConfigs; i++) {
		// numParasWithBestArgMatchConfigs.add(0);
		// numParasWithBestCatMatchConfigs.add(0);
		// numParasWithSomeArgMatchConfigs.add(0);
		// numParasWithSomeCatMatchConfigs.add(0);
		// }
		//
		// for 1-nearest neighbor
		List<Double> bestSent2SimsConfigs = new ArrayList<>();
		List<Double> bestSent2HeuristicsConfigs = new ArrayList<>();
		for (int i = 0; i < numScoreConfigs; i++) {
			bestSent2SimsConfigs.add(0.);
			bestSent2HeuristicsConfigs.add(0.);
		}

		try {

			// read file
			Scanner fileReader = new Scanner(new FileReader(path));

			int lastSent1 = 0;

			boolean hasArgMatch = false;
			boolean hasCatMatch = false;

			while (fileReader.hasNextLine()) {
				String nextLine = fileReader.nextLine();

				try {

					Scanner lineSplitter = new Scanner(nextLine);

					int sent1 = lineSplitter.nextInt();
					int sent2 = lineSplitter.nextInt();
					double heuristicScore = lineSplitter.nextDouble();

					if (heuristicScore == 1.0 || heuristicScore == 0.99) {
						hasArgMatch = true;
						hasCatMatch = true;
					} else if (heuristicScore == 0.8 || heuristicScore == 0.79) {
						hasCatMatch = true;
					}

					double[] sims = new double[numScoreConfigs];
					sims[0] = lineSplitter.nextDouble();
					sims[1] = lineSplitter.nextDouble();
					sims[2] = lineSplitter.nextDouble();
					sims[3] = lineSplitter.nextDouble();
					sims[4] = lineSplitter.nextDouble();
					sims[5] = lineSplitter.nextDouble();
					sims[6] = lineSplitter.nextDouble();
					sims[7] = lineSplitter.nextDouble();
					sims[8] = lineSplitter.nextDouble();
					sims[9] = lineSplitter.nextDouble(); // idf score, only one
															// we care about?

					lineSplitter.close();

					// check whether we're on a new sent1
					if (sent1 != lastSent1 || !fileReader.hasNextLine()) {
						// // store to hashmaps
						// numPairs++;
						// for (int i=0; i<numScoreConfigs; i++) {
						//
						// scoreSums.add(i, scoreSums.remove(i) +
						// bestSent2SimsConfigs.get(i));
						//
						// List<Double> simLevelScoreSums =
						// simLevelToScoreSums.get(bestSent2HeuristicsConfigs.get(i));
						// simLevelScoreSums.add(i, simLevelScoreSums.remove(i)
						// + bestSent2SimsConfigs.get(i));
						//
						// List<Integer> simLevelNumPairs =
						// simLevelToNumPairsMap.get(bestSent2HeuristicsConfigs.get(i));
						// simLevelNumPairs.add(i, simLevelNumPairs.remove(i) +
						// 1);
						//
						// }
						//
						// // store counts
						// if (hasArgMatch) {
						// for (int i=0; i<numScoreConfigs; i++) {
						// numParasWithSomeArgMatchConfigs.add(i,
						// numParasWithSomeArgMatchConfigs.remove(i)+1);
						// numParasWithSomeCatMatchConfigs.add(i,
						// numParasWithSomeCatMatchConfigs.remove(i)+1);
						// double bestConfig =
						// bestSent2HeuristicsConfigs.get(i);
						// if (bestConfig == 1.0 || bestConfig == 0.99) {
						// numParasWithBestArgMatchConfigs.add(i,
						// numParasWithBestArgMatchConfigs.remove(i)+1);
						// numParasWithBestCatMatchConfigs.add(i,
						// numParasWithBestCatMatchConfigs.remove(i)+1);
						// } else if (bestConfig == 0.8 || bestConfig == 0.79) {
						// numParasWithBestCatMatchConfigs.add(i,
						// numParasWithBestCatMatchConfigs.remove(i)+1);
						// }
						// }
						// } else if (hasSimMatch) {
						// for (int i=0; i<numScoreConfigs; i++) {
						// numParasWithSomeCatMatchConfigs.add(i,
						// numParasWithSomeCatMatchConfigs.remove(i)+1);
						// double bestConfig =
						// bestSent2HeuristicsConfigs.get(i);
						// if (bestConfig == 1.0 || bestConfig == 0.99) { //
						// this shouldn't happen; should have thrown hasArgMatch
						// System.out.println("debug: found bestConfig == 1.0 || bestConfig == 0.99 in hasSimMatch");
						// numParasWithBestCatMatchConfigs.add(i,
						// numParasWithBestCatMatchConfigs.remove(i)+1);
						// } else if (bestConfig == 0.8 || bestConfig == 0.79) {
						// numParasWithBestCatMatchConfigs.add(i,
						// numParasWithBestCatMatchConfigs.remove(i)+1);
						// }
						// }
						// }

						// build tuple
						//  we're only considering sim measure #x ( #9 = idf weights)
//						int simMeasureID = 4;
						int simMeasureID = sim;
						boolean matchesArg = false;
						boolean matchesCat = false;
						double bestHeuristic = bestSent2HeuristicsConfigs
								.get(simMeasureID);
						if (bestHeuristic == 1.0 || bestHeuristic == 0.99) {
							matchesArg = true;
							matchesCat = true;
						} else if (bestHeuristic == 0.8
								|| bestHeuristic == 0.79) {
							matchesCat = true;
						}
						SimScoreMatchTuple simScoreTuple = new SimScoreMatchTuple(
								bestSent2SimsConfigs.get(simMeasureID), "", "",
								matchesArg, matchesCat, hasArgMatch,
								hasCatMatch);
						scoreMatchTuples.add(simScoreTuple);

						// reset lists
						for (int i = 0; i < numScoreConfigs; i++) {
							bestSent2SimsConfigs.remove(i);
							bestSent2SimsConfigs.add(i, 0.);
							bestSent2HeuristicsConfigs.remove(i);
							bestSent2HeuristicsConfigs.add(i, 0.);
						}

						hasArgMatch = false;
						hasCatMatch = false;
						lastSent1 = sent1;

					}

					// compare each sim to last max value, replace if greater
					for (int i = 0; i < numScoreConfigs; i++) {
						if (sims[i] > bestSent2SimsConfigs.get(i)) {
							double oldSim = bestSent2SimsConfigs.remove(i);
							double oldHeur = bestSent2HeuristicsConfigs
									.remove(i);
							bestSent2SimsConfigs.add(i, sims[i]);
							bestSent2HeuristicsConfigs.add(i, heuristicScore);
						}
						// @TODO general knn here?
					}

				} catch (NoSuchElementException e) {
					// shouldn't happen unless we have an abnormally-formed
					// line, or a bug in our code
					e.printStackTrace();
					System.out.println("error while parsing line: " + nextLine);
				}

			}

			fileReader.close();

			// sort the tuples by score
			Collections.sort(scoreMatchTuples);

			// iterate over the tuples
			//  compute accuracies at each index; only consider argument-carrying paragraphs for now
			int matchCountArg = 0;
			int matchCountCat = 0;
			int totalCount = 0;
			
			// lines for writing to file
			List<String> lines = new ArrayList<>();
			for (int t = 0; t < scoreMatchTuples.size(); t++) {

				SimScoreMatchTuple scoreMatchTuple = scoreMatchTuples.get(t);
				boolean somethingMatchesArg = scoreMatchTuple.getSomethingMatchesArg();
				boolean somethingMatchesCat = scoreMatchTuple.getSomethingMatchesCat();
				boolean matchesArg = scoreMatchTuple.getMatchesArg();
				boolean matchesCat = scoreMatchTuple.getMatchesCat();
				double simScore = scoreMatchTuple.getSimScore();
				
				if (somethingMatchesArg && matchesArg) {
					matchCountArg++;
					matchCountCat++;
					totalCount++;
				} else if (somethingMatchesArg && matchesCat) {
					matchCountCat++;
					totalCount++;
				} else if (somethingMatchesArg) {
					totalCount++;
				}
				
				if (matchesArg && !somethingMatchesArg) {
					// shouldn't happen; 
					System.out.println("err: matchesArg && !somethingMatchesArg: "+scoreMatchTuple.toString());
				}
				if (matchesArg && !somethingMatchesCat) {
					// shouldn't happen; 
					System.out.println("err: matchesArg && !somethingMatchesCat: "+scoreMatchTuple.toString());
				}
				
//				System.out.println("debug: tuple " + t + ": "
//						+ scoreMatchTuple.toString());
				
				if (somethingMatchesArg) {
					double accArg = ((double)matchCountArg/(double)totalCount);
					double accCat = ((double)matchCountCat/(double)totalCount);
					System.out.println("debug: @ tuple " + t + " (totalCount="+totalCount+"): sim="+simScore+", argAcc="+accArg+", catAcc="+accCat);
					// build line for writing:
					String line = totalCount+","+metricName+","+simScore+","+accArg+","+accCat+"\n";
					lines.add(line);
				}
				
				
			}
			
			// write lines to file
			String header = "totalCount,metricName,simScore,accArg,accCat\n";
			try {

				FileWriter writer = new FileWriter("simPoints_"
						+ datasetName + "_" + metricName + ".csv");
				writer.write(header);
				for (String line : lines) {
					writer.write(line);
				}
				writer.close();

			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("error writing to sim .csv file: "
						+ "simPoints_"
						+ datasetName + "_" + metricName + ".csv");
			}
			
			return lines;

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("error reading file " + path);
		}
		
		return null;

	}

	/**
	 * 
	 * new purpose of method: assess quality of top (or top 3 MV?) matches; for
	 * a text-based paragraph, if there is a same-arg (or same-side), then we
	 * want to know, does the top sim match belong to one of the appropriate
	 * args?
	 * 
	 * 
	 * @param path
	 * @param name
	 */
	public static void readAndAnalyzeSimFileTopMatchOnly(String path,
			String name) {

		// idea: only compute sim scores for the top match for each text
		// paragraph; also, compute accuracy

		// list of all valid sim levels
		List<Double> simLevels = new ArrayList<>();
		simLevels.add(1.0);
		simLevels.add(0.99);
		simLevels.add(0.8);
		simLevels.add(0.79);
		simLevels.add(0.6);
		simLevels.add(0.59);
		simLevels.add(0.4);
		simLevels.add(0.39); // won't actually be present, b/c of bug
		simLevels.add(0.0);

		// # of similarity metrics we get per file
		int numScoreConfigs = 10;

		// since we have so many classes, handle as hashmap;
		// during document reading just store counts in map, merge
		// as desired after
		int numPairs = 0;
		Map<Double, List<Integer>> simLevelToNumPairsMap = new HashMap<>();
		for (Double simLevel : simLevels) {
			List<Integer> simLevelNumPairs = new ArrayList<>();
			for (int i = 0; i < numScoreConfigs; i++) {
				simLevelNumPairs.add(0);
			}
			simLevelToNumPairsMap.put(simLevel, simLevelNumPairs);
		}

		// note: all these should be computed ONLY for the top match for each
		// text paragraph!

		List<Double> scoreSums = new ArrayList<>();
		for (int i = 0; i < numScoreConfigs; i++) {
			scoreSums.add(0.);
		}
		Map<Double, List<Double>> simLevelToScoreSums = new HashMap<>();
		for (Double simLevel : simLevels) {
			List<Double> simLevelScoreSums = new ArrayList<>();
			for (int i = 0; i < numScoreConfigs; i++) {
				simLevelScoreSums.add(0.);
			}
			simLevelToScoreSums.put(simLevel, simLevelScoreSums);
		}

		Map<Double, List<Integer>> simLevelToNumMatches = new HashMap<>();
		for (Double simLevel : simLevels) {
			List<Integer> simLevelNumMatches = new ArrayList<>();
			for (int i = 0; i < numScoreConfigs; i++) {
				simLevelNumMatches.add(0);
			}
			simLevelToNumMatches.put(simLevel, simLevelNumMatches);
		}

		// counts for number of text paras that actually have some match
		List<Integer> numParasWithSomeArgMatchConfigs = new ArrayList<>();
		List<Integer> numParasWithSomeCatMatchConfigs = new ArrayList<>();

		// counts for number of text paras who match on highest scores
		List<Integer> numParasWithBestArgMatchConfigs = new ArrayList<>();
		List<Integer> numParasWithBestCatMatchConfigs = new ArrayList<>();

		for (int i = 0; i < numScoreConfigs; i++) {
			numParasWithBestArgMatchConfigs.add(0);
			numParasWithBestCatMatchConfigs.add(0);
			numParasWithSomeArgMatchConfigs.add(0);
			numParasWithSomeCatMatchConfigs.add(0);
		}

		try {

			// read file
			Scanner fileReader = new Scanner(new FileReader(path));

			// for 1-nearest neighbor
			int lastSent1 = 0;
			List<Double> bestSent2SimsConfigs = new ArrayList<>();
			List<Double> bestSent2HeuristicsConfigs = new ArrayList<>();
			for (int i = 0; i < numScoreConfigs; i++) {
				bestSent2SimsConfigs.add(0.);
				bestSent2HeuristicsConfigs.add(0.);
			}

			// quick and dirty support for 3-nearest-neighbor
			// (DON'T reuse this code! it's too messy!!!)

			List<Double> bestSent2SimsConfigs2nn = new ArrayList<>();
			List<Double> bestSent2HeuristicsConfigs2nn = new ArrayList<>();
			for (int i = 0; i < numScoreConfigs; i++) {
				bestSent2SimsConfigs2nn.add(0.);
				bestSent2HeuristicsConfigs2nn.add(0.);
			}

			List<Double> bestSent2SimsConfigs3nn = new ArrayList<>();
			List<Double> bestSent2HeuristicsConfigs3nn = new ArrayList<>();
			for (int i = 0; i < numScoreConfigs; i++) {
				bestSent2SimsConfigs3nn.add(0.);
				bestSent2HeuristicsConfigs3nn.add(0.);
			}

			boolean hasArgMatch = false;
			boolean hasSimMatch = false;

			while (fileReader.hasNextLine()) {
				String nextLine = fileReader.nextLine();

				try {

					Scanner lineSplitter = new Scanner(nextLine);

					int sent1 = lineSplitter.nextInt();
					int sent2 = lineSplitter.nextInt();
					double heuristicScore = lineSplitter.nextDouble();

					if (heuristicScore == 1.0 || heuristicScore == 0.99) {
						hasArgMatch = true;
						hasSimMatch = true;
					} else if (heuristicScore == 0.8 || heuristicScore == 0.79) {
						hasSimMatch = true;
					}

					double[] sims = new double[numScoreConfigs];
					sims[0] = lineSplitter.nextDouble();
					sims[1] = lineSplitter.nextDouble();
					sims[2] = lineSplitter.nextDouble();
					sims[3] = lineSplitter.nextDouble();
					sims[4] = lineSplitter.nextDouble();
					sims[5] = lineSplitter.nextDouble();
					sims[6] = lineSplitter.nextDouble();
					sims[7] = lineSplitter.nextDouble();
					sims[8] = lineSplitter.nextDouble();
					sims[9] = lineSplitter.nextDouble();

					lineSplitter.close();

					// check whether we're on a new sent1
					if (sent1 != lastSent1 || !fileReader.hasNextLine()) {
						// store to hashmaps
						numPairs++;
						lastSent1 = sent1;
						for (int i = 0; i < numScoreConfigs; i++) {

							scoreSums.add(i, scoreSums.remove(i)
									+ bestSent2SimsConfigs.get(i));

							List<Double> simLevelScoreSums = simLevelToScoreSums
									.get(bestSent2HeuristicsConfigs.get(i));
							simLevelScoreSums.add(i,
									simLevelScoreSums.remove(i)
											+ bestSent2SimsConfigs.get(i));

							List<Integer> simLevelNumPairs = simLevelToNumPairsMap
									.get(bestSent2HeuristicsConfigs.get(i));
							simLevelNumPairs.add(i,
									simLevelNumPairs.remove(i) + 1);

						}

						// store counts
						if (hasArgMatch) {
							for (int i = 0; i < numScoreConfigs; i++) {
								numParasWithSomeArgMatchConfigs.add(i,
										numParasWithSomeArgMatchConfigs
												.remove(i) + 1);
								numParasWithSomeCatMatchConfigs.add(i,
										numParasWithSomeCatMatchConfigs
												.remove(i) + 1);
								double bestConfig = bestSent2HeuristicsConfigs
										.get(i);
								if (bestConfig == 1.0 || bestConfig == 0.99) {
									numParasWithBestArgMatchConfigs.add(i,
											numParasWithBestArgMatchConfigs
													.remove(i) + 1);
									numParasWithBestCatMatchConfigs.add(i,
											numParasWithBestCatMatchConfigs
													.remove(i) + 1);
								} else if (bestConfig == 0.8
										|| bestConfig == 0.79) {
									numParasWithBestCatMatchConfigs.add(i,
											numParasWithBestCatMatchConfigs
													.remove(i) + 1);
								}
							}
						} else if (hasSimMatch) {
							for (int i = 0; i < numScoreConfigs; i++) {
								numParasWithSomeCatMatchConfigs.add(i,
										numParasWithSomeCatMatchConfigs
												.remove(i) + 1);
								double bestConfig = bestSent2HeuristicsConfigs
										.get(i);
								if (bestConfig == 1.0 || bestConfig == 0.99) { // this
																				// shouldn't
																				// happen;
																				// should
																				// have
																				// thrown
																				// hasArgMatch
									System.out
											.println("debug: found bestConfig == 1.0 || bestConfig == 0.99 in hasSimMatch");
									numParasWithBestCatMatchConfigs.add(i,
											numParasWithBestCatMatchConfigs
													.remove(i) + 1);
								} else if (bestConfig == 0.8
										|| bestConfig == 0.79) {
									numParasWithBestCatMatchConfigs.add(i,
											numParasWithBestCatMatchConfigs
													.remove(i) + 1);
								}
							}
						}

						// reset lists
						for (int i = 0; i < numScoreConfigs; i++) {
							bestSent2SimsConfigs.remove(i);
							bestSent2SimsConfigs.add(i, 0.);
							bestSent2HeuristicsConfigs.remove(i);
							bestSent2HeuristicsConfigs.add(i, 0.);
						}

						hasArgMatch = false;
						hasSimMatch = false;

					}

					// compare each sim to last max value, replace if greater
					for (int i = 0; i < numScoreConfigs; i++) {
						if (sims[i] > bestSent2SimsConfigs.get(i)) {
							double oldSim = bestSent2SimsConfigs.remove(i);
							double oldHeur = bestSent2HeuristicsConfigs
									.remove(i);
							bestSent2SimsConfigs.add(i, sims[i]);
							bestSent2HeuristicsConfigs.add(i, heuristicScore);
						}
						// @TODO general knn here?
					}

				} catch (NoSuchElementException e) {
					// shouldn't happen unless we have an abnormally-formed
					// line, or a bug in our code
					e.printStackTrace();
					System.out.println("error while parsing line: " + nextLine);
				}

			}

			fileReader.close();

			// perform category merging, display
			// (could perform outside of try, but if read failed, no sense to
			// analyze)

			String type = "\n\n\n\nsummmary (total = " + numPairs + ")\n";
			// type += name + ":scoreUnweightUniWordsPunct"; // add totals
			// System.out.println(type + "\t" + (scoreSums.get(0) / numPairs));
			// type = name + ":scoreUnweightUniWordsPunctFreq";
			// System.out.println(type + "\t" + (scoreSums.get(1) / numPairs));
			// type = name + ":scoreUnweightUniWords";
			// System.out.println(type + "\t" + (scoreSums.get(2) / numPairs));
			// type = name + ":scoreUnweightUniWordsFreq";
			// System.out.println(type + "\t" + (scoreSums.get(3) / numPairs));
			// type = name + ":scoreUnweightUniContent";
			// System.out.println(type + "\t" + (scoreSums.get(4) / numPairs));
			// type = name + ":scoreUnweightBiWordsPunct";
			// System.out.println(type + "\t" + (scoreSums.get(5) / numPairs));
			// type = name + ":scoreUnweightBiWords";
			// System.out.println(type + "\t" + (scoreSums.get(6) / numPairs));
			// type = name + ":scoreUnweightBiContent";
			// System.out.println(type + "\t" + (scoreSums.get(7) / numPairs));
			// type = name + ":scoreEntropyWeightUniWords";
			// System.out.println(type + "\t" + (scoreSums.get(8) / numPairs));
			// type = name + ":scoreIdfWeightUniWords";
			// System.out.println(type + "\t" + (scoreSums.get(9) / numPairs));

			// print info for additional groups

			// for text paras with argument, what % had highest sim with a
			// correct argument para?
			List<Double> fracArgumentsWithTopSimSameArg = new ArrayList<>();
			List<Double> fracArgumentsWithTopSimSameCat = new ArrayList<>();
			for (int i = 0; i < numScoreConfigs; i++) {
				fracArgumentsWithTopSimSameArg
						.add((double) numParasWithBestArgMatchConfigs.get(i)
								/ (double) numParasWithSomeArgMatchConfigs
										.get(i));
				fracArgumentsWithTopSimSameCat
						.add((double) numParasWithBestCatMatchConfigs.get(i)
								/ (double) numParasWithSomeCatMatchConfigs
										.get(i));
			}

			// for text paras with category (and thus argument), what % had
			// highest sim with a correct category para?

			// group: same argument (key = 1.0 OR 0.99)
			List<Double> scoreSumsSameArgument = new ArrayList<>();
			List<Integer> numPairsSameArgument = new ArrayList<>();
			for (int i = 0; i < numScoreConfigs; i++) {
				scoreSumsSameArgument.add(i, simLevelToScoreSums.get(1.0)
						.get(i) + simLevelToScoreSums.get(0.99).get(i));
				numPairsSameArgument.add(i,
						simLevelToNumPairsMap.get(1.0).get(i)
								+ simLevelToNumPairsMap.get(0.99).get(i));
			}

			type = "\n\nnum some argument: "
					+ numParasWithSomeArgMatchConfigs.get(0) + "\n";
			type += name + ":scoreUnweightUniWordsPunct"; // add totals, perc
															// improvements over
															// all
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameArg.get(0)));
			type = name + ":scoreUnweightUniWordsPunctFreq";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameArg.get(1)));
			type = name + ":scoreUnweightUniWords";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameArg.get(2)));
			type = name + ":scoreUnweightUniWordsFreq";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameArg.get(3)));
			type = name + ":scoreUnweightUniContent";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameArg.get(4)));
			type = name + ":scoreUnweightBiWordsPunct";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameArg.get(5)));
			type = name + ":scoreUnweightBiWords";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameArg.get(6)));
			type = name + ":scoreUnweightBiContent";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameArg.get(7)));
			type = name + ":scoreEntropyWeightUniWords";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameArg.get(8)));
			type = name + ":scoreIdfWeightUniWords";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameArg.get(9)));

			type = "\n\nnum some cat: "
					+ numParasWithSomeCatMatchConfigs.get(0) + "\n";
			type += name + ":scoreUnweightUniWordsPunct"; // add totals, perc
															// improvements over
															// all
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameCat.get(0)));
			type = name + ":scoreUnweightUniWordsPunctFreq";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameCat.get(1)));
			type = name + ":scoreUnweightUniWords";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameCat.get(2)));
			type = name + ":scoreUnweightUniWordsFreq";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameCat.get(3)));
			type = name + ":scoreUnweightUniContent";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameCat.get(4)));
			type = name + ":scoreUnweightBiWordsPunct";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameCat.get(5)));
			type = name + ":scoreUnweightBiWords";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameCat.get(6)));
			type = name + ":scoreUnweightBiContent";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameCat.get(7)));
			type = name + ":scoreEntropyWeightUniWords";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameCat.get(8)));
			type = name + ":scoreIdfWeightUniWords";
			System.out.println(type + "\t"
					+ (fracArgumentsWithTopSimSameCat.get(9)));

			// type = "\n\nsame argument\n";
			// type += name + ":scoreUnweightUniWordsPunct"; // add totals, perc
			// improvements over all
			// System.out.println(type + "\t" + (scoreSumsSameArgument.get(0) /
			// numPairs) + " (fraction: "+ (100* (scoreSumsSameArgument.get(0) /
			// numPairs) / (scoreSums.get(0) / numPairs))
			// +"%) (accuracy: "+(numPairsSameArgument.get(0) /
			// (double)numPairs)+")");
			// type = name + ":scoreUnweightUniWordsPunctFreq";
			// System.out.println(type + "\t" + (scoreSumsSameArgument.get(1) /
			// numPairs) + " (fraction: "+ (100* (scoreSumsSameArgument.get(1) /
			// numPairs) / (scoreSums.get(1) / numPairs))
			// +"%) (accuracy: "+(numPairsSameArgument.get(0) /
			// (double)numPairs)+")");
			// type = name + ":scoreUnweightUniWords";
			// System.out.println(type + "\t" + (scoreSumsSameArgument.get(2) /
			// numPairs) + " (fraction: "+ (100* (scoreSumsSameArgument.get(2) /
			// numPairs) / (scoreSums.get(2) / numPairs))
			// +"%) (accuracy: "+(numPairsSameArgument.get(0) /
			// (double)numPairs)+")");
			// type = name + ":scoreUnweightUniWordsFreq";
			// System.out.println(type + "\t" + (scoreSumsSameArgument.get(3) /
			// numPairs) + " (fraction: "+ (100* (scoreSumsSameArgument.get(3) /
			// numPairs) / (scoreSums.get(3) / numPairs))
			// +"%) (accuracy: "+(numPairsSameArgument.get(0) /
			// (double)numPairs)+")");
			// type = name + ":scoreUnweightUniContent";
			// System.out.println(type + "\t" + (scoreSumsSameArgument.get(4) /
			// numPairs) + " (fraction: "+ (100* (scoreSumsSameArgument.get(4) /
			// numPairs) / (scoreSums.get(4) / numPairs))
			// +"%) (accuracy: "+(numPairsSameArgument.get(0) /
			// (double)numPairs)+")");
			// type = name + ":scoreUnweightBiWordsPunct";
			// System.out.println(type + "\t" + (scoreSumsSameArgument.get(5) /
			// numPairs) + " (fraction: "+ (100* (scoreSumsSameArgument.get(5) /
			// numPairs) / (scoreSums.get(5) / numPairs))
			// +"%) (accuracy: "+(numPairsSameArgument.get(0) /
			// (double)numPairs)+")");
			// type = name + ":scoreUnweightBiWords";
			// System.out.println(type + "\t" + (scoreSumsSameArgument.get(6) /
			// numPairs) + " (fraction: "+ (100* (scoreSumsSameArgument.get(6) /
			// numPairs) / (scoreSums.get(6) / numPairs))
			// +"%) (accuracy: "+(numPairsSameArgument.get(0) /
			// (double)numPairs)+")");
			// type = name + ":scoreUnweightBiContent";
			// System.out.println(type + "\t" + (scoreSumsSameArgument.get(7) /
			// numPairs) + " (fraction: "+ (100* (scoreSumsSameArgument.get(7) /
			// numPairs) / (scoreSums.get(7) / numPairs))
			// +"%) (accuracy: "+(numPairsSameArgument.get(0) /
			// (double)numPairs)+")");
			// type = name + ":scoreEntropyWeightUniWords";
			// System.out.println(type + "\t" + (scoreSumsSameArgument.get(8) /
			// numPairs) + " (fraction: "+ (100* (scoreSumsSameArgument.get(8) /
			// numPairs) / (scoreSums.get(8) / numPairs))
			// +"%) (accuracy: "+(numPairsSameArgument.get(0) /
			// (double)numPairs)+")");
			// type = name + ":scoreIdfWeightUniWords";
			// System.out.println(type + "\t" + (scoreSumsSameArgument.get(9) /
			// numPairs) + " (fraction: "+ (100* (scoreSumsSameArgument.get(9) /
			// numPairs) / (scoreSums.get(9) / numPairs))
			// +"%) (accuracy: "+(numPairsSameArgument.get(0) /
			// (double)numPairs)+")");

			// group: same category & side (key = 1.0 OR 0.99 OR 0.8 OR 0.79)
			List<Double> scoreSumsSameCatSameSide = new ArrayList<>();
			List<Integer> numPairsSameCatSameSide = new ArrayList<>();
			for (int i = 0; i < numScoreConfigs; i++) {
				scoreSumsSameCatSameSide.add(simLevelToScoreSums.get(1.0)
						.get(i)
						+ simLevelToScoreSums.get(0.99).get(i)
						+ simLevelToScoreSums.get(0.8).get(i)
						+ simLevelToScoreSums.get(0.79).get(i));
				numPairsSameCatSameSide.add(i, simLevelToNumPairsMap.get(1.0)
						.get(i)
						+ simLevelToNumPairsMap.get(0.99).get(i)
						+ simLevelToNumPairsMap.get(0.8).get(i)
						+ simLevelToNumPairsMap.get(0.79).get(i));
			}

			// type = "\n\nsame category and side\n";
			// type += name + ":scoreUnweightUniWordsPunct";
			// System.out.println(type + "\t" + (scoreSumsSameCatSameSide.get(0)
			// / numPairs) + " (fraction: "+ (100*
			// (scoreSumsSameCatSameSide.get(0) / numPairs) / (scoreSums.get(0)
			// / numPairs)) +"%) (accuracy: "+(numPairsSameCatSameSide.get(0) /
			// (double)numPairs)+")");
			// type = name + ":scoreUnweightUniWordsPunctFreq";
			// System.out.println(type + "\t" + (scoreSumsSameCatSameSide.get(1)
			// / numPairs) + " (fraction: "+ (100*
			// (scoreSumsSameCatSameSide.get(1) / numPairs) / (scoreSums.get(1)
			// / numPairs)) +"%) (accuracy: "+(numPairsSameCatSameSide.get(1) /
			// (double)numPairs)+")");
			// type = name + ":scoreUnweightUniWords";
			// System.out.println(type + "\t" + (scoreSumsSameCatSameSide.get(2)
			// / numPairs) + " (fraction: "+ (100*
			// (scoreSumsSameCatSameSide.get(2) / numPairs) / (scoreSums.get(2)
			// / numPairs)) +"%) (accuracy: "+(numPairsSameCatSameSide.get(2) /
			// (double)numPairs)+")");
			// type = name + ":scoreUnweightUniWordsFreq";
			// System.out.println(type + "\t" + (scoreSumsSameCatSameSide.get(3)
			// / numPairs) + " (fraction: "+ (100*
			// (scoreSumsSameCatSameSide.get(3) / numPairs) / (scoreSums.get(3)
			// / numPairs)) +"%) (accuracy: "+(numPairsSameCatSameSide.get(3) /
			// (double)numPairs)+")");
			// type = name + ":scoreUnweightUniContent";
			// System.out.println(type + "\t" + (scoreSumsSameCatSameSide.get(4)
			// / numPairs) + " (fraction: "+ (100*
			// (scoreSumsSameCatSameSide.get(4) / numPairs) / (scoreSums.get(4)
			// / numPairs)) +"%) (accuracy: "+(numPairsSameCatSameSide.get(4) /
			// (double)numPairs)+")");
			// type = name + ":scoreUnweightBiWordsPunct";
			// System.out.println(type + "\t" + (scoreSumsSameCatSameSide.get(5)
			// / numPairs) + " (fraction: "+ (100*
			// (scoreSumsSameCatSameSide.get(5) / numPairs) / (scoreSums.get(5)
			// / numPairs)) +"%) (accuracy: "+(numPairsSameCatSameSide.get(5) /
			// (double)numPairs)+")");
			// type = name + ":scoreUnweightBiWords";
			// System.out.println(type + "\t" + (scoreSumsSameCatSameSide.get(6)
			// / numPairs) + " (fraction: "+ (100*
			// (scoreSumsSameCatSameSide.get(6) / numPairs) / (scoreSums.get(6)
			// / numPairs)) +"%) (accuracy: "+(numPairsSameCatSameSide.get(6) /
			// (double)numPairs)+")");
			// type = name + ":scoreUnweightBiContent";
			// System.out.println(type + "\t" + (scoreSumsSameCatSameSide.get(7)
			// / numPairs) + " (fraction: "+ (100*
			// (scoreSumsSameCatSameSide.get(7) / numPairs) / (scoreSums.get(7)
			// / numPairs)) +"%) (accuracy: "+(numPairsSameCatSameSide.get(7) /
			// (double)numPairs)+")");
			// type = name + ":scoreEntropyWeightUniWords";
			// System.out.println(type + "\t" + (scoreSumsSameCatSameSide.get(8)
			// / numPairs) + " (fraction: "+ (100*
			// (scoreSumsSameCatSameSide.get(8) / numPairs) / (scoreSums.get(8)
			// / numPairs)) +"%) (accuracy: "+(numPairsSameCatSameSide.get(8) /
			// (double)numPairs)+")");
			// type = name + ":scoreIdfWeightUniWords";
			// System.out.println(type + "\t" + (scoreSumsSameCatSameSide.get(9)
			// / numPairs) + " (fraction: "+ (100*
			// (scoreSumsSameCatSameSide.get(9) / numPairs) / (scoreSums.get(9)
			// / numPairs)) +"%) (accuracy: "+(numPairsSameCatSameSide.get(9) /
			// (double)numPairs)+")");

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("error reading file " + path);
		}

	}

	public static void readAndAnalyzeSimFile(String path, String name) {

		// list of all valid sim levels
		List<Double> simLevels = new ArrayList<>();
		simLevels.add(1.0);
		simLevels.add(0.99);
		simLevels.add(0.8);
		simLevels.add(0.79);
		simLevels.add(0.6);
		simLevels.add(0.59);
		simLevels.add(0.4);
		simLevels.add(0.39); // won't actually be present, b/c of bug
		simLevels.add(0.0);

		// since we have so many classes, handle as hashmap;
		// during document reading just store counts in map, merge
		// as desired after
		int numPairs = 0;
		Map<Double, Integer> simLevelToNumPairsMap = new HashMap<>();
		for (Double simLevel : simLevels) {
			simLevelToNumPairsMap.put(simLevel, 0);
		}

		// # of similarity metrics we get per file
		int numScoreConfigs = 10;

		List<Double> scoreSums = new ArrayList<>();
		for (int i = 0; i < numScoreConfigs; i++) {
			scoreSums.add(0.);
		}
		Map<Double, List<Double>> simLevelToScoreSums = new HashMap<>();
		for (Double simLevel : simLevels) {
			List<Double> simLevelScoreSums = new ArrayList<>();
			for (int i = 0; i < numScoreConfigs; i++) {
				simLevelScoreSums.add(0.);
			}
			simLevelToScoreSums.put(simLevel, simLevelScoreSums);
		}

		try {

			Scanner fileReader = new Scanner(new FileReader(path));

			// read file
			while (fileReader.hasNextLine()) {
				String nextLine = fileReader.nextLine();

				try {

					Scanner lineSplitter = new Scanner(nextLine);

					int sent1 = lineSplitter.nextInt();
					int sent2 = lineSplitter.nextInt();
					double heuristicScore = lineSplitter.nextDouble();
					double scoreUnweightUniWordsPunct = lineSplitter
							.nextDouble();
					double scoreUnweightUniWordsPunctFreq = lineSplitter
							.nextDouble();
					double scoreUnweightUniWords = lineSplitter.nextDouble();
					double scoreUnweightUniWordsFreq = lineSplitter
							.nextDouble();
					double scoreUnweightUniContent = lineSplitter.nextDouble();
					double scoreUnweightBiWordsPunct = lineSplitter
							.nextDouble();
					double scoreUnweightBiWords = lineSplitter.nextDouble();
					double scoreUnweightBiContent = lineSplitter.nextDouble();
					double scoreEntropyWeightUniWords = lineSplitter
							.nextDouble();
					double scoreIdfWeightUniWords = lineSplitter.nextDouble();

					lineSplitter.close();

					// use heuristicScore as key into hashmaps
					// do cat-specific computations
					simLevelToNumPairsMap.put(heuristicScore,
							simLevelToNumPairsMap.get(heuristicScore) + 1);

					List<Double> simLevelScoreSums = simLevelToScoreSums
							.get(heuristicScore);
					simLevelScoreSums.add(0, simLevelScoreSums.remove(0)
							+ scoreUnweightUniWordsPunct);
					simLevelScoreSums.add(1, simLevelScoreSums.remove(1)
							+ scoreUnweightUniWordsPunctFreq);
					simLevelScoreSums.add(2, simLevelScoreSums.remove(2)
							+ scoreUnweightUniWords);
					simLevelScoreSums.add(3, simLevelScoreSums.remove(3)
							+ scoreUnweightUniWordsFreq);
					simLevelScoreSums.add(4, simLevelScoreSums.remove(4)
							+ scoreUnweightUniContent);
					simLevelScoreSums.add(5, simLevelScoreSums.remove(5)
							+ scoreUnweightBiWordsPunct);
					simLevelScoreSums.add(6, simLevelScoreSums.remove(6)
							+ scoreUnweightBiWords);
					simLevelScoreSums.add(7, simLevelScoreSums.remove(7)
							+ scoreUnweightBiContent);
					simLevelScoreSums.add(8, simLevelScoreSums.remove(8)
							+ scoreEntropyWeightUniWords);
					simLevelScoreSums.add(9, simLevelScoreSums.remove(9)
							+ scoreIdfWeightUniWords);

					// do non-cat-specific computations
					numPairs++;
					scoreSums.add(0, scoreSums.remove(0)
							+ scoreUnweightUniWordsPunct);
					scoreSums.add(1, scoreSums.remove(1)
							+ scoreUnweightUniWordsPunctFreq);
					scoreSums.add(2, scoreSums.remove(2)
							+ scoreUnweightUniWords);
					scoreSums.add(3, scoreSums.remove(3)
							+ scoreUnweightUniWordsFreq);
					scoreSums.add(4, scoreSums.remove(4)
							+ scoreUnweightUniContent);
					scoreSums.add(5, scoreSums.remove(5)
							+ scoreUnweightBiWordsPunct);
					scoreSums
							.add(6, scoreSums.remove(6) + scoreUnweightBiWords);
					scoreSums.add(7, scoreSums.remove(7)
							+ scoreUnweightBiContent);
					scoreSums.add(8, scoreSums.remove(8)
							+ scoreEntropyWeightUniWords);
					scoreSums.add(9, scoreSums.remove(9)
							+ scoreIdfWeightUniWords);

				} catch (NoSuchElementException e) {
					// shouldn't happen unless we have an abnormally-formed
					// line, or a bug in our code
					e.printStackTrace();
					System.out.println("error while parsing line: " + nextLine);
				}

			}

			fileReader.close();

			// perform category merging, display
			// (could perform outside of try, but if read failed, no sense to
			// analyze)

			String type = "\n\n\n\nsummmary (total = " + numPairs + ")\n";
			type += name + ":scoreUnweightUniWordsPunct"; // add totals
			System.out.println(type + "\t" + (scoreSums.get(0) / numPairs));
			type = name + ":scoreUnweightUniWordsPunctFreq";
			System.out.println(type + "\t" + (scoreSums.get(1) / numPairs));
			type = name + ":scoreUnweightUniWords";
			System.out.println(type + "\t" + (scoreSums.get(2) / numPairs));
			type = name + ":scoreUnweightUniWordsFreq";
			System.out.println(type + "\t" + (scoreSums.get(3) / numPairs));
			type = name + ":scoreUnweightUniContent";
			System.out.println(type + "\t" + (scoreSums.get(4) / numPairs));
			type = name + ":scoreUnweightBiWordsPunct";
			System.out.println(type + "\t" + (scoreSums.get(5) / numPairs));
			type = name + ":scoreUnweightBiWords";
			System.out.println(type + "\t" + (scoreSums.get(6) / numPairs));
			type = name + ":scoreUnweightBiContent";
			System.out.println(type + "\t" + (scoreSums.get(7) / numPairs));
			type = name + ":scoreEntropyWeightUniWords";
			System.out.println(type + "\t" + (scoreSums.get(8) / numPairs));
			type = name + ":scoreIdfWeightUniWords";
			System.out.println(type + "\t" + (scoreSums.get(9) / numPairs));

			// print info for additional groups

			// group: same argument (key = 1.0 OR 0.99)
			int numPairsSameArgument = simLevelToNumPairsMap.get(1.0)
					+ simLevelToNumPairsMap.get(0.99);
			List<Double> scoreSumsSameArgument = new ArrayList<>();
			// for (int i=0; i<numScoreConfigs; i++) {
			scoreSumsSameArgument.add(simLevelToScoreSums.get(1.0).get(0)
					+ simLevelToScoreSums.get(0.99).get(0));
			scoreSumsSameArgument.add(simLevelToScoreSums.get(1.0).get(1)
					+ simLevelToScoreSums.get(0.99).get(1));
			scoreSumsSameArgument.add(simLevelToScoreSums.get(1.0).get(2)
					+ simLevelToScoreSums.get(0.99).get(2));
			scoreSumsSameArgument.add(simLevelToScoreSums.get(1.0).get(3)
					+ simLevelToScoreSums.get(0.99).get(3));
			scoreSumsSameArgument.add(simLevelToScoreSums.get(1.0).get(4)
					+ simLevelToScoreSums.get(0.99).get(4));
			scoreSumsSameArgument.add(simLevelToScoreSums.get(1.0).get(5)
					+ simLevelToScoreSums.get(0.99).get(5));
			scoreSumsSameArgument.add(simLevelToScoreSums.get(1.0).get(6)
					+ simLevelToScoreSums.get(0.99).get(6));
			scoreSumsSameArgument.add(simLevelToScoreSums.get(1.0).get(7)
					+ simLevelToScoreSums.get(0.99).get(7));
			scoreSumsSameArgument.add(simLevelToScoreSums.get(1.0).get(8)
					+ simLevelToScoreSums.get(0.99).get(8));
			scoreSumsSameArgument.add(simLevelToScoreSums.get(1.0).get(9)
					+ simLevelToScoreSums.get(0.99).get(9));
			// }

			type = "\n\nsame argument (total = " + numPairsSameArgument + ")\n";
			type += name + ":scoreUnweightUniWordsPunct"; // add totals, perc
															// improvements over
															// all
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameArgument.get(0) / numPairsSameArgument)
							+ " (fraction: "
							+ (100 * (scoreSumsSameArgument.get(0) / numPairsSameArgument) / (scoreSums
									.get(0) / numPairs)) + "%)");
			type = name + ":scoreUnweightUniWordsPunctFreq";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameArgument.get(1) / numPairsSameArgument)
							+ " (fraction: "
							+ (100 * (scoreSumsSameArgument.get(1) / numPairsSameArgument) / (scoreSums
									.get(1) / numPairs)) + "%)");
			type = name + ":scoreUnweightUniWords";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameArgument.get(2) / numPairsSameArgument)
							+ " (fraction: "
							+ (100 * (scoreSumsSameArgument.get(2) / numPairsSameArgument) / (scoreSums
									.get(2) / numPairs)) + "%)");
			type = name + ":scoreUnweightUniWordsFreq";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameArgument.get(3) / numPairsSameArgument)
							+ " (fraction: "
							+ (100 * (scoreSumsSameArgument.get(3) / numPairsSameArgument) / (scoreSums
									.get(3) / numPairs)) + "%)");
			type = name + ":scoreUnweightUniContent";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameArgument.get(4) / numPairsSameArgument)
							+ " (fraction: "
							+ (100 * (scoreSumsSameArgument.get(4) / numPairsSameArgument) / (scoreSums
									.get(4) / numPairs)) + "%)");
			type = name + ":scoreUnweightBiWordsPunct";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameArgument.get(5) / numPairsSameArgument)
							+ " (fraction: "
							+ (100 * (scoreSumsSameArgument.get(5) / numPairsSameArgument) / (scoreSums
									.get(5) / numPairs)) + "%)");
			type = name + ":scoreUnweightBiWords";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameArgument.get(6) / numPairsSameArgument)
							+ " (fraction: "
							+ (100 * (scoreSumsSameArgument.get(6) / numPairsSameArgument) / (scoreSums
									.get(6) / numPairs)) + "%)");
			type = name + ":scoreUnweightBiContent";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameArgument.get(7) / numPairsSameArgument)
							+ " (fraction: "
							+ (100 * (scoreSumsSameArgument.get(7) / numPairsSameArgument) / (scoreSums
									.get(7) / numPairs)) + "%)");
			type = name + ":scoreEntropyWeightUniWords";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameArgument.get(8) / numPairsSameArgument)
							+ " (fraction: "
							+ (100 * (scoreSumsSameArgument.get(8) / numPairsSameArgument) / (scoreSums
									.get(8) / numPairs)) + "%)");
			type = name + ":scoreIdfWeightUniWords";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameArgument.get(9) / numPairsSameArgument)
							+ " (fraction: "
							+ (100 * (scoreSumsSameArgument.get(9) / numPairsSameArgument) / (scoreSums
									.get(9) / numPairs)) + "%)");

			// group: same category & side (key = 1.0 OR 0.99 OR 0.8 OR 0.79)
			int numPairsSameCatSameSide = simLevelToNumPairsMap.get(1.0)
					+ simLevelToNumPairsMap.get(0.99)
					+ simLevelToNumPairsMap.get(0.8)
					+ simLevelToNumPairsMap.get(0.79);
			List<Double> scoreSumsSameCatSameSide = new ArrayList<>();
			// for (int i=0; i<numScoreConfigs; i++) {
			scoreSumsSameCatSameSide.add(simLevelToScoreSums.get(1.0).get(0)
					+ simLevelToScoreSums.get(0.99).get(0)
					+ simLevelToScoreSums.get(0.8).get(0)
					+ simLevelToScoreSums.get(0.79).get(0));
			scoreSumsSameCatSameSide.add(simLevelToScoreSums.get(1.0).get(1)
					+ simLevelToScoreSums.get(0.99).get(1)
					+ simLevelToScoreSums.get(0.8).get(1)
					+ simLevelToScoreSums.get(0.79).get(1));
			scoreSumsSameCatSameSide.add(simLevelToScoreSums.get(1.0).get(2)
					+ simLevelToScoreSums.get(0.99).get(2)
					+ simLevelToScoreSums.get(0.8).get(2)
					+ simLevelToScoreSums.get(0.79).get(2));
			scoreSumsSameCatSameSide.add(simLevelToScoreSums.get(1.0).get(3)
					+ simLevelToScoreSums.get(0.99).get(3)
					+ simLevelToScoreSums.get(0.8).get(3)
					+ simLevelToScoreSums.get(0.79).get(3));
			scoreSumsSameCatSameSide.add(simLevelToScoreSums.get(1.0).get(4)
					+ simLevelToScoreSums.get(0.99).get(4)
					+ simLevelToScoreSums.get(0.8).get(4)
					+ simLevelToScoreSums.get(0.79).get(4));
			scoreSumsSameCatSameSide.add(simLevelToScoreSums.get(1.0).get(5)
					+ simLevelToScoreSums.get(0.99).get(5)
					+ simLevelToScoreSums.get(0.8).get(5)
					+ simLevelToScoreSums.get(0.79).get(5));
			scoreSumsSameCatSameSide.add(simLevelToScoreSums.get(1.0).get(6)
					+ simLevelToScoreSums.get(0.99).get(6)
					+ simLevelToScoreSums.get(0.8).get(6)
					+ simLevelToScoreSums.get(0.79).get(6));
			scoreSumsSameCatSameSide.add(simLevelToScoreSums.get(1.0).get(7)
					+ simLevelToScoreSums.get(0.99).get(7)
					+ simLevelToScoreSums.get(0.8).get(7)
					+ simLevelToScoreSums.get(0.79).get(7));
			scoreSumsSameCatSameSide.add(simLevelToScoreSums.get(1.0).get(8)
					+ simLevelToScoreSums.get(0.99).get(8)
					+ simLevelToScoreSums.get(0.8).get(8)
					+ simLevelToScoreSums.get(0.79).get(8));
			scoreSumsSameCatSameSide.add(simLevelToScoreSums.get(1.0).get(9)
					+ simLevelToScoreSums.get(0.99).get(9)
					+ simLevelToScoreSums.get(0.8).get(9)
					+ simLevelToScoreSums.get(0.79).get(9));
			// }

			type = "\n\nsame category and side (total = "
					+ numPairsSameCatSameSide + ")\n";
			type += name + ":scoreUnweightUniWordsPunct";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameCatSameSide.get(0) / numPairsSameCatSameSide)
							+ " (fraction: "
							+ (100 * (scoreSumsSameCatSameSide.get(0) / numPairsSameCatSameSide) / (scoreSums
									.get(0) / numPairs)) + "%)");
			type = name + ":scoreUnweightUniWordsPunctFreq";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameCatSameSide.get(1) / numPairsSameCatSameSide)
							+ " (fraction: "
							+ (100 * (scoreSumsSameCatSameSide.get(1) / numPairsSameCatSameSide) / (scoreSums
									.get(1) / numPairs)) + "%)");
			type = name + ":scoreUnweightUniWords";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameCatSameSide.get(2) / numPairsSameCatSameSide)
							+ " (fraction: "
							+ (100 * (scoreSumsSameCatSameSide.get(2) / numPairsSameCatSameSide) / (scoreSums
									.get(2) / numPairs)) + "%)");
			type = name + ":scoreUnweightUniWordsFreq";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameCatSameSide.get(3) / numPairsSameCatSameSide)
							+ " (fraction: "
							+ (100 * (scoreSumsSameCatSameSide.get(3) / numPairsSameCatSameSide) / (scoreSums
									.get(3) / numPairs)) + "%)");
			type = name + ":scoreUnweightUniContent";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameCatSameSide.get(4) / numPairsSameCatSameSide)
							+ " (fraction: "
							+ (100 * (scoreSumsSameCatSameSide.get(4) / numPairsSameCatSameSide) / (scoreSums
									.get(4) / numPairs)) + "%)");
			type = name + ":scoreUnweightBiWordsPunct";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameCatSameSide.get(5) / numPairsSameCatSameSide)
							+ " (fraction: "
							+ (100 * (scoreSumsSameCatSameSide.get(5) / numPairsSameCatSameSide) / (scoreSums
									.get(5) / numPairs)) + "%)");
			type = name + ":scoreUnweightBiWords";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameCatSameSide.get(6) / numPairsSameCatSameSide)
							+ " (fraction: "
							+ (100 * (scoreSumsSameCatSameSide.get(6) / numPairsSameCatSameSide) / (scoreSums
									.get(6) / numPairs)) + "%)");
			type = name + ":scoreUnweightBiContent";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameCatSameSide.get(7) / numPairsSameCatSameSide)
							+ " (fraction: "
							+ (100 * (scoreSumsSameCatSameSide.get(7) / numPairsSameCatSameSide) / (scoreSums
									.get(7) / numPairs)) + "%)");
			type = name + ":scoreEntropyWeightUniWords";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameCatSameSide.get(8) / numPairsSameCatSameSide)
							+ " (fraction: "
							+ (100 * (scoreSumsSameCatSameSide.get(8) / numPairsSameCatSameSide) / (scoreSums
									.get(8) / numPairs)) + "%)");
			type = name + ":scoreIdfWeightUniWords";
			System.out
					.println(type
							+ "\t"
							+ (scoreSumsSameCatSameSide.get(9) / numPairsSameCatSameSide)
							+ " (fraction: "
							+ (100 * (scoreSumsSameCatSameSide.get(9) / numPairsSameCatSameSide) / (scoreSums
									.get(9) / numPairs)) + "%)");

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("error reading file " + path);
		}

	}

	public static void readDatasets() {

		// build dataset doc paths, build

		// iterate over SIM_DATASETS
		for (String simDatasetName : SIM_DATASETS) {

			// iterate over SIM_FILENAMES_P2
			for (String simFilename : SIM_FILENAMES_P2) {

				String path = SIM_DIRS_ROOT + simDatasetName + SIM_DIR_P2
						+ simDatasetName + simFilename;

				// readAndAnalyzeSimFile(path, simDatasetName);
				readAndAnalyzeSimFileTopMatchOnly(path, simDatasetName);

			}

		}

	}

	public static void doIterativeSim() {

		// iterate over SIM_DATASETS
		for (String simDatasetName : SIM_DATASETS) {
			System.out.println("\n\ndataset: "+simDatasetName);
			
			String header = "totalCount,metricName,simScore,accArg,accCat\n";
			List<String> allLines = new ArrayList<>();
			
			// iterate over SIM_FILENAMES_P2
//			for (String simFilename : SIM_FILENAMES_P2) {
			for (String simFilename : SIM_FILENAMES_P2_NORAWAVG) {

				System.out.println("\nsimFilename: " + simFilename);

				// String simFilename = SIM_FILENAMES_P2[0];
				String path = SIM_DIRS_ROOT + simDatasetName + SIM_DIR_P2
						+ simDatasetName + simFilename;

				// readAndAnalyzeSimFile(path, simDatasetName);
				// readAndAnalyzeSimFileTopMatchOnly(path, simDatasetName);
				
				// idf-weight unigram
				allLines.addAll(readAndAnalyzeSimFileTopMatchesIterative(path, simDatasetName, 9, "idfUni" + simFilename, simDatasetName));
				// entropy-weight unigram
				allLines.addAll(readAndAnalyzeSimFileTopMatchesIterative(path, simDatasetName, 8, "entropyUni" + simFilename, simDatasetName));
				// bigram content
				allLines.addAll(readAndAnalyzeSimFileTopMatchesIterative(path, simDatasetName, 7, "biContent" + simFilename, simDatasetName));
				// unigram content
				allLines.addAll(readAndAnalyzeSimFileTopMatchesIterative(path, simDatasetName, 4, "uniContent" + simFilename, simDatasetName));
				// unigram
				allLines.addAll(readAndAnalyzeSimFileTopMatchesIterative(path, simDatasetName, 2, "uni" + simFilename, simDatasetName));

			}
			
			// write header, lines to file
			try {
				
				FileWriter writer = new FileWriter("simPoints_"+simDatasetName+".csv");
				writer.write(header);
				for (String line : allLines) {
					writer.write(line);
				}
				writer.close();
				
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("error writing to sim .csv file: "+"simPoints_"+simDatasetName+".csv");
			}

		}

	}

	public static void main(String[] args) {

		// readDatasets();
		doIterativeSim();

	}

}
