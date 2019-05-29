package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;

public class BinOp2Addr extends AbstractInstruction {

	public enum Operation {
		add_int(0xb0, "+"),
		sub_int(0xb1, "-"),
		mul_int(0xb2, "*"),
		div_int(0xb3, "/"),
		rem_int(0xb4, "%"),
		and_int(0xb5, "&"),
		or_int(0xb6, "|"),
		xor_int(0xb7, "^"),
		shl_int(0xb8, "<<"),
		shr_int(0xb9, ">>"),
		ushr_int(0xba, ">>>"),
		add_long(0xbb, "+"),
		sub_long(0xbc, "-"),
		mul_long(0xbd, "*"),
		div_long(0xbe, "/"),
		rem_long(0xbf, "%"),
		and_long(0xc0, "&"),
		or_long(0xc1, "|"),
		xor_long(0xc2, "^"),
		shl_long(0xc3, "<<"),
		shr_long(0xc4, ">>"),
		ushr_long(0xc5, ">>>"),
		add_float(0xc6, "+"),
		sub_float(0xc7, "-"),
		mul_float(0xc8, "*"),
		div_float(0xc9, "/"),
		rem_float(0xca, "%"),
		add_double(0xcb, "+"),
		sub_double(0xcc, "-"),
		mul_double(0xcd, "*"),
		div_double(0xce, "/"),
		rem_double(0xcf, "%");
		public final int opcode;
		public final String operator;
		Operation(int o, String p) {
			opcode = o;
			operator = p;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new BinOp2Addr(op); } };
	}
	
	private Operation operation;
	private int vA, vB;
	
	public BinOp2Addr(Operation op) {
		operation = op;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_12x;
	}

	@Override
	public int getOpcode() {
		return operation.opcode;
	}

	@Override
	public void setArguments(long[] args) {
		vA = (int) args[0];
		vB = (int) args[1];
	}

	@Override
	public long[] getArguments() {
		return new long[] { vA, vB };
	}

	@Override
	public String toString() {
		return "v" + vA + " " + operation.operator + "= v" + vB;
	}

	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		String regB = tracker.getRegisterName(vB);
		return tracker.getRegisterName(vA) + " " + operation.operator + "= " + regB;
	}

}
