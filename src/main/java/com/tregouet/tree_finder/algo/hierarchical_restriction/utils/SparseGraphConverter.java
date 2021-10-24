package com.tregouet.tree_finder.algo.hierarchical_restriction.utils;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;

public class SparseGraphConverter<V, E> {

	private final List<V> topoOrderedSet;
	private final List<E> edges;
	private final List<Pair<Integer, Integer>> sparseEdges = new ArrayList<>();
	private final SparseIntDirectedGraph sparseGraph;
	
	public SparseGraphConverter(DirectedAcyclicGraph<V, E> dag, boolean skipClosure) {
		if (!skipClosure)
			TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(dag);
		topoOrderedSet = new ArrayList<>();
		TopologicalOrderIterator<V, E> topoIte = new TopologicalOrderIterator<>(dag);
		topoIte.forEachRemaining(v -> topoOrderedSet.add(v));
		edges = new ArrayList<>(dag.edgeSet());
		for (E edge : edges) {
			sparseEdges.add(
					new Pair<Integer, Integer>(
							topoOrderedSet.indexOf(dag.getEdgeSource(edge)), 
							topoOrderedSet.indexOf(dag.getEdgeTarget(edge))));
		}
		sparseGraph = new SparseIntDirectedGraph(topoOrderedSet.size(), sparseEdges);
	}
	
	public DirectedAcyclicGraph<Integer, Integer> asSparseDAG() {
		DirectedAcyclicGraph<Integer, Integer> dAG = new DirectedAcyclicGraph<>(null,  null,  false);
		Graphs.addAllEdges(dAG, sparseGraph, sparseGraph.edgeSet());
		return dAG;
	}
	
	public List<E> getEdgeSet(IntArrayList sparseEdgeSet) {
		List<E> edgeSet = new ArrayList<>();
		for (int sparseEdge : sparseEdgeSet) {
			edgeSet.add(edges.get(sparseEdge));
		}
		return edgeSet;
	}
	
	public V getElement(int sparseElement) {
		return topoOrderedSet.get(sparseElement);
	}
	
	public List<V> getSet(IntArrayList sparseSet){
		List<V> vertexSet = new ArrayList<>();
		for (int sparseVertex : sparseSet) {
			vertexSet.add(topoOrderedSet.get(sparseVertex));
		}
		return vertexSet;
	}	
	
	public List<V> getSet(IntArraySet sparseSet){
		List<V> vertexSet = new ArrayList<>();
		for (int sparseVertex : sparseSet) {
			vertexSet.add(topoOrderedSet.get(sparseVertex));
		}
		return vertexSet;
	}
	
	public SparseIntDirectedGraph getSparseGraph() {
		return sparseGraph;
	}

}
