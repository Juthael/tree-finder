package com.tregouet.tree_finder.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.ClassificationTree;
import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.utils.SparseGraphConverter;
import com.tregouet.tree_finder.utils.StructureInspector;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;

public class TreeFinderOpt<V, E> implements ITreeFinder<V, E> {

	private final DirectedAcyclicGraph<V, E> rootedInverted;
	private final List<V> topoOrderedSet = new ArrayList<>();
	private final SparseGraphConverter<V, E> sparseConverter;
	private final V maximum;	
	private final Set<V> atoms = new HashSet<>();
	private final SparseIntDirectedGraph sparse;
	private final int sparseMaximum;
	private final IntArraySet sparseAtoms = new IntArraySet();
	private final List<IntArrayList> sparseTreeRestrictions = new ArrayList<>();
	private int treeIdx = 0;
	
	/*
	 * The first parameter MUST be a rooted inverted DAG. 
	 * No transitive reduction must have been operated on it.
	 */
	public TreeFinderOpt(DirectedAcyclicGraph<V, E> rootedInverted) throws InvalidInputException {
		if (!StructureInspector.isARootedInvertedDirectedAcyclicGraph(rootedInverted))
			throw new InvalidInputException("The parameter is not a rooted inverted directed acyclic graph.");
		if (!StructureInspector.isTransitive(rootedInverted))
			throw new InvalidInputException("The parameter graph is not transitive.");
		this.rootedInverted = new DirectedAcyclicGraph<>(null, null, false);
		Graphs.addAllEdges(this.rootedInverted, rootedInverted, rootedInverted.edgeSet());
		TransitiveReduction.INSTANCE.reduce(rootedInverted);
		new TopologicalOrderIterator<>(this.rootedInverted).forEachRemaining(v -> topoOrderedSet.add(v));
		this.maximum = topoOrderedSet.get(topoOrderedSet.size() - 1);
		for (V element : topoOrderedSet) {
			if (rootedInverted.inDegreeOf(element) == 0)
				atoms.add(element);
		}
		sparseMaximum = topoOrderedSet.size() - 1;
		for (V atom : atoms)
			sparseAtoms.add(topoOrderedSet.indexOf(atom));
		sparseConverter = new SparseGraphConverter<>(rootedInverted, true);
		sparse = sparseConverter.getSparseGraph();
		TreeFinderSparse treeFinderSparse = new TreeFinderSparse(sparse, sparseMaximum, sparseAtoms);
		sparseTreeRestrictions.addAll(treeFinderSparse.getSparseTreeVertexSets());
	}
	
	//UNSAFE
	public TreeFinderOpt(DirectedAcyclicGraph<V, E> rootedInverted, boolean unsafeMode) {
		this.rootedInverted = new DirectedAcyclicGraph<>(null, null, false);
		Graphs.addAllEdges(this.rootedInverted, rootedInverted, rootedInverted.edgeSet());
		TransitiveReduction.INSTANCE.reduce(rootedInverted);
		new TopologicalOrderIterator<>(this.rootedInverted).forEachRemaining(v -> topoOrderedSet.add(v));
		this.maximum = topoOrderedSet.get(topoOrderedSet.size() - 1);
		for (V element : topoOrderedSet) {
			if (rootedInverted.inDegreeOf(element) == 0)
				atoms.add(element);
		}
		sparseMaximum = topoOrderedSet.size() - 1;
		for (V atom : atoms)
			sparseAtoms.add(topoOrderedSet.indexOf(atom));
		sparseConverter = new SparseGraphConverter<>(rootedInverted, true);
		sparse = sparseConverter.getSparseGraph();
		TreeFinderSparse treeFinderSparse = new TreeFinderSparse(sparse, sparseMaximum, sparseAtoms);
		sparseTreeRestrictions.addAll(treeFinderSparse.getSparseTreeVertexSets());
	}	

	public int getNbOfTrees() {
		return sparseTreeRestrictions.size();
	}

	@Override
	public boolean hasNext() {
		return treeIdx < sparseTreeRestrictions.size();
	}

	@Override
	public ClassificationTree<V, E> next() {
		ClassificationTree<V, E> nextTree = new ClassificationTree<V, E>(
				rootedInverted, sparseConverter.getSet(sparseTreeRestrictions.get(treeIdx)), 
				maximum, atoms, false);
		treeIdx++;
		return nextTree;
	}

}
