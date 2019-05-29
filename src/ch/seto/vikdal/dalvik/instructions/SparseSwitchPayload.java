package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;

public class SparseSwitchPayload extends AbstractInstruction {

	private int size;
	private int[] keys;
	private int[] targets;
	
	public SparseSwitchPayload() {
		size = 0;
	}
	
	public SparseSwitchPayload(short[] bytecode, int off) {
		setArguments(bytecode, off);
	}
	
	@Override
	public int getOpcode() {
		return 0x00;
	}

	@Override
	public int getInstructionSize() {
		return (size * 4) + 2;
	}

	@Override
	public short[] getBytecode() {
		short[] bytecode = new short[(size * 4) + 2];
		bytecode[0] = 0x0200;
		bytecode[1] = (short) size;
		for (int i = 0; i < size; i++) {
			bytecode[2 + i * 2] = (short) (keys[i] & 0xffff);
			bytecode[2 + i * 2 + 1] = (short) (keys[i] >>> 16);
		}
		for (int i = 0; i < size; i++) {
			bytecode[2 + size * 2 + i * 2] = (short) (targets[i] & 0xffff);
			bytecode[2 + size * 2 + i * 2 + 1] = (short) (targets[i] >>> 16);
		}
		return bytecode;
	}

	@Override
	public void setArguments(short[] bytecode, int off) {
		size = bytecode[off + 1];
		keys = new int[size];
		for (int i = 0; i < size; i++) {
			keys[i] = (bytecode[off + 2 + i * 2] & 0xffff) | (bytecode[off + 2 + i * 2 + 1] << 16);
		}
		targets = new int[size];
		for (int i = 0; i < size; i++) {
			targets[i] = (bytecode[off + 2 + size * 2 + i * 2] & 0xffff) | (bytecode[off + 2 + size * 2 + i * 2 + 1] << 16);
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
		keys = new int[size];
		for (int i = 0; i < size; i++) {
			keys[i] = (int) args[i + 1];
		}
		targets = new int[size];
		for (int i = 0; i < size; i++) {
			targets[i] = (int) args[size + i + 1];
		}
	}

	@Override
	public long[] getArguments() {
		long[] ret = new long[size * 2 + 1];
		ret[0] = size;
		for (int i = 0; i < size; i++) {
			ret[i + 1] = keys[i];
		}
		for (int i = 0; i < size; i++) {
			ret[size + i + 1] = targets[i];
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
			ret.append(keys[i]);
			ret.append(": GOTO ");
			ret.append(targets[0]);
		}
		for (i = 1; i < targets.length; i++) {
			ret.append("; case ");
			ret.append(keys[i]);
			ret.append(": GOTO ");
			ret.append(targets[i]);
		}
		ret.append(" }");
		return ret.toString();
	}

}
