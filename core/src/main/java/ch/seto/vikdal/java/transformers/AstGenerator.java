package ch.seto.vikdal.java.transformers;

import org.jgrapht.traverse.DepthFirstIterator;

import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.code.Block;
import ch.seto.vikdal.java.code.Method;

public class AstGenerator {

	private SymbolTable table;

	/**
	 * Creates a transformer that can turn a list of instructions into a code graph.
	 * 
	 * @param table a symbol table
	 */
	public AstGenerator(SymbolTable table) {
		this.table = table;
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
	 * @param vertex the current graph node
	 * @return a new code block containing all instructions below this node
	 */
	private Block instructifyGraph(Function fn, GraphNode vertex) {
		DepthFirstIterator<GraphNode, GraphEdge> dfs = new DepthFirstIterator<>(fn.code);
		InstructionGenerator gen = new InstructionGenerator();
		dfs.addTraversalListener(gen);
		for (; dfs.hasNext(); dfs.next());
		return gen.getBlock();
	}

}
