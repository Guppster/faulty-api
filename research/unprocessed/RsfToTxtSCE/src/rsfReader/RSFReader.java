package rsfReader;

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

	public RSFReader(String Filename){
		file = new File(Filename);
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException fnfe) {
			System.out.println(fnfe.getMessage());
		}
	}

	public List<String> getEntity(){
		String line = null;
		List<String> Entity = new ArrayList<String>();
		try {
			line = reader.readLine();
			Pattern p = Pattern.compile("\"([^\"]*)\"");
			Matcher m = p.matcher(line);
			while (m.find()) {
				Entity.add(m.group(1));
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
}
