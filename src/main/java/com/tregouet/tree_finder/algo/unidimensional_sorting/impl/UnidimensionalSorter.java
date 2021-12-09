 package com.tregouet.tree_finder.algo.unidimensional_sorting.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;

import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.algo.unidimensional_sorting.IDichotomizable;
import com.tregouet.tree_finder.algo.unidimensional_sorting.IUnidimensionalSorter;
import com.tregouet.tree_finder.data.Tree;
import com.tregouet.tree_finder.data.UpperSemilattice;
import com.tregouet.tree_finder.utils.Functions;
import com.tregouet.tree_finder.utils.StructureInspector;

public class UnidimensionalSorter<D extends IDichotomizable<D>, E> implements IUnidimensionalSorter<D, E> {
	
	private List<Tree<D, E>> trees;
	private Iterator<Tree<D, E>> treeIte;
	private List<D> topoOrderedSet;
	private List<Set<D>> lowerSets;
	private List<Set<D>> setEncodingInPowerSetOfMinima;
	
	public UnidimensionalSorter(UpperSemilattice<D, E> alphas) 
			throws IOException {
		TransitiveReduction.INSTANCE.reduce(alphas);
		try {
			alphas.validate();
		}
		catch (DataFormatException e) {
			throw new IOException("UnidimensionalSorter() : invalid parameter");
		}
		setUpSorter(alphas);
	}
	
	protected UnidimensionalSorter(UpperSemilattice<D, E> alphas, boolean skipValidation) {
		TransitiveReduction.INSTANCE.reduce(alphas);
		setUpSorter(alphas);
	}	
	
	private static <A> boolean emptyIntersection(Set<A> set1, Set<A> set2) {
		for (A a : set1) {
			if (set2.contains(a))
				return false;
		}
		return true;
	}
	
	@Override
	public List<Tree<D, E>> getSortingTrees() {
		return trees;
	}	
	
	@Override
	public boolean hasNext() {
		return treeIte.hasNext();
	}	
	
