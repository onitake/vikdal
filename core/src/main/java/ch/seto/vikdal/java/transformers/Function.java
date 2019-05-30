package ch.seto.vikdal.java.transformers;

import org.jgrapht.DirectedGraph;

import ch.seto.vikdal.java.ClassMethodDescriptor;

public class Function {
	public final ClassMethodDescriptor descriptor;
	public final GraphNode headerVertex;
	public final DirectedGraph<GraphNode, GraphEdge> code;
	
	public Function(ClassMethodDescriptor descriptor, GraphNode headerVertex, DirectedGraph<GraphNode, GraphEdge> code) {
		this.descriptor = descriptor;
		this.headerVertex = headerVertex;
		this.code = code;
	}
	
	/**
	 * TODO Only implements a shallow copy
	 */
	@Override
	public Function clone() {
		
		Function copy = new Function(descriptor, headerVertex, code);
		return copy;
	}
}
