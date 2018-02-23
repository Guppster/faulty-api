package SCEHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Stemmer.StemmToken;

public class createAVLHashMap{
	private static StemmToken ST = new StemmToken();

	/* finalHashCode: calculates the hash code for a string */
	public static int finalHashCode(String stringForHashing, int arraySize){

		int hashCodeTmp = (stringForHashing.hashCode() & 0x7fffffff);
		int hashCode = hashCodeTmp % arraySize;

		// System.out.println("HashCode for string "+ stringForHashing +" is "+hashCode);
		return hashCode;
	}

	/* insertToAVL: inserts the source code entity to the correct AVL tree in AVL hash map */
	public static AVLTree<String>[] insertToAVL(AVLTree<String>[] avlTree, int hasCode, String sourceCodeEntity){

		avlTree[hasCode].insert(sourceCodeEntity);

		return avlTree;
	}

	public static AVLTree<String>[] createAVLHashmap(String fileName, AVLTree<String>[] avlTree){

		BufferedReader br = null;
		System.out.println("AVLTree Length: " + avlTree.length);

		try {

			String sCurrentLine;
			String StemmedCL;
			List<String> PreFinalTokens = new ArrayList<String>();
			List<String> FinalTokens = new ArrayList<String>();
			br = new BufferedReader(new FileReader(fileName));
			String buffer;
			int hashCode;
			while ((sCurrentLine = br.readLine()) != null) {
				PreFinalTokens.clear();
				FinalTokens.clear();
				PreFinalTokens = TokenizeString(sCurrentLine);
				// for(String s:PreFinalTokens){
				// buffer = ST.StemIt(s);
				// FinalTokens.add(buffer);
				// hashCode = finalHashCode(s, avlTree.length);
				// avlTree = insertToAVL(avlTree, hashCode, s);
				// hashCode = finalHashCode(buffer, avlTree.length);
				// avlTree = insertToAVL(avlTree, hashCode, buffer);
				//
				// }
				StemmedCL = ST.StemIt(sCurrentLine);
				hashCode = finalHashCode(sCurrentLine, avlTree.length);
				avlTree = insertToAVL(avlTree, hashCode, sCurrentLine);
				hashCode = finalHashCode(StemmedCL, avlTree.length);
				avlTree = insertToAVL(avlTree, hashCode, StemmedCL);
				// insert the source code entity to the correct avlTree
				// hashCode = finalHashCode(sCurrentLine1, avlTree.length);
				// avlTree = insertToAVL(avlTree, hashCode, sCurrentLine1);
				// }tToAVL(avlTree, hashCode, sCurrentLine);

			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return avlTree;
	}

	/* arrInitialization: initializes the array by creating an AVL tree for each array 'bucket' */
	public static AVLTree<String>[] arrInitialization(AVLTree<String>[] avlTree, int arraySize){

		for (int i = 0; i < arraySize; i++) {
			avlTree[i] = new AVLTree<String>();
		}
		return avlTree;
	}

	/* printAllTheAVLtrees: print all the existing avl trees in the hash map */
	public static void printAllTheAVLtrees(AVLTree<String>[] avlTree){

		for (int i = 0; i < avlTree.length; i++) {

			try {
				System.out.println("i : " + i);
				avlTree[i].PrintTree();
				System.out.println();
			} catch (NullPointerException e) {
				System.out.println("i : " + i + " and there is nothing in the tree");
			}
		}
	}

	private static List<String> TokenizeString(String Line){
		List<String> Tokenized = new ArrayList<String>();
		String Token;
		Token = Line.replace("||", " ");
		Token = Token.replace("::", " ");
		Token = Token.replace("|", " ");
		Token = Token.replace(".", " ");
		Token = Token.replace("()", " ");
		Token = Token.replace("(", " ");
		Token = Token.replace(")", " ");
		Token = Token.replace("_", " ");
		Token = Token.replace("/", " ");
		Token = Token.replace("*", " ");
		Token = Token.replace(">", " ");
		Token = Token.replace("<", " ");
		// System.out.println(Token);
		for (String s : Token.split(" ")) {
			Tokenized.add(s);

		}
		return Tokenized;
	}
}
