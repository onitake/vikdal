package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.Node;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.ReturnStmt;

public class Return extends AbstractInstruction {

	public enum Operation {
		return_(0x0f),
		return_wide(0x10),
		return_object(0x11);
		public final int opcode;
		Operation(int o) {
			opcode = o;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new Return(op); } };
	}
	
	private Operation operation;
	private int vA;
	
	public Return(Operation op) {
		operation = op;
	}
	
	@Override
	public int getOpcode() {
		return operation.opcode;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_11x;
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
	public boolean breaksProgramFlow() {
		return true;
	}

	@Override
	public String toString() {
		return "return v" + vA;
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		return "return " + tracker.getRegisterName(vA);
	}

	@Override
	public Node toAST(SymbolTable table) {
		NameExpr valexp = new NameExpr("v" + vA);
		Node ret = new ReturnStmt(valexp);
		ret.setData(this);
		return ret;
	}

}