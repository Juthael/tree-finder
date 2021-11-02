package com.tregouet.tree_finder.algo.hierarchical_restriction.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.sun.source.tree.AssertTree;
import com.tregouet.tree_finder.EdgeForTests;
import com.tregouet.tree_finder.algo.hierarchical_restriction.impl.RestrictorBruteForce;
import com.tregouet.tree_finder.data.Tree;
import com.tregouet.tree_finder.error.InvalidInputException;
import com.tregouet.tree_finder.utils.StructureInspector;
import com.tregouet.tree_finder.viz.Visualizer;

@SuppressWarnings("unused")
public class RestrictorBruteForceTest {
	
	//toy dataset "upper semilattice"
	private DirectedAcyclicGraph<String, EdgeForTests> upperSemilattice;
	private Set<String> uSLatoms = new HashSet<>();
	private String a = "A";
	private String b = "B";
	private String c = "C";
	private String d = "D";
	RestrictorBruteForce<String, EdgeForTests> semiLatticeTreeFinder;
	
	//toy dataset "rooted inverted"
	private DirectedAcyclicGraph<String, EdgeForTests> rootedInverted;
	private Set<String> rInvAtoms = new HashSet<>();
	private String ab1 = "AB1";
	private String ab2 = "AB2";
	private String cd1 = "CD1";
	private String cd2 = "CD2";
	private String abc1 = "ABC1";
	private String abc2 = "ABC2";
	private String bcd = "BCD";
	private String abcd = "ABCD";
	RestrictorBruteForce<String, EdgeForTests> rootedInvertedTreeFinder;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void whenTreesReturnedThenValid1() throws IOException, InvalidInputException {
		setUpPowerSetMinusEmptySet();
		for (String vertex : upperSemilattice.vertexSet()) {
			if (upperSemilattice.inDegreeOf(vertex) == 0)
				uSLatoms.add(vertex);
		}
		semiLatticeTreeFinder = 
				new RestrictorBruteForce<>(upperSemilattice);
		/*
		Visualizer.visualize(upperSemilattice, "2110091649_BFusl");
		*/
		boolean returnedValid = true;
		int checkCount = 0;
		while (semiLatticeTreeFinder.hasNext()) {
			Tree<String, EdgeForTests> nextTree = semiLatticeTreeFinder.next();
			if (!StructureInspector.isATree(nextTree))
				returnedValid = false;
			/*
			TransitiveReduction.INSTANCE.reduce(nextTree);
			Visualizer.visualize(nextTree, "2110091649_BFtree" + Integer.toString(checkCount));
			*/
			checkCount++;
		}
		assertTrue(returnedValid && checkCount > 0);
	}
	
	@Test
	public void whenTreesReturnedThenValid2() throws IOException, InvalidInputException {
		setUpRootedInverted();
		boolean returnedValid = true;
		int checkCount = 0;
		rootedInvertedTreeFinder = 
				new RestrictorBruteForce<>(rootedInverted);
		while (rootedInvertedTreeFinder.hasNext()) {			
			Tree<String, EdgeForTests> nextTree = rootedInvertedTreeFinder.next();
			if (!isValid(nextTree))
				returnedValid = false;
			checkCount++;
		}
		/*
		System.out.println("Nb of trees found : " + Integer.toString(checkCount));
		*/
		assertTrue(returnedValid && checkCount > 0);
	}
	
