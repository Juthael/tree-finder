package com.tregouet.tree_finder.utils;

import static org.junit.Assert.*;

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
	private DirectedAcyclicGraph<String, EdgeForTests> unrootedDAG;
	private SparseIntDirectedGraph unrootedDAGSparse;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void whenChecksIfParameterIsClassificationTreeThenReturnsTrueOnlyWhenExpected() {
		fail("Not yet implemented");
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
		//except isTransitive(), oviously...
		fail("Not yet implemented");
	}
	

}
