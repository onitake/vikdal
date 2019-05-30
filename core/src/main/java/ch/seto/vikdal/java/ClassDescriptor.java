package ch.seto.vikdal.java;

import java.util.*;

public final class ClassDescriptor implements Descriptor {
	/**
	 * The type id of this class
	 */
	public final int classid;
	/**
	 * The access and other modifiers
	 */
	public final EnumSet<Modifier> access;
	/**
	 * The type id of the superclass
	 */
	public final int superid;
	/**
	 * All implemented interfaces, a set of type ids
	 */
	public final Set<Integer> interfaces;
	/**
	 * The list of static and instance fields
	 */
	public final List<ClassFieldDescriptor> fields;
	/**
	 * The list of direct and virtual methods
	 */
	public final List<ClassMethodDescriptor> methods;
	
	// TODO: class annotations, field annotations, method annotations
	
	public ClassDescriptor(int clsid, EnumSet<Modifier> acc, int sipr, Set<Integer> ints, List<ClassFieldDescriptor> flds, List<ClassMethodDescriptor> mtds) {
		classid = clsid;
		access = acc;
		superid = sipr;
		interfaces = ints;
		fields = flds;
		methods = mtds;
	}

	@Override
	public String toString(SymbolTable table) {
		return toString(table, null);
	}

	@Override
	public String toString(SymbolTable table, Map<String, Object> flags) {
		StringBuilder name = new StringBuilder();
		name.append(Type.humanReadableDescriptor(table.lookupType(classid)));
		if (DescriptorUtils.valueForKey(flags, EXTENDS, false)) {
			if (superid != -1) {
				String supertype = table.lookupType(superid);
				if (!"Ljava/lang/Object;".equals(supertype)) {
					name.append(" extends ");
					name.append(Type.humanReadableDescriptor(supertype));
				}
			}
		}
		return name.toString();
	}
}
