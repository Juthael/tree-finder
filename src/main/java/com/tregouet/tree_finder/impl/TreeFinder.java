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
	private ITreeFinder<V, E> treeFinderInUSL;
	private final boolean bruteForce;
	
	
	public TreeFinder(DirectedAcyclicGraph<V, E> rootedInvertedDAG) throws InvalidRootedInvertedDAGException {
		if (!StructureInspector.isARootedInvertedDirectedAcyclicGraph(rootedInvertedDAG))
			throw new InvalidRootedInvertedDAGException();
		bruteForce = false;
		TransitiveReduction.INSTANCE.reduce(rootedInvertedDAG);
		for (V element : rootedInvertedDAG.vertexSet()) {
			if (rootedInvertedDAG.inDegreeOf(element) == 0)
				minimals.add(element);
		}
		uslFinder = new UpperSemilatticeFinder<>(rootedInvertedDAG, minimals);
		treeFinderInUSL = new TreeFinderOpt<V, E>(uslFinder.next(), minimals);
	}
	
	public TreeFinder(DirectedAcyclicGraph<V, E> rootedInvertedDAG, boolean bruteForce) throws InvalidRootedInvertedDAGException {
		if (!StructureInspector.isARootedInvertedDirectedAcyclicGraph(rootedInvertedDAG))
			throw new InvalidRootedInvertedDAGException();
		this.bruteForce = bruteForce;
		TransitiveReduction.INSTANCE.reduce(rootedInvertedDAG);
		for (V element : rootedInvertedDAG.vertexSet()) {
			if (rootedInvertedDAG.inDegreeOf(element) == 0)
				minimals.add(element);
		}
		uslFinder = new UpperSemilatticeFinder<>(rootedInvertedDAG, minimals);
		if (bruteForce)
			treeFinderInUSL = new TreeFinderBruteForce<>(uslFinder.next(), minimals);
		treeFinderInUSL = new TreeFinderOpt<V, E>(uslFinder.next(), minimals);
	}	

	@Override
	public boolean hasNext() {
		return (uslFinder.hasNext() || treeFinderInUSL.hasNext());
	}

	@Override
	public ClassificationTree<V, E> next() {
		if (!treeFinderInUSL.hasNext()) {
			if (bruteForce)
				treeFinderInUSL = new TreeFinderBruteForce<>(uslFinder.next(), minimals);
			else treeFinderInUSL = new TreeFinderOpt<V, E>(uslFinder.next(), minimals);
		}	
		return treeFinderInUSL.next();
	}

}
