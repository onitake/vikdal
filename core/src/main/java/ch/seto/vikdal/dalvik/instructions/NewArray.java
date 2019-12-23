package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.expr.ArrayCreationExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;

public class NewArray extends AbstractInstruction {

	public static InstructionFactory factory() {
		return new InstructionFactory() { public Instruction newInstance() { return new NewArray(); } };
	}
	
	private int vA, vB, type;
	
	@Override
	public int getOpcode() {
		return 0x23;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_22c;
	}
	
	@Override
	public void setArguments(long[] args) {
		vA = (int) args[0];
		vB = (int) args[1];
		type = (int) args[2];
	}

	@Override
	public long[] getArguments() {
		return new long[] { vA, vB, type };
	}
	
	@Override
	public String toString() {
		return "v" + vA + " = new TYPE_" + type + "[v" + vB + "]";
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		String regB = tracker.getRegisterName(vB);
		tracker.setRegisterType(vA, Type.ARRAY);
		return tracker.getRegisterName(vA) + " = new " + Type.humanReadableDescriptor(table.lookupType(type)) + "[" + regB + "]";
	}

	@Override
	public Statement toAST(SymbolTable table) {
		NameExpr targexp = new NameExpr("v" + vA);
		String tname = table.lookupType(type);
		ClassOrInterfaceType typexp = new ClassOrInterfaceType(tname);
		ArrayCreationExpr exp = new ArrayCreationExpr(typexp, vB, null);
		Statement ret = new ExpressionStmt(new AssignExpr(targexp, exp, AssignExpr.Operator.assign));
		ret.setData(this);
		return ret;
	}

}