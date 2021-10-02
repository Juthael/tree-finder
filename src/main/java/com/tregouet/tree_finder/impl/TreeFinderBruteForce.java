package com.tregouet.tree_finder.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.InTree;
import com.tregouet.tree_finder.error.InvalidSemiLatticeException;

public class TreeFinderBruteForce<V, E> implements ITreeFinder<V, E> {

	private final Set<V> minimals;
	private final Map<Set<V>, V> closedSubsetsOfMinimalsToTheirSupremum = new HashMap<>();
	private final Set<Set<Set<V>>> powerSetOfClosedSubsetsOfMinimals;
	private final Set<Set<Set<V>>> maximalHierarchiesOfMinimals;
	private final Set<Set<V>> treeVertexSets = new HashSet<>();
	
	public TreeFinderBruteForce(DirectedAcyclicGraph<V, E> upperSemilattice, boolean validate) 
			throws InvalidSemiLatticeException {
		if (validate && !ITreeFinder.isAnUpperSemiLattice(upperSemilattice))
			throw new InvalidSemiLatticeException();
		minimals = upperSemilattice.vertexSet().stream()
				.filter(v -> upperSemilattice.inDegreeOf(v) == 0)
				.collect(Collectors.toSet());
		upperSemilattice.vertexSet().stream()
			.forEach(v -> 
				closedSubsetsOfMinimalsToTheirSupremum.put(minimalLowerBounds(v, upperSemilattice), v));
		powerSetOfClosedSubsetsOfMinimals = powerSet(closedSubsetsOfMinimalsToTheirSupremum.keySet());
		maximalHierarchiesOfMinimals = powerSetOfClosedSubsetsOfMinimals.stream()
				.filter(s -> isAMaximalHierarchy(s))
				.collect(Collectors.toSet());
		for (Set<Set<V>> hierarchy : maximalHierarchiesOfMinimals) {
			Set<V> treeVertexSet = hierarchy.stream()
					.map(s -> closedSubsetsOfMinimalsToTheirSupremum.get(s))
					.collect(Collectors.toSet());
			treeVertexSets.add(treeVertexSet);
		}
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InTree<V, E> next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNbOfTrees() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private Set<V> minimalLowerBounds(V element, DirectedAcyclicGraph<V, E> upperSemilattice) {
		return Sets.intersection(minimals, upperSemilattice.getAncestors(element));
	}
	
	private Set<Set<Set<V>>> powerSet(Set<Set<V>> subsets) {
		List<Set<V>> subsetList = new ArrayList<>(subsets);
		int listSize = subsetList.size();
		Set<Set<Set<V>>> powerSetOfSubsets = new HashSet<>();
		for (int i = 0 ; i < (1 << listSize) ; i++) {
			Set<Set<V>> setOfSubsets = new HashSet<>(listSize);
			for (int j = 0 ; j < listSize ; j++) {
				if(((1 << j) & i) > 0) {
					setOfSubsets.add(subsetList.get(j));
				}
			}
			powerSetOfSubsets.add(setOfSubsets);
		}
		return powerSetOfSubsets;
	}
	
	private boolean isAMaximalHierarchy(Set<Set<V>> setOfMinimalSubsets) {
		return (coversEveryMinimal(setOfMinimalSubsets) 
				&& isAHierarchy(setOfMinimalSubsets) 
				&& isMaximal(setOfMinimalSubsets));
	}
	
	private boolean coversEveryMinimal(Set<Set<V>> setOfMinimalSubsets) {
		Set<V> covered = new HashSet<>();
		for (Set<V> subset : setOfMinimalSubsets) {
			for (V minimal : subset)
				covered.add(minimal);
		}
		return covered.equals(minimals);
	}
	
	private boolean isAHierarchy(Set<Set<V>> setOfMinimalSubsets) {
		List<Set<V>> listOfMinimalSubsets = new ArrayList<>(setOfMinimalSubsets);
		for (int i = 0 ; i < listOfMinimalSubsets.size() - 1 ; i++) {
			Set<V> iSet = listOfMinimalSubsets.get(i);
			for (int j = i + 1 ; j < listOfMinimalSubsets.size() ; i++) {
				Set<V> jSet = listOfMinimalSubsets.get(j);
				Set<V> intersection = Sets.intersection(iSet, jSet);
				if (!intersection.equals(iSet) && !intersection.equals(jSet) && !intersection.isEmpty())
					return false;
			}
		}
		return true;
	}
	
	private boolean isMaximal(Set<Set<V>> setOfMinimalSubsets) {
		boolean isMaximal = true;
		Set<Set<Set<V>>> nonMaximalAfterAll = new HashSet<>(); 
		for (Set<Set<V>> alreadyFound : maximalHierarchiesOfMinimals) {
			if (alreadyFound.containsAll(setOfMinimalSubsets))
				isMaximal = false;
			else if (setOfMinimalSubsets.containsAll(alreadyFound))
				nonMaximalAfterAll.add(alreadyFound);
		}
		maximalHierarchiesOfMinimals.removeAll(nonMaximalAfterAll);
		return isMaximal;
	}

}
