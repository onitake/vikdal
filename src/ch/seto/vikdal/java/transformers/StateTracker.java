package ch.seto.vikdal.java.transformers;

import java.util.*;

import ch.seto.vikdal.java.Type;

/**
 * Dalvik Java VM state tracker.
 * Allows tracking of the types and names of Dalvik's register bank.
 */
public class StateTracker implements Cloneable {
	
	private class Register {
		public final int index;
		public Type type;
		private String name;
		public Register(int i) {
			index = i;
			type = Type.INT;
			setDefaultName();
			registers.put(i, this);
		}
		public void setName(String n) {
			if (name != null && !name.equals(n)) {
				names.remove(name);
			}
			name = n;
			names.add(name);
		}
		public String getName() {
			return name;
		}
		public void setDefaultName() {
			setName("v" + index);
		}
	}
	
	private static int DEFAULT_LOW = 0;
	private static int DEFAULT_HIGH = 0xffff;
	
	private Set<String> names;
	private Map<Integer, Register> registers;
	private int registersLow;
	private int registersHigh;
	
	/**
	 * Creates a new Dalvik VM state tracker to simulate code flow through a method.
	 * Values are not tracked.
	 */
	public StateTracker() {
		names = new HashSet<String>();
		registers = new TreeMap<Integer, Register>();
		registersLow = DEFAULT_LOW;
		registersHigh = DEFAULT_HIGH;
	}
	
	private Register getRegister(int register) {
		if (register < registersLow || register > registersHigh) {
			throw new IndexOutOfBoundsException("Register value out of range");
		}
		Register reg = registers.get(register);
		if (reg == null) {
			reg = new Register(register);
		}
		return reg;
	}
	
	/**
	 * Sets the valid range for register numbers.
	 * @param low the lower limit (default DEFAULT_LOW, 0)
	 * @param high the upper limit (default DEFAULT_HIGH, 65535)
	 */
	public void setRegisterRange(int low, int high) {
		registersLow = low;
		registersHigh = high;
	}
	
	/**
	 * Gets the lower register boundary
	 */
	public int getLowerRegisterBoundary() {
		return registersLow;
	}
	
	/**
	 * Gets the upper register boundary
	 */
	public int getUpperRegisterBoundary() {
		return registersHigh;
	}
	
	/**
	 * Sets the type of a register. Use the pseudo enums {@link Type#LONG} and {@link Type#DOUBLE} to
	 * assign the type of a register pair (register and register+1).
	 * @param register the register to assign to
	 * @param type the type of that register
	 * @throws IndexOutOfBoundsException if one of the assigned registers is out of range
	 */
	public void setRegisterType(int register, Type type) {
		if (getRegister(register).type != type) {
			getRegister(register).setDefaultName();
		}
		if (type == Type.LONG) {
			getRegister(register).type = Type.LONG_LOW;
			getRegister(register + 1).type = Type.LONG_HIGH;
		} else if (type == Type.DOUBLE) {
			getRegister(register).type = Type.DOUBLE_LOW;
			getRegister(register + 1).type = Type.DOUBLE_HIGH;
		} else {
			getRegister(register).type = type;
		}
	}
	
	/**
	 * Sets the name of a register or register pair.
	 * If the register has type {@link Type#LONG_LOW} or {@link Type#DOUBLE_LOW} and the following
	 * register has {@link Type#LONG_HIGH} or {@link Type#DOUBLE_HIGH}, then the same name will
	 * be assigned to that register, and vice-versa.
	 * TODO Currently disabled due to buggyness of naming algorithm
	 * @param register the register to assign to
	 * @param name the name of that register
	 * @throws IndexOutOfBoundsException if one of the assigned registers is out of range
	 */
	//public void setRegisterName(int register, String name) { }
	public void setRegisterName(int register, String name) {
		// TODO match name against /v([0-9]+)/ and verify that the register number equals \1
		/*Register reg = getRegister(register);
		if (reg.type == Type.LONG_LOW || reg.type == Type.DOUBLE_LOW) {
			Register regh = getRegister(register + 1);
			if (regh.type == Type.LONG_HIGH || reg.type == Type.DOUBLE_HIGH) {
				reg.setName(name);
				regh.setName(name);
			} else {
				reg.setName(name);
			}
		} else if (reg.type == Type.LONG_HIGH || reg.type == Type.DOUBLE_HIGH) {
			Register regl = getRegister(register - 1);
			if (regl.type == Type.LONG_LOW || reg.type == Type.DOUBLE_LOW) {
				regl.setName(name);
				reg.setName(name);
			} else {
				reg.setName(name);
			}
		} else {
			reg.setName(name);
		}*/
	}
	
	/**
	 * Gets the current type of a register. No special handling for register pairs is provided.
	 * @param register the register to get the type from
	 * @return the register type
	 * @throws IndexOutOfBoundsException if the register is out of range
	 */
	public Type getRegisterType(int register) {
		return getRegister(register).type;
	}

	/**
	 * Gets the current name of a register. No special handling for register pairs is provided.
	 * @param register the register to get the name from
	 * @return the register name
	 * @throws IndexOutOfBoundsException if the register is out of range
	 */
	public String getRegisterName(int register) {
		return getRegister(register).getName();
	}

	/**
	 * Assigns an automatic name derived from type to the register. The name is unique for this tracker,
	 * but may still collide with a type. If no non-colliding name can be found, the register keeps its name.
	 * @param register the register to assign to
	 * @param type a type name
	 * @return true, if a name was successfully assigned, false if no unoccupied name could be found
	 */
	public boolean setAutomaticName(int register, String type) {
		String name = null;
		for (int i = 0; i < registersHigh && name == null; i++) {
			String ident = identifierForType(type, i);
			if (!names.contains(ident)) {
				setRegisterName(register, ident);
				name = ident;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns an identifier that is similar to type. The first character is turned into lower case and the index is appended,
	 * unless index is less than or equal 0. If index is 0 and type and the resulting string are identical, the 0 is appended
	 * nonetheless.
	 * @param type a java type descriptor
	 * @param index a counter
	 * @return an identifier
	 */
	public static String identifierForType(String type, int index) {
		// if none of these characters are found, begin will be -1 + 1 = 0
		int begin = Math.max(Math.max(type.lastIndexOf('/'), type.lastIndexOf('[')), type.lastIndexOf('$')) + 1;
		int end = type.lastIndexOf(';');
		if (end == -1) end = type.length();
		String stype = type.substring(begin, end);
		char first = stype.charAt(0);
		StringBuilder ret = new StringBuilder();
		ret.append(Character.toLowerCase(first));
		ret.append(stype.substring(1));
		if (type.charAt(0) == '[') {
			ret.append("Array");
		}
		if (index < 0) {
			index = 0;
		}
		if (index > 0 || ret.toString().equals(stype)) {
			ret.append(index);
		}
		// TODO verify that this is a valid java identifier, remove invalid characters
		return ret.toString();
	}
	
	@Override
	public Object clone() {
		StateTracker ret = new StateTracker();
		ret.registersLow = registersLow;
		ret.registersHigh = registersHigh;
		for (Map.Entry<Integer, Register> reg : registers.entrySet()) {
			ret.setRegisterName(reg.getKey(), reg.getValue().name);
			ret.setRegisterType(reg.getKey(), reg.getValue().type);
		}
		return ret;
	}
}
