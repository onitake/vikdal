package ch.seto.vikdal.java;

import java.util.Map;

/**
 * Interface for augmented string conversion methods, supporting automatic symbolication.
 */
public interface Descriptor {
	/**
	 * Boolean flag; request decoration with all qualifiers
	 */
	public static final String FULLY_QUALIFIED = "FULLY_QUALIFIED";
	/**
	 * Boolean flag; display types without package names
	 */
	public static final String SHORT_TYPES = "SHORT_TYPES";
	/**
	 * Boolean flag; request decoration with the argument list
	 */
	public static final String ARGUMENT_LIST = "ARGUMENT_LIST";
	/**
	 * Boolean flag; request class name decoration with its supertype (only if not {@link java.lang.Object})
	 */
	public static final String EXTENDS = "EXTENDS";
	/**
	 * Boolean flag; request decoration with field or return type
	 */
	public static final String TYPE = "TYPE";
	/**
	 * Boolean flag; request decoration with field value (constant fields)
	 */
	public static final String FIELD_VALUE = "FIELD_VALUE";

	/**
	 * Return a descriptive string for this instance, augmented with symbols from the given symbol table
	 * @param table a symbol lookup facility
	 * @return a string describing the instance in a human readable format
	 */
	public String toString(SymbolTable table);
	/**
	 * Return a descriptive string for this instance, augmented with symbols from the given symbol table.
	 * The optional flags argument allows configuration of the output format.
	 * @param table a symbol lookup facility
	 * @param flags a list of implementation specific display options, may be null if no options are desired
	 * @return a string describing the instance in a human readable format
	 */
	public String toString(SymbolTable table, Map<String, Object> flags);
}
