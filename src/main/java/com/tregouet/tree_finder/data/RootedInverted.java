package com.tregouet.tree_finder.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.utils.StructureInspector;

public class RootedInverted<V, E> extends DirectedAcyclicGraph<V, E> {

	private static final long serialVersionUID = -7454975765743463119L;
	private V root;
	private Set<V> leaves;
	private List<V> topologicalSortingOfVertices = null;
	
	protected RootedInverted(DirectedAcyclicGraph<V, E> dag, Collection<V> restriction, V root, Set<V> leaves) {
		super(null, dag.getEdgeSupplier(), false);
		this.root = root;
		this.leaves = leaves;
		List<E> restrictedEdges = new ArrayList<>();
		for (E edge : dag.edgeSet()) {
			if (restriction.contains(dag.getEdgeSource(edge)) && restriction.contains(dag.getEdgeTarget(edge)))
				restrictedEdges.add(edge);
		}
		Graphs.addAllVertices(this, restriction);
		Graphs.addAllEdges(this, dag, restrictedEdges);
	}		
	
	/*UNSAFE. The first parameter MUST be a rooted inverted graph, and the effective root must be the second parameter
	 * effective leaves the third, etc.
	 */
	protected RootedInverted(DirectedAcyclicGraph<V, E> rootedInverted, V root, Set<V> leaves, 
			List<V> topoOrder) {
		super(null, rootedInverted.getEdgeSupplier(), false);
		Graphs.addAllVertices(this, rootedInverted.vertexSet());
		Graphs.addAllEdges(this, rootedInverted, rootedInverted.edgeSet());
		this.root = root;
		this.leaves = leaves;
		this.topologicalSortingOfVertices = topoOrder;
	}
	
	protected RootedInverted(RootedInverted<V, E> rootedInverted) {
		super(null, rootedInverted.getEdgeSupplier(), false);
		this.root = rootedInverted.root;
		this.leaves = rootedInverted.leaves;
		Graphs.addAllVertices(this, rootedInverted.vertexSet());
		Graphs.addAllEdges(this, rootedInverted, rootedInverted.edgeSet());
		this.topologicalSortingOfVertices = rootedInverted.getTopologicalOrder();
	}	
	
	public boolean addAsNewRoot(V element, boolean operateOnTransitiveReduction) {
		boolean added;
		if (operateOnTransitiveReduction) {
			added = addVertex(element);
			if (added) {
				addEdge(root, element);
				root = element;
				topologicalSortingOfVertices.add(element);
			}
		}
		else {
			Set<V> elements = vertexSet();
			added = addVertex(element);
			if (added) {
				for (V setElement : elements) {
					addEdge(setElement, element);
				}
				root = element;
				topologicalSortingOfVertices.add(element);
			}
		}
		return added;
	}
	
	public Set<V> getLeaves(){
		return new HashSet<>(leaves);
	}
	
	public V getRoot() {
		return root;
	}
	
	public List<V> getTopologicalOrder() {
		if (topologicalSortingOfVertices == null) {
			topologicalSortingOfVertices = new ArrayList<>();
			new TopologicalOrderIterator<V, E>(this).forEachRemaining(topologicalSortingOfVertices::add);
		}
		return new ArrayList<V>(topologicalSortingOfVertices);
	}
	
	@Override
	public boolean removeVertex(V element) {
		if (root.equals(element)) {
			Set<E> rootIncomingEdges = incomingEdgesOf(root);
			if (rootIncomingEdges.size() != 1)
				return false;
			else root = getEdgeSource(rootIncomingEdges.iterator().next());
		}
		boolean removed = super.removeVertex(element);
		if (removed) {
			topologicalSortingOfVertices.remove(element);
			leaves.remove(element);
		}
		return removed;
	}	
	
	public void validate() throws InvalidInputException {
		if (!StructureInspector.isARootedInvertedDirectedAcyclicGraph(this))
			throw new InvalidInputException("ClassificationTree() : parameters do not allow the instantiation "
					+ "of a valid classification tree.");
	}

}
