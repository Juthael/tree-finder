package com.tregouet.tree_finder.data;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.graph.DirectedAcyclicGraph;

import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.utils.StructureInspector;

public class Tree<V, E> extends RootedInvertedGraph<V, E> {

	private static final long serialVersionUID = 2206651329473240403L;

	/* 
	 * No transitive reduction must have been operated on first parameter. 
	 * UNSAFE. The restriction of the first parameter to the second parameter MUST be a tree. 
	 */
	public Tree(DirectedAcyclicGraph<V, E> source, Collection<V> treeVertices, V root, Set<V> leaves) {
		super(source, treeVertices, root, leaves);
	}
	
	//UNSAFE. The parameter MUST be a tree.
	public Tree(DirectedAcyclicGraph<V, E> tree, V root, Supplier<E> edgeSupplier) {
		super(tree, root, edgeSupplier);
	}
	
	//UNSAFE. The parameter MUST be a tree.
	public Tree(RootedInvertedGraph<V, E> tree, Supplier<E> edgeSupplier) {
		super(tree, edgeSupplier);
	}		

	public void validate() throws InvalidInputException {
		if (!StructureInspector.isATree(this))
			throw new InvalidInputException("ClassificationTree() : parameters do not allow the instantiation "
					+ "of a valid classification tree.");
	}

}
