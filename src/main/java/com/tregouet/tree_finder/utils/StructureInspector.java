package com.tregouet.tree_finder.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graphs;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.google.common.collect.Sets;

public class StructureInspector {

	private StructureInspector() {
	}
	
	public static <V, E> boolean isAClassificationTree(DirectedAcyclicGraph<V, E> dag) {
		if (!isAnUpperSemilattice(dag))
			return false;
		List<Set<V>> closedSupGeneratorSubsetsOfMinimals = new ArrayList<>();
		Set<V> minimals = dag.vertexSet().stream()
				.filter(v -> dag.inDegreeOf(v) == 0)
				.collect(Collectors.toSet());
		for (V element : dag.vertexSet())
			closedSupGeneratorSubsetsOfMinimals.add(
					new HashSet<>(Sets.intersection(dag.getAncestors(element), minimals)));
		for (int i = 0 ; i < closedSupGeneratorSubsetsOfMinimals.size() - 1 ; i++) {
			Set<V> iMinimalLowerBounds = closedSupGeneratorSubsetsOfMinimals.get(i);
			for (int j = i + 1 ; j < closedSupGeneratorSubsetsOfMinimals.size() ; j++) {
				Set<V> jMinimalLowerBounds = closedSupGeneratorSubsetsOfMinimals.get(j);
				Set<V> intersection = Sets.intersection(iMinimalLowerBounds, jMinimalLowerBounds);
				//hierarchy clause
				if (!intersection.equals(iMinimalLowerBounds) 
						&& !intersection.equals(jMinimalLowerBounds) 
						&& !intersection.isEmpty())
					return false;
			}
		}
		return true;
	}
	
	public static <V, E> boolean isAnUpperSemilattice(DirectedAcyclicGraph<V, E> dag) {
		if (dag.vertexSet().isEmpty())
			return true; //sad, but true
		List<V> topoListOfVertices = new ArrayList<>();
		new TopologicalOrderIterator<V, E>(dag).forEachRemaining(v -> topoListOfVertices.add(v));
		for (int i = 0 ; i < topoListOfVertices.size() - 1; i++) {
			V iVertex = topoListOfVertices.get(i);
			Set<V> iUpperSet = dag.getDescendants(iVertex);
			iUpperSet.add(iVertex);
			for (int j = i + 1 ; j < topoListOfVertices.size() ; j++) {
				V jVertex = topoListOfVertices.get(j);
				Set<V> jUpperSet = dag.getDescendants(jVertex);
				jUpperSet.add(jVertex);
				Set<V> ijUpperSet = new HashSet<>(Sets.intersection(iUpperSet, jUpperSet));
				if (ijUpperSet.isEmpty())
					//then {i,j} admits no upper bound, and dag is not an upper semilattice
					return false;
				V ijAlledgedSupremum = null;
				int vertexIdx = 0;
				while (ijAlledgedSupremum == null) {
					V testedVertex = topoListOfVertices.get(vertexIdx);
					if (ijUpperSet.contains(testedVertex))
						ijAlledgedSupremum = testedVertex;
					else vertexIdx++;
				}
				ijUpperSet.removeAll(dag.getDescendants(ijAlledgedSupremum));
				if (ijUpperSet.size() != 1)
					//then {i,j} upper set admits many minimal elements, and dag is not an upper semilattice. 
					return false;
			}
		}
		return true;
	}
	
