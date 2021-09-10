package com.tregouet.tree_finder.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.api.hyperdrive.Coord;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.InTree;
import com.tregouet.tree_finder.error.InvalidSemiLatticeException;
import com.tregouet.tree_finder.utils.CoordAdvancer;
import com.tregouet.tree_finder.utils.NArrayBool;

public class TreeFinder<V, E> implements ITreeFinder<V, E> {

	private final DirectedAcyclicGraph<V, E> upperSemiLattice;
	private final List<V> sortedVertices = new ArrayList<V>();
	private V root;
	private final List<V> leaves = new ArrayList<V>();
	//paths in ith list start from the ith leaf in the list of leaves
	private final List<List<GraphPath<V,E>>> listsOfPaths = new ArrayList<>();
	private final int[] intersArrayDimensions;
	private final NArrayBool intersectionArray;
	private final int[] coords;
	private boolean lastCoordReached = false;
	private InTree<V,E> nextTree = null;
	
	//Unsafe
	public TreeFinder(DirectedAcyclicGraph<V, E> rootedInvertedDAG) {
		this.upperSemiLattice = rootedInvertedDAG;
		TopologicalOrderIterator<V, E> verticesSorter = new TopologicalOrderIterator<V,E>(rootedInvertedDAG);
		verticesSorter.forEachRemaining(sortedVertices::add);
		for (V vertex : sortedVertices) {
			if (rootedInvertedDAG.inDegreeOf(vertex) == 0)
				leaves.add(vertex);
			if (rootedInvertedDAG.outDegreeOf(vertex) == 0)
				root = vertex;
		}
		AllDirectedPaths<V, E> pathFinder = new AllDirectedPaths<>(rootedInvertedDAG);
		for (V leaf : leaves) {
			listsOfPaths.add(pathFinder.getAllPaths(leaf, root, true, null));
		}
		intersArrayDimensions = new int[leaves.size()];
		for (int i = 0 ; i < leaves.size() ; i++) {
			intersArrayDimensions[i] = listsOfPaths.get(i).size();
		}
		intersectionArray = new NArrayBool(intersArrayDimensions);
		setIntersectionArray();
		coords = new int[leaves.size()];
		engage();
	}

	//Safe if 2nd argument is 'true'
	public TreeFinder(DirectedAcyclicGraph<V, E> rootedInvertedDAG, boolean validateArg) 
			throws InvalidSemiLatticeException {
		this(rootedInvertedDAG);
		if (validateArg && !ITreeFinder.isRootedInvertedDirectedAcyclicGraph(rootedInvertedDAG))
			throw new InvalidSemiLatticeException("TreeFinder constructor : argument is not an "
						+ "upper semilattice.");
	}

	@Override
	public int getNbOfTrees() {
		int nbOfTrees = 0;
		int[] coords = new int[intersArrayDimensions.length];
		do {
			if (intersectionArray.get(coords) == false)
				nbOfTrees++;
		}
		while (Coord.advance(coords, intersArrayDimensions));
		return nbOfTrees;
	}
	
	@Override
	public boolean hasNext() {
		return (nextTree != null);
	}
	
	@Override
	public InTree<V, E> next() {
		InTree<V, E> returned = nextTree;
		if (lastCoordReached)
			nextTree = null;
		else {
			InTree<V, E> newTree;
			boolean newTreeFound = false;
			do {
				if (intersectionArray.get(coords) == false) {
					Set<E> newTreeEdges = new HashSet<>();
					for (int i = 0 ; i < coords.length ; i++) {
						newTreeEdges.addAll(listsOfPaths.get(i).get(coords[i]).getEdgeList());
					}
					newTree = new InTree<>(root, leaves, upperSemiLattice, newTreeEdges);
					nextTree = newTree;
					newTreeFound = true;
				}
				lastCoordReached = !Coord.advance(coords, intersArrayDimensions);
			}
			while (!newTreeFound && !lastCoordReached);
		}
		return returned;
	}
	
	private boolean earlyIntersectionFound(GraphPath<V, E> path1, GraphPath<V, E> path2) {
		if (path1.getVertexList().isEmpty() || path2.getVertexList().isEmpty()) {
			//should not happen
			return false;
		}
		List<V> path1VertexList = path1.getVertexList();
		List<V> path2VertexList = path2.getVertexList();
		int path1Idx = 0;
		int path2Idx = -1;
		while (path2Idx == -1 && path1Idx < path1VertexList.size()) {
			path2Idx = path2VertexList.indexOf(path1VertexList.get(path1Idx));
			if (path2Idx == -1)
				path1Idx++;
		}
		if (path2Idx == -1) {
			//should not happen
			return false;
		}
		return !path1VertexList.subList(path1Idx, path1VertexList.size())
				.equals(path2VertexList.subList(path2Idx, path2VertexList.size()));
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
						if (earlyIntersectionFound(path1, path2)) {
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
}
