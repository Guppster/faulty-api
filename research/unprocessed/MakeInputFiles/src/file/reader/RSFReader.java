package file.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RSFReader{

	private File file = null;
	BufferedReader reader = null;
	private String Relation;
	private String FromEntity;
	private String ToEntity;
	private static String base_Directory = "../../Data_FileSystem/";

	public RSFReader(String Filename){
		file = new File(base_Directory + "/RSFS/" + Filename);
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException fnfe) {
			System.out.println(fnfe.getMessage());
		}
	}

	public List<String> parseLine(){
		String line = null;
		String[] substrings;
		List<String> Entity = new ArrayList<String>();
		try {
			line = reader.readLine();
			substrings = line.split("\t");
			Relation = substrings[0];
			Pattern p = Pattern.compile("\"([^\"]*)\"");
			Matcher m = p.matcher(substrings[1]);
			while (m.find()) {
				FromEntity = m.group(1);
			}
			m = p.matcher(substrings[2]);
			while (m.find()) {
				ToEntity = m.group(1);
			}
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		return Entity;
	}

	public boolean close_Reader(){
		try {
			reader.close();
			return true;
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return false;
		}

	}

	public boolean readerReady(){
		try {
			if (reader.ready())
				return true;
			else
				return false;
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return false;
		}
	}

	public String getRelation(){
		return Relation.toLowerCase();
	}

	public String getFromEntity(){
		return FromEntity.toLowerCase();
	}

	public String getToEntity(){
		return ToEntity.toLowerCase();
	}
}
