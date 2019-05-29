package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;

public class Monitor extends AbstractInstruction {

	public enum Operation {
		monitor_enter(0x1d),
		monitor_exit(0x1e);
		public final int opcode;
		Operation(int o) {
			opcode = o;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new Monitor(op); } };
	}
	
	private Operation operation;
	private int vA;
	
	public Monitor(Operation op) {
		operation = op;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_11x;
	}
	
	@Override
	public int getOpcode() {
		return operation.opcode;
	}
	
	@Override
	public void setArguments(long[] args) {
		vA = (int) args[0];
	}

	@Override
	public long[] getArguments() {
		return new long[] { vA };
	}
	
	@Override
	public String toString() {
		if (operation == Operation.monitor_enter) {
			return "SYNCSTART(v" + vA + ")";
		} else {
			return "SYNCEND(v" + vA + ")";
		}
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		if (operation == Operation.monitor_enter) {
			return "SYNCSTART(" + tracker.getRegisterName(vA) + ")";
		} else {
			return "SYNCEND(" + tracker.getRegisterName(vA) + ")";
		}
	}
}