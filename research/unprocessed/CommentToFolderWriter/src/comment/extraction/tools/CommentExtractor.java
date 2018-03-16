package comment.extraction.tools;

import init.mongodb.MongoDBConnection;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import query.mongodb.QueryMongo;

import com.mongodb.DBObject;

// 	TODO Fetch Comments for whole Association Chain not for single Entry

public class CommentExtractor{

	String FileName = "";
	String ProductName = "";
	Map<Integer, List<Integer>> DependenciesMap = null;
	Map<Integer, List<String>> CommentMap = new HashMap<Integer, List<String>>();
	MongoDBConnection mdbc;
	QueryMongo qm;
	Map<Integer, List<Integer>> BlocksAllMap = new HashMap<Integer, List<Integer>>();
	Map<Integer, List<Integer>> BlockToDepends = new HashMap<Integer, List<Integer>>();

	public CommentExtractor(String Name, String DBName, String DBCollection){
		FileName = Name + "_dependencies.ser";
		ProductName = Name;
		mdbc = new MongoDBConnection(DBName, DBCollection);
		qm = new QueryMongo(mdbc);
	}

	private void reverseDepends_onTo_Blocks(Map<Integer, List<Integer>> dependencies){
		Set<Integer> keys = dependencies.keySet();
		for (Integer i : keys) {
			for (Integer j : dependencies.get(i)) {
				if (BlockToDepends.containsKey(j)) {
					BlockToDepends.get(j).add(i);
				} else {
					BlockToDepends.put(j, new ArrayList<Integer>());
					BlockToDepends.get(j).add(i);
				}
			}
		}
		// printMap(BlockToDepends);
	}

	private void createDepends_onMap(Map<Integer, List<Integer>> dependencies){

		Set<Integer> keys = dependencies.keySet();
		// System.out.print("keys.size() = " + dependencies.keySet().size());
		for (Integer key : keys) {
			if (dependencies.containsKey(key)) {
				if (!dependencies.get(key).isEmpty()) {
					BlocksAllMap.put(key, new ArrayList<Integer>());
					createDependentMap(dependencies, key);
				}
			}
		}
	}

	private void createDependentMap(Map<Integer, List<Integer>> dependencies, Integer id){
		if (dependencies.containsKey(id))
			if (!dependencies.get(id).isEmpty()) {

				// System.out.print("-> {");
				for (Integer idInner : dependencies.get(id)) {
					BlocksAllMap.get(id).add(idInner);
					createDependentMap(dependencies, idInner);
				}
			}
	}

	@SuppressWarnings("unused")
	private void printMap(Map<Integer, List<Integer>> Map){
		Set<Integer> keys = Map.keySet();
		for (Integer key : keys) {
			System.out.print(key + "->");
			for (Integer i : Map.get(key)) {
				System.out.print(i + " ");
			}
			System.out.println();
		}
	}

	@SuppressWarnings("unchecked")
	public void DeserializeAndPrepareData(){
		HashMap<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		try {
			FileInputStream fis = new FileInputStream(FileName);
			ObjectInputStream ois = new ObjectInputStream(fis);
			map = (HashMap<Integer, List<Integer>>) ois.readObject();
			ois.close();
			fis.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
			return;
		}
		DependenciesMap = map;
		// System.out.println("DependenciesMap");
		// printMap(DependenciesMap);
		// System.out.println("-------------------------");
		// System.out.println("BlocksAllMap");
		reverseDepends_onTo_Blocks(DependenciesMap);
		createDepends_onMap(BlockToDepends);
		// printMap(BlocksAllMap);
		// printMap(BlocksAllMap);
		return;
	}

	public void SerializeCommentMap(String product, Map<Integer, List<String>> CommentMap){
		try {
			FileOutputStream fos = new FileOutputStream(product + "_CommentMap.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(CommentMap);
			oos.close();
			fos.close();
			System.out.printf("Serialized HashMap data is saved in " + product + "_CommentMap.ser");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void CreateCommentMap(String product){
		FetchComments(BlocksAllMap);
		SerializeCommentMap(product, CommentMap);
		// -----------
		// for(Integer id:CommentMap.keySet()){
		// System.out.println(id);
		// if(!CommentMap.get(id).isEmpty())
		// for(String s:CommentMap.get(id)){
		// System.out.println(s);
		// System.out.println("--------------------------------------------------------------------------------------");
		// }
		// System.out.println("\n--------------------------------------------------------------------------------------");
		// System.out.println("--------------------------------------------------------------------------------------");
		// }
		// -----------
	}

	public void FetchComments(Map<Integer, List<Integer>> DependenciesMap){
		Set<Integer> MapKeys = null;
		if (!DependenciesMap.isEmpty()) {
			MapKeys = DependenciesMap.keySet();
		} else {
			System.out.println("!!No Reports!!");
			return;
		}

		for (Integer Mapi : MapKeys) {
			List<Integer> CurrIds = DependenciesMap.get(Mapi);
			List<String> temp = new ArrayList<String>();
			temp = getComments(Mapi);
			if (temp != null)
				CommentMap.put(Mapi, temp);
			else
				CommentMap.put(Mapi, new ArrayList<String>());
			for (Integer id : CurrIds) {
				temp = getComments(id);
				if (temp != null)
					CommentMap.put(id, temp);
				else
					CommentMap.put(id, new ArrayList<String>());
			}
			// CommentMap.get(Mapi).(new ArrayList<String>()));
		}

	}

	private List<String> getComments(Integer id){
		DBObject[] r = qm.query("id", id.intValue());
		JSONObject result = null;

		if (r.length >= 1) {
			try {
				result = new JSONObject(new JSONTokener(r[0].toString()));
			} catch (JSONException e) {
				System.out.println(e.getMessage());
			}
		} else {
			return null;
		}
		List<String> CommentsList = new ArrayList<String>();
		JSONArray Comments = null;
		try {
			Comments = result.getJSONArray("comments");
			for (int i = 0; i < Comments.length(); i++) {
				CommentsList.add(Comments.getJSONObject(i).getString("text"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (CommentsList.isEmpty())
			return null;
		else
			return CommentsList;
	}

	@SuppressWarnings("unchecked")
	public void DeserializeMap(){
		HashMap<Integer, List<String>> map = new HashMap<Integer, List<String>>();
		try {
			FileInputStream fis = new FileInputStream(ProductName + "_CommentMap.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			map = (HashMap<Integer, List<String>>) ois.readObject();
			ois.close();
			fis.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
			return;
		}
		CommentMap = map;
		// reverseDepends_onTo_Blocks(DependenciesMap);
		// createDepends_onMap(BlockToDepends);
		// printMap(BlocksAllMap);
		return;
	}

	public Map<Integer, List<String>> getCommentMap(){
		// printMap(BlocksAllMap);
		return CommentMap;
	}

	public Map<Integer, List<Integer>> getBlockToDepends(){
		return BlockToDepends;
	}

}
