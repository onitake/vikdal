package ch.seto.vikdal.java.transformers;

import java.util.Stack;

import edu.uci.ics.jung.graph.DirectedGraph;

public class DepthFirstTraversal<N, E> {
	private final DirectedGraph<N, E> graph;
	
	public DepthFirstTraversal(DirectedGraph<N, E> graph) {
		this.graph = graph;
	}
	
	public void traverse(DepthFirstVisitor<N, E> visitor, N head) {
		if (!graph.containsVertex(head)) {
			throw new IllegalArgumentException("head must be a member of graph");
		}
		Stack<N> stack = new Stack<N>();
		stack.push(head);
		traverse(visitor, stack);
	}

	private void traverse(DepthFirstVisitor<N, E> visitor, Stack<N> stack) {
		N from = stack.peek();
		visitor.visitNode(from);
		for (E edge : graph.getOutEdges(from)) {
			N to = graph.getDest(edge);
			// check for loops
			if (!stack.contains(to) ) {
				visitor.visitEdge(edge, from, to);
				stack.push(to);
				traverse(visitor, stack);
				stack.pop();
				visitor.visitReturnEdge(edge, from, to);
			}
		}
	}
}
