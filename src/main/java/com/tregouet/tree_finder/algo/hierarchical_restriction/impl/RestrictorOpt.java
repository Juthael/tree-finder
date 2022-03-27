package com.tregouet.tree_finder.algo.hierarchical_restriction.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.algo.hierarchical_restriction.IHierarchicalRestrictionFinder;
import com.tregouet.tree_finder.algo.hierarchical_restriction.utils.SparseGraphConverter;
import com.tregouet.tree_finder.data.InvertedTree;
import com.tregouet.tree_finder.utils.StructureInspector;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public class RestrictorOpt<V, E> implements IHierarchicalRestrictionFinder<V, E> {

	private final DirectedAcyclicGraph<V, E> rootedInverted;
	private final List<V> topoOrderedSet = new ArrayList<>();
	private final SparseGraphConverter<V, E> sparseConverter;
	private final V maximum;	
	private final Set<V> atoms = new HashSet<>();
	private final SparseIntDirectedGraph sparse;
	private final List<IntArrayList> sparseTreeRestrictions = new ArrayList<>();
	private int treeIdx = 0;
	
	/*
	 * The first parameter MUST be a rooted inverted DAG. 
	 * No transitive reduction must have been operated on it.
	 */
	public RestrictorOpt(DirectedAcyclicGraph<V, E> rootedInverted) throws IOException {
		if (!StructureInspector.isARootedInvertedDirectedAcyclicGraph(rootedInverted))
			throw new IOException("The parameter is not a rooted inverted directed acyclic graph.");
		if (!StructureInspector.isTransitive(rootedInverted))
			throw new IOException("The parameter graph is not transitive.");
		this.rootedInverted = new DirectedAcyclicGraph<>(null, null, false);
		Graphs.addAllEdges(this.rootedInverted, rootedInverted, rootedInverted.edgeSet());
		TransitiveReduction.INSTANCE.reduce(rootedInverted);
		new TopologicalOrderIterator<>(this.rootedInverted).forEachRemaining(v -> topoOrderedSet.add(v));
		this.maximum = topoOrderedSet.get(topoOrderedSet.size() - 1);
		for (V element : topoOrderedSet) {
			if (rootedInverted.inDegreeOf(element) == 0)
				atoms.add(element);
		}
		sparseConverter = new SparseGraphConverter<>(rootedInverted, true);
		sparse = sparseConverter.getSparseGraph();
		RestrictorSparse restrictorSparse = new RestrictorSparse(sparse);
		sparseTreeRestrictions.addAll(restrictorSparse.getSparseTreeVertexSets());
	}
	
	/*
	 * UNSAFE
	 * The first parameter MUST be a rooted inverted DAG. 
	 * No transitive reduction must have been operated on it.
	 */
	public RestrictorOpt(DirectedAcyclicGraph<V, E> rootedInverted, boolean unsafeModeSignature) {
		this.rootedInverted = new DirectedAcyclicGraph<>(null, null, false);
		Graphs.addAllEdges(this.rootedInverted, rootedInverted, rootedInverted.edgeSet());
		TransitiveReduction.INSTANCE.reduce(rootedInverted);
		new TopologicalOrderIterator<>(rootedInverted).forEachRemaining(v -> topoOrderedSet.add(v));
		this.maximum = topoOrderedSet.get(topoOrderedSet.size() - 1);
		for (V element : topoOrderedSet) {
			if (rootedInverted.inDegreeOf(element) == 0)
				atoms.add(element);
		}
		sparseConverter = new SparseGraphConverter<>(rootedInverted, true);
		sparse = sparseConverter.getSparseGraph();
		RestrictorSparse restrictorSparse = new RestrictorSparse(sparse);
		sparseTreeRestrictions.addAll(restrictorSparse.getSparseTreeVertexSets());
	}	

	public int getNbOfTrees() {
		return sparseTreeRestrictions.size();
	}

	@Override
	public boolean hasNext() {
		return treeIdx < sparseTreeRestrictions.size();
	}

	@Override
	public InvertedTree<V, E> next() {
		InvertedTree<V, E> nextTree = new InvertedTree<V, E>(
				rootedInverted, sparseConverter.getSet(sparseTreeRestrictions.get(treeIdx)), 
				maximum, atoms);
		treeIdx++;
		return nextTree;
	}
	
	@Override
	public InvertedTree<V, E> nextTransitiveReduction() {
		InvertedTree<V, E> nextTree = next();
		TransitiveReduction.INSTANCE.reduce(nextTree);
		return nextTree;
	}

}
