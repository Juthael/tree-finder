package com.tregouet.tree_finder.impl;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;

public class SparseExtractor {

	private final IntArraySet minimals;
	private final boolean[] elements;
	private final List<IntArrayList> coveredElements = new ArrayList<>();
	private final List<IntArraySet> lowerSets = new ArrayList<>();
	private final List<IntArraySet> minimalLowerBounds = new ArrayList<>();
	private final List<List<IntArrayList>> forkingSubsets = new ArrayList<>();
	private final List<List<IntArrayList>> subTrees = new ArrayList<>();
	private final List<IntArrayList> treeRestrictionsOfUSL;
	
	/*
	 * UNSAFE. Parameter MUST be the transitive reduction of an upper semilattice. 
	 */
	public SparseExtractor(SparseIntDirectedGraph reducedUSL, int root, IntArraySet minimals) {
		this.minimals = minimals;
		//set elements and coveredElements
		TopologicalOrderIterator<Integer, Integer> topoIte = new TopologicalOrderIterator<>(reducedUSL);
		elements = new boolean[root];
		for (int i = 0 ; i <= root ; i++) {
			coveredElements.add(null);
		}
		while (topoIte.hasNext()) {
			Integer element = topoIte.next();
			int unboxedElement = element;
			elements[unboxedElement] = true;
			List<Integer> covered = Graphs.predecessorListOf(reducedUSL, element);
			coveredElements.add(unboxedElement, new IntArrayList(covered));
		}
		//set lowerSets and minimalLowerBounds
		for (int i = 0 ; i <= root ; i++) {
			if (elements[i]) {
				if (minimals.contains(i)) {
					IntArraySet singleton = new IntArraySet(new int[] {i});
					lowerSets.add(singleton);
					minimalLowerBounds.add(singleton);
				}
				else {
					IntArraySet iLowerSet = new IntArraySet();
					iLowerSet.add(i);
					IntArraySet iSupGeneratorSubsetOfMinimals = new IntArraySet();
					for (int iCoveredElmnt : coveredElements.get(i)) {
						iLowerSet.addAll(lowerSets.get(iCoveredElmnt));
						iSupGeneratorSubsetOfMinimals.addAll(minimalLowerBounds.get(iCoveredElmnt));
					}
					lowerSets.add(iLowerSet);
					minimalLowerBounds.add(iSupGeneratorSubsetOfMinimals);
				}
			}
			else {
				lowerSets.add(null);
				minimalLowerBounds.add(null);
			}
		}
		//set forkingSubSets and subTrees
		boolean[] closed = new boolean[elements.length];
		for (int i = 0 ; i < elements.length ; i++) {
			closed[i] = !elements[i];
		}
		for (int i = 0 ; i < elements.length ; i++) {
			if (!closed[i])
				forkingSubsets.add(getMinimalLowerBoundsOf(i, closed));
			else forkingSubsets.add(null);
			subTrees.add(null);
		}
		//set treeRestrictionsOfUSL
		treeRestrictionsOfUSL = getSubTrees(root);
	}
	
	public int getNbOfTrees() {
		return treeRestrictionsOfUSL.size();
	}
	
	public List<IntArrayList> getSparseTreeVertexSets() {
		return treeRestrictionsOfUSL;
	}

	private List<IntArrayList> getForkingSubsetOfLowerBounds(int element, IntArrayList uncompleteLowerBoundSubset, 
			IntArraySet reachableMinimalsSoFar, boolean[] closed) {
		List<IntArrayList> forkingLowerBoundSubsets = new ArrayList<>();
		for (int i = element - 1 ; i >= 0 ; i--) {
			if (!closed[i]) {
				IntArraySet iReachableMinimals = minimalLowerBounds.get(i);
				if (Sets.intersection(reachableMinimalsSoFar, iReachableMinimals).isEmpty()) {
					IntArrayList nextLowerBounds = new IntArrayList(uncompleteLowerBoundSubset);
					nextLowerBounds.add(i);
					IntArraySet nextCoveredMinimals = 
							new IntArraySet(Sets.union(reachableMinimalsSoFar, iReachableMinimals));
					if (nextCoveredMinimals.equals(minimalLowerBounds.get(element)))
						forkingLowerBoundSubsets.add(nextLowerBounds);
					else {
						boolean[] nextClosed = new boolean[elements.length];
						System.arraycopy(closed, 0, nextClosed, 0, elements.length);
						for (int iLowerBound : lowerSets.get(i)) {
							nextClosed[iLowerBound] = true;
						}
						forkingLowerBoundSubsets.addAll(
								getForkingSubsetOfLowerBounds(element, nextLowerBounds, nextCoveredMinimals, 
										nextClosed));
					}
				}
			}
		}
		return forkingLowerBoundSubsets;
	}

	private List<IntArrayList> getMinimalLowerBoundsOf(int element, boolean[] closed) {
		if (minimals.contains(element))
			return new ArrayList<>();
		return getForkingSubsetOfLowerBounds(
				element, new IntArrayList(), minimalLowerBounds.get(element), closed);
	}

	private List<IntArrayList> getSubTrees(int localRoot) {
		List<IntArrayList> subTreesFromLocalRoot = new ArrayList<>();
		if (minimals.contains(localRoot)) {
			subTreesFromLocalRoot.add(new IntArrayList(new int[] {localRoot}));
			return subTreesFromLocalRoot;
		}
		else {
			List<IntArrayList> forkGenerators = forkingSubsets.get(localRoot);
			for (IntArrayList forkingLowerBounds : forkGenerators) {
				List<List<IntArrayList>> subTreesFromLowerBounds = new ArrayList<>();
				for (int lowerBound : forkingLowerBounds) {
					List<IntArrayList> subTreesFromLowerBound = subTrees.get(lowerBound);
					if (subTreesFromLowerBound == null)
						subTreesFromLowerBounds.add(getSubTrees(lowerBound));
					else subTreesFromLowerBounds.add(subTreesFromLowerBound);
				}
				for (List<IntArrayList> oneSubtreeForEachLB : Lists.cartesianProduct(subTreesFromLowerBounds)) {
					IntArrayList subTreeFromLocalRoot = new IntArrayList(new int[] {localRoot});
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
