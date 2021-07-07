package com.tregouet.tree_finder;

import java.util.Iterator;

import org.jgrapht.graph.SimpleDirectedGraph;

public interface ITreeFinder<V,E> extends Iterator<SimpleDirectedGraph<V,E>> {

}
