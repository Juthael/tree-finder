package com.tregouet.tree_finder.utils;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class CoordAdvancerTest {

	int[] coords = new int[4];
	int[] limits = new int[4];
	private int[][] expected;
	
	@Before
	public void setUp() throws Exception {
		coords[1] = 2;
		coords[3] = 4;
		limits[0] = 3;
		limits[1] = 3;
		limits[2] = 3;
		limits[3] = 5;
		expected = new int[9][];
		expected[0] = new int[] {0,2,0,4};
		expected[1] = new int[] {1,2,0,4};
		expected[2] = new int[] {2,2,0,4};
		expected[3] = new int[] {0,2,1,4};
		expected[4] = new int[] {1,2,1,4};
		expected[5] = new int[] {2,2,1,4};
		expected[6] = new int[] {0,2,2,4};
		expected[7] = new int[] {1,2,2,4};
		expected[8] = new int[] {2,2,2,4};
	}

	@Test
	public void whenAdvanceInSpecifiedAreaThenTraversalAsExpected() {
		boolean asExpected = true;
		int constant1Idx = 1;
		int constant2Idx = 3;
		int expIdx = 0;
		do {
			//System.out.println(Arrays.toString(coords));
			asExpected = (Arrays.equals(coords, expected[expIdx]));
			expIdx++;
		}
		while (CoordAdvancer.advanceInSpecifiedArea(coords, limits, constant1Idx, constant2Idx) && asExpected == true);
		assertTrue(asExpected);
	}

}
