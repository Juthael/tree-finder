package com.tregouet.tree_finder.algo.unidimensional_sorting;

import java.util.Collection;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.Tree;

public interface IUnidimensionalSorter<V, E> extends ITreeFinder<V, E> {
	
	Collection<Tree<V, E>> getSortingTrees();

}
