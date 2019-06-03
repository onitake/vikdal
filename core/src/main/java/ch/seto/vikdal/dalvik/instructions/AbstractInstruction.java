package ch.seto.vikdal.dalvik.instructions;

import java.util.Arrays;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.dalvik.Instructions;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;

public abstract class AbstractInstruction implements Instruction {
	
	@Override
	public int getInstructionSize() {
		return getFormat().getInstructionSize();
	}

	@Override
	public short[] getBytecode() {
		return getFormat().encodeInstruction(getOpcode(), getArguments());
	}
	
	@Override
	public boolean hasBranches() {
		return false;
	}
	
	@Override
	public int[] getBranches() {
		return new int[0];
	}
	
	@Override
	public boolean areBranchesRelative() {
		return true;
	}
	
	@Override
	public boolean breaksProgramFlow() {
		return false;
	}
	
	@Override
	public void setArguments(short[] bytecode, int off) {
		setArguments(getFormat().decodeArguments(bytecode, off));
	}

	@Override
	public void setArguments(long[] args) { }

	@Override
	public long[] getArguments() { return null; };

	@Override
	public int hashCode() {
		return Arrays.hashCode(getBytecode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Instruction other = (Instruction) obj;
		return Arrays.equals(getBytecode(), other.getBytecode());
	}
	
	@Override
	public String toString() {
		return Instructions.byteCodeToHexString(getBytecode());
	}
	
	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		return toString();
	}

}
