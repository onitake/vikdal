package ch.seto.vikdal.java.transformers;

/**
 * DepthFirstVisitor is the interface all DFS visitors must implement.
 *
 * @param <N> a graph node type
 * @param <E> a graph edge type
 */
public interface DepthFirstVisitor<N, E> {
	/**
	 * This visitor will be called whenever a node is touched.
	 * @param node
	 */
	void visitNode(N node);
	/**
	 * This visitor will be called when an edge is traversed.
	 * @param edge the edge
	 * @param from the starting point
	 * @param to the destination
	 */
	void visitEdge(E edge, N from, N to);
	/**
	 * This visitor will be called when backtracking an edge.
	 * @param edge the edge
	 * @param from the previous starting point, now the destination
	 * @param to the previous starting point, now the destination
	 */
	void visitReturnEdge(E edge, N from, N to);
}
