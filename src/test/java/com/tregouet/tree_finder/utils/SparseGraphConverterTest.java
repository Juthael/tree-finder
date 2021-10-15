package com.tregouet.tree_finder.utils;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tregouet.tree_finder.EdgeForTests;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;

public class SparseGraphConverterTest {

	private DirectedAcyclicGraph<String, EdgeForTests> anyDAG;
	private String a = "A";
	private String b = "B";
	private String c = "C";
	private String d = "D";
	private String ab = "AB";
	private String bc = "BC";
	private String bcd = "BCD";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		anyDAG = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		anyDAG.addVertex(a);
		anyDAG.addVertex(b);
		anyDAG.addVertex(c);
		anyDAG.addVertex(d);
		anyDAG.addVertex(ab);
		anyDAG.addVertex(bc);
		anyDAG.addVertex(bcd);
		anyDAG.addEdge(a, ab);
		anyDAG.addEdge(b, ab);
		anyDAG.addEdge(b, bc);
		anyDAG.addEdge(c, bc);
		anyDAG.addEdge(bc, bcd);
		anyDAG.addEdge(d, bcd);
	}

	@Test
	public void whenGraphIsConvertedThenCanBeRebuiltInItsOriginalForm() {
		boolean originalVerticesRecovered;
		boolean originalEdgesRecovered;
		boolean rebuiltDAGEqualsOriginal;
		Set<String> dAGVertexSet = anyDAG.vertexSet();
		Set<EdgeForTests> dAGEdgeSet = anyDAG.edgeSet();
		SparseGraphConverter<String, EdgeForTests> converter = new SparseGraphConverter<>(anyDAG, false);
		SparseIntDirectedGraph sparseDAG = converter.getSparseGraph();
		IntArraySet sparseDAGVertexSet = new IntArraySet(sparseDAG.vertexSet());
		IntArrayList sparseDAGEdgeSet = new IntArrayList(sparseDAG.edgeSet());
		List<String> recoveredVertexSet = converter.getSet(sparseDAGVertexSet);
		List<EdgeForTests> recoveredEdgeSet = converter.getEdgeSet(sparseDAGEdgeSet);
		originalVerticesRecovered = dAGVertexSet.equals(new HashSet<>(recoveredVertexSet));
		originalEdgesRecovered = dAGEdgeSet.equals(new HashSet<>(recoveredEdgeSet));
		DirectedAcyclicGraph<String, EdgeForTests> rebuiltDAG = 
				new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		for (String recoveredVertex : recoveredVertexSet) {
			rebuiltDAG.addVertex(recoveredVertex);
		}
		for (EdgeForTests recoveredEdge : recoveredEdgeSet)
			rebuiltDAG.addEdge(recoveredEdge.getSource(), recoveredEdge.getTarget());
		rebuiltDAGEqualsOriginal = anyDAG.equals(rebuiltDAG);
		assertTrue (originalVerticesRecovered && originalEdgesRecovered && rebuiltDAGEqualsOriginal);
	}
	
	@Test
	public void whenGraphIsConvertedThenAscendingOrderOverVerticesIsTopological() {
		boolean ascendinfOrderIsTopological = true;
		int nbOfChecks = 0;
		List<String> dAGVerticesInTopologicalOrder = new ArrayList<>();
		new TopologicalOrderIterator<String, EdgeForTests>(anyDAG).forEachRemaining(
				v -> dAGVerticesInTopologicalOrder.add(v));
		SparseGraphConverter<String, EdgeForTests> converter = new SparseGraphConverter<>(anyDAG, false);
		SparseIntDirectedGraph sparseDAG = converter.getSparseGraph();
		List<Integer> sparseVertexSet = new ArrayList<>(sparseDAG.vertexSet());
		for (int i = 0 ; i < sparseVertexSet.size() - 1 ; i++) {
			Integer iSparseVertex = sparseVertexSet.get(i);
			String iRecoveredVertex = converter.getElement(iSparseVertex);
			for (int j = i + 1 ; j < sparseVertexSet.size() ; j++) {
				Integer jSparseVertex = sparseVertexSet.get(j);
				String jRecoveredVertex = converter.getElement(jSparseVertex);
				if (iSparseVertex < jSparseVertex 
						&& dAGVerticesInTopologicalOrder.indexOf(iRecoveredVertex) 
						>= dAGVerticesInTopologicalOrder.indexOf(jRecoveredVertex)) {
					ascendinfOrderIsTopological = false;
				}
				else if (iSparseVertex > jSparseVertex 
						&& dAGVerticesInTopologicalOrder.indexOf(iRecoveredVertex)
						<= dAGVerticesInTopologicalOrder.indexOf(jRecoveredVertex)) {
					ascendinfOrderIsTopological = false;
				}
				nbOfChecks++;
			}
		}
		assertTrue(ascendinfOrderIsTopological && nbOfChecks > 0);
	}

}
