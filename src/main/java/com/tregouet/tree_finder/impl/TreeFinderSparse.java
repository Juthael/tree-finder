package com.tregouet.tree_finder.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tregouet.tree_finder.utils.Visualizer;

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
		//HERE
		/*
		try {
			Visualizer.visualize(rootedInverted, "debug");
		} catch (IOException e) {
			System.out.println("failed");
		}
		*/
		//HERE
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
			IntArraySet atomsToCover, IntArraySet coveredAtomsSoFar, boolean[] skipInspection) {
		System.out.println("Element : " + element);
		System.out.println("Uncomplete fork : " + uncompleteFork.toString());
		System.out.println("Covered so far : " + coveredAtomsSoFar.toString());
		//HERE
		List<IntArrayList> forkingLowerBoundSubsets = new ArrayList<>();
		int searchStartIdx =	
				(uncompleteFork.isEmpty() ? element - 1 : (uncompleteFork.getInt(uncompleteFork.size() - 1)) - 1);
		IntArraySet remainingAtoms = new IntArraySet(atomsToCover);
		remainingAtoms.removeAll(coveredAtomsSoFar);
		for (int i = searchStartIdx ; i >= max(remainingAtoms) ; i--) {
			if (!skipInspection[i]) {
				//HERE
				System.out.println("Test of " + i);
				if (continuedForkWillBeMaximal(element, uncompleteFork, i)) {
					IntArrayList continuedFork = new IntArrayList(uncompleteFork);
					continuedFork.add(i);
					IntArraySet nextCoveredAtoms = new IntArraySet(coveredAtomsSoFar);
					nextCoveredAtoms.addAll(lowerBoundAtoms.get(i));
					if (nextCoveredAtoms.equals(atomsToCover)) {
						forkingLowerBoundSubsets.add(continuedFork);
						//HERE
						System.out.println("Addition of " + i + " yields valid fork : " + continuedFork.toString());
						//HERE
					}
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
								nextCoveredAtoms, nextSkipInspection));
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

	/*	returns a singleton with the least upper bound (supremum) if the constructor's first 
	 *	parameter is the graph of an upper semilattice.
	 */
	private IntArrayList getMinimalUpperBounds(int e1, int e2) {
		IntArrayList minimalUpperBounds = new IntArrayList(upperSets.get(e1));
		minimalUpperBounds.retainAll(upperSets.get(e2));
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
	
	private int max(IntArraySet set) {
		int max = -1;
		for (int element : set) {
		 if (element > max)
			 max = element;
		}
		return max;
	}
	
	private boolean continuedForkWillBeMaximal(int root, IntArrayList uncompleteFork, int newElement) {
		for (int forkLowerBound : uncompleteFork) {
			if (!getMinimalUpperBounds(forkLowerBound, newElement).contains(root))
				return false;
		}
		return true;
	}

}
