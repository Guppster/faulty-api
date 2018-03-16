package gr.ntua.softlab.solutionspace;

import gr.ntua.softlab.filepaths.Paths;
import gr.ntua.softlab.rsfrepresentation.RsfRepresentation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class InputReader{
	@SuppressWarnings("unused")
	private String BugReport;
	private final String productName;
	private String[] filenames;
	private String[] BugReports;
	private ArrayList<Integer> BugNumbers;
	private final RsfRepresentation rsfRepresentation;
	private int i = -1;
	private String filename;
	private ArrayList<String> lsaTokens;
	private Set<String> answer = new HashSet<>();
    private final Set<String> inputTokens = new HashSet<>();
    private Map<String, String> fileName2Cluster;
	private Map<String, Set<String>> clusterName2fileName;

	public InputReader(String productName, RsfRepresentation rsfRepresentation){
		this.productName = productName;
		this.rsfRepresentation = rsfRepresentation;
		FetchBugNumbers();
		makeNameArrays(BugNumbers);
		readClusters();
	}

	private Set<String> CompileAnswers(int Bug_ID){
		BufferedReader ansbr;
		File answers = new File(Paths.GOLDSTANDARD + productName + "/" + BugNumbers.get(Bug_ID) + "_sol.txt");
		answer = new HashSet<>();
		try {
			ansbr = new BufferedReader(new FileReader(answers));
			while (ansbr.ready()) {
				String s = ansbr.readLine().toLowerCase();
				if (rsfRepresentation.isEntity(s))
					answer.add(s);
			}
			ansbr.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		return answer;
	}

	private void makeNameArrays(ArrayList<Integer> BugNumbers){
		filenames = new String[BugNumbers.size()];
		BugReports = new String[BugNumbers.size()];
		Collections.sort(BugNumbers);
		int i = 0;
		for (Integer I : BugNumbers) {
			filenames[i] = I + "_LSA.txt";
			BugReports[i] = I + ".txt";
			i++;
		}
	}

	public String getBugNumber(){
		return BugReport.split("\\.")[0];
	}

	private void FetchBugNumbers(){
		BugNumbers = new ArrayList<>();
		try {
			File BugIDs = new File(Paths.BRLists + productName + ".lst");
			BufferedReader bugIDr = new BufferedReader(new FileReader(BugIDs));
			while (bugIDr.ready()) {
				int buffer = Integer.parseInt(bugIDr.readLine());
				BugNumbers.add(buffer);
			}
			bugIDr.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}

	private void readLsaInput(){
		answer = CompileAnswers(i);
		try {
			BufferedReader lsaInputReader = new BufferedReader(new FileReader(Paths.FINALINPUTBRS + productName + "/input/" + filename));
			lsaTokens = new ArrayList<>();
			String Entity;
			while (lsaInputReader.ready()) {
				Entity = lsaInputReader.readLine();
				lsaTokens.add(Entity);
			}
			lsaInputReader.close();
			for (String s : answer) {
				lsaTokens.remove(s);
			}
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}

	private void readInputBugReport(){
		try {
			BufferedReader inputBugReportReader = new BufferedReader(new FileReader(Paths.QUERYBRS + productName + "/" + BugReport));
			while (inputBugReportReader.ready())
				inputTokens.add(inputBugReportReader.readLine());
			inputTokens.removeAll(answer);
			inputBugReportReader.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}

	public boolean nextReport(){
		if (i < BugReports.length - 1) {
			i++;
			BugReport = BugReports[i];
			filename = filenames[i];
			System.out.println(filename);
			answer.clear();
			answer.addAll(CompileAnswers(i));
			readLsaInput();
			readInputBugReport();
			return true;
		}
		return false;
	}

	private void readClusters(){
		fileName2Cluster = new HashMap<>();
		clusterName2fileName = new HashMap<>();
		try {
			BufferedReader clustersReader = new BufferedReader(
					new FileReader(Paths.ACDC_OUTPUT_PATH + productName + "_file_final_acdc.rsf"));
			while (clustersReader.ready()) {
				String[] tokens = clustersReader.readLine().split(" ");
				fileName2Cluster.put(rsfRepresentation.getName(tokens[2]), rsfRepresentation.getName(tokens[1].split("\\.")[0]));
			}
			for (Entry<String, String> file2Cluster : fileName2Cluster.entrySet()) {
				if (clusterName2fileName.containsKey(file2Cluster.getValue()))
					clusterName2fileName.get(file2Cluster.getValue()).add(file2Cluster.getKey());
				else {
					Set<String> toInsert = new HashSet<>();
					toInsert.add(file2Cluster.getKey());
					clusterName2fileName.put(file2Cluster.getValue(), toInsert);
				}
			}
			clustersReader.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}

	public Set<String> getLsaTokens(){
		return new HashSet<>(lsaTokens);
	}

	public Set<String> getAnswer(){
		return answer;
	}

	public String getFilename(){
		return filename;
	}

	public ArrayList<Integer> getBugNumbers(){
		return BugNumbers;
	}

	public String[] getBugReports(){
		return BugReports;
	}

	public String[] getFileNames(){
		return filenames;
	}

	public Set<String> getInputTokens(){
		return inputTokens;
	}

	public Map<String, String> getFileNames2Clusters(){
		return fileName2Cluster;
	}

	public Map<String, Set<String>> getClusterName2FileName(){
		return clusterName2fileName;
	}
}
