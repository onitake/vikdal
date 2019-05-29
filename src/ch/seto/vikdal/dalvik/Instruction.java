package ch.seto.vikdal.dalvik;

import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;

public interface Instruction {
	/**
	 * The number of bytecode values occupied by this instruction.<br/>
	 * May depend on the arguments. Do not use {@link AbstractInstruction#getFormat()}.getInstructionSize() directly.
	 * <br/><b>Note:</b> You don't need to override this method if you derive from {@link AbstractInstruction}
	 * and the instruction size is solely dependent on the opcode. The default implementation returns
	 * {@link AbstractInstruction#getFormat()}.getInstructionSize().
	 * @return the instruction size in bytecode words (shorts)
	 */
	public int getInstructionSize();
	/**
	 * The bytecode format of this instruction.
	 * @return an instruction format
	 */
	public Format getFormat();
	/**
	 * The opcode this instruction represents, a value between 0x00 and 0xff. 
	 * @return the opcode of the instruction
	 */
	public int getOpcode();	
	/**
	 * The encoded bytecode sequence for this instruction.
	 * <br/><b>Note:</b> You don't need to override this method if you derive from {@link AbstractInstruction}.
	 * @return a bytecode sequence
	 */
	public short[] getBytecode();
	/**
	 * Decodes and assigns the arguments from a given bytecode sequence.<br/>
	 * The sequence needs to hold all the bytecodes for this instruction, starting from offset off,
	 * or an exception will be thrown.
	 * <br/><b>Note:</b> You don't need to override this method if you derive from {@link AbstractInstruction}.
	 * @param bytecode an array of bytecodes
	 * @param off the offset of the first bytecode to examine
	 * @throws ArrayIndexOutOfBoundsException if the bytecode sequence is too short
	 */
	public void setArguments(short[] bytecode, int off);
	/**
	 * Assigns the list of decoded but not interpreted (as long) arguments to this instruction.
	 * <br/><b>Note:</b> You don't need to override this method if you derive from {@link AbstractInstruction}
	 * and the instruction doesn't take arguments. The default implementation doesn't do anything.
	 * @param args the list of instruction arguments
	 */
	public void setArguments(long[] args);
	/**
	 * Returns the list of interpreted (as long) but not encoded arguments of this instruction.
	 * <br/><b>Note:</b> You don't need to override this method if you derive from {@link AbstractInstruction}
	 * and the instruction doesn't take arguments. The default implementation returns null.
	 * @return the list of instruction arguments
	 */
	public long[] getArguments();
	/**
	 * Checks if this instruction has one ore more branch targets.
	 * <br/><b>Note:</b> You don't need to override this method if you derive from {@link AbstractInstruction}
	 * and the instruction doesn't branch. The default implementation returns false.
	 * @return true, if this is a branch instruction
	 */
	public boolean hasBranches();
	/**
	 * Returns a list of all branch targets for this instruction.<br/>
	 * <br/><b>Note:</b> You don't need to override this method if you derive from {@link AbstractInstruction}
	 * and your {@link AbstractInstruction#hasBranches()} returns false.
	 * @return a list of branch labels (relative if {@link #areBranchesRelative()} returns true)
	 */
	public int[] getBranches();
	/**
	 * Checks if this instruction has one ore more branch targets.
	 * <br/><b>Note:</b> You don't need to override this method if you derive from {@link AbstractInstruction}
	 * and the instruction doesn't branch, or your branches are relative. The default implementation returns true.
	 * @return true, if the branches from this instruction are relative
	 */
	public boolean areBranchesRelative();
	/**
	 * Checks if succeeding instructions can be reached directly, i.e. if there is a "next" instruction in the
	 * program flow. Branch targets do not count as "next" instructions. Return true if the instruction breaks
	 * flow, like in a return or unconditional goto statement. {@link AbstractInstruction#breaksProgramFlow()}
	 * returns false by default.
	 */
	public boolean breaksProgramFlow();
	/**
	 * Returns a human readable description of this instruction with added symbol names
	 * <br/><b>Note:</b> If you derive from {@link AbstractInstruction}, this method returns the same value as
	 * {@link AbstractInstruction#toString()} by default.
	 * @param table a symbol table to retrieve data from
	 * @param tracker a register state tracker, may be updated
	 */
	public String toString(SymbolTable table, StateTracker tracker);
}
