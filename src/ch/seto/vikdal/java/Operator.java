package ch.seto.vikdal.java;

import java.util.EnumSet;

/**
 * Abstract representation of a Java operator.
 * All possible combinations are available, but you should only implement those that
 * the operation supports. All other operations should throw {@link UnsupportedOperationException}.
 * {@link ch.seto.vikdal.operators.AbstractOperator} does that by default for all combinations.
 * You should also override {@link #toString()} to return the source code representation of the
 * operator, as well as {@link #supportedTypes()} to return all supported operators.
 */
public interface Operator {
	
	public enum Type {
		UNARY_LOGICAL,
		UNARY_MATH_INT,
		UNARY_MATH_LONG,
		UNARY_MATH_FLOAT,
		UNARY_MATH_DOUBLE,
		BINARY_LOGICAL,
		BINARY_RELATIONAL_INT,
		BINARY_RELATIONAL_LONG,
		BINARY_RELATIONAL_FLOAT,
		BINARY_RELATIONAL_DOUBLE,
		BINARY_RELATIONAL_OBJECT,
		BINARY_MATH_INT,
		BINARY_MATH_LONG,
		BINARY_MATH_FLOAT,
		BINARY_MATH_DOUBLE,
		TERNARY_BOOL,
		TERNARY_INT,
		TERNARY_LONG,
		TERNARY_FLOAT,
		TERNARY_DOUBLE,
		TERNARY_OBJECT,
	};
	
	public EnumSet<Type> supportedTypes();
	
	public boolean unaryLogical(boolean a);
	
	public int unaryMath(int a);
	public long unaryMath(long a);
	public float unaryMath(float a);
	public double unaryMath(double a);

	public boolean binaryLogical(boolean a, boolean b);

	public boolean binaryRelational(int a, int b);
	public boolean binaryRelational(long a, long b);
	public boolean binaryRelational(float a, float b);
	public boolean binaryRelational(double a, double b);
	public boolean binaryRelational(Object a, Object b);

	public int binaryMath(int a, int b);
	public long binaryMath(long a, long b);
	public float binaryMath(float a, float b);
	public double binaryMath(double a, double b);

	public boolean ternary(boolean a, boolean b, boolean c);
	public int ternary(boolean a, int b, int c);
	public long ternary(boolean a, long b, long c);
	public float ternary(boolean a, float b, float c);
	public double ternary(boolean a, double b, double c);
	public Object ternary(boolean a, Object b, Object c);

}
