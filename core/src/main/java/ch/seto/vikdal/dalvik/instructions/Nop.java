package ch.seto.vikdal.dalvik.instructions;

import ch.seto.vikdal.dalvik.Format;
import ch.seto.vikdal.java.SymbolTable;
import japa.parser.ast.Node;
import japa.parser.ast.comments.BlockComment;
import japa.parser.ast.stmt.EmptyStmt;

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

	@Override
	public Node toAST(SymbolTable table) {
		Node ret = new EmptyStmt();
		ret.setComment(new BlockComment("NOP"));
		ret.setData(this);
		return ret;
	}

}