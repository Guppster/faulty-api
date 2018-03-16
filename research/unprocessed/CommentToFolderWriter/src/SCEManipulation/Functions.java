package SCEManipulation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import specificDepthRetrieval.RelationSearcher;

@SuppressWarnings("unused")
public class Functions{
	private String product = null;
	private RelationSearcher RS = new RelationSearcher();
	private List<String> Functions = null;

	/**
	 * Constructor for the Class "Functions" sets the parameter "product" of the Class to the passed parameter to the Constructor "product"
	 * 
	 * @param product
	 */
	public Functions(String product){
		this.product = product;
	}

	/**
	 * CreateRelationGraph creates a new Instance of the Object {@link specificDepthRetrieval.RelationSearcher} and calls its method
	 * {@link specificDepthRetrieval.RelationSearcher#FindRelations()} and then stores the result of the call to
	 * {@link RelationSearcher#getFunctionMethod()} to the field FUnctions of type List<String>
	 */
	public void CreateRelationGraph(String product){
		RS = new RelationSearcher();
		RS.FindRelations(product);
		Functions = RS.getFunctionMethod();
	}

	/**
	 * returns the List<String> Functions containing all the Function Names already Created
	 * 
	 * @return @see {@link Functions#Functions}
	 */
	public List<String> getFunctionsList(){
		return Functions;
	}

	/**
	 * opens a serialization file called product_functions.ser which contains the List<String> Functions which is what this object is mostly
	 * about
	 * 
	 * @param product
	 */
	@SuppressWarnings("all")
	public void DeserializeFunctions(String product){
		List<String> RelAndEIDs = new ArrayList<String>();
		try {
			FileInputStream fileIn = new FileInputStream("./" + product + "_functions.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			Functions = (List<String>) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException | ClassNotFoundException ioe) {
			System.out.println(ioe.getMessage() + " Exception thrown at Deserialization...");
		}
	}

	/**
	 * Serializes the List<String> Functions contained in this instance of the object @see {@link Functions}
	 * 
	 * @param product
	 * @param SerializableObject
	 */
	@SuppressWarnings("all")
	public void SerializeFunctions(String product, Serializable SerializableObject){
		try {
			FileOutputStream fileOut = new FileOutputStream("./" + product + "_functions.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(SerializableObject);
			out.close();
			fileOut.close();
			System.out.println("Serialized data is saved in /" + product + "_functions.ser");
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage() + " Exception thrown at Serialization...");
		}
	}

	/**
	 * prints all members of the List<String> Functions
	 */
	public void print(){
		for (String s : Functions) {
			System.out.println(s);
		}
	}

	/**
	 * receives as parameter a String and returns true if given string is equal to a member of the List<String> Functions or false if it is
	 * not
	 * 
	 * @param s
	 * @return
	 */
	public boolean isFunction(String s){
		return RS.isFunction(s);
	}

	/**
	 * Returns the RelationsSearcher created in @see {@link Functions#CreateRelationGraph()}
	 * 
	 * @return
	 */
	public RelationSearcher getRelationSearcher(){
		return RS;
	}
}
