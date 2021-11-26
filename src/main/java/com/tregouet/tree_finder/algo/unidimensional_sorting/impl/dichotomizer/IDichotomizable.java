package com.tregouet.tree_finder.algo.unidimensional_sorting.impl.dichotomizer;

public interface IDichotomizable<D> {
	
	D rebut(); 
	
	D rebutWith(D absorbed);
	
	boolean isRebutter();

}
