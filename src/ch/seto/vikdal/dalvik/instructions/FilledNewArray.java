package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;

public class FilledNewArray extends AbstractInstruction {

	public static InstructionFactory factory() {
		return new InstructionFactory() { public Instruction newInstance() { return new FilledNewArray(); } };
	}
	
	private int size, type;
	private int[] vX;
	
	@Override
	public int getOpcode() {
		return 0x24;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_35c;
	}
	
	@Override
	public void setArguments(long[] args) {
		size = (int) args[0];
		type = (int) args[1];
		vX = new int[size];
		for (int i = 0; i < size; i++) {
			vX[i] = (int) args[2 + i];
		}
	}

	@Override
	public long[] getArguments() {
		long[] ret = new long[7];
		ret[0] = size;
		ret[1] = type;
		for (int i = 0; i < size; i++) {
			ret[i + 2] = (int) vX[i];
		}
		return ret;
	}
	
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("new TYPE_");
		ret.append(type);
		ret.append("[");
		ret.append(size);
		ret.append("] { ");
		boolean first = true;
		for (int n = 0; n < size; n++) {
			if (first) {
				first = false;
			} else {
				ret.append(", ");
			}
			ret.append("v");
			ret.append(vX[n]);
		}
		ret.append(" }");
		return ret.toString();
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		StringBuilder ret = new StringBuilder();
		ret.append("new ");
		ret.append(table.lookupType(type));
		ret.append("[");
		ret.append(size);
		ret.append("] { ");
		boolean first = true;
		for (int n = 0; n < size; n++) {
			if (first) {
				first = false;
			} else {
				ret.append(", ");
			}
			ret.append(tracker.getRegisterName(vX[n]));
		}
		ret.append(" }");
		return ret.toString();
	}
	
}