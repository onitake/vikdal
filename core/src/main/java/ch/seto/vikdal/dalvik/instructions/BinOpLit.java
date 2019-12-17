package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.Node;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.ExpressionStmt;

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

	@Override
	public Node toAST() {
		NameExpr targexp = new NameExpr("v" + vA);
		NameExpr srcexp = new NameExpr("v" + vB);
		IntegerLiteralExpr valexp = new IntegerLiteralExpr(String.valueOf(value));
		BinaryExpr.Operator opexp = null;
		switch (operation) {
		case add_int16:
		case add_int8:
			opexp = BinaryExpr.Operator.plus;
			break;
		case and_int16:
		case and_int8:
			opexp = BinaryExpr.Operator.binAnd;
			break;
		case div_int16:
		case div_int8:
			opexp = BinaryExpr.Operator.divide;
			break;
		case mul_int16:
		case mul_int8:
			opexp = BinaryExpr.Operator.times;
			break;
		case or_int16:
		case or_int8:
			opexp = BinaryExpr.Operator.binOr;
			break;
		case rem_int16:
		case rem_int8:
			opexp = BinaryExpr.Operator.remainder;
			break;
		case rsub_int16:
		case rsub_int8:
			opexp = BinaryExpr.Operator.minus;
			break;
		case shl_int8:
			opexp = BinaryExpr.Operator.lShift;
			break;
		case shr_int8:
			opexp = BinaryExpr.Operator.rSignedShift;
			break;
		case ushr_int8:
			opexp = BinaryExpr.Operator.rUnsignedShift;
			break;
		case xor_int16:
		case xor_int8:
			opexp = BinaryExpr.Operator.xor;
			break;
		}
		if (opexp == null) {
			throw new RuntimeException("Invalid operation: " + operation.toString());
		}
		BinaryExpr exp = null;
		if (operation == Operation.rsub_int16 || operation == Operation.rsub_int8) {
			exp = new BinaryExpr(valexp, srcexp, opexp);
		} else {
			exp = new BinaryExpr(srcexp, valexp, opexp);
		}
		Node ret = new ExpressionStmt(new AssignExpr(targexp, exp, AssignExpr.Operator.assign));
		ret.setData(this);
		return ret;
	}	
}
