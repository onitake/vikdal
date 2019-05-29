package ch.seto.vikdal.dex;

public enum Type {
	VALUE_UNKNOWN(-1, "unknown", '\0'),
	VALUE_BYTE(0x00, "byte", 'B'),
	VALUE_SHORT(0x02, "short", 'S'),
	VALUE_CHAR(0x03, "char", 'C'),
	VALUE_INT(0x04, "int", 'I'),
	VALUE_LONG(0x06, "long", 'J'),
	VALUE_FLOAT(0x10, "float", 'F'),
	VALUE_DOUBLE(0x11, "double", 'D'),
	VALUE_STRING(0x17, "string", 'I'),
	VALUE_TYPE(0x18, "type", 'I'),
	VALUE_FIELD(0x19, "field", 'I'),
	VALUE_METHOD(0x1a, "method", 'I'),
	VALUE_ENUM(0x1b, "enum", 'I'),
	VALUE_ARRAY(0x1c, "array", '['),
	VALUE_ANNOTATION(0x1d, "annotation", 'I'),
	VALUE_NULL(0x1e, "nullvalue", '\0'),
	VALUE_BOOLEAN(0x1f, "boolean", 'Z');
	
	public final int flag;
	public final String description;
	public final char code;
	
	private Type(int f, String d, char c) {
		flag = f;
		description = d;
		code = c;
	}
	
	public static Type fromFlag(int flag) {
		for (Type type : Type.values()) {
			if (flag == type.flag) return type;
		}
		return VALUE_UNKNOWN;
	}
	
	public static Type fromCode(char code) {
		for (Type type : Type.values()) {
			if (type != Type.VALUE_UNKNOWN && code == type.code) return type;
		}
		return VALUE_UNKNOWN;
	}
	
}
