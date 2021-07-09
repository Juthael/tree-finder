package com.tregouet.tree_finder.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.Before;
import org.junit.Test;

public class TreeFinderTest {

	private String a = "A";
	private String b = "B";
	private String c = "C";
	private String ab = "AB";
	private String ac = "AC";
	private String bc = "BC";
	private String abc = "ABC";
	private List<String> leaves = new ArrayList<>(Arrays.asList(new String[]{a, b, c}));
	DirectedAcyclicGraph<String, Edge> upperSemiLattice;
	
	@Before
	public void setUp() throws Exception {
		setUpperSemiLattice();
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	private void setUpperSemiLattice() {
		upperSemiLattice = new DirectedAcyclicGraph<>(null, null, false);
		upperSemiLattice.addVertex(a);
		upperSemiLattice.addVertex(b);
		upperSemiLattice.addVertex(c);
		upperSemiLattice.addVertex(ab);
		upperSemiLattice.addVertex(ac);
		upperSemiLattice.addVertex(bc);
		upperSemiLattice.addVertex(abc);
		upperSemiLattice.addEdge(a, ab);
		upperSemiLattice.addEdge(a, ac);
		upperSemiLattice.addEdge(b, ab);
		upperSemiLattice.addEdge(b, bc);
		upperSemiLattice.addEdge(c, ac);
		upperSemiLattice.addEdge(c, bc);
		upperSemiLattice.addEdge(ab, abc);
		upperSemiLattice.addEdge(ac, abc);
		upperSemiLattice.addEdge(bc, abc);
	}	

}

@SuppressWarnings("serial")
class Edge extends DefaultEdge {

	public String getSource() {
		return (String) super.getSource();
	}
	
}
