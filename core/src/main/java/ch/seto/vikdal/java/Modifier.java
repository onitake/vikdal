package ch.seto.vikdal.java;

public enum Modifier {
	PUBLIC("public", java.lang.reflect.Modifier.PUBLIC),
	PRIVATE("private", java.lang.reflect.Modifier.PRIVATE),
	PROTECTED("protected", java.lang.reflect.Modifier.PROTECTED),
	STATIC("static", java.lang.reflect.Modifier.STATIC),
	FINAL("final", java.lang.reflect.Modifier.FINAL),
	SYNCHRONIZED("synchronized", java.lang.reflect.Modifier.SYNCHRONIZED),
	VOLATILE("volatile", java.lang.reflect.Modifier.VOLATILE),
	TRANSIENT("transient", java.lang.reflect.Modifier.TRANSIENT),
	NATIVE("native", java.lang.reflect.Modifier.NATIVE),
	ABSTRACT("abstract", java.lang.reflect.Modifier.ABSTRACT);
	
	/**
	 * A human-readable description of this modifier.
	 */
	public final String description;
	/**
	 * The corresponding {@link java.lang.reflect.Modifier} modifier.
	 */
	public final int javaModifier;
	
	private Modifier(String desc, int jmodifier) {
		description = desc;
		javaModifier = jmodifier;
	}
	
	@Override
	public String toString() {
		return description;
	}
}
