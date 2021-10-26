package com.tregouet.tree_finder.algo.unidimensional_sorting.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.tregouet.tree_finder.data.Tree;

public class BasicFunc {

	private BasicFunc() {
	}
	
	public static <V> int minIndexOf(Set<V> targetElements, List<V> listOfElements) {
		int minIdx = listOfElements.size() - 1;
		for (V element : targetElements) {
			int elementIdx = listOfElements.indexOf(element);
			if (elementIdx < minIdx)
				minIdx = elementIdx;
		}
		return minIdx;
	}
	
	public static <V, E> List<List<Tree<V, E>>> cartesianProduct(List<Tree<V, E>> treeSet1, 
			List<Tree<V, E>> treeSet2) {
		List<List<Tree<V, E>>> pairsOfTrees = new ArrayList<>();
		for (Tree<V, E> firstTree : treeSet1) {
			for (Tree<V, E> secondTree : treeSet2){
				List<Tree<V, E>> pairOfTrees = new ArrayList<>();
				pairOfTrees.add(firstTree);
				pairOfTrees.add(secondTree);
				pairsOfTrees.add(pairOfTrees);
			}
		}
		return pairsOfTrees;
	}

}
