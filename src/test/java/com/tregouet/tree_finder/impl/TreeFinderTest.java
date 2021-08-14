package com.tregouet.tree_finder.impl;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.InTree;
import com.tregouet.tree_finder.error.InvalidSemiLatticeException;
import com.tregouet.tree_finder.error.InvalidTreeException;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

public class TreeFinderTest {

	//toy dataset "One2Seven
	private static Integer one = 1;
	private static Integer two = 2;
	private static Integer three = 3;
	private static Integer four = 4;
	private static Integer five = 5;
	private static Integer six = 6;
	private static Integer seven = 7;
	private static DirectedAcyclicGraph<Integer, Edge> notConnectedDAG;
	private static DirectedAcyclicGraph<Integer, Edge> notRootedDAG;
	private static DirectedAcyclicGraph<Integer, Edge> notInvertedDAG;
	
	//toy dataset "ABC"
	private static String a = "A";
	private static String b = "B";
	private static String c = "C";
	private static String ab = "AB";
	private static String ac = "AC";
	private static String bc = "BC";
	private static String abc = "ABC";
	private static List<String> verticesABC = new ArrayList<>(Arrays.asList(new String[] {a, b, c, ab, ac, bc, abc}));
	private static List<String> leavesABC = new ArrayList<>(Arrays.asList(new String[]{a, b, c}));
	private static DirectedAcyclicGraph<String, Edge> upperSemiLatticeABC;
	
