package preBucket.data.arrangement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import specificDepthRetrieval.RelationSearcher;
import SCEManipulation.Classes;
import SCEManipulation.Functions;

public class DatasetSeedWriter{
	private Map<Integer, Map<Integer, List<String>>> ListTokens = null;
	private ExecutorService DBwriteExecutorService;
	private String DatabaseName;
	private String Product;
	private Functions functions;
	private Classes classes;
	private RelationSearcher RS = null;
	private Set<Integer> totalIDs = new HashSet<Integer>();

	/**
	 * Constructor of @see {@link preBucket.data.arrangement.DatasetSeedWriter} for creating datasetSeeds without Expansion of the
	 * DatasetSeeds
	 * 
	 * @param ListTokens
	 * @param functions
	 * @param DatabaseName
	 * @param Product
	 */
	public DatasetSeedWriter(Map<Integer, Map<Integer, List<String>>> ListTokens, Functions functions, String DatabaseName, String Product){
		this.ListTokens = ListTokens;
		DBwriteExecutorService = Executors.newFixedThreadPool(50);
		this.DatabaseName = DatabaseName;
		this.Product = Product;
		this.functions = functions;
	}

	/**
	 * Constructor of @see {@link preBucket.data.arrangement.DatasetSeedWriter} for making DatasetSeeds with Expansion of the Initially
	 * extracted terms using the @see {@link RelationSearcher} and its methods
	 *
	 * @param ListTokens
	 * @param functions
	 * @param classes
	 * @param relationSearcher
	 * @param DatabaseName
	 * @param Product
	 */
	public DatasetSeedWriter(Map<Integer, Map<Integer, List<String>>> ListTokens, Functions functions, Classes classes,
			RelationSearcher relationSearcher, String DatabaseName, String Product){
		this.ListTokens = ListTokens;
		DBwriteExecutorService = Executors.newFixedThreadPool(50);
		this.DatabaseName = DatabaseName;
		this.Product = Product;
		this.functions = functions;
		this.classes = classes;
		RS = relationSearcher;
	}

	/**
	 * WriteToDB() is a method of @see {@link DatasetSeedWriter} which uses all parameters of the Constructor to make @see
	 * {@link DatasetSeedMaker} Instances which will create the DatasetSeeds which are composed by two named List<String> which is a
	 * Map<String, List<String>> on of which contains all Functions including those found in a BugReport but also those functions which are
	 * related to those, and are retrieved by using the RelationSearcher methods @see {@link RelationSearcher#getCalledFunctions(String)}
	 * and @see {@link RelationSearcher#getCallingFunctions(String)}. the second list contains all non_function elements to begin with but
	 * those are not included in the final List because we want to include only terms which are also Classes, for this reason we use the @see
	 * {@link RelationSearcher#getDefinedInClass(String)} which finds a related to a term class. So in the end we have one List<String>
	 * Functions and another List<String> Classes which are the written to the Database
	 */
	public void WriteToDB(){
		Iterator<Entry<Integer, Map<Integer, List<String>>>> IListTokens = ListTokens.entrySet().iterator();
		// int i = 0;
		while (IListTokens.hasNext()) {

			DatasetSeedMaker DSM = new DatasetSeedMaker(RS, functions, classes, DatabaseName, Product);
			Entry<Integer, Map<Integer, List<String>>> EListTokens = IListTokens.next();
			DSM.setExecutionParameters(EListTokens.getValue(), EListTokens.getKey());
			totalIDs.add(EListTokens.getKey());
			DBwriteExecutorService.execute(DSM);
			DSM = null;
		}
		try {
			DBwriteExecutorService.shutdown();
			boolean done = false;
			while (!done) {
				done = DBwriteExecutorService.awaitTermination(400, TimeUnit.SECONDS);
			}
			// if (DBwriteExecutorService.awaitTermination(400, TimeUnit.SECONDS))
			// System.out.println("DatasetSeedWriter executed ok");
			// else
			// System.out.println("DatasetSeedWriter need more timeoutTime");
		} catch (InterruptedException IE) {
			System.out.println(IE.getMessage());
		}
		SerializeAllIDs(Product);
	}

	/**
	 * Because this Class is instantiated only to build DatasetSeeds, this method should always be called prior to calling the @see
	 * {@link DatasetSeedWriter#WriteToDB()} method it checks the working directory for a serialization file name after the product name
	 * followed by the "_seed.ser" suffix
	 * 
	 * @param productName
	 * @return
	 */
	public static Boolean checkForExistingSeeds(String productName){
		File root = new File("./");
		for (String f : root.list()) {
			if (f.contains(productName + "_seed.ser")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the DatabaseConnections is Open and if it is it attempts to close it
	 */
	public void closeDB(){
		if (DatasetSeedMaker.connectionOpen())
			DatasetSeedMaker.closeDB();
	}

	/**
	 * Returns the List<Integer> containing the IDs of all BugReports for which DatasetSeeds have been created. Calling this method has
	 * meaning only after we have called @see {@link DatasetSeedWriter#WriteToDB()}
	 * 
	 * @return
	 */
	public Set<Integer> getAllIDs(){
		return totalIDs;
	}

	/**
	 * Deserializes a serialization file of of name [product_name]+"_seed.ser" shoud be called only after calling @see
	 * {@link DatasetSeedWriter#checkForExistingSeeds(String)} and it returns true
	 */
	@SuppressWarnings("unchecked")
	public void DeserializeAllIDs(String Product){
		totalIDs = new HashSet<Integer>();
		try {
			FileInputStream fileIn = new FileInputStream("./" + Product + "_seed.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			totalIDs = (Set<Integer>) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException | ClassNotFoundException ioe) {
			System.out.println(ioe.getMessage() + " Exception thrown at Deserialization...");
		}
	}

	/**
	 * Sets the Functions instance variable for this instance
	 */
	public void setFunctionMap(Functions fs){
		this.functions = fs;
	}

	/**
	 * Sets the Classes instance variable for this instance
	 * 
	 * @param cl
	 */
	public void setClassesMap(Classes cl){
		this.classes = cl;
	}

	/**
	 * Sets the RelationSearcher instance variable for this instance
	 * 
	 * @param RS
	 */
	public void setRelationSearcher(RelationSearcher RS){
		this.RS = RS;
	}

	/**
	 * Serializes all IDs which means the List<Integer> containing all the IDs of all BugReports for which DatasetSeeds have been created.
	 * It is called from the @see {@link DatasetSeedWriter#WriteToDB()} method after it has finished building and writting the DatasetSeeds
	 * 
	 * @param Product
	 *            is the productName and is used to appropriately name the resulting serialization file
	 */
	private void SerializeAllIDs(String Product){
		try {
			FileOutputStream fileOut = new FileOutputStream("./" + Product + "_seed.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(totalIDs);
			out.close();
			fileOut.close();
			System.out.println("Serialized data is saved in /" + Product + "_seed.ser");
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage() + " Exception thrown at Serialization...");
		}
		return;
	}

}
