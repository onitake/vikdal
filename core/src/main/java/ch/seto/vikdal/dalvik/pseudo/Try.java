package ch.seto.vikdal.dalvik.pseudo;

import japa.parser.ast.Node;
import japa.parser.ast.stmt.TryStmt;

public class Try extends PseudoInstruction {
	
	@Override
	public String toString() {
		return "try";
	}

	@Override
	public Node toAST() {
		Node ret = new TryStmt(null, null, null);
		ret.setData(this);
		return ret;
	}
	
}
