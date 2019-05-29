package ch.seto.vikdal.java.code;

/**
 * Interface representing a Java statement
 */
public interface Statement {

	/**
	 * Transform the Java statement into an AST statement.
	 * @return an AST statement node
	 */
	public japa.parser.ast.stmt.Statement toASTStatement();
	
}
