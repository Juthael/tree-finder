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

public class TreeFinderSparse implements ITreeFinder<Integer, Integer> {
	
	private final SparseIntDirectedGraph upperSemiLattice;
	private final int nbOfVertices;
	private final int[] reversedTopoList;
	private final IntArrayList leaves;
	private final int nbOfLeaves;
	private final List<IntArraySet> ancestorSets = new ArrayList<>();
	private final List<IntArraySet> encodingSubsetsOfLeaves = new ArrayList<>();
	private final List<IntArrayList> vertexSetsForTrees;
	private int treeIdx = 0;

	//Unsafe. First parameter MUST be an upperSemiLattice, otherwise behavior is undefined. 
	public TreeFinderSparse(SparseIntDirectedGraph upperSemiLattice, IntArrayList leaves) {
		this.upperSemiLattice = upperSemiLattice;
		TopologicalOrderIterator<Integer, Integer> topoIte = new TopologicalOrderIterator<>(upperSemiLattice);
		nbOfVertices = upperSemiLattice.vertexSet().size();
		reversedTopoList = new int[nbOfVertices];
		int topoListIdx = nbOfVertices - 1;
		while (topoIte.hasNext())
			reversedTopoList[topoListIdx--] = ((int) topoIte.next());
		this.leaves = leaves;
		nbOfLeaves = leaves.size();
		populateLists();
		vertexSetsForTrees = buildTrees();
	}
	
	private List<IntArrayList> buildTrees() {
		if (nbOfVertices > 1) {
			//no good reason why it should be otherwise
			return continueTreeRecursively(1, new boolean[nbOfVertices], 
					new IntArrayList(new int[] {reversedTopoList[0]}), new IntArraySet());
		}
		else return new ArrayList<>(Arrays.asList(new IntArrayList(reversedTopoList)));
	}
	
	private List<IntArrayList> continueTreeRecursively(int newElem, boolean[] closed, IntArrayList incompleteTree, 
			IntArraySet coveredLeaves) {
		List<IntArrayList> completeTrees = new ArrayList<>();
		IntArrayList continuedTree = new IntArrayList(incompleteTree);
		continuedTree.add(newElem);
		IntArraySet coveredLeavesAfterAddition = new IntArraySet(coveredLeaves);
		coveredLeavesAfterAddition.addAll(encodingSubsetsOfLeaves.get(newElem));
		if (coveredLeavesAfterAddition.size() == nbOfLeaves) {
			completeTrees.add(continuedTree);
		}
		else {
			boolean[] closedForNow = closed.clone();
			for (int ancestor : ancestorSets.get(newElem)) {
				closedForNow[ancestor] = true;
			}
			boolean[] closedForNext = closedForNow.clone();
			for (int i = newElem + 1 ; i < nbOfVertices ; i++) {
				if (!closedForNow[i] 
						&& (Sets.intersection(coveredLeavesAfterAddition, encodingSubsetsOfLeaves.get(i))).isEmpty()) {
					completeTrees.addAll(
							continueTreeRecursively(i, closedForNext, continuedTree, coveredLeavesAfterAddition));
					for (int iAncestor : ancestorSets.get(i))
						closedForNow[iAncestor] = true;
				}
			}
		}
		return completeTrees;
	}
	
	private void populateLists() {
		for (int i = 0 ; i < nbOfVertices ; i++) {
			encodingSubsetsOfLeaves.add(null);
			ancestorSets.add(null);
		}
		populateListsRecursively(0);
	}
	
	private IntArraySet populateListsRecursively(int vertex) {
		IntArraySet leafEncoding = encodingSubsetsOfLeaves.get(vertex);
		if (leafEncoding == null) {
			if (leaves.contains(vertex)) {
				encodingSubsetsOfLeaves.add(vertex, new IntArraySet(new int[] {vertex}));
				ancestorSets.add(vertex, new IntArraySet());
			}
			else {
				leafEncoding = new IntArraySet();
				IntArraySet ancestors = ancestorSets.get(vertex);
				boolean buildAncestors = (ancestors == null);
				for (Integer predecessor : Graphs.predecessorListOf(upperSemiLattice, vertex)) {
					int unboxedPrec = (int) predecessor;
					leafEncoding.addAll(populateListsRecursively(unboxedPrec));
					if (buildAncestors) {
						ancestors.add(unboxedPrec);
						ancestors.addAll(ancestorSets.get(unboxedPrec));
					}
				}
				encodingSubsetsOfLeaves.add(vertex, leafEncoding);
				if (buildAncestors)
					ancestorSets.add(vertex, ancestors);
			}
		}
		return leafEncoding;
	}

	@Override
	public boolean hasNext() {
		return treeIdx < vertexSetsForTrees.size() - 1;
	}

	@Override
	public InTree<Integer, Integer> next() {
		Set<Integer> treeEdgeSet = new HashSet<>();
		List<Integer> treeVertexSet = vertexSetsForTrees.get(treeIdx);
		List<Integer> leaveList = leaves;
		for (Integer edge : upperSemiLattice.edgeSet()) {
			if (treeVertexSet.contains((int) upperSemiLattice.getEdgeSource(edge)) 
					&& treeVertexSet.contains((int) upperSemiLattice.getEdgeTarget(edge))){
				treeEdgeSet.add(edge);
			}
		}
		treeIdx++;
		return new InTree<Integer, Integer>(reversedTopoList[0], leaveList, upperSemiLattice, treeEdgeSet);
	}

	@Override
	public int getNbOfTrees() {
		return vertexSetsForTrees.size();
	}
	
	public List<IntArrayList> getTreeVertexSets() {
		return vertexSetsForTrees;
	}

}
