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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tregouet.tree_finder.data.RootedInvertedGraph;

public class WithinSetFunc {

	private WithinSetFunc() {
	}

	public static <V, E extends DefaultEdge> List<V> maxima(DirectedAcyclicGraph<V, E> dag) {
		List<V> maxima = new ArrayList<>();
		for (V element : dag.vertexSet()) {
			if (dag.outDegreeOf(element) == 0)
				maxima.add(element);
		}
		return maxima;
	}
	
	public static <V, E extends DefaultEdge> List<V> minima(DirectedAcyclicGraph<V, E> dag) {
		List<V> minima = new ArrayList<>();
		for (V element : dag.vertexSet()) {
			if (dag.inDegreeOf(element) == 0)
				minima.add(element);
		}
		return minima;
	}	
	
	public static <V, E extends DefaultEdge> List<V> reversedTopologicalOrder(RootedInvertedGraph<V, E> rootedInverted) {
		return new ArrayList<>(Lists.reverse(rootedInverted.getTopologicalSortingOfVertices()));
	}
	
	public static <V, E extends DefaultEdge> RootedInvertedGraph<V, E> lowerSet(
			DirectedAcyclicGraph<V, E> source, V lowerSetMaximum, Supplier<E> edgeSupplier) {
		Set<V> lowerSet = source.getAncestors(lowerSetMaximum);
		lowerSet.add(lowerSetMaximum);
		return new RootedInvertedGraph<V, E>(source, lowerSet, lowerSetMaximum, edgeSupplier);
	}
	
	public static <V, E extends DefaultEdge> DirectedAcyclicGraph<V, E> complement(
			DirectedAcyclicGraph<V, E> source, Set<V> subset, Supplier<E> edgeSupplier) {
		DirectedAcyclicGraph<V, E> complement = new DirectedAcyclicGraph<>(null, edgeSupplier, false);
		Set<V> complementElements = new HashSet<>(Sets.difference(source.vertexSet(), subset));
		Set<E> complementRelation = new HashSet<>();
		for (E edge : source.edgeSet()) {
			if (complementElements.contains(source.getEdgeSource(edge)) 
					&& complementElements.contains(source.getEdgeTarget(edge)))
				complementRelation.add(edge);
		}
		Graphs.addAllVertices(complement, complementElements);
		Graphs.addAllEdges(complement, source, complementRelation);
		return complement;
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
	
	public static <V, E extends DefaultEdge> DirectedAcyclicGraph<V, E> beheaded(RootedInvertedGraph<V, E> rootedInverted, 
			V root , Supplier<E> edgeSupplier) {
		DirectedAcyclicGraph<V, E> uprooted = new DirectedAcyclicGraph<>(null, edgeSupplier, false);
		Graphs.addAllVertices(uprooted, rootedInverted.vertexSet());
		Graphs.addAllEdges(uprooted, rootedInverted, rootedInverted.edgeSet());
		uprooted.removeVertex(root);
		return uprooted;
	}
	
	public static <V, E extends DefaultEdge> Set<V> finishingSubset(DirectedAcyclicGraph<V, E> dag, Set<V> base) {
		Set<V> finishingSubset = new HashSet<>(base);
		for (V baseElement : base) {
			finishingSubset.addAll(dag.getDescendants(baseElement));
		}
		return finishingSubset;
	} 
	
	public static <V, E extends DefaultEdge> Set<V> beginningSubset(DirectedAcyclicGraph<V, E> dag, Set<V> base) {
		Set<V> beginningSubset = new HashSet<>(base);
		for (V baseElement : base) {
			beginningSubset.addAll(dag.getAncestors(baseElement));
		}
		return beginningSubset;
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
	
	public static <V, E extends DefaultEdge> Set<V> nonMinimalUpperBounds(DirectedAcyclicGraph<V, E> dag, 
			Set<V> subset) {
		int subsetSize = subset.size();
		if (subsetSize == 0)
			return null;
		Set<V> nonMinimalUpperBounds = new HashSet<>();
		if (subsetSize == 1) {
			nonMinimalUpperBounds.addAll(dag.vertexSet());
			nonMinimalUpperBounds.removeAll(subset);
			return nonMinimalUpperBounds;
		}
		Iterator<V> subsetIte = subset.iterator();
		nonMinimalUpperBounds.addAll(dag.getDescendants(subsetIte.next()));
		while (subsetIte.hasNext())
			nonMinimalUpperBounds.retainAll(dag.getDescendants(subsetIte.next()));
		Set<V> minimalUpperBounds = new HashSet<>(nonMinimalUpperBounds);
		for (V upperBound : nonMinimalUpperBounds) {
			if (minimalUpperBounds.contains(upperBound))
				minimalUpperBounds.removeAll(dag.getDescendants(upperBound));
		}
		nonMinimalUpperBounds.removeAll(minimalUpperBounds);
		return nonMinimalUpperBounds;
	}
			
}
