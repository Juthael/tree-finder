 package com.tregouet.tree_finder.algo.unidimensional_sorting.impl.dichotomizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.algo.unidimensional_sorting.IUnidimensionalSorter;
import com.tregouet.tree_finder.algo.unidimensional_sorting.impl.AbstractSorter;
import com.tregouet.tree_finder.algo.unidimensional_sorting.utils.Functions;
import com.tregouet.tree_finder.data.Tree;
import com.tregouet.tree_finder.data.UpperSemilattice;
import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.utils.StructureInspector;

public class Dichotomizer<D extends IDichotomizable<D>, E>
	extends AbstractSorter<D, E> implements IUnidimensionalSorter<D, E> {
	
	private List<D> topoOrderedSet;
	private Set<D> minima;
	private List<Set<D>> lowerSets;
	private List<Set<D>> setEncodingInPowerSetOfMinima;
	
	public Dichotomizer(UpperSemilattice<D, E> alphas) 
			throws InvalidInputException {
		super(alphas);
		setUpDichotomizer(alphas);
	}
	
	protected Dichotomizer(UpperSemilattice<D, E> alphas, boolean skipValidation) {
		super(alphas, skipValidation);
		setUpDichotomizer(alphas);
	}	
	
	@Override
	protected List<Tree<D, E>> sort(UpperSemilattice<D, E> alphas) {
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
		Set<Set<D>> cleanPartitions = new HashSet<>();
		for (int i = 0 ; i < topoOrderedSet.size() - 1 ; i++) {
			//select betas as one kind of alphas
			D betaClass = topoOrderedSet.get(i);
			Set<D> reachedMinima = setEncodingInPowerSetOfMinima.get(i);
			Set<D> unreachedMinima = new HashSet<>(Sets.difference(minima, reachedMinima));
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
			for (Tree<D, E> betaSorting : sort(betas)) {
				for (Tree<D, E> nonBetaSorting : new Dichotomizer<D, E>(nonBetas, true).getSortingTrees()) {
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
									instantiateAlphaSortingTree(alphaClass, alphas, betaSorting, nonBetaSorting));
						}
					}
					else {
						//then the non-beta maximum is a beta class rebutter
						nonBetaSorting.removeVertex(alphaClass);
						D antiBetaClass;
						if (nonBetaClasses.size() ==1) {
							D nonBetaClass = nonBetaClasses.get(0);
							antiBetaClass = betaClass.rebutWith(nonBetaClass);
							List<D> nonBetaClassPredecessors = Graphs.predecessorListOf(nonBetaSorting, nonBetaClass);
							nonBetaSorting.removeVertex(nonBetaClass);
							nonBetaSorting.addVertex(antiBetaClass);
							for(D nonBetaClassPredecessor : nonBetaClassPredecessors) {
								nonBetaSorting.addEdge(nonBetaClassPredecessor, antiBetaClass);
							}
						}
						else {
							antiBetaClass = betaClass.rebut();
							nonBetaSorting.addVertex(antiBetaClass);
							for (D nonBetaClass : nonBetaClasses)
								nonBetaSorting.addEdge(nonBetaClass, antiBetaClass);
						}
						
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
	
	private void setUpDichotomizer(UpperSemilattice<D, E> alphas) {
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

}
