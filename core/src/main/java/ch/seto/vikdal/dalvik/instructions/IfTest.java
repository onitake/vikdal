package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.Statement;

public class IfTest extends AbstractInstruction {

	public enum Operation {
		if_eq(0x32, "=="),
		if_ne(0x33, "!="),
		if_lt(0x34, "<"),
		if_ge(0x35, ">="),
		if_gt(0x36, ">"),
		if_le(0x37, ">=");
		public final int opcode;
		public final String operator;
		Operation(int o, String p) {
			opcode = o;
			operator = p;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new IfTest(op); } };
	}
	
	private Operation operation;
	private int vA, vB, branch;
	
	public IfTest(Operation op) {
		operation = op;
	}
	
	@Override
	public int getOpcode() {
		return operation.opcode;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_22t;
	}
	
	@Override
	public void setArguments(long[] args) {
		vA = (int) args[0];
		vB = (int) args[1];
		// sign extension
		branch = ((int) args[2]) << 16 >> 16;
	}

	@Override
	public long[] getArguments() {
		return new long[] { vA, vB, branch };
	}
	
	@Override
	public boolean hasBranches() {
		return true;
	}
	
	@Override
	public int[] getBranches() {
		return new int[] { branch };
	}

	@Override
	public String toString() {
		return "if (v" + vA + " " + operation.operator + " v" + vB + ") GOTO " + (branch >= 0 ? "+" : "") + branch;
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		return "if (" + tracker.getRegisterName(vA) + " " + operation.operator + " " + tracker.getRegisterName(vB) + ") GOTO " + (branch >= 0 ? "+" : "") + branch;
	}

	@Override
	public Statement toAST(SymbolTable table) {
		NameExpr srcaexp = new NameExpr("v" + vA);
		NameExpr srcbexp = new NameExpr("v" + vB);
		BinaryExpr.Operator opexp = null;
		switch (operation) {
		case if_eq:
			opexp = BinaryExpr.Operator.equals;
			break;
		case if_ge:
			opexp = BinaryExpr.Operator.greaterEquals;
			break;
		case if_gt:
			opexp = BinaryExpr.Operator.greater;
			break;
		case if_le:
			opexp = BinaryExpr.Operator.lessEquals;
			break;
		case if_lt:
			opexp = BinaryExpr.Operator.less;
			break;
		case if_ne:
			opexp = BinaryExpr.Operator.notEquals;
			break;
		}
		if (opexp == null) {
			throw new RuntimeException("Invalid operation: " + operation.toString());
		}
		BinaryExpr exp = new BinaryExpr(srcaexp, srcbexp, opexp);
		Statement ret = new IfStmt(exp, null, null);
		ret.setData(this);
		return ret;
	}
	
}