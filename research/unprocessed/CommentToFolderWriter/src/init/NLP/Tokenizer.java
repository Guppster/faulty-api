package init.NLP;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class Tokenizer {
	
	static InputStream is;
	static TokenizerModel model;
	static TokenizerME tokenizer;
	
	public Tokenizer(){
		try{
			is = new FileInputStream("models/en-token.bin");
			model = new TokenizerModel(is);
			tokenizer = new TokenizerME(model);
			is.close();
		}catch(IOException ioe){
			System.out.println("Tokenizer.java ERROR :" + ioe.getMessage());
		}
	}

	public static String[] Tokenize(String paragraph){
		//always start with a model
		String tokens[] = tokenizer.tokenize(paragraph);
	 
		return tokens;
	}
}
