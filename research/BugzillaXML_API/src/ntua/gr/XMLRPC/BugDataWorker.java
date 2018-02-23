package ntua.gr.XMLRPC;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.json.JSONException;
import org.json.JSONObject;

public class BugDataWorker extends Thread implements Runnable{
	XmlRpcClient client = null;
	Map<String,ArrayList<Integer>> params;
	ArrayList<Integer> ids;
	JSONObject jsonBug = null;
	PrintWriter writer;
	String homeDir = "";
	
	BugDataWorker(XmlRpcClient client, Map<String,ArrayList<Integer>> params, ArrayList<Integer> ids){
		super();
		this.client = client;
		this.params = params;
		this.ids = ids;
	}
	
	BugDataWorker(XmlRpcClient client, Map<String,ArrayList<Integer>> params, ArrayList<Integer> ids, String homeDir){
		this(client,params,ids);
		this.homeDir = homeDir;
		writer = null;
	}
	private void createWriter(){
		try {
			writer = new PrintWriter(homeDir + "/" + ids.get(0) + ".txt","UTF-8");
		}catch(FileNotFoundException | UnsupportedEncodingException fnfe){
			fnfe.printStackTrace();
		}	
	}

@Override
@SuppressWarnings("unchecked")
	public void run(){
//	System.out.println("bug no :" + this.ids.get(0) + " started");
	
		try{
		Object result =  (Object) client.execute("Bug.get", new Object[] {params});
	    Map<String, Object> resultMap = (Map<String, Object>) result;
	    BugData BugSchema = new BugData(resultMap);
	    JSONObject jsonBug = null;
	    jsonBug = BugSchema.createJsonObject();
	    if(jsonBug!=null){
//	    	System.out.println("in");
	    	createWriter();
	    	BugCommentWorker BCWorker = new BugCommentWorker(client, params, ids,writer);
	    	BCWorker.dorun();
	    	try{
	    	if(BCWorker.jsonComment != null){
		    	jsonBug.put("comments", BCWorker.jsonComment.getJSONArray("comments"));
		    	writer.println(jsonBug.toString());
	    	}else{
	    		writer.println(jsonBug.toString());
	    	}
	    	}catch(JSONException jne){jne.printStackTrace();}
	    } else {
//	    	System.out.println("out");
	    }
//	    	System.out.println(jsonBug.toString());
		}catch(XmlRpcException xre){
			if (!xre.getMessage().contains("#")) {
				System.out.println("XMLRPCEXCEPTION = " + xre.getMessage());
			}
		}catch(Exception e){
			System.out.println("this is it" + e.getMessage());
			System.out.println("kati phge poly strava sto : " + ids.get(0));
		}
		if(writer != null)
			writer.close();
//		System.out.println("bug no :" + this.ids.get(0) + " ended");
	}
	
	
	
	
}
