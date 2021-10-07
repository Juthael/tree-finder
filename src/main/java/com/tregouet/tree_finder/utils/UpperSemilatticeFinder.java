package com.tregouet.tree_finder.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.error.InvalidSemilatticeException;

public class UpperSemilatticeFinder<V, E> implements Iterator<DirectedAcyclicGraph<V, E>> {

	private final DirectedAcyclicGraph<V, E> rootedInvertedDAG;
	private final List<V> vertexSet;
	private final int setCardinal;
	private final List<Set<V>> uSLSupGeneratorMinimals = new ArrayList<>();
	private final int[][] uSLSupremaCoords;
	private final int[] uSLSupremaArrayDimensions;
	private final int[] coordinates;
	private boolean hasNext = true;
	private int coordinateIdx = 0;
	private boolean dAGisReduced = true;
	
	/*
	 * Unsafe. Parameter MUST be the transitive reduction of a rooted inverted directed acyclic graph, 
	 * and ascending order over vertices MUST be topological.  
	 */
	public UpperSemilatticeFinder(DirectedAcyclicGraph<V, E> rootedInvertedDAGReduced, Set<V> minimals) {
		this.rootedInvertedDAG = rootedInvertedDAGReduced;
		vertexSet = new ArrayList<>();
		new TopologicalOrderIterator<V, E>(rootedInvertedDAGReduced).forEachRemaining(v -> vertexSet.add(v));
		setCardinal = rootedInvertedDAGReduced.vertexSet().size();
		Map<V, Set<V>> elmtToMinimalLowerBounds = new HashMap<>();
		for (V element : vertexSet) {
			if (minimals.contains(element)) {
				Set<V> singleton = new HashSet<>();
				singleton.add(element);
				elmtToMinimalLowerBounds.put(element, singleton);
			}
			else {
				Set<V> iMinimalLowerBounds = new HashSet<>();
				for (V predecessor : Graphs.predecessorListOf(rootedInvertedDAGReduced, element))
					iMinimalLowerBounds.addAll(elmtToMinimalLowerBounds.get(predecessor));
				elmtToMinimalLowerBounds.put(element, iMinimalLowerBounds);
			}
		}
		int[] uSLSupremaOversizedDimensionArray = new int[elmtToMinimalLowerBounds.size()];
		int subsetIndex;
		for (Set<V> minimalSubset : elmtToMinimalLowerBounds.values()) {
			subsetIndex = uSLSupGeneratorMinimals.indexOf(minimalSubset);
			if (subsetIndex == -1) {
				uSLSupGeneratorMinimals.add(minimalSubset);
				subsetIndex = uSLSupGeneratorMinimals.size() - 1;
			}
			uSLSupremaOversizedDimensionArray[subsetIndex]++;
		}
		uSLSupremaArrayDimensions =
				Arrays.copyOfRange(uSLSupremaOversizedDimensionArray, 0, uSLSupGeneratorMinimals.size());
		coordinates = new int[uSLSupGeneratorMinimals.size()]; 
		uSLSupremaCoords = new int[uSLSupGeneratorMinimals.size()][];
		for (int i = 0 ; i < uSLSupremaCoords.length ; i++) {
			uSLSupremaCoords[i] = new int[uSLSupremaArrayDimensions[i]];
		}
		for (int i = 0 ; i < setCardinal ; i++) {
			int uSLSupremumCoord = uSLSupGeneratorMinimals.indexOf(elmtToMinimalLowerBounds.get(i));
			uSLSupremaCoords[uSLSupremumCoord][coordinates[uSLSupremumCoord]] = i;
			coordinates[uSLSupremumCoord]++;
		}
		Arrays.fill(coordinates, 0);
	}

	//For test use. Same as advance(), with free parameters. 
	public static boolean advance(int[] coordinates, int[]arrayDimensions, int coordinateIdx) {
		if (coordinates[coordinateIdx] < arrayDimensions[coordinateIdx] - 1) {
			coordinates[coordinateIdx]++;
			coordinateIdx = 0;
			return true;
		}
		if (coordinateIdx == coordinates.length - 1)
			return false;
		coordinateIdx++;
		Arrays.fill(coordinates,  0, coordinateIdx, 0);
		return advance(coordinates, arrayDimensions, coordinateIdx);
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}
		
	public void initialize() {
		Arrays.fill(coordinates, 0);
		coordinateIdx = 0;
	}
	
	/*
	 * Returns a closed USL
	 */
	@Override
	public DirectedAcyclicGraph<V, E> next() {
		if (dAGisReduced)
			closeDAG();
		DirectedAcyclicGraph<V, E> nextUSL = new DirectedAcyclicGraph<>(null, null, false);
		List<V> nextUSLVertices = new ArrayList<>();
		List<E> nextUSLEdges = new ArrayList<>();
		for (int i = 0 ; i < coordinates.length ; i++)
			nextUSLVertices.add(vertexSet.get(uSLSupremaCoords[i][coordinates[i]]));
		for (E edge : rootedInvertedDAG.edgeSet()) {
			if (nextUSLVertices.contains(rootedInvertedDAG.getEdgeSource(edge)) 
					&& nextUSLVertices.contains(rootedInvertedDAG.getEdgeTarget(edge)))
				nextUSLEdges.add(edge);
		}
		Graphs.addAllEdges(nextUSL, rootedInvertedDAG, nextUSLEdges);
		hasNext = advance();
		return nextUSL;
	}
	
	public void validateNext() throws InvalidSemilatticeException {
		if (!StructureInspector.isAnUpperSemilattice(next()))
			throw new InvalidSemilatticeException();
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
	
	private void closeDAG() {
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(rootedInvertedDAG);
		dAGisReduced = false;
	}

}
