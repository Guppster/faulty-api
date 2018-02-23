package init.NLP;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class SentenceDetection {
	static InputStream is;
	static SentenceModel model;
	static SentenceDetectorME sdetector;
	
	public SentenceDetection(){
		try{
			is = new FileInputStream("models/en-sent.bin");
			model = new SentenceModel(is);
			sdetector = new SentenceDetectorME(model);
			is.close();
		}catch(IOException ioe){
			System.out.println("SentenceDetection.java ERROR :" + ioe.getMessage());
		}
	}
	
	public String[] SentenceDetect(String paragraph){
		
		//always start with a model, a model is learned from training data
		String sentences[] = sdetector.sentDetect(paragraph);
		
	return sentences;
	}
}
