package com.tregouet.tree_finder.utils;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public class SparseGraphConverter<V, E> {

	private final List<V> vertices;
	private final List<E> edges;
	private final List<Pair<Integer, Integer>> sparseEdges = new ArrayList<>();
	private final SparseIntDirectedGraph sparseGraph;
	
	public SparseGraphConverter(DirectedAcyclicGraph<V, E> dag) {
		vertices = new ArrayList<>(dag.vertexSet());
		edges = new ArrayList<>(dag.edgeSet());
		for (E edge : edges) {
			sparseEdges.add(
					new Pair<Integer, Integer>(
							vertices.indexOf(dag.getEdgeSource(edge)), 
							vertices.indexOf(dag.getEdgeTarget(edge))));
		}
		sparseGraph = new SparseIntDirectedGraph(vertices.size(), sparseEdges);
	}
	
	public SparseIntDirectedGraph getSparseGraph() {
		return sparseGraph;
	}
	
	public List<V> getVertexSet(IntArrayList sparseVertexSet){
		List<V> vertexSet = new ArrayList<>();
		for (int sparseVertex : sparseVertexSet) {
			vertexSet.add(vertices.get(sparseVertex));
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
