package com.tregouet.tree_finder.algo.unidimensional_sorting;

import java.util.Set;

public interface IDichotomizable<D> {
	
	boolean isComplementary(); 
	
	D buildComplementOfThis(Set<D> rebutterMinimalLowerBounds);
	
	D complementThisWith(D absorbed);
	
	D getComplemented();
	
	//HERE
	int getID();
	//HERE

}
