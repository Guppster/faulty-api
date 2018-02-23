package preBucket.data.arrangement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import specificDepthRetrieval.RelationSearcher;
import SCEManipulation.Classes;
import SCEManipulation.Functions;

public class DatasetSeedMaker implements Runnable{
	private static Functions FunctionMap = null;
	@SuppressWarnings("unused")
	private static Classes ClassMap = null;
	private Map<Integer, Map<String, List<String>>> DatasetSeed = null;
	private static MongoGridFSConnection MGFSC;
	private Map<Integer, List<String>> Tokens = null;
	private Integer ID;
	RelationSearcher RS = null;

	/**
	 * Constructor for creation of an instance to be used for unexpanded DatasetSeeds creation
	 * 
	 * @param functions
	 * @param DatabaseName
	 * @param Product
	 */
	public DatasetSeedMaker(Functions functions, String DatabaseName, String Product){
		FunctionMap = functions;
		DatasetSeed = new HashMap<Integer, Map<String, List<String>>>();
		MGFSC = MongoGridFSConnection.getInstance(DatabaseName, Product + "_DatasetSeedsExpanded");
	}

	/**
	 * Constructor for creation of an instance to be used for expanded DatasetSeeds creation for what this creation entails @see
	 * {@link DatasetSeedWriter#WriteToDB()} method Documentation
	 * 
	 * @param RS
	 * @param functions
	 * @param classes
	 * @param DatabaseName
	 * @param Product
	 */
	public DatasetSeedMaker(RelationSearcher RS, Functions functions, Classes classes, String DatabaseName, String Product){
		FunctionMap = functions;
		ClassMap = classes;
		DatasetSeed = new HashMap<Integer, Map<String, List<String>>>();
		MGFSC = MongoGridFSConnection.getInstance(DatabaseName, Product + "_DatasetSeedsExpanded");
		this.RS = RS;
	}

	/**
	 * The implementation of the run() method containing a call to the method that we actually want to use when running this class
	 */
	@Override
	public void run(){
		makeDatasetSeed();
	}

	/**
	 * Composite setter method which allows an external class's method @see {@link DatasetSeedWriter#WriteToDB()} to set the appropriate
	 * execution parameters prior to calling the run() method or actually executing the using an executor pool
	 * 
	 * @param Tokens
	 * @param ID
	 */
	public void setExecutionParameters(Map<Integer, List<String>> Tokens, Integer ID){
		this.Tokens = Tokens;
		this.ID = ID;
		// if (ID == 131353) {
		// System.out.println(ID);
		// System.out.println(Tokens);
		// }
	}

	/**
	 * The makeDatasetSeed() method is where this class's work is actually done after we have already done all necessary preparations so
	 * that it can run correctly. It Firstly segregates the given List<String> Tokens to two List<String> on called functions and one called
	 * nonFunctions which contain exactly what their name so aptly defines. Then a series of buffer variables and lists are defined which
	 * are later on used to store transient information which results in the construction of the expanded functions and non_Functions lists
	 * which become functions and classes.
	 */
	private void makeDatasetSeed(){
		List<String> functions = new ArrayList<String>();
		List<String> nonFunctions = new ArrayList<String>();
		Iterator<Entry<Integer, List<String>>> ITokens = Tokens.entrySet().iterator();
		while (ITokens.hasNext()) {
			Entry<Integer, List<String>> ETokens = ITokens.next();
			DatasetSeed.put(ETokens.getKey(), new HashMap<String, List<String>>());
			for (String s : ETokens.getValue()) {
				if (FunctionMap.isFunction(s)) {
					functions.add(s);
				} else {
					nonFunctions.add(s);
				}
			}
			// The following was misplaced within the above closed for loop
			List<String> fn = new ArrayList<String>();
			fn.addAll(functions);
			Set<String> Expandedfn = new HashSet<String>();
			Expandedfn.addAll(fn);
			for (String s : fn) {
				Expandedfn.addAll(RS.getCalledFunctions(s));
				Expandedfn.addAll(RS.getCallingFunctions(s));
			}
			List<String> nFn = new ArrayList<String>();
			nFn.addAll(nonFunctions);
			Set<String> ExpandednFn = new HashSet<String>();
			for (String s : nonFunctions) {
				if (RS.isClass(s))
					ExpandednFn.add(s);
			}
			for (String s : nFn) {
				ExpandednFn.addAll(RS.getDefinedInClass(s));
			}
			List<String> funs = new ArrayList<String>();
			List<String> clas = new ArrayList<String>();
			funs.addAll(Expandedfn);
			clas.addAll(ExpandednFn);
			DatasetSeed.get(ETokens.getKey()).put("functions", funs);
			DatasetSeed.get(ETokens.getKey()).put("nonFunctions", clas);
			// System.out.println(clas);
			// if (ID == 131353) {
			// System.out.println("functions :" + functions);
			// System.out.println("nonFunctions :" + nonFunctions);
			// }
			functions.clear();
			nonFunctions.clear();
			// which for loop reached over here ... :(
		}
		MGFSC.InsertGridFSInMongo(makeJSON(DatasetSeed, ID), ID, "_Seed");
		// Iterator<Entry<Integer, Map<String, List<String>>>> IDataset = DatasetSeed.entrySet().iterator();
		// while (IDataset.hasNext()) {
		// Entry<Integer, Map<String, List<String>>> EIDataset = IDataset.next();
		// System.out.print("{\"" + EIDataset.getKey() + "\" : {");
		// Iterator<Entry<String, List<String>>> IInternal = EIDataset.getValue().entrySet().iterator();
		// while (IInternal.hasNext()) {
		// Entry<String, List<String>> EIInternal = IInternal.next();
		// System.out.print("\"" + EIInternal.getKey() + " \"[");
		// for (String a : EIInternal.getValue()) {
		// System.out.print("\"" + a + "\",");
		// }
		// System.out.print("],");
		// }
		// System.out.print("},");
		// }
		// System.out.println("}");
		DatasetSeed.clear();
	}

	/**
	 * makeJSON receives a complete DatasetSeed and it's corresponding Integer BugReport ID from which it is derived and is processed into a
	 * new JSONObject suitable for insertion to a MongoDB instance
	 * 
	 * @param DatasetSeed
	 * @param ID
	 * @return
	 */
	private String makeJSON(Map<Integer, Map<String, List<String>>> DatasetSeed, Integer ID){
		JSONObject jsonTemp = new JSONObject();
		try {
			jsonTemp.put(ID.toString(), new JSONObject(DatasetSeed));
			// System.out.println(jsonTemp.toString());
		} catch (JSONException jne) {
			System.out.println(jne.getMessage());
		}
		return jsonTemp.toString();
	}

	/**
	 * Checks to see if the mongoDBConnection is still open
	 * 
	 * @return
	 */
	public static boolean connectionOpen(){
		return MGFSC.connectionOpen();
	}

	/**
	 * Closes the DB Connections
	 */
	public static void closeDB(){
		MGFSC.closeDB();
	}

}
