package com.tregouet.tree_finder.algo.unidimensional_sorting.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.algo.unidimensional_sorting.IUnidimensionalSorter;
import com.tregouet.tree_finder.algo.unidimensional_sorting.utils.Functions;
import com.tregouet.tree_finder.data.Tree;
import com.tregouet.tree_finder.data.UpperSemilattice;
import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.utils.StructureInspector;

public class UnidimensionalSorter<V, E> implements IUnidimensionalSorter<V, E> {
	
	private final Set<Tree<V, E>> trees;
	private Iterator<Tree<V, E>> treeIte;

	public UnidimensionalSorter(UpperSemilattice<V, E> alphas) 
			throws InvalidInputException {
		TransitiveReduction.INSTANCE.reduce(alphas);
		alphas.validate();
		trees = sort(alphas);
		treeIte = trees.iterator();
	}
	
	@Override
	public Set<Tree<V, E>> getSortingTrees() {
		return trees;
	}

	@Override
	public boolean hasNext() {
		return treeIte.hasNext();
	}

	@Override
	public Tree<V, E> next() {
		return treeIte.next();
	}

	private Set<Tree<V, E>> sort(UpperSemilattice<V, E> alphas) {
		//returned set of sortings
		Set<Tree<V, E>> alphaSortings = new HashSet<>();
		List<V> topoOrderedSet = alphas.getTopologicalOrder();
		Set<V> minima = alphas.getLeaves();
		List<Set<V>> lowerSets = new ArrayList<>();
		//make the upper semilattice atomistic
		//build topological list of lowersets
		//build semilattice encoding in power set of minima
		List<Set<V>> setEncodingInPowerSetOfMinima = new ArrayList<>();
		for (int i = 0 ; i < topoOrderedSet.size() ; i++) {
			V element = topoOrderedSet.get(i);
			Set<V> elementLowerSet = Functions.lowerSet(alphas, element); 
			Set<V> elementEncoding = 
					new HashSet<>(
							Sets.intersection(elementLowerSet, minima));
			if (setEncodingInPowerSetOfMinima.contains(elementEncoding)) {
				topoOrderedSet.remove(i);
				Functions.removeVertexAndPreserveConnectivity(alphas, element);
				i--;
			}
			else {
				lowerSets.add(elementLowerSet);
				setEncodingInPowerSetOfMinima.add(elementEncoding);
			}
		}
		//unnecessary but efficient shortcut
		if (StructureInspector.isATree(alphas)) {
			alphaSortings.add(new Tree<V, E>(alphas));
			return alphaSortings;
		}		
		//start sorting
		V alphaClass = alphas.getRoot();
		boolean[] skipElement = new boolean[topoOrderedSet.size()];
		for (int i = 0 ; i < topoOrderedSet.size() - 1 ; i++) {
			if (!skipElement[i]) {
				//select betas as one kind of alphas
				V betaClass = topoOrderedSet.get(i);
				Set<V> reachedMinima = setEncodingInPowerSetOfMinima.get(i);
				Set<V> unreachedMinima = new HashSet<>(Sets.difference(minima, reachedMinima));
				UpperSemilattice<V, E> betas = 
						new UpperSemilattice<V, E>(
								alphas, lowerSets.get(i), betaClass, reachedMinima);
				UpperSemilattice<V, E> nonBetas;
				int antiBetaClassIdx = setEncodingInPowerSetOfMinima.indexOf(unreachedMinima);
				if (antiBetaClassIdx != -1) {
					//then there exists a beta/non-beta dichotomy
					V antiBetaClass = topoOrderedSet.get(antiBetaClassIdx);
					nonBetas = new UpperSemilattice<V, E>(
							alphas, lowerSets.get(antiBetaClassIdx), antiBetaClass, unreachedMinima);
					skipElement[antiBetaClassIdx] = true;
				}
				else {
					//then the beta/non-beta dichotomy does not exist
					V unreachedMinimaSupremum = Functions.supremum(alphas, unreachedMinima);
					nonBetas = new UpperSemilattice<V, E>(alphas, 
							new HashSet<>(
									Sets.difference(
											Functions.lowerSet(alphas, unreachedMinimaSupremum), 
											betas.vertexSet())),  
							unreachedMinimaSupremum, unreachedMinima); 
				}
				//each alpha sorting based on betas contains a beta sorting and a sorting of non-betas
				for (Tree<V, E> betaSorting : sort(betas)) {
					for (Tree<V, E> nonBetaSorting : sort(nonBetas)) {
						DirectedAcyclicGraph<V, E> alphaSorting = 
								Functions.cardinalSum(betaSorting, nonBetaSorting, alphas.getEdgeSupplier());
						Set<V> maxima = Functions.maxima(alphaSorting);
						alphaSorting.addVertex(alphaClass);
						for (V maximalElement : maxima) {
							if (!maximalElement.equals(alphaClass))
								alphaSorting.addEdge(maximalElement, alphaClass);
						}
						List<V> alphaSortingTopoOrderedSet = new ArrayList<>(topoOrderedSet);
						alphaSortingTopoOrderedSet.retainAll(alphaSorting.vertexSet());
						Tree<V, E> alphaSortingTree = 
								new Tree<V, E>(alphaSorting, alphaClass, minima, alphaSortingTopoOrderedSet);
						alphaSortings.add(alphaSortingTree);
					}
				}
			}
		}
		return alphaSortings;
	}

}
