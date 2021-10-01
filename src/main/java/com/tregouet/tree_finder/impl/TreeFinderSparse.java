package com.tregouet.tree_finder.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.InTree;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;

public class TreeFinderSparse implements ITreeFinder<Integer, Integer> {

	private final SparseIntDirectedGraph upperSemiLattice;
	private final IntArrayList minimals;
	private final boolean[] elements;
	private final List<IntArrayList> coveredElements = new ArrayList<>();
	private final List<IntArraySet> lowerSets = new ArrayList<>();
	private final List<IntArraySet> minimalLowerBounds = new ArrayList<>();
	private final List<List<IntArrayList>> forkingSubsets = new ArrayList<>();
	private final List<IntArrayList> treeRestrictionsOfUSL;
	private int treeIdx = 0;
	
	public TreeFinderSparse(SparseIntDirectedGraph upperSemiLattice, int root, IntArrayList minimals) {
		this.upperSemiLattice = upperSemiLattice;
		this.minimals = minimals;
		//set elements and coveredElements
		TopologicalOrderIterator<Integer, Integer> topoIte = new TopologicalOrderIterator<>(upperSemiLattice);
		elements = new boolean[root];
		for (int i = 0 ; i <= root ; i++) {
			coveredElements.add(null);
		}
		while (topoIte.hasNext()) {
			Integer element = topoIte.next();
			int unboxedElement = (int) element;
			elements[unboxedElement] = true;
			List<Integer> covered = Graphs.predecessorListOf(upperSemiLattice, element);
			coveredElements.add(unboxedElement, new IntArrayList(covered));
		}
		//set lowerSets and minimalLowerBounds
		for (int i = 0 ; i <= root ; i++) {
			if (elements[i]) {
				if (minimals.contains(i)) {
					IntArraySet wrappedMinimal = new IntArraySet(new int[] {i});
					lowerSets.add(wrappedMinimal);
					minimalLowerBounds.add(wrappedMinimal);
				}
				else {
					IntArraySet iLowerSet = new IntArraySet(new int[] {i});
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
		//set forkingSubSets
		boolean[] closed = new boolean[elements.length];
		for (int i = 0 ; i < elements.length ; i++) {
			closed[i] = !elements[i];
		}
		for (int i = 0 ; i < elements.length ; i++) {
			if (!closed[i])
				forkingSubsets.add(getMinimalLowerBoundsOf(i, closed));
			else forkingSubsets.add(null);
		}
		//set treeRestrictionsOfUSL
		treeRestrictionsOfUSL = getSubTrees(root);
	}
	
	public List<IntArrayList> getSparseTreeVertexSets() {
		return treeRestrictionsOfUSL;
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
					subTreesFromLowerBounds.add(getSubTrees(lowerBound));
				}
				for (List<IntArrayList> oneSubtreeForEachLB : Lists.cartesianProduct(subTreesFromLowerBounds)) {
					IntArrayList subTreeFromLocalRoot = new IntArrayList(new int[] {localRoot});
					for (IntArrayList lowerBoundSubTree : oneSubtreeForEachLB) {
						subTreeFromLocalRoot.addAll(lowerBoundSubTree);
					}
					subTreesFromLocalRoot.add(subTreeFromLocalRoot);
				}
			}
		}
		return subTreesFromLocalRoot;
	}
	
	private List<IntArrayList> getMinimalLowerBoundsOf(int element, boolean[] closed) {
		if (minimals.contains(element))
			return new ArrayList<>();
		return getTreeForkGeneratorsOf(
				element, new IntArrayList(), minimalLowerBounds.get(element), closed);
	}
	
	private List<IntArrayList> getTreeForkGeneratorsOf(int element, IntArrayList lowerBounds, 
			IntArraySet coveredMinimals, boolean[] closed) {
		List<IntArrayList> treeForkGenerators = new ArrayList<>();
		for (int i = element - 1 ; i >= 0 ; i--) {
			if (!closed[i]) {
				IntArraySet iSupGeneratorMinimals = minimalLowerBounds.get(i);
				if (Sets.intersection(coveredMinimals, iSupGeneratorMinimals).isEmpty()) {
					IntArrayList nextLowerBounds = new IntArrayList(lowerBounds);
					nextLowerBounds.add(i);
					IntArraySet nextCoveredMinimals = 
							new IntArraySet(Sets.union(coveredMinimals, iSupGeneratorMinimals));
					if (nextCoveredMinimals.equals(minimalLowerBounds.get(element)))
						treeForkGenerators.add(nextLowerBounds);
					else {
						boolean[] nextClosed = new boolean[elements.length];
						System.arraycopy(closed, 0, nextClosed, 0, elements.length);
						for (int iLowerBound : lowerSets.get(i)) {
							nextClosed[iLowerBound] = true;
						}
						treeForkGenerators.addAll(
								getTreeForkGeneratorsOf(element, nextLowerBounds, nextCoveredMinimals, 
										nextClosed));
					}
				}
			}
		}
		return treeForkGenerators;
	}

	@Override
	public boolean hasNext() {
		return treeIdx < treeRestrictionsOfUSL.size() - 1;
	}

	@Override
	public InTree<Integer, Integer> next() {
		Integer root = elements.length - 1;
		List<Integer> leaves = new ArrayList<>(minimals);
		Set<Integer> edges = new HashSet<>();
		IntArrayList treeVertices = treeRestrictionsOfUSL.get(treeIdx++);
		for (Integer edge : upperSemiLattice.edgeSet()) {
			if (treeVertices.contains((int) upperSemiLattice.getEdgeSource(edge))
					&& treeVertices.contains((int) upperSemiLattice.getEdgeTarget(edge)))
				edges.add(edge);
		}
		return new InTree<Integer, Integer>(root, leaves, upperSemiLattice, edges);
	}

	@Override
	public int getNbOfTrees() {
		return treeRestrictionsOfUSL.size();
	}

}
