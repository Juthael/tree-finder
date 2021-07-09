package com.tregouet.tree_finder.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.api.hyperdrive.Coord;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.InTree;
import com.tregouet.tree_finder.error.InvalidSemiLatticeExeption;
import com.tregouet.tree_finder.utils.CoordAdvancer;
import com.tregouet.tree_finder.utils.NArrayBool;

public class TreeFinder<V, E> implements ITreeFinder<V, E> {

	public static <V,E> boolean lateIntersectionFound(GraphPath<V, E> path1, GraphPath<V, E> path2) {
		boolean lateIntersection = false;
		List<V> path1Vertices = path1.getVertexList();
		int path1NbOfVert = path1Vertices.size();
		int path1Idx = 0;
		Iterator<V> path2Ite = path2.getVertexList().iterator();
		while (path2Ite.hasNext() 
				&& path1Vertices.get(path1Idx).equals(path2Ite.next())) {
			path1Idx++;
		}
		while (path2Ite.hasNext() && !lateIntersection) {
			lateIntersection = path1Vertices.subList(path1Idx, path1NbOfVert).contains(path2Ite.next());
		}
		return lateIntersection;
	}
	private final DirectedAcyclicGraph<V, E> upperSemiLattice;
	private final List<V> sortedVertices = new ArrayList<V>();
	private V root;
	private final List<V> sortedLeaves = new ArrayList<V>();
	//paths in ith list start from the ith leaf in the list of leaves
	private final List<List<GraphPath<V,E>>> listsOfPaths = new ArrayList<>();
	private final int[] intersArrayDimensions;
	private final NArrayBool intersectionArray;
	private final int[] coords;
	private InTree<V,E> nextTree = null;
	
	//Unsafe
	public TreeFinder(DirectedAcyclicGraph<V, E> upperSemiLattice) {
		this.upperSemiLattice = upperSemiLattice;
		TopologicalOrderIterator<V, E> verticesSorter = new TopologicalOrderIterator<V,E>(upperSemiLattice);
		verticesSorter.forEachRemaining(sortedVertices::add);
		for (V vertex : sortedVertices) {
			if (upperSemiLattice.inDegreeOf(vertex) == 0)
				sortedLeaves.add(vertex);
			if (upperSemiLattice.outDegreeOf(vertex) == 0)
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

	//Safe if 2nd argument is 'true'
	public TreeFinder(DirectedAcyclicGraph<V, E> upperSemiLattice, boolean validateArg) 
			throws InvalidSemiLatticeExeption {
		this(upperSemiLattice);
		if (validateArg && !thisIsAnUpperSemilattice())
			throw new InvalidSemiLatticeExeption("TreeFinder constructor : argument is not an "
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
		InTree<V, E> newTree;
		boolean newTreeFound = false;
		do {
			if (intersectionArray.get(coords) == false) {
				Set<E> newTreeEdges = new HashSet<>();
				for (int i = 0 ; i < coords.length ; i++) {
					newTreeEdges.addAll(listsOfPaths.get(i).get(coords[i]).getEdgeList());
				}
				newTree = new InTree<>(root, sortedLeaves, upperSemiLattice, newTreeEdges);
				nextTree = newTree;
				newTreeFound = true;
			}
		}
		while (Coord.advance(coords, intersArrayDimensions) && !newTreeFound);
		if (!newTreeFound)
			nextTree = null;
		return returned;
	}
	
	private boolean admitsASupremum(V vertex1, V vertex2, ConnectivityInspector<V, E> inspector) {
		//there can't be two distinct but equal vertices
		if (vertex1 == vertex2)
			return true;
		//if two elements are related, then the greatest is the supremum
		V firstVertex = (sortedVertices.indexOf(vertex1) < sortedVertices.indexOf(vertex2) ? vertex1 : vertex2);
		V secondVertex = ((firstVertex == vertex1) ? vertex2 : vertex1);
		if (inspector.pathExists(firstVertex, secondVertex))
			return true;
		//whether two elements are connected or not, their supremum is their least upper bound
		Set<V> upperSet = upperSemiLattice.getDescendants(vertex1);
		upperSet.removeAll(upperSemiLattice.getDescendants(vertex2));
		if (upperSet.isEmpty())
			return false;
		int nbOfMinimalElemInUpperSet = 0;
		for (V upperBound : upperSet) {
			List<V> upperBoundPrecInUpperSet = Graphs.predecessorListOf(upperSemiLattice, upperBound);
			upperBoundPrecInUpperSet.retainAll(upperSet);
			if (upperBoundPrecInUpperSet.isEmpty())
				nbOfMinimalElemInUpperSet++;
		}
		return (nbOfMinimalElemInUpperSet == 1);
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

	//if true, then every pair of elements admits a supremum
	private boolean thisIsAnUpperSemilattice() {
		if (sortedVertices.size() < 2)
			return true;
		boolean isAnUpperSL = true;
		ConnectivityInspector<V, E> inspector = new ConnectivityInspector<>(upperSemiLattice);
		int vertex1Idx = 0;
		while (isAnUpperSL && vertex1Idx < sortedVertices.size() - 1) {
			int vertex2Idx = vertex1Idx + 1;
			while (isAnUpperSL && vertex2Idx < sortedVertices.size()) {
				isAnUpperSL = 
						admitsASupremum(sortedVertices.get(vertex1Idx), sortedVertices.get(vertex2Idx), inspector);
				vertex2Idx++;
			}
			vertex1Idx++;
		}
		return isAnUpperSL;
	}
	
}
