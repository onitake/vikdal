package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;

public class ConstObject extends AbstractInstruction {

	public enum Operation {
		const_string(0x1a, Format.FORMAT_21c),
		const_string_jumbo(0x1b, Format.FORMAT_31c),
		const_class(0x1c, Format.FORMAT_21c);
		public final int opcode;
		public final Format format;
		Operation(int o, Format f) {
			opcode = o;
			format = f;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new ConstObject(op); } };
	}
	
	private Operation operation;
	private int vA, value;
	
	public ConstObject(Operation op) {
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
		value = (int) args[1];
	}

	@Override
	public long[] getArguments() {
		return new long[] { vA, value };
	}
	
	@Override
	public String toString() {
		if (operation == Operation.const_class) {
			return "v" + vA + " = TYPE_" + value;
		} else {
			return "v" + vA + " = STRING_" + value;
		}
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		tracker.setRegisterType(vA, Type.OBJECT);
		if (operation == Operation.const_class) {
			return tracker.getRegisterName(vA) + " = " + Type.humanReadableDescriptor(table.lookupType(value)) + ".class";
		} else {
			return tracker.getRegisterName(vA) + " = \"" + table.lookupString(value) + "\"";
		}
	}
}