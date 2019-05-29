package ch.seto.vikdal.java;

import java.util.Map;

public final class TryDescriptor {
	/**
	 * The instruction start address of the try block
	 */
	public final int start;
	/**
	 * The number of instructions covered by the try block
	 */
	public final int length;
	/**
	 * A list of exception type ids and corresponding catch block addresses
	 */
	public final Map<Integer, Integer> catches;
	/**
	 * The address of the empty catch block (i.e. catching all unspecified exceptions)
	 */
	public final int catchall;
	
	public TryDescriptor(int st, int len, Map<Integer, Integer> cats, int all) {
		start = st;
		length = len;
		catches = cats;
		catchall = all;
	}
}