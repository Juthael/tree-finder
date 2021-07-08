package com.tregouet.tree_finder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.tregouet.tree_finder.data.InTree;

public interface ITreeFinder<V,E> extends Iterator<InTree<V,E>> {
	
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
	
	int getNbOfTrees();

}
