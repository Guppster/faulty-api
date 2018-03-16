package TokenContainer;

public class Token {
	private String token;
	private String tag;
	private int position;
	
	public Token(String newToken, String newTag, int newPosition){
		token = newToken;
		tag = newTag;
		position = newPosition;
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String newToken){
		token = newToken;
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setTag(String newTag){
		tag = newTag;
	}
	
	public int getPosition() {
		return position;
	}
	
	public void setPosition(int newPosition){
		position = newPosition;
	}
}
