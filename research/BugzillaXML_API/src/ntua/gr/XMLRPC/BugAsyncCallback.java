package ntua.gr.XMLRPC;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.AsyncCallback;
import org.json.JSONException;
import org.json.JSONObject;

public class BugAsyncCallback extends Thread implements AsyncCallback, Runnable {
	JSONObject BugReportObject; 
	ExecutorService ES = null;
	Object bugData;
	
	
	BugAsyncCallback(JSONObject BugReportObject, ExecutorService ES){
		super();
		this.BugReportObject = BugReportObject;
		this.ES = ES;
	}

	@Override
	public void handleError(XmlRpcRequest BugRequest, Throwable t) {
//		System.out.println("In Bug error");
//		t.printStackTrace();
	}

	@Override
	public void handleResult(XmlRpcRequest BugRequest, Object bugData) {
		this.bugData = bugData;		
		ES.execute(this);
	}
	
	@SuppressWarnings("unchecked")
	public void run(){
		Map<String, Object> resultMap = (Map<String, Object>) bugData;
	    BugData BugSchema = new BugData(resultMap);
	    JSONObject jsonBug = null;
	    jsonBug = BugSchema.createJsonObject();
	    if(!BugReportObject.keys().hasNext() && jsonBug!=null)
//	    	System.out.println(jsonBug.toString());
	    try{
	    	BugReportObject.put("bugs", jsonBug.getJSONArray("bugs"));
//	    System.out.println(jsonBug.getJSONArray("bugs").getJSONObject(0).get("id"));
	    }catch(JSONException jne){}
	}
}
