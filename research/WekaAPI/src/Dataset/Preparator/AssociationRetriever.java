package Dataset.Preparator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AssociationRetriever{
	private String product;
	private Integer ID;

	public AssociationRetriever(String product, Integer ID){
		this.product = product;
		this.ID = ID;
	}

	public static List<String> print_fileList(){
		List<String> FileList = new ArrayList<String>();
		String rootName = ".././././AssociationChains/";
		File root = new File(rootName);
		if (root.isDirectory())
			for (String s : root.list()) {
				// System.out.println(s);
				// if (s.endsWith(".ser"))
				FileList.add(s);
			}
		System.out.println(FileList);
		return FileList;
	}
}
