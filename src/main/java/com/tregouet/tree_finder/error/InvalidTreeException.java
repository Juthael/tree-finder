package com.tregouet.tree_finder.error;

public class InvalidTreeException extends Exception {

	private static final long serialVersionUID = 6365877261011308617L;

	public InvalidTreeException() {
	}

	public InvalidTreeException(String message) {
		super(message);
	}

	public InvalidTreeException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidTreeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidTreeException(Throwable cause) {
		super(cause);
	}

}
