package com.tregouet.tree_finder.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.data.RootedInverted;

public class StructureInspector {

	private StructureInspector() {
	}
	
	//public for test use
	public static SparseIntDirectedGraph getTransitiveClosure(SparseIntDirectedGraph directedGraph) {
		int nbOfElements = directedGraph.vertexSet().size();
		List<Set<Integer>> strictUpperSets = new ArrayList<>(nbOfElements);
		for (int i = 0 ; i < nbOfElements ; i++) {
			strictUpperSets.add(null);
		}
		List<Integer> topoOrderedSet = new ArrayList<>();
		new TopologicalOrderIterator<Integer, Integer>(directedGraph).forEachRemaining(v -> topoOrderedSet.add(v));
		for (int i = nbOfElements - 1 ; i >= 0 ; i--) {
			Integer iElement = topoOrderedSet.get(i);
			Set<Integer> iStrictUpperSet = new HashSet<>();
			List<Integer> iSuccessors = Graphs.successorListOf(directedGraph, iElement);
			iStrictUpperSet.addAll(iSuccessors);
			for (Integer iSuccessor : iSuccessors) {
				iStrictUpperSet.addAll(strictUpperSets.get(topoOrderedSet.indexOf(iSuccessor)));
			}
			strictUpperSets.set(i, iStrictUpperSet);
		}
		List<Pair<Integer, Integer>> edgesInTransitiveGraph = new ArrayList<>();
		for (int i = 0 ; i < nbOfElements ; i++) {
			Integer source = topoOrderedSet.get(i);
			for (Integer target : strictUpperSets.get(i)) {
				edgesInTransitiveGraph.add(new Pair<>(source, target));
			}
		}
		return new SparseIntDirectedGraph(nbOfElements, edgesInTransitiveGraph);
	}
	
	public static <G, V, E> boolean isALowerSemilattice(DirectedAcyclicGraph<V, E> dag) {
		if (dag.vertexSet().isEmpty())
			return true; //sad, but true
		List<V> topoOrderedSet = new ArrayList<>();
		new TopologicalOrderIterator<V, E>(dag).forEachRemaining(v -> topoOrderedSet.add(v));
		for (int i = 0 ; i < topoOrderedSet.size() - 1; i++) {
			V iElement = topoOrderedSet.get(i);
			Set<V> iLowerSet = dag.getAncestors(iElement);
			iLowerSet.add(iElement);
			for (int j = i + 1 ; j < topoOrderedSet.size() ; j++) {
				V jElement = topoOrderedSet.get(j);
				Set<V> jLowerSet = dag.getAncestors(jElement);
				jLowerSet.add(jElement);
				Set<V> ijLowerSet = new HashSet<>(Sets.intersection(iLowerSet, jLowerSet));
				if (ijLowerSet.isEmpty())
					//then {i,j} admits no upper bound, and dag is not an upper semilattice
					return false;
				V ijAlledgedInfimum = null;
				int elementIdx = topoOrderedSet.size() - 1;
				while (ijAlledgedInfimum == null) {
					V testedElement = topoOrderedSet.get(elementIdx);
					if (ijLowerSet.contains(testedElement))
						ijAlledgedInfimum = testedElement;
					else elementIdx--;
				}
				ijLowerSet.removeAll(dag.getAncestors(ijAlledgedInfimum));
				if (ijLowerSet.size() != 1)
					//then {i,j} upper set admits many minimal elements, and dag is not an upper semilattice. 
					return false;
			}
		}
		return true;
	}
	
