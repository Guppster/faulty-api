package query.mongodb;

import init.mongodb.MongoDBConnection;

import java.util.List;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class QueryMongo implements QueryActions{
	String field;
	String value;
	private MongoDBConnection mongoToBeQueried;
	private DBObject[] query = new DBObject[1];
	private DBCollection LocalCollection;

	public QueryMongo(MongoDBConnection mdbc){
		mongoToBeQueried = mdbc;
		mongoToBeQueried.getDBCollection(mongoToBeQueried.getActiveCollectionName());
		LocalCollection = mdbc.getCollection();
	}

	/**
	 * query(String field, String value) returns DBObject[]. The DBObject[] contains all query results where field equals value
	 */
	public DBObject[] query(String field, String value){
		query[0] = (DBObject) JSON.parse("{'bugs.0." + field + "':'" + value + "'}");
		DBCursor CollectedResult = (LocalCollection.find(query[0]));
		DBObject[] result = new DBObject[CollectedResult.count()];
		int total = result.length;
		int i = 0;
		while (i < total) {
			if (!CollectedResult.hasNext())
				System.out.println("Sorry No Record Satisfying Given Query Criteria Found In Database!");
			else {
				result[i] = CollectedResult.next();
				// System.out.println(CollectedResult.next());
				i++;
			}
		}
		// System.out.println(result.length);
		return result;
	}

	/**
	 * @param id
	 *            takes a bug id as input type int
	 * @return returns an array of DBObject that contain corresponding bugReports
	 */

	public DBObject[] query(String field, int value){
		query[0] = (DBObject) JSON.parse("{'bugs.0." + field + "':" + value + "}");
		DBCursor CollectedResult = (LocalCollection.find(query[0]));
		DBObject[] result = new DBObject[CollectedResult.count()];
		int i = 0;
		if (!CollectedResult.hasNext())
			System.out.println("Sorry No Record Satisfying Given Query Criteria Found In Database!");
		else {
			result[i] = CollectedResult.next();
			// System.out.println(result[i]);
			i++;
		}
		// System.out.println(result.length);
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<String> queryDistinctGeneral(String field){
		List<String> CollectedResult = (List<String>) (LocalCollection.distinct("bugs.0." + field + ""));

		return CollectedResult;
	}

}
