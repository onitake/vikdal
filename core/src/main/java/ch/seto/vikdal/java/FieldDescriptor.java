package ch.seto.vikdal.java;

import java.util.Map;

public class FieldDescriptor implements Descriptor {
	/**
	 * The field id of this field
	 */
	public final int fieldid;
	/**
	 * The type id of the class this field belongs to
	 */
	public final int classid;
	/**
	 * The type id of this field
	 */
	public final int typeid;
	/**
	 * This field's name
	 */
	public final String name;

	public FieldDescriptor(int fld, int typ, int ftyp, String nam) {
		fieldid = fld;
		classid = typ;
		typeid = ftyp;
		name = nam;
	}
	
	@Override
	public String toString(SymbolTable table) {
		return toString(table, null);
	}

	@Override
	public String toString(SymbolTable table, Map<String, Object> flags) {
		StringBuilder ret = new StringBuilder();
		if (DescriptorUtils.valueForKey(flags, TYPE, true)) {
			String type = Type.humanReadableDescriptor(table.lookupType(typeid));
			if (DescriptorUtils.valueForKey(flags, SHORT_TYPES, false)) {
				type = type.substring(type.lastIndexOf('.') + 1);
			}
			ret.append(type);
			ret.append(' ');
		}
		if (DescriptorUtils.valueForKey(flags, FULLY_QUALIFIED, false)) {
			String type = Type.humanReadableDescriptor(table.lookupType(classid));
			if (DescriptorUtils.valueForKey(flags, SHORT_TYPES, false)) {
				type = type.substring(type.lastIndexOf('.') + 1);
			}
			ret.append(type);
			ret.append('.');
		}
		ret.append(name);
		return ret.toString();
	}
	
}
