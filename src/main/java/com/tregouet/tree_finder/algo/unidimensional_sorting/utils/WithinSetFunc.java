package com.tregouet.tree_finder.algo.unidimensional_sorting.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.tregouet.tree_finder.data.RootedInverted;

public class WithinSetFunc {

	private WithinSetFunc() {
	}

	public static <V, E extends DefaultEdge> Set<V> lowerSet(DirectedAcyclicGraph<V, E> source, V lowerSetMaximum) {
		Set<V> lowerSet = source.getAncestors(lowerSetMaximum);
		lowerSet.add(lowerSetMaximum);
		return lowerSet;
	}
	
	public static <V, E extends DefaultEdge> List<V> maxima(DirectedAcyclicGraph<V, E> dag) {
		List<V> maxima = new ArrayList<>();
		for (V element : dag.vertexSet()) {
			if (dag.outDegreeOf(element) == 0)
				maxima.add(element);
		}
		return maxima;
	}
	
	public static <V, E extends DefaultEdge> DirectedAcyclicGraph<V, E> remove(RootedInverted<V, E> rootedInverted, 
			V element , Supplier<E> edgeSupplier) {
		DirectedAcyclicGraph<V, E> elementRemoved = new DirectedAcyclicGraph<>(null, edgeSupplier, false);
		Graphs.addAllVertices(elementRemoved, rootedInverted.vertexSet());
		Graphs.addAllEdges(elementRemoved, rootedInverted, rootedInverted.edgeSet());
		elementRemoved.removeVertex(element);
		return elementRemoved;
	}
	
	public static <V, E extends DefaultEdge> DirectedAcyclicGraph<V, E> restriction(DirectedAcyclicGraph<V, E> source, 
			Collection<V> restrictTo, Supplier<E> edgeSupplier) {
		DirectedAcyclicGraph<V, E> restriction = new DirectedAcyclicGraph<>(null, edgeSupplier, false);
		Set<E> restrictedEdges = new HashSet<>();
		for (E edge : source.edgeSet()) {
			if (restrictTo.contains(source.getEdgeSource(edge)) && restrictTo.contains(source.getEdgeTarget(edge)))
				restrictedEdges.add(edge);
		}
		Graphs.addAllVertices(restriction, restrictTo);
		Graphs.addAllEdges(restriction, source, restrictedEdges);
		return restriction;
	}
	
	public static <V, E extends DefaultEdge> V supremum(DirectedAcyclicGraph<V, E> dag, Set<V> subset) {
		int subsetSize = subset.size();
		if (subsetSize == 0)
			return null;
		Iterator<V> subsetIte = subset.iterator();
		if (subsetSize == 1) {
			return subsetIte.next();
		}
		else {
			Set<V> upperBounds = new HashSet<>();
			upperBounds.addAll(dag.getDescendants(subsetIte.next()));
			while (subsetIte.hasNext())
				upperBounds.retainAll(dag.getDescendants(subsetIte.next()));
			Set<V> minimalUpperBounds = new HashSet<>(upperBounds);
			for (V upperBound : upperBounds) {
				if (minimalUpperBounds.contains(upperBound))
					minimalUpperBounds.removeAll(dag.getDescendants(upperBound));
			}
			if (minimalUpperBounds.size() ==1)
				return minimalUpperBounds.iterator().next();
		}
		return null;
	}
			
}
