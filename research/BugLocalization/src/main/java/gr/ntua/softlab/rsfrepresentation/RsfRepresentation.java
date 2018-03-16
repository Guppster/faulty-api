/**
 * 
 */
package gr.ntua.softlab.rsfrepresentation;

import gr.ntua.softlab.cdifutilities.CdifRepresentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author or10n
 *
 */
public class RsfRepresentation{
	private final Map<String, Map<String, Set<String>>> relationEntityEntitiesID;
	private final Map<String, Map<String, Set<String>>> entityRelationEntitiesID;
	private final Map<String, Map<String, Set<String>>> entityEntitiesRelationID;
	private final Map<String, Map<String, Set<String>>> relationEntityEntities;
	private final Map<String, Map<String, Set<String>>> entityRelationEntities;
	private final Map<String, Map<String, Set<String>>> entityEntitiesRelation;
	private final Map<String, Map<String, Set<String>>> relationInverseEntityEntity;
	private Map<String, Map<String, Set<String>>> fileFileRelation;
	private Map<String, Map<String, Map<String, Integer>>> fileToFileToRelationAndCardinality;
	private Set<String> allEntityNames;
	private final List<String[]> fileRelations = new ArrayList<>();
	private final Map<String, String> entityToId;
	private final Map<String, String> idToEntity;
	private Set<CallsTuple> calls;
	private final Set<String> files = new HashSet<>();
	private CdifRepresentation cdifRepresentation;

	// private Set<DeclaredTuple> declarations;

	/**
	 * @Constructor RsfRepresentation() no arguments constructor, it initializes all maps, which are used to provide necessary functionality
	 */
	public RsfRepresentation(){
		relationEntityEntitiesID = new HashMap<>();
		entityRelationEntitiesID = new HashMap<>();
		entityEntitiesRelationID = new HashMap<>();
		relationEntityEntities = new HashMap<>();
		entityRelationEntities = new HashMap<>();
		entityEntitiesRelation = new HashMap<>();
		relationInverseEntityEntity = new HashMap<>();
		entityToId = new HashMap<>();
		idToEntity = new HashMap<>();
	}

	/**
	 * insert receives a String[] tokens which is exactly the same as the String[] output by the RsfReader.readLine() method.
	 * 
	 * @param tokens
	 *            [0] relation
	 * @param tokens
	 *            [1] entity1 name
	 * @param tokens
	 *            [2] entity1 id
	 * @param tokens
	 *            [3] entity2 name
	 * @param tokens
	 *            [4] entity2 id
	 */
	public void insert(String[] tokens){
		insertToMap(relationEntityEntitiesID, new String[] { tokens[0], tokens[2], tokens[4] });
		insertToMap(entityRelationEntitiesID, new String[] { tokens[2], tokens[0], tokens[4] });
		insertToMap(entityEntitiesRelationID, new String[] { tokens[2], tokens[4], tokens[0] });
		insertToMap(relationEntityEntities, new String[] { tokens[0], tokens[1], tokens[3] });
		insertToMap(entityRelationEntities, new String[] { tokens[1], tokens[0], tokens[3] });
		insertToMap(entityEntitiesRelation, new String[] { tokens[1], tokens[3], tokens[0] });
		insertToMap(relationInverseEntityEntity, new String[] { tokens[0], tokens[4], tokens[2] });
		insertToDictionaries(new String[] { tokens[1], tokens[2], tokens[3], tokens[4] });
	}

	/**
	 * Receives an Entity Name exactly as it is found in the .rsf file (must not have been lowercased! and returns its respective id
	 * 
	 * @param name
	 *            an Entity's Name
	 * @return the Entity's Id
	 */
	public String getId(String name){
		return entityToId.get(name);
	}

	/**
	 * Receives an Entity Id which must be present in the initial .rsf file and returns its Name (exactly as it is found in the .rsf
	 * 
	 * @param id
	 *            an Entity's Id
	 * @return the Entity's Name
	 */
	public String getName(String id){
		return idToEntity.get(id);
	}

	/**
	 * printAllRelations just prints the names of all relations available
	 */
	public void printAllRelations(){
		for (Entry<String, Map<String, Set<String>>> e : relationEntityEntities.entrySet())
			System.out.println(e.getKey());
		return;
	}