	public static boolean isAnUpperSemilattice(SparseIntDirectedGraph directedGraph) {
		if (directedGraph.vertexSet().isEmpty())
			return true; //sad, but true
		SparseIntDirectedGraph transitiveDirectedGraph;
		if (isTransitive(directedGraph))
			transitiveDirectedGraph = directedGraph;
		else transitiveDirectedGraph = getTransitiveClosure(directedGraph);
		List<Integer> topoListOfVertices = new ArrayList<>();
		new TopologicalOrderIterator<>(transitiveDirectedGraph).forEachRemaining(v -> topoListOfVertices.add(v));
		for (int i = 0 ; i < topoListOfVertices.size() - 1 ; i++) {
			Integer iVertex = topoListOfVertices.get(i);
			//since relation is transitive, strict upper bounds are all direct successors
			Set<Integer> iUpperSet = new HashSet<>(Graphs.successorListOf(transitiveDirectedGraph, iVertex));
			iUpperSet.add(iVertex);
			for (int j = i + 1 ; j < topoListOfVertices.size() ; j++) {
				Integer jVertex = topoListOfVertices.get(j);
				Set<Integer> jUpperSet = new HashSet<>(Graphs.successorListOf(transitiveDirectedGraph, jVertex));
				jUpperSet.add(jVertex);
				Set<Integer> ijUpperSet = new HashSet<>(Sets.intersection(iUpperSet, jUpperSet));
				if (ijUpperSet.isEmpty())
					//then {i,j} admits no upper bound, and dag is not an upper semilattice
					return false;
				Integer ijAlledgedSupremum = null;
				int vertexIdx = 0;
				while (ijAlledgedSupremum == null) {
					Integer textedVertex = topoListOfVertices.get(vertexIdx);
					if (ijUpperSet.contains(textedVertex)) {
						ijAlledgedSupremum = textedVertex;
					}
					else vertexIdx++;
				}
				ijUpperSet.removeAll(Graphs.successorListOf(transitiveDirectedGraph, ijAlledgedSupremum));
				if (ijUpperSet.size() != 1)
					//then {i,j} upper set admits many minimal elements, and dag is not an upper semilattice. 
					return false;
			}
		}
		return true;
	}
	
	public static <V, E> boolean isARootedInvertedDirectedAcyclicGraph(DirectedAcyclicGraph<V, E> dag) {
		//directed and acyclic : guaranteed by the parameter type
		//if directed, acyclic and has only 1 vertex of out degree 0, then rooted and inverted
		V root = null;
		for (V vertex : dag.vertexSet()) {
			if (dag.outDegreeOf(vertex) == 0) {
				if (root == null)
					root = vertex;
				else return false;
			}
		}
		return true;
	}
	
	public static boolean isTransitive(SparseIntDirectedGraph directedGraph) {
		Set<Integer> edges = directedGraph.edgeSet();
		for (Integer edge : edges) {
			Integer source = directedGraph.getEdgeSource(edge);
			Integer target = directedGraph.getEdgeTarget(edge);
			for (Integer other : edges) {
				if (directedGraph.getEdgeSource(other).equals(target)) {
					Integer otherTarget = directedGraph.getEdgeTarget(other);
					if (!directedGraph.containsEdge(source, otherTarget))
						return false;
				}
			}
		}
		return true;
	}
	
	public static boolean isRooted(SparseIntDirectedGraph directedGraph) {
		int nbOfMaximalElements = 0;
		for (Integer vertex : directedGraph.vertexSet()) {
			if (directedGraph.outDegreeOf(vertex) == 0)
				nbOfMaximalElements++;
		}
		return nbOfMaximalElements == 1;
	}
	
	//public for test use
	public static SparseIntDirectedGraph getTransitiveClosure(SparseIntDirectedGraph directedGraph) {
		int nbOfVertices = directedGraph.vertexSet().size();
		List<Set<Integer>> strictUpperSets = new ArrayList<>(nbOfVertices);
		for (int i = 0 ; i < nbOfVertices ; i++) {
			strictUpperSets.add(null);
		}
		List<Integer> topoListOfVertices = new ArrayList<>();
		new TopologicalOrderIterator<Integer, Integer>(directedGraph).forEachRemaining(v -> topoListOfVertices.add(v));
		for (int i = nbOfVertices - 1 ; i >= 0 ; i--) {
			Integer iVertex = topoListOfVertices.get(i);
			Set<Integer> iVertexStrictUpperBounds = new HashSet<>();
			List<Integer> iSuccessors = Graphs.successorListOf(directedGraph, iVertex);
			iVertexStrictUpperBounds.addAll(iSuccessors);
			for (Integer iSuccessor : iSuccessors) {
				iVertexStrictUpperBounds.addAll(strictUpperSets.get(topoListOfVertices.indexOf(iSuccessor)));
			}
			strictUpperSets.set(i, iVertexStrictUpperBounds);
		}
		List<Pair<Integer, Integer>> edgesInTransitiveGraph = new ArrayList<>();
		for (int i = 0 ; i < nbOfVertices ; i++) {
			Integer source = topoListOfVertices.get(i);
			for (Integer target : strictUpperSets.get(i)) {
				edgesInTransitiveGraph.add(new Pair<>(source, target));
			}
		}
		return new SparseIntDirectedGraph(nbOfVertices, edgesInTransitiveGraph);
	}

}
