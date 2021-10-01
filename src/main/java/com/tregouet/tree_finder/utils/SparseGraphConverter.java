package com.tregouet.tree_finder.utils;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public class SparseGraphConverter<V, E> {

	private final List<V> topoOrderedVertices;
	private final List<E> edges;
	private final List<Pair<Integer, Integer>> sparseEdges = new ArrayList<>();
	private final SparseIntDirectedGraph sparseGraph;
	
	public SparseGraphConverter(DirectedAcyclicGraph<V, E> dag) {
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
	
	public SparseIntDirectedGraph getSparseGraph() {
		return sparseGraph;
	}
	
	public List<V> getVertexSet(IntArrayList sparseVertexSet){
		List<V> vertexSet = new ArrayList<>();
		for (int sparseVertex : sparseVertexSet) {
			vertexSet.add(topoOrderedVertices.get(sparseVertex));
		}
		return vertexSet;
	}
	
	public List<E> getEdgeSet(IntArrayList sparseEdgeSet) {
		List<E> edgeSet = new ArrayList<>();
		for (int sparseEdge : sparseEdgeSet) {
			edgeSet.add(edges.get(sparseEdge));
		}
		return edgeSet;
	}

}
