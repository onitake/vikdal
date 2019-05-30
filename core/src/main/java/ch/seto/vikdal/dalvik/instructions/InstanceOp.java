package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.FieldDescriptor;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;

public class InstanceOp extends AbstractInstruction {

	public enum Operation {
		iget(0x52),
		iget_wide(0x53),
		iget_object(0x54),
		iget_boolean(0x55),
		iget_byte(0x56),
		iget_char(0x57),
		iget_short(0x58),
		iput(0x59),
		iput_wide(0x5a),
		iput_object(0x5b),
		iput_boolean(0x5c),
		iput_byte(0x5d),
		iput_char(0x5e),
		iput_short(0x5f);
		public final int opcode;
		Operation(int o) {
			opcode = o;
		}
	}

	public static InstructionFactory factory(final Operation op) {
		return new InstructionFactory() { public Instruction newInstance() { return new InstanceOp(op); } };
	}
	
	private Operation operation;
	private int vA, vB, field;
	
	public InstanceOp(Operation op) {
		operation = op;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_22c;
	}

	@Override
	public int getOpcode() {
		return operation.opcode;
	}

	@Override
	public void setArguments(long[] args) {
		vA = (int) args[0];
		vB = (int) args[1];
		field = (int) args[2];
	}

	@Override
	public long[] getArguments() {
		return new long[] { vA, vB, field };
	}

	@Override
	public String toString() {
		switch (operation) {
		case iget:
		case iget_boolean:
		case iget_byte:
		case iget_char:
		case iget_object:
		case iget_short:
		case iget_wide:
			return "v" + vA + " = v" + vB + ".FIELD_" + field;
		case iput:
		case iput_boolean:
		case iput_byte:
		case iput_char:
		case iput_object:
		case iput_short:
		case iput_wide:
			return "v" + vB + ".FIELD_" + field + " = v" + vA;
		}
		return super.toString();
	}

	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		FieldDescriptor def = table.lookupField(field);
		switch (operation) {
		case iget:
		case iget_boolean:
		case iget_byte:
		case iget_char:
		case iget_object:
		case iget_short:
		case iget_wide:
			tracker.setRegisterType(vA, Type.fromDescriptor(table.lookupType(def.typeid)));
			return tracker.getRegisterName(vA) + " = " + tracker.getRegisterName(vB) + "." + def.name;
		case iput:
		case iput_boolean:
		case iput_byte:
		case iput_char:
		case iput_object:
		case iput_short:
		case iput_wide:
			return tracker.getRegisterName(vB) + "." + def.name + " = " + tracker.getRegisterName(vA);
		}
		return super.toString(table, tracker);
	}
}
