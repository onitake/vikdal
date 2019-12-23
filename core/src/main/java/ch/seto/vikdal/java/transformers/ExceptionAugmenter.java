package ch.seto.vikdal.java.transformers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;

import ch.seto.vikdal.dalvik.pseudo.Catch;
import ch.seto.vikdal.dalvik.pseudo.Try;
import ch.seto.vikdal.java.ClassMethodDescriptor;
import ch.seto.vikdal.java.EdgeTag;
import ch.seto.vikdal.java.TryDescriptor;

public class ExceptionAugmenter implements Transformer<Function, Function> {

	@Override
	public Function transform(Function input) {
		ClassMethodDescriptor method = input.descriptor;
		DirectedGraph<GraphNode, GraphEdge> graph = input.code;
		Map<Integer, InstructionGraphNode> vertexMap = allNodes(graph);
		
		// order try-catch blocks by address, and inside-out
		List<TryDescriptor> exceptions = new ArrayList<TryDescriptor>(method.exceptions);
		Collections.sort(exceptions, new Comparator<TryDescriptor>() {
			public int compare(TryDescriptor a, TryDescriptor b) {
				if (a.start == b.start) {
					return a.length - b.length;
				} else {
					return a.start - b.start;
				}
			}
		});
		// break out try blocks
		for (TryDescriptor ex : exceptions) {
			InstructionGraphNode start = vertexMap.get(ex.start);
			InstructionGraphNode end = vertexMap.get(ex.start + ex.length);
			if (start != null && end != null) {
				int[] types = new int[ex.catches.size()];
				int[] catches = new int[ex.catches.size()];
				int i = 0;
				for (Map.Entry<Integer, Integer> entry : ex.catches.entrySet()) {
					types[i] = entry.getKey();
					catches[i] = entry.getValue();
					i++;
				}
				InstructionGraphNode trynode = new InstructionGraphNode(-1, new Try());
				InstructionGraphNode catnode = new InstructionGraphNode(-1, new Catch(types, catches, ex.catchall));
				graph.addVertex(trynode);
				graph.addVertex(catnode);
				// copy the edge lists to avoid concurrent modifications
				for (GraphEdge inedge : new ArrayList<GraphEdge>(graph.incomingEdgesOf(start))) {
					GraphNode source = inedge.source;
					graph.removeEdge(inedge);
					graph.addEdge(source, trynode).setTag(inedge.getTag());
				}
				graph.addEdge(trynode, start);
				for (GraphEdge inedge : new ArrayList<GraphEdge>(graph.incomingEdgesOf(end))) {
					GraphNode source = inedge.source;
					graph.removeEdge(inedge);
					graph.addEdge(source, catnode).setTag(inedge.getTag());
				}
				graph.addEdge(catnode, end);
				graph.addEdge(trynode, catnode).setTag(EdgeTag.TRYCATCH);
				for (int target : catches) {
					graph.addEdge(catnode, vertexMap.get(target)).setTag(EdgeTag.CATCH);
				}
				if (ex.catchall != -1) {
					graph.addEdge(catnode, vertexMap.get(ex.catchall)).setTag(EdgeTag.CATCHALL);
				}
				// update vertex map with try node, so subsequent try-catch blocks get inserted before
				vertexMap.put(ex.start, trynode);
			}
		}
		
		return input;
	}
	
	private static Map<Integer, InstructionGraphNode> allNodes(Graph<GraphNode, GraphEdge> graph) {
		Map<Integer, InstructionGraphNode> nodes = new LinkedHashMap<>();
		for (GraphNode node : graph.vertexSet()) {
			InstructionGraphNode inode = (InstructionGraphNode) node;
			nodes.put(inode.getAddress(), inode);
		}
		return nodes;
	}
}
