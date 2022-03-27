package com.tregouet.tree_finder.utils;

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
import com.tregouet.tree_finder.data.InvertedTree;
import com.tregouet.tree_finder.data.InvertedUpperSemilattice;
import com.tregouet.tree_finder.data.Tree;

public class Functions {

	private Functions() {
	}
	
	public static <V, E> DirectedAcyclicGraph<V, E> cardinalSum(
			List<InvertedTree<V, E>> dags, Supplier<E> edgeSupplier) {
		if (dags.size() == 1)
			return dags.get(0);
		DirectedAcyclicGraph<V, E> cardinalSum = new DirectedAcyclicGraph<>(null, edgeSupplier, false);
		for (DirectedAcyclicGraph<V, E> dag : dags) {
			Graphs.addAllVertices(cardinalSum, dag.vertexSet());
			Graphs.addAllEdges(cardinalSum, dag, dag.edgeSet());
		}
		return cardinalSum;
	}	
	
	public static <V, E> DirectedAcyclicGraph<V, E> cardinalSum(InvertedTree<V, E> tree1, InvertedTree<V, E> tree2, 
			Supplier<E> edgeSupplier) {
		DirectedAcyclicGraph<V, E> cardinalSum = new DirectedAcyclicGraph<>(null, edgeSupplier, false);
		Graphs.addAllVertices(cardinalSum, tree1.vertexSet());
		Graphs.addAllVertices(cardinalSum, tree2.vertexSet());
		Graphs.addAllEdges(cardinalSum, tree1, tree1.edgeSet());
		Graphs.addAllEdges(cardinalSum, tree2, tree2.edgeSet());
		return cardinalSum;
	}
	
	public static <V, E> boolean isStrictUpperBoundOfBreadthFirst(V v1, V v2, DirectedAcyclicGraph<V, E> graph) {
		if (v1.equals(v2))
			return false;
		List<V> nextRankSuccessors;
		Set<V> nextRank = new HashSet<>(Graphs.predecessorListOf(graph, v1));
		do {
			if (nextRank.contains(v2))
				return true;
			nextRankSuccessors = new ArrayList<>(nextRank);
			nextRank.clear();
			for (V nextRankSucc : nextRankSuccessors)
				nextRank.addAll(Graphs.predecessorListOf(graph, nextRankSucc));
			
		}
		while(!nextRank.isEmpty());
		return false;
	}	
		
	
	public static <V, E> boolean isStrictUpperBoundOfDepthFirst(V v1, V v2, DirectedAcyclicGraph<V, E> graph) {
		if (v1.equals(v2))
			return false;
		for (E incomingEdge : graph.incomingEdgesOf(v1)) {
			V predecessor = graph.getEdgeSource(incomingEdge);
			if (predecessor.equals(v2) || isStrictUpperBoundOfDepthFirst(predecessor, v2, graph))
				return true;
		}
		return false;
	}	
	
	public static <V, E> Set<V> lowerSet(DirectedAcyclicGraph<V, E> source, V lowerSetMaximum) {
		Set<V> lowerSet = source.getAncestors(lowerSetMaximum);
		lowerSet.add(lowerSetMaximum);
		return lowerSet;
	}
	
	public static <V, E> Set<V> upperSet(DirectedAcyclicGraph<V, E> source, V upperSetMinimum) {
		Set<V> upperSet = source.getDescendants(upperSetMinimum);
		upperSet.add(upperSetMinimum);
		return upperSet;
	}
	
	public static <V, E> Set<V> maxima(DirectedAcyclicGraph<V, E> dag) {
		Set<V> maxima = new HashSet<>();
		for (V element : dag.vertexSet()) {
			if (dag.outDegreeOf(element) == 0)
				maxima.add(element);
		}
		return maxima;
	}
	