	/**
	 * Receives an Entity Name exactly as it is found in the .rsf file (must not have been lowercased! and returns its respective id
	 * 
	 * @param name
	 *            an Entity's Name
	 * @return the Entity's Id
	 */
	public String getActualId(String name){
		if (files.isEmpty()) {
			for (Entry<String, Set<String>> e : relationEntityEntitiesID.get("FileBelongsToModule").entrySet())
				files.add(idToEntity.get(e.getKey()));
		}
		// System.out.println(files);
		String key = "";
		for (String s : files) {
			if (s.contains(name)) {
				key = s;
				break;
			}
		}
		// if(key.equals(""))
		// System.out.println(name);
		return entityToId.get(key);
	}

	public Set<String> getFileNamesStrings(){
		Map<String, Set<String>> FileBelongsToModule = relationEntityEntities.get("FileBelongsToModule");
		return FileBelongsToModule.keySet();
	}

	/**
	 * Receives as input the name of a Relation, it can be one of the following : {DeclaredIn, DefinedIn, Calls, MethodBelongsToClass,
	 * AttributeBelongsToClass, HasType, HasTypeDef, ModuleBelongsToModule, FileBelongsToModule, Include, MacroDefinition,
	 * ClassBelongsToFile, InheritsFrom, Typedef, AccessibleEntityBelongsToFile, Accesses, UsesType, Sets, entityBelongsToBlock, MacroUse,
	 * EntityLocation}
	 * 
	 * @param relation
	 * @return A Map<String, Set<String>> with key the name of the from Entity and the value is the Set<String> of the to Entities
	 */
	public Map<String, Set<String>> getCompleteRelation(String relation){
		if (relationEntityEntitiesID.containsKey(relation))
			return relationEntityEntitiesID.get(relation);
		else
			return new HashMap<>();
	}

	/**
	 * Receives an input the name of a From entity, and returns a Map<String, Set<String>>
	 * 
	 * @param entity
	 *            name of Entity
	 * @return a Map<String, Set<String>> with key the relations and values the To entities related via the _key_ relation
	 */
	public Map<String, Set<String>> getRelatedToEntity(String entity){
		return entityRelationEntitiesID.get(entity);
	}

	public int getEntityConnectivity(String entity){
		int connectivity = 0;
		if (entityRelationEntitiesID.containsKey(entity)) {
			for (Entry<String, Map<String, Set<String>>> e : relationEntityEntitiesID.entrySet()) {
				if (e.getValue().containsKey(entity))
					connectivity += e.getValue().get(entity).size();
			}
			for (Entry<String, Map<String, Set<String>>> e : relationInverseEntityEntity.entrySet()) {
				if (e.getValue().containsKey(entity))
					connectivity += e.getValue().get(entity).size();
			}
		}
		return connectivity;
	}

	/**
	 * Using a variety of private methods it returns the .rsf in String ready to be output to File
	 * 
	 * @return a String containing all the lines of the .rsf we want to create which will be comprised of the relations Include and Implied
	 *         Calls between files
	 */
	public String getFileRelationsRsf(){
		StringBuilder result = new StringBuilder();
		if (fileFileRelation == null) {
			fileFileRelation = new HashMap<>();
		}
		associateFile();
        for (Entry<String, Map<String, Set<String>>> fileFileRelationEntry : fileFileRelation.entrySet())
        {
            for (Entry<String, Set<String>> fileRelationEntry : fileFileRelationEntry.getValue().entrySet())
            {
                for (String relation : fileRelationEntry.getValue())
                {
                    result.append(relation).append("\t\"").append(fileFileRelationEntry.getKey()).append("\"#").append(getId(fileFileRelationEntry.getKey())).append("\t\"").append(fileRelationEntry.getKey()).append("\"#").append(getId(fileRelationEntry.getKey())).append("\n");
                }
            }
        }
		result = new StringBuilder();
		for (String[] tokens : fileRelations) {
			result.append(tokens[2]).append("\t\"").append(tokens[0]).append("\"#").append(getId(tokens[0])).append("\t\"").append(tokens[1]).append("\"#").append(getId(tokens[1])).append("\n");
		}
		return result.toString();
	}

