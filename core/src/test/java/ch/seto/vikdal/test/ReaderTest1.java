package ch.seto.vikdal.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import ch.seto.vikdal.dex.DexReader;

public class ReaderTest1 {

	private static final String TEST_FILE1 = "/test1.dat";
	
	private static final float FLOAT_EPSILON = 0.000001f;
	private static final double DOUBLE_EPSILON = 0.00000000000001;
	
	private File datafile;

	@Before
	public void setUp() throws URISyntaxException {
		URL res = getClass().getResource(TEST_FILE1);
		assertNotNull("Can't find data file", res);
		datafile = new File(res.toURI());
	}
	
	@After
	public void tearDown() {
		datafile = null;
	}
	
	@Test
	public void test() throws IOException {
		DexReader reader = new DexReader(datafile);
		try {
			final byte[] byteCases = { 0, 16, 127, -128, -112, -1 };
			final int[] unsignedByteCases = { 0, 16, 127, 128, 144, 255 };
			final short[] shortCases = { 0, 16, 32767, -32768, -32752, -1 };
			final int[] unsignedShortCases = { 0, 16, 32767, 32768, 32784, 65535 };
			final int[] intCases = { 0, 16, 2147483647, -2147483648, -2147483632, -1 };
			final long[] unsignedIntCases = { 0L, 16L, 2147483647L, 2147483648L, 2147483664L, 4294967295L };
			final long[] longCases = { 0L, 16L, 9223372036854775807L, -9223372036854775808L, -9223372036854775792L, -1L };
			final float[] floatCases = { 0.0f, 1.0f, Float.MAX_VALUE, -0.0f, -1.0f, Float.MIN_VALUE };
			final double[] doubleCases = { 0.0, 1.0, Double.MAX_VALUE, -0.0, -1.0, Double.MIN_VALUE };
			final int[] sleb128Cases = { 0, 1, -1, -128 };
			final long[] uleb128Cases = { 0, 1, 127, 16256 };
			final long[] uleb128p1Cases = { -1, 0, 126, 16255 };
			
			for (byte cas : byteCases) {
				assertEquals("readByte()", cas, reader.readByte());
			}
			for (int cas : unsignedByteCases) {
				assertEquals("readUnsignedByte()", cas, reader.readUnsignedByte());
			}
			
			for (short cas : shortCases) {
				assertEquals("readLEShort()", cas, reader.readLEShort());
			}
			for (int cas : unsignedShortCases) {
				assertEquals("readLEUnsignedShort()", cas, reader.readLEUnsignedShort());
			}
			for (short cas : shortCases) {
				assertEquals("readBEShort()", cas, reader.readBEShort());
			}
			for (int cas : unsignedShortCases) {
				assertEquals("readBEUnsignedShort()", cas, reader.readBEUnsignedShort());
			}
			reader.setBigEndian(false);
			for (short cas : shortCases) {
				assertEquals("readShort(little)", cas, reader.readShort());
			}
			for (int cas : unsignedShortCases) {
				assertEquals("readUnsignedShort(little)", cas, reader.readUnsignedShort());
			}
			reader.setBigEndian(true);
			for (short cas : shortCases) {
				assertEquals("readShort(big)", cas, reader.readShort());
			}
			for (int cas : unsignedShortCases) {
				assertEquals("readUnsignedShort(big)", cas, reader.readUnsignedShort());
			}

			for (int cas : intCases) {
				assertEquals("readLEInt()", cas, reader.readLEInt());
			}
			for (long cas : unsignedIntCases) {
				assertEquals("readLEUnsignedInt()", cas, reader.readLEUnsignedInt());
			}
			for (int cas : intCases) {
				assertEquals("readBEInt()", cas, reader.readBEInt());
			}
			for (long cas : unsignedIntCases) {
				assertEquals("readBEUnsignedInt()", cas, reader.readBEUnsignedInt());
			}
			reader.setBigEndian(false);
			for (int cas : intCases) {
				assertEquals("readInt(little)", cas, reader.readInt());
			}
			for (long cas : unsignedIntCases) {
				assertEquals("readUnsignedInt(little)", cas, reader.readUnsignedInt());
			}
			reader.setBigEndian(true);
			for (int cas : intCases) {
				assertEquals("readInt(big)", cas, reader.readInt());
			}
			for (long cas : unsignedIntCases) {
				assertEquals("readUnsignedInt(big)", cas, reader.readUnsignedInt());
			}
			
			for (long cas : longCases) {
				assertEquals("readLELong()", cas, reader.readLELong());
			}
			for (long cas : longCases) {
				assertEquals("readBELong()", cas, reader.readBELong());
			}
			reader.setBigEndian(false);
			for (long cas : longCases) {
				assertEquals("readLong(little)", cas, reader.readLong());
			}
			reader.setBigEndian(true);
			for (long cas : longCases) {
				assertEquals("readLong(big)", cas, reader.readLong());
			}

			for (float cas : floatCases) {
				assertEquals("readLEFloat()", cas, reader.readLEFloat(), FLOAT_EPSILON);
			}
			for (float cas : floatCases) {
				assertEquals("readBEFloat()", cas, reader.readBEFloat(), FLOAT_EPSILON);
			}
			reader.setBigEndian(false);
			for (float cas : floatCases) {
				assertEquals("readFloat(little)", cas, reader.readFloat(), FLOAT_EPSILON);
			}
			reader.setBigEndian(true);
			for (float cas : floatCases) {
				assertEquals("readFloat(big)", cas, reader.readFloat(), FLOAT_EPSILON);
			}

			for (double cas : doubleCases) {
				assertEquals("readLEFloat()", cas, reader.readLEDouble(), DOUBLE_EPSILON);
			}
			for (double cas : doubleCases) {
				assertEquals("readBEFloat()", cas, reader.readBEDouble(), DOUBLE_EPSILON);
			}
			reader.setBigEndian(false);
			for (double cas : doubleCases) {
				assertEquals("readFloat(little)", cas, reader.readDouble(), DOUBLE_EPSILON);
			}
			reader.setBigEndian(true);
			for (double cas : doubleCases) {
				assertEquals("readFloat(big)", cas, reader.readDouble(), DOUBLE_EPSILON);
			}
			
			for (int cas : sleb128Cases) {
				assertEquals("readLEB128()", cas, reader.readLEB128());
			}
			for (long cas : uleb128Cases) {
				assertEquals("readUnsignedLEB128()", cas, reader.readUnsignedLEB128());
			}
			for (long cas : uleb128p1Cases) {
				assertEquals("readUnsignedLEB128P1()", cas, reader.readUnsignedLEB128P1());
			}
		} catch (IOException e) {
			fail(e.toString());
		} catch (RuntimeException e) {
			throw e;
		} finally {
			reader.close();
		}
	}

}
