package com.tregouet.tree_finder.data;

import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;

import org.jgrapht.graph.DirectedAcyclicGraph;

import com.tregouet.tree_finder.utils.StructureInspector;

public class Tree<V, E> extends Rooted<V, E> {

	private static final long serialVersionUID = 8707995759989510595L;
	
	public Tree(DirectedAcyclicGraph<V, E> tree, V root, Set<V> leaves, List<V> topoOrder) {
		super(tree, root, leaves, topoOrder);
	}
	
	@Override
	public void validate() throws DataFormatException {
		if (!StructureInspector.isATree(this))
			throw new DataFormatException("parameters do not allow the instantiation "
					+ "of a valid tree.");
	}	

	public E incomingEdgeOf(V element) {
		for (E edge : edgeSet()) {
			if (getEdgeTarget(edge).equals(element))
				return edge;
		}
		return null;
	}
	
	

}
