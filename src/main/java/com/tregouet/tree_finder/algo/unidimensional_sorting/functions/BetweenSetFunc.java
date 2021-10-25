package com.tregouet.tree_finder.algo.unidimensional_sorting.functions;

import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

public class BetweenSetFunc {

	private BetweenSetFunc() {
	}
	
	//Side effect on first parameter. Operates on a transitive reduction.
	public static <V, E extends DefaultEdge> void ordinalSum(DirectedAcyclicGraph<V, E> dag, V newMaximum, 
			boolean operateOnTransitiveReduction){
		if (operateOnTransitiveReduction) {
			Set<V> maxima = WithinSetFunc.maxima(dag);
			dag.addVertex(newMaximum);
			for (V maximal : maxima)
				dag.addEdge(maximal, newMaximum);
		}
		else {
			Set<V> elements = dag.vertexSet();
			dag.addVertex(newMaximum);
			for (V element : elements) {
				dag.addEdge(element, newMaximum);
			}
		}
	}
	
	public static <V, E extends DefaultEdge> DirectedAcyclicGraph<V, E> cardinalSum(
			Set<DirectedAcyclicGraph<V, E>> dags, Supplier<E> edgeSupplier) {
		DirectedAcyclicGraph<V, E> cardinalSum = new DirectedAcyclicGraph<>(null, edgeSupplier, false);
		for (DirectedAcyclicGraph<V, E> dag : dags)
			Graphs.addAllEdges(cardinalSum, dag, dag.edgeSet());
		return cardinalSum;
	}

}
