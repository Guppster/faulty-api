package bugReport.fetch.nlpProcess;

import gr.ntua.softlab.filepaths.Paths;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import NLPTool.NLPProcess;

import com.mongodb.DBObject;

public class CommentMapperAndNLPProcessor{
	private Map<Integer, Map<Integer, List<String>>> ListTokens = new HashMap<Integer, Map<Integer, List<String>>>();
	private Map<Integer, List<String>> ReportCommentsPerID = new HashMap<Integer, List<String>>();
	private DBObject[] BunchOfReports = null;
	private String ProductName = null;
	private Boolean Deserialization_Flag = false;

	public CommentMapperAndNLPProcessor(String Product){
		ProductName = Product;
	}

	/**
	 * 
	 * @param Reports
	 *            an DBObject[] type parameter containing all reports that we want to process with NLP
	 * @param Product
	 *            a String containing the name of the product
	 */
	public CommentMapperAndNLPProcessor(DBObject[] Reports, String Product){
		BunchOfReports = Reports;
		ProductName = Product;
	}

	public void setReports(DBObject[] Reports){
		BunchOfReports = Reports;
	}

	// Checked ?
	/**
	 * InsertCommentToMap() is Called from
	 */
	private void InsertCommentsToMap(){
		JSONObject result = null;
		if (BunchOfReports.length >= 1) {
			for (DBObject r : BunchOfReports) {
				try {
					result = new JSONObject(new JSONTokener(r.toString()));
					// System.out.println(result.getJSONArray("comments").getJSONObject(0).getString("text"));
				} catch (JSONException e) {
					System.out.println(e.getMessage());
				}
				List<String> CommentsList = new ArrayList<String>();
				JSONArray Comments = null;
				int BugID;
				try {
					if (result.has("comments")) {
						Comments = result.getJSONArray("comments");
						BugID = result.getJSONArray("bugs").getJSONObject(0).getInt("id");
						// System.out.println(BugID);
						for (int i = 0; i < Comments.length(); i++) {
							String CommentBuffer = Comments.getJSONObject(i).getString("text");
							CommentsList.add(CommentBuffer);
						}
						ReportCommentsPerID.put(new Integer(BugID), CommentsList);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} else {
			return;
		}
		return;
	}

	private void NLPProcess(){
		NLPProcess nlpp = new NLPProcess(ProductName);
		Iterator<Entry<Integer, List<String>>> IReportCommentsPerID = ReportCommentsPerID.entrySet().iterator();
		while (IReportCommentsPerID.hasNext()) {
			int CommentNumber = 1;
			Entry<Integer, List<String>> EReportCommentsPerID = IReportCommentsPerID.next();
			ListTokens.put(EReportCommentsPerID.getKey(), new HashMap<Integer, List<String>>());
			// System.out.println("processing BugID No: " + EReportCommentsPerID.getKey());
			for (String s : EReportCommentsPerID.getValue()) {
				nlpp.clearCommentBasket();
				nlpp.Process(s);
				if (!ListTokens.get(EReportCommentsPerID.getKey()).containsKey((Integer) CommentNumber)) {
					ListTokens.get(EReportCommentsPerID.getKey()).put((Integer) CommentNumber, new ArrayList<String>());
				}
				// System.out.println(s);
				// System.out.println(nlpp.getCommentBasket());
				ListTokens.get(EReportCommentsPerID.getKey()).get(CommentNumber).addAll(nlpp.getCommentBasket());
				CommentNumber++;
			}
		}
	}

	private void SerializeListTokens(){
		try {
			FileOutputStream fileOut = new FileOutputStream("./" + ProductName + "_Tokens.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(ListTokens);
			out.close();
			System.out.println("Serialized Tokens Object saved in ./" + ProductName + "_Tokens.ser");
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage() + " Exception thrown at Serialization of Tokens...");
		}
	}

	@SuppressWarnings("unchecked")
	private void DeserializeListTokens(String product){
		try {
			FileInputStream fileIn = new FileInputStream("./" + product + "_Tokens.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			ListTokens = (Map<Integer, Map<Integer, List<String>>>) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException | ClassNotFoundException ioe) {
			System.out.println(ioe.getMessage() + " Exception thrown at Deserialization...");
		}
	}

	public Boolean checkForExistingTokens(){
		File root = new File(Paths.SERIALIZEDREPORTS);
		for (String f : root.list()) {
			if (f.contains(ProductName + "_Tokens.ser")) {
				Deserialization_Flag = true;
			}
		}
		return Deserialization_Flag;
	}

	private void removeDuplicates(Map<Integer, Map<Integer, List<String>>> List){
		Iterator<Entry<Integer, Map<Integer, List<String>>>> IList = List.entrySet().iterator();
		Set<String> buffer = new HashSet<>();
		while (IList.hasNext()) {
			Entry<Integer, Map<Integer, List<String>>> EList = IList.next();
			Iterator<Entry<Integer, List<String>>> IEntryList = EList.getValue().entrySet().iterator();
			while (IEntryList.hasNext()) {
				Entry<Integer, List<String>> EEntryList = IEntryList.next();
				buffer.addAll(EEntryList.getValue());
				EEntryList.getValue().clear();
				EEntryList.getValue().addAll(buffer);
				buffer.clear();
			}
		}
	}

	/**
	 * Calls Method {@link bugReport.fetch.nlpProcess.CommentMapperAndNLPProcessor #InsertCommentsToMap() InsertCommentsToMap} and
	 * {@link bugReport.fetch.nlpProcess.CommentMapperAndNLPProcessor #NLPProcess() NLPProcess())} and then returns the final result which
	 * is a Map<Integer, Map<Integer, List<String>>> which contains for each bugReport(outer Integer Key), for each Comment(inner Integer
	 * Key) all Source Code Entities in a ArrayList.
	 * 
	 * @return
	 */
	public Map<Integer, Map<Integer, List<String>>> getListTokens(){
		if (!Deserialization_Flag) {
			InsertCommentsToMap();
			NLPProcess();
			SerializeListTokens();
		} else {
			System.out.println("Now Deserializing the ListTokens from: " + ProductName + "_Tokens.ser");
			DeserializeListTokens(ProductName);
			System.out.println("Done Deserializing the ListTokens");
		}
		removeDuplicates(ListTokens);
		return ListTokens;
	}

}
