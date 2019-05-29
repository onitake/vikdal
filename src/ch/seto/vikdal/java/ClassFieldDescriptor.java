package ch.seto.vikdal.java;

import java.util.EnumSet;
import java.util.Map;

public class ClassFieldDescriptor extends FieldDescriptor {
	/**
	 * A set of modifiers for this field
	 */
	public final EnumSet<Modifier> modifiers;

	public ClassFieldDescriptor(int fld, int typ, int ftyp, String nam, EnumSet<Modifier> mods) {
		super(fld, typ, ftyp, nam);
		modifiers = mods;
	}

	@Override
	public String toString(SymbolTable table) {
		return toString(table, null);
	}

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
