package init.mongodb;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

public class MongoGridFSQuery{
	private Mongo mongo = null;
	private DB db = null;
	private GridFS gfs = null;
	private static MongoGridFSQuery instance = null;

	private MongoGridFSQuery(String DatabaseName, String CollectionName){
		mongo = initializeMongoClient();
		db = mongo.getDB(DatabaseName);
		gfs = new GridFS(db, CollectionName);
		// gfs = new GridFS(db, CollectionName);
	}

	public static MongoGridFSQuery getInstance(String DatabaseName, String CollectionName){
		if (instance == null) {
			instance = new MongoGridFSQuery(DatabaseName, CollectionName);
		}
		return instance;
	}

	private Mongo initializeMongoClient(){
		try {
			return new MongoClient();
		} catch (UnknownHostException ue) {
			System.out.println(ue.getMessage());
			return null;
		}
	}

	public JSONObject GetFile_GridFS(String FileName){
		JSONObject result = new JSONObject();
		DBObject query = new BasicDBObject();
		query.put("filename", FileName);
		GridFSDBFile ans = null;
		ans = gfs.findOne(query);
		if (ans == null) {
			return result;
		} else {
			ans.getInputStream();
			InputStream is = ans.getInputStream();
			String a = "";
			try {
				int c = is.read();
				while (c != -1) {
					a += "" + (char) c;
					c = is.read();
				}
				// System.out.println(a);
				result = new JSONObject(a);
			} catch (IOException ioe) {
				ioe.printStackTrace();

			} catch (JSONException jne) {
				System.out.println(jne.getMessage());
			}
		}
		return result;
	}

	public void closeDB(){
		mongo.close();
	}
}