	@Override
	public Tree<D, E> next() {
		return treeIte.next();
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
		/* A clean partition of alphas is a set of alpha subclasses such as
		 * 1/ no subclass is a "rebutter", i.e. the generated complement of another class 
		 * 2/ the intersection of the subclasses' respective encodings in the power set of minima is empty 
		 * 3/ the union of the subclasses' respective encodings in the power set of minima is the set 
		 * of minima in the semilattice of alphas.
		 */
		Set<D> alphaMinima = alphas.getLeaves();
		//because of recursivity
		List<D> alphaRestrictedTopoOrderedSet = restrictTopoOrderedSetTo(alphas);
		Set<Set<D>> cleanPartitions = new HashSet<>();
		//a beta class is one kind of alphas
		D betaClass;
		for (int i = 0 ; i < topoOrderedSet.indexOf(alphaClass) - 1 ; i++) {
			//select a beta class
			betaClass = alphaRestrictedTopoOrderedSet.get(i);
			if (betaClass != null) {
				Set<D> reachedMinima = setEncodingInPowerSetOfMinima.get(i);
				Set<D> unreachedMinima = new HashSet<>(Sets.difference(alphaMinima, reachedMinima));
				//build the semilattice of betas
				UpperSemilattice<D, E> betas = 
						new UpperSemilattice<D, E>(
								alphas, lowerSets.get(i), betaClass, reachedMinima);
				UpperSemilattice<D, E> nonBetas;
				D unreachedMinimaSupremum = Functions.supremum(alphas, unreachedMinima);
				//build the semilattice of non betas, which may have alpha class as a maximum
				nonBetas = new UpperSemilattice<D, E>(alphas, 
						new HashSet<>(
								Sets.difference(
										Functions.lowerSet(alphas, unreachedMinimaSupremum), 
										betas.vertexSet())),  
						unreachedMinimaSupremum, unreachedMinima);
				/* Each alpha sorting having betas as a kind of alphas contains a sorting of betas and a 
				 * sorting of non-betas.
				 */
				List<Tree<D, E>> betaSortings = sort(betas);
				Collection<Tree<D, E>> nonBetaSortings = new UnidimensionalSorter<D, E>(nonBetas, true).getSortingTrees();
				for (Tree<D, E> betaSorting : betaSortings) {
					for (Tree<D, E> immutableNonBetaSorting : nonBetaSortings) {
						//because rebutting mechanism can modify the non beta sorting tree
						Tree<D, E> nonBetaSorting = new Tree<D, E>(immutableNonBetaSorting);
						boolean nonBetaContainsRebutters;
						boolean partitionIsClean;
						/* If the semilattice of non-betas has alpha class as its maximum, then the non-beta 
						 * classes are its coatoms ; if not, the unique non-beta class is the semilattice's maximum. 
						 */
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
						partitionIsClean = 
								(nonBetaContainsRebutters ? 
										false : perfectPartition(reachedMinima, nonBetaClasses));
						if (partitionIsClean) {
							//then no need to instantiate a beta class rebutter
							Set<D> alphaKinds = new HashSet<>(nonBetaClasses);
							alphaKinds.add(betaClass);
							boolean newPartition = cleanPartitions.add(alphaKinds);
							if (newPartition) {
								alphaSortings.add(
										instantiateAlphaSortingTree(alphaClass, alphas, alphaMinima, 
												betaSorting, nonBetaSorting));
							}
						}
						else {
							//then the non-beta maximum is a beta class rebutter
							D antiBetaClass;
							boolean alphaClassToBeRemoved = nonBetaSorting.containsVertex(alphaClass);
							if (nonBetaClasses.size() == 1) {
								D nonBetaClass = nonBetaClasses.get(0);
								antiBetaClass = betaClass.complementThisWith(nonBetaClass);
								nonBetaSorting.replaceVertex(nonBetaClass, antiBetaClass);
								if (alphaClassToBeRemoved)
									nonBetaSorting.removeVertex(alphaClass);
							}
							else {
								antiBetaClass = betaClass.buildComplementOfThis(unreachedMinima);
								if (alphaClassToBeRemoved)
									nonBetaSorting.replaceVertex(alphaClass, antiBetaClass);
								else {
									nonBetaSorting.addVertex(antiBetaClass);
									for (D nonBetaClass : nonBetaClasses)
										nonBetaSorting.addEdge(nonBetaClass, antiBetaClass);
								}
							}
							alphaSortings.add(
									instantiateAlphaSortingTree(alphaClass, alphas, alphaMinima, 
											betaSorting, nonBetaSorting));
						}
					}
				}
			}
		}
		return alphaSortings;
	}
	
	private boolean containsRebutters(Collection<D> nonBetaClasses) {
		for (D nonBetaClass : nonBetaClasses) {
			if (nonBetaClass.isComplementary())
				return true;
		}
		return false;
	}
	
	private Tree<D, E> instantiateAlphaSortingTree(D alphaClass, UpperSemilattice<D, E> alphas, 
			Set<D> alphasMinima, Tree<D, E> betaSorting, Tree<D, E> nonBetaSorting) {
		Tree<D, E> alphaSortingTree;
		DirectedAcyclicGraph<D, E> alphaSorting = 
				Functions.cardinalSum(betaSorting, nonBetaSorting, alphas.getEdgeSupplier());
		Set<D> maxima = Functions.maxima(alphaSorting);
		alphaSorting.addVertex(alphaClass);
		for (D maximalElement : maxima) {
			if (!maximalElement.equals(alphaClass))
				alphaSorting.addEdge(maximalElement, alphaClass);
		}
		alphaSortingTree = new Tree<D, E>(alphaSorting, alphaClass, alphasMinima, null);
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
	
	private List<D> restrictTopoOrderedSetTo(UpperSemilattice<D, E> alphas){
		List<D> restrictedTopoOrderedSet = new ArrayList<>(topoOrderedSet.size());
		for (D element : topoOrderedSet) {
			if (alphas.containsVertex(element))
				restrictedTopoOrderedSet.add(element);
			else restrictedTopoOrderedSet.add(null);
		}
		return restrictedTopoOrderedSet;
	}
	
	private void setUpSorter(UpperSemilattice<D, E> alphas) {
		topoOrderedSet = alphas.getTopologicalOrder();
		Set<D> minima = alphas.getLeaves();
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

}
