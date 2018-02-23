package InputPreProcessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
///home/or10n/Desktop/Diploma_Knowledge/Weka&&Manuals/Weka/weka-3-6-12
public class InputReader {
	String PathAndFile = "";
	String rootPath = "../../../../../../../home/or10n/workspace/Diploma/DatasetGenerator/Dataset/amarokARFF/";
//	File f = new File(rootPath);
	public InputReader(String PathAndFile){
//		System.out.println(f.isDirectory());
//		File[] e = f.listFiles();
//		for(File ff:e){
//			System.out.println(ff);
//		}
		this.PathAndFile = rootPath + PathAndFile;
		System.out.println(PathAndFile);
//		try {
//			BufferedReader br = new BufferedReader(new FileReader(PathAndFile));
//			File FILE = new File(rootPath + "home/or10n/Desktop/Diploma_Knowledge/Weka&&Manuals/Weka/weka-3-6-12/data/");
//			String[] Files = FILE.list();
//			for(String s : Files){
//				System.out.println(s);
//			}
//			System.out.println(FILE.listFiles()[0]);
//			while(br.ready()){
//				System.out.println(br.readLine());
//			}
//			
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
	public Instances readInput(){
		Instances data = null;
		try{
			DataSource source = new DataSource(PathAndFile);
			data = source.getDataSet();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
//		  setting class attribute if the data format does not provide this information
//		  For example, the XRFF format saves the class attribute information as well
//		 if (data.classIndex() == -1)
//		   data.setClassIndex(data.numAttributes() - 1);
		 return data;
	}
}
