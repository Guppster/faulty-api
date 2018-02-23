package bugReport.fetch.nlpProcess;

import init.mongodb.MongoDBConnection;
import query.mongodb.QueryMongo;

import com.mongodb.DBObject;

public class BugReportFetch{
	private DBObject[] Reports = null;
	private String Field = null;
	private String Value = null;
	private String DatabaseName = null;

	public BugReportFetch(String Field, String Value, String DatabaseName){
		this.Field = Field;
		this.Value = Value;
		this.DatabaseName = DatabaseName;
	}

	public void fetchReports(){
		MongoDBConnection mdbc = new MongoDBConnection(DatabaseName, "BUGS");
		QueryMongo qm = new QueryMongo(mdbc);
		Reports = qm.query(Field, Value);
	}

	public DBObject[] getReports(){
		return Reports;
	}
}
