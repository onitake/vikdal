package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;

public class BinOp extends AbstractInstruction {

	public enum Operation {
		add_int(0x90, "+"),
		sub_int(0x91, "-"),
		mul_int(0x92, "*"),
		div_int(0x93, "/"),
		rem_int(0x94, "%"),
		and_int(0x95, "&"),
		or_int(0x96, "|"),
		xor_int(0x97, "^"),
		shl_int(0x98, "<<"),
		shr_int(0x99, ">>"),
		ushr_int(0x9a, ">>>"),
		add_long(0x9b, "+"),
		sub_long(0x9c, "-"),
		mul_long(0x9d, "*"),
		div_long(0x9e, "/"),
		rem_long(0x9f, "%"),
		and_long(0xa0, "&"),
		or_long(0xa1, "|"),
		xor_long(0xa2, "^"),
		shl_long(0xa3, "<<"),
		shr_long(0xa4, ">>"),
		ushr_long(0xa5, ">>>"),
		add_float(0xa6, "+"),
		sub_float(0xa7, "-"),
		mul_float(0xa8, "*"),
		div_float(0xa9, "/"),
		rem_float(0xaa, "%"),
		add_double(0xab, "+"),
		sub_double(0xac, "-"),
		mul_double(0xad, "*"),
		div_double(0xae, "/"),
		rem_double(0xaf, "%");
		public final int opcode;
		public final String operator;
		Operation(int o, String p) {
			opcode = o;
			operator = p;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new BinOp(op); } };
	}
	
	private Operation operation;
	private int vA, vB, vC;
	
	public BinOp(Operation op) {
		operation = op;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_23x;
	}

	@Override
	public int getOpcode() {
		return operation.opcode;
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
		return "v" + vA + " = v" + vB + " " + operation.operator + " v" + vC;
	}

	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		String regB = tracker.getRegisterName(vB);
		String regC = tracker.getRegisterName(vC);
		tracker.setRegisterType(vA, tracker.getRegisterType(vB));
		return tracker.getRegisterName(vA) + " = " + regB + " " + operation.operator + " " + regC;
	}
	
}
