package com.tregouet.tree_finder.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.DataFormatException;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.tregouet.tree_finder.utils.StructureInspector;

public class Rooted<V, E> extends AbstractRooted<V, E> {

	private static final long serialVersionUID = -1794073516769685432L;

	/*UNSAFE. The first parameter MUST be a rooted graph, and the effective root must be the second parameter,
	 * effective leaves the third, etc.
	 */
	protected Rooted(DirectedAcyclicGraph<V, E> rooted, V root, Set<V> leaves,
			List<V> topoOrder) {
		super(rooted);
		Graphs.addAllVertices(this, rooted.vertexSet());
		Graphs.addAllEdges(this, rooted, rooted.edgeSet());
		this.root = root;
		this.leaves = leaves;
		if (topoOrder != null)
			this.topologicalSortingOfVertices = topoOrder;
		else {
			topologicalSortingOfVertices = new ArrayList<>();
			new TopologicalOrderIterator<>(rooted).forEachRemaining(topologicalSortingOfVertices::add);
		}
	}

	@Override
	public boolean addAsNewRoot(V element, boolean operateOnTransitiveReduction) {
		boolean added;
		if (operateOnTransitiveReduction) {
			added = addVertex(element);
			if (added) {
				addEdge(element, root);
				root = element;
				topologicalSortingOfVertices.add(0, element);
			}
		}
		else {
			Set<V> elements = vertexSet();
			added = addVertex(element);
			if (added) {
				for (V setElement : elements) {
					addEdge(element, setElement);
				}
				root = element;
				topologicalSortingOfVertices.add(0, element);
			}
		}
		return added;
	}

	@Override
	public boolean removeVertex(V element) {
		if (root.equals(element)) {
			Set<E> rootOutgoingEdges = outgoingEdgesOf(root);
			if (rootOutgoingEdges.size() != 1)
				//otherwise, the DAG wouldn't be rooted anymore
				return false;
			else root = getEdgeSource(rootOutgoingEdges.iterator().next());
		}
		boolean removed = super.removeVertex(element);
		if (removed) {
			topologicalSortingOfVertices.remove(element);
			leaves.remove(element);
		}
		return removed;
	}

	@Override
	public void validate() throws DataFormatException {
		if (!StructureInspector.isARootedDirectedAcyclicGraph(this))
			throw new DataFormatException("ClassificationTree() : parameters do not allow the instantiation "
					+ "of a rooted inverted directed graph.");
	}

}
