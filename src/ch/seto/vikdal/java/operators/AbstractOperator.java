package ch.seto.vikdal.java.operators;

import java.util.EnumSet;

import ch.seto.vikdal.java.Operator;

public abstract class AbstractOperator implements Operator {

	@Override
	public EnumSet<Type> supportedTypes() {
		return EnumSet.noneOf(Operator.Type.class);
	}

	@Override
	public boolean unaryLogical(boolean a) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public int unaryMath(int a) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public long unaryMath(long a) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public float unaryMath(float a) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public double unaryMath(double a) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public boolean binaryLogical(boolean a, boolean b) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public boolean binaryRelational(int a, int b) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public boolean binaryRelational(long a, long b) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public boolean binaryRelational(float a, float b) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public boolean binaryRelational(double a, double b) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public boolean binaryRelational(Object a, Object b) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public int binaryMath(int a, int b) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public long binaryMath(long a, long b) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public float binaryMath(float a, float b) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public double binaryMath(double a, double b) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public boolean ternary(boolean a, boolean b, boolean c) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public int ternary(boolean a, int b, int c) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public long ternary(boolean a, long b, long c) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public float ternary(boolean a, float b, float c) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public double ternary(boolean a, double b, double c) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

	@Override
	public Object ternary(boolean a, Object b, Object c) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support this operator type");
	}

}
