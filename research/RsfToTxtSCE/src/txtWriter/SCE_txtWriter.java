package txtWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SCE_txtWriter{
	private File file = null;
	private BufferedWriter bw = null;

	public SCE_txtWriter(String Filename){
		file = new File(Filename);
		try {
			bw = new BufferedWriter(new FileWriter(file));
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}

	public boolean write_Entity(String Entity){
		try {
			bw.write(Entity);
			bw.newLine();
			return true;
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return false;
		}
	}

	public boolean close_Writer(){
		try {
			bw.close();
			return true;
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return false;
		}
	}
}
