package ch.seto.vikdal.java;

import java.util.List;
import java.util.Map;

public class MethodDescriptor implements Descriptor {
	/**
	 * The method id this descriptor represents
	 */
	public final int methodid;
	/**
	 * The type id of the class this method belongs to
	 */
	public final int classid;
	/**
	 * The method's name
	 */
	public final String name;
	/**
	 * The return type id
	 */
	public final int returntype;
	/**
	 * A list of type ids, one for each argument of the method
	 */
	public final List<Integer> parameters;

	public MethodDescriptor(int mtd, int typ, String nam, int ret, List<Integer> params) {
		methodid = mtd;
		classid = typ;
		name = nam;
		returntype = ret;
		parameters = params;
	}
	
	@Override
	public String toString(SymbolTable table) {
		return toString(table, null);
	}

	/**
	 * Optional flags for this descriptor are:
	 * <ul>
	 * <li>{@link Descriptor#SHORT_TYPES} (boolean, default false) - suppress full package names</li>
	 * <li>{@link Descriptor#ARGUMENT_LIST} (boolean, default true) - decorate the description with the argument list</li>
	 * </ul>
	 * @see {@link Descriptor#toString(SymbolTable, Map)}
	 */
	@Override
	public String toString(SymbolTable table, Map<String, Object> flags) {
		StringBuilder ret = new StringBuilder();
		String rettype = Type.humanReadableDescriptor(table.lookupType(returntype));
		if (DescriptorUtils.valueForKey(flags, SHORT_TYPES, false)) {
			rettype = rettype.substring(rettype.lastIndexOf('.') + 1);
		}
		ret.append(rettype);
		ret.append(' ');
		//ret.append(javaDescriptorToHuman(table.lookupType(classid)));
		//ret.append('.');
		if ("<init>".equals(name)) {
			// TODO: use the class name instead
			ret.append(name);
		} else {
			ret.append(name);
		}
		if (DescriptorUtils.valueForKey(flags, ARGUMENT_LIST, true)) {
			ret.append('(');
			boolean first = true;
			for (int parameter : parameters) {
				if (first) {
					first = false;
				} else {
					ret.append(", ");
				}
				String argtype = Type.humanReadableDescriptor(table.lookupType(parameter));
				if (DescriptorUtils.valueForKey(flags, SHORT_TYPES, false)) {
					argtype = argtype.substring(argtype.lastIndexOf('.') + 1);
				}
				ret.append(argtype);
			}
			ret.append(')');
		}
		return ret.toString();
	}
}
