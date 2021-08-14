package com.tregouet.tree_finder.error;

public class InvalidSemiLatticeException extends Exception {

	private static final long serialVersionUID = -5464382551763696265L;

	public InvalidSemiLatticeException() {
	}

	public InvalidSemiLatticeException(String message) {
		super(message);
	}

	public InvalidSemiLatticeException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidSemiLatticeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidSemiLatticeException(Throwable cause) {
		super(cause);
	}

}
