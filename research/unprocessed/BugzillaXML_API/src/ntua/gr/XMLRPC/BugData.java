package ntua.gr.XMLRPC;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public class BugData {
//	Local Variables Declaration------------------------------------------------
	private Map<String,Object> LocalBugReport = null;
	
//	Constructor declaration ---------------------------------------------------
	public BugData(Map<String,Object> BugReport){
		LocalBugReport = BugReport;
	}
//	Helper function for constructing Json--------------------------------------
@SuppressWarnings("unchecked")
	private String convertHashMap2JsonString(Object Data, String key){
	String JsonFormattedData = "";
	switch(Data.getClass().toString()){
			case "class java.lang.String" :
				if(key != "")
					JsonFormattedData += "\"" + AuxiliaryMethods.literalizeEscapes(key) + "\":";
				JsonFormattedData += "\"" + AuxiliaryMethods.literalizeEscapes((String)Data) + "\"";
				break;
		case "class [Ljava.lang.Object;" :
			int ObjectArrayLength;
			ObjectArrayLength = ((Object[])Data).length;
			if(key != "")
				JsonFormattedData += "\"" + key + "\":[";
			
			if(ObjectArrayLength != 0)
				for(int j = 0;j < ObjectArrayLength; j++)
				{
					JsonFormattedData += convertHashMap2JsonString(((Object)((Object[])Data)[j]),"");
					if(j < ObjectArrayLength-1)
						JsonFormattedData += ",";
				}	
			else
				JsonFormattedData += "";
			JsonFormattedData += "]";
			break;
		case "class java.lang.Boolean" :
			if(key != "")
				JsonFormattedData += "\"" + key + "\":";
			JsonFormattedData += (boolean)Data;
			break;
		case "class java.util.Date" :
			if(key != "")
				JsonFormattedData += "\"" + key + "\":";
			JsonFormattedData += "\"" + ((Date)Data).toString() + "\"";
			break;
		case "class java.lang.Integer" :
			if(key != "")
				JsonFormattedData += "\"" + key + "\":";
			JsonFormattedData += "" + ((Integer)Data).toString() + "";
			break;
		case "class java.util.HashMap" :
			int insideMapLength = 0;
			Map<String,Object> Content_Map = (Map<String,Object>)Data;
			Set<String> inKeySet = Content_Map.keySet();
			insideMapLength = inKeySet.size();
			if(key != "")
				JsonFormattedData += "\"" + key + "\":{";
			else 
				JsonFormattedData += "{";
			for(String inKey : inKeySet){
				String buf;
				buf = convertHashMap2JsonString(((Object)Content_Map.get(inKey)), inKey);
				JsonFormattedData += buf;
			if(insideMapLength > 1)
			{
				insideMapLength--;
				if(buf.length()!=0)
				JsonFormattedData +=",";
			}else{
				JsonFormattedData += "}";
			}
			}
			break;
		default:
			break;
	}
	return JsonFormattedData;
}

//	Main Method for constructing Json -----------------------------------------
	@SuppressWarnings("unchecked")
	public JSONObject createJsonObject(){
		int i = 0;
		String JsonBug_string = "";
		JSONObject JsonBug = null;
    	Object[] innerObject1 =(Object[]) LocalBugReport.get("bugs");
	    	for(i = 0; i < innerObject1.length;i++){
		    	Map<String, Object> innerObject= (Map<String, Object>) innerObject1[i];
		    	Set<String> keys = innerObject.keySet();
		    	JsonBug_string += "{\"bugs\":[{";
		    	int Repetitions = keys.size();
		    	int counter = 0;
		    	for(String key : keys){
		    		Object Content = innerObject.get(key);
		    		JsonBug_string += convertHashMap2JsonString(Content, key);
		    		counter++;
		    		if(counter < Repetitions)
		    			JsonBug_string +=",";
		    		else
		    			JsonBug_string += "}]}";
		    	}
	    	}
//	    	System.out.println(JsonBug_string);
	    	try{
	    		JsonBug = new JSONObject(JsonBug_string);
	    	}catch(JSONException jne){
//	    		jne.printStackTrace();
//	    		System.out.println(JsonBug_string);
	    		System.out.println("Data Phase : Unable to create JSONObject for bug_id = " + ((Map<String,Object>)((Object[])LocalBugReport.get("bugs"))[0]).get("id") + "\n");
	    	}
	    	return JsonBug;
	}
//	End of Class Declaration---------------------------------------------------
}
