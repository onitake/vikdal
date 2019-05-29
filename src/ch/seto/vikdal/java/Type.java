package ch.seto.vikdal.java;

public enum Type {
	/**
	 * Void is only a valid type under certain circumstances and does not actually
	 * have a size of one register. This is only meant to avoid infinite loops.
	 */
	VOID("void", 1),
	BOOLEAN("boolean", 1),
	BYTE("byte", 1),
	SHORT("short", 1),
	CHAR("char", 1),
	INT("int", 1),
	/**
	 * Only used as an argument to {@link StateTracker#setRegisterType(int, Type)},
	 * will cause register to be set to LONG_LOW and register+1 to LONG_HIGH
	 */
	LONG("long", 2),
	LONG_LOW("long", 2),
	LONG_HIGH("long", 2),
	FLOAT("float", 1),
	/**
	 * Only used as an argument to {@link StateTracker#setRegisterType(int, Type)},
	 * will cause register to be set to DOUBLE_LOW and register+1 to DOUBLE_HIGH
	 */
	DOUBLE("double", 2),
	DOUBLE_LOW("double", 2),
	DOUBLE_HIGH("double", 2),
	ARRAY("[]", 1),
	OBJECT("Object", 1);
	
	/**
	 * The human-readable type name.
	 */
	private final String name;
	/**
	 * The number of machine base registers required for this type.
	 */
	private final int registers;
	
	private Type(String n, int r) {
		name = n;
		registers = r;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * @return the number of registers required by this type
	 */
	public int getRegisterCount() {
		return registers;
	}
	
	public static Type fromDescriptor(String descriptor) {
		if (descriptor == null || descriptor.length() < 1) {
			return null;
		}
		switch (descriptor.charAt(0)) {
		case 'B':
			return BYTE;
		case 'C':
			return CHAR;
		case 'D':
			return DOUBLE;
		case 'F':
			return FLOAT;
		case 'I':
			return INT;
		case 'J':
			return LONG;
		case 'L':
			return OBJECT;
		case 'S':
			return SHORT;
		case 'V':
			return VOID;
		case 'Z':
			return BOOLEAN;
		case '[':
			return ARRAY;
		}
		return null;
	}
	
	public static String humanReadableDescriptor(String descriptor) {
		Type type = fromDescriptor(descriptor);
		if (type == null) {
			return descriptor;
		}
		switch (type) {
		case OBJECT:
			return descriptor.substring(1, descriptor.indexOf(';')).replace('/', '.');
		case ARRAY:
			return humanReadableDescriptor(descriptor.substring(1)) + "[]";
		default:
			return type.toString();
		}
	}
	
}