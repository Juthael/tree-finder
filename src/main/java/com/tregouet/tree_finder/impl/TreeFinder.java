package com.tregouet.tree_finder.impl;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.ClassificationTree;
import com.tregouet.tree_finder.error.InvalidRootedInvertedDAGException;
import com.tregouet.tree_finder.utils.StructureInspector;
import com.tregouet.tree_finder.utils.UpperSemilatticeFinder;

public class TreeFinder<V, E> implements ITreeFinder<V, E> {

	private final Set<V> minimals = new HashSet<>();
	private final UpperSemilatticeFinder<V, E> uslFinder;
	private TreeFinderUSLOpt<V, E> treeFinder;
	
	public TreeFinder(DirectedAcyclicGraph<V, E> rootedInvertedDAG) throws InvalidRootedInvertedDAGException {
		if (!StructureInspector.isARootedInvertedDirectedAcyclicGraph(rootedInvertedDAG))
			throw new InvalidRootedInvertedDAGException();
		TransitiveReduction.INSTANCE.reduce(rootedInvertedDAG);
		for (V element : rootedInvertedDAG.vertexSet()) {
			if (rootedInvertedDAG.inDegreeOf(element) == 0)
				minimals.add(element);
		}
		uslFinder = new UpperSemilatticeFinder<>(rootedInvertedDAG, minimals);
		treeFinder = new TreeFinderUSLOpt<V, E>(uslFinder.next(), false, minimals);
	}

	@Override
	public boolean hasNext() {
		return uslFinder.hasNext() || treeFinder.hasNext();
	}

	@Override
	public ClassificationTree<V, E> next() {
		if (!treeFinder.hasNext())
			treeFinder = new TreeFinderUSLOpt<V, E>(uslFinder.next(), false, minimals);
		return treeFinder.next();
	}

}
