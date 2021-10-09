package com.tregouet.tree_finder.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tregouet.tree_finder.EdgeForTests;
import com.tregouet.tree_finder.data.ClassificationTree;
import com.tregouet.tree_finder.utils.StructureInspector;
import com.tregouet.tree_finder.viz.Visualizer;

@SuppressWarnings("unused")
public class TreeFinderBruteForceTest {
	
	private DirectedAcyclicGraph<String, EdgeForTests> upperSemilattice;
	private Set<String> atoms = new HashSet<>();
	private String a = "A";
	private String b = "B";
	private String c = "C";
	private String d = "D";
	TreeFinderBruteForce<String, EdgeForTests> treeFinder;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		setUpPowerSetMinusEmptySet();
		for (String vertex : upperSemilattice.vertexSet()) {
			if (upperSemilattice.inDegreeOf(vertex) == 0)
				atoms.add(vertex);
		}
		treeFinder = 
				new TreeFinderBruteForce<>(upperSemilattice, atoms);
	}

	@Test
	public void whenTreesReturnedThenValid() throws IOException {
		/*
		Visualizer.visualize(upperSemilattice, "2110091649_BFusl");
		*/
		boolean returnedValid = true;
		int checkCount = 0;
		while (treeFinder.hasNext()) {
			ClassificationTree<String, EdgeForTests> nextTree = treeFinder.next();
			if (!StructureInspector.isAClassificationTree(nextTree))
				returnedValid = false;
			/*
			TransitiveReduction.INSTANCE.reduce(nextTree);
			Visualizer.visualize(nextTree, "2110091649_BFtree" + Integer.toString(checkCount));
			*/
			checkCount++;
		}
		assertTrue(returnedValid && checkCount > 0);
	}
	
	private void setUpPowerSetMinusEmptySet() {
		upperSemilattice = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		List<String> set = new ArrayList<>(Arrays.asList(new String[] {a, b, c, d}));
		int setCardinal = set.size();
		List<Set<String>> powerSet = new ArrayList<>();
		List<String> powerSetAsStrings = new ArrayList<>();
		for (int i = 0 ; i < (1 << setCardinal) ; i++) {
			Set<String> subset = new HashSet<>(setCardinal);
			for (int j = 0 ; j < setCardinal ; j++) {
				if (((1 << j) & i) > 0)
					subset.add(set.get(j));
			}
			powerSet.add(subset);
		}
		powerSet.remove(new HashSet<String>());
		powerSet.stream().forEach(s -> powerSetAsStrings.add(s.toString()));
		powerSetAsStrings.stream().forEach(s -> upperSemilattice.addVertex(s));
		for (int i = 0 ; i < powerSet.size() - 1 ; i++) {
			Set<String> iSubset = powerSet.get(i);
			String iSubsetAsString = powerSetAsStrings.get(i);
			for (int j = i + 1 ; j < powerSet.size() ; j++) {
				Set<String> jSubset = powerSet.get(j);
				if (iSubset.containsAll(jSubset))
					upperSemilattice.addEdge(powerSetAsStrings.get(j), iSubsetAsString);
				else if (jSubset.containsAll(iSubset))
					upperSemilattice.addEdge(iSubsetAsString, powerSetAsStrings.get(j));
			}
		}
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(upperSemilattice);
	}	

}
