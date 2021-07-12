package com.tregouet.tree_finder.data;

import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.error.InvalidTreeException;

public class InTree<V, E> extends DirectedAcyclicGraph<V, E> {

	private static final long serialVersionUID = 2206651329473240403L;
	private V root;
	private List<V> sortedLeaves;

	//Unsafe
	public InTree(V root, List<V> sortedLeaves, DirectedAcyclicGraph<V, E> source, Set<E> edges) {
		super(null, null, false);
		this.root = root;
		this.sortedLeaves = sortedLeaves;
		Graphs.addAllEdges(this, source, edges);
	}
	
	//Safe if last argument is 'true'
	public InTree(V root, List<V> sortedLeaves, DirectedAcyclicGraph<V, E> source, Set<E> edges, boolean validate) 
			throws InvalidTreeException {
		this(root, sortedLeaves, source, edges);
		if (validate)
			validate();
	}
	
	public List<V> getLeaves(){
		return sortedLeaves;
	}
	
	public V getRoot() {
		return root;
	}
	
	public void validate() throws InvalidTreeException {
		if (!ITreeFinder.isAnInTree(this))
			throw new InvalidTreeException();
	}

}
