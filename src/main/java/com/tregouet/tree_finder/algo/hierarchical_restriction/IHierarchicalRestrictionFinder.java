package com.tregouet.tree_finder.algo.hierarchical_restriction;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.InvertedTree;

public interface IHierarchicalRestrictionFinder<V, E> extends ITreeFinder<V, E> {

	InvertedTree<V, E> nextTransitiveReduction();

}
