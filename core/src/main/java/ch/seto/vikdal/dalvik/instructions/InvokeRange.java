package ch.seto.vikdal.dalvik.instructions;

import java.util.ArrayList;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.ClassDescriptor;
import ch.seto.vikdal.java.MethodDescriptor;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.QualifiedNameExpr;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.type.ClassOrInterfaceType;

public class InvokeRange extends AbstractInstruction {

	public enum Operation {
		invoke_virtual(0x74),
		invoke_super(0x75),
		invoke_direct(0x76),
		invoke_static(0x77),
		invoke_interface(0x78);
		public final int opcode;
		Operation(int o) {
			opcode = o;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new InvokeRange(op); } };
	}
	
	private Operation operation;
	private int size, method, vC;
	
	public InvokeRange(Operation op) {
		operation = op;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_3rc;
	}

	@Override
	public int getOpcode() {
		return operation.opcode;
	}

	@Override
	public void setArguments(long[] args) {
		size = (int) args[0];
		method = (int) args[1];
		vC = (int) args[2];
	}

	@Override
	public long[] getArguments() {
		return new long[] { size, method, vC };
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		int first;
		if (operation == Operation.invoke_static) {
			first = 0;
		} else {
			ret.append("v");
			ret.append(vC);
			ret.append(".");
			first = 1;
		}
		ret.append("METHOD_");
		ret.append(method);
		ret.append("(");
		boolean firsta = true;
		for (int n = first; n < size; n++) {
			if (firsta) {
				firsta = false;
			} else {
				ret.append(", ");
			}
			ret.append("v");
			ret.append(vC + n);
		}
		ret.append(")");
		return ret.toString();
	}

	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		// TODO update register
		// TODO use the type to test for super calls
		MethodDescriptor def = table.lookupMethod(method);
		StringBuilder ret = new StringBuilder();
		int n;
		if (operation == Operation.invoke_static) {
			n = 0;
			ret.append(Type.humanReadableDescriptor(table.lookupType(def.classid)));
		} else {
			ret.append(tracker.getRegisterName(vC));
			n = 1;
		}
		ret.append(".");
		ret.append(def.name);
		ret.append("(");
		boolean first = true;
		for (; n < size; n++) {
			if (first) {
				first = false;
			} else {
				ret.append(", ");
			}
			ret.append(tracker.getRegisterName(vC + n));
		}
		ret.append(")");
		return ret.toString();
	}

	@Override
	public Statement toAST(SymbolTable table) {
		MethodDescriptor mdesc = table.lookupMethod(method);
		ArrayList<Expression> argsexp = new ArrayList<Expression>();
		Expression classexp = null;
		switch (operation) {
		case invoke_static:
			for (int n = 0; n < size; n++) {
				NameExpr argexp = new NameExpr("v" + (vC + n));
				argsexp.add(argexp);
			}
			ClassDescriptor cdesc = table.lookupClass(mdesc.classid);
			classexp = new ClassExpr(new ClassOrInterfaceType(table.lookupType(cdesc.classid)));
			break;
		case invoke_direct:
		case invoke_interface:
		case invoke_virtual:
			if (size < 1) {
				throw new RuntimeException("Can't call an instance method without object reference");
			}
			for (int n = 1; n < size; n++) {
				NameExpr argexp = new NameExpr("v" + (vC + n));
				argsexp.add(argexp);
			}
			classexp = new NameExpr("v" + vC);
			break;
		case invoke_super:
			if (size < 1) {
				throw new RuntimeException("Can't call an instance method without object reference");
			}
			for (int n = 1; n < size; n++) {
				NameExpr argexp = new NameExpr("v" + (vC + n));
				argsexp.add(argexp);
			}
			classexp = new QualifiedNameExpr(new NameExpr("v" + vC), "super");
			break;
		}
		if (classexp == null) {
			throw new RuntimeException("Invalid operation: " + operation.toString());
		}
		Statement ret = new ExpressionStmt(new MethodCallExpr(classexp, mdesc.name, argsexp));
		ret.setData(this);
		return ret;
	}
}
