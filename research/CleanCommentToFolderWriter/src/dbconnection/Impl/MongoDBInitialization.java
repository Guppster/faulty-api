package dbconnection.Impl;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public interface MongoDBInitialization {

	boolean checkCollectionExists(String CollectionName);
	
	DB createDB(String MongoDBName);
	
	MongoClient initializeMongoClient();
}
