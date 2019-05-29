package ch.seto.vikdal.dalvik.pseudo;

import java.util.Arrays;

import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;

public class Entry extends PseudoInstruction {

	private int type;
	private String name;
	private int firstreg;
	private int numregs;
	private int[] regtypes;
	
	public Entry(int typ, String nam, int first, int num, int[] rtyps) {
		type = typ;
		name = nam;
		firstreg = first;
		numregs = num;
		regtypes = Arrays.copyOf(rtyps, rtyps.length);
	}
	
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("TYPE_");
		ret.append(type);
		ret.append(' ');
		ret.append(name);
		ret.append('(');
		boolean first = true;
		for (int i = firstreg; i < firstreg + numregs; i++) {
			if (first) {
				first = false;
			} else {
				ret.append(", ");
			}
			ret.append('v');
			ret.append(i);
		}
		ret.append(')');
		return ret.toString();
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		StringBuilder ret = new StringBuilder();
		ret.append(Type.humanReadableDescriptor(table.lookupType(type)));
		ret.append(' ');
		ret.append(name);
		ret.append('(');
		boolean first = true;
		for (int i = firstreg, j = 0; i < firstreg + numregs; j++) {
			if (first) {
				first = false;
			} else {
				ret.append(", ");
			}
			// TODO array and object types lack the underlying type and are printed as 'Object' and '[]'
			//Type type = tracker.getRegisterType(i);
			//ret.append(type.toString());
			ret.append(Type.humanReadableDescriptor(table.lookupType(regtypes[j])));
			ret.append(' ');
			ret.append(tracker.getRegisterName(i));
			i += tracker.getRegisterType(i).getRegisterCount();
		}
		ret.append(')');
		return ret.toString();
	}
}
