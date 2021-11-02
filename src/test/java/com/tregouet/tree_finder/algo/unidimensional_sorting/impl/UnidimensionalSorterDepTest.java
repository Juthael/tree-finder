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
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.algo.unidimensional_sorting.IUnidimensionalSorter;
import com.tregouet.tree_finder.data.Tree;
import com.tregouet.tree_finder.data.UpperSemilattice;
import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.viz.Visualizer;

@SuppressWarnings("unused")
public class UnidimensionalSorterDepTest {
	
	private final String a = "A";
	private final String b = "B";
	private final String c = "C";
	private final String d = "D";
	private final Set<String> atoms = new HashSet<>(Arrays.asList(new String[] {a, b, c, d}));
	//dataset1
	private UpperSemilattice<Set<String>, DefaultEdge> upperSemiLattice = null;
	//dataset2
	private DirectedAcyclicGraph<String, DefaultEdge> rootedInverted = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void whenSortingsRequestedThenReturned1() throws InvalidInputException, IOException {
		setUpUpperSemilattice();
		IUnidimensionalSorter<Set<String>, DefaultEdge> sorter = 
				new UnidimensionalSorter<Set<String>, DefaultEdge>(upperSemiLattice);
		int treeIdx = 0;
		while (sorter.hasNext()) {
			Tree<Set<String>, DefaultEdge> nextTree = sorter.next();
			
			Visualizer.visualize(nextTree, "2110261512_tree" + Integer.toString(treeIdx), 0);
			
			treeIdx++;
		}
		assertTrue(treeIdx > 0);
	}
	
	/*
	@Test
	public void whenSortingRequestedThenReturned2() throws InvalidInputException, IOException {
		setUpRootedInverted();
		IUnidimensionalSorter<String, DefaultEdge> sorter = 
				new UnidimensionalSorterDep<>(rootedInverted, DefaultEdge::new);
		int treeIdx = 0;
		while (sorter.hasNext()) {
			Tree<String, DefaultEdge> nextTree = sorter.next();
			
			Visualizer.visualize(nextTree, "2110261512_RI_tree" + Integer.toString(treeIdx), 'a');
			
			treeIdx++;
		}
		assertTrue(treeIdx > 0);
	}
	*/
	
	private void setUpUpperSemilattice() {
		DirectedAcyclicGraph<Set<String>, DefaultEdge> upperSemiLattice = new DirectedAcyclicGraph<>(null, DefaultEdge::new, false);
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
		List<Set<String>> topoOrder = new ArrayList<>();
		Set<Set<String>> leaves = new HashSet<>();
		Set<String> root = null;;
		TopologicalOrderIterator<Set<String>, DefaultEdge> topoIte = new TopologicalOrderIterator<>(upperSemiLattice);
		while (topoIte.hasNext()) {
			Set<String> nextElement = topoIte.next();
			topoOrder.add(nextElement);
			if (upperSemiLattice.inDegreeOf(nextElement) == 0)
				leaves.add(nextElement);
			if (upperSemiLattice.outDegreeOf(nextElement) == 0)
				root = nextElement;
		}
		this.upperSemiLattice = new UpperSemilattice<Set<String>, DefaultEdge>(upperSemiLattice, root, leaves, topoOrder);
	}
	
	private void setUpRootedInverted() {
		String bc = "BC";
		String cd = "CD";
		String abc = "ABC";
		String bcd = "BCD";
		String abcd = "ABCD";
		rootedInverted = new DirectedAcyclicGraph<>(null, DefaultEdge::new, false);
		rootedInverted.addVertex(a);
		rootedInverted.addVertex(b);
		rootedInverted.addVertex(c);
		rootedInverted.addVertex(d);
		rootedInverted.addVertex(bc);
		rootedInverted.addVertex(cd);
		rootedInverted.addVertex(abc);
		rootedInverted.addVertex(bcd);
		rootedInverted.addVertex(abcd);
		rootedInverted.addEdge(a, abc);
		rootedInverted.addEdge(b, bc);
		rootedInverted.addEdge(c, bc);
		rootedInverted.addEdge(c, cd);
		rootedInverted.addEdge(d, cd);
		rootedInverted.addEdge(bc, abc);
		rootedInverted.addEdge(bc, bcd);
		rootedInverted.addEdge(cd, bcd);
		rootedInverted.addEdge(abc, abcd);
		rootedInverted.addEdge(bcd, abcd);
	}

}
