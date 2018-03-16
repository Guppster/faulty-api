package gr.ntua.softlab.main;

//EBOOK.GURU(AT)yahoo(dot)com
import gr.ntua.softlab.filepaths.Paths;
import gr.ntua.softlab.goodinges.fpgrowth.FPGrowth;
import gr.ntua.softlab.makefilersf.FileRsfMaker;
import gr.ntua.softlab.solutionspace.InputReader;
import gr.ntua.softlab.solutionspaceclustering.ThreadTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Main {

	private String productName;
	private FileRsfMaker fileRsfMaker;
	private InputReader inputReader;
	private Set<String> answer = new HashSet<String>();
	private Set<String> lsaTokens = new HashSet<String>();
	private Set<String> inputTokens = new HashSet<String>();
	private Set<String> fileLsaTokens = new HashSet<String>();
	private Map<String, String> fileNames2Cluster = new HashMap<String, String>();
	private Map<String, Set<String>> clusterName2FileName = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> searchModuleToFiles;
	private Map<String, Set<String>> queryModuleToFiles;
	private Map<String, Set<String>> moduleToFiles;
	private Set<String> clique = new HashSet<String>();
	private Set<String> unusedRelations = new HashSet<String>();
	private Set<String> bad = new HashSet<String>();
	private Map<String, Map<String, Set<String>>> allFileRelationsReversed;
	private Map<String, Double> averagePerFileCon = new HashMap<String, Double>();
	private Map<String, Double> averageToCliqueCon = new HashMap<String, Double>();
	private Map<String, Double> fileToCliqueClusterConnectivity = new HashMap<String, Double>();
	private Map<String, Double> fileToCliqueConnectivity = new HashMap<String, Double>();
	private final int maxClusterSize = 5;
	private String currentFileName = "";
	// private Set<String> expandedLsaTokens;
	// private Set<String> expandedInputTokens;
	private Map<String, Set<String>> solutionClusterName2FileName;
	private Map<String, String> solutionFileName2ClusterName;
	private Set<String> finalV = new HashSet<String>();
	private Map<String, Integer> NameToId = new HashMap<String, Integer>();
	private Map<Integer, String> IdToName = new HashMap<Integer, String>();
	// private Set<String> clusterExpandedLsaTokens = new HashSet<String>();
	// private Map<String, ArrayList<String>> fileName2Parts = new
	// HashMap<String, ArrayList<String>>();
	// private Map<String, Set<String>> filebelongstomodule;
	// private Map<String, Integer> queryModuleWeights;
	// private Map<String, Integer> moduleWeights;
	private Set<String> fileInputTokens;

	public static void main(String[] args) {
		Main main = new Main();
		main.run();
	}

	private void run() {
		try {
			BufferedReader consoleReader = new BufferedReader(
					new InputStreamReader(System.in));
			System.out.println("productName : ");
			productName = consoleReader.readLine();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		fileRsfMaker = new FileRsfMaker(productName);
		fileRsfMaker.doLift();
		inputReader = new InputReader(productName,
				fileRsfMaker.getRsfRepresentation());
		fileNames2Cluster.putAll(inputReader.getFileNames2Clusters());
		clusterName2FileName.putAll(inputReader.getClusterName2FileName());
		// for (Entry<String, Set<String>> cluster :
		// clusterName2FileName.entrySet()) {
		// if (cluster.getValue().size() > 50)
		// System.out.println(cluster.getKey() + " " +
		// cluster.getValue().size());
		// }
		// unusedRelations.add("include");
		// unusedRelations.add("macrouse");
		// unusedRelations.add("macrodefinition");
		// unusedRelations.add("sets");
		// unusedRelations.add("methodbelongstoclass");
		// unusedRelations.add("declaredin");
		// unusedRelations.add("hastype");
		// unusedRelations.add("entitylocation");
		// unusedRelations.add("filebelongstomodule");
		// unusedRelations.add("usestype");
		// unusedRelations.add("accessibleentitybelongstofile");
		// unusedRelations.add("inheritsfrom");
		// unusedRelations.add("calls");
		// unusedRelations.add("classbelongstofile");
		// unusedRelations.add("definedin");
		// unusedRelations.add("attributebelongstoclass");
		// unusedRelations.add("accesses");
		reverseAllFileRelations();
		// ---------------------- TEST GROUNDS ------------------------

		Map<String, Set<String>> FileRelBuckets = new HashMap<String, Set<String>>();

		Map<String, Map<String, Set<String>>> FileRels = new HashMap<String, Map<String, Set<String>>>();
		FileRels.putAll(fileRsfMaker.getAllFileRelations());
		for (Entry<String, Map<String, Set<String>>> e : FileRels.entrySet()) {
			if (!unusedRelations.contains(e.getKey()))
				for (Entry<String, Set<String>> ee : e.getValue().entrySet()) {
					for (String s : ee.getValue()) {
						if (FileRelBuckets.containsKey(ee.getKey())) {
							FileRelBuckets.get(ee.getKey()).addAll(
									ee.getValue());
						} else {
							FileRelBuckets.put(ee.getKey(), ee.getValue());
						}

					}
				}
		}
		for (Entry<String, Map<String, Set<String>>> e : allFileRelationsReversed
				.entrySet()) {
			if (!unusedRelations.contains(e.getValue())) {
				for (Entry<String, Set<String>> ee : e.getValue().entrySet()) {
					for (String s : ee.getValue()) {
						if (FileRelBuckets.containsKey(ee.getKey())) {
							FileRelBuckets.get(ee.getKey()).addAll(
									ee.getValue());
						} else {
							FileRelBuckets.put(ee.getKey(), ee.getValue());
						}
					}
				}
			}
		}
		while (prepareNextReport(FileRelBuckets)) {
			FileRelBuckets.clear();
			FileRels = new HashMap<String, Map<String, Set<String>>>();
			FileRels.putAll(fileRsfMaker.getAllFileRelations());
			for (Entry<String, Map<String, Set<String>>> e : FileRels
					.entrySet()) {
				if (!unusedRelations.contains(e.getKey()))
					for (Entry<String, Set<String>> ee : e.getValue()
							.entrySet()) {
						for (String s : ee.getValue()) {
							if (FileRelBuckets.containsKey(ee.getKey())) {
								FileRelBuckets.get(ee.getKey()).addAll(
										ee.getValue());
							} else {
								FileRelBuckets.put(ee.getKey(), ee.getValue());
							}

						}
					}
			}
			for (Entry<String, Map<String, Set<String>>> e : allFileRelationsReversed
					.entrySet()) {
				if (!unusedRelations.contains(e.getValue())) {
					for (Entry<String, Set<String>> ee : e.getValue()
							.entrySet()) {
						for (String s : ee.getValue()) {
							if (FileRelBuckets.containsKey(ee.getKey())) {
								FileRelBuckets.get(ee.getKey()).addAll(
										ee.getValue());
							} else {
								FileRelBuckets.put(ee.getKey(), ee.getValue());
							}
						}
					}
				}
			}
		}
		// prepareNextReport(FileRelBuckets);
		// ------------------------------------------------------------

		// while (prepareNextReport()) {
		// System.out
		// .println("------------------------------------------------------------------------------------------------------------------------");
		// System.out
		// .println("************************************************************************************************************************");
		// System.out
		// .println("------------------------------------------------------------------------------------------------------------------------");
		// }
	}

	private void constructSolutionSpace() {
		// make fileLsaTokens
		fileLsaTokens = new HashSet<String>();
		Map<String, Set<String>> entityNameToFileNames = fileRsfMaker
				.getEntityNameToFileNames();
		for (String token : lsaTokens) {
			if (fileRsfMaker.getEntityNameToFileNames().containsKey(token))
				fileLsaTokens.addAll(fileRsfMaker.getEntityNameToFileNames()
						.get(token));
			fileLsaTokens.retainAll(fileRsfMaker.getAllRelations()
					.get("filebelongstomodule").keySet());
		}
		// Make fileInputTokens and remove the GoldStandard
		fileInputTokens = new HashSet<String>();
		for (String s : inputTokens) {
			fileInputTokens.addAll(entityNameToFileNames.get(s));
			fileInputTokens.retainAll(fileRsfMaker.getAllRelations()
					.get("filebelongstomodule").keySet());
		}
		// fileLsaTokens.addAll(fileInputTokens);
	}

	private boolean prepareNextReport(Map<String, Set<String>> FileRelBuckets) {
		clearSets();
		if (inputReader.nextReport()) {
			answer.retainAll(fileRsfMaker.getEntityNameToFileNames().keySet());
			answer = inputReader.getAnswer();
			lsaTokens = inputReader.getLsaTokens();
			lsaTokens.retainAll(fileRsfMaker.getEntityNameToFileNames()
					.keySet());
			inputTokens = inputReader.getInputTokens();
			inputTokens.retainAll(fileRsfMaker.getEntityNameToFileNames()
					.keySet());
			currentFileName = inputReader.getFilename();
			// reverseAllFileRelations();
			constructSolutionSpace();
			Set<String> toRemove = new HashSet<String>();
			fileLsaTokens.addAll(expandeTokenSet(fileLsaTokens));
			for (String s : fileLsaTokens)
				if (fileRsfMaker.getRsfRepresentation().getId(s).contains("m"))
					toRemove.add(s);
			fileLsaTokens.removeAll(toRemove);
			toRemove.clear();
			toRemove.addAll(FileRelBuckets.keySet());
			toRemove.removeAll(fileLsaTokens);
			for (String s : toRemove)
				FileRelBuckets.remove(s);
			for (Entry<String, Set<String>> e : FileRelBuckets.entrySet())
				e.getValue().retainAll(fileLsaTokens);
			Map<String, Set<Integer>> FileRelInt = new HashMap<String, Set<Integer>>();
			makeTempIDs(fileLsaTokens);
			for (Entry<String, Set<String>> e : FileRelBuckets.entrySet()) {
				if (!fileRsfMaker.getRsfRepresentation().getId(e.getKey())
						.contains("m")) {
					FileRelInt.put(e.getKey(), new HashSet<Integer>());
					for (String s : e.getValue()) {
						if (fileRsfMaker.getRsfRepresentation().getId(s) != null)
							if (fileRsfMaker.getRsfRepresentation().getId(s)
									.contains("m"))
								continue;
							else
								FileRelInt.get(e.getKey()).add(NameToId.get(s));
					}
				}
			}
			int max = 0;
			int totalBuckets = 0;
			Set<Integer> totalRelatedFiles = new HashSet<Integer>();
			Set<String> ModulesMustRemove = new HashSet<String>();
			for (Entry<String, Set<Integer>> e : FileRelInt.entrySet()) {
				if (!e.getValue().isEmpty()) {
					totalBuckets++;

					if (e.getValue().size() > max)
						max = e.getValue().size();
					totalRelatedFiles.addAll(e.getValue());
				}
			}
			System.out.println("megistes sxeseis = " + max + "||||"
					+ "totalBuckets = " + totalBuckets + "||||totalFiles = "
					+ totalRelatedFiles.size());
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
						"../../apriori_" + productName + ".input")));
				for (Entry<String, Set<Integer>> e : FileRelInt.entrySet()) {
					if (!e.getValue().isEmpty()) {
						for (Integer i : e.getValue())
							bw.write(i + " ");
						bw.newLine();
					}
				}
				bw.close();

			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
			// calculate Suppport
			Integer support = 0;
			for (Entry<String, Set<Integer>> e : FileRelInt.entrySet())
				support += e.getValue().size();
			if (FileRelInt.size() != 0) {
				// System.out.println("|   average Bucket Size = "
				// + Math.ceil(support / (double) FileRelInt.size()));
				// if (support / (double) FileRelInt.size() < 0.05 *
				// totalRelatedFiles
				// .size()){
				// support = (int) Math.round(Math
				// .ceil((support / (double) FileRelInt.size()) * 3));
				// }else{
				// if(support / (double) FileRelInt.size() > 0.1 *
				// totalRelatedFiles)
				// }
				support = (int) Math.round(Math
						.ceil((support / (double) FileRelInt.size())));
				String[] args = new String[2];
				args[0] = "../../apriori_" + productName + ".input";
				// args[1] = "" + support ;
				support = (int) (0.3 * totalRelatedFiles.size());
				// args[1] = "" + 0.2 * totalRelatedFiles.size();
				// args[1] = "" + 200;
				System.out.println(support);
				try {
					FPGrowth fpg = new FPGrowth(new File(args[0]), support,
							productName);
					// args[1] = "" + support / (double) FileRelInt.size();
					// System.out.println(args[1]);
					// Apriori a = new Apriori(args);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}

			// performRanking();
			// calculateInitialResults();
			// System.out.println("####################Search Modules######################");
			// makeSearchModuleWeights();
			// System.out.println("********************Inputs Modules**********************");
			// makeQueryModuleWeights();
			// System.out.println("********************Answers Size **********************");
			// System.out.println(answer.size());
			// System.out.println("####################Paths from Lsa######################");
			// calculatePathsFromLsa();
			// System.out.println("####################Paths from Input######################");
			// calculatePathsFromInput();
			// for (Entry<String, Set<String>> moduleAndContents :
			// searchModuleToFiles.entrySet()) {
			// moduleAndContents.getValue().retainAll(answer);
			// System.out.println(moduleAndContents.getKey() + " " +
			// moduleAndContents.getValue().size() + " " + answer.size());
			// }
			// System.out.println("--------------------Answer Modules----------------------");
			// makeAnswerModuleWeights();
			// makeFilename2Parts();
			// makeAnswerModuleWeights();
			return true;
		} else {
			return false;
		}

	}

	@SuppressWarnings("unused")
	private void makeFilename2Parts() {
		for (String s : fileNames2Cluster.keySet()) {
			for (String w : s
					.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
				System.out.println(w);
			}
			System.out
					.println("----------------------------------------------------------");
		}
	}

	private void reverseAllFileRelations() {
		allFileRelationsReversed = new HashMap<String, Map<String, Set<String>>>();
		for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfMaker
				.getAllFileRelations().entrySet()) {
			for (Entry<String, Set<String>> entityToEntities : completeRelation
					.getValue().entrySet()) {
				for (String targetEntity : entityToEntities.getValue()) {
					if (allFileRelationsReversed.containsKey(completeRelation
							.getKey()))
						if (allFileRelationsReversed.get(
								completeRelation.getKey()).containsKey(
								targetEntity))
							allFileRelationsReversed
									.get(completeRelation.getKey())
									.get(targetEntity)
									.add(entityToEntities.getKey());
						else {
							Set<String> toInsert = new HashSet<String>();
							toInsert.add(entityToEntities.getKey());
							allFileRelationsReversed.get(
									completeRelation.getKey()).put(
									targetEntity, toInsert);
						}
					else {
						Map<String, Set<String>> toInsertMap = new HashMap<String, Set<String>>();
						Set<String> toInsert = new HashSet<String>();
						toInsert.add(entityToEntities.getKey());
						toInsertMap.put(targetEntity, toInsert);
						allFileRelationsReversed.put(completeRelation.getKey(),
								toInsertMap);
					}
				}
			}
		}
	}

	private void performRanking() {
		Set<String> Visited = new HashSet<String>();
		// Visited.addAll(fileLsaTokens);
		Visited.addAll(expandeTokenSet(fileLsaTokens));
		finalV.addAll(Visited);
		Set<String> temp = new HashSet<String>();
		temp.addAll(fileRsfMaker.getAllRelations().get("filebelongstomodule")
				.keySet());
		int c = 0;
		for (String s : temp)
			if (!s.contains(".h"))
				c++;
		System.out.println("Total System Files = " + temp.size());
		System.out.println("Total non .h Files = " + c);
		Set<String> nonLibraries = new HashSet<String>();
		for (String s : finalV)
			if (!s.contains(".h"))
				nonLibraries.add(s);
		System.out.println("Total non .h Files from Expansion= "
				+ nonLibraries.size());
		if (nonLibraries.size() < 0.15 * c)
			outputURanked();
		else
			performClusterRanking();
	}

	private void outputURanked() {
		Set<String> Visited = new HashSet<String>();
		clique.clear();
		for (String s : finalV)
			if (!s.contains(".h"))
				Visited.add(s);
		finalV.clear();
		finalV.addAll(Visited);
		Map<String, Map<String, Set<String>>> fileRsfIn = new HashMap<String, Map<String, Set<String>>>();
		fileRsfIn.putAll(fileRsfMaker.getAllFileRelations());
		for (String s : unusedRelations)
			fileRsfIn.remove(s);
		clique.addAll(selectMostConnected(fileRsfMaker.getAllFileRelations(),
				finalV));
		System.out.println("clique size = " + clique.size());
		for (String fileName : finalV) {
			fileToCliqueConnectivity.put(fileName, new Double(0.0));
		}
		// for (Entry<String, Map<String, Set<String>>> completeRelation :
		// fileRsfIn.entrySet()) {
		// for (Entry<String, Set<String>> entityToEntities :
		// completeRelation.getValue().entrySet()) {
		// for (String s : entityToEntities.getValue()) {
		// if (finalV.contains(s) && finalV.contains(entityToEntities.getKey()))
		// if (clique.contains(s))
		// fileToCliqueConnectivity.put(entityToEntities.getKey(),
		// fileToCliqueConnectivity.get(entityToEntities.getKey()) + 1);
		// else if (clique.contains(entityToEntities.getKey()))
		// fileToCliqueConnectivity.put(s, fileToCliqueConnectivity.get(s) + 1);
		//
		// }
		// }
		// }
		for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn
				.entrySet()) {
			for (Entry<String, Set<String>> entityToEntities : completeRelation
					.getValue().entrySet()) {
				for (String s : entityToEntities.getValue()) {
					if (finalV.contains(s))
						fileToCliqueConnectivity.put(s,
								fileToCliqueConnectivity.get(s) + 1);
					else if (finalV.contains(entityToEntities.getKey()))
						fileToCliqueConnectivity.put(entityToEntities.getKey(),
								fileToCliqueConnectivity.get(entityToEntities
										.getKey()) + 1);
				}
			}
		}
		fileToCliqueConnectivity = sortByComparatorDouble(fileToCliqueConnectivity);
		/*
		 * fileToCliqueConnectivity =
		 * sortByComparatorDouble(fileToCliqueConnectivity); Map<Double,
		 * List<String>> scoreToKeys = new HashMap<Double, List<String>>(); for
		 * (Entry<String, Double> fileToClique :
		 * fileToCliqueClusterConnectivity.entrySet()) { if
		 * (scoreToKeys.containsKey(fileToClique.getValue())) {
		 * scoreToKeys.get(fileToClique.getValue()).add(fileToClique.getKey());
		 * } else { scoreToKeys.put(fileToClique.getValue(), new
		 * ArrayList<String>());
		 * scoreToKeys.get(fileToClique.getValue()).add(fileToClique.getKey());
		 * } } // for (Entry<Double, List<String>> e : scoreToKeys.entrySet())
		 * // Collections.sort(e.getValue());
		 * 
		 * Map<Double, List<String>> treeMap = new TreeMap<Double,
		 * List<String>>(new Comparator<Double>(){
		 * 
		 * @Override public int compare(Double o1, Double o2){ return
		 * o2.compareTo(o1); }
		 * 
		 * }); treeMap.putAll(scoreToKeys); try { new File(Paths.URANKEDREPORTS
		 * + productName).mkdirs(); BufferedWriter URankedWriter = new
		 * BufferedWriter(new FileWriter(Paths.URANKEDREPORTS + productName +
		 * "/" + currentFileName.split("_")[0] + ".csv"));
		 * 
		 * for (Entry<Double, List<String>> file : treeMap.entrySet()) { // if
		 * (file.getValue() != 0) Collections.sort(file.getValue()); for (String
		 * s : file.getValue()) if (!answer.contains(s)) {
		 * URankedWriter.write(String.format("\t%-85s\t%15f\t\n", s,
		 * file.getKey())); System.out.format("\t%-85s\t%15f\t\n", s,
		 * file.getKey()); } else {
		 * URankedWriter.write(String.format("\t%-85s\t%15f\t<-------------\n",
		 * s, file.getKey()));
		 * System.out.format("\t%-85s\t%15f\t<-------------\n", s,
		 * file.getKey()); } } scoreToKe.clear(); treeMap.clear();
		 */
		Map<String, Double> treeMap = new TreeMap<String, Double>(
				new Comparator<String>() {

					@Override
					public int compare(String o1, String o2) {
						return o2.compareTo(o1);
					}

				});
		treeMap.putAll(fileToCliqueConnectivity);
		try {
			new File(Paths.URANKEDREPORTS + productName).mkdirs();
			BufferedWriter URankedWriter = new BufferedWriter(new FileWriter(
					Paths.URANKEDREPORTS + productName + "/"
							+ currentFileName.split("_")[0] + ".csv"));
			System.out.println(currentFileName.split("_")[0]);
			for (Entry<String, Double> file : treeMap.entrySet())
				// if (file.getValue() != 0)
				if (!answer.contains(file.getKey())) {
					URankedWriter.write(String.format("\t%-85s\t%15f\t\n",
							file.getKey(), file.getValue()));
					System.out.format("\t%-85s\t%15f\t\n", file.getKey(),
							file.getValue());
				} else {
					URankedWriter.write(String.format(
							"\t%-85s\t%15f\t<-------------\n", file.getKey(),
							file.getValue()));
					System.out.format("\t%-85s\t%15f\t<-------------\n",
							file.getKey(), file.getValue());
				}
			URankedWriter.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}

	private void performClusterRanking() {
		Set<String> clustersToBreakDown = new HashSet<String>();
		Map<String, Set<String>> brokenDownClusters = new HashMap<String, Set<String>>();
		Set<String> Visited = new HashSet<String>();
		Set<String> clique = new HashSet<String>();
		Set<String> hits = new HashSet<String>();
		Set<String> GSClusters = new HashSet<String>();
		Set<String> inputTokensClusters = new HashSet<String>();
		Set<String> bad = new HashSet<String>();
		clique.clear();
		Visited.addAll(finalV);
		// Recall before applying anything but the expansion step and without
		// removing the .h files
		System.out.println("LsaFileTokens at start = " + fileLsaTokens.size());
		System.out.println("LsaFileTokens after expansion " + finalV.size());
		hits.addAll(finalV);
		hits.retainAll(answer);
		System.out.println("Recall (Pre reduction)= " + hits.size()
				/ (double) answer.size());
		fileInputTokens.removeAll(bad);
		// clique.addAll(fileInputTokens);
		clique.addAll(selectMostConnected(fileRsfMaker.getAllFileRelations(),
				finalV));
		// clique.addAll(selectMostConnected(fileRsfMaker.getAllFileRelations(),
		// fileInputTokens));
		System.out
				.println("***********************PERFORMING CLUSTERING***********************");
		// finalV.clear();
		// for (String s : Visited) {
		// if (!s.contains(".h"))
		// finalV.add(s);
		// }
		printSolutionRsf(finalV, fileRsfMaker.getAllFileRelations(),
				inputReader.getBugNumber(), fileRsfMaker.getEntityId(), clique);
		Set<String> VisitedTry = new HashSet<String>();
		// remove .h from answer
		hits.addAll(answer);
		for (String s : hits)
			if (s.contains(".h"))
				answer.remove(s);
		hits.clear();
		for (String fileName : answer)
			GSClusters.add(solutionFileName2ClusterName.get(fileName));
		// remove .h from visited
		hits.addAll(Visited);
		for (String s : hits)
			if (s.contains(".h"))
				Visited.remove(s);
		finalV.clear();
		finalV.addAll(Visited);

		// -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~ClusterSizecalculation-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-
		// -~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-
		for (Entry<String, Set<String>> cluster : solutionClusterName2FileName
				.entrySet()) {
			if (cluster.getValue().size() > maxClusterSize)
				clustersToBreakDown.add(cluster.getKey());
		}

		// ~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-Call big cluster
		// Splitter~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-
		// ~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~
		// Map<String, Set<String>> rearrangedBig = new HashMap<String,
		// Set<String>>();
		// rearrangedBig.putAll(rearrangeClusters(clustersToBreakDown, clique));
		// for (String s : clustersToBreakDown) {
		// solutionClusterName2FileName.get(s).clear();
		// solutionClusterName2FileName.remove(s);
		// }
		// solutionClusterName2FileName.putAll(rearrangedBig);
		// solutionFileName2ClusterName.clear();
		// for (Entry<String, Set<String>> cluster :
		// solutionClusterName2FileName.entrySet())
		// for (String fileName : cluster.getValue())
		// solutionFileName2ClusterName.put(fileName, cluster.getKey());

		// ---------------------------------------------------------------------------------------------------------------------------------------------------
		// **************************************************************FinalResultsCalculation**************************************************************
		// ---------------------------------------------------------------------------------------------------------------------------------------------------
		toFromCliqueConnectivity(fileRsfMaker.getAllFileRelations(), clique,
				GSClusters);
		// VisitedTry.addAll(SelectInGSValues(fileRsfMaker.getAllFileRelations(),
		// GSClusters));
		Map<String, Double> clusterScored = new HashMap<String, Double>();
		double MaxClusterSize = 0.0;
		for (Entry<String, Set<String>> e : solutionClusterName2FileName
				.entrySet()) {
			if (MaxClusterSize < e.getValue().size())
				MaxClusterSize = e.getValue().size();
		}
		// -~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+
		// ~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+MetricHasBeenChanged+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+
		// -~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+~-=-~+
		for (Entry<String, Double> cluster : averagePerFileCon.entrySet()) {
			if (solutionClusterName2FileName.get(cluster.getKey()).size() != 0)
				clusterScored.put(
						cluster.getKey(),
						(cluster.getValue() / averageToCliqueCon.get(cluster
								.getKey()))
								* solutionClusterName2FileName.get(
										cluster.getKey()).size());
			// clusterScored.put(
			// cluster.getKey(),
			// (cluster.getValue() / (averageToCliqueCon.get(cluster.getKey()) +
			// Math.log10(solutionClusterName2FileName.get(
			// cluster.getKey()).size())))// );
			// * solutionClusterName2FileName.get(cluster.getKey()).size());
		}
		clusterScored = sortByComparatorDouble(clusterScored);
		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(
				clusterScored.entrySet());
		Map<String, Set<String>> top50PercentileClusters = new HashMap<String, Set<String>>();
		double max = 0;
		for (Entry<String, Double> cluster : clusterScored.entrySet())
			if (max < cluster.getValue())
				max = cluster.getValue();

		// Expectation Calculation
		// ~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*

		Map<String, Double> clusterExpectation = new HashMap<String, Double>();
		double meanExpectation = 0.0;
		for (Entry<String, Double> cluster : clusterScored.entrySet()) {
			clusterExpectation
					.put(cluster.getKey(),
							cluster.getValue()
									* (solutionClusterName2FileName.get(
											cluster.getKey()).size() / (double) solutionFileName2ClusterName
											.size()));
			if (!Double.isNaN(clusterExpectation.get(cluster.getKey()))
					&& clusterExpectation.get(cluster.getKey()) < 10000000)
				meanExpectation += clusterExpectation.get(cluster.getKey());
		}
		meanExpectation = meanExpectation / clusterExpectation.size();
		System.out.println("meanExpectation = " + meanExpectation);
		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// ++++++++++++++++++++++++++Expectation
		// Printing+++++++++++++++++++++++++
		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// for (Entry<String, Double> cluster : clusterExpectation.entrySet())
		// if ((cluster.getValue() / meanExpectation) > 10)
		// System.out.format("%85s\t%15f\n", cluster.getKey(),
		// cluster.getValue());

		// Result print out
		// ~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*
		// FirstLevelOfReporting~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=
		try {
			new File(Paths.FIRSTLEVELREPORTS + productName).mkdirs();
			BufferedWriter firstLevelWriter = new BufferedWriter(
					new FileWriter(Paths.FIRSTLEVELREPORTS + productName + "/"
							+ currentFileName.split("_")[0] + ".csv"));
			firstLevelWriter.write(String.format(
					"%-85s\t%15s\t%15s\t%15s\t%10s\t%15s\n", "ClusterName",
					"Score", "expectation", "Connections", "ToClique",
					"ClusterSize", "ScorePercent"));
			System.out.format("%-85s\t%15s\t%15s\t%15s\t%10s\t%15s\n",
					"ClusterName", "Score", "expectation", "Connections",
					"ToClique", "ClusterSize", "ScorePercent");
			for (Entry<String, Double> cluster : clusterScored.entrySet()) {
				if (!GSClusters.contains(cluster.getKey())) {
					if (list.indexOf(cluster) > 0.5 * list.size()
							&& solutionClusterName2FileName.get(
									cluster.getKey()).size() < 0.5 * MaxClusterSize) {
						top50PercentileClusters.put(cluster.getKey(),
								solutionClusterName2FileName.get(cluster
										.getKey()));
						firstLevelWriter
								.write(String
										.format("%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f\n",
												cluster.getKey(),
												cluster.getValue(),
												cluster.getValue()
														* (solutionClusterName2FileName
																.get(cluster
																		.getKey())
																.size() / (double) solutionFileName2ClusterName
																.size()),
												averagePerFileCon.get(cluster
														.getKey())
														* solutionClusterName2FileName
																.get(cluster
																		.getKey())
																.size(),
												averageToCliqueCon.get(cluster
														.getKey())
														* solutionClusterName2FileName
																.get(cluster
																		.getKey())
																.size(),
												solutionClusterName2FileName
														.get(cluster.getKey())
														.size(),
												cluster.getValue() / max));
						System.out
								.format("%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f\n",
										cluster.getKey(),
										cluster.getValue(),
										cluster.getValue()
												* (solutionClusterName2FileName
														.get(cluster.getKey())
														.size() / (double) solutionFileName2ClusterName
														.size()),
										averagePerFileCon.get(cluster.getKey())
												* solutionClusterName2FileName
														.get(cluster.getKey())
														.size(),
										averageToCliqueCon.get(cluster.getKey())
												* solutionClusterName2FileName
														.get(cluster.getKey())
														.size(),
										solutionClusterName2FileName.get(
												cluster.getKey()).size(),
										cluster.getValue() / max);
					}
				} else {
					if (list.indexOf(cluster) > 0.5 * list.size()
							&& solutionClusterName2FileName.get(
									cluster.getKey()).size() < 0.5 * MaxClusterSize) {
						top50PercentileClusters.put(cluster.getKey(),
								solutionClusterName2FileName.get(cluster
										.getKey()));
						firstLevelWriter
								.write(String
										.format("%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f\t<++++++++++++GS\n",
												cluster.getKey(),
												cluster.getValue(),
												cluster.getValue()
														* (solutionClusterName2FileName
																.get(cluster
																		.getKey())
																.size() / (double) solutionFileName2ClusterName
																.size()),
												averagePerFileCon.get(cluster
														.getKey())
														* solutionClusterName2FileName
																.get(cluster
																		.getKey())
																.size(),
												averageToCliqueCon.get(cluster
														.getKey())
														* solutionClusterName2FileName
																.get(cluster
																		.getKey())
																.size(),
												solutionClusterName2FileName
														.get(cluster.getKey())
														.size(),
												cluster.getValue() / max));
						System.out
								.format("%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f <++++++++++++GS\n",
										cluster.getKey(),
										cluster.getValue(),
										cluster.getValue()
												* (solutionClusterName2FileName
														.get(cluster.getKey())
														.size() / (double) solutionFileName2ClusterName
														.size()),
										averagePerFileCon.get(cluster.getKey())
												* solutionClusterName2FileName
														.get(cluster.getKey())
														.size(),
										averageToCliqueCon.get(cluster.getKey())
												* solutionClusterName2FileName
														.get(cluster.getKey())
														.size(),
										solutionClusterName2FileName.get(
												cluster.getKey()).size(),
										cluster.getValue() / max);
					}
				}
			}

			for (Entry<String, Set<String>> finalSelectedCluster : top50PercentileClusters
					.entrySet()) {
				firstLevelWriter
						.write("------------------------------______________________________---------------------------\n");
				firstLevelWriter.write(finalSelectedCluster.getKey() + "\n");
				firstLevelWriter
						.write("....................................................................\n");
				System.out
						.println("------------------------------______________________________---------------------------");
				System.out.println(finalSelectedCluster.getKey());
				System.out
						.println("....................................................................");
				for (Entry<String, Double> filetoCliqueCluster : fileToCliqueClusterConnectivity
						.entrySet()) {
					if (finalSelectedCluster.getValue().contains(
							filetoCliqueCluster.getKey())
							&& filetoCliqueCluster.getValue() > 0)
						if (!answer.contains(filetoCliqueCluster.getKey())) {
							firstLevelWriter.write(String.format(
									"\t\t%-85s\t%15f\n",
									filetoCliqueCluster.getKey(),
									filetoCliqueCluster.getValue()));
							System.out.format("\t\t%-85s\t%15f\n",
									filetoCliqueCluster.getKey(),
									filetoCliqueCluster.getValue());
						} else {
							firstLevelWriter.write(String.format(
									"\t\t%-85s\t%15f\t<-----------\n",
									filetoCliqueCluster.getKey(),
									filetoCliqueCluster.getValue()));
							System.out.format("\t\t%-85s\t%15f<-----------\n",
									filetoCliqueCluster.getKey(),
									filetoCliqueCluster.getValue());
						}
				}
				firstLevelWriter
						.write("______________________________------------------------------___________________________\n");
				System.out
						.println("______________________________------------------------------___________________________");
			}
			firstLevelWriter.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		// ZeroReporting~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=
		try {
			new File(Paths.ZEROREPORTS + productName).mkdirs();
			BufferedWriter ZeroWriter = new BufferedWriter(new FileWriter(
					Paths.ZEROREPORTS + productName + "/"
							+ currentFileName.split("_")[0] + ".csv"));
			// fileToCliqueConnectivity =
			// sortByComparatorDouble(fileToCliqueConnectivity);
			Map<String, Double> treeMap = new TreeMap<String, Double>(
					new Comparator<String>() {

						@Override
						public int compare(String o1, String o2) {
							return o2.compareTo(o1);
						}

					});
			treeMap.putAll(fileToCliqueConnectivity);
			fileToCliqueConnectivity.clear();
			fileToCliqueConnectivity.putAll(treeMap);
			// fileToCliqueCluster = sort
			for (Entry<String, Set<String>> finalSelectedCluster : top50PercentileClusters
					.entrySet()) {
				ZeroWriter
						.write(String
								.format("------------------------------______________________________---------------------------\n"));
				System.out
						.println("------------------------------______________________________---------------------------");
				ZeroWriter.write(String.format(finalSelectedCluster.getKey()
						+ "\n"));
				System.out.println(finalSelectedCluster.getKey());
				ZeroWriter
						.write(String
								.format("....................................................................\n"));
				System.out
						.println("....................................................................");

				for (Entry<String, Double> filetoCliqueCluster : fileToCliqueClusterConnectivity
						.entrySet()) {
					if (finalSelectedCluster.getValue().contains(
							filetoCliqueCluster.getKey())
							&& filetoCliqueCluster.getValue() == 0)
						if (!answer.contains(filetoCliqueCluster.getKey())) {
							ZeroWriter.write(String.format("\t\t%-85s\t%15f\n",
									filetoCliqueCluster.getKey(),
									filetoCliqueCluster.getValue()));
							System.out.format("\t\t%-85s\t%15f\n",
									filetoCliqueCluster.getKey(),
									filetoCliqueCluster.getValue());
						} else {
							ZeroWriter.write(String.format(
									"\t\t%-85s\t%15f\t<-----------\n",
									filetoCliqueCluster.getKey(),
									filetoCliqueCluster.getValue()));
							System.out.format(
									"\t\t%-85s\t%15f\t<-----------\n",
									filetoCliqueCluster.getKey(),
									filetoCliqueCluster.getValue());
						}
				}
				ZeroWriter
						.write(String
								.format("______________________________------------------------------___________________________\n"));
				System.out
						.println("______________________________------------------------------___________________________");
			}
			ZeroWriter.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		// BigReporting~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=
		try {
			top50PercentileClusters.clear();
			new File(Paths.BIGREPORTS + productName).mkdirs();
			BufferedWriter BigWriter = new BufferedWriter(new FileWriter(
					Paths.BIGREPORTS + productName + "/"
							+ currentFileName.split("_")[0] + ".csv"));
			BigWriter.write(String.format(
					"%-85s\t%15s\t%15s\t%15s\t%10s\t%15s\n", "ClusterName",
					"Score", "expectation", "Connections", "ToClique",
					"ClusterSize", "ScorePercent"));
			System.out.format("%-85s\t%15s\t%15s\t%15s\t%10s\t%15s\n",
					"ClusterName", "Score", "expectation", "Connections",
					"ToClique", "ClusterSize", "ScorePercent");
			for (Entry<String, Double> cluster : clusterScored.entrySet()) {
				if (!GSClusters.contains(cluster.getKey())) {
					if (list.indexOf(cluster) > 0.5 * list.size()
							&& solutionClusterName2FileName.get(
									cluster.getKey()).size() >= 0.5 * MaxClusterSize) {
						top50PercentileClusters.put(cluster.getKey(),
								solutionClusterName2FileName.get(cluster
										.getKey()));
						BigWriter
								.write(String
										.format("%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f\n",
												cluster.getKey(),
												cluster.getValue(),
												cluster.getValue()
														* (solutionClusterName2FileName
																.get(cluster
																		.getKey())
																.size() / (double) solutionFileName2ClusterName
																.size()),
												averagePerFileCon.get(cluster
														.getKey())
														* solutionClusterName2FileName
																.get(cluster
																		.getKey())
																.size(),
												averageToCliqueCon.get(cluster
														.getKey())
														* solutionClusterName2FileName
																.get(cluster
																		.getKey())
																.size(),
												solutionClusterName2FileName
														.get(cluster.getKey())
														.size(),
												cluster.getValue() / max));
						System.out
								.format("%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f\n",
										cluster.getKey(),
										cluster.getValue(),
										cluster.getValue()
												* (solutionClusterName2FileName
														.get(cluster.getKey())
														.size() / (double) solutionFileName2ClusterName
														.size()),
										averagePerFileCon.get(cluster.getKey())
												* solutionClusterName2FileName
														.get(cluster.getKey())
														.size(),
										averageToCliqueCon.get(cluster.getKey())
												* solutionClusterName2FileName
														.get(cluster.getKey())
														.size(),
										solutionClusterName2FileName.get(
												cluster.getKey()).size(),
										cluster.getValue() / max);
					}
				} else {
					if (list.indexOf(cluster) > 0.5 * list.size()
							&& solutionClusterName2FileName.get(
									cluster.getKey()).size() >= 0.5 * MaxClusterSize) {
						top50PercentileClusters.put(cluster.getKey(),
								solutionClusterName2FileName.get(cluster
										.getKey()));
						BigWriter
								.write(String
										.format("%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f\t<++++++++++++GS\n",
												cluster.getKey(),
												cluster.getValue(),
												cluster.getValue()
														* (solutionClusterName2FileName
																.get(cluster
																		.getKey())
																.size() / (double) solutionFileName2ClusterName
																.size()),
												averagePerFileCon.get(cluster
														.getKey())
														* solutionClusterName2FileName
																.get(cluster
																		.getKey())
																.size(),
												averageToCliqueCon.get(cluster
														.getKey())
														* solutionClusterName2FileName
																.get(cluster
																		.getKey())
																.size(),
												solutionClusterName2FileName
														.get(cluster.getKey())
														.size(),
												cluster.getValue() / max));
						System.out
								.format("%-85s\t%15f\t%15f\t%15f\t%15f\t%15d\t%15f <++++++++++++GS\n",
										cluster.getKey(),
										cluster.getValue(),
										cluster.getValue()
												* (solutionClusterName2FileName
														.get(cluster.getKey())
														.size() / (double) solutionFileName2ClusterName
														.size()),
										averagePerFileCon.get(cluster.getKey())
												* solutionClusterName2FileName
														.get(cluster.getKey())
														.size(),
										averageToCliqueCon.get(cluster.getKey())
												* solutionClusterName2FileName
														.get(cluster.getKey())
														.size(),
										solutionClusterName2FileName.get(
												cluster.getKey()).size(),
										cluster.getValue() / max);
					}
				}
			}
			Map<Double, List<String>> inClusterRanking = new HashMap<Double, List<String>>();

			for (Entry<String, Set<String>> finalSelectedCluster : top50PercentileClusters
					.entrySet()) {
				BigWriter
						.write("------------------------------______________________________---------------------------\n");
				System.out
						.println("------------------------------______________________________---------------------------");
				BigWriter.write(finalSelectedCluster.getKey() + "\n");
				System.out.println(finalSelectedCluster.getKey());
				BigWriter
						.write("....................................................................\n");
				System.out
						.println("....................................................................");
				for (Entry<String, Double> filetoCliqueCluster : fileToCliqueClusterConnectivity
						.entrySet()) {
					if (finalSelectedCluster.getValue().contains(
							filetoCliqueCluster.getKey())) {
						if (inClusterRanking.containsKey(filetoCliqueCluster
								.getValue())) {
							inClusterRanking
									.get(filetoCliqueCluster.getValue()).add(
											filetoCliqueCluster.getKey());
						} else {
							inClusterRanking.put(
									filetoCliqueCluster.getValue(),
									new ArrayList<String>());
							inClusterRanking
									.get(filetoCliqueCluster.getValue()).add(
											filetoCliqueCluster.getKey());
						}
					}
				}

				Map<Double, List<String>> treeMap1 = new TreeMap<Double, List<String>>(
						new Comparator<Double>() {

							@Override
							public int compare(Double o1, Double o2) {
								return o2.compareTo(o1);
							}

						});
				treeMap1.putAll(inClusterRanking);
				// inClusterRanking.clear();
				for (Entry<Double, List<String>> sameValue : treeMap1
						.entrySet()) {
					Collections.sort(sameValue.getValue());
					for (String s : sameValue.getValue())
						if (!answer.contains(s)) {
							BigWriter.write(String.format("\t\t%-85s\t%15f\n",
									s, sameValue.getKey()));
							System.out.format("\t\t%-85s\t%15f\n", s,
									sameValue.getKey());

						} else {
							BigWriter.write(String.format(
									"\t\t%-85s\t%15f\t<-----------\n", s,
									sameValue.getKey()));
							System.out.format(
									"\t\t%-85s\t%15f\t<-----------\n", s,
									sameValue.getKey());
						}
				}
				inClusterRanking.clear();
				treeMap1.clear();
				BigWriter
						.write("______________________________------------------------------___________________________\n");
				System.out
						.println("______________________________------------------------------___________________________");
			}
			BigWriter.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		// hits.clear();
		// hits.addAll(VisitedTry);
		// hits.retainAll(answer);
		// System.out.println("VisitedTry = " + VisitedTry.size());
		// System.out.println("Recall (Post connectivity reduction)= " +
		// hits.size() / (double) answer.size());
		// // bad.add("src/widgets/osd.cpp");
		// // bad.add("tests/testdebug.cpp");
		// //
		// bad.add("tests/synchronization/testmasterslavesynchronizationjob.cpp");
		// // bad.add("tests/synchronization/testunionjob.cpp");
		// // bad.add("src/covermanager/covermanager.cpp");
		// // Set<String> a = new HashSet<String>();
		// // a.addAll(fileInputTokens);
		// // a.removeAll(bad);
		// // a.retainAll(finalV);
		// // fileInputTokens.clear();
		// // for (String s : a)
		// // if (!s.contains(".h"))
		// // fileInputTokens.add(s);
		// for (String inputFile : fileInputTokens) {
		// // System.out.println(solutionFileName2ClusterName.get(inputFile));
		// inputTokensClusters.add(solutionFileName2ClusterName.get(inputFile));
		// }
		// Map<String, Map<String, Integer>> weights =
		// countInterClusterRelations(fileRsfMaker.getAllFileRelations(),
		// inputTokensClusters);
		//
		// Set<String> clustersFinal = selectClusters(weights,
		// inputTokensClusters);
		// inputTokensClusters.addAll(clustersFinal);
		// // System.out.println("Total good Clusters selected " +
		// goodClusters);
		// Visited.clear();
		// finalV.removeAll(bad);
		// Visited.addAll(finalV);
		// finalV.clear();
		// for (String s : Visited)
		// if (GSClusters.contains(solutionFileName2ClusterName.get(s)))
		// finalV.add(s);
		// System.out.println("Gold Standard " + answer);
		// System.out.println("LsaFileTokens at start = " +
		// fileLsaTokens.size());
		// System.out.println("LsaFileTokens after expansion " + finalV.size());
		// hits = new HashSet<String>();
		// hits.addAll(finalV);
		// hits.retainAll(answer);
		// System.out.println("Recall (Post Cluster reduction)= " + hits.size()
		// / (double) answer.size());

	}

	private Map<String, Set<String>> rearrangeClusters(
			Set<String> clustersToBreakDown, Set<String> clique) {
		// System.out.println("Max Cluster Size = " + maxClusterSize);
		Map<String, Map<String, Integer>> perClusterCliqueConnectivity = new HashMap<String, Map<String, Integer>>();
		Map<String, Set<String>> allSplitClusters = new HashMap<String, Set<String>>();
		Set<String> cliqueCluster = new HashSet<String>();
		Map<String, Map<String, Set<String>>> fileRsfIn = new HashMap<String, Map<String, Set<String>>>();

		fileRsfIn.putAll(fileRsfMaker.getAllFileRelations());
		for (String relName : unusedRelations)
			fileRsfIn.remove(relName);

		for (String s : clique) {
			cliqueCluster.add(solutionFileName2ClusterName.get(s));
		}
		// System.out.println(cliqueCluster.size());
		for (String s : clustersToBreakDown) {
			Map<String, Integer> clusterSpecificConnectivity = new HashMap<String, Integer>();
			for (String fileName : solutionClusterName2FileName.get(s)) {
				clusterSpecificConnectivity.put(fileName, new Integer(0));
			}
			perClusterCliqueConnectivity.put(s, clusterSpecificConnectivity);
		}

		for (String cluster : clustersToBreakDown) {
			for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn
					.entrySet()) {
				for (Entry<String, Set<String>> entityToEntities : completeRelation
						.getValue().entrySet()) {
					for (String s : entityToEntities.getValue()) {
						if (solutionFileName2ClusterName.containsKey(s)
								&& solutionFileName2ClusterName
										.containsKey(entityToEntities.getKey())) {
							if (!solutionFileName2ClusterName.get(s).equals(
									solutionFileName2ClusterName
											.get(entityToEntities.getKey()))
									&& cliqueCluster
											.contains(solutionFileName2ClusterName
													.get(s))
									&& solutionFileName2ClusterName.get(
											entityToEntities.getKey()).equals(
											cluster)) {
								perClusterCliqueConnectivity.get(cluster).put(
										entityToEntities.getKey(),
										perClusterCliqueConnectivity.get(
												cluster).get(
												entityToEntities.getKey()) + 1);
							} else if (!solutionFileName2ClusterName.get(s)
									.equals(solutionFileName2ClusterName
											.get(entityToEntities.getKey()))
									&& cliqueCluster
											.contains(solutionFileName2ClusterName
													.get(entityToEntities
															.getKey()))
									&& solutionFileName2ClusterName.get(s)
											.equals(cluster)) {
								perClusterCliqueConnectivity.get(cluster).put(
										s,
										perClusterCliqueConnectivity.get(
												cluster).get(s) + 1);
							}
						}
					}
				}
			}
			perClusterCliqueConnectivity
					.put(cluster, sortByComparator(perClusterCliqueConnectivity
							.get(cluster)));
		}
		// up to here is OK
		// rearrange part
		Map<String, Map<String, Set<String>>> clusterToSmallerClusters = new HashMap<String, Map<String, Set<String>>>();
		int clusterPartitions = 0;
		for (Entry<String, Map<String, Integer>> cluster : perClusterCliqueConnectivity
				.entrySet()) {
			Map<String, Set<String>> splitClusters = new HashMap<String, Set<String>>();
			clusterPartitions = Math.floorDiv(
					solutionClusterName2FileName.get(cluster.getKey()).size(),
					maxClusterSize) + 1;
			// System.out.println("clusterPartitions = " + clusterPartitions);
			int nonZeroToCliqueConnectivities = 0;
			for (Entry<String, Integer> clusterFileToClique : cluster
					.getValue().entrySet())
				if (clusterFileToClique.getValue() > 0)
					nonZeroToCliqueConnectivities++;
			nonZeroToCliqueConnectivities = Math.floorDiv(
					nonZeroToCliqueConnectivities, clusterPartitions) + 1;
			// System.out.println("nonZeroToCliqueConnectivities = " +
			// nonZeroToCliqueConnectivities + " | clusterPreSplitSize = "
			// + solutionClusterName2FileName.get(cluster.getKey()).size() +
			// " | clusterPartitions = " + clusterPartitions);
			int i = 0;
			Set<String> totalUsed = new HashSet<String>();
			String currentClusterKey = "";
			for (Entry<String, Integer> clusterFileToCluster : cluster
					.getValue().entrySet()) {
				if (i == 0) {
					currentClusterKey = clusterFileToCluster.getKey();
					splitClusters.put(currentClusterKey, new HashSet<String>());
					splitClusters.get(currentClusterKey).add(currentClusterKey);
					i = (i + 1) % nonZeroToCliqueConnectivities;
				} else {
					splitClusters.get(currentClusterKey).add(
							clusterFileToCluster.getKey());
					i = (i + 1) % nonZeroToCliqueConnectivities;
				}
			}
			// System.out.println(cluster.getKey() + " " +
			// splitClusters.keySet().size());
			allSplitClusters.putAll(splitClusters);

			clusterToSmallerClusters.put(cluster.getKey(), splitClusters);
			solutionClusterName2FileName.get(cluster.getKey()).removeAll(
					totalUsed);
		}
		// for (Entry<String, Set<String>> sc : allSplitClusters.entrySet()) {
		// System.out.println("sc = " + sc.getKey() + " -> " +
		// sc.getValue().size());
		// }
		// OK UP TO HERE
		// ~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-Merging
		// Part~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-
		// ~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-~-
		// ======================================Initialization of
		// orphansToClustersConnections======================================
		Map<String, Map<String, Integer>> orphansToClustersConnections = new HashMap<String, Map<String, Integer>>();
		for (String clusterName : clustersToBreakDown) {
			for (String orphanName : solutionClusterName2FileName
					.get(clusterName)) {
				orphansToClustersConnections.put(orphanName,
						new HashMap<String, Integer>());
				for (String splitCluster : clusterToSmallerClusters.get(
						clusterName).keySet()) {
					orphansToClustersConnections.get(orphanName).put(
							splitCluster, new Integer(0));
				}
			}
		}

		// ++++++++++++++++++++++++++++++++++++Calculation of Connections to
		// parts of new Cluster++++++++++++++++++++++++++++++++++++
		for (String clusterName : clustersToBreakDown) {
			for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn
					.entrySet()) {
				for (Entry<String, Set<String>> entityToEntities : completeRelation
						.getValue().entrySet()) {
					for (String s : entityToEntities.getValue()) {
						for (Entry<String, Set<String>> splitCluster : clusterToSmallerClusters
								.get(clusterName).entrySet()) {
							if (solutionClusterName2FileName.get(clusterName)
									.contains(entityToEntities.getKey())
									&& splitCluster.getValue().contains(s)) {
								orphansToClustersConnections.get(
										entityToEntities.getKey()).put(
										splitCluster.getKey(),
										orphansToClustersConnections.get(
												entityToEntities.getKey()).get(
												splitCluster.getKey()) + 1);
							} else {
								if (solutionClusterName2FileName.get(
										clusterName).contains(s)
										&& splitCluster.getValue().contains(
												entityToEntities.getKey())) {
									orphansToClustersConnections
											.get(s)
											.put(splitCluster.getKey(),
													orphansToClustersConnections
															.get(s)
															.get(splitCluster
																	.getKey()) + 1);
								}
							}
						}
					}
				}
			}
		}
		Map<String, Map<String, Integer>> temp = new HashMap<String, Map<String, Integer>>();
		temp.putAll(orphansToClustersConnections);
		for (Entry<String, Map<String, Integer>> e : temp.entrySet()) {
			orphansToClustersConnections.put(e.getKey(),
					sortByComparatorReverse(e.getValue()));
		}

		// -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-Assignment of orphans to
		// designated Clusters-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-
		for (Entry<String, Map<String, Integer>> fileConnections : orphansToClustersConnections
				.entrySet()) {
			for (String targetCluster : fileConnections.getValue().keySet()) {
				if (allSplitClusters.get(targetCluster).size() < 1 * maxClusterSize) {
					allSplitClusters.get(targetCluster).add(
							fileConnections.getKey());
					break;
				}
			}
		}
		// for (Entry<String, Set<String>> cl : allSplitClusters.entrySet())
		// System.out.println(cl.getKey() + " : " + cl.getValue().size());
		return allSplitClusters;
	}

	private void printSolutionRsf(Set<String> SolutionSet,
			Map<String, Map<String, Set<String>>> fileRsfIn, String Bug_ID,
			Map<String, String> entityToId, Set<String> fileInputSet) {
		System.out.println(fileInputSet.size());
		Map<String, Map<String, Set<String>>> fileRsf = new HashMap<String, Map<String, Set<String>>>();
		fileRsf.putAll(fileRsfIn);
		bad.clear();
		bad.add("src/widgets/osd.cpp");
		bad.add("tests/testdebug.cpp");
		bad.add("tests/synchronization/testmasterslavesynchronizationjob.cpp");
		bad.add("tests/synchronization/testunionjob.cpp");
		// bad.add("src/core/support/debug.h");
		// bad.add("src/covermanager/covermanager.cpp");
		// bad.add("src/toolbar/maintoolbar.cpp");
		// bad.add("src/context/applets/albums/albums.cpp");
		solutionClusterName2FileName = new HashMap<String, Set<String>>();
		solutionFileName2ClusterName = new HashMap<String, String>();
		// fileRsf.remove("include");
		// fileRsf.remove("macrouse");
		fileRsf.remove("macrodefinition");
		// fileRsf.remove("sets");
		fileRsf.remove("methodbelongstoclass");
		fileRsf.remove("declaredin");
		fileRsf.remove("hastype");
		fileRsf.remove("entitylocation");
		fileRsf.remove("filebelongstomodule");
		fileRsf.remove("usestype");
		// fileRsf.remove("accessibleentitybelongstofile");
		fileRsf.remove("inheritsfrom");
		// fileRsf.remove("calls");
		fileRsf.remove("classbelongstofile");
		fileRsf.remove("definedin");
		// fileRsf.remove("attributebelongstoclass");
		// fileRsf.remove("accesses");
		Set<String> relations = new HashSet<String>();
		relations.add("calls");
		relations.add("sets");
		relations.add("accesses");
		relations.add("attributebelongstoclass");
		relations.add("accessibleentitybelongstofile");
		try {
			BufferedWriter solutionWriter = new BufferedWriter(new FileWriter(
					new File(Paths.SOLUTION_ACDC_INPUT + Bug_ID
							+ "_sol_final_acdc.rsf")));
			for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsf
					.entrySet()) {
				for (Entry<String, Set<String>> fromEntityToEntity : completeRelation
						.getValue().entrySet()) {
					for (String toEntity : fromEntityToEntity.getValue()) {
						// if
						// (fromEntityToEntity.getKey().contains("sqlmeta.cpp")
						// || toEntity.contains("sqlmeta.cpp"))
						// System.out.println("From " + fromEntityToEntity +
						// " To " + toEntity);
						if (SolutionSet.contains(fromEntityToEntity.getKey())
								&& SolutionSet.contains(toEntity)
								&& !bad.contains(fromEntityToEntity.getKey())
								&& !bad.contains(toEntity))
							solutionWriter.write(completeRelation.getKey()
									+ " "
									+ entityToId.get(fromEntityToEntity
											.getKey()) + " "
									+ entityToId.get(toEntity) + "\n");
					}
				}
			}
			Set<String> inFileInputSet = new HashSet<String>();
			inFileInputSet.addAll(fileInputSet);
			for (String s : fileInputSet) {
				inFileInputSet.remove(s);
				for (String ss : inFileInputSet) {
					// for (int i = 0; i < 500; i++)
					for (String sss : relations) {
						solutionWriter.write(sss + " " + entityToId.get(s)
								+ " " + entityToId.get(ss) + "\n");
						solutionWriter.write(sss + " " + entityToId.get(ss)
								+ " " + entityToId.get(s) + "\n");
					}
				}
				inFileInputSet.add(s);
			}

			solutionWriter.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		ThreadTest.SolutionClustering(Bug_ID + "_sol_final_acdc.rsf");

		try {
			BufferedReader solutionClusterReader = new BufferedReader(
					new FileReader(Paths.SOLUTION_CLUSTERS + Bug_ID
							+ "_sol_final_acdc.rsf"));
			while (solutionClusterReader.ready()) {
				String[] line = solutionClusterReader.readLine().split(" ");
				String clusterName = line[1].split("\\.")[0];
				if (!line[1].contains("orphanContainer")) {
					clusterName = fileRsfMaker.getIdEntity().get(
							line[1].split("\\.")[0]);
				} else
					clusterName = "orphanContainer";
				// System.out.println(clusterName);
				String fileName = fileRsfMaker.getIdEntity().get(line[2]);
				// System.out.println(fileName);
				if (solutionClusterName2FileName.containsKey(clusterName)) {
					solutionClusterName2FileName.get(clusterName).add(fileName);
				} else {
					solutionClusterName2FileName.put(clusterName,
							new HashSet<String>());
					solutionClusterName2FileName.get(clusterName).add(fileName);
				}
				if (!solutionFileName2ClusterName.containsKey(fileName))
					solutionFileName2ClusterName.put(fileName, clusterName);
			}
			Set<String> libraryFiles = new HashSet<String>();
			for (Entry<String, Set<String>> cluster : solutionClusterName2FileName
					.entrySet()) {
				for (String fileName : cluster.getValue()) {
					if (fileName.contains(".h"))
						libraryFiles.add(fileName);
				}
				cluster.getValue().removeAll(libraryFiles);
				libraryFiles.clear();
			}
			for (Entry<String, String> toCluster : solutionFileName2ClusterName
					.entrySet())
				if (toCluster.getKey().contains(".h"))
					libraryFiles.add(toCluster.getKey());
			for (String s : libraryFiles)
				solutionFileName2ClusterName.remove(s);
			solutionClusterReader.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		// for (Entry<String, Set<String>> cluster :
		// solutionClusterName2FileName.entrySet())
		// if (cluster.getValue().size() > 50)
		// System.out.println(cluster.getKey() + " : " +
		// cluster.getValue().size());
	}

	private Map<String, Map<String, Integer>> countInterClusterRelations(
			Map<String, Map<String, Set<String>>> fileRsfIn,
			Set<String> goodClusters) {
		Map<String, Map<String, Set<String>>> fileRsf = new HashMap<String, Map<String, Set<String>>>();
		Set<String> validFiles = new HashSet<String>();
		Map<String, Map<String, Integer>> weights = new HashMap<String, Map<String, Integer>>();
		validFiles.addAll(solutionFileName2ClusterName.keySet());
		fileRsf.putAll(fileRsfIn);
		fileRsf.remove("include");
		fileRsf.remove("macrouse");
		fileRsf.remove("macrodefinition");
		// fileRsf.remove("sets");
		fileRsf.remove("methodbelongstoclass");
		fileRsf.remove("declaredin");
		fileRsf.remove("hastype");
		fileRsf.remove("entitylocation");
		fileRsf.remove("filebelongstomodule");
		fileRsf.remove("usestype");
		fileRsf.remove("accessibleentitybelongstofile");
		fileRsf.remove("inheritsfrom");
		// fileRsf.remove("calls");
		fileRsf.remove("classbelongstofile");
		fileRsf.remove("definedin");
		fileRsf.remove("attributebelongstoclass");
		// fileRsf.remove("accesses");
		for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsf
				.entrySet()) {
			for (Entry<String, Set<String>> entityToEntities : completeRelation
					.getValue().entrySet()) {
				for (String toEntity : entityToEntities.getValue()) {
					if (validFiles.contains(entityToEntities.getKey())
							&& validFiles.contains(toEntity)) {
						if (goodClusters.contains(solutionFileName2ClusterName
								.get(entityToEntities.getKey())))
							if (weights
									.containsKey(solutionFileName2ClusterName
											.get(entityToEntities.getKey()))) {
								if (weights
										.get(solutionFileName2ClusterName
												.get(entityToEntities.getKey()))
										.containsKey(
												solutionFileName2ClusterName
														.get(toEntity))) {
									Integer cur = weights.get(
											solutionFileName2ClusterName
													.get(entityToEntities
															.getKey())).get(
											solutionFileName2ClusterName
													.get(toEntity));
									cur += 1;
									weights.get(
											solutionFileName2ClusterName
													.get(entityToEntities
															.getKey()))
											.put(solutionFileName2ClusterName
													.get(toEntity),
													cur);
								} else {
									weights.get(
											solutionFileName2ClusterName
													.get(entityToEntities
															.getKey()))
											.put(solutionFileName2ClusterName
													.get(toEntity),
													new Integer(1));
								}
							} else {
								weights.put(solutionFileName2ClusterName
										.get(entityToEntities.getKey()),
										new HashMap<String, Integer>());
								weights.get(
										solutionFileName2ClusterName
												.get(entityToEntities.getKey()))
										.put(solutionFileName2ClusterName
												.get(toEntity),
												new Integer(1));
							}
						else if (weights
								.containsKey(solutionFileName2ClusterName
										.get(toEntity))) {
							if (weights.get(
									solutionFileName2ClusterName.get(toEntity))
									.containsKey(
											solutionFileName2ClusterName
													.get(entityToEntities
															.getKey()))) {
								Integer cur = weights
										.get(solutionFileName2ClusterName
												.get(toEntity))
										.get(solutionFileName2ClusterName
												.get(entityToEntities.getKey()));
								cur += 1;
								weights.get(
										solutionFileName2ClusterName
												.get(toEntity))
										.put(solutionFileName2ClusterName.get(entityToEntities
												.getKey()), cur);
							} else {
								weights.get(
										solutionFileName2ClusterName
												.get(toEntity))
										.put(solutionFileName2ClusterName.get(entityToEntities
												.getKey()), new Integer(1));
							}
						} else {
							weights.put(
									solutionFileName2ClusterName.get(toEntity),
									new HashMap<String, Integer>());
							weights.get(
									solutionFileName2ClusterName.get(toEntity))
									.put(solutionFileName2ClusterName.get(entityToEntities
											.getKey()), new Integer(1));
						}
					}
				}
			}
		}
		return weights;
	}

	private Set<String> selectClusters(
			Map<String, Map<String, Integer>> clusterConnectionWeights,
			Set<String> inputClusters) {
		Map<String, Integer> perClusterGain = new HashMap<String, Integer>();
		Set<String> finalClusters = new HashSet<String>();
		for (Entry<String, Map<String, Integer>> oneClusterWeight : clusterConnectionWeights
				.entrySet()) {
			for (Entry<String, Integer> clusterWeights : oneClusterWeight
					.getValue().entrySet()) {
				if (perClusterGain.containsKey(clusterWeights.getKey()))
					perClusterGain
							.put(clusterWeights.getKey(),
									(perClusterGain.get(clusterWeights.getKey()) + clusterWeights
											.getValue()));
				else
					perClusterGain.put(clusterWeights.getKey(),
							clusterWeights.getValue());
			}
		}
		int sum = 0;
		double average = 0;
		for (Entry<String, Integer> clusterWeight : perClusterGain.entrySet()) {
			// System.out.println(clusterWeight.getKey() + " : " +
			// clusterWeight.getValue());
			sum += clusterWeight.getValue();
		}
		average = sum / (double) perClusterGain.size();
		for (Entry<String, Integer> clusterEntry : perClusterGain.entrySet())
			if (clusterEntry.getValue() < average) {
				finalClusters.add(clusterEntry.getKey());
			}
		finalClusters.addAll(inputClusters);
		return finalClusters;
	}

	private Set<String> selectMostConnected(
			Map<String, Map<String, Set<String>>> fileRsfIn,
			Set<String> candidateForConnection) {
		Set<String> mostConnectedFiles = new HashSet<String>();
		Map<String, Integer> fileConnectivity = new HashMap<String, Integer>();
		Set<String> finalVi = new HashSet<String>();
		// finalVi.addAll(candidateForConnection);
		for (String s : candidateForConnection)
			if (!s.endsWith(".h"))
				finalVi.add(s);
		System.out.println("Starting with " + candidateForConnection.size()
				+ " files, after removing .h I am left with " + finalVi.size()
				+ " files");
		for (String fileName : finalVi) {
			fileConnectivity.put(fileName, new Integer(0));
		}

		for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn
				.entrySet()) {
			for (Entry<String, Set<String>> fromEntityToEntities : completeRelation
					.getValue().entrySet()) {
				for (String toEntity : fromEntityToEntities.getValue()) {
					if (finalVi.contains(fromEntityToEntities.getKey()))
						fileConnectivity.put(fromEntityToEntities.getKey(),
								fileConnectivity.get(fromEntityToEntities
										.getKey()) + 1);
					if (finalVi.contains(toEntity))
						fileConnectivity.put(toEntity,
								fileConnectivity.get(toEntity) + 1);
				}
			}
		}

		fileConnectivity = sortByComparator(fileConnectivity);
		int i = fileConnectivity.size();
		for (Entry<String, Integer> forfile : fileConnectivity.entrySet()) {
			i--;
			if (i <= fileConnectivity.size() * 0.1 && i <= 20)
				mostConnectedFiles.add(forfile.getKey());
		}
		return mostConnectedFiles;
	}

	private void toFromCliqueConnectivity(
			Map<String, Map<String, Set<String>>> fileRsfIn,
			Set<String> clique, Set<String> GSClusters) {
		for (String s : unusedRelations)
			fileRsfIn.remove(s);
		Set<String> cliqueCluster = new HashSet<String>();
		for (String s : clique) {
			cliqueCluster.add(solutionFileName2ClusterName.get(s));
		}
		// System.out.println(cliqueCluster);
		// *****************************************************ClusterToCliqueConnectivity************************************************************
		// --------------------------------------------------------------------------------------------------------------------------------------------
		// --------------------------------------------------------------------------------------------------------------------------------------------
		// --------------------------------------------------------------------------------------------------------------------------------------------
		Map<String, Integer> cliqueClusterConnectivity = new HashMap<String, Integer>();
		for (String clusterName : solutionClusterName2FileName.keySet())
			cliqueClusterConnectivity.put(clusterName, new Integer(0));
		for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn
				.entrySet()) {
			for (Entry<String, Set<String>> entityToEntities : completeRelation
					.getValue().entrySet()) {
				for (String s : entityToEntities.getValue()) {
					if (solutionFileName2ClusterName.containsKey(s)
							&& solutionFileName2ClusterName
									.containsKey(entityToEntities.getKey()))
						if (!solutionFileName2ClusterName.get(s).equals(
								solutionFileName2ClusterName
										.get(entityToEntities.getKey()))
								&& cliqueCluster
										.contains(solutionFileName2ClusterName
												.get(s))) {
							cliqueClusterConnectivity.put(
									solutionFileName2ClusterName
											.get(entityToEntities.getKey()),
									cliqueClusterConnectivity
											.get(solutionFileName2ClusterName
													.get(entityToEntities
															.getKey())) + 1);
							cliqueClusterConnectivity.put(
									solutionFileName2ClusterName.get(s),
									cliqueClusterConnectivity
											.get(solutionFileName2ClusterName
													.get(s)) + 1);

						} else {
							if (!solutionFileName2ClusterName.get(s).equals(
									solutionFileName2ClusterName
											.get(entityToEntities.getKey()))
									&& cliqueCluster
											.contains(solutionFileName2ClusterName
													.get(entityToEntities
															.getKey()))) {
								cliqueClusterConnectivity
										.put(solutionFileName2ClusterName
												.get(s),
												cliqueClusterConnectivity
														.get(solutionFileName2ClusterName
																.get(s)) + 1);
								cliqueClusterConnectivity
										.put(solutionFileName2ClusterName
												.get(entityToEntities.getKey()),
												cliqueClusterConnectivity
														.get(solutionFileName2ClusterName
																.get(entityToEntities
																		.getKey())) + 1);
							}
						}
				}
			}
		}
		averageToCliqueCon.clear();
		for (Entry<String, Integer> cluster : cliqueClusterConnectivity
				.entrySet()) {
			averageToCliqueCon.put(
					cluster.getKey(),
					cluster.getValue()
							/ (double) solutionClusterName2FileName.get(
									cluster.getKey()).size());
		}

		// *********************************************************ClusterConnectivity****************************************************************
		// --------------------------------------------------------------------------------------------------------------------------------------------
		// --------------------------------------------------------------------------------------------------------------------------------------------
		// --------------------------------------------------------------------------------------------------------------------------------------------
		Map<String, Integer> clusterConnectivity = new HashMap<String, Integer>();
		for (String clusterName : solutionClusterName2FileName.keySet())
			clusterConnectivity.put(clusterName, new Integer(0));
		for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn
				.entrySet()) {
			for (Entry<String, Set<String>> entityToEntities : completeRelation
					.getValue().entrySet()) {
				for (String s : entityToEntities.getValue()) {
					if (solutionFileName2ClusterName.containsKey(s)
							&& solutionFileName2ClusterName
									.containsKey(entityToEntities.getKey())) {
						// if (solutionFileName2ClusterName.containsKey(s)) {
						// if
						// (!solutionFileName2ClusterName.get(s).equals(solutionFileName2ClusterName.get(entityToEntities.getKey())))
						// {
						clusterConnectivity
								.put(solutionFileName2ClusterName
										.get(entityToEntities.getKey()),
										clusterConnectivity
												.get(solutionFileName2ClusterName
														.get(entityToEntities
																.getKey())) + 1);
						clusterConnectivity.put(solutionFileName2ClusterName
								.get(s), clusterConnectivity
								.get(solutionFileName2ClusterName.get(s)) + 1);

					}
					// if
					// (solutionFileName2ClusterName.containsKey(entityToEntities.getKey()))
					// {
					// clusterConnectivity.put(solutionFileName2ClusterName.get(entityToEntities.getKey()),
					// clusterConnectivity.get(solutionFileName2ClusterName.get(entityToEntities.getKey()))
					// + 1);
					// }
				}
			}
		}
		averagePerFileCon.clear();
		for (Entry<String, Integer> cluster : clusterConnectivity.entrySet()) {
			averagePerFileCon.put(
					cluster.getKey(),
					cluster.getValue()
							/ (double) solutionClusterName2FileName.get(
									cluster.getKey()).size());
		}
		// *******************************************************FileToCliqueConnectivity*************************************************************
		// --------------------------------------------------------------------------------------------------------------------------------------------
		// --------------------------------------------------------------------------------------------------------------------------------------------
		// --------------------------------------------------------------------------------------------------------------------------------------------
		fileToCliqueClusterConnectivity = new HashMap<String, Double>();
		for (String fileName : solutionFileName2ClusterName.keySet()) {
			fileToCliqueClusterConnectivity.put(fileName, new Double(0.0));
		}
		for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn
				.entrySet()) {
			for (Entry<String, Set<String>> entityToEntities : completeRelation
					.getValue().entrySet()) {
				for (String s : entityToEntities.getValue()) {
					if (solutionFileName2ClusterName.containsKey(s)
							&& solutionFileName2ClusterName
									.containsKey(entityToEntities.getKey()))
						if (!solutionFileName2ClusterName.get(s).equals(
								solutionFileName2ClusterName
										.get(entityToEntities.getKey()))
								&& cliqueCluster
										.contains(solutionFileName2ClusterName
												.get(s)))
							fileToCliqueClusterConnectivity
									.put(entityToEntities.getKey(),
											fileToCliqueClusterConnectivity
													.get(entityToEntities
															.getKey()) + 1);
						else if (!solutionFileName2ClusterName.get(s).equals(
								solutionFileName2ClusterName
										.get(entityToEntities.getKey()))
								&& cliqueCluster
										.contains(solutionFileName2ClusterName
												.get(entityToEntities.getKey())))
							fileToCliqueClusterConnectivity.put(s,
									fileToCliqueClusterConnectivity.get(s) + 1);

				}
			}
		}
		fileToCliqueClusterConnectivity = sortByComparatorDouble(fileToCliqueClusterConnectivity);
		// System.out
		// .println("***************************************************fileToCliqueClustersConnectivity***************************************************");
		// for (Entry<String, Double> e :
		// fileToCliqueClusterConnectivity.entrySet())
		// if (!answer.contains(e.getKey()))
		// System.out.format("%85s\t%15f\n", e.getKey(), e.getValue());
		// else
		// System.out.format("%85s\t%15f <-------------\n", e.getKey(),
		// e.getValue());
		// *****************************************************FileToCliqueClusterConnectivity********************************************************
		// --------------------------------------------------------------------------------------------------------------------------------------------
		// --------------------------------------------------------------------------------------------------------------------------------------------
		// --------------------------------------------------------------------------------------------------------------------------------------------
		for (String fileName : solutionFileName2ClusterName.keySet()) {
			fileToCliqueConnectivity.put(fileName, new Double(0.0));
		}
		for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn
				.entrySet()) {
			for (Entry<String, Set<String>> entityToEntities : completeRelation
					.getValue().entrySet()) {
				for (String s : entityToEntities.getValue()) {
					if (solutionFileName2ClusterName.containsKey(s)
							&& solutionFileName2ClusterName
									.containsKey(entityToEntities.getKey()))
						if (!solutionFileName2ClusterName.get(s).equals(
								solutionFileName2ClusterName
										.get(entityToEntities.getKey()))
								&& clique.contains(s))
							fileToCliqueConnectivity.put(entityToEntities
									.getKey(), fileToCliqueConnectivity
									.get(entityToEntities.getKey()) + 1);
						else if (!solutionFileName2ClusterName.get(s).equals(
								solutionFileName2ClusterName
										.get(entityToEntities.getKey()))
								&& clique.contains(entityToEntities.getKey()))
							fileToCliqueConnectivity.put(s,
									fileToCliqueConnectivity.get(s) + 1);

				}
			}
		}
		fileToCliqueConnectivity = sortByComparatorDouble(fileToCliqueConnectivity);
		// System.out
		// .println("***************************************************fileToCliqueConnectivity***************************************************");
		// for (Entry<String, Double> e : fileToCliqueConnectivity.entrySet())
		// if (!answer.contains(e.getKey()))
		// System.out.format("%85s\t%15f\n", e.getKey(), e.getValue());
		// else
		// System.out.format("%85s\t%15f <-------------\n", e.getKey(),
		// e.getValue());
	}

	private Set<String> SelectInGSValues(
			Map<String, Map<String, Set<String>>> fileRsfIn,
			Set<String> GSClusters) {
		// Removes UnusedRelations
		for (String s : unusedRelations)
			fileRsfIn.remove(s);
		Set<String> mostConnectedFiles = new HashSet<String>();
		int sum = 0;
		// double average;
		Map<String, Integer> fileConnectivity = new HashMap<String, Integer>();
		for (String fileName : finalV) {
			fileConnectivity.put(fileName, new Integer(0));
		}
		for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfIn
				.entrySet()) {
			for (Entry<String, Set<String>> fromEntityToEntities : completeRelation
					.getValue().entrySet()) {
				for (String toEntity : fromEntityToEntities.getValue()) {
					// if (!clique.contains(fromEntityToEntities.getKey()) &&
					// clique.contains(toEntity)
					// || clique.contains(fromEntityToEntities.getKey()) &&
					// !clique.contains(toEntity)) {
					if (finalV.contains(fromEntityToEntities.getKey())) {
						if (completeRelation.getKey().equals("calls"))
							fileConnectivity.put(fromEntityToEntities.getKey(),
									fileConnectivity.get(fromEntityToEntities
											.getKey()) + 1);
						if (completeRelation.getKey().equals("sets"))
							fileConnectivity.put(fromEntityToEntities.getKey(),
									fileConnectivity.get(fromEntityToEntities
											.getKey()) + 1);
						if (completeRelation.getKey().equals("accesses"))
							fileConnectivity.put(fromEntityToEntities.getKey(),
									fileConnectivity.get(fromEntityToEntities
											.getKey()) + 1);
					}
					if (finalV.contains(toEntity)) {
						if (completeRelation.getKey().equals("calls"))
							fileConnectivity.put(toEntity,
									fileConnectivity.get(toEntity) + 1);
						if (completeRelation.getKey().equals("sets"))
							fileConnectivity.put(toEntity,
									fileConnectivity.get(toEntity) + 1);
						if (completeRelation.getKey().equals("accesses"))
							fileConnectivity.put(toEntity,
									fileConnectivity.get(toEntity) + 1);
						// }
					}
				}
			}
		}
		for (Entry<String, Integer> forfile : fileConnectivity.entrySet())
			sum += forfile.getValue();
		// average = sum / (double) fileConnectivity.size();
		fileConnectivity = sortByComparator(fileConnectivity);
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(
				fileConnectivity.entrySet());
		// int i = fileConnectivity.size();
		sum = 0;
		for (Entry<String, Integer> forfile : fileConnectivity.entrySet()) {
			if (GSClusters.contains(solutionFileName2ClusterName.get(forfile
					.getKey()))) {
				if (solutionFileName2ClusterName.get(forfile.getKey()) != null
						&& !forfile.getKey().contains(".h")) {
					sum += forfile.getValue();
					// System.out.format("%70s\t%10d\tposition = %10d\n",
					// forfile.getKey(), forfile.getValue(),
					// (list.size() - list.indexOf(forfile) + 1));
				}
			}
			// mostConnectedFiles.add(forfile.getKey());
		}
		Set<String> selectors = new HashSet<String>();
		for (Entry<String, Integer> entry : list) {
			if (list.indexOf(entry) < 0.3 * list.size())
				selectors.add(entry.getKey());
		}
		printSumOfFileConnectivityInGsClusters(fileConnectivity);
		for (String s : selectors) {
			if (solutionFileName2ClusterName.containsKey(s))
				mostConnectedFiles.addAll(solutionClusterName2FileName
						.get(solutionFileName2ClusterName.get(s)));
		}
		// System.out.println("----^^^^****----^^^^****----^^^^****" +
		// solutionClusterName2FileName.size());
		// System.out.println("****----****----****----****----" + sum);
		return mostConnectedFiles;
	}

	private void printSumOfFileConnectivityInGsClusters(
			Map<String, Integer> fileConnectivity) {
		Set<String> gsClusters = new HashSet<String>();
		Map<String, Integer> clusterConnectivitySum = new HashMap<String, Integer>();
		for (String gsToken : answer) {
			gsClusters.add(solutionFileName2ClusterName.get(gsToken));
		}
		for (String s : solutionClusterName2FileName.keySet()) {
			clusterConnectivitySum.put(s, new Integer(0));
		}

		for (Entry<String, Integer> file : fileConnectivity.entrySet()) {
			if (solutionFileName2ClusterName.containsKey(file.getKey())) {
				// System.out.println(solutionFileName2ClusterName.get(file.getKey())
				// + " - > " + file.getValue());
				// if
				// (gsClusters.contains(solutionFileName2ClusterName.get(file.getKey())))
				// {
				clusterConnectivitySum.put(
						solutionFileName2ClusterName.get(file.getKey()),
						clusterConnectivitySum.get(solutionFileName2ClusterName
								.get(file.getKey())) + file.getValue());
				// }
			}
		}
		clusterConnectivitySum = sortByComparator(clusterConnectivitySum);
		averagePerFileCon.clear();
		for (Entry<String, Integer> cluster : clusterConnectivitySum.entrySet()) {
			averagePerFileCon.put(
					cluster.getKey(),
					cluster.getValue()
							/ (double) solutionClusterName2FileName.get(
									cluster.getKey()).size());
		}

		// for (Entry<String, Integer> cluster :
		// clusterConnectivitySum.entrySet()) {
		// if (!gsClusters.contains(cluster.getKey()))
		// System.out.format("%80s\t%10d\t%10d\t%5f\n", cluster.getKey(),
		// solutionClusterName2FileName.get(cluster.getKey()).size(),
		// cluster.getValue(), cluster.getValue() / (double)
		// solutionClusterName2FileName.get(cluster.getKey()).size());
		// else
		// System.out.format("%80s\t%10d\t%10d\t%5f <-------------------\n",
		// cluster.getKey(),
		// solutionClusterName2FileName.get(cluster.getKey()).size(),
		// cluster.getValue(), cluster.getValue()
		// / (double)
		// solutionClusterName2FileName.get(cluster.getKey()).size());
		// }

	}

	private Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(
				unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it
				.hasNext();) {
			Map.Entry<String, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	private Map<String, Integer> sortByComparatorReverse(
			Map<String, Integer> unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(
				unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				return -(o1.getValue()).compareTo(o2.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it
				.hasNext();) {
			Map.Entry<String, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	private void makeTempIDs(Set<String> AllFiles) {
		Set<String> a = new TreeSet<String>();
		a.addAll(AllFiles);
		AllFiles.clear();
		AllFiles.addAll(a);
		Integer ID = 0;
		for (String s : a) {
			NameToId.put(s, ID);
			IdToName.put(ID, s);
			ID = ID + 1;
		}
		System.out.println("ID = " + ID);
		return;
	}

	private Map<String, Double> sortByComparatorDouble(
			Map<String, Double> unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(
				unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			@Override
			public int compare(Map.Entry<String, Double> o1,
					Map.Entry<String, Double> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it
				.hasNext();) {
			Map.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	private void clearSets() {
		answer.clear();
		lsaTokens.clear();
		inputTokens.clear();
	}

	private Set<String> expandeTokenSet(Set<String> tokenSetForExpansion) {
		Map<String, Map<String, Set<String>>> allRelsLocal = new HashMap<String, Map<String, Set<String>>>();
		Set<String> localAnswer = new HashSet<String>();
		Set<String> Visited = new HashSet<String>();
		Set<String> currentFrontier = new HashSet<String>();
		Set<String> nextFrontier = new HashSet<String>();
		Set<String> toAddToVisited = new HashSet<String>();
		Set<String> toRemoveFromAnswer = new HashSet<String>();
		// prepare allRelsLocal Map containing all relations both forward and
		// backwards
		allRelsLocal.putAll(allFileRelationsReversed);
		for (Entry<String, Map<String, Set<String>>> completeRelation : fileRsfMaker
				.getAllFileRelations().entrySet()) {
			for (Entry<String, Set<String>> entityToEntities : completeRelation
					.getValue().entrySet()) {
				if (allRelsLocal.get(completeRelation.getKey()).containsKey(
						entityToEntities.getKey()))
					allRelsLocal.get(completeRelation.getKey())
							.get(entityToEntities.getKey())
							.addAll(entityToEntities.getValue());
				else
					allRelsLocal.get(completeRelation.getKey()).put(
							entityToEntities.getKey(),
							entityToEntities.getValue());

			}
		}
		// Eliminate from the map all the Uncommented Relations Usually all of
		// them except "sets", "accesses" and "calls"
		// allRelsLocal.remove("include");
		// allRelsLocal.remove("macrouse");
		// allRelsLocal.remove("macrodefinition");
		// allRelsLocal.remove("sets");
		// allRelsLocal.remove("methodbelongstoclass");
		// allRelsLocal.remove("declaredin");
		// allRelsLocal.remove("hastype");
		// allRelsLocal.remove("entitylocation");
		// allRelsLocal.remove("filebelongstomodule");
		// allRelsLocal.remove("usestype");
		// allRelsLocal.remove("accessibleentitybelongstofile");
		// allRelsLocal.remove("inheritsfrom");
		// allRelsLocal.remove("calls");
		// allRelsLocal.remove("classbelongstofile");
		// allRelsLocal.remove("definedin");
		// allRelsLocal.remove("attributebelongstoclass");
		// allRelsLocal.remove("accesses");

		// Copy the answer Set to a localAnswer set
		for (String s : answer)
			if (!s.contains(".h"))
				localAnswer.add(s);
		answer.retainAll(localAnswer);

		// Initialize the search frontier for the DFS expansion algorithm

		nextFrontier.addAll(fileLsaTokens);
		currentFrontier.addAll(nextFrontier);
		nextFrontier.clear();
		localAnswer.removeAll(fileLsaTokens);
		Visited.addAll(currentFrontier);
		int step = 0;
		while (!currentFrontier.isEmpty()
				&& /* !localAnswer.isEmpty() */step < 1) {
			for (Entry<String, Map<String, Set<String>>> completeRelation : allRelsLocal
					.entrySet()) {
				for (String s : currentFrontier) {
					if (completeRelation.getValue().containsKey(s)) {
						for (String target : completeRelation.getValue().get(s)) {
							if (!Visited.contains(target)
									&& !currentFrontier.contains(target))
								nextFrontier.add(target);
							if (localAnswer.contains(target))
								toRemoveFromAnswer.add(target);
							toAddToVisited.add(target);
						}
					}
				}
			}
			localAnswer.removeAll(toRemoveFromAnswer);
			toRemoveFromAnswer.clear();
			step++;
			currentFrontier.clear();
			currentFrontier.addAll(nextFrontier);
			nextFrontier.clear();
			Visited.addAll(toAddToVisited);
			toAddToVisited.clear();
		}

		System.out.println("Total expansion Steps = " + step);
		return Visited;
	}

}
