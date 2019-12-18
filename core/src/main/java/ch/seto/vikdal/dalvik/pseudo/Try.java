package ch.seto.vikdal.dalvik.pseudo;

import ch.seto.vikdal.java.SymbolTable;
import japa.parser.ast.Node;
import japa.parser.ast.stmt.TryStmt;

public class Try extends PseudoInstruction {
	
	@Override
	public String toString() {
		return "try";
	}

	@Override
	public Node toAST(SymbolTable table) {
		Node ret = new TryStmt(null, null, null);
		ret.setData(this);
		return ret;
	}
	
}
