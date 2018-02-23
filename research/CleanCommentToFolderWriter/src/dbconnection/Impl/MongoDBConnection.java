package dbconnection.Impl;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class MongoDBConnection implements MongoDBInitialization {
	DB db;
	MongoClient mongoClient;
	DBCollection collection;
	String activeCollectionName;
	// Vanilla Constructor
	public MongoDBConnection(){
	}
	public MongoDBConnection(String MongoDBName, String CollectionName){
		mongoClient = initializeMongoClient();
		db = createDB(MongoDBName);
		activeCollectionName = CollectionName;
	}
	
	public boolean checkCollectionExists(String CollectionName){
		if(db.collectionExists(CollectionName))
			return true;
		else
			return false;
	}
	
	public void getDBCollection(String CollectionName){
		if(checkCollectionExists(CollectionName))
			collection = db.getCollection(CollectionName);
		else{
			System.out.println("Collection " + CollectionName + " has been created");
			collection = db.getCollection(CollectionName);
		}
	}
	
	public DB createDB(String MongoDBName){
		if(mongoClient.getDatabaseNames().contains(MongoDBName))
			return mongoClient.getDB(MongoDBName);
		else
			System.out.println("Database didn't exist so I created it");
			return mongoClient.getDB(MongoDBName);
	}
	
	public MongoClient initializeMongoClient(){
		try{
			return new MongoClient();
		}catch(UnknownHostException uhe){
			System.err.print(uhe.getMessage());
			return null;
		}
	}
	public String getActiveCollectionName(){
		return activeCollectionName;
	}
	public DBCollection getCollection(){
		return collection;
	}
}
