package query.mongodb;

import java.util.List;

import org.json.JSONObject;


public interface DiscoverSchema {
	
	public List<String> FindJsonConstructAllKeys(JSONObject Data);
	
	public String convertHashMap2JsonString(Object Data, String key);
}
