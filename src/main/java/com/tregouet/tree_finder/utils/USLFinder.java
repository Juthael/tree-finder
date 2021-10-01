package com.tregouet.tree_finder.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;

import com.google.common.primitives.Ints;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;

public class USLFinder implements Iterator<SparseIntDirectedGraph> {

	private final SparseIntDirectedGraph rootedInvertedDAG;
	private final int setCardinal;
	private final IntArraySet minimals = new IntArraySet();
	private final List<IntArraySet> USLSupGeneratorMinimals = new ArrayList<>();
	private final int[][] USLSuprema;
	private final int[] USLSupremaDimensions;
	private final int[] coordinates;
	private int coordinatesIdx = 0;
	private boolean hasNext = true;
	
	//Unsafe. Parameter MUST be a rooted inverted Directed Acyclic Graph, otherwise behavior is undefined.
	public USLFinder(SparseIntDirectedGraph rootedInvertedDAG) {
		this.rootedInvertedDAG = rootedInvertedDAG;
		setCardinal = rootedInvertedDAG.vertexSet().size();
		List<IntArraySet> minimalLowerBounds = new ArrayList<>();
		for (int i = 0 ; i < setCardinal ; i++) {
			if (rootedInvertedDAG.inDegreeOf(i) == 0) {
				minimals.add(i);
				minimalLowerBounds.add(new IntArraySet(new int[] {i}));
			}
			else {
				IntArraySet iMinimalLowerBounds = new IntArraySet();
				for (Integer predecessor : Graphs.predecessorListOf(rootedInvertedDAG, i))
					iMinimalLowerBounds.addAll(minimalLowerBounds.get((int) predecessor));
				minimalLowerBounds.add(iMinimalLowerBounds);
			}
		}
		for (IntArraySet minimalSubset : minimalLowerBounds) {
			if (!USLSupGeneratorMinimals.contains(minimalSubset))
				USLSupGeneratorMinimals.add(minimalSubset);
		}
		USLSuprema = new int[USLSupGeneratorMinimals.size()][0];
		for (int i = 0 ; i < setCardinal ; i++) {
			int USLSupremumIndex = USLSupGeneratorMinimals.indexOf(minimalLowerBounds.get(i));
			if (USLSuprema[USLSupremumIndex].length == 0)
				USLSuprema[USLSupremumIndex] = new int[]{i};
			else USLSuprema[USLSupremumIndex] = Ints.concat(USLSuprema[USLSupremumIndex], new int[] {i});
		}
		USLSupremaDimensions = new int[USLSuprema.length];
		for (int i = 0 ; i < USLSuprema.length ; i++) {
			USLSupremaDimensions[i] = USLSuprema[i].length;
		}
		coordinates = new int[USLSuprema.length];
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public SparseIntDirectedGraph next() {
		SparseIntDirectedGraph nextUSL = new SparseIntDirectedGraph(0, new ArrayList<Pair<Integer, Integer>>());
		for (int i = 0 ; i < coordinates.length ; i++)
			nextUSL.addVertex(USLSuprema[i][coordinates[i]]);
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
		
	}

}
