package com.tregouet.tree_finder.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

public abstract class AbstractRooted<V, E> extends DirectedAcyclicGraph<V, E> {

	private static final long serialVersionUID = 2720735900692432726L;

	protected V root;
	protected Set<V> leaves;
	protected List<V> topologicalSortingOfVertices = null;

	protected AbstractRooted(DirectedAcyclicGraph<V, E> dag) {
		super(null, dag.getEdgeSupplier(), false);
	}

	public abstract boolean addAsNewRoot(V element, boolean operateOnTransitiveReduction);

	public Set<V> getLeaves(){
		return new HashSet<>(leaves);
	}

	public V getRoot() {
		return root;
	}

	public List<V> getTopologicalOrder() {
		if (topologicalSortingOfVertices == null) {
			topologicalSortingOfVertices = new ArrayList<>();
			new TopologicalOrderIterator<>(this).forEachRemaining(topologicalSortingOfVertices::add);
		}
		return new ArrayList<>(topologicalSortingOfVertices);
	}

	@Override
	public boolean removeVertex(V element) {
		return super.removeVertex(element);
	}

	public boolean replaceVertex(V element, V substitute) {
		if (element == substitute)
			return true;
		List<V> successors = Graphs.successorListOf(this, element);
		List<V> predecessors = Graphs.predecessorListOf(this, element);
		if (super.removeVertex(element)) {
			addVertex(substitute);
			for (V successor : successors)
				addEdge(substitute, successor);
			for (V predecessor : predecessors)
				addEdge(predecessor, substitute);
			return true;
		}
		return false;
	}

	public abstract void validate() throws DataFormatException;

}
