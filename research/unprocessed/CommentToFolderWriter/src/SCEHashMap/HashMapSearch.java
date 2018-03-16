package SCEHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;


public class HashMapSearch {

	private AVLTree<String>[] avltree;
	private int HashSize;
	public HashMapSearch(AVLTree<String>[] avlTree, int HashSize){
		this.avltree = avlTree;
		this.HashSize = HashSize;
	}
	
	public boolean hashMapSearch(String stringToSearch){
		
		int hashCode = createAVLHashMap.finalHashCode(stringToSearch, HashSize);
		if (avltree[hashCode].search(stringToSearch)){
			return true;
		}else {
			return false;
		}
	}
	
	public static String[] eliminatePunctuation(String[] tokens){
		
		String[] punctuationArr = {".", "," , "!", ";", "?", ":"};
		
		//eliminate punctuation
		for(int i=0; i<tokens.length; i++){
			if (tokens[i].length() != 0){
				if(Arrays.asList(punctuationArr).contains(tokens[i].substring(tokens[i].length()-1))){
					tokens[i] = tokens[i].substring(0, tokens[i].length()-1);
				}
			}	
		}
		return tokens;
		
	}
	
	public void readCommentsFile(File dir, String filePath, AVLTree<String>[] avlTree, int arraySize){
		
		for(File file : dir.listFiles()){
			System.out.println("File is: "+file.getName());
			
			BufferedReader br = null;
			PrintWriter writer = null;
			String line;
			try{
				writer = new PrintWriter("results\\tokensFrom_"+file.getName(), "UTF-8");
				br = new BufferedReader(new FileReader(filePath+"\\"+file.getName()));
				while ((line = br.readLine()) != null) {
					//System.out.println(line);
					
					String [] tokens = line.split("\\s");
					
					tokens = eliminatePunctuation(tokens);
					
					for (int i=0; i<tokens.length; i++){
						if (hashMapSearch(tokens[i])){
							writer.println(tokens[i]);	
						}
					}
				}
				
			}catch (IOException e){
				e.printStackTrace();
			}finally {
				try{
					if (br!=null){
						br.close();
						writer.close();
						}	
					}catch (IOException ex){
						ex.printStackTrace();
					}
			}
		}	
	}	
	
}
