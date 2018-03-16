package print.write.utils;

import java.io.File;

public class FileActions {
	public FileActions(){
	}
	
	public String createUserDir(final String dirName) {
		String homeDiraddress = "./Dataset/";
		try{
	    final File homeDir = new File(homeDiraddress);
	    final File dir = new File(homeDir, dirName);
	    if (!dir.exists() && !dir.mkdirs()) {
	        System.out.println("Unable to create " + dir.getAbsolutePath());
	    }else{
	    	homeDiraddress += dirName;
	    }
		} catch (Exception ioe) {
			System.out.println("can't create dir");
		}
		return homeDiraddress;
	}
}
