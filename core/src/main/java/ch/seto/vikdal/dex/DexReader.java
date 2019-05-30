package ch.seto.vikdal.dex;

import java.io.*;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class DexReader implements DataInput, Closeable {
	
	private interface Seekable {
		public void seek(long pos) throws IOException;
		public long getFilePointer() throws IOException;
		public long length() throws IOException;
	}
	
	private interface SeekableDataInput extends Seekable, DataInput, Closeable { }
	
	private static class RandomAccessByteBuffer extends DataInputStream implements SeekableDataInput {
		private static class SeekableByteArrayInputStream extends ByteArrayInputStream implements Seekable {
			private int offset;

			public SeekableByteArrayInputStream(byte[] buf) {
				super(buf);
			}

			public SeekableByteArrayInputStream(byte[] buf, int offset, int length) {
				super(buf, offset, length);
				this.offset = offset;
			}

			@Override
			public void seek(long pos) throws IOException {
				if (pos > length() || pos < 0) {
					throw new EOFException("Seek position out of bounds");
				}
				this.pos = offset + (int) pos;
			}

			@Override
			public long getFilePointer() {
				return pos - offset;
			}

			@Override
			public long length() {
				return count - offset;
			}
		}
		
		public RandomAccessByteBuffer(byte[] data) {
			super(new SeekableByteArrayInputStream(data));
		}

		@Override
		public void seek(long pos) throws IOException {
			((Seekable) in).seek(pos);
		}

		@Override
		public long getFilePointer() throws IOException {
			return ((Seekable) in).getFilePointer();
		}

		@Override
		public long length() throws IOException {
			return ((Seekable) in).length();
		}
	}
	
	private static class RandomAccessFile2 extends RandomAccessFile implements SeekableDataInput {
		public RandomAccessFile2(File file, String mode) throws FileNotFoundException {
			super(file, mode);
		}

		public RandomAccessFile2(String name, String mode) throws FileNotFoundException {
			super(name, mode);
		}
	}

	private SeekableDataInput input;
	private boolean big;

	// Creates a new data input filter that supports various big and little endian data formats
	// Little endian mode is enabled by default, use setBigEndian(true) to switch
	public DexReader(String file) throws FileNotFoundException {
		this(new File(file));
	}
	
	// Creates a new data input filter that supports various big and little endian data formats
	// Little endian mode is enabled by default, use setBigEndian(true) to switch
	public DexReader(File file) throws FileNotFoundException {
		input = new RandomAccessFile2(file, "r");
		big = false;
	}

	// Creates a new data input filter that supports various big and little endian data formats
	// Little endian mode is enabled by default, use setBigEndian(true) to switch
	public DexReader(byte[] data) {
		input = new RandomAccessByteBuffer(data);
		big = false;
	}

	// Sets the data byte order to big endian (if mode is true) or little endian (if mode is false)
	public void setBigEndian(boolean mode) {
		big = mode;
	}
	
	public boolean getBigEndian() {
		return big;
	}
	
	public int read(byte[] buffer) throws IOException {
		try {
			input.readFully(buffer);
		} catch (EOFException e) {
			return -1;
		}
		return buffer.length;
	}

	public int read(byte[] buffer, int off, int len) throws IOException {
		int toread = Math.min(len, buffer.length);
		try {
			input.readFully(buffer, off, toread);
		} catch (EOFException e) {
			return -1;
		}
		return toread;
	}
	
	public int read() throws IOException {
		return input.readByte() & 0xff;
	}
	
	public long getFilePointer() throws IOException {
		return input.getFilePointer();
	}
	
	public long length() throws IOException {
		return input.length();
	}
	
	public void seek(long pos) throws IOException {
		input.seek(pos);
	}
	
	@Override
	public void close() throws IOException {
		input.close();
	}
	
	@Override
	public void readFully(byte[] buffer, int off, int len) throws IOException {
		input.readFully(buffer, off, len);
	}

	@Override
	public void readFully(byte[] buffer) throws IOException {
		input.readFully(buffer);
	}
	@Override
	public int skipBytes(int n) throws IOException {
		return input.skipBytes(n);
	}
	
	public int readLEInt() throws IOException {
		return (int) readRunlength(3, 3, false);
	}
	
	public int readBEInt() throws IOException {
		return (int) readRunlength(3, 3, true);
	}

	@Override
	public int readInt() throws IOException {
		return (int) readRunlength(3, 3, big);
	}
	
	public long readLEUnsignedInt() throws IOException {
		return readRunlength(3, 3, false) & 0xffffffffL;
	}
	
	public long readBEUnsignedInt() throws IOException {
		return readRunlength(3, 3, true) & 0xffffffffL;
	}

	public long readUnsignedInt() throws IOException {
		return readRunlength(3, 3, big) & 0xffffffffL;
	}

	public short readLEShort() throws IOException {
		return (short) readRunlength(1, 1, false);
	}
	
	public short readBEShort() throws IOException {
		return (short) readRunlength(1, 1, true);
	}

	@Override
	public short readShort() throws IOException {
		return (short) readRunlength(1, 1, big);
	}
	
	public int readLEUnsignedShort() throws IOException {
		return (int) (readRunlength(1, 1, false) & 0xffffL);
	}
	
	public int readBEUnsignedShort() throws IOException {
		return (int) (readRunlength(1, 1, true) & 0xffffL);
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return (int) (readRunlength(1, 1, big) & 0xffffL);
	}
	
	@Override
	public byte readByte() throws IOException {
		return (byte) readRunlength(0, 0, big);
	}
	
	@Override
	public int readUnsignedByte() throws IOException {
		return (int) (readRunlength(0, 0, big) & 0xffL);
	}
	
	public long readLELong() throws IOException {
		return readRunlength(7, 7, false);
	}
	
	public long readBELong() throws IOException {
		return readRunlength(7, 7, true);
	}

	@Override
	public long readLong() throws IOException {
		return readRunlength(7, 7, big);
	}

	public int readLEB128() throws IOException {
		int ret = 0;
		int data = read();
		if (data != -1) {
			ret |= data & 0x7f;
			if ((data & 0x80) != 0) {
				data = read();
				if (data != -1) {
					ret |= (data & 0x7f) << 7;
					if ((data & 0x80) != 0) {
						data = read();
						if (data != -1) {
							ret |= (data & 0x7f) << 14;
							if ((data & 0x80) != 0) {
								data = read();
								if (data != -1) {
									ret |= (data & 0x7f) << 21;
									if ((data & 0x80) != 0) {
										data = read();
										if (data != -1) {
											if ((data & 0x80) != 0) {
												throw new DexEncodingException("LEB128 too long");
											}
											ret |= (data & 0x0f) << 28;
										}
									} else {
										if ((data & 0x40) != 0) {
											ret |= 0xf0000000;
										}
									}
								}
							} else {
								if ((data & 0x40) != 0) {
									ret |= 0xffe00000;
								}
							}
						}
					} else {
						if ((data & 0x40) != 0) {
							ret |= 0xffffc000;
						}
					}
				}
			} else {
				if ((data & 0x40) != 0) {
					ret |= 0xffffff80;
				}
			}
		}
		return ret;
	}

	public long readUnsignedLEB128() throws IOException {
		long ret = 0;
		int data = read();
		if (data != -1) {
			ret |= data & 0x7fL;
			if ((data & 0x80) != 0) {
				data = read();
				if (data != -1) {
					ret |= (data & 0x7fL) << 7;
					if ((data & 0x80) != 0) {
						data = read();
						if (data != -1) {
							ret |= (data & 0x7fL) << 14;
							if ((data & 0x80) != 0) {
								data = read();
								if (data != -1) {
									ret |= (data & 0x7fL) << 21;
									if ((data & 0x80) != 0) {
										data = read();
										if (data != -1) {
											if ((data & 0x80) != 0) {
												throw new DexEncodingException("LEB128 too long");
											}
											ret |= (data & 0x0fL) << 28;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return ret;
	}

	public long readUnsignedLEB128P1() throws IOException {
		long ret = 0;
		int data = read();
		if (data != -1) {
			ret |= data & 0x7fL;
			if ((data & 0x80) != 0) {
				data = read();
				if (data != -1) {
					ret |= (data & 0x7fL) << 7;
					if ((data & 0x80) != 0) {
						data = read();
						if (data != -1) {
							ret |= (data & 0x7fL) << 14;
							if ((data & 0x80) != 0) {
								data = read();
								if (data != -1) {
									ret |= (data & 0x7fL) << 21;
									if ((data & 0x80) != 0) {
										data = read();
										if (data != -1) {
											if ((data & 0x80) != 0) {
												throw new DexEncodingException("LEB128 too long");
											}
											ret |= (data & 0x0fL) << 28;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return ret - 1;
	}

	@Override
	public String readUTF() throws IOException {
		long len = readUnsignedLEB128();
		int index = 0;
		char[] buffer = new char[(int) len];
		int data;
		while (index < len && (data = input.readByte()) > 0) {
			if ((data & 0x80) != 0) {
				if ((data & 0x40) != 0) {
					if ((data & 0x20) != 0) {
						if ((data & 0x10) != 0) {
							throw new DexEncodingException("Only 1-, 2- and 3-byte UTF-8 code points are valid for MUTF-8");
						}
						int data2 = input.readByte();
						if ((data2 & 0xc0) != 0x80) {
							throw new DexEncodingException("Invalid UTF-8 multibyte sequence");
						}
						int data3 = input.readByte();
						if ((data3 & 0xc0) != 0x80) {
							throw new DexEncodingException("Invalid UTF-8 multibyte sequence");
						}
						buffer[index++] = (char) (((data & 0x0f) << 12) | ((data2 & 0x3f) << 6) | (data3 & 0x3f));
					} else {
						int data2 = input.readByte();
						if ((data2 & 0xc0) != 0x80) {
							throw new DexEncodingException("Invalid UTF-8 multibyte sequence");
						}
						buffer[index++] = (char) (((data & 0x0f) << 6) | (data2 & 0x3f));
					}
				} else {
					throw new DexEncodingException("Invalid UTF-8 coding");
				}
			} else {
				buffer[index++] = (char) (data & 0x7f);
			}
		}
		return new String(buffer);
	}

	public double readLEDouble() throws IOException {
		return Double.longBitsToDouble(readLeftRunlength(7, 7, false));
	}

	public double readBEDouble() throws IOException {
		return Double.longBitsToDouble(readLeftRunlength(7, 7, true));
	}

	@Override
	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readLeftRunlength(7, 7, big));
	}

	public float readLEFloat() throws IOException {
		return Float.intBitsToFloat((int) readLeftRunlength(3, 3, false));
	}

	public float readBEFloat() throws IOException {
		return Float.intBitsToFloat((int) readLeftRunlength(3, 3, true));
	}

	@Override
	public float readFloat() throws IOException {
		return Float.intBitsToFloat((int) readLeftRunlength(3, 3, big));
	}

	public Entry<Long, Map<Long, Value>> readEncodedAnnotation() throws IOException {
		long type_idx = readUnsignedLEB128();
		long annotation_size = readUnsignedLEB128();
		Map<Long, Value> elements = new HashMap<Long, Value>();
		for (int i = 0; i < (int) annotation_size; i++) {
			long name_idx = readUnsignedLEB128();
			Value value = readEncodedValue();
			elements.put(name_idx, value);
		}
		return new AbstractMap.SimpleEntry<Long, Map<Long, Value>>(type_idx, elements);
	}
	
	public Value[] readEncodedArray() throws IOException {
		long array_size = readUnsignedLEB128();
		Value[] values = new Value[(int) array_size];
		for (int i = 0; i < (int) array_size; i++) {
			values[i] = readEncodedValue();
		}
		return values;
	}
	
	public Value readEncodedValue() throws IOException {
		int typearg = readUnsignedByte();
		int type = typearg & 0x1f;
		int arg = (typearg >> 5) & 0x7;
		Type vtype = Type.fromFlag(type);
		switch (vtype) {
		case VALUE_ANNOTATION:
			return new Value(vtype, readEncodedAnnotation());
		case VALUE_ARRAY:
			return new Value(vtype, readEncodedArray());
		case VALUE_BOOLEAN:
			if (arg < 0 || arg > 1) {
				throw new DexEncodingException("Invalid boolean value");
			}
			return new Value(vtype, arg == 1);
		case VALUE_BYTE:
			return new Value(vtype, (byte) readRunlength(arg, 0, big));
		case VALUE_CHAR:
			return new Value(vtype, (char) readRunlength(arg, 1, big));
		case VALUE_DOUBLE:
			return new Value(vtype, Double.longBitsToDouble(readLeftRunlength(arg, 7, big)));
		case VALUE_ENUM:
			return new Value(vtype, (int) readRunlength(arg, 3, big));
		case VALUE_FIELD:
			return new Value(vtype, (int) readRunlength(arg, 3, big));
		case VALUE_FLOAT:
			return new Value(vtype, Float.intBitsToFloat((int) readLeftRunlength(arg, 3, big)));
		case VALUE_INT:
			return new Value(vtype, (int) readRunlength(arg, 3, big));
		case VALUE_LONG:
			return new Value(vtype, readLeftRunlength(arg, 7, big));
		case VALUE_METHOD:
			return new Value(vtype, (int) readRunlength(arg, 3, big));
		case VALUE_NULL:
			if (arg < 0 || arg > 0) {
				throw new DexEncodingException("Invalid null value");
			}
			return new Value(vtype, null);
		case VALUE_SHORT:
			return new Value(vtype, (short) readRunlength(arg, 1, big));
		case VALUE_STRING:
			return new Value(vtype, (int) readRunlength(arg, 3, big));
		case VALUE_TYPE:
			return new Value(vtype, (int) readRunlength(arg, 3, big));
		default:
			throw new DexEncodingException("Unknown data type " + type);
		}
	}
	
	@Override
	public boolean readBoolean() throws IOException {
		return readByte() != 0;
	}

	@Override
	public char readChar() throws IOException {
		return (char) readShort();
	}

	@Override
	public String readLine() throws IOException {
		return input.readLine();
	}
	
	private long readRunlength(int runlen, int maxrun, boolean bigendian) throws IOException {
		if (runlen < 0 || runlen > maxrun) {
			throw new IllegalArgumentException("Invalid run length for type");
		}
		byte[] data = new byte[runlen + 1];
		if (read(data) != data.length) {
			throw new DexEncodingException("Short read on input");
		}
		long ret = 0;
		if (bigendian) {
			for (int i = 0; i < runlen; i++) {
				ret |= ((long) data[runlen - i] & 0xff) << (i << 3);
			}
			ret |= (long) data[0] << (runlen << 3);
		} else {
			for (int i = 0; i < runlen; i++) {
				ret |= ((long) data[i] & 0xff) << (i << 3);
			}
			ret |= (long) data[runlen] << (runlen << 3);
		}
		return ret;
	}

	private long readLeftRunlength(int runlen, int maxrun, boolean bigendian) throws IOException {
		if (runlen < 0 || runlen > maxrun) {
			throw new IllegalArgumentException("Invalid run length for type");
		}
		byte[] data = new byte[runlen + 1];
		if (read(data) != data.length) {
			throw new DexEncodingException("Short read on input");
		}
		long ret = 0;
		if (bigendian) {
			for (int i = 0; i < runlen; i++) {
				ret |= ((long) data[runlen - i] & 0xff) << ((i + maxrun - runlen) << 3);
			}
			ret |= (long) data[0] << (maxrun << 3);
		} else {
			for (int i = 0; i < runlen; i++) {
				ret |= ((long) data[i] & 0xff) << ((i + maxrun - runlen) << 3);
			}
			ret |= (long) data[runlen] << (maxrun << 3);
		}
		return ret;
	}

}
