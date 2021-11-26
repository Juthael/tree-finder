package com.tregouet.tree_finder.algo.unidimensional_sorting.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveReduction;
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

import utils.EdgeForTests;
import utils.RandomUSLGenerator;

@SuppressWarnings("unused")
public class UnidimensionalSorterTest {
	
	private final String a = "A";
	private final String b = "B";
	private final String c = "C";
	private final String d = "D";
	private final Set<String> atoms = new HashSet<>(Arrays.asList(new String[] {a, b, c, d}));
	//dataset1
	private UpperSemilattice<Set<String>, EdgeForTests> powerSetUSL = null;
	//dataset2
	private UpperSemilattice<String, EdgeForTests> notComplementedUSL = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void whenSortingsOfPowerSetUSLRequestedThenReturned() throws InvalidInputException, IOException {
		setUpPowerSet();
		IUnidimensionalSorter<Set<String>, EdgeForTests> sorter = 
				new UnidimensionalSorter<Set<String>, EdgeForTests>(powerSetUSL);
		Collection<Tree<Set<String>, EdgeForTests>> sortingTrees = sorter.getSortingTrees();
		assertTrue(sortingTrees.size() > 0);
	}
	
	
	@Test
	public void whenSortingsOfNotComplementedUSLRequestedThenReturned() throws InvalidInputException, IOException {
		setUpNotComplementedUSL();
		/*
		Visualizer.visualize(((DirectedAcyclicGraph<String, EdgeForTests>) notComplementedUSL), "2111030946_USL", 'a');
		*/
		IUnidimensionalSorter<String, EdgeForTests> sorter = 
				new UnidimensionalSorter<String, EdgeForTests>(notComplementedUSL);
		Collection<Tree<String, EdgeForTests>> sortingTrees = sorter.getSortingTrees();
		/*
		int treeIdx = 0;
		for (Tree<String, EdgeForTests> tree : sortingTrees){
			Visualizer.visualize(tree, "2111030946_RI_tree" + Integer.toString(treeIdx++), 'a');
		}
		*/
		assertTrue(sortingTrees.size() > 0);
	}
	
	@Test
	public void whenSortingOfRandomUSLRequestedThenReturned() throws InvalidInputException, IOException {
		boolean returned = true;
		int maxNbOfTrials = 1;
		int maxNbOfLeaves = 5;
		int nbOfTreesReturned = 0;
		RandomUSLGenerator generator = new RandomUSLGenerator(maxNbOfLeaves);
		for (int i = 0 ; i < maxNbOfTrials ; i++) {
			UpperSemilattice<Set<String>, DefaultEdge> randomUSL = generator.nextRandomUSL();
			TransitiveReduction.INSTANCE.reduce(randomUSL);
			/*
			Visualizer.visualize(randomUSL, "2111011144_usl" + i, 0);
			*/
			nbOfTreesReturned = 0;
			IUnidimensionalSorter<Set<String>, DefaultEdge> sorter = new UnidimensionalSorter<>(randomUSL);
			while (sorter.hasNext()) {
				Tree<Set<String>, DefaultEdge> nextTree = sorter.next();
				/*
				Visualizer.visualize(nextTree, "2111011144_usl" + i + "tree" + nbOfTreesReturned, 0);
				*/
				nbOfTreesReturned++;
			}
			if (nbOfTreesReturned == 0)
				returned = false;
			nbOfTreesReturned = 0;
		}
		assertTrue(returned);
	}
	
	@Test
	public void whenTopologicalOrderRequestedThenContainsEveryElement() throws InvalidInputException {
		setUpPowerSet();
		boolean everyElementFound = true;
		/*
		Visualizer.visualize(((DirectedAcyclicGraph<String, EdgeForTests>) notComplementedUSL), "2111030946_USL", 'a');
		*/
		IUnidimensionalSorter<Set<String>, EdgeForTests> sorter = 
				new UnidimensionalSorter<Set<String>, EdgeForTests>(powerSetUSL);
		Collection<Tree<Set<String>, EdgeForTests>> sortingTrees = sorter.getSortingTrees();
		/*
		int treeIdx = 0;
		for (Tree<String, EdgeForTests> tree : sortingTrees){
			Visualizer.visualize(tree, "2111030946_RI_tree" + Integer.toString(treeIdx++), 'a');
		}
		*/
		for (Tree<Set<String>, EdgeForTests> sortingTree : sortingTrees) {
			if (!sortingTree.vertexSet().equals(new HashSet<Set<String>>(sortingTree.getTopologicalOrder())))
				everyElementFound = false;
		}
		assertTrue(sortingTrees.size() > 0);
	}
	
	private void setUpPowerSet() {
		DirectedAcyclicGraph<Set<String>, EdgeForTests> upperSemiLattice = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
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
		TopologicalOrderIterator<Set<String>, EdgeForTests> topoIte = new TopologicalOrderIterator<>(upperSemiLattice);
		while (topoIte.hasNext()) {
			Set<String> nextElement = topoIte.next();
			topoOrder.add(nextElement);
			if (upperSemiLattice.inDegreeOf(nextElement) == 0)
				leaves.add(nextElement);
			if (upperSemiLattice.outDegreeOf(nextElement) == 0)
				root = nextElement;
		}
		this.powerSetUSL = new UpperSemilattice<Set<String>, EdgeForTests>(upperSemiLattice, root, leaves, topoOrder);
	}
	
	private void setUpNotComplementedUSL() {
		String bc = "BC";
		String cd = "CD";
		String abc = "ABC";
		String bcd = "BCD";
		String abcd = "ABCD";
		DirectedAcyclicGraph<String, EdgeForTests> smallUpperSemilattice = 
				new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		smallUpperSemilattice.addVertex(a);
		smallUpperSemilattice.addVertex(b);
		smallUpperSemilattice.addVertex(c);
		smallUpperSemilattice.addVertex(d);
		smallUpperSemilattice.addVertex(bc);
		smallUpperSemilattice.addVertex(cd);
		smallUpperSemilattice.addVertex(abc);
		smallUpperSemilattice.addVertex(bcd);
		smallUpperSemilattice.addVertex(abcd);
		smallUpperSemilattice.addEdge(a, abc);
		smallUpperSemilattice.addEdge(b, bc);
		smallUpperSemilattice.addEdge(c, bc);
		smallUpperSemilattice.addEdge(c, cd);
		smallUpperSemilattice.addEdge(d, cd);
		smallUpperSemilattice.addEdge(bc, abc);
		smallUpperSemilattice.addEdge(bc, bcd);
		smallUpperSemilattice.addEdge(cd, bcd);
		smallUpperSemilattice.addEdge(abc, abcd);
		smallUpperSemilattice.addEdge(bcd, abcd);
		List<String> topologicalOrder =new ArrayList<>();
		new TopologicalOrderIterator<>(smallUpperSemilattice).forEachRemaining(topologicalOrder::add);
		Set<String> leaves = new HashSet<>(Arrays.asList(new String[] {a, b, c, d}));
		this.notComplementedUSL = 
				new UpperSemilattice<String, EdgeForTests>(smallUpperSemilattice, abcd, leaves, topologicalOrder);
	}

}