	public Map<String, Map<String, Map<String, Integer>>> getFileToFileToRelationToCardinalityMap(){
		return fileToFileToRelationAndCardinality;
	}

	/**
	 * it Returns all Function names (complete names which are DefinedIn the File (complete name) which is passed as argument
	 * 
	 * @param File
	 *            the name of a File
	 * @return a Set<String> containing all Functions DefinedIn File(param)
	 */
	public Set<String> getContainedFunctions(String File){
		Set<String> containedFunctions = new HashSet<>();
		if (relationInverseEntityEntity.get("DefinedIn").containsKey(File))
			containedFunctions.addAll(relationInverseEntityEntity.get("DefinedIn").get(File));
		return containedFunctions;
	}

	/**
	 * A private Function for effectively adding triplets to the desired Map.
	 * 
	 * @param localMap
	 * @param inputs
	 */
	private void insertToMap(Map<String, Map<String, Set<String>>> localMap, String[] inputs){
		if (localMap.containsKey(inputs[0])) {
			if (localMap.get(inputs[0]).containsKey(inputs[1])) {
				if (localMap.get(inputs[0]).get(inputs[1]).contains(inputs[2])) {
					return;
				} else {
					localMap.get(inputs[0]).get(inputs[1]).add(inputs[2]);
				}
			} else {
				localMap.get(inputs[0]).put(inputs[1], initializeSet(inputs[2]));
			}
		} else {
			localMap.put(inputs[0], initializeStringSetMap(inputs[1], initializeSet(inputs[2])));
		}
	}

	/**
	 * insertion of String[4] to the dictionaries private instance variables
	 * 
	 * @param entityIds
	 */
	private void insertToDictionaries(String[] entityIds){
		if (!entityToId.containsKey(entityIds[0]))
			entityToId.put(entityIds[0], entityIds[1]);
		if (!entityToId.containsKey(entityIds[2]))
			entityToId.put(entityIds[2], entityIds[3]);
		if (!idToEntity.containsKey(entityIds[1]))
			idToEntity.put(entityIds[1], entityIds[0]);
		if (!idToEntity.containsKey(entityIds[3]))
			idToEntity.put(entityIds[3], entityIds[2]);
	}

	/**
	 * private method for initializing a new Set which takes the firstElement and returns a reference to the newSet which already contains
	 * the firstElement
	 * 
	 * @param firstElement
	 * @return
	 */
	private Set<String> initializeSet(String firstElement){
		Set<String> newSet = new HashSet<>();
		newSet.add(firstElement);
		return newSet;
	}

	/**
	 * private method for initializing a new HashMap<String, Set<String>> which takes a Set and a Key and puts the to a new HashMap which is
	 * then returned to the Caller
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	private Map<String, Set<String>> initializeStringSetMap(String key, Set<String> value){
		return new HashMap<>();
	}

	/**
	 * private method which is used to store in a Set<CallsTuple> all the Tuples < Caller, Callee > which are to be used in extracting the
	 * implied "Calls" relation between files
	 */
	private void extractCalls(){
		calls = new HashSet<>();
        for (Entry<String, Set<String>> callsEntries : relationEntityEntitiesID.get("Calls").entrySet())
        {
            for (String callee : callsEntries.getValue())
            {
                calls.add(new CallsTuple(callsEntries.getKey(), callee));
            }
        }
	}

