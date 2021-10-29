package com.tregouet.tree_finder.algo.unidimensional_sorting.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tregouet.tree_finder.algo.unidimensional_sorting.IUnidimensionalSorter;
import com.tregouet.tree_finder.algo.unidimensional_sorting.utils.BasicFunc;
import com.tregouet.tree_finder.algo.unidimensional_sorting.utils.BetweenSetsFunc;
import com.tregouet.tree_finder.algo.unidimensional_sorting.utils.WithinSetFunc;
import com.tregouet.tree_finder.data.RootedInvertedGraph;
import com.tregouet.tree_finder.data.Tree;
import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.utils.StructureInspector;

public class UnidimensionalSorter<V, E extends DefaultEdge> implements IUnidimensionalSorter<V, E> {

	private final Supplier<E> edgeSupplier;
	private final List<Tree<V, E>> trees;
	private int treeIdx = 0;
	
	//The first parameter MUST be an upper semilattice or an exception will be thrown 
	public UnidimensionalSorter(DirectedAcyclicGraph<V, E> dag, Supplier<E> edgeSupplier) throws InvalidInputException {
		if (!StructureInspector.isARootedInvertedDirectedAcyclicGraph(dag))
			throw new InvalidInputException("The first parameter is not an upper semilattice");
		TransitiveReduction.INSTANCE.reduce(dag);
		V root = null;
		Set<V> atoms = new HashSet<>();
		List<V> topologicalOrder = new ArrayList<>();
		TopologicalOrderIterator<V, E> topoIte = new TopologicalOrderIterator<>(dag);
		while (topoIte.hasNext()) {
			V nextElem = topoIte.next();
			topologicalOrder.add(nextElem);
			if (dag.inDegreeOf(nextElem) == 0)
				atoms.add(nextElem);
			if (!topoIte.hasNext())
				root = nextElem;
		}
		RootedInvertedGraph<V, E> rootedInverted = 
				new RootedInvertedGraph<V, E>(dag, root, atoms, topologicalOrder, edgeSupplier);
		this.edgeSupplier = edgeSupplier;
		trees = scatter(rootedInverted);
	}
	
	public UnidimensionalSorter(RootedInvertedGraph<V, E> rootedInverted, Supplier<E> edgeSupplier) {
		TransitiveReduction.INSTANCE.reduce(rootedInverted);
		this.edgeSupplier = edgeSupplier;
		trees = scatter(rootedInverted);
		
	}
	
	private List<Tree<V, E>> scatter(RootedInvertedGraph<V, E> alphaCategories) {
		List<Tree<V, E>> alphaSortings = new ArrayList<>();
		if (StructureInspector.isATree(alphaCategories)) {
			Tree<V, E> singleton = 
					new Tree<>(alphaCategories, edgeSupplier);
			alphaSortings.add(singleton);
			return alphaSortings;
		}
		V alphaClass = alphaCategories.getRoot();
		DirectedAcyclicGraph<V, E> alphaSubCategories = 
				WithinSetFunc.beheaded(alphaCategories, alphaClass, edgeSupplier);
		Set<V> atoms = alphaCategories.getLeaves();
		List<V> subAlphasTopoOrder = alphaCategories.getTopologicalSortingOfVertices();
		subAlphasTopoOrder.remove(subAlphasTopoOrder.size() - 1);
		List<List<Tree<V, E>>> sortingsOfSubAlphas = gather(alphaSubCategories, atoms, subAlphasTopoOrder);
		for (List<Tree<V, E>> subAlphaSorting : sortingsOfSubAlphas) {
			DirectedAcyclicGraph<V, E> sum = BetweenSetsFunc.cardinalSum(subAlphaSorting, edgeSupplier);
			BetweenSetsFunc.asMaximum(sum, alphaClass, true);
			alphaSortings.add(new Tree<>(sum, alphaClass, edgeSupplier));
		}
		return alphaSortings;
	}
	
	private List<List<Tree<V, E>>> gather(DirectedAcyclicGraph<V, E> scatteredCategories, Set<V> atoms, 
			List<V> topoOrder) {
		List<List<Tree<V, E>>> classifications = new ArrayList<>();
		List<V> inspectionOrder = new ArrayList<>(Lists.reverse(topoOrder));
		int lastInspectableIndex = BasicFunc.minIndexOf(atoms, inspectionOrder);
		inspectionOrder.subList(lastInspectableIndex + 1, inspectionOrder.size()).clear();
		for (int i = 0 ; i < inspectionOrder.size() ; i++) {
			V alphaClass = inspectionOrder.get(i);
			RootedInvertedGraph<V, E> alphaCategories = 
					WithinSetFunc.lowerSet(scatteredCategories, alphaClass, edgeSupplier);
			Set<V> alphaReachedAtoms = alphaCategories.getLeaves();
			if (WithinSetFunc.supremum(alphaCategories, alphaReachedAtoms).equals(alphaClass)) {
				List<Tree<V, E>> alphaSortings = scatter(alphaCategories);
				Set<V> unreachedAtoms = new HashSet<>(Sets.difference(atoms, alphaReachedAtoms));
				Set<V> nonAlphaInspectableElements = new HashSet<>(
						Sets.difference(scatteredCategories.vertexSet(), alphaCategories.vertexSet()));
				nonAlphaInspectableElements.removeAll(inspectionOrder.subList(0, i+1));
				DirectedAcyclicGraph<V, E> nonAlphaInspectableCategories = 
						WithinSetFunc.restriction(scatteredCategories, nonAlphaInspectableElements, edgeSupplier);
				V antiAlphaClass = WithinSetFunc.supremum(nonAlphaInspectableCategories, unreachedAtoms);
				if (antiAlphaClass != null) {
					inspectionOrder.remove(antiAlphaClass);
					RootedInvertedGraph<V, E> antiAlphaCategories = 
							WithinSetFunc.lowerSet(nonAlphaInspectableCategories, antiAlphaClass, edgeSupplier);
					List<Tree<V, E>> antiAlphaSortings = scatter(antiAlphaCategories);
					for (Tree<V, E> alphaSorting : alphaSortings) {
						for (Tree<V, E> antiAlphaSorting : antiAlphaSortings) {
							List<Tree<V, E>> dichotomy = new ArrayList<>();
							dichotomy.add(alphaSorting);
							dichotomy.add(antiAlphaSorting);
							classifications.add(dichotomy);
						}
					}
				}
				else {
					List<V> restrictedTopoOrder = new ArrayList<>(topoOrder);
					topoOrder.retainAll(nonAlphaInspectableElements);
					List<List<Tree<V, E>>> classificationsOfNonAlphas = 
							gather(nonAlphaInspectableCategories, unreachedAtoms, restrictedTopoOrder);
					for (Tree<V, E> alphaSorting : alphaSortings) {
						for (List<Tree<V, E>> nonAlphaClassification : classificationsOfNonAlphas) {
							List<Tree<V, E>> completeClassification = new ArrayList<>();
							completeClassification.add(alphaSorting);
							completeClassification.addAll(nonAlphaClassification);
							classifications.add(completeClassification);
						}
					}
				}
			}
		}
		return classifications;
	}

	@Override
	public boolean hasNext() {
		return treeIdx < trees.size();
	}

	@Override
	public Tree<V, E> next() {
		return trees.get(treeIdx++);
	}

}
