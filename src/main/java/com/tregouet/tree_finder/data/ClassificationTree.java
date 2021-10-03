package com.tregouet.tree_finder.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.error.InvalidTreeException;

public class ClassificationTree<V, E> extends DirectedAcyclicGraph<V, E> {

	private static final long serialVersionUID = 2206651329473240403L;
	private V root;
	private List<V> leaves;
	private List<V> topologicalSortingOfVertices = null;

	//Unsafe
	public ClassificationTree(V root, List<V> leaves, DirectedAcyclicGraph<V, E> source, Set<E> edges) {
		super(null, null, false);
		this.root = root;
		this.leaves = leaves;
		Graphs.addAllEdges(this, source, edges);
	}	
	
	//Safe if last argument is 'true'
	public ClassificationTree(V root, List<V> leaves, DirectedAcyclicGraph<V, E> source, Set<E> edges, boolean validate) 
			throws InvalidTreeException {
		this(root, leaves, source, edges);
		if (validate)
			validate();
	}
	
	/* 
	 * No transitive reduction must have been operated on first parameter. 
	 * Safe if last argument is 'true'
	 */
	public ClassificationTree(DirectedAcyclicGraph<V, E> upperSemilattice, Set<V> treeVertices, V root, Set<V> leaves, 
			boolean validate) {
		super(null, null, false);
		this.root = root;
		this.leaves = new ArrayList<>(leaves);
		Set<E> edges = upperSemilattice.edgeSet().stream()
				.filter(e -> treeVertices.contains(upperSemilattice.getEdgeSource(e)) 
						&& treeVertices.contains(upperSemilattice.getEdgeTarget(e)))
				.collect(Collectors.toSet());
		Graphs.addAllEdges(this, upperSemilattice, edges);
	}
	
	/* 
	 * No transitive reduction must have been operated on first parameter. 
	 * Safe if last argument is 'true'
	 */
	public ClassificationTree(DirectedAcyclicGraph<V, E> upperSemilattice, List<V> treeVertices, V root, List<V> leaves, 
			boolean validate) {
		super(null, null, false);
		this.root = root;
		this.leaves = leaves;
		Set<E> edges = upperSemilattice.edgeSet().stream()
				.filter(e -> treeVertices.contains(upperSemilattice.getEdgeSource(e)) 
						&& treeVertices.contains(upperSemilattice.getEdgeTarget(e)))
				.collect(Collectors.toSet());
		Graphs.addAllEdges(this, upperSemilattice, edges);
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
