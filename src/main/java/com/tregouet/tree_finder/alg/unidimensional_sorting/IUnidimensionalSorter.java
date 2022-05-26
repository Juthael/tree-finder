package com.tregouet.tree_finder.alg.unidimensional_sorting;

import java.util.Set;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.InvertedTree;

public interface IUnidimensionalSorter<D extends IDichotomizable<D>, E> extends ITreeFinder<D, E> {

	Set<InvertedTree<D, E>> getSortingTrees();

}
