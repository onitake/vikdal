package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.Statement;

public class ArrayLength extends AbstractInstruction {

	public static InstructionFactory factory() {
		return new InstructionFactory() { public Instruction newInstance() { return new ArrayLength(); } };
	}
	
	private int vA, vB;
	
	@Override
	public int getOpcode() {
		return 0x21;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_12x;
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
		return "v" + vA + " = v" + vB + ".length";
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		tracker.setRegisterType(vA, Type.INT);
		return tracker.getRegisterName(vA) + " = " + tracker.getRegisterName(vB) + ".length";
	}

	@Override
	public Statement toAST(SymbolTable table) {
		NameExpr arrexp = new NameExpr("v" + vB);
		FieldAccessExpr exp = new FieldAccessExpr(arrexp, "length");
		NameExpr targexp = new NameExpr("v" + vA);
		Statement ret = new ExpressionStmt(new AssignExpr(targexp, exp, Operator.assign));
		ret.setData(this);
		return ret;
	}
}