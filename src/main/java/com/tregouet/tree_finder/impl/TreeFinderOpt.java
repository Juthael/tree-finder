package com.tregouet.tree_finder.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.ClassificationTree;
import com.tregouet.tree_finder.utils.SparseGraphConverter;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;

public class TreeFinderOpt<V, E> implements ITreeFinder<V, E> {

	private final DirectedAcyclicGraph<V, E> atomisticRIDAG;
	private final List<V> topoOrderedSet = new ArrayList<>();
	private final SparseGraphConverter<V, E> sparseConverter;
	private final V maximum;	
	private final Set<V> atoms;
	private final SparseIntDirectedGraph sparseAtomisticRIDAG;
	private final int sparseMaximum;
	private final IntArraySet sparseAtoms = new IntArraySet();
	private final List<IntArrayList> sparseTreeRestrictions = new ArrayList<>();
	private int treeIdx = 0;
	
	/*
	 * UNSAFE. The first parameter MUST be an atomistic rooted inverted DAG. 
	 * No transitive reduction must have been operated on it.
	 */
	protected TreeFinderOpt(DirectedAcyclicGraph<V, E> atomisticRIDAG, Set<V> atoms) {
		this.atomisticRIDAG = new DirectedAcyclicGraph<>(null, null, false);
		Graphs.addAllEdges(this.atomisticRIDAG, atomisticRIDAG, atomisticRIDAG.edgeSet());
		TransitiveReduction.INSTANCE.reduce(atomisticRIDAG);
		new TopologicalOrderIterator<>(atomisticRIDAG).forEachRemaining(v -> topoOrderedSet.add(v));
		this.maximum = topoOrderedSet.get(topoOrderedSet.size() - 1);
		this.atoms = atoms;
		sparseMaximum = topoOrderedSet.size() - 1;
		for (V atom : atoms)
			sparseAtoms.add(topoOrderedSet.indexOf(atom));
		sparseConverter = new SparseGraphConverter<>(atomisticRIDAG, true);
		sparseAtomisticRIDAG = sparseConverter.getSparseGraph();
		TreeFinderSparse treeFinderSparse = new TreeFinderSparse(sparseAtomisticRIDAG, sparseMaximum, sparseAtoms);
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
				atomisticRIDAG, sparseConverter.getVertexSet(sparseTreeRestrictions.get(treeIdx)), 
				maximum, atoms, false);
		treeIdx++;
		return nextTree;
	}

}
