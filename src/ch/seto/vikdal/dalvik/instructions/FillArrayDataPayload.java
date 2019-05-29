package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;

public class FillArrayDataPayload extends AbstractInstruction {

	private int element_width;
	private int size;
	private byte[] data;
	
	public FillArrayDataPayload() {
		element_width = 0;
		size = 0;
	}
	
	public FillArrayDataPayload(short[] bytecode, int off) {
		setArguments(bytecode, off);
	}
	
	@Override
	public int getOpcode() {
		return 0x00;
	}

	@Override
	public int getInstructionSize() {
		return (size * element_width + 1) / 2 + 4;
	}

	@Override
	public short[] getBytecode() {
		short[] bytecode = new short[(size * element_width + 1) / 2 + 4];
		bytecode[0] = 0x0300;
		bytecode[1] = (short) element_width;
		bytecode[2] = (short) (size & 0xffff);
		bytecode[3] = (short) (size >>> 16);
		for (int i = 0; i < data.length; i += 2) {
			bytecode[4 + i / 2] = (short) (data[i] & 0xff);
			if (i + 1 < data.length) {
				bytecode[4 + i / 2] |= (short) (data[i + 1] << 8);
			}
		}
		return bytecode;
	}

	@Override
	public void setArguments(short[] bytecode, int off) {
		element_width = bytecode[off + 1] & 0xffff;
		size = (bytecode[off + 2] & 0xffff) | (bytecode[off + 3] << 16);
		data = new byte[size * element_width];
		for (int i = 0; i < data.length; i += 2) {
			data[i] = (byte) (bytecode[off + 4 + i / 2] & 0xff);
			if (i + 1 < data.length) {
				data[i + 1] = (byte) (bytecode[off + 4 + i / 2] >>> 8);
			}
		}
	}

	@Override
	public Format getFormat() {
		// you shouldn't use this to decode/encode the instruction, rather call getByteCode directly
		return Format.FORMAT_00x;
	}

	@Override
	public void setArguments(long[] args) {
		element_width = (int) args[0];
		size = (int) args[1];
		data = new byte[size * element_width];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) args[i + 2];
		}
	}

	@Override
	public long[] getArguments() {
		long[] ret = new long[size * element_width + 2];
		ret[0] = element_width;
		ret[1] = size;
		for (int i = 0; i < data.length; i++) {
			ret[i + 2] = data[i];
		}
		return ret;
	}

}
