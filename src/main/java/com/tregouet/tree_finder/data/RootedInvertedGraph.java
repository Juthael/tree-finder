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
	private final V root;
	private final Set<V> leaves;
	private List<V> topologicalSortingOfVertices = null;	
	
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
	
	//UNSAFE. The first parameter MUST be a rooted inverted graph, and the effective root must be the second parameter.
	public RootedInvertedGraph(DirectedAcyclicGraph<V, E> rootedInverted, V root, Supplier<E> edgeSupplier) {
		super(null, edgeSupplier, false);
		Graphs.addAllVertices(this, rootedInverted.vertexSet());
		Graphs.addAllEdges(this, rootedInverted, rootedInverted.edgeSet());
		this.root = root;
		leaves = new HashSet<>();
		for (V element : this.vertexSet()) {
			if (inDegreeOf(element) == 0)
				leaves.add(element);
		}
	}
	
	//UNSAFE. The first parameter MUST be a rooted inverted graph, and the effective root must be the second parameter.
	public RootedInvertedGraph(DirectedAcyclicGraph<V, E> rootedInverted, V root, Set<V> leaves, 
			List<V> topoOrder, Supplier<E> edgeSupplier) {
		super(null, edgeSupplier, false);
		Graphs.addAllVertices(this, rootedInverted.vertexSet());
		Graphs.addAllEdges(this, rootedInverted, rootedInverted.edgeSet());
		this.root = root;
		this.leaves = leaves;
		this.topologicalSortingOfVertices = topoOrder;
	}
	
	protected RootedInvertedGraph(RootedInvertedGraph<V, E> rootedInverted, Supplier<E> edgeSupplier) {
		super(null, edgeSupplier, false);
		this.root = rootedInverted.root;
		this.leaves = rootedInverted.leaves;
		Graphs.addAllEdges(this, rootedInverted, rootedInverted.edgeSet());
		this.topologicalSortingOfVertices = rootedInverted.topologicalSortingOfVertices;
	}
	
	protected RootedInvertedGraph(DirectedAcyclicGraph<V, E> dag, List<V> restriction, V root, Set<V> leaves) {
		super(null, null, false);
		this.root = root;
		this.leaves = leaves;
		Set<E> edges = dag.edgeSet().stream()
				.filter(e -> restriction.contains(dag.getEdgeSource(e)) 
						&& restriction.contains(dag.getEdgeTarget(e)))
				.collect(Collectors.toSet());
		Graphs.addAllEdges(this, dag, edges);
	}	
	
	protected RootedInvertedGraph(DirectedAcyclicGraph<V, E> dag, Set<V> restriction, V root, Set<V> leaves) {
		super(null, null, false);
		this.root = root;
		this.leaves = leaves;
		Set<E> edges = dag.edgeSet().stream()
				.filter(e -> restriction.contains(dag.getEdgeSource(e)) 
						&& restriction.contains(dag.getEdgeTarget(e)))
				.collect(Collectors.toSet());
		Graphs.addAllEdges(this, dag, edges);
	}	
	
	public Set<V> getLeaves(){
		return new HashSet<>(leaves);
	}
	
	public V getRoot() {
		return root;
	}
	
	public List<V> getTopologicalSortingOfVertices() {
		if (topologicalSortingOfVertices == null) {
			topologicalSortingOfVertices = new ArrayList<>();
			new TopologicalOrderIterator<V, E>(this).forEachRemaining(topologicalSortingOfVertices::add);
		}
		return new ArrayList<V>(topologicalSortingOfVertices);
	}	
	
	public void validate() throws InvalidInputException {
		if (!StructureInspector.isARootedInvertedDirectedAcyclicGraph(this))
			throw new InvalidInputException("ClassificationTree() : parameters do not allow the instantiation "
					+ "of a valid classification tree.");
	}

}
