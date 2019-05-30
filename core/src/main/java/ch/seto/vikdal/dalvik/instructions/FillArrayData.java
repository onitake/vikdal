package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;

public class FillArrayData extends AbstractInstruction {

	public static InstructionFactory factory() {
		return new InstructionFactory() { public Instruction newInstance() { return new FillArrayData(); } };
	}
	
	private int vA, table;
	
	@Override
	public int getOpcode() {
		return 0x26;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_31t;
	}
	
	@Override
	public void setArguments(long[] args) {
		vA = (int) args[0];
		table = (int) args[1];
	}

	@Override
	public long[] getArguments() {
		return new long[] { vA, table };
	}
	
	@Override
	public boolean hasBranches() {
		return true;
	}
	
	@Override
	public int[] getBranches() {
		return new int[] { table };
	}
	
	@Override
	public String toString() {
		return "v" + vA + " = ARRAY_TABLE(" + (table >= 0 ? "+" : "") + table + ")";
	}

	@Override
	public String toString(SymbolTable symbols, StateTracker tracker) {
		tracker.setRegisterType(vA, Type.ARRAY);
		return tracker.getRegisterName(vA) + " = ARRAY_TABLE(" + (table >= 0 ? "+" : "") + table + ")";
	}

}