package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.ClassDescriptor;
import ch.seto.vikdal.java.FieldDescriptor;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.AssignExpr.Operator;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;

public class StaticOp extends AbstractInstruction {

	public enum Operation {
		sget(0x60),
		sget_wide(0x61),
		sget_object(0x62),
		sget_boolean(0x63),
		sget_byte(0x64),
		sget_char(0x65),
		sget_short(0x66),
		sput(0x67),
		sput_wide(0x68),
		sput_object(0x69),
		sput_boolean(0x6a),
		sput_byte(0x6b),
		sput_char(0x6c),
		sput_short(0x6d);
		public final int opcode;
		Operation(int o) {
			opcode = o;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new StaticOp(op); } };
	}
	
	private Operation operation;
	private int vA, field;
	
	public StaticOp(Operation op) {
		operation = op;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_21c;
	}

	@Override
	public int getOpcode() {
		return operation.opcode;
	}

	@Override
	public void setArguments(long[] args) {
		vA = (int) args[0];
		field = (int) args[1];
	}

	@Override
	public long[] getArguments() {
		return new long[] { vA, field };
	}

	@Override
	public String toString() {
		switch (operation) {
		case sget:
		case sget_boolean:
		case sget_byte:
		case sget_char:
		case sget_object:
		case sget_short:
		case sget_wide:
			return "v" + vA + " = FIELD_" + field;
		case sput:
		case sput_boolean:
		case sput_byte:
		case sput_char:
		case sput_object:
		case sput_short:
		case sput_wide:
			return "FIELD_" + field + " = v" + vA;
		}
		return super.toString();
	}


	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		FieldDescriptor def = table.lookupField(field);
		String typ = table.lookupType(def.typeid);
		switch (operation) {
		case sget:
		case sget_boolean:
		case sget_byte:
		case sget_char:
		case sget_object:
		case sget_short:
		case sget_wide:
			tracker.setRegisterType(vA, Type.fromDescriptor(typ));
			return tracker.getRegisterName(vA) + " = " + Type.humanReadableDescriptor(typ) + "." + def.name;
		case sput:
		case sput_boolean:
		case sput_byte:
		case sput_char:
		case sput_object:
		case sput_short:
		case sput_wide:
			return Type.humanReadableDescriptor(table.lookupType(def.classid)) + "." + table.lookupField(field).name + " = " + tracker.getRegisterName(vA);
		}
		return super.toString(table, tracker);
	}

	@Override
	public Statement toAST(SymbolTable table) {
		FieldDescriptor fdesc = table.lookupField(field);
		ClassDescriptor cdesc = table.lookupClass(fdesc.classid);
		String cname = table.lookupType(cdesc.classid);
		FieldAccessExpr exp = new FieldAccessExpr(new ClassExpr(new ClassOrInterfaceType(cname)), fdesc.name);
		NameExpr valexp = new NameExpr("v" + vA);
		Statement ret = null;
		switch (operation) {
		case sget:
		case sget_boolean:
		case sget_byte:
		case sget_char:
		case sget_object:
		case sget_short:
		case sget_wide:
			ret = new ExpressionStmt(new AssignExpr(valexp, exp, Operator.assign));
			break;
		case sput:
		case sput_boolean:
		case sput_byte:
		case sput_char:
		case sput_object:
		case sput_short:
		case sput_wide:
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
