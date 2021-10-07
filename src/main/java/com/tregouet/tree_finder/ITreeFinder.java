package com.tregouet.tree_finder;

import java.util.Iterator;

import com.tregouet.tree_finder.data.ClassificationTree;

public interface ITreeFinder<V,E> extends Iterator<ClassificationTree<V,E>> {

}
