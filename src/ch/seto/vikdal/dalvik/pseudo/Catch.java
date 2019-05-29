package ch.seto.vikdal.dalvik.pseudo;

import java.util.Arrays;

import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;

public class Catch extends PseudoInstruction {
	
	private int[] types;
	private int[] catches;
	private int catchall;
	
	public Catch(int[] typs, int[] cats, int catall) {
		if (typs.length != cats.length) {
			throw new RuntimeException("Number of types doesn't match number of catch block targets");
		}
		types = Arrays.copyOf(typs, typs.length);
		catches = Arrays.copyOf(cats, cats.length);
		catchall = catall;
	}
	
	@Override
	public boolean hasBranches() {
		return true;
	}
	
	@Override
	public int[] getBranches() {
		return catches;
	}
	
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("{ ");
		boolean first = true;
		for (int i = 0; i < types.length; i++) {
			if (first) {
				first = false;
			} else {
				ret.append(", ");
			}
			ret.append("catch (TYPE_" + types[i] + ") GOTO " + catches[i]);
		}
		if (catchall != -1) {
			if (!first) {
				ret.append(", ");
			}
			ret.append("catch () GOTO " + catchall);
		}
		ret.append(" }");
		return ret.toString();
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		StringBuilder ret = new StringBuilder();
		ret.append("{ ");
		boolean first = true;
		for (int i = 0; i < types.length; i++) {
			if (first) {
				first = false;
			} else {
				ret.append(", ");
			}
			ret.append("catch (" + Type.humanReadableDescriptor(table.lookupType(types[i])) + ") GOTO " + catches[i]);
		}
		if (catchall != -1) {
			if (!first) {
				ret.append(", ");
			}
			ret.append("catch () GOTO " + catchall);
		}
		ret.append(" }");
		return ret.toString();
	}
	
}
