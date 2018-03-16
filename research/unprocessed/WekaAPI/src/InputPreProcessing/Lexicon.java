package InputPreProcessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.associations.FPGrowth.AssociationRule;
/**
 * The Lexicon Class is a container 
 * for a Dictionary of terms found in the Resolved 
 * BugReport of a WekaReady .arff dataset
 * it also provides a set of methods for creating
 * and using the Dictionary(variable called "Lexicon"
 * TODO make the class also accept the name of the 
 * product in order to make it usefull for other
 * products as well
 * 
 * @author or10n
 *
 */
public class Lexicon {
	private Map<String, String> Lexicon;
	private BufferedReader br;
	private String rootPath = "../../../../../../../home/or10n/workspace/Diploma/DatasetGenerator/Dataset/amarokARFF/";
	private static int Status = 0;
	
	public class Lexicon_Exception extends IOException{
		String detailMessage = "";
		Lexicon_Exception(){
			detailMessage = "File was not a .arff WEKA dataset file";
		}

	}
	/**
	 * Lexicon Constructor 
	 * accepts String InputFileName and creates
	 * a BufferedReader for the corresponding File
	 * File must be a .arff file otherwise
	 * it will throw a Lexicon_Exception
	 * @param InputFileName
	 */
	public Lexicon(String InputFileName) throws Lexicon_Exception{
		if(!InputFileName.endsWith(".arff")){
			Status = 1;
			throw new Lexicon_Exception();
		}else{
			try {
				br = new BufferedReader(new FileReader(new File(rootPath + InputFileName)));
				Status = 0;
			} catch (FileNotFoundException e) {
				Status = 1;
				System.out.println(e.getMessage());
			}
			Lexicon = new HashMap<String, String>();			
		}
	}
	/** 
	 * @return
	 * returns the value of the Static field Status
	 * which is
	 * 0 if everything went ok with the creation of the BufferedReader
	 * 1 if the file was not a .arff
	 * 2 if anything else went wrong.
	 */
	public static int getStatus(){
		return Status;
	}
	/**
	 * I populates the Dictionary with terms it finds in the .arff
	 * file.
	 */
	public void PopulateLexicon(){
		String LineBuffer ="";
		String FromResolved = "";
		String[] FromResolvedTokenized; 
		try{
			while(br.ready()){
				LineBuffer = br.readLine();
				if(LineBuffer.startsWith("\"")){
					FromResolved = LineBuffer.split(" , ")[1];
					FromResolved = FromResolved.replace("\"", "");
					FromResolvedTokenized = FromResolved.split(" ");
					for(int i=0; i < FromResolvedTokenized.length; i++){
						if(!Lexicon.containsKey(FromResolvedTokenized[i]))
							Lexicon.put(FromResolvedTokenized[i], "Found");
					}
				}
			}			
		}catch(IOException ioe){
			System.out.println(ioe.getMessage());
		}
	}
	
	/**
	 * 
	 * @param allRules
	 * allRules is a List<AssociationRule> containing Rules derived from running a
	 * WEKA association rule Generator 
	 * @return
	 * returns another List of type List<AssociationRule> containing all Rules
	 * whose consequence contains only terms found in the Resolved BugReport 
	 * set of terms
	 */
	
	public List<AssociationRule> selectGoodRules(List<AssociationRule> allRules){
		List<AssociationRule> GoodRules = new ArrayList<AssociationRule>();
		for(AssociationRule ar:allRules){
			int ok = 0;
			String[] RuleBuf = null;
			String SpecificConsequence = ar.getConsequence().toString();
			RuleBuf = SpecificConsequence.substring(1,SpecificConsequence.length()-1).split(" ");
//			System.out.println(RuleBuf[0]);
			for(String bi:RuleBuf){
				if(bi.endsWith("=1"))
					bi = bi.replace("=1", "");
				if(bi.endsWith("=1,"))
					bi = bi.replace("=1,", "");
				if(!Lexicon.containsKey(bi)){
					ok = 1;
				}
			}
			if(ok == 0)
			GoodRules.add(ar);
		}
		return GoodRules;
	}
	
	
	
}
