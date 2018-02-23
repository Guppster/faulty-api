package commentWriterEntry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import specificDepthRetrieval.RelationSearcher;
import SCEManipulation.Functions;
import bugReport.fetch.nlpProcess.BugReportFetch;
import bugReport.fetch.nlpProcess.CommentMapperAndNLPProcessor;

import com.mongodb.DBObject;

public class CommentWriterEntryPoint{
	// private static final Logger LOG = LoggerFactory.getLogger(MahoutTryIt.class);
	private static final String BASE_PATH = "../../../../../../../media/or10n/Data/ProductComments/";

	private static Map<Integer, Map<Integer, List<String>>> ListTokens = new HashMap<Integer, Map<Integer, List<String>>>();
	private static String product = "";
	private static String DatabaseName = "";
	private static DBObject[] Reports = null;
	private static Functions FunctionMap = null;
	private static BugReportFetch BRF = null;
	private static CommentMapperAndNLPProcessor CMNP = null;
	private static RelationSearcher RS = null;
	private static Map<Integer, Map<Integer, List<String>>> ListOfExpandedTokens = null;
	private static boolean Deserialization_Flag = false;

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		System.out.println();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.print("Enter the name of the Database :");
			DatabaseName = br.readLine();
			System.out.print("Enter the name of the Product :");
			product = br.readLine();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage() + " Exception thrown at read from Console ");
		}
		CMNP = new CommentMapperAndNLPProcessor(product);
		if (!CMNP.checkForExistingTokens()) {
			// BRF gets the corresponding Reports from the Database and stores them in a private variable of Type DBObject which is
			// accessible through the .getReports() public method.
			BRF = new BugReportFetch("product", product, DatabaseName);
			System.out.println("Fetching Reports from Database ...");
			BRF.fetchReports();
			Reports = BRF.getReports();
			// CMNP.setReports is used to pass the Set of corresponding Bug Reports to the Object for further processing.
			CMNP.setReports(Reports);
			System.out.println("Starting Tokenization ...");
			// listTokens is a Map of type Map<Integer, Map<Integer, List<String>>> where the first integer is the BugReport_ID
			// the second Integer is the number of each comment within a BugReport and the List<String> contains the list of tokens taken we
			// keep from each comment after processing.
			ListTokens = CMNP.getListTokens();
			System.out.println("Tokenization Completed ...");
		} else {
			ListTokens = CMNP.getListTokens();
		}
		File folder = new File(BASE_PATH + product + "/");
		// if (folder.isDirectory())
		// for (String s : folder.list()) {
		// System.out.println(s);
		// }
		if (!checkForExistingTokens()) {
			System.out.println("cleanAndExpandTokens");
			ListOfExpandedTokens = cleanAndExpandTokens(ListTokens);
			System.out.println("Serializing ListOfExpandedTokens");
			SerializeListTokens();
			System.out.println("Done Serializing ListOfExpandedTokens");
		} else {
			System.out.println("Deserializing ListOfExpandedTokens ");
			DeserializeListTokens(product);
			System.out.println("Done Deserializing ListOfExpandedTokens");
		}
		System.out.println("PrintToFile");
		PrintToFile(ListOfExpandedTokens, product);
	}

	private static Map<Integer, Map<Integer, List<String>>> cleanAndExpandTokens(Map<Integer, Map<Integer, List<String>>> ListOfTokens){
		Map<Integer, Map<Integer, List<String>>> ListOfExtraTokens = new HashMap<Integer, Map<Integer, List<String>>>();
		Map<Integer, List<String>> PerCommentTokens = new HashMap<Integer, List<String>>();
		Set<String> Expanded;
		Iterator<Entry<Integer, Map<Integer, List<String>>>> IListOfTokens = ListOfTokens.entrySet().iterator();
		FunctionMap = new Functions(product);
		FunctionMap.CreateRelationGraph(product);
		RS = FunctionMap.getRelationSearcher();
		int counter = 0;
		while (IListOfTokens.hasNext()) {
			Entry<Integer, Map<Integer, List<String>>> EListOfTokens = IListOfTokens.next();
			Integer BugReportID = EListOfTokens.getKey();
			Iterator<Entry<Integer, List<String>>> IEListOfTokens = EListOfTokens.getValue().entrySet().iterator();
			PerCommentTokens = new HashMap<Integer, List<String>>();
			while (IEListOfTokens.hasNext()) {
				Entry<Integer, List<String>> EEListOfTokens = IEListOfTokens.next();
				Integer CommentID = EEListOfTokens.getKey();
				List<String> Tokens = EEListOfTokens.getValue();
				Expanded = new HashSet<String>();
				Expanded.addAll(Tokens);
				for (String s : Tokens) {
					counter++;
					if (RS.isFunction(s)) {
						Expanded.addAll(RS.getCalledFunctions(s));
						Expanded.addAll(RS.getCallingFunctions(s));
					} else {
						if (RS.isClass(s)) {
							Expanded.addAll(RS.getDefinedInClass(s));
						}
					}
					if (counter % 100000 == 0)
						System.out.println("another hundred Thousand have been Processed");
				}
				List<String> EXP = new ArrayList<String>();
				EXP.addAll(Expanded);
				PerCommentTokens.put(CommentID, EXP);
			}
			ListOfExtraTokens.put(BugReportID, PerCommentTokens);
		}
		return ListOfExtraTokens;
	}

	private static void SerializeListTokens(){
		try {
			FileOutputStream fileOut = new FileOutputStream("./" + product + "_GoodComments.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(ListOfExpandedTokens);
			out.close();
			System.out.println("Serialized Tokens Object saved in ./" + product + "_GoodComments.ser");
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage() + " Exception thrown at Serialization of Tokens...");
		}
	}

	@SuppressWarnings("unchecked")
	private static void DeserializeListTokens(String product){
		try {
			FileInputStream fileIn = new FileInputStream("./" + product + "_GoodComments.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			ListOfExpandedTokens = (Map<Integer, Map<Integer, List<String>>>) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException | ClassNotFoundException ioe) {
			System.out.println(ioe.getMessage() + " Exception thrown at Deserialization...");
		}
	}

	public static Boolean checkForExistingTokens(){
		File root = new File("./");
		for (String f : root.list()) {
			if (f.contains(product + "_GoodComments.ser")) {
				Deserialization_Flag = true;
			}
		}
		return Deserialization_Flag;
	}

	private static void PrintToFile(Map<Integer, Map<Integer, List<String>>> LOET, String product){
		File Container1 = new File(BASE_PATH + product);
		int i = 0;
		boolean toKeep = false;
		final String FINAL_PATH = BASE_PATH + product + "/";
		try {
			Writer toDelete = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(BASE_PATH + "toDelete.txt"), "utf-8"));
			if (Container1.mkdir() || Container1.isDirectory()) {
				Iterator<Entry<Integer, Map<Integer, List<String>>>> ILOET = LOET.entrySet().iterator();
				while (ILOET.hasNext()) {
					toKeep = false;
					Entry<Integer, Map<Integer, List<String>>> ELOET = ILOET.next();
					Integer BUG_ID = ELOET.getKey();
					String Filename = FINAL_PATH + BUG_ID + ".txt";
					Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Filename), "utf-8"));
					// writer.write("something");
					Iterator<Entry<Integer, List<String>>> IELOET = ELOET.getValue().entrySet().iterator();
					while (IELOET.hasNext()) {
						i++;
						Entry<Integer, List<String>> EELOET = IELOET.next();
						List<String> towrite = EELOET.getValue();
						if (i % 100000 == 0) {
							System.out.println("One more hundred thousand Comments have been printed");
						}
						if (!towrite.isEmpty()) {
							toKeep = true;
						}
						for (String s : towrite) {
							writer.write(s + " ");
						}
						writer.write("\n");
					}
					if (!toKeep) {
						toDelete.write(Filename.split("/")[Filename.split("/").length - 1]);
						toDelete.write("\n");
					}
					writer.close();
				}
				toDelete.close();
			}
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}

	@SuppressWarnings("unused")
	private static void PrintOneToFile(Map<Integer, Map<Integer, List<String>>> LOET, String product){
		File Container1 = new File(BASE_PATH + product);
		File file = null;
		int i = 0;
		final String FINAL_PATH = BASE_PATH + product + "/";
		if (Container1.mkdir()) {
			Iterator<Entry<Integer, Map<Integer, List<String>>>> ILOET = LOET.entrySet().iterator();
			try {
				while (ILOET.hasNext()) {
					Entry<Integer, Map<Integer, List<String>>> ELOET = ILOET.next();
					Integer BUG_ID = ELOET.getKey();
					file = new File(FINAL_PATH + BUG_ID + ".txt");
					PrintWriter writer = new PrintWriter(file);

					Iterator<Entry<Integer, List<String>>> IELOET = ELOET.getValue().entrySet().iterator();
					while (IELOET.hasNext()) {
						i++;
						Entry<Integer, List<String>> EELOET = IELOET.next();
						List<String> towrite = EELOET.getValue();
						if (i % 1000 == 0) {
							System.out.println(towrite);
						}
						for (String s : towrite)
							writer.print(s + " ");
						writer.println();
					}
					writer.close();
				}
			} catch (FileNotFoundException fnfe) {
				System.out.println(fnfe.getMessage());
			}
		}
	}
}
