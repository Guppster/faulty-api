package graph.facilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import graph.creation.GraphRepresentation;

public class DijkstraPaths{

	DijkstraShortestPath<String, GraphRepresentation.MyLink> DSP;
	String CenterOfAttention;

	public DijkstraPaths(GraphRepresentation GR, String CenterOfAttention){
		DSP = new DijkstraShortestPath<String, GraphRepresentation.MyLink>(GR.getGraph());
		this.CenterOfAttention = CenterOfAttention;
	}

	public Map<String, List<String>> findShortestPaths(Set<String> ToConnect){
		Set<List<GraphRepresentation.MyLink>> Paths = new HashSet<List<GraphRepresentation.MyLink>>();
		for (String s : ToConnect) {
			Paths.add(DSP.getPath(CenterOfAttention, s));
		}
		Map<String, List<String>> ProductionRules = new HashMap<String, List<String>>();
		for (List<GraphRepresentation.MyLink> MyLinkList : Paths) {
			for (GraphRepresentation.MyLink ml : MyLinkList) {
				if (!ProductionRules.containsKey(ml.To)) {
					List<String> toInsert = new ArrayList<String>();
					toInsert.add(ml.Type);
					ProductionRules.put(ml.To, toInsert);
				} else {
					ProductionRules.get(ml.To).add(ml.Type);
				}
			}
		}
		return ProductionRules;
	}
}
