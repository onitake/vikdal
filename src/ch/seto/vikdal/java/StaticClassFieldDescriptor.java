package ch.seto.vikdal.java;

import java.util.EnumSet;
import java.util.Map;

import ch.seto.vikdal.dex.Value;

public final class StaticClassFieldDescriptor extends ClassFieldDescriptor {
	/**
	 * The default value this static field is initialized to
	 */
	public final Value defvalue;

	public StaticClassFieldDescriptor(int fld, int typ, int ftyp, String nam, EnumSet<Modifier> mods, Value def) {
		super(fld, typ, ftyp, nam, mods);
		defvalue = def;
	}

	@Override
	public String toString(SymbolTable table) {
		return toString(table, null);
	}

	@Override
	public String toString(SymbolTable table, Map<String, Object> flags) {
		StringBuilder ret = new StringBuilder();
		ret.append(super.toString(table, flags));
		ret.append(" = ");
		ret.append(defvalue.getObjectValue());
		return ret.toString();
	}
}
