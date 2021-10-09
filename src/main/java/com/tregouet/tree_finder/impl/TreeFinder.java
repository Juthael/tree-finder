package com.tregouet.tree_finder.impl;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.ClassificationTree;
import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.utils.StructureInspector;
import com.tregouet.tree_finder.utils.MaxAtomisticRestrictionFinder;

public class TreeFinder<V, E> implements ITreeFinder<V, E> {

	private final Set<V> atoms = new HashSet<>();
	private final MaxAtomisticRestrictionFinder<V, E> atomisticRestrictionFinder;
	private ITreeFinder<V, E> treeFinder;
	
	
	public TreeFinder(DirectedAcyclicGraph<V, E> rootedInvertedDAG) throws InvalidInputException {
		if (!StructureInspector.isARootedInvertedDirectedAcyclicGraph(rootedInvertedDAG))
			throw new InvalidInputException();
		TransitiveReduction.INSTANCE.reduce(rootedInvertedDAG);
		for (V element : rootedInvertedDAG.vertexSet()) {
			if (rootedInvertedDAG.inDegreeOf(element) == 0)
				atoms.add(element);
		}
		atomisticRestrictionFinder = new MaxAtomisticRestrictionFinder<>(rootedInvertedDAG, atoms);
		treeFinder = new TreeFinderOpt<V, E>(atomisticRestrictionFinder.next(), atoms);
	}

	@Override
	public boolean hasNext() {
		return (atomisticRestrictionFinder.hasNext() || treeFinder.hasNext());
	}

	@Override
	public ClassificationTree<V, E> next() {
		if (!treeFinder.hasNext())
			treeFinder = new TreeFinderOpt<V, E>(atomisticRestrictionFinder.next(), atoms);
		return treeFinder.next();
	}

}
