package read.write.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ReadFromFile{
	File file = null;
	BufferedReader listreader = null;
	BufferedReader ReportReader = null;
	private static String base_Directory = "../../Data_FileSystem";
	String SourcePath = base_Directory + "/TokenizedReportsPerProject/";
	// String SourcePath = "/home/or10n/workspace/Diploma/ProjectTermsExtractor/";
	String BasePath = base_Directory + "/";
	// String BasePath = "/home/or10n/workspace/KrystaleniaLSA/lsa/";
	String DestinationPath = base_Directory + "/FinalInputBRs/";

	// String DestinationPath = "/home/or10n/workspace/Diploma/MakeInputFiles/";

	public ReadFromFile(){
		// public ReadFromFile(String Filename){
		// file = new File(Filename);
		// try {
		// reader = new BufferedReader(new FileReader(file));
		// } catch (FileNotFoundException fnfe) {
		// System.out.println(fnfe.getMessage());
		// }
	}

	public void AccumulateToSingleFile(String ProductName){
		File file = new File(BasePath + ProductName + "_output/");
		try {
			for (File f : file.listFiles()) {
				listreader = new BufferedReader(new FileReader(f));
				String DestinationName = f.getName().split("\\.")[0];
				// System.out.println(DestinationName);
				WriteToFile wtf = new WriteToFile(DestinationName, ProductName);
				while (readerReady(listreader)) {
					File source = new File(SourcePath + ProductName + "/" + getLine(listreader));
					ReportReader = new BufferedReader(new FileReader(source));
					PassToFile(ReportReader, wtf);
					ReportReader.close();
				}
				wtf.writeToFile();
				wtf.close();
			}
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}

	public String getLine(BufferedReader br){
		try {
			return br.readLine().toLowerCase();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return "zong";
		}
	}

	public void PassToFile(BufferedReader br, WriteToFile WTF){
		while (readerReady(br)) {
			WTF.addTerm(getLine(br));
		}
	}

	public boolean readerReady(BufferedReader br){
		try {
			if (br.ready())
				return true;
			else
				return false;
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return false;
		}
	}
}
