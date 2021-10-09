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
	private final List<List<IntArrayList>> subTrees;
	private final List<IntArrayList> trees;
	
	/*
	 * UNSAFE. Parameter MUST be the transitive reduction of an atomistic RIDAG, and the ascending 
	 * order on vertices must be topological. 
	 */
	protected TreeFinderSparse(SparseIntDirectedGraph reducedRIDAG, int maximum, IntArraySet atoms) {
		this.atoms = atoms;
		//set covered elements, lower sets and sup-encoding subsets of minimals
		for (int i = 0 ; i <= maximum ; i++) {
			coveredElements.add(new IntArrayList(Graphs.predecessorListOf(reducedRIDAG, i)));
			if (atoms.contains(i)) {
				IntArraySet singleton = new IntArraySet(new int[] {i});
				lowerSets.add(singleton);
				supEncodingSubsetsOfAtoms.add(singleton);
			}
			else {
				IntArraySet iLowerSet = new IntArraySet();
				iLowerSet.add(i);
				IntArraySet iSupEncodingSubsetOfAtoms = new IntArraySet();
				for (int iCoveredElmnt : coveredElements.get(i)) {
					iLowerSet.addAll(lowerSets.get(iCoveredElmnt));
					iSupEncodingSubsetOfAtoms.addAll(supEncodingSubsetsOfAtoms.get(iCoveredElmnt));
				}
				lowerSets.add(iLowerSet);
				supEncodingSubsetsOfAtoms.add(iSupEncodingSubsetOfAtoms);
			}
		}
		//set forkingSubSets and subTrees
		boolean[] closed = new boolean[maximum + 1];
		subTrees = new ArrayList<>(maximum + 1);
		for (int i = 0 ; i <= maximum ; i++) {
			forkingSubsets.add(getForkingSubsetsOfLowerBounds(i, closed));
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
			IntArraySet atomsToCover, IntArraySet coveredAtomsSoFar, boolean[] closed) {
		List<IntArrayList> forkingLowerBoundSubsets = new ArrayList<>();
		int searchStart =	
				(uncompleteFork.isEmpty() ? element - 1 : uncompleteFork.getInt(uncompleteFork.size() - 1) - 1);
		boolean[] closedForNow = new boolean[closed.length];
		System.arraycopy(closed, 0, closedForNow, 0, closed.length);
		for (int i = searchStart ; i >= 0 ; i--) {
			if (!closedForNow[i]) {
				IntArraySet iCoveredAtoms = supEncodingSubsetsOfAtoms.get(i);
				if (atomsToCover.containsAll(iCoveredAtoms) 
						&& Sets.intersection(coveredAtomsSoFar, iCoveredAtoms).isEmpty()) {
					IntArrayList continuedFork = new IntArrayList(uncompleteFork);
					continuedFork.add(i);
					IntArraySet nextCoveredAtoms = new IntArraySet(coveredAtomsSoFar);
					nextCoveredAtoms.addAll(iCoveredAtoms);
					if (nextCoveredAtoms.equals(atomsToCover))
						forkingLowerBoundSubsets.add(continuedFork);
					else {
						boolean[] nextClosed = new boolean[closed.length];
						System.arraycopy(closed, 0, nextClosed, 0, closed.length);
						for (int iLowerBound : lowerSets.get(i)) {
							nextClosed[iLowerBound] = true;
							closedForNow[iLowerBound] = true;
						}
						forkingLowerBoundSubsets.addAll(
								completeForkingSubsetsOfLowerBounds(element, continuedFork, atomsToCover, 
								nextCoveredAtoms, nextClosed));
					}
				}
			}
		}
		return forkingLowerBoundSubsets;
	}

	private List<IntArrayList> getForkingSubsetsOfLowerBounds(int element, boolean[] closed) {
		if (atoms.contains(element))
			return new ArrayList<>();
		return completeForkingSubsetsOfLowerBounds(
				element, new IntArrayList(), supEncodingSubsetsOfAtoms.get(element), new IntArraySet(), closed);
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
