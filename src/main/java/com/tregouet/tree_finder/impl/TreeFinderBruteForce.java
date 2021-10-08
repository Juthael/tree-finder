package com.tregouet.tree_finder.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.ClassificationTree;

public class TreeFinderBruteForce<V, E> implements ITreeFinder<V, E> {

	private final DirectedAcyclicGraph<V, E> atomisticRIDAG;
	private final V maximum;
	private final Set<V> atoms;
	private final Map<Set<V>, V> encodingSubsetsOfAtomsToTheirSupremum = new HashMap<>();
	private final Set<Set<Set<V>>> powerSetOfEncodingSubsetsOfAtoms;
	private final Set<Set<Set<V>>> maximalHierarchiesOfAtoms;
	private final Set<Set<V>> treeRestrictions = new HashSet<>();
	private Iterator<Set<V>> treeIte;
	
	/*
	 * UNSAFE. The parameter MUST be an atomistic rooted inverted DAG (reduced or not)
	 */
	protected TreeFinderBruteForce(DirectedAcyclicGraph<V, E> atomisticRIDAG, Set<V> minimals) {
		this.atomisticRIDAG = atomisticRIDAG;
		V maximum = null;
		Iterator<V> vIte = atomisticRIDAG.vertexSet().iterator();
		while (maximum == null) {
			V v = vIte.next();
			if (atomisticRIDAG.outDegreeOf(v) == 0)
				maximum = v;
		}
		this.maximum = maximum;
		this.atoms = minimals;
		atomisticRIDAG.vertexSet().stream()
			.forEach(v -> 
				encodingSubsetsOfAtomsToTheirSupremum.put(atomLowerBounds(v), v));
		powerSetOfEncodingSubsetsOfAtoms = powerSet(encodingSubsetsOfAtomsToTheirSupremum.keySet());
		maximalHierarchiesOfAtoms = powerSetOfEncodingSubsetsOfAtoms.stream()
				.filter(s -> isAMaximalHierarchy(s))
				.collect(Collectors.toSet());
		for (Set<Set<V>> hierarchy : maximalHierarchiesOfAtoms) {
			Set<V> treeRestriction = hierarchy.stream()
					.map(s -> encodingSubsetsOfAtomsToTheirSupremum.get(s))
					.collect(Collectors.toSet());
			treeRestrictions.add(treeRestriction);
		}
		treeIte = treeRestrictions.iterator();
	}

	public int getNbOfTrees() {
		return treeRestrictions.size();
	}

	@Override
	public boolean hasNext() {
		return treeIte.hasNext();
	}

	@Override
	public ClassificationTree<V, E> next() {
		return new ClassificationTree<V, E>(atomisticRIDAG, treeIte.next(), maximum, atoms, false);
	}
	
	private boolean coversEveryMinimal(Set<Set<V>> subsetsOfAtoms) {
		Set<V> covered = new HashSet<>();
		for (Set<V> subset : subsetsOfAtoms) {
			covered.addAll(subset);
		}
		return covered.equals(atoms);
	}
	
	private boolean isAHierarchy(Set<Set<V>> setOfMinimalSubsets) {
		List<Set<V>> listOfAtomSubsets = new ArrayList<>(setOfMinimalSubsets);
		for (int i = 0 ; i < listOfAtomSubsets.size() - 1 ; i++) {
			Set<V> iSet = listOfAtomSubsets.get(i);
			for (int j = i + 1 ; j < listOfAtomSubsets.size() ; j++) {
				Set<V> jSet = listOfAtomSubsets.get(j);
				Set<V> intersection = Sets.intersection(iSet, jSet);
				if (!intersection.equals(iSet) && !intersection.equals(jSet) && !intersection.isEmpty())
					return false;
			}
		}
		return true;
	}
	
	private boolean isAMaximalHierarchy(Set<Set<V>> setOfAtomSubsets) {
		return (coversEveryMinimal(setOfAtomSubsets) 
				&& isAHierarchy(setOfAtomSubsets) 
				&& isMaximal(setOfAtomSubsets));
	}
	
	private boolean isMaximal(Set<Set<V>> hierarchyOfAtoms) {
		boolean isMaximal = true;
		Set<Set<Set<V>>> notMaximalAfterAll = new HashSet<>(); 
		for (Set<Set<V>> alreadyFound : maximalHierarchiesOfAtoms) {
			if (alreadyFound.containsAll(hierarchyOfAtoms))
				isMaximal = false;
			else if (hierarchyOfAtoms.containsAll(alreadyFound))
				notMaximalAfterAll.add(alreadyFound);
		}
		maximalHierarchiesOfAtoms.removeAll(notMaximalAfterAll);
		return isMaximal;
	}
	
	private Set<V> atomLowerBounds(V element) {
		Set<V> lowerBoundAtoms = new HashSet<>();
		lowerBoundAtoms.add(element);
		lowerBoundAtoms.addAll(atomisticRIDAG.getAncestors(element));
		lowerBoundAtoms.retainAll(atoms);
		return lowerBoundAtoms;
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

}