	public static <V, E> boolean removeVertexAndPreserveConnectivity(DirectedAcyclicGraph<V, E> dag, V removed) {
		if (!dag.containsVertex(removed))
			return false;
		List<V> predecessorsOfRemoved = Graphs.predecessorListOf(dag, removed);
		List<V> successorsOfRemoved = Graphs.successorListOf(dag, removed);
		dag.removeVertex(removed);
		for (V predecessor : predecessorsOfRemoved) {
			for (V successor : successorsOfRemoved) {
				if (!isStrictUpperBoundOfBreadthFirst(successor, predecessor, dag))
					dag.addEdge(predecessor, successor);
			}
		}
		return true;
	}	
	
	public static <V, E extends DefaultEdge> DirectedAcyclicGraph<V, E> restriction(DirectedAcyclicGraph<V, E> source, 
			Collection<V> restrictTo) {
		DirectedAcyclicGraph<V, E> restriction = new DirectedAcyclicGraph<>(null, source.getEdgeSupplier(), false);
		Set<E> restrictedEdges = new HashSet<>();
		for (E edge : source.edgeSet()) {
			if (restrictTo.contains(source.getEdgeSource(edge)) && restrictTo.contains(source.getEdgeTarget(edge)))
				restrictedEdges.add(edge);
		}
		Graphs.addAllVertices(restriction, restrictTo);
		Graphs.addAllEdges(restriction, source, restrictedEdges);
		return restriction;
	}
	
	public static <V, E> V commonAncestor(Tree<V, E> tree, Set<V> subset) {
		V genus = null;
		int subsetSize = subset.size();
		if (subsetSize == 0)
			return null;
		Iterator<V> subsetIte = subset.iterator();
		if (subsetSize == 1)
			return subsetIte.next();
		Set<V> lowerBounds = new HashSet<>(lowerSet(tree, subsetIte.next()));
		while (subsetIte.hasNext())
			lowerBounds.retainAll(lowerSet(tree, subsetIte.next()));
		Iterator<V> reversedTopoOrderIte = Lists.reverse(tree.getTopologicalOrder()).iterator();
		while(genus == null){
			V next = reversedTopoOrderIte.next();
			if (lowerBounds.contains(next))
				genus = next;
		}
		return genus;
	}	
	
	public static <V, E> V commonAncestor(Tree<V, E> tree, V v1, V v2) {
		V genus = null;
		Set<V> lowerBounds = Sets.intersection(lowerSet(tree, v1), lowerSet(tree, v2));
		Iterator<V> reversedTopoOrderIte = Lists.reverse(tree.getTopologicalOrder()).iterator();
		while(genus == null){
			V next = reversedTopoOrderIte.next();
			if (lowerBounds.contains(next))
				genus = next;
		}
		return genus;
	}
	
	public static <V, E> V supremum(InvertedUpperSemilattice<V, E> dag, Set<V> subset) {
		int subsetSize = subset.size();
		if (subsetSize == 0)
			return null;
		Iterator<V> subsetIte = subset.iterator();
		if (subsetSize == 1) 
			return subsetIte.next();
		Set<V> upperBounds = new HashSet<>(upperSet(dag, subsetIte.next()));
		while (subsetIte.hasNext())
			upperBounds.retainAll(upperSet(dag, subsetIte.next()));
		Set<V> minimalUpperBounds = new HashSet<>(upperBounds);
		for (V upperBound : upperBounds) {
			if (minimalUpperBounds.contains(upperBound))
				minimalUpperBounds.removeAll(dag.getDescendants(upperBound));
		}
		if (minimalUpperBounds.size() == 1)
			return minimalUpperBounds.iterator().next();
		return null;
	}
	
	public static <V, E> V supremum(InvertedUpperSemilattice<V, E> dag, V v1, V v2) {
		if (v1.equals(v2))
			return v1;
		Set<V> upperBounds = Sets.intersection(upperSet(dag, v1), upperSet(dag, v2));
		Set<V> minimalUpperBounds = new HashSet<>(upperBounds);
		for (V upperBound : upperBounds) {
			if (minimalUpperBounds.contains(upperBound))
				minimalUpperBounds.removeAll(dag.getDescendants(upperBound));
		}
		if (minimalUpperBounds.size() == 1)
			return minimalUpperBounds.iterator().next();
		return null;
	}

}
