package com.tregouet.tree_finder.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.utils.StructureInspector;

public class RootedInvertedGraph<V, E> extends DirectedAcyclicGraph<V, E> {

	private static final long serialVersionUID = -7454975765743463119L;
	private final  V root;
	private final Set<V> leaves;
	private List<V> topologicalSortingOfVertices = null;
	
	/* 
	 * UNSAFE. The restriction of the first parameter to the second parameter MUST be a rooted inverted graph.
	 */
	public RootedInvertedGraph(DirectedAcyclicGraph<V, E> dag, List<V> restriction, V root, Set<V> leaves) {
		super(null, null, false);
		this.root = root;
		this.leaves = leaves;
		Set<E> edges = dag.edgeSet().stream()
				.filter(e -> restriction.contains(dag.getEdgeSource(e)) 
						&& restriction.contains(dag.getEdgeTarget(e)))
				.collect(Collectors.toSet());
		Graphs.addAllEdges(this, dag, edges);
	}
	
	/* 
	 * UNSAFE. The restriction of the first parameter to the second parameter MUST be a rooted inverted graph.
	 */
	public RootedInvertedGraph(DirectedAcyclicGraph<V, E> dag, Set<V> restriction, V root, Set<V> leaves) {
		super(null, null, false);
		this.root = root;
		this.leaves = leaves;
		Set<E> edges = dag.edgeSet().stream()
				.filter(e -> restriction.contains(dag.getEdgeSource(e)) 
						&& restriction.contains(dag.getEdgeTarget(e)))
				.collect(Collectors.toSet());
		Graphs.addAllEdges(this, dag, edges);
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
	
	/* 
	 * UNSAFE. The restriction of the first parameter's relation to the second parameter MUST be a rooted 
	 * inverted graph, and the effective root must be the third parameter.
	 */
	public RootedInvertedGraph(DirectedAcyclicGraph<V, E> source, Set<E> edges, V root, Supplier<E> edgeSupplier) {
		super(null, edgeSupplier, false);
		Graphs.addAllEdges(this, source, edges);
		this.root = root;
		leaves = new HashSet<>();
		for (V element : this.vertexSet()) {
			if (inDegreeOf(element) == 0)
				leaves.add(element);
		}
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
