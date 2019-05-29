package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;

public class UnOp extends AbstractInstruction {

	public enum Operation {
		neg_int(0x7b, "-"),
		not_int(0x7c, "~"),
		neg_long(0x7d, "-"),
		not_long(0x7e, "~"),
		neg_float(0x7f, "-"),
		neg_double(0x80, "-"),
		int_to_long(0x81, "(long) "),
		int_to_float(0x82, "(float) "),
		int_to_double(0x83, "(double) "),
		long_to_int(0x84, "(int) "),
		long_to_float(0x85, "(float) "),
		long_to_double(0x86, "(double) "),
		float_to_int(0x87, "(int) "),
		float_to_long(0x88, "(long) "),
		float_to_double(0x89, "(double) "),
		double_to_int(0x8a, "(int) "),
		double_to_long(0x8b, "(long) "),
		double_to_float(0x8c, "(float) "),
		int_to_byte(0x8d, "(byte) "),
		int_to_char(0x8e, "(char) "),
		int_to_short(0x8f, "(short) ");
		public final int opcode;
		public final String operator;
		Operation(int o, String p) {
			opcode = o;
			operator = p;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new UnOp(op); } };
	}
	
	private Operation operation;
	private int vA, vB;
	
	public UnOp(Operation op) {
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
		return "v" + vA + " = " + operation.operator + "v" + vB;
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		String regB = tracker.getRegisterName(vB);
		switch (operation) {
		case double_to_float:
		case int_to_float:
		case long_to_float:
			tracker.setRegisterType(vA, Type.FLOAT);
			break;
		case double_to_int:
		case float_to_int:
		case long_to_int:
			tracker.setRegisterType(vA, Type.INT);
			break;
		case double_to_long:
		case float_to_long:
			tracker.setRegisterType(vA, Type.LONG);
			break;
		case float_to_double:
		case int_to_double:
		case long_to_double:
			tracker.setRegisterType(vA, Type.DOUBLE);
			break;
		case int_to_byte:
			tracker.setRegisterType(vA, Type.BYTE);
			break;
		case int_to_char:
			tracker.setRegisterType(vA, Type.CHAR);
			break;
		case int_to_long:
			tracker.setRegisterType(vA, Type.LONG);
			break;
		case int_to_short:
			tracker.setRegisterType(vA, Type.SHORT);
			break;
		case neg_double:
		case neg_float:
		case neg_int:
		case neg_long:
		case not_int:
		case not_long:
			tracker.setRegisterType(vA, tracker.getRegisterType(vB));
			break;
		}
		return tracker.getRegisterName(vA) + " = " + operation.operator + regB;
	}
}
