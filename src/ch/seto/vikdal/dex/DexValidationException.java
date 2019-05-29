package ch.seto.vikdal.dex;

public class DexValidationException extends RuntimeException {

	private static final long serialVersionUID = -4863180772527964796L;

	public DexValidationException() {
		super();
	}

	public DexValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DexValidationException(String message) {
		super(message);
	}

	public DexValidationException(Throwable cause) {
		super(cause);
	}

}
