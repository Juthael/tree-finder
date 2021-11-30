package com.tregouet.tree_finder.algo.unidimensional_sorting;

import java.util.List;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.Tree;

public interface IUnidimensionalSorter<D extends IDichotomizable<D>, E> extends ITreeFinder<D, E> {
	
	List<Tree<D, E>> getSortingTrees();

}