	@Test
	public void whenTreesRequestedThenExpectedReturned() throws InvalidInputException {
		setUpRootedInverted();
		Set<Tree<String, EdgeForTests>> returned = new HashSet<>();
		rootedInvertedTreeFinder = new RestrictorBruteForce<>(rootedInverted);
		while (rootedInvertedTreeFinder.hasNext())
			returned.add(rootedInvertedTreeFinder.next());
		Set<Tree<String, EdgeForTests>> expected = expect();		
		assertTrue(!returned.isEmpty() && !expected.isEmpty() & returned.equals(expected));		
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
	
	private Set<Tree<String, EdgeForTests>> expect() {
		Set<Tree<String, EdgeForTests>> expected = new HashSet<>();
		Set<Set<String>> expectedVertexSets = new HashSet<>();
		expectedVertexSets.add(new HashSet<>(Arrays.asList(new String[]{abcd, abc1, ab1, a, b, c, d})));
		expectedVertexSets.add(new HashSet<>(Arrays.asList(new String[]{abcd, abc1, ab2, a, b, c, d})));
		expectedVertexSets.add(new HashSet<>(Arrays.asList(new String[]{abcd, abc2, ab1, a, b, c, d })));
		expectedVertexSets.add(new HashSet<>(Arrays.asList(new String[]{abcd, abc2, ab2, a, b, c, d })));
		expectedVertexSets.add(new HashSet<>(Arrays.asList(new String[]{abcd, bcd, cd1, a, b, c, d })));
		expectedVertexSets.add(new HashSet<>(Arrays.asList(new String[]{abcd, bcd, cd2, a, b, c, d })));
		expectedVertexSets.add(new HashSet<>(Arrays.asList(new String[]{abcd, ab1, cd1, a, b, c, d })));
		expectedVertexSets.add(new HashSet<>(Arrays.asList(new String[]{abcd, ab1, cd2, a, b, c, d })));
		expectedVertexSets.add(new HashSet<>(Arrays.asList(new String[]{abcd, ab2, cd1, a, b, c, d })));
		expectedVertexSets.add(new HashSet<>(Arrays.asList(new String[]{abcd, ab2, cd2, a, b, c, d })));
		for (Set<String> expectedVertexSet : expectedVertexSets) {
			Set<EdgeForTests> expectedEdgeSet = new HashSet<>();
			for (EdgeForTests edge : rootedInverted.edgeSet()) {
				if (expectedVertexSet.contains(rootedInverted.getEdgeSource(edge))
						&& expectedVertexSet.contains(rootedInverted.getEdgeTarget(edge)))
					expectedEdgeSet.add(edge);
			}
			DirectedAcyclicGraph<String, EdgeForTests> expectedDAG = 
					new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
			Graphs.addAllEdges(expectedDAG, rootedInverted, expectedEdgeSet);
			List<String> expTopoOrder = new ArrayList<>();
			TopologicalOrderIterator<String, EdgeForTests> topoIte = new TopologicalOrderIterator<>(expectedDAG);
			topoIte.forEachRemaining(expTopoOrder::add);
			expected.add(new Tree<String, EdgeForTests>(expectedDAG, abcd, uSLatoms, expTopoOrder));
		}
		return expected;
	}
	
	private boolean isValid(Tree<String, EdgeForTests> alledgedTree) {
		boolean isATree = true;
		TransitiveReduction.INSTANCE.reduce(alledgedTree);
		List<String> topoElements = new ArrayList<>();
		new TopologicalOrderIterator<>(alledgedTree).forEachRemaining(e -> topoElements.add(e));
		String root = abcd;
		//hierarchy clause n°1
		if (!topoElements.contains(root) || !topoElements.containsAll(rInvAtoms))
			isATree = false;
		List<Set<String>> lowerSets = new ArrayList<>(topoElements.size());
		for (String iElement : topoElements) {
			Set<String> iLowerSet = new HashSet<>();
			iLowerSet.add(iElement);
			for (EdgeForTests incomingEdge : alledgedTree.incomingEdgesOf(iElement)) {
				String predecessor = alledgedTree.getEdgeSource(incomingEdge);
				//HERE
				iLowerSet.addAll(lowerSets.get(topoElements.indexOf(predecessor)));
			}
			lowerSets.add(iLowerSet);
		}
		for (int j = 0 ; j < topoElements.size() - 1 ; j++) {
			Set<String> jLowerSet = lowerSets.get(j);
			for (int k = j + 1 ; k < topoElements.size() ; k++) {
				Set<String> kLowerSet = lowerSets.get(k);
				Set<String> intersection = new HashSet<>(Sets.intersection(jLowerSet, kLowerSet));
				//hierarchy clause n°2
				if (!intersection.isEmpty() && !intersection.equals(jLowerSet) && !intersection.equals(kLowerSet))
					isATree = false;
			}
		}
		return isATree;
	}

}