	/**
	 * private Method which populates the List<String[]> fileRelations and the Map<String, Map<String, Set<String>>> fileFileRelations which
	 * will be returned in .rsf String form when needed.
	 */
	private void associateFile(){
		if (cdifRepresentation != null) {
			System.out.println("I insert Accesses and Sets");
			Map<String, String> entityToFile = cdifRepresentation.readFile();
			if (relationEntityEntitiesID.get("Accesses") != null) {
                for (Entry<String, Set<String>> entityEntitiesEntry : relationEntityEntitiesID.get("Accesses").entrySet())
                {
                    for (String accessedEntity : entityEntitiesEntry.getValue())
                    {
                        fileRelations.add(new String[]{entityToFile.get(entityEntitiesEntry.getKey()), entityToFile.get(accessedEntity),
                                "Accesses"});
                    }
                }
			}

			if (relationEntityEntitiesID.get("Sets") != null) {
                for (Entry<String, Set<String>> entityEntitiesEntry : relationEntityEntitiesID.get("Sets").entrySet())
                {
                    for (String setEntity : entityEntitiesEntry.getValue())
                    {
                        fileRelations.add(new String[]{entityToFile.get(entityEntitiesEntry.getKey()), entityToFile.get(setEntity),
                                "Sets"});
                    }
                }
			}
		}

		extractCalls();
		if (relationEntityEntitiesID.get("Include") != null) {
            for (Entry<String, Set<String>> entityEntitiesEntry : relationEntityEntitiesID.get("Include").entrySet())
            {
                for (String file : entityEntitiesEntry.getValue())
                {
                    // insertToMap(fileFileRelation, new String[]{entityEntitiesEntry.getKey(), file, "Include" });
                    fileRelations.add(new String[]{entityEntitiesEntry.getKey(), file, "Include"});
                }
            }
		}
		for (CallsTuple ct : calls) {
			for (String s : getDefinedIn(ct.getCaller()))
				if (!s.contains(".h"))
					for (String ss : getDefinedIn(ct.getCallee())) {
						// insertToMap(fileFileRelation, new String[]{getDeclaredIn(ct.getCaller()), getDeclaredIn(ct.getCallee()),
						// "Calls"});
						if (!ss.contains(".h")) {
							// insertToMap(fileFileRelation, new String[]{s, ss, "Calls"});
							fileRelations.add(new String[] { s, ss, "Calls" });
						}
					}
			for (String s : getDeclaredIn(ct.getCaller()))
				if (!s.contains(".h"))
					for (String ss : getDeclaredIn(ct.getCallee())) {
						// insertToMap(fileFileRelation, new String[]{getDeclaredIn(ct.getCaller()), getDeclaredIn(ct.getCallee()),
						// "Calls"});
						if (!ss.contains(".h")) {
							// insertToMap(fileFileRelation, new String[]{s, ss, "Calls"});
							fileRelations.add(new String[] { s, ss, "Calls" });
						}
					}
			for (String s : getDeclaredIn(ct.getCaller()))
				if (!s.contains(".h"))
					for (String ss : getDefinedIn(ct.getCallee())) {
						// insertToMap(fileFileRelation, new String[]{getDeclaredIn(ct.getCaller()), getDeclaredIn(ct.getCallee()),
						// "Calls"});
						if (!ss.contains(".h")) {
							// insertToMap(fileFileRelation, new String[]{s, ss, "Calls"});
							fileRelations.add(new String[] { s, ss, "Calls" });
						}
					}
			for (String s : getDefinedIn(ct.getCaller()))
				if (!s.contains(".h"))
					for (String ss : getDeclaredIn(ct.getCallee())) {
						// insertToMap(fileFileRelation, new String[]{getDeclaredIn(ct.getCaller()), getDeclaredIn(ct.getCallee()),
						// "Calls"});
						if (!ss.contains(".h")) {
							// insertToMap(fileFileRelation, new String[]{s, ss, "Calls"});
							fileRelations.add(new String[] { s, ss, "Calls" });
						}
					}
		}
	}

	/**
	 * public method which returns the File from the RsfRepresentation where a Function is DefinedIn
	 * 
	 * @param function
	 * @return
	 */
	public String getDefinedInFile(String function){
		if (relationEntityEntitiesID.get("DefinedIn").containsKey(function)) {
			for (String s : relationEntityEntitiesID.get("DefinedIn").get(function))
				return s;
			return "";
		} else
			return "";
	}

	/**
	 * private method which returns the File for the RsfRepresentation where a Function is DefinedIn
	 * 
	 * @param function
	 * @return
	 */
	private Set<String> getDefinedIn(String function){
		// String file = "none";
		// for(String s :relationEntityEntities.get("DeclaredIn").get(function)){
		// file = s;
		// break;
		// }
		if (relationEntityEntitiesID.get("DefinedIn").containsKey(function))
			return relationEntityEntitiesID.get("DefinedIn").get(function);
		else
			return (new HashSet<>());
	}

