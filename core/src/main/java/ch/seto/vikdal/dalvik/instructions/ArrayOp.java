package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.Node;
import japa.parser.ast.expr.ArrayAccessExpr;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.expr.NameExpr;

public class ArrayOp extends AbstractInstruction {

	public enum Operation {
		aget(0x44),
		aget_wide(0x45),
		aget_object(0x46),
		aget_boolean(0x47),
		aget_byte(0x48),
		aget_char(0x49),
		aget_short(0x4a),
		aput(0x4b),
		aput_wide(0x4c),
		aput_object(0x4d),
		aput_boolean(0x4e),
		aput_byte(0x4f),
		aput_char(0x50),
		aput_short(0x51);
		public final int opcode;
		Operation(int o) {
			opcode = o;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new ArrayOp(op); } };
	}
	
	private Operation operation;
	private int vA, vB, vC;
	
	public ArrayOp(Operation op) {
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
		switch (operation) {
		case aget:
		case aget_boolean:
		case aget_byte:
		case aget_char:
		case aget_object:
		case aget_short:
		case aget_wide:
			return "v" + vA + " = v" + vB + "[v" + vC + "]";
		case aput:
		case aput_boolean:
		case aput_byte:
		case aput_char:
		case aput_object:
		case aput_short:
		case aput_wide:
			return "v" + vB + "[v" + vC + "] = v" + vA;
		}
		return super.toString();
	}

	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		boolean assign;
		switch (operation) {
		case aget:
			// assume int, could also be float
			tracker.setRegisterType(vA, Type.INT);
			assign = true;
			break;
		case aget_boolean:
			tracker.setRegisterType(vA, Type.BOOLEAN);
			assign = true;
			break;
		case aget_byte:
			tracker.setRegisterType(vA, Type.BYTE);
			assign = true;
			break;
		case aget_char:
			tracker.setRegisterType(vA, Type.CHAR);
			assign = true;
			break;
		case aget_object:
			tracker.setRegisterType(vA, Type.OBJECT);
			assign = true;
			break;
		case aget_short:
			tracker.setRegisterType(vA, Type.SHORT);
			assign = true;
			break;
		case aget_wide:
			// assume long, could also be double
			tracker.setRegisterType(vA, Type.LONG);
			assign = true;
			break;
		case aput:
		case aput_boolean:
		case aput_byte:
		case aput_char:
		case aput_object:
		case aput_short:
		case aput_wide:
			assign = false;
			break;
		default:
			return null;
		}
		if (assign) {
			return tracker.getRegisterName(vA) + " = " + tracker.getRegisterName(vB) + "[" + tracker.getRegisterName(vC) + "]";
		} else {
			return tracker.getRegisterName(vB) + "[" + tracker.getRegisterName(vC) + "] = " + tracker.getRegisterName(vA);
		}
	}

	@Override
	public Node toAST() {
		NameExpr arrexp = new NameExpr("v" + vB);
		NameExpr idxexp = new NameExpr("v" + vC);
		ArrayAccessExpr exp = new ArrayAccessExpr(arrexp, idxexp);
		NameExpr valexp = new NameExpr("v" + vA);
		Node ret = null;
		switch (operation) {
		case aget:
		case aget_boolean:
		case aget_byte:
		case aget_char:
		case aget_object:
		case aget_short:
		case aget_wide:
			ret = new ExpressionStmt(new AssignExpr(valexp, exp, Operator.assign));
			break;
		case aput:
		case aput_boolean:
		case aput_byte:
		case aput_char:
		case aput_object:
		case aput_short:
		case aput_wide:
			ret = new ExpressionStmt(new AssignExpr(exp, valexp, Operator.assign));
			break;
		}
		if (ret == null) {
			throw new RuntimeException("Invalid operation: " + operation.toString());
		}
		ret.setData(this);
		return ret;
	}
}
