package ch.seto.vikdal.adapter;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;

import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

public class GraphvizAdapter<V, E> {
	private static final String VERTEX_ATTRIBUTE = "vertex";
	private static final String EDGE_ATTRIBUTE = "edge";
	private MutableGraph g;
	private Map<V, MutableNode> vertices;

	public GraphvizAdapter(DirectedGraph<V, E> graph) {
		if (graph == null) {
			throw new IllegalArgumentException();
		}
		vertices = new HashMap<>();
		g = Factory.mutGraph("JGraphT");
		g.setDirected(true);
		insertGraph(graph);
	}

	public Graphviz getGraph() {
		return Graphviz.fromGraph(g);
	}

	private void insertGraph(Graph<V, E> graph) {
		for (V vertex : graph.vertexSet()) {
			addVertex(vertex);
		}
		for (E edge : graph.edgeSet()) {
			addEdge(edge, graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
		}
	}

	private void addEdge(E edge, V source, V target) {
		MutableNode s = vertices.get(source);
		MutableNode t = vertices.get(target);
		s.addLink(t);
	}

	private void addVertex(V vertex) {
		MutableNode node = Factory.mutNode(vertex.toString());
		node.add(VERTEX_ATTRIBUTE, vertex);
		vertices.put(vertex, node);
		g.add(node);
	}
}
