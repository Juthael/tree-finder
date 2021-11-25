package com.tregouet.tree_finder.algo.unidimensional_sorting.impl.dichotomizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveReduction;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.algo.unidimensional_sorting.IUnidimensionalSorter;
import com.tregouet.tree_finder.algo.unidimensional_sorting.utils.Functions;
import com.tregouet.tree_finder.data.Tree;
import com.tregouet.tree_finder.data.UpperSemilattice;
import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.utils.StructureInspector;

public class Dichotomizer<D, E> implements IUnidimensionalSorter<D, E> {
	
	private final Set<Tree<D, E>> trees;
	private Iterator<Tree<D, E>> treeIte;
	
	public Dichotomizer(UpperSemilattice<IDichotomizable<D>, E> alphas) 
			throws InvalidInputException {
		TransitiveReduction.INSTANCE.reduce(alphas);
		alphas.validate();
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
	public Set<Tree<D, E>> getSortingTrees() {
		return trees;
	}
	
	private Set<Tree<IDichotomizable<D>, E>> sort(UpperSemilattice<IDichotomizable<D>, E> alphas) {
		//returned set of sortings
		Set<Tree<IDichotomizable<D>, E>> alphaSortings = new HashSet<>();
		List<IDichotomizable<D>> topoOrderedSet = alphas.getTopologicalOrder();
		Set<IDichotomizable<D>> minima = alphas.getLeaves();
		List<Set<IDichotomizable<D>>> lowerSets = new ArrayList<>();
		//make the upper semilattice atomistic
		//build topological list of lowersets
		//build semilattice encoding in power set of minima
		List<Set<IDichotomizable<D>>> setEncodingInPowerSetOfMinima = new ArrayList<>();
		for (int i = 0 ; i < topoOrderedSet.size() ; i++) {
			IDichotomizable<D> element = topoOrderedSet.get(i);
			Set<IDichotomizable<D>> elementLowerSet = Functions.lowerSet(alphas, element); 
			Set<IDichotomizable<D>> elementEncoding = 
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
			alphaSortings.add(new Tree<IDichotomizable<D>, E>(alphas));
			return alphaSortings;
		}
		//start sorting
		IDichotomizable<D> alphaClass = alphas.getRoot();
		boolean[] skipElement = new boolean[topoOrderedSet.size()];
		for (int i = 0 ; i < topoOrderedSet.size() - 1 ; i++) {
			if (!skipElement[i]) {
				//select betas as one kind of alphas
				IDichotomizable<D> betaClass = topoOrderedSet.get(i);
				Set<IDichotomizable<D>> reachedMinima = setEncodingInPowerSetOfMinima.get(i);
				Set<IDichotomizable<D>> unreachedMinima = new HashSet<>(Sets.difference(minima, reachedMinima));
				UpperSemilattice<IDichotomizable<D>, E> betas = 
						new UpperSemilattice<IDichotomizable<D>, E>(
								alphas, lowerSets.get(i), betaClass, reachedMinima);
				UpperSemilattice<IDichotomizable<D>, E> nonBetas;
				IDichotomizable<D> unreachedMinimaSupremum = Functions.supremum(alphas, unreachedMinima);
				nonBetas = new UpperSemilattice<IDichotomizable<D>, E>(alphas, 
						new HashSet<>(
								Sets.difference(
										Functions.lowerSet(alphas, unreachedMinimaSupremum), 
										betas.vertexSet())),  
						unreachedMinimaSupremum, unreachedMinima);
				//determine non-beta maxima
				List<IDichotomizable<D>> nonBetaClasses = new ArrayList<>();
				if (unreachedMinimaSupremum.equals(alphaClass))
					nonBetaClasses.addAll(Graphs.predecessorListOf(nonBetas, alphaClass));
				else nonBetaClasses.add(unreachedMinimaSupremum);
				//determine if beta class / non-beta classes is a perfect division of alphas
				boolean perfectDivision = true;
				int nonBetaClassIdx = 0;
				while (perfectDivision && nonBetaClassIdx < nonBetaClasses.size()) {
					IDichotomizable<D> nonBetaClass = nonBetaClasses.get(nonBetaClassIdx);
					Set<IDichotomizable<D>> nonBetaMaxReachedMinima = 
							setEncodingInPowerSetOfMinima.get(topoOrderedSet.indexOf(nonBetaClass));
					if (!emptyIntersection(reachedMinima, nonBetaMaxReachedMinima)) {
						
					}
				}
				
			}
		}
	}
	
	private static <A> boolean emptyIntersection(Set<A> set1, Set<A> set2) {
		for (A a : set1) {
			if (set2.contains(a))
				return false;
		}
		return true;
	}

}
