package ch.seto.vikdal.java.transformers;

import java.util.*;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DirectedMultigraph;

import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.instructions.*;
import ch.seto.vikdal.dalvik.pseudo.*;
import ch.seto.vikdal.java.*;

public class CodeGraphGenerator {
	
	/**
	 * Transforms a list of instructions into a code graph that is closer to Java code.
	 * May add pseudo instructions.
	 * <p>TODO: Incomplete.</p>
	 * @param code the code flow to transform, a mapping from addresses to instructions
	 * @param method the method descriptor, used to add try/catch pairs, a method entry point node and initialise the register state tracker
	 * @return a pseudo code graph
	 * @throws ProgramVerificationException if one of the transformation steps produced an invalid result, an algorithmic error occurred or
	 * the code flow contained unrecoverable errors
	 */
	public Function transformToPseudoCode(SortedMap<Integer, Instruction> code, ClassMethodDescriptor method) throws ProgramVerificationException {
		// generate a code graph from the instruction list
		DirectedGraph<GraphNode, GraphEdge> graph = new DirectedMultigraph<GraphNode, GraphEdge>(GraphEdge.FACTORY);
		Map<Integer, InstructionGraphNode> nodes = new LinkedHashMap<Integer, InstructionGraphNode>();
		InstructionGraphNode previous = null;
		for (Map.Entry<Integer, Instruction> entry : code.entrySet()) {
			InstructionGraphNode node = new InstructionGraphNode(entry.getKey(), entry.getValue());
			graph.addVertex(node);
			nodes.put(node.getAddress(), node);
			if (previous != null) {
				graph.addEdge(previous, node);
			}
			previous = node;
		}
		for (GraphNode node : graph.vertexSet()) {
			InstructionGraphNode inode = (InstructionGraphNode) node;
			Instruction instruction = inode.getInstruction();
			if (instruction.hasBranches()) {
				// all other edges are branch targets
				if (instruction.getBranches().length == 0) {
					throw new ProgramVerificationException("A branching instruction must not have 0 branch targets");
				}
				int offset;
				if (instruction.areBranchesRelative()) {
					offset = inode.getAddress();
				} else {
					offset = 0;
				}
				// ignore payloads, they are handled by the corresponding instructions themselves
				if (!(
					instruction instanceof PackedSwitchPayload ||
					instruction instanceof SparseSwitchPayload ||
					instruction instanceof FillArrayDataPayload
				)) {
					for (int branch : instruction.getBranches()) {
						branch += offset;
						InstructionGraphNode target = nodes.get(branch);
						if (target == null) {
							throw new ProgramVerificationException("Invalid branch target " + branch);
						}
						if (instruction instanceof Switch) {
							Instruction tinstruction = target.getInstruction();
							if (tinstruction.hasBranches()) {
								for (int sbranch : tinstruction.getBranches()) {
									sbranch += offset;
									InstructionGraphNode starget = nodes.get(sbranch);
									if (starget == null) {
										throw new ProgramVerificationException("Invalid branch target " + sbranch);
									}
									try {
										graph.addEdge(node, starget).setTag(EdgeTag.CASE);
									} catch (IllegalArgumentException e) {
										throw new ProgramVerificationException(e);
									}
								}
							}
						} else if (instruction instanceof FillArrayData) {
							try {
								graph.addEdge(node, target).setTag(EdgeTag.DATA);
							} catch (IllegalArgumentException e) {
								throw new ProgramVerificationException(e);
							}
						} else {
							try {
								graph.addEdge(node, target).setTag(EdgeTag.GOTO);
							} catch (IllegalArgumentException e) {
								throw new ProgramVerificationException(e);
							}
						}
					}
				}
			}
		}

		// - collect unconditional goto and return statements
		// - generate a searchable map of addresses to vertices
		// - find entry and exit points
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
		
		// add entry point pseudo instruction (method header)
		int ioffset;
		int[] parameters;
		if (method.modifiers.contains(Modifier.STATIC)) {
			ioffset = 0;
			parameters = new int[method.parameters.size()];
		} else {
			ioffset = 1;
			parameters = new int[method.parameters.size() + 1];
			parameters[0] = method.classid;
		}
		for (int i = 0; i < method.parameters.size(); i++) {
			parameters[i + ioffset] = method.parameters.get(i);
		}
		InstructionGraphNode headerVertex = new InstructionGraphNode(-1, new Entry(method.returntype, method.name, method.registers - method.inputs, method.inputs, parameters));
		graph.addVertex(headerVertex);
		graph.addEdge(headerVertex, entryVertex).setTag(EdgeTag.ENTRY);
		
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
		
		// TODO detect and generate loops and nested ifs, possibly duplicate blocks and finalization code

		return new Function(method, headerVertex, graph);
	}
}
