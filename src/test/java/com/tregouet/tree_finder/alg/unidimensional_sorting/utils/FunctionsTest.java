package com.tregouet.tree_finder.alg.unidimensional_sorting.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tregouet.tree_finder.data.InvertedUpperSemilattice;
import com.tregouet.tree_finder.utils.Functions;

import utils.DichotomizableString;
import utils.EdgeForTests;

public class FunctionsTest {

	private final DichotomizableString a = new DichotomizableString("A");
	private final DichotomizableString b = new DichotomizableString("B");
	private final DichotomizableString c = new DichotomizableString("C");
	private final DichotomizableString d = new DichotomizableString("D");
	private final DichotomizableString e = new DichotomizableString("E");
	private InvertedUpperSemilattice<DichotomizableString, EdgeForTests> notComplementedUSL = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void whenEitherDepthFirstOrBreadthFirstIsStrictUpperBoundOfMethodCalledThenReturnsTheSame() {
		int nbOfChecks = 0;
		setUpNotComplementedUSL();
		Set<DichotomizableString> elements = notComplementedUSL.vertexSet();
		Iterator<DichotomizableString> iterator1 = elements.iterator(); 
		while (iterator1.hasNext()) {
			DichotomizableString e1 = iterator1.next();
			Iterator<DichotomizableString> iterator2 = elements.iterator();
			while (iterator2.hasNext()) {
				DichotomizableString e2 = iterator2.next();
				if (Functions.isStrictUpperBoundOfBreadthFirst(e1, e2, notComplementedUSL) 
						!= Functions.isStrictUpperBoundOfDepthFirst(e1, e2, notComplementedUSL))
					fail();
				nbOfChecks++;
			}
		}
		assertTrue(nbOfChecks > 0);
	}
	
	private void setUpNotComplementedUSL() {
		DichotomizableString ab = new DichotomizableString("AB");
		DichotomizableString bc = new DichotomizableString("BC");
		DichotomizableString cd = new DichotomizableString("CD");
		DichotomizableString abc = new DichotomizableString("ABC");
		DichotomizableString cde = new DichotomizableString("CDE");
		DichotomizableString abcde = new DichotomizableString("ABCDE");
		DirectedAcyclicGraph<DichotomizableString, EdgeForTests> notComplementedUSL = 
				new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		notComplementedUSL.addVertex(a);
		notComplementedUSL.addVertex(b);
		notComplementedUSL.addVertex(c);
		notComplementedUSL.addVertex(d);
		notComplementedUSL.addVertex(e);
		notComplementedUSL.addVertex(ab);
		notComplementedUSL.addVertex(bc);
		notComplementedUSL.addVertex(cd);
		notComplementedUSL.addVertex(abc);
		notComplementedUSL.addVertex(cde);
		notComplementedUSL.addVertex(abcde);
		notComplementedUSL.addEdge(a, ab);
		notComplementedUSL.addEdge(b, ab);
		notComplementedUSL.addEdge(b, bc);
		notComplementedUSL.addEdge(c, bc);
		notComplementedUSL.addEdge(c, cd);
		notComplementedUSL.addEdge(d, cd);
		notComplementedUSL.addEdge(e, cde);
		notComplementedUSL.addEdge(ab, abc);
		notComplementedUSL.addEdge(bc, abc);
		notComplementedUSL.addEdge(cd, cde);
		notComplementedUSL.addEdge(abc, abcde);
		notComplementedUSL.addEdge(cde, abcde);
		List<DichotomizableString> topologicalOrder =new ArrayList<>();
		new TopologicalOrderIterator<>(notComplementedUSL).forEachRemaining(topologicalOrder::add);
		Set<DichotomizableString> leaves = new HashSet<>(Arrays.asList(new DichotomizableString[] {a, b, c, d, e}));
		this.notComplementedUSL = 
				new InvertedUpperSemilattice<DichotomizableString, EdgeForTests>(
						notComplementedUSL, abcde, leaves, topologicalOrder);
	}	

}
