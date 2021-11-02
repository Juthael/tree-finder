package com.tregouet.tree_finder.data;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.utils.StructureInspector;

public class Tree<V, E extends DefaultEdge> extends UpperSemilattice<V, E> {

	private static final long serialVersionUID = 2206651329473240403L;

	/* 
	 * No transitive reduction must have been operated on first parameter. 
	 * UNSAFE. The restriction of the first parameter to the second parameter MUST be a tree. 
	 */
	public Tree(DirectedAcyclicGraph<V, E> source, Collection<V> treeVertices, V root, Set<V> leaves) {
		super(source, treeVertices, root, leaves);
	}
	
	//UNSAFE. The first parameter MUST be a tree.
	public Tree(DirectedAcyclicGraph<V, E> tree, V root, Set<V> leaves, List<V> topoOrderedSet) {
		super(tree, root, leaves, topoOrderedSet);
	}
	
	//UNSAFE. The parameter MUST be a tree.
	public Tree(RootedInvertedGraph<V, E> tree) {
		super(tree);
	}		

	@Override
	public void validate() throws InvalidInputException {
		if (!StructureInspector.isATree(this))
			throw new InvalidInputException("ClassificationTree() : parameters do not allow the instantiation "
					+ "of a valid classification tree.");
	}

}
