package com.tregouet.tree_finder.impl;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;

public class TreeFinderSparse {

	private final IntArraySet atoms;
	private final List<IntArrayList> predecessors = new ArrayList<>();
	private final List<IntArraySet> lowerSets = new ArrayList<>();
	private final List<IntArraySet> upperSets = new ArrayList<>();
	private final List<IntArraySet> lowerBoundAtoms = new ArrayList<>();
	private final List<List<IntArrayList>> forkingSubsets = new ArrayList<>();
	private final List<List<IntArrayList>> subTrees;
	private final List<IntArrayList> trees;
	
	/*
	 * UNSAFE. Parameter MUST be the transitive reduction of an rooted inverted DAG, and the ascending 
	 * order on vertices must be topological. 
	 */
	protected TreeFinderSparse(SparseIntDirectedGraph rootedInverted, int maximum, IntArraySet atoms) {
		this.atoms = atoms;
		//set predecessors, lower sets and sup-encoding subsets of minimals
		for (int i = 0 ; i <= maximum ; i++) {
			predecessors.add(new IntArrayList(Graphs.predecessorListOf(rootedInverted, i)));
			if (atoms.contains(i)) {
				IntArraySet singleton = new IntArraySet(new int[] {i});
				lowerSets.add(singleton);
				lowerBoundAtoms.add(singleton);
			}
			else {
				IntArraySet iLowerSet = new IntArraySet();
				iLowerSet.add(i);
				IntArraySet iSupEncodingSubsetOfAtoms = new IntArraySet();
				for (int iCoveredElmnt : predecessors.get(i)) {
					iLowerSet.addAll(lowerSets.get(iCoveredElmnt));
					iSupEncodingSubsetOfAtoms.addAll(lowerBoundAtoms.get(iCoveredElmnt));
				}
				lowerSets.add(iLowerSet);
				lowerBoundAtoms.add(iSupEncodingSubsetOfAtoms);
			}
			//prepare for below
			upperSets.add(null);
		}
		//set upper sets
		for (int i = maximum ; i >= 0 ; i--) {
			IntArraySet iUpperSet = new IntArraySet();
			iUpperSet.add(i);
			for (int successor : Graphs.successorListOf(rootedInverted, i)) {
				iUpperSet.addAll(upperSets.get(successor));
			}
			upperSets.set(i, iUpperSet);
		}
		//set forkingSubSets and subTrees
		subTrees = new ArrayList<>(maximum + 1);
		for (int i = 0 ; i <= maximum ; i++) {
			forkingSubsets.add(getForkingSubsetsOfLowerBounds(i));
			subTrees.add(null);
		}
		//set trees
		trees = getSubTrees(maximum);
	}
	
	public int getNbOfTrees() {
		return trees.size();
	}
	
	public List<IntArrayList> getSparseTreeVertexSets() {
		return trees;
	}

	private List<IntArrayList> completeForkingSubsetsOfLowerBounds(int element, IntArrayList uncompleteFork, 
			IntArraySet atomsToCover, IntArraySet coveredAtomsSoFar, boolean[] skipInspection) {
		List<IntArrayList> completeMaxForks = new ArrayList<>();
		int searchStartIdx =	
				(uncompleteFork.isEmpty() ? element - 1 : (uncompleteFork.getInt(uncompleteFork.size() - 1)) - 1);
		IntArrayList remainingAtoms = new IntArrayList(atomsToCover);
		remainingAtoms.removeAll(coveredAtomsSoFar);
		for (int i = searchStartIdx ; i >= max(remainingAtoms) ; i--) {
			if (!skipInspection[i]) {
				IntArrayList continuedFork = new IntArrayList(uncompleteFork);
				continuedFork.add(i);
				IntArraySet nextCoveredAtoms = new IntArraySet(coveredAtomsSoFar);
				nextCoveredAtoms.addAll(lowerBoundAtoms.get(i));
				if (nextCoveredAtoms.equals(atomsToCover) && forkIsMaximal(continuedFork, completeMaxForks))
					completeMaxForks.add(continuedFork);
				else {
					boolean[] nextSkipInspection = new boolean[i];
					System.arraycopy(skipInspection, 0, nextSkipInspection, 0, i);
					for (int j = i - 1 ; j >=0 ; j--) {
						if (!nextSkipInspection[j] 
								&& !Sets.intersection(nextCoveredAtoms, lowerBoundAtoms.get(j)).isEmpty())
							nextSkipInspection[j] = true;
					}
					List<IntArrayList> returnedForks = 
							completeForkingSubsetsOfLowerBounds(element, continuedFork, atomsToCover, 
							nextCoveredAtoms, nextSkipInspection);
					for (IntArrayList returnedFork : returnedForks) {
						if (forkIsMaximal(returnedFork, completeMaxForks))
							completeMaxForks.add(returnedFork);
					}
				}
			}
		}
		return completeMaxForks;
	}

