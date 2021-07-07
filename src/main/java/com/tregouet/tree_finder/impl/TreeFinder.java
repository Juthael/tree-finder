package com.tregouet.tree_finder.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.utils.CoordAdvancer;
import com.tregouet.tree_finder.utils.NArrayBool;

public class TreeFinder<V, E> implements ITreeFinder<V, E> {

	private final SimpleDirectedGraph<V, E> upperSemiLattice;
	private final List<V> sortedVertices = new ArrayList<V>();
	private V root;
	private final List<V> sortedLeaves = new ArrayList<V>();
	//paths in ith list start from the ith leaf
	private final List<List<GraphPath<V,E>>> listsOfPaths = new ArrayList<>();
	private final int[] intersArrayDimensions;
	private final NArrayBool intersectionArray;
	private final int[] coords;
	private SimpleDirectedGraph<V,E> nextTree = null;
	
	
	public TreeFinder(SimpleDirectedGraph<V, E> upperSemiLattice, boolean validateArg) {
		this.upperSemiLattice = upperSemiLattice;
		TopologicalOrderIterator<V, E> verticesSorter = new TopologicalOrderIterator<V,E>(upperSemiLattice);
		verticesSorter.forEachRemaining(sortedVertices::add);
		for (V vertex : sortedVertices) {
			if (upperSemiLattice.inDegreeOf(vertex) == 0)
				sortedLeaves.add(vertex);
			else if (upperSemiLattice.outDegreeOf(vertex) == 0)
				root = vertex;
		}
		AllDirectedPaths<V, E> pathFinder = new AllDirectedPaths<>(upperSemiLattice);
		for (V leaf : sortedLeaves) {
			listsOfPaths.add(pathFinder.getAllPaths(leaf, root, true, null));
		}
		intersArrayDimensions = new int[sortedLeaves.size()];
		for (int i = 0 ; i < sortedLeaves.size() ; i++) {
			intersArrayDimensions[i] = listsOfPaths.get(i).size();
		}
		intersectionArray = new NArrayBool(intersArrayDimensions);
		setIntersectionArray();
		coords = new int[sortedLeaves.size()];
		engage();
	}

	@Override
	public boolean hasNext() {
		return (nextTree != null);
	}

	@Override
	public SimpleDirectedGraph<V, E> next() {
		SimpleDirectedGraph<V, E> returned = nextTree;
		SimpleDirectedGraph<V, E> newTree;
		boolean nextTreeFound = false;
		do {
			if (intersectionArray.get(coords) == false) {
				newTree = new SimpleDirectedGraph<>(null, null, false);
				Set<E> edges = new HashSet<>();
				for (int i = 0 ; i < coords.length ; i++) {
					edges.addAll(listsOfPaths.get(i).get(coords[i]).getEdgeList());
				}
				Graphs.addAllEdges(newTree, upperSemiLattice, edges);
				nextTree = newTree;
				nextTreeFound = true;
			}
		}
		while (!nextTreeFound && CoordAdvancer.advance(coords, intersArrayDimensions));
		if (!nextTreeFound)
			nextTree = null;
		return returned;
	}
	
	private void engage() {
		next();
	}
	
	private final void setIntersectionArray() {
		for (int path1ListIdx = 0 ; path1ListIdx < listsOfPaths.size() - 1 ; path1ListIdx++) {
			for (int path1Idx = 0 ; path1Idx < listsOfPaths.get(path1ListIdx).size() ; path1Idx++) {
				for (int path2ListIdx = path1ListIdx + 1 ; path2ListIdx < listsOfPaths.size() ; path2ListIdx++) {
					for (int path2Idx = 0 ; path2Idx < listsOfPaths.get(path2ListIdx).size() ; path2Idx++) {
						GraphPath<V, E> path1 = listsOfPaths.get(path1ListIdx).get(path1Idx);
						GraphPath<V, E> path2 = listsOfPaths.get(path2ListIdx).get(path2Idx);
						if (lateIntersectionFound(path1, path2)) {
							int[] closedAreaInitialCoord = new int[listsOfPaths.size()];
							closedAreaInitialCoord[path1ListIdx] = path1Idx;
							closedAreaInitialCoord[path2ListIdx] = path2Idx;
							do {
								intersectionArray.set(closedAreaInitialCoord, true);
							}
							while (CoordAdvancer.advanceInSpecifiedArea(
									closedAreaInitialCoord, intersArrayDimensions, path1ListIdx, path2ListIdx));
						}
					}					
				}
			}
		}
	}
	
	public static <V,E> boolean lateIntersectionFound(GraphPath<V, E> path1, GraphPath<V, E> path2) {
		boolean lateIntersection = false;
		List<V> path1Vertices = path1.getVertexList();
		int path1NbOfVert = path1Vertices.size();
		Iterator<V> path2Ite = path2.getVertexList().iterator();
		int path1Idx = 0;
		while (path2Ite.hasNext() 
				&& path1Vertices.get(path1Idx).equals(path2Ite.next())) {
			path1Idx++;
		}
		while (path2Ite.hasNext() && !lateIntersection) {
			lateIntersection = path1Vertices.subList(path1Idx, path1NbOfVert).contains(path2Ite.next());
		}
		return lateIntersection;
	}
}
