package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.InstructionFactory;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.Type;
import ch.seto.vikdal.java.transformers.StateTracker;

public class InstanceOf extends AbstractInstruction {

	public static InstructionFactory factory() {
		return new InstructionFactory() { public Instruction newInstance() { return new InstanceOf(); } };
	}
	
	private int vA, vB, type;
	
	@Override
	public int getOpcode() {
		return 0x20;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_22c;
	}
	
	@Override
	public void setArguments(long[] args) {
		vA = (int) args[0];
		vB = (int) args[1];
		type = (int) args[2];
	}

	@Override
	public long[] getArguments() {
		return new long[] { vA, vB, type };
	}
	
	@Override
	public String toString() {
		return "v" + vA + " = v" + vB + " instanceof TYPE_" + type;
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		tracker.setRegisterType(vA, Type.BOOLEAN);
		return tracker.getRegisterName(vA) + " = " + tracker.getRegisterName(vB) + " instanceof " + Type.humanReadableDescriptor(table.lookupType(type));
	}
}