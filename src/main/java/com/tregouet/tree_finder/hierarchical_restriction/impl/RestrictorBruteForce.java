package com.tregouet.tree_finder.hierarchical_restriction.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.data.Tree;
import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.hierarchical_restriction.IHierarchicalRestrictionFinder;
import com.tregouet.tree_finder.utils.StructureInspector;

public class RestrictorBruteForce<V, E> implements IHierarchicalRestrictionFinder<V, E> {

	private final DirectedAcyclicGraph<V, E> rootedInverted;
	private final V maximum;
	private final Set<V> atoms = new HashSet<>();
	private final Set<V> others = new HashSet<>();
	private final Set<Set<V>> treeRestrictions = new HashSet<>();
	private Iterator<Set<V>> treeIte;
	
	/*
	 * The parameter MUST be a rooted inverted DAG (reduced or not). Since the 
	 * generation of a power set is involved, large inputs will throw exceptions. 
	 */
	protected RestrictorBruteForce(DirectedAcyclicGraph<V, E> rootedInverted) 
			throws InvalidInputException {
		if (!StructureInspector.isARootedInvertedDirectedAcyclicGraph(rootedInverted))
			throw new InvalidInputException("The parameter is not a rooted inverted directed acyclic graph.");
		this.rootedInverted = rootedInverted;
		V maximum = null;
		Iterator<V> vIte = rootedInverted.vertexSet().iterator();
		while (vIte.hasNext()) {
			V e = vIte.next();
			if (maximum == null && rootedInverted.outDegreeOf(e) == 0)
				maximum = e;
			else if (rootedInverted.inDegreeOf(e) == 0)
				atoms.add(e);
		}
		this.maximum = maximum;
		for (V element : rootedInverted.vertexSet()) {
			if (!element.equals(maximum) && !atoms.contains(element))
				others.add(element);
		}
		Set<Set<V>> powerSetOfOthers = new HashSet<>(Sets.powerSet(others));
		powerSetOfOthers.remove(new HashSet<>());
		Set<Set<V>> candidates = new HashSet<>();
		for (Set<V> subsetOfOthers : powerSetOfOthers) {
			Set<V> candidate = new HashSet<>(subsetOfOthers);
			candidate.add(maximum);
			candidate.addAll(atoms);
			candidates.add(candidate);
		}
		for (Set<V> candidate : candidates) {
			if (isATree(candidate) && isMaximal(candidate))
				treeRestrictions.add(candidate);
		}
		treeIte = treeRestrictions.iterator();
	}

	public int getNbOfTrees() {
		return treeRestrictions.size();
	}

	@Override
	public boolean hasNext() {
		return treeIte.hasNext();
	}

	@Override
	public Tree<V, E> next() {
		return new Tree<V, E>(rootedInverted, treeIte.next(), maximum, atoms, false);
	}
	
	private boolean isATree(Set<V> candidateRestriction) {
		DirectedAcyclicGraph<V, E> candidateGraph = new DirectedAcyclicGraph<>(null, null, false);
		Graphs.addAllVertices(candidateGraph, candidateRestriction);
		Set<E> edges = new HashSet<>();
		for (E edge : rootedInverted.edgeSet()) {
			if (candidateRestriction.contains(rootedInverted.getEdgeSource(edge))
					&& candidateRestriction.contains(rootedInverted.getEdgeTarget(edge)))
				edges.add(edge);
		}
		Graphs.addAllEdges(candidateGraph, rootedInverted, edges);
		return StructureInspector.isATree(candidateGraph);
	}
	
	private boolean isMaximal(Set<V> thisTree) {
		boolean isMaximal = true;
		Set<Set<V>> notMaximalAfterAll = new HashSet<>(); 
		for (Set<V> alreadyFound : treeRestrictions) {
			if (alreadyFound.containsAll(thisTree))
				isMaximal = false;
			else if (thisTree.containsAll(alreadyFound))
				notMaximalAfterAll.add(alreadyFound);
		}
		treeRestrictions.removeAll(notMaximalAfterAll);
		return isMaximal;
	}

}
