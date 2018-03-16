package SCEHashMap;


public class AVLEntry {
	
	String FileName = "";
	private int arraySize;
	public AVLEntry(String Name, int HashSize){	
		arraySize = HashSize;
		FileName = Name + "_SCE.txt";
	}
	@SuppressWarnings("unchecked")
	public AVLTree<String>[] CreateAVLHashMap(){
		
//		set the Array length
//		create an Array of AVL trees
		AVLTree<?>[] avlArr = new AVLTree<?>[arraySize];
//		initialize the array
		AVLTree<String>[] avlTree = createAVLHashMap.arrInitialization((AVLTree<String>[]) avlArr, arraySize);
//		read the file with the Source Code Entities and for each hash code, insert the SCE to the correct AVL tree
		avlTree = createAVLHashMap.createAVLHashmap(FileName, avlTree);
//		PrintAVLHashMap DEBUG Line
//		createAVLHashMap.printAllTheAVLtrees(avlTree);
		return avlTree;
	}
	
/*
	public static void main(String[] args) {
		
		String fileName;
		fileName = "SourceCodeEntities.txt";
		
		//set the Array length
		int arraySize = 103;
		//create an Array of AVL trees
		AVLTree<?>[] avlArr = new AVLTree<?>[arraySize];

		//initialize the array
		System.out.println("Initializing the AVL array...");
		AVLTree<String>[] avlTree = createAVLHashMap.arrInitialization((AVLTree<String>[]) avlArr, arraySize);
		

		//read the file with the Source Code Entities and for each hash code, insert the SCE to the correct AVL tree
		System.out.println("Creating the AVLTrees' hash map...");
		avlTree = createAVLHashMap.createAVLHashmap(fileName, avlTree);
		
		System.out.println();
		System.out.println();
		
		createAVLHashMap.printAllTheAVLtrees(avlTree);
		
		//check if a source code entity exists in file given
		System.out.println("Searching for a scource code entity...");
		
		//read each file in the directory
		String filePath = "C:\\Users\\�����������\\Desktop\\Ptuxiaki_NTUA\\MongoDB\\Amarok";
		File dir = new File(filePath);
		
//		HashMapSearch.readCommentsFile(dir, filePath, avlTree, arraySize);
		
	}
*/
}