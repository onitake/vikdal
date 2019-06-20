package ch.seto.vikdal.java.transformers;

import org.jgrapht.traverse.DepthFirstIterator;

import ch.seto.vikdal.java.Modifier;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;

public class FunctionSymbolicator implements Transformer<Function, Function> {

	private SymbolTable table;

	/**
	 * Creates a transformer that can turn a list of instructions into a code graph.
	 * 
	 * @param table a symbol table
	 */
	public FunctionSymbolicator(SymbolTable table) {
		this.table = table;
	}

	/**
	 * Generates symbol names for the variables and adds descriptions on each node.
	 * @param fn a code graph
	 * @return a symbolicated code graph
	 * @throws ProgramVerificationException if there was a type conflict
	 */
	@Override
	public Function transform(Function fn) throws ProgramVerificationException {
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

}
