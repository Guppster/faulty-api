import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;


public class ImportJSONToMongoDB {
	
	public static void importJSONFileToDBUsingJavaDriver(String pathToFile, DB db, String collectionName) {
	    // open file
	    FileInputStream fstream = null;
	    try {
	        fstream = new FileInputStream(pathToFile);
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	        System.out.println("file not exist, exiting");
	        return;
	    }
	    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

	    // read it by line and convert to JSONObject then to MongoJSON
	    String strLine;
	    DBCollection newColl =   db.getCollection(collectionName);
	    JSONObject jsontemp = new JSONObject();
	    try {
	        while ((strLine = br.readLine()) != null) {
	            // convert line by line to BSON
	        	DBObject[] bson = new DBObject[1];
	        	jsontemp = new JSONObject(strLine);
//	        	JSONObject jsonbuff = new JSONObject(jsontemp.getJSONArray("bugs"));
//	        	JSONArray jarray = new JSONArray(jsontemp);
//	        	System.out.println(jsontemp.toString());
	            bson[0] = (DBObject) JSON.parse(jsontemp.toString());
	            // insert BSONs to database
	            try {
	                newColl.insert(bson);
	            }
	            catch (MongoException e) {
	              // duplicate key
	              e.printStackTrace();
	            }


	        }
	        br.close();
	    } catch (IOException | JSONException e) {
	        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
	    }


	}
	public static void main(String[] args){
		ImportJSONToMongoDB Importer = new ImportJSONToMongoDB();
		try{
		MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB("BugzillaMozillaDB");
		int i;
		int j;
		String baseFileAddress = "D:/JSONBugFromRESTofBUGZILLA@MOZILLA/Saved/";
		for(i = 2; i <=11; i++){
			for(j = (i-1)*10*10000; j < i*10*10000; j+=10000){
				importJSONFileToDBUsingJavaDriver(baseFileAddress + i + "/" + "JSON_BUGS" + j + "to" + (j+10000-1) + ".txt", db, "BUGS");	
			}
		}
		}catch(UnknownHostException | MongoException me){
			me.printStackTrace();
		}
	}
}
