package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.Node;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BinaryExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.ExpressionStmt;

public class Cmp extends AbstractInstruction {

	public enum Operation {
		cmpl_float(0x2d),
		cmpg_float(0x2e),
		cmpl_double(0x2f),
		cmpg_double(0x30),
		cmp_long(0x31);
		public final int opcode;
		Operation(int o) {
			opcode = o;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new Cmp(op); } };
	}
	
	private Operation operation;
	private int vA, vB, vC;
	
	public Cmp(Operation op) {
		operation = op;
	}
	
	@Override
	public int getOpcode() {
		return operation.opcode;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_23x;
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
		return "v" + vA + " = v" + vB + " < v" + vC + " ? -1 : v" + vB + " > v" + vC + " ? 1 : 0"; 
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		return tracker.getRegisterName(vA) + " = " + tracker.getRegisterName(vB) + " < " + tracker.getRegisterName(vC) + " ? -1 : " + tracker.getRegisterName(vB) + " > " + tracker.getRegisterName(vC) + " ? 1 : 0"; 
	}

	@Override
	public Node toAST() {
		NameExpr targexp = new NameExpr("v" + vA);
		NameExpr srcaexp = new NameExpr("v" + vB);
		NameExpr srcbexp = new NameExpr("v" + vC);
		BinaryExpr.Operator opexp = null;
		switch (operation) {
		case cmp_long:
			opexp = BinaryExpr.Operator.equals;
			break;
		case cmpg_double:
		case cmpg_float:
			opexp = BinaryExpr.Operator.greater;
			break;
		case cmpl_double:
		case cmpl_float:
			opexp = BinaryExpr.Operator.less;
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