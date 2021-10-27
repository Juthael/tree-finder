package com.tregouet.tree_finder.algo.unidimensional_sorting.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.algo.unidimensional_sorting.IUnidimensionalSorter;
import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.viz.Visualizer;

class UnidimensionalSorterTest {

	private final String a = "A";
	private final String b = "B";
	private final String c = "C";
	private final String d = "D";
	private final Set<String> atoms = new HashSet<>(Arrays.asList(new String[] {a, b, c, d}));
	private DirectedAcyclicGraph<Set<String>, DefaultEdge> upperSemiLattice;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
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
	void whenSortingsRequestedThenReturned() throws InvalidInputException, IOException {
		
		TransitiveReduction.INSTANCE.reduce(upperSemiLattice);
		Visualizer.visualize(upperSemiLattice, "2110261512_USL", 0);
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(upperSemiLattice);
		
		IUnidimensionalSorter<Set<String>, DefaultEdge> sorter = 
				new UnidimensionalSorter<>(upperSemiLattice, DefaultEdge::new);
		int treeIdx = 0;
		while (sorter.hasNext()) {
			
			Visualizer.visualize(sorter.next(), "2110261512_tree" + Integer.toString(treeIdx), 0);
			
			treeIdx++;
		}
		assertTrue(treeIdx > 0);
	}

}