	private boolean forkIsMaximal(IntArrayList newFork, List<IntArrayList> previousForks) {
		for (IntArrayList previousFork : previousForks) {
			IntArraySet prevForkLowerSet = lowerSet(previousFork);
			if (prevForkLowerSet.containsAll(newFork))
				return false;				
		}
		return true;
	}
	
	private List<IntArrayList> getForkingSubsetsOfLowerBounds(int element) {
		if (atoms.contains(element))
			return null;
		IntArraySet atomsToCover = lowerBoundAtoms.get(element);
		int nbOfAtomsToCover = atomsToCover.size();
		boolean[] skipInspection = new boolean[element];
		for (int i = 0 ; i < element ; i++) {
			IntArraySet iLowerBoundAtoms = lowerBoundAtoms.get(i);
			int iLowerBoundAtomsCardinal = iLowerBoundAtoms.size();
			if (nbOfAtomsToCover < iLowerBoundAtomsCardinal)
				skipInspection[i] = true;
			else if (nbOfAtomsToCover == iLowerBoundAtomsCardinal) {
				if (!lowerSets.get(element).contains(i))
					skipInspection[i] = true;
			}
			else if (!atomsToCover.containsAll(iLowerBoundAtoms)) 
				skipInspection[i] = true;
		}
		return completeForkingSubsetsOfLowerBounds(element, new IntArrayList(), atomsToCover, 
				new IntArraySet(), skipInspection);
	}
	
	private List<IntArrayList> getSubTrees(int localRoot) {
		List<IntArrayList> subTreesFromLocalRoot = new ArrayList<>();
		if (atoms.contains(localRoot)) {
			subTreesFromLocalRoot.add(new IntArrayList(new int[] {localRoot}));
			return subTreesFromLocalRoot;
		}
		else {
			List<IntArrayList> forks = forkingSubsets.get(localRoot);
			for (IntArrayList fork : forks) {
				List<List<IntArrayList>> subTreesFromLowerBounds = new ArrayList<>();
				for (int lowerBound : fork) {
					List<IntArrayList> subTreesFromLowerBound = subTrees.get(lowerBound);
					if (subTreesFromLowerBound == null)
						subTreesFromLowerBounds.add(getSubTrees(lowerBound));
					else subTreesFromLowerBounds.add(subTreesFromLowerBound);
				}
				for (List<IntArrayList> oneSubtreeForEachLB : Lists.cartesianProduct(subTreesFromLowerBounds)) {
					IntArrayList subTreeFromLocalRoot = new IntArrayList();
					subTreeFromLocalRoot.add(localRoot);
					for (IntArrayList lowerBoundSubTree : oneSubtreeForEachLB)
						subTreeFromLocalRoot.addAll(lowerBoundSubTree);
					subTreesFromLocalRoot.add(subTreeFromLocalRoot);
				}
			}
		}
		subTrees.set(localRoot, subTreesFromLocalRoot);
		return subTreesFromLocalRoot;
	}
	
	private IntArraySet lowerSet(IntArrayList set) {
		IntArraySet lowerSet = new IntArraySet();
		for (int element : set) {
			lowerSet.addAll(lowerSets.get(element));
		}
		return lowerSet;
	}
	
	private int max(IntArrayList set) {
		int max = -1;
		for (int element : set) {
		 if (element > max)
			 max = element;
		}
		return max;
	}

}