	//toy dataset "PowerSet"
	private static DirectedAcyclicGraph<Set<Integer>, Edge> upperSemiLatticePowerSet;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		setUpNotConnectedDAG();
		setUpNotRootedDAG();
		setUpNotInvertedDAG();
		setUpperSemiLatticeABC();
	}

	
	//Can last up to 300 sec.
	@Test
	public void whenLargeInputThenTreesStillReturned() throws InvalidSemiLatticeException, IOException {
		@SuppressWarnings("unused")
		int nbOfTreesReturned = 0;
		int nbOfAtomsInPowerSet = 5;
		setUpUpperSemiLatticeFromPowerSetOfNElements(nbOfAtomsInPowerSet);
		ITreeFinder<Set<Integer>, Edge> finder = new TreeFinder<>(upperSemiLatticePowerSet, true);
		//System.out.println("Nb of trees to be found : " + finder.getNbOfTrees());
		while (finder.hasNext()) {
			finder.next();
			/*
			nbOfTreesReturned++;
			if (nbOfTreesReturned % 10000 == 0)
				System.out.println(nbOfTreesReturned + "trees returned");
			*/
		}
	}
	
	@SuppressWarnings("unused")
	@Test
	public void whenParameterIsNotARootedInvertedDAGThenExceptionThrownWithSafeConstructor() throws IOException {
		boolean exceptionThrownWithNotConnectedDAG = false;
		boolean exceptionThrownWithNotRootedDAG = false;
		boolean exceptionThrownWithNotInvertedDAG = false;
		boolean exceptionThrownWithABC = false;
		boolean exceptionThrownWithPowerSet = false;		
		ITreeFinder<Integer, Edge> treeFinderNotConnected;
		ITreeFinder<Integer, Edge> treeFinderNotRooted;
		ITreeFinder<Integer, Edge> treeFinderNotInverted;
		ITreeFinder<String, Edge> treeFinderABC;
		ITreeFinder<Set<Integer>, Edge> treeFinderPowerSet;
		setUpUpperSemiLatticeFromPowerSetOfNElements(4);
		try {
			treeFinderNotConnected = new TreeFinder<>(notConnectedDAG, true);
		}
		catch (Exception e) {
			exceptionThrownWithNotConnectedDAG = true;
		}
		try {
			treeFinderNotRooted = new TreeFinder<>(notRootedDAG, true);
		}
		catch (InvalidSemiLatticeException e) {
			exceptionThrownWithNotRootedDAG = true;
		}
		try {
			treeFinderNotInverted = new TreeFinder<>(notInvertedDAG, true);
		}
		catch (InvalidSemiLatticeException e) {
			exceptionThrownWithNotInvertedDAG = true;
		}
		try {
			treeFinderABC = new TreeFinder<>(upperSemiLatticeABC, true);
		}
		catch (InvalidSemiLatticeException e) {
			exceptionThrownWithABC = true;
		}
		try {
			treeFinderPowerSet = new TreeFinder<>(upperSemiLatticePowerSet, true);
		}
		catch (InvalidSemiLatticeException e) {
			exceptionThrownWithPowerSet = true;
		}
		assertTrue(exceptionThrownWithNotConnectedDAG 
				&& exceptionThrownWithNotRootedDAG
				&& exceptionThrownWithNotInvertedDAG
				&& !exceptionThrownWithABC 
				&& !exceptionThrownWithPowerSet);
	}
	
	@Test
	public void whenTreesRequestedThenAllTreesReturnedAreValid() throws InvalidSemiLatticeException {
		boolean allTreesReturnedAreValid = true;
		ITreeFinder<String, Edge> finder = new TreeFinder<>(upperSemiLatticeABC, true);
		while (finder.hasNext()) {
			InTree<String, Edge> currTree = finder.next();
			try {
				currTree.validate();
			}
			catch (InvalidTreeException e) {
				allTreesReturnedAreValid = false;
			}
		}
		assertTrue(allTreesReturnedAreValid);
	}
	
	@Test
	public void whenTreesRequestedThenExpectedNbOfTreesReturned() throws InvalidSemiLatticeException {
		int nbOfTreesExpected = 8;
		ITreeFinder<String, Edge> finder = new TreeFinder<>(upperSemiLatticeABC, true);
		assertTrue(nbOfTreesExpected == finder.getNbOfTrees());
	}
	
	@Test
	public void whenTreesRequestedThenExpectedTreesReturned() 
			throws InvalidTreeException, InvalidSemiLatticeException {
		ITreeFinder<String, Edge> finder = new TreeFinder<>(upperSemiLatticeABC, true);
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
	
	@SuppressWarnings("unused")
	private void printGraph(Graph<Set<Integer>, Edge> graph) throws IOException {
		//Get file name and path
		String fileName;
		String graphPath;
		System.out.print("Enter the name of the graph file :");
		Scanner sc = new Scanner(System.in);
		fileName = sc.nextLine();
		System.out.println(System.lineSeparator() + "Enter a location for the graph file : ");
		System.out.println("Ex : D:\\\\ProjetDocs\\\\essais_viz\\\\");
		graphPath = sc.nextLine();
		sc.close();
		System.out.println(System.lineSeparator() + "Got it.");
		//convert in DOT format
		DOTExporter<Set<Integer>, Edge> exporter = new DOTExporter<>();
		exporter.setVertexAttributeProvider((v) -> {
			Map<String, Attribute> map = new LinkedHashMap<>();
			map.put("label", DefaultAttribute.createAttribute(v.toString()));
			return map;
		});
		Writer writer = new StringWriter();
		exporter.exportGraph(graph, writer);
		String stringDOT = writer.toString();
		/* TO SEE DOT FILE : 
		 * System.out.println(stringDOT);
		 */
		MutableGraph dotGraph = new Parser().read(stringDOT);
		Graphviz.fromGraph(dotGraph).width(graph.vertexSet().size()*100)
			.render(Format.PNG).toFile(new File(graphPath + fileName));
	}
	
	private InTree<String, Edge> setN1() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.removeVertex(bc);
		nArg.addEdge(a, ab);
		nArg.addEdge(b, ab);
		nArg.addEdge(c, ac);
		nArg.addEdge(ab, abc);
		nArg.addEdge(ac, abc);
		return new InTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}
	
	private InTree<String, Edge> setN2() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.removeVertex(ac);
		nArg.addEdge(a, ab);
		nArg.addEdge(b, ab);
		nArg.addEdge(c, bc);
		nArg.addEdge(ab, abc);
		nArg.addEdge(bc, abc);
		return new InTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}
	
	
	
	private InTree<String, Edge> setN3() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.addEdge(a, ab);
		nArg.addEdge(b, bc);
		nArg.addEdge(c, ac);
		nArg.addEdge(ab, abc);
		nArg.addEdge(ac, abc);
		nArg.addEdge(bc, abc);
		return new InTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}
	
	private InTree<String, Edge> setN4() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.removeVertex(ac);
		nArg.addEdge(a, ab);
		nArg.addEdge(b, bc);
		nArg.addEdge(c, bc);
		nArg.addEdge(ab, abc);
		nArg.addEdge(bc, abc);
		return new InTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}
	
	private InTree<String, Edge> setN5() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.removeVertex(bc);
		nArg.addEdge(a, ac);
		nArg.addEdge(b, ab);
		nArg.addEdge(c, ac);
		nArg.addEdge(ab, abc);
		nArg.addEdge(ac, abc);
		return new InTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}	
	
	private InTree<String, Edge> setN6() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.addEdge(a, ac);
		nArg.addEdge(b, ab);
		nArg.addEdge(c, bc);
		nArg.addEdge(ab, abc);
		nArg.addEdge(ac, abc);
		nArg.addEdge(bc, abc);
		return new InTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}
	
	private InTree<String, Edge> setN7() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.removeVertex(ab);
		nArg.addEdge(a, ac);
		nArg.addEdge(b, bc);
		nArg.addEdge(c, ac);
		nArg.addEdge(ac, abc);
		nArg.addEdge(bc, abc);
		return new InTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}
	
	private InTree<String, Edge> setN8() throws InvalidTreeException {
		DirectedAcyclicGraph<String, Edge> nArg = new DirectedAcyclicGraph<>(null, Edge::new, false);
		Graphs.addAllVertices(nArg, verticesABC);
		nArg.removeVertex(ab);
		nArg.addEdge(a, ac);
		nArg.addEdge(b, bc);
		nArg.addEdge(c, bc);
		nArg.addEdge(ac, abc);
		nArg.addEdge(bc, abc);
		return new InTree<String, Edge>(abc, leavesABC, nArg, nArg.edgeSet(), true);
	}
	
	private static void setUpperSemiLatticeABC() {
		upperSemiLatticeABC = new DirectedAcyclicGraph<>(null, Edge::new, false);
		upperSemiLatticeABC.addVertex(a);
		upperSemiLatticeABC.addVertex(b);
		upperSemiLatticeABC.addVertex(c);
		upperSemiLatticeABC.addVertex(ab);
		upperSemiLatticeABC.addVertex(ac);
		upperSemiLatticeABC.addVertex(bc);
		upperSemiLatticeABC.addVertex(abc);
		upperSemiLatticeABC.addEdge(a, ab);
		upperSemiLatticeABC.addEdge(a, ac);
		upperSemiLatticeABC.addEdge(b, ab);
		upperSemiLatticeABC.addEdge(b, bc);
		upperSemiLatticeABC.addEdge(c, ac);
		upperSemiLatticeABC.addEdge(c, bc);
		upperSemiLatticeABC.addEdge(ab, abc);
		upperSemiLatticeABC.addEdge(ac, abc);
		upperSemiLatticeABC.addEdge(bc, abc);
	}
	
	private static void setUpUpperSemiLatticeFromPowerSetOfNElements(int n) throws IOException {
		List<Set<Integer>> powerSet = new ArrayList<>();
		//build power set
		int[] atoms = new int[n];
		for (int i = 0 ; i < n ; i++) {
			atoms[i] = i;
		}
		
		for (int i = 0 ; i < (1 << n) ; i++) {
			Set<Integer> subset = new HashSet<Integer>();
			for (int j = 0 ; j < n ; j++) {
				if (((1 << j) & i) > 0)
					subset.add(atoms[j]);
			}
			powerSet.add(subset);
		}
		//remove empty set
		powerSet.remove(new HashSet<Integer>());
		//build graph
		upperSemiLatticePowerSet = new DirectedAcyclicGraph<>(null, Edge::new, false);
		for (Set<Integer> subset : powerSet)
			upperSemiLatticePowerSet.addVertex(subset);
		for (int i = 0 ; i < powerSet.size() - 1 ; i++) {
			for (int j = i+1 ; j < powerSet.size() ; j++) {
				if (powerSet.get(j).containsAll(powerSet.get(i)))
					upperSemiLatticePowerSet.addEdge(powerSet.get(i), powerSet.get(j));
				else if (powerSet.get(i).containsAll(powerSet.get(j)))
					upperSemiLatticePowerSet.addEdge(powerSet.get(j), powerSet.get(i));
			}
		}
		TransitiveReduction.INSTANCE.reduce(upperSemiLatticePowerSet);
		//print graph
		//printGraph(upperSemiLatticePowerSet);
	}
	
	private static void setUpNotConnectedDAG() {
		notConnectedDAG = new DirectedAcyclicGraph<>(null, Edge::new, false);
		notConnectedDAG.addVertex(one);
		notConnectedDAG.addVertex(two);
		notConnectedDAG.addVertex(three);
		notConnectedDAG.addVertex(four);
		notConnectedDAG.addVertex(five);
		notConnectedDAG.addVertex(six);
		notConnectedDAG.addVertex(seven);
		notConnectedDAG.addEdge(seven,four);
		notConnectedDAG.addEdge(seven,five);
		notConnectedDAG.addEdge(four,two);
		notConnectedDAG.addEdge(four,three);
		notConnectedDAG.addEdge(five,two);
		notConnectedDAG.addEdge(five,three);
		notConnectedDAG.addEdge(two,one);
		notConnectedDAG.addEdge(three,one);
	}
	
	private static void setUpNotRootedDAG() {
		notRootedDAG = new DirectedAcyclicGraph<>(null, Edge::new, false);
		notRootedDAG.addVertex(two);
		notRootedDAG.addVertex(three);
		notRootedDAG.addVertex(four);
		notRootedDAG.addVertex(five);
		notRootedDAG.addVertex(six);
		notRootedDAG.addVertex(seven);
		notRootedDAG.addEdge(six,four);
		notRootedDAG.addEdge(six,five);
		notRootedDAG.addEdge(seven,four);
		notRootedDAG.addEdge(seven,five);
		notRootedDAG.addEdge(four,two);
		notRootedDAG.addEdge(four,three);
		notRootedDAG.addEdge(five,two);
		notRootedDAG.addEdge(five,three);
	}	
	
	private static void setUpNotInvertedDAG() {
		notInvertedDAG = new DirectedAcyclicGraph<>(null, Edge::new, false);
		notInvertedDAG.addVertex(one);
		notInvertedDAG.addVertex(two);
		notInvertedDAG.addVertex(three);
		notInvertedDAG.addVertex(four);
		notInvertedDAG.addVertex(five);
		notInvertedDAG.addVertex(six);
		notInvertedDAG.addVertex(seven);
		notInvertedDAG.addEdge(four, six);
		notInvertedDAG.addEdge(five, six);
		notInvertedDAG.addEdge(four, seven);
		notInvertedDAG.addEdge(five, seven);
		notInvertedDAG.addEdge(two, four);
		notInvertedDAG.addEdge(three, four);
		notInvertedDAG.addEdge(two, five);
		notInvertedDAG.addEdge(three, five);
		notInvertedDAG.addEdge(one, two);
		notInvertedDAG.addEdge(one, three);
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
		return super.getSource().toString();
	}
	
	@Override
	public int hashCode() {
		return getSource().hashCode() + getTarget().hashCode();
	}
	
}
