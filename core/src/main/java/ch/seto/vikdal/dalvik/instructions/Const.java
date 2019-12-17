package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.Node;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.ExpressionStmt;

public class Const extends AbstractInstruction {

	public enum Operation {
		const4(0x12, Format.FORMAT_11n, 28, false),
		const16(0x13, Format.FORMAT_21s, 16, false),
		const_(0x14, Format.FORMAT_31i, 0, false),
		const_high16(0x15, Format.FORMAT_21h, 16, true);
		public final int opcode;
		public final Format format;
		public final int shift;
		public final boolean high;
		Operation(int o, Format f, int s, boolean h) {
			opcode = o;
			format = f;
			shift = s;
			high = h;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new Const(op); } };
	}
	
	private Operation operation;
	private int vA, value;
	
	public Const(Operation op) {
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
		vA = (int) args[0];
		if (operation.high) {
			value = (int) args[1] << operation.shift;
		} else {
			// sign extension
			value = (int) args[1] << operation.shift >> operation.shift;
		}
	}

	@Override
	public long[] getArguments() {
		if (operation.high) {
			return new long[] { vA, value >>> operation.shift };
		} else {
			return new long[] { vA, value };
		}
	}
	
	@Override
	public String toString() {
		return "v" + vA + " = " + value;
	}
	
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		switch (tracker.getRegisterType(vA)) {
		case FLOAT:
			return tracker.getRegisterName(vA) + " = " + Float.intBitsToFloat(value) + "f // " + value;
		case BYTE:
		case SHORT:
		case CHAR:
			return tracker.getRegisterName(vA) + " = " + value;
		case INT:
			return tracker.getRegisterName(vA) + " = " + value + " // " + Float.intBitsToFloat(value) + "f";
		default:
			// differentiating types can only be made based on value heuristics, so we assume int by default
			tracker.setRegisterType(vA, Type.INT);
			return tracker.getRegisterType(vA).toString() + ' ' + tracker.getRegisterName(vA) + " = " + value + " // " + Float.intBitsToFloat(value) + "f";
		}
	}

	@Override
	public Node toAST() {
		NameExpr targexp = new NameExpr("v" + vA);
		IntegerLiteralExpr exp = new IntegerLiteralExpr(String.valueOf(value));
		Node ret = new ExpressionStmt(new AssignExpr(targexp, exp, AssignExpr.Operator.assign));
		ret.setData(this);
		return ret;
	}
}