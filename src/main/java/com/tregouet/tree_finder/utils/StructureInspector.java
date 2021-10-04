package com.tregouet.tree_finder.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
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
			return true;
		List<V> topoListOfVertices = new ArrayList<>();
		new TopologicalOrderIterator<V, E>(dag).forEachRemaining(v -> topoListOfVertices.add(v));
		for (int i = 0 ; i < topoListOfVertices.size() - 1; i++) {
			V iVertex = topoListOfVertices.get(i);
			Set<V> iUpperSet = dag.getDescendants(iVertex);
			iUpperSet.add(iVertex);
			for (int j = i + 1 ; j < topoListOfVertices.size() ; j++) {
				V jVertex = topoListOfVertices.get(j);
				Set<V> ijUpperSet = dag.getDescendants(jVertex);
				ijUpperSet.add(jVertex);
				ijUpperSet.retainAll(iUpperSet);
				Iterator<V> ijUpperSetIte = ijUpperSet.iterator();
				if (!ijUpperSetIte.hasNext())
					//then {i,j} admits no upper bound, and dag is not an upper semilattice
					return false;
				V minimal = ijUpperSetIte.next();
				while (ijUpperSetIte.hasNext()) {
					V nextUpperBoundOfIJ = ijUpperSetIte.next();
					if (!topoListOfVertices.subList(topoListOfVertices.indexOf(minimal), topoListOfVertices.size())
							.contains(nextUpperBoundOfIJ))
						minimal = nextUpperBoundOfIJ;
				}
				ijUpperSet.removeAll(dag.getDescendants(minimal));
				if (ijUpperSet.size() != 1)
					//then {i,j} upper set admits many minimal elements, and dag is not an upper semilattice. 
					return false;
			}
		}
		return true;
	}
	
	public static boolean isAnUpperSemilattice(SparseIntDirectedGraph directedGraph) {
		if (directedGraph.vertexSet().isEmpty())
			return true;
		if (!isTransitive(directedGraph))
			return false;
		List<Integer> topoListOfVertices = new ArrayList<>();
		new TopologicalOrderIterator<>(directedGraph).forEachRemaining(v -> topoListOfVertices.add(v));
		for (int i = 0 ; i < topoListOfVertices.size() - 1 ; i++) {
			Integer iVertex = topoListOfVertices.get(i);
			//since relation is transitive, strict upper bounds bounds are successors
			Set<Integer> iUpperSet = new HashSet<>(Graphs.successorListOf(directedGraph, iVertex));
			iUpperSet.add(iVertex);
			for (int j = 0 ; j < topoListOfVertices.size() ; j++) {
				Integer jVertex = topoListOfVertices.get(j);
				Set<Integer> ijUpperSet = new HashSet<>(Graphs.successorListOf(directedGraph, jVertex));
				ijUpperSet.add(jVertex);
				ijUpperSet.retainAll(iUpperSet);
				Iterator<Integer> ijUpperSetIte = ijUpperSet.iterator();
				if (!ijUpperSetIte.hasNext())
					//then {i,j} admits no upper bound, and dag is not an upper semilattice
					return false;
				Integer minimal = ijUpperSetIte.next();
				while (ijUpperSetIte.hasNext()) {
					Integer nextUpperBoundOfIJ = ijUpperSetIte.next();
					if (!topoListOfVertices.subList(topoListOfVertices.indexOf(minimal), topoListOfVertices.size())
							.contains(nextUpperBoundOfIJ))
						minimal = nextUpperBoundOfIJ;
				}
				ijUpperSet.removeAll(Graphs.successorListOf(directedGraph, minimal));
				if (ijUpperSet.size() != 1)
					//then {i,j} upper set admits many minimal elements, and dag is not an upper semilattice. 
					return false;
			}
		}
		return true;
	}
	
	public static <V, E> boolean isARootedInvertedDirectedAcyclicGraph(DirectedAcyclicGraph<V, E> dag) {
		//directed and acyclic : guaranteed by the parameter type
		//connected
		ConnectivityInspector<V, E> inspector = new ConnectivityInspector<>(dag);
		if (!inspector.isConnected())
			return false;
		//rooted
		V root = null;
		List<V> leaves = new ArrayList<>();
		for (V vertex : dag.vertexSet()) {
			if (dag.outDegreeOf(vertex) == 0) {
				if (root == null)
					root = vertex;
				else return false;
			}
			if (dag.inDegreeOf(vertex) == 0)
				leaves.add(vertex);
		}
		//inverted : every edge is on one path at least from a leaf to the root
		Set<E> allEdges = dag.edgeSet();
		Set<E> pathEdges = new HashSet<>();
		AllDirectedPaths<V, E> pathFinder = new AllDirectedPaths<>(dag);
		for (V leaf : leaves) {
			List<GraphPath<V, E>> leafToRootPaths = pathFinder.getAllPaths(leaf, root, true, null); 
			for (GraphPath<V, E> leafToRootPath : leafToRootPaths)
				pathEdges.addAll(leafToRootPath.getEdgeList());
		}
		return allEdges.equals(pathEdges);
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
		List<Integer> outDegree0Vertices = directedGraph.edgeSet().stream()
				.filter(v -> directedGraph.outDegreeOf(v) == 0)
				.collect(Collectors.toList());
		if (outDegree0Vertices.size() != 1)
			return false;
		List<Integer> lowerBounds = Graphs.predecessorListOf(directedGraph, outDegree0Vertices.get(0));
		return (lowerBounds.size() == directedGraph.vertexSet().size() - 1);
	}

}
