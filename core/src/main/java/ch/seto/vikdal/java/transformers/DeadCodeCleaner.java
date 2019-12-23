package ch.seto.vikdal.java.transformers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;

import ch.seto.vikdal.dalvik.instructions.Goto;
import ch.seto.vikdal.dalvik.instructions.Return;
import ch.seto.vikdal.dalvik.instructions.ReturnVoid;
import ch.seto.vikdal.dalvik.instructions.Throw;
import ch.seto.vikdal.java.EdgeTag;

/**
 * - collect unconditional goto and return statements
 * - generate a searchable map of addresses to vertices
 * - find entry and exit points
 */
public class DeadCodeCleaner implements Transformer<Function, Function> {
	@Override
	public Function transform(Function input) {
		DirectedGraph<GraphNode, GraphEdge> graph = input.code;
		InstructionGraphNode entryVertex = null;
		Set<InstructionGraphNode> returnVertices = new HashSet<InstructionGraphNode>();
		Set<GraphEdge> deadEdges = new LinkedHashSet<GraphEdge>();
		Map<Integer, InstructionGraphNode> vertexMap = new HashMap<Integer, InstructionGraphNode>();
		for (GraphNode node : graph.vertexSet()) {
			InstructionGraphNode inode = (InstructionGraphNode) node;
			// eliminate all edges after a return or throw
			if (inode.getInstruction() instanceof Return || inode.getInstruction() instanceof ReturnVoid || inode.getInstruction() instanceof Throw) {
				deadEdges.addAll(graph.outgoingEdgesOf(node));
			}
			// eliminate edges from goto instructions that are not branches
			if (inode.getInstruction() instanceof Goto) {
				deadEdges.addAll(GraphEdge.edgesWithoutTag(graph.outgoingEdgesOf(node), EdgeTag.GOTO));
			}
			
			// add vertex to search map
			vertexMap.put(inode.getAddress(), inode);
			
			// find the entry and exit points
			if (inode.getAddress() == 0) {
				if (entryVertex != null) {
					throw new ProgramVerificationException("Graph has multiple entry points");
				}
				entryVertex = inode;
			}
			if (inode.getInstruction() instanceof Return || inode.getInstruction() instanceof ReturnVoid) {
				returnVertices.add(inode);
			}
		}
		
		// eliminate dead edges
		graph.removeAllEdges(deadEdges);
		
		// verify the graph
		if (entryVertex == null) {
			throw new ProgramVerificationException("Graph does not have an entry point");
		}
		if (returnVertices.size() <= 0) {
			throw new ProgramVerificationException("Graph does not have any exit points");
		}
		return input;
	}
}
