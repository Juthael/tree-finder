package com.tregouet.tree_finder.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class UpperSemiLatticeFinder implements Iterator<SparseIntDirectedGraph> {
	
	private final SparseIntDirectedGraph rootedInvertedDAG;
	private final int nbOfVertices;
	private final Set<Integer> leaves = new HashSet<>();
	private final List<IntArrayList> minimalUpperBounds = new ArrayList<>();
	private final int[] minimalUpperBoundsDimensions;
	private int[] coordinates;
	private int coordinateIdx = 0;
	private boolean hasNext = true;

	//Unsafe. Parameter MUST be a rooted inverted Directed Acyclic Graph, otherwise behavior is undefined.
	public UpperSemiLatticeFinder(SparseIntDirectedGraph rootedInvertedDAG) {
		this.rootedInvertedDAG = rootedInvertedDAG;
		nbOfVertices = rootedInvertedDAG.vertexSet().size();
		List<Set<Integer>> minimalLowerBounds = new ArrayList<>();
		while (topoIte.hasNext()) {
			Integer nextVertex = topoIte.next();
			Set<Integer> MinimalLowerBoundsForNext = new HashSet<>();
			if (rootedInvertedDAG.inDegreeOf(nextVertex) == 0) {
				leaves.add(nextVertex);
				MinimalLowerBoundsForNext.add(nextVertex);
			}
			else {
				for (Integer predecessor : Graphs.predecessorListOf(rootedInvertedDAG, nextVertex)) {
					MinimalLowerBoundsForNext.addAll(
							minimalLowerBounds.get(predecessor));
				}
			}
			minimalLowerBounds.add(MinimalLowerBoundsForNext);
		}
		boolean[] skipInspection = new boolean[verticesInTopologicalOrder.size()]; 
		for (int i = 0 ; i < verticesInTopologicalOrder.size() ; i++) {
			if (!skipInspection[i]) {
				Set<Integer> iSubsetOfLeaves = minimalLowerBounds.get(i);
				IntArrayList minimalUpperBound = new IntArrayList();
				minimalUpperBound.add((int) verticesInTopologicalOrder.get(i));
				for (int j = i + 1 ; j < verticesInTopologicalOrder.size() ; j++) {
					if (!skipInspection[j] 
							&& iSubsetOfLeaves.equals(
									minimalLowerBounds.get(j))) {
						minimalUpperBound.add((int) verticesInTopologicalOrder.get(j));
						skipInspection[j] = true;
					}
				}
				//REVOIR LES NOMS. VERIFIER ADVANCE. INITIALISER LE TABLEAU DIMENSIONS.
				minimalUpperBounds.add(minimalUpperBound);
			}
		}
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public SparseIntDirectedGraph next() {
		IntArrayList nextUSLVertices = new IntArrayList();
		for (int i = 0 ; i < coordinates.length ; i++)
			nextUSLVertices.add(minimalUpperBounds.get(i).getInt(coordinates[i]));
		List<Pair<Integer, Integer>> nextUSLEdges = new ArrayList<>();
		Integer edgeSource;
		Integer edgeTarget;
		for (Integer edge : rootedInvertedDAG.edgeSet()) {
			edgeSource = rootedInvertedDAG.getEdgeSource(edge);
			edgeTarget = rootedInvertedDAG.getEdgeTarget(edge);
			if (nextUSLVertices.contains((int) edgeSource)
					&& nextUSLVertices.contains((int) edgeTarget)) {
				nextUSLEdges.add(new Pair<>(edgeSource, edgeTarget));
			}
		}
		advance();
		return new SparseIntDirectedGraph(nextUSLVertices.size(), nextUSLEdges);
	}
	
	private void advance() {
		if (coordinates[coordinateIdx] < minimalUpperBoundsDimensions[coordinateIdx] - 1) {
			coordinates[coordinateIdx]++;
			coordinateIdx = 0;
		}
		else if (coordinateIdx == coordinates.length - 1)
				hasNext = false;
		else {
			coordinateIdx++;
			resetCoordinatesBefore(coordinateIdx);
			advance();
		}
	}
	
	private void resetCoordinatesBefore(int coordinateIdx) {
		Arrays.fill(coordinates, 0, coordinateIdx, 0);
	}

}
