package com.tregouet.tree_finder.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.Before;
import org.junit.Test;

import com.tregouet.tree_finder.error.InvalidTreeException;

public class InTreeTest {

	private String a = "A";
	private String b = "B";
	private String c = "C";
	private String ab = "AB";
	private String ac = "AC";
	private String bc = "BC";
	private String abc = "ABC";
	private List<String> leaves = new ArrayList<>(Arrays.asList(new String[]{a, b, c}));
	DirectedAcyclicGraph<String, Edge> upperSemiLattice;
	DirectedAcyclicGraph<String, Edge> properTreeArg;
	DirectedAcyclicGraph<String, Edge> notConnectedArg;
	DirectedAcyclicGraph<String, Edge> noRootArg;
	DirectedAcyclicGraph<String, Edge> manyPathsArg;
	
	
	@Before
	public void setUp() throws Exception {
		setUpperSemiLattice();
		setProperTree();
		setNotConnected();
		setNoRoot();
		setManyPathsFromLeafToRoot();
	}

	@SuppressWarnings("unused")
	@Test
	public void whenSafeConstructorUsedThenIllegalArgThrowsException() {
		boolean exceptionIfNotConnected = false;
		boolean exceptionIfNoRoot = false;
		boolean exceptionIfManyPaths = false;
		boolean exceptionIfProperTree = false;
		try {
			InTree<String, Edge> notConnected = 
					new InTree<>(abc, leaves, notConnectedArg, notConnectedArg.edgeSet(), true);
		}
		catch (InvalidTreeException e) {
			exceptionIfNotConnected = true;
		}
		try {
			InTree<String, Edge> noRoot = 
					new InTree<>(abc, leaves, noRootArg, noRootArg.edgeSet(), true);
		}
		catch (InvalidTreeException e) {
			exceptionIfNoRoot = true;
		}
		try {
			InTree<String, Edge> manyPaths = 
					new InTree<>(abc, leaves, manyPathsArg, manyPathsArg.edgeSet(), true);
		}
		catch (InvalidTreeException e) {
			exceptionIfManyPaths = true;
		}
		try {
			InTree<String, Edge> properTree = 
					new InTree<String, Edge>(abc, leaves, properTreeArg, properTreeArg.edgeSet(), true);
		}
		catch (InvalidTreeException e) {
			exceptionIfProperTree = true;
		}
		assertTrue(exceptionIfNotConnected && exceptionIfNoRoot && exceptionIfManyPaths && !exceptionIfProperTree);
	}
	
	@Test
	public void whenDifferentVerticesThenNotEqual() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> smallerTreeArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		smallerTreeArg.addVertex(abc);
		smallerTreeArg.addVertex(ab);
		smallerTreeArg.addVertex(ac);
		smallerTreeArg.addVertex(a);
		smallerTreeArg.addVertex(b);
		smallerTreeArg.addEdge(ab, abc);
		smallerTreeArg.addEdge(ac, abc);
		smallerTreeArg.addEdge(b, ab);
		smallerTreeArg.addEdge(a, ac);
		List<String> smallerTreeLeaves = new ArrayList<>(Arrays.asList(new String[] {a, b}));
		InTree<String, Edge> smallerTree = 
				new InTree<String, Edge>(abc, smallerTreeLeaves, smallerTreeArg, smallerTreeArg.edgeSet(), true);
		assertFalse(properTreeArg.equals(smallerTree));
	}
	
	@Test
	public void whenSameVerticesAndDifferentEdgesThenNotEqual() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> differentTreeArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(differentTreeArg, properTreeArg.vertexSet());
		differentTreeArg.addEdge(ab, abc);
		differentTreeArg.addEdge(bc, abc);
		differentTreeArg.addEdge(a, ab);
		differentTreeArg.addEdge(b, bc);
		differentTreeArg.addEdge(c, bc);
		InTree<String, Edge> differentTree = 
				new InTree<String, Edge>(abc, leaves, differentTreeArg, differentTreeArg.edgeSet(), true);
		assertFalse(differentTree.equals(properTreeArg));
	}
	
	//provided edge class overrides hashCode() and equals()
	@Test
	public void whenSameVerticesAndSameEdgesThenEqual() throws InvalidTreeException {
		InTree<String, Edge> propertTree = 
				new InTree<String, Edge>(abc, leaves, properTreeArg, properTreeArg.edgeSet(), true);
		DirectedAcyclicGraph<String, Edge> sameTreeArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(sameTreeArg, properTreeArg.vertexSet());
		sameTreeArg.addEdge(ab, abc);
		sameTreeArg.addEdge(bc, abc);
		sameTreeArg.addEdge(a, ab);
		sameTreeArg.addEdge(b, ab);
		sameTreeArg.addEdge(c, bc);
		InTree<String, Edge> sameTree = 
				new InTree<String, Edge>(abc, leaves, sameTreeArg, sameTreeArg.edgeSet(), true);
		assertTrue(sameTree.equals(propertTree));
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
	
	private void setNotConnected() {
		Set<Edge> edgesInNotConnected = upperSemiLattice.edgeSet()
				.stream()
				.filter(e -> !e.getSource().equals(a))
				.collect(Collectors.toSet());
		notConnectedArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllEdges(notConnectedArg, upperSemiLattice, edgesInNotConnected);
	}
	
	private void setNoRoot() {
		noRootArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllEdges(noRootArg, upperSemiLattice, upperSemiLattice.edgeSet());
		noRootArg.removeVertex(abc);
		
	}
	
	private void setProperTree() {
		Set<Edge> edgesInProperTree = new HashSet<>();
		edgesInProperTree.add(upperSemiLattice.getEdge(a, ab));
		edgesInProperTree.add(upperSemiLattice.getEdge(b, ab));
		edgesInProperTree.add(upperSemiLattice.getEdge(c, bc));
		edgesInProperTree.add(upperSemiLattice.getEdge(ab, abc));
		edgesInProperTree.add(upperSemiLattice.getEdge(bc, abc));
		properTreeArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllEdges(properTreeArg, upperSemiLattice, edgesInProperTree);
	}
	
	private void setManyPathsFromLeafToRoot() {
		Set<Edge> edgesInManyPaths = new HashSet<>(upperSemiLattice.edgeSet());
		edgesInManyPaths.add(upperSemiLattice.getEdge(b, bc));
		manyPathsArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllEdges(manyPathsArg, upperSemiLattice, edgesInManyPaths);
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
