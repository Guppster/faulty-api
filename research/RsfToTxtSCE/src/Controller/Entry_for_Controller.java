package Controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import rsfReader.RSFReader;
import txtWriter.SCE_txtWriter;

public class Entry_for_Controller{

	public static void main(String[] args){
		String filename_source = null;
		String filename_target = null;
		Set<String> uniqueEntries = new HashSet<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.println("Enter the file name of the .rsf");
			filename_source = br.readLine();
			System.out.println("Enter the project name");
			filename_target = br.readLine() + "_SCE.txt";
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		RSFReader rsfreader = new RSFReader(filename_source);
		SCE_txtWriter sce_txtwriter = new SCE_txtWriter(filename_target);
		int size = uniqueEntries.size();
		while (rsfreader.readerReady()) {
			for (String s : rsfreader.getEntity()) {
				uniqueEntries.add(s);
				if (uniqueEntries.size() != size) {
					sce_txtwriter.write_Entity(s);
					size++;
				}

			}
		}

	}
}
