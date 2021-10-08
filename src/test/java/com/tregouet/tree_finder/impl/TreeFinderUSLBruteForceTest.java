package com.tregouet.tree_finder.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tregouet.tree_finder.EdgeForTests;
import com.tregouet.tree_finder.utils.StructureInspector;

public class TreeFinderUSLBruteForceTest {
	
	private DirectedAcyclicGraph<String, EdgeForTests> upperSemilattice;
	
	private String a = "A";
	private String b = "B";
	private String c = "C";
	private String d = "D";
	private String e = "E";
	TreeFinderBruteForce<String, EdgeForTests> treeFinder;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		setUpPowerSetMinusEmptySet();
		treeFinder = 
				new TreeFinderBruteForce<>(upperSemilattice, new HashSet<>(Arrays.asList(new String[] {a, b, c, d, e})));
	}

	@Test
	public void whenTreesReturnedThenValid() {
		boolean returnedValid = true;
		int checkCount = 0;
		while (treeFinder.hasNext()) {
			if (!StructureInspector.isAClassificationTree(treeFinder.next()))
				returnedValid = false;
			checkCount++;
		}
		assertTrue(returnedValid && checkCount > 0);
	}
	
	private void setUpPowerSetMinusEmptySet() {
		upperSemilattice = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		List<String> set = new ArrayList<>(Arrays.asList(new String[] {a, b, c, d, e}));
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
