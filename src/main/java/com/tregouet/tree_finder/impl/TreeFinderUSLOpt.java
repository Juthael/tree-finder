package com.tregouet.tree_finder.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.ClassificationTree;
import com.tregouet.tree_finder.utils.SparseGraphConverter;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;

public class TreeFinderUSLOpt<V, E> implements ITreeFinder<V, E> {

	private final DirectedAcyclicGraph<V, E> upperSemilattice;
	private final List<V> vertexTopoList = new ArrayList<>();
	private final SparseGraphConverter<V, E> sparseConverter;
	private final V root;	
	private final Set<V> minimals;
	private final SparseIntDirectedGraph sparseUpperSemilattice;
	private final int sparseRoot;
	private final IntArraySet sparseMinimals = new IntArraySet();
	private final List<IntArrayList> sparseTreeVertexSets = new ArrayList<>();
	private int treeIdx = 0;
	
	/*
	 * UNSAFE. The first parameter MUST be an upper semilattice
	 */
	public TreeFinderUSLOpt(DirectedAcyclicGraph<V, E> upperSemilattice, boolean reduced, Set<V> minimals) {
		this.upperSemilattice = upperSemilattice;
		if (!reduced)
			TransitiveReduction.INSTANCE.reduce(this.upperSemilattice);
		new TopologicalOrderIterator<>(upperSemilattice).forEachRemaining(v -> vertexTopoList.add(v));
		this.root = vertexTopoList.get(vertexTopoList.size() - 1);
		this.minimals = minimals;
		sparseRoot = vertexTopoList.size() - 1;
		for (V minimal : minimals)
			sparseMinimals.add(vertexTopoList.indexOf(minimal));
		sparseConverter = new SparseGraphConverter<>(upperSemilattice, true);
		sparseUpperSemilattice = sparseConverter.getSparseGraph();
		TreeFinderUSLSparse sparseTF = new TreeFinderUSLSparse(sparseUpperSemilattice, sparseRoot, sparseMinimals);
		sparseTreeVertexSets.addAll(sparseTF.getSparseTreeVertexSets());
	}

	public int getNbOfTrees() {
		return sparseTreeVertexSets.size();
	}

	@Override
	public boolean hasNext() {
		return treeIdx < sparseTreeVertexSets.size() - 1;
	}

	@Override
	public ClassificationTree<V, E> next() {
		return new ClassificationTree<V, E>(
				upperSemilattice, sparseConverter.getVertexSet(sparseTreeVertexSets.get(treeIdx++)), 
				root, minimals, false);
	}

}
