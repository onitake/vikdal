package ch.seto.vikdal.java.transformers;

public class ProgramVerificationException extends RuntimeException {

	private static final long serialVersionUID = 2L;

	public ProgramVerificationException() {
		super();
	}

	public ProgramVerificationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProgramVerificationException(String message) {
		super(message);
	}

	public ProgramVerificationException(Throwable cause) {
		super(cause);
	}

}
