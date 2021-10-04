package com.tregouet.tree_finder.error;

public class InvalidSemilatticeException extends Exception {

	private static final long serialVersionUID = -5464382551763696265L;

	public InvalidSemilatticeException() {
	}

	public InvalidSemilatticeException(String message) {
		super(message);
	}

	public InvalidSemilatticeException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidSemilatticeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidSemilatticeException(Throwable cause) {
		super(cause);
	}

}
