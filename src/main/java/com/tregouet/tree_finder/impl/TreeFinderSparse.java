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
	private final List<IntArrayList> coveredElements = new ArrayList<>();
	private final List<IntArraySet> lowerSets = new ArrayList<>();
	private final List<IntArraySet> supEncodingSubsetsOfAtoms = new ArrayList<>();
	private final List<List<IntArrayList>> forkingSubsets = new ArrayList<>();
	private final List<List<IntArrayList>> subTrees = new ArrayList<>();
	private final List<IntArrayList> trees;
	
	/*
	 * UNSAFE. Parameter MUST be the transitive reduction of an upper semilattice, and the ascending 
	 * order on vertices must be topological. 
	 */
	public TreeFinderSparse(SparseIntDirectedGraph reducedUSL, int maximum, IntArraySet minimals) {
		this.atoms = minimals;
		//set covered elements, lower sets and sup-encoding subsets of minimals
		for (int i = 0 ; i <= maximum ; i++) {
			coveredElements.add(new IntArrayList(Graphs.predecessorListOf(reducedUSL, i)));
			if (minimals.contains(i)) {
				IntArraySet singleton = new IntArraySet(new int[] {i});
				lowerSets.add(singleton);
				supEncodingSubsetsOfAtoms.add(singleton);
			}
			else {
				IntArraySet iLowerSet = new IntArraySet();
				iLowerSet.add(i);
				IntArraySet iSupEncodingSubsetOfMinimals = new IntArraySet();
				for (int iCoveredElmnt : coveredElements.get(i)) {
					iLowerSet.addAll(lowerSets.get(iCoveredElmnt));
					iSupEncodingSubsetOfMinimals.addAll(supEncodingSubsetsOfAtoms.get(iCoveredElmnt));
				}
				lowerSets.add(iLowerSet);
				supEncodingSubsetsOfAtoms.add(iSupEncodingSubsetOfMinimals);
			}
		}
		//set forkingSubSets and subTrees
		boolean[] closed = new boolean[maximum + 1];
		for (int i = 0 ; i <= maximum ; i++) {
			forkingSubsets.add(getForkingSubsetsOf(i, closed));
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

	private List<IntArrayList> getForkingSubsetOfLowerBounds(int element, IntArrayList uncompleteLowerBoundSubset, 
			IntArraySet coveredMinimalsSoFar, boolean[] closed) {
		List<IntArrayList> forkingLowerBoundSubsets = new ArrayList<>();
		for (int i = element - 1 ; i >= 0 ; i--) {
			if (!closed[i]) {
				IntArraySet iCoveredMinimals = supEncodingSubsetsOfAtoms.get(i);
				if (Sets.intersection(coveredMinimalsSoFar, iCoveredMinimals).isEmpty()) {
					IntArrayList nextFork = new IntArrayList(uncompleteLowerBoundSubset);
					nextFork.add(i);
					IntArraySet nextCoveredMinimals = new IntArraySet(coveredMinimalsSoFar);
					nextCoveredMinimals.addAll(iCoveredMinimals);
					if (nextCoveredMinimals.equals(supEncodingSubsetsOfAtoms.get(element)))
						forkingLowerBoundSubsets.add(nextFork);
					else {
						boolean[] nextClosed = new boolean[closed.length];
						System.arraycopy(closed, 0, nextClosed, 0, closed.length);
						for (int iLowerBound : lowerSets.get(i)) {
							nextClosed[iLowerBound] = true;
						}
						forkingLowerBoundSubsets.addAll(
								getForkingSubsetOfLowerBounds(element, nextFork, nextCoveredMinimals, 
										nextClosed));
					}
				}
			}
		}
		return forkingLowerBoundSubsets;
	}

	private List<IntArrayList> getForkingSubsetsOf(int element, boolean[] closed) {
		if (atoms.contains(element))
			return new ArrayList<>();
		return getForkingSubsetOfLowerBounds(
				element, new IntArrayList(), supEncodingSubsetsOfAtoms.get(element), closed);
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
		subTrees.add(localRoot, subTreesFromLocalRoot);
		return subTreesFromLocalRoot;
	}

}
