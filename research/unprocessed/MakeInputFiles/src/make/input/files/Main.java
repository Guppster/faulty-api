package make.input.files;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import read.write.files.ReadFromFile;
import triple.store.Triplets;
import file.reader.RSFReader;

public class Main{
	String productName;
	BufferedReader br;
	Triplets T;

	public static void main(String[] args){
		Main m = new Main();
		m.run();
	}

	private Main(){
		br = new BufferedReader(new InputStreamReader(System.in));
	}

	private void run(){
		initialize();
		Read();
	}

	private void initialize(){
		try {
			System.out.print("Product : ");
			productName = br.readLine();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		RSFReader rsfr = new RSFReader(productName + "_final.rsf");
		T = new Triplets(productName);
		if (!T.checkForExistingSeeds()) {
			while (rsfr.readerReady()) {
				rsfr.parseLine();
				T.insertToMaps(rsfr.getFromEntity(), rsfr.getRelation(), rsfr.getToEntity());
			}
			T.serialize();
		} else {
			T.deserialize();
		}
	}

	private void Read(){
		ReadFromFile rff = new ReadFromFile();
		rff.AccumulateToSingleFile(productName);
	}
}
