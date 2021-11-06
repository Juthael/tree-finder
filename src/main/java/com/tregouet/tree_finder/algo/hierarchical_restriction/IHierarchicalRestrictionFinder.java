package com.tregouet.tree_finder.algo.hierarchical_restriction;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.Tree;

public interface IHierarchicalRestrictionFinder<V, E> extends ITreeFinder<V, E> {

	Tree<V, E> nextTransitiveReduction();

}
