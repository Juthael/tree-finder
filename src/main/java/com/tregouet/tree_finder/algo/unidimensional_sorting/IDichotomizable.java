package com.tregouet.tree_finder.algo.unidimensional_sorting;

public interface IDichotomizable<D> {
	
	boolean isRebutter(); 
	
	D rebut();
	
	D rebutWith(D absorbed);

}
