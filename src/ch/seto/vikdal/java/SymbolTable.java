package ch.seto.vikdal.java;

/**
 * Interface for classes supporting symbol, class and method table lookup
 */
public interface SymbolTable {
	/**
	 * Returns the number of strings.
	 */
	public int numberOfStrings();
	/**
	 * Finds and returns a string from the string table.
	 * @param index the string id
	 * @return the corresponding string, or null if not found
	 */
	public String lookupString(int index);
	/**
	 * Returns the number of methods.
	 */
	public int numberOfMethods();
	/**
	 * Finds and returns a method descriptor.
	 * @param index the method id
	 * @return a descriptor for the corresponding method, or null if not found
	 */
	public MethodDescriptor lookupMethod(int index);
	/**
	 * Finds and returns a class descriptor.
	 * @param index the type id of the class
	 * @return a descriptor of the corresponding class, or null if no class definition was found
	 */
	public ClassDescriptor lookupClass(int index);
	/**
	 * Returns the number of fields.
	 */
	public int numberOfFields();
	/**
	 * Finds and returns a field descriptor.
	 * @param index the field id
	 * @return a descriptor of the corresponding field, or null if not found
	 */
	public FieldDescriptor lookupField(int index);
	/**
	 * Returns the number of types.
	 */
	public int numberOfTypes();
	/**
	 * Finds and returns a type specifier.
	 * @param index the index of the type on the type declaration table
	 * @return a machine readable type specifier, or null if not found
	 */
	public String lookupType(int index);
}
