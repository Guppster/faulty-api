package ntua.gr.XMLRPC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class AggregateFiles {
	File Directory;
	File outputFile;
	File[] initialFiles;
	String DirName;
	AggregateFiles(String Dir1, String Dir){
		DirName = Dir1;
		Directory = new File(Dir);
		if(Directory.isDirectory()){
			initialFiles = Directory.listFiles();
		}
	}
	
	public void Aggregate(int start, int end){
		try{
			outputFile = new File(DirName + "/From" + start + "to" + end);
			PrintWriter outputWriter = new PrintWriter(outputFile);
		for(File file : initialFiles){
			BufferedReader br = new BufferedReader(new FileReader(file));
			outputWriter.print(br.readLine() + "\n");
			br.close();
		}
		outputWriter.close();
		}catch(IOException ioe){
		}
	}
}
