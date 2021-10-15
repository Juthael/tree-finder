package com.tregouet.tree_finder.utils;
import org.jgrapht.graph.DefaultEdge;

public class EdgeForTests extends DefaultEdge {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2533716287256278853L;

	@Override
	public boolean equals(Object o) {
		if (getClass() != o.getClass())
			return false;
		EdgeForTests other = (EdgeForTests) o;
		return (getSource().equals(other.getSource()) && getTarget().equals(other.getTarget()));
	}
	
	@Override
	public String getSource() {
		return super.getSource().toString();
	}
	
	@Override
	public String getTarget() {
		return super.getTarget().toString();
	}	
	
	@Override
	public int hashCode() {
		return getSource().hashCode() + getTarget().hashCode();
	}	

}
