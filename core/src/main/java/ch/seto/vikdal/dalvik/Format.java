package ch.seto.vikdal.dalvik;

public enum Format {

	FORMAT_00x(0, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return null;
		}
		public short[] encodeArguments(long[] args) {
			return null;
		}
	}),
	
	FORMAT_10x(1, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[0];
		}
		public short[] encodeArguments(long[] args) {
			return new short[1];
		}
	}),
	
	FORMAT_12x(1, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xfL, (bytecode[off] >> 12) & 0xfL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) (((args[0] & 0xf) << 8) | ((args[1] & 0xf) << 12)) };
		}
	}),
	
	FORMAT_11n(1, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xfL, (bytecode[off] >> 12) & 0xffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) (((args[0] & 0xf) << 8) | ((args[1] & 0xf) << 12)) };
		}
	}),
	
	FORMAT_11x(1, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8) };
		}
	}),
	
	FORMAT_10t(1, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8) };
		}
	}),
	
	FORMAT_20t(2, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { bytecode[off + 1] & 0xffffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { 0, (short) (args[0] & 0xffff) };
		}
	}),
	
	FORMAT_20bc(2, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL, bytecode[off + 1] & 0xffffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8), (short) (args[1] & 0xffff) };
		}
	}),
	
	FORMAT_22x(2, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL, bytecode[off + 1] & 0xffffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8), (short) (args[1] & 0xffff) };
		}
	}),
	
	FORMAT_21t(2, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL, bytecode[off + 1] & 0xffffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8), (short) (args[1] & 0xffff) };
		}
	}),
	
	FORMAT_21s(2, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL, bytecode[off + 1] & 0xffffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8), (short) (args[1] & 0xffff) };
		}
	}),
	
	FORMAT_21h(2, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL, bytecode[off + 1] & 0xffffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8), (short) (args[1] & 0xffff) };
		}
	}),
	
	FORMAT_21c(2, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL, bytecode[off + 1] & 0xffffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8), (short) (args[1] & 0xffff) };
		}
	}),
	
	FORMAT_23x(2, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL, bytecode[off + 1] & 0xffL, (bytecode[off + 1] >> 8) & 0xffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8), (short) ((args[1] & 0xff) | ((args[2] & 0xff) << 8)) };
		}
	}),
	
	FORMAT_22b(2, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL, bytecode[off + 1] & 0xffL, (bytecode[off + 1] >> 8) & 0xffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8), (short) ((args[1] & 0xff) | ((args[2] & 0xff) << 8)) };
		}
	}),
	
	FORMAT_22t(2, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xfL, (bytecode[off] >> 12) & 0xfL, bytecode[off + 1] & 0xffffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) (((args[0] & 0xf) << 8) | ((args[1] & 0xf) << 12)), (short) (args[2] & 0xffff) };
		}
	}),
	
	FORMAT_22s(2, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xfL, (bytecode[off] >> 12) & 0xfL, bytecode[off + 1] & 0xffffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) (((args[0] & 0xf) << 8) | ((args[1] & 0xf) << 12)), (short) (args[2] & 0xffff) };
		}
	}),
	
	FORMAT_22c(2, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xfL, (bytecode[off] >> 12) & 0xfL, bytecode[off + 1] & 0xffffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) (((args[0] & 0xf) << 8) | ((args[1] & 0xf) << 12)), (short) (args[2] & 0xffff) };
		}
	}),
	
	FORMAT_22cs(2, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xfL, (bytecode[off] >> 12) & 0xfL, bytecode[off + 1] & 0xffffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) (((args[0] & 0xf) << 8) | ((args[1] & 0xf) << 12)), (short) (args[2] & 0xffff) };
		}
	}),
	
	FORMAT_30t(3, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off + 1] & 0xffffL) | ((bytecode[off + 2] & 0xffffL) << 16) };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { 0, (short) (args[0] & 0xffff), (short) ((args[0] >>> 16) & 0xffff) };
		}
	}),
	
	FORMAT_32x(3, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { bytecode[off + 1] & 0xffffL, bytecode[off + 2] & 0xffffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { 0, (short) (args[0] & 0xffff), (short) (args[1] & 0xffff) };
		}
	}),
	
	FORMAT_31i(3, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL, (bytecode[off + 1] & 0xffffL) | ((long) bytecode[off + 2] << 16) };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8), (short) (args[1] & 0xffff), (short) ((args[1] >>> 16) & 0xffff) };
		}
	}),
	
	FORMAT_31t(3, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL, (bytecode[off + 1] & 0xffffL) | ((long) bytecode[off + 2] << 16) };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8), (short) (args[1] & 0xffff), (short) ((args[1] >>> 16) & 0xffff) };
		}
	}),
	
	FORMAT_31c(3, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL, (bytecode[off + 1] & 0xffffL) | ((long) bytecode[off + 2] << 16) };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8), (short) (args[1] & 0xffff), (short) ((args[1] >>> 16) & 0xffff) };
		}
	}),
	
	FORMAT_35c(3, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 12) & 0xf, bytecode[off + 1] & 0xffffL, bytecode[off + 2] & 0xfL, (bytecode[off + 2] >> 4) & 0xfL, (bytecode[off + 2] >> 8) & 0xfL, (bytecode[off + 2] >> 12) & 0xfL, (bytecode[off] >> 8) & 0xfL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) (((args[0] & 0xf) << 12) | ((args[6] & 0xf) << 8)), (short) (args[1] & 0xffff), (short) ((args[2] & 0xf) | ((args[3] & 0xf) << 4) | ((args[4] & 0xf) << 8) | ((args[5] & 0xf) << 12)) };
		}
	}),
	
	FORMAT_35ms(3, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 12) & 0xf, bytecode[off + 1] & 0xffffL, bytecode[off + 2] & 0xfL, (bytecode[off + 2] >> 4) & 0xfL, (bytecode[off + 2] >> 8) & 0xfL, (bytecode[off + 2] >> 12) & 0xfL, (bytecode[off] >> 8) & 0xfL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) (((args[0] & 0xf) << 12) | ((args[6] & 0xf) << 8)), (short) (args[1] & 0xffff), (short) ((args[2] & 0xf) | ((args[3] & 0xf) << 4) | ((args[4] & 0xf) << 8) | ((args[5] & 0xf) << 12)) };
		}
	}),
	
	FORMAT_35mi(3, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 12) & 0xf, bytecode[off + 1] & 0xffffL, bytecode[off + 2] & 0xfL, (bytecode[off + 2] >> 4) & 0xfL, (bytecode[off + 2] >> 8) & 0xfL, (bytecode[off + 2] >> 12) & 0xfL, (bytecode[off] >> 8) & 0xfL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) (((args[0] & 0xf) << 12) | ((args[6] & 0xf) << 8)), (short) (args[1] & 0xffff), (short) ((args[2] & 0xf) | ((args[3] & 0xf) << 4) | ((args[4] & 0xf) << 8) | ((args[5] & 0xf) << 12)) };
		}
	}),
	
	FORMAT_3rc(3, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL, bytecode[off + 1] & 0xffffL, bytecode[off + 2] & 0xffffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8), (short) (args[1] & 0xffff), (short) (args[2] & 0xffff) };
		}
	}),
	
	FORMAT_3rms(3, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL, bytecode[off + 1] & 0xffffL, bytecode[off + 2] & 0xffffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8), (short) (args[1] & 0xffff), (short) (args[2] & 0xffff) };
		}
	}),
	
	FORMAT_3rmi(3, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL, bytecode[off + 1] & 0xffffL, bytecode[off + 2] & 0xffffL };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8), (short) (args[1] & 0xffff), (short) (args[2] & 0xffff) };
		}
	}),
	
	FORMAT_51l(5, new FormatHandler() {
		public long[] decodeArguments(short[] bytecode, int off) {
			return new long[] { (bytecode[off] >> 8) & 0xffL, (bytecode[off + 1] & 0xffffL) | ((bytecode[off + 2] & 0xffffL) << 16) | ((bytecode[off + 3] & 0xffffL) << 32) | ((bytecode[off + 4] & 0xffffL) << 48) };
		}
		public short[] encodeArguments(long[] args) {
			return new short[] { (short) ((args[0] & 0xff) << 8), (short) (args[1] & 0xffff), (short) ((args[1] >>> 16) & 0xffff), (short) ((args[1] >>> 32) & 0xffff), (short) ((args[1] >>> 48) & 0xffff) };
		}
	});

	private interface FormatHandler {
		public long[] decodeArguments(short[] bytecode, int off);
		public short[] encodeArguments(long[] args);
	}

	private final int size;
	private final FormatHandler handler;

	private Format(int s, FormatHandler h) {
		size = s;
		handler = h;
	}
	
	public int getInstructionSize() {
		return size;
	}

	public long[] decodeArguments(short[] bytecode, int off) {
		return handler.decodeArguments(bytecode, off);
	}

	public short[] encodeInstruction(int opcode, long[] args) {
		short[] instruction = handler.encodeArguments(args);
		instruction[0] = (short) ((instruction[0] & 0xff00) | (opcode & 0xff));
		return instruction;
	}

}
