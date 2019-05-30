package ch.seto.vikdal.java;

import java.util.*;

public final class ClassMethodDescriptor extends MethodDescriptor {
	public final EnumSet<Modifier> modifiers;
	public final int registers;
	public final int inputs;
	public final int outputs;
	public final Set<TryDescriptor> exceptions;

	public ClassMethodDescriptor(int mtd, int typ, String nam, EnumSet<Modifier> acc, int ret, List<Integer> params, int regs, int ins, int outs, Set<TryDescriptor> exs) {
		super(mtd, typ, nam, ret, params);
		modifiers = acc;
		registers = regs;
		inputs = ins;
		outputs = outs;
		exceptions = exs;
	}
	
	@Override
	public String toString(SymbolTable table) {
		return toString(table, null);
	}

	/**
	 * Optional flags for this descriptor are:
	 * <ul>
	 * <li>{@link Descriptor#FULLY_QUALIFIED} (boolean, default true) - decorate the description with all method qualifiers</li>
	 * </ul>
	 * @see {@link Descriptor#toString(SymbolTable, Map)}
	 */
	@Override
	public String toString(SymbolTable table, Map<String, Object> flags) {
		StringBuilder ret = new StringBuilder();
		if (DescriptorUtils.valueForKey(flags, FULLY_QUALIFIED, true)) {
			for (Modifier modifier : modifiers)  {
				ret.append(modifier.description);
				ret.append(' ');
			}
		}
		ret.append(super.toString(table, flags));
		return ret.toString();
	}
}
