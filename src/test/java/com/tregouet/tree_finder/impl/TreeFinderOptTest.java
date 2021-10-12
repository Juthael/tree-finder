package com.tregouet.tree_finder.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.EdgeForTests;
import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.ClassificationTree;
import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.utils.StructureInspector;
import com.tregouet.tree_finder.viz.Visualizer;

public class TreeFinderOptTest {
	
	//toy dataset "ABC"
	private String a = "A";
	private String b = "B";
	private String c = "C";
	private String ab = "AB";
	private String ac = "AC";
	private String bc = "BC";
	private String abc = "ABC";
	private List<String> verticesABC = new ArrayList<>(Arrays.asList(new String[] {a, b, c, ab, ac, bc, abc}));
	private Set<String> leavesABC = new HashSet<>(Arrays.asList(new String[]{a, b, c}));
	private DirectedAcyclicGraph<String, EdgeForTests> upperSemiLatticeABC;
	
	//toy dataset "PowerSet"
	private DirectedAcyclicGraph<Set<Integer>, EdgeForTests> nPowerSet;
	private Set<Set<Integer>> powerSetAtoms = new HashSet<>();
	
	//toy dataset "BruteForce comparison"
	private DirectedAcyclicGraph<String, EdgeForTests> bruteForceComparison;
	private Set<String> bFatoms = new HashSet<>();
	private String d = "D";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void whenInputIsLargeThenProceedsInReasonableTime() throws IOException {
		setUpUpperSemiLatticeFromPowerSetOfNElements(7);
		long start = System.currentTimeMillis();
		ITreeFinder<Set<Integer>, EdgeForTests> tF = new TreeFinderOpt<>(nPowerSet, powerSetAtoms);
		int nbOfTreesReturned = 0;
		while (tF.hasNext()) {
			tF.next();
			nbOfTreesReturned++;
		}
		long complete = System.currentTimeMillis();
		/*
		System.out.println("Nb of trees returned = " + Integer.toString(nbOfTreesReturned));
		*/
		assertTrue(complete - start < 10000 && nbOfTreesReturned > 0);
	}
	
	@Test
	public void whenTreesReturnedThenValid() throws IOException {
		boolean allReturnedTreesAreValid = true;
		int nbOfChecks = 0;
		setUpperSemiLatticeABC();
		/*
		Visualizer.visualize(upperSemiLatticeABC, "2110091427_USL");
		*/
		ITreeFinder<String, EdgeForTests> tF = new TreeFinderOpt<>(upperSemiLatticeABC, leavesABC);
		while (tF.hasNext()) {
			ClassificationTree<String, EdgeForTests> tree = tF.next();
			/*
			Visualizer.visualize(tree, "2110091427_tree" + Integer.toString(nbOfChecks));
			*/
			if (!StructureInspector.isAClassificationTree(tree))
				allReturnedTreesAreValid = false;
			nbOfChecks++;
		}
		assertTrue(allReturnedTreesAreValid && nbOfChecks > 0);
	}
	
