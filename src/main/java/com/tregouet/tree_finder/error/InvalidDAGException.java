package com.tregouet.tree_finder.error;

public class InvalidDAGException extends Exception {

	private static final long serialVersionUID = -5464382551763696265L;

	public InvalidDAGException() {
	}

	public InvalidDAGException(String message) {
		super(message);
	}

	public InvalidDAGException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidDAGException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidDAGException(Throwable cause) {
		super(cause);
	}

}
