package com.tregouet.tree_finder.impl;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.ClassificationTree;
import com.tregouet.tree_finder.utils.SparseGraphConverter;
import com.tregouet.tree_finder.utils.UpperSemilatticeFinder;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;

public class TreeFinderOpt<V, E> implements ITreeFinder<V, E> {

	DirectedAcyclicGraph<V, E> input;
	DirectedAcyclicGraph<Integer, Integer> sparseInput;
	private final SparseGraphConverter<V, E> sparseConverter;
	private final V root;
	private final int sparseRoot;
	private final List<V> minimals;
	private final IntArraySet sparseMinimals = new IntArraySet();
	private final List<SparseIntDirectedGraph> sparseUpperSemilattices = new ArrayList<>();
	private final List<IntArrayList> sparseTreeVertexSets = new ArrayList<>();
	private int treeIdx = 0;
	
	public TreeFinderOpt(DirectedAcyclicGraph<V, E> rootedInvertedDAG, boolean relationIsTransitive, 
			boolean isAnUpperSemilattice) {
		if (!relationIsTransitive) 
			TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(rootedInvertedDAG);
		input = rootedInvertedDAG;
		sparseConverter = new SparseGraphConverter<>(input, true);
		SparseIntDirectedGraph sparseInput = sparseConverter.getSparseGraph();
		this.sparseInput = sparseConverter.asSparseDAG();
		int sRoot = -1;
		for (Integer vertex : sparseInput.vertexSet()) {
			if (sparseInput.inDegreeOf(vertex) == 0) {
				sparseMinimals.add((int) vertex);
			}
				
			else if (sparseInput.outDegreeOf(vertex) == 0)
				sRoot = vertex;
		}
		sparseRoot = sRoot;
		root = sparseConverter.getVertex(sRoot);
		minimals = sparseConverter.getVertexSet(sparseMinimals);
		if (isAnUpperSemilattice) {
			sparseUpperSemilattices.add(sparseInput);
		}
		else {
			UpperSemilatticeFinder upperSemilatticeFinder = new UpperSemilatticeFinder(sparseInput, sparseMinimals);
			upperSemilatticeFinder.forEachRemaining(l -> sparseUpperSemilattices.add(l));
		}
		for (SparseIntDirectedGraph uSL : sparseUpperSemilattices) {
			TreeFinderSparse sparseTF = new TreeFinderSparse(uSL, sparseRoot, sparseMinimals);
			sparseTreeVertexSets.addAll(sparseTF.getSparseTreeVertexSets());
		}
	}

	@Override
	public int getNbOfTrees() {
		return sparseTreeVertexSets.size();
	}

	@Override
	public boolean hasNext() {
		return treeIdx < sparseTreeVertexSets.size() - 1;
	}

	@Override
	public ClassificationTree<V, E> next() {
		return new ClassificationTree<V, E>(input, sparseConverter.getVertexSet(sparseTreeVertexSets.get(treeIdx++)), 
				root, minimals, false);
	}

}
