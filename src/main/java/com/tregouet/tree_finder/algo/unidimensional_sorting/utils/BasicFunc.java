package com.tregouet.tree_finder.algo.unidimensional_sorting.utils;

import java.util.List;
import java.util.Set;

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

}
