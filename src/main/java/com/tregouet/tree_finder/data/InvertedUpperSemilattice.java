package com.tregouet.tree_finder.data;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;

import org.jgrapht.graph.DirectedAcyclicGraph;

import com.tregouet.tree_finder.utils.StructureInspector;

public class InvertedUpperSemilattice<V, E> extends InvertedRooted<V, E> {

	private static final long serialVersionUID = -4915255891481664388L;

	public InvertedUpperSemilattice(DirectedAcyclicGraph<V, E> dag, Collection<V> restriction, V root, Set<V> leaves) {
		super(dag, restriction, root, leaves);
	}

	public InvertedUpperSemilattice(DirectedAcyclicGraph<V, E> rootedInverted, V root, Set<V> leaves,
			List<V> topoOrder) {
		super(rootedInverted, root, leaves, topoOrder);
	}

	protected InvertedUpperSemilattice(InvertedRooted<V, E> rootedInverted) {
		super(rootedInverted);
	}

	@Override
	public void validate() throws DataFormatException {
		if (!StructureInspector.isAnUpperSemilattice(this))
			throw new DataFormatException("ClassificationTree() : parameters do not allow the instantiation "
					+ "of a valid classification tree.");
	}


}
