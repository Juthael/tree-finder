package com.tregouet.tree_finder.data;

import static org.junit.Assert.*;

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
	DirectedAcyclicGraph<String, Edge> properTree;
	DirectedAcyclicGraph<String, Edge> notConnected;
	DirectedAcyclicGraph<String, Edge> noRoot;
	DirectedAcyclicGraph<String, Edge> manyPathsFromLeafToRoot;
	
	
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
			InTree<String, Edge> notConnectedArg = 
					new InTree<>(abc, leaves, notConnected, notConnected.edgeSet(), true);
		}
		catch (InvalidTreeException e) {
			exceptionIfNotConnected = true;
		}
		try {
			InTree<String, Edge> noRootArg = 
					new InTree<>(abc, leaves, noRoot, noRoot.edgeSet(), true);
		}
		catch (InvalidTreeException e) {
			exceptionIfNoRoot = true;
		}
		try {
			InTree<String, Edge> manyPathsArg = 
					new InTree<>(abc, leaves, manyPathsFromLeafToRoot, manyPathsFromLeafToRoot.edgeSet(), true);
		}
		catch (InvalidTreeException e) {
			exceptionIfManyPaths = true;
		}
		try {
			InTree<String, Edge> properArg = 
					new InTree<String, Edge>(abc, leaves, properTree, properTree.edgeSet(), true);
		}
		catch (InvalidTreeException e) {
			exceptionIfProperTree = true;
		}
		assertTrue(exceptionIfNotConnected && exceptionIfNoRoot && exceptionIfManyPaths && !exceptionIfProperTree);
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
	
	private void setNotConnected() {
		Set<Edge> edgesInNotConnected = upperSemiLattice.edgeSet()
				.stream()
				.filter(e -> !e.getSource().equals(a))
				.collect(Collectors.toSet());
		notConnected = new DirectedAcyclicGraph<>(null, null, false);
		Graphs.addAllEdges(notConnected, upperSemiLattice, edgesInNotConnected);
	}
	
	private void setNoRoot() {
		noRoot = new DirectedAcyclicGraph<>(null, null, false);
		Graphs.addAllEdges(noRoot, upperSemiLattice, upperSemiLattice.edgeSet());
		noRoot.removeVertex(abc);
		
	}
	
	private void setProperTree() {
		Set<Edge> edgesInProperTree = new HashSet<>();
		edgesInProperTree.add(upperSemiLattice.getEdge(a, ab));
		edgesInProperTree.add(upperSemiLattice.getEdge(b, ab));
		edgesInProperTree.add(upperSemiLattice.getEdge(c, bc));
		edgesInProperTree.add(upperSemiLattice.getEdge(ab, abc));
		edgesInProperTree.add(upperSemiLattice.getEdge(bc, abc));
		properTree = new DirectedAcyclicGraph<>(null, null, false);
		Graphs.addAllEdges(properTree, upperSemiLattice, edgesInProperTree);
	}
	
	private void setManyPathsFromLeafToRoot() {
		Set<Edge> edgesInManyPaths = new HashSet<>(upperSemiLattice.edgeSet());
		edgesInManyPaths.add(upperSemiLattice.getEdge(b, bc));
		manyPathsFromLeafToRoot = new DirectedAcyclicGraph<>(null, null, false);
		Graphs.addAllEdges(manyPathsFromLeafToRoot, upperSemiLattice, edgesInManyPaths);
	}

}

@SuppressWarnings("serial")
class Edge extends DefaultEdge {

	public String getSource() {
		return (String) super.getSource();
	}
	
}
