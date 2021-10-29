package com.tregouet.tree_finder.algo.unidimensional_sorting.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.algo.unidimensional_sorting.IUnidimensionalSorter;
import com.tregouet.tree_finder.data.Tree;
import com.tregouet.tree_finder.error.InvalidInputException;

public class UnidimensionalSorterTest {
	
	private final String a = "A";
	private final String b = "B";
	private final String c = "C";
	private final String d = "D";
	private final Set<String> atoms = new HashSet<>(Arrays.asList(new String[] {a, b, c, d}));
	private DirectedAcyclicGraph<Set<String>, DefaultEdge> upperSemiLattice;	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		upperSemiLattice = new DirectedAcyclicGraph<>(null, DefaultEdge::new, false);
		List<Set<String>> vertices = new ArrayList<>(Sets.powerSet(atoms));
		vertices.remove(new HashSet<String>());
		Graphs.addAllVertices(upperSemiLattice, vertices);
		for (int i = 0 ; i < vertices.size() - 1 ; i++) {
			Set<String> iVertex = vertices.get(i);
			for (int j = i + 1 ; j < vertices.size() ; j++) {
				Set<String> jVertex = vertices.get(j);
				if (iVertex.containsAll(jVertex))
					upperSemiLattice.addEdge(jVertex, iVertex);
				else if (jVertex.containsAll(iVertex))
					upperSemiLattice.addEdge(iVertex, jVertex);
			}
		}
	}
	
	@Test
	public void whenSortingsRequestedThenReturned() throws InvalidInputException {
		IUnidimensionalSorter<Set<String>, DefaultEdge> sorter = 
				new UnidimensionalSorter<>(upperSemiLattice, DefaultEdge::new);
		int treeIdx = 0;
		while (sorter.hasNext()) {
			Tree<Set<String>, DefaultEdge> nextTree = sorter.next();
			/*
			Visualizer.visualize(nextTree, "2110261512_tree" + Integer.toString(treeIdx), 0);
			*/
			treeIdx++;
		}
		assertTrue(treeIdx > 0);
	}

}
