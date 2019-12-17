package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.Node;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.ThrowStmt;

public class Throw extends AbstractInstruction {

	public static InstructionFactory factory() {
		return new InstructionFactory() { public Instruction newInstance() { return new Throw(); } };
	}
	
	private int vA;
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_11x;
	}
	
	@Override
	public int getOpcode() {
		return 0x27;
	}
	
	@Override
	public void setArguments(long[] args) {
		vA = (int) args[0];
	}

	@Override
	public long[] getArguments() {
		return new long[] { vA };
	}
	
	@Override
	public String toString() {
		return "throw v" + vA;
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		return "throw " + tracker.getRegisterName(vA);
	}

	@Override
	public Node toAST() {
		NameExpr valexp = new NameExpr("v" + vA);
		Node ret = new ThrowStmt(valexp);
		ret.setData(this);
		return ret;
	}
	
}