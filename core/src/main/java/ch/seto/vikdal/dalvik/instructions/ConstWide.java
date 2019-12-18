package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.Node;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.LongLiteralExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.ExpressionStmt;

public class ConstWide extends AbstractInstruction {

	public enum Operation {
		const_wide16(0x16, Format.FORMAT_21s, 48, false),
		const_wide32(0x17, Format.FORMAT_31i, 32, false),
		const_wide(0x18, Format.FORMAT_51l, 0, false),
		const_wide_high16(0x19, Format.FORMAT_21h, 48, true);
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
		return new InstructionFactory() { public Instruction newInstance() { return new ConstWide(op); } };
	}
	
	private Operation operation;
	private int vA;
	private long value;
	
	public ConstWide(Operation op) {
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
			value = args[1] << operation.shift;
		} else {
			// sign extension
			value = args[1] << operation.shift >> operation.shift;
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
		return "v" + vA + " = " + value + "L";
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		switch (tracker.getRegisterType(vA)) {
		case DOUBLE:
			return tracker.getRegisterName(vA) + " = " + Double.longBitsToDouble(value) + " // " + value;
		case LONG:
			return tracker.getRegisterName(vA) + " = " + value + "L // " + Double.longBitsToDouble(value);
		default:
			// differentiating types can only be made based on value heuristics, so we assume long by default
			tracker.setRegisterType(vA, Type.LONG);
			return tracker.getRegisterType(vA).toString() + ' ' + tracker.getRegisterName(vA) + " = " + value + "L // " + Double.longBitsToDouble(value);
		}
	}

	@Override
	public Node toAST(SymbolTable table) {
		NameExpr targexp = new NameExpr("v" + vA);
		LongLiteralExpr exp = new LongLiteralExpr(String.valueOf(value));
		Node ret = new ExpressionStmt(new AssignExpr(targexp, exp, AssignExpr.Operator.assign));
		ret.setData(this);
		return ret;
	}

}