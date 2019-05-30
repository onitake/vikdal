package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;

public class Cmp extends AbstractInstruction {

	public enum Operation {
		cmpl_float(0x2d),
		cmpg_float(0x2e),
		cmpl_double(0x2f),
		cmpg_double(0x30),
		cmp_long(0x31);
		public final int opcode;
		Operation(int o) {
			opcode = o;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new Cmp(op); } };
	}
	
	private Operation operation;
	private int vA, vB, vC;
	
	public Cmp(Operation op) {
		operation = op;
	}
	
	@Override
	public int getOpcode() {
		return operation.opcode;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_23x;
	}
	
	@Override
	public void setArguments(long[] args) {
		vA = (int) args[0];
		vB = (int) args[1];
		vC = (int) args[2];
	}

	@Override
	public long[] getArguments() {
		return new long[] { vA, vB, vC };
	}
	
	@Override
	public String toString() {
		return "v" + vA + " = v" + vB + " < v" + vC + " ? -1 : v" + vB + " > v" + vC + " ? 1 : 0"; 
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		return tracker.getRegisterName(vA) + " = " + tracker.getRegisterName(vB) + " < " + tracker.getRegisterName(vC) + " ? -1 : " + tracker.getRegisterName(vB) + " > " + tracker.getRegisterName(vC) + " ? 1 : 0"; 
	}
}