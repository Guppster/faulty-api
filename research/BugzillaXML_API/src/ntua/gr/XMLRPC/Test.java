package ntua.gr.XMLRPC;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class Test {
	public static void main(String[] args){
		try{
	    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
	    config.setEncoding("UTF-8");
	    config.setServerURL(new URL("https://bugzilla.gnome.org/xmlrpc.cgi"));
	    XmlRpcClient client = new XmlRpcClient();
	    client.setConfig(config);
	//    Map<String,Integer> params = new HashMap<String,Integer>();
	    Map<String,ArrayList<Integer>> params = new HashMap<String,ArrayList<Integer>>();
	    ArrayList<Integer> ids = new ArrayList<Integer>();
	    	ids.add(123123);
	    params.put("ids",ids);
	//    Get Bug Report Data
	    Object result =  (Object) client.execute("Bug.get", new Object[] {params});
	    if(result == null){}
		}catch(MalformedURLException mue){
			mue.printStackTrace();
		}catch(XmlRpcException xre)
		{
			xre.printStackTrace();
		}
		}
}
