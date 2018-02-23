package comment.extraction.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentLoader {
	String FileName = "";
	Map<Integer, List<String>> CommentMap = null;
	public CommentLoader(String FileName){
		this.FileName = FileName;
	}
	@SuppressWarnings("unchecked")
	public void DeserializeData(String name){
		HashMap<Integer, List<String>> map = new HashMap<Integer,List<String>>();
		try
		{	
			FileInputStream fis = new FileInputStream(name);
			ObjectInputStream ois = new ObjectInputStream(fis);
			map = (HashMap<Integer,List<String>>) ois.readObject();
			ois.close();
			fis.close();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}catch(ClassNotFoundException c){
			System.out.println("Class not found");
			c.printStackTrace();
		}
		CommentMap = map;
	}
	
	public Map<Integer, List<String>> getCommentMap(){
		return CommentMap;
	}
	
}
