package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;

public class FilledNewArrayRange extends AbstractInstruction {

	public static InstructionFactory factory() {
		return new InstructionFactory() { public Instruction newInstance() { return new FilledNewArrayRange(); } };
	}
	
	private int vC, size, type;
	
	@Override
	public int getOpcode() {
		return 0x25;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_3rc;
	}
	
	@Override
	public void setArguments(long[] args) {
		size = (int) args[0];
		type = (int) args[1];
		vC = (int) args[2];
	}

	@Override
	public long[] getArguments() {
		return new long[] { size, type, vC };
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
			ret.append(vC + n);
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
			ret.append(tracker.getRegisterName(vC + n));
		}
		ret.append(" }");
		return ret.toString();
	}

}