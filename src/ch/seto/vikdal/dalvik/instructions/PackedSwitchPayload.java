package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;

public class PackedSwitchPayload extends AbstractInstruction {

	private int size;
	private int first_key;
	private int[] targets;
	
	public PackedSwitchPayload() {
		size = 0;
	}
	
	public PackedSwitchPayload(short[] bytecode, int off) {
		setArguments(bytecode, off);
	}
	
	@Override
	public int getOpcode() {
		return 0x00;
	}

	@Override
	public int getInstructionSize() {
		return (size * 2) + 4;
	}

	@Override
	public short[] getBytecode() {
		short[] bytecode = new short[(size * 2) + 4];
		bytecode[0] = 0x0100;
		bytecode[1] = (short) size;
		bytecode[2] = (short) (first_key & 0xffff);
		bytecode[3] = (short) (first_key >>> 16);
		for (int i = 0; i < size; i++) {
			bytecode[4 + i * 2] = (short) (targets[i] & 0xffff);
			bytecode[4 + i * 2 + 1] = (short) (targets[i] >>> 16);
		}
		return bytecode;
	}

	@Override
	public void setArguments(short[] bytecode, int off) {
		size = bytecode[off + 1];
		first_key = (bytecode[off + 2] & 0xffff) | (bytecode[off + 3] << 16);
		targets = new int[size];
		for (int i = 0; i < size; i++) {
			targets[i] = (bytecode[off + 4 + i * 2] & 0xffff) | (bytecode[off + 4 + i * 2 + 1] << 16);
		}
	}

	@Override
	public Format getFormat() {
		// you shouldn't use this to decode/encode the instruction, rather call getByteCode directly
		return Format.FORMAT_00x;
	}

	@Override
	public void setArguments(long[] args) {
		size = (int) args[0];
		first_key = (int) args[1];
		targets = new int[size];
		for (int i = 0; i < size; i++) {
			targets[i] = (int) args[i + 2];
		}
	}

	@Override
	public long[] getArguments() {
		long[] ret = new long[size + 2];
		ret[0] = size;
		ret[1] = first_key;
		for (int i = 0; i < size; i++) {
			ret[i + 2] = targets[i];
		}
		return ret;
	}

	@Override
	public boolean hasBranches() {
		return true;
	}

	@Override
	public int[] getBranches() {
		return targets;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("{ ");
		int i = 0;
		if (targets.length > 0) {
			ret.append("case ");
			ret.append(first_key + i);
			ret.append(": GOTO ");
			ret.append(targets[0]);
		}
		for (i = 1; i < targets.length; i++) {
			ret.append("; case ");
			ret.append(first_key + i);
			ret.append(": GOTO ");
			ret.append(targets[i]);
		}
		ret.append(" }");
		return ret.toString();
	}

}
