package utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.google.common.collect.Sets;
import com.tregouet.tree_finder.data.UpperSemilattice;
import com.tregouet.tree_finder.utils.Functions;

public class RandomUSLGenerator {

	private final Set<String> atoms;
	private final UpperSemilattice<Set<String>, DefaultEdge> powerSet;
	
	public RandomUSLGenerator(int maxNbOfLeaves) {
		atoms = setUpAtoms(maxNbOfLeaves);
		powerSet = setUpPowerSet(atoms);
	}
	
	private UpperSemilattice<Set<String>, DefaultEdge> setUpPowerSet(Set<String> atoms) {
		DirectedAcyclicGraph<Set<String>, DefaultEdge> upperSemiLattice = 
				new DirectedAcyclicGraph<>(null, DefaultEdge::new, false);
		List<Set<String>> vertices = new ArrayList<>(Sets.powerSet(atoms));
		vertices.remove(new HashSet<String>());
		Graphs.addAllVertices(upperSemiLattice, vertices);
		for (int i = 0 ; i < vertices.size() - 1 ; i++) {
			Set<String> iVertex = vertices.get(i);
			for (int j = i + 1 ; j < vertices.size() ; j++) {
				Set<String> jVertex = vertices.get(j);
				if (iVertex.containsAll(jVertex))
					upperSemiLattice.addEdge(jVertex, iVertex);
				else if (jVertex.containsAll(iVertex))
					upperSemiLattice.addEdge(iVertex, jVertex);
			}
		}
		List<Set<String>> topoOrder = new ArrayList<>();
		Set<Set<String>> leaves = new HashSet<>();
		Set<String> root = null;;
		TopologicalOrderIterator<Set<String>, DefaultEdge> topoIte = new TopologicalOrderIterator<>(upperSemiLattice);
		while (topoIte.hasNext()) {
			Set<String> nextElement = topoIte.next();
			topoOrder.add(nextElement);
			if (upperSemiLattice.inDegreeOf(nextElement) == 0)
				leaves.add(nextElement);
			if (upperSemiLattice.outDegreeOf(nextElement) == 0)
				root = nextElement;
		}
		return new UpperSemilattice<Set<String>, DefaultEdge>(upperSemiLattice, root, leaves, topoOrder);
	}
	
	private Set<String> setUpAtoms(int nbOfLeaves) {
		Set<String> leaves = new HashSet<>();
		char leaf = 'A';
		for (int i = 0 ; i <= nbOfLeaves ; i++) {
			leaves.add(new String(new char[] {leaf++}));
		}
		return leaves;
	}
	
	public UpperSemilattice<Set<String>, DefaultEdge> nextRandomUSL() {
		List<Set<String>> elements = new ArrayList<>(powerSet.vertexSet());
		elements.remove(powerSet.getRoot());
		Set<DirectedAcyclicGraph<Set<String>, DefaultEdge>> lowerSets = new HashSet<>();
		for (int i = 0 ; i < powerSet.getLeaves().size() ; i++) {
			lowerSets.add(
					Functions.restriction(
							powerSet, 
							Functions.lowerSet(powerSet, elements.get(new Random().nextInt(elements.size())))));
		}
		DirectedAcyclicGraph<Set<String>, DefaultEdge> randomDAG = 
				new DirectedAcyclicGraph<>(null, DefaultEdge::new, false);
		for(DirectedAcyclicGraph<Set<String>, DefaultEdge> lowerSet : lowerSets) {
			Graphs.addAllVertices(randomDAG, lowerSet.vertexSet());
			Graphs.addAllEdges(randomDAG, lowerSet, lowerSet.edgeSet());
		}
		Set<Set<String>> randomDAGMaxima = Functions.maxima(randomDAG);
		randomDAG.addVertex(powerSet.getRoot());
		for (Set<String> element : randomDAGMaxima) {
			randomDAG.addEdge(element, powerSet.getRoot());
		}
		Set<Set<String>> leaves = randomDAG.vertexSet().stream()
				.filter(e -> randomDAG.inDegreeOf(e) == 0)
				.collect(Collectors.toSet());
		List<Set<String>> topoOrder = new ArrayList<>();
		new TopologicalOrderIterator<>(randomDAG).forEachRemaining(topoOrder::add);
		return new UpperSemilattice<Set<String>, DefaultEdge>(randomDAG, powerSet.getRoot(), leaves, topoOrder);
	}

}
