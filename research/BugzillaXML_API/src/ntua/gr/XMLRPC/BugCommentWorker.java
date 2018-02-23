package ntua.gr.XMLRPC;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.json.JSONObject;

public class BugCommentWorker extends Thread implements Runnable{
	XmlRpcClient client = null;
	Map<String,ArrayList<Integer>> params;
	ArrayList<Integer> ids;
	JSONObject jsonComment = null;
	PrintWriter writer;
	
	
	BugCommentWorker(XmlRpcClient client, Map<String,ArrayList<Integer>> RequestData, ArrayList<Integer> ids){
		super();
		this.client = client;
		this.params= RequestData;
		this.ids = ids;
	}
	BugCommentWorker(XmlRpcClient client, Map<String,ArrayList<Integer>> RequestData, ArrayList<Integer> ids, PrintWriter writer){
		this(client,RequestData,ids);
		this.writer = writer;
	}
	
	@SuppressWarnings("unchecked")
	public void dorun(){
		try{
			
			Object result = (Object) client.execute("Bug.comments", new Object[] {params});
			
		    Map<String, Object> CommentMap = (Map<String, Object>) result;
		    Map<String,Object> CommentMap_bug = (Map<String, Object>) CommentMap.get("bugs");
		    Map<String,Object> CommentMap_bug_id = (Map<String,Object>) CommentMap_bug.get((ids.get(0).toString()));
		    Object[] CommentMap_bug_id_comment = (Object[]) CommentMap_bug_id.get("comments");
	//	    Here the key of the Map<String, Object> is the id that whas used as parameter to the XML-RPC function
		    BugComment CommentSchema = new BugComment(CommentMap_bug_id_comment);
		    jsonComment = CommentSchema.createJsonObject();
		
//		    if(jsonComment!=null)
//		    	System.out.println(jsonComment.toString());
		
			}catch(XmlRpcException xre){}
	}
	
	
@Override
@SuppressWarnings("unchecked")
	public void run(){
		try{
			
		Object result = (Object) client.execute("Bug.comments", new Object[] {params});
		
	    Map<String, Object> CommentMap = (Map<String, Object>) result;
	    Map<String,Object> CommentMap_bug = (Map<String, Object>) CommentMap.get("bugs");
	    Map<String,Object> CommentMap_bug_id = (Map<String,Object>) CommentMap_bug.get((ids.get(0).toString()));
	    Object[] CommentMap_bug_id_comment = (Object[]) CommentMap_bug_id.get("comments");
//    Here the key of the Map<String, Object> is the id that whas used as parameter to the XML-RPC function
	    BugComment CommentSchema = new BugComment(CommentMap_bug_id_comment);
	    jsonComment = CommentSchema.createJsonObject();
	
//	    if(jsonComment!=null)
//	    	System.out.println(jsonComment.toString());
	
		}catch(XmlRpcException xre){}
	}
}
