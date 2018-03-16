package init.NLP;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import opennlp.tools.cmdline.postag.POSModelLoader;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class POSTager {
	static POSModel model = null;
	static POSTaggerME tagger;
	
	public POSTager(){
		model = new POSModelLoader().load(new File("models/en-pos-maxent.bin"));
		tagger = new POSTaggerME(model);
	}

	public JSONObject POSTag(String input){
//		String input = "Hi. How are you? This is Mike.";
		ObjectStream<String> lineStream = new PlainTextByLineStream(new StringReader(input));
	 
//		perfMon.start();
		String line;
		String[] tags = null;
//		POSSample sample = null;
		String whitespaceTokenizerLine[] = null;
		try{
			while ((line = lineStream.read()) != null) {
				
				whitespaceTokenizerLine = WhitespaceTokenizer.INSTANCE
						.tokenize(line);
				tags = tagger.tag(whitespaceTokenizerLine);
				//Create a new POSSample representing Tokens with Tags.
//				sample = new POSSample(whitespaceTokenizerLine, tags);
				//System.out.println(sample.toString());
				
//			perfMon.incrementCounter();
			}
		}catch(IOException ioe){
			System.out.println("POSTager.java IOERROR :" + ioe.getMessage());
		}
//		perfMon.stopAndPrintFinalResult();
		
		//create response
		JSONObject response = new JSONObject();
		
		try {
			JSONArray tagsResponse = new JSONArray(tags);
			JSONArray tokensResponse = new JSONArray(whitespaceTokenizerLine);
			response.put("tags", tagsResponse);
			response.put("tokens", tokensResponse);
		} catch (JSONException jne) {
			System.out.println("POSTager.java JSON ERROR :" + jne.getMessage());
		}
		

		return response;
				
	}
}
