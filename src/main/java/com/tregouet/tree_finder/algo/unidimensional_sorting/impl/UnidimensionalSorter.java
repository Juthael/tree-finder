package com.tregouet.tree_finder.algo.unidimensional_sorting.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.text.DefaultEditorKit.BeepAction;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tregouet.tree_finder.algo.unidimensional_sorting.IUnidimensionalSorter;
import com.tregouet.tree_finder.algo.unidimensional_sorting.utils.BasicFunc;
import com.tregouet.tree_finder.algo.unidimensional_sorting.utils.BetweenSetFunc;
import com.tregouet.tree_finder.algo.unidimensional_sorting.utils.WithinSetFunc;
import com.tregouet.tree_finder.data.RootedInvertedGraph;
import com.tregouet.tree_finder.data.Tree;

public class UnidimensionalSorter<V, E extends DefaultEdge> implements IUnidimensionalSorter<V, E> {

	private final Supplier<E> edgeSupplier;
	
	public UnidimensionalSorter(DirectedAcyclicGraph<V, E> dag, Supplier<E> edgeSupplier) {
		this.edgeSupplier = edgeSupplier;
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
		List<V> alphaSubRoots = WithinSetFunc.maxima(alphaSubCategories);
		if (alphaSubRoots.size() == 1) {
			V subAlphaClass = alphaSubRoots.get(0);
			RootedInvertedGraph<V, E> subAlphaCategories = 
					new RootedInvertedGraph<>(alphaSubCategories, subAlphaClass, edgeSupplier);
			List<Tree<V, E>> subAlphaSortings = slice(subAlphaCategories);
			for (Tree<V, E> sorting : subAlphaSortings) {
				BetweenSetFunc.ordinalSum(sorting, alphaClass, true);
				alphaSortings.add(sorting);
			}
		}
		else {
			Set<V> atoms = alphaCategories.getLeaves();
			List<V> subAlphasTopoOrder = alphaCategories.getTopologicalSortingOfVertices();
			subAlphasTopoOrder.remove(subAlphasTopoOrder.size() - 1);
			List<List<Tree<V, E>>> sortingsOfSubAlphas = discern(alphaSubCategories, atoms, subAlphasTopoOrder);
			for (List<Tree<V, E>> subAlphaSorting : sortingsOfSubAlphas) {
				DirectedAcyclicGraph<V, E> sum = BetweenSetFunc.cardinalSum(subAlphaSorting, edgeSupplier);
				BetweenSetFunc.ordinalSum(sum, alphaClass, true);
				alphaSortings.add(new Tree<>(sum, alphaClass, edgeSupplier));
			}
		}
		return alphaSortings;
	}
	
	private List<List<Tree<V, E>>> discern(DirectedAcyclicGraph<V, E> scatteredCategories, Set<V> atoms, 
			List<V> topoOrder) {
		List<List<Tree<V, E>>> sortings = new ArrayList<>();
		List<V> inspectionOrder = new ArrayList<>(Lists.reverse(topoOrder));
		int lastInspectableIndex = BasicFunc.maxIndexOf(atoms, inspectionOrder);
		inspectionOrder.subList(lastInspectableIndex + 1, inspectionOrder.size()).clear();
		for (V alphaClass : inspectionOrder) {
			RootedInvertedGraph<V, E> alphaCategories = 
					WithinSetFunc.lowerSet(scatteredCategories, alphaClass, edgeSupplier);
			List<Tree<V, E>> alphaSortings = slice(alphaCategories);
			if (alphaCategories.getLeaves().containsAll(atoms)) {
				sortings.add(alphaSortings);
			}
			else {
				Set<V> unreachedAtoms = new HashSet<>(Sets.difference(atoms, alphaCategories.getLeaves()));
			}
		}
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Tree<V, E> next() {
		// TODO Auto-generated method stub
		return null;
	}

}
