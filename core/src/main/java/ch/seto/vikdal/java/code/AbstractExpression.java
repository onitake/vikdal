package ch.seto.vikdal.java.code;

abstract class AbstractExpression implements Expression {

	@Override
	public japa.parser.ast.expr.Expression toASTExpression() {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " can not be represented as an AST expression");
	}

}
