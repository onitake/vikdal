package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.Node;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.ExpressionStmt;

public class Move extends AbstractInstruction {

	public enum Operation {
		move(0x01, Format.FORMAT_12x),
		move_from16(0x02, Format.FORMAT_22x),
		move16(0x03, Format.FORMAT_32x),
		move_wide(0x04, Format.FORMAT_12x),
		move_wide_from16(0x05, Format.FORMAT_22x),
		move_wide16(0x06, Format.FORMAT_32x),
		move_object(0x07, Format.FORMAT_12x),
		move_object_from16(0x08, Format.FORMAT_22x),
		move_object16(0x09, Format.FORMAT_32x);
		public final int opcode;
		public final Format format;
		Operation(int o, Format f) {
			opcode = o;
			format = f;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new Move(op); } };
	}
	
	private Operation operation;
	private int vA, vB;
	
	public Move(Operation op) {
		operation = op;
	}
	
	@Override
	public int getOpcode() {
		return operation.opcode;
	}
	
	@Override
	public Format getFormat() {
		return operation.format;
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
		return "v" + vA + " = v" + vB;
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		tracker.setRegisterType(vA, tracker.getRegisterType(vB));
		return tracker.getRegisterName(vA) + " = " + tracker.getRegisterName(vB);
	}

	@Override
	public Node toAST() {
		NameExpr targexp = new NameExpr("v" + vA);
		NameExpr srcexp = new NameExpr("v" + vB);
		Node ret = new ExpressionStmt(new AssignExpr(targexp, srcexp, AssignExpr.Operator.assign));
		ret.setData(this);
		return ret;
	}

}