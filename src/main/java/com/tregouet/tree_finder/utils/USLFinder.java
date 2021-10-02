package com.tregouet.tree_finder.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;

import it.unimi.dsi.fastutil.ints.IntArraySet;

public class USLFinder implements Iterator<SparseIntDirectedGraph> {

	private final SparseIntDirectedGraph rootedInvertedDAG;
	private final int setCardinal;
	private final List<IntArraySet> uSLSupGeneratorMinimals = new ArrayList<>();
	private final int[][] uSLSuprema;
	private final int[] uSLSupremaArrayDimensions;
	private final int[] coordinates;
	private boolean hasNext = true;
	private int coordinateIdx = 0;
	
	/*
	 * Unsafe. Parameter MUST be a rooted inverted directed acyclic graph, and ascending order 
	 * over vertices MUST be topological.  
	 */
	public USLFinder(SparseIntDirectedGraph rootedInvertedDAG, IntArraySet minimals) {
		this.rootedInvertedDAG = rootedInvertedDAG;
		setCardinal = rootedInvertedDAG.vertexSet().size();
		List<IntArraySet> minimalLowerBounds = new ArrayList<>();
		for (int i = 0 ; i < setCardinal ; i++) {
			if (minimals.contains(i))
				minimalLowerBounds.add(new IntArraySet(new int[] {i}));
			else {
				IntArraySet iMinimalLowerBounds = new IntArraySet();
				for (Integer predecessor : Graphs.predecessorListOf(rootedInvertedDAG, i))
					iMinimalLowerBounds.addAll(minimalLowerBounds.get((int) predecessor));
				minimalLowerBounds.add(iMinimalLowerBounds);
			}
		}
		int[] uSLSupremaArrayDimensionsOversized = new int[minimalLowerBounds.size()];
		int subsetIndex;
		for (IntArraySet minimalSubset : minimalLowerBounds) {
			subsetIndex = uSLSupGeneratorMinimals.indexOf(minimalSubset);
			if (subsetIndex == -1) {
				uSLSupGeneratorMinimals.add(minimalSubset);
				subsetIndex = uSLSupGeneratorMinimals.size() - 1;
			}
			uSLSupremaArrayDimensionsOversized[subsetIndex]++;
		}
		uSLSupremaArrayDimensions =
				Arrays.copyOfRange(uSLSupremaArrayDimensionsOversized, 0, uSLSupGeneratorMinimals.size());
		coordinates = new int[uSLSupGeneratorMinimals.size()]; 
		uSLSuprema = new int[uSLSupGeneratorMinimals.size()][];
		for (int i = 0 ; i < uSLSuprema.length ; i++) {
			uSLSuprema[i] = new int[uSLSupremaArrayDimensions[i]];
		}
		for (int i = 0 ; i < setCardinal ; i++) {
			int uSLSupremumIndex = uSLSupGeneratorMinimals.indexOf(minimalLowerBounds.get(i));
			uSLSuprema[uSLSupremumIndex][coordinates[uSLSupremumIndex]] = i;
			coordinates[uSLSupremumIndex]++;
		}
		Arrays.fill(coordinates, 0);
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public SparseIntDirectedGraph next() {
		SparseIntDirectedGraph nextUSL = new SparseIntDirectedGraph(0, new ArrayList<Pair<Integer, Integer>>());
		for (int i = 0 ; i < coordinates.length ; i++)
			nextUSL.addVertex(uSLSuprema[i][coordinates[i]]);
		for (Integer edge : rootedInvertedDAG.edgeSet()) {
			Integer edgeSource = rootedInvertedDAG.getEdgeSource(edge);
			Integer edgeTarget = rootedInvertedDAG.getEdgeTarget(edge);
			if (nextUSL.containsVertex(edgeSource) 
					&& nextUSL.containsVertex(edgeTarget))
				nextUSL.addEdge(edgeSource, edgeSource, edge);
		}
		hasNext = advance();
		return nextUSL;
	}
		
	private boolean advance() {
		if (coordinates[coordinateIdx] < uSLSupremaArrayDimensions[coordinateIdx] - 1) {
			coordinates[coordinateIdx]++;
			coordinateIdx = 0;
			return true;
		}
		if (coordinateIdx == coordinates.length - 1)
			return false;
		coordinateIdx++;
		Arrays.fill(coordinates,  0, coordinateIdx, 0);
		return advance();
	}
	
	//For test use. Same as above, with free parameters. 
	public boolean advance(int[] coordinates, int[]arrayDimensions, int coordinateIdx) {
		if (coordinates[coordinateIdx] < arrayDimensions[coordinateIdx] - 1) {
			coordinates[coordinateIdx]++;
			coordinateIdx = 0;
			return true;
		}
		if (coordinateIdx == coordinates.length - 1)
			return false;
		coordinateIdx++;
		Arrays.fill(coordinates,  0, coordinateIdx, 0);
		return advance();
	}

}
