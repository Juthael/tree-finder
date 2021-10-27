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
	
	//The first parameter MUST be rooted and inverted or an exception will be thrown 
	public UnidimensionalSorter(DirectedAcyclicGraph<V, E> dag, Supplier<E> edgeSupplier) throws InvalidInputException {
		if (!StructureInspector.isARootedInvertedDirectedAcyclicGraph(dag))
			throw new InvalidInputException("The first parameter is not a rooted inverted graph.");
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
		trees = slice(rootedInverted);
	}
	
	public UnidimensionalSorter(RootedInvertedGraph<V, E> rootedInverted, Supplier<E> edgeSupplier) {
		TransitiveReduction.INSTANCE.reduce(rootedInverted);
		this.edgeSupplier = edgeSupplier;
		trees = slice(rootedInverted);
		
	}
	
	private List<Tree<V, E>> slice(RootedInvertedGraph<V, E> alphaCategories) {
		List<Tree<V, E>> alphaSortings = new ArrayList<>();
		if (alphaCategories.vertexSet().size() == 1) {
			Tree<V, E> singleton = 
					new Tree<>(alphaCategories, edgeSupplier);
			alphaSortings.add(singleton);
			return alphaSortings;
		}
		V alphaClass = alphaCategories.getRoot();
		DirectedAcyclicGraph<V, E> alphaSubCategories = 
				WithinSetFunc.uprooted(alphaCategories, alphaClass, edgeSupplier);
		List<V> subAlphaMaxima = WithinSetFunc.maxima(alphaSubCategories);
		if (subAlphaMaxima.size() == 1) {
			V subAlphaClass = subAlphaMaxima.get(0);
			RootedInvertedGraph<V, E> subAlphaCategories = 
					new RootedInvertedGraph<>(alphaSubCategories, subAlphaClass, edgeSupplier);
			alphaSortings.addAll(slice(subAlphaCategories));
		}
		else {
			Set<V> atoms = alphaCategories.getLeaves();
			List<V> subAlphasTopoOrder = alphaCategories.getTopologicalSortingOfVertices();
			subAlphasTopoOrder.remove(subAlphasTopoOrder.size() - 1);
			List<List<Tree<V, E>>> sortingsOfSubAlphas = discern(alphaSubCategories, atoms, subAlphasTopoOrder);
			for (List<Tree<V, E>> subAlphaSorting : sortingsOfSubAlphas) {
				DirectedAcyclicGraph<V, E> sum = BetweenSetsFunc.cardinalSum(subAlphaSorting, edgeSupplier);
				BetweenSetsFunc.ordinalSum(sum, alphaClass, true);
				alphaSortings.add(new Tree<>(sum, alphaClass, edgeSupplier));
			}
		}
		return alphaSortings;
	}
	
	private List<List<Tree<V, E>>> discern(DirectedAcyclicGraph<V, E> scatteredCategories, Set<V> atoms, 
			List<V> topoOrder) {
		List<List<Tree<V, E>>> setsOfDiscernedClasses = null;
		List<V> inspectionOrder = new ArrayList<>(Lists.reverse(topoOrder));
		int lastInspectableIndex = BasicFunc.minIndexOf(atoms, inspectionOrder);
		inspectionOrder.subList(lastInspectableIndex + 1, inspectionOrder.size()).clear();
		for (V alphaClass : inspectionOrder) {
			RootedInvertedGraph<V, E> alphaCategories = 
					WithinSetFunc.lowerSet(scatteredCategories, alphaClass, edgeSupplier);
			List<Tree<V, E>> alphaSortings = slice(alphaCategories);
			Set<V> reachedAtoms = alphaCategories.getLeaves();
			if (reachedAtoms.containsAll(atoms)) {
				setsOfDiscernedClasses = new ArrayList<>();
				setsOfDiscernedClasses.add(alphaSortings);
			}
			else {
				Set<V> unreachedAtoms = new HashSet<>(Sets.difference(atoms, reachedAtoms));
				List<V> restrictedTopoOrder = new ArrayList<>(topoOrder);
				restrictedTopoOrder.retainAll(WithinSetFunc.finishingSubset(scatteredCategories, unreachedAtoms));
				restrictedTopoOrder.removeAll(WithinSetFunc.nonMinimalUpperBounds(scatteredCategories, unreachedAtoms));
				DirectedAcyclicGraph<V, E> relevantNonAlphaCategories = 
						WithinSetFunc.restriction(scatteredCategories, restrictedTopoOrder, edgeSupplier);
				List<V> nonAlphaMaxima = WithinSetFunc.maxima(relevantNonAlphaCategories);
				if (nonAlphaMaxima.size() == 1) {
					V antiAlphaClass = nonAlphaMaxima.get(0); 
					RootedInvertedGraph<V, E> antiAlphaCategories = 
							new RootedInvertedGraph<V, E>(
									relevantNonAlphaCategories, antiAlphaClass, unreachedAtoms, restrictedTopoOrder, 
									edgeSupplier);
					List<Tree<V, E>> antiAlphaSortings = slice(antiAlphaCategories);
					setsOfDiscernedClasses = BasicFunc.cartesianProduct(alphaSortings, antiAlphaSortings);
				}
				else {
					setsOfDiscernedClasses = new ArrayList<>();
					List<List<Tree<V, E>>> setsOfDiscernedNonAlphaClasses = 
							discern(relevantNonAlphaCategories, unreachedAtoms, restrictedTopoOrder);
					for (List<Tree<V, E>> discernedNonAlphaClasses : setsOfDiscernedNonAlphaClasses) {
						for (Tree<V, E> alphaSorting : alphaSortings) {
							List<Tree<V, E>> discernedClasses = new ArrayList<>();
							discernedClasses.add(alphaSorting);
							discernedClasses.addAll(discernedNonAlphaClasses);
							setsOfDiscernedClasses.add(discernedClasses);
						}
					}
				}
			}
		}
		return setsOfDiscernedClasses;
	}

	@Override
	public boolean hasNext() {
		return treeIdx < trees.size() - 1;
	}

	@Override
	public Tree<V, E> next() {
		return trees.get(treeIdx++);
	}

}
