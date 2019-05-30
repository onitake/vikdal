package ch.seto.vikdal.java.code;

import java.util.ArrayList;
import java.util.List;

import japa.parser.ast.stmt.BlockStmt;

public class Block implements Statement {
	
	public List<Statement> statements;
	
	public Block() {
		statements = new ArrayList<Statement>();
	}
	
	@Override
	public japa.parser.ast.stmt.Statement toASTStatement() {
		List<japa.parser.ast.stmt.Statement> stmts = new ArrayList<japa.parser.ast.stmt.Statement>();
		for (Statement s : statements) {
			stmts.add(s.toASTStatement());
		}
		return new BlockStmt(stmts);
	}

}
