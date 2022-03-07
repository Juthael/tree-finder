package com.tregouet.tree_finder.algo.hierarchical_restriction.impl;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
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

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.Tree;
import com.tregouet.tree_finder.utils.StructureInspector;

import utils.EdgeForTests;

public class RestrictorOptTest {
	
	//toy dataset "rooted inverted"
	private String a = "A";
	private String b = "B";
	private String c = "C";
	private String d = "D";
	private String ab1 = "AB1";
	private String ab2 = "AB2";
	private String cd1 = "CD1";
	private String cd2 = "CD2";
	private String abc1 = "ABC1";
	private String abc2 = "ABC2";
	private String bcd = "BCD";
	private String abcd = "ABCD";
	private DirectedAcyclicGraph<String, EdgeForTests> rootedInverted;
	private Set<String> rInvAtoms = new HashSet<>();
	
	//toy dataset "PowerSet"
	private DirectedAcyclicGraph<Set<Integer>, EdgeForTests> nPowerSet;
	private Set<Set<Integer>> powerSetAtoms = new HashSet<>();
	
	//toy dataset "BruteForce comparison"
	private DirectedAcyclicGraph<String, EdgeForTests> bruteForceComparison;
	private Set<String> bFatoms = new HashSet<>();

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
		ITreeFinder<Set<Integer>, EdgeForTests> tF = new RestrictorOpt<>(nPowerSet);
		int nbOfTreesReturned = 0;
		while (tF.hasNext()) {
			tF.next();
			nbOfTreesReturned++;
		}
		long complete = System.currentTimeMillis();
		/*
		System.out.println("Nb of trees returned = " + Integer.toString(nbOfTreesReturned));
		*/
		assertTrue(complete - start < 20000 && nbOfTreesReturned > 0);
	}
	
	@Test
	public void whenTreesReturnedThenValid() throws IOException {
		boolean valid = true;
		int rInvCheckCount = 0;
		int nPowerCheckCount = 0;
		setUpRootedInverted();
		setUpUpperSemiLatticeFromPowerSetOfNElements(5);
		ITreeFinder<String, EdgeForTests> treeFinderRootedInverted = new RestrictorOpt<>(rootedInverted);
		ITreeFinder<Set<Integer>, EdgeForTests> treeFinderSemilattice = new RestrictorOpt<>(nPowerSet);
		while (treeFinderRootedInverted.hasNext()) {
			if (!StructureInspector.isATree(treeFinderRootedInverted.next()))
				valid = false;
			rInvCheckCount++;
		}
		while (treeFinderSemilattice.hasNext()) {
			if (!StructureInspector.isATree(treeFinderSemilattice.next()))
				valid = false;
			nPowerCheckCount++;
		}
		assertTrue(valid && rInvCheckCount > 0 && nPowerCheckCount > 0);
	}
	
	@Test
	public void whenTreesRequestedThenExpectedReturned1() throws Exception {
		setUpSemilatticeForComparisonWithBruteForce();
		Set<Tree<String, EdgeForTests>> returnedFromBruteForce = new HashSet<>();
		Set<Tree<String, EdgeForTests>> returnedFromOpt = new HashSet<>();
		ITreeFinder<String, EdgeForTests> brute = new RestrictorBruteForce<>(bruteForceComparison);
		brute.forEachRemaining(t -> returnedFromBruteForce.add(t));
		ITreeFinder<String, EdgeForTests> opt = new RestrictorOpt<>(bruteForceComparison);
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
	public void whenTreesRequestedThenExpectedReturned2() throws IOException {
		setUpRootedInverted();
		Set<Tree<String, EdgeForTests>> expected = new HashSet<>();
		Set<Tree<String, EdgeForTests>> returned = new HashSet<>();
		ITreeFinder<String, EdgeForTests> treeFinderBruteForce = 
				new RestrictorBruteForce<>(rootedInverted);
		while (treeFinderBruteForce.hasNext())
			expected.add(treeFinderBruteForce.next());		
		/*
		int bfIdx = 0;
		for (DirectedAcyclicGraph<String, EdgeForTests> tree : expected) {
			Visualizer.visualize(tree, "2110141554_bfTree" + Integer.toString(bfIdx++));
		}
		*/
		ITreeFinder<String, EdgeForTests> treeFinderOpt = new RestrictorOpt<>(rootedInverted);
		while (treeFinderOpt.hasNext())
			returned.add(treeFinderOpt.next());
		/*
		int optIdx = 0;
		for (ClassificationTree<String, EdgeForTests> tree : returned) {
			Visualizer.visualize(tree, "2110141554_optTree" + Integer.toString(optIdx++));
		}
		*/
		assertTrue(!returned.isEmpty() && !expected.isEmpty() & returned.equals(expected));		
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
	
	private void setUpRootedInverted() {
		rootedInverted = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		rootedInverted.addVertex(a);
		rootedInverted.addVertex(b);
		rootedInverted.addVertex(c);
		rootedInverted.addVertex(d);
		rootedInverted.addVertex(ab1);
		rootedInverted.addVertex(ab2);
		rootedInverted.addVertex(cd1);
		rootedInverted.addVertex(cd2);
		rootedInverted.addVertex(abc1);
		rootedInverted.addVertex(abc2);
		rootedInverted.addVertex(bcd);
		rootedInverted.addVertex(abcd);
		rootedInverted.addEdge(a,  ab1);
		rootedInverted.addEdge(a,  ab2);
		rootedInverted.addEdge(b,  ab1);
		rootedInverted.addEdge(b,  ab2);
		rootedInverted.addEdge(b,  bcd);
		rootedInverted.addEdge(c,  abc1);
		rootedInverted.addEdge(c,  abc2);
		rootedInverted.addEdge(c,  cd1);
		rootedInverted.addEdge(c,  cd2);
		rootedInverted.addEdge(d,  cd1);
		rootedInverted.addEdge(d,  cd2);
		rootedInverted.addEdge(ab1,  abc1);
		rootedInverted.addEdge(ab1,  abc2);
		rootedInverted.addEdge(ab2,  abc1);
		rootedInverted.addEdge(ab2,  abc2);
		rootedInverted.addEdge(cd1,  bcd);
		rootedInverted.addEdge(cd2,  bcd);
		rootedInverted.addEdge(abc1,  abcd);
		rootedInverted.addEdge(abc2,  abcd);
		rootedInverted.addEdge(bcd,  abcd);
		for (String vertex : rootedInverted.vertexSet()) {
			if (rootedInverted.inDegreeOf(vertex) == 0)
				rInvAtoms.add(vertex);
		}
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(rootedInverted);
	}

}
