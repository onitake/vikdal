package ch.seto.vikdal.dalvik.pseudo;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.dalvik.Instruction;
import ch.seto.vikdal.java.SymbolTable;
import ch.seto.vikdal.java.transformers.StateTracker;
import japa.parser.ast.comments.BlockComment;
import japa.parser.ast.stmt.EmptyStmt;
import japa.parser.ast.stmt.Statement;

public abstract class PseudoInstruction implements Instruction {

	@Override
	public final int getInstructionSize() {
		return 0;
	}

	@Override
	public final Format getFormat() {
		return null;
	}

	@Override
	public final int getOpcode() {
		return 0;
	}

	@Override
	public final short[] getBytecode() {
		return null;
	}

	@Override
	public final void setArguments(short[] bytecode, int off) {
	}

	@Override
	public void setArguments(long[] args) {
	}

	@Override
	public long[] getArguments() {
		return null;
	}

	@Override
	public boolean hasBranches() {
		return false;
	}

	@Override
	public int[] getBranches() {
		return null;
	}

	@Override
	public final boolean areBranchesRelative() {
		return false;
	}
	
	@Override
	public boolean breaksProgramFlow() {
		return false;
	}

	@Override
	public String toString(SymbolTable table, StateTracker tracker) {
		return toString();
	}

	@Override
	public Statement toAST(SymbolTable table) {
		// default transformation: just turn this into a block comment containing the pseudo instruction,
		// attached to an empty statement
		Statement ret = new EmptyStmt();
		ret.setComment(new BlockComment(toString()));
		ret.setData(this);
		return ret;
	}

}
