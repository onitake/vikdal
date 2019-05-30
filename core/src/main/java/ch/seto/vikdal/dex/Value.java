package ch.seto.vikdal.dex;

import java.util.Map;
import java.util.Map.Entry;

public class Value {

	protected final Type type;
	protected final Object data;
	
	Value(Type t, Object d) {
		type = t;
		data = d;
	}
	
	Value(Type t) {
		type = t;
		switch (type) {
		case VALUE_BYTE:
			data = (byte) 0;
			break;
		case VALUE_SHORT:
			data = (short) 0;
			break;
		case VALUE_CHAR:
			data = (char) 0;
			break;
		case VALUE_INT:
			data = 0;
			break;
		case VALUE_LONG:
			data = 0L;
			break;
		case VALUE_FLOAT:
			data = 0.0f;
			break;
		case VALUE_DOUBLE:
			data = 0.0;
			break;
		case VALUE_BOOLEAN:
			data = false;
			break;
		default:
			data = null;
		}
	}
	
	public Type getType() {
		return type;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Value other = (Value) obj;
		if (type != other.type) return false;
		if (data == null) {
			if (other.data != null) return false;
		} else if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return type.description + "(" + String.valueOf(data) + ")";
	}
	
	public Object getObjectValue() {
		return data;
	}
	
	public boolean getBooleanValue() {
		if (type == Type.VALUE_BOOLEAN) {
			return (Boolean) data;
		}
		throw new IllegalArgumentException("Underlying type is not boolean");
	}

	public byte getByteValue() {
		if (type == Type.VALUE_BYTE) {
			return (Byte) data;
		}
		throw new IllegalArgumentException("Underlying type is not byte");
	}

	public short getShortValue() {
		if (type == Type.VALUE_SHORT) {
			return (Short) data;
		}
		throw new IllegalArgumentException("Underlying type is not short");
	}

	public char getCharValue() {
		if (type == Type.VALUE_CHAR) {
			return (Character) data;
		}
		throw new IllegalArgumentException("Underlying type is not char");
	}

	public int getIntValue() {
		if (type == Type.VALUE_INT || type == Type.VALUE_STRING || type == Type.VALUE_TYPE || type == Type.VALUE_FIELD || type == Type.VALUE_METHOD || type == Type.VALUE_ENUM) {
			return (Integer) data;
		}
		throw new IllegalArgumentException("Underlying type is not compatbile with int");
	}

	public long getLongValue() {
		if (type == Type.VALUE_LONG) {
			return (Long) data;
		}
		throw new IllegalArgumentException("Underlying type is not long");
	}

	public float getFloatValue() {
		if (type == Type.VALUE_FLOAT) {
			return (Float) data;
		}
		throw new IllegalArgumentException("Underlying type is not float");
	}

	public double getDoubleValue() {
		if (type == Type.VALUE_DOUBLE) {
			return (Double) data;
		}
		throw new IllegalArgumentException("Underlying type is not double");
	}

	public Value[] getArrayValue() {
		if (type == Type.VALUE_ARRAY) {
			return (Value[]) data;
		}
		throw new IllegalArgumentException("Underlying type is not array");
	}

	@SuppressWarnings("unchecked")
	public Entry<Long, Map<Long, Value>> getAnnotationValue() {
		if (type == Type.VALUE_ANNOTATION) {
			return (Entry<Long, Map<Long, Value>>) data;
		}
		throw new IllegalArgumentException("Underlying type is not annotation");
	}

}
