package NLPTool;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

public class Tokenizer {

	public static String[] Tokenize(String paragraph) throws InvalidFormatException, IOException {
		
		//always start with a model
		InputStream is = new FileInputStream("models/en-token.bin");
	 	TokenizerModel model = new TokenizerModel(is);
	 	TokenizerME tokenizer = new TokenizerME(model);
	 	
		String tokens[] = tokenizer.tokenize(paragraph);
	 
		is.close();
		return tokens;
	}
}
