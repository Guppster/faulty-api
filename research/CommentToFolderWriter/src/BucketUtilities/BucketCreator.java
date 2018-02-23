package BucketUtilities;

import init.mongodb.MongoGridFSQuery;

import java.util.ArrayList;
import java.util.Collections;
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

import SCEManipulation.Functions;
import bucket.implementation.Bucket;

import com.mongodb.MongoException;

public class BucketCreator implements Runnable{
	@SuppressWarnings("unused")
	private Functions FunctionMap;
	@SuppressWarnings("unused")
	private String Product = null;
	@SuppressWarnings("unused")
	private String DatabaseName = null;
	private Integer ID;
	private static MongoGridFSQuery MGFSQ;
	private static MongoGridFSConnection MGFSC = null;
	private static Map<String, Bucket> FNBuckets = Collections.synchronizedMap(new HashMap<String, Bucket>());
	private static Map<String, Bucket> NFNBuckets = Collections.synchronizedMap(new HashMap<String, Bucket>());
	private static Set<String> FNKeys = new HashSet<String>();
	private static Set<String> NFNKeys = new HashSet<String>();

	public BucketCreator(String DatabaseName, String Product){
		this.Product = Product;
		this.DatabaseName = DatabaseName;
		// this.ID = ID;
		// BMDBC = new MongoDBConnection(DatabaseName, Product + "Buckets");
		MGFSC = MongoGridFSConnection.getInstance(DatabaseName, Product + "_BucketsExpanded");
		MGFSQ = MongoGridFSQuery.getInstance(DatabaseName, Product + "_DatasetSeedsExpanded");
	}

	public BucketCreator(String DatabaseName, String Product, Integer ID){
		this.Product = Product;
		this.DatabaseName = DatabaseName;
		this.ID = ID;
		// BMDBC = new MongoDBConnection(DatabaseName, Product + "Buckets");
		MGFSC = MongoGridFSConnection.getInstance(DatabaseName, Product + "_BucketsExpanded");
		MGFSQ = MongoGridFSQuery.getInstance(DatabaseName, Product + "_DatasetSeedsExpanded");
	}

	public static Map<String, Bucket> getFNMap(){
		return FNBuckets;
	}

	public static Map<String, Bucket> getNFNMap(){
		return NFNBuckets;
	}

	public void setID(Integer ID){
		this.ID = ID;
	}

	public int getFNKeySize(){
		return FNKeys.size();
	}

	public Set<String> getFNKeys(){
		return FNKeys;
	}

	public int getNFNKeySize(){
		return NFNKeys.size();
	}

	public static void InsertToMongoDB(JSONObject jsontemp, String Key){
		try {
			MGFSC.InsertGridFSInMongo(jsontemp.toString(), Key, "");
			// jsontemp.remove(ID.toString());
		} catch (MongoException me) {
			System.out.println(me.getMessage());
		}
	}

	private void InsertToMongoDB(JSONObject jsontemp, Integer ID){
		try {
			MGFSC.InsertGridFSInMongo(jsontemp.toString(), ID, "_Bucket");
			// jsontemp.remove(ID.toString());
		} catch (MongoException me) {
			System.out.println(me.getMessage());
		}
	}

