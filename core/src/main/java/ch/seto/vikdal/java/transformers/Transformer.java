package ch.seto.vikdal.java.transformers;

/**
 * Basic interface for a data transformer.
 * @param <I> the data input type
 * @param <O> the data output type
 */
public interface Transformer <I, O> {
	/**
	 * Transforms the input.
	 * Does not modify the input object or its composites.
	 * @param input a data object
	 * @return a new data object
	 */
	public O transform(I input);
}
