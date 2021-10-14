package com.tregouet.tree_finder.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.utils.StructureInspector;

public class ClassificationTree<V, E> extends DirectedAcyclicGraph<V, E> {

	private static final long serialVersionUID = 2206651329473240403L;
	private V root;
	private Set<V> leaves;
	private List<V> topologicalSortingOfVertices = null;

	/* 
	 * No transitive reduction must have been operated on first parameter. 
	 * Safe if last argument is 'true'
	 */
	public ClassificationTree(DirectedAcyclicGraph<V, E> upperSemilattice, List<V> treeVertices, V root, Set<V> leaves, 
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
	
	/* 
	 * No transitive reduction must have been operated on first parameter. 
	 * Safe if last argument is 'true'
	 */
	public ClassificationTree(DirectedAcyclicGraph<V, E> upperSemilattice, Set<V> treeVertices, V root, Set<V> leaves, 
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
	
	//Unsafe
	public ClassificationTree(V root, Set<V> leaves, DirectedAcyclicGraph<V, E> source, Set<E> edges) {
		super(null, null, false);
		this.root = root;
		this.leaves = leaves;
		Graphs.addAllEdges(this, source, edges);
	}
	
	//Safe if last argument is 'true'
	public ClassificationTree(V root, Set<V> leaves, DirectedAcyclicGraph<V, E> source, Set<E> edges, boolean validate) 
			throws InvalidInputException {
		this(root, leaves, source, edges);
		if (validate)
			validate();
	}	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		ClassificationTree other = (ClassificationTree) obj;
		if (topologicalSortingOfVertices == null) {
			if (other.topologicalSortingOfVertices != null)
				return false;
		} else if (!topologicalSortingOfVertices.equals(other.topologicalSortingOfVertices))
			return false;
		return (this.edgeSet().equals(other.edgeSet()));
	}
	
	public Set<V> getLeaves(){
		return leaves;
	}
	
	public V getRoot() {
		return root;
	}
	
	public List<V> getTopologicalSortingOfVertices() {
		if (topologicalSortingOfVertices == null) {
			topologicalSortingOfVertices = new ArrayList<>();
			new TopologicalOrderIterator<V, E>(this).forEachRemaining(topologicalSortingOfVertices::add);
		}
		return topologicalSortingOfVertices;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((topologicalSortingOfVertices == null) ? 0 : topologicalSortingOfVertices.hashCode());
		return result;
	}

	public void validate() throws InvalidInputException {
		if (!StructureInspector.isAClassificationTree(this))
			throw new InvalidInputException("ClassificationTree() : parameters do not allow the instantiation "
					+ "of a valid classification tree.");
	}

}
