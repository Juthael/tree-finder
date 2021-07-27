package com.tregouet.tree_finder.impl;

import static org.junit.Assert.*;

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
import org.junit.Before;
import org.junit.Test;

import com.tregouet.tree_finder.ITreeFinder;
import com.tregouet.tree_finder.data.InTree;
import com.tregouet.tree_finder.error.InvalidSemiLatticeExeption;
import com.tregouet.tree_finder.error.InvalidTreeException;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

public class TreeFinderTest {

	//toy dataset "One2Seven
	private Integer one = 1;
	private Integer two = 2;
	private Integer three = 3;
	private Integer four = 4;
	private Integer five = 5;
	private Integer six = 6;
	private Integer seven = 7;
	DirectedAcyclicGraph<Integer, Edge> upperSemiLatticeOne2Seven;
	
	//toy dataset "ABC"
	private String a = "A";
	private String b = "B";
	private String c = "C";
	private String ab = "AB";
	private String ac = "AC";
	private String bc = "BC";
	private String abc = "ABC";
	private List<String> verticesABC = new ArrayList<>(Arrays.asList(new String[] {a, b, c, ab, ac, bc, abc}));
	private List<String> leavesABC = new ArrayList<>(Arrays.asList(new String[]{a, b, c}));
	DirectedAcyclicGraph<String, Edge> upperSemiLatticeABC;
	
	//toy dataset "PowerSet"
	DirectedAcyclicGraph<Set<Integer>, Edge> upperSemiLatticePowerSet;
	
	@Before
	public void setUp() throws Exception {
		setUpUpperSemiLatticeOne2Seven();
		setUpperSemiLatticeABC();
	}

	
	//Can last up to 300 sec.
	@Test
	public void whenLargeInputThenTreesStillReturned() throws InvalidSemiLatticeExeption, IOException {
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
	
	@Test
	public void whenTreesRequestedThenAllTreesReturnedAreValid() throws InvalidSemiLatticeExeption {
		boolean allTreesReturnedAreValid = true;
		ITreeFinder<Integer, Edge> finder = new TreeFinder<>(upperSemiLatticeOne2Seven, true);
		while (finder.hasNext()) {
			InTree<Integer, Edge> currTree = finder.next();
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
	public void whenTreesRequestedThenExpectedNbOfTreesReturned() throws InvalidSemiLatticeExeption {
		int nbOfTreesExpected = 12;
		ITreeFinder<Integer, Edge> finder = new TreeFinder<>(upperSemiLatticeOne2Seven, true);
		assertTrue(nbOfTreesExpected == finder.getNbOfTrees());
	}
	
	@Test
	public void whenTreesRequestedThenExpectedTreesReturned() 
			throws InvalidTreeException, InvalidSemiLatticeExeption {
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
	
	private void setUpperSemiLatticeABC() {
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
	
	private void setUpUpperSemiLatticeFromPowerSetOfNElements(int n) throws IOException {
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
	
	private void setUpUpperSemiLatticeOne2Seven() {
		upperSemiLatticeOne2Seven = new DirectedAcyclicGraph<>(null, Edge::new, false);
		upperSemiLatticeOne2Seven.addVertex(one);
		upperSemiLatticeOne2Seven.addVertex(two);
		upperSemiLatticeOne2Seven.addVertex(three);
		upperSemiLatticeOne2Seven.addVertex(four);
		upperSemiLatticeOne2Seven.addVertex(five);
		upperSemiLatticeOne2Seven.addVertex(six);
		upperSemiLatticeOne2Seven.addVertex(seven);
		upperSemiLatticeOne2Seven.addEdge(six,four);
		upperSemiLatticeOne2Seven.addEdge(six,five);
		upperSemiLatticeOne2Seven.addEdge(seven,four);
		upperSemiLatticeOne2Seven.addEdge(seven,five);
		upperSemiLatticeOne2Seven.addEdge(four,two);
		upperSemiLatticeOne2Seven.addEdge(four,three);
		upperSemiLatticeOne2Seven.addEdge(five,two);
		upperSemiLatticeOne2Seven.addEdge(five,three);
		upperSemiLatticeOne2Seven.addEdge(two,one);
		upperSemiLatticeOne2Seven.addEdge(three,one);
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
