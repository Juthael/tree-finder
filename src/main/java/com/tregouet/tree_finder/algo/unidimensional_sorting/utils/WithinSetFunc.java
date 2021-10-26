package com.tregouet.tree_finder.algo.unidimensional_sorting.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.collect.Lists;
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
		Set<E> edges = new HashSet<>();
		for (E edge : source.edgeSet()) {
			if (lowerSet.contains(source.getEdgeSource(edge)) && lowerSet.contains(source.getEdgeTarget(edge)))
				edges.add(edge);
		}
		return new RootedInvertedGraph<V, E>(source, edges, lowerSetMaximum, edgeSupplier);
	}
	
	public static <V, E extends DefaultEdge> DirectedAcyclicGraph<V, E> restriction(DirectedAcyclicGraph<V, E> source, 
			List<V> restrictTo, Supplier<E> edgeSupplier) {
		DirectedAcyclicGraph<V, E> restriction = new DirectedAcyclicGraph<>(null, edgeSupplier, false);
		Set<E> edges = new HashSet<>();
		for (E edge : source.edgeSet()) {
			if (restrictTo.contains(source.getEdgeSource(edge)) && restrictTo.contains(source.getEdgeTarget(edge)))
				edges.add(edge);
		}
		Graphs.addAllEdges(restriction, source, edges);
		return restriction;
	}
	
	public static <V, E extends DefaultEdge> DirectedAcyclicGraph<V, E> uprooted(RootedInvertedGraph<V, E> rootedInverted, 
			V root , Supplier<E> edgeSupplier) {
		DirectedAcyclicGraph<V, E> uprooted = new DirectedAcyclicGraph<>(null, edgeSupplier, false);
		Set<E> edges = new HashSet<>();
		for (E edge : rootedInverted.edgeSet()) {
			if (!rootedInverted.getEdgeTarget(edge).equals(root))
				edges.add(edge);
		}
		Graphs.addAllEdges(uprooted, rootedInverted, edges);
		return uprooted;
	}
	
	public static <V, E extends DefaultEdge> Set<V> finishingSubset(DirectedAcyclicGraph<V, E> dag, Set<V> base) {
		Set<V> finishingSubset = new HashSet<>(base);
		for (V baseElement : base) {
			finishingSubset.addAll(dag.getDescendants(baseElement));
		}
		return finishingSubset;
	} 

}
