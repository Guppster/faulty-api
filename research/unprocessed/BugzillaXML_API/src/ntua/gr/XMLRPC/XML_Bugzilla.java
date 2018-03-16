package ntua.gr.XMLRPC;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;


public class XML_Bugzilla {
	
	static protected String createUserDir(final String dirName) {
		String homeDiraddress = "./XML2JSONBUGS/";
		try{
	    final File homeDir = new File(homeDiraddress);
	    final File dir = new File(homeDir, dirName);
	    if (!dir.exists() && !dir.mkdirs()) {
	        System.out.println("Unable to create " + dir.getAbsolutePath());
	    }else{
	    	homeDiraddress += dirName;
	    }
		} catch (Exception ioe) {
			System.out.println("can't create dir");
		}
		return homeDiraddress;
	}
		
	public static void main(String[] args){
		int j;
		int start = 0;
		int end = 0;
		System.setProperty("jsse.enableSNIExtension", "false");
		String URL1 = "www.gnome.org";
		String URL = "https://bugzilla.gnome.org/xmlrpc.cgi";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try{
			System.out.print("please give host url <Example> :\"www.mozilla.org\": ");
			URL1 = br.readLine();
			System.out.println("-------------------------------------------------------------------------------");
			System.out.print("please give bugzilla XML-RPC endpoint of given host <Example> \"https://bugzilla.mozilla.org/xmlrpc.cgi\" : ");
			URL = br.readLine();
			System.out.print("please first bug_id : ");
			start = Integer.parseInt(br.readLine());
			System.out.print("please insert last bug_id : ");
			end = Integer.parseInt(br.readLine());
			br.close();
		}catch(IOException ioe){
			ioe.printStackTrace();
		};
		String homeDir = createUserDir(URL1+"/From" + start + "TO" + end);
		String SaveDir = createUserDir(URL1+"/Aggregate");
		if(URL!=""){
			String[] URLmat = new String[1];
			URLmat[0] = URL1;
			try{
			InstallCert.installcacert(URLmat);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
//		On https://bugzilla.gnome.org/xmlrpc.cgi bug_id 1290 causes a fatal error -> I should use for debugging purposes
		final long chronos = System.currentTimeMillis();
		System.out.println(chronos);
		ExecutorService ES = Executors.newCachedThreadPool();
		XmlRpcClient client = new XmlRpcClient();
			 	try {
	    	XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setEncoding("UTF-8");
			config.setEnabledForExceptions(true);
			config.setEnabledForExtensions(true);
			config.setServerURL(new URL(URL));
			
			client.setConfig(config);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
	    for(j = start;j<end+1;j++){ 
	   
//			client.setConfig(config);
//			TracHttpClientTransportFactory factory=new TracHttpClientTransportFactory(client,new HttpClient());
//			client.setTransportFactory(factory);
//			XmlRpcCommonsTransportFactory transportFactory=new XmlRpcCommonsTransportFactory(client);
//			transportFactory.setHttpClient(new HttpClient());
//			client.setTransportFactory(transportFactory);
		    
	    	if(j%500 == 0)
	    		System.out.println("j: "+  j);
			
//	    		 try {
//						Thread.sleep(5000);
//					} catch (InterruptedException e) {
//						// 
//					}
	    	
			
			    
		//			    Map<String,Integer> params = new HashMap<String,Integer>();
			    Map<String,ArrayList<Integer>> params = new HashMap<String,ArrayList<Integer>>();
			    ArrayList<Integer> ids = new ArrayList<Integer>();
	//		    for(j = 1290;j<1303;j++){
//		    	ids.clear();
		    	ids.add(j);
		    	params.put("ids",ids);
		    	
//			    Initialize
//			    JSONObject BugReportObject = new JSONObject();
//			    Get Bug Report Data
//	*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*
//	Asynchronous XMLRP Call Begin -----------------------------------------------------------------------------------
//			    BugAsyncCallback BACb = new BugAsyncCallback(BugReportObject,ES);
//			    client.executeAsync("Bug.get", new Object[] {params}, BACb);
//	Asynchronous XMLRP Call End -------------------------------------------------------------------------------------
		
		    	
//	Synchronous Call bug---------------------------------------------------------------------------------------------
//			    Object result =  (Object) client.execute("Bug.get", new Object[] {params});
//			    Map<String, Object> resultMap = (Map<String, Object>) result;
//			    BugData BugSchema = new BugData(resultMap);
//			    JSONObject jsonBug = null;
//			    jsonBug = BugSchema.createJsonObject();
//			    if(jsonBug!=null)
//			    	System.out.println(jsonBug.toString());
//	Synchronous Call end bug-----------------------------------------------------------------------------------------			    
//  Thread-ing Implementation Begin----------------------------------------------------------------------------------
			    ES.execute(new BugDataWorker(client, params, ids,homeDir));
			    try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}
//  Thread-ing Implementation End------------------------------------------------------------------------------------
//			    Get Bug Report Comments
//	*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*~*
			    
			    
			    
//	Asynchronous XMLRP Call Begin -----------------------------------------------------------------------------------
//			    CommentAsyncCallback CACb = new CommentAsyncCallback(BugReportObject,ES);
//			    client.executeAsync("Bug.comments", new Object[] {params}, CACb);
//	Asynchronous XMLRP Call End -------------------------------------------------------------------------------------
			    
//			     Pass the client into the Runnable Task so that You can control the flow and return of the program using 
//			     ExecutorService
			    
//	Synchronous Call Comment-----------------------------------------------------------------------------------------	    
//			    result = (Object) client.execute("Bug.comments", new Object[] {params});
//			    Map<String, Object> CommentMap = (Map<String, Object>) result;
//			    Map<String,Object> CommentMap_bug = (Map<String, Object>) CommentMap.get("bugs");
//			    Map<String,Object> CommentMap_bug_id = (Map<String,Object>) CommentMap_bug.get((ids.get(0).toString()));
//			    Object[] CommentMap_bug_id_comment = (Object[]) CommentMap_bug_id.get("comments");
////			    Here the key of the Map<String, Object> is the id that whas used as parameter to the XML-RPC function
//			    BugComment CommentSchema = new BugComment(CommentMap_bug_id_comment);
//			    JSONObject jsonComment = null;
//			    jsonComment = CommentSchema.createJsonObject();
//		    
//			    if(jsonComment!=null)
//			    	System.out.println(jsonComment.toString());
//			}
//	Synchronous Call end Comment-------------------------------------------------------------------------------------
//			}catch(XmlRpcException  xce){
//	//				xce.printStackTrace();
//	//				System.out.println();
//			}
	    
				
	    }
//	    try{
//	    	if (j%100 == 0)
//	    		Thread.sleep(j*(j/10));
//	    }catch(InterruptedException ie){
//	    	ie.printStackTrace();
//	    }
	    	ES.shutdown();

	    	try{
	    		ES.awaitTermination(30, TimeUnit.MINUTES);
	    	}catch(InterruptedException ie){
	    		ie.printStackTrace();
	    	}
	    	while(!ES.isTerminated());
	    AggregateFiles af = new AggregateFiles(SaveDir, homeDir);
	    af.Aggregate(start, end);
	    System.out.println(System.currentTimeMillis() - chronos);
//	    try{
//		    br.readLine();
//		    br.close();
//	    }catch(IOException ioe){
//	    	ioe.printStackTrace();
//	    }

	}
}
