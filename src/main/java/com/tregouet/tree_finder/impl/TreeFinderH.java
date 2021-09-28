package com.tregouet.tree_finder.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graphs;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.InTree;
import com.tregouet.tree_finder.error.InvalidSemiLatticeException;

public class TreeFinderH<V, E> implements ITreeFinder<V, E> {
	
	private final DirectedAcyclicGraph<V, E> upperSemiLattice;
	private final List<V> reversedTopoList = new ArrayList<>();
	private final List<E> edgeList = new ArrayList<>();
	private final int nbOfVertices;
	private final SparseIntDirectedGraph sparseGraph;
	private final List<Integer> leaves = new ArrayList<>();
	private final int nbOfLeaves;
	private final List<List<Integer>> ancestorSets = new ArrayList<>();
	private final List<Set<Integer>> encodingSubsetsOfLeaves = new ArrayList<>();
	private final List<List<Integer>> sparseTrees;
	private int sparseTreeIdx = 0;

	public TreeFinderH(DirectedAcyclicGraph<V, E> upperSemiLattice, boolean checkArg) 
			throws InvalidSemiLatticeException {
		if (checkArg && !ITreeFinder.isAnUpperSemiLattice(upperSemiLattice))
			throw new InvalidSemiLatticeException();
		this.upperSemiLattice = upperSemiLattice;
		new TopologicalOrderIterator<>(upperSemiLattice).forEachRemaining(v -> reversedTopoList.add(v));
		Collections.reverse(reversedTopoList);
		nbOfVertices = reversedTopoList.size();
		List<Pair<Integer, Integer>> sparseEdges = new ArrayList<>();
		for (E edge : upperSemiLattice.edgeSet()) {
			sparseEdges.add(new Pair<Integer, Integer>(
							reversedTopoList.indexOf(upperSemiLattice.getEdgeSource(edge)), 
							reversedTopoList.indexOf(upperSemiLattice.getEdgeTarget(edge))));
			edgeList.add(edge);
			
		}
		sparseGraph = new SparseIntDirectedGraph(nbOfVertices, sparseEdges);
		for (Integer vertex : sparseGraph.vertexSet()) {
			if (sparseGraph.inDegreeOf(vertex) == 0)
				leaves.add(vertex);
		}
		nbOfLeaves = leaves.size();
		populateLists();
		sparseTrees = buildTrees();
	}
	
	public TreeFinderH(DirectedAcyclicGraph<V, E> upperSemiLattice) {
		this.upperSemiLattice = upperSemiLattice;
		new TopologicalOrderIterator<>(upperSemiLattice).forEachRemaining(v -> reversedTopoList.add(v));
		Collections.reverse(reversedTopoList);
		nbOfVertices = reversedTopoList.size();
		List<Pair<Integer, Integer>> sparseEdges = new ArrayList<>();
		for (E edge : upperSemiLattice.edgeSet()) {
			sparseEdges.add(new Pair<Integer, Integer>(
							reversedTopoList.indexOf(upperSemiLattice.getEdgeSource(edge)), 
							reversedTopoList.indexOf(upperSemiLattice.getEdgeTarget(edge))));
		}
		sparseGraph = new SparseIntDirectedGraph(nbOfVertices, sparseEdges);
		for (Integer vertex : sparseGraph.vertexSet()) {
			if (sparseGraph.inDegreeOf(vertex) == 0)
				leaves.add(vertex);
		}
		nbOfLeaves = leaves.size();
		populateLists();
		sparseTrees = buildTrees();
	}	

	@Override
	public boolean hasNext() {
		return sparseTreeIdx < sparseTrees.size();
	}

	@Override
	public InTree<V, E> next() {
		List<E> edges = new ArrayList();
		List<Integer> sparseVertices = sparseTrees.get(sparseTreeIdx);
		for (Integer edge : sparseGraph.edgeSet()) {
			if (sparseVertices.contains(sparseGraph.getEdgeSource(edge)) 
					&& sparseVertices.contains(sparseGraph.getEdgeTarget(edge))) {
				edges.add(edgeList.get(edge));
			}
		}
		
	}

	@Override
	public int getNbOfTrees() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private List<List<Integer>> buildTrees() {
		return continueTree(0, new boolean[nbOfVertices], new ArrayList<>(), new HashSet<>());
	}
	
	private List<List<Integer>> continueTree(int newElem, boolean[] closed, List<Integer> incompleteTree, 
			Set<Integer> coveredLeaves) {
		List<List<Integer>> completeTrees = new ArrayList<>();
		List<Integer> continuedTree = new ArrayList<>(incompleteTree);
		continuedTree.add(newElem);
		Set<Integer> coveredLeavesAfterAddition = new HashSet<>(coveredLeaves);
		coveredLeavesAfterAddition.addAll(encodingSubsetsOfLeaves.get(newElem));
		if (coveredLeavesAfterAddition.size() == nbOfLeaves) {
			completeTrees.add(continuedTree);
		}
		else {
			boolean[] closedForNow = closed.clone();
			for (Integer ancestor : ancestorSets.get(newElem)) {
				closedForNow[ancestor] = true;
			}
			boolean[] closedForNext = closedForNow.clone();
			for (int i = newElem + 1 ; i < nbOfVertices ; i++) {
				if (!closedForNow[i] 
						&& (Sets.intersection(coveredLeaves, encodingSubsetsOfLeaves.get(i))).isEmpty()) {
					completeTrees.addAll(continueTree(i, closedForNext, continuedTree, coveredLeaves));
					for (Integer iAncestor : ancestorSets.get(i))
						closedForNow[iAncestor] = true;
				}
			}
		}
		return completeTrees;
	}
	
	private void populateLists() {
		for (int i = 0 ; i < reversedTopoList.size() ; i++) {
			encodingSubsetsOfLeaves.add(null);
			ancestorSets.add(null);
		}
		populateListsRecursively(0);
	}
	
	private Set<Integer> populateListsRecursively(Integer vertex) {
		Set<Integer> leafEncoding = encodingSubsetsOfLeaves.get(vertex);
		if (leafEncoding == null) {
			if (leaves.contains(vertex)) {
				encodingSubsetsOfLeaves.add(vertex, new HashSet<>(Arrays.asList(new Integer[]{vertex})));
				ancestorSets.add(vertex, new ArrayList<>());
			}
			else {
				leafEncoding = new HashSet<>();
				List<Integer> ancestors = ancestorSets.get(vertex);
				boolean buildAncestors = (ancestors == null);
				for (Integer predecessor  : Graphs.predecessorListOf(sparseGraph, vertex)) {
					leafEncoding.addAll(populateListsRecursively(predecessor));
					if (buildAncestors) {
						ancestors.add(predecessor);
						ancestors.addAll(ancestorSets.get(predecessor));
					}
				}
				encodingSubsetsOfLeaves.add(vertex, leafEncoding);
				if (buildAncestors)
					ancestorSets.add(vertex, ancestors);
			}	
		}
		return leafEncoding;
	}

}
