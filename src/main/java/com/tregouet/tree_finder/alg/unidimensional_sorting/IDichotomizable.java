package com.tregouet.tree_finder.alg.unidimensional_sorting;

import java.util.Set;

public interface IDichotomizable<D> {

	boolean isComplementary();

	D buildComplementOfThis(Set<D> rebutterMinimalLowerBounds, D supremum);

	D complementThisWith(D absorbed);

	D getComplemented();

}
