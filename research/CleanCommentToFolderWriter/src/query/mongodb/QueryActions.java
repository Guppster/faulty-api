package query.mongodb;

import com.mongodb.DBObject;

public interface QueryActions {
	DBObject[] query(String Field, String Value);
}