	public static <G, V, E> boolean isAnUpperSemilattice(DirectedAcyclicGraph<V, E> dag) {
		if (dag.vertexSet().isEmpty())
			return true; //sad, but true
		List<V> topoOrderedSet = new ArrayList<>();
		new TopologicalOrderIterator<V, E>(dag).forEachRemaining(v -> topoOrderedSet.add(v));
		for (int i = 0 ; i < topoOrderedSet.size() - 1; i++) {
			V iElement = topoOrderedSet.get(i);
			Set<V> iUpperSet = dag.getDescendants(iElement);
			iUpperSet.add(iElement);
			for (int j = i + 1 ; j < topoOrderedSet.size() ; j++) {
				V jElement = topoOrderedSet.get(j);
				Set<V> jUpperSet = dag.getDescendants(jElement);
				jUpperSet.add(jElement);
				Set<V> ijUpperSet = new HashSet<>(Sets.intersection(iUpperSet, jUpperSet));
				if (ijUpperSet.isEmpty())
					//then {i,j} admits no upper bound, and dag is not an upper semilattice
					return false;
				V ijAlledgedSupremum = null;
				int elementIdx = 0;
				while (ijAlledgedSupremum == null) {
					V testedElement = topoOrderedSet.get(elementIdx);
					if (ijUpperSet.contains(testedElement))
						ijAlledgedSupremum = testedElement;
					else elementIdx++;
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
		List<Integer> topoOrderedSet = new ArrayList<>();
		new TopologicalOrderIterator<>(transitiveDirectedGraph).forEachRemaining(v -> topoOrderedSet.add(v));
		for (int i = 0 ; i < topoOrderedSet.size() - 1 ; i++) {
			Integer iElement = topoOrderedSet.get(i);
			//since relation is transitive, strict upper bounds are all direct successors
			Set<Integer> iUpperSet = new HashSet<>(Graphs.successorListOf(transitiveDirectedGraph, iElement));
			iUpperSet.add(iElement);
			for (int j = i + 1 ; j < topoOrderedSet.size() ; j++) {
				Integer jElement = topoOrderedSet.get(j);
				Set<Integer> jUpperSet = new HashSet<>(Graphs.successorListOf(transitiveDirectedGraph, jElement));
				jUpperSet.add(jElement);
				Set<Integer> ijUpperSet = new HashSet<>(Sets.intersection(iUpperSet, jUpperSet));
				if (ijUpperSet.isEmpty())
					//then {i,j} admits no upper bound, and dag is not an upper semilattice
					return false;
				Integer ijAlledgedSupremum = null;
				int elementIdx = 0;
				while (ijAlledgedSupremum == null) {
					Integer tetedElement = topoOrderedSet.get(elementIdx);
					if (ijUpperSet.contains(tetedElement)) {
						ijAlledgedSupremum = tetedElement;
					}
					else elementIdx++;
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
		for (V element : dag.vertexSet()) {
			if (dag.outDegreeOf(element) == 0) {
				if (root == null)
					root = element;
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
	
	public static <V, E> boolean isATree(DirectedAcyclicGraph<V, E> dag) {
		boolean isATree = true;
		if (dag.vertexSet().size() == 1)
			return isATree;
		List<V> topoOrderedSet = new ArrayList<>();
		new TopologicalOrderIterator<>(dag).forEachRemaining(e -> topoOrderedSet.add(e));
		if (!(dag instanceof RootedInverted<?, ?>)) {
			Set<V> outDegree0 = new HashSet<>();
			for (V element : topoOrderedSet) {
				if (dag.outDegreeOf(element) == 0)
					outDegree0.add(element);
			}
			//hierarchy clause n°1
			if (outDegree0.size() != 1)
				isATree = false;	
		}
		List<Set<V>> lowerSets = new ArrayList<>(topoOrderedSet.size());
		for (V iElement : topoOrderedSet) {
			Set<V> iLowerSet = new HashSet<>();
			iLowerSet.add(iElement);
			//more efficient if the graph is reduced, but still valid otherwise.
			for (E incomingEdge : dag.incomingEdgesOf(iElement))
				iLowerSet.addAll(lowerSets.get(topoOrderedSet.indexOf(dag.getEdgeSource(incomingEdge))));
			lowerSets.add(iLowerSet);
		}
		for (int j = 0 ; j < topoOrderedSet.size() - 1 ; j++) {
			Set<V> jLowerSet = lowerSets.get(j);
			for (int k = j + 1 ; k < topoOrderedSet.size() ; k++) {
				Set<V> kLowerSet = lowerSets.get(k);
				Set<V> intersection = new HashSet<>(Sets.intersection(jLowerSet, kLowerSet));
				//hierarchy clause n°2
				if (!intersection.isEmpty() && !intersection.equals(jLowerSet) && !intersection.equals(kLowerSet))
					isATree = false;
			}
		}
		return isATree;
	}
	
	public static boolean isRooted(SparseIntDirectedGraph directedGraph) {
		Integer root = null;
		for (Integer element : directedGraph.vertexSet()) {
			if (directedGraph.outDegreeOf(element) == 0) {
				if (root == null)
					root = element;
				else return false;
			}
		}
		return true;
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
