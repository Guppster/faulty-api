package bucket.implementation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

//Change implementation of Bucket Container
public class Bucket{
	public final String key;
	private Map<String, CopyOnWriteArraySet<String>> CommentScopeRelation = new HashMap<String, CopyOnWriteArraySet<String>>();
	private Map<String, CopyOnWriteArraySet<String>> ReportScopeRelation = new HashMap<String, CopyOnWriteArraySet<String>>();

	public Bucket(String key){
		this.key = key.toLowerCase();
	}

	public void insert_in_CommentScopeRelation(String Value){
		if (CommentScopeRelation.containsKey(key)) {
			CommentScopeRelation.get(key).add(Value.toLowerCase());
		} else {
			CopyOnWriteArraySet<String> Buffer = new CopyOnWriteArraySet<String>();
			CommentScopeRelation.put(key, Buffer);
			CommentScopeRelation.get(key).add(Value.toLowerCase());
		}
	}

	public void insert_in_ReportScopeRelation(String Value){
		if (ReportScopeRelation.containsKey(key)) {
			ReportScopeRelation.get(key).add(Value.toLowerCase());
		} else {
			CopyOnWriteArraySet<String> Buffer = new CopyOnWriteArraySet<String>();
			ReportScopeRelation.put(key, Buffer);
			ReportScopeRelation.get(key).add(Value.toLowerCase());
		}
	}

	public void insert_in_CommentScopeRelation(List<String> Value){
		if (CommentScopeRelation.containsKey(key)) {
			for (String s : Value)
				CommentScopeRelation.get(key).add(s.toLowerCase());
			// CommentScopeRelation.get(key).addAll(Value);
		} else {
			CopyOnWriteArraySet<String> Buffer = new CopyOnWriteArraySet<String>();
			CommentScopeRelation.put(key, Buffer);
			for (String s : Value)
				CommentScopeRelation.get(key).add(s.toLowerCase());
			// CommentScopeRelation.get(key).addAll(Value);
		}
	}

	public void insert_in_ReportScopeRelation(List<String> Value){
		if (ReportScopeRelation.containsKey(key)) {
			for (String s : Value)
				ReportScopeRelation.get(key).add(s.toLowerCase());
			// ReportScopeRelation.get(key).addAll(Value);
		} else {
			CopyOnWriteArraySet<String> Buffer = new CopyOnWriteArraySet<String>();
			ReportScopeRelation.put(key, Buffer);
			for (String s : Value)
				ReportScopeRelation.get(key).add(s.toLowerCase());
			// ReportScopeRelation.get(key).addAll(Value);
		}
	}

	public Map<String, CopyOnWriteArraySet<String>> getCommentScopeRelation(){
		return CommentScopeRelation;
	}

	public Map<String, CopyOnWriteArraySet<String>> getReportScopeRelation(){
		return ReportScopeRelation;
	}

	public String toString(){
		String output = "";
		output += "CommentScopeRelation :\n";
		output += key + ":\n";
		output += CommentScopeRelation.get(key).toString() + "\n";
		output += "ReportScopeRelation :\n";
		output += key + ":\n";
		output += ReportScopeRelation.get(key).toString() + "\n";
		return output;
	}

}
