package ch.seto.vikdal.java.transformers;

import ch.seto.vikdal.dalvik.pseudo.Entry;
import ch.seto.vikdal.java.ClassMethodDescriptor;
import ch.seto.vikdal.java.EdgeTag;
import ch.seto.vikdal.java.Modifier;

/**
 * Adds entry point pseudo instruction (method header)
 */
public class FunctionHeaderGenerator implements Transformer<Function, Function> {
	@Override
	public Function transform(Function input) {
		ClassMethodDescriptor method = input.descriptor;
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
		input.code.addVertex(headerVertex);
		input.code.addEdge(headerVertex, input.headerVertex).setTag(EdgeTag.ENTRY);
		input.headerVertex = headerVertex;
		return input;
	}

}