	private JSONObject getDatasetSeed(Integer ID){
		try {
			return MGFSQ.GetFile_GridFS(ID + "_Seed");
		} catch (MongoException me) {
			System.out.println(me.getMessage());
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Map<Integer, Map<Integer, Map<String, List<String>>>> JsonToMap(JSONObject json) throws JSONException{
		// System.out.println(json.toString());
		Map<Integer, Map<Integer, Map<String, List<String>>>> SeedMap = new HashMap<Integer, Map<Integer, Map<String, List<String>>>>();
		Iterator<String> keys = json.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			Integer ikey = (Integer) Integer.parseInt(key);
			JSONObject Inside = json.getJSONObject(key);
			Iterator<String> Ikeys = Inside.keys();
			Map<Integer, Map<String, List<String>>> InsideMap = new HashMap<Integer, Map<String, List<String>>>();
			SeedMap.put(ikey, InsideMap);
			while (Ikeys.hasNext()) {
				String Ikey = Ikeys.next();
				Integer iikey = (Integer) Integer.parseInt(Ikey);
				InsideMap.put(iikey, new HashMap<String, List<String>>());
				// System.out.println(Ikey);
				JSONObject IInside = Inside.getJSONObject(Ikey);
				Iterator<String> IIkeys = IInside.keys();
				Map<String, List<String>> InnerArray = new HashMap<String, List<String>>();
				InsideMap.put(iikey, InnerArray);
				while (IIkeys.hasNext()) {
					String IIkey = IIkeys.next();
					InnerArray.put(IIkey, new ArrayList<String>());
					// System.out.println(IIkey);
					JSONArray IIInside = IInside.getJSONArray(IIkey);
					// System.out.println(IIInside.length());
					for (int i = 0; i < IIInside.length(); i++) {
						InnerArray.get(IIkey).add(IIInside.getString(i));
					}
				}
			}

		}
		return SeedMap;
	}

	public void run(){
		try {
			create(ID);
		} catch (JSONException jne) {
			System.out.println(jne.getMessage());
		}
	}

	public void run(Integer ID){
		try {
			create(ID);
		} catch (JSONException jne) {
			System.out.println(jne.getMessage());
		}

	}

	/**
	 * Creates a number of hashMaps representing relations between Source code Entities This version 2.0 actually creates cumulative
	 * relational HashMaps which contain for each given entity all of its counterparts that are related to it throughout the entire
	 * collection of BR's which compose the set of BR's related to a specific Product
	 * 
	 * @param ID
	 * @throws JSONException
	 */

	/* How to proceed in code outline iterator for outer keys of seed iterator for second keys of seed iterator for third keys of seed
	 * iterator for fourth keys of seed addition of each of the entities in their respective Bucket within the Bucket containement class */

	private void create(Integer ID) throws JSONException{
		// Integer mi = ID;
		// Define the DatasetSeed Variable and assign to it
		// the result of the getDatasetSeed(ID) call via a call to JsonToMap method
		Set<String> NonFunctionRepSet = new HashSet<String>();
		Set<String> FunctionRepSet = new HashSet<String>();
		List<String> NonFunctionReport = new ArrayList<String>();
		List<String> FunctionReport = new ArrayList<String>();
		JSONObject gfsDBFile = getDatasetSeed(ID);
		Map<Integer, Map<Integer, Map<String, List<String>>>> DatasetSeed = JsonToMap(gfsDBFile);
		// Per Comment Section Begin ---------------------

		// Iterator for id (should have only one entry)
		Iterator<Entry<Integer, Map<Integer, Map<String, List<String>>>>> IDatasetSeed = DatasetSeed.entrySet().iterator();
		while (IDatasetSeed.hasNext()) {
			Entry<Integer, Map<Integer, Map<String, List<String>>>> EDatasetSeed = IDatasetSeed.next();
			// Bucket.put(EDatasetSeed.getKey(), new HashMap<Integer, Map<String, Map<String, List<String>>>>());
			if (!EDatasetSeed.getValue().isEmpty()) {
				Map<Integer, Map<String, List<String>>> CommentLevel = EDatasetSeed.getValue();

				// Iterator for Comment_id (May have multiple entries)
				Iterator<Entry<Integer, Map<String, List<String>>>> ICommentLevel = CommentLevel.entrySet().iterator();
				while (ICommentLevel.hasNext()) {
					// CommentNumberAndBucket = new HashMap<String, Map<String, List<String>>>();
					Entry<Integer, Map<String, List<String>>> ECommentLevel = ICommentLevel.next();
					if (!ECommentLevel.getValue().isEmpty()) {
						Map<String, List<String>> FNFLevel = ECommentLevel.getValue();

						// Inner Keys MUST have only two keys ("nonFunctions", "functions")
						List<String> NonFunctionTokens = FNFLevel.get("nonFunctions");
						List<String> FunctionTokens = FNFLevel.get("functions");
						List<String> toRemove = new ArrayList<String>();
						for (String s : NonFunctionTokens) {
							if (s.length() < 3)
								toRemove.add(s);
						}
						NonFunctionTokens.removeAll(toRemove);
						toRemove.clear();
						for (String s : FunctionTokens) {
							if (s.length() < 3)
								toRemove.add(s);
						}
						FunctionTokens.removeAll(toRemove);
						toRemove.clear();
						// SOLVED!!!!! CAUTION IT IS NEVER??? EXecuted

						if (!NonFunctionTokens.isEmpty() && !FunctionTokens.isEmpty()) {
							/* TODO create logic to store buckets in correct location within the @dataset map */
							MakeFNtoNFN_perComment(NonFunctionTokens, FunctionTokens);
							MakeNFNtoFN_perComment(NonFunctionTokens, FunctionTokens);
							// Bucket.get(EDatasetSeed.getKey()).put(ECommentLevel.getKey(), CommentNumberAndBucket);
							NonFunctionRepSet.addAll(NonFunctionTokens);
							FunctionRepSet.addAll(FunctionTokens);
						} else {
							NonFunctionRepSet.addAll(NonFunctionTokens);
							FunctionRepSet.addAll(FunctionTokens);
						}
					}
				}
			}
			// Per Comment Section End -----------------------
			// Per Report Section Begin ----------------------------------------------------------------
			NonFunctionReport.addAll(NonFunctionRepSet);
			FunctionReport.addAll(FunctionRepSet);
			MakeFNtoNFN_perReport(NonFunctionReport, FunctionReport);
			MakeNFNtoFN_perReport(NonFunctionReport, FunctionReport);
			// CommentNumberAndBucket = new HashMap<String, Map<String, List<String>>>();
			// CommentNumberAndBucket.put("FNtoNFN", FNtoNFN);
			// CommentNumberAndBucket.put("NFNtoFN", NFNtoFN);
			// Bucket.get(EDatasetSeed.getKey()).put(new Integer(-1), CommentNumberAndBucket);
			// Per Report Section End ------------------------------------------------------------------
		}
		// JSONObject toInsert = new JSONObject();
		// try {
		// toInsert = new JSONObject(Bucket);
		// // System.out.println(toInsert.toString());
		// } catch (OutOfMemoryError ooME) {
		// System.out.println(ooME.getMessage() + " @" + ID);
		// }
		// InsertToMongoDB(toInsert, ID);
		// System.out.println(mi);
	}

	// To work it needs a different getDatasetSeed function accepting no arguments

	@SuppressWarnings("unused")
	private void create1() throws JSONException{
		Map<Integer, Map<Integer, Map<String, Map<String, List<String>>>>> Dataset = new HashMap<Integer, Map<Integer, Map<String, Map<String, List<String>>>>>();
		Set<String> NonFunctionRepSet = new HashSet<String>();
		Set<String> FunctionRepSet = new HashSet<String>();
		List<String> NonFunctionReport = new ArrayList<String>();
		List<String> FunctionReport = new ArrayList<String>();
		List<String> NonFunctionComment;
		List<String> FunctionComment;
		JSONObject gfsDBFile = getDatasetSeed(ID);
		Map<Integer, Map<Integer, Map<String, List<String>>>> DatasetSeed = JsonToMap(gfsDBFile);
		// if (ID == 131353) {
		// System.out.println("Now creating Bucket for ID :" + DatasetSeed.keySet());
		// }
		JSONObject test = new JSONObject(DatasetSeed);
		Iterator<Entry<Integer, Map<Integer, Map<String, List<String>>>>> IDatasetSeed = DatasetSeed.entrySet().iterator();
		while (IDatasetSeed.hasNext()) {
			Entry<Integer, Map<Integer, Map<String, List<String>>>> EDatasetSeed = IDatasetSeed.next();
			Integer BugReportID = EDatasetSeed.getKey();
			Map<Integer, Map<String, Map<String, List<String>>>> Dataset_CommentLevel = new HashMap<Integer, Map<String, Map<String, List<String>>>>();
			Dataset.put(BugReportID, Dataset_CommentLevel);
			Map<String, Map<String, List<String>>> Dataset_SetTypeLevel = new HashMap<String, Map<String, List<String>>>();
			// System.out.println(BugReportID);
			if (!EDatasetSeed.getValue().isEmpty()) {
				Iterator<Entry<Integer, Map<String, List<String>>>> IEDatasetSeed = EDatasetSeed.getValue().entrySet().iterator();
				while (IEDatasetSeed.hasNext()) {
					Entry<Integer, Map<String, List<String>>> EEDatasetSeed = IEDatasetSeed.next();
					Integer BugReportCommentID = EEDatasetSeed.getKey();
					Dataset_CommentLevel.put(BugReportCommentID, Dataset_SetTypeLevel);
					// System.out.println(BugReportCommentID);
					if (!EEDatasetSeed.getValue().isEmpty()) {
						List<String> NonFunctionTokens = EEDatasetSeed.getValue().get("nonFunctions");
						// System.out.println(NonFunctionTokens);
						List<String> FunctionTokens = EEDatasetSeed.getValue().get("functions");
						// System.out.println(FunctionTokens);
						Map<String, List<String>> Dataset_NFNtoFNCommentLevel = new HashMap<String, List<String>>();
						for (String s : NonFunctionTokens) {
							Dataset_NFNtoFNCommentLevel.put(s, FunctionTokens);
						}
						Map<String, List<String>> Dataset_FNtoNFNCommentLevel = new HashMap<String, List<String>>();
						for (String s : FunctionTokens) {
							Dataset_FNtoNFNCommentLevel.put(s, NonFunctionTokens);
						}
						Dataset_SetTypeLevel.put("NFNtoFNComment", Dataset_NFNtoFNCommentLevel);
						Dataset_SetTypeLevel.put("FNtoNFNComment", Dataset_FNtoNFNCommentLevel);
						NonFunctionRepSet.addAll(NonFunctionTokens);
						FunctionRepSet.addAll(FunctionTokens);
					}
					NonFunctionReport.addAll(NonFunctionRepSet);
					FunctionRepSet.addAll(FunctionRepSet);
				}
				Map<String, List<String>> Dataset_NFNtoFNReportLevel = new HashMap<String, List<String>>();
				for (String s : NonFunctionReport) {
					Dataset_NFNtoFNReportLevel.put(s, FunctionReport);
				}
				Map<String, List<String>> Dataset_FNtoNFNReportLevel = new HashMap<String, List<String>>();
				for (String s : FunctionReport) {
					Dataset_FNtoNFNReportLevel.put(s, NonFunctionReport);
				}
				Dataset_SetTypeLevel.put("NFNtoFNReport", Dataset_NFNtoFNReportLevel);
				Dataset_SetTypeLevel.put("FNtoNFNReport", Dataset_FNtoNFNReportLevel);
			}
		}
		JSONObject toInsert = new JSONObject();
		try {
			toInsert = new JSONObject(Dataset);
		} catch (OutOfMemoryError ooME) {
			System.out.println(ooME.getMessage() + " @" + ID);
		}
		Dataset.clear();
		// System.out.println(toInsert.toString());
		InsertToMongoDB(toInsert, ID);
		// TODO complete creation script
		// System.out.println(test.toString());
		// System.out.println(gfsDBFile.keySet());
		// System.out.println(gfsDBFile.toMap().keySet());
		// List<String> CommentFunctions = new ArrayList<String>();
		// List<String> CommentNonFunctions = new ArrayList<String>();
		// List<String> ReportFunctions = new ArrayList<String>();
		// List<String> ReportNonFunctions = new ArrayList<String>();
		// Map<String, List<String>> FNtoNFN_perComment = new HashMap<String, List<String>>();
		// Map<String, List<String>> NFNtoFN_perComment = new HashMap<String, List<String>>();
		// Map<String, List<String>> FNtoNFN_perReport = new HashMap<String, List<String>>();
		// Map<String, List<String>> NFNtoFN_perReport = new HashMap<String, List<String>>();
	}

	/* Iterator<Entry<Integer, Map<Integer, List<String>>>> IlistTokens = ListTokens.entrySet().iterator(); while (IlistTokens.hasNext()) {
	 * Entry<Integer, Map<Integer, List<String>>> ElistTokens = IlistTokens.next(); Bucket CurrentBucket = new Bucket(ElistTokens.getKey());
	 * CommentFunctions.clear(); CommentNonFunctions.clear(); // // AllBuckets.put(ElistTokens.getKey(), new HashMap<String, Map<Integer,
	 * Map<String, List<String>>>>()); // AllBuckets.get(ElistTokens.getKey()).put("FNtoNFN_perComment", new HashMap<Integer, Map<String,
	 * List<String>>>()); // AllBuckets.get(ElistTokens.getKey()).put("NFNtoFN_perComment", new HashMap<Integer, Map<String,
	 * List<String>>>()); // AllBuckets.get(ElistTokens.getKey()).put("FNtoNFN_perReport", new HashMap<Integer, Map<String,
	 * List<String>>>()); // AllBuckets.get(ElistTokens.getKey()).put("NFNtoFN_perReport", new HashMap<Integer, Map<String,
	 * List<String>>>());
	 * 
	 * Iterator<Entry<Integer, List<String>>> ITokens = ElistTokens.getValue().entrySet().iterator(); while (ITokens.hasNext()) {
	 * Entry<Integer, List<String>> ETokens = ITokens.next(); for (String s : ETokens.getValue()) { if (FunctionMap.isFunction(s)) {
	 * CommentFunctions.add(s); } else { CommentNonFunctions.add(s); } } FNtoNFN_perComment = MakeFNtoNFN_perComment(CommentNonFunctions,
	 * CommentFunctions); Iterator<Entry<String, List<String>>> IFNtoNFN_perComment = FNtoNFN_perComment.entrySet().iterator(); while
	 * (IFNtoNFN_perComment.hasNext()) { Entry<String, List<String>> EFNtoNFN_perComment = IFNtoNFN_perComment.next();
	 * CurrentBucket.insert_in_Comment_FunToNonFun(ETokens.getKey(), EFNtoNFN_perComment.getKey(), EFNtoNFN_perComment.getValue());
	 * 
	 * } // AllBuckets.get(ElistTokens.getKey()).get("FNtoNFN_perComment").put(ETokens.getKey(), FNtoNFN_perComment); NFNtoFN_perComment =
	 * MakeNFNtoFN_perComment(CommentNonFunctions, CommentFunctions); Iterator<Entry<String, List<String>>> INFNtoFN_perComment =
	 * NFNtoFN_perComment.entrySet().iterator(); while (IFNtoNFN_perComment.hasNext()) { Entry<String, List<String>> ENFNtoFN_perComment =
	 * INFNtoFN_perComment.next(); CurrentBucket.insert_in_Comment_NonFunToFun(ETokens.getKey(), ENFNtoFN_perComment.getKey(),
	 * ENFNtoFN_perComment.getValue());
	 * 
	 * } // AllBuckets.get(ElistTokens.getKey()).get("NFNtoFN_perComment").put(ETokens.getKey(), NFNtoFN_perComment);
	 * ReportFunctions.addAll(CommentFunctions); ReportNonFunctions.addAll(CommentNonFunctions);
	 * 
	 * } FNtoNFN_perReport = MakeFNtoNFN_perReport(ReportNonFunctions, ReportFunctions); Iterator<Entry<String, List<String>>>
	 * IFNtoNFN_perReport = FNtoNFN_perReport.entrySet().iterator(); while (IFNtoNFN_perReport.hasNext()) { Entry<String, List<String>>
	 * EFNtoNFN_perReport = IFNtoNFN_perReport.next(); CurrentBucket.insert_in_Report_FunToNonFun(EFNtoNFN_perReport.getKey(),
	 * EFNtoNFN_perReport.getValue()); } // AllBuckets.get(ElistTokens.getKey()).get("FNtoNFN_perReport").put(-1, FNtoNFN_perReport);
	 * NFNtoFN_perReport = MakeNFNtoFN_perReport(ReportNonFunctions, ReportFunctions); Iterator<Entry<String, List<String>>>
	 * INFNtoFN_perReport = NFNtoFN_perReport.entrySet().iterator(); while (INFNtoFN_perReport.hasNext()) { Entry<String, List<String>>
	 * ENFNtoFN_perReport = INFNtoFN_perReport.next(); CurrentBucket.insert_in_Report_NonFunToFun(ENFNtoFN_perReport.getKey(),
	 * ENFNtoFN_perReport.getValue()); } // AllBuckets.get(ElistTokens.getKey()).get("NFNtoFN_perReport").put(-1, NFNtoFN_perReport); //
	 * printBucket(AllBuckets.get(ElistTokens.getKey())); // CDBC.save(AllBuckets); // AllBuckets.clear();
	 * InsertToMongoDB(CurrentBucket.MakeJSON(), ElistTokens.getKey()); } } */
	private synchronized void MakeFNtoNFN_perComment(List<String> NonFunctionTokens, List<String> FunctionTokens){
		Bucket B;
		for (String s : FunctionTokens) {
			if (FNBuckets.containsKey(s)) {
				// System.out.println("FNtoNFN_perComment :" + s);
				B = FNBuckets.get(s);
				B.insert_in_CommentScopeRelation(NonFunctionTokens);
			} else {
				B = new Bucket(s);
				B.insert_in_CommentScopeRelation(NonFunctionTokens);
				FNKeys.add(s);
				FNBuckets.put(s, B);
			}
		}
	}

	private synchronized void MakeNFNtoFN_perComment(List<String> NonFunctionTokens, List<String> FunctionTokens){
		Bucket B;
		for (String s : NonFunctionTokens) {
			if (NFNBuckets.containsKey(s)) {
				// System.out.println("NFNtoFN_perComment :" + s);
				B = NFNBuckets.get(s);
				B.insert_in_CommentScopeRelation(FunctionTokens);
			} else {
				B = new Bucket(s);
				B.insert_in_CommentScopeRelation(FunctionTokens);
				NFNKeys.add(s);
				NFNBuckets.put(s, B);
			}
		}
	}

	private synchronized void MakeFNtoNFN_perReport(List<String> NonFunctionTokens, List<String> FunctionTokens){
		Bucket B;
		for (String s : FunctionTokens) {
			if (FNBuckets.containsKey(s)) {
				// System.out.println("FNtoNFN_perReport :" + s);
				B = FNBuckets.get(s);
				B.insert_in_ReportScopeRelation(NonFunctionTokens);
			} else {
				B = new Bucket(s);
				B.insert_in_ReportScopeRelation(NonFunctionTokens);
				FNKeys.add(s);
				FNBuckets.put(s, B);
			}
		}
	}

	private synchronized void MakeNFNtoFN_perReport(List<String> NonFunctionTokens, List<String> FunctionTokens){
		Bucket B;
		for (String s : NonFunctionTokens) {
			if (NFNBuckets.containsKey(s)) {
				// System.out.println("NFNtoFN_perReport :" + s);
				B = NFNBuckets.get(s);
				B.insert_in_ReportScopeRelation(FunctionTokens);
			} else {
				B = new Bucket(s);
				B.insert_in_ReportScopeRelation(FunctionTokens);
				NFNKeys.add(s);
				NFNBuckets.put(s, B);
			}
			// if (s != "entry") {
			// System.out.println(s + " = entry");
			// Map<String, CopyOnWriteArraySet<String>> info = B.getReportScopeRelation();
			// Iterator<Entry<String, CopyOnWriteArraySet<String>>> Iinfo = info.entrySet().iterator();
			// while (Iinfo.hasNext()) {
			// CopyOnWriteArraySet<String> foo = Iinfo.next().getValue();
			// for (String a : foo) {
			// System.out.print(a + " ,");
			// }
			// }
			//
			// }
		}
	}

	@SuppressWarnings("unused")
	private void printBucket(Map<String, Map<Integer, Map<String, List<String>>>> Bucket){
		Iterator<Entry<String, Map<Integer, Map<String, List<String>>>>> IFN_NFNTags = Bucket.entrySet().iterator();
		while (IFN_NFNTags.hasNext()) {
			Entry<String, Map<Integer, Map<String, List<String>>>> EFN_NFNTags = IFN_NFNTags.next();
			System.out.println(EFN_NFNTags.getKey());
			Iterator<Entry<Integer, Map<String, List<String>>>> IComment = EFN_NFNTags.getValue().entrySet().iterator();
			while (IComment.hasNext()) {
				Entry<Integer, Map<String, List<String>>> EComment = IComment.next();
				System.out.println("for Comment No :" + EComment.getKey());
				Iterator<Entry<String, List<String>>> IBucket = EComment.getValue().entrySet().iterator();
				while (IBucket.hasNext()) {
					Entry<String, List<String>> EBucket = IBucket.next();
					System.out.println(EBucket.getKey());
					for (String s : EBucket.getValue()) {
						System.out.print(s + " ");
					}
					System.out.println();
				}
			}
		}
	}

	public static void closeDB(){
		MGFSQ.closeDB();
		MGFSC.closeDB();
	}
}
