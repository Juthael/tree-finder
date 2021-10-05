package com.tregouet.tree_finder.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tregouet.tree_finder.EdgeForTests;

public class StructureInspectorTest {
	
	private DirectedAcyclicGraph<String, EdgeForTests> classificationTree;
	private DirectedAcyclicGraph<String, EdgeForTests> powerSetMinusEmptySet;
	private SparseIntDirectedGraph powerSetMinusEmptySetSparse;
	private DirectedAcyclicGraph<String, EdgeForTests> unrooted;
	private SparseIntDirectedGraph unrootedSparse;
	
	private String a = "A";
	private String b = "B";
	private String c = "C";
	private String d = "D";
	private String e = "E";
	private String ab = "AB";
	private String bc = "BC";
	private String bcd = "BCD";
	private String abcd = "ABCD";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		setClassificationTree();
		setPowerSetMinusEmptySet();
		SparseGraphConverter<String, EdgeForTests> powerSetConverter = 
				new SparseGraphConverter<>(powerSetMinusEmptySet, true);
		powerSetMinusEmptySetSparse = powerSetConverter.getSparseGraph();
		setUnrootedDAG();
		SparseGraphConverter<String, EdgeForTests> unrootedConverter = 
				new SparseGraphConverter<>(unrooted, false);
		unrootedSparse = unrootedConverter.getSparseGraph();
	}

	@Test
	public void whenChecksIfParameterIsClassificationTreeThenReturnsTrueOnlyWhenExpected() {
		assertTrue(
				StructureInspector.isAClassificationTree(classificationTree)
				&& !StructureInspector.isAClassificationTree(powerSetMinusEmptySet)
				&& !StructureInspector.isAClassificationTree(unrooted));
	}
	
	@Test
	public void whenChecksIfDAGParameterIsAnUpperSemilatticeThenReturnsTrueOnlyWhenExpected() {
		fail("Not yet implemented");
	}
	
	@Test
	public void whenChecksIfSparseParameterIsAnUpperSemilatticeThenReturnsTrueOnlyWhenExpected() {
		fail("Not yet implemented");
	}
	
	@Test
	public void whenChecksIfParameterIsRootedAndInvertedThenReturnsTrueOnlyWhenExpected() {
		fail("Not yet implemented");
	}
	
	@Test
	public void whenChecksIfSparseParameterIsTransitiveThenReturnsTrueOnlyWhenExpected() {
		fail("Not yet implemented");
	}
	
	@Test
	public void whenChecksIfSparseParameterIsRootedThenReturnsTrueOnlyWhenExpected() {
		fail("Not yet implemented");
	}
	
	@Test
	public void whenTransitiveClosureRequestedThenReturned() {
		fail("Not yet implemented");
	}
	
	@Test
	public void theCallOfAnyMethodReturnsTheSameValueWetherTheParameterIsAGivenGraphOrItsTransitiveReduction() {
		//except isTransitive() method, obviously
		fail("Not yet implemented");
	}
	
	private void setClassificationTree() {
		classificationTree = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		classificationTree.addVertex(a);
		classificationTree.addVertex(b);
		classificationTree.addVertex(c);
		classificationTree.addVertex(d);
		classificationTree.addVertex(bc);
		classificationTree.addVertex(bcd);
		classificationTree.addVertex(abcd);
		classificationTree.addEdge(a, abcd);
		classificationTree.addEdge(b, bc);
		classificationTree.addEdge(c, bc);
		classificationTree.addEdge(d, bcd);
		classificationTree.addEdge(bc, bcd);
		classificationTree.addEdge(bcd, abcd);
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(classificationTree);
	}
	
	private void setPowerSetMinusEmptySet() {
		powerSetMinusEmptySet = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
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
		powerSetAsStrings.stream().forEach(s -> powerSetMinusEmptySet.addVertex(s));
		for (int i = 0 ; i < powerSet.size() - 1 ; i++) {
			Set<String> iSubset = powerSet.get(i);
			String iSubsetAsString = powerSetAsStrings.get(i);
			for (int j = i + 1 ; j < powerSet.size() ; j++) {
				Set<String> jSubset = powerSet.get(j);
				if (iSubset.containsAll(jSubset))
					powerSetMinusEmptySet.addEdge(powerSetAsStrings.get(j), iSubsetAsString);
				else if (jSubset.containsAll(iSubset))
					powerSetMinusEmptySet.addEdge(iSubsetAsString, powerSetAsStrings.get(j));
			}
		}
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(powerSetMinusEmptySet);
	}
	
	private void setUnrootedDAG() {
		unrooted = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		unrooted.addVertex(a);
		unrooted.addVertex(b);
		unrooted.addVertex(c);
		unrooted.addVertex(d);
		unrooted.addVertex(ab);
		unrooted.addVertex(bc);
		unrooted.addVertex(bcd);
		unrooted.addEdge(a, ab);
		unrooted.addEdge(b, ab);
		unrooted.addEdge(b, bc);
		unrooted.addEdge(c, bc);
		unrooted.addEdge(bc, bcd);
		unrooted.addEdge(d, bcd);
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(unrooted);
	}
	

}
