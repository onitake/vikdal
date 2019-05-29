package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;

public class Nop extends AbstractInstruction {
	
	public Nop() { }
	
	@Override
	public int getOpcode() {
		return 0x00;
	}
	
	@Override
	public Format getFormat() {
		return Format.FORMAT_10x;
	}
	
	@Override
	public String toString() {
		return "";
	}

}