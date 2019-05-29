package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;

public class BinOpLit extends AbstractInstruction {

	public enum Operation {
		add_int16(0xd0, "+", true),
		rsub_int16(0xd1, "-", true),
		mul_int16(0xd2, "*", true),
		div_int16(0xd3, "/", true),
		rem_int16(0xd4, "%", true),
		and_int16(0xd5, "&", true),
		or_int16(0xd6, "|", true),
		xor_int16(0xd7, "^", true),
		add_int8(0xd8, "+", false),
		rsub_int8(0xd9, "-", false),
		mul_int8(0xda, "*", false),
		div_int8(0xdb, "/", false),
		rem_int8(0xdc, "%", false),
		and_int8(0xdd, "&", false),
		or_int8(0xde, "|", false),
		xor_int8(0xdf, "^", false),
		shl_int8(0xe0, "<<", false),
		shr_int8(0xe1, ">>", false),
		ushr_int8(0xe2, ">>>", false); 
		public final int opcode;
		public final String operator;
		public final boolean bit16;
		Operation(int o, String p, boolean w) {
			opcode = o;
			operator = p;
			bit16 = w;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new BinOpLit(op); } };
	}
	
	private Operation operation;
	private int vA, vB, value;
	
	public BinOpLit(Operation op) {
		operation = op;
	}
	
	@Override
	public Format getFormat() {
		if (operation.bit16) {
			return Format.FORMAT_22s;
		} else {
			return Format.FORMAT_22b;
		}
	}

	@Override
	public int getOpcode() {
		return operation.opcode;
	}

	@Override
	public void setArguments(long[] args) {
		vA = (int) args[0];
		vB = (int) args[1];
		// sign extension
		if (operation.bit16) {
			value = ((int) args[2]) << 16 >> 16;
		} else {
			value = ((int) args[2]) << 24 >> 24;
		}
	}

	@Override
	public long[] getArguments() {
		return new long[] { vA, vB, value };
	}

	@Override
	public String toString() {
		if (operation == Operation.rsub_int16 || operation == Operation.rsub_int8) {
			return "v" + vA + " = " + value + " " + operation.operator + " v" + vB;
		} else {
			return "v" + vA + " = v" + vB + " " + operation.operator + " " + value;
		}
	}

	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		String regB = tracker.getRegisterName(vB);
		if (operation == Operation.rsub_int16 || operation == Operation.rsub_int8) {
			//tracker.setRegisterType(vA, Type.INT);
			return tracker.getRegisterName(vA) + " = " + value + " " + operation.operator + " " + regB;
		} else {
			tracker.setRegisterType(vA, tracker.getRegisterType(vB));
			return tracker.getRegisterName(vA) + " = " + regB + " " + operation.operator + " " + value;
		}
	}

}
