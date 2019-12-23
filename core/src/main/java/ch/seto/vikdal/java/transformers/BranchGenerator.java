package ch.seto.vikdal.java.transformers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;

import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.instructions.FillArrayData;
import ch.seto.vikdal.dalvik.instructions.FillArrayDataPayload;
import ch.seto.vikdal.dalvik.instructions.IfTest;
import ch.seto.vikdal.dalvik.instructions.IfTestZ;
import ch.seto.vikdal.dalvik.instructions.PackedSwitchPayload;
import ch.seto.vikdal.dalvik.instructions.SparseSwitchPayload;
import ch.seto.vikdal.dalvik.instructions.Switch;
import ch.seto.vikdal.java.EdgeTag;

public class BranchGenerator implements Transformer<Function, Function> {
	@Override
	public Function transform(Function input) {
		DirectedGraph<GraphNode, GraphEdge> graph = input.code;
		Map<Integer, InstructionGraphNode> nodes = allNodes(graph);
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
				if (instruction instanceof IfTest || instruction instanceof IfTestZ) {
					// all if instructions need to have a "next" instruction
					Set<GraphEdge> edges = graph.outgoingEdgesOf(node);
					if (edges.size() < 1) {
						throw new ProgramVerificationException("If instruction doesn't have a successor");
					}
					if (edges.size() > 1) {
						throw new ProgramVerificationException("If instruction has more than one successor");
					}
					for (GraphEdge edge : edges) {
						edge.setTag(EdgeTag.THEN);
					}
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
						} else if (instruction instanceof IfTest || instruction instanceof IfTestZ) {
							try {
								graph.addEdge(node, target).setTag(EdgeTag.ELSE);
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
