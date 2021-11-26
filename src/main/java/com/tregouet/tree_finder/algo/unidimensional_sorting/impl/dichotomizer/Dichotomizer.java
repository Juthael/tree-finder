package com.tregouet.tree_finder.algo.unidimensional_sorting.impl.dichotomizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.algo.unidimensional_sorting.IUnidimensionalSorter;
import com.tregouet.tree_finder.algo.unidimensional_sorting.utils.Functions;
import com.tregouet.tree_finder.data.Tree;
import com.tregouet.tree_finder.data.UpperSemilattice;
import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.utils.StructureInspector;

public class Dichotomizer<D extends IDichotomizable<D>, E> implements IUnidimensionalSorter<D, E> {
	
	private final List<Tree<D, E>> trees;
	private Iterator<Tree<D, E>> treeIte;
	private final List<D> topoOrderedSet;
	private final Set<D> minima;
	private final List<Set<D>> lowerSets;
	private final List<Set<D>> setEncodingInPowerSetOfMinima;
	
	public Dichotomizer(UpperSemilattice<D, E> alphas) 
			throws InvalidInputException {
		TransitiveReduction.INSTANCE.reduce(alphas);
		alphas.validate();
		topoOrderedSet = alphas.getTopologicalOrder();
		minima = alphas.getLeaves();
		lowerSets = new ArrayList<>();
		//make the upper semilattice atomistic
		//build topological list of lowersets
		//build semilattice encoding in power set of minima
		setEncodingInPowerSetOfMinima = new ArrayList<>();
		for (int i = 0 ; i < topoOrderedSet.size() ; i++) {
			D element = topoOrderedSet.get(i);
			Set<D> elementLowerSet = Functions.lowerSet(alphas, element); 
			Set<D> elementEncoding = 
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
		trees = sort(alphas);
		treeIte = trees.iterator();
	}

	@Override
	public boolean hasNext() {
		return treeIte.hasNext();
	}

	@Override
	public Tree<D, E> next() {
		return treeIte.next();
	}

	@Override
	public List<Tree<D, E>> getSortingTrees() {
		return trees;
	}
	
	private List<Tree<D, E>> sort(UpperSemilattice<D, E> alphas) {
		//returned set of sortings
		List<Tree<D, E>> alphaSortings = new ArrayList<>();
		//unnecessary but efficient shortcut
		if (StructureInspector.isATree(alphas)) {
			alphaSortings.add(new Tree<D, E>(alphas));
			return alphaSortings;
		}
		//start sorting
		D alphaClass = alphas.getRoot();
		Set<Set<D>> perfectPartitions = new HashSet<>();
		for (int i = 0 ; i < topoOrderedSet.size() - 1 ; i++) {
			//select betas as one kind of alphas
			D betaClass = topoOrderedSet.get(i);
			Set<D> reachedMinima = setEncodingInPowerSetOfMinima.get(i);
			Set<D> unreachedMinima = new HashSet<>(Sets.difference(minima, reachedMinima));
			UpperSemilattice<D, E> betas = 
					new UpperSemilattice<D, E>(
							alphas, lowerSets.get(i), betaClass, reachedMinima);
			UpperSemilattice<D, E> nonBetas;
			D unreachedMinimaSupremum = Functions.supremum(alphas, unreachedMinima);
			nonBetas = new UpperSemilattice<D, E>(alphas, 
					new HashSet<>(
							Sets.difference(
									Functions.lowerSet(alphas, unreachedMinimaSupremum), 
									betas.vertexSet())),  
					unreachedMinimaSupremum, unreachedMinima);
			for (Tree<D, E> betaSorting : sort(betas)) {
				for (Tree<D, E> nonBetaSorting : sort(nonBetas)) {
					boolean nonBetaContainsRebutters;
					boolean partitionIsPerfect;
					List<D> nonBetaClasses;
					if (nonBetaSorting.getRoot().equals(alphaClass)) {
						nonBetaClasses = Graphs.predecessorListOf(nonBetaSorting, alphaClass);
						nonBetaContainsRebutters = containsRebutters(nonBetaClasses);
					}
					else {
						nonBetaClasses = new ArrayList<>(1);
						nonBetaClasses.add(nonBetaSorting.getRoot());
						nonBetaContainsRebutters = false;
					}
					partitionIsPerfect = 
							(nonBetaContainsRebutters ? 
									false : perfectPartition(reachedMinima, nonBetaClasses));
					if (partitionIsPerfect) {
						Set<D> alphaKinds = new HashSet<>(nonBetaClasses);
						alphaKinds.add(betaClass);
						boolean newPartition = perfectPartitions.add(alphaKinds);
						if (newPartition) {
							alphaSortings.add(
									instantiateAlphaSortingTree(alphaClass, alphas, betaSorting, nonBetaSorting));
						}
					}
					else {
						nonBetaSorting.removeVertex(alphaClass);
						D antiBetaClass = betaClass.rebut();
						for (D nonBetaClass : nonBetaClasses)
							nonBetaSorting.addEdge(nonBetaClass, antiBetaClass);
						alphaSortings.add(
								instantiateAlphaSortingTree(alphaClass, alphas, betaSorting, nonBetaSorting));
					}
				}
			}
		}
		return alphaSortings;
	}
	
	private Tree<D, E> instantiateAlphaSortingTree(D alphaClass, UpperSemilattice<D, E> alphas, 
			Tree<D, E> betaSorting, Tree<D, E> nonBetaSorting) {
		Tree<D, E> alphaSortingTree;
		DirectedAcyclicGraph<D, E> alphaSorting = 
				Functions.cardinalSum(betaSorting, nonBetaSorting, alphas.getEdgeSupplier());
		Set<D> maxima = Functions.maxima(alphaSorting);
		alphaSorting.addVertex(alphaClass);
		for (D maximalElement : maxima) {
			if (!maximalElement.equals(alphaClass))
				alphaSorting.addEdge(maximalElement, alphaClass);
		}
		alphaSortingTree = new Tree<D, E>(alphaSorting, alphaClass, minima, null);
		return alphaSortingTree;
	}
	
	private boolean perfectPartition(Set<D> betaReachedMinima, List<D> nonBetaClasses) {
		Set<D> nonBetaReachedMinima = reachedMinima(nonBetaClasses);
		return emptyIntersection(betaReachedMinima, nonBetaReachedMinima);
	}
	
	private Set<D> reachedMinima(List<D> dList) {
		Set<D> reachedMinima = new HashSet<>();
		for (D d : dList) {
			reachedMinima.addAll(setEncodingInPowerSetOfMinima.get(topoOrderedSet.indexOf(d)));
		}
		return reachedMinima;
	}
	
	private boolean containsRebutters(Collection<D> nonBetaClasses) {
		for (D nonBetaClass : nonBetaClasses) {
			if (nonBetaClass.isRebutter())
				return false;
		}
		return true;
	}
	
	private static <A> boolean emptyIntersection(Set<A> set1, Set<A> set2) {
		for (A a : set1) {
			if (set2.contains(a))
				return false;
		}
		return true;
	}

}