	@Test
	public void whenTreesRequestedThenExpectedReturned1() throws Exception {
		setUpSemilatticeForComparisonWithBruteForce();
		Set<ClassificationTree<String, EdgeForTests>> returnedFromBruteForce = new HashSet<>();
		Set<ClassificationTree<String, EdgeForTests>> returnedFromOpt = new HashSet<>();
		ITreeFinder<String, EdgeForTests> brute = new TreeFinderBruteForce<>(bruteForceComparison, bFatoms);
		brute.forEachRemaining(t -> returnedFromBruteForce.add(t));
		ITreeFinder<String, EdgeForTests> opt = new TreeFinderOpt<>(bruteForceComparison, bFatoms);
		opt.forEachRemaining(t -> returnedFromOpt.add(t));
		/*
		Iterator<ClassificationTree<String, EdgeForTests>> bfIte = returnedFromBruteForce.iterator();
		int bfIdx = 0;
		while (bfIte.hasNext()) {
			ClassificationTree<String, EdgeForTests> nextBF = bfIte.next();
			TransitiveReduction.INSTANCE.reduce(nextBF);
			Visualizer.visualize(nextBF, "2110091728bf_" + Integer.toString(bfIdx));
			bfIdx++;
		}
		*/
		/*
		Iterator<ClassificationTree<String, EdgeForTests>> optIte = returnedFromOpt.iterator();
		int optIdx = 0;
		while (optIte.hasNext()) {
			ClassificationTree<String, EdgeForTests> nextOPT = optIte.next();
			TransitiveReduction.INSTANCE.reduce(nextOPT);
			Visualizer.visualize(nextOPT, "2110091728opt_" + Integer.toString(optIdx));
			optIdx++;
		}
		*/
		/*
		Set<ClassificationTree<String, EdgeForTests>> difference = Sets.difference(returnedFromBruteForce, returnedFromOpt);
		int idx = 0;
		for (ClassificationTree<String, EdgeForTests> diffTree : difference) {
			Visualizer.visualize(diffTree, "2110091728DIFF_" + Integer.toString(idx++));
		}
		*/
		assertTrue(returnedFromBruteForce.equals(returnedFromOpt));
	}
	
	@Test
	public void whenTreesRequestedThenExpectedReturned2() {
		
	}
	
	@Test
	public void whenInputIsNotAtomisticThenWorksAllTheSame() {
		fail("Not yet implemented");
	}
	
	private void setUpperSemiLatticeABC() {
		upperSemiLatticeABC = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		upperSemiLatticeABC.addVertex(a);
		upperSemiLatticeABC.addVertex(b);
		upperSemiLatticeABC.addVertex(c);
		upperSemiLatticeABC.addVertex(ab);
		upperSemiLatticeABC.addVertex(ac);
		upperSemiLatticeABC.addVertex(bc);
		upperSemiLatticeABC.addVertex(abc);
		upperSemiLatticeABC.addEdge(a, ab);
		upperSemiLatticeABC.addEdge(a, ac);
		upperSemiLatticeABC.addEdge(b, ab);
		upperSemiLatticeABC.addEdge(b, bc);
		upperSemiLatticeABC.addEdge(c, ac);
		upperSemiLatticeABC.addEdge(c, bc);
		upperSemiLatticeABC.addEdge(ab, abc);
		upperSemiLatticeABC.addEdge(ac, abc);
		upperSemiLatticeABC.addEdge(bc, abc);
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(upperSemiLatticeABC);
	}
	
