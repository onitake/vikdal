package ch.seto.vikdal.dalvik.instructions;

import java.util.ArrayList;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.MethodDescriptor;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.Node;
import japa.parser.ast.expr.ClassExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.SuperExpr;
import japa.parser.ast.stmt.ExpressionStmt;

public class Invoke extends AbstractInstruction {

	public enum Operation {
		invoke_virtual(0x6e),
		invoke_super(0x6f),
		invoke_direct(0x70),
		invoke_static(0x71),
		invoke_interface(0x72);
		public final int opcode;
		Operation(int o) {
			opcode = o;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new Invoke(op); } };
	}
	
	private Operation operation;
	private int size, method;
	private int[] vX;
	
	public Invoke(Operation op) {
		operation = op;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_35c;
	}

	@Override
	public int getOpcode() {
		return operation.opcode;
	}

	@Override
	public void setArguments(long[] args) {
		size = (int) args[0];
		method = (int) args[1];
		vX = new int[size];
		for (int i = 0; i < size; i++) {
			vX[i] = (int) args[2 + i];
		}
	}

	@Override
	public long[] getArguments() {
		long[] ret = new long[7];
		ret[0] = size;
		ret[1] = method;
		for (int i = 0; i < size; i++) {
			ret[i + 2] = (int) vX[i];
		}
		return ret;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		int first;
		if (operation == Operation.invoke_static) {
			first = 0;
		} else {
			ret.append("v");
			ret.append(vX[0]);
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
			ret.append(vX[n]);
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
			ret.append(tracker.getRegisterName(vX[0]));
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
			ret.append(tracker.getRegisterName(vX[n]));
		}
		ret.append(")");
		return ret.toString();
	}

	@Override
	public Node toAST() {
		// TODO METHOD LOOKUP
		String methodname = "METHOD_" + method;
		ArrayList<Expression> argsexp = new ArrayList<Expression>();
		for (int v : vX) {
			NameExpr argexp = new NameExpr("v" + v);
			argsexp.add(argexp);
		}
		Expression classexp = null;
		switch (operation) {
		case invoke_direct:
		case invoke_interface:
		case invoke_static:
		case invoke_virtual:
			// TODO pass method's containing type
			classexp = new ClassExpr();
			break;
		case invoke_super:
			// TODO pass method's containing type
			classexp = new SuperExpr();
			break;
		}
		if (classexp == null) {
			throw new RuntimeException("Invalid operation: " + operation.toString());
		}
		Node ret = new ExpressionStmt(new MethodCallExpr(classexp, methodname, argsexp));
		ret.setData(this);
		return ret;
	}

}
