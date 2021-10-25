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

public class RootedInvertedGraph<V, E> extends DirectedAcyclicGraph<V, E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7454975765743463119L;
	private final  V root;
	private final Set<V> leaves;
	private List<V> topologicalSortingOfVertices = null;
	
	/* 
	 * UNSAFE. The restriction of the first parameter to the second parameter MUST be a rooted inverted graph.
	 */
	public RootedInvertedGraph(DirectedAcyclicGraph<V, E> upperSemilattice, List<V> treeVertices, V root, Set<V> leaves) {
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
	 * UNSAFE. The restriction of the first parameter to the second parameter MUST be a rooted inverted graph.
	 */
	public RootedInvertedGraph(DirectedAcyclicGraph<V, E> upperSemilattice, Set<V> treeVertices, V root, Set<V> leaves) {
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
	 * UNSAFE. The restriction of the first parameter to the second parameter MUST be a rooted inverted graph.
	 */
	public RootedInvertedGraph(V root, Set<V> leaves, DirectedAcyclicGraph<V, E> source, Set<E> edges) {
		super(null, null, false);
		this.root = root;
		this.leaves = leaves;
		Graphs.addAllEdges(this, source, edges);
	}	
	
	//Safe if last argument is 'true'
	public RootedInvertedGraph(V root, Set<V> leaves, DirectedAcyclicGraph<V, E> source, Set<E> edges, boolean validate) 
			throws InvalidInputException {
		this(root, leaves, source, edges);
		if (validate)
			validate();
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
	
	public void validate() throws InvalidInputException {
		if (!StructureInspector.isARootedInvertedDirectedAcyclicGraph(this))
			throw new InvalidInputException("ClassificationTree() : parameters do not allow the instantiation "
					+ "of a valid classification tree.");
	}	

}
