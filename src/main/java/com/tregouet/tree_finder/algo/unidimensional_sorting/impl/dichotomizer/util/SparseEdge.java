package com.tregouet.tree_finder.algo.unidimensional_sorting.impl.dichotomizer.util;

public class SparseEdge {

	private final int source;
	private final int target;
	
	public SparseEdge(int source, int target) {
		this.source = source;
		this.target = target;
	}

	public int getSource() {
		return source;
	}

	public int getTarget() {
		return target;
	}

}
