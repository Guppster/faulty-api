package triple.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Triplets{
	private Map<String, Map<String, Set<String>>> Entity_Relation_Entities = new HashMap<String, Map<String, Set<String>>>();
	private Map<String, Map<String, Set<String>>> Relation_Entity_Entities = new HashMap<String, Map<String, Set<String>>>();
	private Map<String, Map<String, Set<String>>> Entity_Entity_Relations = new HashMap<String, Map<String, Set<String>>>();
	private String product;
	private Set<String> AllEntities = new HashSet<String>();

	public Triplets(String Product){
		product = Product;
	}

	private Map<String, Set<String>> initialize_Map(){
		return new HashMap<String, Set<String>>();
	}

	private Set<String> initialize_Set(){
		return new HashSet<String>();
	}

	public void printKeys(){
		System.out.println(Relation_Entity_Entities.keySet());
	}

	private void insert_to_ERE(String FromEntity, String Relation, String ToEntity){
		Map<String, Set<String>> Relation_Entities;
		Set<String> Entities;
		if (Entity_Relation_Entities.containsKey(FromEntity)) {
			Relation_Entities = Entity_Relation_Entities.get(FromEntity);
			if (Relation_Entities.containsKey(Relation)) {
				Entities = Relation_Entities.get(Relation);
				if (Entities.contains(ToEntity)) {
					return;
				} else {
					Entities.add(ToEntity);
				}
			} else {
				Entities = initialize_Set();
				Entities.add(ToEntity);
				Relation_Entities.put(Relation, Entities);
			}
		} else {
			Relation_Entities = initialize_Map();
			Entities = initialize_Set();
			Entities.add(ToEntity);
			Relation_Entities.put(Relation, Entities);
			Entity_Relation_Entities.put(FromEntity, Relation_Entities);
		}

	}

	private void insert_to_REE(String FromEntity, String Relation, String ToEntity){
		Map<String, Set<String>> Entity_Entities;
		Set<String> Entities;
		if (Relation_Entity_Entities.containsKey(Relation)) {
			Entity_Entities = Relation_Entity_Entities.get(Relation);
			if (Entity_Entities.containsKey(FromEntity)) {
				Entities = Entity_Entities.get(FromEntity);
				if (Entities.contains(ToEntity)) {
					return;
				} else {
					Entities.add(ToEntity);
				}
			} else {
				Entities = initialize_Set();
				Entities.add(ToEntity);
				Entity_Entities.put(FromEntity, Entities);
			}
		} else {
			Entity_Entities = initialize_Map();
			Entities = initialize_Set();
			Entities.add(ToEntity);
			Entity_Entities.put(FromEntity, Entities);
			Relation_Entity_Entities.put(Relation, Entity_Entities);
		}
	}

	private void insert_to_EER(String FromEntity, String Relation, String ToEntity){
		Map<String, Set<String>> Entity_Relations;
		Set<String> Relations;
		if (Entity_Entity_Relations.containsKey(FromEntity)) {
			Entity_Relations = Entity_Entity_Relations.get(FromEntity);
			if (Entity_Relations.containsKey(ToEntity)) {
				Relations = Entity_Relations.get(ToEntity);
				if (Relations.contains(Relation)) {
					return;
				} else {
					Relations.add(Relation);
				}
			} else {
				Relations = initialize_Set();
				Relations.add(Relation);
				Entity_Relations.put(ToEntity, Relations);
			}
		} else {
			Entity_Relations = initialize_Map();
			Relations = initialize_Set();
			Relations.add(Relation);
			Entity_Relations.put(ToEntity, Relations);
			Entity_Entity_Relations.put(FromEntity, Entity_Relations);
		}
	}

	public void insertToMaps(String FromEntity, String Relation, String ToEntity){
		if (!Relation.equals("hastypedef") && !Relation.equals("visibility")) {
			insert_to_EER(FromEntity, Relation, ToEntity);
			insert_to_ERE(FromEntity, Relation, ToEntity);
			insert_to_REE(FromEntity, Relation, ToEntity);
			AllEntities.add(FromEntity);
			AllEntities.add(ToEntity);
		}
	}

	public boolean isEntity(String Term){
		if (AllEntities.contains(Term))
			return true;
		else
			return false;
	}

	public Map<String, Map<String, Set<String>>> get_EER(){
		return Entity_Entity_Relations;
	}

	public Map<String, Map<String, Set<String>>> get_ERE(){
		return Entity_Relation_Entities;
	}

	public Map<String, Map<String, Set<String>>> get_REE(){
		return Relation_Entity_Entities;
	}

	public void printSize(){
		System.out.println("EER Size : " + Entity_Entity_Relations.size());
		System.out.println("ERE Size : " + Entity_Relation_Entities.size());
		System.out.println("REE Size : " + Relation_Entity_Entities.size());
	}

	/**
	 * Serializes all IDs which means the List<Integer> containing all the IDs of all BugReports for which DatasetSeeds have been created.
	 * It is called from the @see {@link DatasetSeedWriter#WriteToDB()} method after it has finished building and writting the DatasetSeeds
	 * 
	 * @param Product
	 *            is the productName and is used to appropriately name the resulting serialization file
	 */
	public void serialize(){
		try {
			ArrayList<Map<String, Map<String, Set<String>>>> AllMaps = new ArrayList<Map<String, Map<String, Set<String>>>>();
			AllMaps.add(Entity_Relation_Entities);
			AllMaps.add(Relation_Entity_Entities);
			AllMaps.add(Entity_Entity_Relations);
			FileOutputStream fileOut = new FileOutputStream("./" + product + "_TripleStore.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			FileOutputStream fileOut2 = new FileOutputStream("./" + product + "_AllEntities.ser");
			ObjectOutputStream out2 = new ObjectOutputStream(fileOut2);
			out.writeObject(AllMaps);
			out2.writeObject(AllEntities);
			out.close();
			out2.close();
			fileOut.close();
			fileOut2.close();
			// System.out.println("Serialized data is saved in /" + Product + "_seed.ser");
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage() + " Exception thrown at Serialization...");
		}
		return;
	}

	/**
	 * Deserializes a serialization file of of name [product_name]+"_seed.ser" shoud be called only after calling @see
	 * {@link DatasetSeedWriter#checkForExistingSeeds(String)} and it returns true
	 */
	@SuppressWarnings("unchecked")
	public void deserialize(){
		ArrayList<Map<String, Map<String, Set<String>>>> AllMaps = new ArrayList<Map<String, Map<String, Set<String>>>>();
		try {
			FileInputStream fileIn = new FileInputStream("./" + product + "_TripleStore.ser");
			FileInputStream fileIn2 = new FileInputStream("./" + product + "_AllEntities.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			ObjectInputStream in2 = new ObjectInputStream(fileIn2);
			AllMaps = (ArrayList<Map<String, Map<String, Set<String>>>>) in.readObject();
			Entity_Relation_Entities = AllMaps.get(0);
			Relation_Entity_Entities = AllMaps.get(1);
			Entity_Entity_Relations = AllMaps.get(2);
			AllEntities = (Set<String>) in2.readObject();
			in.close();
			in2.close();
			fileIn.close();
			fileIn2.close();
		} catch (IOException | ClassNotFoundException ioe) {
			System.out.println(ioe.getMessage() + " Exception thrown at Deserialization...");
		}
	}

	/**
	 * Because this Class is instantiated only to build DatasetSeeds, this method should always be called prior to calling the @see
	 * {@link DatasetSeedWriter#WriteToDB()} method it checks the working directory for a serialization file name after the product name
	 * followed by the "_seed.ser" suffix
	 * 
	 * @param productName
	 * @return
	 */
	public Boolean checkForExistingSeeds(){
		File root = new File("./");
		for (String f : root.list()) {
			if (f.contains(product + "_TripleStore.ser")) {
				deserialize();
				return true;
			}
		}
		return false;
	}

}
