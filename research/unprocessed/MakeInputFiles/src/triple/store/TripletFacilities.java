package triple.store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TripletFacilities{
	Triplets triplet;
	private Map<String, Map<String, Set<String>>> Entity_Relation_Entities = new HashMap<String, Map<String, Set<String>>>();
	private Map<String, Map<String, Set<String>>> Relation_Entity_Entities = new HashMap<String, Map<String, Set<String>>>();
	private Map<String, Map<String, Set<String>>> Entity_Entity_Relations = new HashMap<String, Map<String, Set<String>>>();
	private Map<String, Set<String>> Core_Entities = new HashMap<String, Set<String>>();

	// private String product;

	public TripletFacilities(Triplets T){
		triplet = T;
		Entity_Relation_Entities = T.get_ERE();
		Relation_Entity_Entities = T.get_REE();
		Entity_Entity_Relations = T.get_EER();
	}

	/**
	 * given an entity it returns all related Entities
	 * 
	 * @param FromEntity
	 * @return
	 */
	public Set<String> getRelatedTo(String FromEntity){
		Set<String> Useful_Relations = new HashSet<String>();
		Useful_Relations.add("calls");
		Useful_Relations.add("methodbelongstoclass");
		Useful_Relations.add("declaredin");
		Useful_Relations.add("definedin");
		Useful_Relations.add("inheritsfrom");
		Useful_Relations.add("classbelongstofile");
		Useful_Relations.add("entitylocation");
		Useful_Relations.add("filebelongstomodule");
		Set<String> Related = new HashSet<String>();
		if (Entity_Entity_Relations.containsKey(FromEntity)) {
			for (Entry<String, Set<String>> Entity_Relation : Entity_Entity_Relations.get(FromEntity).entrySet())
				if (Useful_Relations.containsAll(Entity_Relation.getValue()))
					Related.addAll(Entity_Entity_Relations.get(FromEntity).keySet());
		}
		return Related;
	}

	/**
	 * given two entities it returns the Relation or Relations between them in a set
	 * 
	 * @param FromEntity
	 * @param ToEntity
	 * @return
	 */

	public Set<String> getRelationBetween(String FromEntity, String ToEntity){
		Map<String, Set<String>> Related = new HashMap<String, Set<String>>();
		if (Entity_Entity_Relations.containsKey(FromEntity)) {
			Related.putAll(Entity_Entity_Relations.get(FromEntity));
			if (Related.containsKey(ToEntity)) {
				return Related.get(ToEntity);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Given a relation it returns the hashMap from entity to entities depicting all entities which are connected by this relation.
	 * 
	 * @param Relation
	 * @return
	 */
	public Map<String, Set<String>> getRelatedWithRelation(String Relation){
		Map<String, Set<String>> Related = new HashMap<String, Set<String>>();
		if (Relation_Entity_Entities.containsKey(Relation)) {
			Related.putAll(Relation_Entity_Entities.get(Relation));
		}
		return Related;
	}

	/**
	 * for given entity it returns a set of all relative relations to which this entity partakes
	 * 
	 * @param FromEntity
	 * @return
	 */
	public Set<String> getExistingRelations(String FromEntity){
		Set<String> Relations = new HashSet<String>();
		if (Entity_Relation_Entities.containsKey(FromEntity)) {
			Relations.addAll(Entity_Relation_Entities.get(FromEntity).keySet());
		}
		return Relations;
	}

	/**
	 * Receives as input a Set<String of Tokens consisting of Methods and a number and returns the call expansion to the depth defined by
	 * the number
	 * 
	 * @param inputTokens
	 * @param Depth
	 * @return
	 */
	public Set<String> ExpandOnCalls(Set<String> inputTokens, int Depth){
		if (Depth == 0) {
			return inputTokens;
		} else {
			Depth--;
			Set<String> Expanded = new HashSet<String>();
			Expanded.addAll(inputTokens);
			Map<String, Set<String>> Entity_Entities = Relation_Entity_Entities.get("calls");
			// System.out.println(Relation_Entity_Entities.keySet());
			Set<String> Downwards = new HashSet<String>();
			// Downwards expansion
			Downwards.addAll(Entity_Entities.keySet());
			Downwards.retainAll(inputTokens);
			for (String key : Downwards) {
				Expanded.addAll(Entity_Entities.get(key));
			}
			// Downwards End
			// Upwards Expansion
			Set<String> EEKeySet = Entity_Entities.keySet();
			Set<String> Upwards = new HashSet<String>();
			for (String key : EEKeySet) {
				Upwards.addAll(Entity_Entities.get(key));
				Upwards.retainAll(inputTokens);
				if (!Upwards.isEmpty())
					Expanded.add(key);
				Upwards.clear();
			}
			// Upwards End
			Expanded.addAll(ExpandOnCalls(Expanded, Depth));
			return Expanded;
		}
	}

	/**
	 * From a given set of arbitrary terms it returns only those terms which are methods
	 * 
	 * @param inputTokens
	 * @return
	 */
	public Set<String> getMethods(Set<String> inputTokens, Set<String> Methods){
		// Set<String> Methods = new HashSet<String>();
		// Map<String, Set<String>> Entity_Entities = Relation_Entity_Entities.get("methodbelongstoclass");
		// Methods.addAll(Entity_Entities.keySet());
		inputTokens.retainAll(Methods);
		return inputTokens;
	}

	/**
	 * It takes as input a set of Classes and returns the methods that belong to these classes
	 * 
	 * @param inputTokens
	 * @return
	 */
	public Set<String> getMethodsFromClasses(Set<String> inputTokens){
		Set<String> MethodsInClasses = new HashSet<String>();
		Map<String, Set<String>> Entity_Entities = Relation_Entity_Entities.get("methodbelongstoclass");
		Set<String> Methods = new HashSet<String>();
		Methods = Entity_Entities.keySet();
		Set<String> Classes = new HashSet<String>();
		for (String key : Methods) {
			Classes.addAll(Entity_Entities.get(key));
			Classes.retainAll(inputTokens);
			if (!Classes.isEmpty())
				MethodsInClasses.add(key);
			Classes.clear();
		}
		return MethodsInClasses;
	}

	/**
	 * Given a set of Filenames it returns another set containing Classes contained in the files
	 * 
	 * @param inputTokens
	 * @return
	 */

	public Set<String> getClassFromFiles(Set<String> inputTokens){
		Set<String> ClassesInFiles = new HashSet<String>();
		Map<String, Set<String>> Entity_Entities = Relation_Entity_Entities.get("classbelongstofile");
		Set<String> Classes = new HashSet<String>();
		Classes = Entity_Entities.keySet();
		Set<String> Files = new HashSet<String>();
		for (String key : Classes) {
			Files.addAll(Entity_Entities.get(key));
			Files.retainAll(inputTokens);
			if (!Files.isEmpty())
				ClassesInFiles.add(key);
			Files.clear();
		}
		return ClassesInFiles;
	}

	/**
	 * It creates sets of SCEs containing only one type of SCE each
	 */

	public void populateCoreEntities(){
		Core_Entities.put("Class", new HashSet<String>());
		Core_Entities.put("Method", new HashSet<String>());
		Core_Entities.put("Signature", new HashSet<String>());
		Core_Entities.put("File", new HashSet<String>());
		Core_Entities.put("Header", new HashSet<String>());
		Map<String, Set<String>> fileToModule = Relation_Entity_Entities.get("filebelongstomodule");
		Map<String, Set<String>> classToFile = Relation_Entity_Entities.get("classbelongstofile");
		Map<String, Set<String>> methodToClass = Relation_Entity_Entities.get("methodbelongstoclass");
		Map<String, Set<String>> methodToSignature = Relation_Entity_Entities.get("signature");
		Map<String, Set<String>> methodToHeader = Relation_Entity_Entities.get("declaredin");
		Iterator<Entry<String, Set<String>>> ifileToModule = fileToModule.entrySet().iterator();
		while (ifileToModule.hasNext()) {
			Entry<String, Set<String>> efileToModule = ifileToModule.next();
			Core_Entities.get("File").add(efileToModule.getKey());
		}
		Iterator<Entry<String, Set<String>>> iclassToFile = classToFile.entrySet().iterator();
		while (iclassToFile.hasNext()) {
			Entry<String, Set<String>> eclassToFile = iclassToFile.next();
			Core_Entities.get("Class").add(eclassToFile.getKey());
			Core_Entities.get("File").addAll(eclassToFile.getValue());
		}
		Iterator<Entry<String, Set<String>>> imethodToClass = methodToClass.entrySet().iterator();
		while (imethodToClass.hasNext()) {
			Entry<String, Set<String>> emethodToClass = imethodToClass.next();
			Core_Entities.get("Method").add(emethodToClass.getKey());
			Core_Entities.get("Class").addAll(emethodToClass.getValue());
		}
		Iterator<Entry<String, Set<String>>> imethodToSignature = methodToSignature.entrySet().iterator();
		while (imethodToSignature.hasNext()) {
			Entry<String, Set<String>> emethodToSignature = imethodToSignature.next();
			Core_Entities.get("Method").add(emethodToSignature.getKey());
			Core_Entities.get("Signature").addAll(emethodToSignature.getValue());
		}
		Iterator<Entry<String, Set<String>>> imethodToHeader = methodToHeader.entrySet().iterator();
		while (imethodToHeader.hasNext()) {
			Entry<String, Set<String>> emethodToHeader = imethodToHeader.next();
			Core_Entities.get("Method").add(emethodToHeader.getKey());
			Core_Entities.get("Header").addAll(emethodToHeader.getValue());
		}
	}

	/**
	 * I would like it to return only functions.... it returns nothing :s
	 * 
	 * @param Entity
	 * @return
	 */

	public Set<String> findMethod(String Entity){
		Set<String> result = new HashSet<String>();
		String MethodKey = "Method";
		String ClassKey = "Class";
		String FileKey = "File";
		String SignatureKey = "Signature";
		@SuppressWarnings("unused")
		String HeaderKey = "declaredin";
		if (Core_Entities.get(MethodKey).contains(Entity))
			result.add(Entity);
		else if (Core_Entities.get(ClassKey).contains(Entity))
			result.addAll(findDownWardsMethods(Entity));
		else if (Core_Entities.get(FileKey).contains(Entity))
			for (String method : findDownWardsMethods(Entity))
				result.addAll(findDownWardsClass(method));
		else if (Core_Entities.get(SignatureKey).contains(Entity))
			result.addAll(findMethodFromSignature(Entity));
		else {
			Set<String> eSet = new HashSet<String>();
			eSet.add(Entity);
			result.addAll(findUpWardsMethod(eSet, 0));
		}

		return result;
	}

	/**
	 * CHECKED NOT RUN YET
	 * 
	 * @param Entity
	 * @return
	 */

	private Set<String> findDownWardsMethods(String Entity){
		Set<String> Methods = new HashSet<String>();
		Iterator<Entry<String, Set<String>>> IEE = Relation_Entity_Entities.get("methodbelongstoclass").entrySet().iterator();
		while (IEE.hasNext()) {
			Entry<String, Set<String>> EEE = IEE.next();
			if (EEE.getValue().contains(Entity))
				if (Core_Entities.get("Method").contains(EEE.getKey()))
					Methods.add(EEE.getKey());
		}
		return Methods;
	}

	/**
	 * CHECKED NOT RUN YET
	 * 
	 * @param Entity
	 * @return
	 */
	private Set<String> findDownWardsClass(String Entity){
		Set<String> Classes = new HashSet<String>();
		Iterator<Entry<String, Set<String>>> IEE = Relation_Entity_Entities.get("classbelongstofile").entrySet().iterator();
		while (IEE.hasNext()) {
			Entry<String, Set<String>> EEE = IEE.next();
			if (EEE.getValue().contains(Entity))
				if (Core_Entities.get("Class").contains(EEE.getKey()))
					Classes.add(EEE.getKey());
		}
		return Classes;
	}

	/**
	 * CHECKED NOT RUN YET
	 * 
	 * @param Entity
	 * @return
	 */
	private Set<String> findMethodFromSignature(String Entity){
		Set<String> Method = new HashSet<String>();
		Iterator<Entry<String, Set<String>>> IEE = Relation_Entity_Entities.get("signature").entrySet().iterator();
		while (IEE.hasNext()) {
			Entry<String, Set<String>> EEE = IEE.next();
			if (EEE.getValue().contains(Entity)) {
				Method.add(EEE.getKey());
				break;
			}
		}
		return Method;
	}

	private Set<String> findUpWardsMethod(Set<String> Entity, int time){
		Set<String> Methods = new HashSet<String>();
		Set<String> SearchSet = new HashSet<String>();
		boolean FOUND = false;
		Iterator<Entry<String, Map<String, Set<String>>>> IEER = Entity_Entity_Relations.entrySet().iterator();
		for (String s : Entity) {
			while (IEER.hasNext()) {
				Entry<String, Map<String, Set<String>>> EEER = IEER.next();
				if (EEER.getValue().keySet().contains(s)) {
					if (Core_Entities.get("Method").contains(EEER.getKey())) {
						Methods.add(EEER.getKey());
						FOUND = true;
					} else
						SearchSet.add(EEER.getKey());
				}
				if (EEER.getKey().equals(s)) {
					for (String ss : EEER.getValue().keySet())
						if (Core_Entities.get("Method").contains(ss)) {
							Methods.add(ss);
							FOUND = true;
						} else {
							SearchSet.add(ss);
						}
				}
			}
		}
		if (!FOUND && time < 6)
			Methods.addAll(findUpWardsMethod(SearchSet, (time + 1)));
		return Methods;
	}

	public void findNearestClass(){

	}

	public void findNearestFile(){

	}
}
