package ch.seto.vikdal.java.code;

import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.IfStmt;

public class IfElse extends AbstractStatement {

	public Expression condition;
	public Block ifPath;
	public Block elsePath;
	
	public IfElse() {
		super();
	}

	public boolean hasElse() {
		return elsePath != null;
	}
	
	@Override
	public japa.parser.ast.stmt.Statement toASTStatement() {
		japa.parser.ast.expr.Expression condExpr = condition.toASTExpression();
		BlockStmt thenStmt = (BlockStmt) ifPath.toASTStatement();
		BlockStmt elseStmt = null;
		if (elsePath != null) {
			elseStmt = (BlockStmt) elsePath.toASTStatement();
		}
		return new IfStmt(condExpr, thenStmt, elseStmt);
	}

}