	private ClassificationTree<String, Edge> setN1() throws InvalidInputException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.removeVertex(bc);
		nArg.addEdge(a, ab);
		nArg.addEdge(b, ab);
		nArg.addEdge(c, ac);
		nArg.addEdge(ab, abc);
		nArg.addEdge(ac, abc);
		return new ClassificationTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}
	
	private ClassificationTree<String, Edge> setN2() throws InvalidInputException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.removeVertex(ac);
		nArg.addEdge(a, ab);
		nArg.addEdge(b, ab);
		nArg.addEdge(c, bc);
		nArg.addEdge(ab, abc);
		nArg.addEdge(bc, abc);
		return new ClassificationTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}
	
	
	
	private ClassificationTree<String, Edge> setN3() throws InvalidInputException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.addEdge(a, ab);
		nArg.addEdge(b, bc);
		nArg.addEdge(c, ac);
		nArg.addEdge(ab, abc);
		nArg.addEdge(ac, abc);
		nArg.addEdge(bc, abc);
		return new ClassificationTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}
	
	private ClassificationTree<String, Edge> setN4() throws InvalidInputException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.removeVertex(ac);
		nArg.addEdge(a, ab);
		nArg.addEdge(b, bc);
		nArg.addEdge(c, bc);
		nArg.addEdge(ab, abc);
		nArg.addEdge(bc, abc);
		return new ClassificationTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}
	
	private ClassificationTree<String, Edge> setN5() throws InvalidInputException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.removeVertex(bc);
		nArg.addEdge(a, ac);
		nArg.addEdge(b, ab);
		nArg.addEdge(c, ac);
		nArg.addEdge(ab, abc);
		nArg.addEdge(ac, abc);
		return new ClassificationTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}	
	
	private ClassificationTree<String, Edge> setN6() throws InvalidInputException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.addEdge(a, ac);
		nArg.addEdge(b, ab);
		nArg.addEdge(c, bc);
		nArg.addEdge(ab, abc);
		nArg.addEdge(ac, abc);
		nArg.addEdge(bc, abc);
		return new ClassificationTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}
	
	private ClassificationTree<String, Edge> setN7() throws InvalidInputException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.removeVertex(ab);
		nArg.addEdge(a, ac);
		nArg.addEdge(b, bc);
		nArg.addEdge(c, ac);
		nArg.addEdge(ac, abc);
		nArg.addEdge(bc, abc);
		return new ClassificationTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}
	
	private ClassificationTree<String, Edge> setN8() throws InvalidInputException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.removeVertex(ab);
		nArg.addEdge(a, ac);
		nArg.addEdge(b, bc);
		nArg.addEdge(c, bc);
		nArg.addEdge(ac, abc);
		nArg.addEdge(bc, abc);
		return new ClassificationTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}	
	
	private void setUpSemilatticeForComparisonWithBruteForce() {
		bruteForceComparison = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
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
		powerSetAsStrings.stream().forEach(s -> bruteForceComparison.addVertex(s));
		for (int i = 0 ; i < powerSet.size() - 1 ; i++) {
			Set<String> iSubset = powerSet.get(i);
			String iSubsetAsString = powerSetAsStrings.get(i);
			for (int j = i + 1 ; j < powerSet.size() ; j++) {
				Set<String> jSubset = powerSet.get(j);
				if (iSubset.containsAll(jSubset))
					bruteForceComparison.addEdge(powerSetAsStrings.get(j), iSubsetAsString);
				else if (jSubset.containsAll(iSubset))
					bruteForceComparison.addEdge(iSubsetAsString, powerSetAsStrings.get(j));
			}
		}
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(bruteForceComparison);
		for (String vertex : bruteForceComparison.vertexSet()) {
			if (bruteForceComparison.inDegreeOf(vertex) == 0)
				bFatoms.add(vertex);
		}
	}
	
	private void setUpUpperSemiLatticeFromPowerSetOfNElements(int n) {
		List<Set<Integer>> powerSet = new ArrayList<>();
		//build power set
		int[] atoms = new int[n];
		for (int i = 0 ; i < n ; i++) {
			atoms[i] = i;
		}
		
		for (int i = 0 ; i < (1 << n) ; i++) {
			Set<Integer> subset = new HashSet<Integer>();
			for (int j = 0 ; j < n ; j++) {
				if (((1 << j) & i) > 0)
					subset.add(atoms[j]);
			}
			powerSet.add(subset);
		}
		//remove empty set
		powerSet.remove(new HashSet<Integer>());
		//build graph
		nPowerSet = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		for (Set<Integer> subset : powerSet)
			nPowerSet.addVertex(subset);
		for (int i = 0 ; i < powerSet.size() - 1 ; i++) {
			for (int j = i+1 ; j < powerSet.size() ; j++) {
				if (powerSet.get(j).containsAll(powerSet.get(i)))
					nPowerSet.addEdge(powerSet.get(i), powerSet.get(j));
				else if (powerSet.get(i).containsAll(powerSet.get(j)))
					nPowerSet.addEdge(powerSet.get(j), powerSet.get(i));
			}
		}
		for (Set<Integer> vertex : nPowerSet.vertexSet()) {
			if (nPowerSet.inDegreeOf(vertex) == 0)
				powerSetAtoms.add(vertex);
		}
	}	

}
