package com.tregouet.tree_finder;

import java.util.Iterator;

import org.jgrapht.graph.DefaultEdge;

import com.tregouet.tree_finder.data.Tree;

public interface ITreeFinder<V,E extends DefaultEdge> extends Iterator<Tree<V,E>> {

}
