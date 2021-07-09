package com.tregouet.tree_finder.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.Before;
import org.junit.Test;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.InTree;
import com.tregouet.tree_finder.error.InvalidTreeException;

public class TreeFinderTest {

	private String a = "A";
	private String b = "B";
	private String c = "C";
	private String ab = "AB";
	private String ac = "AC";
	private String bc = "BC";
	private String abc = "ABC";
	private List<String> vertices = new ArrayList<>(Arrays.asList(new String[] {a, b, c, ab, ac, bc, abc}));
	private List<String> leaves = new ArrayList<>(Arrays.asList(new String[]{a, b, c}));
	DirectedAcyclicGraph<String, Edge> upperSemiLattice;
	
	@Before
	public void setUp() throws Exception {
		setUpperSemiLattice();
	}

	@Test
	public void whenTreesRequestedThenExpectedReturned() throws InvalidTreeException {
		ITreeFinder<String, Edge> finder = new TreeFinder<>(upperSemiLattice);
		boolean asExpected = true;
		Set<InTree<String, Edge>> expected = new HashSet<>();
		expected.add(setN1());
		expected.add(setN2());
		expected.add(setN3());
		expected.add(setN4());
		expected.add(setN5());
		expected.add(setN6());
		expected.add(setN7());
		expected.add(setN8());
		if (finder.getNbOfTrees() != expected.size())
			asExpected = false;
		else {
			while (asExpected && finder.hasNext()) {
				asExpected = expected.remove(finder.next());
			}
		}
		assertTrue(asExpected);
	}
	
	private void setUpperSemiLattice() {
		upperSemiLattice = new DirectedAcyclicGraph<>(null, Edge::new, false);
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
	
	
	
	private InTree<String, Edge> setN1() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, vertices);
		nArg.removeVertex(bc);
		nArg.addEdge(a, ab);
		nArg.addEdge(b, ab);
		nArg.addEdge(c, ac);
		nArg.addEdge(ab, abc);
		nArg.addEdge(ac, abc);
		return new InTree<String, Edge>(abc, leaves, nArg, nArg.edgeSet(), true);
	}
	
	private InTree<String, Edge> setN2() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, vertices);
		nArg.removeVertex(ac);
		nArg.addEdge(a, ab);
		nArg.addEdge(b, ab);
		nArg.addEdge(c, bc);
		nArg.addEdge(ab, abc);
		nArg.addEdge(bc, abc);
		return new InTree<String, Edge>(abc, leaves, nArg, nArg.edgeSet(), true);
	}
	
	private InTree<String, Edge> setN3() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, vertices);
		nArg.addEdge(a, ab);
		nArg.addEdge(b, bc);
		nArg.addEdge(c, ac);
		nArg.addEdge(ab, abc);
		nArg.addEdge(ac, abc);
		nArg.addEdge(bc, abc);
		return new InTree<String, Edge>(abc, leaves, nArg, nArg.edgeSet(), true);
	}	
	
	private InTree<String, Edge> setN4() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, vertices);
		nArg.removeVertex(ac);
		nArg.addEdge(a, ab);
		nArg.addEdge(b, bc);
		nArg.addEdge(c, bc);
		nArg.addEdge(ab, abc);
		nArg.addEdge(bc, abc);
		return new InTree<String, Edge>(abc, leaves, nArg, nArg.edgeSet(), true);
	}
	
	private InTree<String, Edge> setN5() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, vertices);
		nArg.removeVertex(bc);
		nArg.addEdge(a, ac);
		nArg.addEdge(b, ab);
		nArg.addEdge(c, ac);
		nArg.addEdge(ab, abc);
		nArg.addEdge(ac, abc);
		return new InTree<String, Edge>(abc, leaves, nArg, nArg.edgeSet(), true);
	}
	
	private InTree<String, Edge> setN6() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, vertices);
		nArg.addEdge(a, ac);
		nArg.addEdge(b, ab);
		nArg.addEdge(c, bc);
		nArg.addEdge(ab, abc);
		nArg.addEdge(ac, abc);
		nArg.addEdge(bc, abc);
		return new InTree<String, Edge>(abc, leaves, nArg, nArg.edgeSet(), true);
	}
	
	private InTree<String, Edge> setN7() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, vertices);
		nArg.removeVertex(ab);
		nArg.addEdge(a, ac);
		nArg.addEdge(b, bc);
		nArg.addEdge(c, ac);
		nArg.addEdge(ac, abc);
		nArg.addEdge(bc, abc);
		return new InTree<String, Edge>(abc, leaves, nArg, nArg.edgeSet(), true);
	}
	
	private InTree<String, Edge> setN8() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, vertices);
		nArg.removeVertex(ab);
		nArg.addEdge(a, ac);
		nArg.addEdge(b, bc);
		nArg.addEdge(c, bc);
		nArg.addEdge(ac, abc);
		nArg.addEdge(bc, abc);
		return new InTree<String, Edge>(abc, leaves, nArg, nArg.edgeSet(), true);
	}

}

@SuppressWarnings("serial")
class Edge extends DefaultEdge {

	@Override
	public String getSource() {
		return (String) super.getSource();
	}
	
	@Override
	public int hashCode() {
		return getSource().hashCode() + getTarget().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (getClass() != o.getClass())
			return false;
		Edge other = (Edge) o;
		return (getSource().equals(other.getSource()) && getTarget().equals(other.getTarget()));
	}
	
}
