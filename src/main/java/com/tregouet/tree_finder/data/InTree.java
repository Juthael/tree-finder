package com.tregouet.tree_finder.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.error.InvalidTreeException;

public class InTree<V, E> extends DirectedAcyclicGraph<V, E> {

	private static final long serialVersionUID = 2206651329473240403L;
	private V root;
	private List<V> leaves;
	private List<V> topologicalSortingOfVertices = null;

	//Unsafe
	public InTree(V root, List<V> leaves, DirectedAcyclicGraph<V, E> source, Set<E> edges) {
		super(null, null, false);
		this.root = root;
		this.leaves = leaves;
		Graphs.addAllEdges(this, source, edges);
	}
	
	//Safe if last argument is 'true'
	public InTree(V root, List<V> leaves, DirectedAcyclicGraph<V, E> source, Set<E> edges, boolean validate) 
			throws InvalidTreeException {
		this(root, leaves, source, edges);
		if (validate)
			validate();
	}
	
	/* 
	 * No transitive reduction must have been operated on first parameter. 
	 * Safe if last argument is 'true'
	 */
	public InTree(DirectedAcyclicGraph<V, E> unreducedLattice, Set<V> treeVertices, V root, Set<V> leaves, 
			boolean validate) {
		super(null, null, false);
		this.root = root;
		this.leaves = new ArrayList<>(leaves);
		Set<E> edges = unreducedLattice.edgeSet().stream()
				.filter(e -> treeVertices.contains(unreducedLattice.getEdgeSource(e)) 
						&& treeVertices.contains(unreducedLattice.getEdgeTarget(e)))
				.collect(Collectors.toSet());
		Graphs.addAllEdges(this, unreducedLattice, edges);
		TransitiveReduction.INSTANCE.reduce(this);
	}
	
	public List<V> getLeaves(){
		return leaves;
	}
	
	public V getRoot() {
		return root;
	}
	
	public void validate() throws InvalidTreeException {
		if (!ITreeFinder.isAnInTree(this))
			throw new InvalidTreeException();
	}
	
	public List<V> getTopologicalSortingOfVertices() {
		if (topologicalSortingOfVertices == null) {
			topologicalSortingOfVertices = new ArrayList<>();
			new TopologicalOrderIterator<V, E>(this).forEachRemaining(topologicalSortingOfVertices::add);
		}
		return topologicalSortingOfVertices;
	}

}
