package mainMethod;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import weka.associations.FPGrowth.AssociationRule;
import weka.core.converters.ArffSaver;
import Dataset.Preparator.AssociationRetriever;
import FPGrowthImplimentation.FPGWrapper;
import InputPreProcessing.InputPreprocessor;
import InputPreProcessing.InputReader;
import InputPreProcessing.Lexicon;
import InputPreProcessing.Lexicon.Lexicon_Exception;
import InputPreProcessing.TargetTokens;

public class Runner{

	public static void main(String[] args){
		// ---Attribute Declaration---
		AssociationRetriever.print_fileList();
		String pp = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		List<AssociationRule> Rules = null;
		List<AssociationRule> GoodRules = new ArrayList<AssociationRule>();
		Lexicon targetTokens = null;
		// ---Program Start---
		System.out.print("Please insert desired bug's Dataset id: ");
		try {
			pp = br.readLine();
			pp += "_dataset.arff";
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		// ---Dictionary Creation---
		try {
			targetTokens = new Lexicon(pp);
		} catch (Lexicon_Exception LE) {
			System.out.println(LE.getMessage());
		}
		if (Lexicon.getStatus() == 0) {
			targetTokens.PopulateLexicon();
		}
		// ---WEKA InputReader Creation---
		InputReader ir = new InputReader(pp);
		InputPreprocessor ip = new InputPreprocessor(ir.readInput());
		TargetTokens TT = new TargetTokens(pp);
		// ---WEKA InputFiltering---
		TT.findTargetTokens();
		TT.RemoveDuplicates();
		// TT.PrintTargetTokens();
		// ---WEKA FPGrowth Algorithm Initialization and run---
		FPGWrapper fpgw = new FPGWrapper(ip.getWVNTN_DATA(), TT.getTargetTokens());
		fpgw.setOptions();
		fpgw.Execute();
		Rules = fpgw.getRules();
		// ---Selection of Interesting Rules from the Set of GeneratedRules Using Dictionary---
		GoodRules = targetTokens.selectGoodRules(Rules);
		for (AssociationRule ar : GoodRules) {
			System.out.println(ar.toString());
		}
		// ---EXPERIMENTAL---
		// ---Saving of resulting filtered Input Dataset in a dummy File---
		ArffSaver saver = new ArffSaver();
		saver.setInstances(ip.getWVNTN_DATA());
		try {
			saver.setFile(new File("./test.arff"));
			// saver.setDestination(new File("./data/test.arff")); // **not** necessary in 3.5.4 and later
			saver.writeBatch();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}

	}

}
