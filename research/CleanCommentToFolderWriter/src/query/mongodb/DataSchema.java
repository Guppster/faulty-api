package query.mongodb;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

public class DataSchema implements DiscoverSchema{
	@SuppressWarnings("unchecked")
	public List<String> FindJsonConstructAllKeys(JSONObject Data){
		ArrayList<String> Keys = new ArrayList<String>();
		Iterator<String> keys = Data.keys();
			while(keys.hasNext()){
				Keys.add(keys.next());
			}
		
		return Keys;
	}
	@SuppressWarnings("unchecked")
	public String convertHashMap2JsonString(Object Data, String key){
		String JsonFormattedData = "";
		switch(Data.getClass().toString()){
				case "class java.lang.String" :
					if(key != "")
						JsonFormattedData += "\"" + (key) + "\":";
					JsonFormattedData += "\"" + ((String)Data) + "\"";
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
					JsonFormattedData +=buf;
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
}
