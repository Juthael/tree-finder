package com.tregouet.tree_finder.data;

import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedAcyclicGraph;

import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.utils.StructureInspector;

public class Tree<V, E> extends RootedInvertedGraph<V, E> {

	private static final long serialVersionUID = 2206651329473240403L;

	/* 
	 * No transitive reduction must have been operated on first parameter. 
	 * UNSAFE. The restriction of the first parameter to the second parameter MUST be a tree. 
	 */
	public Tree(DirectedAcyclicGraph<V, E> upperSemilattice, List<V> treeVertices, V root, Set<V> leaves) {
		super(upperSemilattice, treeVertices, root, leaves);
	}	
	
	/* 
	 * No transitive reduction must have been operated on first parameter. 
	 * UNSAFE. The restriction of the first parameter to the second parameter MUST be a tree.
	 */
	public Tree(DirectedAcyclicGraph<V, E> upperSemilattice, Set<V> treeVertices, V root, Set<V> leaves) {
		super(upperSemilattice, treeVertices, root, leaves);
	}
	
	//UNSAFE. The restriction of the source's relation to the last parameter MUST be a tree.
	public Tree(V root, Set<V> leaves, DirectedAcyclicGraph<V, E> source, Set<E> edges) {
		super(root, leaves, source, edges);
	}
	
	//Safe if last argument is 'true'
	public Tree(V root, Set<V> leaves, DirectedAcyclicGraph<V, E> source, Set<E> edges, boolean validate) 
			throws InvalidInputException {
		this(root, leaves, source, edges);
		if (validate)
			validate();
	}	

	public void validate() throws InvalidInputException {
		if (!StructureInspector.isATree(this))
			throw new InvalidInputException("ClassificationTree() : parameters do not allow the instantiation "
					+ "of a valid classification tree.");
	}

}
