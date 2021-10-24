package com.tregouet.tree_finder.utils;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveClosure;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tregouet.tree_finder.EdgeForTests;
import com.tregouet.tree_finder.algo.hierarchical_restriction.utils.SparseGraphConverter;

public class StructureInspectorTest {
	
	private DirectedAcyclicGraph<String, EdgeForTests> classificationTree;
	private DirectedAcyclicGraph<String, EdgeForTests> wronglyOrientedClassificationTree;
	private DirectedAcyclicGraph<String, EdgeForTests> upperSemilatticeButNotTree;
	private DirectedAcyclicGraph<String, EdgeForTests> doesNotHaveARoot;
	private DirectedAcyclicGraph<String, EdgeForTests> isNotAtomistic;
	private DirectedAcyclicGraph<String, EdgeForTests> classificationTreeReduced;
	private DirectedAcyclicGraph<String, EdgeForTests> wronglyOrientedClassificationTreeReduced;
	private DirectedAcyclicGraph<String, EdgeForTests> upperSemilatticeButNotTreeReduced;
	private DirectedAcyclicGraph<String, EdgeForTests> doesNotHaveARootReduced;
	
	private String a = "A";
	private String b = "B";
	private String c = "C";
	private String d = "D";
	private String e = "E";
	private String ab = "AB";
	private String bc = "BC";
	private String bcd = "BCD";
	private String abcd = "ABCD";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		setUpClassificationTree();
		setUpLeafOrientedClassificationTree();
		setUpPowerSetMinusEmptySet();
		setUpUnrootedDAG();
	}

	@Test
	public void whenChecksIfParameterIsClassificationTreeThenReturnsAsExpected() {
		assertTrue(
				StructureInspector.isATree(classificationTree)
				&& !StructureInspector.isATree(wronglyOrientedClassificationTree)
				&& !StructureInspector.isATree(upperSemilatticeButNotTree)
				&& !StructureInspector.isATree(doesNotHaveARoot));
	}
	
	@Test
	public void whenChecksIfDAGParameterIsAnUpperSemilatticeThenReturnsAsExpected() {
		assertTrue(
				StructureInspector.isAnUpperSemilattice(classificationTree)
				&& !StructureInspector.isAnUpperSemilattice(wronglyOrientedClassificationTree)
				&& StructureInspector.isAnUpperSemilattice(upperSemilatticeButNotTree)
				&& !StructureInspector.isAnUpperSemilattice(doesNotHaveARoot));
	}
	
	@Test
	public void whenChecksIfSparseParameterIsAnUpperSemilatticeThenReturnsAsExpected() {
		SparseGraphConverter<String, EdgeForTests> classificationTreeConverter = 
				new SparseGraphConverter<>(classificationTree, true);
		SparseGraphConverter<String, EdgeForTests> wronglyOrientedConverter = 
				new SparseGraphConverter<>(wronglyOrientedClassificationTree, true);
		SparseGraphConverter<String, EdgeForTests> semilatticeNotTreeConverter = 
				new SparseGraphConverter<>(upperSemilatticeButNotTree, true);
		SparseGraphConverter<String, EdgeForTests> unrootedConverter = 
				new SparseGraphConverter<>(doesNotHaveARoot, true);
		SparseIntDirectedGraph classificationTreeSparse = classificationTreeConverter.getSparseGraph();
		SparseIntDirectedGraph wronglyOrientedSparse = wronglyOrientedConverter.getSparseGraph();
		SparseIntDirectedGraph semilatticeNotTreeSparse = semilatticeNotTreeConverter.getSparseGraph();
		SparseIntDirectedGraph unrootedSparse = unrootedConverter.getSparseGraph();
		assertTrue(StructureInspector.isAnUpperSemilattice(classificationTreeSparse)
				&& !StructureInspector.isAnUpperSemilattice(wronglyOrientedSparse)
				&& StructureInspector.isAnUpperSemilattice(semilatticeNotTreeSparse)
				&& !StructureInspector.isAnUpperSemilattice(unrootedSparse));
	}
	
	@Test
	public void whenChecksIfParameterIsRootedAndInvertedThenReturnsAsExpected() {
		assertTrue(StructureInspector.isARootedInvertedDirectedAcyclicGraph(classificationTree)
				&& !StructureInspector.isARootedInvertedDirectedAcyclicGraph(wronglyOrientedClassificationTree)
				&& StructureInspector.isARootedInvertedDirectedAcyclicGraph(upperSemilatticeButNotTree)
				&& !StructureInspector.isARootedInvertedDirectedAcyclicGraph(doesNotHaveARoot));
	}
	
	@Test 
	public void whenCheckIfParameterIsAtomisticThenReturnsAsExpected() throws IOException {
		setUpNotAtomisticDAG();
		assertTrue(StructureInspector.isAtomistic(classificationTree)
				&& !StructureInspector.isAtomistic(wronglyOrientedClassificationTree)
				&& StructureInspector.isAtomistic(upperSemilatticeButNotTree)
				&& StructureInspector.isAtomistic(doesNotHaveARoot)
				&& !StructureInspector.isAtomistic(isNotAtomistic));
	}
	
	@Test
	public void whenChecksIfSparseParameterIsTransitiveThenReturnsTrueOnlyWhenExpected() {
		setUpReducedDAGS();
		SparseGraphConverter<String, EdgeForTests> classificationTreeConverter = 
				new SparseGraphConverter<>(classificationTree, true);
		SparseGraphConverter<String, EdgeForTests> wronglyOrientedConverter = 
				new SparseGraphConverter<>(wronglyOrientedClassificationTree, true);
		SparseGraphConverter<String, EdgeForTests> semilatticeNotTreeConverter = 
				new SparseGraphConverter<>(upperSemilatticeButNotTree, true);
		SparseGraphConverter<String, EdgeForTests> unrootedConverter = 
				new SparseGraphConverter<>(doesNotHaveARoot, true);
		SparseGraphConverter<String, EdgeForTests> redClassificationTreeConverter =
				
				new SparseGraphConverter<>(classificationTreeReduced, true);
		SparseGraphConverter<String, EdgeForTests> redWronglyOrientedConverter = 
				new SparseGraphConverter<>(wronglyOrientedClassificationTreeReduced, true);
		SparseGraphConverter<String, EdgeForTests> redSemilatticeNotTreeConverter = 
				new SparseGraphConverter<>(upperSemilatticeButNotTreeReduced, true);
		SparseGraphConverter<String, EdgeForTests> redUnrootedConverter = 
				new SparseGraphConverter<>(doesNotHaveARootReduced, true);
		
		SparseIntDirectedGraph classificationTreeSparse = classificationTreeConverter.getSparseGraph();
		SparseIntDirectedGraph wronglyOrientedSparse = wronglyOrientedConverter.getSparseGraph();
		SparseIntDirectedGraph semilatticeNotTreeSparse = semilatticeNotTreeConverter.getSparseGraph();
		SparseIntDirectedGraph unrootedSparse = unrootedConverter.getSparseGraph();
		
		SparseIntDirectedGraph classificationTreeSparseRed = redClassificationTreeConverter.getSparseGraph();
		SparseIntDirectedGraph wronglyOrientedSparseRed = redWronglyOrientedConverter.getSparseGraph();
		SparseIntDirectedGraph semilatticeNotTreeSparseRed = redSemilatticeNotTreeConverter.getSparseGraph();
		SparseIntDirectedGraph unrootedSparseRed = redUnrootedConverter.getSparseGraph();
		
		assertTrue(StructureInspector.isTransitive(classificationTreeSparse)
				&& !StructureInspector.isTransitive(classificationTreeSparseRed)
				&& StructureInspector.isTransitive(wronglyOrientedSparse)
				&& !StructureInspector.isTransitive(wronglyOrientedSparseRed)
				&& StructureInspector.isTransitive(semilatticeNotTreeSparse)
				&& !StructureInspector.isTransitive(semilatticeNotTreeSparseRed)
				&& StructureInspector.isTransitive(unrootedSparse)
				&& !StructureInspector.isTransitive(unrootedSparseRed));
	}
	
	@Test
	public void whenChecksIfSparseParameterIsRootedThenReturnsTrueOnlyWhenExpected() {
		SparseGraphConverter<String, EdgeForTests> classificationTreeConverter = 
				new SparseGraphConverter<>(classificationTree, true);
		SparseGraphConverter<String, EdgeForTests> wronglyOrientedConverter = 
				new SparseGraphConverter<>(wronglyOrientedClassificationTree, true);
		SparseGraphConverter<String, EdgeForTests> semilatticeNotTreeConverter = 
				new SparseGraphConverter<>(upperSemilatticeButNotTree, true);
		SparseGraphConverter<String, EdgeForTests> unrootedConverter = 
				new SparseGraphConverter<>(doesNotHaveARoot, true);
		SparseIntDirectedGraph classificationTreeSparse = classificationTreeConverter.getSparseGraph();
		SparseIntDirectedGraph wronglyOrientedSparse = wronglyOrientedConverter.getSparseGraph();
		SparseIntDirectedGraph semilatticeNotTreeSparse = semilatticeNotTreeConverter.getSparseGraph();
		SparseIntDirectedGraph unrootedSparse = unrootedConverter.getSparseGraph();
		assertTrue(StructureInspector.isRooted(classificationTreeSparse)
				&& !StructureInspector.isRooted(wronglyOrientedSparse)
				&& StructureInspector.isRooted(semilatticeNotTreeSparse)
				&& !StructureInspector.isRooted(unrootedSparse));
	}
	
	@Test
	public void whenTransitiveClosureRequestedThenReturned() {
		setUpReducedDAGS();
		SparseGraphConverter<String, EdgeForTests> converter = 
				new SparseGraphConverter<>(upperSemilatticeButNotTreeReduced, true);
		SparseIntDirectedGraph semilatticeNotTreeSparseRed = converter.getSparseGraph();
		SparseIntDirectedGraph semilatticeNotTreeSparseClosedAgain = 
				StructureInspector.getTransitiveClosure(semilatticeNotTreeSparseRed);
		DirectedAcyclicGraph<String, EdgeForTests> semiLatticeClosedAgain = 
				new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		for (Integer sparseVertex : semilatticeNotTreeSparseClosedAgain.vertexSet())
			semiLatticeClosedAgain.addVertex(converter.getElement(sparseVertex));
		for (Integer sparseEdge : semilatticeNotTreeSparseClosedAgain.edgeSet()) {
			Integer sparseSource = semilatticeNotTreeSparseClosedAgain.getEdgeSource(sparseEdge);
			Integer sparseTarget = semilatticeNotTreeSparseClosedAgain.getEdgeTarget(sparseEdge);
			semiLatticeClosedAgain.addEdge(converter.getElement(sparseSource), converter.getElement(sparseTarget));
		}
		assertTrue(semiLatticeClosedAgain.equals(upperSemilatticeButNotTree));
	}
	
	@Test
	public void theCallOfAnyMethodReturnsTheSameValueWetherTheParameterIsAGivenGraphOrItsTransitiveReduction() {
		//except isTransitive(), obviously
		setUpReducedDAGS();
		assertTrue(
				(StructureInspector.isATree(classificationTree) == 
					StructureInspector.isATree(classificationTreeReduced)) 
				&& (StructureInspector.isATree(wronglyOrientedClassificationTree) == 
				StructureInspector.isATree(wronglyOrientedClassificationTreeReduced)) 
				&& (StructureInspector.isATree(upperSemilatticeButNotTree) == 
				StructureInspector.isATree(upperSemilatticeButNotTreeReduced)) 
				&& (StructureInspector.isATree(doesNotHaveARoot) == 
				StructureInspector.isATree(doesNotHaveARootReduced)) 
				
				&& (StructureInspector.isAnUpperSemilattice(classificationTree) == 
					StructureInspector.isAnUpperSemilattice(classificationTreeReduced))
				&& (StructureInspector.isAnUpperSemilattice(wronglyOrientedClassificationTree) == 
				StructureInspector.isAnUpperSemilattice(wronglyOrientedClassificationTreeReduced))
				&& (StructureInspector.isAnUpperSemilattice(upperSemilatticeButNotTree) == 
				StructureInspector.isAnUpperSemilattice(upperSemilatticeButNotTreeReduced))
				&& (StructureInspector.isAnUpperSemilattice(doesNotHaveARoot) == 
				StructureInspector.isAnUpperSemilattice(doesNotHaveARootReduced))
				
				&& (StructureInspector.isARootedInvertedDirectedAcyclicGraph(classificationTree) == 
				StructureInspector.isARootedInvertedDirectedAcyclicGraph(classificationTreeReduced))
				&& (StructureInspector.isARootedInvertedDirectedAcyclicGraph(wronglyOrientedClassificationTree) == 
				StructureInspector.isARootedInvertedDirectedAcyclicGraph(wronglyOrientedClassificationTreeReduced))
				&& (StructureInspector.isARootedInvertedDirectedAcyclicGraph(upperSemilatticeButNotTree) == 
				StructureInspector.isARootedInvertedDirectedAcyclicGraph(upperSemilatticeButNotTreeReduced))
				&& (StructureInspector.isARootedInvertedDirectedAcyclicGraph(doesNotHaveARoot) == 
				StructureInspector.isARootedInvertedDirectedAcyclicGraph(doesNotHaveARootReduced))
				);
	}
	
	private void setUpClassificationTree() {
		classificationTree = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		classificationTree.addVertex(a);
		classificationTree.addVertex(b);
		classificationTree.addVertex(c);
		classificationTree.addVertex(d);
		classificationTree.addVertex(bc);
		classificationTree.addVertex(bcd);
		classificationTree.addVertex(abcd);
		classificationTree.addEdge(a, abcd);
		classificationTree.addEdge(b, bc);
		classificationTree.addEdge(c, bc);
		classificationTree.addEdge(d, bcd);
		classificationTree.addEdge(bc, bcd);
		classificationTree.addEdge(bcd, abcd);
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(classificationTree);
	}
	
	private void setUpLeafOrientedClassificationTree() {
		wronglyOrientedClassificationTree = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		wronglyOrientedClassificationTree.addVertex(a);
		wronglyOrientedClassificationTree.addVertex(b);
		wronglyOrientedClassificationTree.addVertex(c);
		wronglyOrientedClassificationTree.addVertex(d);
		wronglyOrientedClassificationTree.addVertex(bc);
		wronglyOrientedClassificationTree.addVertex(bcd);
		wronglyOrientedClassificationTree.addVertex(abcd);
		wronglyOrientedClassificationTree.addEdge(abcd, a);
		wronglyOrientedClassificationTree.addEdge(bc, b);
		wronglyOrientedClassificationTree.addEdge(bc, c);
		wronglyOrientedClassificationTree.addEdge(bcd, d);
		wronglyOrientedClassificationTree.addEdge(bc, bcd);
		wronglyOrientedClassificationTree.addEdge(abcd, bcd);
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(wronglyOrientedClassificationTree);
	}
	
	private void setUpPowerSetMinusEmptySet() {
		upperSemilatticeButNotTree = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		List<String> set = new ArrayList<>(Arrays.asList(new String[] {a, b, c, d, e}));
		int setCardinal = set.size();
		List<Set<String>> powerSet = new ArrayList<>();
		List<String> powerSetAsStrings = new ArrayList<>();
		for (int i = 0 ; i < (1 << setCardinal) ; i++) {
			Set<String> subset = new HashSet<>(setCardinal);
			for (int j = 0 ; j < setCardinal ; j++) {
				if (((1 << j) & i) > 0)
					subset.add(set.get(j));
			}
			powerSet.add(subset);
		}
		powerSet.remove(new HashSet<String>());
		powerSet.stream().forEach(s -> powerSetAsStrings.add(s.toString()));
		powerSetAsStrings.stream().forEach(s -> upperSemilatticeButNotTree.addVertex(s));
		for (int i = 0 ; i < powerSet.size() - 1 ; i++) {
			Set<String> iSubset = powerSet.get(i);
			String iSubsetAsString = powerSetAsStrings.get(i);
			for (int j = i + 1 ; j < powerSet.size() ; j++) {
				Set<String> jSubset = powerSet.get(j);
				if (iSubset.containsAll(jSubset))
					upperSemilatticeButNotTree.addEdge(powerSetAsStrings.get(j), iSubsetAsString);
				else if (jSubset.containsAll(iSubset))
					upperSemilatticeButNotTree.addEdge(iSubsetAsString, powerSetAsStrings.get(j));
			}
		}
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(upperSemilatticeButNotTree);
	}
	
	private void setUpUnrootedDAG() {
		doesNotHaveARoot = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		doesNotHaveARoot.addVertex(a);
		doesNotHaveARoot.addVertex(b);
		doesNotHaveARoot.addVertex(c);
		doesNotHaveARoot.addVertex(d);
		doesNotHaveARoot.addVertex(ab);
		doesNotHaveARoot.addVertex(bc);
		doesNotHaveARoot.addVertex(bcd);
		doesNotHaveARoot.addEdge(a, ab);
		doesNotHaveARoot.addEdge(b, ab);
		doesNotHaveARoot.addEdge(b, bc);
		doesNotHaveARoot.addEdge(c, bc);
		doesNotHaveARoot.addEdge(bc, bcd);
		doesNotHaveARoot.addEdge(d, bcd);
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(doesNotHaveARoot);
	}
	
	private void setUpReducedDAGS() {
		classificationTreeReduced = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		Graphs.addAllEdges(classificationTreeReduced, classificationTree, classificationTree.edgeSet());
		TransitiveReduction.INSTANCE.reduce(classificationTreeReduced);
		
		wronglyOrientedClassificationTreeReduced = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		Graphs.addAllEdges(wronglyOrientedClassificationTreeReduced, 
				wronglyOrientedClassificationTree, wronglyOrientedClassificationTree.edgeSet());
		TransitiveReduction.INSTANCE.reduce(wronglyOrientedClassificationTreeReduced);
		
		upperSemilatticeButNotTreeReduced = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		Graphs.addAllEdges(upperSemilatticeButNotTreeReduced, 
				upperSemilatticeButNotTree, upperSemilatticeButNotTree.edgeSet());
		TransitiveReduction.INSTANCE.reduce(upperSemilatticeButNotTreeReduced);
		
		doesNotHaveARootReduced = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		Graphs.addAllEdges(doesNotHaveARootReduced, doesNotHaveARoot, doesNotHaveARoot.edgeSet());
		TransitiveReduction.INSTANCE.reduce(doesNotHaveARootReduced);
	}
	
	private void setUpNotAtomisticDAG() {
		isNotAtomistic = new DirectedAcyclicGraph<>(null, EdgeForTests::new, false);
		String abBIS = "ABbis"; 
		Graphs.addAllVertices(isNotAtomistic, Arrays.asList(new String[] {a, b, c, d, ab, bc, bcd, abcd, abBIS}));
		isNotAtomistic.addEdge(a, abBIS);
		isNotAtomistic.addEdge(a, ab);
		isNotAtomistic.addEdge(b, abBIS);
		isNotAtomistic.addEdge(b, ab);
		isNotAtomistic.addEdge(b, bc);
		isNotAtomistic.addEdge(c, bc);
		isNotAtomistic.addEdge(d, bcd);
		isNotAtomistic.addEdge(abBIS, abcd);
		isNotAtomistic.addEdge(ab, abcd);
		isNotAtomistic.addEdge(bc, bcd);
		isNotAtomistic.addEdge(bcd, abcd);
		TransitiveClosure.INSTANCE.closeDirectedAcyclicGraph(isNotAtomistic);
	}
	

}
