package com.tregouet.tree_finder.alg.unidimensional_sorting.impl;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tregouet.tree_finder.alg.unidimensional_sorting.IUnidimensionalSorter;
import com.tregouet.tree_finder.alg.unidimensional_sorting.impl.UnidimensionalSorter;
import com.tregouet.tree_finder.data.InvertedTree;
import com.tregouet.tree_finder.data.InvertedUpperSemilattice;

import utils.DichotomizableString;
import utils.EdgeForTests;

public class UnidimensionalSorterTest {
	
	private final DichotomizableString a = new DichotomizableString("A");
	private final DichotomizableString b = new DichotomizableString("B");
	private final DichotomizableString c = new DichotomizableString("C");
	private final DichotomizableString d = new DichotomizableString("D");
	private final DichotomizableString e = new DichotomizableString("E");
	private InvertedUpperSemilattice<DichotomizableString, EdgeForTests> notComplementedUSL = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() {
	}

	@Test
	public void whenSortingsOfNotComplementedUSLRequestedThenReturned() throws IOException {
		setUpNotComplementedUSL();
		/*
		Visualizer.visualize(
				((DirectedAcyclicGraph<DichotomizableString, EdgeForTests>) notComplementedUSL), 
				"2111030946_USL", 0.0);
		*/
		IUnidimensionalSorter<DichotomizableString, EdgeForTests> sorter = 
				new UnidimensionalSorter<DichotomizableString, EdgeForTests>(notComplementedUSL);
		Collection<InvertedTree<DichotomizableString, EdgeForTests>> sortingTrees = sorter.getSortingTrees();
		/*
		int treeIdx = 0;
		for (Tree<DichotomizableString, EdgeForTests> tree : sortingTrees){
			Visualizer.visualize(tree, "2111261532_D_tree" + Integer.toString(treeIdx++), 0.0);
		}
		*/
		assertTrue(sortingTrees.size() > 0);
	}
	
	private void setUpNotComplementedUSL() {
		DichotomizableString ab = new DichotomizableString("AB");
		DichotomizableString bc = new DichotomizableString("BC");
		DichotomizableString cd = new DichotomizableString("CD");
		DichotomizableString abc = new DichotomizableString("ABC");
		DichotomizableString cde = new DichotomizableString("CDE");
		DichotomizableString abcde = new DichotomizableString("ABCDE");
		DirectedAcyclicGraph<DichotomizableString, EdgeForTests> notComplementedUSL = 
				new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		notComplementedUSL.addVertex(a);
		notComplementedUSL.addVertex(b);
		notComplementedUSL.addVertex(c);
		notComplementedUSL.addVertex(d);
		notComplementedUSL.addVertex(e);
		notComplementedUSL.addVertex(ab);
		notComplementedUSL.addVertex(bc);
		notComplementedUSL.addVertex(cd);
		notComplementedUSL.addVertex(abc);
		notComplementedUSL.addVertex(cde);
		notComplementedUSL.addVertex(abcde);
		notComplementedUSL.addEdge(a, ab);
		notComplementedUSL.addEdge(b, ab);
		notComplementedUSL.addEdge(b, bc);
		notComplementedUSL.addEdge(c, bc);
		notComplementedUSL.addEdge(c, cd);
		notComplementedUSL.addEdge(d, cd);
		notComplementedUSL.addEdge(e, cde);
		notComplementedUSL.addEdge(ab, abc);
		notComplementedUSL.addEdge(bc, abc);
		notComplementedUSL.addEdge(cd, cde);
		notComplementedUSL.addEdge(abc, abcde);
		notComplementedUSL.addEdge(cde, abcde);
		List<DichotomizableString> topologicalOrder =new ArrayList<>();
		new TopologicalOrderIterator<>(notComplementedUSL).forEachRemaining(topologicalOrder::add);
		Set<DichotomizableString> leaves = new HashSet<>(Arrays.asList(new DichotomizableString[] {a, b, c, d, e}));
		this.notComplementedUSL = 
				new InvertedUpperSemilattice<DichotomizableString, EdgeForTests>(
						notComplementedUSL, abcde, leaves, topologicalOrder);
	}

}
