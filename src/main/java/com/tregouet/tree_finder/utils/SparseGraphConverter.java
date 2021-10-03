package com.tregouet.tree_finder.utils;

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

	private final List<V> topoOrderedVertices;
	private final List<E> edges;
	private final List<Pair<Integer, Integer>> sparseEdges = new ArrayList<>();
	private final SparseIntDirectedGraph sparseGraph;
	
	public SparseGraphConverter(DirectedAcyclicGraph<V, E> dag, boolean relationIsTransitive) {
		if (!relationIsTransitive)
			TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(dag);
		topoOrderedVertices = new ArrayList<>();
		TopologicalOrderIterator<V, E> topoIte = new TopologicalOrderIterator<>(dag);
		topoIte.forEachRemaining(v -> topoOrderedVertices.add(v));
		edges = new ArrayList<>(dag.edgeSet());
		for (E edge : edges) {
			sparseEdges.add(
					new Pair<Integer, Integer>(
							topoOrderedVertices.indexOf(dag.getEdgeSource(edge)), 
							topoOrderedVertices.indexOf(dag.getEdgeTarget(edge))));
		}
		sparseGraph = new SparseIntDirectedGraph(topoOrderedVertices.size(), sparseEdges);
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
	
	public SparseIntDirectedGraph getSparseGraph() {
		return sparseGraph;
	}
	
	public V getVertex(int sparseVertex) {
		return topoOrderedVertices.get(sparseVertex);
	}	
	
	public List<V> getVertexSet(IntArrayList sparseVertexSet){
		List<V> vertexSet = new ArrayList<>();
		for (int sparseVertex : sparseVertexSet) {
			vertexSet.add(topoOrderedVertices.get(sparseVertex));
		}
		return vertexSet;
	}
	
	public List<V> getVertexSet(IntArraySet sparseVertexSet){
		List<V> vertexSet = new ArrayList<>();
		for (int sparseVertex : sparseVertexSet) {
			vertexSet.add(topoOrderedVertices.get(sparseVertex));
		}
		return vertexSet;
	}

}
