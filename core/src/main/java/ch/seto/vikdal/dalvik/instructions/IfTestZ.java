package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.Statement;

public class IfTestZ extends AbstractInstruction {

	public enum Operation {
		if_eqz(0x38, "=="),
		if_nez(0x39, "!="),
		if_ltz(0x3a, "<"),
		if_gez(0x3b, ">="),
		if_gtz(0x3c, ">"),
		if_lez(0x3d, "<=");
		public final int opcode;
		public final String operator;
		Operation(int o, String p) {
			opcode = o;
			operator = p;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new IfTestZ(op); } };
	}
	
	private Operation operation;
	private int vA, branch;
	
	public IfTestZ(Operation op) {
		operation = op;
	}
	
	@Override
	public int getOpcode() {
		return operation.opcode;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_21t;
	}
	
	@Override
	public void setArguments(long[] args) {
		vA = (int) args[0];
		// sign extension
		branch = ((int) args[1]) << 16 >> 16;
	}

	@Override
	public long[] getArguments() {
		return new long[] { vA, branch };
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
		return "if (v" + vA + " " + operation.operator + " 0) GOTO " + (branch >= 0 ? "+" : "") + branch;
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		return "if (" + tracker.getRegisterName(vA) + " " + operation.operator + " 0) GOTO " + (branch >= 0 ? "+" : "") + branch;
	}

	@Override
	public Statement toAST(SymbolTable table) {
		NameExpr srcexp = new NameExpr("v" + vA);
		BinaryExpr.Operator opexp = null;
		switch (operation) {
		case if_eqz:
			opexp = BinaryExpr.Operator.equals;
			break;
		case if_gez:
			opexp = BinaryExpr.Operator.greaterEquals;
			break;
		case if_gtz:
			opexp = BinaryExpr.Operator.greater;
			break;
		case if_lez:
			opexp = BinaryExpr.Operator.lessEquals;
			break;
		case if_ltz:
			opexp = BinaryExpr.Operator.less;
			break;
		case if_nez:
			opexp = BinaryExpr.Operator.notEquals;
			break;
		}
		if (opexp == null) {
			throw new RuntimeException("Invalid operation: " + operation.toString());
		}
		BinaryExpr exp = new BinaryExpr(srcexp, new IntegerLiteralExpr("0"), opexp);
		Statement ret = new IfStmt(exp, null, null);
		ret.setData(this);
		return ret;
	}
	
}