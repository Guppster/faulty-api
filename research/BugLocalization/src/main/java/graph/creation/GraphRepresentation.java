package graph.creation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.io.GraphMLWriter;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class GraphRepresentation{
	private final Graph<String, MyLink> The_Graph;
	private Map<String, Set<String>> Core_Entities;

	private int edgeCount = 0;

	public class MyLink{
		public final String Type;
		final String From;
		public final String To;
		final int id;

		MyLink(String Type, String From, String To){
			this.id = edgeCount++; // This is defined in the outer class.
			this.Type = Type;
			this.From = From;
			this.To = To;
		}

		public String toString(){ // Always good for debugging
			return "E_" + Type + "_id_" + id;
		}
	}

	public GraphRepresentation(){
		The_Graph = new UndirectedSparseMultigraph<>();
		// The_Graph = new SparseMultigraph<String, MyLink>();
	}

	public void insertVertex(String Vertex){
		The_Graph.addVertex(Vertex);
	}

	public Set<MyLink> getEdge(String Vertex1, String Vertex2){
		Collection<MyLink> EdgesFromVertex1 = The_Graph.getIncidentEdges(Vertex1);
		Collection<MyLink> EdgesFromVertex2 = The_Graph.getIncidentEdges(Vertex2);
		EdgesFromVertex1.retainAll(EdgesFromVertex2);
		return new HashSet<>(EdgesFromVertex1);
	}

	public void insertEdge(String Relation, String From, String To){
		MyLink ml = new MyLink(Relation, From, To);
		The_Graph.addEdge(ml, From, To);
	}

	public Set<MyLink> getEdges(String Vertex){
		return new HashSet<>(The_Graph.getIncidentEdges(Vertex));
	}

	public void addEdges(Set<MyLink> Edges){
		for (MyLink ml : Edges) {
			MyLink nml = new MyLink(ml.Type, ml.From, ml.To);
			if (The_Graph.containsVertex(ml.From) && The_Graph.containsVertex(ml.To))
				The_Graph.addEdge(nml, ml.From, ml.To);
		}
	}

	public void printNeighbors(String Source){
		System.out.println(The_Graph.getNeighborCount(Source));
		System.out.println(The_Graph.getNeighbors(Source));
		// System.out.println(The_Graph.getEdgeCount());
		// System.out.println(The_Graph.getVertexCount());
	}

	public Set<String> getNeighbors(String Source){
		Set<String> Neighbors = new HashSet<>();

		if (The_Graph.containsVertex(Source) && !The_Graph.getNeighbors(Source).isEmpty())
			Neighbors.addAll(The_Graph.getNeighbors(Source));
		return Neighbors;
	}

	public Set<Set<String>> cluster(){
		WeakComponentClusterer<String, MyLink> Clusterer = new WeakComponentClusterer<>();
		return Clusterer.transform(The_Graph);
	}

	public Set<String> getVertices(){
		return new HashSet<>(The_Graph.getVertices());
	}

	public void reduce_edges(){
		Collection<MyLink> edges = new HashSet<>(The_Graph.getEdges());
		for (MyLink ML : edges) {
			switch (ML.Type) {
			case "include":
				break;
			case "definedin":
				break;
			case "declaredin":
				break;
			case "calls":
				break;
			case "inheritsfrom":
				break;
			case "methodbelongstoclass":
				break;
			case "classbelongstofile":
				break;
			default:
				The_Graph.removeEdge(ML);
				break;
			}
		}
	}

	public void removeVerticesAndEdges(Set<String> Center){
		String center = "center";
		The_Graph.addVertex(center);
		MyLink ml;
		for (String s : Center) {
			if (The_Graph.getIncidentEdges(s) != null) {
				Collection<MyLink> CurrentLinks = new HashSet<>(The_Graph.getIncidentEdges(s));
				for (MyLink ML : CurrentLinks) {
					if (The_Graph.getEndpoints(ML).getFirst().equals(s) && !Center.contains(ML.To)) {
						ml = new MyLink(ML.Type, center, ML.To);
						The_Graph.addEdge(ml, center, The_Graph.getEndpoints(ML).getSecond());
					}
					if (The_Graph.getEndpoints(ML).getSecond().equals(s) && !Center.contains(ML.From)) {
						ml = new MyLink(ML.Type, ML.From, center);
						The_Graph.addEdge(ml, The_Graph.getEndpoints(ML).getSecond(), center);
					}
					The_Graph.removeEdge(ML);
				}
			}

		}
	}

	@SuppressWarnings("all")
	public void Show_Graph(){
		// Layout<V, E>, VisualizationComponent<V,E>
		Layout<String, MyLink> layout = new CircleLayout<String, MyLink>(The_Graph);
		layout.setSize(new Dimension(1280, 1024));
		VisualizationViewer<String, MyLink> VViewer = new VisualizationViewer<String, MyLink>(layout);
		VViewer.setPreferredSize(new Dimension(1280, 1024));
		VViewer.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
		VViewer.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<MyLink>());
		// Setup up a new vertex to paint transformer...
		Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>(){
			public Paint transform(String s){
				return Color.GREEN;
			}
		};
		// Set up a new stroke Transformer for the edges
		float dash[] = { 10.0f };
		final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		Transformer<MyLink, Stroke> edgeStrokeTransformer = new Transformer<MyLink, Stroke>(){
			public Stroke transform(MyLink m){
				return edgeStroke;
			}
		};
		VViewer.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		VViewer.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
		VViewer.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		VViewer.setGraphMouse(gm);
		JFrame frame = new JFrame("Interactive Graph View");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(VViewer);
		frame.pack();
		frame.setVisible(true);
	}

	@SuppressWarnings("all")
	public void Save_Graph(int i){
		try {
			GraphMLWriter<String, MyLink> graphWriter = new GraphMLWriter<String, MyLink>();
			AbstractLayout al = new StaticLayout(The_Graph);
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("The_Graph" + i + ".graphML")));
			graphWriter.addVertexData("x", null, "0", new Transformer<String, String>(){
				public String transform(String v){
					return Double.toString(al.getX(v));
				}
			});

			graphWriter.addVertexData("y", null, "0", new Transformer<String, String>(){
				public String transform(String v){
					return Double.toString(al.getY(v));
				}
			});
			// public void addVertexData (
			// String id,
			// String description,
			// String default_value,
			// Transformer<String, String> vertex_transformer);
			graphWriter.save(The_Graph, out);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}

	public void populateCoreEntities(){
		Core_Entities.put("Class", new HashSet<>());
		Core_Entities.put("Method", new HashSet<>());
		Core_Entities.put("Signature", new HashSet<>());
		Core_Entities.put("File", new HashSet<>());
		for (MyLink ml : The_Graph.getEdges()) {
			switch (ml.Type) {
			case "filebelongstomodule":
				Core_Entities.get("File").add(The_Graph.getSource(ml));
				break;
			case "classbelongstofile":
				Core_Entities.get("File").add(The_Graph.getDest(ml));
				Core_Entities.get("Class").add(The_Graph.getSource(ml));
				break;
			case "methodbelongstoclass":
				Core_Entities.get("Method").add(The_Graph.getSource(ml));
				Core_Entities.get("Class").add(The_Graph.getDest(ml));
				break;
			case "signature":
				Core_Entities.get("Method").add(The_Graph.getSource(ml));
				Core_Entities.get("Signature").add(The_Graph.getDest(ml));
				break;
			default:
				break;
			}
		}
	}

	public Graph<String, MyLink> getGraph(){
		return The_Graph;
	}
}
