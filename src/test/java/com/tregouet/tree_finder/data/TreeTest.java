package com.tregouet.tree_finder.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TreeTest {

	private String a = "A";
	private String b = "B";
	private String c = "C";
	private String d = "D";
	private String ab = "AB";
	private String ac = "AC";
	private String bc = "BC";
	private String abc = "ABC";
	DirectedAcyclicGraph<String, Edge> properTreeDAG;
	DirectedAcyclicGraph<String, Edge> notRootedDAG;
	DirectedAcyclicGraph<String, Edge> violatingHierarchyClauseDAG;
	Tree<String, Edge> properTree;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		setTree();
		setUnrootedDAG();
		setHierarchyClauseViolatingDAG();
	}

	@Test
	public void whenDifferentVerticesThenNotEqual() {
		DirectedAcyclicGraph<String, Edge> otherDAG = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllEdges(otherDAG, properTreeDAG, properTreeDAG.edgeSet());
		otherDAG.addVertex(d);
		otherDAG.addEdge(d, abc);
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(otherDAG);
		List<String> topoOrder = new ArrayList<>();
		new TopologicalOrderIterator<>(otherDAG).forEachRemaining(topoOrder::add);
		Tree<String, Edge> otherTree = 
				new Tree<String, Edge>(otherDAG, abc, new HashSet<String>(Arrays.asList(new String[] {a, b, c})), 
						topoOrder);
		assertFalse(properTree.equals(otherTree));
	}
	
	@Test
	public void whenSameVerticesAndDifferentEdgesThenNotEqual() {
		DirectedAcyclicGraph<String, Edge> differentTreeArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(differentTreeArg, properTreeDAG.vertexSet());
		differentTreeArg.addEdge(a, ab);
		differentTreeArg.addEdge(b, ab);
		differentTreeArg.addEdge(c, abc);
		differentTreeArg.addEdge(ab, abc);		
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(differentTreeArg);
		List<String> topoOrder = new ArrayList<>();
		new TopologicalOrderIterator<>(differentTreeArg).forEachRemaining(topoOrder::add);
		Tree<String, Edge> differentTree = 
				new Tree<String, Edge>(differentTreeArg, abc, new HashSet<String>(Arrays.asList(new String[] {a, b, c})), 
						topoOrder);
		assertFalse(differentTree.equals(properTreeDAG));
	}
	
	//provided edge class overrides hashCode() and equals()
	@Test
	public void whenSameVerticesAndSameEdgesThenEqual() {
		DirectedAcyclicGraph<String, Edge> sameTreeDAG = new DirectedAcyclicGraph<>(null, Edge::new, false);
		sameTreeDAG.addVertex(a);
		sameTreeDAG.addVertex(b);
		sameTreeDAG.addVertex(c);
		sameTreeDAG.addVertex(ab);
		sameTreeDAG.addVertex(ac);
		sameTreeDAG.addVertex(abc);
		sameTreeDAG.addEdge(a, ab);
		sameTreeDAG.addEdge(b, ab);
		sameTreeDAG.addEdge(c, ac);
		sameTreeDAG.addEdge(ab, abc);
		sameTreeDAG.addEdge(ac, abc);
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(sameTreeDAG);
		List<String> topoOrder = new ArrayList<>();
		new TopologicalOrderIterator<>(sameTreeDAG).forEachRemaining(topoOrder::add);
		Tree<String, Edge> sameTree = 
				new Tree<String, Edge>(sameTreeDAG, abc, new HashSet<String>(Arrays.asList(new String[] {a, b, c})), 
						topoOrder);
		assertTrue(sameTree.equals(properTree));
	}
	
	@Test
	public void whenEntropyReductionMatrixRequiredThenReturned() {
		boolean asExpected = true;
		Double[][] entropyReductionMatrix = null;
		try {
			entropyReductionMatrix = properTree.getEntropyReductionMatrix();			
		}
		catch (Exception e) {
			asExpected = false;
		}
		assertTrue(asExpected && entropyReductionMatrix != null);
	}
	
	private void setUnrootedDAG() {
		notRootedDAG = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllEdges(notRootedDAG, properTreeDAG, properTreeDAG.edgeSet());
		notRootedDAG.removeVertex(abc);
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(notRootedDAG);
	}	
	
	private void setHierarchyClauseViolatingDAG() {
		violatingHierarchyClauseDAG = new DirectedAcyclicGraph<>(null, Edge::new, false);
		violatingHierarchyClauseDAG.addVertex(a);
		violatingHierarchyClauseDAG.addVertex(b);
		violatingHierarchyClauseDAG.addVertex(c);
		violatingHierarchyClauseDAG.addVertex(ab);
		violatingHierarchyClauseDAG.addVertex(ac);
		violatingHierarchyClauseDAG.addVertex(bc);
		violatingHierarchyClauseDAG.addVertex(abc);
		violatingHierarchyClauseDAG.addEdge(a, ab);
		violatingHierarchyClauseDAG.addEdge(a, ac);
		violatingHierarchyClauseDAG.addEdge(b, ab);
		violatingHierarchyClauseDAG.addEdge(b, bc);
		violatingHierarchyClauseDAG.addEdge(c, ac);
		violatingHierarchyClauseDAG.addEdge(c, bc);
		violatingHierarchyClauseDAG.addEdge(ab, abc);
		violatingHierarchyClauseDAG.addEdge(ac, abc);
		violatingHierarchyClauseDAG.addEdge(bc, abc);
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(violatingHierarchyClauseDAG);
	}
	
	private void setTree() {
		properTreeDAG = new DirectedAcyclicGraph<>(null, Edge::new, false);
		properTreeDAG.addVertex(a);
		properTreeDAG.addVertex(b);
		properTreeDAG.addVertex(c);
		properTreeDAG.addVertex(ab);
		properTreeDAG.addVertex(ac);
		properTreeDAG.addVertex(abc);
		properTreeDAG.addEdge(a, ab);
		properTreeDAG.addEdge(b, ab);
		properTreeDAG.addEdge(c, ac);
		properTreeDAG.addEdge(ab, abc);
		properTreeDAG.addEdge(ac, abc);
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(properTreeDAG);
		List<String> topoOrder = new ArrayList<>();
		new TopologicalOrderIterator<>(properTreeDAG).forEachRemaining(topoOrder::add);
		properTree = 
				new Tree<String, Edge>(properTreeDAG, abc, new HashSet<String>(Arrays.asList(new String[] {a, b, c})), 
						topoOrder);
	}
}

@SuppressWarnings("serial")
class Edge extends DefaultEdge {

	@Override
	public boolean equals(Object o) {
		if (getClass() != o.getClass())
			return false;
		Edge other = (Edge) o;
		return (getSource().equals(other.getSource()) && getTarget().equals(other.getTarget()));
	}
	
	@Override
	public String getSource() {
		return (String) super.getSource();
	}
	
	@Override
	public int hashCode() {
		return getSource().hashCode() + getTarget().hashCode();
	}	
}
