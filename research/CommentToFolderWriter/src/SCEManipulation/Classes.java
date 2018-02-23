package SCEManipulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import specificDepthRetrieval.RelationSearcher;
import HashMapRelations.RelationsMap;
import HashMapTool.HashMapMaker;

public class Classes{
	// private Map<String, List<String>> FunctionsMethods = new HashMap<String, List<String>>();
	// private String product = null;
	private HashMapMaker HMP;
	private List<Integer> Classes = new ArrayList<Integer>();
	private Set<String> ClassesStr = new HashSet<String>();

	/**
	 * creates an instance of the Class @see {@link Classes}
	 * 
	 * @param RS
	 * @see {@link specificDepthRetrieval.RelationSearcher}
	 */
	public Classes(RelationSearcher RS){
		this.HMP = RS.getHashMapMaker();
	}

	/**
	 * creates an instance of the Class @see {@link Classes}
	 * 
	 * @param HMP
	 * @see {@link HashMapMaker}
	 */
	public Classes(HashMapMaker HMP){
		this.HMP = HMP;
	}

	/**
	 * Gets the RelationMap from a RelationsMap @see {@link HashMapTool.HashMapMaker#getRelationsMap()} then gets Relations from the RM by
	 * calling @see {@link HashMapRelations.RelationsMap} and consequently gets all relations based on the type of relation
	 * "ClassBelongsToFile" as found in the corresponding .rsf file and accumulates the results in the List<Integer> Classes
	 */
	public void detectClasses(){
		RelationsMap RM = HMP.getRelationsMap();
		Map<String, Map<Integer, Map<String, List<Integer>>>> Relations = RM.getRelations();
		Map<Integer, Map<String, List<Integer>>> Class = Relations.get("ClassBelongsToFile");
		Iterator<Entry<Integer, Map<String, List<Integer>>>> IClass = Class.entrySet().iterator();
		while (IClass.hasNext()) {
			Entry<Integer, Map<String, List<Integer>>> IClassEntry = IClass.next();
			if (IClassEntry.getValue() != null) {
				Iterator<Integer> IClasses = IClassEntry.getValue().get("up").iterator();
				while (IClasses.hasNext()) {
					int id = (int) IClasses.next();
					if (!Classes.contains(id)) {
						Classes.add(id);
					}
				}
				// }
			}
		}
	}

	/**
	 * Based on the List<Integer> Classes it creates the corresponding List<String> ClassesStr
	 */
	public void makeFunctionNames(){
		String name1[];
		String name2[];
		for (Integer i : Classes) {
			String name = HMP.getEntityIDs().getEntityName(i);
			if (name != "NOT_FOUND_IN_RSF") {
				if (!ClassesStr.contains(name)) {
					name = name.toLowerCase();
					ClassesStr.add(name);
					name1 = name.split("\\(");
					ClassesStr.add(name1[0]);
					name2 = name1[0].split("::");
					for (String s : name2) {
						ClassesStr.add(s);
					}
					for (String s : name2) {
						name1 = s.split("\\.");
						for (String a : name1) {
							ClassesStr.add(a);
						}
					}
					name1 = name.split("\\(");
					name2 = name1[0].split("\\.");
					for (String s : name2) {
						ClassesStr.add(s);
					}
					for (String s : name2) {
						name1 = s.split("::");
						for (String a : name1) {
							ClassesStr.add(a);
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 * @return @see {@link SCEManipulation.Classes#ClassesStr}
	 */
	public List<String> getClasses(){
		List<String> TCS = new ArrayList<String>();
		TCS.addAll(ClassesStr);
		return TCS;
	}
}
