package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;

public class Goto extends AbstractInstruction {

	public enum Operation {
		goto_(0x28, Format.FORMAT_10t, 24),
		goto16(0x29, Format.FORMAT_20t, 16),
		goto32(0x2a, Format.FORMAT_31i, 0);
		public final int opcode;
		public final Format format;
		public final int shift;
		Operation(int o, Format f, int s) {
			opcode = o;
			format = f;
			shift = s;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new Goto(op); } };
	}
	
	private Operation operation;
	private int branch;
	
	public Goto(Operation op) {
		operation = op;
	}
	
	@Override
	public int getOpcode() {
		return operation.opcode;
	}
	
	@Override
	public Format getFormat() {
		return operation.format;
	}
	
	@Override
	public void setArguments(long[] args) {
		// sign extension
		branch = (int) args[0] << operation.shift >> operation.shift;
	}

	@Override
	public long[] getArguments() {
		return new long[] { branch };
	}
	
	@Override
	public boolean hasBranches() {
		return true;
	}
	
	@Override
	public boolean breaksProgramFlow() {
		return true;
	}
	
	@Override
	public int[] getBranches() {
		return new int[] { branch };
	}

	@Override
	public String toString() {
		return "GOTO " + branch;
	}
}