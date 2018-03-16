package Stemmer;


public class StemmToken {
	String StemmedString = null;
	PorterStemmer PS = null;
	public StemmToken(){
		PS = new PorterStemmer();
	}
	public String StemIt(String InitialString){
		PS.reset();
		StemmedString = PS.stem(InitialString);
		return StemmedString;
	}
}
