package ntua.gr.XMLRPC;

public class AuxiliaryMethods {
	protected static String literalizeEscapes(String input){
		String finalString = "";
		for (int i = 0; i < input.length(); i++){
			finalString += isEscaped(input.charAt(i));
		}
		return finalString;
	}
	private static String isEscaped(char c){
	String unescaped = "";
		switch(c) {
			case '\"': unescaped = "\\\"";
			break;
			case '\t': unescaped = "\\t";
			break;
			case '\b': unescaped = "\\n";
			break;
			case '\f': unescaped = "\\f";
			break;
			case '\r': unescaped = "\\r";
			break;
			case '\n': unescaped = "\\n";
			break;
			case '\'': unescaped = "\\'";
			break;
			case '\\': unescaped = "\\\\";
			break;
				default :  unescaped = "" + c ;
		}	
		return unescaped;
	}
}
