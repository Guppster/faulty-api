package read.write.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class WriteToFile{
	File file = null;
	BufferedWriter writer = null;
	private static String base_Directory = "../../Data_FileSystem";
	String DestinationPath = base_Directory + "/FinalInputBRs/";
	Set<String> Terms = new HashSet<String>();

	public WriteToFile(String Filename, String ProductName){
		checkIfExists(ProductName);
		file = new File(DestinationPath + ProductName + "/" + Filename.split("_")[1] + "_LSA.txt");
		try {
			writer = new BufferedWriter(new FileWriter(file));
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}

	public void addTerm(String Term){
		Terms.add(Term);
	}

	private boolean checkIfExists(String ProductName){
		File directory = new File(DestinationPath + ProductName + "/");
		if (directory.isDirectory()) {
			return true;
		} else {
			directory.mkdir();
			return true;
		}

	}

	public boolean writeToFile(){
		try {
			for (String s : Terms) {
				writer.write(s);
				writer.newLine();
			}
			return true;
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return false;
		}
	}

	public void close(){
		try {
			writer.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}
}
