package com.tregouet.tree_finder.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tregouet.tree_finder.EdgeForTests;
import com.tregouet.tree_finder.error.InvalidSemilatticeException;
import com.tregouet.tree_finder.viz.Visualizer;

public class UpperSemilatticeFinderTest {

	private DirectedAcyclicGraph<String, EdgeForTests> rootedNotLatticeDAG;
	private UpperSemilatticeFinder<String, EdgeForTests> uSLFinder;
	private List<DirectedAcyclicGraph<String, EdgeForTests>> semiLattices;
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
	private String bcdTunnel = "BCDsi";
	private String abcd = "ABCD";
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		rootedNotLatticeDAG = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		rootedNotLatticeDAG.addVertex(a);
		rootedNotLatticeDAG.addVertex(b);
		rootedNotLatticeDAG.addVertex(c);
		rootedNotLatticeDAG.addVertex(d);
		rootedNotLatticeDAG.addVertex(ab1);
		rootedNotLatticeDAG.addVertex(ab2);
		rootedNotLatticeDAG.addVertex(cd1);
		rootedNotLatticeDAG.addVertex(cd2);
		rootedNotLatticeDAG.addVertex(abc1);
		rootedNotLatticeDAG.addVertex(abc2);
		rootedNotLatticeDAG.addVertex(bcd);
		rootedNotLatticeDAG.addVertex(bcdTunnel);
		rootedNotLatticeDAG.addVertex(abcd);
		rootedNotLatticeDAG.addEdge(a, ab1);
		rootedNotLatticeDAG.addEdge(a, ab2);
		rootedNotLatticeDAG.addEdge(b, ab1);
		rootedNotLatticeDAG.addEdge(b, ab2);
		rootedNotLatticeDAG.addEdge(b, bcd);
		rootedNotLatticeDAG.addEdge(c, abc1);
		rootedNotLatticeDAG.addEdge(c, abc2);
		rootedNotLatticeDAG.addEdge(c, cd1);
		rootedNotLatticeDAG.addEdge(c, cd2);
		rootedNotLatticeDAG.addEdge(d, cd1);
		rootedNotLatticeDAG.addEdge(d, cd2);
		rootedNotLatticeDAG.addEdge(ab1, abc1);
		rootedNotLatticeDAG.addEdge(ab1, abc2);
		rootedNotLatticeDAG.addEdge(ab2, abc1);
		rootedNotLatticeDAG.addEdge(ab2, abc2);
		rootedNotLatticeDAG.addEdge(cd1, bcd);
		rootedNotLatticeDAG.addEdge(cd2, bcd);
		rootedNotLatticeDAG.addEdge(abc1, abcd);
		rootedNotLatticeDAG.addEdge(abc2, abcd);
		rootedNotLatticeDAG.addEdge(bcd, bcdTunnel);
		rootedNotLatticeDAG.addEdge(bcdTunnel, abcd);
		Set<String> minimals = new HashSet<>(Arrays.asList(new String[] {a, b, c, d}));
		uSLFinder = new UpperSemilatticeFinder<>(rootedNotLatticeDAG, minimals);
		semiLattices = new ArrayList<>();
	}

	@Test
	public void whenParameterAdvanceRequestedThenProceedsAsExpected() {
		boolean asExpected = true;
		int[] coordinates = new int[] {0, 0, 0};
		int[] arrayDimensions = new int[] {3, 2, 4};
		int coordinateIdx = 0;
		int[][] expected = expectCoordinates();
		for (int i = 1 ; i < 24 ; i++) {
			UpperSemilatticeFinder.advance(coordinates, arrayDimensions, coordinateIdx);
			if (!Arrays.equals(coordinates, expected[i]))
				asExpected = false;
		}
		boolean hasNext = UpperSemilatticeFinder.advance(coordinates, arrayDimensions, coordinateIdx);
		assertTrue(asExpected && !hasNext);
	}
	
	@Test
	public void whenLatticesRequestedThenExpectedNumberReturned() throws IOException {
		uSLFinder.forEachRemaining(l -> semiLattices.add(l));
		/*
		int uSLIdx = 0;
		for (DirectedAcyclicGraph<String, EdgeForTests> uSL : semiLattices) {
			Visualizer.visualize(uSL, "2110081116_uSL" + Integer.toString(uSLIdx++));
		}
		*/
		assertTrue(semiLattices.size() == 8);
	}
	
	@Test
	public void whenLatticesRequestedThenReturnedLatticesAreValid() {
		boolean onlyValidLatticesReturned = true;
		int nbOfChecks = 0;
		while (uSLFinder.hasNext()) {
			nbOfChecks++;
			try {
				uSLFinder.validateNext();
			}
			catch (InvalidSemilatticeException e) {
				onlyValidLatticesReturned = false;
			}
		}
		assertTrue(onlyValidLatticesReturned && nbOfChecks > 0);
	}
	
	@Test
	public void whenLatticeIsReturnedThenOnlyItsMinimalElementsAreSupIrreducible() {
		fail("Not yet implemented");
	}
	
	private int[][] expectCoordinates() {
		int[][] expected = new int[24][];
		expected[0] = new int[] {0, 0, 0};
		expected[1] = new int[] {1, 0, 0};
		expected[2] = new int[] {2, 0, 0};
		expected[3] = new int[] {0, 1, 0};
		expected[4] = new int[] {1, 1, 0};
		expected[5] = new int[] {2, 1, 0};
		expected[6] = new int[] {0, 0, 1};
		expected[7] = new int[] {1, 0, 1};
		expected[8] = new int[] {2, 0, 1};
		expected[9] = new int[] {0, 1, 1};
		expected[10] = new int[] {1, 1, 1};
		expected[11] = new int[] {2, 1, 1};
		expected[12] = new int[] {0, 0, 2};
		expected[13] = new int[] {1, 0, 2};
		expected[14] = new int[] {2, 0, 2};
		expected[15] = new int[] {0, 1, 2};
		expected[16] = new int[] {1, 1, 2};
		expected[17] = new int[] {2, 1, 2};
		expected[18] = new int[] {0, 0, 3};
		expected[19] = new int[] {1, 0, 3};
		expected[20] = new int[] {2, 0, 3};
		expected[21] = new int[] {0, 1, 3};
		expected[22] = new int[] {1, 1, 3};
		expected[23] = new int[] {2, 1, 3};
		return expected;
	}

}
