package ch.seto.vikdal.java.code;

import ch.seto.vikdal.java.SymbolTable;

/**
 * Interface representing a Java statement
 */
public interface Statement {

	/**
	 * Transform the Java statement into an AST statement.
	 * @return an AST statement node
	 */
	public japa.parser.ast.stmt.Statement toASTStatement(SymbolTable table);
	
}
