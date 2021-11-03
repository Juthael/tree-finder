package com.tregouet.tree_finder.algo.unidimensional_sorting;

import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.Tree;

public interface IUnidimensionalSorter<V, E extends DefaultEdge> extends ITreeFinder<V, E> {
	
	Set<Tree<V, E>> getSortingTrees();

}
