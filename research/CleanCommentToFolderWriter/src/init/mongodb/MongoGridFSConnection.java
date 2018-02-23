package init.mongodb;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import org.apache.commons.io.IOUtils;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

public class MongoGridFSConnection{

	private Mongo mongo = null;
	private DB db = null;
	private GridFS gfs = null;
	@SuppressWarnings("unused")
	private GridFSInputFile gfsIF = null;
	private static MongoGridFSConnection instance = null;

	private MongoGridFSConnection(String DatabaseName, String CollectionName){
		// private MongoGridFSConnection(String DatabaseName){
		mongo = initializeMongoClient();
		db = mongo.getDB(DatabaseName);
		gfs = new GridFS(db, CollectionName);
		// gfs = new GridFS(db);
	}

	public static MongoGridFSConnection getInstance(String DatabaseName, String CollectionName){
		if (instance == null) {
			instance = new MongoGridFSConnection(DatabaseName, CollectionName);
			// instance = new MongoGridFSConnection(DatabaseName);
		}
		return instance;
	}

	public void InsertGridFSInMongo(String data, Integer ID, String NameSuffix){
		try {
			InputStream InStream = IOUtils.toInputStream(data, "UTF-8");
			GridFSInputFile gfsIF = gfs.createFile(InStream, ID + NameSuffix);
			gfsIF.save();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}

	}

	private MongoClient initializeMongoClient(){
		try {
			return new MongoClient();
		} catch (UnknownHostException ue) {
			System.out.println(ue.getMessage());
			return null;
		}
	}

	public boolean connectionOpen(){
		if (instance == null)
			return false;
		else
			return true;
	}

	public void closeDB(){
		mongo.close();
	}

}
