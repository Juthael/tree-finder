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

	//Intentional side effects on previouslyPicked param
	private List<IntArrayList> completeForkingSubsetsOfLowerBounds(int element, IntArrayList uncompleteFork, 
			IntArraySet atomsToCover, IntArraySet coveredAtomsSoFar, boolean[] skipInspection, 
			IntArrayList previouslyPicked) {
		List<IntArrayList> forkingLowerBoundSubsets = new ArrayList<>();
		int searchStartIdx =	
				(uncompleteFork.isEmpty() ? element - 1 : (uncompleteFork.getInt(uncompleteFork.size() - 1)) - 1);
		for (int i = searchStartIdx ; i >= 0 ; i--) {
			if (!skipInspection[i]) {
				IntArrayList continuedFork = new IntArrayList(uncompleteFork);
				continuedFork.add(i);
				boolean alreadyCoveredByPrevious = false;
				IntArrayList continuedForkMinUpperBounds = getMinimalUpperBounds(continuedFork);
				for (int picked : previouslyPicked) {
					if (continuedForkMinUpperBounds.contains(picked)) {
						alreadyCoveredByPrevious = true;
						break;
					}
				}
				if (!alreadyCoveredByPrevious) {
					previouslyPicked.add(i);
					IntArraySet nextCoveredAtoms = new IntArraySet(coveredAtomsSoFar);
					nextCoveredAtoms.addAll(lowerBoundAtoms.get(i));
					if (nextCoveredAtoms.equals(atomsToCover))
						forkingLowerBoundSubsets.add(continuedFork);
					else {
						boolean[] nextSkipInspection = new boolean[i];
						System.arraycopy(skipInspection, 0, nextSkipInspection, 0, i);
						for (int j = i - 1 ; j >=0 ; j--) {
							if (!nextSkipInspection[j] 
									&& !Sets.intersection(nextCoveredAtoms, lowerBoundAtoms.get(j)).isEmpty())
								nextSkipInspection[j] = true;
						}
						forkingLowerBoundSubsets.addAll(
								completeForkingSubsetsOfLowerBounds(element, continuedFork, atomsToCover, 
								nextCoveredAtoms, nextSkipInspection, previouslyPicked));
					}
				}
			}
		}
		return forkingLowerBoundSubsets;
	}

	private List<IntArrayList> getForkingSubsetsOfLowerBounds(int element) {
		if (atoms.contains(element))
			return new ArrayList<>();
		IntArraySet atomsToCover = lowerBoundAtoms.get(element);
		boolean[] skipInspection = new boolean[element];
		for (int i = 0 ; i < element ; i++) {
			IntArraySet iLowerBoundAtoms = lowerBoundAtoms.get(i); 
			if (iLowerBoundAtoms.size() >= atomsToCover.size() || !atomsToCover.containsAll(iLowerBoundAtoms))
				skipInspection[i] = true;
		}
		return completeForkingSubsetsOfLowerBounds(element, new IntArrayList(), atomsToCover, 
				new IntArraySet(), skipInspection, new IntArrayList());
	}

	/*	returns a singleton with the least upper bound (supremum) if the constructor's first 
	 *	parameter is the graph of an upper semilattice.
	 */
	private IntArrayList getMinimalUpperBounds(IntArrayList set) {
		if (set.size() == 1)
			return set;
		IntArrayList minimalUpperBounds = new IntArrayList(upperSets.get(set.getInt(0)));
		for (int i = 1 ; i < set.size() ; i++) {
			minimalUpperBounds.retainAll(upperSets.get(set.getInt(i)));
		}
		minimalUpperBounds.sort(null);
		int j = 0;
		while (j < minimalUpperBounds.size()) {
			int jElem = minimalUpperBounds.getInt(j);
			IntArraySet jStrictUpperBounds = new IntArraySet(upperSets.get(jElem));
			jStrictUpperBounds.remove(jElem);
			minimalUpperBounds.removeAll(jStrictUpperBounds);
			j++;
		}
		return minimalUpperBounds;
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

}
