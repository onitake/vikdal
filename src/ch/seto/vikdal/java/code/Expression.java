package ch.seto.vikdal.java.code;

/**
 * Interface representing a Java statement
 */
public interface Expression {

	/**
	 * Transform the Java statement into an AST expression.
	 * @return an AST expression node
	 */
	public japa.parser.ast.expr.Expression toASTExpression();
	
}
