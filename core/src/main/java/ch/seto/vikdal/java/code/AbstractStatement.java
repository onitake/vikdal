package ch.seto.vikdal.java.code;

import japa.parser.ast.comments.LineComment;
import japa.parser.ast.stmt.EmptyStmt;

abstract class AbstractStatement implements Statement {

	@Override
	public japa.parser.ast.stmt.Statement toASTStatement() {
		// Rather display this as a comment instead of throwing
		japa.parser.ast.stmt.Statement stmt = new EmptyStmt();
		stmt.setComment(new LineComment(getClass().getSimpleName() + " can not be represented as an AST statement"));
		return stmt;
		//throw new UnsupportedOperationException(getClass().getSimpleName() + " can not be represented as an AST statement");
	}

}
