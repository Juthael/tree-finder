package com.tregouet.tree_finder.error;

public class InvalidRootedInvertedDAGException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1219253130228493953L;

	public InvalidRootedInvertedDAGException() {
	}

	public InvalidRootedInvertedDAGException(String message) {
		super(message);
	}

	public InvalidRootedInvertedDAGException(Throwable cause) {
		super(cause);
	}

	public InvalidRootedInvertedDAGException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidRootedInvertedDAGException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
