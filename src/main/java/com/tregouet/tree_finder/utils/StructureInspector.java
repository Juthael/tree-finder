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
	
	public static <V, E> boolean isAClassificationTree(DirectedAcyclicGraph<V, E> dag) {
		boolean isATree = true;
		List<V> topoElements = new ArrayList<>();
		new TopologicalOrderIterator<>(dag).forEachRemaining(e -> topoElements.add(e));
		Set<V> outDegree0 = topoElements.stream().filter(e -> dag.outDegreeOf(e) == 0).collect(Collectors.toSet());
		//hierarchy clause n°1
		if (outDegree0.size() != 1)
			isATree = false;
		List<Set<V>> lowerSets = new ArrayList<>(topoElements.size());
		for (V iElement : topoElements) {
			Set<V> iLowerSet = new HashSet<>();
			iLowerSet.add(iElement);
			//more efficient if the graph is reduced, but still valid otherwise.
			for (E incomingEdge : dag.incomingEdgesOf(iElement))
				iLowerSet.addAll(lowerSets.get(topoElements.indexOf(dag.getEdgeSource(incomingEdge))));
			lowerSets.add(iLowerSet);
		}
		for (int j = 0 ; j < topoElements.size() - 1 ; j++) {
			Set<V> jLowerSet = lowerSets.get(j);
			for (int k = j + 1 ; k < topoElements.size() ; k++) {
				Set<V> kLowerSet = lowerSets.get(k);
				Set<V> intersection = new HashSet<>(Sets.intersection(jLowerSet, kLowerSet));
				//hierarchy clause n°2
				if (!intersection.isEmpty() && !intersection.equals(jLowerSet) && !intersection.equals(kLowerSet))
					isATree = false;
			}
		}
		return isATree;
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
	
	public static <V, E> boolean isAtomistic(DirectedAcyclicGraph<V, E> dag) {
		Set<V> atoms = new HashSet<>();
		Set<Set<V>> encodingSubsetsOfAtoms = new HashSet<>();
		TopologicalOrderIterator<V, E> topoIte = new TopologicalOrderIterator<>(dag);
		while (topoIte.hasNext()) {
			V nextElem = topoIte.next();
			if (dag.inDegreeOf(nextElem) == 0) {
				atoms.add(nextElem);
				Set<V> singleton = new HashSet<>();
				encodingSubsetsOfAtoms.add(singleton);
			}
			else {
				Set<V> encodingSubsetOfAtoms = new HashSet<>(Sets.intersection(dag.getAncestors(nextElem), atoms));
				if (!encodingSubsetsOfAtoms.add(encodingSubsetOfAtoms))
					return false;	
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
	
	public static <V, E> boolean isTransitive(DirectedAcyclicGraph<V, E> directedGraph) {
		Set<E> edges = directedGraph.edgeSet();
		for (E edge : edges) {
			V a = directedGraph.getEdgeSource(edge);
			V b = directedGraph.getEdgeTarget(edge);
			for (E other : edges) {
				if (directedGraph.getEdgeSource(other).equals(b)) {
					V c = directedGraph.getEdgeTarget(other);
					if (!directedGraph.containsEdge(a, c))
						return false;
				}
			}
		}
		return true;
	}
	
	public static boolean isTransitive(SparseIntDirectedGraph directedGraph) {
		Set<Integer> edges = directedGraph.edgeSet();
		for (Integer edge : edges) {
			Integer a = directedGraph.getEdgeSource(edge);
			Integer b = directedGraph.getEdgeTarget(edge);
			for (Integer other : edges) {
				if (directedGraph.getEdgeSource(other).equals(b)) {
					Integer c = directedGraph.getEdgeTarget(other);
					if (!directedGraph.containsEdge(a, c))
						return false;
				}
			}
		}
		return true;
	}

}
