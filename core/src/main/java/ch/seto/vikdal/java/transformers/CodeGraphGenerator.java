package ch.seto.vikdal.java.transformers;

import java.util.*;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.DepthFirstIterator;

import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.instructions.*;
import ch.seto.vikdal.dalvik.pseudo.*;
import ch.seto.vikdal.java.*;
import ch.seto.vikdal.java.code.Block;
import ch.seto.vikdal.java.code.Method;
import ch.seto.vikdal.java.code.Statement;
import japa.parser.ast.Node;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.type.ClassOrInterfaceType;

public class CodeGraphGenerator {
	
	private SymbolTable table;

	/**
	 * Creates a transformer that can turn a list of instructions into a code graph.
	 * 
	 * @param table a symbol table
	 */
	public CodeGraphGenerator(SymbolTable table) {
		this.table = table;
	}
	
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
		Map<Integer, GraphNode> nodes = new LinkedHashMap<Integer, GraphNode>();
		GraphNode previous = null;
		for (Map.Entry<Integer, Instruction> entry : code.entrySet()) {
			GraphNode node = new GraphNode(entry.getKey(), entry.getValue());
			graph.addVertex(node);
			nodes.put(node.getAddress(), node);
			if (previous != null) {
				graph.addEdge(previous, node);
			}
			previous = node;
		}
		for (GraphNode node : graph.vertexSet()) {
			if (node.getInstruction().hasBranches()) {
				// all other edges are branch targets
				if (node.getInstruction().getBranches().length == 0) {
					throw new ProgramVerificationException("A branching instruction must not have 0 branch targets");
				}
				int offset;
				if (node.getInstruction().areBranchesRelative()) {
					offset = node.getAddress();
				} else {
					offset = 0;
				}
				// ignore payloads, they are handled by the corresponding instructions themselves
				if (!(
					node.getInstruction() instanceof PackedSwitchPayload ||
					node.getInstruction() instanceof SparseSwitchPayload ||
					node.getInstruction() instanceof FillArrayDataPayload
				)) {
					for (int branch : node.getInstruction().getBranches()) {
						branch += offset;
						GraphNode target = nodes.get(branch);
						if (target == null) {
							throw new ProgramVerificationException("Invalid branch target " + branch);
						}
						if (node.getInstruction() instanceof Switch) {
							if (target.getInstruction().hasBranches()) {
								for (int sbranch : target.getInstruction().getBranches()) {
									sbranch += offset;
									GraphNode starget = nodes.get(sbranch);
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
						} else if (node.getInstruction() instanceof FillArrayData) {
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
		GraphNode entryVertex = null;
		Set<GraphNode> returnVertices = new HashSet<GraphNode>();
		Set<GraphEdge> deadEdges = new LinkedHashSet<GraphEdge>();
		Map<Integer, GraphNode> vertexMap = new HashMap<Integer, GraphNode>();
		for (GraphNode node : graph.vertexSet()) {
			// eliminate all edges after a return or throw
			if (node.getInstruction() instanceof Return || node.getInstruction() instanceof ReturnVoid || node.getInstruction() instanceof Throw) {
				deadEdges.addAll(graph.outgoingEdgesOf(node));
			}
			// eliminate edges from goto instructions that are not branches
			if (node.getInstruction() instanceof Goto) {
				deadEdges.addAll(GraphEdge.edgesWithoutTag(graph.outgoingEdgesOf(node), EdgeTag.GOTO));
			}
			
			// add vertex to search map
			vertexMap.put(node.getAddress(), node);
			
			// find the entry and exit points
			if (node.getAddress() == 0) {
				if (entryVertex != null) {
					throw new ProgramVerificationException("Graph has multiple entry points");
				}
				entryVertex = node;
			}
			if (node.getInstruction() instanceof Return || node.getInstruction() instanceof ReturnVoid) {
				returnVertices.add(node);
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
		GraphNode headerVertex = new GraphNode(-1, new Entry(method.returntype, method.name, method.registers - method.inputs, method.inputs, parameters));
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
			GraphNode start = vertexMap.get(ex.start);
			GraphNode end = vertexMap.get(ex.start + ex.length);
			if (start != null && end != null) {
				int[] types = new int[ex.catches.size()];
				int[] catches = new int[ex.catches.size()];
				int i = 0;
				for (Map.Entry<Integer, Integer> entry : ex.catches.entrySet()) {
					types[i] = entry.getKey();
					catches[i] = entry.getValue();
					i++;
				}
				GraphNode trynode = new GraphNode(-1, new Try());
				GraphNode catnode = new GraphNode(-1, new Catch(types, catches, ex.catchall));
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
	
	/**
	 * Generates symbol names for the variables and adds descriptions on each node.
	 * @param fn a code graph
	 * @return a symbolicated code graph
	 * @throws ProgramVerificationException if there was a type conflict
	 */
	public Function symbolicate(Function fn) throws ProgramVerificationException {
		// create a state tracker that generates local symbol names
		StateTracker tracker = new StateTracker();
		tracker.setRegisterRange(0, fn.descriptor.registers - 1);
		int regindex = fn.descriptor.registers;
		for (int i = fn.descriptor.parameters.size() - 1; i >= 0; i--) {
			String type = table.lookupType(fn.descriptor.parameters.get(i));
			Type jtype = Type.fromDescriptor(type);
			if (jtype == null) {
				throw new ProgramVerificationException("Type of parameter can not be deduced");
			}
			regindex -= jtype.getRegisterCount();
			tracker.setRegisterType(regindex, jtype);
			tracker.setAutomaticName(regindex, type);
		}
		if (!fn.descriptor.modifiers.contains(Modifier.STATIC)) {
			regindex--;
			tracker.setRegisterType(regindex, Type.OBJECT);
			tracker.setRegisterName(regindex, "this");
		}
		if (regindex != fn.descriptor.registers - fn.descriptor.inputs) {
			throw new ProgramVerificationException("Number of input registers does not match number of registers required by arguments");
		}

		// deep clone the code graph to avoid modifying it
		Function mod = fn.clone();
		
		// do a DFS traversal, dropping descriptions on all nodes
		DepthFirstIterator<GraphNode, GraphEdge> iterator = new DepthFirstIterator<GraphNode, GraphEdge>(mod.code, mod.headerVertex);
		GraphSymbolicator symbolicator = new GraphSymbolicator(table, tracker);
		iterator.addTraversalListener(symbolicator);
		for (; iterator.hasNext(); iterator.next());
		if (!symbolicator.hasSucceeded()) {
			throw new ProgramVerificationException("Symbolication failed due to register type override");
		}
		
		return mod;
	}
	
	/**
	 * Transforms a single function into a Java code graph, which can then be
	 * turned into an AST (Abstract Syntax Tree) and further into Java code.
	 * @param fn the function to transform
	 * @return a method object that represents both the method and its code
	 */
	public Method transformToStatements(Function fn) {
		// first the function name
		String name;
		if ("<init>".equals(fn.descriptor.name)) {
			// <init> = constructor
			String fullName = Type.humanReadableDescriptor(table.lookupType(fn.descriptor.classid));
			int dotIndex = fullName.lastIndexOf('.');
			if (dotIndex > -1) {
				name = fullName.substring(dotIndex + 1);
			} else {
				name = fullName;
			}
		} else {
			name = fn.descriptor.name;
		}
		// TODO we should also handle <classinit> = static initialiser
		Method m = new Method(name);
		// then the modifiers and return type
		m.modifiers = fn.descriptor.modifiers.clone();
		m.type = Type.humanReadableDescriptor(table.lookupType(fn.descriptor.returntype));
		// then the arguments
		for (int i = 0; i < fn.descriptor.parameters.size(); i++) {
			int paramType = fn.descriptor.parameters.get(i);
			m.arguments.add(new Method.Argument(Type.humanReadableDescriptor(table.lookupType(paramType)), "arg" + i));
		}
		// and the code
		m.block = instructifyGraph(fn, fn.headerVertex);
		return m;
	}

	/**
	 * Transforms a node into code, generating code blocks recursively where appropriate.
	 * @param fn the function descriptor
	 * @param vertex the current vertex
	 * @return a new code block containing all instructions below this node
	 */
	private Block instructifyGraph(Function fn, GraphNode vertex) {
		Block ret = new Block();
		// generate an expression block
		if (vertex.getInstruction() instanceof Entry) {
			
		}
		return ret;
	}

}
