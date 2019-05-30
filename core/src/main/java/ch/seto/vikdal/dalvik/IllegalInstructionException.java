package ch.seto.vikdal.dalvik;

public class IllegalInstructionException extends Exception {

	private static final long serialVersionUID = 3697316869238235294L;

	public IllegalInstructionException() {
	}

	public IllegalInstructionException(String message) {
		super(message);
	}

	public IllegalInstructionException(Throwable cause) {
		super(cause);
	}

	public IllegalInstructionException(String message, Throwable cause) {
		super(message, cause);
	}

}
