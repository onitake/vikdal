package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.Node;
import japa.parser.ast.expr.*;
import japa.parser.ast.stmt.ExpressionStmt;

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

	@Override
	public Node toAST(SymbolTable table) {
		NameExpr targexp = new NameExpr("v" + vA);
		NameExpr srcaexp = new NameExpr("v" + vB);
		NameExpr srcbexp = new NameExpr("v" + vC);
		BinaryExpr.Operator opexp = null;
		switch (operation) {
		case add_double:
		case add_float:
		case add_int:
		case add_long:
			opexp = BinaryExpr.Operator.plus;
			break;
		case and_int:
		case and_long:
			// boolean AND is not covered by this
			opexp = BinaryExpr.Operator.binAnd;
			break;
		case div_double:
		case div_float:
		case div_int:
		case div_long:
			opexp = BinaryExpr.Operator.divide;
			break;
		case mul_double:
		case mul_float:
		case mul_int:
		case mul_long:
			opexp = BinaryExpr.Operator.times;
			break;
		case or_int:
		case or_long:
			// boolean OR is not covered by this
			opexp = BinaryExpr.Operator.binOr;
			break;
		case rem_double:
		case rem_float:
		case rem_int:
		case rem_long:
			opexp = BinaryExpr.Operator.remainder;
			break;
		case shl_int:
		case shl_long:
			opexp = BinaryExpr.Operator.lShift;
			break;
		case shr_int:
		case shr_long:
			opexp = BinaryExpr.Operator.rSignedShift;
			break;
		case sub_double:
		case sub_float:
		case sub_int:
		case sub_long:
			opexp = BinaryExpr.Operator.minus;
			break;
		case ushr_int:
		case ushr_long:
			opexp = BinaryExpr.Operator.rUnsignedShift;
			break;
		case xor_int:
		case xor_long:
			opexp = BinaryExpr.Operator.xor;
			break;
		}
		if (opexp == null) {
			throw new RuntimeException("Invalid operation: " + operation.toString());
		}
		BinaryExpr exp = new BinaryExpr(srcaexp, srcbexp, opexp);
		Node ret = new ExpressionStmt(new AssignExpr(targexp, exp, AssignExpr.Operator.assign));
		ret.setData(this);
		return ret;
	}	
}
