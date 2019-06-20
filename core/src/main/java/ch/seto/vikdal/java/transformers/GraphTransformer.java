package ch.seto.vikdal.java.transformers;

import org.jgrapht.Graph;

/**
 * Basic interface for a data transformer.
 * 
 * Takes a graph as input and produces a new graph as output.
 * May not modify the graph or its nodes in-place.
 */
public interface GraphTransformer {
	/**
	 * Transforms the input graph.
	 * Does not modify the graph or its edges and nodes in-place.
	 * @param input a graph
	 * @return a new graph
	 */
	public Graph<GraphNode, GraphEdge> transform(Graph<GraphNode, GraphEdge> input);
}
