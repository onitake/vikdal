package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;

public class MoveResult extends AbstractInstruction {

	public enum Operation {
		move_result(0x0a),
		move_result_wide(0x0b),
		move_result_object(0x0c),
		move_exception(0x0d);
		public final int opcode;
		Operation(int o) {
			opcode = o;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new MoveResult(op); } };
	}
	
	private Operation operation;
	private int vA;
	
	public MoveResult(Operation op) {
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
		return "v" + vA + " = RESULT";
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		// TODO infer type from previous instruction
		return tracker.getRegisterName(vA) + " = RESULT";
	}
	
}