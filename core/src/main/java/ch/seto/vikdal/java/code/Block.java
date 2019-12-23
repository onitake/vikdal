package ch.seto.vikdal.java.code;

import java.util.ArrayList;
import java.util.List;

import ch.seto.vikdal.java.SymbolTable;
import japa.parser.ast.stmt.BlockStmt;

public class Block implements Statement {
	
	public List<Statement> statements;
	
	public Block() {
		statements = new ArrayList<Statement>();
	}
	
	@Override
	public japa.parser.ast.stmt.Statement toASTStatement(SymbolTable table) {
		List<japa.parser.ast.stmt.Statement> stmts = new ArrayList<japa.parser.ast.stmt.Statement>();
		for (Statement s : statements) {
			stmts.add(s.toASTStatement(table));
		}
		return new BlockStmt(stmts);
	}

}
