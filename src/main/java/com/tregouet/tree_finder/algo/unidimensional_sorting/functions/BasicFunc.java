package com.tregouet.tree_finder.algo.unidimensional_sorting.functions;

import java.util.List;
import java.util.Set;

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

}
