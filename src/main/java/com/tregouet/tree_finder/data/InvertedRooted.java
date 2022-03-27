package com.tregouet.tree_finder.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.utils.StructureInspector;

public class InvertedRooted<V, E> extends AbstractRooted<V, E> {

	private static final long serialVersionUID = -7454975765743463119L;
	
	protected InvertedRooted(DirectedAcyclicGraph<V, E> dag, Collection<V> restriction, V root, Set<V> leaves) {
		super(dag);
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
	
	/*UNSAFE. The first parameter MUST be a rooted inverted graph, and the effective root must be the second parameter,
	 * effective leaves the third, etc.
	 */
	protected InvertedRooted(DirectedAcyclicGraph<V, E> rootedInverted, V root, Set<V> leaves, 
			List<V> topoOrder) {
		super(rootedInverted);
		Graphs.addAllVertices(this, rootedInverted.vertexSet());
		Graphs.addAllEdges(this, rootedInverted, rootedInverted.edgeSet());
		this.root = root;
		this.leaves = leaves;
		if (topoOrder != null)
			this.topologicalSortingOfVertices = topoOrder;
		else {
			topologicalSortingOfVertices = new ArrayList<>();
			new TopologicalOrderIterator<>(rootedInverted).forEachRemaining(topologicalSortingOfVertices::add);
		}
	}
	
	protected InvertedRooted(InvertedRooted<V, E> rootedInverted) {
		super(rootedInverted);
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
	
	@Override
	public boolean removeVertex(V element) {
		if (root.equals(element)) {
			Set<E> rootIncomingEdges = incomingEdgesOf(root);
			if (rootIncomingEdges.size() != 1)
				//otherwise, the DAG wouldn't be rooted anymore
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
	
	@Override
	public void validate() throws DataFormatException {
		if (!StructureInspector.isARootedInvertedDirectedAcyclicGraph(this))
			throw new DataFormatException("ClassificationTree() : parameters do not allow the instantiation "
					+ "of a rooted inverted directed graph.");
	}

}
