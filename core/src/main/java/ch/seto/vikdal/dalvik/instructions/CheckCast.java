package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.Node;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.expr.CastExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.type.ClassOrInterfaceType;

public class CheckCast extends AbstractInstruction {

	public static InstructionFactory factory() {
		return new InstructionFactory() { public Instruction newInstance() { return new CheckCast(); } };
	}
	
	private int vA, type;
	
	@Override
	public int getOpcode() {
		return 0x1f;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_21c;
	}
	
	@Override
	public void setArguments(long[] args) {
		vA = (int) args[0];
		type = (int) args[1];
	}

	@Override
	public long[] getArguments() {
		return new long[] { vA, type };
	}
	
	@Override
	public String toString() {
		return "v" + vA + " = (TYPE_" + type + ") v" + vA;
	}

	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		String regA = tracker.getRegisterName(vA);
		tracker.setAutomaticName(vA, table.lookupType(type));
		return tracker.getRegisterName(vA) + " = (" + Type.humanReadableDescriptor(table.lookupType(type)) + ") " + regA;
	}

	@Override
	public Node toAST() {
		// TODO TYPE LOOKUP
		String typename = "TYPE_" + type;
		NameExpr varexp = new NameExpr("v" + vA);
		CastExpr exp = new CastExpr(new ClassOrInterfaceType(typename), varexp);
		Node ret = new ExpressionStmt(new AssignExpr(varexp, exp, Operator.assign));
		ret.setData(this);
		return ret;
	}

}