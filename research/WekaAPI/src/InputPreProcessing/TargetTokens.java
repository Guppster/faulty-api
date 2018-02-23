package InputPreProcessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TargetTokens {
	List<String> TargetTokens;
	File Dataset;
	String rootPath = "../../../../../../../home/or10n/workspace/Diploma/DatasetGenerator/Dataset/amarokARFF/";
	public TargetTokens(String Dataset){
		this.Dataset = new File(rootPath + Dataset);
		TargetTokens = new ArrayList<String>();
	}
	
	public void findTargetTokens(){
		String Buffer;
		String[] BufferTokens;
		try{
			BufferedReader DatasetReader = new BufferedReader(new FileReader(Dataset));
			while(DatasetReader.ready()){
				Buffer = DatasetReader.readLine();
				BufferTokens = Buffer.split(" ");
				for(String s:BufferTokens){
					if(s.startsWith("res_")){
						if(s.endsWith("\""))
							s = s.substring(0, s.length()-1);
					TargetTokens.add(s);
					}
				}
			}
			DatasetReader.close();
		}catch(IOException ioe){
			System.out.println(ioe.getMessage());
		}
	}
	
	public void RemoveDuplicates(){
		List<String> noDups = new ArrayList<String>();
		for(String s:TargetTokens){
			if(!noDups.contains(s)){
				noDups.add(s);
			}
		}
		TargetTokens = noDups;
	}
	
	public List<String> getTargetTokens(){
		return TargetTokens;
	}
	
	public void PrintTargetTokens(){
		for(String s:TargetTokens){
			System.out.println(s);
		}
	}
}
