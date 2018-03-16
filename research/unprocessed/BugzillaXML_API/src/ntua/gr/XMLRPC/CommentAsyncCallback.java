package ntua.gr.XMLRPC;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.AsyncCallback;
import org.json.JSONException;
import org.json.JSONObject;

public class CommentAsyncCallback implements AsyncCallback, Runnable {
	JSONObject BugReportObject = null;
	ExecutorService ES = null;
	Object CommentData;
	CommentAsyncCallback(JSONObject BugReportObject, ExecutorService ES){
		super();
		this.BugReportObject = BugReportObject;
		this.ES = ES;
	}

	@Override
	public void handleError(XmlRpcRequest CommentRequest, Throwable t) {
//		System.out.println("In Comment Error");

	}
	String key;
	@Override
	public void handleResult(XmlRpcRequest CommentRequest, Object CommentData) {
		this.CommentData = CommentData;
		ES.execute(this);

	}
	@SuppressWarnings("unchecked")
	public void run(){
		Object[] CommentMap_bug_id_comment = null;
		Map<String,Object> CommentMap_bug_id = null;
	    Map<String, Object> CommentMap = (Map<String, Object>) CommentData;
	    Map<String,Object> CommentMap_bug = (Map<String, Object>) CommentMap.get("bugs");
	    Set<String> CommentMap_bug_keySet = CommentMap_bug.keySet();
	    for(String key : CommentMap_bug_keySet){
	    this.key = key;
	    CommentMap_bug_id = (Map<String,Object>) CommentMap_bug.get(key);
	    break;
	    }
	    if(CommentMap_bug_id!=null){
	    CommentMap_bug_id_comment = (Object[]) CommentMap_bug_id.get("comments");
	    }
//	    Here the key of the Map<String, Object> is the id that whas used as parameter to the XML-RPC function
	    BugComment CommentSchema = new BugComment(CommentMap_bug_id_comment);
	    JSONObject jsonComment = null;
	    jsonComment = CommentSchema.createJsonObject();
    
	    while(!BugReportObject.keys().hasNext()){}
	    	try{
	    	BugReportObject.put("comments", jsonComment.getJSONArray("comments"));
	    	}catch(JSONException jne){
	    	}
//	    	System.out.println(BugReportObject.toString());
	    	System.out.println("DONE FOR BUG_ID = " + key);

	}

}
