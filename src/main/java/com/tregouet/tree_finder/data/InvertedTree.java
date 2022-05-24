package com.tregouet.tree_finder.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.utils.Functions;
import com.tregouet.tree_finder.utils.StructureInspector;

public class InvertedTree<V, E> extends InvertedUpperSemilattice<V, E> {

	private static final long serialVersionUID = 2206651329473240403L;
	private Double[][] entropyReductionMatrix = null;

	/* 
	 * No transitive reduction must have been operated on first parameter. 
	 * UNSAFE. The restriction of the first parameter to the second parameter MUST be a tree. 
	 */
	public InvertedTree(DirectedAcyclicGraph<V, E> source, Collection<V> treeVertices, V root, Set<V> leaves) {
		super(source, treeVertices, root, leaves);
	}
	
	//UNSAFE. The first parameter MUST be a tree.
	public InvertedTree(DirectedAcyclicGraph<V, E> tree, V root, Set<V> leaves, List<V> topoOrderedSet) {
		super(tree, root, leaves, topoOrderedSet);
	}
	
	//UNSAFE. The parameter MUST be a tree.
	public InvertedTree(InvertedRooted<V, E> tree) {
		super(tree);
	}	
	
	public Double[][] getEntropyReductionMatrix(){
		if (entropyReductionMatrix == null)
			setUpEntropyReductionMatrix();
		return entropyReductionMatrix;
	}

	@Override
	public void validate() throws DataFormatException {
		if (!StructureInspector.isAnInvertedTree(this))
			throw new DataFormatException("parameters do not allow the instantiation "
					+ "of a valid tree.");
	}
	
	private void setUpEntropyReductionMatrix() {
		List<V> topoOrderedSet = getTopologicalOrder();
		Set<V> leaves = getLeaves();
		int setCardinal = topoOrderedSet.size();
		entropyReductionMatrix = new Double[setCardinal][setCardinal];
		double[] entropy = new double[setCardinal];
		for (int i = 0 ; i < setCardinal ; i++) {
			double iCardinal = (double) 
					Sets.intersection(Functions.lowerSet(this, topoOrderedSet.get(i)), leaves).size();
			entropy[i] = -binaryLogarithm(1 / iCardinal);		
		}
		for (int i = 0 ; i < setCardinal ; i++) {
			entropyReductionMatrix[i][i] = 0.0;
			V iVertex = topoOrderedSet.get(i);
			for (int j = i+1 ; j < setCardinal ; j++) {
				V jVertex = topoOrderedSet.get(j);
				if (isStrictLowerBoundOf(iVertex, jVertex)) {
					entropyReductionMatrix[i][j] = entropy[j] - entropy[i];
				}
			}
		}
	}
	
	private static double binaryLogarithm(double arg) {
		return Math.log10(arg)/Math.log10(2);
	}
	
	public boolean isStrictLowerBoundOf(V iVertex, V jVertex) {
		V nextSuccessor = iVertex;
		while (!nextSuccessor.equals(getRoot())) {
			nextSuccessor = Graphs.successorListOf(this, nextSuccessor).get(0);
			if (nextSuccessor.equals(jVertex))
				return true;
		}
		return false;
	}
	
	public E outgoingEdgeOf(V element) {
		for (E edge : edgeSet()) {
			if (getEdgeSource(edge).equals(element))
				return edge;
		}
		return null;
	}	
	
	public Set<V> upArrowRelatedWith(V element) {
		if (element.equals(root))
			return null;
		V successor = getEdgeTarget(outgoingEdgeOf(element));
		Set<V> upArrowRelated = new HashSet<>();
		for (V v : Graphs.predecessorListOf(this, successor)) {
			if (!v.equals(element))
				upArrowRelated.add(v);
		}
		return upArrowRelated;
	}	

}
