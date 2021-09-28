package com.tregouet.tree_finder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.data.InTree;

public interface ITreeFinder<V,E> extends Iterator<InTree<V,E>> {
	
	public static <V, E> boolean isARootedInvertedDirectedAcyclicGraph(DirectedAcyclicGraph<V, E> dag) {
		//directed and acyclic : guaranteed by the argument type
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
	
	public static <V, E> boolean isAnInTree(DirectedAcyclicGraph<V, E> dag) {
		//An in-tree is directed and acyclic : guaranteed by the argument type
		//An in-tree is connected
		ConnectivityInspector<V, E> inspector = new ConnectivityInspector<>(dag);
		if (!inspector.isConnected())
			return false;
		//an in-tree has a root
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
		//an in-tree has only one path from any leaf to the root
		AllDirectedPaths<V, E> pathFinder = new AllDirectedPaths<>(dag);
		for (V leaf : leaves) {
			if (pathFinder.getAllPaths(leaf, root, true, null).size() != 1)
				return false;
		}
		return true;
	}
	
	public static <V, E> boolean isAnUpperSemiLattice(DirectedAcyclicGraph<V, E> dag) {
		if (dag.vertexSet().isEmpty())
			return true;
		List<V> topoListOfVertices = new ArrayList<>();
		new TopologicalOrderIterator<V, E>(dag).forEachRemaining(v -> topoListOfVertices.add(v));
		for (int i = 0 ; i < topoListOfVertices.size() - 1; i++) {
			V iVertex = topoListOfVertices.get(i);
			Set<V> iUpperBounds = dag.getDescendants(iVertex);
			iUpperBounds.add(iVertex);
			for (int j = i + 1 ; j < topoListOfVertices.size() ; j++) {
				V jVertex = topoListOfVertices.get(j);
				Set<V> commonUpperBounds = dag.getDescendants(jVertex);
				commonUpperBounds.add(jVertex);
				commonUpperBounds.retainAll(iUpperBounds);
				Iterator<V> commonUpperBoundsIte = commonUpperBounds.iterator();
				if (!commonUpperBoundsIte.hasNext())
					//then {i,j} admits no supremum (least upper bound), and dag is not an upper semilattice
					return false;
				V minimal = commonUpperBoundsIte.next();
				while (commonUpperBoundsIte.hasNext()) {
					V nextCommonUpperBound = commonUpperBoundsIte.next();
					if (!topoListOfVertices.subList(topoListOfVertices.indexOf(minimal), topoListOfVertices.size())
							.contains(nextCommonUpperBound)) {
						minimal = nextCommonUpperBound;
					}
				}
				commonUpperBounds.removeAll(dag.getDescendants(minimal));
				if (commonUpperBounds.size() != 1)
					//then {i,j} admits no supremum (least upper bound), and dag is not an upper semilattice
					return false;
			}
		}
		return true;
	}
	
	int getNbOfTrees();

}
