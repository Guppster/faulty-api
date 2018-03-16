package BucketUtilities;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bucket.implementation.Bucket;

public class BucketCreatorExecutor{
	private ExecutorService ES = null;
	private Set<Integer> IDs = null;
	private String DatabaseName = null;
	private String Product = null;
	private BucketCreator BC;

	public BucketCreatorExecutor(Set<Integer> IDs, String Product, String DatabaseName){
		ES = Executors.newFixedThreadPool(500);
		this.IDs = IDs;
		this.Product = Product;
		this.DatabaseName = DatabaseName;
	}

	public void ExecuteCreation(){
		BC = new BucketCreator(DatabaseName, Product);
		// int count = 1;
		for (Integer ID : IDs) {
			BC = new BucketCreator(DatabaseName, Product, ID);
			// BC.run(ID);
			// BC.run();
			// BC.setID(ID);
			// count++;
			// if (count % 10 == 0) {
			// try {
			// Thread.sleep(60000);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
			// System.out.println(count);
			// System.out.println(Thread.activeCount());
			ES.execute(BC);
			// break;
			// BC = null;
		}
		try {
			// while (Thread.activeCount() == 85) {
			// Thread.sleep(5000);
			// System.out.println("FNLength = " + getFNLength());
			// System.out.println("NFNLength = " + getNFNLength());
			// }
			// System.out.println("BC.getFNKeySize() = " + BC.getFNKeySize());
			// System.out.println("BC.getNFNKeySize() = " + BC.getNFNKeySize());
			ES.shutdown();
			boolean done = false;
			while (!done) {
				done = ES.awaitTermination(1000, TimeUnit.SECONDS);
			}
			// if (ES.awaitTermination(1000, TimeUnit.SECONDS))
			// System.out.println("all well");

			// BucketCreator.closeDB();
		} catch (InterruptedException ie) {
			System.out.println(ie.getMessage());
		}

		JSONObject toInsert = new JSONObject();
		// Insertion of Buckets per Key to Database
		Map<String, Bucket> FNBuckets = BucketCreator.getFNMap();
		Map<String, Bucket> NFNBuckets = BucketCreator.getNFNMap();
		Iterator<Entry<String, Bucket>> IFNBuckets = FNBuckets.entrySet().iterator();
		while (IFNBuckets.hasNext()) {
			Entry<String, Bucket> EFNBuckets = IFNBuckets.next();
			toInsert = new JSONObject();
			try {
				if (EFNBuckets.getValue().getCommentScopeRelation().containsKey(EFNBuckets.getKey()))
					toInsert.put("CommentScopeRelation",
							new JSONArray(EFNBuckets.getValue().getCommentScopeRelation().get(EFNBuckets.getKey())));
				if (EFNBuckets.getValue().getReportScopeRelation().containsKey(EFNBuckets.getKey()))
					toInsert.put("ReportScopeRelation",
							new JSONArray(EFNBuckets.getValue().getReportScopeRelation().get(EFNBuckets.getKey())));
				// System.out.println(EFNBuckets.getValue().toString());
				// System.out.println(toInsert);
				if (toInsert.toString() != "null")
					BucketCreator.InsertToMongoDB(toInsert, EFNBuckets.getKey().toLowerCase());
			} catch (OutOfMemoryError | JSONException ooME) {
				System.out.println(ooME.getMessage() + " @" + EFNBuckets.getKey());
			}
		}
		Iterator<Entry<String, Bucket>> INFNBuckets = NFNBuckets.entrySet().iterator();
		while (INFNBuckets.hasNext()) {
			Entry<String, Bucket> ENFNBuckets = INFNBuckets.next();
			toInsert = new JSONObject(ENFNBuckets);
			try {
				if (ENFNBuckets.getValue().getCommentScopeRelation().containsKey(ENFNBuckets.getKey()))
					toInsert.put("CommentScopeRelation",
							new JSONArray(ENFNBuckets.getValue().getCommentScopeRelation().get(ENFNBuckets.getKey())));
				if (ENFNBuckets.getValue().getReportScopeRelation().containsKey(ENFNBuckets.getKey()))
					toInsert.put("ReportScopeRelation",
							new JSONArray(ENFNBuckets.getValue().getReportScopeRelation().get(ENFNBuckets.getKey())));
				if (toInsert.toString() != "null")
					BucketCreator.InsertToMongoDB(toInsert, ENFNBuckets.getKey().toLowerCase());
			} catch (OutOfMemoryError | JSONException ooME) {
				System.out.println(ooME.getMessage() + " @" + ENFNBuckets.getKey());
			}
		}
	}

	@SuppressWarnings("unused")
	private int getFNLength(){
		Set<String> keys = getFNMap().keySet();
		return keys.size();
	}

	@SuppressWarnings("unused")
	private int getNFNLength(){
		Set<String> keys = getNFNMap().keySet();
		return keys.size();
	}

	public Map<String, Bucket> getFNMap(){
		return BucketCreator.getFNMap();
	}

	public Map<String, Bucket> getNFNMap(){
		return BucketCreator.getNFNMap();
	}

	public String getFNSize(){
		return "BC.getFNKeySize() = " + BC.getFNKeySize();
	}

	public String getNFNSize(){
		return "BC.getNFNKeySize() = " + BC.getNFNKeySize();
	}

	public void findwrong(){
		Set<String> keys = getFNMap().keySet();
		Set<String> keys2 = BC.getFNKeys();
		for (String s : keys) {
			if (keys2.contains(s)) {
				continue;
			} else {
				// System.out.println(s);
			}
		}
	}

	public void CloseDB(){
		BucketCreator.closeDB();
	}
}