	/**
	 * private method which returns the File for the RsfRepresentation where a Function is DefinedIn
	 * 
	 * @param function
	 * @return
	 */
	private Set<String> getDeclaredIn(String function){
		// String file = "none";
		// for(String s :relationEntityEntities.get("DeclaredIn").get(function)){
		// file = s;
		// break;
		// }
		if (relationEntityEntitiesID.get("DeclaredIn").containsKey(function))
			return relationEntityEntitiesID.get("DeclaredIn").get(function);
		else
			return (new HashSet<>());
	}

	public Set<String> getFunctionIds(){
		Map<String, Set<String>> Calls = relationEntityEntitiesID.get("Calls");
		Set<String> Functions = new HashSet<>();
		for (Entry<String, Set<String>> Call : Calls.entrySet()) {
			Functions.add(Call.getKey());
			Functions.addAll(Call.getValue());
		}
		return Functions;
	}

	public Set<String> getFileIds(){
		Map<String, Set<String>> FileBelongsToModule = relationEntityEntitiesID.get("filebelongstomodule");
		Set<String> Files = new HashSet<>();
		for (Entry<String, Set<String>> fileToModule : FileBelongsToModule.entrySet()) {
			Files.add(fileToModule.getKey());
			for (String s : fileToModule.getValue())
				if (!s.contains("M"))
					Files.add(s);
		}
		return Files;
	}

	/**
	 * public method which returns all File names found in the RsfRepresentation using the relation "FileBelongsToModule"
	 * 
	 * @return
	 */
	public Set<String> getFileNames(){
		Map<String, Set<String>> FileBelongsToModule = relationEntityEntitiesID.get("FileBelongsToModule");
		return FileBelongsToModule.keySet();
	}

	public Set<String> getFunctionNames(){
		Map<String, Set<String>> Calls = relationEntityEntitiesID.get("Calls");
		Set<String> Functions = new HashSet<>();
		for (Entry<String, Set<String>> Call : Calls.entrySet()) {
			Functions.add(Call.getKey());
			Functions.addAll(Call.getValue());
		}
		return Functions;
	}

	private void makeAllEntities(){
		allEntityNames = new HashSet<>();
		for (Entry<String, Map<String, Set<String>>> e : entityEntitiesRelation.entrySet()) {
			allEntityNames.add(e.getKey());
			allEntityNames.addAll(e.getValue().keySet());
		}
		/* for (Entry<String, Set<String>> e : relationEntityEntities.get("calls").entrySet()) { allEntityNames.add(e.getKey());
		 * allEntityNames.addAll(e.getValue()); } for (Entry<String, Set<String>> e : relationEntityEntities.get("accesses").entrySet()) {
		 * allEntityNames.add(e.getKey()); allEntityNames.addAll(e.getValue()); } for (Entry<String, Set<String>> e :
		 * relationEntityEntities.get("sets").entrySet()) { allEntityNames.add(e.getKey()); allEntityNames.addAll(e.getValue()); } for
		 * (Entry<String, Set<String>> e : relationEntityEntities.get("include").entrySet()) { allEntityNames.add(e.getKey());
		 * allEntityNames.addAll(e.getValue()); } for (Entry<String, Set<String>> e :
		 * relationEntityEntities.get("filebelongstomodule").entrySet()) { allEntityNames.add(e.getKey());
		 * allEntityNames.addAll(e.getValue()); } */
	}

	public boolean isEntity(String s){
		if (allEntityNames == null)
			makeAllEntities();
        return allEntityNames.contains(s);
	}

	public Map<String, Map<String, Set<String>>> getRelationEntityEntitiesMap(){
		return relationEntityEntities;
	}

	public Map<String, Map<String, Set<String>>> getEntityEntitiesRelationIDMap(){
		return entityEntitiesRelationID;
	}

	public Map<String, String> getIdToEntityName(){
		return idToEntity;
	}

	public Map<String, String> getEntityToId(){
		return entityToId;
	}

}
