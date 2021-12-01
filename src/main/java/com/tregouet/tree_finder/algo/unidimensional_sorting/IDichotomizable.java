package com.tregouet.tree_finder.algo.unidimensional_sorting;

import java.util.Set;

public interface IDichotomizable<D> {
	
	boolean isRebutter(); 
	
	D buildRebutterOfThis(Set<D> rebutterMinimalLowerBounds);
	
	D rebutThisWith(D absorbed);
	
	void setAsRebutterOf(D rebutted);
	
	D getRebutted();

}
