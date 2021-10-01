package com.tregouet.tree_finder.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.InTree;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;

public class TreeFinderSparseDeprec implements ITreeFinder<Integer, Integer> {
	
	private final SparseIntDirectedGraph upperSemilattice;
	private final int setCardinal;
	private final int[] reversedTopoList;
	private final IntArrayList minimals;
	private final int nbOfMinimals;
	private final List<IntArraySet> strictLowerBounds = new ArrayList<>();
	private final List<IntArraySet> supGeneratorSubsetsOfMinimals = new ArrayList<>();
	private final List<IntArrayList> treeRestrictionsOfSet;
	private int treeIdx = 0;

	//Unsafe. First parameter MUST be an upperSemiLattice, otherwise behavior is undefined. 
	public TreeFinderSparseDeprec(SparseIntDirectedGraph upperSemilattice, IntArrayList minimals) {
		this.upperSemilattice = upperSemilattice;
		TopologicalOrderIterator<Integer, Integer> topoIte = new TopologicalOrderIterator<>(upperSemilattice);
		setCardinal = upperSemilattice.vertexSet().size();
		reversedTopoList = new int[setCardinal];
		int topoListIdx = setCardinal - 1;
		while (topoIte.hasNext())
			reversedTopoList[topoListIdx--] = ((int) topoIte.next());
		this.minimals = minimals;
		nbOfMinimals = minimals.size();
		populateLists();
		treeRestrictionsOfSet = buildTrees();
	}
	
	private List<IntArrayList> buildTrees() {
		if (setCardinal > 1) {
			//no good reason why it should be otherwise
			return continueTreeRecursively(1, new boolean[setCardinal], 
					new IntArrayList(new int[] {reversedTopoList[0]}), new IntArraySet());
		}
		else return new ArrayList<>(Arrays.asList(new IntArrayList(reversedTopoList)));
	}
	
	private List<IntArrayList> continueTreeRecursively(int newElemIdx, boolean[] closedAlready, 
			IntArrayList incompleteTree, IntArraySet coveredMinimals) {
		boolean[] closed = closedAlready.clone();
		List<IntArrayList> completeTrees = new ArrayList<>();
		IntArrayList continuedTree = new IntArrayList(incompleteTree);
		continuedTree.add(reversedTopoList[newElemIdx]);
		IntArraySet coveredMinimalsAfterAddition = new IntArraySet(coveredMinimals);
		coveredMinimalsAfterAddition.addAll(supGeneratorSubsetsOfMinimals.get(newElemIdx));
		for (int i = newElemIdx + 1 ; i < setCardinal ; i++) {
			if (!closed[i]) {
				if (Sets.intersection(coveredMinimalsAfterAddition, supGeneratorSubsetsOfMinimals.get(i)).isEmpty()) {
					completeTrees.addAll(
							continueTreeRecursively(i, closed, continuedTree, coveredMinimalsAfterAddition));
					for (int iAncestor : strictLowerBounds.get(i))
						closed[iAncestor] = true;
				}
			}
		}
		return completeTrees;
	}
	
	private void populateLists() {
		for (int i = 0 ; i < setCardinal ; i++) {
			supGeneratorSubsetsOfMinimals.add(null);
			strictLowerBounds.add(null);
		}
		populateListsRecursively(0);
	}
	
	private IntArraySet populateListsRecursively(int vertexIdx) {
		int vertex = reversedTopoList[vertexIdx];
		IntArraySet subsetOfMinimals = supGeneratorSubsetsOfMinimals.get(vertexIdx);
		if (subsetOfMinimals == null) {
			if (minimals.contains(vertex)) {
				supGeneratorSubsetsOfMinimals.add(vertexIdx, new IntArraySet(new int[] {vertex}));
				strictLowerBounds.add(vertexIdx, new IntArraySet());
			}
			else {
				Integer wrappedVertex = vertex;
				subsetOfMinimals = new IntArraySet();
				IntArraySet ancestors = strictLowerBounds.get(vertexIdx);
				boolean buildAncestors = (ancestors == null);
				for (Integer predecessor : Graphs.predecessorListOf(upperSemilattice, wrappedVertex)) {
					int unboxedPrec = (int) predecessor;
					subsetOfMinimals.addAll(populateListsRecursively(unboxedPrec));
					if (buildAncestors) {
						ancestors.add(unboxedPrec);
						//non
						ancestors.addAll(strictLowerBounds.get(unboxedPrec));
					}
				}
				supGeneratorSubsetsOfMinimals.add(vertexIdx, subsetOfMinimals);
				if (buildAncestors)
					strictLowerBounds.add(vertexIdx, ancestors);
			}
		}
		return subsetOfMinimals;
	}

	@Override
	public boolean hasNext() {
		return treeIdx < treeRestrictionsOfSet.size() - 1;
	}

	@Override
	public InTree<Integer, Integer> next() {
		Set<Integer> treeEdgeSet = new HashSet<>();
		List<Integer> treeVertexSet = treeRestrictionsOfSet.get(treeIdx);
		List<Integer> leaveList = minimals;
		for (Integer edge : upperSemilattice.edgeSet()) {
			if (treeVertexSet.contains((int) upperSemilattice.getEdgeSource(edge)) 
					&& treeVertexSet.contains((int) upperSemilattice.getEdgeTarget(edge))){
				treeEdgeSet.add(edge);
			}
		}
		treeIdx++;
		return new InTree<Integer, Integer>(reversedTopoList[0], leaveList, upperSemilattice, treeEdgeSet);
	}

	@Override
	public int getNbOfTrees() {
		return treeRestrictionsOfSet.size();
	}
	
	public List<IntArrayList> getTreeVertexSets() {
		return treeRestrictionsOfSet;
	}

}
