package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;

public class Switch extends AbstractInstruction {

	public enum Operation {
		packed_switch(0x2b),
		sparse_switch(0x2c);
		public final int opcode;
		Operation(int o) {
			opcode = o;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new Switch(op); } };
	}
	
	private Operation operation;
	private int vA, table;
	
	public Switch(Operation op) {
		operation = op;
	}
	
	@Override
	public int getOpcode() {
		return operation.opcode;
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
		if (operation == Operation.packed_switch) {
			return "switch (v" + vA + ") PACKED_TABLE(" + table + ")";
		} else {
			return "switch (v" + vA + ") SPARSE_TABLE(" + table + ")";
		}
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		if (operation == Operation.packed_switch) {
			return "switch (" + tracker.getRegisterName(vA) + ") PACKED_TABLE(" + table + ")";
		} else {
			return "switch (" + tracker.getRegisterName(vA) + ") SPARSE_TABLE(" + table + ")";
		}
	}

}