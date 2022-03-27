package com.tregouet.tree_finder;

import java.util.Iterator;

import com.tregouet.tree_finder.data.InvertedTree;

public interface ITreeFinder<V,E> extends Iterator<InvertedTree<V,E>> {
}
