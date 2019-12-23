package ch.seto.vikdal.java.transformers;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;

import ch.seto.vikdal.dalvik.instructions.Goto;

public class GotoCleaner implements Transformer<Function, Function> {
	@Override
	public Function transform(Function input) {
		DirectedGraph<GraphNode, GraphEdge> graph = input.code;
		Set<GraphNode> removals = new HashSet<GraphNode>();
		for (GraphNode node : graph.vertexSet()) {
			InstructionGraphNode inode = (InstructionGraphNode) node;
			if (inode.getInstruction() instanceof Goto) {
				Set<GraphEdge> oedges = graph.outgoingEdgesOf(node);
				if (oedges.size() != 1) {
					throw new ProgramVerificationException("Goto needs exactly one outgoing edge");
				}
				GraphNode onode = ((GraphEdge) oedges.toArray()[0]).target;
				Set<GraphEdge> iedges = graph.incomingEdgesOf(node);
				for (GraphEdge iedge : iedges) {
					graph.addEdge(iedge.source, onode).setTag(iedge.getTag());
				}
				removals.add(node);
			}
		}
		graph.removeAllVertices(removals);
		return input;
	}
}
