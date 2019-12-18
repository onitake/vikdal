package ch.seto.vikdal.java.transformers;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.java.ClassMethodDescriptor;
import ch.seto.vikdal.java.SymbolTable;

public class Decompiler {
	@SuppressWarnings("unused")
	private final SymbolTable table;
	private final CodeGraphGenerator generator;
	private final List<Transformer<Function, Function>> transformations;

	public Decompiler(SymbolTable table) {
		this.table = table;
		generator = new CodeGraphGenerator();
		transformations = new ArrayList<>();
		transformations.add(new FunctionSymbolicator(table));
	}

	public Function transform(SortedMap<Integer, Instruction> input, ClassMethodDescriptor method) {
		Function fn = generator.transformToPseudoCode(input, method);
		for (Transformer<Function, Function> transformer : transformations) {
			fn = transformer.transform(fn);
		}
		return fn;
	}
}
