package net.sf.librefundraiser.io;

public class MoneyParsingException extends RuntimeException {

	private static final long serialVersionUID = -7558973540701529291L;

	public MoneyParsingException() {
		super();
	}

	public MoneyParsingException(String message) {
		super(message);
		
	}

	public MoneyParsingException(Throwable cause) {
		super(cause);
		
	}

	public MoneyParsingException(String message, Throwable cause) {
		super(message, cause);
	}

}
