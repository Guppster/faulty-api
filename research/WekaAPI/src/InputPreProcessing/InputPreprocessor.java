package InputPreProcessing;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class InputPreprocessor {
	Instances WV_Data;
	Instances WVNTN_Data;
	StringToWordVector stwv = new StringToWordVector();
	NumericToNominal nTn = new NumericToNominal();
	public InputPreprocessor(Instances data){
//		String[] Options = new String[2];
//		Options[0] = "-R";
//		Options[1] = "1";
		try {
//			stwv.setOptions(Options);
			stwv.setInputFormat(data);
			WV_Data = Filter.useFilter(data, stwv);
			nTn.setInputFormat(WV_Data);
			WVNTN_Data = Filter.useFilter(WV_Data,nTn);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public Instances getWVNTN_DATA(){
		return WVNTN_Data;
	}
	public Instances getWV_DATA(){
		return WV_Data;
	}
	
}
