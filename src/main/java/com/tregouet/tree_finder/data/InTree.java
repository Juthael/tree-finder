package com.tregouet.tree_finder.data;

import java.util.List;

import org.jgrapht.graph.DirectedAcyclicGraph;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.error.InvalidTreeException;

public class InTree<V, E> extends DirectedAcyclicGraph<V, E> {

	private static final long serialVersionUID = 2206651329473240403L;
	private V root;
	private List<V> sortedLeaves;

	public InTree(V root, List<V> sortedLeaves) {
		super(null, null, false);
		this.root = root;
		this.sortedLeaves = sortedLeaves;
	}
	
	public void validate() throws InvalidTreeException {
		if (!ITreeFinder.isAnInTree(this))
			throw new InvalidTreeException();
	}
	
	public V getRoot() {
		return root;
	}
	
	public List<V> getLeaves(){
		return sortedLeaves;
	}

}
