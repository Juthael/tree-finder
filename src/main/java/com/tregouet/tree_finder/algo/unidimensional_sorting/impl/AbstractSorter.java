package com.tregouet.tree_finder.algo.unidimensional_sorting.impl;

import java.util.Collection;
import java.util.Iterator;

import org.jgrapht.alg.TransitiveReduction;

import com.tregouet.tree_finder.algo.unidimensional_sorting.IUnidimensionalSorter;
import com.tregouet.tree_finder.data.Tree;
import com.tregouet.tree_finder.data.UpperSemilattice;
import com.tregouet.tree_finder.error.InvalidInputException;

public abstract class AbstractSorter<V, E> implements IUnidimensionalSorter<V, E> {

	protected final Collection<Tree<V, E>> trees;
	private Iterator<Tree<V, E>> treeIte;
	
	public AbstractSorter(UpperSemilattice<V, E> alphas) throws InvalidInputException {
		TransitiveReduction.INSTANCE.reduce(alphas);
		alphas.validate();
		trees = sort(alphas);
		treeIte = trees.iterator();
	}

	@Override
	public boolean hasNext() {
		return treeIte.hasNext();
	}

	@Override
	public Tree<V, E> next() {
		return treeIte.next();
	}

	@Override
	public Collection<Tree<V, E>> getSortingTrees() {
		return trees;
	}
	
	abstract protected Collection<Tree<V, E>> sort(UpperSemilattice<V, E> alphas);

}
