package ch.seto.vikdal.java.transformers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.DepthFirstIterator;

import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.java.ClassMethodDescriptor;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.code.Method;

import japa.parser.ast.Node;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.type.ClassOrInterfaceType;

public class Decompiler {
	private final SymbolTable table;
	private final List<Transformer<Function, Function>> transformer;
	private final AstGenerator ast;

	public Decompiler(SymbolTable table) {
		this.table = table;
		transformer = new ArrayList<>();
		transformer.add(new FunctionHeaderGenerator());
		transformer.add(new BranchGenerator());
		transformer.add(new DeadCodeCleaner());
		transformer.add(new ExceptionAugmenter());
		transformer.add(new GotoCleaner());
		transformer.add(new FunctionSymbolicator(table));
		ast = new AstGenerator(table);
	}

	public Function graphify(SortedMap<Integer, Instruction> input, ClassMethodDescriptor method) {
		// generate a code graph from the instruction list
		DirectedGraph<GraphNode, GraphEdge> graph = new DirectedMultigraph<GraphNode, GraphEdge>(GraphEdge.FACTORY);
		Map<Integer, InstructionGraphNode> nodes = new LinkedHashMap<Integer, InstructionGraphNode>();
		InstructionGraphNode previous = null;
		InstructionGraphNode entryVertex = null;
		for (Map.Entry<Integer, Instruction> entry : input.entrySet()) {
			InstructionGraphNode node = new InstructionGraphNode(entry.getKey(), entry.getValue());
			graph.addVertex(node);
			nodes.put(node.getAddress(), node);
			if (previous != null) {
				graph.addEdge(previous, node);
			}
			previous = node;
			if (entry.getKey() == 0) {
				entryVertex = node;
			}
		}
		// construct a basic linear function
		return new Function(method, entryVertex, graph);
	}
	
	public Function transform(Function fn) {
		for (Transformer<Function, Function> transformer : transformer) {
			fn = transformer.transform(fn);
		}
		return fn;
	}
	
	public MethodDeclaration astify(Function fn) {
		MethodDeclaration method = new MethodDeclaration();
		method.setName(fn.descriptor.name);
		method.setType(new ClassOrInterfaceType(table.lookupType(fn.descriptor.returntype)));
		ArrayList<Parameter> parameters = new ArrayList<>();
		for (int param : fn.descriptor.parameters) {
			String tname = table.lookupType(param);
			parameters.add(new Parameter(0, new ClassOrInterfaceType(tname), null));
		}
		method.setParameters(parameters);
		method.setData(fn);
		DepthFirstIterator<GraphNode, GraphEdge> iterator = new DepthFirstIterator<GraphNode, GraphEdge>(fn.code, fn.headerVertex);
		BodyGenerator bodygen = new BodyGenerator(table);
		iterator.addTraversalListener(bodygen);
		for (; iterator.hasNext(); iterator.next());
		method.setBody(bodygen.getBody());
		return method;
	}

	public Node decompile(SortedMap<Integer, Instruction> input, ClassMethodDescriptor method) {
		Function fn = graphify(input, method);
		fn = transform(fn);
		Method m = ast.transformToStatements(fn);
		return m.getASTBody(table);
	}
}
