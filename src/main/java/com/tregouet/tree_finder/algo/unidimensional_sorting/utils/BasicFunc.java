package com.tregouet.tree_finder.algo.unidimensional_sorting.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.util.Pair;

import com.tregouet.tree_finder.data.Tree;

public class BasicFunc {

	private BasicFunc() {
	}
	
	public static <V> int maxIndexOf(Set<V> targetElements, List<V> listOfElements) {
		int maxIdx = -1;
		for (V element : targetElements) {
			int elementIdx = listOfElements.indexOf(element);
			if (elementIdx > maxIdx)
				maxIdx = elementIdx;
		}
		return maxIdx;
	}
	
	public static <V, E> Set<Pair<Tree<V, E>, Tree<V, E>>> cartesianProduct(Set<Tree<V, E>> treeSet1, 
			Set<Tree<V, E>> treeSet2) {
		Set<Pair<Tree<V, E>, Tree<V, E>>> pairsOfTrees = new HashSet<>();
		for (Tree<V, E> firstPairElement : treeSet1) {
			for (Tree<V, E> secondPairElement : treeSet2)
				pairsOfTrees.add(new Pair<Tree<V,E>, Tree<V,E>>(firstPairElement, secondPairElement));
		}
		return pairsOfTrees;
	}

}
